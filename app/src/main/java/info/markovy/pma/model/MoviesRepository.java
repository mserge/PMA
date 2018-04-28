package info.markovy.pma.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.markovy.pma.BuildConfig;
import info.markovy.pma.model.data.MovieContract;
import info.markovy.pma.model.data.UIMovie;
import info.markovy.pma.model.data.UIMovieDBImpl;
import info.markovy.pma.model.data.UIMovieStored;
import info.markovy.pma.model.data.UIMoviesList;
import info.markovy.pma.model.data.UIMoviesListResultPageImpl;
import info.markovy.pma.model.data.UIMoviesListStored;
import info.markovy.pma.viewmodel.ShowModes;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;

/**
 * Created by mserge on 10.03.2018.
 */

public class MoviesRepository {
    private static final String TAG = "MoviesRepository";
    private static final String LANG = "en";
    private static MoviesRepository instance;
    private static List<UIMovie> favorites;
    // TODO remove this singleton
    public static synchronized MoviesRepository getInstance() {
        if(instance == null){
            instance = new MoviesRepository();
            favorites = new ArrayList<>();
        }
        return instance;
    }

    public LiveData<UIMoviesList> geMovies(MutableLiveData<ShowModes> state, ContentResolver cr) {
        final MutableLiveData<UIMoviesList> data = new MutableLiveData<>();
        // TODO implement caching
        new AsyncTask<Void, Void, UIMoviesList>(){

            @Override
            protected void onPostExecute(UIMoviesList moviesResult) {
                data.setValue(moviesResult);
            }

            @Override
            protected UIMoviesList doInBackground(Void... voids) {
                final String apiKey = BuildConfig.API_KEY;
                Log.d(TAG, "Using API key" + apiKey);

                try {
                    TmdbMovies movies;
                    UIMoviesList results ;
                    switch (state.getValue()) {
                        case POPULAR:
                            movies = new TmdbApi(apiKey).getMovies();
                            // TODO Implement proxy
                            results = new UIMoviesListResultPageImpl(movies.getPopularMovies(LANG, 0));
                            break;
                        case TOP:
                            movies = new TmdbApi(apiKey).getMovies();
                            results = new UIMoviesListResultPageImpl(movies.getTopRatedMovies(LANG, 0));
                            break;
                        case STARRED:
                            // TODO implement starred
                            results = new UIMoviesListStored(LoadFavorites(cr));
                            break;
                        default:
                                results = null;
                                break;
                    }
                    Log.d(TAG, results != null ? "Total is: " + results.getTotalResults() : "Empty");
                    return results;
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                    return null;
                }
            }

        }.execute();

        return data;
    }
    // TODO replace to LIveData
    public LiveData<MovieDb> getMovie(UIMovie movie) {
        final MutableLiveData<MovieDb> data = new MutableLiveData<>();
        if(movie instanceof UIMovieDBImpl) {
           data.setValue(((UIMovieDBImpl) movie).getDblink());
        }
        //  implement load for POJO
        if(movie instanceof  UIMovieStored){
            new AsyncTask<Integer, Void, MovieDb>(){
                @Override
                protected void onPostExecute(MovieDb movieDb) {
                    data.setValue(movieDb);
                }

                @Override
                protected MovieDb doInBackground(Integer... ids) {
                    try {
                        final String apiKey = BuildConfig.API_KEY;
                        TmdbMovies movies = new TmdbApi(apiKey).getMovies();
                        return movies.getMovie(ids[0], LANG);
                    } catch (Exception e){
                        Log.e(TAG, "Exception when getMovie " + e.getMessage());
                        return null;
                    }
                }
            }.execute(movie.getId());
        }
        return data;
    }

    public void addFavoriteMovie(MovieDb value, ContentResolver cr) {
        //TODO check for duplicates when adding and implement ContentProvider
        //favorites.add(new UIMovieStored(value.getTitle(), value.getPosterPath(), value.getId()));

        ContentValues values = new ContentValues();
        values.put(MovieContract.MoviesEntry.COLUMN_ID, value.getId());
        values.put(MovieContract.MoviesEntry.COLUMN_TITLE, value.getTitle());
        values.put(MovieContract.MoviesEntry.COLUMN_POSTER, value.getPosterPath());
        values.put(MovieContract.MoviesEntry.COLUMN_DATE, new Date().getTime());
        new AsyncTask<ContentValues, Void, Uri>(){
            @Override
            protected void onPostExecute(Uri uri) {
                if(uri!=null){
                    Log.d(TAG, uri.toString());
                } else {
                    Log.d(TAG, "Null URI returned");
                }
            }

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                return cr.insert(MovieContract.MoviesEntry.CONTENT_URI, contentValues[0]);
            }
        }.execute(values);
    }

    public static List<UIMovie> LoadFavorites(ContentResolver cr){
        Cursor cursor = cr.query(MovieContract.MoviesEntry.CONTENT_URI,
                null,
                null,
                null,
                MovieContract.MoviesEntry.COLUMN_DATE);
        if (cursor == null) {
            // error - log some message
        }
        else if (cursor.getCount() < 1) {
            // nothing to show  - log some message
            Log.d(TAG, "no entries");
        }
        else {
            favorites = new ArrayList<>();
            while(cursor.moveToNext()){
                Log.d(TAG, "Loaded id " + cursor.getInt(cursor.getColumnIndex(MovieContract.MoviesEntry.COLUMN_ID)));
                UIMovieStored uiMovieStored = new UIMovieStored(
                        cursor.getString(cursor.getColumnIndex(MovieContract.MoviesEntry.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(MovieContract.MoviesEntry.COLUMN_POSTER)),
                        cursor.getInt(cursor.getColumnIndex(MovieContract.MoviesEntry.COLUMN_ID))
                );
                favorites.add(uiMovieStored);
            }
        }
        cursor.close();
        return favorites;
    }
}

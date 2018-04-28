package info.markovy.pma.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import info.markovy.pma.BuildConfig;
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

    public LiveData<UIMoviesList> geMovies(MutableLiveData<ShowModes> state) {
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
                    TmdbMovies movies = new TmdbApi(apiKey).getMovies();
                    UIMoviesList results ;
                    switch (state.getValue()) {
                        case POPULAR:
                            // TODO Implement proxy
                            results = new UIMoviesListResultPageImpl(movies.getPopularMovies(LANG, 0));
                            break;
                        case TOP:
                            results = new UIMoviesListResultPageImpl(movies.getTopRatedMovies(LANG, 0));
                            break;
                        case STARRED:
                            // TODO implement starred
                            results = new UIMoviesListStored(favorites);
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
                    final String apiKey = BuildConfig.API_KEY;
                    TmdbMovies movies = new TmdbApi(apiKey).getMovies();
                    return movies.getMovie(ids[0], LANG);

                }
            }.execute(movie.getId());
        }
        return data;
    }

    public void addFavoriteMovie(MovieDb value) {
        //TODO check for duplicates when adding and implement ContentProvider
        favorites.add(new UIMovieStored(value.getTitle(), value.getPosterPath(), value.getId()));
    }
}

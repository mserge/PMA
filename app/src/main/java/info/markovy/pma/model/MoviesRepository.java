package info.markovy.pma.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import info.markovy.pma.BuildConfig;
import info.markovy.pma.model.data.UIMovie;
import info.markovy.pma.model.data.UIMovieDBImpl;
import info.markovy.pma.model.data.UIMoviesList;
import info.markovy.pma.model.data.UIMoviesListResultPageImpl;
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
    // TODO remove this singleton
    public static synchronized MoviesRepository getInstance() {
        if(instance == null){
            instance = new MoviesRepository();
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
                            results = null;
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
    public MovieDb getMovie(UIMovie movie) {
        if(movie instanceof UIMovieDBImpl) {
            return ((UIMovieDBImpl) movie).getDblink();
        }
        // TODO implement load for POJO
        return null;
    }
}

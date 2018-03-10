package info.markovy.pma.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import info.markovy.pma.BuildConfig;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

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

    public LiveData<MovieResultsPage> geMovies(MutableLiveData<Boolean> state) {
        final MutableLiveData<MovieResultsPage> data = new MutableLiveData<>();
        // TODO implement caching
        new AsyncTask<Void, Void, MovieResultsPage>(){

            @Override
            protected void onPostExecute(MovieResultsPage moviesResult) {
                data.setValue(moviesResult);
            }

            @Override
            protected MovieResultsPage doInBackground(Void... voids) {
                final String apiKey = BuildConfig.API_KEY;
                Log.d(TAG, "Using API key" + apiKey);

                try {
                    TmdbMovies movies = new TmdbApi(apiKey).getMovies();
                    MovieResultsPage results;
                    if(state.getValue().booleanValue()){
                        results = movies.getPopularMovies(LANG, 0);
                    } else
                    {
                        results = movies.getTopRatedMovies(LANG, 0);
                    }

                    Log.d(TAG, "Total is: " + results.getTotalResults());
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
}

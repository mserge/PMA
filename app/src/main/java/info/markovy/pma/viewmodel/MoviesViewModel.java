package info.markovy.pma.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

import info.markovy.pma.model.MoviesRepository;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

/**
 * Created by mserge on 10.03.2018.
 */

public class MoviesViewModel extends ViewModel {
    private MutableLiveData<ShowModes> mCurrentState = new MutableLiveData<ShowModes>();;
    // TODO make injection via some other way
    final private MoviesRepository mRepository = MoviesRepository.getInstance();
    //TODO implement Loading/Error handling via https://developer.android.com/topic/libraries/architecture/guide.html#addendum
    public final LiveData<MovieResultsPage> mMovies =
            // TODO 1 - change to
            Transformations.switchMap(mCurrentState, (state) -> {
                return mRepository.geMovies(mCurrentState);
            });


    public void setCurrentMovie(MovieDb movie) {
        currentMovie.setValue(movie);
    }

    public MutableLiveData<MovieDb> getCurrentMovie() {
        return currentMovie;
    }

    private  MutableLiveData<MovieDb> currentMovie = new MutableLiveData<>();

    public LiveData<MovieResultsPage> getMovies() {
        return mMovies;
    }

    public void setState(ShowModes state){
        mCurrentState.setValue(state);
    }

    public MutableLiveData<ShowModes> getState() {
        return mCurrentState;
    }
 }

package info.markovy.pma.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import info.markovy.pma.model.MoviesRepository;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;


/**
 * Created by mserge on 10.03.2018.
 */

public class MoviesViewModel extends ViewModel {
    private MutableLiveData<Boolean> mCurrentState = new MutableLiveData<Boolean>();;
    // TODO make injection via some other way
    final private MoviesRepository mRepository = MoviesRepository.getInstance();

    public final LiveData<MovieResultsPage> mMovies =
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

    public void setState(boolean state){
        mCurrentState.setValue(new Boolean(state));
    }

    public void revertState(){
        mCurrentState.setValue(!mCurrentState.getValue());
    }

    public MutableLiveData<Boolean> getState() {
        return mCurrentState;
    }
 }

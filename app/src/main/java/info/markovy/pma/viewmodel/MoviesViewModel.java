package info.markovy.pma.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import info.markovy.pma.model.MoviesRepository;
import info.markovy.pma.model.data.UIMovie;
import info.markovy.pma.model.data.UIMoviesList;
import info.movito.themoviedbapi.model.MovieDb;


/**
 * Created by mserge on 10.03.2018.
 */

public class MoviesViewModel extends AndroidViewModel {
    private MutableLiveData<ShowModes> mCurrentState = new MutableLiveData<ShowModes>();;
    // TODO make injection via some other way
    final private MoviesRepository mRepository = MoviesRepository.getInstance();
    //TODO implement Loading/Error handling via https://developer.android.com/topic/libraries/architecture/guide.html#addendum
    public final LiveData<UIMoviesList> mMovies =
            Transformations.switchMap(mCurrentState, (state) -> {
                return mRepository.geMovies(mCurrentState, getApplication().getContentResolver());
            });

    public MoviesViewModel(@NonNull Application application) {
        super(application);
    }


    public void setCurrentMovie(UIMovie movie) {
        if(movie != null)
             mRepository.getMovie(movie, currentMovie);
        else
            currentMovie.setValue(null);
    }

    public LiveData<MovieDb> getCurrentMovie() {
        return currentMovie;
    }

    private MutableLiveData<MovieDb> currentMovie = new MutableLiveData<>();

    public LiveData<UIMoviesList> getMovies() {
        return mMovies;
    }

    public void setState(ShowModes state){
        mCurrentState.setValue(state);
        currentMovie.setValue(null);
    }

    public MutableLiveData<ShowModes> getState() {
        return mCurrentState;
    }

    public void setCurrentFavorite() {
        mRepository.addFavoriteMovie(currentMovie.getValue(), getApplication().getContentResolver());
    }
}

package info.markovy.pma.viewmodel;

import com.github.vivchar.rendererrecyclerviewadapter.ViewModel;

import info.movito.themoviedbapi.model.Reviews;

public class MoviesDetailReview implements ViewModel {
    private final Reviews mReview;

    public MoviesDetailReview(Reviews review) {
        mReview = review;
    }


    public String getAuthor() {
        return mReview.getAuthor();
    }


    public String getContent() {
        return mReview.getContent();
    }

    public String getURL() {
        return mReview.getUrl();
    }
}

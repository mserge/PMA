package info.markovy.pma.model.data;

import java.util.List;

public class UIMoviesListStored implements UIMoviesList {


    private final List<UIMovie> mStored;

    public UIMoviesListStored(List<UIMovie> favorites) {
        mStored = favorites;
    }

    @Override
    public List<UIMovie> getResults() {
        return mStored;
    }

    @Override
    public int getTotalResults() {
        return mStored.size();
    }
}

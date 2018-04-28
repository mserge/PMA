package info.markovy.pma.model.data;

import java.util.ArrayList;
import java.util.List;

import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

public class UIMoviesListResultPageImpl implements UIMoviesList {
    MovieResultsPage impl;

    public UIMoviesListResultPageImpl(MovieResultsPage impl) {
        this.impl = impl;
    }

    @Override
    public List<UIMovie> getResults() {
        List<UIMovie> copy = new ArrayList<>();
        for (MovieDb db:
             impl.getResults()) {
            copy.add(new UIMovieDBImpl(db));
        }
        return copy;
    }

    @Override
    public int getTotalResults() {
        return 0;
    }
}

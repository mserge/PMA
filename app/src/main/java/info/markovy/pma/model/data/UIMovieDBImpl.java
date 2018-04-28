package info.markovy.pma.model.data;

import info.movito.themoviedbapi.model.MovieDb;

public class UIMovieDBImpl implements UIMovie {
    MovieDb dblink;



    public UIMovieDBImpl(MovieDb db) {
        dblink = db;
    }

    @Override
    public String getTitle() {
        return dblink.getTitle();
    }

    @Override
    public String getPosterPath() {
        return dblink.getPosterPath();
    }


    public MovieDb getDblink() {
        return dblink;
    }


    @Override
    public int getId() {
        return dblink.getId();
    }


}

package info.markovy.pma.model.data;

import info.movito.themoviedbapi.model.MovieDb;

public class UIMovieDBImpl implements UIMovie {
    MovieDb dblink;
    boolean isStarred = false;

    public UIMovieDBImpl(MovieDb dblink, boolean isStarred ) {
        this.dblink = dblink;
        this.isStarred = isStarred;
    }

    public UIMovieDBImpl(MovieDb db) {
        dblink = db;
        isStarred = false;
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

    @Override
    public boolean isStarred() {
        return isStarred;
    }
}

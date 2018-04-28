package info.markovy.pma.model.data;

public class UIMovieStored implements UIMovie {
    private String mTitle;
    private String mPosterPath;
    private int mId;

    public UIMovieStored(String mTitle, String mPosterPath, int mId) {
        this.mTitle = mTitle;
        this.mPosterPath = mPosterPath;
        this.mId = mId;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getPosterPath() {
        return mPosterPath;
    }

    @Override
    public int getId() {
        return mId;
    }
}

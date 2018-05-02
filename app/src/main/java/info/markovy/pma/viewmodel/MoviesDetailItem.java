package info.markovy.pma.viewmodel;

import com.github.vivchar.rendererrecyclerviewadapter.ViewModel;

public class MoviesDetailItem implements ViewModel {
    int mSubjectResource;
    Object mDetailObject;

    public MoviesDetailItem(int mSubjectResource, Object mDetailObject) {
        this.mSubjectResource = mSubjectResource;
        this.mDetailObject = mDetailObject;
    }

    public int getSubjectResource() {
        return mSubjectResource;
    }

    public Object getDetailObject() {
        return mDetailObject;
    }
}


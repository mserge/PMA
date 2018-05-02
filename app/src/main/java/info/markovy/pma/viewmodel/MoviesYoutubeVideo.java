package info.markovy.pma.viewmodel;

import com.github.vivchar.rendererrecyclerviewadapter.ViewModel;

import info.movito.themoviedbapi.model.Video;

public class MoviesYoutubeVideo implements ViewModel {

    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    private Video mVideo;

    public MoviesYoutubeVideo(Video mVideo) {
        this.mVideo = mVideo;
    }

    public String getURL() {
        return YOUTUBE_URL + mVideo.getKey();
    }

    public String getTypeSizeLabel () {
        return mVideo.getType() + " (" + mVideo.getSize().toString() + "p)";
    }

    public  String getName() {return mVideo.getName(); }
}

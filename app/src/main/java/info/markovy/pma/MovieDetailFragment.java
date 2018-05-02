package info.markovy.pma;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.vivchar.rendererrecyclerviewadapter.RendererRecyclerViewAdapter;
import com.github.vivchar.rendererrecyclerviewadapter.ViewModel;
import com.github.vivchar.rendererrecyclerviewadapter.ViewRenderer;
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewBinder;
import com.github.vivchar.rendererrecyclerviewadapter.binder.ViewFinder;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import info.markovy.pma.model.data.UIMovie;
import info.markovy.pma.viewmodel.MoviesDetailItem;
import info.markovy.pma.viewmodel.MoviesDetailReview;
import info.markovy.pma.viewmodel.MoviesViewModel;
import info.markovy.pma.viewmodel.MoviesYoutubeVideo;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.Reviews;
import info.movito.themoviedbapi.model.Video;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or top fragment
 * on handsets.
 */
public class MovieDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    private static final String TAG = "MovieDetailFragment";
    private static final CharSequence YOUTUBE = "YouTube";

    MoviesViewModel viewModel;
    private RendererRecyclerViewAdapter mRecyclerViewAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(getActivity()).get(MoviesViewModel.class);
        observeViewModel(viewModel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.detail_toolbar);

        if(toolbar!= null) toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
               getActivity().onBackPressed();
            }
        });
        Button button = rootView.findViewById(R.id.btn_set_favorite);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.setCurrentFavorite();
            }
        });
        mRecyclerViewAdapter = new RendererRecyclerViewAdapter();
        mRecyclerViewAdapter.registerRenderer(getYourViewBinder());
        mRecyclerViewAdapter.registerRenderer(getReviewViewBinder());
        mRecyclerViewAdapter.registerRenderer(getVideoViewBinder());

        final RecyclerView recyclerView = rootView.findViewById(R.id.movie_details_list);
        recyclerView.setAdapter(mRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return rootView;
    }
   // https://image.tmdb.org/t/p/w1400_and_h450_face/2SEgJ0mHJ7TSdVDbkGU061tR33K.jpg
    public String getURL(String posterPath) {
        return "https://image.tmdb.org/t/p/w1400_and_h450_face/" + posterPath;
    }
    private void observeViewModel(MoviesViewModel viewModel) {
        // Update the list when the data changes

        viewModel.getCurrentMovie().observe(this, new Observer<MovieDb>() {
            @Override
            public void onChanged(@Nullable MovieDb movie) {
                Activity activity = getActivity();
                CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
                View container = activity.findViewById(R.id.movie_detail_container);
                if (movie != null) {
                    if(container != null) container.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Recieved movie:" + movie.toString());

                    if (appBarLayout != null) {
                        appBarLayout.setTitle(movie.getTitle());
                    }
                    ImageView backDropImage = (ImageView) getView().findViewById(R.id.backdrop);
                    Picasso.get().load(getURL(movie.getBackdropPath())).into(backDropImage);
                    // Show the dummy content as text in a TextView.
                    mRecyclerViewAdapter.setItems(getMovieDetailItems(movie));
                    mRecyclerViewAdapter.notifyDataSetChanged();

                } else {
                    Log.d(TAG, "No movie loaded");
                    if (appBarLayout != null) {
                        appBarLayout.setTitle(getString(R.string.no_data_available));
                    }
                    if(container!=null) container.setVisibility(View.GONE);

                }
            }
        });
    }

    private List<ViewModel> getMovieDetailItems(MovieDb movie) {
        List<ViewModel>  list = new ArrayList<>();


        list.add(new MoviesDetailItem(-1, movie.getOverview()));
        list.add(new MoviesDetailItem(R.string.movie_detail_rating_text, movie.getVoteAverage()));
        list.add(new MoviesDetailItem(R.string.movie_detail_release_text, movie.getReleaseDate()));

        if( movie.getReviews() != null) {
            list.add(new MoviesDetailItem(-1, getString(R.string.available_reviews)));
            Log.d(TAG, "Reviews: " + movie.getReviews().size());
            for(Reviews review: movie.getReviews()){
                list.add(new MoviesDetailReview(review));
            }
        } else {
            list.add(new MoviesDetailItem(-1, "No Reviews"));
        }
        if( movie.getVideos() != null) {
            Log.d(TAG, "Trailers: " + movie.getVideos().size());
            list.add(new MoviesDetailItem(R.string.videos_available, movie.getVideos().size() ));
            for(Video video: movie.getVideos()){
                if(StringUtils.equals(YOUTUBE, video.getSite())){
                    list.add(new MoviesYoutubeVideo(video));
                }
            }
        }

        return  list;
    }

    private ViewRenderer getYourViewBinder() {
        return new ViewBinder<>(
                R.layout.movie_detail_item, /* your item layout */
                MoviesDetailItem.class, /* your model class */
                new ViewBinder.Binder<MoviesDetailItem>() {
                    @Override
                    public void bindView(@NonNull MoviesDetailItem model, @NonNull ViewFinder finder, @NonNull List<Object> payloads) {
                        finder
                                .setText(R.id.movie_detail_text, model.getSubjectResource() == -1
                                        ? (String) model.getDetailObject() : getString(model.getSubjectResource(), model.getDetailObject()));
                    }
                }
        );
    }
    private ViewRenderer getReviewViewBinder() {
        return new ViewBinder<>(
                R.layout.item_reviews, /* your item layout */
                MoviesDetailReview.class, /* your model class */
                new ViewBinder.Binder<MoviesDetailReview>() {
                    @Override
                    public void bindView(@NonNull MoviesDetailReview model, @NonNull ViewFinder finder, @NonNull List<Object> payloads) {
                        finder
                                .setText(R.id.review_tv_name, model.getAuthor())
                                .setText(R.id.review_tv_content, model.getContent())
                                .setOnClickListener(R.id.review_button, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String url = model.getURL();
                                        try {
                                            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                            startActivity(myIntent);
                                        } catch (ActivityNotFoundException e) {
                                             Toast.makeText(getActivity(), "No application can handle this request."  + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
                                            e.printStackTrace();
                                        }
                                    }
                                });
;
                    }
                }
        );
    }

    private ViewRenderer getVideoViewBinder() {
        return new ViewBinder<>(
                R.layout.item_videos, /* your item layout */
                MoviesYoutubeVideo.class, /* your model class */
                new ViewBinder.Binder<MoviesYoutubeVideo>() {
                    @Override
                    public void bindView(@NonNull MoviesYoutubeVideo model, @NonNull ViewFinder finder, @NonNull List<Object> payloads) {
                        finder
                                .setText(R.id.video_tv_name, model.getName())
                                .setText(R.id.video_tv_type, model.getTypeSizeLabel())
                                .setOnClickListener(R.id.video_button, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        String url = model.getURL();
                                        try {
                                            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                            startActivity(myIntent);
                                        } catch (ActivityNotFoundException e) {
                                             Toast.makeText(getActivity(), "No application can handle this request."  + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
                                            e.printStackTrace();
                                        }
                                    }
                                });
;
                    }
                }
        );
    }
}

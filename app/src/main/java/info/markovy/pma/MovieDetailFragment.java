package info.markovy.pma;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Movie;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import info.markovy.pma.model.data.UIMovie;
import info.markovy.pma.viewmodel.MoviesViewModel;
import info.movito.themoviedbapi.model.MovieDb;

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

    MoviesViewModel viewModel;

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
                if (movie != null) {

                    Log.d(TAG, "Recieved movie:" + movie.toString());

                    Activity activity = getActivity();
                    CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
                    if (appBarLayout != null) {
                        appBarLayout.setTitle(movie.getTitle());
                    }
                    ImageView backDropImage = (ImageView) getView().findViewById(R.id.backdrop);
                    Picasso.get().load(getURL(movie.getBackdropPath())).into(backDropImage);
                    // Show the dummy content as text in a TextView.
                    ((TextView) getView().findViewById(R.id.movie_detail)).setText(movie.getOverview());
                    ((TextView) getView().findViewById(R.id.movie_rating)).setText(getString(R.string.movie_detail_rating_text, movie.getVoteAverage()));
                    ((TextView) getView().findViewById(R.id.movie_release_date)).setText(getString(R.string.movie_detail_release_text, movie.getReleaseDate()));
                }
            }
        });
    }
}

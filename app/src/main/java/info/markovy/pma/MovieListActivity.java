package info.markovy.pma;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;


import info.markovy.pma.viewmodel.MoviesViewModel;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailFragment} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity {

    private static final String TAG = "MovieListActivity";
    private static final java.lang.String KEY_STATE = "KEY_STATE";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private MoviesViewModel viewModel;
    private MoviesPageRecyclerViewAdapter adapter;
    private SwitchCompat switchCompat;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_STATE, viewModel.getState().getValue());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        View recyclerView = findViewById(R.id.movie_list);
        assert recyclerView != null;
        viewModel = ViewModelProviders.of(this).get(MoviesViewModel.class);
        observeViewModel(viewModel);
        boolean bInitialState = false;
        if(savedInstanceState!=null){
            bInitialState = savedInstanceState.getBoolean(KEY_STATE, false);
        }
        viewModel.setState(bInitialState);
        setupRecyclerView((RecyclerView) recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.switch_mode);
        item.setActionView(R.layout.switch_layout);
        switchCompat = item.getActionView().findViewById(R.id.switchForActionBar);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "Clicked " + b);
                viewModel.setState(b);
            }
        });
        return true;
    }

    private void observeViewModel(MoviesViewModel viewModel) {
        // Update the list when the data changes
        viewModel.getMovies().observe(this, new Observer<MovieResultsPage>() {
            @Override
            public void onChanged(@Nullable MovieResultsPage results) {
                if (results != null) {
                    //…
                    //projectAdapter.setProjectList(projects);
                    adapter.setMovieResults(results);
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Recieved results:" + results.toString());
                }
            }
        });
        viewModel.getState().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if(switchCompat!= null) switchCompat.setChecked(aBoolean);
                if(aBoolean){
                    setTitle(getString(R.string.title_popular));
                } else{
                    setTitle(getString(R.string.title_top_rated));
                }

            }
        });
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new MoviesPageRecyclerViewAdapter(this, viewModel.getMovies().getValue());
        recyclerView.setAdapter(adapter);
    }

    private void onMovieSelect(View view, MovieDb movie) {
        viewModel.setCurrentMovie(movie);
        MovieDetailFragment fragment = new MovieDetailFragment();
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public static class MoviesPageRecyclerViewAdapter
            extends RecyclerView.Adapter<MoviesPageRecyclerViewAdapter.ViewHolder> {

        private final MovieListActivity mParentActivity;

        public void setMovieResults(MovieResultsPage mPage) {
            this.mPage = mPage;
        }

        private MovieResultsPage mPage;

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MovieDb movie = (MovieDb) view.getTag();
                mParentActivity.onMovieSelect(view, movie);
            }
        };

        MoviesPageRecyclerViewAdapter(MovieListActivity parent,
                                      MovieResultsPage page) {
            mPage = page;
            mParentActivity = parent;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if(mPage != null && mPage.getResults() != null) {
                MovieDb movie = mPage.getResults().get(position);
                holder.mIdView.setText(String.valueOf(movie.getId()));
                holder.mContentView.setText(movie.getTitle());

                holder.itemView.setTag(movie);
                holder.itemView.setOnClickListener(mOnClickListener);
            } else {
                Log.d(TAG, "Empty results set.");
            }
        }

        @Override
        public int getItemCount() {
            return mPage!= null && mPage.getResults() != null ? mPage.getResults().size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
            }
        }
    }
}

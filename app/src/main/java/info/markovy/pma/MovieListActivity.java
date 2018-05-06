package info.markovy.pma;

import android.app.Fragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import info.markovy.pma.model.data.UIMovie;
import info.markovy.pma.viewmodel.MoviesViewModel;
import info.markovy.pma.viewmodel.ShowModes;
import info.movito.themoviedbapi.model.MovieDb;
import info.markovy.pma.model.data.UIMoviesList;

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
    private static final String KEY_POSITION = "KEY_POSITION";
    private static final String BACKSTACK_TAG = "BACKSTACK_TAG";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private MoviesViewModel viewModel;
    private MoviesPageRecyclerViewAdapter adapter;
    private Spinner switchCompat;
    private TextView txtNoData;
    private RecyclerView.LayoutManager layoutManager;
    private boolean initSelection = false;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(layoutManager !=null)
           outState.putParcelable(KEY_POSITION, layoutManager.onSaveInstanceState());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        txtNoData = findViewById(R.id.txt_nodata);
        View recyclerView = findViewById(R.id.movie_list);
        assert recyclerView != null;
        viewModel = ViewModelProviders.of(this).get(MoviesViewModel.class);
        observeViewModel(viewModel);
        setupRecyclerView((RecyclerView) recyclerView);
        if(viewModel.getState().getValue() == null ) { // first run - set default mode
                viewModel.getState().setValue(ShowModes.POPULAR);
        }
        if(savedInstanceState!=null){
            Parcelable parcelable = savedInstanceState.getParcelable(KEY_POSITION);
            if(parcelable != null && layoutManager !=null)
                layoutManager.onRestoreInstanceState(parcelable);
        }

        if(viewModel.getCurrentMovie().getValue() != null){
            navigateMovie();
        }
        // listen for backstack change as proposed at https://developer.android.com/training/implementing-navigation/temporal
        getSupportFragmentManager().addOnBackStackChangedListener(
                  new FragmentManager.OnBackStackChangedListener() {
                      public void onBackStackChanged() {
                          // Update your UI here.
                          android.support.v4.app.Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(BACKSTACK_TAG);
                          if(fragmentByTag!=null){
                              Log.d(TAG, "Fragment tag found");
                          } else if(!mTwoPane){
                              Log.d(TAG, "Fragment not found, cleaning Viewmodel");
                            viewModel.setCurrentMovie(null); // clear model
                          }
                      }
                  });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.switch_mode);
        item.setActionView(R.layout.switch_layout);
        switchCompat = item.getActionView().findViewById(R.id.spinner);
        switchCompat.setSelection(viewModel.getState().getValue().getValue());
        switchCompat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Note that list should match enums from ShowModes
                // first selection after restart should be ignored once (it's triggered after init)
                if (initSelection) {
                    Log.d(TAG, "Selected " + i);
                    viewModel.setState(ShowModes.from(i));

                } else {
                    Log.d(TAG, "Selection ignored " + i);
                    initSelection = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return true;
    }

    private void observeViewModel(MoviesViewModel viewModel) {
        // Update the list when the data changes
        viewModel.getMovies().observe(this, new Observer<UIMoviesList>() {
            @Override
            public void onChanged(@Nullable UIMoviesList results) {
                if (results != null &&  results.getResults() != null &&  results.getResults().size() > 0) {
                    txtNoData.setVisibility(View.GONE);
                    if(mTwoPane && viewModel.getCurrentMovie().getValue() == null){
                        // TODO may be we should select at scrolled position??
                        onMovieSelect( results.getResults().get(0));
                    }
                } else {
                    // no data recieved
                    txtNoData.setVisibility(View.VISIBLE);
                }
                adapter.setMovieResults(results);
                adapter.notifyDataSetChanged();
            }
        });
        viewModel.getState().observe(this, new Observer<ShowModes>() {
            @Override
            public void onChanged(@Nullable ShowModes mode) {
                Log.d(TAG, "New mode selected" + mode.toString());
                // if state changed we change our Spinner
                if(switchCompat!= null) switchCompat.setSelection(mode.getValue());
                // and we scroll to top (as no sense to keep old position)
                if(layoutManager!=null) layoutManager.scrollToPosition(0);
            }
        });
        viewModel.getCurrentMovie().observe(this, new Observer<MovieDb>() {
            @Override
            public void onChanged(@Nullable MovieDb movie) {
                Log.d(TAG, " Activity Movie changed to " +( movie == null ? "empty" : movie.getTitle()));
              if(movie != null){
                    Log.d(TAG, "New movie value recieved");
              } else {
                  Log.d(TAG, "Empty value recieved");
                   if (!mTwoPane) getSupportFragmentManager().popBackStack();
              }
            }
        });
    }

    private void navigateMovie() {
        MovieDetailFragment fragment = new MovieDetailFragment();
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, fragment, BACKSTACK_TAG)
                    .addToBackStack(BACKSTACK_TAG)
                    .commit();
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new MoviesPageRecyclerViewAdapter(this, viewModel.getMovies().getValue());
        recyclerView.setHasFixedSize(true);
        int viewWidth = recyclerView.getMeasuredWidth();
        Log.d(TAG, "Width is measured to: " + String.valueOf(viewWidth));
        layoutManager = new GridAutofitLayoutManager(getApplicationContext(), 320);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void onMovieSelect(UIMovie movie) {
        viewModel.setCurrentMovie(movie);
        navigateMovie();
    }

    public static class MoviesPageRecyclerViewAdapter
            extends RecyclerView.Adapter<MoviesPageRecyclerViewAdapter.ViewHolder> {

        private final MovieListActivity mParentActivity;

        public void setMovieResults(UIMoviesList mPage) {
            this.mPage = mPage;
        }

        private UIMoviesList mPage;

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIMovie movie = (UIMovie) view.getTag();
                mParentActivity.onMovieSelect(movie);
            }
        };

        MoviesPageRecyclerViewAdapter(MovieListActivity parent,
                                      UIMoviesList page) {
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
                UIMovie movie = mPage.getResults().get(position);
                //holder.mIdView.setText(String.valueOf(movie.getId()));
                holder.mContentView.setText(movie.getTitle());
                Picasso.get().load(getURL(movie.getPosterPath())).into(holder.mImageView);
                holder.itemView.setTag(movie);
                holder.itemView.setOnClickListener(mOnClickListener);
            } else {
                Log.d(TAG, "Empty results set.");
            }
        }

                // https://image.tmdb.org/t/p/w185_and_h278_bestv2/zvQqY4ksXtB6HEjjewsH3DQzI6g.jpg
        public String getURL(String posterPath) {
            return "https://image.tmdb.org/t/p/w185_and_h278_bestv2/" + posterPath;
        }

        @Override
        public int getItemCount() {
            return mPage!= null && mPage.getResults() != null ? mPage.getResults().size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mIdView;
            final TextView mContentView;
            final ImageView mImageView;

            ViewHolder(View view) {
                super(view);
                mIdView = (TextView) view.findViewById(R.id.id_text);
                mContentView = (TextView) view.findViewById(R.id.content);
                mImageView = view.findViewById(R.id.image_poster);
            }
        }
    }

    public class GridAutofitLayoutManager extends GridLayoutManager
    {
        private int mColumnWidth;
        private boolean mColumnWidthChanged = true;

        public GridAutofitLayoutManager(Context context, int columnWidth)
        {
        /* Initially set spanCount to 1, will be changed automatically later. */
            super(context, 1);
            setColumnWidth(checkedColumnWidth(context, columnWidth));
        }

        public GridAutofitLayoutManager(Context context, int columnWidth, int orientation, boolean reverseLayout)
        {
        /* Initially set spanCount to 1, will be changed automatically later. */
            super(context, 1, orientation, reverseLayout);
            setColumnWidth(checkedColumnWidth(context, columnWidth));
        }

        private int checkedColumnWidth(Context context, int columnWidth)
        {
            if (columnWidth <= 0)
            {
            /* Set default columnWidth value (48dp here). It is better to move this constant
            to static constant on top, but we need context to convert it to dp, so can't really
            do so. */
                columnWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                        context.getResources().getDisplayMetrics());
            }
            return columnWidth;
        }

        public void setColumnWidth(int newColumnWidth)
        {
            if (newColumnWidth > 0 && newColumnWidth != mColumnWidth)
            {
                mColumnWidth = newColumnWidth;
                mColumnWidthChanged = true;
            }
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state)
        {
            int width = getWidth();
            int height = getHeight();
            if (mColumnWidthChanged && mColumnWidth > 0 && width > 0 && height > 0)
            {
                int totalSpace;
                if (getOrientation() == VERTICAL)
                {
                    totalSpace = width - getPaddingRight() - getPaddingLeft();
                }
                else
                {
                    totalSpace = height - getPaddingTop() - getPaddingBottom();
                }
                int spanCount = Math.max(1, totalSpace / mColumnWidth);
                setSpanCount(spanCount);
                mColumnWidthChanged = false;
            }
            super.onLayoutChildren(recycler, state);
        }
    }
}

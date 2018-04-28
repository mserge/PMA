package info.markovy.pma.model.data;


import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


/**
     * This class serves as the ContentProvider for all of Movies App's data. This class allows us to
     * Insert data, query  data.
     * <p>
     * Although ContentProvider implementation requires the implementation of additional methods to
     * perform single inserts, updates, and the ability to get the type of the data from a URI.
     * However, here, they are not implemented for the sake of brevity and simplicity. If you would
     * like, you may implement them on your own. However, we are not going to be teaching how to do
     * so in this course.
     */
public class MoviesProvider  extends ContentProvider {


        private static final int CODE_MOVIES = 1;
        private MovieDBHelper mOpenHelper;
        private static final UriMatcher sUriMatcher = buildUriMatcher();


        /**
         * Creates the UriMatcher that will match each URI to the CODE_WEATHER and
         * CODE_WEATHER_WITH_DATE constants defined above.
         * <p>
         * It's possible you might be thinking, "Why create a UriMatcher when you can use regular
         * expressions instead? After all, we really just need to match some patterns, and we can
         * use regular expressions to do that right?" Because you're not crazy, that's why.
         * <p>
         * UriMatcher does all the hard work for you. You just have to tell it which code to match
         * with which URI, and it does the rest automagically. Remember, the best programmers try
         * to never reinvent the wheel. If there is a solution for a problem that exists and has
         * been tested and proven, you should almost always use it unless there is a compelling
         * reason not to.
         *
         * @return A UriMatcher that correctly matches the constants for CODE_WEATHER and CODE_WEATHER_WITH_DATE
         */
        public static UriMatcher buildUriMatcher() {

            /*
             * All paths added to the UriMatcher have a corresponding code to return when a match is
             * found. The code passed into the constructor of UriMatcher here represents the code to
             * return for the root URI. It's common to use NO_MATCH as the code for this case.
             */
            final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
            final String authority = MovieContract.CONTENT_AUTHORITY;

            /*
             * For each type of URI you want to add, create a corresponding code. Preferably, these are
             * constant fields in your class so that you can use them throughout the class and you no
             * they aren't going to change. In Movies App, we use CODE_WEATHER or CODE_WEATHER_WITH_DATE.
             */

            /* This URI is content://com.example.android.sunshine/weather/ */
            matcher.addURI(authority, MovieContract.PATH_MOVIES, CODE_MOVIES);


            return matcher;
        }

        /**
         * In onCreate, we initialize our content provider on startup. This method is called for all
         * registered content providers on the application main thread at application launch time.
         * It must not perform lengthy operations, or application startup will be delayed.
         *
         * Nontrivial initialization (such as opening, upgrading, and scanning
         * databases) should be deferred until the content provider is used (via {@link #query},
         * {@link #bulkInsert(Uri, ContentValues[])}, etc).
         *
         * Deferred initialization keeps application startup fast, avoids unnecessary work if the
         * provider turns out not to be needed, and stops database errors (such as a full disk) from
         * halting application launch.
         *
         * @return true if the provider was successfully loaded, false otherwise
         */
        @Override
        public boolean onCreate() {
            /*
             * As noted in the comment above, onCreate is run on the main thread, so performing any
             * lengthy operations will cause lag in your app. Since DbHelper's constructor is
             * very lightweight, we are safe to perform that initialization here.
             */
            mOpenHelper = new MovieDBHelper(getContext());
            return true;
        }

        /**
         * Handles query requests from clients. We will use this method in Movies App to query for all
         * of our weather data as well as to query for the weather on a particular day.
         *
         * @param uri           The URI to query
         * @param projection    The list of columns to put into the cursor. If null, all columns are
         *                      included.
         * @param selection     A selection criteria to apply when filtering rows. If null, then all
         *                      rows are included.
         * @param selectionArgs You may include ?s in selection, which will be replaced by
         *                      the values from selectionArgs, in order that they appear in the
         *                      selection.
         * @param sortOrder     How the rows in the cursor should be sorted.
         * @return A Cursor containing the results of the query. In our implementation,
         */
        @Override
        public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                            String[] selectionArgs, String sortOrder) {

            Cursor cursor;

            /*
             * Here's the switch statement that, given a URI, will determine what kind of request is
             * being made and query the database accordingly.
             */
            switch (sUriMatcher.match(uri)) {
                case CODE_MOVIES:
                    cursor = mOpenHelper.getReadableDatabase().query(
                            MovieContract.MoviesEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);

                break;

                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }

            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }

        /**
         * In Movies App, we aren't going to do anything with this method.
         *
         * @param uri the URI to query.
         * @return nothing in Movies App, but normally a MIME type string, or null if there is no type.
         */
        @Override
        public String getType(@NonNull Uri uri) {
            throw new RuntimeException("We are not implementing getType in Movies App.");
        }


        @Override
        public Uri insert(@NonNull Uri uri, ContentValues values) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            long _id = -1;
            if(sUriMatcher.match(uri) == CODE_MOVIES) {

                  db.beginTransaction();
                    int rowsInserted = 0;
                    try {

                      _id = db.insert(MovieContract.MoviesEntry.TABLE_NAME, null, values);
                     db.setTransactionSuccessful();
                 } finally {
                    db.endTransaction();
                    }
            }
         return _id >= 0 ? MovieContract.MoviesEntry.buildMoviesPathWithId(_id) : null;
        }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        throw new RuntimeException("We are not implementing delete in Movies app.");
    }

    @Override
        public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            throw new RuntimeException("We are not implementing update in Movies App");
        }

        /**
         * You do not need to call this method. This is a method specifically to assist the testing
         * framework in running smoothly. You can read more at:
         * http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
         */
        @Override
        @TargetApi(11)
        public void shutdown() {
            mOpenHelper.close();
            super.shutdown();
        }
    }
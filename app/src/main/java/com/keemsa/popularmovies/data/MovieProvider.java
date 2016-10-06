package com.keemsa.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.NotifyInsert;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by sebastian on 10/2/16.
 */
@ContentProvider(
        authority = MovieProvider.AUTHORITY,
        database = MovieDatabase.class,
        packageName = "com.keemsa.popularmovies.provider")
public final class MovieProvider {
    public static final String AUTHORITY = "com.keemsa.popularmovies";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String MOVIE = "movie";
        String TRAILER = "trailer";
        String REVIEW = "review";
    }

    private static Uri builderUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = MovieDatabase.MOVIE)
    public static class Movie {
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + Path.MOVIE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + Path.MOVIE;

        @ContentUri(
                path = Path.MOVIE,
                type = CONTENT_DIR_TYPE,
                defaultSort = MovieColumns._ID + " ASC"
        )
        public static final Uri ALL = builderUri(Path.MOVIE);

        @InexactContentUri(
                path = Path.MOVIE + "/#",
                name = "WITH_ID",
                type = CONTENT_ITEM_TYPE,
                whereColumn = MovieColumns._ID,
                pathSegment = 1
        )
        public static Uri withId(long id) {
            return builderUri(Path.MOVIE, String.valueOf(id));
        }

        @NotifyInsert(paths = Path.MOVIE)
        public static Uri[] onInsert(ContentValues values) {
            final long movieId = values.getAsLong(MovieColumns._ID);
            return new Uri[]{withId(movieId)};
        }
    }

    @TableEndpoint(table = MovieDatabase.TRAILER)
    public static class Trailer {
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + Path.TRAILER;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + Path.TRAILER;

        @InexactContentUri(
                path = Path.MOVIE + "/#/" + Path.TRAILER,
                name = "OF_MOVIE",
                type = CONTENT_DIR_TYPE,
                whereColumn = TrailerColumns.MOVIE_ID,
                pathSegment = 2
        )
        public static Uri ofMovie(long movie_id) {
            return builderUri(Path.MOVIE, String.valueOf(movie_id), Path.TRAILER);
        }

        @InexactContentUri(
                path = Path.TRAILER + "/*",
                name = "WITH_ID",
                type = CONTENT_ITEM_TYPE,
                whereColumn = TrailerColumns._ID,
                pathSegment = 1
        )
        public static Uri withId(String id) {
            return builderUri(Path.TRAILER, id);
        }

        @ContentUri(
                path = Path.TRAILER,
                type = CONTENT_DIR_TYPE
        )
        public static final Uri ALL = builderUri(Path.TRAILER);

    }

    @TableEndpoint(table = MovieDatabase.REVIEW)
    public static class Review {
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + Path.REVIEW;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + Path.REVIEW;

        @InexactContentUri(
                path = Path.MOVIE + "/#/" + Path.REVIEW,
                name = "OF_MOVIE",
                type = CONTENT_DIR_TYPE,
                whereColumn = ReviewColumns.MOVIE_ID,
                pathSegment = 2
        )
        public static Uri ofMovie(long movie_id) {
            return builderUri(Path.MOVIE, String.valueOf(movie_id), Path.REVIEW);
        }

        @InexactContentUri(
                path = Path.REVIEW + "/*",
                name = "WITH_ID",
                type = CONTENT_ITEM_TYPE,
                whereColumn = ReviewColumns._ID,
                pathSegment = 1
        )
        public static Uri withId(String id) {
            return builderUri(Path.REVIEW, id);
        }

        @ContentUri(
                path = Path.REVIEW,
                type = CONTENT_DIR_TYPE
        )
        public static final Uri ALL = builderUri(Path.REVIEW);
    }
}

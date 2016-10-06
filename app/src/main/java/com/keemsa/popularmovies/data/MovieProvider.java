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

        @ContentUri(
                path = Path.MOVIE,
                type = CONTENT_DIR_TYPE,
                defaultSort = MovieColumns._ID + " ASC"
        )
        public static final Uri CONTENT_URI = builderUri(Path.MOVIE);

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + Path.MOVIE;

        @InexactContentUri(
                path = Path.MOVIE + "/#",
                name = "MOVIE_ID",
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
}

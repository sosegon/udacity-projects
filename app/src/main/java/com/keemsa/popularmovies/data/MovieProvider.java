package com.keemsa.popularmovies.data;

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
        String MOVIES = "movies";
    }

    private static Uri builderUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }


    @TableEndpoint(table = MovieDatabase.MOVIE)
    public static class Movies {
        @ContentUri(
                path = Path.MOVIES,
                type = "vnd.android.cursor.dir/movie",
                defaultSort = MovieColumns.RATING + " ASC"
        )
        public static final Uri CONTENT_URI = builderUri(Path.MOVIES);

        @InexactContentUri(
                path = Path.MOVIES + "/#",
                name = "MOVIE_ID",
                type = "vnd.android.cursor.item/movie",
                whereColumn = MovieColumns._ID,
                pathSegment = 1
        )
        public static Uri withId(long id) {
            return builderUri(Path.MOVIES, String.valueOf(id));
        }

        @NotifyInsert(paths = Path.MOVIES)
        public static Uri[] onInsert(ContentValues values) {
            final long movieId = values.getAsLong(MovieColumns._ID);
            return new Uri[]{withId(movieId)};
        }
    }
}

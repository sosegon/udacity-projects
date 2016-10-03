package com.keemsa.popularmovies.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by sebastian on 10/2/16.
 */
@Database(
        version = MovieDatabase.VERSION,
        packageName = "com.keemsa.popularmovies.provider"
)
public class MovieDatabase {
    public static final int VERSION = 1;

    @Table(MovieColums.class)
    public static final String MOVIE = "movie";
}

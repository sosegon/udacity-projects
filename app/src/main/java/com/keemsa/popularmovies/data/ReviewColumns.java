package com.keemsa.popularmovies.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by sebastian on 10/4/16.
 */
public interface ReviewColumns {
    @DataType(TEXT)
    @PrimaryKey
    String _ID = "_id";

    @DataType(TEXT)
    @NotNull
    String AUTHOR = "author";

    @DataType(TEXT)
    @NotNull
    String CONTENT = "CONTENT";

    @DataType(TEXT)
    @NotNull
    String URL = "URL";

    @DataType(INTEGER)
    @References(table = MovieDatabase.MOVIE, column = MovieColumns._ID)
    String MOVIE_ID = "movie_id";
}

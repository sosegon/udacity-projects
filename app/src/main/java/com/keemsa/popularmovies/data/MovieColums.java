package com.keemsa.popularmovies.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.REAL;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by sebastian on 10/1/16.
 */
public interface MovieColums {

    @DataType(INTEGER)
    @PrimaryKey
    @AutoIncrement
    String _ID = "_id";
    @DataType(TEXT)
    @NotNull
    String TITLE = "title";
    @DataType(TEXT)
    String SYNOPSIS = "synopsis";
    @DataType(TEXT)
    @NotNull
    String POSTER_URL = "poster_url";
    @DataType(INTEGER)
    @NotNull
    String RELEASE_DATE = "release_date";
    @DataType(REAL)
    String RATING = "rating";

}

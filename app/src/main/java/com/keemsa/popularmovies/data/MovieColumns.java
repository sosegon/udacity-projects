package com.keemsa.popularmovies.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.REAL;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by sebastian on 10/1/16.
 */
public interface MovieColumns {
    @DataType(INTEGER)
    @PrimaryKey
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

    /*
       This field determines in which kind of query the movie was retrieved
       (by popularity or by rating) and if the movie is a favourite one.
       Instead of having boolean fields for query by popularity, query by
       rating, and favourite, an integer between 0 and 7 is used. Its value
       is determined from a binary table:

       Rating | Popularity | Favourite | Query_type | Possible
         0         0            0           0            No
         0         0            1           1            No
         0         1            0           2            Yes
         0         1            1           3            Yes
         1         0            0           4            Yes
         1         0            1           5            Yes
         1         1            0           6            Yes
         1         1            1           7            Yes

       The table is self descriptive, but it's worth to mention
       that the field Possible means that those values are not
       allowed. 0 and 1 are not possible because a movie must be
       retrieved by popularity or by query.
     */
    @DataType(INTEGER)
    @NotNull
    String QUERY_TYPE = "query_type";
}

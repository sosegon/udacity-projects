package com.keemsa.popularmovies;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by sebastian on 10/4/16.
 */
public class UtilityUnitTest {

    @Test
    public void dateInMilliseconds() throws Exception {
        String date = "2016-01-01";
        long dateMilliseconds = Utility.getDateInMilliSeconds(date);
        assertEquals(dateMilliseconds, 1451606400000L);

    }
}

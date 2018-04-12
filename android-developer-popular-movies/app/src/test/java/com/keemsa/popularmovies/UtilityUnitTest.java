package com.keemsa.popularmovies;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void formatPosterUrl() throws Exception {
        String posterUrl = "/ioeijdjksj";
        posterUrl = Utility.formatPosterUrl(posterUrl);
        assertTrue(posterUrl.equals("ioeijdjksj"));

        posterUrl = Utility.formatPosterUrl(posterUrl);
        assertTrue(posterUrl.equals("ioeijdjksj"));
    }

    @Test
    public void createQueryType() throws Exception {
        int type = Utility.createQueryType(true, false, true);
        assertEquals(type, 5);
    }
}

package com.sam_chordas.android.stockhawk;

import com.sam_chordas.android.stockhawk.rest.Utils;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * Created by sebastian on 11/27/16.
 */
public class UtilsUnitTest {

  @Test
  public void getCurrentDate() {
    String cDate = Utils.getCurrentDate();
    //assertTrue(cDate.equals("2016-11-27"));
  }

  @Test
  public void getPreviousWeekDate(){
    String date = "2016-11-27";
    String prevWeek = Utils.getPreviousWeekDate(date);

    assertTrue(prevWeek.equals("2016-11-20"));
  }

  @Test
  public void getPreviousMonthDate(){
    String date = "2016-11-27";
    String prevMonth = Utils.getPreviousMonthDate(date);

    assertTrue(prevMonth.equals("2016-10-28"));
  }

  @Test
  public void getPreviousYearDate(){
    String date = "2016-11-27";
    String prevYear = Utils.getPreviousYearDate(date);

    assertTrue(prevYear.equals("2015-11-28"));
  }


}

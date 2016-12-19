package com.sam_chordas.android.stockhawk;

import com.sam_chordas.android.stockhawk.rest.Utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
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

  @Test
  public void isValidStockSymbol(){
    String s1 = "YSDHS";
    String s2 = "23123";
    String s3 = "WE323";
    String s4 = "WEWE-";
    String s5 = "2133~";
    String s6 = "SDÈSD";
    String s7 = "SD SD";
    String s8 = "";
    String s9 = null;

    assertEquals(Utils.isValidStockSymbol(s1), true);
    assertEquals(Utils.isValidStockSymbol(s2), true);
    assertEquals(Utils.isValidStockSymbol(s3), true);
    assertEquals(Utils.isValidStockSymbol(s4), false);
    assertEquals(Utils.isValidStockSymbol(s5), false);
    assertEquals(Utils.isValidStockSymbol(s6), false);
    assertEquals(Utils.isValidStockSymbol(s7), false);
    assertEquals(Utils.isValidStockSymbol(s8), false);
    assertEquals(Utils.isValidStockSymbol(s9), false);
  }
}

package com.sam_chordas.android.stockhawk.data;

/**
 * Created by sebastian on 11/21/16.
 */
public interface Projections {
  String STOCK[] = {
          QuoteColumns._ID,
          QuoteColumns.BIDPRICE,
          QuoteColumns.CHANGE,
          QuoteColumns.CREATED,
          QuoteColumns.ISCURRENT,
          QuoteColumns.ISUP,
          QuoteColumns.PERCENT_CHANGE,
          QuoteColumns.SYMBOL
  };

  int STOCK_ID = 0,
      STOCK_BIDPRICE = 1,
      STOCK_CHANGE = 2,
      STOCK_CREATED = 3,
      STOCK_ISCURRENT = 4,
      STOCK_ISUP = 5,
      STOCK_PERCENT_CHANGE = 6,
      STOCK_SYMBOL = 7;
}

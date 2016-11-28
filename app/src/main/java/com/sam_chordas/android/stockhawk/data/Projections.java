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

  String HISTORIC[] = {
          PriceColumns._ID,
          PriceColumns.PRICE,
          PriceColumns.DATE,
          PriceColumns.QUOTE_ID
  };

  int HISTORIC_ID = 0,
      HISTORIC_PRICE = 1,
      HISTORIC_DATE = 2,
      HISTORIC_QUOTE_ID = 3;
}

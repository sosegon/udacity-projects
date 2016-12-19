package com.sam_chordas.android.stockhawk;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by sebastian on 11/23/16.
 */
public interface AppStatus {

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
          STOCK_STATUS_UNKNOWN,
          STOCK_STATUS_OK,
          STOCK_STATUS_NO_CONNECTION,
          STOCK_STATUS_NO_RESPONSE,
          STOCK_STATUS_INVALID_DATA,
          STOCK_STATUS_INVALID_STOCK,
          STOCK_STATUS_ENCODING_ERROR,
          STOCK_STATUS_DATABASE_ERROR,
          STOCK_STATUS_NO_DATA,
          STOCK_STATUS_INCOMPLETE_STOCK_DATA
  })
  @interface StockStatus {}

  int STOCK_STATUS_UNKNOWN = 0,
      STOCK_STATUS_OK = 1,
      STOCK_STATUS_NO_CONNECTION = 2,
      STOCK_STATUS_NO_RESPONSE = 3,
      STOCK_STATUS_INVALID_DATA = 4,
      STOCK_STATUS_INVALID_STOCK = 5,
      STOCK_STATUS_ENCODING_ERROR = 6,
      STOCK_STATUS_DATABASE_ERROR = 7,
      STOCK_STATUS_NO_DATA = 8,
      STOCK_STATUS_INCOMPLETE_STOCK_DATA = 9;
}

package com.sam_chordas.android.stockhawk;

/**
 * Created by sebastian on 11/24/16.
 */
public class InvalidStockException extends Exception {

  public InvalidStockException(String detailMessage) {
    super(detailMessage);
  }
}

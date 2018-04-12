package com.sam_chordas.android.stockhawk;

/**
 * Created by sebastian on 11/24/16.
 */
public class NonExistingStockException extends Exception {

  public NonExistingStockException(String detailMessage) {
    super(detailMessage);
  }
}

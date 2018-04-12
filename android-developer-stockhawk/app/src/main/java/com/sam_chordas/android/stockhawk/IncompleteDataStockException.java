package com.sam_chordas.android.stockhawk;

/**
 * Created by sebastian on 12/19/16.
 */
public class IncompleteDataStockException extends Exception {
  public IncompleteDataStockException(String detailMessage) {
    super(detailMessage);
  }
}

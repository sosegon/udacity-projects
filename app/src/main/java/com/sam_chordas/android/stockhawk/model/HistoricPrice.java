package com.sam_chordas.android.stockhawk.model;

/**
 * Created by sebastian on 11/30/16.
 */
public class HistoricPrice {
  private int stockId;
  private long date;
  private float price;
  private String symbol;

  public HistoricPrice(String symbol, int stockId, long date, float price) {
    this.symbol = symbol;
    this.stockId = stockId;
    this.date = date;
    this.price = price;
  }

  public int getStockId() {
    return stockId;
  }

  public void setStockId(int stockId) {
    this.stockId = stockId;
  }

  public long getDate() {
    return date;
  }

  public void setDate(long date) {
    this.date = date;
  }

  public float getPrice() {
    return price;
  }

  public void setPrice(float price) {
    this.price = price;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }
}

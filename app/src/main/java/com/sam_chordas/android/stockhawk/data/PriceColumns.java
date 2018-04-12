package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by sebastian on 11/29/16.
 */
public interface PriceColumns {

  @DataType(INTEGER)
  @PrimaryKey
  @AutoIncrement
  String _ID = "_id";

  @DataType(INTEGER)
  @NotNull
  String DATE = "date";

  @DataType(TEXT)
  @NotNull
  String PRICE = "price";

  @DataType(TEXT)
  @NotNull
  @References(table = QuoteDatabase.QUOTES, column = QuoteColumns.SYMBOL)
  String STOCK_SYMBOL = "stock_symbol";
}

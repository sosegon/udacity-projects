package com.sam_chordas.android.stockhawk.data;

import android.net.Uri;
import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by sam_chordas on 10/5/15.
 */
@ContentProvider(authority = QuoteProvider.AUTHORITY, database = QuoteDatabase.class)
public class QuoteProvider {
  public static final String AUTHORITY = "com.sam_chordas.android.stockhawk.data.QuoteProvider";

  static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

  interface Path{
    String QUOTES = "quotes";
    String PRICES = "prices";
  }

  private static Uri buildUri(String... paths){
    Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
    for (String path:paths){
      builder.appendPath(path);
    }
    return builder.build();
  }

  @TableEndpoint(table = QuoteDatabase.QUOTES)
  public static class Quotes{
    @ContentUri(
        path = Path.QUOTES,
        type = "vnd.android.cursor.dir/quote"
    )
    public static final Uri CONTENT_URI = buildUri(Path.QUOTES);

    @InexactContentUri(
        name = "STOCK_SYMBOL",
        path = Path.QUOTES + "/*",
        type = "vnd.android.cursor.item/quote",
        whereColumn = QuoteColumns.SYMBOL,
        pathSegment = 1
    )
    public static Uri withSymbol(String symbol){
      return buildUri(Path.QUOTES, symbol);
    }
  }

  @TableEndpoint(table = QuoteDatabase.PRICES)
  public static class Prices{
    @ContentUri(
            path = Path.PRICES,
            type = "vnd.android.cursor.dir/price"
    )
    public static final Uri CONTENT_URI = buildUri(Path.PRICES);

    @InexactContentUri(
            name = "DATE_RANGE",
            path = Path.PRICES + "/*/#/#",
            type = "vnd.android.cursor.dir/price",
            whereColumn = {PriceColumns.STOCK_SYMBOL, PriceColumns.DATE + ">", PriceColumns.DATE + "<"},
            pathSegment = {1, 2, 3}
    )
    public static Uri historicRange(String stockName, long startDate, long endDate){
      return buildUri(Path.PRICES, stockName, String.valueOf(startDate), String.valueOf(endDate));
    }

    @InexactContentUri(
            name = "DATE",
            path = Path.PRICES + "/*/#",
            type = "vnd.android.cursor.dir/price",
            whereColumn = {PriceColumns.STOCK_SYMBOL, PriceColumns.DATE},
            pathSegment = {1, 2}
    )
    public static Uri historicPoint(String stockName, long date){
      return buildUri(Path.PRICES, stockName, String.valueOf(date));
    }
  }
}

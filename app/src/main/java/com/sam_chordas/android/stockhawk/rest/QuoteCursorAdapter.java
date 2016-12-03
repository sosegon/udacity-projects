package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Projections;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;

/**
 * Created by sam_chordas on 10/6/15.
 *  Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
    implements ItemTouchHelperAdapter{

  private static Context mContext;
  private static Typeface robotoLight;
  private boolean isPercent;
  public QuoteCursorAdapter(Context context, Cursor cursor){
    super(context, cursor);
    mContext = context;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_quote, parent, false);
    ViewHolder vh = new ViewHolder(itemView);
    return vh;
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor){
    String symbol = cursor.getString(Projections.STOCK_SYMBOL);
    String bidPrice = cursor.getString(Projections.STOCK_BIDPRICE);
    viewHolder.symbol.setText(symbol);
    viewHolder.bidPrice.setText(bidPrice);
    int sdk = Build.VERSION.SDK_INT;
    if (cursor.getInt(Projections.STOCK_ISUP) == 1){
      if (sdk < Build.VERSION_CODES.JELLY_BEAN){
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
      }else {
        viewHolder.change.setBackground(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
      }
    } else{
      if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
      } else{
        viewHolder.change.setBackground(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
      }
    }
    String change = Utils.showPercent ? cursor.getString(Projections.STOCK_PERCENT_CHANGE)
                                      : cursor.getString(Projections.STOCK_CHANGE);
    viewHolder.change.setText(change);

    boolean isTemp = cursor.getInt(Projections.STOCK_ISTEMP) == 1;
    viewHolder.pgr.setVisibility(isTemp ? View.VISIBLE : View.GONE);
    viewHolder.bidPrice.setVisibility(!isTemp ? View.VISIBLE : View.GONE);
    viewHolder.change.setVisibility(!isTemp ? View.VISIBLE : View.GONE);

    String symbolDesc = mContext.getString(R.string.desc_stock_name, Utils.spellWord(symbol));
    String priceDesc = mContext.getString(R.string.desc_stock_current_price, Utils.spellWord(bidPrice));
    int descRes = Utils.showPercent ? R.string.desc_stock_percentage_change : R.string.desc_stock_price_change;
    String changeDesc = mContext.getString(descRes, Utils.spellWord(change));

    viewHolder.symbol.setContentDescription(symbolDesc);
    viewHolder.bidPrice.setContentDescription(priceDesc);
    viewHolder.change.setContentDescription(changeDesc);
  }

  @Override public void onItemDismiss(int position) {
    Cursor c = getCursor();
    c.moveToPosition(position);
    String symbol = c.getString(Projections.STOCK_SYMBOL);
    mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);
    notifyItemRemoved(position);
  }

  @Override public int getItemCount() {
    return super.getItemCount();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder
      implements ItemTouchHelperViewHolder, View.OnClickListener{
    public final TextView symbol;
    public final TextView bidPrice;
    public final TextView change;
    public final ProgressBar pgr;
    public ViewHolder(View itemView){
      super(itemView);
      symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
      symbol.setTypeface(robotoLight);
      bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
      change = (TextView) itemView.findViewById(R.id.change);
      pgr = (ProgressBar) itemView.findViewById(R.id.pgr);
    }

    @Override
    public void onItemSelected(){
      itemView.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onItemClear(){
      itemView.setBackgroundColor(0);
    }

    @Override
    public void onClick(View v) {

    }
  }
}

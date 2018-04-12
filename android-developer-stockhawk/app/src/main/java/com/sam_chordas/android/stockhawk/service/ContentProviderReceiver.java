package com.sam_chordas.android.stockhawk.service;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;

/**
 * Created by sebastian on 12/5/16.
 */

@SuppressLint("ParcelCreator")
public class ContentProviderReceiver extends ResultReceiver {

  public interface Resolver {
    void onResolveResult(int resultCode, Bundle resultData);
  }

  public final static String TAG = "content_provider_receiver";
  private Resolver mResolver;

  public ContentProviderReceiver(Handler handler) {
    super(handler);
  }

  @Override
  protected void onReceiveResult(int resultCode, Bundle resultData) {
    if(mResolver != null){
      mResolver.onResolveResult(resultCode, resultData);
    }
  }

  public void setResolver(Resolver mResolver) {
    this.mResolver = mResolver;
  }
}

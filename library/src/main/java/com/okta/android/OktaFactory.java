package com.okta.android;

import android.content.Context;

public interface OktaFactory<T> {
    T buildOkta(int color, OktaConfig config, Context context, OktaStorage storage);
}

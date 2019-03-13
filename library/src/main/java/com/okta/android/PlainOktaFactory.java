package com.okta.android;

import android.content.Context;

public class PlainOktaFactory implements OktaFactory<Okta> {
    @Override
    public Okta buildOkta(int color, OktaConfig config, Context context, OktaStorage storage) {
        return new Okta(config, storage, context, color);
    }
}

package com.okta.android.async;

import android.content.Context;
import com.okta.android.*;

public class AsyncOktaFactory implements OktaFactory<OktaAsync> {

    @Override
    public OktaAsync buildOkta(int color, OktaConfig config, Context context, OktaStorage storage) {
        PlainOktaFactory factory = new PlainOktaFactory();
        return new OktaAsync(factory.buildOkta(color, config, context, storage));
    }
}

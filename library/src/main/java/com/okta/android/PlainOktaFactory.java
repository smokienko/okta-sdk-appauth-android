package com.okta.android;

public class PlainOktaFactory implements OktaFactory<Okta> {
    @Override
    public Okta buildOkta(int color, OktaConfig config, OktaStorage storage) {
        return new Okta(config, storage, color);
    }
}

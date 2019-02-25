package com.okta.android;

public interface OktaFactory<T> {
    T buildOkta(int color, OktaConfig config, OktaStorage storage);
}

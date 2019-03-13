package com.okta.android;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;

public class OktaBuilder {
    @ColorRes
    private int color;
    private OktaStorage storage;
    private OktaConfig config;
    private OktaFactory factory;
    private Context context;

    public OktaBuilder withColor(@ColorRes int color) {
        this.color = color;
        return this;
    }

    public OktaBuilder withStorage(@NonNull OktaStorage storage) {
        this.storage = storage;
        return this;
    }

    public OktaBuilder withConfig(@NonNull OktaConfig config) {
        this.config = config;
        return this;
    }

    public OktaBuilder withOktaFactory(@NonNull OktaFactory oktaFactory) {
        this.factory = oktaFactory;
        return this;
    }

    public OktaBuilder withContext(@NonNull Context context) {
        this.context = context;
        return this;
    }

    public <T> T build() {
        if (storage == null) {
            throw new IllegalStateException("Storage can not be null!");
        }
        if (config == null) {
            throw new IllegalStateException("Config can not be null!");
        }
        if (factory == null) {
            throw new IllegalStateException("OktaFactory can not be null!");
        }
        return (T) factory.buildOkta(color, config, context, storage);
    }
}

package com.okta.android;


public interface OktaStorage {

    void save(String key, String value);

    String get(String key);

    void delete(String key);


}

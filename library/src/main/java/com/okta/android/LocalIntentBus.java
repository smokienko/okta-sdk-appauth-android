package com.okta.android;

import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

public class LocalIntentBus {

    public static final String  BROWSER_AUTH_CHANNEL = "browserAuthChannel";

    private final Map<String, IntentObserver> observers = new HashMap<>();
    private final Map<String, Intent> pendingIntents = new HashMap<>();

    private static LocalIntentBus mInstance;
    private static final Object mLock = new Object();

    private LocalIntentBus() {}

    public static LocalIntentBus getInstance(){
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new LocalIntentBus();
            }
            return mInstance;
        }
    }

    public void register(String channel, IntentObserver observer) {
        observers.put(channel, observer);
        postPending(channel);
    }

    public void unregister(String channel) {
        observers.remove(channel);
    }

    public void unregisterAll() {
        observers.clear();
    }

    public void post(String channel, Intent intent) {
        IntentObserver observer = observers.get(channel);
        if (observer != null) {
            observer.onIntentReceived(intent);
        } else {
            pendingIntents.put(channel, intent);
        }
    }

    public void postPending(String channel) {
        IntentObserver observer = observers.get(channel);
        if (observer != null) {
            Intent intent = pendingIntents.remove(channel);
            if (intent != null) {
                observer.onIntentReceived(intent);
            }
        }
    }

    public boolean containsPending(String channel) {
        return pendingIntents.containsKey(channel);
    }

    public interface IntentObserver {

        void onIntentReceived(Intent intent);

    }
}

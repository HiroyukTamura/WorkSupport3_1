/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.cks.hiroyuki2.worksupport3;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;

/**
 * アプリケーションクラス
 */

public class MainApplication extends Application {
    private static final String TAG = "MANUAL_TAG: " + MainApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Log.d(TAG, "onCreate: fire");

        if (LeakCanary.isInAnalyzerProcess(this)) {
            //何も書き加えてはいけない
            return;
        }

        LeakCanary.install(this);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

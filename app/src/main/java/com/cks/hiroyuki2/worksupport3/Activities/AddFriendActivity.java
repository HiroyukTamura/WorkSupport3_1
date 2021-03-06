/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.cks.hiroyuki2.worksupport3.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.cks.hiroyuki2.worksupport3.Fragments.AddFriendFragment;
import com.cks.hiroyuki2.worksupport3.Fragments.OnAddedFriendFragment;
import com.cks.hiroyuki2.worksupport3.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import static com.cks.hiroyuki2.worksupport3.Util.initAdMob;
import static com.cks.hiroyuki2.worksupprotlib.Util.delimiter;
import static com.cks.hiroyuki2.worksupprotlib.Util.logAnalytics;

@EActivity(R.layout.activity_add_fridend_acitivity)
public class AddFriendActivity extends AppCompatActivity implements View.OnClickListener{
    
    private static final String TAG = "MANUAL_TAG: " + AddFriendActivity.class.getSimpleName();
    private PermissionListener listener;
    @ViewById(R.id.toolbar) Toolbar toolbar;
    @ViewById(R.id.coordinator) CoordinatorLayout cl;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        listener = SnackbarOnDeniedPermissionListener.Builder
                .with(cl, R.string.permission_rational)
                .withButton(R.string.permission_snb_btn, this)
                .withCallback(new Snackbar.Callback(){
                    @Override
                    public void onShown(Snackbar sb) {
                        super.onShown(sb);
                        // do nothing
                    }

                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);

                    }
                })
                .build();
    }

    @AfterViews
    void onAfterViews(){
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initAdMob(this);
        logAnalytics(TAG + "起動", this);

        if (getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
        } else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            AddFriendFragment frag = com.cks.hiroyuki2.worksupport3.Fragments.AddFriendFragment_
                    .builder().build();
            fragmentTransaction.add(R.id.fragment_container, frag).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            String contents = result.getContents();
            if(contents == null) {
                Toast.makeText(this, "キャンセルしました", Toast.LENGTH_LONG).show();
            } else if (contents.equalsIgnoreCase("0")) {//0はエラーを表す?? @see https://goo.gl/nKV7Mw
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            } else {
                String[] strings = contents.split(delimiter);
                if (strings.length != 3){//このあたり、関係のないQRでないことを確認してください！！
                    Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
                    return;
                }

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                OnAddedFriendFragment frag = com.cks.hiroyuki2.worksupport3.Fragments.OnAddedFriendFragment_
                        .builder()
                        .name(strings[0])
                        .photoUrl(strings[1])
                        .userUid(strings[2])
                        .build();
                fragmentTransaction.replace(R.id.fragment_container, frag);
                fragmentTransaction.commitAllowingStateLoss();//todo これでいいのか検討すること @see https://goo.gl/jOz17J
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public PermissionListener getListener() {
        return listener;
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof AddFriendFragment){
            ((AddFriendFragment) fragment).onPermissionAdmitted();
        }
    }
}

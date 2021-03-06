/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.cks.hiroyuki2.worksupport3.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.cks.hiroyuki2.worksupport3.Activities.AddFriendActivity;
import com.cks.hiroyuki2.worksupport3.R;
import com.cks.hiroyuki2.worksupport3.Util;
import com.example.hiroyuki3.worksupportlibw.Adapters.AddFriendVPAdapter;
import com.google.zxing.integration.android.IntentIntegrator;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * {@link AddFriendActivity}のひとり子分。
 */
@EFragment(R.layout.fragment_add_friend)
public class AddFriendFragment extends Fragment implements AddFriendVPAdapter.IAddFriendVPAdapter{

    private static final String TAG = "MANUAL_TAG: " + AddFriendFragment.class.getSimpleName();
    private AddFriendVPAdapter adapter;
    @ViewById(R.id.tab) TabLayout tab;
    @ViewById(R.id.vp) ViewPager vp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void onAfterViews(){
        adapter = new AddFriendVPAdapter(getContext(), this);
        vp.setAdapter(adapter);
        tab.setupWithViewPager(vp);
    }

    public void onPermissionAdmitted(){
        if (getActivity() == null)
            return;

        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    //ただこれだけだからさ、いちいちimplementするほどでもないんだけどさ。fragmentからこういう処理しないと気持ち悪いのよ。
    @Override
    public void onClickCameraButton() {
        Util.checkPermission(getActivity(), ((AddFriendActivity) getActivity()).getListener());/*非同期じゃないから大丈夫*/
    }
}

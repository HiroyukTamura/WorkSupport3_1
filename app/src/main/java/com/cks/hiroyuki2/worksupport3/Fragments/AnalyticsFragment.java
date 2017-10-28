/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.cks.hiroyuki2.worksupport3.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cks.hiroyuki2.worksupport3.Adapters.AnalyticsVPAdapter;
import com.cks.hiroyuki2.worksupport3.R;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.List;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

import static com.cks.hiroyuki2.worksupport3.Util.IS_DATA_MINE;
import static com.cks.hiroyuki2.worksupport3.Util.UID;
import static com.cks.hiroyuki2.worksupport3.Util.time2String;

@EFragment(R.layout.analytics_vp)
public class AnalyticsFragment extends Fragment implements ValueEventListener, IValueFormatter {
    private static final String TAG = "MANUAL_TAG: " + AnalyticsFragment.class.getSimpleName();

    @ViewById(R.id.vertical_vp) VerticalViewPager vp;
//    @ViewById(R.id.wof_container) LinearLayout wofLL;
    @ViewById(R.id.hamburger) ImageView hamburger;
    @ViewById(R.id.in_wof_ll) LinearLayout wofLL;
//    @ViewById(R.id.spacer) public Space space;
    private OnHamburgerClickListener mListener;
    @FragmentArg("uid") public String uid;
    @FragmentArg("isMine") boolean isMine;
    private View rootView;
    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnHamburgerClickListener) {
            mListener = (OnHamburgerClickListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        context = null;
    }

    public View getRootView() {
        return rootView;
    }

    @AfterViews
    void onAfterViews(){
        rootView = getView();

        if (!isMine){
            hamburger.setImageResource(R.drawable.ic_arrow_back_white_24dp);
        }

        showWeekOfDay();
        AnalyticsVPAdapter adapter = new AnalyticsVPAdapter(getContext(), this);
        vp.setAdapter(adapter);
        vp.setCurrentItem(AnalyticsVPAdapter.PAGE/2);
        vp.setOffscreenPageLimit(2);
    }

    private void showWeekOfDay(){
        //曜日を表示する
        String[] wof = getResources().getStringArray(R.array.dof);
        for (int i = 0; i <7 ; i++) {
            TextView tv = new TextView(getContext());
            tv.setText(wof[i]);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            lp.weight = 1;
            tv.setLayoutParams(lp);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(16);
            tv.setGravity(Gravity.CENTER);
            if (wof[i].equals("日"))
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.red_anton));
            wofLL.addView(tv);
        }
    }

    public void onClickUpBtn() {
        vp.setCurrentItem(vp.getCurrentItem()-1);
    }

    public void onClickDownBtn() {
        vp.setCurrentItem(vp.getCurrentItem()+1);
    }

    public LinearLayout getWofLL(){
        return wofLL;
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        int hour = (int)entry.getX();
        int min = Math.round((entry.getX() - hour) * 60);
        return time2String(hour, min);
    }

    public interface OnHamburgerClickListener {
        void onHamburgerClick();
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.exists()){
            Log.d(TAG, "onDataChange: !dataSnapshot.exists()" + dataSnapshot.getRef().getKey());
        } else {
            List<HashMap<String, Object>> list = ( List<HashMap<String, Object>>) dataSnapshot.getValue();
            Log.d(TAG, "onDataChange: ふにふに" + dataSnapshot.getRef().toString());
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "onCancelled: " + databaseError.getMessage());
    }

    @Click(R.id.hamburger)
    void openNavigation(){
        mListener.onHamburgerClick();
    }
}
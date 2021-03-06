/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.cks.hiroyuki2.worksupport3;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.cks.hiroyuki2.worksupport3.Fragments.EditTemplateFragment;
import com.cks.hiroyuki2.worksupport3.Fragments.SettingFragment;
import com.cks.hiroyuki2.worksupport3.Fragments.ShareBoardFragment;
import com.cks.hiroyuki2.worksupprotlib.CalendarDialogFragment;
import com.cks.hiroyuki2.worksupprotlib.SettingDialogFragment;
import com.example.hiroyuki3.worksupportlibw.Adapters.RecordVPAdapter;

/**
 * RecordDialogFragmentを呼ぶのおじさん！シンプル！
 */

public class DialogKicker {
    public static void kickDialogInOnClick(@NonNull String from, int callback, @NonNull Bundle bundle, @NonNull Fragment fragment){
        RecordDialogFragment dialog = RecordDialogFragment.newInstance(bundle);
        dialog.setTargetFragment(fragment, callback);
        dialog.show(fragment.getActivity().getSupportFragmentManager(), from);
    }

    public static void kickDialogInOnClick(@NonNull String from, int callback, @NonNull Bundle bundle, @NonNull ShareBoardFragment fragment){
        ShareBoardDialog dialog = ShareBoardDialog.newInstance(bundle);
        dialog.setTargetFragment(fragment, callback);
        dialog.show(fragment.getActivity().getSupportFragmentManager(), from);
    }

    public static Bundle makeBundleInOnClick(@Nullable Bundle bundle, @NonNull String from, int dataNum){
        if (bundle == null)
            bundle = new Bundle();
        bundle.putString("from", from);
        bundle.putInt(RecordVPAdapter.DATA_NUM, dataNum);
        return bundle;
    }

    public static void kickInputDialog(Bundle bundle, String command, int commandInt, Fragment fragment){
        RecordDialogFragmentInput dialog = RecordDialogFragmentInput.newInstance(bundle);
        dialog.setTargetFragment(fragment, commandInt);
        dialog.show(fragment.getActivity().getSupportFragmentManager(), command);
    }

    public static void kickWidgetDialog(@Nullable Bundle bundle, String from, int callback, EditTemplateFragment fragment){
        TempWidgetDialogFragment dialog = TempWidgetDialogFragment.newInstance(bundle);
        dialog.setTargetFragment(fragment, callback);
        dialog.show(fragment.getActivity().getSupportFragmentManager(), from);
    }

    public static void kickCalendarDialog(Bundle bundle, String from, int callback, Fragment fragment){
        CalendarDialogFragment dialog = CalendarDialogFragment.newInstance(bundle);
        dialog.setTargetFragment(fragment, callback);
        dialog.show(fragment.getActivity().getSupportFragmentManager(), from);
    }

    public static void kickTimePickerDialog(@NonNull String from, int callback, @NonNull Bundle bundle, @NonNull Fragment fragment){
        RecordDialogFragmentPicker dialog = RecordDialogFragmentPicker.newInstance(bundle);
        dialog.setTargetFragment(fragment, callback);
        dialog.show(fragment.getActivity().getSupportFragmentManager(), from);
    }

    public static void kickCircleAndInputDialog(@NonNull String from, int callback, @NonNull Bundle bundle, @NonNull Fragment fragment){
        RecordDiaogFragmentTag dialog = RecordDiaogFragmentTag.newInstance(bundle);
        dialog.setTargetFragment(fragment, callback);
        dialog.show(fragment.getActivity().getSupportFragmentManager(), from);
    }

    public static void kickSettingDialog(@NonNull String from, int callback, SettingFragment fragment){
        Bundle bundle = new Bundle();
        bundle.putInt(SettingDialogFragment.BUNDLE_KEY_COMMAND, callback);
        SettingDialogFragment f = SettingDialogFragment.newInstance(bundle);
        f.setTargetFragment(fragment, SettingDialogFragment.DIALOG_CALLBACK);
        f.show(fragment.getFragmentManager(), from);
    }
}

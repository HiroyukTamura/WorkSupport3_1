/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.cks.hiroyuki2.worksupport3;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cks.hiroyuki2.worksupport3.Fragments.ShareBoardFragment;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.cks.hiroyuki2.worksupprotlib.Util.setImgFromStorage;

/**
 * Created by hiroyuki2 on 2017/10/27.
 */
public class ShareBoardDialogAdapter extends BaseAdapter {
    private int requestCode;
    private Context context;
    private LayoutInflater inflater;
    private List<String> list;
    @android.support.annotation.ColorRes int color0;
    @android.support.annotation.ColorRes int color1;

    ShareBoardDialogAdapter(@NonNull Context context, int requestCode, @NonNull List<String> list){
        this.requestCode = requestCode;
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        switch (requestCode){
            case ShareBoardFragment.DIALOG_CODE:
                color0 = ContextCompat.getColor(context, R.color.colorAccentDark);
                color1 = ContextCompat.getColor(context, R.color.colorPrimaryDark);
                this.list = list;
                break;
        }
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.board_dialog_list_item, viewGroup, false);

        TextView tv = view.findViewById(R.id.tv);
        tv.setText(list.get(i));

        ImageView iv = view.findViewById(R.id.iv);
        int res;
        switch (requestCode){
            case ShareBoardFragment.DIALOG_CODE:
                switch (i){
                    case 0:
                        iv.setImageResource(R.drawable.ic_mode_edit_white_36dp);
                        iv.setColorFilter(color0);
                        break;
                    case 1:
                        iv.setImageResource(R.drawable.ic_folder_white_48dp);
                        iv.setColorFilter(color1);
                        break;
                    case 2:
                        iv.setImageResource(R.drawable.doc);
                        break;
                }
                break;
        }
        return view;
    }
}

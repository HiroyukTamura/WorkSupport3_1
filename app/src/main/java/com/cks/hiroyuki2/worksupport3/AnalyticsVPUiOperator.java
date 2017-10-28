/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.cks.hiroyuki2.worksupport3;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cks.hiroyuki2.worksupport3.Fragments.AnalyticsFragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static com.cks.hiroyuki2.worksupport3.FirebaseConnection.datePattern;
import static com.cks.hiroyuki2.worksupport3.FirebaseConnection.delimiter;
import static com.cks.hiroyuki2.worksupport3.Util.cal2date;
import static com.cks.hiroyuki2.worksupport3.Util.colorId;
import static com.cks.hiroyuki2.worksupport3.Util.getTimeEveDataSetFromRecordData;
import static com.cks.hiroyuki2.worksupport3.Util.time2String;

/**
 * AnalyticsVPAdapterのお助けやくおじさん！みんな協力して働くんだね！
 */

public class AnalyticsVPUiOperator implements ValueEventListener, IValueFormatter, ViewTreeObserver.OnScrollChangedListener {

    private static final String TAG = "MANUAL_TAG: " + AnalyticsVPUiOperator.class.getSimpleName();
    private static final float LINE_WIDTH = 3f;
    private static final int COLUMN_NAME_LINE_LIMIT = 15;
    private View rootView;
    @BindView(R.id.chart) LineChart chart;
    @BindView(R.id.scroll) HorizontalScrollView hsv;
    @BindView(R.id.table) LinearLayout tableLL;
    @BindView(R.id.date_tv) TextView dateTv;
    @BindView(R.id.left_screen) LinearLayout leftLL;
    @BindView(R.id.legend_fl) FlowLayout legendFl;
    @BindDimen(R.dimen.column_min_width) int columnMinWidth;
    @BindDimen(R.dimen.grid_padding) int padding;
    @BindDimen(R.dimen.legend_total_height) int legendHeight;
    @BindColor(R.color.red_anton_dark) int colorGradationMax;
    @BindColor(R.color.blue_pint) int colorGradationMin;
    @BindColor(R.color.blue_gray_light) int blueGrayLight;
    @BindColor(R.color.colorAccentDark) int colorAccentDark;
    @BindColor(R.color.colorPrimaryDark) int colorPrimaryDark;
    @BindDrawable(R.drawable.fui_done_check_mark) Drawable check;
    @BindDrawable(R.drawable.analytics_divider) Drawable divider;
    @BindDimen(R.dimen.check_drw_size) int checkSize;//これでpx変換される
    @BindDimen(R.dimen.unchek_drw_thickness) int uncheckThickSize;
    @BindDimen(R.dimen.msv_top_margin) int mcvTopMargin;
    @BindDimen(R.dimen.def_mp_chart_padding) int defChardPadding;
    @BindDimen(R.dimen.wof_width) int wofWidth;
//    private LineChart chart;
    private ArrayList<ILineDataSet> lines = new ArrayList<> ();
    private ArrayList<Calendar> loadCal;
    private int verticalRowPad;
    private List<RecordData> tempateList;
    private Calendar startCal;
    private LayoutInflater inflater;
    private AnalyticsFragment analyticsFragment;
    private List<Pair<Integer, String>> legendListForRange = new ArrayList<>();
    private List<Pair<Integer, String>> legendListForTimeEve = new ArrayList<>();

    public AnalyticsVPUiOperator(@NonNull View rootView, Calendar startCal, AnalyticsFragment analyticsFragment){
        ButterKnife.bind(this, rootView.getRootView());

        this.rootView = rootView;
        this.startCal = startCal;
        this.analyticsFragment = analyticsFragment;
        tempateList = TemplateEditor.deSerialize(rootView.getContext());
        if (tempateList == null) return;//エラー処理？？

        initParams();
        setDate();
        setColumns();
        initData(startCal.getTime());
        configChart();
    }

    private void initParams(){
        verticalRowPad = padding*2;
        hsv.getViewTreeObserver().addOnScrollChangedListener(this);
        inflater = (LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mListener = (IAnalyticsVPUiOperator)rootView.getContext();
    }

    //タイトルを設定
    private void setDate(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(startCal.getTime());
        String start = cal2date(startCal, Util.DATE_PATTERN_DOT_MD);
        cal.add(Calendar.DATE, 6);
        String end = cal2date(cal, Util.DATE_PATTERN_DOT_MD);
        String title = start + " - " + end;
        dateTv.setText(title);
    }

    private void setColumns(){
        for (int m=0; m<tempateList.size(); m++) {

            RecordData data = tempateList.get(m);
            switch (data.dataType){
                case 2:
                case 4://タグプール
                {
                    LinearLayout column = (LinearLayout) inflater.inflate(R.layout.analytics_columun, tableLL, false);
                    tableLL.addView(column);
                    TextView legendCell = column.findViewById(R.id.legend_cell);
                    if (data.dataName.length() > COLUMN_NAME_LINE_LIMIT)
                        legendCell.setLines(2);
                    legendCell.setText(data.dataName);
                }
                    break;

                case 3://params
                {
                    LinearLayout column = makeColumn();
                    tableLL.addView(column);
                    //まず大項目を追加
                    TextView bigArticleCell = setLegendBigCellOfParams(data.dataName);
                    column.addView(bigArticleCell);
                    //次に小項目を追加
                    LinearLayout bigColumn = makeBigArticleColumn();
                    column.addView(bigColumn);

                    for (String key : data.data.keySet()) {
                        LinearLayout smallColumn = (LinearLayout) inflater.inflate(R.layout.analytics_columun, bigColumn, false);
                        bigColumn.addView(smallColumn);
                        String value = (String) data.data.get(key);
                        if (value == null) continue;
                        String smlArticle = value.split(delimiter)[1];

                        TextView smallCell = smallColumn.findViewById(R.id.legend_cell);
                        smallCell.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, legendHeight / 2));
                        smallCell.setText(smlArticle);
                    }
                }
                    break;
            }
        }
    }

    private LinearLayout makeColumn(){
        LinearLayout column = new LinearLayout(rootView.getContext());
        column.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        column.setMinimumWidth(columnMinWidth);
        column.setOrientation(LinearLayout.VERTICAL);
        return column;
    }

    private LinearLayout makeBigArticleColumn(){
        LinearLayout column = new LinearLayout(rootView.getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        column.setLayoutParams(lp);
        column.setOrientation(LinearLayout.HORIZONTAL);
//        column.setPadding(0, 0, 0, mcvTopMargin);
        return column;
    }

    private TextView setLegendBigCellOfParams(String string){
        TextView tv = new TextView(rootView.getContext());
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, legendHeight/2));
        tv.setText(string);
        tv.setPadding(padding, 0, padding, 0);
        tv.setBackgroundColor(blueGrayLight);
        tv.setTextColor(Color.BLACK);
        tv.setGravity(Gravity.CENTER);
        return tv;
    }

    private void fitChartWidth(){
        int width = analyticsFragment.getRootView().getWidth() - wofWidth;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
        leftLL.setLayoutParams(lp);
    }

    private void initData(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("usersParam").child(analyticsFragment.uid);
        RecordDataUtil util = RecordDataUtil.getInstance();
        String[] dof = rootView.getResources().getStringArray(R.array.dof);
        String[] axisValue = new String[7];
        loadCal = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            final int n = i;
            String ymd = cal2date(cal, datePattern);
            int ymdInt = Integer.parseInt(ymd);

            if (util.dataMap.containsKey(ymdInt) && util.dataMap.get(ymdInt) != null && !util.dataMap.get(ymdInt).isEmpty()){
                List<RecordData> list = util.dataMap.get(ymdInt);
                Log.d(TAG, "onCreateView: こいつデータあります！");
                drawData(list, n);

                loadCal.add(cal);
                if (loadCal.size() == 7)
                    showData();

                cal.add(Calendar.DATE, 1);
                continue;
            }

            DatabaseReference refer = ref.child(ymd);
            FirebaseEventHandler handler = new FirebaseEventHandler(cal) {
                @Override
                public void onOnDataChange(DataSnapshot dataSnapshot, boolean isSnapShotExist) {
                    if (isSnapShotExist && !list.isEmpty()){//listはnonNull、かつ、listは空でありうることに注意してください。
                        Log.w(TAG, "onOnDataChange: " + dataSnapshot.getRef().toString());
                        drawData(list, n);
                    }
                    loadCal.add(cal);
                    if (loadCal.size() == 7)
                        showData();
                }

                @Override
                public void onOnCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onOnCancelled: " + databaseError.getMessage());
                    loadCal.add(cal);
                    if (loadCal.size() == 7)
                        showData();
                }
            };

            handler.initValueEventListener();
            refer.addListenerForSingleValueEvent(handler.getListener());

            axisValue[i] = cal.get(Calendar.DATE)+ "("+dof[i]+")";

            cal.add(Calendar.DATE, 1);
        }
    }

    private void drawData(List<RecordData> list, int dataRow){
        int count = 0;
        for (RecordData data: list) {
            if (data.dataType == 0)
                continue;

            if ((data.data == null || data.data.isEmpty())
                    && (data.dataType == 2 || data.dataType == 3 || data.dataType == 4)) {
                count++;
                continue;
            }

            LinearLayout column = (LinearLayout) tableLL.getChildAt(count);

            if (data.dataType == 1){
                drawLine(data, dataRow);

            } else if (data.dataType == 2) {
                FlowLayout fl = (FlowLayout) column.getChildAt(dataRow+1);//一行目は項目名
                addTag2Fl(data, fl);
                count++;

            } else if (data.dataType == 3){//params
                addParams2Fl(data, column, dataRow);
                count++;

            } else if (data.dataType == 4){
//                FlowLayout fl = (FlowLayout) column.getChildAt(dataRow+1);//一行目は項目名
//                TextView tv = createCommentTv(data);
//                fl.addView(tv);
                addComment2Fl(column, dataRow, data);
                count++;
            }
        }
    }

    //region dataType == 1 系列
    private void drawLine(RecordData data, int dataRow){
        List<Entry> entryList0 = new ArrayList<>();
        List<Entry> entryList1 = new ArrayList<>();
        List<Entry> entryList2 = new ArrayList<>();
        List<Entry> entryList3 = new ArrayList<>();

        TimeEventDataSet timeEveSet = getTimeEveDataSetFromRecordData(data);
        if (timeEveSet == null)
            return;

        List<TimeEventRange> ranges = timeEveSet.getRangeList();
        for (TimeEventRange range : ranges) {
            switch (range.getColorNum()){
                case 0:
                    addRangeEntryToList(entryList0, range, dataRow);
                    break;
                case 1:
                    addRangeEntryToList(entryList1, range, dataRow);
                    break;
                case 2:
                    addRangeEntryToList(entryList2, range, dataRow);
                    break;
                case 3:
                    addRangeEntryToList(entryList3, range, dataRow);
                    break;
            }
        }

        List<TimeEvent> eveList = timeEveSet.getEventList();
        for (TimeEvent timeEve: eveList) {
            List<Entry> entryTimeEve = new ArrayList<>();
            entryTimeEve.add(new Entry(timeEve.getHour(), dataRow));
            add2Lines(entryTimeEve, timeEve.getColorNum());
        }

        add2Lines(entryList0, 0);
        add2Lines(entryList1, 1);
        add2Lines(entryList2, 2);
        add2Lines(entryList3, 3);

        setLegend(timeEveSet);
    }

    private static void addRangeEntryToList(List<Entry> entryList, TimeEventRange range, int dataRow){
        entryList.add(new Entry(range.getStart().getHour(), dataRow));
        entryList.add(new Entry(range.getEnd().getHour(), dataRow));
    }
    //endregion


    //region dataType == 2 系列
    private void addTag2Fl(RecordData data, FlowLayout fl){
        List<View> tagList = new LinkedList<>();
        for (String key: data.data.keySet()) {
            String s = (String)data.data.get(key);
            String strings[] = s.split(delimiter);
            if (strings[2].equals(Boolean.toString(false)))
                continue;
            final View view = makeCircleAndTxt(strings[0], Integer.parseInt(strings[1]));
            tagList.add(view);
        }
        new AnalyticsTagpoolObserver(fl, tagList);
    }
    //endregion

    //region dataType == 3 系列
    ///////////////////////////params系列ここから//////////////////////
    private void addParams2Fl(RecordData data, LinearLayout column, int dataRow){
        int countInner = 0;//一行目は項目名
        LinearLayout container = (LinearLayout) column.getChildAt(1);

        for (String key : data.data.keySet()) {
            String value = (String) data.data.get(key);
            if (value == null) continue;

            LinearLayout smlColumn = (LinearLayout) container.getChildAt(countInner);
            FlowLayout fl = (FlowLayout) smlColumn.getChildAt(dataRow+1);

            String[] values = value.split(delimiter);
            switch (values[0]){
                case "0":
                    FrameLayout wrapper = createCheckView(Boolean.parseBoolean(values[2]));
                    fl.addView(wrapper);
                    break;

                case "1":
                    int max = Integer.parseInt(values[3]);
                    int min = 1;
                    int val = Integer.parseInt(values[2]);
                    TextView tv = createDigitView(max, val - min, values[2]);
                    fl.addView(tv);
                    break;
            }

            countInner++;
        }
    }

    /** なぜDrawableをFrameLayoutラップしているかというと、imageViewはgravityを指定できないし、親ビューがFlowLayoutになるので、layout_gravityも効かないためです*/
    private FrameLayout createCheckView(boolean isCheck){
        ImageView iv = new ImageView(rootView.getContext());
        FrameLayout.LayoutParams lp = isCheck
                ? new FrameLayout.LayoutParams(checkSize, checkSize)
                : new FrameLayout.LayoutParams(checkSize/2, uncheckThickSize);
        lp.gravity = Gravity.CENTER;
        iv.setLayoutParams(lp);
        FrameLayout flWrapper = new FrameLayout(rootView.getContext());
        flWrapper.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        flWrapper.addView(iv);

        if (isCheck){
            Drawable checkDrw = check.mutate();
            checkDrw.setColorFilter(colorPrimaryDark, PorterDuff.Mode.SRC_IN);
            iv.setImageDrawable(checkDrw);
        } else {
            Drawable checkDrw = divider.mutate();
            checkDrw.setColorFilter(colorAccentDark, PorterDuff.Mode.SRC_IN);
            iv.setImageDrawable(checkDrw);
        }

        return flWrapper;
    }

    private TextView createDigitView(int max, int digit, @NonNull String txt){
        TextView tv = new TextView(rootView.getContext());
        tv.setTextSize(18);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.CENTER);
        tv.setText(txt);
        tv.setTextColor(Color.rgb(
                Color.red(colorGradationMin) + (Color.red(colorGradationMax) - Color.red(colorGradationMax)) * digit/max,
                Color.green(colorGradationMin) + (Color.green(colorGradationMax) - Color.green(colorGradationMax)) * digit/max,
                Color.blue(colorGradationMin) + (Color.blue(colorGradationMax) - Color.blue(colorGradationMax)) * digit/max
        ));
        return tv;
    }
    ///////////////////////////params系列ここまで//////////////////////
    //endregion

    //region dataType == 4 系列
    private void addComment2Fl(LinearLayout column, int dataRow, RecordData data){
        FlowLayout fl = (FlowLayout) column.getChildAt(dataRow+1);//一行目は項目名
        TextView tv = createCommentTv(data);
        new AnalyticsCommentObserver(fl, tv);
    }

    @NonNull
    private TextView createCommentTv(RecordData data){
        TextView tv = new TextView(rootView.getContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.CENTER_VERTICAL);
        if (data.data.containsKey("comment") && data.data.get("comment") != null){
            String string = (String) data.data.get("comment");
            tv.setText(string);
        }
        return tv;
    }
    //endregion

    static private float hourMin2Hour(@NonNull String string){
        String[] hm = string.split(":");
        int hour = Integer.parseInt(hm[0]);
        float min = Integer.parseInt(hm[1]) /60;
        return hour+min;
    }

    private void add2Lines(List<Entry> entries, int num){
        if (entries == null || entries.isEmpty())
            return;
        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setValueFormatter(this);
        setWholeCircleColor(dataSet, colorId.get(num));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setValueTextSize(14f);//これはdp指定であることに注意してください
        dataSet.setLineWidth(LINE_WIDTH);
        lines.add(dataSet);
    }

    private void setWholeCircleColor(LineDataSet dataSet, int colorId){
        int color = ContextCompat.getColor(rootView.getContext(), colorId);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setCircleColorHole(color);
    }

    private void showData(){
        fitChartWidth();//横幅をセットする

        chart.setData(new LineData(lines));
        chart.setViewPortOffsets(defChardPadding, 0, defChardPadding, 0);
        chart.invalidate();

        setLegendLayoutPrams();
    }

    private void setLegendLayoutPrams(){
        RelativeLayout.LayoutParams lp = (new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (chart.getHeight()+defChardPadding*2)/(7*2)));
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        legendFl.setLayoutParams(lp);
    }

    private void configChart(){
        chart.getAxisLeft().setAxisMinimum(-0.5f);
        chart.getAxisLeft().setAxisMaximum(6.5f);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setInverted(true);

        chart.getAxisLeft().setDrawLabels(false);
//        chart.getXAxis().setDrawLabels(false);
//        chart.setExtraTopOffset(padding);
//        chart.setExtraBottomOffset(padding);
        chart.getLegend().setEnabled(false);
        chart.setContentDescription("");

        chart.getAxisRight().setEnabled(false);

//        chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        chart.getXAxis().setAxisMinimum(0);
        chart.getXAxis().setAxisMaximum(24);
        chart.getXAxis().setDrawLabels(false);
        chart.getXAxis().setDrawAxisLine(false);

        chart.setNoDataText(rootView.getResources().getText(R.string.no_data_txt).toString());
        chart.setNoDataTextColor(ContextCompat.getColor(rootView.getContext(), R.color.colorPrimaryDark));
        chart.getDescription().setEnabled(false);
    }

    private void setLegend(@NonNull TimeEventDataSet timeEveSet){
        for (TimeEventRange range: timeEveSet.getRangeList()) {
            String value = range.getStart().getName() + "→" + range.getEnd().getName();
            innerSetLegend(legendListForRange, range.getColorNum(), value);
        }

        for (TimeEvent event : timeEveSet.getEventList()) {
            innerSetLegend(legendListForTimeEve, event.getColorNum(), event.getName());
        }
    }

    private void innerSetLegend(List<Pair<Integer, String>> legendList, int colorNum, String value){
        Pair<Integer, String> pair = new Pair<>(colorNum, value);
        if (legendList.contains(pair))
            return;
        legendList.add(pair);
        View tag = makeCircleAndTxt(value, colorNum);
        legendFl.addView(tag);
        //タグが見切れている場合には隠す
        if (legendFl.getBaseline() < tag.getBaseline())
            tag.setVisibility(GONE);
    }

    @NonNull
    private View makeCircleAndTxt(@NonNull String value, int colorNum){
        View view = inflater.inflate(R.layout.analytics_table_tag, null);
        TextView tv = view.findViewById(R.id.tv);
        tv.setText(value);
        int colorId = Util.colorId.get(colorNum);
        int color = ContextCompat.getColor(rootView.getContext(), colorId);
        ImageView iv = view.findViewById(R.id.circle);
        iv.getDrawable().mutate().setColorFilter(color, PorterDuff.Mode.SRC);
        return view;
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        int hour = (int)entry.getX();
        int min = Math.round((entry.getX() - hour) * 60);
        return time2String(hour, min);
    }

    @Override
    public void onScrollChanged() {
        int scrollX = hsv.getScrollX();
        if (scrollX == 0) return;
        fr.castorflex.android.verticalviewpager.VerticalViewPager vp = rootView.getRootView().findViewById(R.id.vertical_vp);
        if (vp == null) return;
            
        scroll(vp.getCurrentItem()+1, scrollX);
        scroll(vp.getCurrentItem()-1, scrollX);
    }

    private void scroll(int pos, int scrollX){
        View item = rootView.getRootView().findViewWithTag(pos);
        HorizontalScrollView hsv = item.findViewById(R.id.scroll);
        hsv.getViewTreeObserver().removeOnScrollChangedListener(this);
        hsv.scrollTo(scrollX, 0);
        hsv.getViewTreeObserver().addOnScrollChangedListener(this);
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

    @OnClick(R.id.up_btn)
    void wOnClickUpBtn(){
        analyticsFragment.onClickUpBtn();
    }

    @OnClick(R.id.down_btn)
    void wOnClickDownBtn(){
        analyticsFragment.onClickDownBtn();
    }
}
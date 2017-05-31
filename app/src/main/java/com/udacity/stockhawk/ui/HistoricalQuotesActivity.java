package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.IntegerRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class HistoricalQuotesActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private static final int STOCK_LOADER = 0;
    /*@SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view_hist)
    RecyclerView stockRecyclerView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error_hist)
    TextView error;*/
    private HistoricalQuotesAdapter adapter;
    private String symbol;
    private LineChart mChart;
    private SeekBar mSeekBarX;
    private TextView tvX;
    private ArrayList<Entry> history_entries = new ArrayList<Entry>();

    /*@Override
    public void onClick(int clickedItemIndex) { Timber.d(Integer.toString(clickedItemIndex)); }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.d("Create Activity");

        setContentView(R.layout.activity_historical_quotes);
        ButterKnife.bind(this);

        Intent it = getIntent();
        if (it.hasExtra(Intent.EXTRA_TEXT)) {
            symbol = it.getStringExtra(Intent.EXTRA_TEXT);
        } else {
            //error(getString(R.string.no_symbol));
        }

        setTitle(symbol);

        Cursor cursor = getContentResolver().query(Contract.Quote.makeUriForStock(symbol),
                null, null, null, null, null);

        if(cursor.moveToNext()){
            //ArrayList<Pair<String,String>> history = new ArrayList();
            String history_str = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
            String[] history_arr = history_str.split("\n");
            //SimpleDateFormat sdfr = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            for (String str: history_arr) {
                String[] str_split = str.split(", ");
                //String date = sdfr.format(new Date(Long.parseLong(str_split[0])));
                //String price = "$" + String.valueOf(Math.round(100 * new Float(str_split[1])) / 100.0);
                //history.add(new Pair(date, price));
                history_entries.add(new Entry(Long.parseLong(str_split[0]), Float.parseFloat(str_split[1])));
                Timber.d(String.valueOf(Float.parseFloat(str_split[0])));
                Timber.d(String.valueOf(Float.parseFloat(str_split[1])));
            }
            Collections.reverse(history_entries);
            //adapter = new HistoricalQuotesAdapter(history/*, this*/);
            //stockRecyclerView.setAdapter(adapter);
            //stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }else{
            //error(getString(R.string.no_historical_data));
        }


        /* chart */
        tvX = (TextView) findViewById(R.id.tvXMax);
        mSeekBarX = (SeekBar) findViewById(R.id.seekBar1);
        mSeekBarX.setProgress(100);
        tvX.setText("100");

        mSeekBarX.setOnSeekBarChangeListener(this);

        mChart = (LineChart) findViewById(R.id.chart1);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        mChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setViewPortOffsets(0f, 0f, 0f, 0f);

        // add data
        setData(100, 30);
        mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        //xAxis.setTypeface(mTfLight);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        //leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(170f);
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.rgb(255, 192, 56));

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        tvX.setText("" + (mSeekBarX.getProgress()));

        setData(mSeekBarX.getProgress(), 50);

        // redraw
        mChart.invalidate();
    }

    private void setData(int count, float range) {

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(history_entries, "DataSet 1");
        set1.setAxisDependency(AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(false);
        set1.setDrawValues(false);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);

        // create a data object with the datasets
        LineData data = new LineData(set1);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        // set data
        mChart.setData(data);
    }


    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /*private void error(String str) {
        error.setText(str);
        error.setVisibility(View.VISIBLE);
    }*/

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

}

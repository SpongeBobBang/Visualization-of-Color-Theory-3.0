package com.google.ar.sceneform.Visualization_of_Color_Theory;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.media.Image;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.ar.sceneform.Visualization_of_Color_Theory.helpers.ImageChannelDataPoint;
import com.google.ar.sceneform.Visualization_of_Color_Theory.helpers.MyMarkerView;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener  {
    private static String TAG = "Chart";
    private static String[] names_painting = {"Parisian Boulevard", "Ship Hull", "Still Life"};

    private static final int
                        iRed = AugmentedImageNode.iRed,
                        iYellow = AugmentedImageNode.iYellow,
                        iBlue = AugmentedImageNode.iBlue;

    private static final String
            FOLDER_IMAGES = AugmentedImageFragment.FOLDER_IMAGES;
    private static final String[] NAMES_IMAGE = AugmentedImageFragment.NAMES_IMAGE;

    private int curr_img;

    TextView namePainting;
    private Bitmap currPainting;
    private ImageChannelDataPoint.Line_State currLineState;
    private int currChannel;

    private RadioGroup radioGroupState;
    private RadioGroup radioGroupChannel;
    private ImageView painting;

    private LineChart chart;
    private SeekBar seekBarLineNo, seekBarUnit;
    private TextView tvX, tvY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        namePainting = findViewById(R.id.namePainting);
        ConstraintLayout layout = findViewById(R.id.layoutChart);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            curr_img = extras.getInt(AugmentedImageActivity.EXTRA_I_PAINTING, -1);
        }
        setText(curr_img);
        namePainting.setVisibility(View.VISIBLE);
        currPainting = AugmentedImageNode.getBitmapFromAsset(this,
                FOLDER_IMAGES + "/" + NAMES_IMAGE[curr_img]);
        Log.i(TAG, "Painting's height:" + currPainting.getHeight() + "; width: " + currPainting.getWidth());
        painting = findViewById(R.id.painting);
//        painting.setImageBitmap(currPainting);
        switch (curr_img) {
            case 0:
                painting.setBackgroundResource(R.drawable.forest);
                layout.setBackgroundResource(R.drawable.forest_blurred);
                break;
            case 1:
                painting.setBackgroundResource(R.drawable.reflection);
                layout.setBackgroundResource(R.drawable.reflection_blurred);
                break;
            case 2:
                painting.setBackgroundResource(R.drawable.still_life);
                layout.setBackgroundResource(R.drawable.still_life_blurred);
                break;
        }

        currLineState = ImageChannelDataPoint.Line_State.ROW;
        currChannel = iRed;

        radioGroupState = findViewById(R.id.radioGroupState);
        radioGroupState.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = group.findViewById(checkedId);
                int iState = -1; // If not changed later, error
                switch (radioButton.getId()) {
                    case R.id.radioRow:
                        iState = 0;
                        break;
                    case R.id.radioCol:
                        iState = 1;
                        break;
                }
                currLineState = ImageChannelDataPoint.Line_State.values()[iState];
                setSeekBarLineNoMax(currLineState);
                setData(currLineState, seekBarLineNo.getProgress(), currChannel, seekBarUnit.getProgress());
            }
        });

        radioGroupChannel = findViewById(R.id.radioGroupChannel);
        radioGroupChannel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = group.findViewById(checkedId);
                int iChannel = -1; // If not changed later, error
                switch (radioButton.getId()) { // A case is always met
                    case R.id.radioRed:
                        iChannel = 0;
                        break;
                    case R.id.radioYellow:
                        iChannel = 1;
                        break;
                    case R.id.radioBlue:
                        iChannel = 2;
                        break;
                }
                currChannel = iChannel;
                chart.invalidate(); // Redraw
                setData(currLineState, seekBarLineNo.getProgress(), currChannel, seekBarUnit.getProgress());
            }
        });

        tvX = findViewById(R.id.tvXMax);
        tvY = findViewById(R.id.tvYMax);

        seekBarLineNo = findViewById(R.id.seekBar1);
        if (currPainting != null) {
            seekBarLineNo.setMax(currPainting.getHeight() - 1); // For row number initialized
        }
        seekBarLineNo.setOnSeekBarChangeListener(this);

        seekBarUnit = findViewById(R.id.seekBar2);
        seekBarUnit.setMax(200);                     // For width between data points
        seekBarUnit.setOnSeekBarChangeListener(this);


        {   // // Chart Style // //
            chart = findViewById(R.id.chart1);

            // background color
            chart.setBackgroundColor(Color.WHITE);

            // disable description text
            chart.getDescription().setEnabled(false);

            // enable touch gestures
            chart.setTouchEnabled(true);

            // set listeners
//            chart.setOnChartValueSelectedListener(this);
            chart.setDrawGridBackground(false);

            // create marker to display box when values are selected
            MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);

            // Set the marker to the chart
            mv.setChartView(chart);
            chart.setMarker(mv);

            // enable scaling and dragging
            chart.setDragEnabled(true);
            chart.setScaleEnabled(true);
            // chart.setScaleXEnabled(true);
            // chart.setScaleYEnabled(true);

            // force pinch zoom along both axis
            chart.setPinchZoom(true);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }

        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = chart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            chart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            yAxis.setAxisMaximum(255f); // for color depth of 8-bit
            yAxis.setAxisMinimum(0f);
        }


        {   // // Create Limit Lines // //
            LimitLine llXAxis = new LimitLine(9f, "Index 10");
            llXAxis.setLineWidth(4f);
            llXAxis.enableDashedLine(10f, 10f, 0f);
            llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            llXAxis.setTextSize(10f);

            LimitLine ll1 = new LimitLine(255f, "Upper Limit");
            ll1.setLineWidth(4f);
            ll1.enableDashedLine(10f, 10f, 0f);
            ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            ll1.setTextSize(10f);

            LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
            ll2.setLineWidth(4f);
            ll2.enableDashedLine(10f, 10f, 0f);
            ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            ll2.setTextSize(10f);

            // draw limit lines behind data instead of on top
            yAxis.setDrawLimitLinesBehindData(true);
            xAxis.setDrawLimitLinesBehindData(true);

            // add limit lines
            yAxis.addLimitLine(ll1);
            yAxis.addLimitLine(ll2);
            //xAxis.addLimitLine(llXAxis);
        }

        // add data
        seekBarLineNo.setProgress(0);
        seekBarUnit.setProgress(25);
        setData(currLineState, seekBarLineNo.getProgress(), currChannel, seekBarUnit.getProgress());

        // draw points over time
        chart.animateX(1500);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // draw legend entries as lines
        l.setForm(Legend.LegendForm.LINE);
    }

    private void setSeekBarLineNoMax(ImageChannelDataPoint.Line_State state) {
        seekBarLineNo.setMax(state == ImageChannelDataPoint.Line_State.ROW ?
                                currPainting.getHeight() - 1 : currPainting.getWidth() - 1);
    }

    public void onBackPressed() { // Go back to previous activity
        super.onBackPressed();
    }

    private void setText(int curr_img) {
        namePainting.setText("Painting " + (curr_img+1) + ": " + names_painting[curr_img]);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        tvX.setText(String.valueOf(seekBarLineNo.getProgress()));
        tvY.setText(String.valueOf(seekBarUnit.getProgress()));

        setData(currLineState, seekBarLineNo.getProgress(), currChannel, seekBarUnit.getProgress());

        // redraw
        chart.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    private void setData(ImageChannelDataPoint.Line_State lineState, int noLine, int channel, int unit) {
        Log.i(TAG, "setData() called on " + lineState.toString() + " " + noLine
                            + " with channel " + channel + " and unit gap of " + unit);

        ArrayList<Entry> entries = new ArrayList<>();
        int[] values = ImageChannelDataPoint.getDataPoints(currPainting, lineState, noLine, channel, unit);
        int numPoints = values.length;
        for (int i = 0; i < numPoints; i++) {
            entries.add(new Entry(i, values[i], getDrawable(R.drawable.star)));
        }

        LineDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(entries);
            set1.notifyDataSetChanged();

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                Drawable drawable = getDrawable(R.drawable.fade_red);
                Log.i(TAG, "Reached picking drawable with channel: " + channel);
                // drawables only supported on api level 18 and above
                switch (channel) {
                    case 0:
                        drawable = getDrawable(R.drawable.fade_red);
                        Log.i(TAG, "Drawable fade Red selected");
                        break;
                    case 1:
                        drawable = getDrawable(R.drawable.fade_yellow);
                        Log.i(TAG, "Drawable fade Yellow selected");
                        break;
                    case 2:
                        drawable = getDrawable(R.drawable.fade_blue);
                        Log.i(TAG, "Drawable fade Blue selected");
                        break;
                }
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.BLACK);
            }

            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            // create a dataset and give it a type
            set1 = new LineDataSet(entries, "DataSet 1");

            set1.setDrawIcons(false);

            // draw dashed line
            set1.enableDashedLine(10f, 5f, 0f);

            // black lines and points
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);

            // line thickness and point size
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);

            // draw points as solid circles
            set1.setDrawCircleHole(false);

            // customize legend entry
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            // text size of values
            set1.setValueTextSize(9f);

            // draw selection line as dashed
            set1.enableDashedHighlightLine(10f, 5f, 0f);

            // set the filled area
            set1.setDrawFilled(true);
            set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return chart.getAxisLeft().getAxisMinimum();
                }
            });

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                Drawable drawable = getDrawable(R.drawable.fade_red);
                Log.i(TAG, "Reached picking drawable with channel: " + channel);
                // drawables only supported on api level 18 and above
                switch (channel) {
                    case 0:
                        drawable = getDrawable(R.drawable.fade_red);
                        Log.i(TAG, "Drawable fade Red selected");
                        break;
                    case 1:
                        drawable = getDrawable(R.drawable.fade_yellow);
                        Log.i(TAG, "Drawable fade Yellow selected");
                        break;
                    case 2:
                        drawable = getDrawable(R.drawable.fade_blue);
                        Log.i(TAG, "Drawable fade Blue selected");
                        break;
                }
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            chart.setData(data);
        }
    }

}

package com.ligresoftware.ticketappfree.utils;

import android.app.Activity;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

import com.ligresoftware.ticketappfree.R;

import java.text.DecimalFormat;

public class InputFilterMinMaxFloat implements InputFilter {

    private float min, max;
    private Activity activity;

    public InputFilterMinMaxFloat(float min, float max, Activity activity) {
        this.min = min;
        this.max = max;
        this.activity = activity;
    }

    public InputFilterMinMaxFloat(String min, String max, Activity activity) {
        this.min = Float.parseFloat(min);
        this.max = Float.parseFloat(max);
        this.activity = activity;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            float input = Float.parseFloat(dest.toString() + source.toString());
            if (isInRange(min, max, input)) {
                return null;
            } else {
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(2);
                Toast.makeText(activity, activity.getString(R.string.max_values_string, df.format(min), df.format(max)), Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException nfe) {
        }
        return "";
    }

    private boolean isInRange(float a, float b, float c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }

}

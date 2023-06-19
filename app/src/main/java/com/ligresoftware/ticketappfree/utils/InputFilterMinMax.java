package com.ligresoftware.ticketappfree.utils;

import android.app.Activity;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

import com.ligresoftware.ticketappfree.R;

public class InputFilterMinMax implements InputFilter {

    private int min, max;
    private Activity activity;

    public InputFilterMinMax(int min, int max, Activity activity) {
        this.min = min;
        this.max = max;
        this.activity = activity;
    }

    public InputFilterMinMax(String min, String max, Activity activity) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
        this.activity = activity;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(dest.toString() + source.toString());
            if (isInRange(min, max, input)) {
                return null;
            } else {
                Toast.makeText(activity, activity.getString(R.string.max_values, min, max), Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException nfe) {
        }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }

}

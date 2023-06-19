package com.ligresoftware.ticketappfree.utils;

import android.text.Editable;
import android.text.TextWatcher;

public class MyTwoCents implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable arg0) {
        int length = arg0.length();
        if (length > 0) {
            if (nrOfDecimal(arg0.toString()) > 2)
                arg0.delete(length - 1, length);
        }

    }


    private int nrOfDecimal(String nr) {
        int len = nr.length();
        int pos = len;
        for (int i = 0; i < len; i++) {
            if (nr.charAt(i) == '.') {
                pos = i + 1;
                break;
            }
        }
        return len - pos;
    }
}

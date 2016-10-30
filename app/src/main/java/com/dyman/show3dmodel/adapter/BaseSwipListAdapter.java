package com.dyman.show3dmodel.adapter;

import android.widget.BaseAdapter;

import com.dyman.show3dmodel.BaseActivity;

/**
 * Created by dyman on 16/8/20.
 */
public abstract class BaseSwipListAdapter extends BaseAdapter {
    public boolean getSwipEnableByPosition(int position) {
        return true;
    }
}

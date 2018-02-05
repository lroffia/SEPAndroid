package com.vaimee.www.simplepubsub;

import android.app.Activity;

/**
 * Created by luca on 05/02/18.
 */

public interface MyListener {
    public void showMessage(String text);
    public Activity getActivity();
    public void onConnectionLost();
}

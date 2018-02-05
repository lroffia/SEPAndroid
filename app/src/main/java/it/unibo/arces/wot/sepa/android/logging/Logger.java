package it.unibo.arces.wot.sepa.android.logging;

import android.util.Log;

/**
 * Created by luca on 21/06/17.
 */
public class Logger {

    private String name;

    public Logger(String name) {
        this.name = name;
    }

    public void debug(String text) {
        Log.d(name, "debug: " + text);
    }

    public void info(String text) {
        Log.i(name, "info: " + text);
    }

    public void warn(String text) {
        Log.w(name, "warn: " + text);
    }

    public void error(String text) {
        Log.e(name, "error: " + text);
    }

    public void fatal(String text) {
        Log.e(name, "fatal: " + text);
    }
}

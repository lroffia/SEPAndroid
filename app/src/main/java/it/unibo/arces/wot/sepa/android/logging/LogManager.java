package it.unibo.arces.wot.sepa.android.logging;

import android.util.Log;

/**
 * Created by luca on 21/06/17.
 */
public class LogManager {

    public static Logger getLogger(String name) {
        Log.d("LogManager", "getLogger " + name);
        return new Logger(name);
    }
}

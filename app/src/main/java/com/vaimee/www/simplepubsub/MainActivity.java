package com.vaimee.www.simplepubsub;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.response.Response;

public class MainActivity extends AppCompatActivity implements  MyListener {

    // A SEPA agent can be a producer, a consumer or an aggregator
    MyProducer producer;
    MyConsumer consumer;
    MyAggregator aggregator;

    // Message number
    int messageNumber = 0;

    // Activity message handler
    private Handler mHandler;
    private final int SHOW_TEXT = 0;
    private final int SEND_MESSAGE = 1;

    private boolean sepaInit() {
        try {
            showMessage("Create producer...");
            producer = new MyProducer(this);
        } catch (SEPAProtocolException e) {
            showMessage(e.getLocalizedMessage());
            return false;
        } catch (IOException e) {
            showMessage(e.getLocalizedMessage());
            return false;
        } catch (SEPAPropertiesException e) {
            showMessage(e.getLocalizedMessage());
            return false;
        }

        try {
            showMessage("Create consumer...");
            consumer = new MyConsumer(this);
        } catch (SEPAProtocolException e) {
            showMessage(e.getLocalizedMessage());
            return false;
        } catch (IOException e) {
            showMessage(e.getLocalizedMessage());
            return false;
        } catch (SEPAPropertiesException e) {
            showMessage(e.getLocalizedMessage());
            return false;
        }
        try {
            showMessage("Create aggregator...");
            aggregator = new MyAggregator(this);
        } catch (SEPAProtocolException e) {
            showMessage(e.getLocalizedMessage());
            return false;
        } catch (IOException e) {
            showMessage(e.getLocalizedMessage());
            return false;
        } catch (SEPAPropertiesException e) {
            showMessage(e.getLocalizedMessage());
            return false;
        }

        // No forced bindings are specified by both agents
        showMessage("Consumer subscribe...");
        Response ret = consumer.subscribe(null);
        if (ret.isError()) {
            showMessage(ret.toString());
            return false;
        }
        showMessage("Aggregator subscribe...");
        aggregator.subscribe(null);
        if (ret.isError()) {
            showMessage(ret.toString());
            return false;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                switch (inputMessage.what) {
                    case SHOW_TEXT:
                        TextView text = findViewById(R.id.outputText);
                        text.append(inputMessage.obj.toString());
                        text.append("\n");
                        break;
                    case SEND_MESSAGE:
                        // To avoid network activity on main thread!
                        new Thread(){
                            public void run(){
                                producer.publish();
                            }
                        }.start();
                        break;
                }
            }
        };

        // Init SEPA agents
        if(!sepaInit()) showMessage("FAILED");
        else showMessage("SUCCESS");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = mHandler.obtainMessage(SEND_MESSAGE, "Ciao!");
                message.sendToTarget();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showMessage(String text) {
        Message message = mHandler.obtainMessage(SHOW_TEXT, text);
        message.sendToTarget();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onConnectionLost() {

    }
}

package com.vaimee.www.simplepubsub;

import java.io.IOException;

import it.unibo.arces.wot.sepa.android.logging.LogManager;
import it.unibo.arces.wot.sepa.android.logging.Logger;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.sparqlresults.ARBindingsResults;
import it.unibo.arces.wot.sepa.commons.sparqlresults.Bindings;
import it.unibo.arces.wot.sepa.commons.sparqlresults.BindingsResults;
import it.unibo.arces.wot.sepa.pattern.ApplicationProfile;
import it.unibo.arces.wot.sepa.pattern.Consumer;

/**
 * Created by luca on 05/02/18.
 *
 * The agent logs the added and removed messages. It is subscribed with the following:
 *
 * "SUBSCRIBE_TO_MESSAGES":{
 *      "sparql" : "SELECT ?message ?text ?time
 *      WHERE {?message rdf:type schema:Message ; schema:text ?text ; schema:dateSent ?time} ORDER BY ?time"}}}
 */

public class MyConsumer extends Consumer {

    Logger log = LogManager.getLogger("MyConsumer");
    MyListener mListener;

    public MyConsumer(MyListener activity) throws SEPAProtocolException, IOException, SEPAPropertiesException {
        super(new ApplicationProfile(activity.getActivity().getAssets().open("pubsub.jsap")), "SUBSCRIBE_TO_MESSAGES");
        mListener = activity;
    }

    @Override
    public void onPing() {
        log.debug("@MyConsumer::onPing");
    }

    @Override
    public void onBrokenSocket() {
        log.debug("@MyConsumer::onBrokenSocket");
        mListener.onConnectionLost();
    }

    @Override
    public void onError(ErrorResponse errorResponse) {
        log.debug("@MyConsumer::onError");
    }

    @Override
    public void onResults(ARBindingsResults results) {
        log.debug("@MyConsumer::onResults");
    }

    @Override
    public void onAddedResults(BindingsResults results) {
        log.debug("@MyConsumer::onAddedResults");
        for (Bindings bindings : results.getBindings()) {
            String text = bindings.getBindingValue("text");

            mListener.showMessage("(+) "+text);
        }
    }

    @Override
    public void onRemovedResults(BindingsResults results) {
        log.debug("@MyConsumer::onRemovedResults");

        for (Bindings bindings : results.getBindings()) {
            String message = bindings.getBindingValue("message");
            String text = bindings.getBindingValue("text");
            String time = bindings.getBindingValue("time");

            mListener.showMessage("(-) "+text);
        }

    }
}

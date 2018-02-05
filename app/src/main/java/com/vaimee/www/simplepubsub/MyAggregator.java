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
import it.unibo.arces.wot.sepa.commons.sparqlresults.RDFTermURI;
import it.unibo.arces.wot.sepa.pattern.Aggregator;
import it.unibo.arces.wot.sepa.pattern.ApplicationProfile;

/**
 * Created by luca on 05/02/18.
 *
 *  The agent is characterized by the following:
 *
 * "SUBSCRIBE_TO_MESSAGES":{
 *      "sparql" : "SELECT ?message ?text ?time
 *      WHERE {?message rdf:type schema:Message ; schema:text ?text ; schema:dateSent ?time} ORDER BY ?time"}
 *
 * "REMOVE_A_MESSAGE":{
 *      "sparql" : "DELETE {?message ?p ?o} WHERE {?message rdf:type schema:Message ; ?p ?o}",
 *      "forcedBindings":{"message":{"type":"uri"}}}
 *
 *  The agent removes the messages that have been published.
 *
 *  On every notification, the "message" binding is extracted and used as forced binding in the "REMOVE_A_MESSAGE" update
 *
 * */

public class MyAggregator extends Aggregator {
    Logger log = LogManager.getLogger("MyConsumer");
    MyListener mListener;

    public MyAggregator(MyListener activity) throws SEPAProtocolException, IOException, SEPAPropertiesException {
        super(new ApplicationProfile(activity.getActivity().getAssets().open("pubsub.jsap")), "SUBSCRIBE_TO_MESSAGES","REMOVE_A_MESSAGE");
        mListener = activity;
    }

    @Override
    public void onPing() {
        log.debug("@MyAggregator::onPing");
    }

    @Override
    public void onBrokenSocket() {
        log.debug("@MyAggregator::onBrokenSocket");
        mListener.onConnectionLost();
    }

    @Override
    public void onError(ErrorResponse errorResponse) {
        log.debug("@MyAggregator::onError");
    }

    @Override
    public void onResults(ARBindingsResults results) {
        log.debug("@MyAggregator::onResults");
    }

    @Override
    public void onAddedResults(BindingsResults results) {
        log.debug("@MyAggregator::onAddedResults");

        // Remove the published messages
        for (Bindings bindings : results.getBindings()) {
            // Extract the "message" binding value
            String message = bindings.getBindingValue("message");
            String text = bindings.getBindingValue("text");

            // Create the forced bindings
            Bindings forcedBindings = new Bindings();
            forcedBindings.addBinding("message",new RDFTermURI(message));

            mListener.showMessage("Remove message: "+text);
            // Issue the update
            update(forcedBindings);
        }
    }

    @Override
    public void onRemovedResults(BindingsResults results) {
        log.debug("@MyAggregator::onRemovedResults");
    }
}

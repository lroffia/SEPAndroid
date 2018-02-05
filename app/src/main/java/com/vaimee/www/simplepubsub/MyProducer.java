package com.vaimee.www.simplepubsub;

import java.io.IOException;

import it.unibo.arces.wot.sepa.android.logging.LogManager;
import it.unibo.arces.wot.sepa.android.logging.Logger;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.sparqlresults.Bindings;
import it.unibo.arces.wot.sepa.commons.sparqlresults.RDFTermLiteral;
import it.unibo.arces.wot.sepa.pattern.ApplicationProfile;
import it.unibo.arces.wot.sepa.pattern.Producer;

/**
 * Created by luca on 05/02/18.
 *
 * The agent publishes a new message by using the following update:
 *
 * "PUBLISH_A_MESSAGE":{
 *      "sparql" : "INSERT {?message rdf:type schema:Message ; schema:text ?text ; schema:dateSent ?time}
 *      WHERE {BIND(STR(now()) AS ?time) BIND(IRI(CONCAT(\"http://schema.org/Message-\",STRUUID())) AS ?message)}",
 *      "forcedBindings":{"text":{"type":"literal"}}}
 *
 *  A message has a text in the form "Message #n" where n is an incremental number
 */

public class MyProducer extends Producer {
    Logger log = LogManager.getLogger("MyProducer");

    int n = 0;

    public MyProducer(MyListener activity) throws SEPAProtocolException, IOException, SEPAPropertiesException {
        super(new ApplicationProfile(activity.getActivity().getAssets().open("pubsub.jsap")), "PUBLISH_A_MESSAGE");
    }

    public boolean publish() {
        // Create the forced bindings
        Bindings forcedBindings = new Bindings();
        forcedBindings.addBinding("text",new RDFTermLiteral("Message #"+n++));

        // Issue the update
        Response ret = update(forcedBindings);
        if (ret.isUpdateResponse()) log.debug("Message published");
        return ret.isUpdateResponse();
    }
}

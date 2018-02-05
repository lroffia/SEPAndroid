package it.unibo.arces.wot.sepa.api;

import java.net.URI;
import java.net.URISyntaxException;

import it.unibo.arces.wot.sepa.android.logging.LogManager;
import it.unibo.arces.wot.sepa.android.logging.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.response.Notification;
import it.unibo.arces.wot.sepa.commons.response.Response;

public class SPARQL11SEWebsocket implements ISubscriptionHandler {
    private Logger logger = LogManager.getLogger("SPARQL11SEWebsocket");

    private long TIMEOUT = 5000;

    private ISubscriptionHandler handler;
    private URI wsURI = null;
    private SEPAWebsocketClient client = null;
    private boolean connected = false;

    public SPARQL11SEWebsocket(String wsUrl, ISubscriptionHandler handler) throws URISyntaxException {
        wsURI = new URI(wsUrl);

        if (handler == null) {
            logger.fatal("Notification handler is null. Client cannot be initialized");
            throw new IllegalArgumentException("Notificaton handler is null");
        }

        this.handler = handler;
    }

    public Response subscribe(String sparql) {
        if (sparql == null)
            return new ErrorResponse(500, "SPARQL query is null");

        // Create SPARQL 1.1 Subscribe request
        JsonObject request = new JsonObject();
        request.add("subscribe", new JsonPrimitive(sparql));

        if (!connected) {
            client = new SEPAWebsocketClient(wsURI, this);

            client.connect();
            if (!client.getConnection().isOpen()) {
                synchronized (this) {
                    try {
                        wait(TIMEOUT);
                    } catch (InterruptedException e) {
                    }
                }
            }
            if (!client.getConnection().isOpen()) {
                logger.error("Not connected");
                return new ErrorResponse(408, "Not connected");
            }
            connected = true;
        }

        // Send request and wait for response
        Response ret = null;
        try {
            ret = client.sendAndReceive(request.toString(), TIMEOUT);
        } catch (WebsocketNotConnectedException e) {
            logger.error(e.getMessage());
            return new ErrorResponse(408, e.getMessage());
        }

        if (ret.isSubscribeResponse()) connected = true;

        return ret;
    }

    public Response unsubscribe(String spuid) {
        if (!connected) {
            logger.error("Not connected");
            return new ErrorResponse(408, "Not connected");
        }

        if (spuid == null)
            return new ErrorResponse(500, "SPUID is null");

        // Create SPARQL 1.1 Unsubscribe request
        JsonObject request = new JsonObject();
        if (spuid != null)
            request.add("unsubscribe", new JsonPrimitive(spuid));

        try {
            // Send request and wait for response
            return client.sendAndReceive(request.toString(), TIMEOUT);
        } catch (WebsocketNotConnectedException e) {
            logger.error(e.getMessage());
            return new ErrorResponse(408, e.getMessage());
        }
    }

    @Override
    public void onSemanticEvent(Notification notify) {
        handler.onSemanticEvent(notify);
    }

    @Override
    public void onPing() {
        handler.onPing();
    }

    @Override
    public void onBrokenSocket() {
        if (connected) {
            connected = false;
            handler.onBrokenSocket();
        }
    }

    @Override
    public void onError(ErrorResponse errorResponse) {
        handler.onError(errorResponse);
    }
}

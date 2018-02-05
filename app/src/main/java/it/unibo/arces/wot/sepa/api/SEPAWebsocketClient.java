package it.unibo.arces.wot.sepa.api;

import java.net.URI;

import it.unibo.arces.wot.sepa.android.logging.LogManager;
import it.unibo.arces.wot.sepa.android.logging.Logger;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.response.Notification;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.response.SubscribeResponse;
import it.unibo.arces.wot.sepa.commons.response.UnsubscribeResponse;

public class SEPAWebsocketClient extends WebSocketClient {
    protected static Logger logger = LogManager.getLogger("SEPAWebsocketClient");

    private ISubscriptionHandler handler;
    private Response response = new ErrorResponse(500, "null");
    private boolean responseReceived = false;

    public SEPAWebsocketClient(URI wsUrl, ISubscriptionHandler handler) {
        super(wsUrl);

        if (handler == null) {
            logger.fatal("Notification handler is null. Client cannot be initialized");
            throw new IllegalArgumentException("Notificaton handler is null");
        }

        this.handler = handler;
    }

    private synchronized Response waitResponse(long timeout) {
        if (!responseReceived)
            try {
                wait(timeout);
            } catch (InterruptedException e) {
                return new ErrorResponse(500, e.getMessage());
            }

        return response;
    }

    private synchronized void setResponse() {
        responseReceived = true;
        notify();
    }

    public Response sendAndReceive(String message, long timeout) {
        // Send request and wait response
        responseReceived = false;
        send(message);
        return waitResponse(timeout);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.debug("@onOpen");
        synchronized (handler) {
            handler.notify();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.debug("@onClose code: " + code + " reason: " + reason + " remote: " + remote);
        if (handler != null) handler.onBrokenSocket();
    }

    @Override
    public void onError(Exception ex) {
        ErrorResponse error = new ErrorResponse(500, ex.getMessage());
        logger.debug("@onError: " + error);
        if (handler != null) handler.onError(error);
    }

    @Override
    public void onMessage(String message) {
        logger.debug("@onMessage " + message);

        // Parse message
        JsonObject notify = new JsonParser().parse(message).getAsJsonObject();

        if (notify.get("ping") != null) {
            if (handler != null) handler.onPing();
        } else if (notify.get("subscribed") != null) {
            response = new SubscribeResponse(notify);
            setResponse();
        } else if (notify.get("unsubscribed") != null) {
            response = new UnsubscribeResponse(notify);
            setResponse();
        } else if (notify.get("results") != null) {
            if (handler != null) handler.onSemanticEvent(new Notification(notify));
        } else if (notify.get("code") != null) {
            if (handler != null) handler.onError(new ErrorResponse(notify));
        } else
            logger.error("Unknown message: " + message);
    }
}

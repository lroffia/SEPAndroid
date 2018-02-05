/* This class implements the SPARQL 1.1 Protocol (https://www.w3.org/TR/sparql11-protocol/)
 * 
 * Author: Luca Roffia (luca.roffia@unibo.it)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package it.unibo.arces.wot.sepa.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import it.unibo.arces.wot.sepa.commons.exceptions.SEPAProtocolException;
import it.unibo.arces.wot.sepa.api.SPARQL11Properties.HTTPMethod;
import it.unibo.arces.wot.sepa.commons.request.QueryRequest;
import it.unibo.arces.wot.sepa.commons.request.Request;
import it.unibo.arces.wot.sepa.commons.request.UpdateRequest;
import it.unibo.arces.wot.sepa.commons.response.ErrorResponse;
import it.unibo.arces.wot.sepa.commons.response.Response;
import it.unibo.arces.wot.sepa.commons.response.UpdateResponse;

import it.unibo.arces.wot.sepa.android.logging.LogManager;
import it.unibo.arces.wot.sepa.android.logging.Logger;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * This class implements the SPARQL 1.1 Protocol
 */

public class SPARQL11Protocol {

    /**
     * The Constant logger.
     */
    private static final Logger logger = LogManager.getLogger("SPARQL11Protocol");

    /**
     * The m bean name.
     */
    protected static String mBeanName = "arces.unibo.SEPA.server:type=SPARQL11Protocol";

    /**
     * The properties.
     */
    protected SPARQL11Properties properties;

    // HTTP fields
    final URL queryUrl;
    final URL updateUrl;


    public SPARQL11Protocol(SPARQL11Properties properties) throws SEPAProtocolException {
        if (properties == null) {
            logger.fatal("Properties are null");
            throw new SEPAProtocolException(new IllegalArgumentException("Properties are null"));
        }
        this.properties = properties;

        // Create update POST request
        try {
            updateUrl = new URL("http://" + properties.getHost() + ":" + properties.getHttpPort() + properties.getUpdatePath());
            queryUrl = new URL("http://" + properties.getHost() + ":" + properties.getHttpPort() + properties.getQueryPath());
        } catch (IOException e) {
            throw new SEPAProtocolException(e);
        }
    }

    /**
     * Implements a SPARQL 1.1 update operation
     * (https://www.w3.org/TR/sparql11-protocol/)
     * <p>
     * <pre>
     * update via URL-encoded POST
     * - HTTP Method: POST
     * - Query String Parameters: None
     * - Request Content Type: <b>application/x-www-form-urlencoded</b>
     * - Request Message Body: URL-encoded, ampersand-separated query parameters. <b>update</b> (exactly 1). using-graph-uri (0 or more). using-named-graph-uri (0 or more)
     *
     * update via POST directly
     * - HTTP Method: POST
     * - Query String parameters: using-graph-uri (0 or more); using-named-graph-uri (0 or more)
     * - Request Content Type: <b>application/sparql-update</b>
     * - Request Message Body: Unencoded SPARQL update request string
     * </pre>
     * <p>
     * UPDATE 2.2 update operation The response to an update request indicates
     * success or failure of the request via HTTP response status code.
     */
    public Response update(UpdateRequest req, int timeout) {
        return post(req, timeout, true);
    }

    private Response post(Request req, int timeout, boolean update) {
        int responseCode = 0;
        String responseBody = null;
        String requestBody = null;
        HttpURLConnection request = null;

        try {
            if (update) {
                request = (HttpURLConnection) updateUrl.openConnection();
                request.setRequestProperty("Accept", properties.getUpdateAcceptHeader());
                request.setRequestProperty("Content-Type", properties.getUpdateContentTypeHeader());
            } else {
                request = (HttpURLConnection) queryUrl.openConnection();
                request.setRequestProperty("Accept", properties.getQueryAcceptHeader());
                request.setRequestProperty("Content-Type", properties.getQueryContentTypeHeader());
            }
            request.setRequestMethod("POST");
            request.setDoOutput(true);
            request.setDoInput(true);
            request.setConnectTimeout(timeout);
            request.setReadTimeout(timeout);

            if (update) {
                if (properties.getUpdateMethod().equals(HTTPMethod.URL_ENCODED_POST)) {
                    requestBody = "update=" + URLEncoder.encode(req.getSPARQL(), "UTF-8");
                } else {
                    requestBody = req.getSPARQL();
                }
            } else {
                if (properties.getQueryMethod().equals(HTTPMethod.URL_ENCODED_POST)) {
                    requestBody = "query=" + URLEncoder.encode(req.getSPARQL(), "UTF-8");
                } else {
                    requestBody = req.getSPARQL();
                }
            }
            request.setFixedLengthStreamingMode(requestBody.length());

            request.connect();

            OutputStream os = request.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(requestBody);
            writer.flush();
            writer.close();
            os.close();

            responseCode = request.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            request.getInputStream()));
            StringBuffer sb = new StringBuffer("");
            String line = "";

            while ((line = in.readLine()) != null) {

                sb.append(line);
                break;
            }

            in.close();
            responseBody = sb.toString();

            if (responseCode >= 400) {
                try {
                    return new ErrorResponse(req.getToken(), new JsonParser().parse(responseBody).getAsJsonObject());
                } catch (JsonParseException e) {
                    return new ErrorResponse(req.getToken(), responseCode, responseBody);
                }
            }

            return new UpdateResponse(req.getToken(), responseBody);
        } catch (IOException e) {
            return new ErrorResponse(500, e.getMessage());
        } finally {
            if (request != null) request.disconnect();
        }
    }

    /**
     * Implements a SPARQL 1.1 query operation
     * (https://www.w3.org/TR/sparql11-protocol/)
     * <p>
     * <pre>
     * query via GET
     * - HTTP Method: GET
     * - Query String Parameters: <b>query</b> (exactly 1). default-graph-uri (0 or more). named-graph-uri (0 or more)
     * - Request Content Type: None
     * - Request Message Body: None
     *
     * query via URL-encoded POST
     * - HTTP Method: POST
     * - Query String Parameters: None
     * - Request Content Type: <b>application/x-www-form-urlencoded</b>
     * - Request Message Body: URL-encoded, ampersand-separated query parameters. <b>query</b> (exactly 1). default-graph-uri (0 or more). named-graph-uri (0 or more)
     *
     * query via POST directly
     * - HTTP Method: POST
     * - Query String parameters: default-graph-uri (0 or more). named-graph-uri (0 or more)
     * - Request Content Type: <b>application/sparql-query</b>
     * - Request Message Body: Unencoded SPARQL update request string
     *
     * QUERY 2.1.5 Accepted Response Formats
     *
     * Protocol clients should use HTTP content negotiation [RFC2616] to request
     * response formats that the client can consume. See below for more on
     * potential response formats.
     *
     * 2.1.6 Success Responses
     *
     * The SPARQL Protocol uses the response status codes defined in HTTP to
     * indicate the success or failure of an operation. Consult the HTTP
     * specification [RFC2616] for detailed definitions of each status code.
     * While a protocol service should use a 2XX HTTP response code for a
     * successful query, it may choose instead to use a 3XX response code as per
     * HTTP.
     *
     * The response body of a successful query operation with a 2XX response is
     * either:
     *
     * a SPARQL Results Document in XML, JSON, or CSV/TSV format (for SPARQL
     * Query forms SELECT and ASK); or, an RDF graph [RDF-CONCEPTS] serialized,
     * for example, in the RDF/XML syntax [RDF-XML], or an equivalent RDF graph
     * serialization, for SPARQL Query forms DESCRIBE and CONSTRUCT). The
     * content type of the response to a successful query operation must be the
     * media type defined for the format of the response body.
     *
     * 2.1.7 Failure Responses
     *
     * The HTTP response codes applicable to an unsuccessful query operation
     * include:
     *
     * 400 if the SPARQL query supplied in the request is not a legal sequence
     * of characters in the language defined by the SPARQL grammar; or, 500 if
     * the service fails to execute the query. SPARQL Protocol services may also
     * return a 500 response code if they refuse to execute a query. This
     * response does not indicate whether the server may or may not process a
     * subsequent, identical request or requests. The response body of a failed
     * query request is implementation defined. Implementations may use HTTP
     * content negotiation to provide human-readable or machine-processable (or
     * both) information about the failed query request.
     *
     * A protocol service may use other 4XX or 5XX HTTP response codes for other
     * failure conditions, as per HTTP.
     *
     * </pre>
     */
    public Response query(QueryRequest req, int timeout) {
        if (properties.getQueryMethod().equals(HTTPMethod.GET)) return get(req, timeout);
        else return post(req, timeout, false);
    }

    private Response get(QueryRequest req, int timeout) {
        int responseCode = 0;
        String responseBody = null;
        HttpURLConnection request = null;

        String urlQuery = null;

        try {
            urlQuery = URLEncoder.encode("?query=" + req.getSPARQL(), "UTF-8");
            URL queryUrl = new URL("http://" + properties.getHost() + ":" + properties.getHttpPort() + properties.getQueryPath() + urlQuery);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            return new ErrorResponse(500, e.getMessage());
        }

        try {
            request = (HttpURLConnection) queryUrl.openConnection();
            request.setRequestProperty("Accept", properties.getQueryAcceptHeader());
            request.setRequestProperty("Content-Type", properties.getQueryContentTypeHeader());

            request.setRequestMethod("GET");
            request.setConnectTimeout(timeout);
            request.setReadTimeout(timeout);

            responseCode = request.getResponseCode();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            request.getInputStream()));
            StringBuffer sb = new StringBuffer("");
            String line = "";

            while ((line = in.readLine()) != null) {

                sb.append(line);
                break;
            }

            in.close();
            responseBody = sb.toString();

            if (responseCode >= 400) {
                try {
                    return new ErrorResponse(req.getToken(), new JsonParser().parse(responseBody).getAsJsonObject());
                } catch (JsonParseException e) {
                    return new ErrorResponse(req.getToken(), responseCode, responseBody);
                }
            }

            return new UpdateResponse(req.getToken(), responseBody);
        } catch (IOException e) {
            return new ErrorResponse(500, e.getMessage());
        } finally {
            request.disconnect();
        }
    }
}

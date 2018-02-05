/* This class implements a JSON parser of an .sap file
 * 
 * Author: Luca Roffia (luca.roffia@unibo.it)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package it.unibo.arces.wot.sepa.pattern;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import it.unibo.arces.wot.sepa.android.logging.LogManager;
import it.unibo.arces.wot.sepa.android.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unibo.arces.wot.sepa.api.SPARQL11SEProperties;
import it.unibo.arces.wot.sepa.commons.exceptions.SEPAPropertiesException;
import it.unibo.arces.wot.sepa.commons.sparqlresults.Bindings;
import it.unibo.arces.wot.sepa.commons.sparqlresults.RDFTerm;
import it.unibo.arces.wot.sepa.commons.sparqlresults.RDFTermLiteral;
import it.unibo.arces.wot.sepa.commons.sparqlresults.RDFTermURI;

/**
 * {"parameters":
 * {"host":"www.vaimee.com",
 * "ports":{"http":8000,"https":8443,"ws":9000,"wss":9443},
 * "paths":{"query":"/query","update":"/update","subscribe":"/subscribe","register":"/oauth/register","tokenRequest":"/oauth/token","securePath":"/secure"},
 * "methods":{"query":"POST","update":"URL_ENCODED_POST"},"formats":{"query":"JSON","update":"HTML"},
 * "security":{"client_id":"...","client_secret":"...","jwt":"...","expires":"...","type":"..."}},
 * "namespaces":{"chat":"http://wot.arces.unibo.it/chat#","rdf":"http://www.w3.org/1999/02/22-rdf-syntax-ns#"},
 * <p>
 * "updates":{
 * "SEND_MESSAGE":{"sparql":"INSERT {?message rdf:type chat:Message ; chat:text ?text ; chat:from ?sender ; chat:to ?receiver; chat:time ?time} WHERE { BIND(now() AS ?time) BIND(IRI(CONCAT(\"chat:Message-\",STRUUID())) AS ?message)}",
 * "forcedBindings":{"text":{"type":"literal","value":""},"sender":{"type":"literal","value":""},"receiver":{"type":"literal","value":""}}},
 * <p>
 * "DELETE_MESSAGES":{"sparql":"DELETE {?message rdf:type chat:Message ; chat:text ?text ; chat:from ?sender ; chat:to ?receiver; chat:time ?time} WHERE {?message rdf:type chat:Message ; chat:text ?text ; chat:from ?sender ; chat:to ?receiver; chat:time ?time}",
 * "forcedBindings":{"receiver":{"type":"literal","value":""}}}},
 * <p>
 * "queries":{
 * "RECEIVE_MESSAGE":{"sparql":"SELECT ?sender ?text ?time WHERE {?message rdf:type chat:Message ; chat:text ?text ; chat:from ?sender ; chat:to ?receiver; chat:time ?time} ORDER BY ?time",
 * "forcedBindings":{"receiver":{"type":"literal","value":""}}}}}
 */

public class ApplicationProfile extends SPARQL11SEProperties {
    protected Logger logger = LogManager.getLogger("JSAP");

    public ApplicationProfile(InputStream in) throws SEPAPropertiesException {
        super(in);
    }

    public ApplicationProfile(InputStream in, byte[] secret) throws SEPAPropertiesException {
        super(in, secret);
    }

    public JsonObject getExtendedData() {
        if (doc.get("extended") == null) return null;
        return doc.get("extended").getAsJsonObject();
    }

    /**
     * "updates": {
     * "ADD_PERSON":{
     * "sparql":"INSERT DATA { ?person rdf:type iot:Person . ?person iot:hasName ?name }",
     * "forcedBindings": {
     * "person" : {"type":"uri", "value":""},
     * "name" : {"type":"literal", "value":""}}}
     * },
     */
    public String update(String updateID) {
        JsonElement elem = null;
        if ((elem = doc.get("updates")) != null)
            if ((elem = elem.getAsJsonObject().get(updateID)) != null)
                if ((elem = elem.getAsJsonObject().get("sparql")) != null)
                    return elem.getAsString();
        return null;
    }

    public String subscribe(String subscribeID) {
        JsonElement elem = null;
        if ((elem = doc.get("queries")) != null)
            if ((elem = elem.getAsJsonObject().get(subscribeID)) != null)
                if ((elem = elem.getAsJsonObject().get("sparql")) != null)
                    return elem.getAsString();
        return null;
    }

    public Set<String> getUpdateIds() {
        JsonElement elem;
        HashSet<String> ret = new HashSet<String>();
        if ((elem = doc.get("updates")) != null)
            for (Entry<String, JsonElement> key : elem.getAsJsonObject().entrySet()) {
                ret.add(key.getKey());
            }
        return ret;
    }

    public Set<String> getSubscribeIds() {
        JsonElement elem;
        HashSet<String> ret = new HashSet<String>();
        if ((elem = doc.get("queries")) != null)
            for (Entry<String, JsonElement> key : elem.getAsJsonObject().entrySet()) {
                ret.add(key.getKey());
            }
        return ret;
    }

    /**
     * "forcedBindings": {
     * "person" : {"type":"uri", "value":""},
     * "name" : {"type":"literal", "value":""}}}
     *
     * @param selectedValue
     * @return
     */
    public Bindings updateBindings(String selectedValue) {
        JsonElement elem;
        Bindings ret = new Bindings();
        if ((elem = doc.get("updates")) != null)
            if ((elem = elem.getAsJsonObject().get(selectedValue)) != null)
                if ((elem = elem.getAsJsonObject().get("forcedBindings")) != null) {
                    for (Entry<String, JsonElement> binding : elem.getAsJsonObject().entrySet()) {
                        JsonObject value = binding.getValue().getAsJsonObject();
                        RDFTerm bindingValue = null;

                        if (value.get("type") != null) {
                            if (value.get("type").getAsString().equals("uri")) {
                                bindingValue = new RDFTermURI(value.get("value").getAsString());
                            } else {
                                bindingValue = new RDFTermLiteral(value.get("value").getAsString());
                            }
                        }
                        ret.addBinding(binding.getKey(), bindingValue);
                    }
                }
        return ret;
    }

    public Bindings subscribeBindings(String selectedValue) {
        JsonElement elem;
        Bindings ret = new Bindings();
        if ((elem = doc.get("queries")) != null)
            if ((elem = elem.getAsJsonObject().get(selectedValue)) != null)
                if ((elem = elem.getAsJsonObject().get("forcedBindings")) != null) {
                    for (Entry<String, JsonElement> binding : elem.getAsJsonObject().entrySet()) {
                        JsonObject value = binding.getValue().getAsJsonObject();
                        RDFTerm bindingValue = null;

                        if (value.get("type") != null) {
                            if (value.get("type").getAsString().equals("uri")) {
                                bindingValue = new RDFTermURI(value.get("value").getAsString());
                            } else {
                                bindingValue = new RDFTermLiteral(value.get("value").getAsString());
                            }
                        }
                        ret.addBinding(binding.getKey(), bindingValue);
                    }
                }
        return ret;
    }

    /**
     * "namespaces" : { "iot":"http://www.arces.unibo.it/iot#","rdf":"http://www.w3.org/1999/02/22-rdf-syntax-ns#"},
     */

    public Set<String> getPrefixes() {
        JsonElement elem;
        HashSet<String> ret = new HashSet<String>();
        if ((elem = doc.get("namespaces")) != null)
            for (Entry<String, JsonElement> key : elem.getAsJsonObject().entrySet()) {
                ret.add(key.getKey());
            }
        return ret;
    }

    public String getNamespaceURI(String prefix) {
        JsonElement elem;
        String ret = null;
        if ((elem = doc.get("namespaces")) != null)
            if ((elem = elem.getAsJsonObject().get(prefix)) != null)
                return elem.getAsString();
        return ret;
    }

    public String getFileName() {
        return propertiesFile;
    }

    public String printParameters() {
        return parameters.toString();
    }
}

/* This class represents a blank node RDF term
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

package it.unibo.arces.wot.sepa.commons.sparqlresults;

import com.google.gson.JsonPrimitive;

// TODO: Auto-generated Javadoc

/**
 * The Class RDFTermBNode.
 */
public class RDFTermBNode extends RDFTerm {

    /**
     * Instantiates a new RDF term B node.
     *
     * @param value the value
     */
    public RDFTermBNode(String value) {
        super(value);

        json.add("type", new JsonPrimitive("bnode"));
    }
}

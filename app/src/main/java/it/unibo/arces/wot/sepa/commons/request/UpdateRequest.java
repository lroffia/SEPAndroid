/* This class represents an update request
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

package it.unibo.arces.wot.sepa.commons.request;

import it.unibo.arces.wot.sepa.commons.request.Request;

// TODO: Auto-generated Javadoc

/**
 * This class represents the request to perform a SPARQL 1.1 Update
 */

public class UpdateRequest extends Request {

    /**
     * Instantiates a new update request.
     *
     * @param token  the token
     * @param sparql the sparql
     */
    public UpdateRequest(Integer token, String sparql) {
        super(token, sparql);
    }

    /**
     * Instantiates a new update request.
     *
     * @param sparql the sparql
     */
    public UpdateRequest(String sparql) {
        super(sparql);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (token != -1) return "UPDATE #" + token + " " + sparql;
        return "UPDATE " + sparql;
    }
}

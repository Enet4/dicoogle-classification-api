/**
 * Copyright (C) 2016  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
 *
 * This file is part of Dicoogle/dicoogle-classification-api.
 *
 * Dicoogle/dicoogle-classification-api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Dicoogle/dicoogle-classification-api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Dicoogle.  If not, see <http://www.gnu.org/licenses/>.
 */
package pt.ua.dicoogle.classification.api;

import pt.ua.dicoogle.sdk.QueryInterface;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/** Interface for Dicoogle classifiers under the form of query interface plugins.
 * @author Eduardo Pinho
 */
public interface ClassifierPlugin extends QueryInterface {
    /** Perform a classification on the given item
     *
     * @param query the classification criterion. This is usually an identifier of the class family.
     * @param parameters the var-arg parameters, where the first element represents the item to be
     *                   classified. If this element is a URI or a string representing the same URI, the
     *                   respective item in storage will be retrieved and converted for classification.
     *                   Otherwise, a simple cast to the internal representation is attempted.
     * @return a collection of predictions, where the result's URI has the format
     *         `class:/<classifier>/<criterion>#<class>` and the probability is kept in the result's score.
     */
    @Override
    public Collection<SearchResult> query(String query, Object... parameters);

}

/**
 * Copyright (C) 2014  Universidade de Aveiro, DETI/IEETA, Bioinformatics Group - http://bioinformatics.ua.pt/
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;

/**
 *
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public class Classifiers {

    private Classifiers() {}

    /** Convert a classifier's dictionary of predictions into a collection of results.
     *
     * @param classifierName the unique name of the classifier
     * @param predictions the predictions
     * @return a Dicoogle classification compliant collection of results containing the predictions
     */
    public static Collection<SearchResult> toResult(String classifierName, Map<String, Double> predictions) {
        return predictions.entrySet().stream()
                // using URI for keeping classifier and prediction + probability in score
                .map(e -> new SearchResult(PredictionIdentifier.toURI(classifierName, e.getKey()), e.getValue(), new HashMap<>(2)))
                .collect(Collectors.toList());
    }

    /** Convert a collection of results back into a dictionary of predictions.
     *
     * @param predictions the predictions
     * @return a Dicoogle classification compliant collection of results containing the predictions
     */
    public static Map<URI, Double> toDictionary(Collection<SearchResult> predictions) {
        return predictions.stream()
                .collect(HashMap::new,
                        (m, r) -> m.put(r.getURI(), r.getScore()),
                        (a,b) -> a.putAll(b));
    }

}

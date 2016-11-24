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
/*
 */
package pt.ua.dicoogle.classification.api;

import java.util.Map;

/** Generic interface for classifiers in Dicoogle.
 *
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public interface ClassifierInterface<D> {

    /** Perform a classification on the given item
     *
     * @param criterion the classification criterion. This is an identifier of the class set.
     * @param item the item to be classified. if it's a String or a URI, it should be
     * interpreted as the URI of an item in storage.
     * @param parameters additional var-arg parameters for miscellaneous options
     * @return a mapping of predictions, where the key is the class and the value is the respective probability
     */
    public Map<String, Double> predict(String criterion, D item, Object... parameters);

    /** Adjust the datapoint to this classifier's specifications (e.g. image dimensions).
     *
     * @param item the data point to classify
     * @return a datapoint, possibly the same object, with adjusted parameters
     */
    public default D adapt(D item) {
        return item;
    }
}

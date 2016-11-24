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

import java.io.Serializable;
import java.util.Collection;

/** Base type for classifier descriptors. These serializable instances allow plugins to lazily instantiate classifiers
 * by keeping all of the necessary parameters in a single object.
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public interface ClassifierDescriptor extends Serializable {

    /** Getter for the classifier's name.
     *
     * @return a unique name for the classifier
     */
    public String getName();

    /** Getter for the classifier's list of terms that are used as the classification criteria.
     *
     * @return a preferably immutable collection of criterion terms, usually of all possible prediction class family names
     */
    public Collection<String> getCriteria();

    /**
     * @return whether to immediately instantiate a new classifier on registration
     */
    public boolean isPreload();
}
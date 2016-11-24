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
package pt.ua.dicoogle.classification.api.condition;

import pt.ua.dicoogle.classification.api.util.AxialType;

import java.util.Objects;

/**
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public class CustomDicomConditions {

    private CustomDicomConditions() {
    }

    public static DicomCondition axialTypeEquals(String axialType) {
        Objects.requireNonNull(axialType);
        AxialType t = AxialType.valueOf(axialType.toUpperCase());
        return obj -> AxialType.getCTAxialType(obj).orElse(null) == t;
    }
}

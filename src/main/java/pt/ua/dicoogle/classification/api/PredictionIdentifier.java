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

import java.net.URI;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public class PredictionIdentifier {
    private final String classifierName;
    private final String criterion;
    private final String predictionClass;

    private static final Pattern PREDICTION =
            Pattern.compile("^([^:/?#]+)/([^:/?#]+(?:/[^:/?#]+)*)#([^:/?#]+)$");

    public PredictionIdentifier(String classifierName, String criterion, String predictionClass) {
        Objects.requireNonNull(classifierName);
        Objects.requireNonNull(criterion);
        Objects.requireNonNull(predictionClass);
        this.classifierName = classifierName;
        this.criterion = criterion;
        this.predictionClass = predictionClass;
    }

    public String getClassifierName() {
        return classifierName;
    }

    public String getCriterion() {
        return criterion;
    }

    public String getPredictionClass() {
        return predictionClass;
    }

    public static PredictionIdentifier decompose(URI uri) {
        Objects.requireNonNull(uri);
        if (!uri.getScheme().equalsIgnoreCase("class")) {
            throw new IllegalArgumentException("Invalid prediction identifier: bad scheme");
        }
        String text = uri.toString().substring("class://".length());
        Matcher m = PREDICTION.matcher(text);
        if (!m.matches()) {
            throw new IllegalArgumentException(
                    "Invalid prediction identifier: URI \"" + uri + "\" is not in a valid format");
        }

        MatchResult res = m.toMatchResult();
        String classifierName, criterion, prediction;

        classifierName = res.group(1);
        criterion = res.group(2);
        prediction = res.group(3);
        return new PredictionIdentifier(classifierName, criterion, prediction);
    }

    /** Obtain a textual representation of this prediction identifier.
     *
     * @return the identifying URI converted to a string
     */
    @Override
    public String toString() {
        return this.toURI().toString();
    }

    public static URI toURI(String classifierName, String rest) {
        return URI.create("class://" + classifierName + "/" + rest);
    }

    public static URI toURI(String classifierName, String criterion, String prediction) {
        return URI.create("class://" + classifierName + "/" + criterion + "#" + prediction);
    }

    public URI toURI() {
        return URI.create("class://" + classifierName + "/" + criterion + "#" + predictionClass);
    }

    public static String getPredictedClass(URI uri) {
        return uri.getFragment();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PredictionIdentifier that = (PredictionIdentifier) o;
        return Objects.equals(classifierName, that.classifierName) &&
                Objects.equals(criterion, that.criterion) &&
                Objects.equals(predictionClass, that.predictionClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classifierName, criterion, predictionClass);
    }
}

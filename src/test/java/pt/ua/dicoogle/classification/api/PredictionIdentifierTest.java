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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public class PredictionIdentifierTest {

    private static final Collection<Pair<URI, PredictionIdentifier>> TRUTH = Arrays.asList(
            Pair.of(URI.create("class://I/AM#PRED"),
                    new PredictionIdentifier("I", "AM", "PRED")),
            Pair.of(URI.create("class://my-classifier/modality#CT"),
                    new PredictionIdentifier("my-classifier", "modality", "CT")),
            Pair.of(URI.create("CLASS://my-classifier/modality#US"),
                    new PredictionIdentifier("my-classifier", "modality", "US")),
            Pair.of(URI.create("Class://_classificador_/1/2/3#0"),
                    new PredictionIdentifier("_classificador_", "1/2/3", "0"))
    );

    @Test
    public void testBadIds() throws URISyntaxException {
        try {
            PredictionIdentifier.decompose(new URI("class://something/is/missing"));
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            // ok
        }

        try {
            PredictionIdentifier.decompose(new URI("Relocate://to/San#Francisco"));
            Assert.fail();
        } catch (IllegalArgumentException ex) {
            // ok
        }
    }

    @Test
    public void testWithTruth() {
        int i = 0;
        for (Pair<URI, PredictionIdentifier> p : TRUTH) {
            try {
                PredictionIdentifier id = PredictionIdentifier.decompose(p.getLeft());
                Assert.assertEquals("Decompose #" + i, p.getRight(), id);
                URI u = id.toURI();
                Assert.assertEquals("ToURI #" + i, p.getLeft(), u);
                i++;
            } catch (IllegalArgumentException ex) {
                throw new AssertionError("Failed at truthy pair #" + i, ex);
            }
        }
    }

}

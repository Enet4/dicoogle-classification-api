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

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomCodingException;
import org.dcm4che2.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ua.dicoogle.sdk.StorageInputStream;
import pt.ua.dicoogle.sdk.StorageInterface;
import pt.ua.dicoogle.sdk.core.DicooglePlatformInterface;
import pt.ua.dicoogle.sdk.core.PlatformCommunicatorInterface;
import pt.ua.dicoogle.sdk.datastructs.SearchResult;
import pt.ua.dicoogle.sdk.settings.ConfigurationHolder;

/** Utility abstract class for the integration of classifiers in Dicoogle.
 *
 * @param <B> a suitable representation type for the data points being classified
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public abstract class AbstractClassifierPlugin<B> implements ClassifierInterface<B>, ClassifierPlugin, PlatformCommunicatorInterface {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Access to the Dicoogle platform, which is automatically injected by the system.
     */
    protected DicooglePlatformInterface platform = null;
    protected boolean enabled = true;
    protected ConfigurationHolder settings = null;

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
    public Collection<SearchResult> query(String query, Object... parameters) {
        try {
            if (parameters.length < 1) {
                throw new IllegalArgumentException("Missing item for classification in parameters[0]");
            }
            Object item = parameters[0];
            Object[] restArgs = Arrays.copyOfRange(parameters, 1, parameters.length);
            if (item instanceof URI) {
                return Classifiers.toResult(this.getName(), this.predict(query, (URI) item, restArgs));
            } else if (item instanceof String) {
                return Classifiers.toResult(this.getName(), this.predict(query, URI.create((String) item), restArgs));
            } else if (item instanceof DicomObject) {
                return Classifiers.toResult(this.getName(), this.predict(query, ((DicomObject) item), restArgs));
            } else {
                return Classifiers.toResult(this.getName(), this.predict(query, (B) item, restArgs));
            }
        } catch (RuntimeException ex) {
            logger.warn("Unexpected failure", ex);
            return Collections.EMPTY_LIST;
        }
    }

    /** Perform a classification on the given item.
     * 
     * @param criterion the classification criterion. This is an identifier of the class set.
     * @param item the URI of the item to be classified
     * @param parameters additional var-arg parameters for miscellaneous options
     * @return a dictionary of predicted values and respective scores
     * @throws java.util.NoSuchElementException if the item does not exist
     */
    public Map<String, Double> predict(String criterion, URI item, Object... parameters) {
        try (DicomInputStream dcmStream = new DicomInputStream(this.fromStorage(item).get().getInputStream())) {
            DicomObject obj = dcmStream.readDicomObject();
            if (this.canClassify(criterion, obj, parameters)) {
                return this.predict(criterion, obj, parameters);
            }
            // silently leave
            return Collections.EMPTY_MAP;
        } catch (DicomCodingException ex) {
            logger.warn("Ignoring non-DICOM (or corrupted) file {}, ignoring");
            return Collections.EMPTY_MAP;
        } catch (IOException ex) {
            logger.warn("Failed to classify {} for {}", item, criterion, ex);
            return Collections.EMPTY_MAP;
        }
    }

    /** Perform a classification on the given DICOM object.
     *
     * @param criterion the classification criterion. This is an identifier of the class set.
     * @param obj a DICOM object
     * @param parameters additional var-arg parameters for miscellaneous options
     * @return a dictionary of predicted values and respective scores
     * @throws java.util.NoSuchElementException if the item does not exist
     */
    public Map<String, Double> predict(String criterion, DicomObject obj, Object... parameters) {
        try {
            return this.predict(criterion, this.toDataPoint(obj), parameters);
        } catch (IOException ex) {
            logger.warn("Failed to classify DICOM object for {}", criterion, ex);
            return Collections.EMPTY_MAP;
        }
    }

    /** Check whether this classifier can carry on with classifying the given DICOM object.
     *
     * @param criterion the classification criterion. This is an identifier of the class set.
     * @param obj a DICOM object
     * @param parameters additional var-arg parameters for miscellaneous options
     * @return true iff this classifier can provide valid results with the given DICOM object
     */
    protected abstract boolean canClassify(String criterion, DicomObject obj, Object... parameters);

    /** Obtain a data point of the retrieved resource based on the given Dicoogle object. The
     * object may have already been obtained from a Dicoogle storage item. The use of this method
     * may therefore reduce the number of storage item reads.
     *
     * @param dicomObject a DICOM object
     * @return a data point, ready to be tested on a predictor
     */
    protected abstract B toDataPoint(DicomObject dicomObject) throws IOException;

    /** Perform a classification on the given item
     *
     * @param criterion the classification criterion. This is an identifier of the class set.
     * @param item the item to be classified. if it's a String or a URI, it should be
     * interpreted as the URI of an item in storage.
     * @param parameters additional var-arg parameters for miscellaneous options
     * @return a dictionary of predicted values and respective scores
     */
    @Override
    public abstract Map<String, Double> predict(String criterion, B item, Object... parameters);

    protected Optional<StorageInputStream> fromStorage(URI item) {
        StorageInterface store = this.platform.getStorageForSchema(item);
        if (store == null) {
            return Optional.empty();
        }
        for (StorageInputStream o : store.at(item)) {
            return Optional.of(o);
        }
        return Optional.empty();
    }

    @Override
    public boolean enable() {
        this.enabled = true;
        return true;
    }

    @Override
    public boolean disable() {
        this.enabled = false;
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setSettings(ConfigurationHolder configurationHolder) {
        this.settings = configurationHolder;
    }

    @Override
    public ConfigurationHolder getSettings() {
        return this.settings;
    }

    @Override
    public abstract String getName();

    @Override
    public void setPlatformProxy(DicooglePlatformInterface core) {
        this.platform = core;
    }

}

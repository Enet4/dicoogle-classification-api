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

/**
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ua.dicoogle.classification.api.util.LazyLoader;

import java.util.*;
import java.util.stream.Stream;

/** This abstract manager instantiates, destroys and delegates tasks to classifiers. This class implements classifier
 * selection based on classification criteria, making it possible for a Dicoogle classifier plugin to withhold multiple
 * classifiers. It also supports multi-criterion prediction in a single call, by using comma-separated criteria.
 * @param <C> the concrete type of classifiers to manage
 * @param <B> a suitable representation type for the data points being classified
 * @param <D> a possible sub-type for classifier descriptors
 * @author Eduardo Pinho
 */
public abstract class AbstractClassifierManager<C extends ClassifierInterface<B>, B, D extends ClassifierDescriptor> extends AbstractClassifierPlugin<B> implements ClassifierManager<C, B, D> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractClassifierManager.class);

    private final Map<String, LazyLoader<C>> classifiers = new HashMap<>();
    private final Map<String, String> byCriterion = new HashMap<>();

    @Override
    public void register(D descriptor) {
        Objects.requireNonNull(descriptor);

        String name = descriptor.getName();
        logger.debug("Registering classifier {} ...", name);
        LazyLoader<C> l = new LazyLoader<>(() -> this.createClassifier(descriptor));
        if (descriptor.isPreload()) {
            logger.debug("Preloading classifier {} ...", name);
            l.get();
        }
        this.classifiers.put(name, l);
        for (String cr : descriptor.getCriteria()) {
            if (this.byCriterion.containsKey(cr)) {
                logger.warn("Classifier criterion collision! Will use {} instead of {} for '{}'",
                        name, this.byCriterion.get(cr), cr);
            }
            this.byCriterion.put(cr, name);
        }
    }

    /** Destroy a particular classifier instance.
     * @param name the classifier's unique name
     */
    @Override
    public void reset(String name) {
        if (!this.classifiers.containsKey(name)) {
            throw new NoSuchElementException("No such classifier " + name);
        }
        this.classifiers.get(name).reset();
    }

    /** Destroy all classifier instances.
     */
    @Override
    public void resetAll() {
        this.classifiers.values().forEach(l -> l.reset());
    }

    @Override
    public void forceLoad(String name) {
        if (!this.classifiers.containsKey(name)) {
            throw new NoSuchElementException("No such classifier " + name);
        }
        this.classifiers.get(name).get();
    }

    @Override
    public Stream<C> getClassifiers() {
        return this.classifiers.values().stream()
                .filter(LazyLoader::isLoaded)
                .map(LazyLoader::get);
    }

    @Override
    public Optional<C> getClassifierByName(String name) {
        if (!this.classifiers.containsKey(name)) {
            throw new NoSuchElementException("No such classifier " + name);
        }
        return Optional.of(this.classifiers.get(name))
                .filter(LazyLoader::isLoaded)
                .map(LazyLoader::get);
    }

    @Override
    public Stream<String> getAllClassifierNames() {
        return this.classifiers.keySet().stream();
    }

    public Optional<C> getClassifierByCriterion(String criterion) {
        String name = this.byCriterion.get(criterion);
        if (name == null) {
            return Optional.empty();
        }
        return Optional.of(this.classifiers.get(name).get());
    }

    @Override
    public Map<String, Double> predict(String criteria, B datapoint, Object... parameters) {
        Map<String, Double> o = new HashMap<>();

        for (String s : criteria.split(",")) {
            s = s.trim();
            String name = this.byCriterion.get(s);
            if (name == null) {
                throw new IllegalArgumentException("No such classification criterion " + name);
            }
            C classifier = this.classifiers.get(name).get();
            Map<String, Double> prediction = classifier.predict(s, classifier.adapt(datapoint), parameters);
            mergePredictions(o, name, prediction);
        }
        return o;
    }

    private static void mergePredictions(Map<String, Double> o, String nOther, Map<String, Double> pOther) {
        for (Map.Entry<String, Double> e : pOther.entrySet()) {
            o.put(nOther + "#" + e.getKey(), e.getValue());
        }
    }

    /** Method for instantiating a new classifier.
     *
     * @param descriptor the descriptor that parameterizes the classifier
     * @return a new instance of a classifier according to the given descriptor
     */
    protected abstract C createClassifier(D descriptor);
}

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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/** Interface type for classifiers that manage multiple classifiers internally.
 * @author Eduardo Pinho <eduardopinho@ua.pt>
 */
public interface ClassifierManager<C extends ClassifierInterface<B>, B, D extends ClassifierDescriptor> extends ClassifierInterface<B> {

    /** Register a new classifier by providing a descriptor for this classifier.
     *
     * @param descriptor the descriptor object that should fully parameterize the classifier
     */
    public void register(D descriptor);

    /** Destroy a particular classifier instance.
     * @param name the classifier's unique name
     * @throws java.util.NoSuchElementException if no such classifier with the given name was registered
     */
    public void reset(String name);

    /** Destroy all classifier instances.
     */
    public void resetAll();

    /** Retrieve a particular classifier by name.
     *
     * @return an optional containing the loaded classifier,
     *         or an empty optional if the classifier is registered but unloaded
     * @throws java.util.NoSuchElementException if no such classifier with the given name was registered
     */
    public Optional<C> getClassifierByName(String name);

    /** Retrieve all loaded classifiers.
     *
     * @return a stream of classifiers managed and already instantiated
     */
    public Stream<C> getClassifiers();

    /** Force the initialization of a classifier instance with the given name.
     * If the classifier is already loaded, this is a no-op.
     *
     * @param name the name of the classifier
     * @throws java.util.NoSuchElementException if no such classifier with the given name was registered
     */
    public void forceLoad(String name);

    /** Obtain the names of all registered classifiers. Unline {@link #getClassifiers}, this method
     * should take the names of both loaded and unloaded classifiers.
     *
     * @return a stream of unique classifier names
     */
    public Stream<String> getAllClassifierNames();

    /** Perform a classification on the given item. The manager should delegate the task to one or
     * (less usually) more classifiers available and merge the outcomes.
     *
     * @param criterion the classification criterion. This is an identifier of the class set.
     * @param item the item to be classified. if it's a String or a URI, it should be
     * interpreted as the URI of an item in storage.
     * @param parameters additional var-arg parameters for miscellaneous options
     * @return a mapping of predictions, where the key is the class and the value is the respective probability
     */
    @Override
    public Map<String, Double> predict(String criterion, B item, Object... parameters);
}

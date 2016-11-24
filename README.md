
# Dicoogle Classification API

This library aims to describe and document a specific category for Dicoogle query provider
for the purpose of medical image classification.

## Purpose

Classification, by extracting additional information from a given data point, holds its
uses in retrieval. In medical imaging, this may be used for detecting the presence (or
absence) of certain anatomical regions or visually detectable lesions. By integrating
proper methods into Dicoogle, classifiers can be implemented with facilitated access to
medical imaging resources, which are then used in various contexts (teaching,
researching, computer-aided diagnosis, ...).

The core architecture of Dicoogle contemplates 4 kinds of plugins: storage, query, index
and web service. Classification can only fit in the query section, as it can be regarded
as a narrowed down version of a query provider: the latter provides a list of items with
attributes, whereas the former provides a list of items representing class predictions.
The goal of this library is to document an API for query providers taking the form of
classifiers, so that new classifiers can be included in deployment time and used in
run-time via other plugins aware of these classifiers (e.g. multimodal search).

## How to use

Simply add this library as a dependency to the intended plugin. When creating new
classifiers, provide a query interface that implements the `ClassifierInterface`
in your plugin set.

```java
/** Perform a classification on the given item
 *
 * @param criterion the classification criterion. This is an identifier of the class set.
 * @param item the item to be classified. if it's a String or a URI, it should be
 * interpreted as the URI of an item in storage.
 * @param parameters additional var-arg parameters for miscellaneous options
 * @return a mapping of predictions, where the key is the class and the value is the respective probability
 */
public Map<String, Double> predict(String criterion, D item, Object... parameters);
```

Then create a new classifier manager class (extends `AbstractClassifierManager`) for
handling instances of your classifier and converting Dicoogle storage items to data
points.

## Classification specification

The classification-api library already provides a set of components to facilitate
classifier development. Nevertheless, a formalization is still important for validating
classifiers and guaranteeing consistency with Dicoogle.

Classifiers in Dicoogle are to be implemented as Dicoogle query interface plugins.
All classification plugins must abide to the following rules:

1. The classification plugin's class type must implement
   `pt.ua.dicoogle.sdk.QueryInterface` and be exposed as a query plugin to the Dicoogle
   plugin set. Its only mandatory method is the one defined with the prototype
   `query(String, Object...)`.
   
2. The first argument of the `QueryInterface#query` method, `String query`, is to be
   interpreted as the classification criterion, which aggregates (separated by commas)
   the unique names of one or more class families recognized by the classifier.
   
3. The second parameter of `QueryInterface#query`, which is the first element of the
   variably-lengthed parameter list, is to be interpreted as the data point to classify.
   1. If the argument is an instance of `java.net.URI`, the actual data to be classified
      is to be retrieved from storage, where the URI is the unique identifier of the
      item stored in Dicoogle.
   2. If the argument is a `java.lang.String`, a `URI` is created based on the given
      string and rule `1.3.1` is applied with the obtained URI.
   3. If the argument type is not a `java.net.URI` nor a `String`, the classifier may
      also attempt any run-time identification of the element type for its proper
      interpretation. For example, this may include a check for whether the object is an
      instance of `org.dcm4che2.data.DicomObject`, which would then be interpreted as a
      DICOM object. As another example, the classifier may ultimately attempt an
      explicit cast to the classifier's internal data point representation. This
      behavior allows some level of interoperability when using representations
      specified in frameworks such as those in JavaCPP presets (OpenCV, Caffe, ...).
   5. If no other transformation specified from `3.1` to `3.4` is successful, a full
      failure is triggered (see rule `7` regarding failures).
       
4. The method `QueryInterface#query` must return a collection of search results (an
   instance of `java.util.Collection` containing instances of
   `pt.ua.dicoogle.sdk.datastructs.SearchResult`).
   1. All predictions of a classifier are indexed by a URI following the format
   `class://{classifier}/{criterion}#{class}`, where `{classifier}` is the unique
   identifier of the classifier, `{criterion}` is the classification criterion and
   `{class}` is the predicted class. The scheme (and only the scheme) of the URI
   should be treated as case insensitive, and none of the three properties in
   the URI should be empty.
   2. Any independently consistent piece of information regarding a prediction is
      considered an _atomic output_. These atomic outputs may be of one of two types:
      1. A _valid output_ represents a successful and meaningful prediction, and is
      composed by the prediction's identifier (as in `1.4.1`) and a scalar value
      representing the probability of that prediction (a scalar from 0 to 1).
      2. An _error output_ originates from a _partial failure_ in the classification
      process, as in, a problem that is not a full failure, and that does not
      prevent the program from attempting more predictions.
   3. Each atomic output is to be converted to an instance of `SearchResult`. Upon
      construction of the search result:
      1. The `uri` attribute is set to the full prediction identifier (as in `1.4.1`).
      2. The `score` attribute is set to the probability value of the prediction in case
      of a valid output, otherwise it must be set to `NaN` (not a number) or another
      value outside the probability range. Only probability values between 0 and 1
      (inclusive) will be treated as valid output.
      3. The `extra` attribute must be set to a valid map instance, preferably an empty
      instance of `java.util.Map`. In case of an error output, the extra `error` data
      attribute is reserved to contain a `String` object of an error message. Any other
      information provided in this context may be ignored.
   4. All converted outputs are to be contained in a proper collection, and that
      collection must then be returned from the method. No modifications to the
      collection or the contained search results should be done by the caller.

5. Any conversions required for classifying the given data are implementation details
   that must automatically take place internally. This includes image resizing/reshaping
   and pixel/voxel type convertion. The classifier is allowed to make a full failure or
   a partial failure if the data point cannot be properly adapted.

6. Since query providers in Dicoogle should not throw (as of v2.4.0 or older versions),
   classifiers must never explicitly throw checked exceptions and should also catch all
   non-checked exceptions before they are raised beyond the `query` method. Java errors
   are the only exception to this rule.

7. If a failure occurs at any step of classification, the program must abide to the
   following rules:
   1. The program is allowed to log the problem using the appropriate slf4j logger at
   any point of handling the failure.
   2. Any atomic output obtained from the classification process, as specified in `1.4`,
   may be returned, even when some of the problems have failed. In case of a full
   failure however, no further processing should be attempted.
   3. In case of a full failure, the classifier must return an empty collection.


# flambo-example

A Clojure library demonstrate how to use flambo.

## Usage

Run the few manipulations:

```
lein run
```
It will submit the main class defined in project.clj.

  :main flambo-example.spark

To run the app as standalone app, we need to package the app in an Uber jar.

  spark-submit --verbose \
    --jars target/uberjar.jar \
    --master local[4] \
    --class flambo-example.spark
    target/uberjar.jar
    local[4] ip_delay.csv


## Passing functions to Flambo

Spark’s API relies heavily on passing functions in the driver program to run on the cluster. Flambo makes it easy and natural to define serializable Spark functions/operations and provides two ways to do this:

1. flambo.api/defsparkfn: defines named functions:
  
    (f/defsparkfn square [x] (* x x))

2. flambo.api/fn: defines inline anonymous functions:
  
    (-> (f/parallelize sc [1 2 3 4 5])
      (f/map (f/fn [x] (* x x)))
      f/collect)

    (-> (f/text-file sc "data.txt")
      (f/flat-map (f/fn [l] (s/split l #" ")))
      (f/map-to-pair (f/fn [w] (ft/tuple w 1)))
      (f/reduce-by-key (f/fn [x y] (+ x y)))
      f/sort-by-key
      f/collect
      clojure.pprint/pprint)

## Key-Value Pair RDD of (key, value) tuples Operation

In flambo, these operations are available on RDDs of (key, value) tuples.
Flambo handles all of the transformations/serializations to/from Tuple, Tuple2, JavaRDD, JavaPairRDD, etc., so you only need to define the sequence of operations you'd like to perform on your data.

  (f/reduce-by-key ft/tuple w 1))
  (f/combine-by-key ...)
  (f/sort-by-key ...)
  (f/group-by-key ...)
  (f/count-by-key ...)

Tuple functions:

  key-val-fn: destruct tuples into (k,v) and call the supplied fn with those args.
  key-val-val-fn: destruct (K, (Tuple2(V1, V2)) and call fn with k, v1, v2.
  

## License

Copyright © 2015

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

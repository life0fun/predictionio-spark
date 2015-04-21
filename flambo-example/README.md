# flambo-example

A Clojure library demonstrate how to use flambo.

## Usage

Run the few manipulations:

```
lein run
```

To run the app as standalone app, we need to package the app in an Uber jar.

  spark-submit --verbose \
    --jars target/uberjar.jar \
    --master local[4] \
    --class flambo-example.spark
    target/uberjar.jar 
    local[4] ip_delay.csv


## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

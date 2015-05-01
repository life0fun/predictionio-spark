(ns flambo-example.tfidf
  (:require  [flambo.api :as f]
             [flambo.conf :as conf]
             [cheshire.core :refer :all]
             [clj-time.core :as time]
             [clj-time.coerce :as tc]
             [clj-time.format :as tf]
             [clj-http.client :as client]
             [environ.core :refer [env]]
             [clojure.java.browse])
  (:import ;[org.apache.log4j Level Logger]
           [org.slf4j Logger LoggerFactory])
  (:gen-class))


; tf(t, d) = (number of times term t appears in document d) / (total number of terms in document d)
; and its idf weight:

; idf(t) = ln((total number of documents in corpus) / (1 + (number of documents with term t)))


(def logger (LoggerFactory/getLogger "flambo-example"))


;; Spark setup
(def c (-> (conf/spark-conf)
           (conf/master "local[*]")
           (conf/app-name "flambo-example")))

(def sc (f/spark-context c))

(defn log
  [msg]
  (.info logger msg))


(def documents
   [["doc1" "Four score and seven years ago our fathers brought forth on this continent a new nation"]
    ["doc2" "conceived in Liberty and dedicated to the proposition that all men are created equal"]
    ["doc3" "Now we are engaged in a great civil war testing whether that nation or any nation so"]
    ["doc4" "conceived and so dedicated can long endure We are met on a great battlefield of that war"]])

(def doc-data (f/parallelize sc documents))

(f/defsparkfn gen-docid-term-tuples [doc-tuple]
         (let [[doc-id content] doc-tuple
               terms (filter #(not (contains? stopwords %))
                             (clojure.string/split content #" "))
               doc-terms-count (count terms)
               term-frequencies (frequencies terms)]
           (map (fn [term] [doc-id term (term-frequencies term) doc-terms-count])
                (distinct terms))))
user=> (def doc-term-seq (-> doc-data
                             (f/flat-map gen-docid-term-tuples)
                             f/cache))


(def tf-by-doc (-> doc-term-seq
                          (f/map (f/fn [[doc-id term term-freq doc-terms-count]]
                                       [term [doc-id (double (/ term-freq doc-terms-count))]]))
                          f/cache)

(def num-docs (f/count doc-data))

user=> (defn calc-idf [doc-count]
         (f/fn [[term tuple-seq]]
           (let [df (count tuple-seq)]
             [term (Math/log (/ doc-count (+ 1.0 df)))])))
user=> (def idf-by-term (-> doc-term-seq
                            (f/group-by (f/fn [[_ term _ _]] term))
                            (f/map (calc-idf num-docs))
                            f/cache)


(def tfidf-by-term (-> (f/join tf-by-doc idf-by-term)
                              (f/map (f/fn [[term [[doc-id tf] idf]]]
                                           [doc-id term (* tf idf)]))
                              f/cache)

(->> tfidf-by-term
            f/collect
            ((partial sort-by last >))
            (take 10)
            clojure.pprint/pprint)
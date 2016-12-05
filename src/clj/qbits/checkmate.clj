(ns qbits.checkmate
  (:require
   [clojure.tools.logging :as log]))

(defprotocol RetryStrategy
  (run [this f opts]))

(defn try-or-fail [f]
  (try [::success (f)]
       (catch Throwable t
         [::error t])))

(defn constant-backoff [ms]
  (repeat ms))

(defn exponential-back-off [x]
  (iterate #(* Math/E %) x))

(defn sane-backoff [x]
  (lazy-cat
   (repeat x 100)
   (repeat x 500)
   (repeat x 1500)
   (repeat x 15000)
   (repeat x 60000)))

(def default-callbacks
  {:failure #(throw %)
   :success identity})

(def logging-callbacks
  {:failure #(do (log/error [::failure %])
                 (throw %))
   :error #(log/error [::error %])
   :success #(do (log/info [::success %]) %)})

(def delay-runner
  (reify RetryStrategy
    (run [this f opts]
      (let [{:keys [delays success error failure] :as opts}
            (merge {:delays (take 100 (constant-backoff 100))}
                   default-callbacks
                   opts)]

        (loop [delays delays]
          (let [[status ret :as step] (try-or-fail f)]
            (case status
              ::success (when success (success ret))
              ::error (let [[delay & delays] delays]
                        (if delay
                          (do
                            (when error (error ret))
                            (Thread/sleep delay)
                            (recur delays))
                          (when failure (failure ret)))))))))))

(def max-runner
  (reify RetryStrategy
    (run [this f opts]
      (let [{:keys [max error success failure] :as opts}
            (merge {:max 10}
                   default-callbacks
                   opts)]
        (loop [max (dec max)]
          (let [[status ret] (try-or-fail f)]
            (case status
              ::success (when success (success ret))
              ::error (do
                        (when error (error ret))
                        (if (zero? max)
                          (when failure (failure ret))
                          (recur (dec max)))))))))))

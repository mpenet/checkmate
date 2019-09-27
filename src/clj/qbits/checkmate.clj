(ns qbits.checkmate
  (:require
   [clojure.core.async :as async]
   [clojure.tools.logging :as log]))

(defprotocol RetryStrategy
  (run [this f opts])
  (attempt [this f] "Invokes f and returns [::success x] or [::error x]"))

(defn try-attempt
  [f]
  (try
    [::success (f)]
    (catch Exception e
      [::error e])))

(defn constant-backoff [ms]
  (repeat ms))

(defn exponential-backoff [x]
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
          (let [[status ret] (attempt this f)]
            (case status
              ::success (when success (success ret))
              ::error (let [[delay & delays] delays]
                        (if delay
                          (do
                            (when error (error ret))
                            (Thread/sleep delay)
                            (recur delays))
                          (when failure (failure ret)))))))))
    (attempt [_ f]
      (try-attempt f))))

(def max-runner
  (reify RetryStrategy
    (run [this f opts]
      (let [{:keys [max error success failure] :as opts}
            (merge {:max 10}
                   default-callbacks
                   opts)]
        (loop [max (dec max)]
          (let [[status ret] (attempt this f)]
            (case status
              ::success (when success (success ret))
              ::error (do
                        (when error (error ret))
                        (if (zero? max)
                          (when failure (failure ret))
                          (recur (dec max)))))))))
    (attempt [_ f]
      (try-attempt f))))

(def delay-runner-ch
  (reify RetryStrategy
    (run [this f opts]
      (let [{:keys [delays success error failure] :as opts}
            (merge {:delays (take 100 (constant-backoff 100))}
                   default-callbacks
                   opts)]
        (async/go-loop [delays delays]
          (let [[status ret] (attempt this f)]
            (case status
              ::success (when success (success ret))
              ::error (let [[delay & delays] delays]
                        (if delay
                          (do
                            (when error (error ret))
                            (async/<! (async/timeout delay))
                            (recur delays))
                          (when failure (failure ret)))))))))

    (attempt [_ f]
      (try-attempt f))))

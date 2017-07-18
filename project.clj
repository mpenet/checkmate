(defproject cc.qbits/checkmate "0.3.0"
  :description "Retry stuff until it passes or break"
  :url "https://github.com/mpenet/checkmate"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/tools.logging "0.4.0"]]
  :source-paths ["src/clj"]
  :global-vars {*warn-on-reflection* true})

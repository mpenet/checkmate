(defproject cc.qbits/checkmate "0.4.0"
  :description "Retry stuff until it passes or break"
  :url "https://github.com/mpenet/checkmate"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "0.4.500"]
                 [org.clojure/tools.logging "0.5.0"]]
  :source-paths ["src/clj"]
  :global-vars {*warn-on-reflection* true})

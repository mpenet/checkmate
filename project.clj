(defproject cc.qbits/checkmate "0.1.2"
  :description "Retry stuff until it passes or break"
  :url "https://github.com/mpenet/checkmate"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/tools.logging "0.2.6"]]
  :source-paths ["src/clj"]
  :global-vars {*warn-on-reflection* true})

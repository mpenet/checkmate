# checkmate

A minimalist/extensible lib to handle failures and retries in a graceful way.

I wanted a few things:

* Pluggable retry strategies
* Extreme simplicity
* Composable
* Not an avalanche of dynamic vars
* Pluggable reporting via success/error/failure callbacks

## Documentation

Right now it has 3 default RetryStrategies implementations (it's a protocol):

* `delay-runner`
* `max-runner`
* `delay-runner-ch`

each `run` takes a `runner` instance and options, which can be

* `:success `: by default `identity` - function called on successful return
* `:error`: by default `noop` - function called on intermediary error
* `:failure`: by default #(throw %) - function called when the run failed

We also supply `logging-callbacks` that does the same as
`default-callbacks` but logs everything as a tuple of `[state,
return-or-error]`.

Enough words, code:

```clj
(use 'qbits.checkmate)

;; the default
;; after 10 failed tries without pause it'll rethrow last error
(run max-runner #(do something that might fail))

;; in fact this translates to

(run max-runner
     #(do something that might fail)
     {:max 10 ;; try max 10 times
      :success (fn [ret] ret) ;; just return value if ok
      :failure #(throw %) ;; throw on failure after 10 tries
      :error identity}) ;; do nothing in intermediate failures

```

This is quite simple, unobtrusive, you can add metrics/reporting
easily on top of that.

The other strategy takes a seq of delays:

``` clj

(run delay-runner {:delays (take 50 (exponential-backoff 100))})
(run delay-runner {:delays (take 20 (constant-backoff 100))}) ;; try every 100ms
(run delay-runner {:delays (sane-backoff 2)}) ;; max 10 tries (2 of each delay)

;; etc

```
Delays must just be a (potentially infinite) lazy seq of ms, super
easy to generate manipulate.

The 3 shown before are just:
``` clj
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
```

There's an equivalent of `delay-runner` but that runs in a async/go
context, `delay-runner-ch`, so timeouts are a bit cheaper, just make
sure your task is non blocking if you don't want to starve the
core.async threadpool. The arguments are identical otherwise.

The whole lib is under 100 lines, easy to understand, modify, extend.

## Installation

checkmate is [available on Clojars](https://clojars.org/cc.qbits/checkmate).

[![Clojars Project](https://img.shields.io/clojars/v/cc.qbits/checkmate.svg)](https://clojars.org/cc.qbits/checkmate)

## License

Copyright © 2016 [Max Penet](http://twitter.com/mpenet)

Distributed under the
[Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html),
the same as Clojure.

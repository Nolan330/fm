(comment

  (lib/zipv
   vector?
   '[a b & [c d & [e f & gs]]]
   '[int? int? & [int? int? & [int? int? & int?]]])

  (lib/zipv
   vector?
   '[a [b1 b2 b3 :as bs] & [c d & [e f & gs]]]
   '[int? (s/spec (s/* int?)) & [int? int? & [int? int? & int?]]])

  (lib/zipv
   vector?
   '[a bs & [c d & [e f & gs]]]
   '[int? [int? int? int?] & [int? int? & [int? int? & int?]]])

  (lib/zipv
   vector?
   '[a [b1 b2 b3] & [c d & [e f & gs]]]
   '[int? [int? int? int?] & [int? int? & [int? int? & int?]]])

  (lib/zipv
   vector?
   '[a [b1 b2 b3 & bs] & [c d & [e f & gs]]]
   '[int? [int? int? int? & int?] & [int? int? & [int? int? & int?]]])

  (lib/zipv
   vector?
   '[a [b1 b2 b3] & cs]
   '[int? [int? int? int?] & int?])

  (lib/zipv
   vector?
   '[a [b1 b2 b3] & cs]
   '[int? [int? int? int?] & int?])

  (lib/conform-explain
   (s/cat
    :0 int?
    :1 int?
    :&
    (s/?
     (s/cat
      :0 int?
      :& (s/* int?))))
   '[1 2 a b])

  (lib/conform-explain
   (s/cat
    :0 int?
    :1 int?
    :&
    (s/spec
     (s/?
      (s/cat
       :0 int?
       :rest
       (s/?
        (s/cat
         :1 int?
         :rest
         (s/?
          (s/cat
           :2 int?
           :rest (s/? any?)))))))))
   '[1 2 (3 4 5 b)])

  (lib/zipv
   vector?
   '[a b & [c d e & [f g & hs]]]
   '[int? [int? int? int?] & [::s1 (fn [a] (pos? a)) [int? int? int? & int?] & [(s/and pos? even?) int? & int?]]])

  (let [[a [b & cs :as cs2] & ds :as ds2] '[a [b c d] e f g]]
    [a b cs ds cs2 ds2])

  ((fn [a [b & cs :as cs2] & ds]
     [a b cs ds cs2])
   'a '[b c d] 'e 'f 'g)

  ((fn [a b & [c d & [e f & gs]]]
     [a b c d e f gs])
   'a '[b1 b2 b3] 'c 'd 'e)

  ((fn [a b & [c d & [e f & gs :as es] :as cs]]
     [a b c d e f gs es cs])
   'a 'b '[c1 c2 c3] 'd 'e 'f)

  ((fn [a b & [& [c d & es :as cs] :as rest]]
     [a b c d es cs rest])
   'a 'b '[c1 c2 c3] 'd 'e 'f)

  ((fn [a [b & bs] & [& [c d & es :as cs] :as rest]]
     [a b bs c d es cs rest])
   'a '[b1 b2 b3] '[c1 c2 c3] 'd 'e 'f)

  ((fn [a [& bs :as bs2] & [& [c d & es :as cs] :as rest]]
     [a bs bs2 c d es cs rest])
   'a '[b1 b2 b3] '[c1 c2 c3] 'd 'e 'f)

  (fn [a [& bs :as bs2] & [& [c d & es :as cs] :as rest] & f g]
    [a bs bs2 c d es cs rest])

  (lib/conform-explain
   ::fn/args
   '[a [& bs :as bs2] & [& [c d & es :as cs] :as rest]])

  '[int? [& int?] & [& [int? int? & int?]]]
  '[int? [& int?] & [int? [& int?] & int?]]
  '[a [b1 b2 b3] c [d1 d2 d3 d4] e f g]

   (lib/zipv
    sequential?
    '[a [& bs] & [c [& ds] & es]]
    '[a? [& b?] & [c? [& d?] & e?]])

   '[a (b1 b2 b3) c (d1 d2 d3 d4) e1 e2 e3]

   (lib/zipv
    sequential?
    '[a [& bs :as bs2] & [c [& ds :as ds2] & es :as cs2]]
    '[a? [& b?] & [c? [& d?] & e?]])

   (lib/conform-explain
    ::args
    '[a? [& b?] & [c? [& d?] & e?]])

   (lib/zipv
    sequential?
    '[a [& bs :as bs2] & [c [& ds :as ds2] & es :as cs2]]
    '[a? [& b?] & [c? [& d?] & e?]])

  '[a bs2 cs2]

  (lib/conform-explain
   ::arg
   :ns/kw)

  (lib/conform-explain
   ::arg
   [:ns/kw])

  (lib/conform-explain
   ::arg
   '[int? [& int?] & [& [int? int? & int?]]])

  (lib/conform-explain
   ::args
   '[int? [& int?] & [& [int? int? & int?]]])

  (lib/conform-explain
   ::args
   '[& int?])

  (lib/conform-explain
   ::args
   '[int? & int?])

  ((fn [& {:syms [a b c]}]
     [a b c])
   'a 'a 'b 'b 'c 'c)

  ((fn [& {:strs [a b c]}]
     [a b c])
   "a" 'a "b" 'b "c" 'c)

  '[a? [& b?] & [c? [& d?] & e?]]
  (s/spec
   (s/cat
    :p1 a?
    :p2
    (s/spec
     (s/cat
      :variadic
      (s/* b?)
      #_
      (s/?
       (s/* b?))))
    :variadic
    (s/?
       ;; NOTE: no `s/spec`
     (s/cat
      )
     )
    )
   )

  (comment

    ;; TODO: dynamic `:fm/,,,`
    (def args1 [int?])
    (fm ^{:fm/args args1} [x] (+ x 1))

    ;; TODO: warnings (log level?), `conform-explain`
    (fm ^{:fm/args [int?]} [x1 x2])

    ;;;
    )

  (reset! trace-atom [])

  (->form
   {::ns         *ns*
    ::definition '(^{:fm/doc "fn1"}
                   fn1
                   ^{:fm/args    [int?]
                     :fm/ret     int?
                     :fm/rel     (fn [{args :args ret :ret}]
                                   (>= ret (apply + (vals args))))
                     :fm/trace   true
                     :fm/conform true
                     :fm/handler identity}
                   [a :as argv]
                   (prn argv)
                   (inc a))
    ::defaults   {:fm/trace    nil
                  :fm/trace-fn `prn
                  :fm/handler  `identity}}
   ::fn)

  (->form
   {::ns         *ns*
    ::definition '(#_^{:fm/doc "fn1"}
                     fn1
                     #_^{:fm/rel     (fn [{args :args ret :ret}]
                                       (>= ret (apply + (vals args))))
                         :fm/trace   true
                         :fm/conform true
                         :fm/handler identity}
                     [[::a :as ctx]]
                     (prn argv)
                     (inc a))
    ::defaults   {:fm/trace    nil
                  :fm/trace-fn `prn
                  :fm/handler  `identity}}
   ::conse)

  (lib/conform-explain
   ::specv
   [[:a]])

  (lib/conform-explain
   ::core.specs/param-list
   '[a [b :as bs] & xs])

  (lib/conform-explain
   ::specv
   '[a])

  (lib/conform-explain
   ::definition
   '([a b] [a b]))

  (lib/conform-explain
   ::definition
   '([a b & cs] [a b cs]))

  (lib/conform-explain
   ::definition
   '([a ::b & cs] [a b cs]))

  (s/def ::b any?)

  (lib/conform-explain
   ::definition
   '([a ::b & cs] [a b cs]))

  (s/def ::cs (s/keys* :opt-un [::a ::b]))

  (lib/conform-explain
   ::definition
   '(^:fm/conform [a ::b & ::cs] [a b cs]))

  (lib/conform-explain
   ::definition
   '(([a] a)
     ([a ::b] [a b])
     ([a ::b & ::cs] [a b cs])))

  (def conformed-definition1
    #:fm.definition
     {:rest
      [:fm.form/signatures
       [#:fm.signature
         {:argv
          [:fm.context/positional
           {:params
            [[:clojure.core.specs.alpha/binding-form
              '[:local-symbol a]]]}],
          :body '[a]}
        #:fm.signature
         {:argv
          [:fm.context/positional
           {:params
            [[:clojure.core.specs.alpha/binding-form
              '[:local-symbol a]]
             [:clojure.spec.alpha/registry-keyword
              :fm.form/b]]}],
          :body '[[a b]]}
        #:fm.signature
         {:argv
          [:fm.context/positional
           {:params
            [[:clojure.core.specs.alpha/binding-form
              '[:local-symbol a]]
             [:clojure.spec.alpha/registry-keyword
              :fm.form/b]],
            :var-params
            {:ampersand '&,
             :var-form
             [:clojure.spec.alpha/registry-keyword
              :fm.form/cs]}}],
          :body '[[a b cs]]}]]})

  (def conformed-param-list1
    [:fm.context/positional
     {:params
      [[:clojure.core.specs.alpha/binding-form
        '[:local-symbol a]]
       [:clojure.spec.alpha/registry-keyword
        :fm.form/b]],
      :var-params
      {:ampersand '&,
       :var-form
       [:clojure.spec.alpha/registry-keyword
        :fm.form/cs]}
      :as-form {:as :as :as-sym 'specv1}}])

  (->metadata conformed-param-list1 ::conformed-param-list)

  (s/def ::a any?)
  (s/def ::b any?)
  (s/def ::cs any?)

  (->metadata
   (lib/conform-explain
    ::specv
    '[])
   ::conformed-specv)

  (->metadata
   (lib/conform-explain
    ::specv
    '[a])
   ::conformed-specv)

  (->metadata
   (lib/conform-explain
    ::specv
    '[::a])
   ::conformed-specv)

  (->metadata
   (lib/conform-explain
    ::specv
    '[::a b])
   ::conformed-specv)

  (->metadata
   (lib/conform-explain
    ::specv
    '[::a b & cs])
   ::conformed-specv)

  (->metadata
   (lib/conform-explain
    ::specv
    '[::a b & ::cs])
   ::conformed-specv)

  (->metadata
   (lib/conform-explain
    ::specv
    '[::a b & {::cs [{[[c111 c112 v113] c12 c13] :xs} c2 [x1 x2 x3] :as cs]}])
   ::conformed-specv)

  (lib/conform-explain
   ::specv
   '[[]])

  (->metadata
   (lib/conform-explain
    ::specv
    '[[::b {:a [a1 a2 a3] ::c c1 ::d {:keys [d1 d2 d3]}}]])
   ::conformed-specv) ; TODO: generator

  (s/unform
   ::specv
   '[:fm.context/nominal
     [{:params
       [[:keyword :fm.form/b]
        [:fm.form/nominal-binding-map
         {:a
          [:seq-destructure
           {:forms [[:local-symbol a1] [:local-symbol a2] [:local-symbol a3]]}],
          :fm.form/c [:local-symbol c1],
          :fm.form/d [:map-destructure {:keys [d1 d2 d3]}]}]]}]])

  (s/unform
   ::core.specs/binding-form
   '[:seq-destructure
     {:forms
      [[:local-symbol c1] [:local-symbol c2] [:local-symbol c3]],
      :as-form {:as :as, :as-sym cs}}])

  (s/unform
   ::core.specs/binding-form
   '[:map-destructure {:keys [d1 d2 d3] :as ds}])

  (s/unform
   ::core.specs/binding-form
   '[:local-symbol c1]
   #_
   '[:map-destructure {:keys [d1 d2 d3]}]
   #_
   '[:seq-destructure
     {:forms [[:local-symbol a1] [:local-symbol a2] [:local-symbol a3]]}])

  (let [conformed (lib/conform-explain
                   ::definition
                   '(([a] a)
                     ([a b] [a b])
                     ([a b & cs] [a b cs])))
        ctx       (assoc ctx ::ident ::fn ::conformed-definition conformed)
        ctx       (assoc ctx ::metadata (->metadata ctx ::metadata))
        ])

  (defn f1
    [& argms]
    (,,, (into (hash-map) argms)))

  ([] ,,,)
  ([] [] ,,,)
  ([] [[]] ,,,)
  ([[]] ,,,)
  ([[]] [] ,,,)
  ([[]] [[]] ,,,)

  ;; merges: meta-merge, deep merge, specter?
  (into (hash-map) argms)
  (into (vector) (mapcat (fn [x] (sequential? x) x (vector x))) args)
  (into (vector) args)
  (into (vector) (mapcat identity) argvs)
  (partial apply meta-merge)
  (partial apply deep-merge)
  (partial apply zip)
  (->> (select-keys ret right) (partial into args)) ; "closed"

  (derive :fm/-- :fm.sequent/nonse)
  (derive :fm/-> :fm.sequent/conse)
  (derive :fm/<- :fm.sequent/conse)
  (derive :fm/<< :fm.sequent/merge)
  (derive :fm/>> :fm.sequent/merge)
  (derive :fm/<> :fm.sequent/iso)

  (derive :fm.sequent/combine :fm.sequent/merge)

 (fm/defn f1
    [a]
    a)

 (fm/defn m1 [[::a ::b]]
   ::c)

 (defn f1 [a] (inc a)) ; b
 (fm/defn f1 ^:fm/throw [a] (inc a))
 #_(fm/defn f1 ^:fm/ignore [a] (inc a))

 (defn f2 [a]
   (try
     (if (s/valid? int? a)
       (inc a)
       'args-anomaly)
     (catch Throwable t
       'thrown-anomaly)))

 (fm/defn f2 ^{:fm/args [int?]} [a] (inc a))

 (s/def ::a int?)
 (fm/defn f2 [::a] (inc a))

 (fm/defn f1 ^{:fm/args [::a]} [a] (inc a)) ; b
 (fm/defn f1 ^{:fm/args [::a]} [a :as argv] (prn argv) (inc a)) ; b
 (fm/defn f1 [::a] (inc a)) ; b
 (fm/defn f1 ^:fm/-> [::a] (inc a)) ; [b]
 (fm/defn f1 ^:fm/-- [::a] (inc a)) ; [a]
 (fm/defn f1 ^:fm/<< [::a] (inc a)) ; [a b]
 (fm/defn f1 ^:fm/<> [::a] [::b] (inc a), (dec b)) ; [a] | [b]

 (fm/defn f1 [a] [::b] (inc a))

  (fm/defn m1
    ^{:fm/sequent
      {:fm.sequent/ident   :fm.sequent/merge
       :fm.sequent/unit    (partial into [] ,,,)
       :fm.sequent/combine (partial into [] ,,,)}}
    [:a :b :c]
    [:d :e :f]
    [d e f])

  (fm/defn ^:fm/<< m1
    ^{:fm/sequent {:fm.sequent/combine (partial apply meta-merge)}}
    [[:a :b :c]]
    [[:d :e :f]]
    [d e f])

  (m1 [:a] [:b] [:c])

  (fm/defn ^:fm/-- m2
    [])

  (fm/defconse conse1 [[::a ::b ::c]] [[::d]]
    ::d)

  (fm/defn f1 [x y z :as v3]
    v3)

  (lib/conform-explain
   (s/cat
    :fm.form/argv vector?
    :fm.form/retv (s/? vector?)
    :fm.form/body (s/+ any?))
   '([a b c] []))

    ;; NOTE: seqv, argv ambiguity
  (fm/fn [{::keys [a b c]}] ; recognized as sequent by default
    ,,,)

  (fm/fn [[::a ::b ::c]] ; unambiguous `seqv` expression
    ,,,)

  (s/def ::xs ,,,)
  (fm/fn [{::xs {::keys [a b c]}}] ; semantically equivalent `seqv`
    ,,,)

    ;; NOTE: warning + explicit disambiguation
  (fm/fn ^:fm.form/argv
    [{::keys [a b c]}]
    ,,,)

    ;; NOTE: warning + don't handle at all; "[{::keys [a b c]}] is a seqv"
  (fm/fn [{::xs {::keys [a b c] :as xs}}]
    ,,,)

  ;; NOTE: eliminate `seqv` destructuring
  ;; NOTE: change `seqv` specification
  ;; NOTE: try to detect/infer special `:keys` cases


  [[a b c]] :: ::keys

  (defmergesequent req<<developer
    [::db.developer/conn ::session/data]
    [::portal/developer]
    (let [ref    (credential.datalog/nuid->lookup-ref (:nu/id data))
          result (d/q q/credential->developer (d/db conn) ref)]
      (datalog.lib/->data (ffirst result))))

  #_(seqv->pull-pattern)
  #_(seqv->argv)
  (fm/defn ^:fm.sequent/merge merge-developer
    [::db.developer/conn {::session/data {:nu/keys [id]}}] ; TODO: seqv -> pull-pattern
    [::portal/developer]
    (let [ref    (credential.datalog/nuid->lookup-ref id)
          result (d/q q/credential->developer (d/db conn) ref)]
      (datalog.lib/<-ident (ffirst result))))

  (fm/defn ^:fm/<< merge-developer
    [[::db.developer/conn {::session/data {:nu/keys [id]}}]]
    [[::portal/developer]]
    (let [ref    (credential.datalog/nuid->lookup-ref id)
          result (d/q q/credential->developer (d/db conn) ref)]
      (datalog.lib/->data (ffirst result))))

  (fm/defn ^:fm.sequent/nonse transact!
    [:body-params ::db.developer/conn ::portal/developer]
    [::d/tx-report]
    (let [eid           (get developer :db/id)
          subscriptions (get body-params ::portal/subscriptions)
          tx-data       [{:db/id eid ::portal/subscriptions subscriptions}]]
      (d/transact conn {:tx-data tx-data})))

  (fm/defconse req->post-response ^{:fm/handler anomaly-handler}
    []
    [[:status]]
    {:status 204}) ; ALT: respond with developer; EQL

  (defmulti  handler :request-method)
  (defmethod handler :post
    [req]
    (->>
     req
     req<<developer
     req<transact!>
     req->post-response))

  (s/def ::req
    (s/keys
     :req [::session/data]
     :req-un [::request-method]))

  (defmulti handler
    (fm/fn ^{:fm/args ::req}
      [{:keys [request-method] ::session/keys [data]}]
      (let [session-tag (first (lib/conform-throw ::session/data data))]
        [request-method session-tag]))
    :hierarchy #'h1)

  (defmulti handler
    (fn [{:keys [request-method] ::session/keys [data]}]
      [request-method (first (fm/conform-throw ::session/data data))]))

  (defmethod handler [:post ::session/authenticated]
    [req]
    ())

  (defmethod handler :default
    [_req]
    {:status 405})

  (fm/defnonse nonse1
    ([[::db.developer/conn ::b]]
     [::c]
     ::c)
    ([::c]
     [::d]
     ::d))

  (fm/defnonse nonse1
    ([::authenticated]
     [::resp]
     {:status 200})
    ([::unauthenticated]
     [::resp]
     {:status 401}))

  (ns ns1.core)

  (derive :fm.sequent/conse :fm.sequent/ident)
  (derive :fm.sequent/nonse :fm.sequent/ident)
  (derive :fm.sequent/merge :fm.sequent/ident)

  (defmulti multi1
    (fn [metadata]
      (let [descs (descendants :fm.sequent/ident)
            tag   (reduce
                   (fn [acc k]
                     (when-let [v (get metadata k)]
                       (reduced (if (true? v) k v))))
                   nil
                   (conj descs :fm.sequent/ident))]
        tag)))

  (defmethod multi1 :fm.sequent/conse
    [metadata]
    :fm.sequent/conse)

  (defmethod multi1 :fm.sequent/nonse
    [metadata]
    :fm.sequent/nonse)

  (defmethod multi1 :fm.sequent/merge
    [metadata]
    :fm.sequent/merge)

  (defmacro macro1 [& definition]
    (multi1 (meta (first definition))))

  (ns ns2.core
    (:require [ns1.core :as ns1]))

  (derive ::-> :fm.sequent/conse)
  (derive ::-- :fm.sequent/nonse)
  (derive ::<< :fm.sequent/merge)

  (descendants :fm.sequent/ident)

  (ns1/macro ^::-> [])

  (def f1
    (clojure.core/let
     [args11106
      (clojure.spec.alpha/cat :0 int?)
      ret11107
      int?
      rel11108
      (fn [{args :args, ret :ret}] (>= ret (apply + (vals args))))
      trace11109
      clojure.core/prn
      handler11110
      identity]
      (clojure.core/with-meta
        (clojure.core/fn
          fn1
          [a]
          (try
            (clojure.core/let
             [argv11111 [a]]
              (trace11109 {:fm/ident :fm.form/fn1, :fm.trace/args argv11111})
              (if
               (fm.anomaly/anomalous? argv11111)
                (handler11110
                 {:fm.anomaly/ident :fm.anomaly/received,
                  :fm.anomaly/args  argv11111,
                  :fm/ident         :fm.form/fn1})
                (clojure.core/let
                 [conformed-args11112 (clojure.spec.alpha/conform args11106 argv11111)]
                  (trace11109
                   {:fm/ident :fm.form/fn1, :fm.trace/conformed-args conformed-args11112})
                  (if
                   (clojure.spec.alpha/invalid? conformed-args11112)
                    (handler11110
                     {:fm.anomaly/ident :fm.anomaly/args,
                      :clojure.spec.alpha/explain-data
                      (clojure.spec.alpha/explain-data args11106 argv11111),
                      :fm/ident         :fm.form/fn1})
                    (clojure.core/let
                     [argv11111 conformed-args11112]
                      (clojure.core/let
                       [ret11113 (do (inc a))]
                        (trace11109 {:fm/ident :fm.form/fn1, :fm.trace/ret ret11113})
                        (if
                         (fm.anomaly/anomalous? ret11113)
                          (handler11110
                           {:fm.anomaly/ret   ret11113,
                            :fm.anomaly/ident :fm.anomaly/nested,
                            :fm.anomaly/args  argv11111,
                            :fm/ident         :fm.form/fn1})
                          (clojure.core/let
                           [conformed-ret11114 (clojure.spec.alpha/conform ret11107 ret11113)]
                            (trace11109
                             {:fm/ident               :fm.form/fn1,
                              :fm.trace/conformed-ret conformed-ret11114})
                            (if
                             (clojure.spec.alpha/invalid? conformed-ret11114)
                              (handler11110
                               {:fm.anomaly/ident :fm.anomaly/ret,
                                :clojure.spec.alpha/explain-data
                                (clojure.spec.alpha/explain-data ret11107 ret11113),
                                :fm.anomaly/args  argv11111,
                                :fm/ident         :fm.form/fn1})
                              (clojure.core/let
                               [ret11113 conformed-ret11114]
                                (if
                                    (rel11108 {:args argv11111, :ret ret11113})
                                  ret11113
                                  {:fm.anomaly/ident :fm.anomaly/rel,
                                   :clojure.spec.alpha/explain-data
                                   (clojure.spec.alpha/explain-data
                                    rel11108
                                    {:args argv11111, :ret ret11113}),
                                   :fm/ident         :fm.form/fn1})))))))))))
            (catch
             java.lang.Throwable
             thrown__6594__auto__
              (handler11110
               {:fm.anomaly/ident  :fm.anomaly/thrown,
                :fm.anomaly/thrown thrown__6594__auto__,
                :fm.anomaly/args   [a],
                :fm/ident          :fm.form/fn1}))))
        '#:fm{:args     [[int?]],
              :rel      [(fn [{args :args, ret :ret}] (>= ret (apply + (vals args))))],
              :ret      [int?],
              :trace    [true],
              :conform  [true],
              :ident    :fm.form/fn1,
              :arglists [[a]],
              :doc      "fn1",
              :handler  [identity]})))

  (defn fn1
    ([a] (prn "a") a)
    ([a & as] (prn "as") [a as]))

  (clojure.core/let
      [args8064 [:fm.form/bindings :fm/args]]
    (clojure.core/with-meta
      (clojure.core/fn fn1 [])
      '#:fm{:arglists [[a]], :doc "fn1", :args [[int?]], :ident :fm.form/fn1}))

  (->metadata-form
   (->>
    {::ident      ::fn
     ::ns         *ns*
     ::definition (list
                   (with-meta 'fn1 {:fm/doc "fn1"})
                   (list
                    (with-meta '[a [b [c]] & ds] {:fm/args '[int? [int? [int?]] & int?]})
                    '[a b c ds])
                   (list '[a & bs] '[a bs])
                   (list
                    (with-meta '[a] {:fm/doc "sig1" :fm/args '[even?]})
                    'a))}
    <<conformed-definition
    <<metadata)
   :fm/args)

  (->context
   {::ident      ::fn
    ::ns         *ns*
    ::definition '(^{:fm/doc "fn1"} fn1
                   ^{:fm/args [int?]}
                   [a]
                   (inc a))})

  (->context
   {::ident      ::fn
    ::ns         *ns*
    ::definition '(^{:fm/doc "fn1"} fn1
                   ^{:fm/args [int? int?]}
                   (^{:fm/doc "sig1"}
                    [a] (inc a))
                   ([a b] (+ a b)))})

  (->context
   {::ident      ::fn
    ::ns         *ns*
    ::definition (list
                  (with-meta 'fn1 {:fm/doc "fn1"})
                  (with-meta '[a [b [c]] & ds] {:fm/args '[int? [int? [int?]] & int?]})
                  '[a b c ds])})

  (->context
   {::ident      ::fn
    ::ns         *ns*
    ::definition (list
                  (with-meta 'fn1 {:fm/doc "fn1"})
                  (with-meta '[a [b [c] :as b&c] & ds] {:fm/args '[int? [int? [int?]] & int?]})
                  '[a b c ds])})

  (->context
   {::ident      ::fn
    ::ns         *ns*
    ::definition (list
                  (with-meta 'fn1 {:fm/doc "fn1"})
                  (with-meta '[a [b [c] :as b&c] & [d]] {:fm/args '[int? [int? [int?]] & [int?]]})
                  '[a b c ds])})

  (->context
   {::ident      ::fn
    ::ns         *ns*
    ::definition (list
                  (with-meta 'fn1 {:fm/doc "fn1"})
                  (with-meta '[a [b [c] :as b&c] & [d :as f]] {:fm/args '[int? [int? [int?]] & [int?]]})
                  '[a b c ds])})

  (->context
   {::ident      ::fn
    ::ns         *ns*
    ::definition (list
                  (with-meta 'fn1 {:fm/doc "fm1"})
                  (list
                   (with-meta '[a] {:fm/args '[int?]})
                   'a)
                  (list
                   (with-meta '[a b] {:fm/args '[int? int?]})
                   '[a b]))})

  (->context
   {::ident      ::fn
    ::ns         *ns*
    ::definition (list
                  (with-meta 'fn1 {:fm/doc "fn1"})
                  (list
                   (with-meta '[a [b [c]]] {:fm/args '[int? [int? [int?]]]})
                   '[a b c])
                  (list
                   (with-meta '[a [b [c]] & ds] {:fm/args '[int? [int? [int?]] & int?]})
                   '[a b c ds]))})

  (->context
   {::ident      ::fn
    ::ns         *ns*
    ::definition (list
                  (with-meta 'fn1 {:fm/doc "fn1" :fm/args '[int? int?]})
                  (list '[a] 'a)
                  (list '[a b] '[a b]))})

  (->context
   {::ident      ::fn
    ::ns         *ns*
    ::definition (list
                  (with-meta 'fn1 {:fm/doc "fn1" :fm/args '[int? & int?]})
                  (list '[a] 'a)
                  (list '[a & bs] '[a bs]))})

  (s/def ::m1 (s/map-of keyword? any?))

  (->context
   {::ident      ::fn
    ::ns         *ns*
    ::definition (list
                  (with-meta 'fm1 {:fm/doc "fm1" :fm/args '[int? [int? [int?]] ::m1 & {:f int? :g int?}]})
                  (list '[a] 'a)
                  (list '[a [b]] '[a b])
                  (list '[a [b [c]]] '[a b c])
                  (list '[a [b [c]] {:keys [d] :as e}] '[a b c d e])
                  (list '[a [b [c]] {:keys [d] :as e} & {:keys [f g] :as h}] '[a b c d e f g h]))})

  (->context
   {::ident ::fn
    ::ns    *ns*
    ::definition
    '(^{:fm/doc             "fn1"
        :fm/args            [int?]
        :fm/ret             int?
        :fm/rel             (fn [{args :args ret :ret}]
                              (= (apply + args) ret))
        :fm/trace           #{:fm/args :fm/ret}
        :fm/conform         #{:fm/args}
        :fm.anomaly/handler (fn [a] a)}
      fn1
      [a]
      (inc a))})

  (->context
   {::ident ::fn
    ::ns    *ns*
    ::definition
    '(^{:fm/doc             "fn1"
        :fm/args            [int? int?]
        :fm/ret             int?
        :fm/rel             (fn [{args :args ret :ret}]
                              (>= ret (apply + args)))
        :fm/trace           #{:fm/args :fm/ret}
        :fm/conform         #{:fm/args}
        :fm.anomaly/handler (fn [a] a)}
      fn1
      ([a] (inc a))
      ([a b] (+ a b)))})

  (->context
   {::ident ::fn
    ::ns    *ns*
    ::definition
    '(^{:fm/doc             "fn1"
        :fm/args            [int? int?]
        :fm/ret             int?
        :fm/rel             (fn [{args :args ret :ret}]
                              (>= ret (apply + args)))
        :fm/trace           #{:fm/args :fm/ret}
        :fm/conform         #{:fm/args}
        :fm.anomaly/handler (fn [a] a)}
      fn1
      (^{:fm/doc "sig1"}
       [a] (inc a))
      (^{:fm/trace #{:fm/args}
         :fm/ret   even?}
       [a b] (+ a b)))})

  (->context
   {::ident ::fn
    ::ns    *ns*
    ::definition
    '(^{:fm/doc             "fn1"
        :fm/args            [int? int?]
        :fm/ret             int?
        :fm/rel             (fn [{args :args ret :ret}]
                              (>= ret (apply + args)))
        :fm/trace           #{:fm/args :fm/ret}
        :fm/conform         #{:fm/args}
        :fm.anomaly/handler (fn [a] a)}
      (^{:fm/doc "sig1"}
       [a] (inc a))
      (^{:fm/trace #{:fm/args}
         :fm/ret   int?}
       [a b] (+ a b)))})

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? int?]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           true
         :fm/conform         true
         :fm.anomaly/handler (fn [a] (prn "anomaly!" a) a)}
       ([a] (inc a))
       ([a b] (+ a b)))}))

  (def fn1
    (clojure.core/let
     [trace11666
      true
      arglists11668
      '[a]
      arglists11669
      '[a b]
      doc11670
      "fn1"
      rel11671
      (fn [{args :args, ret :ret}] (>= ret (apply + args)))
      column11673
      15
      conform11674
      true
      line11676
      2989
      args11677
      [int?]
      args11678
      [int? int?]
      handler11679
      (fn [a] (prn "anomaly!" a) a)
      ident11681
      :fm.form/fm11665
      ret11682
      int?]
      (clojure.core/with-meta
        (clojure.core/fn
          fm11665
          ([a]
           (try
             (clojure.core/let
              [args11684 [a]]
               (trace11666 {:fm/ident :fm.form/fm11665, :fm.trace/args args11684})
               (if
                (fm.anomaly/anomalous? args11684)
                 (handler11679
                  {:fm.anomaly/ident :fm.anomaly/received,
                   :fm.anomaly/args  args11684,
                   :fm/ident         :fm.form/fm11665})
                 (clojure.core/let
                  [conformed-args11686
                   (clojure.spec.alpha/conform args-spec11685 args11684)]
                   (trace11666
                    {:fm/ident                :fm.form/fm11665,
                     :fm.trace/conformed-args conformed-args11686})
                   (if
                    (clojure.spec.alpha/invalid? conformed-args11686)
                     (handler11679
                      {:fm.anomaly/ident :fm.anomaly/args,
                       :clojure.spec.alpha/explain-data
                       (clojure.spec.alpha/explain-data args-spec11685 args11684),
                       :fm/ident         :fm.form/fm11665})
                     (clojure.core/let
                      [[a] conformed-args11686]
                       (clojure.core/let
                        [ret11687 (do (inc a))]
                         (trace11666 {:fm/ident :fm.form/fm11665, :fm.trace/ret ret11687})
                         (if
                          (fm.anomaly/anomalous? ret11687)
                           (handler11679
                            {:fm.anomaly/ret   ret11687,
                             :fm.anomaly/ident :fm.anomaly/nested,
                             :fm.anomaly/args  args11684,
                             :fm/ident         :fm.form/fm11665})
                           (clojure.core/let
                            [conformed-ret11689
                             (clojure.spec.alpha/conform ret-spec11688 ret11687)]
                             (trace11666
                              {:fm/ident               :fm.form/fm11665,
                               :fm.trace/conformed-ret conformed-ret11689})
                             (if
                              (clojure.spec.alpha/invalid? conformed-ret11689)
                               (handler11679
                                {:fm.anomaly/ident :fm.anomaly/ret,
                                 :clojure.spec.alpha/explain-data
                                 (clojure.spec.alpha/explain-data ret-spec11688 ret11687),
                                 :fm.anomaly/args  args11684,
                                 :fm/ident         :fm.form/fm11665})
                               (clojure.core/let
                                [ret11687 conformed-ret11689]
                                 (if
                                     (rel11671 {:args args11684, :ret ret11687})
                                   ret11687
                                   {:fm.anomaly/ident :fm.anomaly/rel,
                                    :clojure.spec.alpha/explain-data
                                    (clojure.spec.alpha/explain-data
                                     rel11671
                                     {:args args11684, :ret ret11687}),
                                    :fm/ident         :fm.form/fm11665 })))))))))))
             (catch
              java.lang.Throwable
              thrown__6700__auto__
               (handler11679
                {:fm.anomaly/ident  :fm.anomaly/thrown,
                 :fm.anomaly/thrown thrown__6700__auto__,
                 :fm.anomaly/args   [a],
                 :fm/ident          :fm.form/fm11665}))))
          ([a b]
           (try
             (clojure.core/let
              [args11690 [a b]]
               (trace11666 {:fm/ident :fm.form/fm11665, :fm.trace/args args11690})
               (if
                (fm.anomaly/anomalous? args11690)
                 (handler11679
                  {:fm.anomaly/ident :fm.anomaly/received,
                   :fm.anomaly/args  args11690,
                   :fm/ident         :fm.form/fm11665})
                 (clojure.core/let
                  [args-spec11691
                   (clojure.spec.alpha/cat :0 int? :1 int?)
                   conformed-args11692
                   (clojure.spec.alpha/conform args-spec11691 args11690)]
                   (trace11666
                    {:fm/ident                :fm.form/fm11665,
                     :fm.trace/conformed-args conformed-args11692})
                   (if
                    (clojure.spec.alpha/invalid? conformed-args11692)
                     (handler11679
                      {:fm.anomaly/ident :fm.anomaly/args,
                       :clojure.spec.alpha/explain-data
                       (clojure.spec.alpha/explain-data args-spec11691 args11690),
                       :fm/ident         :fm.form/fm11665})
                     (clojure.core/let
                      [[a b] conformed-args11692]
                       (clojure.core/let
                        [ret11693 (do (+ a b))]
                         (trace11666 {:fm/ident :fm.form/fm11665, :fm.trace/ret ret11693})
                         (if
                          (fm.anomaly/anomalous? ret11693)
                           (handler11679
                            {:fm.anomaly/ret   ret11693,
                             :fm.anomaly/ident :fm.anomaly/nested,
                             :fm.anomaly/args  args11690,
                             :fm/ident         :fm.form/fm11665})
                           (clojure.core/let
                            [ret-spec11694
                             int?
                             conformed-ret11695
                             (clojure.spec.alpha/conform ret-spec11694 ret11693)]
                             (trace11666
                              {:fm/ident               :fm.form/fm11665,
                               :fm.trace/conformed-ret conformed-ret11695})
                             (if
                              (clojure.spec.alpha/invalid? conformed-ret11695)
                               (handler11679
                                {:fm.anomaly/ident :fm.anomaly/ret,
                                 :clojure.spec.alpha/explain-data
                                 (clojure.spec.alpha/explain-data ret-spec11694 ret11693),
                                 :fm.anomaly/args  args11690,
                                 :fm/ident         :fm.form/fm11665})
                               (clojure.core/let
                                [ret11693 conformed-ret11695]
                                 (if
                                     (rel11671 {:args args11690, :ret ret11693})
                                   ret11693
                                   {:fm.anomaly/ident :fm.anomaly/rel,
                                    :clojure.spec.alpha/explain-data
                                    (clojure.spec.alpha/explain-data
                                     rel11671
                                     {:args args11690, :ret ret11693}),
                                    :fm/ident         :fm.form/fm11665})))))))))))
             (catch
              java.lang.Throwable
              thrown__6700__auto__
               (handler11679
                {:fm.anomaly/ident  :fm.anomaly/thrown,
                 :fm.anomaly/thrown thrown__6700__auto__,
                 :fm.anomaly/args   [a b],
                 :fm/ident          :fm.form/fm11665})))))
        '{:fm/trace    [true true],
          :fm/arglists ['[a] '[a b]],
          :fm/doc      "fn1",
          :fm/rel
          [(fn [{args :args, ret :ret}] (>= ret (apply + args)))
           (fn [{args :args, ret :ret}] (>= ret (apply + args)))],
          :column      15,
          :fm/conform  [true true],
          :line        2989,
          :fm/args     [[int?] [int? int?]],
          :fm.anomaly/handler
          [(fn [a] (prn "anomaly!" a) a) (fn [a] (prn "anomaly!" a) a)],
          :fm/ident    :fm.form/fm11665,
          :fm/ret      [int? int?]})))

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? int? & int?]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       [a b & cs] (apply + a b cs))}))


  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    int?
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[1 2])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    int?
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[1 2 3])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    int?
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[1 2 3 a])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    int?
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[a 2 a])

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? [int? int? int?] & int?]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       [a [b1 b2 b3] & cs] (apply + a b1 b2 b3 cs))}))

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat :0 int? :1 int? :2 int?))
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[1])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat :0 int? :1 int? :2 int?))
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[1 nil])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat :0 int? :1 int? :2 int?))
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[1 2])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat :0 int? :1 int? :2 int?))
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[1 [2]])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat :0 int? :1 int? :2 int?))
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[1 [a]])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat :0 int? :1 int? :2 int?))
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[1 [a]])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat :0 int? :1 int? :2 int?))
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[1 [2 3 4]])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat :0 int? :1 int? :2 int?))
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[1 [2 3 4] 5 a])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat :0 int? :1 int? :2 int?))
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :0
      int?
      :rest
      (clojure.spec.alpha/* clojure.core/any?))))
   '[a [2 3 4] 5 a])

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? [int? int? int? & int?] & int?]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       [a [b1 b2 b3 & bs :as bs] & cs] (apply + a (into bs cs)))}))

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat
      :0
      int?
      :1
      int?
      :2
      int?
      :&
      (clojure.spec.alpha/* int?)))
    :&
    (clojure.spec.alpha/* int?))
   '[1 [2 3 4]])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat
      :0
      int?
      :1
      int?
      :2
      int?
      :&
      (clojure.spec.alpha/* int?)))
    :&
    (clojure.spec.alpha/* int?))
   '[1 [2 3 4 5]])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat
      :0
      int?
      :1
      int?
      :2
      int?
      :&
      (clojure.spec.alpha/* int?)))
    :&
    (clojure.spec.alpha/* int?))
   '[1 [2 3 4 5] 6])

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :0
    int?
    :1
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat
      :0
      int?
      :1
      int?
      :2
      int?
      :&
      (clojure.spec.alpha/* int?)))
    :&
    (clojure.spec.alpha/* int?))
   '[1 [2 3 4 5] 6 7])

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? [int? & [int?]] & [int? & even?]]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       [a [b1 & [b2]] & [c & ds]] (apply + a (into bs cs)))}))

  (lib/conform-explain
   (clojure.spec.alpha/cat
    :a
    int?
    :bs
    (clojure.spec.alpha/spec
     (clojure.spec.alpha/cat
      :b1
      int?
      :&
      (clojure.spec.alpha/?
       (clojure.spec.alpha/cat
        :b2
        int?
        :rest
        (clojure.spec.alpha/* clojure.core/any?)))))
    :&
    (clojure.spec.alpha/?
     (clojure.spec.alpha/cat
      :c int?
      :& (clojure.spec.alpha/* even?))))
   '[1 [2 3 a b c] d 6])

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? [int? & [int?]] & [int? & [even?]]]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       [a [b1 & [b2] :as bs] & [c & [ds] :as cs]] (apply + a (into bs cs)))}))

  ((fn [a [b & [c :as cs] :as bs] & [e & f :as es]]
     [a b bs c cs e f es])
   'a '[b c h] 'e 'f 'g)

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? [int? & [int?]] & [int? & even?]]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       [a [b1 b2 b3 & bs :as bs] & cs] (apply + a (into bs cs)))}))

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? int?]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       ([a] (inc a))
       (^{:fm/trace #{}
          :fm/ret   even?}
        [a b] (+ a b)))}))

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? int?]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args :fm/ret}
         :fm.anomaly/handler (fn [a] a)}
       ([] 1)
       (^{:fm/trace            #{}
          :fm/ret              even?
          :fm.anomaly/handler? true}
        [a b] (+ a b)))}))

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? int?]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args :fm/ret}
         :fm.anomaly/handler (fn [a] a)}
       ([] 1)
       (^{:fm/trace            #{}
          :fm/ret              [even? & [even? int?]]
          :fm.anomaly/handler? true}
        [a b] (+ a b)))}))

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       ([] 1)
       (^{:fm/args [int? int?]}
        [a b] (+ a b)))}))

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       ([a] (inc a))
       (^{:fm/args [int? int?]}
        [a b] (+ a b)))}))

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? int? & int?]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       (^{:fm/trace #{}
          :fm/ret   even?}
        [a b & cs] (apply + a b cs)))}))

  '[int? int? & [int? int? int?]]
  (let [[a b & [c d e]] '[a b [c d e] [d e] [e]]]
    [a b c d e])

  (let [[a b & [c d e :as cs]] '[a b]]
    [a b cs])

  (lib/conform-explain
   (s/tuple int? int? (s/? (s/cat :c int? :d int? :e int?)))
   '[1 2 nil])

  (let [[a b & [c d e :as cs]] '[a b nil]]
    [a b cs])

  (lib/conform-explain
   (s/tuple int? int? (s/? (s/cat :c int? :d int? :e int?)))
   '[1 2 [nil]]) ; NOTE: should fail

  (let [[a b & [c d e :as cs]] '[a b c]]
    [a b cs])

  (lib/conform-explain
   (s/tuple int? int? (s/? (s/cat :c int? :d int? :e int?)))
   '[1 2 [3]]) ; NOTE: shouldn't fail?

  (lib/conform-explain
   (s/tuple int? int? (s/cat :c int? :d int? :e int?))
   '[1 2 [3 4 5]])

  (lib/conform-explain
   (s/tuple int? int? (s/? (s/cat :c int? :d int? :e int?)))
   '[1 2 []])

  (lib/conform-explain
   (s/tuple int? int? (s/? (s/cat :c int? :d int? :e int?)))
   '[1 2 [3 4 5]])

  (let [[a b & [c d e :as cs]] '[a b c d]]
    [a b c d e cs])

  (lib/conform-explain
   (s/tuple int? int? (s/? (s/cat :c int? :rest (s/? (s/cat :d int? :rest (s/? (s/cat :e int? :rest any?)))))))
   '[1 2 [3 4 5 6]]) ; NOTE: shouldn't fail?

  (lib/conform-explain
   (s/tuple
    int?
    int?
    (s/?
     (s/cat
      :c int?
      :rest
      (s/?
       (s/cat
        :d int?
        :rest
        (s/?
         (s/cat
          :e int?
          :rest any?)))))))
   '[1 2 nil])

  (lib/conform-explain
   (s/cat
    :0 int?
    :1 int?
    :&
    (s/?
     (s/cat
      :0 int?
      :rest
      (s/?
       (s/cat
        :1 int?
        :rest
        (s/?
         (s/cat
          :2 int?
          :rest any?)))))))
   '[1 2 3])

  (lib/conform-explain
   (s/tuple
    int?
    int?
    (s/?
     (s/cat
      :c int?
      :rest
      (s/?
       (s/cat
        :d int?
        :rest
        (s/?
         (s/cat
          :e int?
          :rest any?)))))))
   '[1 2 []])

  (lib/conform-explain
   (s/tuple
    int?
    int?
    (s/?
     (s/cat
      :c int?
      :rest
      (s/?
       (s/cat
        :d int?
        :rest
        (s/?
         (s/cat
          :e int?
          :rest any?)))))))
   '[1 2 ()])

  (lib/conform-explain
   (s/tuple
    int?
    int?
    (s/?
     (s/cat
      :c int?
      :rest
      (s/?
       (s/cat
        :d int?
        :rest
        (s/?
         (s/cat
          :e int?
          :rest any?)))))))
   '[1 2 (3)])

  (lib/conform-explain
   (s/tuple
    int?
    int?
    (s/?
     (s/cat
      :c int?
      :rest
      (s/?
       (s/cat
        :d int?
        :rest
        (s/?
         (s/cat
          :e int?
          :rest any?)))))))
   '[1 2 (3 4)])

  (lib/conform-explain
   (s/tuple
    int?
    int?
    (s/?
     (s/cat
      :c int?
      :rest
      (s/?
       (s/cat
        :d int?
        :rest
        (s/?
         (s/cat
          :e int?
          :rest (s/? any?))))))))
   '[1 2 (3 4 5)])

  (lib/conform-explain
   (s/tuple
    int?
    int?
    (s/?
     (s/cat
      :0 int?
      :rest
      (s/?
       (s/cat
        :1 int?
        :rest
        (s/?
         (s/cat
          :2 int?
          :rest
          (s/? any?))))))))
   '[1 2 (3 4 5 6)])

  (lib/conform-explain
   (s/tuple
    int?
    int?
    (s/?
     (s/cat
      :c int?
      :rest
      (s/?
       (s/cat
        :d int?
        :rest
        (s/?
         (s/cat
          :e int?
          :rest
          (s/? any?))))))))
   '[1 2 (3 4 5 a)])

  (sequential?)
  (::fn/arg+ ,,,) #_=> `(s/? (s/cat ,,,))
  (::fn/arg ,,,) #_=> `(s/* ,,,)

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? int? & [int? int? int?]]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       [a b & [c d e :as cs]]
       (apply + a b cs))}))

  (let [[a b & [c d e & [f g]]] '[a b c d e f g]]
    [a b c d e f g])

  ((fn [a b & [c d e & [f g :as fs] :as cs]]
     [a b c cs d e f g fs])
   'a 'b 'c 'd 'e 'f 'g)

  ((fn [a b & [c d {:keys [e]} & [f g :as fs] :as cs]]
     [a b c cs d e f g fs])
   'a 'b 'c 'd '{:e e} 'f 'g)

  ((fn [a b & [c d {:keys [e]} & [f g :as fs] :as cs]]
     [a b c cs d e f g fs])
   'a)

  ((fn [a b & [c d {:keys [e]} & [f g :as fs] :as cs]]
     [a b c cs d e f g fs])
   'a nil)

  '[int? int? & [int? int? ::m1 & int?]]

  (def trace-idents #{:fm/args :fm/ret})
  (def conform-idents #{:fm/args})

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? int?]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       ([a b] (+ a b)))}))

  (->form
   ::fn
   (->context
    {::ident ::fn
     ::ns    *ns*
     ::definition
     '(^{:fm/doc             "fn1"
         :fm/args            [int? int?]
         :fm/ret             int?
         :fm/rel             (fn [{args :args ret :ret}]
                               (>= ret (apply + args)))
         :fm/trace           #{:fm/args :fm/ret}
         :fm/conform         #{:fm/args}
         :fm.anomaly/handler (fn [a] a)}
       [a b] (+ a b))}))

  ((fn [a b & [c d e]] [a b c d e]) 'a 'b 'c 'd)

  (def context2 *1)
  (lib/conform-explain (eval (get-in context2 [::metadata :fm/args])) [1])
  (lib/conform-explain (eval (get-in context2 [::metadata :fm/args])) [1 [2]])
  (lib/conform-explain (eval (get-in context2 [::metadata :fm/args])) [1 [2 [3]]])
  (lib/conform-explain (eval (get-in context2 [::metadata :fm/args])) [1 [2 [3]] {:d 'd}])
  (lib/conform-explain (eval (get-in context2 [::metadata :fm/args])) [1 [2 [3]] {:d 'd} :f 'f])
  (lib/conform-explain (eval (get-in context2 [::metadata :fm/args])) [1 [2 [3]] {:d 'd} :f 'f :g 'g])

  (s/def ::m1 (s/map-of keyword? any?))

  (def args1 '[int? [int? [int?]] ::m1 & int?])
  (def argv1 '[a [b [c :as cs] :as bs] {:keys [d] :as e} & fs])

  (def conformed-args1 (lib/conform-explain ::fn/args args1))

  (def bindings1
    {:fm/k1 [{::symbol 's1 ::form 'f1}
             {::symbol 's2 ::form 'f2}
             {::symbol 's3 ::form 'f3}]
     :fm/k2 [{::symbol 's3 ::form 'f3}
             {::symbol 's3 ::form 'f3}
             {::symbol 's4 ::form 'f4}]})

  (->form
   ::fn/context-bindings
   {::bindings bindings1})

  ;;;
  )

(comment

  (ns ns1.core)

  (defn f1 [d1]
    (prn d1)
    (when d1 (prn "tracing"))
    ::f1)

  (ns ns2.core
    (:require [ns1.core :as ns1]))

  (def ^:dynamic *trace* false)

  (defmacro f [& definition]
    `(def ~(gensym 'f) ~*trace*))

  (f sym [] [])

  (binding [*trace* true]
    (f sym [] [])) ; NOTE: not sure why this doesn't work

  (alter-var-root #'*trace* (constantly true))

  (f sym [] [])

  (alter-var-root #'*trace* (constantly false))

  (ns ns3.core
    (:require
     [ns2.core :as ns2]))

  (ns2/f sym [] [])

  (ns ns4.core
    (:require
     [ns2.core :as ns2]
     [ns3.core :as ns3]))

  (ns2/f sym [] [])

  (alter-var-root #'ns2/*trace* (constantly true))

  (ns2/f sym [] [])

  ;;;
  )

(comment

  (lib/conform-explain
   ::argv
   '[a b c & ds :as asv]
   #_'[:as argv]
   #_'[ctx tag]
   #_'[[::a ::b ::c :as xs]]
   #_'[{::keys [a b c] :as xs} :as as]
   )

  (lib/conform-explain ::argv '[{:keys [a b c]}])
  (fm/fn [& as :as argv] )
  (fm/fn [:as argv])

  (fm/fn '(::a ::b ::c)
    )


  (defm -wrap-session ^{:fm/args    [router.lib/fn? ::opts]
                        :fm/handler -anomaly-handler}
    [handler opts]
    (fn [{:keys [uri] :as req}]
      (let [route (router/match-route uri)
            req   (into req #::session{:handler handler :opts opts :route route})]
        (session/req->res req))))

  (fn/defn -wrap-session ^{:fm/handler -anomaly-handler}
    [::handler ::opts]
    (fn [{:keys [uri] :as req}]
      (let [route (router/match-route uri)
            req   (into req #::session{:handler handler :opts opts :route route})]
        (session/req->res req))))

  (fn/defn -wrap-session ^{:fm/handler -anomaly-handler}
    [{::router.lib/fn handler} ::opts]
    (fn [{:keys [uri] :as req}]
      (let [route (router/match-route uri)
            req   (into req #::session{:handler handler :opts opts :route route})]
        (session/req->res req))))

  [handler opts]
  [::handler opts]
  [{::handler h} _opts]
  [{::router.lib/fn handler} opts]
  [{::router.lib/keys [a b c]} opts] ; potentially confusing
  [{::router.lib/keys [a b c] :as handler} opts]
  [{::handler {::router.lib/keys [a b c]}} opts]
  [{::router.lib/fn handler} opts]
  [{::router.lib/fn [h1 h2 h3]} opts]
  [{::router.lib/fn [h1 h2 h3]} opts]
  [{::router.lib/fn [h1 h2 h3]} ::opts]
  [{::router.lib/fn [h1 h2 h3]} {::opts {:keys [k1 k2 k3]}}]
  [{::router.lib/fn [h1 h2 h3]} {::opts {:keys [k1 k2 k3]}} & xs]
  [{::router.lib/fn [h1 h2 h3]} {::opts {:keys [k1 k2 k3]}} & ::xs]
  [{::router.lib/fn [h1 h2 h3]} {::opts {:keys [k1 k2 k3]}} & {::xs [x y z]}]

  [[:a {::b {:nu/keys [id]} ::c [c1 c2 c3]}]]

  (fn/defn ^:fm/throw! -wrap-session
    [{:as k}] #_=> [k] #_=> 
    [{:keys [k]}] #_=> [[k :as keys]]
    [{:keys [k] :as ks}] #_=> [{:keys [k] :as ks}]
    (fn [{:keys [uri] :as req}]
      (let [route (router/match-route uri)
            req   (into req #::session{:handler handler :opts opts :route route})]
        (session/req->res req))))

  [::a _b ::c & ::ds :as xs]

  [_]
  [_a]
  [::a]
  [{::a ,,,}]

  (fm/defn ^:fm/-- f1
    ([[::a ::b]]
     [[::c]]
     (inc a b))
    ([[::d ::e]]
     [[::f]]
     )
    )

  [[:a]]
  [[::a]]
  [[{:a ,,,}]]
  [[{::a ,,,}]]
  [[{,,, :a}]]
  [[{,,, ::a}]]

  (s/def ::email->recently-delivered?_args
    (s/keys
     :req [::db.developer/conn ::email/hash-key ::email/debounce-inst]))

  (defm email->recently-delivered? ^{:fm/args ::email->recently-delivered?_args}
    [{::db.developer/keys [conn]
      ::email/keys        [debounce-inst hash-key]}]
    (-> q/email->recently-delivered-count
        (d/q (d/db conn) hash-key debounce-inst)
        (ffirst)
        (or 0)
        (> 0)))

  (fm/defn email->recently-delivered?
    [[::db.developer/conn ::email/hash-key ::email/debounce-inst]]
    (-> q/email->recently-delivered-count
        (d/q (d/db conn) hash-key debounce-inst)
        (ffirst)
        (or 0)
        (> 0)))

  )

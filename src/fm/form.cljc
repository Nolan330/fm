(ns fm.form
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [fm.anomaly :as anomaly]
   [fm.form.fn :as fn]
   [fm.form.lib :as lib]))

  ;; TODO: refactor into `context`
(def ^:dynamic *positional-argument-prefix-symbol*
  `positional-argument)

(def ^:dynamic *variadic-argument-prefix-symbol*
  `variadic-argument)

(def ^:dynamic *signature-prefix-symbol*
  `signature)

(def ^:dynamic *default-anomaly-handler-symbol*
  `identity)

(def ^:dynamic *trace?*
  false)

(def ^:dynamic *default-trace-fn-symbol*
  `prn)

(s/def ::metadata
  (s/keys
   :req
   [:fm/ident            ; :some.ns1/fm1
    :fm/arglists]        ; '([a])
   :opt
   [:fm/doc              ; '(",,," nil ",,,") | ""
    :fm/args             ; '([int?])
    :fm/ret              ; '(int?)
    :fm/rel              ; '(#function[,,,]) '???
    :fm/trace            ; '(true) | '(#{,,,})
    :fm/conform          ; '(true) | '(#{,,,})
    :fm.anomaly/handler  ; '(#function[,,,]) '???
    :fm.anomaly/handler? ; '(true)
    ]))

(s/def ::bindings
  #_{:fm/k1 [{::symbol ::form}]
     :fm/k2 [{::symbol ::form}]})

(s/def ::context
  (s/keys
   :opt
   [::ns
    ::definition
    ::metadata
    ::bindings
    ::fn/conformed-definition
    ::fn/outer-metadata
    ::fn/inner-metadatas]))

'^{:fm/args} (^{:fm/args} [a] a)
'([::a
   {::b b
    c   ::c
    ::d [d1 d2 d3 :as ds]
    ::e {:keys [ek1 ek2 ek3] :as ekv}}
   :as args]
  [::f ::g ::h])
'([[::a
    {::b b
     c   ::c
     ::d [d1 d2 d3 :as ds]
     ::e {:keys [ek1 ek2 ek3] :as ekv}}
    :as args]]
  [[::f ::g ::h]])

(fm.form/->form ::fn/body {})

(defmulti  <<conformed-definition ::ident)
(defmethod <<conformed-definition ::fn
  [{::keys [definition] :as ctx}]
  (assoc
   ctx
   ::fn/conformed-definition
   (lib/conform-throw ::fn/definition definition)))

#_(defmethod <<conformed-definition :default
    [{::keys [definition] :as ctx}]
    (assoc
     ctx
     ::conformed-definition
     (lib/conform-throw ::definition definition)))

(defmulti  ->definition-tag ::ident)
(defmethod ->definition-tag ::fn
  [ctx]
  (get-in ctx [::fn/conformed-definition ::fn/rest 0]))

(defmulti  ->ident ::ident)
(defmethod ->ident ::fn
  [ctx]
  (keyword
   (str (get ctx ::ns))
   (name
    (or
     (get-in ctx [::fn/conformed-definition ::fn/simple-symbol?])
     (gensym 'fm)))))

#_(defmethod ->ident :default
    [ctx]
    (keyword
     (str (get ctx ::ns))
     (name
      (or
       (get-in ctx [::conformed-definition ::lib/simple-symbol?])
       (get-in ctx)
       (gensym 'fm)))))

(def ->symbol
  (comp symbol ->ident))

(def ->simple-symbol
  (comp symbol name ->ident))

(defmulti  ->default-metadata-form (fn [_tag _ctx] :default))
(defmethod ->default-metadata-form :default
  [tag ctx]
  (let [outer  (get-in ctx [::fn/outer-metadata tag])
        inners (map tag (get ctx ::fn/inner-metadatas))
        form   (cond
                 (every? nil? inners) outer
                 (nil? outer)         (apply list inners) ; NOTE: at least one
                 :else                (apply list (cons outer inners)))]
    form))

(defmulti  ->metadata-form (fn [tag _ctx] tag))
(defmethod ->metadata-form :default ; NOTE: keep all metadata
  [tag ctx]
  (->default-metadata-form tag ctx))

(s/def ::metadata
  (s/keys
   :req
   [:fm/ident            ; :some.ns1/fm1
    :fm/arglists]        ; '([a])
   :opt
   [:fm/doc              ; '(",,," nil ",,,") | ""
    :fm/args             ; '([int?])
    :fm/ret              ; '(int?)
    :fm/rel              ; '(#function[,,,]) '???
    :fm/trace            ; '(true) | '(#{,,,})
    :fm/conform          ; '(true) | '(#{,,,})
    :fm.anomaly/handler  ; '(#function[,,,]) '???
    :fm.anomaly/handler? ; '(true)
    ]))

(defmethod ->metadata-form :fm/ident
  [_ ctx]
  (->ident ctx))

(defmethod ->metadata-form :fm/arglists
  [tag ctx]
  (let [tag [tag (->definition-tag ctx)]]
    (->metadata-form tag ctx)))

(defmethod ->metadata-form [:fm/arglists ::fn/signature]
  [_ ctx]
  (let [signature (get-in ctx [::fn/conformed-definition ::fn/rest 1])
        argv      (get signature ::fn/argv)]
    (list `'~argv)))

(defmethod ->metadata-form [:fm/arglists ::fn/signatures]
  [_ ctx]
  (let [signatures (get-in ctx [::fn/conformed-definition ::fn/rest 1])
        argvs      (map (comp (fn [argv] `'~argv) ::fn/argv) signatures)]
    (apply list argvs)))

(defmethod ->metadata-form :fm/doc
  [tag ctx]
  (let [tag [tag (->definition-tag ctx)]]
    (->metadata-form tag ctx)))

(defmethod ->metadata-form [:fm/doc ::fn/signature]
  [_ ctx]
  (let [doc (get-in ctx [::fn/metadata :fm/doc])
        _   (lib/conform-throw ::fn/doc doc)]
    doc))

(defmethod ->metadata-form [:fm/doc ::fn/signatures]
  [_ ctx]
  (let [outer  (get-in ctx [::fn/outer-metadata :fm/doc])
        inners (map :fm/doc (get ctx ::fn/inner-metadatas))
        _      (map
                (partial lib/conform-throw ::fn/doc)
                (remove nil? (cons outer inners)))
        form   (cond
                 (every? nil? inners) outer
                 (nil? outer)         (apply list inners) ; NOTE: at least one
                 :else                (apply list (cons outer inners)))]
    form))

(defmethod ->metadata-form :fm/args
  [tag ctx]
  (let [tag [tag (->definition-tag ctx)]]
    (->metadata-form tag ctx)))

(defmethod ->metadata-form [:fm/args ::fn/signature]
  [_ ctx]
  (let [args (get-in ctx [::fn/metadata :fm/args])
        _    (lib/conform-throw ::fn/args args)]
    (list args)))

(defmethod ->metadata-form [:fm/args ::fn/signatures]
  [_ ctx]
  (let [outer      (get-in ctx [::fn/outer-metadata :fm/args])
        inners     (map :fm/args (get ctx ::fn/inner-metadatas))
        _          (map
                    (partial lib/conform-throw ::fn/args)
                    (remove nil? (cons outer inners)))
        signatures (get-in ctx [::fn/conformed-definition ::fn/rest 1])
        argvs      (map ::fn/argv signatures)
        args       (map-indexed
                    (fn [i inner]
                      (let [argv (nth argvs i)]
                        (if-let [args (or inner outer)]
                          (fn/zipv-args argv args)
                          (fn/zipv-args argv)))) ; TODO: warn
                    inners)]
    (apply list args)))

(defmethod ->metadata-form :fm/ret
  [tag ctx]
  (let [tag [tag (->definition-tag ctx)]]
    (->metadata-form tag ctx)))

(defmethod ->metadata-form [:fm/ret ::fn/signature]
  [_ ctx]
  (let [ret (get-in ctx [::fn/metadata :fm/ret])
        _   (lib/conform-throw ::fn/ret ret)]
    (list ret)))

(defmethod ->metadata-form [:fm/ret ::fn/signatures]
  [_ ctx]
  (let [outer  (get-in ctx [::fn/outer-metadata :fm/ret])
        inners (map :fm/ret (get ctx ::fn/inner-metadatas))
        _      (map
                (partial lib/conform-throw ::fn/ret)
                (remove nil? (cons outer inners)))
        rets   (map (fn [inner] (or inner outer)) inners)]
    (apply list rets)))

(defmethod ->metadata-form :fm/rel
  [tag ctx]
  (let [tag [tag (->definition-tag ctx)]]
    (->metadata-form tag ctx)))

(defmethod ->metadata-form [:fm/rel ::fn/signature]
  [_ ctx]
  (let [rel (get-in ctx [::fn/metadata :fm/rel])
        _   (lib/conform-throw ::fn/rel rel)]
    (list rel)))

(defmethod ->metadata-form [:fm/rel ::fn/signatures]
  [_ ctx]
  (let [outer  (get-in ctx [::fn/outer-metadata :fm/rel])
        inners (map :fm/rel (get ctx ::fn/inner-metadatas))
        _      (map
                (partial lib/conform-throw ::fn/rel)
                (remove nil? (cons outer inners)))
        rels   (map (fn [inner] (or inner outer)) inners)]
    (apply list rels)))

(defmethod ->metadata-form :fm/trace
  [tag ctx]
  (let [tag [tag (->definition-tag ctx)]]
    (->metadata-form tag ctx)))

(defmethod ->metadata-form [:fm/trace ::fn/signature]
  [_ ctx]
  (let [trace (get-in ctx [::fn/metadata :fm/trace])
        _     (lib/conform-throw ::fn/trace trace)]
    (list trace)))

(defmethod ->metadata-form [:fm/trace ::fn/signatures]
  [_ ctx]
  (let [outer  (get-in ctx [::fn/outer-metadata :fm/trace])
        inners (map :fm/trace (get ctx ::fn/inner-metadatas))
        _      (map
                (partial lib/conform-throw ::fn/trace)
                (remove nil? (cons outer inners)))
        traces (map (fn [inner] (or inner outer)) inners)]
    (apply list traces)))

(defmethod ->metadata-form :fm/conform
  [tag ctx]
  (let [tag [tag (->definition-tag ctx)]]
    (->metadata-form tag ctx)))

(defmethod ->metadata-form [:fm/conform ::fn/signature]
  [_ ctx]
  (let [conform (get-in ctx [::fn/metadata :fm/conform])
        _       (lib/conform-throw ::fn/conform conform)]
    (list conform)))

(defmethod ->metadata-form [:fm/conform ::fn/signatures]
  [_ ctx]
  (let [outer    (get-in ctx [::fn/outer-metadata :fm/conform])
        inners   (map :fm/conform (get ctx ::fn/inner-metadatas))
        _        (map
                  (partial lib/conform-throw ::fn/conform)
                  (remove nil? (cons outer inners)))
        conforms (map (fn [inner] (or inner outer)) inners)]
    (apply list conforms)))

(defmethod ->metadata-form :fm.anomaly/handler
  [tag ctx]
  (let [tag [tag (->definition-tag ctx)]]
    (->metadata-form tag ctx)))

(defmethod ->metadata-form [:fm.anomaly/handler ::fn/signature]
  [_ ctx]
  (let [handler (get-in ctx [::fn/metadata :fm.anomaly/handler])
        _       (lib/conform-throw ::fn/handler handler)]
    (list handler)))

(defmethod ->metadata-form [:fm.anomaly/handler ::fn/signatures]
  [_ ctx]
  (let [outer    (get-in ctx [::fn/outer-metadata :fm.anomaly/handler])
        inners   (map :fm.anomaly/handler (get ctx ::fn/inner-metadatas))
        _        (map
                  (partial lib/conform-throw ::fn/handler)
                  (remove nil? (cons outer inners)))
        handlers (map (fn [inner] (or inner outer)) inners)]
    (apply list handlers)))

(defmethod ->metadata-form :fm.anomaly/handler?
  [tag ctx]
  (let [tag [tag (->definition-tag ctx)]]
    (->metadata-form tag ctx)))

(defmethod ->metadata-form [:fm.anomaly/handler? ::fn/signature]
  [_ ctx]
  (let [handler? (get-in ctx [::fn/metadata :fm.anomaly/handler?])
        _        (lib/conform-throw ::fn/handler? handler?)]
    (list handler?)))

(defmethod ->metadata-form [:fm.anomaly/handler? ::fn/signatures]
  [_ ctx]
  (let [outer     (get-in ctx [::fn/outer-metadata :fm.anomaly/handler?])
        inners    (map :fm.anomaly/handler? (get ctx ::fn/inner-metadatas))
        _         (map
                   (partial lib/conform-throw ::fn/handler?)
                   (remove nil? (cons outer inners)))
        handler?s (map (fn [inner] (or inner outer)) inners)]
    (apply list handler?s)))

(defmulti  ->metadata ->definition-tag)
(defmethod ->metadata ::fn/signature
  [{::fn/keys [conformed-definition] :as ctx}]
  (let [metadata  (merge
                   (meta (::fn/simple-symbol? conformed-definition))
                   (meta (::fn/argv (second (::fn/rest conformed-definition)))))
        ctx       (assoc ctx ::fn/metadata metadata)
        xf        (map (fn [k] [k (->metadata-form k ctx)]))
        meta-keys (into #{:fm/ident :fm/arglists} (keys metadata))
        metadata  (into {} xf meta-keys)]
    metadata))

(defmethod ->metadata ::fn/signatures
  [{::fn/keys [conformed-definition]
    ::keys    [definition]
    :as       ctx}]
  (let [signatures (if (symbol? (first definition)) (next definition) definition)
        outer      (merge
                    (meta (first signatures))
                    (meta (::fn/simple-symbol? conformed-definition)))
        inners     (map
                    (comp meta ::fn/argv)
                    (second (::fn/rest conformed-definition)))
        ctx        (assoc ctx ::fn/outer-metadata outer ::fn/inner-metadatas inners)
        xf         (map (fn [k] [k (->metadata-form k ctx)]))
        meta-keys  (into #{:fm/ident :fm/arglists} (mapcat keys) (cons outer inners))
        metadata   (into {} xf meta-keys)]
    metadata))

#_(defmethod ->metadata-form [:fm/arglists ::sequent/signature]
    [tag ctx]
    (let [tag (conj tag (->sequent-context-tag ctx))]
      (->metadata-form tag ctx)))
#_(defmethod ->metadata-form [:fm/arglists ::sequent/signature ::sequent/positional] [_ ctx]) #_[::a ::b] #_-> #_([a b])
#_(defmethod ->metadata-form [:fm/arglists ::sequent/signature ::sequent/associative] [_ ctx]) #_[[::a ::b]] #_-> #_([{a ::a b ::b}])
#_(defmethod ->metadata-form [:fm/arglists ::sequent/signatures] [_ ctx]) ; TODO: delegate
#_(defmethod ->metadata-form [:fm/arglists ::sequent/signatures ::sequent/positional] [_ ctx])
#_(defmethod ->metadata-form [:fm/arglists ::sequent/signatures ::sequent/associative] [_ ctx])

(defmulti  <<metadata ::ident)
(defmethod <<metadata ::fn
  [ctx]
  (assoc ctx ::metadata (->metadata ctx)))

(defmulti  ->bindings ::ident)
(defmethod ->bindings ::fn
  [{::keys [metadata]}]
  (let [xf       (map (fn [[k form]]
                        [k (if (sequential? form)
                             (::bindings
                              (reduce
                               (fn [acc form]
                                 (let [f       (fnil identity (gensym (name k)))
                                       acc     (update-in acc [::form=>symbol form] f)
                                       sym     (get-in acc [::form=>symbol form])
                                       binding {::symbol sym ::form form}
                                       acc     (update acc ::bindings conj binding)]
                                   acc))
                               {::form=>symbol {} ::bindings []}
                               form))
                             [{::symbol (gensym (name k)) ::form form}])]))
        bindings (into {} xf metadata)]
    bindings))

(defmulti  <<bindings ::ident)
(defmethod <<bindings ::fn
  [ctx]
  (assoc ctx ::bindings (->bindings ctx)))

(s/def ::parameters
  (s/keys
   :req
   [::ns
    ::definition]))

  ;; ALT: `interpret`, `analyze`, `read`
(defmulti  ->context ::ident)
(defmethod ->context ::fn
  [parameters]
  (->>
   parameters
   <<conformed-definition
   <<metadata
   <<bindings))

(defmulti  ->forms (fn [tag _ctx] tag))
(defmulti  ->form  (fn [tag _ctx] tag))
(defmethod ->form ::fn
  [_ ctx]
  (let [bindings   (->forms ::fn/context-bindings ctx)
        sym        (->form ::fn/symbol ctx)
        definition (->forms ::fn/definition ctx)
        metadata   (->form ::fn/metadata ctx)]
    `(let [~@bindings]
       (with-meta
         (fn ~sym ~@definition)
         ~metadata))))

(defmethod ->forms ::fn/context-bindings
  [_ ctx]
  (->>
   (get ctx ::bindings) ; TODO: warn on conflict
   (vals)
   (flatten)
   (distinct)
   (mapcat (juxt ::symbol ::form)))) ; TODO: list?

(defmethod ->form ::fn/symbol
  [_ ctx]
  (symbol (name (get-in ctx [::metadata :fm/ident]))))

(defmethod ->forms ::fn/definition
  [tag ctx]
  (let [tag [tag (->definition-tag ctx)]]
    (->forms tag ctx)))

(defmethod ->forms [::fn/definition ::fn/signature]
  [_ ctx]
  (let [signature (get-in ctx [::fn/conformed-definition ::fn/rest 1])
        argv      (get signature ::fn/argv)
        ctx       (into ctx {::signature-index 0})
        body      (->form ::fn/body ctx)]
    (list argv body)))

(defmethod ->forms [::fn/definition ::fn/signatures]
  [_ ctx]
  (let [signatures (get-in ctx [::fn/conformed-definition ::fn/rest 1])
        forms      (map-indexed
                    (fn [i signature]
                      (let [argv (get signature ::fn/argv)
                            ctx  (into ctx {::signature-index i})
                            body (->form ::fn/body ctx)]
                        (list argv body)))
                    signatures)]
    (apply list forms)))

(defmethod ->form ::fn/metadata
  [_ ctx]
  (into
   (hash-map)
   (map
    (fn [[k forms]]
      [k (if (sequential? forms)
           `(list
             ~@(map-indexed
                (fn [i form]
                  (or
                   (get-in ctx [::bindings k i ::symbol])
                   form))
                forms))
           (or
            (get-in ctx [::bindings k 0 ::symbol])
            forms))]))
   (get ctx ::metadata)))

(defmethod ->form ::fn/body
  [_ ctx]
  (let [handler (->form ::fn/handler ctx)
        try     (->form ::fn/try ctx)]
    `(let [res# ~try]
       (if (anomaly/anomalous? res#)
         (~handler res#)
         res#))))

(defmethod ->form ::fn/handler
  [_ ctx]
  (let [index (or (get ctx ::signature-index) 0)]
    (or
     (get-in ctx [::bindings :fm.anomaly/handler index ::symbol])
     *default-anomaly-handler-symbol*)))

(defn ->trace?
  [ctx]
  (let [index (or (get ctx ::signature-index) 0)
        form  (or (get-in ctx [::bindings :fm/trace index ::form]) *trace?*)]
    (cond
      (set? form) form
      (not form)  (constantly false)
      :else       (constantly true))))

(defmethod ->form ::fn/try
  [_ ctx]
  (let [trace? (->trace? ctx) ; ALT: lib
        ident  (->ident ctx)  ; ALT: `->form`
        args   (->form ::fn/args ctx)
        trace  (when (trace? :fm/args)
                 [(list
                   (->form ::fn/trace ctx)
                   {:fm/ident ident :fm.trace/args args})])
        body   nil #_(if (seq args)
                 (->form ::fn/anomalous? ctx)
                 (->form ::fn/ret ctx))]
    `(try
       ~@trace
       ~body
       (catch Throwable throw#
         {:fm/ident         ~ident
          :fm.anomaly/ident :fm.anomaly/throw
          :fm.anomaly/args  ~args
          :fm.anomaly/data  throw#}))))

(defmethod ->form ::fn/argv
  [tag ctx]
  (let [tag [tag (->definition-tag ctx)]]
    (->form tag ctx)))

(defmethod ->form [::fn/argv ::fn/signature]
  [_ ctx]
  (get-in ctx [::fn/conformed-definition ::fn/rest 1 ::fn/argv]))

(defmethod ->form [::fn/argv ::fn/signatures]
  [_ ctx]
  (let [index (or (get ctx ::signature-index) 0)]
    (get-in ctx [::fn/conformed-definition ::fn/rest 1 index ::fn/argv])))

(defmethod ->form ::fn/args
  [_ ctx]
  (mapv fn/arg->symbol (->form ::fn/argv ctx)))

(defmethod ->form ::fn/trace
  [_ ctx]
  (let [index (or (get ctx ::signature-index) 0)]
    (or
     (get-in ctx [::bindings :fm/trace index ::symbol])
     *default-trace-fn-symbol*)))

(defmethod ->form ::fn/anomalous?
  [_ ctx]
  (let [args    (->form ::fn/args ctx)
        handler (->form ::fn/handler ctx)
        binding (->form ::fn/args-binding ctx)]
    `(if (anomaly/anomalous? ~args)
       (~handler ~args)
       ~binding)))

(defmethod ->form ::fn/args-binding
  [_ ctx]
  (let [argv      (->form ::fn/argv ctx)
        conform?  (->conform? ctx)
        trace?    (->trace? ctx)
        ident     (->ident ctx)
        bindings  {::fn/args           {::symbol (gensym 'args) ::form (->form ::fn/args ctx)}
                   ::fn/args-spec      {::symbol (gensym 'args-spec) ::form (->form ::fn/args-spec ctx)}
                   ::fn/conformed-args {::symbol (gensym 'conformed-args) ::form (->form ::fn/conformed-args ctx)}}
        conformed (get-in bindings [::fn/conformed-args ::symbol])
        bindings  (if (conform? :fm/args)
                    (into bindings [::fn/conformed-argv {::symbol argv ::form conformed}])
                    bindings)
        ctx       (update ctx ::bindings into bindings)
        bindings  (mapcat (juxt ::symbol ::form) (vals bindings))
        trace     (when (and (trace? :fm/args) (conform? :fm/args))
                    [(list
                      (->form ::fn/trace ctx)
                      {:fm/ident ident :fm/args conformed})])
        body      (if (get-in ctx [::metadata :fm/args])
                    (->form ::fn/args-anomaly)
                    (->form ::fn/ret))]
    `(let [~@bindings]
       ~@trace
       ~body)))

(defmethod ->form ::fn/ret [_ ctx])

(defmethod ->form ::sequent/body [_ ctx])

(defn ->context-binding-form
  [context]
  `context-binding-form)

(defn fm
  [parameters]
  (let [context (->context (into parameters {::ident ::fn}))
        form    (->context-binding-form context)]
    form))

(defmulti  ->arglists ::ident)
(defmethod ->arglists ::fn
  [{::fn/keys [definition]}]
  (case (first (::fn/rest definition))
    ::fn/signature  (list (::fn/argv (second (::fn/rest definition))))
    ::fn/signatures (apply list (map ::fn/argv (second (::fn/rest definition))))))

(defmulti ->var-metadata-kv key)
(defmulti ->var-metadata-kv :fm/arglists)
(defmulti ->var-metadata-kv :fm/doc)
(defmulti ->var-metadata-kv :default [_] nil)

(defn ->var-metadata
  [{::keys [metadata] :as context}]
  (into
   (hash-map)
   (map ->var-metadata-kv)
   (into
    metadata
    {:fm/arglists (->arglists context)})))

(defn ->var-sym
  [{::keys [metadata] :as context}]
  (with-meta
    (symbol (name (:fm/ident metadata)))
    (->var-meta context)))

(defn defm
  [parameters]
  (let [context (->context (into parameters {::ident ::fn}))
        sym     (->var-sym context)]
    `(def ~sym ~(fm parameters))))

(s/def :fm/ident
  qualified-keyword?)

#_(s/def :fm/doc
    (s/or
     ::fm/signature  string?
     ::fm/signatures (s/coll-of string?)))

(s/def :fm/arglists
  (s/coll-of vector?))

(s/def ::metadata
  (s/keys
   :req
   [:fm/ident]
   :opt
   [:fm/doc              ; '(",,," nil ",,,") | ""
    :fm/arglists         ; '([a] [a b] [a b & cs]) | '([a])
    :fm/args             ; '([int? int? & int?]) | '([int? int? & int?] [pos?] nil nil) | '([int?])
                         ; '([int?] [int? int?] [int? int? & int?])
    :fm/ret              ; '(int? neg? even?)
    :fm/rel              ; '() ??? '(s/,,,)
    :fm/trace            ; '(true) | '(false true false)
    :fm/conform          ; '(true) | '(#{}) | '(#{} true false) | ()
    :fm.anomaly/handler  ; '(h) | '(h f1 f2) | '()
    :fm.anomaly/handler? ; => binding *ignore* (conj (:fm/ignore ,,,) :fm.anomaly/received?)
                         ;
    :fm/sequent          ; ,,, ; ALT: :fm.sequent/ident
    :fm.sequent/left     ; ,,,
    :fm.sequent/right    ; ,,,
    :fm.sequent/nonse    ; ,,,
    :fm/ignore           ; ,,,
    ]))

(s/def ::metadata
  (s/keys
   :req
   [:fm/ident]           ; :some.ns1/fm1
   :opt
   [:fm/doc              ; '(",,," nil ",,,") | ""
    :fm/arglists         ; '([a])
    :fm/args             ; '([int?])
    :fm/ret              ; '(int?)
    :fm/rel              ; '(#function[,,,]) '???
    :fm/trace            ; '(true) | '(#{,,,})
    :fm/conform          ; '(true) | '(#{,,,})
    :fm.anomaly/handler  ; '(#function[,,,]) '???
    :fm.anomaly/handler? ; '(true)
    ]))

(s/def ::metadata
  (s/keys
   :req
   [:fm/ident]           ; :some.ns1/fm1
   :opt
   [:fm/doc              ; '(",,," nil ",,,") | ""
    :fm/arglists         ; '([a])
    :fm/args             ; '([int?])
    :fm/ret              ; '([int?])
    :fm/rel              ; '(#function[,,,]) '???
    :fm/trace            ; '(true) | '(#{,,,})
    :fm/conform          ; '(true) | '(#{,,,})
    :fm.anomaly/handler  ; '(#function[,,,]) '???
    :fm.anomaly/handler? ; '(true)
    ;;
    :fm.sequent/ident    ; :fm.sequent/merge
    :fm.sequent/left     ; '([int?])
    :fm.sequent/right    ; '([int?])
    ]))

(s/def ::metadata
  (s/keys
   :req
   [:fm/ident]           ; :some.ns1/fm1
   :opt
   [:fm/doc              ; '(",,," nil ",,,") | ""
    :fm/arglists         ; '([a] [a b] [a b & cs])
    :fm/args             ; '([int?] [int? int?] [int? int? & int?])
    :fm/ret              ; '([int?] [int?] [int?])
    :fm/rel              ; '(#function[,,,] #fun ,,,,) '???
    :fm/trace            ; '(true true true) | '(#{,,,})
    :fm/conform          ; '(true false true) | '(#{,,,})
    :fm.anomaly/handler  ; '(#function[,,,]) '???
    :fm.anomaly/handler? ; '(true)
    ]))

:fm.anomaly/handler? ; => binding *fm/ignore* (conj (:fm/ignore ,,,) :fm.anomaly/received?)

(s/def ::metadata
  (s/keys
   :req
   [:fm/ident]
   :opt
   [:fm/doc              ; '(",,," nil ",,,") | ""
    :fm/arglists         ; '([a] [a b] [a b & cs]) | '([a])
    :fm/args             ; '([int? int? & int?]) | '([int? int? & int?] [pos?] nil nil) | '([int?])
                                        ; '([int?] [int? int?] [int? int? & int?])
    :fm/ret              ; '(int? neg? even?)
    :fm/rel              ; '() ??? '(s/,,,)
    :fm/trace            ; '(true) | '(false true false)
    :fm/conform          ; '(true) | '(#{}) | '(#{} true false) | ()
    :fm.anomaly/handler  ; '(h) | '(h f1 f2) | '()
    :fm.anomaly/handler? ; '(true false true)
    :fm.sequent/ident    ; :fm.sequent/merge
    :fm.sequent/context  ; :fm.sequent/associative positional optional? ,,,
    ]))

  ;; NOTE: cut elimination, runtime `*ignore*`

(comment

  ;; TODO: dynamic `:fm/args`
  (def args1 [int?])
  (fm ^{:fm/args args1} [x] (+ x 1))

  ;; TODO: warnings (log level?), `conformsplain`
  (fm ^{:fm/args [int?]} [x1 x2])

  ;;;
  )

(comment

  (def f1 (fn [a b c] (mapv inc [a b c])))
  (def f2 (fn [d e f] (mapv inc [d e f])))

  (defn comp1
    [& fs]
    (fn [& args] (reduce (fn [acc f] (apply f acc)) args fs)))

  ((comp1
    f1
    f2) 1 2 3)

  (def f3 (fn [[a b c]] (mapv inc [a b c])))
  (def f4 (fn [[d e f]] (mapv inc [d e f])))

  (defn comp2
    [& fs]
    (fn [& args]
      (let [args (if (and (= (count args) 1) (sequential? (first args))) (first args) args)]
        (reduce
         (fn [acc f] (f acc)) args fs))))

  ((comp2
    f3
    f4) {:a 1 :b 2 :c 3})

  ;;;
  )

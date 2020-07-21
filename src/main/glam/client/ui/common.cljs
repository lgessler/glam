(ns glam.client.ui.common
  (:require [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.components :as c]
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defn loader []
  (dom/div :.ui.massive.active.text.loader "Loading"))

;; form stuff
(defn field-attrs
  "A helper function for getting aspects of a particular field."
  [component field validator]
  (let [form (c/props component)
        entity-ident (c/get-ident component form)
        id (str (first entity-ident) "-" (second entity-ident))
        is-dirty? (fs/dirty? form field)
        clean? (not is-dirty?)
        validity (validator form field)
        is-invalid? (= :invalid validity)
        value (get form field "")]
    {:dirty?   is-dirty?
     :ident    entity-ident
     :id       id
     :clean?   clean?
     :validity validity
     :invalid? is-invalid?
     :value    value}))

(defn input-field
  [id attrs]
  (dom/input
    (merge {:id   id
            :name id}
           attrs)))

(defn input-with-label
  [component field label validator validation-message input-attrs]
  (let [{:keys [dirty? invalid?]} (field-attrs component field validator)]
    (c/fragment
      (dom/div :.field {:classes [(when invalid? "error") (when dirty? "warning")]}
               (dom/label {:htmlFor (str field)} label)
               (input-field (str field) input-attrs))
      (when invalid?
        (dom/div :.ui.error.message {} validation-message)))))


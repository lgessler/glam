(ns glam.client.ui.common
  (:require [com.fulcrologic.fulcro.dom :as dom]))

(defn loader []
  (dom/div :.ui.massive.active.text.loader "Loading"))
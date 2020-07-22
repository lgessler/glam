(ns glam.models.common)

(defn server-message [msg]
  {:server/message msg
   :server/error?  false})

(defn server-error [msg]
  {:server/message msg
   :server/error?  true})
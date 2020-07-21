(ns glam.models.user-common)

(defn valid-password [password] (>= (count password) 8))
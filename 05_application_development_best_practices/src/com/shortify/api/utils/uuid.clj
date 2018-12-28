(ns com.shortify.api.utils.uuid)

(defn create-random-uuid
  []
  (.toString (java.util.UUID/randomUUID)))

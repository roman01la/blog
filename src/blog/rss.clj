(ns blog.rss
  (:require [hiccup.core :as h]
            [clojure.string :as cstr])
  (:import (java.util Date)))

(defn cdata [s]
  (str "<![CDATA[" s "]]>"))

(defn render-item [config [file {{:keys [title date]} :metadata html :html}]]
  (let [link (str (:host config) file ".html")]
    [:item
     [:title (cdata (first title))]
     [:link link]
     [:guid {:isPermaLink "false"} link]
     [:pubDate (first date)]
     ["content:encoded"
      (-> html
          (cstr/replace #"<" "&lt;")
          (cstr/replace #">" "&gt;"))]]))

(defn render [config file-names posts]
  (h/html
    [:rss {"xmlns:dc"      "http://purl.org/dc/elements/1.1/"
           "xmlns:content" "http://purl.org/rss/1.0/modules/content/"
           "xmlns:atom"    "http://www.w3.org/2005/Atom"
           :version        "2.0"}
     [:channel
      [:title (cdata (:title config))]
      [:description (cdata (:description config))]
      [:link (:host config)]
      [:lastBuildDate (.toString (Date.))]
      (->> (zipmap file-names posts)
           (map #(render-item config %)))]]))

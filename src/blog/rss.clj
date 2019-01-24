(ns blog.rss
  (:require [hiccup.core :as h]
            [clojure.string :as cstr]
            [blog.utils :as utils])
  (:import (java.util Date)))

(defn cdata [s]
  (str "<![CDATA[" s "]]>"))

(defn render-item [config {{:keys [title date]} :metadata html :html file :file}]
  (let [link (str (:host config) file ".html")]
    [:item
     [:title (cdata (first title))]
     [:link link]
     [:guid {:isPermaLink "false"} link]
     [:pubDate (utils/format-date date)]
     ["content:encoded"
      (-> html
          (cstr/replace #"<" "&lt;")
          (cstr/replace #">" "&gt;"))]]))

(defn render [config posts]
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
      (map #(render-item config %) posts)]]))

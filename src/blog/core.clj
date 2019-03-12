(ns blog.core
  (:require [hiccup.page :as p]
            [blog.rss :as rss]
            [markdown.core :as md]
            [clojure.string :as cstr]
            [blog.utils :as utils])
  (:import (java.io File)
           (java.util Date Calendar)))

(def config (read-string (slurp "blog.edn")))

(declare ^:dynamic *args*)

(def wpm 200)

(def year
  (let [cal (Calendar/getInstance)]
    (.setTime cal (Date.))
    (.get cal Calendar/YEAR)))

(defn head [{:keys [title link]}]
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width"}]
   [:meta {:name "theme-color" :content (:theme-color config)}]
   [:meta {:name "description" :content (:description config)}]
   [:meta {:property "og:url" :content link}]
   [:meta {:property "og:title" :content title}]
   [:meta {:property "og:description" :content (:description config)}]
   [:meta {:name "twitter:card" :content "summary"}]
   [:meta {:name "twitter:creator" :content (str "@" (:twitter config))}]
   [:meta {:name "twitter:title" :content title}]
   [:meta {:name "twitter:description" :content (:description config)}]
   [:link {:rel "alternate" :type "application/rss+xml" :href "/rss.xml"}]
   [:link {:rel "shortcut icon" :href "/assets/icon.png"}]
   [:title title]
   (p/include-css "https://fonts.googleapis.com/css?family=Lora%7CMerriweather")
   (p/include-css (get *args* "--css"))])

(defn header [{:keys [description?] :or {description? true}}]
  [:header.page-header
   [:img.profile {:src "/assets/profile.jpg"}]
   [:div
    [:h1 [:a {:href "/"} (:title config)]]
    (when description?
      (let [link (str "https://mobile.twitter.com/" (:twitter config))]
        [:p "This blog is mostly brain dump by "
         [:a {:href link :target "_blank"}
          (str "@" (:twitter config))]]))]])

(defn content [& children]
  `[:main ~@children])

(defn footer []
  [:footer.page-footer
   [:p
    (let [items (interpose " . " (:footer config))]
      (for [item items]
        (if (string? item)
          item
          [:a {:href (second item) :target "_blank"} (first item)])))]
   [:p (str "&copy; " year " ")]])

(defn post [{:keys [link title date author html duration comments]}]
  [:article
   [:header
    (let [title [:h1 title]]
      (if link
        [:a {:href link} title]
        title))
    [:div
     [:time (utils/format-date date)]
     (when duration
       [:span.duration (str duration " min read")])]]
   (when html
     [:div html])
   [:footer
    #_(when author
        [:p author " - " (when comments [:a {:href comments} "Discuss on Reddit"])])]])

(defn render-excerpt
  [{{:keys [title date]} :metadata
    html                 :html
    file                 :file}]
  (post {:title    (first title)
         :date     date
         :link     (str file ".html")
         :duration (-> (cstr/split html #" +") count (/ wpm) double Math/round)}))

(defn render-page [posts]
  (p/html5
    (head {:title (:title config)})
    [:body.index
     (header {})
     (content
       (map render-excerpt posts))
     (footer)]))

(defn newsletter-block []
  [:a.patreon-link {:href "https://www.patreon.com/bePatron?c=1239559" :target "_blank"}
   "Become a patron"])

(defn render-post
  [{{:keys [title author date comments]} :metadata
    html                                 :html
    file-name                            :file}]
  [file-name
   (p/html5
     (head {:title (first title) :link (str (:host config) file-name ".html")})
     [:body.post-page
      (header {:description? false})
      (content
        (post {:title    (first title)
               :date     date
               :author   (first author)
               :comments (first comments)
               :html     html})
        (newsletter-block))
      (footer)])])


(defn render-posts [posts]
  (->> posts
       (map render-post)
       (run! #(spit (str "static/" (first %) ".html") (second %)))))

(defn render-main [posts]
  (->> (render-page posts)
       (spit "static/index.html")))

(defn render-rss [posts]
  (->> (rss/render config posts)
       (spit "static/rss.xml")))

(defn ignore-post? [^File f]
  (.startsWith (.getName f) "__"))

(defn parse-date [{{:keys [date]} :metadata :as post}]
  (let [date (->> (first date)
                  (re-matches #"^(\d\d)-(\d\d)-(\d\d\d\d)$")
                  rest
                  (map #(Integer/parseInt ^String %))
                  (zipmap [:date :month :year]))
        d (doto (Date.)
            (.setDate (:date date))
            (.setYear (- (:year date) 1900))
            (.setMonth (dec (:year date))))]
    (-> post
        (assoc-in [:metadata :date] date)
        (assoc-in [:metadata :d] d))))

(defn render-blog []
  (let [files (->> (file-seq (File. "posts"))
                   (filter #(and (.isFile ^File %) (not (ignore-post? %)))))
        file-names (->> files
                        (map #(.getPath ^File %))
                        (map #(re-find #"\/(.*)\.md$" %))
                        (map second))
        posts (->> files
                   (map slurp)
                   (map #(clojure.string/replace % "'" "’"))
                   (map #(md/md-to-html-string-with-meta %))
                   (map parse-date)
                   (map #(assoc %2 :file %1) file-names)
                   (sort-by #(-> % :metadata :d))
                   reverse)]
    (render-posts posts)
    (render-main posts)
    (render-rss posts)))

(defn parse-args [args]
  (->> (partition 2 args)
       (map vec)
       (into {})))

(defn -main [& args]
  (binding [*args* (parse-args args)]
    (render-blog)))

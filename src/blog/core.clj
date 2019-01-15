(ns blog.core
  (:require [hiccup.page :as p]
            [blog.rss :as rss]
            [markdown.core :as md]
            [clojure.string :as cstr])
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
     [:time {:datetime date} date]
     (when duration
       [:span.duration (str duration " min read")])]]
   (when html
     [:div html])
   [:footer
    (when author
      [:p author " - " (when comments [:a {:href comments} "Discuss on Reddit"])])]])

(defn render-excerpt [[file {{:keys [title date]} :metadata html :html}]]
  (post {:title    (first title)
         :date     (first date)
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
  [:form {:action "https://romanliutikov.us15.list-manage.com/subscribe/post?u=f0528471b2156274a80671df0&amp;id=aeac730aeb"
          :method "post"}
   [:input {:type        "email"
            :name        "EMAIL"
            :required    true
            :placeholder "Your email address"}]
   [:div {:style       "position: absolute; left: -5000px;"
          :aria-hidden "true"}
    [:input {:name     "b_f0528471b2156274a80671df0_aeac730aeb"
             :tabindex -1}]]
   [:button "Subscribe"]])

(defn render-post
  [file-name {{:keys [title author date comments]} :metadata html :html}]
  (p/html5
    (head {:title (first title) :link (str (:host config) file-name ".html")})
    [:body.post-page
     (header {:description? false})
     (content
       (post {:title    (first title)
              :date     (first date)
              :author   (first author)
              :comments (first comments)
              :html     html})
       (newsletter-block))
     (footer)]))


(defn render-posts [file-names posts]
  (->> posts
       (map render-post file-names)
       (zipmap file-names)
       (run! #(spit (str "static/" (first %) ".html") (second %)))))

(defn render-main [file-names posts]
  (->> (zipmap file-names posts)
       (render-page)
       (spit "static/index.html")))

(defn render-rss [file-names posts]
  (->> (rss/render config file-names posts)
       (spit "static/rss.xml")))

(defn ignore-post? [^File f]
  (.startsWith (.getName f) "__"))

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
                   (map #(md/md-to-html-string-with-meta %)))]
    (render-posts file-names posts)
    (render-main file-names posts)
    (render-rss file-names posts)))

(defn parse-args [args]
  (->> (partition 2 args)
       (map vec)
       (into {})))

(defn -main [& args]
  (binding [*args* (parse-args args)]
    (render-blog)))

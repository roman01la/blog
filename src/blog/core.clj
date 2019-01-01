(ns blog.core
  (:require [hiccup.page :as p]
            [hiccup.core :as h]
            [markdown.core :as md]
            [clojure.string :as cstr])
  (:import (java.io File)
           (java.util Date Calendar)))

(def wpm 200)

(def year
  (let [cal (Calendar/getInstance)]
    (.setTime cal (Date.))
    (.get cal Calendar/YEAR)))

(defn head [{:keys [title]}]
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width"}]
   [:meta {:name "theme-color" :content "#0000ff"}]
   [:meta {:name "description" :content "This blog is mostly brain dump by Roman Liutikov"}]
   [:link {:rel "alternate" :type "application/rss+xml" :href "/rss.xml"}]
   [:link {:rel "shortcut icon" :href "/icon.png"}]
   [:title title]
   (p/include-css "https://fonts.googleapis.com/css?family=Lora%7CMerriweather")
   (p/include-css "main.css")])

(defn header [{:keys [description?] :or {description? true}}]
  [:header.page-header
   [:img.profile {:src "/profile.jpg"}]
   [:div
    [:h1 [:a {:href "/"} "Roman’s Blog"]]
    (when description?
      [:p "This blog is mostly brain dump by " [:a {:href "https://mobile.twitter.com/roman01la" :target "_blank"} "@roman01la"]])]])

(defn content [& children]
  `[:main ~@children])

(defn footer []
  [:footer.page-footer
   [:p
    (let [items (interpose " . " {"GitHub"  "https://github.com/roman01la"
                                  "Twitter" "https://mobile.twitter.com/roman01la"
                                  "RSS"     "/rss.xml"})]
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
    (head {:title "Roman’s Blog"})
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
  [{{:keys [title author date comments]} :metadata html :html}]
  (p/html5
    (head {:title (str (first title) " | Roman’s Blog")})
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
       (map render-post)
       (zipmap file-names)
       (run! #(spit (str "static/" (first %) ".html") (second %)))))

(defn render-main [file-names posts]
  (->> (zipmap file-names posts)
       (render-page)
       (spit "static/index.html")))

(defn cdata [s]
  (str "<![CDATA[" s "]]>"))

(defn -render-rss-item [[file {{:keys [title date]} :metadata html :html}]]
  (let [link (str "https://romanliutikov.com/" file ".html")]
    [:item
     [:title (cdata (first title))]
     [:link link]
     [:guid {:isPermaLink "false"} link]
     [:pubDate (first date)]
     ["content:encoded"
      (-> html
          (cstr/replace #"<" "&lt;")
          (cstr/replace #">" "&gt;"))]]))

(defn -render-rss [file-names posts]
  (h/html
    [:rss {"xmlns:dc"      "http://purl.org/dc/elements/1.1/"
           "xmlns:content" "http://purl.org/rss/1.0/modules/content/"
           "xmlns:atom"    "http://www.w3.org/2005/Atom"
           :version        "2.0"}
     `[:channel
       [:title ~(cdata "Roman’s Blog")]
       [:description ~(cdata "This blog is mostly brain dump by Roman Liutikov")]
       [:link "https://romanliutikov.com/"]
       [:lastBuildDate ~(.toString (Date.))]
       ~@(->> (zipmap file-names posts) (map -render-rss-item))]]))

(defn render-rss [file-names posts]
  (->> (-render-rss file-names posts)
       (spit "static/rss.xml")))

(defn render-blog []
  (let [files (->> (file-seq (File. "posts"))
                   (filter #(.isFile ^File %)))
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

(render-blog)

(ns blog.utils)

(def months
  ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"])

(defn format-date [{:keys [date month year]}]
  (str (months (dec month)) " " date ", " year))

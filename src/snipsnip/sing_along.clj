;; start emacs with cider
;; open this file
;; press C-c M-j to connect


(ns snipsnip.sing-along
  (:require [net.cgrand.enlive-html :refer :all])) ;;<-- place your cursor here and press C-x e

(use 'clojure.repl)


;; can we find the verse-one node?

((snippet "snipsnip.html" [:#verse-one] [])) ;;place your cursor here, and press C-c C-p (pretty print the result.

;;This makes all the difference.

;; REALLY, start emacs and print C-c C-p after the expression above.
;; anyway. it looks like this:

({:tag :p,
    :attrs {:id "verse-one"},
    :content
    '("Conrad's mother said Conrad dear"
     {:tag :br, :attrs nil, :content ()}
     "\nI must go out and leave you here."
     {:tag :br, :attrs nil, :content ()}
     "\nbut mind now, Conrad, what I say,"
     {:tag :br, :attrs nil, :content ()}
     "\ndon't suck your thumbs while I'm away.")})

;; study the structure of the :content
;; it's a sequence '(I escaped it) of strings
;; and other nodes.


;; some basic enlive selections
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; all the links?

((snippet "snipsnip.html" [:a] []))

#_({:tag :a, :attrs {:href "https://www.tigerlillies.com/"}, :content ("The Tiger Lillies")}
   {:tag :a,
    :attrs {:href "https://www.youtube.com/watch?feature=player_detailpage&v=UmlIUvlaB_Y#t=193"},
    :content ("hillarious video")})

;; note that these are two individual nodes,
;; not having that much to do with each other

;; now let's select content strings using the enlive function
;; the text-pred, which takes a string matching predicate
;; matching predicate and returns an enlive state-machine for this

;; Conrad:
;; first and foremost, what happends to dear Conrad? Look for all 
;; strings which contains Conrad
((snippet "snipsnip.html" [(text-pred #(re-find #"Conrad" %))] []))

#_("Conrad's mother said Conrad dear"
   "\nbut mind now, Conrad, what I say,"
   "\nand Conrad cries out oh!"
   "\nand Conrad's thumbs will be off at last."
   "\nand Conrad cries out oh!"
   "\nand Conrad's thumbs are off at last."
   "well, mama comes home and there Conrad stands,"
   " and Conrad breathes his last."
   " and Conrad bleeds to death at last.\n")

;; It doesn't look very good for Conrad.

;; There's a row  <p>snip! snip</p> in the file, can we find this whole node?

;; this finds the content of the node:
((snippet "snipsnip.html" [(text-pred #(= "snip! snip" %)) ] []))

#_("snip! snip")

;;we can easily find all :p nodes in the document

(count ((snippet "snipsnip.html" [:p] [])))
;; there are 9 nodes in the document

;;we can narrow the search by specifiying more attributes
(count ((snippet "snipsnip.html" [:p.refrain] [])))
;; 2 refrains

;; and btw the :p.refrain is sugar for

((snippet "snipsnip.html" [[:p (has-class "refrain")]] []))

;;where the inner vector is sugar for "and"

;; but now we want to select the outer node (:p) based on things
;; inside the node

;;there's an enlive state-machine predicate

has

;;which takes another selector [...]

;; so we get to something like this:

((snippet "snipsnip.html"
          [[:p 
            (has 
             [(text-pred #(= "snip! snip" %))])]] []))

;; the expression above took some time to get right. There's a file syntax.html
;; in the enlive repo, but essentially the has-function expects a selector-vector

#_({:tag :p, :attrs nil, :content ("snip! snip")})

;; why wouldn't

(has (text-pred #(= "snip! snip" %)))

;;work?
;; well... because "has" builds up a state machine (togheter with [and] #{or} {:to :from}
;; and the selector (text-pred #(= "snip! snip" %)) expects just to be tested on the nodes
;; in the document (select uses this as a predicate)

;; now we want to select all the :p with a :br -tag in it

((snippet "snipsnip.html" [[:p (has [:br])]] []))

;; well, this was much clearer

;; hmm, what does really happen? Well, 

(html-resource "snipsnip.html")

;; ({:type :dtd, :data ["html" nil nil]}
;;  {:tag :html,
;;   :attrs nil,
;;   :content
;;   ({:tag :head,
;;     :attrs nil,
;;     :content
;;     ("\n"
;;      {:tag :meta, :attrs {:charset "utf-8"}, :content nil}
;;      "\n"
;;      {:tag :title, :attrs nil, :content ("Snip Snip")}
;;      "\n")}
;;    "\n"
;;    {:tag :body,
;;     :attrs nil,
;;     :content
;;     ("\n"
;;      {:tag :h1, :attrs nil, :content ("Snip Snip")}
;;      "\n"
;;      {:tag :p,
;;       :attrs nil,
;;       :content
;;       ("by "
;;        {:tag :a,
;;         :attrs {:href "https://www.tigerlillies.com/"},
;;         :content ("The Tiger Lillies")})}
;;      "\n\n"
;; ... 


;; a great, now let's restructure the text a little.
;; Let's say we want to remove the refrains

((snippet "snipsnip.html"
         [:html]  [] 

         ;;selector     ;transform
         [:p.refrain]   nil))

;; now we added a selector-transform pair
;; nil is probably the most boring transform.
;; no wait, identity is:

((snippet "snipsnip.html"
         [:html]  [] 

         ;;selector     ;transform
         [:p.refrain]   identity))

;;leaves the page entirely unaffected

;; let's say we want to remove all the content string containing Conrad,
;; but leaving all other nodes? 

((snippet "snipsnip.html"
         [:html]  [] 

         ;;selector     ;transform
         [:p.refrain]   identity))

;; this removes all content strings (~rows~ in this case)
((snippet "snipsnip.html" [:html] []
          [(text-pred #(re-find #"Conrad" %))] nil))


;;let's say we want to replace all the occurences of Conrad to something else
;; there's a function for that!

(doc replace-words)

net.cgrand.enlive-html/replace-words
;; ([words-to-replacements])
;;   Takes a map of words to replacement strings and replaces 
;;    all occurences. Does not recurse, you have to pair it with an appropriate
;;    selector.

((snippet "snipsnip.html" [:html] []
          [(text-pred #(re-find #"Conrad" %))] (replace-words {"Conrad" "Alexander"})))

;;wouldn't it be great to be able to choose the name of the poor person your self?


((snippet "snipsnip.html" [:html] [suck-a-thumb]
          [(text-pred #(re-find #"Conrad" %))]
          (replace-words {"Conrad" suck-a-thumb})) "Parsifal")

;;one part of it
#_{:tag :p,
      :attrs {:id "verse-one"},
      :content
      ["Parsifal's mother said Parsifal dear"
       {:tag :br, :attrs nil, :content []}
       "\nI must go out and leave you here."
       {:tag :br, :attrs nil, :content []}
       "\nbut mind now, Parsifal, what I say,"
       {:tag :br, :attrs nil, :content []}
       "\ndon't suck your thumbs while I'm away."]}

;; seems to work fine

;; but wait, why do we have to look into text-nodes containting Conrad?


;; for instance we can make sure that any string is going through the replace
;; transformation

((snippet "snipsnip.html" [:html] [suck-a-thumb]
          [(text-pred identity)]

          (replace-words {"Conrad" suck-a-thumb})) "Parsifal")


;; could we also create our own string?-predicate using pred?

;; this does not work:
((snippet "snipsnip.html" [:html] [suck-a-thumb]
          [(pred string?)]

          (replace-words {"Conrad" suck-a-thumb})) "Parsifal")

;; no, then.
;; the reason for this is that ordinary pred expects branch zip nodes, which strings are not.

;; to see the source put your cursor after 
pred

;; and press M-., cider now takes you to the source code of enlive 
;; you can pop back here with M-,







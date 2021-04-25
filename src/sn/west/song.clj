(ns sn.west.song
  (:require
    [sn.melody :refer :all]
    [leipzig.melody :refer :all]
    [leipzig.live :as live]
    [leipzig.live :refer [stop]]
    [leipzig.temperament :as temperament]
    [leipzig.scale :as scale]
    [leipzig.chord :as chord]
    [leipzig.canon :as canon]
    [sn.instruments :refer :all]))

(def progression
  (->> [0 -3 -2 -5]
       (map (partial chord/root chord/seventh))))

; Accompaniment
(def backing
  (let [lefts [true false true false]
        render-chord (fn [[left? chord]] (->> (phrase [4] [chord]) (all :left? left?)))]
    (->>
      progression
      (map vector lefts)
      (mapthen render-chord)
      (part ::accompaniment))))

(def theme
  (let [ill-run-away (->>
                       (after -1/2
                              (phrase
                                [1/2 1/4 1/4 1/2 3]
                                [  3   4   3   4 nil]))
                       (vary (partial but 1/4 1/2 (phrase [1/4] [6]))))     
        my-heart-will-go-west-with-the-sun (->> (phrase
                                                  [1/2 3/4 3/4 2/4 3/4 3/4 1/4 17/4]
                                                  [  3   4   3   2   4   3   2   -1])
                                                (after -1/2))]
    (->>
      ill-run-away
      (then my-heart-will-go-west-with-the-sun)
      (part ::lead))))

(def gymnopédie-one
  (->>
    (phrase (cycle [3/2 3/2 2/2]) [nil 4 6 5 4 1 0 1 2])
    (then (phrase (repeat 4) [-1 0 4 5 4]))
    (where :pitch scale/lower)
    (part ::epilogue)))

; Response
(def a-parting-kiss
  (phrase
    [1/4 1/4 1/4 3/4 10/4]
    [  4   3   4   6    4]))

(def like-fairy-floss
  (with (after -1/4 (phrase [1/4] [3])) a-parting-kiss))

(def dissolves-on-the-tip-of-my-tongue
  (->>
    (phrase
      [1/4 3/4 13/4]
      [  4   6    4])
    (after -1/4)))

(def reply
 (->>
   a-parting-kiss
   (then like-fairy-floss)
   (then (times 2 dissolves-on-the-tip-of-my-tongue))
   (part ::response)))

; Break
(def consider-this
  (after -3/2
     (phrase
       [1/2 1/2 1/2 8/2]
       [  4   9   8   7])))

(def consider-that
  (->> consider-this
       (but 0 1/2 (phrase [8/2] [6]))))

(def consider-everything
  (->> consider-this
       (but 0 8
            (phrase
              [2/2 1/2 2/2 2/2 9/2]
              [  7   8   7   6   4]))))

(def break
 (->>
   consider-this
   (then consider-that)
   (then consider-everything)
   (part ::break)))

; Bass
(def light-bass
  (->> progression
       (map :i)
       (phrase (repeat 4))
       (where :pitch scale/lower)
       (part ::bass)))

(def bassline
  (->> light-bass
       (canon/canon
         (comp (canon/simple 1)
               (canon/interval 6)
               (partial where :duration dec)
               (partial all :left? true)))))

(def back-beat
  (->> (phrase (repeat 4 1) (cycle [nil 0]))
       (times 4)
       (all :drum :pop)
       (part :beat)))

(def beat
  (->> (phrase [6/4 4/4 6/4] (repeat -14))
       (times 4)
       (with (times 2 (phrase [1 2 2 2 1/2 1/2] [nil -10 -10 -10 -10 -13])))
       (all :drum :kick)
       (part :beat)))

(def flat-beat
  (->> (phrase (repeat 4 1) (repeat -14))
       (times 4)
       (part :beat)
       (all :drum :kick)))

(def beat2
  (->> (phrase [1 1 1/4 3/4 1 1/4 1/4 1/2 1/2 1/4 1/4 1 1/2] (cycle [-7 -3]))
       (with (after 4 (phrase [3/2 3/2 1] [-8 -10 -12])))
       (times 2)
       (with beat)
       (part :beat)
       (all :drum :kick)))

(def west
  "I'll run away.
  I'll get away.
  But my heart will go west with the sun."
  (let [gym gymnopédie-one]
    (->>
      (with
;       backing
;       bassline
       light-bass
;       back-beat
;       beat
;       beat2
;       flat-beat
;       theme
;       reply
;       break
        )
        ;(times 2) (with gym)
      (where :pitch (comp temperament/equal scale/A scale/minor))
      (tempo (bpm 80)))))

(comment
  (do (stop) (->> west var live/jam))
  (->> west-with-the-sun live/play)
)

; Arrangement
(defmethod live/play-note ::bass
  [{freq :pitch left? :left?}]
  (let [[position low] (if left? [-1/3 0.3] [1/5 2])]
    (-> freq (groan :volume 0.5 :position position :wet 0.3 :low low :limit 3000))))

(defmethod live/play-note ::accompaniment
  [{freq :pitch left? :left?}]
  (-> freq (shudder :volume 1 :pan (if left? 1/2 -1/2) :wet 0.8 :limit 6000)))

(defmethod live/play-note ::lead
  [{freq :pitch}]
  (-> freq (sawish :pan -1/6 :vibrato 8/3 :wet 0.6 :volume 0.7)))

(defmethod live/play-note ::response
  [{freq :pitch seconds :duration}]
  (-> freq (organ seconds :vol 0.5 :pan -1/4 :wet 0.8))
  (-> freq (sing seconds :volume 2.0 :pan 1/4 :wet 0.9)))

(defmethod live/play-note ::epilogue
  [{freq :pitch seconds :duration}]
  (-> freq (corgan seconds :vol 0.4 :pan 1/2 :wet 0.5 :vibrato 80/60 :room 0.9)))

(defmethod live/play-note ::break
  [{freq :pitch}]
  (-> freq (/ 2) (bell :duration 7 :vol 0.5 :position -1/5 :wet 0.6))
  (-> freq (bell :duration 7 :vol 1.5 :position -1/6 :wet 0.6)))

(def percussion
  {:kick (fn [freq] (kick2 freq :amp 0.8 :sustain 1.2))
   :pop (fn [freq] (tip freq :volume 1.0))})

(defmethod live/play-note :beat
  [{freq :pitch drum :drum}]
  ((drum percussion) freq))

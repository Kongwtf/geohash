(ns geohash.core
  "Geohashing functions for Clojure."
  (:refer-clojure :exclude (interleave))
  (:require [clojure.set :as set]))

(def ^:private alphabet (vec "0123456789bcdefghjkmnpqrstuvwxyz"))

(def ^:private decode-char
  "Decode a character in the alphabet to a number between 0 and 31."
  (into {} (for [i (range (count alphabet))]
             [(alphabet i) i])))

(def ^:private encode-char (set/map-invert decode-char))

(defn- hash-to-long
  "Convert a hash string to a long."
  [s]
  (reduce #(bit-or %2 (bit-shift-left %1 5)) (map decode-char s)))

(defn- deinterleave [n]
  (let [get-num #(reduce (fn [acc i]
                           (if (bit-test % i)
                             (bit-flip acc (/ (dec i) 2))
                             acc))
                         0
                         (range 1 64 2))]
    [(get-num n)
     (get-num (bit-shift-left n 1))]))

(defn- subdivide
  "Subdivide an interval mapped by n. Each bit of n is used to
  determine which side of the interval to return."
  [[lo hi] n bits]
  (let [[r-lo r-hi] (reduce (fn [[lo hi] b]
                              (let [med (/ (+ hi lo) 2)]
                                (if (bit-test n b)
                                  [med hi]
                                  [lo med])))
                            [lo hi]
                            (range (dec bits) -1 -1))]
    (+ r-lo (/ (- r-hi r-lo) 2))))

(defn- locate
  "Returns the encoded number for the smallest interval containing the
  location represented in b bits."
  [[lo hi] loc bits]
  (first
   (reduce (fn [[acc [lo hi]] b]
             (let [med (+ lo (/ (- hi lo) 2))]
               (if (<= loc med)
                 [acc [lo med]]
                 [(bit-flip acc b) [med hi]])))
           [0 [lo hi]]
           (range (dec bits) -1 -1))))

(defn- interleave
  "Interleave x and y into a single number, with the bits of x on the
  odd bits of the result."
  [x y]
  (reduce
   (fn [acc b]
     (let [acc (if (bit-test x b) (bit-flip acc (inc (* b 2))) acc)
           acc (if (bit-test y b) (bit-flip acc (* b 2)) acc)]
       acc))
   0
   (range 30)))

(defn- long-to-hash
  "Convert a number into a hash string of length size."
  [n size]
  (->> (range (dec size) -1 -1)
       (map #(* 5 %))
       (map #(bit-and 2r11111 (bit-shift-right n %)))
       (map encode-char)
       (apply str)))

(defn encode
  "Encode a location into a geohash. If size is not specified, the
  size will be 12."
  ([loc]
     (encode loc 12))
  ([[lat lng] size]
     (let [bits (long (/ (* 5 size) 2))
           lng-bits (if (odd? size) (inc bits) bits)
           lat-num (locate [-90.0 90.0] lat bits)
           lng-num (locate [-180.0 180.0] lng lng-bits)
           n (if (odd? size)
               (interleave lat-num lng-num)
               (interleave lng-num lat-num))]
       (long-to-hash n size))))

(defn decode
  "Decode a geohash into a location."
  [s]
  (let [s (take 12 s)
        [lat-num lng-num] (-> s (hash-to-long) (deinterleave))
        len (count s)
        lat-bits (long (/ (* 5 len) 2))
        lng-bits (if (odd? len) (inc lat-bits) lat-bits)
        lat (subdivide [-90.0 90.0] lat-num lat-bits)
        lng (subdivide [-180.0 180.0] lng-num lng-bits)]
    [lat lng]))

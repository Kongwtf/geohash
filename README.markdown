# geohash

Geohashing functions for Clojure.

See http://geohash.org and http://en.wikipedia.org/wiki/Geohash

This library can produce hashes up to 60 bits long (12 characters).

## Using geohash

If you use Leiningen, add the following dependency to your `project.clj`:

    [geohash "1.0.0"]

Then use it like so:
http://maps.google.com/maps?q=Truth+or+Consequences,+NM&hl=en&sll=37.0625,-95.677068&sspn=48.77566,92.724609&oq=truth+or&hnear=Truth+or+Consequences,+Sierra,+New+Mexico&t=m&z=12

    (require '[geohash.core :as geohash])

    (geohash/encode [37.0625 -95.677068])
    ;= "9yefyg40dwz2"

    (geohash/decode "9yefyg40dwz2")
    ;= [-47.838533939793706 74.12500010803342]

## License

geohash is licensed under the Eclipse Public License v1.0.

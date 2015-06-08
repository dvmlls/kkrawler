# krawlr

[![Build Status](https://travis-ci.org/dvmlls/krawlr.svg?branch=master)](https://travis-ci.org/dvmlls/krawlr)

Crawls a website and outputs a [DOT](http://en.wikipedia.org/wiki/DOT_%28graph_description_language%29) file which you can turn into an SVG digraph with GraphViz `dot` or `sfdp`. 

## Usage

For a small site:
```
$ sbt "run davna.nyc"
$ cat output.gv | dot -Tsvg > output.svg
```

For a larger site:
```
$ sbt "run www.digitalocean.com"
$ cat output.gv | sfdp -Tsvg > output.svg
```

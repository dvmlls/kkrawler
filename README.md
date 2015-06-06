# krawlr

Crawls a website and outputs a [dot](http://en.wikipedia.org/wiki/DOT_%28graph_description_language%29) file which you can turn into an SVG digraph with GraphViz. 

## Usage

For a small site:
```
$ sbt "run davna.nyc"
$ cat output.gv | dot -Tsvg > output.svg
```

For a larger site:
```
$ sbt "run davna.nyc"
$ cat output.gv | sfdp -Tsvg > output.svg
```

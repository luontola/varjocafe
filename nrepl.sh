#!/bin/sh
set -eu
lein clean
lein repl :headless :port 7888

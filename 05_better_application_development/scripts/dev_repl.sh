#! /bin/bash
lein clean
lein with-profile +dev,+dev-local repl

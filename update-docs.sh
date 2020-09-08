#!/bin/bash -x

set -e

BUILDDIR=axle-site-build
STAGEDIR=~/s3/axle-lang.org/

sbt -J-Xmx6G "project axle-docs" mdoc
sbt -J-Xmx6G "project axle-docs" makeSite

mkdir -p $BUILDDIR
cp axle-docs/src/site/Gemfile $BUILDDIR
cp axle-docs/src/site/_config.yml $BUILDDIR
cp axle-docs/src/site/favicon.ico $BUILDDIR
cp -R axle-docs/src/site/tutorial $BUILDDIR
cp -R axle-docs/target/site/* $BUILDDIR
mv *.svg *.png $BUILDDIR/tutorial/images/
cp -R axle-docs/src/site/css $BUILDDIR

JEKYLL_ENV=production jekyll build --source $BUILDDIR --destination $STAGEDIR

(cd $STAGEDIR; python -m SimpleHTTPServer 8000)

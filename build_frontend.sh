#!/bin/sh

sbt 'clean; fullLinkJS'
cd ui
yarn build

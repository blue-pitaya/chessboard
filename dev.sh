#!/bin/sh

export PROJECT_PATH=$(pwd)

RUN_CMD="cd $PROJECT_PATH; sbt '~fastLinkJS'" st & disown
sleep 1
cd "$PROJECT_PATH/ui"; yarn dev

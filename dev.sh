#!/bin/sh

export PROJECT_PATH=$(pwd)

RUN_CMD="cd $PROJECT_PATH; sbt '~fastLinkJS'" alacritty & disown
sleep 1
RUN_CMD="cd $PROJECT_PATH; sbt 'project api' 'run'" alacritty & disown
sleep 1
cd "$PROJECT_PATH/ui"; yarn dev

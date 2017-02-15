#!/usr/bin/env bash
aws s3 sync public/ s3://crossword-status-checker-prod --profile composer

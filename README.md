crossword-status-checker
========================

This is a Lambda function designed to make it easier to see what's gone wrong with a particular crossword. It checks S3 buckets, the crossword API, CAPI and Flexible content for a crossword.

This app has a frontend made up of static files hosted from S3: http://crossword-status-checker-prod.s3-website-eu-west-1.amazonaws.com. The app is only accessible from within kings place or via VPN.

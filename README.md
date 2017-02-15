crossword-status-checker
========================

This is a Lambda function designed to make it easier to see what's gone wrong with a particular crossword. 
It checks S3 buckets, the crossword API, CAPI and Flexible content for a crossword.

This app has a frontend made up of static files hosted from S3: 
http://crossword-status-checker-prod.s3-website-eu-west-1.amazonaws.com. The frontend is only accessible from within
 kings place or via VPN.
 
The lambda also has an endpoint which checks the next N days for crosswords which aren't ready to publish, and sends
 alerts via SNS if it finds any. There is currently a scheduled event set up (not in clouformation, via the console)
 to call this endpoint once a day.
 
Example API usage:
https://<cloudfront-url>/PROD/get-status?type=quiptic&id=896&api-key=<get from aws console>

Finally, there is also a function which will check a specific date for crosswords due to be published then, and list them
this hasn't been wired up to API gateway yet, so the best way to try it is to run the function locally or send a test
event in the AWS console.


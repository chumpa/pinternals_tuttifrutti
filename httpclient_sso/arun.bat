@set A=lib/commons-codec-1.6.jar;lib/commons-logging-1.1.3.jar;lib/fluent-hc-4.3.1.jar;lib/httpclient-4.3.1.jar;lib/httpclient-cache-4.3.1.jar;lib/httpcore-4.3.jar;lib/httpmime-4.3.1.jar 
@set B=lib/commons-codec-1.6.jar;lib/commons-logging-1.1.3.jar;lib/httpclient-4.3.1.jar;lib/httpclient-cache-4.3.1.jar;lib/httpcore-4.3.jar
@java -cp ./bin;%A% ApacheTest http://sapsrv01:50600 PISUPERDUPER PASS
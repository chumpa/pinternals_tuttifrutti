@set L=lib
@set P=%L%/com.sap.aii.ibclient_2.0.0.140716065214.jar;%L%/com.sap.aii.util_2.0.0.140716065214.jar;%L%/sap.com~tc~bl~guidgenerator~impl.jar;%L%/sap.com~tc~exception~impl.jar;%L%/sap.com~tc~logging~java~impl.jar
@java -cp %P%;bin com.pinternals.testmap.Main "http://hostname:50000/rep" Username Password

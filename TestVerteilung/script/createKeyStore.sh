#!/bin/sh
KEYSTORE=certdb/keystore.jks
keytool -genkey -alias applet -keystore $KEYSTORE -storepass Rethymon -keypass Rethymon -validity 3600 -keyalg RSA -keysize 2048 -dname "CN=Klaus Schulte, OU=Schulte 3, O=Schulte.org, L=NRW, ST=Germany, C=DE"
keytool -selfcert -alias applet -keystore $KEYSTORE -storepass Rethymon -keypass Rethymon
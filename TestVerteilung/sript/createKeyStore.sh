#!/bin/sh
KEYSTORE=certdb/keystore.jks
keytool -genkey -alias applet -keystore $KEYSTORE -storepass Rethymon -keypass Rethymon -dname "CN=Klaus Schulte, OU=Schulte 3, O=Schulte.org, L=NRW, ST=Germany, C=DE"
keytool -selfcert -alias applet -keystore $KEYSTORE -storepass Rethymon -keypass Rethymon
#!/bin/sh
sleep 60
echo "------------ START create Secret -------------"
aws secretsmanager create-secret --endpoint-url http://localhost:4566 --name MySecret2 --secret-string file://mysecret.json
sleep 5
echo "------------ DONE -----------"

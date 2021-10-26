# OneOnOne application

Facilitates 1on1 discussions between two employees.

# How to start the app?

## Using bootRun and an already started mongodb

* Make sure you have java 11 or newer on your PATH
* `export MONGODB_CONNECTION_STRING="a valid mongodb connection string"`
* `./gradlew bootRun`

## Using docker-compose

* Make sure you have docker installed
* ./gradlew build
* docker-compose up

# Notes

* a 1on1 belongs to an organizer and an attendee
* both of them must exist
* both of them can see the 1on1
* only the organizer is permitted to edit or delete it
* there's a builtin 'admin' user who can see and edit any of them

# Example queries

* curl -H "Content-type: application/json" -X PUT localhost:8080/users/add -d '{"name":"jancsi"}'
* curl -H "Content-type: application/json" -X PUT localhost:8080/users/add -d '{"name":"juliska"}'
* curl -H "Content-type: application/json" -H "X-AUTHENTICATED-USER: jancsi"  -X POST localhost:8080/1on1/add -d '{"title":"approval","organizer":{"name":"jancsi"}, "attendee":{"name":"juliska"}, "due":"2022-01-01", "location":"home", "open":true}'
* curl -H "X-AUTHENTICATED-USER: juliska" localhost:8080/1on1/all


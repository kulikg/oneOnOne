# OneOnOne application

Facilitates 1on1 discussions between two employees.

# How to start the app?

## Using bootRun and an already started mongodb

1) Make sure you have java 11 or newer on your PATH
1) export MONGODB_CONNECTION_STRING=<a valid mongodb connection string>
1) ./gradlew bootRun

## Using docker-compose

1) Make sure you have docker installed
1) ./gradlew build
1) docker-compose up

# Notes

1) a 1on1 belongs to an organizer and an attendee
1) both of them must exist
1) both of them can see the 1on1
1) only the organizer is permitted to edit or delete it
1) there's a builtin 'admin' user who can see and edit any of them

# Example queries

1) curl -H "Content-type: application/json" -X PUT localhost:8080/users/add -d '{"name":"jancsi"}'
1) curl -H "Content-type: application/json" -X PUT localhost:8080/users/add -d '{"name":"juliska"}'
1) curl -H "Content-type: application/json" -H "X-AUTHENTICATED-USER: jancsi"  -X POST localhost:8080/1on1/add -d '{"title":"approval","organizer":{"name":"jancsi"}, "attendee":{"name":"juliska"}, "due":"2022-01-01", "location":"home", "open":true}'
1) curl -H "X-AUTHENTICATED-USER: juliska" localhost:8080/1on1/all


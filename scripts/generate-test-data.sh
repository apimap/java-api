#!/bin/bash

#
# Add localhost test data
#

#
# Create Test1
#
token="$(curl --header "Content-Type: application/json" \
	--request POST\
	--data '{"codeRepository": "git://Test1", "name":"Test1"}' \
	"http://localhost:8080/api" 2> /dev/null | jq -cr .data.meta.token )"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"version": "1"}' \
	"http://localhost:8080/api/Test1/version"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{
  "data": {
    "name": "Test1",
    "description": "Apimap.io",
    "visibility": "Public",
    "api version": "1",
    "release status": "In Production",
    "interface specification": "JSON:API v1.1",
    "interface description language": "OpenAPI Specification",
    "architecture layer": "Backend",
    "business unit": "My Unit",
    "system identifier": "S07350",
    "documentation": ["http://readme.md"]
  },
  "api catalog version": "1"
}' \
	"http://localhost:8080/api/Test1/version/1/metadata"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data": [{"urn": "urn:apimap:35", "taxonomyVersion": "1"}]}' \
	"http://localhost:8080/api/Test1/version/1/classification"

#
# Create Test2
#
token="$(curl --header "Content-Type: application/json" \
	--request POST\
	--data '{"codeRepository": "git://Test2", "name":"Test2"}' \
	"http://localhost:8080/api" 2> /dev/null | jq -cr .data.meta.token )"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"version": "1"}' \
	"http://localhost:8080/api/Test2/version"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{
  "data": {
    "name": "Test2",
    "description": "Hello World 2",
    "visibility": "Internal",
    "api version": "1",
    "release status": "Design",
    "interface specification": "JSON:API v1.1",
    "interface description language": "OpenAPI Specification",
    "architecture layer": "Backend",
    "business unit": "Apimap.io",
    "system identifier": "S07350",
    "documentation": ["http://apimap.io"]
  },
  "api catalog version": "1"
}' \
	"http://localhost:8080/api/Test2/version/1/metadata"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data": [{"urn": "urn:apimap:1", "taxonomyVersion": "1"}]}' \
	"http://localhost:8080/api/Test2/version/1/classification"

#
# Create Test3
#
token="$(curl --header "Content-Type: application/json" \
	--request POST\
	--data '{"codeRepository": "git://Test3", "name":"Test3"}' \
	"http://localhost:8080/api" 2> /dev/null | jq -cr .data.meta.token )"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"version": "1"}' \
	"http://localhost:8080/api/Test3/version"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{
  "data": {
    "name": "Test3",
    "description": "Hello World 3",
    "visibility": "Public",
    "api version": "1",
    "release status": "In Production",
    "interface specification": "JSON:API v1.1",
    "interface description language": "OpenAPI Specification",
    "architecture layer": "Backend",
    "business unit": "Apimap.io",
    "system identifier": "S07350",
    "documentation": ["http://apimap.io"]
  },
  "api catalog version": "1"
}' \
	"http://localhost:8080/api/Test3/version/1/metadata"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data": [{"urn": "urn:apimap:2", "taxonomyVersion": "1"}]}' \
	"http://localhost:8080/api/Test3/version/1/classification"

#
# Create Test4
#
token="$(curl --header "Content-Type: application/json" \
	--request POST\
	--data '{"codeRepository": "git://Test4", "name":"Test4"}' \
	"http://localhost:8080/api" 2> /dev/null | jq -cr .data.meta.token )"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"version": "1"}' \
	"http://localhost:8080/api/Test4/version"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{
  "data": {
    "name": "Test4",
    "description": "Hello World 4",
    "visibility": "Public",
    "api version": "1",
    "release status": "Deprecated",
    "interface specification": "JSON:API v1.1",
    "interface description language": "OpenAPI Specification",
    "architecture layer": "Backend",
    "business unit": "Apimap.io",
    "system identifier": "S07350",
    "documentation": ["http://apimap.io"]
  },
  "api catalog version": "1"
}' \
	"http://localhost:8080/api/Test4/version/1/metadata"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data": [{"urn": "urn:apimap:4", "taxonomyVersion": "1"}]}' \
	"http://localhost:8080/api/Test4/version/1/classification"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"version": "2"}' \
	"http://localhost:8080/api/Test4/version"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{
  "data": {
    "name": "Test4",
    "description": "Hello World 4 2",
    "visibility": "Public",
    "api version": "2",
    "release status": "Deprecated",
    "interface specification": "JSON:API v1.1",
    "interface description language": "OpenAPI Specification",
    "architecture layer": "Backend",
    "business unit": "Apimap.io",
    "system identifier": "S07350",
    "documentation": ["http://apimap.io"]
  },
  "api catalog version": "1"
}' \
	"http://localhost:8080/api/Test4/version/2/metadata"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data": [{"urn": "urn:apimap:4", "taxonomyVersion": "1"}]}' \
	"http://localhost:8080/api/Test4/version/2/classification"


#
# Create Test5
#
token="$(curl --header "Content-Type: application/json" \
	--request POST\
	--data '{"codeRepository": "git://Test5", "name":"Test5"}' \
	"http://localhost:8080/api" 2> /dev/null | jq -cr .data.meta.token )"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"version": "1"}' \
	"http://localhost:8080/api/Test5/version"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{
  "data": {
    "name": "Test5",
    "description": "Hello World 5",
    "visibility": "Public",
    "api version": "1",
    "release status": "In Production",
    "interface specification": "JSON:API v1.1",
    "interface description language": "OpenAPI Specification",
    "architecture layer": "Backend",
    "business unit": "Apimap.io",
    "system identifier": "S07350",
    "documentation": ["http://apimap.io"]
  },
  "api catalog version": "1"
}' \
	"http://localhost:8080/api/Test5/version/1/metadata"

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data": [{"urn": "urn:apimap:2", "taxonomyVersion": "1"}]}' \
	"http://localhost:8080/api/Test5/version/1/classification"


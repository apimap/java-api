#!/bin/bash

#
# Add localhost test data
#

#
# Create Taxonomy
#
taxonomyToken="$(curl --header "Content-Type: application/json" \
	--request POST\
	--data '{"data":{"attributes":{"name": "My Taxonomy", "nid":"apimap", "description": "My First Taxonomy"}}}' \
	"http://localhost:8080/taxonomy" 2> /dev/null | jq -cr .data.meta.token )"

echo ${taxonomyToken} > taxonomy.token

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${taxonomyToken}" \
  --request POST  \
  --data '{"data":{"attributes":{"version": "1", "nid": "apimap"}}}' \
  "http://localhost:8080/taxonomy/apimap/version" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${taxonomyToken}" \
  --request POST \
  --data '{"data":{"attributes":{"title":"First Category","urn":"urn:apimap:1","url":"taxonomy://First Category","description":"This is the first category", "type": "classification"}}}' \
  "http://localhost:8080/taxonomy/apimap/version/1/urn" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${taxonomyToken}" \
  --request POST \
  --data '{"data":{"attributes":{"title":"Second Category","urn":"urn:apimap:2","url":"taxonomy://Second Category","description":"This is the second category", "type": "classification"}}}' \
  "http://localhost:8080/taxonomy/apimap/version/1/urn" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${taxonomyToken}" \
  --request POST \
  --data '{"data":{"attributes":{"title":"First Category First Option","urn":"urn:apimap:3","url":"taxonomy://First Category/First Option","description":"This is the first category first option", "type": "classification"}}}' \
  "http://localhost:8080/taxonomy/apimap/version/1/urn" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${taxonomyToken}" \
  --request POST \
  --data '{"data":{"attributes":{"title":"First Category Second Option","urn":"urn:apimap:4","url":"taxonomy://First Category/Second Option","description":"This is the first category second option", "type": "classification"}}}' \
  "http://localhost:8080/taxonomy/apimap/version/1/urn" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${taxonomyToken}" \
  --request POST \
  --data '{"data":{"attributes":{"title":"Second Category First Option","urn":"urn:apimap:5","url":"taxonomy://Second Category/First Option","description":"This is the second category second option", "type": "classification"}}}' \
  "http://localhost:8080/taxonomy/apimap/version/1/urn" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${taxonomyToken}" \
  --request POST \
  --data '{"data":{"attributes":{"title":"Second Category Second Option","urn":"urn:apimap:6","url":"taxonomy://Second Category/Second Option","description":"This is the second category second option", "type": "classification"}}}' \
  "http://localhost:8080/taxonomy/apimap/version/1/urn" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${taxonomyToken}" \
  --request POST \
  --data '{"data":{"attributes":{"title":"Reference","urn":"urn:apimap:7","url":"taxonomy://First Category","description":"Reference", "type": "reference"}}}' \
  "http://localhost:8080/taxonomy/apimap/version/1/urn" 2> /dev/null | jq

#
# Create Test1
#
token="$(curl --header "Content-Type: application/json" \
	--request POST\
	--data '{"data":{"attributes":{"codeRepository": "git://Test1", "name":"Test1"}}}' \
	"http://localhost:8080/api" 2> /dev/null | jq -cr .data.meta.token )"

echo ${token} > test1.token

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":{"attributes":{"version": "1"}}}' \
	"http://localhost:8080/api/Test1/version" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":{"attributes":{
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
  }}}' \
	"http://localhost:8080/api/Test1/version/1/metadata" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":[{"attributes": {"urn": "urn:apimap:35", "taxonomyVersion": "1"}}]}' \
	"http://localhost:8080/api/Test1/version/1/classification" 2> /dev/null | jq

#
# Create Test2
#
token="$(curl --header "Content-Type: application/json" \
	--request POST\
	--data '{"data":{"attributes":{"codeRepository": "git://Test2", "name":"Test2"}}}' \
	"http://localhost:8080/api" 2> /dev/null | jq -cr .data.meta.token )"

echo ${token} > test2.token

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":{"attributes":{"version": "1"}}}' \
	"http://localhost:8080/api/Test2/version" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":{"attributes":{
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
  }}}' \
	"http://localhost:8080/api/Test2/version/1/metadata" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
  --data '{"data":[{"attributes": {"urn": "urn:apimap:1", "taxonomyVersion": "1"}}]}' \
	"http://localhost:8080/api/Test2/version/1/classification" 2> /dev/null | jq

#
# Create Test3
#
token="$(curl --header "Content-Type: application/json" \
	--request POST\
	--data '{"data":{"attributes":{"codeRepository": "git://Test3", "name":"Test3"}}}' \
	"http://localhost:8080/api" 2> /dev/null | jq -cr .data.meta.token )"

echo ${token} > test3.token

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":{"attributes":{"version": "1"}}}' \
	"http://localhost:8080/api/Test3/version" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":{"attributes":{
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
  }}}' \
	"http://localhost:8080/api/Test3/version/1/metadata" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":[{"attributes": {"urn": "urn:apimap:2", "taxonomyVersion": "1"}}]}' \
	"http://localhost:8080/api/Test3/version/1/classification" 2> /dev/null | jq

#
# Create Test4
#
token="$(curl --header "Content-Type: application/json" \
	--request POST\
	--data '{"data":{"attributes":{"codeRepository": "git://Test4", "name":"Test4"}}}' \
	"http://localhost:8080/api" 2> /dev/null | jq -cr .data.meta.token )"

echo ${token} > test4.token

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":{"attributes":{"version": "1"}}}' \
	"http://localhost:8080/api/Test4/version" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":{"attributes":{
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
  }}}' \
	"http://localhost:8080/api/Test4/version/1/metadata" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":[{"attributes": {"urn": "urn:apimap:4", "taxonomyVersion": "1"}}, {"attributes": {"urn": "urn:apimap:5", "taxonomyVersion": "1"}}]}' \
	"http://localhost:8080/api/Test4/version/1/classification" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":{"attributes":{"version": "2"}}}' \
	"http://localhost:8080/api/Test4/version" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":{"attributes":{
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
  }}}' \
	"http://localhost:8080/api/Test4/version/2/metadata" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
  --data '{"data":[{"attributes": {"urn": "urn:apimap:4", "taxonomyVersion": "1"}}]}' \
	"http://localhost:8080/api/Test4/version/2/classification" 2> /dev/null | jq


#
# Create Test5
#
token="$(curl --header "Content-Type: application/json" \
	--request POST\
	--data '{"data":{"attributes":{"codeRepository": "git://Test5", "name":"Test5"}}}' \
	"http://localhost:8080/api" 2> /dev/null | jq -cr .data.meta.token )"

echo ${token} > test5.token

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":{"attributes":{"version": "1"}}}' \
	"http://localhost:8080/api/Test5/version" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
	--data '{"data":{"attributes":{
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
  }}}' \
	"http://localhost:8080/api/Test5/version/1/metadata" 2> /dev/null | jq

curl --header "Content-Type: application/json" \
  --header "Authorization: Bearer ${token}" \
	--request POST\
  --data '{"data":[{"attributes": {"urn": "urn:apimap:7", "taxonomyVersion": "1"}}]}' \
	"http://localhost:8080/api/Test5/version/1/classification" 2> /dev/null | jq


Apimap.io API
=====

🥳 **Happy Coding** 🥳

This section is targeted to developers that want to communicate with the API directly.

## Table of Contents
* [Introduction](#introduction)
* [Roles](#roles)

## Introduction

This document defines the recommended configuration when using Apimap with a MongoDB database.

## Roles

**Important:** Always specify the absolute minimum access to the backend user. It should never be able to drop tables or any other action that read, write and update content.

### 1) Create a database
```mongodb-json-query
use apimap
```

### 2) Create collections
```mongodb-json-query
db.createCollection("api")
db.createCollection("apiClassification")
db.createCollection("apiVersion")
db.createCollection("metadata")
db.createCollection("taxonomyCollection")
db.createCollection("taxonomyCollectionVersion")
db.createCollection("taxonomyCollectionVersionURN")
```

### 2.1) Create indexes
```mongodb-json-query
db.api.createIndex(
	{name: 1}
)

db.apiVersion.createIndex(
	{name: 1}
)

db.apiVersion.createIndex(
	{apiId: 1, version:1 }
)

db.apiVersion.createIndex(
	{apiId: 1, created:-1 }
)

db.apiClassification.createIndex(
	{taxonomyUrn: 1}
)

db.apiClassification.createIndex(
	{apiId: 1, apiVersion: 1}
)

db.apiClassification.createIndex(
	{apiId: 1, apiVersion: 1, taxonomyUrn: 1}
)

db.metadata.createIndex(
	{apiId: 1}
)

db.metadata.createIndex(
	{apiId: 1, apiVersion: 1}
)

db.taxonomyCollection.createIndex(
	{nid: 1}
)

db.taxonomyCollection.createIndex(
	{nid: 1, created: -1}
)

db.taxonomyCollectionVersion.createIndex(
	{nid: 1, version: 1}
)

db.taxonomyCollectionVersionURN.createIndex(
	{taxonomyVersion: 1}
)

db.taxonomyCollectionVersionURN.createIndex(
	{nid: 1, version: 1}
)
```

### 3) Create a role
```mongodb-json-query
db.createRole(
    {
        role: "apimapRole", 
        privileges: [
            { resource: { db: "apimap", collection: "api" }, actions: [ "insert", "remove", "update", "find" ] },
            { resource: { db: "apimap", collection: "apiClassification" }, actions: [ "insert", "remove", "update", "find" ] },
            { resource: { db: "apimap", collection: "apiVersion" }, actions: [ "insert", "remove", "update", "find" ] },
            { resource: { db: "apimap", collection: "metadata" }, actions: [ "insert", "remove", "update", "find" ] },
            { resource: { db: "apimap", collection: "taxonomyCollection" }, actions: [ "insert", "remove", "update", "find" ] },
            { resource: { db: "apimap", collection: "taxonomyCollectionVersion" }, actions: [ "insert", "remove", "update", "find" ] },
            { resource: { db: "apimap", collection: "taxonomyCollectionVersionURN" }, actions: [ "insert", "remove", "update", "find" ] }
        ],
        roles: []
    }
)
```

### 4) Create a user
```mongodb-json-query
db.createUser(
    {
        user: "apimapUser",
        pwd: passwordPrompt(),
        roles: [
          { role: "apimapRole", db: "apimap" }  
        ]
    }
)
```
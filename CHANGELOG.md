Changelog
===

### 04.08.2022
- Added automated versioning to the project
- Added prometheus
- Added issue and PR request templates
- Added rating support
- Added readme and changelog uploads

### 28.06.2022
- Updated api collection name query parsing
- Made CodeRepository optional again
- Made metadata filters case insensitive on mongodb

### 21.06.2022
- Moving to openjdk 18-slim-bullseye docker image

### 20.06.2022
- Added MongoDB support
- Code rewritten to be reactive

### 23.03.2022
- Added search functionality to classifications and api endpoints
- Simple sort order on returned classifications
- Added more generated test data
- Added metrics exposed in Spring endpoint /actuator. 
- Log usage as INFO messages. 
- Added API endpoint /statistics/apis-history to return information of API history (API name and created data).
- Updated dependencies

### 25.02.2022
- Updated REST library version to fix element type bug

### 7.2.2022
- Added linking between old / new taxonomy urns
- Added token to zip-file content dump
- Fixed urn matching bug

### 13.12.2021
- Fixed Log4j zero day

### 9.12.2021
- Simplified token auth check code

### 25.11.2021
- Fixed 500 bug due to missing configuration bean

### 1.11.2021
- First public release 🎉
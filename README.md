Apimap.io API
===

🎉 **Welcome** 🎉

This is the home of the Apimap.io project, a freestanding solution to keep track of all functionality a company
provides through an API. It is a push based system, connected with your build pipeline or manually updated using our CLI.

> **Application programming interface (API)**: Point of functional integration between two or more systems connected 
> through commonly known standards

**Why is this project useful?** Lost track of all the API functionality provided inside your organization? Don't want
to be tied to an API proxy or management solution? The Apimap.io project uploads, indexes and enables discoverability of all
your organizations APIs. We care about the source code, removing the limitation of where the API is hosted and how your
network is constructed.

## Table of Contents

* [Project Components](#project-components)
* [Run](#run)
* [Contributing](#contributing)

I want to know more of the technical details and implementation guides: [DEVELOPER.md](DEVELOPER.md)

## Project Components
___
This is a complete software solution consisting of a collection of freestanding components. Use only the components you 
find useful, create the rest to custom fit your organization.

- A **Developer Portal** with wizards and implementation information
- A **Discovery Portal** to display APIs and filter search results
- An **API** to accommodate all the information
- A **Jenkins plugin** to automate information parsing and upload
- A **CLI** to enable manual information uploads

## Run
___

We primarily recommend the following two ways of running the applications:
- Locally using bootRun
- From our published Docker image

### Locally using bootRun

Based on Spring Boot, all the usual targets exist. The easiest way to get started is using **bootRun** 

> gradlew bootRun

#### From our published Docker image

It is possible to use the image "as-is", although this will NOT keep any content after shutdown/reboot.

> docker run -p 8080:8080 apimap/api

#### Requirements

Each node need a persistent storage volume to keep data between restarts. Default location is '/var/apimap'

```shell script
docker volume create apimap-data
```

#### Configuring the Docker Image

We love "build once deploy anywhere" and all configuration is done using the Spring configuration properties system.

You can find our default configuration in the application.yaml file, and we recommend that you override using environment variables.
More information about Spring and the configuration system is available at [docs.spring.io](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

Setting faq and support urls using **JSON Application Properties**
> docker run -p 8080:8080 --env SPRING_APPLICATION_JSON='{"apimap":{"metadata":{"copyright": "your organization", "faq":"your url", "support":"your url"}}}' apimap/api

## Contributing
___

Read [howto contribute](CONTRIBUTING.md) to this project.
FROM openjdk:18-alpine
MAINTAINER apimap.io
RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir /var/apimap && chown spring:spring /var/apimap
USER spring:spring
COPY build/dependency/BOOT-INF/lib /app/lib
COPY build/dependency/META-INF /app/META-INF
COPY build/dependency/BOOT-INF/classes /app
ENV SPRING_APPLICATION_JSON "{}"
EXPOSE 8080
ENTRYPOINT ["java","-cp","app:app/lib/*","io.apimap.api.Application"]
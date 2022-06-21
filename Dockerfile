FROM openjdk:18-slim-bullseye
MAINTAINER apimap.io
RUN adduser --uid 467 --system --group spring
RUN mkdir /var/apimap && chown spring:spring /var/apimap
USER spring:spring
COPY build/dependency/BOOT-INF/lib /app/lib
COPY build/dependency/META-INF /app/META-INF
COPY build/dependency/BOOT-INF/classes /app
ENV SPRING_APPLICATION_JSON "{}"
EXPOSE 8080
ENTRYPOINT ["java","-cp","app:app/lib/*","io.apimap.api.Application"]
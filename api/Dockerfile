FROM eclipse-temurin:25-jre-alpine

RUN apk --no-cache add curl tzdata \
    && addgroup -S -g 1000 app \
    && adduser -S -D -H -u 1000 -G app -s /sbin/nologin app \
    && mkdir -p /app/profile-pictures \
    && chown 1000:1000 /app/profile-pictures \
    && chmod 0755 /app/profile-pictures \
    && chmod 0555 /app

WORKDIR /app
COPY --chown=0:0 --chmod=0444 ./build/libs/*.jar /app/api.jar

VOLUME /app/profile-pictures
ENV STORAGE_PROFILEPICTURE_FOLDERPATH=/app/profile-pictures \
    TZ=UTC \
    HOME=/tmp \
    JAVA_TOOL_OPTIONS="-Duser.timezone=UTC -Doracle.jdbc.timezoneAsRegion=false"

USER 1000:1000

HEALTHCHECK --interval=1m --timeout=5s --start-period=45s --retries=3 \
    CMD curl --fail --silent --show-error --max-time 3 http://127.0.0.1:8081/actuator/health || exit 1

EXPOSE 8080
STOPSIGNAL SIGTERM
ENTRYPOINT ["java", "-jar", "/app/api.jar"]

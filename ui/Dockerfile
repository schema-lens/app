FROM nginxinc/nginx-unprivileged:1.31.7-alpine-slim

USER root

COPY --chown=0:0 --chmod=0444 ./nginx-main.conf /etc/nginx/nginx.conf
COPY --chown=0:0 --chmod=0444 ./nginx.conf /etc/nginx/conf.d/default.conf
COPY --chown=0:0 ./dist/schema-lens/browser /usr/share/nginx/html
RUN find /usr/share/nginx/html -type d -exec chmod 0555 {} + \
    && find /usr/share/nginx/html -type f -exec chmod 0444 {} +

USER 101:101

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -q -T 3 -O /dev/null http://127.0.0.1:8080/ || exit 1

EXPOSE 8080
STOPSIGNAL SIGQUIT
ENTRYPOINT ["nginx", "-g", "daemon off;"]

FROM httpd:alpine

RUN rm -rf /usr/local/apache2/htdocs/*

COPY dist/frontend/browser/ /usr/local/apache2/htdocs/

RUN echo "FallbackResource /index.html" >> /usr/local/apache2/conf/httpd.conf
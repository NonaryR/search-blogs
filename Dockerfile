FROM clojure

ADD project.clj /opt/search_blogs/project.clj
WORKDIR /opt/search_blogs
RUN lein deps
ADD . /opt/search_blogs

EXPOSE 3000

CMD ["lein", "run"]

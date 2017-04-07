#!/bin/bash
cd $(dirname $0)
while ! curl -s http://es:9200 2>&1> /dev/null; do
    sleep 1
done
echo "==> building index"
curl -s -XPUT http://es:9200/page-views --data-binary @index.json
echo "==> bulk insert data"
curl -s -XPOST http://es:9200/_bulk --data-binary @data.json

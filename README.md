#ristory-search

# 环境
单节点环境(Centos7)，基于elasticsearch-7.0.0+elasticsearch-analysis-ik-7.0.0

```
!/bin/bash
author: ristory
#install es
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.0.0-x86_64.rpm
rpm -ivh elasticsearch-7.0.0-x86_64.rpm
systemctl status elasticsearch

#install ik
cd /usr/share/elasticsearch/plugins
mkdir -p ik && cd ik
wget https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.0.0/elasticsearch-analysis-ik-7.0.0.zip
unzip elasticsearch-analysis-ik-7.0.0.zip
systemctl restart elasticsearch
```

#ES配置
##1.jvm配置：/etc/elasticsearch/jvm.options
```
## JVM configuration

################################################################
## IMPORTANT: JVM heap size
################################################################
##
## You should always set the min and max JVM heap
## size to the same value. For example, to set
## the heap to 4 GB, set:
################################################################ Xms和Xmx设置成同样大小，避免频繁分配内存和反复GC，并建议强制锁定内存，关闭swap
## -Xms4g
## -Xmx4g
##
## See https://www.elastic.co/guide/en/elasticsearch/reference/current/heap-size.html
## for more information
##
################################################################

# Xms represents the initial size of total heap space
# Xmx represents the maximum size of total heap space

-Xms1g
-Xmx1g

################################################################
## Expert settings
################################################################
##
## All settings below this section are considered
## expert settings. Don't tamper with them unless
## you understand what you are doing
##
################################################################

## GC configuration
-XX:+UseConcMarkSweepGC
-XX:CMSInitiatingOccupancyFraction=75
-XX:+UseCMSInitiatingOccupancyOnly

## G1GC Configuration
# NOTE: G1GC is only supported on JDK version 10 or later.
################################################################ JDK 10后可改用G1垃圾回收器
# To use G1GC uncomment the lines below.
# 10-:-XX:-UseConcMarkSweepGC
# 10-:-XX:-UseCMSInitiatingOccupancyOnly
# 10-:-XX:+UseG1GC
# 10-:-XX:InitiatingHeapOccupancyPercent=75

## DNS cache policy
# cache ttl in seconds for positive DNS lookups noting that this overrides the
# JDK security property networkaddress.cache.ttl; set to -1 to cache forever
-Des.networkaddress.cache.ttl=60
# cache ttl in seconds for negative DNS lookups noting that this overrides the
# JDK security property networkaddress.cache.negative ttl; set to -1 to cache
# forever
-Des.networkaddress.cache.negative.ttl=10

## optimizations

# pre-touch memory pages used by the JVM during initialization
-XX:+AlwaysPreTouch

## basic

# explicitly set the stack size
-Xss1m

# set to headless, just in case
-Djava.awt.headless=true

# ensure UTF-8 encoding by default (e.g. filenames)
-Dfile.encoding=UTF-8

# use our provided JNA always versus the system one
-Djna.nosys=true

# turn off a JDK optimization that throws away stack traces for common
# exceptions because stack traces are important for debugging
-XX:-OmitStackTraceInFastThrow

# flags to configure Netty
-Dio.netty.noUnsafe=true
-Dio.netty.noKeySetOptimization=true
-Dio.netty.recycler.maxCapacityPerThread=0

# log4j 2
################################################################ 使用log4j2输出ES日志，可以考虑集成到Sentry中，做ES健康实时告警
-Dlog4j.shutdownHookEnabled=false
-Dlog4j2.disable.jmx=true

-Djava.io.tmpdir=${ES_TMPDIR}

## heap dumps

# generate a heap dump when an allocation from the Java heap fails
# heap dumps are created in the working directory of the JVM
-XX:+HeapDumpOnOutOfMemoryError

# specify an alternative path for heap dumps; ensure the directory exists and
# has sufficient space
-XX:HeapDumpPath=/var/lib/elasticsearch

# specify an alternative path for JVM fatal error logs
-XX:ErrorFile=/var/log/elasticsearch/hs_err_pid%p.log

## JDK 8 GC logging

8:-XX:+PrintGCDetails
8:-XX:+PrintGCDateStamps
8:-XX:+PrintTenuringDistribution
8:-XX:+PrintGCApplicationStoppedTime
8:-Xloggc:/var/log/elasticsearch/gc.log
8:-XX:+UseGCLogFileRotation
8:-XX:NumberOfGCLogFiles=32
8:-XX:GCLogFileSize=64m

# JDK 9+ GC logging
9-:-Xlog:gc*,gc+age=trace,safepoint:file=/var/log/elasticsearch/gc.log:utctime,pid,tags:filecount=32,filesize=64m
# due to internationalization enhancements in JDK 9 Elasticsearch need to set the provider to COMPAT otherwise
# time/date parsing will break in an incompatible way for some date patterns and locals
9-:-Djava.locale.providers=COMPAT
```

##2. es配置 ： /etc/elasticsearch/elasticsearch.yml 
```
# ======================== Elasticsearch Configuration =========================
#
# NOTE: Elasticsearch comes with reasonable defaults for most settings.
#       Before you set out to tweak and tune the configuration, make sure you
#       understand what are you trying to accomplish and the consequences.
#
# The primary way of configuring a node is via this file. This template lists
# the most important settings you may want to configure for a production cluster.
#
# Please consult the documentation for further information on configuration options:
# https://www.elastic.co/guide/en/elasticsearch/reference/index.html
#
# ---------------------------------- Cluster -----------------------------------
#
# Use a descriptive name for your cluster:
################################################################ 集群配置
#cluster.name: my-application
#
# ------------------------------------ Node ------------------------------------
#
# Use a descriptive name for the node:
################################################################ 节点配置
#node.name: node-1
#
# Add custom attributes to the node:
#
#node.attr.rack: r1
#
# ----------------------------------- Paths ------------------------------------
#
# Path to directory where to store the data (separate multiple locations by comma):
################################################################ 数据路径
path.data: /var/lib/elasticsearch
#
# Path to log files:
################################################################ 日志路径
path.logs: /var/log/elasticsearch
#
# ----------------------------------- Memory -----------------------------------
#
# Lock the memory on startup:
################################################################ 常锁内存
#bootstrap.memory_lock: true
#
# Make sure that the heap size is set to about half the memory available
# on the system and that the owner of the process is allowed to use this
# limit.
#
# Elasticsearch performs poorly when the system is swapping the memory.
#
# ---------------------------------- Network -----------------------------------
#
# Set the bind address to a specific IP (IPv4 or IPv6):
################################################################ 绑定IP
network.host: 192.168.10.221
#
# Set a custom port for HTTP:
################################################################ HTTP服务端口
#http.port: 9200
#
# For more information, consult the network module documentation.
#
# --------------------------------- Discovery ----------------------------------
#
# Pass an initial list of hosts to perform discovery when this node is started:
# The default list of hosts is ["127.0.0.1", "[::1]"]
################################################################ 暴露IP
discovery.seed_hosts: [192.168.10.221]
#
# Bootstrap the cluster using an initial set of master-eligible nodes:
#
#cluster.initial_master_nodes: ["node-1", "node-2"]
#
# For more information, consult the discovery and cluster formation module documentation.
#
# ---------------------------------- Gateway -----------------------------------
#
# Block initial recovery after a full cluster restart until N nodes are started:
#
#gateway.recover_after_nodes: 3
#
# For more information, consult the gateway module documentation.
#
# ---------------------------------- Various -----------------------------------
#
# Require explicit names when deleting indices:
#
#action.destructive_requires_name: true
```

#例子
index、mapping、docs、query
```
[root@vm-221 ik]# curl -XPUT http://localhost:9200/index
{"acknowledged":true,"shards_acknowledged":true,"index":"index"}[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# curl -XPOST http://localhost:9200/index/_mapping -H 'Content-Type:application/json' -d'
> {
>         "properties": {
>             "content": {
>                 "type": "text",
>                 "analyzer": "ik_max_word",
>                 "search_analyzer": "ik_smart"
>             }
>         }
> 
> }'
{"acknowledged":true}[root@vm-221 ik]# curl -XPOST http://localhost:9200/index/_create/1 -H 'Content-Type:application/json' -d'
> {"content":"美国留给伊拉克的是个烂摊子吗"}
> '
{"_index":"index","_type":"_doc","_id":"1","_version":1,"result":"created","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":0,"_primary_term":1}[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# curl -XPOST http://localhost:9200/index/_create/2 -H 'Content-Type:application/json' -d'
> {"content":"公安部：各地校车将享最高路权"}
> '
{"_index":"index","_type":"_doc","_id":"2","_version":1,"result":"created","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":1,"_primary_term":1}[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# curl -XPOST http://localhost:9200/index/_create/3 -H 'Content-Type:application/json' -d'
> {"content":"中韩渔警冲突调查：韩警平均每天扣1艘中国渔船"}
> '
{"_index":"index","_type":"_doc","_id":"3","_version":1,"result":"created","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":2,"_primary_term":1}[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# curl -XPOST http://localhost:9200/index/_create/4 -H 'Content-Type:application/json' -d'
> {"content":"中国驻洛杉矶领事馆遭亚裔男子枪击 嫌犯已自首"}
> '
{"_index":"index","_type":"_doc","_id":"4","_version":1,"result":"created","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":3,"_primary_term":1}[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# 
[root@vm-221 ik]# curl -XPOST http://localhost:9200/index/_search  -H 'Content-Type:application/json' -d'
> {
>     "query" : { "match" : { "content" : "中国" }},
>     "highlight" : {
>         "pre_tags" : ["<tag1>", "<tag2>"],
>         "post_tags" : ["</tag1>", "</tag2>"],
>         "fields" : {
>             "content" : {}
>         }
>     }
> }
> '
{"took":803,"timed_out":false,"_shards":{"total":1,"successful":1,"skipped":0,"failed":0},"hits":{"total":{"value":2,"relation":"eq"},"max_score":0.6548752,"hits":[{"_index":"index","_type":"_doc","_id":"3","_score":0.6548752,"_source":
{"content":"中韩渔警冲突调查：韩警平均每天扣1艘中国渔船"}
,"highlight":{"content":["中韩渔警冲突调查：韩警平均每天扣1艘<tag1>中国</tag1>渔船"]}},{"_index":"index","_type":"_doc","_id":"4","_score":0.634845,"_source":
{"content":"中国驻洛杉矶领事馆遭亚裔男子枪击 嫌犯已自首"}
,"highlight":{"content":["<tag1>中国</tag1>驻洛杉矶领事馆遭亚裔男子枪击 嫌犯已自首"]}}]}}[root@vm-221 ik]# 
```
#IK配置
ik配置 ： /usr/share/elasticsearch/plugins/ik/config/IKAnalyzer.cfg.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
        <comment>IK Analyzer 扩展配置</comment>
        <!--本地扩展字典 -->
        <entry key="ext_dict">extra_1.dic;extra_2.dic</entry>
        <!--本地的扩展停止词字典-->
        <entry key="ext_stopwords">extra_3.dic</entry>
        <!--远程扩展字典 -->
        <entry key="remote_ext_dict">http://ikdict.panguyr.com/remote_ext_dict</entry>
        <!--远程扩展停止词字典-->
        <entry key="remote_ext_stopwords">http://ikdict.panguyr.com/remote_ext_stopwords</entry>
</properties>
```
#服务
git@git.panguyr.tech:pg-server/pg-search.git
```
curl -XPOST http://127.0.0.1:9999/pgsearch/post\?uRL\=_search -H 'Content-Type:application/json' -d'
{
 "query" : { "match" : { "content" : "自首" }}
}
'
{"headers":[{"name":"content-type","buffer":{"empty":false,"full":false},"valuePos":13,"value":"application/json; charset=UTF-8","elements":[{"name":"application/json","value":null,"parameters":[{"name":"charset","value":"UTF-8"}],"parameterCount":1}]},{"name":"content-length","buffer":{"empty":false,"full":false},"valuePos":15,"value":"316","elements":[{"name":"316","value":null,"parameters":[],"parameterCount":0}]}],"statusCode":200,"responseBody":"{\"took\":3,\"timed_out\":false,\"_shards\":{\"total\":1,\"successful\":1,\"skipped\":0,\"failed\":0},\"hits\":{\"total\":{\"value\":1,\"relation\":\"eq\"},\"max_score\":1.102704,\"hits\":[{\"_index\":\"index\",\"_type\":\"_doc\",\"_id\":\"4\",\"_score\":1.102704,\"_source\":\n{\"content\":\"中国驻洛杉矶领事馆遭亚裔男子枪击 嫌犯已自首\"}\n}]}}","errMEssage":null}%
```


#热更新
* [ ] 远程扩展字段、远程扩展停止词字典
* [ ] HTTP字典服务（ikdict）：url请求到此服务，返回的http-header需包含ETAG 和 Last-Modified，当任一值发生变化，IK插件触发重取词库动作，从而热更新词库
* [ ] 更新字典服务（refresh-ikdict）

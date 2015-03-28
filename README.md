Nagini
======
This project is used to help deploying and configuring distributed systems (applications) easily.  


General Steps/Content Index
---------------------------
 Below are the steps to deploy and undeploy distributed applications by using Nagini. Each point will be described in detail in next section.  


 1. Know about remote Nagini server folder structure, and config files.  
 1.1 Nagini Server Folder Structure  
 1.2 Nagini Config Files  


 2. Install Nagini  
 2.1  Download and compile Nagini jar  
 2.2  Configure Nagini properties and application cluster in nagini/config.  
 2.3  Install Nagini to remote hosts. This starts remote Nagini servers.  


 3. Deploy Configuration and Application  
 3.1  Deploy configuration files to remote servers.  
 3.2  Clean previous application binary (optional), and deploy new application binary to remote servers.  


 4. Run The Remote Application For Whatever Purposes   
 4.1  Start application instances and check status.  


 5. Clean-up  
 5.1  Clean application binary on remote hosts.  
 5.3  Terminate remote Nagini servers.  
 5.4  Remove remote Nagini folders.  


 Addendum.A Prepare Nagini Server Configurations  
 A.1  nagini.properties  
 A.2  host.list
 A.3  Application Config Files


Operational Details
-------------------

## 1. Know about remote Nagini server folder structure and config files.

### 1.1 Nagini Server Folder Structure (constructed when installing Nagini)

```
|-server.base.path
   |-config
       -nagini.properties            defines all Nagini server/client props, e.g. paths and jvm options
       -host.list                    declars Nagini host urls and corresponding application node ids
   |-nagini
      |-bin                          contains scripts to run classes
      |-dist                         contains Nagini executable
      |-lib                          contains Nagini referenced libraries
   |-application
      |-*                            contains application executable and library
   |-node_*
      |-config                       contains application node config
```

### 1.2 Nagini Config Files (need to modify before installation and testing)

```
|-client.base.path
   |-config
       -host.list                    contains host names and corresponding node ids
                                       required by bin/setup.sh, NaginiClient and NaginiServer
       -nagini.properties            defines all Nagini props for both server and client
                                       copied to all Nagini servers at deploy-config
      |-application                  contains all application config files
                                       copied to all Nagini servers and deployed to node config path at deploy-config
```

## 2. Install Nagini  

### 2.1  Download and compile Nagini jar  

```bash
cd ~
git clone git@github.com:cshaxu/nagini.git
cd nagini
ant dist
```


### 2.2  Configure Nagini and application in nagini/config.  

You need to configure files that is used to setup Nagini servers and application nodes, see the following sections:
```
A.1  nagini.properties
A.2  host.list
A.3  Application config files
```


The Nagini server and application nodes are defined in `host.list`. Everytime you run `bin/setup.sh` or `bin/nagini-client.sh`, you are running the operation against the whole Nagini cluster defined in `host.list`.    


### 2.3  Install Nagini to remote hosts. This starts remote Nagini server instances.  

```bash
bash bin/setup.sh install
```
setup.sh reads configurations from `nagini.properties` and `host.list`. It makes all directories on remote Nagini hosts, stops all running Nagini server instances, copies Nagini jars to remote Nagini hosts, and starts the remote Nagini servers. After installation, you can use `bin/nagini-client.sh` to operate the remote Nagini servers.


## 3. Deploy Configuration and Application.  

### 3.1  Deploy configuration files to remote servers.  

```bash
bash bin/nagini-client.sh deploy config
```
This command uploads the server config files (as described in section 2.2) to all remote Nagini servers. Then each Nagini server instance will forward the applicaiton configuration files in `config/application` to each application node path. Please allow a few seconds before you execute next Nagini command using `nagini-client.sh`, because this command requires reloading config on all Nagini servers.


### 3.2  Clean previous application executable (optional), and deploy new application distributable to remote servers.  

```bash
# stops application instances, and then removes application executable
# from all remote Nagini servers. this is optional.
bash bin/nagini-client.sh clean app
# stops application instances, downloads application from git repository,
# build application executable and upload the application executable to all Nagini servers.
bash bin/nagini-client.sh deploy app
```
Usually you only need to do deploy-app, unless you need to clean up the existing application executable.


## 4. Run The Remote Application For Whatever Purposes  

### 4.1  Start application instances and check status.  

```bash
# this command brings up all application instances on remote Nagini servers.
# the application startup command and options are defined in `nagini.properties`.
bash bin/nagini-client.sh start app
# allow 10 seconds for application instances to start.
sleep 10
# run this command to check if application instances are running (non-blocking)
bash bin/nagini-client.sh ping
# run this command to dump application instance output on screen (blocking)
bash bin/nagini-client.sh watch app
```


## 5. Clean-up  

### 5.1  Clean application distributable on remote Nagini hosts.  

```bash
bash bin/nagini-client.sh stop app
bash bin/nagini-client.sh clean app
```


### 5.2  Terminate remote Nagini servers.  

Unless necessary, you can keep Nagini server instances running.  
```bash
bash bin/nagini-client.sh control stop
```


### 5.3  Remove remote Nagini folders.  

Unless necessary, you can leave Nagini server folders for next startup.  
```bash
bash bin/setup.sh uninstall
```


## Addendum.A Prepare Nagini Configurations  

### A.1  nagini.properties  

```bash

# client properties
# jvm options to start Nagini client command-line tool on client side
client.jvm.options=-Xmx256M

# Nagini client base path on client side
client.base.path=/tmp/nagini-client

# Nagini client temp path on client side
client.temp.path=/tmp

# path of java executable for all client side java applications, i.e. nagini-client
client.java.exec=java

# client application properties
# git repository uri for downloading application distributbale
client.app.git.repo.uri=https://github.com/voldemort/voldemort.git

# git branch name of the application, master by default
client.app.git.repo.branch=master

# command to build the application binary in application base path
client.app.build.command=bash gradlew --stacktrace clean jar

# list of files/folders to be copied to server side application binary folder, all relative to application base path
client.app.build.output.rel.paths=contrib,dist,lib,src/java/log4j.properties

# server properties

# username to log on and start Nagini server instances on all remote hosts
server.user.name=nagini-dev

# jvm options to start Nagini server instance
server.jvm.options=-Xmx1G

# log file name to dump Nagini server instance output
server.logfile.name=nagini-server.log

# Nagini server base path on remote hosts
server.base.path=/tmp/nagini-server

# Nagini temp path on remote hosts
server.temp.path=/tmp

# port id used for Nagini server socket
server.port.id=6356

# path of java executable for all server side java applications
server.java.exec=java

# whether to enable server-application watch feature
server.watch.enabled=true

# server application properties
# java class path to start remote application instances, path relative to server.base.path/application/
server.app.java.class.rel.paths=dist,lib

# application startup command-line options, the wildcard # is to be replaced with node path.
server.app.java.class.options=#

# main class name of application
server.app.java.main.class=voldemort.server.VoldemortServer

# jvm options to start application instances on remote Nagini servers
server.app.jvm.options=-Xmx30G -server
```

This file is read by `bin/setup.sh` and `nagini.config.NaginiConfig`.  


### A.2  host.list  

The file `host.list` is simple a text file where each line contains a valid url in the Nagini cluster.  

The following file defines one Nagini server that hosts two application nodes [0, 1]:  
```
localhost,0,1
```

This file is read by `bin/setup.sh` and `nagini.config.NaginiConfig`.  


### A.3  Application Config Files

All the files in `client.base.path/config/application` will be coped to `server.base.path/config/application` at deploy-config, along with the Nagini config files. And then, these fills will be copied to `server.base.path/node_*/config`. All files will remain the same, except for those files with node id as suffix. These suffix-ed files will be renamed.  


For example, we have `server.properties.0` and `server.properties.1` in `client.base.path/config/application`. When we run `deploy-config`, `server.properties.0` will be copied to `server.base.path/node_0/config/server.properties`, while `server.properties.1` being copied to `server.base.path/node_1/config/server.properties`.  


--
Developed by Xu Ha, 2015.

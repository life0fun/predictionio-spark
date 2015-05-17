# Oryx 2 example

1. Install cdh on mac

  http://blog.cloudera.com/blog/2014/09/how-to-install-cdh-on-mac-osx-10-9-mavericks/
  
  http://www.cloudera.com/content/cloudera/en/documentation/core/latest/topics/cdh_vd_cdh_package_tarball.html


go to bin/ directory, Run the three Layers with:

    ./run.sh --layer-jar oryx-batch-2.0.0-SNAPSHOT.jar --conf example.conf

    ./run.sh --layer-jar oryx-speed-2.0.0-SNAPSHOT.jar --conf example.conf

    ./run.sh --layer-jar oryx-serving-2.0.0-SNAPSHOT.jar --conf example.conf
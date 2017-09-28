# local-cloud-sql

A local server, which uses a remote database within Google Cloud SQL.

### Preliminary configuration

##### Cloud Platform project

* [Select or create](https://console.cloud.google.com/cloud-resource-manager)
    a Cloud Platform project.
* [Enable](https://support.google.com/cloud/answer/6293499#enable-billing) billing for your project.

##### Cloud SQL

* [Enable](https://console.cloud.google.com/flows/enableapi?apiid=sqladmin) the Cloud SQL Administration API.
* [Create](https://console.cloud.google.com/sql/instances) 
a Cloud SQL instance.
* Create a SQL database on your Cloud SQL instance.
    * In the [Google Cloud Platform Console](https://console.cloud.google.com),
     click the Cloud Shell icon in the upper right corner.
     
        When the Cloud Shell finishes initializing, you should see:
      
        `Welcome to Cloud Shell! Type "help" to get started.
        username@example-id:~$`
      
    * At the Cloud Shell prompt, connect to your Cloud SQL instance:
      
        `gcloud sql connect myinstance --user=username`
    * Enter the password for the `username`.
      
        You should see the mysql prompt.
      
    * Create a SQL database on your Cloud SQL instance:
    
        `CREATE DATABASE dbName;`

##### Local configuration

* Install the latest [Google Cloud SDK](https://cloud.google.com/sdk/docs/#install_the_latest_cloud_tools_version_cloudsdk_current_version).

* Authorize `gcloud` to access Google Cloud Platform, e.g. using `gcloud auth login` command.
To successfully connect to a database, required either `Cloud SQL client` or `Editor` or `Owner` role.
The roles can be configured [here](https://console.cloud.google.com/iam-admin/iam/).

### Server starting

You can start the server from an IDE or using the command line:
 
 `gradlew :local-cloud-sql:runServer -Pconf=instance_connection_name,db_name,username,password`
 
##### Parameters

* `instance_connection_name` - the connection name for a Cloud SQL [instance](https://console.cloud.google.com/sql/instances).
* `db_name` - the name of the database, which belongs to the instance.
* `username` - the username, which belongs to the instance.
* `password` - the password for the username.
 
  

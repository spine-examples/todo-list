# compute-cloud-sql

A server for deploying to Google Compute Engine, which uses a remote database within Google Cloud SQL.

### Configure a Cloud Platform project

1. [Select or create](https://console.cloud.google.com/cloud-resource-manager)
    a Cloud Platform project.
1. [Enable](https://support.google.com/cloud/answer/6293499#enable-billing) billing for your project.
1. Enable the following roles for your Cloud Platform account (The roles can be configured [here](https://console.cloud.google.com/iam-admin/iam/)):
    * `Editor`, to deploy a Cloud Endpoints configuration and work with Cloud SQL.
    * `ComputeImageUser`, to create and run a Compute Engine instance.
1. Enable APIs:
    * [Endpoints API](https://console.cloud.google.com/endpoints).
    * [Compute Engine API](https://console.cloud.google.com/flows/enableapi?apiid=compute_component).
    * [Cloud SQL Administration API](https://console.cloud.google.com/flows/enableapi?apiid=sqladmin).

### Create a Cloud SQL instance

1. [Create](https://console.cloud.google.com/sql/instances) 
a Cloud SQL instance.
1. Create a SQL database on your Cloud SQL instance.
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

### Deploy a Cloud Endpoints configuration and Docker image for gRPC server

1. Install the latest [Google Cloud SDK](https://cloud.google.com/sdk/docs/#install_the_latest_cloud_tools_version_cloudsdk_current_version).

1. Authorize `gcloud` to access Google Cloud Platform, e.g. using `gcloud auth login` command.

1. Build the code:
    ```bash
    gradlew build
    ```

1. Go to the directory of `compute-cloud-sql` module (if you are in the root of `todo-list` project):
    ```bash
    cd ./deployment/compute-cloud-sql
    ```

1. Edit `api_config.yaml`. Replace `MY_PROJECT_ID` with your project id.

1. Edit `src/main/resources/cloud-sql.properties` according to the Cloud SQL instance configuration.

1. Deploy a service config to Service Management:

    ```bash
    gcloud service-management deploy build/descriptors/spine_grpc_services.pb api_config.yaml
    # The Config ID should be printed out, looks like: 2017-02-01r0, remember this
    ```
    
1. Build a docker image for gRPC server and store in your Container Registry:

    ```bash
    gcloud container builds submit --tag gcr.io/${GCLOUD_PROJECT_NAME}/todolist-gce:1.0 .
    ```

### Create a Compute Engine instance and connect a client

1. Create a Compute Engine instance and ssh in:

    ```bash
    # Creates a firewall rule to allow access through the port #80.
    gcloud compute firewall-rules create http-server --allow tcp:80
 
    gcloud compute instances create todolist-server \
       --image-family gci-stable \
       --image-project google-containers \
       --tags http-server \
       --scopes=default,sql-admin,storage-rw \
       --machine-type f1-micro \
       --zone europe-west1-b
    
    gcloud compute ssh todolist-server
    ```

1. Go the the popped up window. Set some variables to make commands easier:

    ```bash
    GCLOUD_PROJECT=$(curl -s "http://metadata.google.internal/computeMetadata/v1/project/project-id" -H "Metadata-Flavor: Google")
    SERVICE_NAME=todolist.endpoints.${GCLOUD_PROJECT}.cloud.goog
    SERVICE_CONFIG_ID=<Your Cloud Endpoints config ID>
    ```

1. Pull credentials to access Container Registry, and run the gRPC server container:

    ```bash
    /usr/share/google/dockercfg_update.sh
    docker run -d --name=todolist-gce gcr.io/${GCLOUD_PROJECT}/todolist-gce:1.0
    ```

1. Run the Endpoints proxy:

    ```bash
    docker run --detach --name=esp \
        -p 80:9000 \
        --link=todolist-gce:todolist-gce \
        gcr.io/endpoints-release/endpoints-runtime:1 \
        -s ${SERVICE_NAME} \
        -v ${SERVICE_CONFIG_ID} \
        -P 9000 \
        -a grpc://todolist-gce:50051
    ```

1. Back on the local machine, get the external IP of the GCE instance:

    ```bash
    gcloud compute instances list
    ```

1. Run the client:

    ```bash
    gradlew runTodoClient -Pconf=<IP of GCE Instance>,80
    ```

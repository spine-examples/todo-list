# compute-cloud-sql

A server for deploying to Google Compute Engine, which uses a remote database within Google Cloud SQL.

### Permissions
To configure a Google Cloud Platform project for this sample, the following roles are required:
* `Editor`, to deploy a Cloud Endpoints configuration.
* `ComputeImageUser`, to create and run a Compute Engine instance. 

### Start up process

1. Build the code:
    ```bash
    gradlew build
    ```

1. Go to the directory of `compute-cloud-sql` module (if you are in the root of `todo-list` project):
    ```bash
    cd ./deployment/compute-cloud-sql
    ```

1. Edit `api_config.yaml`. Replace `MY_PROJECT_ID` with your project id.

1. Deploy a service config to Service Management:

    ```bash
    gcloud service-management deploy build/descriptors/spine_grpc_services.pb api_config.yaml
    # The Config ID should be printed out, looks like: 2017-02-01r0, remember this
    ```
    
1. Build a docker image for gRPC server and store in your Container Registry:

    ```bash
    gcloud container builds submit --tag gcr.io/${GCLOUD_PROJECT_NAME}/todolist-gce:1.0 .
    ```

1. Create a Compute Engine instance and ssh in:

    ```bash
    # Creates a firewall rule to allow access through the port #80.
    gcloud compute firewall-rules create http-server --allow tcp:80
 
    gcloud compute instances create todolist-server \
       --image-family gci-stable \
       --image-project google-containers \
       --tags http-server \
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
    gradle runTodoClient -Pconf=<IP of GCE Instance>,80
    ```

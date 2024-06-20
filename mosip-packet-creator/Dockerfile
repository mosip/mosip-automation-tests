FROM mosipdev/openjdk-21-jre:latest

ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_group=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_uid=1001

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_gid=1001

# set working directory for the user
ENV work_dir=/home/${container_user}/

ARG KUBECTL_VERSION=1.22.9

# install packages and create user
RUN apt-get update \
    && apt install jq -y \
    && groupadd -g ${container_user_gid} ${container_user_group} \
    && useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/bash -m ${container_user} -d ${work_dir} \
    && curl -LO "https://storage.googleapis.com/kubernetes-release/release/v${KUBECTL_VERSION}/bin/linux/amd64/kubectl" \
    && chmod +x kubectl \
    && mv kubectl /usr/local/bin/

# select container user for all tasks
USER ${container_user_uid}:${container_user_gid}

COPY --chown=${container_user_uid}:${container_user} --chmod=771 ./entrypoint.sh ${work_dir}/entrypoint.sh
COPY --chown=${container_user}:${container_user} ./build_files/ ${work_dir}
COPY --chown=${container_user}:${container_user} ./profile_resource /tmp/profile_resource

# set environmental variables
ENV mountPath=/home//${container_user}/mountvolume
ENV authCertsPath=/home/${container_user}/authcerts

# set working directory
WORKDIR ${work_dir}

EXPOSE 8080

ENTRYPOINT ["/bin/bash", "-c", "./entrypoint.sh"]

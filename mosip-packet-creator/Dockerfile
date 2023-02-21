FROM openjdk:11-jre

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

# install packages and create user
RUN groupadd -g ${container_user_gid} ${container_user_group} \
    && useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/bash -m ${container_user} \
    && chown -R ${container_user}:${container_user} ${work_dir}

# set working directory
WORKDIR ${work_dir}

COPY ./build_files/ ./
ADD ./entrypoint.sh .
RUN chmod +x ./entrypoint.sh

EXPOSE 8080

# select container user for all tasks
USER ${container_user_uid}:${container_user_gid} ${work_dir}

ENTRYPOINT ["./entrypoint.sh"]

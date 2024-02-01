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
    && useradd -u ${container_user_uid} -g ${container_user_group} -s /bin/bash -m ${container_user} -d ${work_dir}

# select container user for all tasks
USER ${container_user_uid}:${container_user_gid}

COPY --chown=${container_user_uid}:${container_user} --chmod=771 ./entrypoint.sh ${work_dir}/entrypoint.sh
COPY --chown=${container_user}:${container_user} ./build_files/ ${work_dir}
COPY --chown=${container_user}:${container_user} ./scenarios /tmp/scenarios

# set working directory
WORKDIR ${work_dir}

EXPOSE 8081

ENV USER=
ENV ENDPOINT=
ENV TESTLEVEL=
ENV DEFAULT_SCENARIOS=true


ENV ENABLE_INSECURE=
ENV authDemoServiceBaseURL=
ENV authDemoServicePort=
ENV packetUtilityBaseUrl=
ENV mountPathForScenario=
ENV authCertsPath=/home/${container_user}/authcerts
ENV threadCount=
ENV enableDebug=
ENV langselect=
ENV push-reports-to-s3=
ENV s3-account=
ENTRYPOINT ["/bin/bash", "-c", "./entrypoint.sh"]

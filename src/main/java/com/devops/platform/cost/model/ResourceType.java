package com.devops.platform.cost.model;

/**
 * Types of cloud resources.
 */
public enum ResourceType {
    // Compute
    EC2_INSTANCE,
    VIRTUAL_MACHINE,
    CONTAINER,
    KUBERNETES_CLUSTER,
    LAMBDA_FUNCTION,

    // Storage
    S3_BUCKET,
    BLOB_STORAGE,
    EBS_VOLUME,
    FILE_STORAGE,

    // Database
    RDS_INSTANCE,
    DYNAMODB_TABLE,
    COSMOS_DB,
    CLOUD_SQL,
    REDIS_CACHE,

    // Networking
    LOAD_BALANCER,
    VPN_GATEWAY,
    NAT_GATEWAY,
    CDN,

    // Other
    API_GATEWAY,
    SQS_QUEUE,
    SNS_TOPIC,
    ELASTIC_IP,
    OTHER
}

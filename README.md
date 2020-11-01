# opa-java-spring-client


You may configure the PDP client component by setting the following properties in your application:

    pdp.port=8181
    pdp.hostname=localhost
    pdp.policy.path=/mypolicy
    pdp.readTimeout.milliseconds=5000
    pdp.connectionTimeout.milliseconds=5000
    pdp.retry.maxAttempts=2
    pdp.retry.backoff.milliseconds=250
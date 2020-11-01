# opa-java-spring-client

This repository contains a Spring component, for usage in Spring applications, for making policy evaluation requests
to PDPs (Policy Decision Point) which are compatible with the OPA (Open Policy Agent) API. 

## Configuration properties

You may configure the PDP client component by setting the following properties in your 
application.properties:

    pdp.port=8181
    pdp.hostname=localhost
    pdp.policy.path=/mypolicy
    pdp.readTimeout.milliseconds=5000
    pdp.connectionTimeout.milliseconds=5000
    pdp.retry.maxAttempts=2
    pdp.retry.backoff.milliseconds=250
    
1. ```pdp.port``` - the PDP port
1. ```pdp.hostname``` - the PDP address
1. ```pdp.policy.path``` - the path of the policy to evaluate
1. ```pdp.readTimeout.milliseconds``` - read timeout for requests in milliseconds 
1. ```pdp.connectionTimeout.milliseconds``` - connection timeout in milliseconds
1. ```pdp.retry.maxAttempts``` - the maximum number of retry attempts in case a failure occurs
1. ```pdp.retry.backoff.milliseconds``` - the number of milliseconds to backoff between retry attempts
    
## Example usage
 
 Example implementation in a Spring Controller

    @RequestMapping("/sdk")
    public String sdkExample(HttpServletRequest request) throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }

        String[] path = request.getRequestURI().replaceAll("^/|/$", "").split("/");

        //define the input for evaluation
        Map<String, Object> input = new HashMap<String, Object>();
        input.put("method", request.getMethod());
        input.put("path", path);
        input.put("headers", headers);
        input.put("group", "pod_developers");
        input.put("environment", "staging");
        input.put("role", "keti_read");

        JsonNode node = null;
        try {
            node = pdpClient.getJsonResponse(input);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return node.toPrettyString();
    }
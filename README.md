# opa-java-spring-client

This repository contains a Spring component, for usage in Spring applications, for making policy evaluation requests
to PDPs (Policy Decision Point) which are compatible with the OPA (Open Policy Agent) API. 

If you're not familiar with OPA, click [here](https://www.openpolicyagent.org/) to learn more.

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
   
 
<a name="example"></a>
## Example usage

PDP is registered as a spring interceptor

    @Configuration
    public class Configurer implements WebMvcConfigurer {

        @Autowired
        private PdpInterceptor pdpInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(pdpInterceptor);
        }
    }

Example implementation in a Spring Controller 

    // The Authorize annotation indicates that this request should be be authorized
    // using the PDP request interceptor. The resources supplied in the annotation will be
    // sent on the PDP request as well.
    @Authorize(resources = {"sdk.view"})
    @RequestMapping("/sdk")
    public String sdkExample(HttpServletRequest request) throws Exception {

        // ... Controller logic 
    }

Or instead use PDPClient directly to issue a request with your own input

    @RequestMapping("/sdk")
    public String sdkExample(HttpServletRequest request) throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }

        String[] path = request.getRequestURI().replaceAll("^/|/$", "").split("/");

        //define the input for evaluation
        //In your application, you can put anything you'd like on the input for policy evaluation
        Map<String, Object> input = new HashMap<String, Object>();
        input.put("group", "group1");
        input.put("environment", "staging");
        input.put("role", "admin");

        JsonNode node = null;
        try {
            node = pdpClient.getJsonResponse(input);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return node.toPrettyString();
    }
    
## Try it out

Run your PDP (OPA) instance (assuming it runs on localhost:8181).

### Manual Configuration

Set some data on your PDP:

    curl --location --request PUT 'http://localhost:8181/v1/data' \
    --header 'Content-Type: text/plain' \
    --data-raw '{ "internal": {
            "mapping" : {
                "group1" : {
                    "dev" : ["read", "write", "all"],
                    "int" : ["read", "write", "all"],
                    "staging" : ["read", "write", "all"],
                    "preprod" : ["read", "all"],
                    "prod" : ["read"]
                },
                "group2" : {
                    "dev" : ["read"],
                    "int" : ["read"],
                    "staging" : ["read", "write", "all"],
                    "preprod" : ["read", "write", "all"],
                    "prod" : ["read", "write", "all"]
                }    
            }
        }
    }'

Set your policy on the PDP, for example:

    curl --location --request PUT 'http://localhost:8181/v1/policies/mypolicy' \
    --header 'Content-Type: text/plain' \
    --data-raw 'package mypolicy
    
    default allow = false
    default deny = false
    
    allow {   
        requestedGroup := input.group
        env := input.environment
        requestedRole := input.role
    
        requestedRole == data.internal.mapping[requestedGroup][env][_]
    }
    
    roles_granted[roles] {
        requestedGroup := input.group
        env := input.environment
        requestedRole := input.role
        roles := data.internal.mapping[requestedGroup][env][_]
    }'

### Test the evaluation

Test that your PDP evaluates the policy properly:

    curl --location --request POST 'http://localhost:8181/v1/data/mypolicy' \
    --header 'Content-Type: text/plain' \
    --data-raw '{ 
        "input": {
            "group": "group1",
            "environment": "staging",
            "role": "read"
       }
    }'
    
You should see a response as follows:

    {
        "result": {
            "allow": true,
            "deny": false,
            "roles_granted": [
                "read",
                "write",
                "all"
            ]
        }
    }

Now use the PDPClient in your code, as shown in the [example above](#example), for doing similar queries from your application code.

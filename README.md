# opa-java-spring-client
## Abstract
opa-java-spring-client is a Spring middleware meant for authorizing API requests using a 3rd party policy engine (OPA) as the Policy Decision Point (PDP).
If you're not familiar with OPA, please [learn more](https://www.openpolicyagent.org/).

## Data Flow
![enter image description here](https://github.com/build-security/opa-express-middleware/blob/main/Data%20flow.png)

## Usage
### Prerequisites 
- Finish our "onboarding" tutorial
- Run a pdp instance

---
**Important note**

In the following example we used our aws managed pdp instance to ease your first setup, but if you feel comfortable you are recommended to use your own pdp instance instead.
In that case, don't forget to change the **hostname** and the **port** in your code.

---
### Simple usage

You may configure the PDP client component by setting the following properties in your 
application.properties:

    pdp.enable=true
    pdp.allowOnFailure=false
    pdp.port=8181
    pdp.hostname=localhost
    pdp.policy.path=/mypolicy
    pdp.readTimeout.milliseconds=5000
    pdp.connectionTimeout.milliseconds=5000
    pdp.retry.maxAttempts=2
    pdp.retry.backoff.milliseconds=250
   
 ### Mandatory configuration

 1. `hostname`: The hostname of the Policy Decision Point (PDP)
 2. `port`: The port at which the OPA service is running
 3. `policyPath`: Full path to the policy (including the rule) that decides whether requests should be authorized

  ### Optional configuration
 1. `allowOnFailure`: Boolean. "Fail open" mechanism to allow access to the API in case the policy engine is not reachable. **Default is false**.
 2. `includeBody`: Boolean. Whether or not to pass the request body to the policy engine. **Default is true**.
 3. `includeHeaders`: Boolean. Whether or not to pass the request headers to the policy engine. **Default is true**
 4. `timeout`: Boolean. Amount of time to wait before request is abandoned and request is declared as failed. **Default is 1000ms**.
 5. `enable`: Boolean. Whether or not to consult with the policy engine for the specific request. **Default is true**

## Example usage

PDP is registered as a spring interceptor
```java
    @Configuration
    public class Configurer implements WebMvcConfigurer {

        @Autowired
        private PdpInterceptor pdpInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(pdpInterceptor);
        }
    }
```

Example implementation in a Spring Controller 

```java
    // The Authorize annotation indicates that this request should be be authorized
    // using the PDP request interceptor. The resources supplied in the annotation will be
    // sent on the PDP request as well.
    @Authorize(resources = {"sdk.view"})
    @RequestMapping("/sdk")
    public String sdkExample(HttpServletRequest request) throws Exception {

        // ... Controller logic 
    }
```

Or instead use PDPClient directly to issue a request with your own input

```java
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
```
    
## Try it out

Run your PDP (OPA) instance (assuming it runs on localhost:8181).

### PDP Request example

This is what the input received by the PDP would look like.

```
{
    "input": {
        "request": {
            "method": "GET",
            "query": {
                "querykey": "queryvalue"
            },
            "path": "/some/path",
            "scheme": "http",
            "host": "localhost",
            "body": {
                "bodykey": "bodyvalue"
            },
            "headers": {
                "content-type": "application/json",
                "user-agent": "PostmanRuntime/7.26.5",
                "accept": "*/*",
                "cache-control": "no-cache",
                "host": "localhost:3000",
                "accept-encoding": "gzip, deflate, br",
                "connection": "keep-alive",
                "content-length": "24"
            }
        },
        "source": {
            "port": 63405,
            "address": "::1"
        },
        "destination": {
            "port": 3000,
            "address": "::1"
        },
        "resources": {
            "attributes": {
                "region": "israel",
                "userId": "buildsec"
            },
            "permissions": [
                "user.read"
            ]
        },
        "serviceId": 1
    }
}
```

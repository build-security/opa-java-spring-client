# opa-java-spring-client

<img src="Logo-build.png" class="center" alt="drawing" width="10%"/>

## Abstract
[build.security](https://docs.build.security/) provides simple development and management of the organization's authorization policy.
opa-java-spring-client is a Spring middleware intended for performing authorizing requests against build.security pdp/[OPA](https://www.openpolicyagent.org/).

## Data Flow
<img src="https://github.com/build-security/opa-express-middleware/blob/main/Data%20flow.png" alt="drawing" width="40%"/>

## Usage
Before you start we recommend completing the onboarding tutorial.

---
**Important note**

To simplify the setup process, the following example uses a local [build.security pdp instance](https://docs.build.security/policy-decision-points-pdp/pdp-deployments/standalone-docker-1).
If you are already familiar with how to run your PDP (Policy Decision Point), You can also run a pdp on you environment (Dev/Prod, etc).

In that case, don't forget to change the **hostname** and the **port** in your code.

---
### Simple usage

Configure the PDP client component by setting the following properties in your 
application.properties:

    pdp.enable=true
    pdp.allowOnFailure=false
    pdp.port=8181
    pdp.hostname=localhost
    pdp.policy.path=/javaSpring/authz
    pdp.readTimeout.milliseconds=5000
    pdp.connectionTimeout.milliseconds=5000
    pdp.retry.maxAttempts=2
    pdp.retry.backoff.milliseconds=250
   
 ### Mandatory configuration - 

 1. `pdp.hostname`: The hostname of the Policy Decision Point (PDP)
 2. `pdp.port`: The port at which the OPA service is running
 3. `pdp.policyPath.path`: Full path to the policy (including the rule) that decides whether requests should be authorized
 
 [How to get your pdp's hostname and port?](https://docs.build.security/policy-decision-points-pdp#pdp-instances-section)
  ### Optional configuration
 1. `pdp.allowOnFailure`: Boolean. "Fail open" mechanism to allow access to the API in case the policy engine is not reachable. **Default is false**.
 2. `pdp.retry.maxAttempts` - Integer. the maximum number of retry attempts in case a failure occurs. **Default is 2**.
 3. `pdp.enable`: Boolean. Whether or not to consult with the policy engine for the specific request. **Default is true**
 4. `pdp.readTimeout.milliseconds` - Integer. Read timeout for requests in milliseconds. **Default is 5000**
 5. `pdp.connectionTimeout.milliseconds` - Integer. Connection timeout in milliseconds. **Default is 5000**
 6. `pdp.retry.backoff.milliseconds` - Integer. The number of milliseconds to wait between two consecutive retry attempts. **Default is 250**
## Example usage

Register your PDP as a spring interceptor
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

Run your PDP (OPA) instance (assuming it runs on localhost:8181) and your spring server(localhost:8080).  
* Please make sure to [define some pdp policy rules](https://docs.build.security/policies/creating-a-new-policy).
### PDP Request example

This is what the input received by the PDP would look like.

```
{
   "input":{
      "request":{
         "scheme":"http",
         "method":"GET",
         "path":"websecurity",
         "query":{
            
         },
         "headers":{
            "host":"localhost:8080",
            "user-agent":"curl/7.64.1",
            "accept":"*/*"
         }
      },
      "resources":{
         "requirements":[
            "websecurity"
         ],
         "attributes":{
            
         }
      },
      "source":{
         "ipAddress":"172.19.0.1",
         "port":0
      },
      "destination":{
         "ipAddress":"172.19.0.2",
         "port":0
      }
   }
}
```

If everything works well you should receive the following response:
```
{
    "result": {
        "allow": true
    }
}
```
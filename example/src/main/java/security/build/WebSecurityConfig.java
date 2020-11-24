package security.build;

import org.openpolicyagent.voter.OPAVoter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${pdp.port:9000}")
    private String pdpPort;

    @Value("${pdp.hostname}")
    private String pdpHost;

    @Value("${pdp.schema:http}")
    private String pdpSchema;

    @Value("${pdp.policy.path}")
    private String pdpPolicyPath;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //configure the web security filter to go through the OPAVoter if accessing the /websecurity endpoint
        http.authorizeRequests().antMatchers("/websecurity").authenticated().accessDecisionManager(accessDecisionManager());
    }

    public String getPdpPort() {
        return pdpPort;
    }

    public String getPdpHost() {
        return pdpHost;
    }

    public String getPdpPolicyPath() {
        return pdpPolicyPath;
    }

    public String getPdpSchema() {
        return pdpSchema;
    }

    @Bean
    public AccessDecisionManager accessDecisionManager() {
        String url = getPdpSchema() + "://" + getPdpHost() + ":" + getPdpPort() + "/v1/data" + getPdpPolicyPath() + "/allow";

        List<AccessDecisionVoter<? extends Object>> decisionVoters = Arrays
                .asList(new OPAVoter(url));
        return new UnanimousBased(decisionVoters);
    }

}
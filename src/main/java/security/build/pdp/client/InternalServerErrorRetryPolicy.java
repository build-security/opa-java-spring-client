package security.build.pdp.client;

import org.springframework.classify.Classifier;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.web.client.HttpServerErrorException;

/**
 * This class implements a retry policy that takes affect only for errors
 * of type HttpServerErrorException. For any other error type, no retry is done.
 *
 * @see HttpServerErrorException
 */
public class InternalServerErrorRetryPolicy extends ExceptionClassifierRetryPolicy {

    public InternalServerErrorRetryPolicy(SimpleRetryPolicy simpleRetryPolicy) {

        this.setExceptionClassifier(new Classifier<Throwable, RetryPolicy>() {
            @Override
            public RetryPolicy classify(Throwable classifiable) {
                if (classifiable instanceof HttpServerErrorException) {
                    // use the given retry policy only for 5xx errors
                    return simpleRetryPolicy;
                }
                //if this is not a 5xx error then do not retry
                return new NeverRetryPolicy();
            }
        });
    }
}

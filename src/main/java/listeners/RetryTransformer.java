package listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Gan {@link RetryAnalyzer} cho TAT CA @Test (khong phai sua tung method). Dang ky bang
 * &lt;listener class-name="listeners.RetryTransformer"/&gt; trong suite XML.
 */
public class RetryTransformer implements IAnnotationTransformer {
    @Override
    @SuppressWarnings("rawtypes")
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}

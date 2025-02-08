package cr;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * @author Freeman
 */
public class ClasspathExtension implements InvocationInterceptor {

    @Override
    public void interceptBeforeAllMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        intercept(invocation, extensionContext);
    }

    @Override
    public void interceptBeforeEachMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        intercept(invocation, extensionContext);
    }

    @Override
    public void interceptAfterEachMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        intercept(invocation, extensionContext);
    }

    @Override
    public void interceptAfterAllMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        intercept(invocation, extensionContext);
    }

    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext)
            throws Throwable {
        if (isModifiedClassPathClassLoader(extensionContext)) {
            invocation.proceed();
            return;
        }
        invocation.skip();
        runTestWithModifiedClassPath(invocationContext, extensionContext);
    }

    private static void runTestWithModifiedClassPath(
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        Class<?> testClass = extensionContext.getRequiredTestClass();
        Method testMethod = invocationContext.getExecutable();
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        // We only cache Class -> ClassLoader, because the ClassLoader may be different for different test methods
        ModifiedClassPathClassLoader customizedClassLoader = Optional.ofNullable(
                        testMethod.getAnnotation(Classpath.class))
                .map(cr -> buildModifiedClassLoader(cr, originalClassLoader))
                .orElseGet(() -> getCachedClassLevelClassLoader(testClass, originalClassLoader));

        Thread.currentThread().setContextClassLoader(customizedClassLoader);
        try {
            runTest(testClass.getName(), testMethod.getName(), customizedClassLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    private static ModifiedClassPathClassLoader getCachedClassLevelClassLoader(
            Class<?> testClass, ClassLoader originalClassLoader) {
        return ModifiedClassLoaderCache.getOrPut(testClass, () -> {
            Classpath cr = testClass.getAnnotation(Classpath.class);
            return buildModifiedClassLoader(cr, originalClassLoader);
        });
    }

    private static ModifiedClassPathClassLoader buildModifiedClassLoader(
            Classpath cr, ClassLoader originalClassLoader) {
        assert cr != null;
        ModifiedClassPathClassLoaderBuilder generator = ModifiedClassPathClassLoader.builder(originalClassLoader)
                .add(cr.add())
                .exclude(cr.exclude())
                .classpathReplacer(cr);
        return generator.build();
    }

    private static void runTest(String testClassName, String testMethodName, ClassLoader custmizedClassLoader)
            throws Throwable {
        Class<?> testClass = custmizedClassLoader.loadClass(testClassName);
        Method testMethod = findMethod(testClass, testMethodName);
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectMethod(testClass, testMethod))
                .build();
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        launcher.execute(testPlan);
        TestExecutionSummary summary = listener.getSummary();
        List<TestExecutionSummary.Failure> failures = summary.getFailures();
        if (failures != null && !failures.isEmpty()) {
            throw failures.get(0).getException();
        }
    }

    private static Method findMethod(Class<?> testClass, String testMethodName) {
        Optional<Method> method = ReflectionUtils.findMethod(testClass, testMethodName);
        if (!method.isPresent()) {
            Method[] methods = Reflections.getUniqueDeclaredMethods(testClass);
            for (Method candidate : methods) {
                if (candidate.getName().equals(testMethodName)) {
                    return candidate;
                }
            }
        }
        assert method.isPresent();
        return method.get();
    }

    private static void intercept(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
        if (isModifiedClassPathClassLoader(extensionContext)) {
            invocation.proceed();
            return;
        }
        invocation.skip();
    }

    private static boolean isModifiedClassPathClassLoader(ExtensionContext extensionContext) {
        Class<?> testClass = extensionContext.getRequiredTestClass();
        ClassLoader classLoader = testClass.getClassLoader();
        return classLoader.getClass().getName().equals(ModifiedClassPathClassLoader.class.getName());
    }
}

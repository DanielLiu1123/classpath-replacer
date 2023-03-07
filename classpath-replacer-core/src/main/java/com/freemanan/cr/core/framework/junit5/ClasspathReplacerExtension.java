package com.freemanan.cr.core.framework.junit5;

import com.freemanan.cr.core.ModifiedClassPathClassLoader;
import com.freemanan.cr.core.ModifiedClassPathClassLoaderGenerator;
import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
import com.freemanan.cr.core.util.ModifiedClassLoaderCache;
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
public class ClasspathReplacerExtension implements InvocationInterceptor {

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

    private void runTestWithModifiedClassPath(
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        Class<?> testClass = extensionContext.getRequiredTestClass();
        Method testMethod = invocationContext.getExecutable();
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        // We only cache Class -> ClassLoader, because the ClassLoader may be different for different test methods
        ModifiedClassPathClassLoader customizedClassLoader = Optional.ofNullable(
                        testMethod.getAnnotation(ClasspathReplacer.class))
                .map(cr -> getModifiedClassLoader(cr, originalClassLoader))
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
            ClasspathReplacer cr = testClass.getAnnotation(ClasspathReplacer.class);
            return getModifiedClassLoader(cr, originalClassLoader);
        });
    }

    private static ModifiedClassPathClassLoader getModifiedClassLoader(
            ClasspathReplacer cr, ClassLoader originalClassLoader) {
        assert cr != null;
        Action[] actions = cr.value();
        ModifiedClassPathClassLoaderGenerator generator = ModifiedClassPathClassLoaderGenerator.of(originalClassLoader);
        for (Action action : actions) {
            switch (action.verb()) {
                case ADD:
                    generator.add(action.value());
                    break;
                case EXCLUDE:
                    generator.exclude(action.value());
                    break;
                case OVERRIDE:
                    generator.override(action.value());
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + action.verb());
            }
        }
        generator.classpathReplacer(cr);
        return generator.gen();
    }

    private void runTest(String testClassName, String testMethodName, ClassLoader custmizedClassLoader)
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

    private Method findMethod(Class<?> testClass, String testMethodName) {
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

    private void intercept(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
        if (isModifiedClassPathClassLoader(extensionContext)) {
            invocation.proceed();
            return;
        }
        invocation.skip();
    }

    private boolean isModifiedClassPathClassLoader(ExtensionContext extensionContext) {
        Class<?> testClass = extensionContext.getRequiredTestClass();
        ClassLoader classLoader = testClass.getClassLoader();
        return classLoader.getClass().getName().equals(ModifiedClassPathClassLoader.class.getName());
    }
}

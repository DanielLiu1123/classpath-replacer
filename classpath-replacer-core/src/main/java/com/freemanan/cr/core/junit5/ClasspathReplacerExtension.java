package com.freemanan.cr.core.junit5;

import com.freemanan.cr.core.ClassLoaderModifier;
import com.freemanan.cr.core.ModifiedClassPathClassLoader;
import com.freemanan.cr.core.anno.Action;
import com.freemanan.cr.core.anno.ClasspathReplacer;
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

        // TODO: cache
        //        URLClassLoader modifiedClassLoader = ModifiedClassPathClassLoader.get(testClass);

        ClasspathReplacer cr = Optional.ofNullable(testMethod.getAnnotation(ClasspathReplacer.class))
                .orElse(testClass.getAnnotation(ClasspathReplacer.class));
        assert cr != null;

        Action[] actions = cr.value();
        ClassLoaderModifier modifier = ClassLoaderModifier.of(originalClassLoader);
        for (Action action : actions) {
            switch (action.action()) {
                case ADD -> modifier.add(action.value());
                case EXCLUDE -> modifier.exclude(action.value());
                case OVERRIDE -> modifier.override(action.value());
                default -> throw new IllegalStateException("Unexpected value: " + action.action());
            }
        }
        ClassLoader custmizedClassLoader = modifier.gen();

        Thread.currentThread().setContextClassLoader(custmizedClassLoader);
        try {
            runTest(testClass.getName(), testMethod.getName(), custmizedClassLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
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
        if (method.isEmpty()) {
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

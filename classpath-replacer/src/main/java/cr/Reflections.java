package cr;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copy from Spring Framework.
 */
class Reflections {

    /**
     * Pre-built {@link MethodFilter} that matches all non-bridge non-synthetic methods
     * which are not declared on {@code java.lang.Object}.
     *
     * @since 3.0.5
     */
    public static final MethodFilter USER_DECLARED_METHODS =
            (method -> !method.isBridge() && !method.isSynthetic() && (method.getDeclaringClass() != Object.class));

    /**
     * Naming prefix for CGLIB-renamed methods.
     *
     * @see #isCglibRenamedMethod
     */
    private static final String CGLIB_RENAMED_METHOD_PREFIX = "CGLIB$";

    private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];

    /**
     * Cache for {@link Class#getDeclaredMethods()} plus equivalent default methods
     * from Java 8 based interfaces, allowing for fast iteration.
     */
    private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentHashMap<>(32);

    /**
     * Perform the given callback operation on all matching methods of the given
     * class and superclasses (or given interface and super-interfaces).
     * <p>The same named method occurring on subclass and superclass will appear
     * twice, unless excluded by the specified {@link MethodFilter}.
     *
     * @param clazz the class to introspect
     * @param mc    the callback to invoke for each method
     * @param mf    the filter that determines the methods to apply the callback to
     * @throws IllegalStateException if introspection fails
     */
    public static void doWithMethods(Class<?> clazz, MethodCallback mc, MethodFilter mf) {
        if (mf == USER_DECLARED_METHODS && clazz == Object.class) {
            // nothing to introspect
            return;
        }
        Method[] methods = getDeclaredMethods(clazz, false);
        for (Method method : methods) {
            if (mf != null && !mf.matches(method)) {
                continue;
            }
            try {
                mc.doWith(method);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Not allowed to access method '" + method.getName() + "': " + ex);
            }
        }
        // Keep backing up the inheritance hierarchy.
        if (clazz.getSuperclass() != null && (mf != USER_DECLARED_METHODS || clazz.getSuperclass() != Object.class)) {
            doWithMethods(clazz.getSuperclass(), mc, mf);
        } else if (clazz.isInterface()) {
            for (Class<?> superIfc : clazz.getInterfaces()) {
                doWithMethods(superIfc, mc, mf);
            }
        }
    }

    /**
     * Get the unique set of declared methods on the leaf class and all superclasses.
     * Leaf class methods are included first and while traversing the superclass hierarchy
     * any methods found with signatures matching a method already included are filtered out.
     *
     * @param leafClass the class to introspect
     * @throws IllegalStateException if introspection fails
     */
    public static Method[] getUniqueDeclaredMethods(Class<?> leafClass) {
        return getUniqueDeclaredMethods(leafClass, null);
    }

    /**
     * Get the unique set of declared methods on the leaf class and all superclasses.
     * Leaf class methods are included first and while traversing the superclass hierarchy
     * any methods found with signatures matching a method already included are filtered out.
     *
     * @param leafClass the class to introspect
     * @param mf        the filter that determines the methods to take into account
     * @throws IllegalStateException if introspection fails
     * @since 5.2
     */
    public static Method[] getUniqueDeclaredMethods(Class<?> leafClass, MethodFilter mf) {
        final List<Method> methods = new ArrayList<>(20);
        doWithMethods(
                leafClass,
                method -> {
                    boolean knownSignature = false;
                    Method methodBeingOverriddenWithCovariantReturnType = null;
                    for (Method existingMethod : methods) {
                        if (method.getName().equals(existingMethod.getName())
                                && method.getParameterCount() == existingMethod.getParameterCount()
                                && Arrays.equals(method.getParameterTypes(), existingMethod.getParameterTypes())) {
                            // Is this a covariant return type situation?
                            if (existingMethod.getReturnType() != method.getReturnType()
                                    && existingMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
                                methodBeingOverriddenWithCovariantReturnType = existingMethod;
                            } else {
                                knownSignature = true;
                            }
                            break;
                        }
                    }
                    if (methodBeingOverriddenWithCovariantReturnType != null) {
                        methods.remove(methodBeingOverriddenWithCovariantReturnType);
                    }
                    if (!knownSignature && !isCglibRenamedMethod(method)) {
                        methods.add(method);
                    }
                },
                mf);
        return methods.toArray(EMPTY_METHOD_ARRAY);
    }

    private static Method[] getDeclaredMethods(Class<?> clazz, boolean defensive) {
        assert clazz != null;
        Method[] result = declaredMethodsCache.get(clazz);
        if (result == null) {
            try {
                Method[] declaredMethods = clazz.getDeclaredMethods();
                List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
                if (defaultMethods != null) {
                    result = new Method[declaredMethods.length + defaultMethods.size()];
                    System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
                    int index = declaredMethods.length;
                    for (Method defaultMethod : defaultMethods) {
                        result[index] = defaultMethod;
                        index++;
                    }
                } else {
                    result = declaredMethods;
                }
                declaredMethodsCache.put(clazz, (result.length == 0 ? EMPTY_METHOD_ARRAY : result));
            } catch (Throwable ex) {
                throw new IllegalStateException(
                        "Failed to introspect Class [" + clazz.getName() + "] from ClassLoader ["
                                + clazz.getClassLoader() + "]",
                        ex);
            }
        }
        return (result.length == 0 || !defensive) ? result : result.clone();
    }

    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }

    /**
     * Determine whether the given method is a CGLIB 'renamed' method,
     * following the pattern "CGLIB$methodName$0".
     *
     * @param renamedMethod the method to check
     */
    public static boolean isCglibRenamedMethod(Method renamedMethod) {
        String name = renamedMethod.getName();
        if (name.startsWith(CGLIB_RENAMED_METHOD_PREFIX)) {
            int i = name.length() - 1;
            while (i >= 0 && Character.isDigit(name.charAt(i))) {
                i--;
            }
            return (i > CGLIB_RENAMED_METHOD_PREFIX.length() && (i < name.length() - 1) && name.charAt(i) == '$');
        }
        return false;
    }

    /**
     * Action to take on each method.
     */
    @FunctionalInterface
    public interface MethodCallback {

        /**
         * Perform an operation using the given method.
         *
         * @param method the method to operate on
         */
        void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
    }

    /**
     * Callback optionally used to filter methods to be operated on by a method callback.
     */
    @FunctionalInterface
    public interface MethodFilter {

        /**
         * Determine whether the given method matches.
         *
         * @param method the method to check
         */
        boolean matches(Method method);
    }
}

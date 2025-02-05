package cr.util;

import cr.ClasspathReplacer;
import cr.Repository;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

/**
 * @author Freeman
 */
public final class MavenUtils {
    private static final String mavenCenterUrl = "https://repo.maven.apache.org/maven2";
    private static final int MAX_RESOLUTION_ATTEMPTS = 3;
    private static final RepositorySystem repositorySystem;
    private static final DefaultRepositorySystemSession session;

    static {
        DefaultServiceLocator serviceLocator = MavenRepositorySystemUtils.newServiceLocator();
        serviceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        serviceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        repositorySystem = serviceLocator.getService(RepositorySystem.class);

        session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepository = new LocalRepository(System.getProperty("user.home") + "/.m2/repository");
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepository));
    }

    private MavenUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Resolves Maven coordinates to a list of URLs.
     *
     * @param coordinates       Maven coordinates of the form groupId:artifactId:version
     * @param classpathReplacer classpath replacer, nullable
     * @return list of URLs to the resolved artifacts
     */
    public static List<URL> resolveCoordinates(String[] coordinates, ClasspathReplacer classpathReplacer) {
        Exception latestFailure = null;
        for (int i = 0; i < MAX_RESOLUTION_ATTEMPTS; i++) {
            CollectRequest collectRequest = new CollectRequest(null, allRepositories(classpathReplacer));
            collectRequest.setDependencies(createDependencies(coordinates));
            DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
            try {
                DependencyResult result = repositorySystem.resolveDependencies(session, dependencyRequest);
                List<URL> resolvedArtifacts = new ArrayList<>();
                for (ArtifactResult artifact : result.getArtifactResults()) {
                    resolvedArtifacts.add(
                            artifact.getArtifact().getFile().toURI().toURL());
                }
                return resolvedArtifacts;
            } catch (Exception ex) {
                latestFailure = ex;
            }
        }
        throw new IllegalStateException(
                "Resolution failed after " + MAX_RESOLUTION_ATTEMPTS + " attempts", latestFailure);
    }

    private static List<RemoteRepository> allRepositories(ClasspathReplacer classpathReplacer) {
        List<RemoteRepository> extra = extraRepositories(classpathReplacer);
        RemoteRepository central = centralRepository();
        List<RemoteRepository> result = new ArrayList<>(extra);
        result.add(central);
        return result;
    }

    private static List<RemoteRepository> extraRepositories(ClasspathReplacer classpathReplacer) {
        if (classpathReplacer == null) {
            return Collections.emptyList();
        }
        List<RemoteRepository> extraRepositories = new ArrayList<>(classpathReplacer.repositories().length);
        for (Repository repo : classpathReplacer.repositories()) {
            String id = (repo.id() == null || repo.id().isEmpty()) ? repo.value() : repo.id();
            String url = repo.value();
            RemoteRepository.Builder builder = new RemoteRepository.Builder(id, "default", url);
            String username = parseIfNecessary(repo.username());
            String password = parseIfNecessary(repo.password());
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                builder.setAuthentication(new AuthenticationBuilder()
                        .addUsername(username)
                        .addPassword(password)
                        .build());
            }
            RemoteRepository repository = builder.build();
            extraRepositories.add(repository);
        }
        return extraRepositories;
    }

    private static String parseIfNecessary(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.startsWith("${") && str.endsWith("}")) {
            String variable = str.substring(2, str.length() - 1);
            String v = Optional.ofNullable(System.getenv(variable)).orElse(System.getProperty(variable));
            return v != null ? v : str;
        }
        return str;
    }

    private static RemoteRepository centralRepository() {
        return new RemoteRepository.Builder("central", "default", mavenCenterUrl).build();
    }

    private static List<Dependency> createDependencies(String[] coordinates) {
        List<Dependency> dependencies = new ArrayList<>();
        for (String coordinate : coordinates) {
            dependencies.add(new Dependency(new DefaultArtifact(coordinate), null));
        }
        return dependencies;
    }
}

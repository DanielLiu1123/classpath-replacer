package cr;

import cr.util.Const;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.internal.impl.scope.OptionalDependencySelector;
import org.eclipse.aether.internal.impl.scope.ScopeDependencySelector;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.supplier.SessionBuilderSupplier;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;

/**
 * @author Freeman
 */
final class MavenUtils {
    private static final int MAX_RESOLUTION_ATTEMPTS = 3;
    private static final RepositorySystem SYSTEM = new RepositorySystemSupplier().get();
    // SessionBuilderSupplier is not thread-safe, so access is guarded in newSession().
    private static final SessionBuilderSupplier SESSION_BUILDER_SUPPLIER = new SessionBuilderSupplier(SYSTEM);
    private static final Object SESSION_BUILDER_LOCK = new Object();
    private static final List<RemoteRepository> REPOSITORIES =
            List.of(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());

    private MavenUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Resolves Maven coordinate to a list of URLs.
     *
     * @param coordinate Maven coordinates of the form groupId:artifactId:version
     * @return list of URLs to the resolved artifacts
     */
    public static List<URL> resolveCoordinate(String coordinate) {
        if (coordinate == null || coordinate.isEmpty()) {
            throw new IllegalArgumentException("Coordinate cannot be null or empty");
        }
        if (!Pattern.matches(Const.MAVEN_COORDINATE_PATTERN, coordinate)) {
            throw new IllegalArgumentException("Invalid Maven coordinate: " + coordinate);
        }
        Exception latestFailure = null;
        for (int i = 0; i < MAX_RESOLUTION_ATTEMPTS; i++) {
            try (var session = newSession()) {
                Artifact artifact = new DefaultArtifact(coordinate);

                var collectRequest = new CollectRequest();
                collectRequest.setRoot(new Dependency(artifact, "compile", false, null));
                collectRequest.setRepositories(REPOSITORIES);

                var node = SYSTEM.collectDependencies(session, collectRequest).getRoot();
                var dependencyRequest = new DependencyRequest(node, (node1, parents) -> {
                    String scope = node1.getDependency() != null
                            ? node1.getDependency().getScope()
                            : null;
                    return Objects.equals(scope, "compile") || Objects.equals(scope, "runtime");
                });
                DependencyResult result = SYSTEM.resolveDependencies(session, dependencyRequest);

                List<URL> urls = new ArrayList<>();
                for (ArtifactResult artifactResult : result.getArtifactResults()) {
                    urls.add(artifactResult.getArtifact().getPath().toUri().toURL());
                }
                return urls;
            } catch (Exception ex) {
                latestFailure = ex;
            }
        }
        throw new IllegalStateException(
                "Resolution failed after " + MAX_RESOLUTION_ATTEMPTS + " attempts", latestFailure);
    }

    private static RepositorySystemSession.CloseableSession newSession() {
        RepositorySystemSession.SessionBuilder sessionBuilder;
        synchronized (SESSION_BUILDER_LOCK) {
            sessionBuilder = SESSION_BUILDER_SUPPLIER.get();
        }
        sessionBuilder
                .setDependencySelector(new AndDependencySelector(
                        ScopeDependencySelector.fromRoot(List.of("compile", "runtime"), List.of("test", "provided")),
                        OptionalDependencySelector.fromRoot(),
                        new ExclusionDependencySelector()))
                .withLocalRepositories(new LocalRepository(resolveLocalRepositoryPath()));
        return sessionBuilder.build();
    }

    private static Path resolveLocalRepositoryPath() {
        var path = System.getProperty("maven.repo.local");
        return path != null ? Paths.get(path) : Paths.get(defaultLocalRepositoryPath());
    }

    private static String defaultLocalRepositoryPath() {
        return Objects.requireNonNull(System.getProperty("user.home")) + File.separator + ".m2" + File.separator
                + "repository";
    }
}

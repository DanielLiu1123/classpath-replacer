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
    private static final RepositorySystem REPOSITORY_SYSTEM = new RepositorySystemSupplier().get();
    private static final RepositorySystemSession REPOSITORY_SYSTEM_SESSION = newSession();
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
    public static List<URL> resolveCoordinate(String coordinate) throws Exception {
        if (coordinate == null || coordinate.isEmpty()) {
            throw new IllegalArgumentException("Coordinate cannot be null or empty");
        }
        if (!Pattern.matches(Const.MAVEN_COORDINATE_PATTERN, coordinate)) {
            throw new IllegalArgumentException("Invalid Maven coordinate: " + coordinate);
        }
        Artifact artifact = new DefaultArtifact(coordinate);

        var collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, "compile", false, null));
        collectRequest.setRepositories(REPOSITORIES);

        var node = REPOSITORY_SYSTEM
                .collectDependencies(REPOSITORY_SYSTEM_SESSION, collectRequest)
                .getRoot();
        var dependencyRequest = new DependencyRequest(node, (node1, parents) -> {
            String scope = node1.getDependency() != null ? node1.getDependency().getScope() : null;
            return Objects.equals(scope, "compile") || Objects.equals(scope, "runtime");
        });
        DependencyResult result = REPOSITORY_SYSTEM.resolveDependencies(REPOSITORY_SYSTEM_SESSION, dependencyRequest);

        List<URL> urls = new ArrayList<>();
        for (ArtifactResult artifactResult : result.getArtifactResults()) {
            urls.add(artifactResult.getArtifact().getPath().toUri().toURL());
        }
        return urls;
    }

    private static RepositorySystemSession newSession() {
        var builder = new SessionBuilderSupplier(REPOSITORY_SYSTEM).get();
        builder.setDependencySelector(new AndDependencySelector(
                        ScopeDependencySelector.fromRoot(List.of("compile", "runtime"), List.of("test", "provided")),
                        OptionalDependencySelector.fromRoot(),
                        new ExclusionDependencySelector()))
                .withLocalRepositories(new LocalRepository(resolveLocalRepositoryPath()));
        return builder.build();
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

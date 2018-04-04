package net.redpipe.weld.vertx.servicediscovery;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Target({TYPE, METHOD, PARAMETER, FIELD})
@Retention(RUNTIME)
public @interface Service {

    /**
     * @return the name on which the service is published
     */
    @Nonbinding
    String name();

    /**
     * @return the host on which the service is published
     */
    @Nonbinding
    String host();

    /**
     * @return the port on which the service is published
     */
    @Nonbinding
    int port();

    /**
     * @return the host on which the service is published
     */
    @Nonbinding
    String path();

    public final class Literal extends AnnotationLiteral<Service> implements Service {

        private static final long serialVersionUID = 1L;

        static final Literal EMPTY = new Literal("", "", 0, "");

        private final String name;
        private final String host;
        private final int port;
        private final String path;

        public static Literal of(String name, String host, int port, String path) {
            return new Literal(name, host, port, path);
        }

        public String name() {
            return name;
        }

        public String host() {
            return host;
        }

        public int port() {
            return port;
        }

        public String path() {
            return path;
        }

        private Literal(String name, String host, int port, String path) {
            this.name = name;
            this.host = host;
            this.port = port;
            this.path = path;
        }

    }
}

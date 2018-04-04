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
public @interface ServiceClient {

    /**
     * @return the name on which the service is published
     */
    @Nonbinding
    String name();


    public final class Literal extends AnnotationLiteral<ServiceClient> implements ServiceClient {

        private static final long serialVersionUID = 1L;

        static final Literal EMPTY = new Literal("");

        private final String name;

        public static Literal of(String name) {
            return new Literal(name);
        }

        public String name() {
            return name;
        }


        private Literal(String name) {
            this.name = name;
        }

    }
}

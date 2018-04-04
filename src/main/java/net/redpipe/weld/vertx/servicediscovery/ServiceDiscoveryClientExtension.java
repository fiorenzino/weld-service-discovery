/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.redpipe.weld.vertx.servicediscovery;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.servicediscovery.types.HttpEndpoint;
import rx.Single;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.*;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This extension attempts to find all services and for each one register on service discovery registry.
 *
 * @author Fiorenzo Pizza
 */
public class ServiceDiscoveryClientExtension implements Extension {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscoveryClientExtension.class.getName());

    private Set<Class<?>> serviceInterfaces;

    void init(@Observes BeforeBeanDiscovery event) {
        serviceInterfaces = new HashSet<>();
    }

    void findServiceInterfaces(@Observes @WithAnnotations(ServiceClient.class) ProcessAnnotatedType<?> event, BeanManager beanManager) {
        AnnotatedType<?> annotatedType = event.getAnnotatedType();
        if (annotatedType.isAnnotationPresent(ServiceClient.class) && annotatedType.getJavaClass().isInterface()) {
            LOGGER.debug("ServiceClient interface {0} discovered", annotatedType.getJavaClass());
            serviceInterfaces.add(annotatedType.getJavaClass());
        }
    }

    void registerServiceBeans(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        Instance<ServiceDiscoverySupport> supportInstance = CDI.current().select(ServiceDiscoverySupport.class);
        if (!supportInstance.isResolvable()) {
            throw new IllegalStateException("ServiceDiscoverySupport cannot be resolved");
        }
        ServiceDiscoverySupport serviceProxySupport = supportInstance.get();
        for (Class<?> serviceInterface : serviceInterfaces) {
            event
                    .addBean()
                    .id(ServiceDiscoveryClientExtension.class.getName() + "_" + serviceInterface.getName())
                    .scope(Dependent.class)
                    .types(serviceInterface, Object.class)
                    .qualifiers(Any.Literal.INSTANCE, ServiceClient.Literal.EMPTY)
                    .createWith(ctx -> {
                        // First obtain the injection point metadata
                        InjectionPoint injectionPoint = (InjectionPoint)
                                beanManager.getInjectableReference(new InjectionPointMetadataInjectionPoint(), ctx);
                        // And obtain the address on which the service is published
                        Set<Annotation> qualifiers = injectionPoint.getQualifiers();
                        String name = null;
                        for (Annotation qualifier : qualifiers) {
                            if (ServiceClient.class.equals(qualifier.annotationType())) {
                                ServiceClient service = (ServiceClient) qualifier;
                                name = service.name();
                                break;
                            }
                        }
                        if (name == null) {
                            throw new IllegalStateException("Service name is not declared");
                        }
                        String recName = name;
                        Single<WebClient> client = HttpEndpoint
                                .rxGetWebClient(serviceProxySupport.getServiceDiscovery(), rec -> rec.getName().equals(recName));
                        return client;
                    });

            LOGGER.info("Custom bean for service interface {0} registered", serviceInterface);
        }
    }

    private static class InjectionPointMetadataInjectionPoint implements InjectionPoint {

        @Override
        public Type getType() {
            return InjectionPoint.class;
        }

        @SuppressWarnings("serial")
        @Override
        public Set<Annotation> getQualifiers() {
            return Collections.<Annotation>singleton(new AnnotationLiteral<Default>() {
            });
        }

        @Override
        public Bean<?> getBean() {
            return null;
        }

        @Override
        public Member getMember() {
            return null;
        }

        @Override
        public Annotated getAnnotated() {
            return null;
        }

        @Override
        public boolean isDelegate() {
            return false;
        }

        @Override
        public boolean isTransient() {
            return false;
        }

    }

}
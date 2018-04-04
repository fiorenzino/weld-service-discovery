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

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * The default implementation of {@link ServiceDiscoverySupport} relies on the functionality provided by <tt>weld-vertx-core</tt>.
 *
 * @author Fiorenzo Pizza
 */
@ApplicationScoped
public class DefaultServiceDiscoverySupport implements ServiceDiscoverySupport {

    @Inject
    private Vertx vertx;

    @Override
    public Vertx getVertx() {
        return vertx;
    }

    @Override
    public ServiceDiscovery getServiceDiscovery() {
        ServiceDiscovery serviceDiscovery = ServiceDiscovery
                .create(getVertx(), new ServiceDiscoveryOptions().setBackendConfiguration(config()));
        return serviceDiscovery;
    }

    @Override
    public JsonObject config() {
        return getVertx().getOrCreateContext().config();
    }

}

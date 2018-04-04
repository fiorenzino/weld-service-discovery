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

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;

import java.util.concurrent.Executor;

/**
 * A bean with this type and {@link javax.enterprise.inject.Default} qualifier must be available if using <tt>weld-vertx-service-discovery</tt>.
 *
 * @author Fiorenzo Pizza
 */
public interface ServiceDiscoverySupport {

    /**
     * @return the vertx instance
     * @see #getExecutor()
     */
    Vertx getVertx();

    /**
     * @return the ServiceDiscovery instance
     * @see #getExecutor()
     */
    ServiceDiscovery getServiceDiscovery();

    /**
     * @return the JsonObject configuration
     * @see #getExecutor()
     */
    JsonObject config();


    /**
     * By default, the service result handler is executed as blocking code.
     *
     * @return the executor used to execute a service result handler
     * @see Vertx#executeBlocking(io.vertx.core.Handler, boolean, io.vertx.core.Handler)
     */
    default Executor getExecutor() {
        return new Executor() {
            @Override
            public void execute(Runnable command) {
                getVertx().executeBlocking((f) -> {
                    try {
                        command.run();
                        f.complete();
                    } catch (Exception e) {
                        f.fail(e);
                    }
                }, false, null);
            }
        };
    }

    default void publishHttpEndpoint(ServiceDiscovery discovery, String name,
                                     String host, Integer port, String path, Future<Void> future) {
        Record record = HttpEndpoint.createRecord(name, host, port, path, new JsonObject().put("api.name", name));
        if (discovery == null) {
            future.fail("Cannot create service discovery service");
        } else {
            // publish the service
            discovery.publish(record, ar ->
            {
                if (ar.succeeded()) {
                    System.out.println("Service <" + ar.result().getName() + "> published");
                    future.complete();
                } else {
                    future.fail(ar.cause());
                }
            });
        }
    }

}

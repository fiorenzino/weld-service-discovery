# Weld Vert.x Extension for Service Discovery

The primary purpose of `weld-vertx-service-discovery` is to give [Vert.x Service Discovery](https://vertx.io/docs/vertx-service-discovery/java/) using CDI annotations.

## Features

* Allows to inject service client and pubblish services into vertx service discovery (as defined in https://github.com/fiorenzino/weld-service-discovery)

## Documentation

Two annotations:
- service (to publish some rest resource on vertx service discovery)
- service client (to inject )

```
@Service(name="name", host="localhost", port=8081, path="/")
```  for JAXRS resources:
where name, host, port, path are optionals:

- host => config().get("http_address")
- port => config().get("http_port")
- path=> default value is "/"
- name=> default value is java className

The example use for service:

```
@Path("/") 
@Aplicationscoped 
@Service(name="users") 
public class UsersService {
}
```

```
@ServiceClient(name="name") 
```
to inject service client in JAXRS resources:
where name is the service name.


The example use of client:

```
@ServiceClient(name="name")
Single<WebClient> serviceClient;
```

The serviceClient is usable in that way:

```
serviceClient.subscribe((client) -> { 
    if (client == null) { 
        System.out.println("no client"); 
    } else { 
            Single<HttpResponse> req= client.get("/path").rxSend();
            req.map(response -> {
            resultHandler.toHandler().handle(Future.succeededFuture(response.body().toString()));
            return "";
        }).subscribe();
    }
});
```

## Building

To build simply run:
```
> $ mvn clean install
```
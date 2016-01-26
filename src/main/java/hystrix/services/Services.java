package hystrix.services;

import redis.clients.jedis.Jedis;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Created by mschmidt on 1/20/16.
 */
@Path("/services")
public class Services {

    Jedis jedis;

    public Services() {
        jedis = new Jedis(System.getenv("REDIS_HOST"),
                Integer.valueOf(System.getenv("REDIS_PORT")));
    }

    @GET
    @Path("/status/{service_name}")
    public Response serviceStatus(@PathParam("service_name") String serviceName) {
        String hitCountKey = String.format("hit_count.%s", serviceName);
        jedis.incr(hitCountKey);
        String hitCount = jedis.get(hitCountKey);
        if (hitCountIsOverTreshold(hitCount)) {
            return Response.serverError().build();
        }
        return Response.ok(String.format("{serviceName=%s, status='ok'}", serviceName)).build();

    }

    private boolean hitCountIsOverTreshold(String hitCountKey) {
        return Integer.valueOf(hitCountKey) % 3 == 0;
    }

    @GET
    @Path("/healthy")
    public Response healthyService() {
        return Response.ok("Healthy service").build();
    }

    @GET
    @Path("/sick")
    public Response sickService() {

        jedis.incr("sick_service_count");
        if (Integer.valueOf(jedis.get("sick_service_count")) % 3 == 0) {
            if (Boolean.valueOf(jedis.get("sick_service_down"))) {
                jedis.set("sick_service_down", "false");
                return Response.ok().build();
            }
            jedis.set("sick_service_down", "true");
            return Response.serverError().build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/exception")
    public void exceptionService() {
        throw new RuntimeException();
    }

    @GET
    @Path("/timeout")
    public Response timeoutService() throws InterruptedException {

        jedis.incr("timeout_service");
        Thread.sleep(10000);
        return Response.noContent().build();
    }
}

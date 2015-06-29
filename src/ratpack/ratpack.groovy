import com.myott.MicroService
import com.myott.MicroServiceModule
import com.myott.vo.Profile
import com.zaxxer.hikari.HikariConfig
import ratpack.groovy.sql.SqlModule
import ratpack.hikari.HikariModule
import ratpack.hystrix.HystrixMetricsEventStreamHandler
import ratpack.hystrix.HystrixModule
import ratpack.jackson.JacksonModule
import ratpack.server.Service
import ratpack.server.StartEvent

import java.util.concurrent.TimeUnit

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.fromJson
import static ratpack.jackson.Jackson.json

ratpack {
  bindings {
    module SqlModule
    module JacksonModule
    module MicroServiceModule
    module new HystrixModule().sse()

    module(HikariModule) { HikariConfig c ->
      c.addDataSourceProperty("URL", "jdbc:h2:mem:dev;INIT=CREATE SCHEMA IF NOT EXISTS DEV")
      c.setDataSourceClassName("org.h2.jdbcx.JdbcDataSource")
    }

    bindInstance Service, new Service() {
      @Override
      void onStart(StartEvent event) throws Exception {
        event.registry.get(MicroService).createTables()
      }
    }
  }

  handlers { MicroService microService ->
    prefix("service") {
      post("register") {
        microService
          .register(parse(fromJson(Profile)))
          .timeout(500, TimeUnit.MILLISECONDS)
          .subscribe { Long id ->
            render "$id"
          }
      }
      prefix("id") {
        get(":id") {
          microService
            .getById(pathTokens.id as Long)
            .timeout(500, TimeUnit.MILLISECONDS)
            .subscribe { Profile profile ->
              render json(profile)
            }
        }
      }
      prefix("email") {
        get(":email") {
          microService
            .getByEmail(pathTokens.email)
            .timeout(500, TimeUnit.MILLISECONDS)
            .subscribe { Profile profile ->
              render json(profile)
            }
        }
      }
    }

    get("hystrix.stream", new HystrixMetricsEventStreamHandler());
  }
}
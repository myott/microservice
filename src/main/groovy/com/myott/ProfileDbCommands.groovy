package com.myott

import com.google.inject.Inject
import com.myott.vo.Profile
import com.myott.vo.StormService
import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandKey
import com.netflix.hystrix.HystrixObservableCommand
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ratpack.exec.ExecControl

import static ratpack.rx.RxRatpack.observe

/**
 * Created by matt on 6/21/15.
 */
class ProfileDbCommands {

  private final StormService stormService
  private final Sql sql
  private final ExecControl execControl
  private static final HystrixCommandGroupKey hystrixCommandGroupKey =
      HystrixCommandGroupKey.Factory.asKey("sql-profiledb")

  @Inject
  public ProfileDbCommands(StormService stormService, Sql sql, ExecControl execControl) {
    this.stormService = stormService
    this.sql = sql
    this.execControl = execControl
  }

  void createTables() {
    sql.execute('drop table if exists profile')
    sql.executeInsert("""
      | create table profile (
      |   id bigint primary key auto_increment,
      |   first_name varchar(30),
      |   last_name varchar(30),
      |   email varchar(30))""".stripMargin())
  }

  rx.Observable<GroovyRowResult> getById(Long id) {
    return new HystrixObservableCommand<GroovyRowResult>(
        HystrixObservableCommand.Setter
            .withGroupKey(hystrixCommandGroupKey)
            .andCommandKey(HystrixCommandKey.Factory.asKey("getById"))) {

      @Override
      protected rx.Observable<GroovyRowResult> construct() {
        observe(execControl.blocking {
          sql.firstRow([id: id], 'select * from profile where id=:id')
        })
      }

      @Override
      protected String getCacheKey() {
        return "db-profiledb-getById"
      }
    }.toObservable()
  }

  rx.Observable<GroovyRowResult> getByEmail(String email) {
    return new HystrixObservableCommand<GroovyRowResult>(
        HystrixObservableCommand.Setter
            .withGroupKey(hystrixCommandGroupKey)
            .andCommandKey(HystrixCommandKey.Factory.asKey("getByEmail"))) {

      @Override
      protected rx.Observable<GroovyRowResult> construct() {
        observe(execControl.blocking {
          sql.firstRow([email: email], "select * from profile where email=:email")
        })
      }

      @Override
      protected String getCacheKey() {
        return "db-profiledb-getByEmail"
      }
    }.toObservable()
  }

  rx.Observable<Long> insert(Profile profile) {
    return new HystrixObservableCommand<Long>(
        HystrixObservableCommand.Setter
            .withGroupKey(hystrixCommandGroupKey)
            .andCommandKey(HystrixCommandKey.Factory.asKey("insert"))) {

      @Override
      protected rx.Observable<Long> construct() {
        observe(
            execControl.blocking {
              Map map = [firstName: profile.firstName, lastName: profile.lastName, email: profile.email]
              sql.executeInsert(map, 'insert into profile (first_name, last_name, email) values (:firstName, :lastName, :email)')
            }
            .map { List<List<Object>> l ->
              l[0][0]
            }
            .map { it.toString() }
            .map(Long.&parseLong)
        )
      }

      @Override
      protected rx.Observable<Long> resumeWithFallback() {
        observe(
            execControl.blocking {
              stormService.write(profile)
              return 0;
            }
        )
      }
    }.toObservable()
  }
}

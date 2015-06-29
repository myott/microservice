package com.myott

import com.google.inject.Inject
import com.myott.vo.Profile
import groovy.sql.GroovyRowResult
import rx.functions.Func1

/**
 * Created by matt on 6/21/15.
 */
class MicroService {

  final ProfileDbCommands profileDbCommands

  @Inject
  MicroService(ProfileDbCommands profileDbCommands) {
    this.profileDbCommands = profileDbCommands
  }

  void createTables() {
    profileDbCommands.createTables()
  }

  rx.Observable<Long> register(Profile profile) {
    profileDbCommands.insert(profile)
  }

  rx.Observable<Profile> getById(Long id) {
    profileDbCommands.getById(id).map(rowToPerson)
  }

  rx.Observable<Profile> getByEmail(String email) {
    profileDbCommands.getByEmail(email).map(rowToPerson)
  }

  private Func1<GroovyRowResult, Profile> rowToPerson = { GroovyRowResult row ->
    def map = row?.subMap('id', 'email')
    if (map) {
      map.firstName = row.first_name
      map.lastName = row.last_name
    }
    map ? new Profile(map) : null
  }
}

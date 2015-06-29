package com.myott

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.myott.vo.StormService

/**
 * Created by matt on 6/21/15.
 */
class MicroServiceModule extends AbstractModule {
  @Override
  protected void configure() {
   bind(MicroService).in(Scopes.SINGLETON)
   bind(ProfileDbCommands).in(Scopes.SINGLETON)
   bind(StormService).in(Scopes.SINGLETON)
  }
}

package com.aimprosoft.hbase.module

import com.aimprosoft.hbase.Initializer
import com.google.inject.AbstractModule

class PlayModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Initializer]).asEagerSingleton()
  }
}
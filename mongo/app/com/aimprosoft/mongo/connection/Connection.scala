package com.aimprosoft.mongo.connection

import com.aimprosoft.mongo.converters.Converters.{departmentId, name}
import org.mongodb.scala.bson.BsonNumber
import org.mongodb.scala.model.IndexOptions
import org.mongodb.scala.{ConnectionString, Document, MongoClient, MongoClientSettings, MongoCollection, MongoCredential, MongoDatabase, ServerAddress}
import play.api.Configuration

import javax.inject.{Inject, Singleton}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.SeqHasAsJava
import org.mongodb.scala.model.Filters._

@Singleton
class Connection @Inject()(conf: Configuration) {

  private def buildMongoClient(): MongoClient = {

    val user = conf.get[String]("mongo.user")
    val password = conf.get[String]("mongo.password")
    val database = conf.get[String]("mongo.database")

    val address = new ServerAddress(
      conf.get[String]("mongo.host"),
      conf.get[Int]("mongo.port"))

    val clientSettings =
      MongoClientSettings
        .builder()
        .credential(MongoCredential.createCredential(user, database, password.toCharArray))
        .applyToClusterSettings(settings => settings.hosts(List(address).asJava))
        .build()

    MongoClient(clientSettings)
  }

  val client: MongoClient = buildMongoClient()

  val database: MongoDatabase = client.getDatabase("departments")

  val employees: MongoCollection[Document] = database.getCollection("employees")

  val departments: MongoCollection[Document] = database.getCollection("departments")

  {
    Await.ready(employees.createIndex(Document(departmentId -> 1)).toFuture(), 10.seconds)
    Await.ready(departments.createIndex(Document(name -> 1), IndexOptions().unique(true)).toFuture(), 10.seconds)
  }

  // Must be used for 'testing' only
  def truncate() = {
    Await.result(departments.deleteMany(empty()).toFuture(), 10.seconds)
    Await.result(employees.deleteMany(empty()).toFuture(), 10.seconds)
  }

}

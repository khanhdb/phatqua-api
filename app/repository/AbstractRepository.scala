package repository

import play.api.db.{DBApi, Database}

trait AbstractRepository{
  protected def dbAPI: DBApi
  protected def databaseName: String = "default"
  protected def db: Database = dbAPI.database(databaseName)
}

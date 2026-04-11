package ba.fluxor.fetchapi.configuration

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

object DatabaseMigrator {

  fun migrate() {
    val connection = DatabaseConnectionProvider.connection

    val database = DatabaseFactory.getInstance()
      .findCorrectDatabaseImplementation(JdbcConnection(connection))

    Liquibase(
      "db/changelog/db.changelog-master.xml",
      ClassLoaderResourceAccessor(),
      database
    ).update("")

    connection.autoCommit = true

    println("Database migration complete")
  }
}

package ba.fluxor.fetchapi.di

import ba.fluxor.fetchapi.configuration.DatabaseConnectionProvider
import ba.fluxor.fetchapi.feature.project.data.dao.ProjectDao
import ba.fluxor.fetchapi.feature.project.data.ProjectRepository
import ba.fluxor.fetchapi.feature.project.viewmodel.ProjectViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.sql.Connection

val appModule = module {
  single<Connection> { DatabaseConnectionProvider.connection }
  single { ProjectDao(get()) }
  single { ProjectRepository(get()) }
  viewModel { ProjectViewModel(get()) }
}

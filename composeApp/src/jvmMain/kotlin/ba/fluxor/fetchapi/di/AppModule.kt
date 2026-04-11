package ba.fluxor.fetchapi.di

import ba.fluxor.fetchapi.configuration.DatabaseConnectionProvider
import ba.fluxor.fetchapi.feature.project.data.dao.ProjectDao
import ba.fluxor.fetchapi.feature.project.data.ProjectRepository
import ba.fluxor.fetchapi.feature.project.viewmodel.ProjectViewModel
import ba.fluxor.fetchapi.feature.settings.data.SettingDao
import ba.fluxor.fetchapi.feature.settings.data.SettingRepository
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import ba.fluxor.fetchapi.ui.shell.viewmodel.AppShellViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.sql.Connection

val appModule = module {
  single<Connection> { DatabaseConnectionProvider.connection }
  single { ProjectDao(get()) }
  single { ProjectRepository(get()) }
  viewModel { ProjectViewModel(get()) }
  single { SettingDao(get()) }
  single { SettingRepository(get()) }
  viewModel { SettingsViewModel(get()) }
  viewModel { AppShellViewModel() }
}

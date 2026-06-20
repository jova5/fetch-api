package ba.fluxor.fetchapi.di

import ba.fluxor.fetchapi.configuration.DatabaseFactory
import ba.fluxor.fetchapi.feature.folder.data.FolderRepository
import ba.fluxor.fetchapi.feature.folder.data.dao.FolderDao
import ba.fluxor.fetchapi.feature.folder.viewmodel.FolderViewModel
import ba.fluxor.fetchapi.feature.project.data.ProjectRepository
import ba.fluxor.fetchapi.feature.project.data.dao.ProjectDao
import ba.fluxor.fetchapi.feature.project.viewmodel.ProjectViewModel
import ba.fluxor.fetchapi.feature.project_tree.data.TreeReorderService
import ba.fluxor.fetchapi.feature.project_tree.viewmodel.ProjectTreeViewModel
import ba.fluxor.fetchapi.feature.request.data.RequestRepository
import ba.fluxor.fetchapi.feature.request.data.dao.RequestDao
import ba.fluxor.fetchapi.feature.request.viewmodel.RequestViewModel
import ba.fluxor.fetchapi.feature.settings.data.SettingDao
import ba.fluxor.fetchapi.feature.settings.data.SettingRepository
import ba.fluxor.fetchapi.feature.settings.viewmodel.SettingsViewModel
import ba.fluxor.fetchapi.feature.sub_project.data.SubProjectRepository
import ba.fluxor.fetchapi.feature.sub_project.data.SubProjectVariableRepository
import ba.fluxor.fetchapi.feature.sub_project.data.dao.SubProjectDao
import ba.fluxor.fetchapi.feature.sub_project.data.dao.SubProjectVariableDao
import ba.fluxor.fetchapi.feature.sub_project.viewmodel.SubProjectViewModel
import ba.fluxor.fetchapi.feature.tabs.data.TabRepository
import ba.fluxor.fetchapi.feature.tabs.data.dao.TabDao
import ba.fluxor.fetchapi.feature.tabs.viewmodel.TabsViewModel
import kotlinx.coroutines.sync.Mutex
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.sql.Connection

val appModule = module {
  single<Connection> { DatabaseFactory.connection }
  // Serializes write *transactions* across the single shared JDBC connection. Note: plain
  // (non-transactional) writes don't take this lock, so they aren't serialized against an open
  // transaction — accepted for this single-user app. See `Connection.transaction` for details.
  single { Mutex() }
  single { SettingDao(get()) }
  single { SettingRepository(get()) }
  viewModel { SettingsViewModel(get()) }
  single { ProjectDao(get()) }
  single { ProjectRepository(get()) }
  viewModel { ProjectViewModel(get(), get()) }
  single { SubProjectDao(get()) }
  single { SubProjectRepository(get()) }
  single { SubProjectVariableDao(get()) }
  single { SubProjectVariableRepository(get(), get()) }
  single { SubProjectViewModel(get(), get()) }
  single { FolderDao(get()) }
  single { FolderRepository(get()) }
  single { RequestDao(get()) }
  single { RequestRepository(get()) }
  single { RequestViewModel(get()) }
  single { FolderViewModel(get()) }
  single { TreeReorderService(get(), get(), get(), get(), get()) }
  viewModel { ProjectTreeViewModel(get(), get(), get(), get()) }
  single { TabDao(get()) }
  single { TabRepository(get()) }
  single { TabsViewModel(get(), get(), get(), get(), get()) }
}

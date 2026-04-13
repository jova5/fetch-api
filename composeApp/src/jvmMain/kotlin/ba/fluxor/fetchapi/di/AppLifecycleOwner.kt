package ba.fluxor.fetchapi.di

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class AppLifecycleOwner : LifecycleOwner, ViewModelStoreOwner {
  override val lifecycle: LifecycleRegistry = LifecycleRegistry(this)
  override val viewModelStore: ViewModelStore = ViewModelStore()

  init {
    lifecycle.currentState = Lifecycle.State.RESUMED
  }
}

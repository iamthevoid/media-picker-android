package iam.thevoid.mediapicker.picker.fragment

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KClass

class ViewModelLazy<VM : ViewModel>(
        private val cls: KClass<VM>,
        private val storageProducer: () -> ViewModelProvider
) : Lazy<VM> {
    private var cached: VM? = null

    override val value: VM
        get() {
            var vm = cached
            if (vm == null) {
                vm = storageProducer().get(cls.java)
                cached = vm
            }
            return vm
        }

    override fun isInitialized() = cached != null
}

class SingleProviderFactory<E : ViewModel>(private val factory: () -> E) :
        ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val create = factory()
        try {
            return modelClass.cast(create)!!
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("Model class NOT supported", e)
        }

    }
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.activityViewModel() =
        ViewModelLazy(VM::class) {
            ViewModelProvider(
                    activity!!
            )
        }

@MainThread
inline fun <reified VM : ViewModel> Fragment.activityViewModel(noinline factory: () -> VM) =
        ViewModelLazy(VM::class) {
            ViewModelProvider(
                    activity!!,
                    SingleProviderFactory(factory)
            )
        }
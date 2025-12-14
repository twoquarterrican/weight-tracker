package io.github.twoquarterrican.weighttracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.twoquarterrican.weighttracker.data.WeightDao
import io.github.twoquarterrican.weighttracker.data.WeightEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeightViewModel(private val weightDao: WeightDao) : ViewModel() {

    val allWeights: StateFlow<List<WeightEntry>> = weightDao.getAllWeights()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addWeight(weight: Float, date: Long) {
        viewModelScope.launch {
            weightDao.insert(WeightEntry(weight = weight, date = date))
        }
    }

    fun deleteWeight(entry: WeightEntry) {
        viewModelScope.launch {
            weightDao.delete(entry)
        }
    }
}

class WeightViewModelFactory(private val weightDao: WeightDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeightViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeightViewModel(weightDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

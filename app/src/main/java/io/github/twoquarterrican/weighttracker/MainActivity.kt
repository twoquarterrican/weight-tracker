package io.github.twoquarterrican.weighttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import io.github.twoquarterrican.weighttracker.data.WeightDatabase
import io.github.twoquarterrican.weighttracker.ui.WeightTrackerApp
import io.github.twoquarterrican.weighttracker.ui.theme.WeightTrackerTheme
import io.github.twoquarterrican.weighttracker.ui.WeightViewModel
import io.github.twoquarterrican.weighttracker.ui.WeightViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = WeightDatabase.getDatabase(this)
        val viewModelFactory = WeightViewModelFactory(database.weightDao())
        val viewModel = ViewModelProvider(this, viewModelFactory)[WeightViewModel::class.java]

        setContent {
            WeightTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeightTrackerApp(viewModel)
                }
            }
        }
    }
}

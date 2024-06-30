package com.v0lt.feather

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.v0lt.feather.ui.theme.FeatherTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Configuration : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configuration2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContent {
            FeatherTheme {
                val store = UserStore(LocalContext.current)
                var healthbox_url = remember { mutableStateOf("https://v0lttech.com/healthbox/submit.php") }
                var healthbox_service = remember { mutableStateOf("abcdef123456789") }

                val healthbox_url_stored = store.getHealthboxUrl.collectAsState(initial = "")
                val healthbox_service_stored = store.getHealthboxService.collectAsState(initial = "")

                healthbox_url.value = healthbox_url_stored.value
                healthbox_service.value = healthbox_service_stored.value


                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->
                    Button(
                        onClick = { this.finish() },
                        modifier = Modifier
                            .padding(vertical = 40.dp)
                    ) {
                        Text("Back")
                    }
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 100.dp)
                    ) {
                        Text(text = "Configuration", fontSize = 40.sp,)
                        TextField(
                            value = healthbox_url.value,
                            onValueChange = { healthbox_url.value = it },
                            label = {Text(text = "Submission Endpoint")},
                            modifier = Modifier.padding(top = 20.dp) .fillMaxWidth()
                        )
                        TextField(
                            value = healthbox_service.value,
                            onValueChange = { healthbox_service.value = it },
                            label = {Text(text = "Service Identifier")},
                            modifier = Modifier.padding(top = 8.dp) .fillMaxWidth()
                        )
                        Button(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                store.saveHealthboxUrl(healthbox_url.value)
                                store.saveHealthboxService(healthbox_service.value)
                            }

                        }, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}

/*
@Composable
fun ConfigurationForm() {


    TextField(
        value = text,
        onValueChange = { text = it },
    )
}
 */
package com.v0lt.feather

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.v0lt.feather.ui.theme.FeatherTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import org.json.JSONTokener


class UserStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("configuration")
        private val HEALTHBOX_URL = stringPreferencesKey("healthbox_url")
        private val HEALTHBOX_SERVICE = stringPreferencesKey("healthbox_service")

    }

    val getHealthboxUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[HEALTHBOX_URL] ?: "https://v0lttech.com/healthbox/submit.php"
    }
    val getHealthboxService: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[HEALTHBOX_SERVICE] ?: ""
    }

    suspend fun saveHealthboxUrl(value: String) {
        context.dataStore.edit { preferences ->
            preferences[HEALTHBOX_URL] = value
        }
    }
    suspend fun saveHealthboxService(value: String) {
        context.dataStore.edit { preferences ->
            preferences[HEALTHBOX_SERVICE] = value
        }
    }
}

class MainActivity : ComponentActivity() {
    val mood_options = arrayOf("Depressed", "Sad", "Upset", "Bored", "Neutral", "Content", "Pleased", "Happy", "Ecstatic")
    val mood_values = arrayOf(-4, -3, -2, -1, 0, 1, 2, 3, 4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FeatherTheme {
                val store = UserStore(LocalContext.current)
                val healthbox_url_stored = store.getHealthboxUrl.collectAsState(initial = "")
                val healthbox_service_stored = store.getHealthboxService.collectAsState(initial = "")

                var response_text = remember {
                    mutableStateOf("")
                }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->
                    Button(
                        onClick = { val intent = Intent(this, Configuration::class.java)
                            startActivity(intent) },
                        shape = CircleShape,
                        modifier = Modifier
                            .padding(vertical = 40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Configure",
                        )
                    }


                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Text(
                            text = "Feather",
                            fontWeight = FontWeight(1),
                            fontSize = 58.sp,
                            modifier = Modifier
                                .padding(top = 50.dp)
                        )
                        Text(
                            text = "How are you feeling?",
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(bottom = 20.dp)
                        )

                        for (index in 0 .. mood_options.size - 1) {
                            Button(onClick = { SubmitEmotion(response_text, mood_values[index], healthbox_url_stored.value, healthbox_service_stored.value) }) {
                                Text(mood_options[index])
                            }
                        }
                        Text(
                            text = response_text.value.toString(),
                            modifier = Modifier
                                .padding(top = 20.dp)
                        )
                    }
                }
            }
        }
    }


    private fun SubmitEmotion(response_text: MutableState<String>, mood: Int, healthbox_url: String, healthbox_service: String) {
        val current_time = System.currentTimeMillis() / 1000
        val volleyQueue = Volley.newRequestQueue(this)

        val url = "$healthbox_url?service=$healthbox_service&category=mental&metric=mood&key-mood=$mood&key-time=$current_time"

        val request_object = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val healthbox_response = JSONTokener(response.toString()).nextValue() as JSONObject
                if (healthbox_response.has("error")) {
                    response_text.value = "HealthBox Error: ${healthbox_response.toString()}"
                } else if (healthbox_response.has("success")) {
                        response_text.value = "Success"
                } else {
                    response_text.value = response["error"].toString()
                }
            },
            { error -> response_text.value = "Application Error: $error.toString()" }
        )

        volleyQueue.add(request_object)

    }
}
package com.example.androidappdatapersistence

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidappdatapersistence.ui.theme.AndroidAppDataPersistenceTheme
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                MyScreen()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    // Basic Material Theme setup with custom typography
    MaterialTheme {
        content()
    }
}

@Composable
fun MyScreen() {
    var textState by remember { mutableStateOf(TextFieldValue()) }
    var message by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text("Enter text") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                saveToFile(textState.text)
                message = "Data saved"
            }) {
                Text("Save")
            }
            Button(onClick = {
                message = retrieveFromFile() ?: "No data in storage"
            }) {
                Text("Retrieve")
            }
        }
        Text(message)
    }
}

private fun saveToFile(data: String) {
    // Implement file save logic
}

private fun retrieveFromFile(): String? {
    // Implement file retrieve logic
    return null
}

private fun saveToFile(data: String, context: Context) {
    try {
        context.openFileOutput("data.txt", Context.MODE_PRIVATE).use {
            it.write(data.toByteArray())
        }
    } catch (e: Exception) {
        // Handle exceptions
    }
}

private fun retrieveFromFile(context: Context): String? {
    return try {
        context.openFileInput("data.txt").bufferedReader().useLines { lines ->
            lines.fold("") { some, text -> some + text }
        }
    } catch (e: Exception) {
        // Handle exceptions
        null
    }
}

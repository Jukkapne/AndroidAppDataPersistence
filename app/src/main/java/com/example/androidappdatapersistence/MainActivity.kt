package com.example.androidappdatapersistence

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope() // Create a CoroutineScope
    var textState by remember { mutableStateOf(TextFieldValue()) }
    var fileMessage by remember { mutableStateOf("") }
    var roomMessage by remember { mutableStateOf("") }
    var roomSavedData by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // UI for file operations
        TextField(
            value = textState,
            onValueChange = { textState = it },
            label = { Text("Enter text") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                saveToFile(textState.text, context)
                fileMessage = "Data saved to file"
            }) {
                Text("Save to File")
            }
            Button(onClick = {
                fileMessage = retrieveFromFile(context) ?: "No data in file"
            }) {
                Text("Retrieve from File")
            }
        }
        Text(fileMessage)

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // UI for Room operations
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                saveToRoom(textState.text, context)
                roomMessage = "Data saved to Room"
            }) {
                Text("Save to Room")
            }
            Button(onClick = {
                coroutineScope.launch {
                    roomSavedData = retrieveFromRoom(context).joinToString("\n") { it.text }
                    roomMessage = if (roomSavedData.isEmpty()) "No data in Room" else "Data retrieved from Room"
                }
            }) {
                Text("Retrieve from Room")
            }
        }
        Text(roomMessage)
        if (roomSavedData.isNotEmpty()) {
            Text("Room Data: \n$roomSavedData")
        }
    }
}



private fun saveToFile(data: String, context: Context) {
    try {
        context.openFileOutput("data.txt", Context.MODE_APPEND).use {
            it.write((data + "\n").toByteArray()) // Append a newline for each new entry
        }
    } catch (e: Exception) {
        // Handle exceptions
        Log.e("MainActivity", "Error appending to file", e)
    }
}



private fun retrieveFromFile(context: Context): String? {
    return try {
        context.openFileInput("data.txt").bufferedReader().useLines { lines ->
            lines.joinToString("\n")
        }
    } catch (e: Exception) {
        // Log the exception for debugging
        Log.e("MainActivity", "Error reading from file", e)
        null
    }
}

// code for the Room database

//entity class
@Entity(tableName = "texts")
data class TextEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String
)

//data access object
@Dao
interface TextDao {
    @Insert
    suspend fun insertText(textEntity: TextEntity)

    @Query("SELECT * FROM texts")
    suspend fun getAllTexts(): List<TextEntity>
}


//database class
@Database(entities = [TextEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun textDao(): TextDao
}

//implement save and retrieve functions
private fun saveToRoom(data: String, context: Context) {
    val database = Room.databaseBuilder(context, AppDatabase::class.java, "app-database").build()
    CoroutineScope(Dispatchers.IO).launch {
        database.textDao().insertText(TextEntity(text = data))
    }
}

private suspend fun retrieveFromRoom(context: Context): List<TextEntity> {
    val database = Room.databaseBuilder(context, AppDatabase::class.java, "app-database").build()
    return database.textDao().getAllTexts()
}
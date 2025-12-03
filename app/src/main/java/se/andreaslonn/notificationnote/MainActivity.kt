package se.andreaslonn.notificationnote

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.core.app.NotificationManagerCompat
import se.andreaslonn.notificationnote.ui.theme.NotificationNoteTheme
import kotlin.jvm.java
import kotlin.random.Random

private const val CHANNEL_ID = "MY_NOTIFICATION_CHANNEL"

data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val time: Int,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        setContent {
            NotificationNoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val notes = remember { mutableStateListOf(
                        Note(10, "My Title 0", "My content 0", 10),
                        Note(11, "My Title 1", "My content 1", 11),
                    )}

                    LaunchedEffect(1) {
                        notes.forEach {
                            postNotificationNote(it)
                        }
                    }

                    Column {
                        val extras = intent.extras
                        val keys = extras?.keySet()
                        keys?.forEach {
                            println(it)
                            println("str: ${extras.getString(it)}")
                            println("int: ${extras.getInt(it)}")
                        }

                        println(intent.extras)
                        Greeting(
                            name = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1).toString(),
                            modifier = Modifier.padding(innerPadding)
                        )

                        val textFieldStateTitle = rememberTextFieldState("My Title")
                        val textFieldStateContent = rememberTextFieldState("My Content")

                        TextField(
                            state = textFieldStateTitle,
                            label = {
                                Text("Title")
                            }
                        )

                        TextField(
                            state = textFieldStateContent,
                            label = {
                                Text("Content")
                            }
                        )

                        Button(
                            onClick = {
                                // https://developer.android.com/develop/ui/views/notifications/build-notification

                                Toast.makeText(this@MainActivity, "Post notification", Toast.LENGTH_SHORT).show()

                                val noteTitle = textFieldStateTitle.text.toString()
                                val noteContent = textFieldStateContent.text.toString()

                                val note = Note(
                                    id = Random.nextInt(),
                                    title = noteTitle,
                                    content = noteContent,
                                    time = 0
                                )

                                notes.add(note)

                                postNotificationNote(note)
                            }
                        ) {
                            Text(stringResource(R.string.send_notification))
                        }

                        LazyColumn {
                            items(
                                notes,
                                key = { it.id }
                            ) { note ->
                                ListItem(
                                    overlineContent = {
                                        Text(note.id.toString())
                                    },
                                    headlineContent = {
                                        Text(note.title)
                                    },
                                    supportingContent = {
                                        Text(note.content)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun postNotificationNote(note: Note) {
        // https://developer.android.com/develop/ui/views/notifications/build-notification#Actions
        val ACTION_SNOOZE = "snooze"

        val snoozeIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_NOTIFICATION_ID, note.id)
        }
        val pendingIntent: PendingIntent
            = PendingIntent.getActivity(this, note.id, snoozeIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_note_24)
            .setContentTitle(note.title)
            .setContentText(note.content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.notification_note_24, "my action ${note.id}", pendingIntent)

        with(NotificationManagerCompat.from(this@MainActivity)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this@MainActivity, "No permission", Toast.LENGTH_SHORT).show()

                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            notify(note.id, builder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        // https://developer.android.com/develop/ui/views/notifications/channels

        Toast.makeText(this@MainActivity, "Create notification channel", Toast.LENGTH_SHORT).show()
        // Create the NotificationChannel.
        val name = getString(R.string.channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }
}

/*

- Show a notification with title and text
    - "ongoing" where possible
- When dismissed -> send new notification
- On reboot -> send notification
    - DataStore
    - onBootComplete receiver

 */

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotificationNoteTheme {
        Greeting("Android")
    }
}
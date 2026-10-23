package com.example.displayon

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { Home() } } }
    }
}

/** One screen: this phone can be woken (own code) and can wake the other phone (peer code). */
@Composable
fun Home() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val notifPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    val myCode = remember {
        Prefs.code(ctx) ?: List(6) { Random.nextInt(10) }.joinToString("").also { Prefs.setCode(ctx, it) }
    }
    var myStatus by remember { mutableStateOf("Registering…") }
    var peer by remember { mutableStateOf(Prefs.peerCode(ctx)) }
    var sendStatus by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
        myStatus = runCatching { Repo.registerTarget(myCode); "Ready to be woken" }
            .getOrElse { "Error: ${it.message}" }
    }

    Column(
        Modifier.padding(24.dp).fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Display On", style = MaterialTheme.typography.headlineMedium)

        Text("My code (enter this on the other phone):")
        Text(myCode, style = MaterialTheme.typography.displayMedium)
        Text(myStatus)

        HorizontalDivider()

        Text("Other phone's code:")
        OutlinedTextField(peer, { peer = it.filter(Char::isDigit).take(6) }, singleLine = true)
        Button(
            enabled = peer.length == 6,
            onClick = {
                Prefs.setPeerCode(ctx, peer)
                sendStatus = "Sending…"
                scope.launch {
                    sendStatus = runCatching { Repo.sendScreenOn(peer); "Sent" }
                        .getOrElse { "Error: ${it.message}" }
                }
            }
        ) { Text("Turn other phone's display on") }
        Text(sendStatus)

        HorizontalDivider()

        Text("Grant these so this phone's screen can wake while locked:")
        Button({
            ctx.startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}"))
            )
        }) { Text("Allow display over other apps") }
        if (Build.VERSION.SDK_INT >= 34) {
            Button({
                ctx.startActivity(
                    Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${ctx.packageName}"))
                )
            }) { Text("Allow full-screen notifications") }
        }
        Button({
            ctx.startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}"))
            )
        }) { Text("Disable battery optimization") }
    }
}

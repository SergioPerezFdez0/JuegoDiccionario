package es.sergiop.diccionarioapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import es.sergiop.diccionarioapp.ui.theme.DiccionarioAppTheme
import es.sergiop.diccionarioapp.views.VideoPlayer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiccionarioAppTheme {
                MainView("Diccionario")
            }
        }
    }
}

@SuppressLint("ResourceType")
@Composable
fun MainView(name: String) {
    val mContext = LocalContext.current

    var showVideoPlayer by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                showVideoPlayer = false
            }
        }
        timer.start()
        onDispose {
            timer.cancel()
        }
    }

    if (showVideoPlayer) {
        val videoResId: Uri = Uri.parse("android.resource://" + mContext.packageName + "/" + R.raw.nggyu)
        VideoPlayer(videoResId, mContext)
    }
    else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.inversePrimary)
        ) {
            Image(
                painter = painterResource(id = R.raw.logonobg),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(400.dp)
                    .padding(10.dp)
            )

            Text(
                text = "El juego del $name",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            )

            Button(
                onClick = {
                    mContext.startActivity(Intent(mContext, JugarActivity::class.java))
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = "Jugar",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Button(
                onClick = {
                    mContext.startActivity(Intent(mContext, ComoJugarActivity::class.java))
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = "Cómo Jugar",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

            Button(
                onClick = {
                    mContext.startActivity(Intent(mContext, ContactoActivity::class.java))
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = "Contacto",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Descarga el juego del Diccionario")
                    intent.putExtra(Intent.EXTRA_TEXT, "¡Descarga el juego del Diccionario! \uD83D\uDCD9 https://github.com/SergioPerezFdez0/JuegoDiccionario")

                    ContextCompat.startActivity(
                        mContext,
                        Intent.createChooser(intent, "ShareWith"),
                        null
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = "Compartir",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}
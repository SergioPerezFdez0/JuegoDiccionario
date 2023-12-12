package es.sergiop.diccionarioapp

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.sergiop.diccionarioapp.ui.theme.DiccionarioAppTheme
import es.sergiop.diccionarioapp.views.SlideShow

class ComoJugarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiccionarioAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting2()
                }
            }
        }
    }
}

@SuppressLint("ResourceType")
@Composable
fun Greeting2() {
    val scrollState = rememberScrollState()
    val activity = (LocalContext.current as? Activity)
    var currentIndex by remember { mutableIntStateOf(0) }

    val slideTexts: List<String> = listOf(
        "¿Quieres saber cómo jugar al Juego del Diccionario? Pulsa sobre los botones de abajo",
        "El juego se desarrolla de la siguiente manera: una persona designada como \"la Madre\" elige una palabra del diccionario (El cual proporcionamos, pero incluimos la opción para jugarlo con diccionario en físico).",
        "Luego, dicta la palabra escogida en voz alta, y a continuación, el resto de los participantes proceden a redactar una definición para dicha palabra.",
        "Los jugadores tienen como objetivo engañar a los demás jugadores y hacerlos creer que su definición es la correcta.",
        "Una vez todos los jugadores han terminado de escribir sus definiciones, la Madre procede a leer en voz alta todas las definiciones.",
        "Después de que la Madre haya concluido la lectura, cada jugador emite su voto para indicar cuál de las definiciones considera que es la correcta.",
        "En términos de puntuación, cada jugador recibe un punto si acierta la definición correcta, un punto adicional por cada vez que otro jugador vota por su definición,",
        "y dos puntos si su definición es idéntica o muy similar a la definición original (Si la palabra es polisémica, se aplica a cualquier definición).",
        "Puedes votarte a ti mismo, pero no recibirás puntos por ello.",
        "Al final del juego, gana el jugador con la puntuación más alta.",
        "¿Qué son las mesas? Las mesas son una forma de llamar a los grupos, cada una se compone de un código de cuatro números, y puedes crear tu propia sala o unirte a una ya creada introduciendo el código después de pulsar sobre \"Unirse a mesa\".",
        "Si creas una Mesa, podrás configurar ciertos aspectos de la partida, como el máximo de rondas o de tiempo, si la Madre cambia cada ronda de persona e incluso botones para expulsar a jugadores o convertir a un jugador en la Madre.",
        "¡Ya sabes jugar al Juego del Diccionario!"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState)
            .background(MaterialTheme.colorScheme.inversePrimary)
    ){
        AnimatedContent(targetState = currentIndex, label = ""){ targetCount ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ){
                Text(
                    text = when (targetCount){
                        0 -> "Cómo jugar"
                        1 -> "El inicio"
                        2 -> "El juego"
                        3 -> "El objetivo"
                        4 -> "La mejor parte"
                        5 -> "Votaciones"
                        6 -> "Cómo se puntúa"
                        7 -> "Clavar una definición"
                        8 -> "Autovoto"
                        9 -> "Cómo se gana"
                        10 -> "Sobre las Mesas"
                        11 -> "Opciones de la Mesa"
                        12 -> "¡Ya sabes jugar!"
                        else -> "Error"
                    },
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                )

                Box(
                    modifier = Modifier
                        .padding(0.dp, 10.dp, 0 .dp, 0.dp)
                        .size(290.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(20.dp))
                        .align(Alignment.CenterHorizontally)
                        .wrapContentSize(Alignment.Center)

                ){
                    Image(
                        painter = when(targetCount){
                            0 -> painterResource(id = R.raw.logonobg)
                            1 -> painterResource(id = R.raw.diccionario)
                            2 -> painterResource(id = R.raw.aescribir)
                            3 -> painterResource(id = R.raw.escrito)
                            4 -> painterResource(id = R.raw.leyendo)
                            5 -> painterResource(id = R.raw.votacion)
                            6 -> painterResource(id = R.raw.punto)
                            7 -> painterResource(id = R.raw.clavada)
                            8 -> painterResource(id = R.raw.votacion)
                            9 -> painterResource(id = R.raw.resultados)
                            10 -> painterResource(id = R.raw.mesa)
                            11 -> painterResource(id = R.raw.config)
                            12 -> painterResource(id = R.raw.logonobg)
                            else -> painterResource(id = R.raw.logonobg)
                        },
                        contentDescription = "Imagen",
                        modifier = Modifier
                            .size(285.dp)
                            .padding(0.dp, 10.dp, 0 .dp, 0.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            }
        }
        SlideShow(
            slideText = slideTexts,
            currentIndex = currentIndex,
            onPreviousClick = { if (currentIndex > 0) currentIndex-- },
            onNextClick = { if (currentIndex < slideTexts.size - 1) currentIndex++ }
        )

        Button(
            onClick = {
                activity?.finish()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier
                .padding(top = 10.dp)
        ) {
            Text(
                text = "Volver",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}
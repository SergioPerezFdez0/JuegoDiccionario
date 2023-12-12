package es.sergiop.diccionarioapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import es.sergiop.diccionarioapp.ui.theme.DiccionarioAppTheme
import es.sergiop.diccionarioapp.views.AlertDialog
import es.sergiop.diccionarioapp.views.BasicCountdownTimer
import es.sergiop.diccionarioapp.views.SlideShow

class JuegoActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiccionarioAppTheme {

                val db = Firebase.firestore
                val mContext = LocalContext.current
                val activity = (LocalContext.current as? Activity)
                val playerName = intent.getStringExtra("playerName")
                val codigoMesa = intent.getStringExtra("codigoMesa")!!
                val openAlertDialog = remember { mutableStateOf(false) }
                val openWordDialog = remember { mutableStateOf(false) }
                val sendWordDialog = remember { mutableStateOf(false) }
                val voteWordDialog = remember { mutableStateOf(false) }
                val openClavadoActivoDialog = remember { mutableStateOf(false) }
                val finalizarDialog = remember { mutableStateOf(false) }
                var palabraSeleccionada by remember { mutableStateOf("") }
                var definicionSeleccionada by remember { mutableStateOf("") }
                var jugadores by remember { mutableStateOf(emptyMap<String, Map<String, Any>>()) }
                var jugadoresExtra by remember { mutableStateOf(emptyMap<String, Map<String, Any>>()) }
                val jugadoresListPuntuacion = remember { mutableStateOf(emptyList<Pair<String, Long>>()) }
                var isTextFieldFocused by remember { mutableStateOf(false ) }
                val keyboardController = LocalSoftwareKeyboardController.current
                val focusRequester = remember { FocusRequester() }
                var jugadorDefinicionEscrita by remember { mutableStateOf("") }
                var currentIndex by remember { mutableIntStateOf(0) }
                var currentlyVoting by remember { mutableIntStateOf(0) }
                var alreadyVoted by remember { mutableStateOf(false) }
                var slideTexts by remember { mutableStateOf(emptyList<String>()) }
                var jugadoresListAleatoria by remember { mutableStateOf(emptyList<String>()) }
                var puntuacionesCount by remember { mutableIntStateOf(0) }
                var puntuacionesCount2 by remember { mutableIntStateOf(0) }
                var definicionPosicionMadre by remember { mutableIntStateOf(0) }
                var jugadorSeleccionado by remember { mutableStateOf("") }
                var clavadoActivo by remember { mutableStateOf(false) }
                var letraSeleccionada by remember { mutableStateOf("a") }
                var jugadorPuntuacionPreResultado by remember { mutableLongStateOf(0) }
                var checkedDiccionarioFisico by remember { mutableStateOf(false) }
                var campoTextoPalabra by remember { mutableStateOf("") }
                var campoTextoDefinicion by remember { mutableStateOf("") }

                //Cambian cada ronda
                var palabraRonda by remember { mutableStateOf("") }
                var numeroRonda by remember { mutableLongStateOf(0) }

                //Condiciones fijas.
                var cambiarMadre by remember { mutableLongStateOf(0) }
                var rondasMax by remember { mutableLongStateOf(0) }
                var maxTiempo by remember { mutableLongStateOf(0) }

                //Condiciones de interfaces
                var partidaHasStarted by remember { mutableStateOf(false) }
                var partidaIsWriting by remember { mutableStateOf(false) }
                var partidaHasEnded by remember { mutableStateOf(false) }
                var isMadreLeyendo by remember { mutableStateOf(false) }
                var isMadreEscogiendo by remember { mutableStateOf(false) }
                var rondaResults by remember { mutableStateOf(false) }

                //Datos del jugador
                var jugadorPuntuacion by remember { mutableLongStateOf(0) }
                var jugadorIsMadre by remember { mutableStateOf(false) }
                var jugadorDefinicion by remember { mutableStateOf("") }
                var jugadorDefinicionEnviada by remember { mutableStateOf(false) }
                var jugadorVotado by remember { mutableLongStateOf(0)}

                val mesasRef = db.collection("mesas").document(codigoMesa)

                mesasRef.get().addOnSuccessListener { document ->
                    //Añade las condiciones de la partida que no pueden ser alterados
                    if (document!= null) {
                        val data = document.data
                        if (data!= null) {
                            cambiarMadre = data["cambiarMadre"] as Long
                            rondasMax = data["rondasMax"] as Long
                            maxTiempo = data["maxTiempo"] as Long
                        }
                    }
                }

                val listenerRegistration = mesasRef.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("listenerJuegoActivity", "Failed with ${e.message}.")
                        return@addSnapshotListener
                    }

                    val data = snapshot?.data
                    if (data!= null) {
                        Log.d("listenerJuegoActivity", "Data: $data")

                        palabraRonda = data["palabraRonda"] as String
                        numeroRonda = data["numeroRonda"] as Long
                        partidaHasEnded = data["partidaHasEnded"] as Boolean
                        isMadreLeyendo = data["isMadreLeyendo"] as Boolean
                        isMadreEscogiendo = data["isMadreEscogiendo"] as Boolean
                        partidaHasStarted = data["partidaHasStarted"] as Boolean
                        partidaIsWriting = data["partidaIsWriting"] as Boolean
                        rondaResults = data["rondaResults"] as Boolean

                        jugadoresListPuntuacion.value = emptyList()

                        //Cogiendo datos para ESTE jugador
                        jugadores = data["jugadores"] as? HashMap<String, Map<String, Any>> ?: hashMapOf()
                        jugadoresExtra = data["jugadoresExtra"] as? HashMap<String, Map<String, Any>> ?: hashMapOf()

                        if (jugadores[playerName] == null) {
                            Toast.makeText(mContext, "No se ha encontrado el jugador $playerName", Toast.LENGTH_LONG).show()
                            activity?.finish() //No funciona?
                        }

                        jugadores.let {
                            jugadoresListPuntuacion.value = jugadores.map { (nombre, datos) ->
                                nombre to (datos["puntuacion"] as Long)
                            }
                        }

                        if (jugadores[playerName] != null) {
                            val jugador = jugadores[playerName] as Map<String, Any>
                            jugadorPuntuacion = jugador["puntuacion"] as Long
                            jugadorIsMadre = jugador["isMadre"] as Boolean
                            jugadorDefinicion = jugador["definicion"] as String
                            jugadorDefinicionEnviada = jugador["definicionEnviada"] as Boolean
                            jugadorVotado = jugador["votado"] as Long
                        } else {
                            Toast.makeText(mContext, "No se ha encontrado el jugador $playerName", Toast.LENGTH_LONG).show()
                            activity?.finish()
                        }
                    } else {
                        jugadoresListPuntuacion.value = emptyList()
                        activity?.finish()
                    }
                }

                BackHandler {
                    Log.d("JuegoActivity", "Back pressed")
                    openAlertDialog.value = true
                }

                AlertDialog(
                    icon = Icons.Default.Home,
                    alertTitle = "Volver",
                    alertText = "¿Está seguro de que desea salir? Sus datos de la partida serán registrados",
                    openDialog = openAlertDialog,
                    onDismissRequest = { openAlertDialog.value = false },
                    onConfirmation = {
                        openAlertDialog.value = false
                        borrarJugadorYPonerExtra(mesasRef, playerName, jugadorPuntuacion, mContext, codigoMesa, listenerRegistration, activity)
                    }
                )

                /**
                 * Vista para los jugadores cuando la partida ya ha empezado
                 */
                if(partidaHasStarted) {

                    /**
                     * Vista para la madre cuando la ronda ha terminado
                     * RESULTS 1/2
                     */
                    if (rondaResults and jugadorIsMadre){

                        definicionSeleccionada = ""

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.inversePrimary)
                        ) {
                            if(rondasMax.toInt() != 0){
                                Text(
                                    text = "RONDA $numeroRonda DE $rondasMax",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            } else{
                                Text(
                                    text = "RONDA $numeroRonda",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            }

                            jugadores.forEach { (jugador, datos) ->
                                val votado = datos["votado"] as Long
                                if (puntuacionesCount2 < jugadores.size) {
                                    puntuacionesCount2++
                                    if (votado > 0) {
                                         //Para dar puntos al jugador que ha escrito otra definicion en caso de haberle votado
                                        val jugadorQueSuma = jugadoresListAleatoria[votado.toInt() - 1]
                                        val jugadorQueSumaIsMadre = jugadores[jugadorQueSuma]?.get("isMadre") as Boolean
                                        val jugadorSeHaAutoVotado = jugadorQueSuma == jugador
                                        if(!jugadorSeHaAutoVotado and !jugadorQueSumaIsMadre){
                                            val puntuacionJugadorQueSuma = jugadores[jugadorQueSuma]?.get("puntuacion") as Long
                                            mesasRef.update("jugadores.$jugadorQueSuma.puntuacion", puntuacionJugadorQueSuma+1)
                                        }
                                    }
                                }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                items(jugadoresListPuntuacion.value) { (jugador, puntuacion) ->
                                    if(jugador != playerName){
                                        Column(
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    jugadorSeleccionado = jugador
                                                    if (clavadoActivo) {
                                                        openClavadoActivoDialog.value = true
                                                    }
                                                }
                                        ) {
                                            Text(
                                                text = "$jugador: $puntuacion punto(s)",
                                                style = MaterialTheme.typography.displaySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier
                                                    .padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    clavadoActivo = !clavadoActivo
                                    Toast.makeText(mContext, "Toca al jugador cuya definición sea correcta", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (clavadoActivo) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier
                                        .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = "Definición clavada",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = if (clavadoActivo) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Button(
                                onClick = {
                                    if(rondasMax.toInt() != 0){
                                        if(numeroRonda == rondasMax){
                                            mesasRef.update("rondaResults", false)
                                            mesasRef.update("partidaHasStarted", false)
                                            mesasRef.update("partidaHasEnded", true)
                                        } else{
                                            puntuacionesCount = 0
                                            puntuacionesCount2 = 0
                                            currentIndex = 0
                                            mesasRef.update("numeroRonda", numeroRonda+1)
                                            mesasRef.update("rondaResults", false)
                                            mesasRef.update("isMadreEscogiendo", true)
                                            jugadores.forEach { (jugador, datos) ->
                                                mesasRef.update("jugadores.$jugador.definicion", "")
                                                mesasRef.update("jugadores.$jugador.definicionEnviada", false)
                                                mesasRef.update("jugadores.$jugador.votado", 0)
                                            }
                                            if(cambiarMadre.toInt() == 2){ //Madre aleatoria cada ronda
                                                val jugadoresList = jugadores.keys.toList()
                                                val jugadorAleatorio = jugadoresList[(jugadoresList.indices).random()]
                                                mesasRef.update("jugadores.$playerName.isMadre", false)
                                                mesasRef.update("jugadores.$jugadorAleatorio.isMadre", true)
                                            }
                                        }
                                    } else{
                                        puntuacionesCount = 0
                                        puntuacionesCount2 = 0
                                        currentIndex = 0
                                        mesasRef.update("numeroRonda", numeroRonda+1)
                                        mesasRef.update("rondaResults", false)
                                        mesasRef.update("isMadreEscogiendo", true)
                                        jugadores.forEach { (jugador, datos) ->
                                            mesasRef.update("jugadores.$jugador.definicion", "")
                                            mesasRef.update("jugadores.$jugador.definicionEnviada", false)
                                            mesasRef.update("jugadores.$jugador.votado", 0)
                                        }
                                        if(cambiarMadre.toInt() == 2){ //Madre aleatoria cada ronda
                                            val jugadoresList = jugadores.keys.toList()
                                            val jugadorAleatorio = jugadoresList[(jugadoresList.indices).random()]
                                            mesasRef.update("jugadores.$playerName.isMadre", false)
                                            mesasRef.update("jugadores.$jugadorAleatorio.isMadre", true)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier
                                    .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = "Continuar",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Button(
                                onClick = {
                                    finalizarDialog.value = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .wrapContentSize(Alignment.Center)
                            ) {
                                Text(
                                    text = "Finalizar partida",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }

                            AlertDialog(
                                icon = Icons.Default.Star,
                                alertTitle = "¿El jugador \"$jugadorSeleccionado\" ha clavado la definición?",
                                alertText = "Consúltelo con el resto de jugadores y de ser cierto, el jugador \"$jugadorSeleccionado\" sumará 2 puntos",
                                openDialog = openClavadoActivoDialog,
                                onDismissRequest = {
                                    openClavadoActivoDialog.value = false
                                    clavadoActivo = false
                                },
                                onConfirmation = {
                                    openClavadoActivoDialog.value = false
                                    clavadoActivo = false

                                    val puntuacionJugadorSeleccionado = jugadores[jugadorSeleccionado]?.get("puntuacion") as Long
                                    mesasRef.update("jugadores.$jugadorSeleccionado.puntuacion", puntuacionJugadorSeleccionado+2)
                                }
                            )

                            AlertDialog(
                                icon = Icons.Default.ExitToApp,
                                alertTitle = "¿Finalizar partida?",
                                alertText = "Se motrará la tabla de puntuaciones de los jugadores",
                                openDialog = finalizarDialog,
                                onDismissRequest = { finalizarDialog.value = false },
                                onConfirmation = {
                                    finalizarDialog.value = false
                                    mesasRef.update("rondaResults", false)
                                    mesasRef.update("partidaHasStarted", false)
                                    mesasRef.update("partidaHasEnded", true)
                                }
                            )
                        }
                    }

                    /**
                     * Vista para los jugadores cuando la ronda ha terminado
                     * RESULTS 2/2
                     */
                    else if (rondaResults and !jugadorIsMadre){
                        jugadorDefinicionEscrita = ""
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.inversePrimary)
                        ) {
                            if(rondasMax.toInt() != 0){
                                Text(
                                    text = "RONDA $numeroRonda DE $rondasMax",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            } else{
                                Text(
                                    text = "RONDA $numeroRonda",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            }
                            val puntuacionRonda = jugadorPuntuacion - jugadorPuntuacionPreResultado
                            if(puntuacionRonda > 0){
                                if(puntuacionRonda >= 3){
                                    Text(
                                        text = "✨\nWow, has ganado $puntuacionRonda puntos esta ronda. Impresionante",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.displayLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .padding(20.dp, 20.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(20.dp, 10.dp)
                                    )
                                }
                                else{
                                    Text(
                                        text = "✅\nHas ganado $puntuacionRonda punto(s) esta ronda",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.displayLarge,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .padding(20.dp, 20.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(20.dp, 10.dp)
                                    )
                                }
                            } else{
                                Text(
                                    text = "❌\nNo has ganado puntos esta ronda",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(20.dp, 20.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            }
                            val jugadorMadre = jugadores.filterValues { it["isMadre"] == true }.keys.toList()[0]
                            val definicionMadre = jugadores[jugadorMadre]?.get("definicion") as String
                            Text(
                                text = "La definición correcta era: \n\"$definicionMadre\"",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.displaySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(0.dp, 20.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(20.dp, 10.dp)
                            )
                        }
                    }

                    /**
                     * Vista para la madre cuando la madre está leyendo las definiciones
                     * LEYENDO 1/2
                     */
                    else if (isMadreLeyendo and jugadorIsMadre) {
                        jugadorSeleccionado = ""
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.inversePrimary)
                        ){
                            if(rondasMax.toInt() != 0){
                                Text(
                                    text = "RONDA $numeroRonda DE $rondasMax",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .padding(0.dp, 20.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            } else{
                                Text(
                                    text = "RONDA $numeroRonda",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .padding(0.dp, 20.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            }
                            Text(
                                text = "Definición ${currentIndex+1}:",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.displayLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(0.dp, 20.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(20.dp, 0.dp)
                            )

                            if (jugadoresListAleatoria.isEmpty()) {
                                jugadoresListAleatoria = jugadores.keys.toList().shuffled()
                            }
                            slideTexts = jugadoresListAleatoria.map { jugador ->
                                val definicion = jugadores[jugador]?.get("definicion") as String
                                definicion
                            }

                            definicionPosicionMadre = slideTexts.indexOf(jugadorDefinicion)

                            SlideShow(
                                slideText = slideTexts,
                                currentIndex = currentIndex,
                                onPreviousClick = { if (currentIndex > 0) currentIndex-- },
                                onNextClick = { if (currentIndex < slideTexts.size - 1) currentIndex++ }
                            )

                            var count = 0
                            jugadores.forEach { (jugador, datos) ->
                                val votados = datos["votado"] as Long
                                if(jugador != playerName) {
                                    if (votados > 0) {
                                        count++
                                    }
                                } else {
                                    count++
                                }
                            }
                            if(count == jugadores.size) { //Cuando ya han votado todos

                                jugadores.forEach { (jugador, datos) ->
                                    val votado = datos["votado"] as Long
                                    if (puntuacionesCount < jugadores.size) {
                                        puntuacionesCount++
                                        if (votado > 0) {
                                            if ((votado.toInt() -1 ) == definicionPosicionMadre){
                                                val puntuacionJugador = jugadores[jugador]?.get("puntuacion") as Long
                                                mesasRef.update("jugadores.$jugador.puntuacion", puntuacionJugador+1)
                                            }
                                        }
                                    }
                                }

                                mesasRef.update("isMadreLeyendo", false)
                                mesasRef.update("rondaResults", true)
                            }
                        }
                    }

                    /**
                     * Vista para los jugadores cuando la madre está leyendo las definiciones
                     * LEYENDO 2/2
                     */
                    else if (isMadreLeyendo and !jugadorIsMadre) {
                        if (!alreadyVoted){
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.inversePrimary)
                            ){
                                if(rondasMax.toInt() != 0){
                                    Text(
                                        text = "RONDA $numeroRonda DE $rondasMax",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.displayLarge,
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(20.dp, 10.dp)
                                    )
                                } else{
                                    Text(
                                        text = "RONDA $numeroRonda",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.displayLarge,
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(20.dp, 10.dp)
                                    )
                                }
                                Text(
                                    text = "VOTACIONES",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(0.dp, 20.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                                LazyColumn(
                                    modifier = Modifier
                                        .padding(16.dp)
                                ) {
                                    val size = jugadores.size
                                    items(size) { i ->
                                        Column(
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    currentlyVoting = i + 1
                                                    voteWordDialog.value = true
                                                }
                                        ) {
                                            Text(
                                                text = "Definición ${i+1}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier
                                                    .padding(8.dp)
                                            )
                                        }
                                    }
                                }
                                AlertDialog(
                                    icon = Icons.Default.Check,
                                    alertTitle = "Votación",
                                    alertText = "¿Quieres votar a la definición $currentlyVoting para la palabra: $palabraRonda?",
                                    openDialog = voteWordDialog,
                                    onDismissRequest = { voteWordDialog.value = false },
                                    onConfirmation = {
                                        voteWordDialog.value = false
                                        alreadyVoted = true
                                        mesasRef.update("jugadores.$playerName.votado", currentlyVoting)
                                    }
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.inversePrimary)
                            ) {
                                Text(
                                    text = "Voto registrado\nEsperando al resto de jugadores",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            }
                        }
                    }

                    /**
                     * Vista para los jugadores cuando están escribiendo sus definiciones
                     * WRITING 1/3
                     */
                    else if(partidaIsWriting and !jugadorIsMadre and !jugadorDefinicionEnviada) {
                        jugadorPuntuacionPreResultado = jugadorPuntuacion
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.inversePrimary)
                                .padding(20.dp, 0.dp, 0.dp, 0.dp)
                        ){
                            if(rondasMax.toInt() != 0){
                                Text(
                                    text = "RONDA $numeroRonda DE $rondasMax",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            } else{
                                Text(
                                    text = "RONDA $numeroRonda",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            }
                            Text(
                                text = "La palabra escogida es \n\" $palabraRonda \"",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.displayMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(0.dp, 20.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(20.dp, 10.dp)
                            )
                            if(maxTiempo.toInt() != 0){
                                BasicCountdownTimer(maxTiempo, onTimerFinish = {
                                    mesasRef.update("jugadores.$playerName.definicion", jugadorDefinicionEscrita)
                                    mesasRef.update("jugadores.$playerName.definicionEnviada", true)
                                })
                            }
                            Box(
                                modifier = Modifier
                                    .padding(20.dp, 20.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                            ) {
                                BasicTextField(
                                    value = jugadorDefinicionEscrita,
                                    onValueChange = { it ->
                                        jugadorDefinicionEscrita = it.filterNot { it == '\n' }
                                    },
                                    textStyle = MaterialTheme.typography.displaySmall,
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            isTextFieldFocused = false
                                            keyboardController?.hide()
                                        }
                                    ),
                                    modifier = Modifier
                                        .padding(15.dp)
                                        .background(MaterialTheme.colorScheme.background)
                                        .focusRequester(focusRequester)
                                        .size(200.dp, 140.dp)
                                )
                            }

                            Button(onClick = {
                                sendWordDialog.value = true
                            },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            ) {
                                Text(
                                    text = "Enviar",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            AlertDialog(
                                icon = Icons.Default.Edit,
                                alertTitle = "¿Quieres mandar como definición '$jugadorDefinicionEscrita' para la palabra: $palabraRonda?",
                                alertText = "$definicionSeleccionada No podrás cambiarla",
                                openDialog = sendWordDialog,
                                onDismissRequest = { sendWordDialog.value = false },
                                onConfirmation = {
                                    sendWordDialog.value = false
                                    jugadorDefinicionEnviada = true

                                    mesasRef.update("jugadores.$playerName.definicion", jugadorDefinicionEscrita)
                                    mesasRef.update("jugadores.$playerName.definicionEnviada", true)
                                }
                            )
                        }
                    }

                    /**
                     * Vista para los jugadores cuando ya han escrito sus definiciones
                     * WRITING 2/3
                     */
                    else if(partidaIsWriting and !jugadorIsMadre and jugadorDefinicionEnviada) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.inversePrimary)
                        ) {
                            if(rondasMax.toInt() != 0){
                                Text(
                                    text = "RONDA $numeroRonda DE $rondasMax",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            } else{
                                Text(
                                    text = "RONDA $numeroRonda",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            }
                            Text(
                                text = "DEFINICION ENVIADA. Esperando al resto de jugadores",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.displayMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(0.dp, 20.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(20.dp, 10.dp)
                            )
                        }
                    }

                    /**
                     * Vista para la madre cuando los jugadores están escribiendo sus definiciones
                     * WRITING 3/3
                     */
                    else if(partidaIsWriting and jugadorIsMadre) {
                        jugadoresListAleatoria = emptyList()
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.inversePrimary)
                        ){
                            if(rondasMax.toInt() != 0){
                                Text(
                                    text = "RONDA $numeroRonda DE $rondasMax",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            } else{
                                Text(
                                    text = "RONDA $numeroRonda",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            }
                            Text(
                                text = "Los jugadores están escribiendo su definición de la palabra $palabraSeleccionada",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.displaySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(20.dp, 10.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(10.dp, 10.dp)
                            )
                            if(maxTiempo.toInt() != 0){
                                BasicCountdownTimer(maxTiempo, onTimerFinish = {
                                    mesasRef.update("partidaIsWriting", false)
                                    mesasRef.update("isMadreLeyendo", true)
                                })
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                items(jugadores.keys.toList()) { jugador ->
                                    if(jugador != playerName) { //Muestra solo a los que no son la madre
                                        val definicionEnviada = jugadores[jugador]?.get("definicionEnviada") as Boolean
                                        Column(
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .fillMaxWidth()
                                                .background(if (definicionEnviada) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer)
                                                .clip(RoundedCornerShape(10.dp))
                                        ) {
                                            Text(
                                                text = jugador,
                                                style = MaterialTheme.typography.displaySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier
                                                    .padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            var count = 0
                            jugadores.forEach { (jugador, datos) ->
                                val definicion = datos["definicion"] as String

                                if (jugador != playerName){
                                    if (definicion != "") {
                                        count++
                                        if(count == jugadores.size) {
                                            mesasRef.update("partidaIsWriting", false)
                                            mesasRef.update("isMadreLeyendo", true)
                                        }
                                    }
                                } else {
                                    count++
                                    if(count == jugadores.size) {
                                        mesasRef.update("partidaIsWriting", false)
                                        mesasRef.update("isMadreLeyendo", true)
                                    }
                                }
                            }
                        }
                    }

                    /**
                     * Vista para la madre cuando tiene que escoger una palabra
                     * ESCOGIENDO 1/2
                     */
                    else if(isMadreEscogiendo and jugadorIsMadre){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.inversePrimary)
                        ){
                            if(rondasMax.toInt() != 0){
                                Text(
                                    text = "RONDA $numeroRonda DE $rondasMax",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            } else{
                                Text(
                                    text = "RONDA $numeroRonda",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Usar diccionario físico",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displaySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(16.dp, 15.dp)
                                )
                                Switch(
                                    checked = checkedDiccionarioFisico,
                                    onCheckedChange = {
                                        checkedDiccionarioFisico = it
                                    },
                                    modifier = Modifier
                                        .padding(16.dp, 0.dp),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                                    ),
                                    thumbContent = if (checkedDiccionarioFisico) {
                                        {
                                            Icon(
                                                painter = painterResource(R.drawable.baseline_check_24),
                                                contentDescription = "Usar diccionario físico",
                                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                            )
                                        }
                                    } else {
                                        {
                                            Icon(
                                                painter = painterResource(R.drawable.baseline_clear_24),
                                                contentDescription = "Usar diccionario digital",
                                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                            )
                                        }
                                    }
                                )
                            }

                            if (checkedDiccionarioFisico){
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .size(300.dp, 550.dp)
                                        .background(MaterialTheme.colorScheme.inversePrimary)
                                ){
                                    Box(
                                        modifier = Modifier
                                            .size(300.dp, 160.dp)
                                            .padding(20.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Palabra:",
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                style = MaterialTheme.typography.displayMedium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .padding(top = 10.dp)
                                            )

                                            BasicTextField(
                                                value = campoTextoPalabra,
                                                onValueChange = { it ->
                                                    campoTextoPalabra = it.filterNot { it == '\n' }
                                                },
                                                textStyle = MaterialTheme.typography.displaySmall,
                                                keyboardOptions = KeyboardOptions.Default.copy(
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onDone = {
                                                        isTextFieldFocused = false
                                                        keyboardController?.hide()
                                                    }
                                                ),
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .background(MaterialTheme.colorScheme.background)
                                                    .focusRequester(focusRequester)
                                                    .size(200.dp, 55.dp)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(300.dp, 160.dp)
                                            .padding(20.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Definición:",
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                style = MaterialTheme.typography.displayMedium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .padding(top = 10.dp)
                                            )

                                            BasicTextField(
                                                value = campoTextoDefinicion,
                                                onValueChange = { it ->
                                                    campoTextoDefinicion = it.filterNot { it == '\n' }
                                                },
                                                textStyle = MaterialTheme.typography.displaySmall,
                                                keyboardOptions = KeyboardOptions.Default.copy(
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(
                                                    onDone = {
                                                        isTextFieldFocused = false
                                                        keyboardController?.hide()
                                                    }
                                                ),
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .background(MaterialTheme.colorScheme.background)
                                                    .focusRequester(focusRequester)
                                                    .size(200.dp, 55.dp)
                                            )
                                        }
                                    }

                                    Button(onClick = {
                                        openWordDialog.value = true
                                        palabraSeleccionada = campoTextoPalabra
                                        definicionSeleccionada = campoTextoDefinicion
                                    },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    ) {
                                        Text(
                                            text = "Enviar",
                                            style = MaterialTheme.typography.displaySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                            } else {
                                val alphabet = listOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'ñ', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')

                                Box (
                                    modifier = Modifier
                                        .size(350.dp, 550.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(5.dp)
                                ) {
                                    Column {
                                        LazyRow(
                                            modifier = Modifier
                                                .padding(5.dp)
                                        ) {
                                            items(alphabet) { letter ->
                                                Box(
                                                    modifier = Modifier
                                                        .padding(5.dp)
                                                        .size(40.dp, 40.dp)
                                                        .background(
                                                            color = MaterialTheme.colorScheme.primaryContainer,
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .wrapContentSize(Alignment.Center)
                                                        .clickable {
                                                            letraSeleccionada = letter.toString()
                                                        }
                                                ) {
                                                    Text(
                                                        text = letter.toString(),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        style = MaterialTheme.typography.displaySmall,
                                                        textAlign = TextAlign.Center,
                                                    )
                                                }
                                            }
                                        }

                                        val wordDefinitionsMap = cogerPalabras(letraSeleccionada, mContext)

                                        LazyVerticalGrid(
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .size(350.dp, 400.dp),
                                            columns = GridCells.Fixed(2)
                                        ) {
                                            items(wordDefinitionsMap.keys.toList()) { word ->
                                                Text(
                                                    text = word,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    style = MaterialTheme.typography.displaySmall,
                                                    modifier = Modifier
                                                        .clickable {
                                                            val definitions = wordDefinitionsMap[word] ?: emptyList()
                                                            openWordDialog.value = true
                                                            palabraSeleccionada = word
                                                            definicionSeleccionada = definitions.toString()
                                                        },
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                val randomWord = wordDefinitionsMap.keys.toList().shuffled()[0]
                                                val definitions = wordDefinitionsMap[randomWord] ?: emptyList()
                                                openWordDialog.value = true
                                                palabraSeleccionada = randomWord
                                                definicionSeleccionada = definitions.toString()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 10.dp)
                                                .wrapContentSize(Alignment.Center)
                                        ) {
                                            Text(
                                                text = "Aleatoria con la $letraSeleccionada",
                                                style = MaterialTheme.typography.displaySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            AlertDialog(
                                icon = Icons.Default.Create,
                                alertTitle = "¿Quiere usar la palabra '$palabraSeleccionada' para esta ronda?",
                                alertText = definicionSeleccionada,
                                openDialog = openWordDialog,
                                onDismissRequest = { openWordDialog.value = false },
                                onConfirmation = {
                                    openWordDialog.value = false
                                    mesasRef.update("palabraRonda", palabraSeleccionada)
                                    mesasRef.update("isMadreEscogiendo", false)
                                    mesasRef.update("partidaIsWriting", true)
                                    mesasRef.update("jugadores.$playerName.definicionEnviada", true)
                                    mesasRef.update("jugadores.$playerName.definicion", definicionSeleccionada)
                                }
                            )
                        }
                    }

                    /**
                     * Vista para los jugadores cuando la madre está eligiendo una palabra
                     * ESCOGIENDO 2/2
                     */
                    else if(isMadreEscogiendo and !(jugadorIsMadre)){
                        alreadyVoted = false
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.inversePrimary)
                            ) {
                                if(rondasMax.toInt() != 0){
                                    Text(
                                        text = "RONDA $numeroRonda DE $rondasMax",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.displayLarge,
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(20.dp, 10.dp)
                                    )
                                } else{
                                    Text(
                                        text = "RONDA $numeroRonda",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.displayLarge,
                                        modifier = Modifier
                                            .padding(0.dp, 50.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(20.dp, 10.dp)
                                    )
                                }

                                Text(
                                    text = "La Madre está seleccionando una palabra del diccionario",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(20.dp, 20.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(20.dp, 10.dp)
                                )

                                LazyColumn( //Muestra solo a los jugadores activos
                                    modifier = Modifier
                                        .padding(16.dp)
                                ) {
                                    items(jugadoresListPuntuacion.value) { (jugador, puntuacion) ->
                                        Box(
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .clip(RoundedCornerShape(10.dp))
                                        ) {
                                            Text(
                                                text = "$jugador: $puntuacion puntos",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier
                                                    .padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                /**
                 * Vista para los jugadores cuando la partida ya ha terminado
                 */
                else if(partidaHasEnded) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.inversePrimary)
                    ){
                        val jugadoresTotales = jugadores + jugadoresExtra
                        val jugadoresTotalesList = jugadoresTotales.toList()
                        val jugadoresTotalesListPuntuacion = jugadoresTotalesList.map { (nombre, datos) ->
                            nombre to (datos["puntuacion"] as Long)
                        }
                        val jugadoresTotalesListPuntuacionOrdenado = jugadoresTotalesListPuntuacion.sortedByDescending { (_, puntuacion) -> puntuacion }
                        Text(
                            text = "\uD83E\uDD47 El ganador es ${jugadoresTotalesListPuntuacionOrdenado[0].first} con ${jugadoresTotalesListPuntuacionOrdenado[0].second} puntos",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.displayLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(20.dp, 0.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp, 10.dp)
                        )

                        LazyColumn(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            itemsIndexed(jugadoresTotalesListPuntuacionOrdenado) { index, (jugador, puntuacion) ->
                                if (index != 0) {
                                    Column(
                                        modifier = Modifier
                                            .padding(3.dp)
                                            .fillMaxWidth()
                                            .background(
                                                when (index){
                                                    1 -> Color(0XFFC0C0C0)
                                                    2 -> Color(0XFFB87333)
                                                    else -> MaterialTheme.colorScheme.primaryContainer
                                                }
                                            )
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        Text(
                                            text = when (index){
                                                1 -> "🥈 $jugador: $puntuacion punto(s)"
                                                2 -> "🥉 $jugador: $puntuacion punto(s)"
                                                else -> "$jugador: $puntuacion punto(s)"
                                            },
                                            style = MaterialTheme.typography.displayMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier
                                                .padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
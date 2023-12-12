package es.sergiop.diccionarioapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import es.sergiop.diccionarioapp.ui.theme.DiccionarioAppTheme
import es.sergiop.diccionarioapp.views.AlertDialog

class MesaActivity() : ComponentActivity() {

    private var listenerRegistration: ListenerRegistration? = null

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {

        listenerRegistration?.remove()
        super.onCreate(savedInstanceState)
        val db = Firebase.firestore
        val playerName = intent.getStringExtra("playerName")
        val codigoMesa = intent.getStringExtra("codigoMesa")!!

        setContent {
            DiccionarioAppTheme {
                val mContext = LocalContext.current
                val activity = (LocalContext.current as? Activity)
                val openAlertDialog = remember { mutableStateOf(false) }
                val openExpulsarDialog = remember { mutableStateOf(false) }
                val openCambiarMadreDialog = remember { mutableStateOf(false) }
                var jugadorSeleccionado by remember { mutableStateOf("") }
                var expulsarActivo by remember { mutableStateOf(false) }
                var cambiarMadreActivo by remember { mutableStateOf(false) }
                var jugadores by remember { mutableStateOf(emptyMap<String, Map<String, Any>>()) }
                val jugadoresListState = remember { mutableStateOf(emptyList<Pair<String, Boolean>>()) }
                val jugadoresList by jugadoresListState
                var maxRondas by remember { mutableIntStateOf(0) }
                var maxTiempo by remember { mutableIntStateOf(0) }
                var tipoMadre by remember { mutableIntStateOf(0) } //0=elegida y no cambia, 1=aleatoria cada partida, 2=aleatoria cada ronda

                var configuracionIsActive by remember { mutableStateOf(false) }

                val mesasRef = db.collection("mesas").document(codigoMesa)
                listenerRegistration = mesasRef.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("listenerRegistrationMesaActivity", "Failed with ${e.message}.")
                        return@addSnapshotListener
                    }

                    val data = snapshot?.data
                    if (data!= null) {
                        Log.d("listenerRegistrationMesaActivity", "Data: $data")

                        jugadoresListState.value = emptyList()
                        jugadores = data["jugadores"] as? HashMap<String, Map<String, Any>> ?: hashMapOf()

                        if (jugadores[playerName] == null) {
                            activity?.finish() //No funciona?
                        }

                        jugadores.let {
                            jugadoresListState.value = jugadores.map { (nombre, datos) ->
                                nombre to (datos["isMadre"] as? Boolean ?: false)
                            }
                        }

                    } else {
                        jugadoresListState.value = emptyList()
                        Toast.makeText(mContext, "Mesa borrada", Toast.LENGTH_LONG).show()
                        activity?.finish()
                    }
                }

                if (!configuracionIsActive){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.inversePrimary)
                    ){
                        Text(
                            text = "Código de mesa: $codigoMesa",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp, 10.dp)
                        )

                        Button(
                            onClick = {
                                configuracionIsActive = !configuracionIsActive
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier
                                .padding(top = 10.dp)
                        ) {
                            Text(
                                text = "Configuración de la partida",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Button(onClick = {
                            if (jugadores.size < 2) {
                                Toast.makeText(mContext, "No hay suficientes jugadores", Toast.LENGTH_LONG).show()
                                return@Button
                            } else {
                                if (tipoMadre == 1) {
                                    val nuevaMadre = jugadoresList[(jugadoresList.indices).random()]
                                    val nuevaMadreNombre = nuevaMadre.first
                                    jugadores.forEach { (nombre, datos) ->
                                        if (datos["isMadre"] as? Boolean == true) {
                                            mesasRef.update("jugadores.$nombre.isMadre", false)
                                        }
                                    }
                                    mesasRef.update("jugadores.$nuevaMadreNombre.isMadre", true)
                                } else if (tipoMadre == 2) {
                                    val nuevaMadre = jugadoresList[(jugadoresList.indices).random()]
                                    val nuevaMadreNombre = nuevaMadre.first
                                    jugadores.forEach { (nombre, datos) ->
                                        if (datos["isMadre"] as? Boolean == true) {
                                            mesasRef.update("jugadores.$nombre.isMadre", false)
                                        }
                                    }
                                    mesasRef.update("jugadores.$nuevaMadreNombre.isMadre", true)
                                }

                                mesasRef.update("partidaHasStarted", true).addOnSuccessListener {
                                    mesasRef.update("isMadreEscogiendo", true)
                                    val intent = Intent(mContext, JuegoActivity::class.java)
                                    intent.putExtra("playerName", playerName)
                                    intent.putExtra("codigoMesa", codigoMesa)
                                    listenerRegistration!!.remove()
                                    mContext.startActivity(intent)
                                }
                            }
                        },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier
                                .padding(top = 10.dp)
                        ) {
                            Text(
                                text = "Empezar partida",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Button(onClick = {
                            listenerRegistration!!.remove()
                            expulsarActivo = !expulsarActivo
                            if(cambiarMadreActivo){
                                cambiarMadreActivo = !cambiarMadreActivo
                            }
                            if(expulsarActivo){
                                Toast.makeText(mContext, "Toca el nombre del jugador que quieres expulsar", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(mContext, "Modo expulsar desactivado", Toast.LENGTH_SHORT).show()
                            }
                        },
                            colors = ButtonDefaults.buttonColors(containerColor = if (expulsarActivo) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier
                                .padding(top = 10.dp)

                        ) {
                            Text(
                                text = "Expulsar jugador",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (tipoMadre == 0){
                            Button(onClick = {
                                listenerRegistration!!.remove()
                                cambiarMadreActivo = !cambiarMadreActivo
                                if(expulsarActivo){
                                    expulsarActivo = !expulsarActivo
                                }
                                if(cambiarMadreActivo){
                                    Toast.makeText(mContext, "Toca el nombre del jugador que va a ser la Madre", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(mContext, "Cambiar Madre desactivado", Toast.LENGTH_SHORT).show()
                                }
                            },
                                colors = ButtonDefaults.buttonColors(containerColor = if (cambiarMadreActivo) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                ) {
                                Text(
                                    text = "Cambiar Madre",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Button(
                            onClick = {
                                openAlertDialog.value = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier
                                .padding(top = 10.dp)
                        ) {
                            Text(
                                text = "Volver",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }

                        AlertDialog(
                            icon = Icons.Default.Info,
                            alertTitle = "Volver",
                            alertText = "¿Está seguro de que desea cerrar la sala?",
                            openDialog = openAlertDialog,
                            onDismissRequest = { openAlertDialog.value = false },
                            onConfirmation = {
                                openAlertDialog.value = false
                                db.collection("mesas").document(codigoMesa)
                                    .delete()
                                activity?.finish()
                            }
                        )

                        BackHandler {
                            openAlertDialog.value = true
                        }

                        LazyColumn(
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            items(jugadoresList) { (jugador, isMadre) ->
                                when (tipoMadre) {
                                    0 -> Box(
                                        modifier = Modifier
                                            .padding(3.dp)
                                            .fillMaxWidth()
                                            .background(if (isMadre) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                jugadorSeleccionado = jugador
                                                if (expulsarActivo) {
                                                    openExpulsarDialog.value = true
                                                } else if (cambiarMadreActivo) {
                                                    openCambiarMadreDialog.value = true
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = if (isMadre) ("$jugador (MADRE)") else jugador,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier
                                                    .padding(8.dp)
                                            )
                                        }
                                    1 -> Box(
                                        modifier = Modifier
                                            .padding(3.dp)
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                jugadorSeleccionado = jugador
                                                if (expulsarActivo) {
                                                    openExpulsarDialog.value = true
                                                } else if (cambiarMadreActivo) {
                                                    openCambiarMadreDialog.value = true
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = jugador,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier
                                                    .padding(8.dp)
                                            )
                                        }
                                    2 -> Box(
                                        modifier = Modifier
                                            .padding(3.dp)
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                jugadorSeleccionado = jugador
                                                if (expulsarActivo) {
                                                    openExpulsarDialog.value = true
                                                } else if (cambiarMadreActivo) {
                                                    openCambiarMadreDialog.value = true
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = jugador,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier
                                                    .padding(8.dp)
                                            )
                                        }
                                }
                            }
                        }

                        AlertDialog(
                            icon = Icons.Default.Delete,
                            alertTitle = "Expulsar al jugador: $jugadorSeleccionado",
                            alertText = "¿Está seguro de que desea expulsar al jugador: $jugadorSeleccionado?",
                            openDialog = openExpulsarDialog,
                            onDismissRequest = { openExpulsarDialog.value = false },
                            onConfirmation = {
                                openExpulsarDialog.value = false
                                expulsarActivo = false

                                if (jugadorSeleccionado == playerName) {
                                    Toast.makeText(mContext, "No puedes expulsarte a ti mismo", Toast.LENGTH_LONG).show()
                                }
                                else if (jugadores[jugadorSeleccionado]?.get("isMadre") == true) {
                                    Toast.makeText(mContext, "No puedes expulsar a la Madre", Toast.LENGTH_LONG).show()
                                }
                                else {
                                    mesasRef.get()
                                        .addOnSuccessListener { documentSnapshot ->
                                            if (documentSnapshot.exists()) {
                                                val data = documentSnapshot.data
                                                val jugadoresData = data?.get("jugadores") as? HashMap<String, Any> ?: hashMapOf()
                                                jugadoresData.remove(jugadorSeleccionado)
                                                data?.set("jugadores", jugadoresData)

                                                if (data != null) {
                                                    mesasRef.set(data)
                                                        .addOnSuccessListener {
                                                            Toast.makeText(mContext, "Jugador expulsado correctamente", Toast.LENGTH_LONG).show()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Toast.makeText(mContext, "Error al expulsar al jugador", Toast.LENGTH_LONG).show()
                                                        }
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(mContext, "Error al buscar la mesa", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        )

                        AlertDialog(
                            icon = Icons.Default.Person,
                            alertTitle = "Cambiar Madre",
                            alertText = "¿Desea dar el puesto de Madre a $jugadorSeleccionado?",
                            openDialog = openCambiarMadreDialog,
                            onDismissRequest = { openCambiarMadreDialog.value = false },
                            onConfirmation = {
                                openCambiarMadreDialog.value = false
                                cambiarMadreActivo = false

                                val jugadorActualIsMadre = jugadores[playerName]?.get("isMadre") as? Boolean
                                if (jugadorActualIsMadre!! and (jugadorSeleccionado == playerName)) {
                                    Toast.makeText(mContext, "Ya eres la Madre", Toast.LENGTH_SHORT).show()
                                } else {
                                    jugadores.forEach { (nombre, datos) ->
                                        if (datos["isMadre"] as? Boolean == true) {
                                            mesasRef.update("jugadores.$nombre.isMadre", false)
                                        }
                                    }
                                    mesasRef.update("jugadores.$jugadorSeleccionado.isMadre", true)
                                        .addOnSuccessListener {
                                            Toast.makeText(mContext, "Madre cambiada correctamente", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(mContext, "Error al cambiar la Madre", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        )
                    }
                } else{

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.inversePrimary)
                    ){
                        Text(
                            text = "Código de mesa: $codigoMesa",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(20.dp, 10.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(350.dp, 120.dp)
                                .padding(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ){
                            Row( //Row para el maximo de rondas
                                modifier = Modifier
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Button(
                                    onClick = {
                                        if(maxRondas != 0){
                                            maxRondas--
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                ) {
                                    Text(
                                        text = "-",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                if(maxRondas != 0){
                                    Text(
                                        text = "Número de rondas:\n$maxRondas",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.displaySmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .padding(10.dp,0.dp,10.dp,0.dp,)
                                    )
                                } else {
                                    Text(
                                        text = "Número de rondas:\nilimitadas",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.displaySmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .padding(10.dp,0.dp,10.dp,0.dp,)
                                    )
                                }
                                Button(
                                    onClick = {
                                        maxRondas++
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                ) {
                                    Text(
                                        text = "+",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(280.dp, 120.dp)
                                .padding(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ){
                            Row( //Row para el maximo de tiempo
                                modifier = Modifier
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = {
                                        if(maxTiempo != 0){
                                            maxTiempo -= 10
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                ) {
                                    Text(
                                        text = "-",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                if(maxTiempo != 0){
                                    Text(
                                        text = "Tiempo:\n$maxTiempo seg.",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.displaySmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .padding(10.dp,0.dp,10.dp,0.dp,)
                                    )
                                } else {
                                    Text(
                                        text = "Tiempo:\nilimitado ",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.displaySmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .padding(10.dp,0.dp,10.dp,0.dp,)
                                    )
                                }
                                Button(
                                    onClick = {
                                        maxTiempo += 10
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                ) {
                                    Text(
                                        text = "+",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(280.dp, 170.dp)
                                .padding(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ){
                            Row( //Row para el cambio de madre
                                modifier = Modifier
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp, 5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Button(
                                        onClick = {
                                            if(tipoMadre != 0){
                                                tipoMadre--
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),) {
                                        Text(
                                            text = "^",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if(tipoMadre != 2){
                                                tipoMadre++
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),) {
                                        Text(
                                            text = "v",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(200.dp, 120.dp)
                                ) {
                                    when(tipoMadre){
                                        0 -> Text(
                                            text = "La Madre no cambia",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            style = MaterialTheme.typography.displaySmall,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                        1 -> Text(
                                            text = "Aleatoria para la partida entera",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            style = MaterialTheme.typography.displaySmall,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                        2 -> Text(
                                            text = "Una Madre aleatoria cada ronda",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            style = MaterialTheme.typography.displaySmall,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "La madre puede terminar la partida antes de llegar al número máximo de rondas\nVotarte a ti mismo no otorgará puntos",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )

                        Button(onClick = {
                            mesasRef.update("rondasMax", maxRondas)
                            mesasRef.update("maxTiempo", maxTiempo)
                            mesasRef.update("cambiarMadre", tipoMadre)
                            configuracionIsActive = !configuracionIsActive
                        },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier
                                .padding(top = 10.dp)
                        ) {
                            Text(
                                text = "Guardar configuración",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Button(
                            onClick = {
                                configuracionIsActive = !configuracionIsActive
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier
                                .padding(top = 10.dp)
                        ) {
                            Text(
                                text = "Cancelar",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }
        }
    }
}
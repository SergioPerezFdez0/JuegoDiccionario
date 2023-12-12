package es.sergiop.diccionarioapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import es.sergiop.diccionarioapp.ui.theme.DiccionarioAppTheme
import es.sergiop.diccionarioapp.views.AlertDialog

class UnidoMesaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiccionarioAppTheme {
                val db = Firebase.firestore
                val mContext = LocalContext.current
                val activity = (LocalContext.current as? Activity)
                val playerName = intent.getStringExtra("playerName")
                val codigoMesa = intent.getStringExtra("codigoMesa")
                val jugadoresListState = remember { mutableStateOf(emptyList<Pair<String, Boolean>>()) }
                val openAlertDialog = remember { mutableStateOf(false) }
                var partidaHasStarted by remember { mutableStateOf(false) }
                var maxRondas by remember { mutableIntStateOf(0) }
                var maxTiempo by remember { mutableIntStateOf(0) }
                var tipoMadre by remember { mutableIntStateOf(0) } //0=elegida y no cambia, 1=aleatoria cada ronda, 2=aleatoria cada partida, 3=la elige la madre al final de cada ronda pero la primera es aleatoria

                val mesasRef = db.collection("mesas").document(codigoMesa!!)
                val listenerRegistration = mesasRef.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }

                    val data = snapshot?.data
                    if (data != null) {
                        partidaHasStarted = data["partidaHasStarted"] as Boolean
                        tipoMadre = (data["cambiarMadre"] as Long).toInt()
                        maxRondas = (data["rondasMax"] as Long).toInt()
                        maxTiempo = (data["maxTiempo"] as Long).toInt()

                        jugadoresListState.value = emptyList()
                        val jugadores = data["jugadores"] as? Map<String, Map<String, Any>>

                        if (jugadores?.get(playerName) == null) {
                            Toast.makeText(mContext, "No se ha encontrado el jugador $playerName", Toast.LENGTH_LONG).show()
                            activity?.finish()
                        }

                        jugadores?.let {
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

                if(partidaHasStarted){
                    val intent = Intent(mContext, JuegoActivity::class.java)
                    intent.putExtra("playerName", playerName)
                    intent.putExtra("codigoMesa", codigoMesa)
                    listenerRegistration.remove()
                    mContext.startActivity(intent)
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.inversePrimary)
                ){
                    Text(
                        text = "Lista de jugadores en\nla mesa $codigoMesa",
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

                    Text(
                        text = "Esperando al Host\npara iniciar el juego",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier
                            .padding(0.dp, 10.dp)
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
                        items(jugadoresListState.value) { (jugador, isMadre) ->
                            when (tipoMadre) {
                                0 -> Box(
                                    modifier = Modifier
                                        .padding(3.dp)
                                        .fillMaxWidth()
                                        .background(if (isMadre) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer)
                                        .clip(RoundedCornerShape(10.dp))
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

                    Box(
                        modifier = Modifier
                            .size(200.dp, 230.dp)
                            .padding(16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(20.dp)
                            )
                    ){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ){
                            Text(
                                text = "Datos de la partida:",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.displayMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(8.dp)
                            )
                            Text(
                                text = "Rondas: $maxRondas",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(8.dp)
                            )
                            Text(
                                text = "Tiempo: $maxTiempo segundos",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(8.dp)
                            )
                            when(tipoMadre){
                                0 -> Text(
                                    text = "La Madre no cambia",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(8.dp)
                                )
                                1 -> Text(
                                    text = "Una Madre aleatoria para\nla partida entera",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(8.dp)
                                )
                                2 -> Text(
                                    text = "Una Madre aleatoria cada ronda",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(8.dp)
                                )
                            }
                        }
                    }

                    AlertDialog(
                        icon = Icons.Default.Info,
                        alertTitle = "Volver",
                        alertText = "¿Está seguro de que desea volver? Se te eliminará de la mesa.",
                        openDialog = openAlertDialog,
                        onDismissRequest = { openAlertDialog.value = false },
                        onConfirmation = {
                            openAlertDialog.value = false
                            mesasRef.get().addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    val data = documentSnapshot.data
                                    val jugadoresData = data?.get("jugadores") as? HashMap<String, Any> ?: hashMapOf()

                                    jugadoresData.remove(playerName)

                                    data?.set("jugadores", jugadoresData)

                                    if (data != null) {
                                        mesasRef.set(data)
                                            .addOnSuccessListener {
                                                Toast.makeText(mContext, "Te has retirado de la mesa $codigoMesa", Toast.LENGTH_LONG).show()
                                                activity?.finish()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(mContext, "Error al eliminar al jugador", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                }
                            }
                                .addOnFailureListener {
                                    Toast.makeText(mContext, "Error al buscar la mesa", Toast.LENGTH_LONG).show()
                                }
                            activity?.finish()
                        }
                    )

                    BackHandler {
                        openAlertDialog.value = true
                    }
                }
            }
        }
    }
}
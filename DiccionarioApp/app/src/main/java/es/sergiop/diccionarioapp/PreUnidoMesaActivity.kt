package es.sergiop.diccionarioapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import es.sergiop.diccionarioapp.ui.theme.DiccionarioAppTheme

class PreUnidoMesaActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            setContent {
                DiccionarioAppTheme {
                    val mContext = LocalContext.current
                    val activity = (LocalContext.current as? Activity)

                    val db = Firebase.firestore

                    var playerName by remember { mutableStateOf("") }
                    var codigoMesa by remember { mutableStateOf("") }

                    var isTextFieldFocused by remember { mutableStateOf(false ) }
                    val keyboardController = LocalSoftwareKeyboardController.current
                    val focusRequester = remember { FocusRequester() }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.inversePrimary)
                    ){

                        Text(
                            text = "Unirse a Mesa",
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
                                    text = "Introduce tu nombre",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displaySmall,
                                    modifier = Modifier
                                        .padding(top = 20.dp)
                                )

                                BasicTextField(
                                    value = playerName,
                                    onValueChange = { it ->
                                        playerName = it.filterNot { it == '\n' }
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
                                        .clip(RoundedCornerShape(60.dp))
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
                                    text = "Código de Mesa",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displaySmall,
                                    modifier = Modifier
                                        .padding(top = 20.dp)
                                )

                                BasicTextField(
                                    value = codigoMesa,
                                    onValueChange = { it ->
                                        codigoMesa = it.filterNot { it == '\n' }
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
                                        .clip(RoundedCornerShape(60.dp))
                                        .padding(16.dp)
                                        .background(MaterialTheme.colorScheme.background)
                                        .focusRequester(focusRequester)
                                        .size(200.dp, 55.dp)
                                )
                            }
                        }

                        Button(onClick = {
                            if (playerName.isNotEmpty() and codigoMesa.isNotEmpty()) {
                                isTextFieldFocused = false
                                keyboardController?.hide()
                                val mesaRef = db.collection("mesas").document(codigoMesa)
                                mesaRef.get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        if (documentSnapshot.exists()) {

                                            val jugadorData = hashMapOf(
                                                playerName to hashMapOf(
                                                    "isMadre" to false,
                                                    "puntuacion" to 0,
                                                    "definicion" to "",
                                                    "definicionEnviada" to false,
                                                    "votado" to 0
                                                )
                                            )

                                            val data = documentSnapshot.data
                                            val jugadoresData = data?.get("jugadores") as? HashMap<String, Any> ?: hashMapOf()
                                            val partidaHasStarted = data?.get("partidaHasStarted") as? Boolean?
                                            val partidaHasEnded = data?.get("partidaHasEnded") as? Boolean?

                                            if (jugadoresData.keys.contains(playerName)) {
                                                Toast.makeText(mContext, "Ya hay un jugador con el mismo nombre en la mesa", Toast.LENGTH_LONG).show()
                                            } else if (jugadoresData.size >= 10) {
                                                Toast.makeText(mContext, "La mesa ya tiene el límite de 10 jugadores", Toast.LENGTH_LONG).show()
                                            } else if (partidaHasStarted!!){
                                                Toast.makeText(mContext, "La partida está en curso", Toast.LENGTH_LONG).show()
                                            } else if (partidaHasEnded!!){
                                                Toast.makeText(mContext, "La partida ha acabado", Toast.LENGTH_LONG).show()
                                            } else {
                                                jugadoresData.putAll(jugadorData)
                                                data["jugadores"] = jugadoresData

                                                mesaRef.set(data)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(mContext, "Te has unido a la mesa $codigoMesa", Toast.LENGTH_LONG).show()
                                                        val intent = Intent(mContext, UnidoMesaActivity::class.java)
                                                        intent.putExtra("playerName", playerName)
                                                        intent.putExtra("codigoMesa", codigoMesa)
                                                        mContext.startActivity(intent)
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Toast.makeText(mContext, "Error al agregar el jugador", Toast.LENGTH_LONG).show()
                                                    }
                                            }
                                        } else {
                                            Toast.makeText(mContext, "La sala con código $codigoMesa no existe", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(mContext, "Error al buscar la mesa", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(mContext, "Introduce un nombre y un código de mesa", Toast.LENGTH_LONG).show()
                            }
                        },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier
                                .padding(top = 10.dp)
                        ) {
                            Text(
                                text = "Unirse a Mesa",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

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
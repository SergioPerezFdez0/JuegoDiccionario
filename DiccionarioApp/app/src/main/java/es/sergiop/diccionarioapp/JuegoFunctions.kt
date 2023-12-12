package es.sergiop.diccionarioapp

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ListenerRegistration

fun cogerPalabras(letra: String, mContext: Context): Map<String, List<String>> {
    var resourceId = R.raw.aformatted

    when (letra){
        "a" -> resourceId = R.raw.aformatted
        "b" -> resourceId = R.raw.bformatted
        "c" -> resourceId = R.raw.cformatted
        "d" -> resourceId = R.raw.dformatted
        "e" -> resourceId = R.raw.eformatted
        "f" -> resourceId = R.raw.fformatted
        "g" -> resourceId = R.raw.gformatted
        "h" -> resourceId = R.raw.hformatted
        "i" -> resourceId = R.raw.iformatted
        "j" -> resourceId = R.raw.jformatted
        "k" -> resourceId = R.raw.kformatted
        "l" -> resourceId = R.raw.lformatted
        "m" -> resourceId = R.raw.mformatted
        "n" -> resourceId = R.raw.nformatted
        "ñ" -> resourceId = R.raw.n2formatted
        "o" -> resourceId = R.raw.oformatted
        "p" -> resourceId = R.raw.pformatted
        "q" -> resourceId = R.raw.qformatted
        "r" -> resourceId = R.raw.rformatted
        "s" -> resourceId = R.raw.sformatted
        "t" -> resourceId = R.raw.tformatted
        "u" -> resourceId = R.raw.uformatted
        "v" -> resourceId = R.raw.vformatted
        "w" -> resourceId = R.raw.wformatted
        "x" -> resourceId = R.raw.xformatted
        "y" -> resourceId = R.raw.yformatted
        "z" -> resourceId = R.raw.zformatted
    }

    val inputStream = mContext.resources.openRawResource(resourceId)
    val lines = inputStream.bufferedReader().readLines()

    val wordDefinitionsPairs = mutableListOf<Pair<String, List<String>>>()
    var currentWord = ""
    val currentDefinitions = mutableListOf<String>()

    lines.forEach { line ->
        val trimmedLine = line.trim()
        if (trimmedLine.startsWith("--") && trimmedLine.endsWith("%%%")) {
            if (currentWord.isNotEmpty()) {
                wordDefinitionsPairs.add(currentWord to currentDefinitions.toList())
                currentDefinitions.clear()
            }
            currentWord = trimmedLine.substring(2, trimmedLine.length - 3)
        } else if (trimmedLine.startsWith("Definición")) {
            currentDefinitions.add(trimmedLine.substring(12).trim())
        }
    }

    if (currentWord.isNotEmpty()) {
        wordDefinitionsPairs.add(currentWord to currentDefinitions.toList())
    }

    return wordDefinitionsPairs.toMap()
}

fun borrarJugadorYPonerExtra(
    mesasRef: DocumentReference,
    playerName: String?,
    jugadorPuntuacion: Long,
    mContext: Context,
    codigoMesa: String,
    listenerRegistration: ListenerRegistration,
    activity: Activity?
) {
    mesasRef.get().addOnSuccessListener { documentSnapshot ->
        if (documentSnapshot.exists()) {
            val data = documentSnapshot.data
            val jugadoresData = data?.get("jugadores") as? HashMap<String, Any> ?: hashMapOf()
            val jugadoresExtraData = data?.get("jugadoresExtra") as? HashMap<String, Any> ?: hashMapOf()

            val extraData = hashMapOf(
                playerName to hashMapOf(
                    "puntuacion" to jugadorPuntuacion,
                )
            )

            extraData.putAll(jugadoresExtraData.mapValues { (_, value) -> value as HashMap<String, Long> })

            jugadoresData.remove(playerName)

            data?.set("jugadoresExtra", extraData)
            data?.set("jugadores", jugadoresData)

            if (data != null) {
                mesasRef.set(data)
                    .addOnSuccessListener {
                        Log.d("JuegoFunctions", "Jugador eliminado correctamente")
                        Toast.makeText(mContext, "Te has retirado de la mesa $codigoMesa", Toast.LENGTH_LONG).show()
                        listenerRegistration.remove()
                        activity?.finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(mContext, "Error al eliminar al jugador", Toast.LENGTH_LONG).show()
                        Log.e("JuegoFunctions", "Error al eliminar al jugador", e)
                    }
            }
        }
    }
}
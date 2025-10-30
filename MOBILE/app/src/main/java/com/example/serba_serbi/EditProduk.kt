package com.example.serba_serbi

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditProduk(
    firestore: FirebaseFirestore,
    produk: Produk,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var noProduk by remember { mutableStateOf(produk.noProduk) }
    var namaProduk by remember { mutableStateOf(produk.namaProduk) }
    var harga by remember { mutableStateOf(produk.harga) }
    var fotoBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // Hanya decode jika fotoBase64 tidak null atau kosong
    LaunchedEffect(produk.fotoBase64) {
        produk.fotoBase64?.takeIf { it.isNotBlank() }?.let { base64 ->
            try {
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                fotoBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) {}
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            fotoBitmap = BitmapFactory.decodeStream(inputStream)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Edit Produk", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = noProduk,
            onValueChange = { noProduk = it },
            label = { Text("No Produk") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = namaProduk,
            onValueChange = { namaProduk = it },
            label = { Text("Nama Produk") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = harga,
            onValueChange = { harga = it },
            label = { Text("Harga") },
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.LightGray, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (fotoBitmap != null) {
                Image(
                    bitmap = fotoBitmap!!.asImageBitmap(),
                    contentDescription = "Foto Produk",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Pilih Foto", color = Color.DarkGray)
            }
        }

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Pilih Foto dari Galeri")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(Color.Gray),
                modifier = Modifier.weight(1f)
            ) { Text("Kembali") }

            Button(
                onClick = {
                    val fotoBase64: String? = fotoBitmap?.let {
                        val stream = java.io.ByteArrayOutputStream()
                        it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, stream)
                        Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
                    } // bisa null kalau tidak ada foto

                    val updatedProduk = Produk(
                        noProduk = noProduk,
                        namaProduk = namaProduk,
                        harga = harga,
                        fotoBase64 = fotoBase64,
                        id = produk.id
                    )

                    firestore.collection("produk").document(produk.id).set(updatedProduk)
                    onBack()
                },
                modifier = Modifier.weight(1f)
            ) { Text("Update") }
        }
    }
}

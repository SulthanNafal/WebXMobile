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
fun TambahProduk(
    firestore: FirebaseFirestore,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var noProduk by remember { mutableStateOf("") }
    var namaProduk by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var fotoBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

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
            .background(Color(0xFFE3F2FD))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Tambah Produk",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF0D47A1)
        )

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

        // Box foto (opsional)
        if (fotoBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = fotoBitmap!!.asImageBitmap(),
                    contentDescription = "Foto Produk",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Button(
            onClick = { launcher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pilih Foto dari Galeri", color = Color.White)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.weight(1f)
            ) { Text("Kembali", color = Color.White) }

            Button(
                onClick = {
                    // Foto bisa null
                    val fotoBase64: String? = fotoBitmap?.let {
                        val stream = java.io.ByteArrayOutputStream()
                        it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, stream)
                        Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
                    }

                    val newProduk = Produk(
                        noProduk = noProduk,
                        namaProduk = namaProduk,
                        harga = harga,
                        fotoBase64 = fotoBase64
                    )

                    firestore.collection("produk").add(newProduk)
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                modifier = Modifier.weight(1f)
            ) { Text("Tambah", color = Color.White) }
        }
    }
}

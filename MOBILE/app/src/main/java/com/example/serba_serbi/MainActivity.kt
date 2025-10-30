package com.example.serba_serbi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.serba_serbi.ui.theme.SerbaserbiTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SerbaserbiTheme {
                var currentScreen by remember { mutableStateOf("list") }
                var selectedProduk by remember { mutableStateOf<Produk?>(null) }
                var dataList by remember { mutableStateOf(listOf<Produk>()) }

                // 🔹 Realtime Firestore listener (ambil Base64 apa adanya)
                LaunchedEffect(Unit) {
                    firestore.collection("produk")
                        .addSnapshotListener { snapshot, e ->
                            if (e == null && snapshot != null) {
                                dataList = snapshot.documents.mapNotNull { doc ->
                                    doc.toObject(Produk::class.java)?.copy(id = doc.id)
                                }
                            }
                        }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFE3F2FD)
                ) {
                    when (currentScreen) {
                        "list" -> {
                            Scaffold(
                                floatingActionButton = {
                                    FloatingActionButton(
                                        onClick = {
                                            selectedProduk = null
                                            currentScreen = "tambah"
                                        },
                                        containerColor = Color(0xFF4CAF50)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Tambah Produk",
                                            tint = Color.White
                                        )
                                    }
                                },
                                containerColor = Color.Transparent
                            ) { padding ->
                                Column(
                                    modifier = Modifier
                                        .padding(padding)
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "Daftar Produk",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color(0xFF0D47A1)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (dataList.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "Belum ada produk.",
                                                color = Color.Gray,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            contentPadding = PaddingValues(bottom = 16.dp)
                                        ) {
                                            items(dataList) { produk ->
                                                ProdukCard(
                                                    produk = produk,
                                                    onEditClick = {
                                                        selectedProduk = produk
                                                        currentScreen = "edit"
                                                    },
                                                    onDeleteClick = {
                                                        hapusProduk(firestore, produk)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "tambah" -> TambahProduk(
                            firestore = firestore,
                            onBack = { currentScreen = "list" }
                        )

                        "edit" -> selectedProduk?.let { produk ->
                            EditProduk(
                                firestore = firestore,
                                produk = produk,
                                onBack = { currentScreen = "list" }
                            )
                        }
                    }
                }
            }
        }
    }
}

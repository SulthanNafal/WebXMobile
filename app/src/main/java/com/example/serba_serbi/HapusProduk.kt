package com.example.serba_serbi

import com.google.firebase.firestore.FirebaseFirestore

fun hapusProduk(firestore: FirebaseFirestore, produk: Produk) {
    firestore.collection("produk").document(produk.id)
        .delete()
        .addOnSuccessListener { }
        .addOnFailureListener { }
}

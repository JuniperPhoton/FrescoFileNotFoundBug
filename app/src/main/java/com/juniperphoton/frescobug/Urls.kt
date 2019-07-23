package com.juniperphoton.frescobug

val urls = mutableListOf<String>().apply {
    repeat(400) {
        add("https://picsum.photos/id/$it/100")
    }
}

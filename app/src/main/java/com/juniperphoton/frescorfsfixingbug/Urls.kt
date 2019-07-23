package com.juniperphoton.frescorfsfixingbug

val urls = mutableListOf<String>().apply {
    repeat(400) {
        add("https://picsum.photos/id/$it/100")
    }
}

package com.example.videojuego

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
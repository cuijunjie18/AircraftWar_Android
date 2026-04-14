package com.example.feature_online

data class OnlineClientData(val getScore: Int, val isGameOver: Boolean) {
}

data class OnlineServerData(val onlineScore: Int, val onlineGameOver: Boolean) {
}
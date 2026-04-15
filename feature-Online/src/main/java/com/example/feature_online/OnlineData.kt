package com.example.feature_online

/**
 * 客户端 -> 服务端 的数据
 * @param myScore 本方当前总分
 * @param isGameOver 本方是否已死亡
 */
data class OnlineClientData(val myScore: Int, val isGameOver: Boolean)

/**
 * 服务端 -> 客户端 的数据
 * @param opponentScore 对手当前总分
 * @param isAllOver 对战是否全部结束（双方都死亡）
 */
data class OnlineServerData(val opponentScore: Int, val isAllOver: Boolean)
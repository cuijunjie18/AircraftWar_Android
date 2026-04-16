# AircraftWar Android

## 背景

HITSZ软件构造实践，基于之前实现的[Windows版本](https://github.com/cuijunjie18/AircraftWar.git) 进行迁移

## 预期规划

- [x] 实现Windows到Android的迁移(目前实现了初始版)
- [x] 使用kotlin重写
- [x] 添加网络功能，实现联机

## 网络联机时序图一览

```mermaid
sequenceDiagram
    participant CF as CreateRoom/JoinRoom Fragment
    participant A as OnlineActivity
    participant GV as GameOnlineView
    participant C as OnlineGameClient
    participant S as OnlineGameServer

    CF->>S: 启动Server (创建房间方)
    CF->>C: 启动Client连接
    Note over S: 等待2个玩家连接...
    S-->>S: 2人就绪，通知各Client游戏开始
    C-->>CF: 收到Server的第一条数据(游戏开始信号)
    CF->>A: activity.startGame(client)
    A->>GV: 创建GameOnlineView(client)
    
    loop 游戏进行中
        GV->>C: 发送本地分数 (sendData)
        C->>S: ClientData{myScore, isGameOver}
        S->>S: 存储各玩家分数
        S->>C: ServerData{对手分数, 游戏是否结束}
        C->>GV: 回调更新对手分数
    end
    
    Note over GV: 英雄死亡
    GV->>C: sendData(score, gameOver=true)
    C->>S: 通知Server本方结束
    S-->>S: 等待另一方也结束
    S->>C: ServerData{对手分数, onlineGameOver=true}
    C->>GV: 回调通知对战结束
    GV->>GV: 显示最终分数面板

```

## 待优化问题

- [ ] 增加无线，无wifi近场联机
- [ ] 优化联机模式下的网络服务架构
- [ ] Activity间的执行栈顺序优化，menu->game->rank，优化为循环，即rank回退是menu,而非game

## 收获

具体见[项目学习笔记](docs/notebook.md)
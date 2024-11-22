package com.example.wiki.service;

import com.example.wiki.domain.Doc;
import com.example.wiki.websocket.WebSocketServer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WsService {
    @Resource
    public WebSocketServer webSocketServer;

    @Async
    public void sendInfo(String message){

        webSocketServer.sendInfo(message);
    }
}

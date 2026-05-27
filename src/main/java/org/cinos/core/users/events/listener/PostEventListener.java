//package com.argctech.core.users.events.listener;
//
//import events.users.org.cinos.core.PostCreateEvent;
//import service.users.org.cinos.core.IUserService;
//import exceptions.utils.users.org.cinos.core.UserNotFoundException;
//import utils.org.cinos.core.JsonParser;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class PostEventListener {
//
//    private final IUserService userService;
//
//    @KafkaListener(topics = "posts-create-topic")
//    public void onPostCreateEvent(String message) throws UserNotFoundException {
//        PostCreateEvent event = JsonParser.toJson(message, PostCreateEvent.class);
//        log.info("Post event received: {}", event.toString());
//    }
//
//}

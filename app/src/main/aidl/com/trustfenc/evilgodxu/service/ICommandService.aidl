package com.trustfenc.evilgodxu.service;

interface ICommandService {
    String executeCommand(String command);

    boolean isAlive();
}

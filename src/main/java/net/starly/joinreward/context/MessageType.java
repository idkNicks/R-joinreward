package net.starly.joinreward.context;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MessageType {

    ERROR("errorMessages"),
    NORMAL("messages"),
    REWARD("reward");

    private final String key;
}

package org.leochen.openAI.structure;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;

@Data
@AllArgsConstructor
public class ChatJson {
    private String model;
    private ArrayList<AskMessage> messages;
}

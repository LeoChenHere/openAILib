package org.leochen.openAI.structure;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AskMessage {
    private String role;
    private String content;
}

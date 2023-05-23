package cn.yuyang.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class PongMessage extends Message {

    private String content;

    public PongMessage(String content) {
        this.content = content;
    }

    @Override
    public int getMessageType() {
        return PongMessage;
    }
}

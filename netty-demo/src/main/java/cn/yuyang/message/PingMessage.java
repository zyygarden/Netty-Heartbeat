package cn.yuyang.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class PingMessage extends Message {

    private String content;

    public PingMessage(String content) {
        this.content = content;
    }

    public PingMessage() {

    }

    @Override
    public int getMessageType() {
        return PingMessage;
    }
}

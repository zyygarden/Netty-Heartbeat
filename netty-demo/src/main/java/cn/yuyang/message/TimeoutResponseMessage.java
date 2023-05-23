package cn.yuyang.message;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
public class TimeoutResponseMessage extends Message {

    private String content;

    public TimeoutResponseMessage(String content) {
        this.content = content;
    }

    @Override
    public int getMessageType() {
        return TimeoutResponseMessage;
    }
}

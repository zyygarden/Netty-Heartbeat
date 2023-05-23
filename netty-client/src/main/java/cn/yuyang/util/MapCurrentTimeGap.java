package cn.yuyang.util;

import io.netty.channel.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapCurrentTimeGap {
    public static final Map<Channel,Long> map = new ConcurrentHashMap<>();
}

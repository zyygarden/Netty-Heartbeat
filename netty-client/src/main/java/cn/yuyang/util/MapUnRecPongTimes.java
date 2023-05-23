package cn.yuyang.util;

import io.netty.channel.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapUnRecPongTimes {
    public static final Map<Channel,Integer> map = new ConcurrentHashMap<>();
}
package cn.yuyang.promise;

import io.netty.util.concurrent.Promise;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PromiseClass {
    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();
}

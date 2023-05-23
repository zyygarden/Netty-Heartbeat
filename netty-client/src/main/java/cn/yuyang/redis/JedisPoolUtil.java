package cn.yuyang.redis;

import cn.yuyang.pojo.MonitorVo;
import com.alibaba.fastjson.JSON;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

public class JedisPoolUtil {

    private static JedisPool jedisPool;

    static {
        //读取配置文件
        InputStream is = JedisPoolUtil.class.getClassLoader().getResourceAsStream("application.properties");
        Properties prop = new Properties();
        try {
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(Integer.parseInt(prop.getProperty("redis.max-idle")));//最大空闲数
        config.setMaxTotal(Integer.parseInt(prop.getProperty("redis.max-active")));//最大连接数
        //创建连接池对象
        jedisPool = new JedisPool(config, prop.getProperty("redis.host"), Integer.parseInt(prop.getProperty("redis.port")));
    }

    public static Jedis getJedis(){
        return jedisPool.getResource();
    }

    public static void main(String[] args) {
        DateFormat sf = new SimpleDateFormat("HH:mm:ss");
        //测试连接池
        Jedis jedis = JedisPoolUtil.getJedis();
        System.out.println(jedis);
        jedis.close();
        Set<String> keys = jedis.keys("*");
        for (String key : keys) {
            System.out.println(key);
        }
        MonitorVo monitorVo = new MonitorVo();
        monitorVo.setClientName("service-1");
        monitorVo.setDateList(sf.format(new Date()));
        monitorVo.setValueList(1L);
        jedis.lpush("heart:monitor:"+monitorVo.getClientName(), JSON.toJSONString(monitorVo));
    }
}

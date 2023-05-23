package cn.yuyang.service;

public class RpcServiceImpl implements RpcService {
    @Override
    public String say(String username) {
        return "来自客户端 " + username + " 的远程调用";
    }
}

package cn.yuyang.session;

public abstract class ClientSessionFactory {
    private static ClientSession clientSession = new ClientSessionMemoryImpl();

    public static ClientSession getSession() {
        return clientSession;
    }
}

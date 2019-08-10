package bb;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

class ServerWrite implements Runnable {
    public Socket s;
    public PrintStream ps = null;
    public BufferedReader br = null;
    public String name;

    public ServerWrite() {
    }

    public ServerWrite(Socket s) throws IOException {
        this.s = s;
        ps = new PrintStream(s.getOutputStream());
        br = new BufferedReader(
                new InputStreamReader(s.getInputStream()));
    }

    private synchronized void remove(String key) {
        Server.map.remove(key);
        sendToAll("当前在线人数为：" + Server.map.size());
        System.out.println("[系统通知] " + name + "已经下线了。");
        System.out.println("当前在线人数为：" + Server.map.size());
    }

    private synchronized void sendToAll(String message) {
        for (PrintStream out : Server.map.values()) {
            out.println(message);
        }
    }

    private synchronized void sendToSomeone(String name, String message,String t) {
        PrintStream pw = Server.map.get(name);
        if (Server.map.get(name)==ps){
            pw.println(message);
        }else {
            ps.println("你私聊对" + name + "说:" + t);
            pw.println(message);
        }


    }

    private String getName() throws Exception {
        try {
            BufferedReader bReader = new BufferedReader(
                    new InputStreamReader(s.getInputStream()));
            PrintWriter ipw = new PrintWriter(
                    new OutputStreamWriter(s.getOutputStream()), true);
            while (true) {
                String nameString = bReader.readLine();
                if (Server.map.containsKey(nameString)) {
                    ipw.println("已存在");
                } else {
                    return nameString;
                }
            }
        } catch (NullPointerException e) {
        }
        return null;
    }

    public void run() {
        try {
            name = getName();
            if (name == null) {
                throw new NullPointerException("该端口号" + s.getPort() + "没有设置昵称强行断开");
            }
            Server.map.put(name, ps);
            Thread.sleep(100);
            sendToAll("\t\t[系统通知] “" + name + "”已上线");
            String msgString = null;
            while ((msgString = br.readLine()) != null) {
                // 检验是否为私聊（格式：@昵称：内容）
                if (msgString.startsWith("@")) {
                    int index = msgString.indexOf(":");
                    if (index >= 0) {
                        String theName = msgString.substring(1, index);
                        if (Server.map.containsKey(theName)) {
                            String info = msgString.substring(index + 1);
                            String tep=info;
                            if (name.equals(theName)) {
                                info = "你自己对你自己说：" + info;
                            } else {
                                info = name + "私聊你：" + info;
                            }
                            sendToSomeone(theName, info,tep);
                            continue;
                        } else {
                            ps.println("该昵称不存在");
                            continue;
                        }
                    }
                } else {
                    if (msgString.equalsIgnoreCase("z")) {
                        for (String s : Server.map.keySet()) {
                            ps.println(s);
                        }
                        continue;
                    }
                }
                System.out.println(name + "发话：" + msgString);
                sendToAll(name + "发话：" + msgString);
            }
            sendToAll("[系统通知] " + name + "已经下线了。");
            remove(name);
            if (s != null) {
                s.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }
}

class Servertools implements Runnable {
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String res = null;
        while (true) {
            res = scanner.next();
            if (res.equalsIgnoreCase("z")) {
                System.out.println("-----------在线人员-------------");
                if (Server.map.size()!=0){
                    for (String s : Server.map.keySet()) {
                        System.out.println(s);
                    }
                }else {
                    System.out.println("---------暂无在线人员-------------");
                }

            } else if (res.equalsIgnoreCase("x")) {
                System.out.println("当前在线人数为：" + Server.map.size());
            }
        }
    }
}

public class Server {
    public static ConcurrentHashMap<String, PrintStream> map = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(3002);
        new Thread(new Servertools()).start();
        while (true) {
            System.out.println("等待连接中1");
            Socket s = serverSocket.accept();
            System.out.println("端口号为" + s.getPort() + "连接成功");
            new Thread(new ServerWrite(s)).start();
        }
    }
}


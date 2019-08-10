package bb;


import java.io.*;
import java.net.Socket;
import java.util.Scanner;


class ClientRead implements Runnable {
    private Socket s;
    BufferedReader br = null;

    public ClientRead(Socket s)
            throws IOException {
        this.s = s;
        br = new BufferedReader(
                new InputStreamReader(s.getInputStream()));
    }

    public void run() {
        try {
            String content = null;
            while ((content = br.readLine()) != null) {
                System.out.println(content);
            }
            System.out.println("服务端已关闭");
            SetName.thread.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ClientWrite implements Runnable {
    private Socket s;
    PrintStream ps = null;

    public ClientWrite(Socket s)
            throws IOException {
        this.s = s;
        ps = new PrintStream(s.getOutputStream());
    }

    public void run() {
        try {
            while (true) {
                String line = null;
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(System.in));
                while ((line = br.readLine()) != null) {
                    ps.println(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

class SetName implements Runnable {
    private Socket s;
    PrintStream ps = null;
    BufferedReader brr = null;
    public static Thread thread = null;

    public SetName(Socket s) throws IOException {
        this.s = s;
        ps = new PrintStream(s.getOutputStream());
        brr = new BufferedReader(
                new InputStreamReader(s.getInputStream()));
    }

    @Override
    public void run() {
        try {
            String name = null;
            String status = null;
            Scanner scanner = new Scanner(System.in);
            System.out.println("请设置你的聊天昵称");
            while (true) {
                name = scanner.next();
                ps.println(name);
                status = brr.readLine();
                if (status.equals("已存在")) {
                    System.out.println("昵称已存在,请重新输入");
                } else if (name.equals("")) {
                    System.out.println("昵称不允许为空,请重新输入");
                } else {
                    System.out.println("设置成功");
                    System.out.println("\t-------私聊格式  @昵称：内容------------");
                    System.out.println("\t--------输入z或Z---------显示在线人员名单");
                    new Thread(new ClientRead(s)).start();
                    thread = new Thread(new ClientWrite(s), name);
                    thread.start();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class Client {
    public static void main(String[] args) throws IOException {
        Socket s = new Socket("127.0.0.1", 3002);
        Thread thread = new Thread(new SetName(s));
        thread.start();
    }
}

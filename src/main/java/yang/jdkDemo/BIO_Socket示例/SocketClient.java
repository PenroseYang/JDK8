package yang.jdkDemo.BIO_Socket示例;

import java.io.*;
import java.net.Socket;

/**
 * @author ：yang
 * @date ：2021/4/16 16:35
 */

public class SocketClient {
    public static void main(String[] args) {
        final String QUIT = "quit";
        final String DEFAULT_SERVER_HOST = "127.0.0.1";
        final int DEFAULT_SERVER_PORT = 8888;
        Socket socket = null;
        BufferedWriter bufferedWriter = null;

        try {
            // 创建socket
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
            //创建IO流
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );
            //等待用户输入信息
            BufferedReader consolReader = new BufferedReader(
                    new InputStreamReader(System.in)
            );
            while (true) {
                String input = consolReader.readLine();
                //发送消息给服务器
                bufferedWriter.write(input + "\n");
                bufferedWriter.flush();
                //读取服务器返回的消息
                String msg = bufferedReader.readLine();
                System.out.println(msg);

                //查看用户是否退出
                if (QUIT.equals(input)) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                    System.out.println("关闭socket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package yang.jdkDemo.BIO_Socket示例;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    public static void main(String[] args) {

        final int DEFAULT_PORT = 8888;
        ServerSocket serverSocket = null;

        //绑定监听端口
        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口" + DEFAULT_PORT);

            while (true) {
                /**
                 * BIO的阻塞问题就在这一行代码上，
                 * socket.accept()正常会卡在这里，等着客户端过来建立连接
                 * 优化就在这一行代码后面，在后面的 handle方法上面加 new Thread，每次接到数据之后都交给新线程来处理
                 * 反正为了保证处理能力，这个while循环必须要保证总有一个线程停在 socket.accept这一行等着建立连接
                 */
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                System.out.println("客户端[" + socket.getPort() + "]已连接");

                handle(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                    System.out.println("关闭ServerSocket");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void handle(Socket socket) throws IOException {
        final String QUIT = "quit";
        /**
         * 这里是第二层阻塞，建立了连接之后不一定能及时收到数据
         * read还会卡在这里等数据
         */
        //创建IO流
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );
        BufferedWriter bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())
        );

        String msg = null;
        while ((msg = bufferedReader.readLine()) != null) {
            // 读取客户端发送的消息,当对方关闭时返回null

            System.out.println("客户端[" + socket.getPort() + "]：" + msg);
            //回复客户发送的消息
            bufferedWriter.write("服务器：" + msg + "\n");
            bufferedWriter.flush(); //保证缓冲区的数据发送出去

            //查看客户端是否退出
            if (QUIT.equals(msg)) {
                System.out.println("客户端[" + socket.getPort() + "]已退出");
                break;
            }
        }
    }
}

package yang.jdkDemo.NIO_Socket示例;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ：yang
 * @date ：2021/4/16 16:48
 */
public class NIOSocketServer {

    private static Integer port = 8080;

    // 通道管理器(Selector)
    private static Selector selector;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1,
            10,
            1000,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(100));

    public static void main(String[] args) {
        try {
            // 创建通道ServerSocketChannel
            ServerSocketChannel open = ServerSocketChannel.open();

            // 将通道设置为非阻塞
            open.configureBlocking(false);

            // 绑定到指定的端口上
            open.bind(new InetSocketAddress(port));

            // 通道管理器(Selector)
            /**
             * Selector多路复用器
             * 问题：
             * 如果有一百万个链接，结果只有100个连接有读写数据，每次处理都要遍历一百万个链接
             * Selector就可以收集那些有读写数据的连接，然后专门处理这些
             */
            selector = Selector.open();

            /**
             * 将通道(Channel)注册到通道管理器(Selector)，并为该通道注册selectionKey.OP_ACCEPT事件
             * 注册该事件后，当事件到达的时候，selector.select()会返回，
             * 如果事件没有到达selector.select()会一直阻塞。
             */
            open.register(selector, SelectionKey.OP_ACCEPT);

            // 循环处理
            while (true) {
                /**
                 * 当注册事件到达时，方法返回，否则该方法会一直阻塞
                 * 该Selector的select()方法将会返回大于0的整数，该整数值就表示该Selector上有多少个Channel具有可用的IO操作
                 *
                 * select方法才是真正的重点
                 */
                int select = selector.select();
                System.out.println("当前有 " + select + " 个channel可以操作");

                // 一个SelectionKey对应一个就绪的通道
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    // 获取事件
                    SelectionKey key = iterator.next();

                    // 移除事件，避免重复处理
                    iterator.remove();

                    /**
                     * 这里的key是上面的selector拿出来的，也就是触发了时间的所有的通道
                     * 无论是连接事件还是读写事件
                     */
                    // 客户端请求连接事件，接受客户端连接就绪
                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        // 监听到读事件，对读事件进行处理
                        threadPoolExecutor.submit(new NioServerHandler(key));
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理客户端连接成功事件
     *
     * @param key
     */
    public static void accept(SelectionKey key) {
        try {
            // 获取客户端连接通道
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            /**
             * （1） 非阻塞第一条，
             * 建立的accept连接就是非阻塞的，即使服务端没有接受到连接也不会阻塞
             *
             * 这个 accept 函数最后肯定会依赖到操作系统的系统调用上面去
             * IO程序到最后肯定都会关联到系统调用上去
             */
            SocketChannel sc = ssc.accept();
            sc.configureBlocking(false);

            // 给通道设置读事件，客户端监听到读事件后，进行读取操作
            sc.register(selector, SelectionKey.OP_READ);
            System.out.println("accept a client : " + sc.socket().getInetAddress().getHostName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听到读事件，读取客户端发送过来的消息
     */
    public static class NioServerHandler implements Runnable {

        private SelectionKey selectionKey;

        public NioServerHandler(SelectionKey selectionKey) {
            this.selectionKey = selectionKey;
        }

        @Override
        public void run() {
            try {
                if (selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                    // 从通道读取数据到缓冲区
                    ByteBuffer buffer = ByteBuffer.allocate(1024);

                    /**
                     * （2） 非阻塞第二条
                     * 连接本身是非阻塞的，没读到数据也不会阻塞
                     */
                    // 输出客户端发送过来的消息
                    socketChannel.read(buffer);
                    buffer.flip();
                    System.out.println("收到客户端" + socketChannel.socket().getInetAddress().getHostName() + "的数据：" + new String(buffer.array()));

                    //将数据添加到key中
                    ByteBuffer outBuffer = ByteBuffer.wrap(buffer.array());

                    // 将消息回送给客户端
                    socketChannel.write(outBuffer);
                    selectionKey.cancel();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
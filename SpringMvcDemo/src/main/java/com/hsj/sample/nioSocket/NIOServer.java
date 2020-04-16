package com.hsj.sample.nioSocket;


import org.springframework.expression.spel.ast.Selection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * @author sergei
 * @create 2020-04-16
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(8080));
        ssc.configureBlocking(false);//设置为非阻塞模式
        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        Handler handler =new Handler(1024);
        while (true){
            if (selector.select(3000)==0){
                System.out.println("等待请求超时...");
                continue;
            }
            System.out.println("处理请求...");
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
            while (keyIter.hasNext()){
                SelectionKey key = keyIter.next();
                try {
                    if (key.isAcceptable()){
                        handler.handleAccept(key);
                        System.out.println("1");
                    }
                    if (key.isReadable()){
                        handler.handleRead(key);
                        System.out.println("2");
                    }
                }catch (IOException ex){
                    keyIter.remove();
                    continue;
                }
                keyIter.remove();
            }
        }
    }

    private static class Handler {
        private int bufferSize = 1024;
        private String localCharset = "UTF-8";

        public Handler() {
        }

        public Handler(int bufferSize) {
            this(bufferSize, null);
        }

        public Handler(String LocalCharset) {
            this(-1, LocalCharset);
        }

        public Handler(int bufferSize, String localCharset) {
            if (bufferSize > 0) this.bufferSize = bufferSize;
            if (localCharset != null) this.localCharset = localCharset;
        }
        public void handleAccept(SelectionKey key)throws IOException{
            SocketChannel sc = ((ServerSocketChannel)key.channel()).accept();
            sc.configureBlocking(false);
            sc.register(key.selector(),SelectionKey.OP_READ, ByteBuffer.allocate(bufferSize));
        }
        public void handleRead(SelectionKey key) throws IOException{
            SocketChannel sc = (SocketChannel) key.channel();
            ByteBuffer buffer = (ByteBuffer) key.attachment();
            buffer.clear();
            if(sc.read(buffer)==-1){
                sc.close();
            }else {
                buffer.flip();//将buffer转换为读状态
                //将buffer中接收到的值按localCharset格式编码后保存到receivedStirng
                String receivedString = Charset.forName(localCharset).newDecoder().decode(buffer).toString();
                System.out.println("from client: "+receivedString);
                String sendString = "rece data:"+receivedString;
                buffer = ByteBuffer.wrap(sendString.getBytes(localCharset));
                sc.write(buffer);
                sc.close();
            }

        }
    }
}

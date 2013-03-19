package echo;
import java.io.IOException;  
import java.net.InetAddress;  
import java.net.InetSocketAddress;  
import java.nio.ByteBuffer;  
import java.nio.channels.SelectionKey;  
import java.nio.channels.Selector;  
import java.nio.channels.ServerSocketChannel;  
import java.nio.channels.SocketChannel;  
import java.util.Set;  
 
/**  
 * Echo������  
 * @author finux  
 */ 
public class EchoServer {  
    public final static int BUFFER_SIZE = 1024; //Ĭ�϶˿�  
    public final static String HOST = "127.0.0.1";  
    public final static int PORT = 8888;  
      
    public static void main(String[] args) {  
        ServerSocketChannel ssc = null;  
        //������  
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);  
        Selector selector = null;  
        try {  
            selector = Selector.open();  
            ssc = ServerSocketChannel.open();  
            ssc.socket().bind(new InetSocketAddress(InetAddress.getByName(HOST), PORT));  
            ssc.configureBlocking(false);  
            ssc.register(selector, SelectionKey.OP_ACCEPT);       
            print("������������׼��������...");  
            System.out.println(selector);
            while (selector.select() > 0) {       
                Set<SelectionKey> selectionKeys = selector.selectedKeys();  
                for (SelectionKey key: selectionKeys) {  
                    if (key.isAcceptable()) {  
                        SocketChannel sc = ssc.accept();  
                        print("���µ����ӣ���ַ��" + sc.socket().getRemoteSocketAddress());  
                        sc.configureBlocking(false);  
                        sc.register(selector, SelectionKey.OP_READ);  
                        // ��Ҫд��:  
                        // sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);  
                        // �Ͼ�������ע������õ��¼�SelectionKey.OP_WRTE  
                        // ����������������accept��CPUҲ����ܵ�100%  
                          
                    }  
                    //same to if ((ops & SelectionKey.OP_READ) == SelectionKey.OP_READ) {  
                    if (key.isReadable()) {   
                        SocketChannel sc = (SocketChannel)key.channel();  
                        print("���µĶ�ȡ����ַ��" + sc.socket().getRemoteSocketAddress());                        
                        buffer.clear();                       
                        sc.read(buffer);  
                        buffer.flip();  
                        byte[] b = new byte[buffer.limit()];  
                        buffer.get(b);  
                        String s = new String(b);  
                        if (s.equals("bye")) {  
                            print("�Ͽ����ӣ�" + sc.socket().getRemoteSocketAddress());    
                            //�Ͽ����Ӻ�ȡ���˼���ͨ������ѡ������ע��  
                            key.cancel();  
                            sc.close();  
                            continue;  
                        }  
                        print("��ȡ������Ϊ��" + s);     
                        buffer.clear();  
                        s = "echo: " + s;  
                        buffer.put(s.getBytes());  
                        buffer.flip();  
                        sc.write(buffer);  
                    }   
                }  
                selectionKeys.clear();  
            }  
        } catch(IOException e) {  
            e.printStackTrace();  
        }   
    }  
      
    private static void print(String s) {  
        System.out.println(s);  
    }  
} 
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
 
public class MainNIO {
 
    /**
     * @param args
     */
    public static void main(String[] args) {
		Pattern hpp = Pattern.compile("(.+):([0-9]+)");
    	for (String x: args) {
    		HostTest ht = null;
    		Matcher m = hpp.matcher(x);
    		if (m.matches()) 
    			ht = new HostTest(m.group(1), Integer.valueOf(m.group(1)));
    		else
    			ht = new HostTest(x);
    		Thread thread = new Thread(ht);
    		thread.start();
    	}
    }
 
    static class HostTest implements Runnable {
    	private String message = "\n\n";
        private Selector selector;
        String url;
        int port = 0;
 
        public HostTest(String url, int port){
            this.url = url;
            this.port = port;
        }
        public HostTest(String url){
            this.url = url;
        }
 
        @Override
        public void run() {
            SocketChannel channel;
            try {
                selector = Selector.open();
                channel = SocketChannel.open();
                channel.configureBlocking(false);
 
                channel.register(selector, SelectionKey.OP_CONNECT);
                channel.connect(new InetSocketAddress(url, port));
 
                while (!Thread.interrupted()){
 
                    selector.select(1000);
                     
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
 
                    while (keys.hasNext()){
                        SelectionKey key = keys.next();
                        keys.remove();
 
                        if (!key.isValid()) continue;
 
                        if (key.isConnectable()){
                            System.out.println("I am connected to the server");
                            connect(key);
                        }   
                        if (key.isWritable()){
                            write(key);
                        }
                        if (key.isReadable()){
                            read(key);
                        }
                    }   
                }
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } finally {
                close();
            }
        }
         
        private void close(){
            try {
                selector.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
 
        private void read (SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer readBuffer = ByteBuffer.allocate(1000);
            readBuffer.clear();
            int length;
            try{
            length = channel.read(readBuffer);
            } catch (IOException e){
                System.out.println("Reading problem, closing connection");
                key.cancel();
                channel.close();
                return;
            }
            if (length == -1){
                System.out.println("Nothing was read from server");
                channel.close();
                key.cancel();
                return;
            }
            readBuffer.flip();
            byte[] buff = new byte[1024];
            readBuffer.get(buff, 0, length);
            System.out.println("Server said: "+new String(buff));
        }
 
        private void write(SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            channel.write(ByteBuffer.wrap(message.getBytes()));
 
            // lets get ready to read.
            key.interestOps(SelectionKey.OP_READ);
        }
 
        private void connect(SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel.isConnectionPending()){
                channel.finishConnect();
            }
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_WRITE);
        }
    }
}

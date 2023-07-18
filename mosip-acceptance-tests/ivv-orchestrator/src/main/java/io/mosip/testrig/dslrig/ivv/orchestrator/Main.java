package io.mosip.testrig.dslrig.ivv.orchestrator;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
        
public class Main {
        
    public static void main(String[] args) throws Exception {
        CountDownLatch latch = new CountDownLatch(10);
        
        WebSocket ws = HttpClient
                .newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create("wss://smtp.qa-1201-b2.mosip.net/mocksmtp/websocket"), new WebSocketClient(latch))
                .join();
        latch.await();
    }
            
    private static class WebSocketClient implements WebSocket.Listener {
        private final CountDownLatch latch;
                
        public WebSocketClient(CountDownLatch latch) { this.latch = latch; }
        
        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("onOpen using subprotocol " + webSocket.getSubprotocol());
            WebSocket.Listener.super.onOpen(webSocket);
        }
        
        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            System.out.println("onText received " + data);
            
            
            latch.countDown();
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }
        
        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.out.println("Bad day! " + webSocket.toString());
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }
}

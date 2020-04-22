package np.com.bottle.podapp.util;

import android.content.Context;
import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import np.com.bottle.podapp.activity.ProvisioningActivity;

public class WebSocketHelper extends WebSocketServer {

    private static String TAG = WebSocketHelper.class.getSimpleName();
    ProvisioningActivity pActivity;

    public WebSocketHelper(ProvisioningActivity pActivity, int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
        this.pActivity = pActivity;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome to the server!"); //This method sends a message to the new client
        broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This method sends a message to all clients connected
        Log.d(TAG, conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {

    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        broadcast( message + "111111111111" );
        Log.d(TAG, "conn: " + message);
        pActivity.onAwsConfigReceived(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }
}

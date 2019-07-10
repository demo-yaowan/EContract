package socket;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.android.LoginActivity;

import org.java_websocket.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import utils.HttpUtil;
import utils.SecurityUtil;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class NewDeviceLoginWebSocket extends WebSocketClient {

    private Handler mHandler;

    public static void main_run(String url, Handler handler) {
        WebSocketClient client = null;
        try {
            client = new NewDeviceLoginWebSocket(new URI(url),handler);
        } catch (URISyntaxException e) {
            System.out.println("url error");
            e.printStackTrace();
        }
        client.connect();
    }

    public NewDeviceLoginWebSocket(URI serverUri, Handler handler) {
        super(serverUri);
        this.mHandler = handler;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        Log.v(TAG, "成功连上长连接！");
    }

    @Override
    public void onMessage(String message) {
        Log.v(TAG, "new message:" + message);
        try {
            JSONObject data = new JSONObject(message);
            String userId = (String) data.get("user_id");
            String signedHash = (String) data.get("signed_hash");

            if (SecurityUtil.verifyStringByRSAPublicKeyString(userId, signedHash, HttpUtil.getServerPublicKey())) {
                Log.v(TAG, "对服务器验签通过！");
                Message mess = mHandler.obtainMessage();
                mess.what = LoginActivity.GET_QR_CODE_SUCCESS;
                JSONObject mesObj = new JSONObject();
                mesObj.put("userId", userId);
                mess.obj = mesObj;
                mess.sendToTarget();
            } else {
                Log.v(TAG, "对服务器验签失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.v(TAG, "Close reason" + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.out.println("error: "+ ex.toString());
    }
}

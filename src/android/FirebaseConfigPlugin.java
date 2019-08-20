package by.chemerisuk.cordova.firebase;

import java.util.Collections;

import android.content.Context;
import android.util.Log;

import by.chemerisuk.cordova.support.CordovaMethod;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;


public class FirebaseConfigPlugin extends ReflectiveCordovaPlugin {
    private static final String TAG = "FirebaseConfigPlugin";

    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase Remote Config plugin");

    }

    @CordovaMethod
    protected void init(final JSONObject defaultsJson, final CallbackContext callbackContext) {
        Log.d(TAG, "init");

        this.firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        try {
            this.firebaseRemoteConfig.setDefaults(toMap(defaultsJson));
            callbackContext.success();
        } catch (JSONException error) {
            callbackContext.error("Parsing error.");
        }
    }

    @CordovaMethod
    protected void update(long expirationDuration, final CallbackContext callbackContext) {
        this.firebaseRemoteConfig.fetch(expirationDuration)
                .addOnCompleteListener(cordova.getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        if (task.isSuccessful()) {
                            callbackContext.success();
                        } else {
                            callbackContext.error(task.getException().getMessage());
                        }
                    }
                });
    }

    @CordovaMethod
    protected void activate(final CallbackContext callbackContext) {
        this.firebaseRemoteConfig.activate()
                .addOnCompleteListener(cordova.getActivity(), new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            callbackContext.sendPluginResult(
                                    new PluginResult(PluginResult.Status.OK, task.getResult()));
                        } else {
                            callbackContext.error(task.getException().getMessage());
                        }
                    }
                });
    }

    @CordovaMethod
    protected void fetchAndActivate(final CallbackContext callbackContext) {
        this.firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(cordova.getActivity(), new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            callbackContext.sendPluginResult(
                                    new PluginResult(PluginResult.Status.OK, task.getResult()));
                        } else {
                            callbackContext.error(task.getException().getMessage());
                        }
                    }
                });
    }

    @CordovaMethod
    protected void getBoolean(String key, CallbackContext callbackContext) {
        callbackContext.sendPluginResult(
                new PluginResult(PluginResult.Status.OK, getValue(key).asBoolean()));
    }

    @CordovaMethod
    protected void getBytes(String key, CallbackContext callbackContext) {
        callbackContext.success(getValue(key).asByteArray());
    }

    @CordovaMethod
    protected void getNumber(String key, CallbackContext callbackContext) {
        callbackContext.sendPluginResult(
                new PluginResult(PluginResult.Status.OK, (float)getValue(key).asDouble()));
    }

    @CordovaMethod
    protected void getString(String key, CallbackContext callbackContext) {
        callbackContext.success(getValue(key).asString());
    }

    private FirebaseRemoteConfigValue getValue(String key) {
        return this.firebaseRemoteConfig.getValue(key);
    }

    private static Map<String, Object> toMap(JSONObject jsonobj)  throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keys = jsonobj.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = jsonobj.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }   return map;
    }

    private static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }
            else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }   return list;
    }
}

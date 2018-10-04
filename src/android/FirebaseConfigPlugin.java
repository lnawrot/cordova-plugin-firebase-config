package by.chemerisuk.cordova.firebase;

import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import by.chemerisuk.cordova.support.CordovaMethod;
import by.chemerisuk.cordova.support.ReflectiveCordovaPlugin;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;


public class FirebaseConfigPlugin extends ReflectiveCordovaPlugin {
    private static final String TAG = "FirebaseRemoteConfigPlugin";

    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "Starting Firebase Remote Config plugin");

    }

    @CordovaMethod
    protected void init(final JSONObject defaultsJson, final CallbackContext callbackContext) {
        Log.d(TAG, "init");

        this.firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        this.firebaseRemoteConfig.setDefaults(toMap(defaultsJson));
        callbackContext.success();
    }

    @CordovaMethod
    protected void update(long ttlSeconds, final CallbackContext callbackContext) {
        if (ttlSeconds == 0) {
            // App should use developer mode to fetch values from the service
            this.firebaseRemoteConfig.setConfigSettings(
                new FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(true)
                    .build()
            );
        }

        this.firebaseRemoteConfig.fetch(ttlSeconds)
            .addOnCompleteListener(cordova.getActivity(), new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        firebaseRemoteConfig.activateFetched();

                        callbackContext.success();
                    } else {
                        callbackContext.error(task.getException().getMessage());
                    }
                }
            });
    }

    @CordovaMethod
    protected void getBoolean(String key, String namespace, CallbackContext callbackContext) {
        boolean value = getValue(key, namespace).asBoolean();

        callbackContext.sendPluginResult(
            new PluginResult(PluginResult.Status.OK, value));
    }

    @CordovaMethod
    protected void getBytes(String key, String namespace, CallbackContext callbackContext) {
        callbackContext.success(getValue(key, namespace).asByteArray());
    }

    @CordovaMethod
    protected void getNumber(String key, String namespace, CallbackContext callbackContext) {
        double value = getValue(key, namespace).asDouble();

        callbackContext.sendPluginResult(
            new PluginResult(PluginResult.Status.OK, (float)value));
    }

    @CordovaMethod
    protected void getString(String key, String namespace, CallbackContext callbackContext) {
        callbackContext.success(getValue(key, namespace).asString());
    }

    private FirebaseRemoteConfigValue getValue(String key, String namespace) {
        if (namespace.isEmpty()) {
            return this.firebaseRemoteConfig.getValue(key);
        } else {
            return this.firebaseRemoteConfig.getValue(key, namespace);
        }
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

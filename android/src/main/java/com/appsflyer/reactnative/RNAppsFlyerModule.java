
package com.appsflyer.reactnative;


import android.app.Application;


import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import static com.appsflyer.reactnative.RNAppsFlyerConstants.*;

public class RNAppsFlyerModule extends ReactContextBaseJavaModule  {

    private ReactApplicationContext reactContext;
    private Application application;

    public RNAppsFlyerModule(ReactApplicationContext reactContext, Application application) {
        super(reactContext);
        this.reactContext = reactContext;
        this.application = application;
    }

    @Override
    public String getName() {
        return "RNAppsFlyer";
    }

    @Override
    public Map<String, Object> getConstants() {
        HashMap<String, Object> constants = new HashMap<String, Object>();
        constants.put("ACHIEVEMENT_UNLOCKED", AFInAppEventType.ACHIEVEMENT_UNLOCKED);
        constants.put("ADD_PAYMENT_INFO", AFInAppEventType.ADD_PAYMENT_INFO);
        constants.put("ADD_TO_CART", AFInAppEventType.ADD_TO_CART);
        constants.put("ADD_TO_WISH_LIST", AFInAppEventType.ADD_TO_WISH_LIST);
        constants.put("COMPLETE_REGISTRATION", AFInAppEventType.COMPLETE_REGISTRATION);
        constants.put("CONTENT_VIEW", AFInAppEventType.CONTENT_VIEW);
        constants.put("INITIATED_CHECKOUT", AFInAppEventType.INITIATED_CHECKOUT);
        constants.put("INVITE", AFInAppEventType.INVITE);
        constants.put("LEVEL_ACHIEVED", AFInAppEventType.LEVEL_ACHIEVED);
        constants.put("LOCATION_CHANGED", AFInAppEventType.LOCATION_CHANGED);
        constants.put("LOCATION_COORDINATES", AFInAppEventType.LOCATION_COORDINATES);
        constants.put("LOGIN", AFInAppEventType.LOGIN);
        constants.put("OPENED_FROM_PUSH_NOTIFICATION", AFInAppEventType.OPENED_FROM_PUSH_NOTIFICATION);
        constants.put("ORDER_ID", AFInAppEventType.ORDER_ID);
        constants.put("PURCHASE", AFInAppEventType.PURCHASE);
        constants.put("RATE", AFInAppEventType.RATE);
        constants.put("RE_ENGAGE", AFInAppEventType.RE_ENGAGE);
        constants.put("SEARCH", AFInAppEventType.SEARCH);
        constants.put("SHARE", AFInAppEventType.SHARE);
        constants.put("SPENT_CREDIT", AFInAppEventType.SPENT_CREDIT);
        constants.put("TRAVEL_BOOKING", AFInAppEventType.TRAVEL_BOOKING);
        constants.put("TUTORIAL_COMPLETION", AFInAppEventType.TUTORIAL_COMPLETION);
        constants.put("UPDATE", AFInAppEventType.UPDATE);
        return constants;
    }

    @ReactMethod
    public void initSdk(
            ReadableMap _options,
            Callback successCallback,
            Callback errorCallback
            ) {

        String devKey = null;
        boolean isDebug;
        boolean isConversionData;

        AppsFlyerLib instance = AppsFlyerLib.getInstance();

        try{

           JSONObject options = RNUtil.readableMapToJson(_options);

            devKey = options.optString(afDevKey, "");

            if(devKey.trim().equals("")){
                errorCallback.invoke( new Exception(NO_DEVKEY_FOUND).getMessage() );
                return;
            }

            isDebug = options.optBoolean(afIsDebug, false);
            instance.setDebugLog(isDebug);

            isConversionData = options.optBoolean(afConversionData, false);

            if(isDebug == true){ Log.d("AppsFlyer", "Starting Tracking");}

            instance.startTracking(application, devKey);

            //TODO: callback should come from SDK
            successCallback.invoke(SUCCESS);
        }
        catch (Exception e){
            errorCallback.invoke(e.getMessage());
            return;
        }

        if(isConversionData == true){
            registerConversionListener(instance);
        }
    }

    private void registerConversionListener(AppsFlyerLib instance){
        instance.registerConversionListener(application.getApplicationContext(), new AppsFlyerConversionListener(){

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
                handleSuccess(afOnAppOpenAttribution, attributionData);
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                handleError(afOnAttributionFailure, errorMessage);
            }

            @Override
            public void onInstallConversionDataLoaded(Map<String, String> conversionData) {
                handleSuccess(afOnInstallConversionDataLoaded, conversionData);
            }

            @Override
            public void onInstallConversionFailure(String errorMessage) {
                handleError(afOnInstallConversionFailure, errorMessage);
            }

            private void handleSuccess(String eventType, Map<String, String> data){
                JSONObject obj = new JSONObject();

                try {
                    obj.put("status", afSuccess);
                    obj.put("type", eventType);
                    obj.put("data",  new JSONObject(data));
                    sendEvent(reactContext, afOnInstallConversionData, obj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void handleError(String eventType, String errorMessage){
                JSONObject obj = new JSONObject();

                try {
                    obj.put("status", afFailure);
                    obj.put("type", eventType);
                    obj.put("data", errorMessage);
                    sendEvent(reactContext, afOnInstallConversionData, obj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private void sendEvent(ReactContext reactContext,
                                   String eventName,
                                   Object params) {
                reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(eventName, params);
            }
        });


    }

    @ReactMethod
    public void trackAppLaunch(){
        Context c = application.getApplicationContext();
        AppsFlyerLib.getInstance().trackEvent(c, null, null);
    }


    @ReactMethod
    public void trackEvent(
            final String eventName, ReadableMap eventData,
            Callback successCallback,
            Callback errorCallback)
    {
        try {

        if(eventName.trim().equals("")){
            errorCallback.invoke( new Exception(NO_EVENT_NAME_FOUND).getMessage() );
            return;
        }

        Map<String, Object> data = RNUtil.toMap(eventData);

        if(data == null){ // in case of no values
            data = new HashMap<>();
        }

//        if(data.size() == 0){
//            errorCallback.invoke( new Exception(NO_EVENT_VALUES_FOUND).getMessage() );
//            return;
//        }

        AppsFlyerLib.getInstance().trackEvent(getReactApplicationContext(), eventName, data);

            //TODO: callback should come from SDK
            successCallback.invoke(SUCCESS);
        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
            return;
        }
    }


    @Deprecated
    @ReactMethod
    public void sendTrackingWithEvent(final String eventName) {
        AppsFlyerLib.getInstance().trackEvent(getReactApplicationContext(), eventName, null);
    }

    @ReactMethod
    public void getAppsFlyerUID(Callback callback) {
        String appId = AppsFlyerLib.getInstance().getAppsFlyerUID(getReactApplicationContext());
        callback.invoke(null, appId);
    }

    @ReactMethod
    public void setGCMProjectID(final String gcmProjectId,
                                Callback successCallback,
                                Callback errorCallback) 
    {
        AppsFlyerLib.getInstance().setGCMProjectNumber(getReactApplicationContext(), gcmProjectId);
        successCallback.invoke(SUCCESS);
    }

    @ReactMethod
    public void setCustomerUserId(final String userId,Callback callback){
        AppsFlyerLib.getInstance().setCustomerUserId(userId);
        callback.invoke(SUCCESS);
    }
}

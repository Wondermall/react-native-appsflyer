
import { NativeModules } from 'react-native';
import { NativeAppEventEmitter } from 'react-native';

const { RNAppsFlyer } = NativeModules;

const appsFlyer = {};

const eventsMap = {};


appsFlyer.initSdk = (options, successC, errorC) => {

    console.log("initSdk: " + eventsMap['onInstallConversionData']);

    options.onInstallConversionDataListener = (eventsMap['onInstallConversionData']) ? true: false;
    return RNAppsFlyer.initSdk(options, successC, errorC);
};

/**
 * iOS only
 */
appsFlyer.trackLocation = (longitude, latitude, callback) => {
    return RNAppsFlyer.trackLocation(longitude, latitude, callback);
};



appsFlyer.trackEvent = (eventName, eventValues, successC, errorC) => {
    return RNAppsFlyer.trackEvent(eventName, eventValues, successC, errorC);
};

appsFlyer.getAppsFlyerUID = (callback) => {
    return RNAppsFlyer.getAppsFlyerUID(callback);
};

/**
 * For Android only (Google API). iOS uses 'didRegisterForRemoteNotificationsWithDeviceToken' in AppDelegate.m 
 */
appsFlyer.setGCMProjectID = (gcmProjectId, successC, errorC) => {
    return RNAppsFlyer.setGCMProjectID(gcmProjectId, successC, errorC);
};

appsFlyer.setCustomerUserId = (userId, successC,) => {
    return RNAppsFlyer.setCustomerUserId(userId, successC);
};

appsFlyer.trackAppLaunch = () => {
    RNAppsFlyer.trackAppLaunch();
}


/**
 * Accessing AppsFlyer Attribution / Conversion Data from the SDK (Deferred Deeplinking)
 * @param callback: contains fields:
 *    status: success/failure
 *    type:
 *          onAppOpenAttribution
 *          onInstallConversionDataLoaded
 *          onAttributionFailure
 *          onInstallConversionFailure
 *    data: metadata,
 * @example {"status":"success","type":"onInstallConversionDataLoaded","data":{"af_status":"Organic","af_message":"organic install"}}
 *
 * @returns {remove: function - unregister listener}
 */
appsFlyer.onInstallConversionData = (callback) => {

    //console.log("onInstallConversionData is called" );

        const listener = NativeAppEventEmitter.addListener('onInstallConversionData',
            (_data) => {
                if(callback && typeof(callback) === typeof(Function)){
                    try{
                        let data = JSON.parse(_data);
                        callback(data);
                    }
                    catch(_error){
                        //throw new AFParseJSONException("...");
                        //TODO: for today we return an error in callback
                        callback(new AFParseJSONException("Invalid data structure", _data));
                    }
                }
            }
        );

    eventsMap['onInstallConversionData'] = listener;

    // unregister listener (suppose should be called from componentWillUnmount() )
    return function remove() {
        listener.remove();
    };
};

function AFParseJSONException(_message, _data) {
    this.message = _message;
    this.data = _data;
    this.name = "AFParseJSONException";
}

export default appsFlyer;
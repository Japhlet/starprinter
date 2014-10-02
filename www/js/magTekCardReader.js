var magTekCardReader = {
    isDeviceConnected: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'isDeviceConnected', // with this action name
            []
        );
    },
    isDeviceOpened: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'isDeviceOpened', // with this action name
            []
        );
    },
    openDevice: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'openDevice', // with this action name
            []
        );
    },
    closeDevice: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'closeDevice', // with this action name
            []
        );
    },
    clearCardData: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'clearCardData', // with this action name
            []
        );
    },
    setCardData: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'setCardData', // with this action name
            []
        );
    },
    getTrackDecodeStatus: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getTrackDecodeStatus', // with this action name
            []
        );
    },
    getTrack1: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getTrack1', // with this action name
            []
        );
    },
    getTrack3: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getTrack3', // with this action name
            []
        );
    },
    getTrack1Masked: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getTrack1Masked', // with this action name
            []
        );
    },
    getTrack2Masked: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getTrack2Masked', // with this action name
            []
        );
    },
    getTrack3Masked: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getTrack3Masked', // with this action name
            []
        );
    },
    getMagnePrintStatus: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getMagnePrintStatus', // with this action name
            []
        );
    },
    getMagnePrint: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getMagnePrint', // with this action name
            []
        );
    },
    getDeviceSerial: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getDeviceSerial', // with this action name
            []
        );
    },
    getSessionID: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getSessionID', // with this action name
            []
        );
    },
    setDeviceProtocolString: function(protocol, successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'setDeviceProtocolString', // with this action name
            [protocol]
        );
    },
    listenForEvents: function(cardData, successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'listenForEvents', // with this action name
            [cardData]
        );
    },
    getCardName: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getCardName', // with this action name
            []
        );
    },
    getCardIIN: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getCardIIN', // with this action name
            []
        );
    },
    getCardLast4: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getCardLast4', // with this action name
            []
        );
    },
    getCardExpDate: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getCardExpDate', // with this action name
            []
        );
    },
    getCardServiceCode: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'getCardServiceCode', // with this action name
            []
        );
    },
    setDeviceType: function(deviceType, successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'MagTekCardReader', // mapped to our native Java class called "MagTekCardReader"
            'setDeviceType', // with this action name
            [deviceType]
        );
    }
};
module.exports = magTekCardReader;

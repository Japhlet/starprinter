var starPrinter = {
    print: function(companyCode, orderId, successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'StarPrinter', // mapped to our native Java class called "StarPrinter"
            'print', // with this action name
            [companyCode, orderId]
        ); 
     },
    openCashDrawer: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'StarPrinter', // mapped to our native Java class called "StarPrinter"
            'openCashDrawer', // with this action name
            []
        ); 
     },
    logMessage: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'StarPrinter', // mapped to our native Java class called "StarPrinter"
            'logMessage', // with this action name
            []
        ); 
     },
    searchTCPPrinters: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'StarPrinter', // mapped to our native Java class called "StarPrinter"
            'searchTCPPrinters', // with this action name
            []
        ); 
     },
    searchBTPrinters: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'StarPrinter', // mapped to our native Java class called "StarPrinter"
            'searchBTPrinters', // with this action name
            []
        ); 
     }

}
module.exports = starPrinter;

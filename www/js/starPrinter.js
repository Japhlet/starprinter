var starPrinter = {
    print: function(orderId, successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'StarPrinter', // mapped to our native Java class called "StarPrinter"
            'print', // with this action name
            [{                  // and this array of custom arguments to create our entry
                "orderId": orderId
            }]
        ); 
     },
    openCashDrawer: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'StarPrinter', // mapped to our native Java class called "StarPrinter"
            'openCashDrawer', // with this action name
            [{"":""}]
        ); 
     },
    searchTCPPrinters: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'StarPrinter', // mapped to our native Java class called "StarPrinter"
            'searchTCPPrinters', // with this action name
            [{"":""}]
        ); 
     },
    searchBTPrinters: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'StarPrinter', // mapped to our native Java class called "StarPrinter"
            'searchBTPrinters', // with this action name
            [{"":""}]
        ); 
     }
}
module.exports = starPrinter;

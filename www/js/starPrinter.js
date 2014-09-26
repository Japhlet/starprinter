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
     }
}
module.exports = starPrinter;

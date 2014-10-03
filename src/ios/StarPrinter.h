//
//  StarPrinter.h
//  PosAndroid
//
//  Created by Jay Lee on 9/30/14.
//
//

#import <Cordova/CDV.h>

@interface StarPrinter : CDVPlugin {}

+ (void)openCashDrawerWithPortname:(CDVInvokedUrlCommand*)command;

+ (void)sendCommand:(NSData *)commandsToPrint
                    portName:(NSString *)portName
                    portSettings:(NSString *)portSettings
                    timeoutMillis:(u_int32_t)timeoutMillis;

@end

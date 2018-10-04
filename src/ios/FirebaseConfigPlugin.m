#import "FirebaseConfigPlugin.h"
@import Firebase;

@implementation FirebaseConfigPlugin

- (void)pluginInitialize {
    NSLog(@"Starting Firebase Remote Config plugin");
}

- (void)init:(CDVInvokedUrlCommand *)command {
    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }

    self.remoteConfig = [FIRRemoteConfig remoteConfig];
    [self.remoteConfig setDefaults:command];
}


- (void)update:(CDVInvokedUrlCommand *)command {
    NSNumber* ttlSeconds = [command argumentAtIndex:0];
    long expirationDuration = [ttlSeconds longValue];

    if (expirationDuration == 0) {
        self.remoteConfig.configSettings = [[FIRRemoteConfigSettings alloc] initWithDeveloperModeEnabled:YES];
    }

    [self.remoteConfig fetchWithExpirationDuration:expirationDuration completionHandler:^(FIRRemoteConfigFetchStatus status, NSError *error) {
        CDVPluginResult *pluginResult = nil;

        if (status == FIRRemoteConfigFetchStatusSuccess) {
            [self.remoteConfig activateFetched];

            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)getString:(CDVInvokedUrlCommand *)command {
    FIRRemoteConfigValue *configValue = [self getConfigValue:command];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                      messageAsString:configValue.stringValue];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getNumber:(CDVInvokedUrlCommand *)command {
    FIRRemoteConfigValue *configValue = [self getConfigValue:command];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                      messageAsDouble:[configValue.numberValue doubleValue]];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getBoolean:(CDVInvokedUrlCommand *)command {
    FIRRemoteConfigValue *configValue = [self getConfigValue:command];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                        messageAsBool:configValue.boolValue];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)getBytes:(CDVInvokedUrlCommand *)command {
    FIRRemoteConfigValue *configValue = [self getConfigValue:command];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                 messageAsArrayBuffer:configValue.dataValue];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (FIRRemoteConfigValue*)getConfigValue:(CDVInvokedUrlCommand *)command {
    NSString* key = [command argumentAtIndex:0];
    NSString* namespace = [command argumentAtIndex:1];

    if ([namespace length] == 0) {
        return [self.remoteConfig configValueForKey:key];
    } else {
        return [self.remoteConfig configValueForKey:key namespace:namespace];
    }
}


@end

#import "NotificationHelperPlugin.h"
#import <notification_helper/notification_helper-Swift.h>

@implementation NotificationHelperPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftNotificationHelperPlugin registerWithRegistrar:registrar];
}
@end

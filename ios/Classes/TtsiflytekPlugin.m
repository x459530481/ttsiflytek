#import "TtsiflytekPlugin.h"
#if __has_include(<ttsiflytek/ttsiflytek-Swift.h>)
#import <ttsiflytek/ttsiflytek-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "ttsiflytek-Swift.h"
#endif

@implementation TtsiflytekPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTtsiflytekPlugin registerWithRegistrar:registrar];
}
@end

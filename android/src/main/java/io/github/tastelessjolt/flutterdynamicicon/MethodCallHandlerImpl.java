package io.github.tastelessjolt.flutterdynamicicon;

import android.content.Context;
import android.content.pm.ActivityInfo;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

class MethodCallHandlerImpl implements MethodChannel.MethodCallHandler {

  Context context;

  public MethodCallHandlerImpl(Context c) {
    context = c;
  }

  @Override
  public void onMethodCall(MethodCall call, @NonNull MethodChannel.Result result) {
    if (call.method.equals("mSupportsAlternateIcons")) {
      result.success(true);
    } else if (call.method.equals("mGetAvailableAlternateIconNames")) {
      List<String> include = call.argument("include");
      List<String> names = IconChanger.getAvailableAliasNames(context, include);
      result.success(names);
    } else if (call.method.equals("mGetAvailableAlternateIcons")) {
      List<String> include = call.argument("include");
      Map<String, byte[]> map = IconChanger.getAvailableAliasIcons(context, include);
      result.success(map);
    } else if (call.method.equals("mGetAlternateIconName")) {
      String name = IconChanger.getCurrentEnabledAlias(context);
      result.success(name != null ? Helper.getIconNameFromActivity(name) : null);
    } else if (call.method.equals("mSetAlternateIconName")) {
      String iconName = call.argument("iconName");
      IconChanger.enableIcon(context, iconName);
    } else if (call.method.equals("mGetApplicationIconBadgeNumber")) {
      result.error("Not supported", "Not supported on Android", null);
    } else if (call.method.equals("mSetApplicationIconBadgeNumber")) {
      result.error("Not supported", "Not supported on Android", null);
    } else {
      result.notImplemented();
    }
  }
}
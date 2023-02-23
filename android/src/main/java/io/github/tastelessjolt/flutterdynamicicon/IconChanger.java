package io.github.tastelessjolt.flutterdynamicicon;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

import io.flutter.Log;

public class IconChanger {
    /// Dividing the all the Activities in three parts
    // 1. All the `activity-alias`s which have `intent-filter` with action MAIN and category LAUNCHER
    //  When mSetAlternateIconName is passed `null`, all such `activity-alias`s are disabled
    //  When mSetAlternateIconName is passed an alias name, only that `activity-alias`s is enabled and 
    //        the rest of the `activity-alias` are disabled
    //
    // 2. All the other legitimate `activity`s which have `intent-filter` with action MAIN and category LAUNCHER
    //  When mSetAlternateIconName is passed `null`, all such `activity`s are enabled
    //  Similarly, when mSetAlternateIconName is passed a alias name, all such `activity`s are disabled
    //
    // 3. All other `activity`s
    //  These `activity`s are not touched 
    //
    static private String TAG = "flutterdynamicicon";

    public static String getCurrentEnabledAlias(Context context) {
        PackageManager pm = context.getPackageManager();

        for(ActivityInfo alias: getAvailableAliases(context, null)) {
            if(Helper.isComponentEnabled(pm, context.getPackageName(), alias.name)) {
                return alias.name;
            }
        }
        return null;
    }

    public static List<ActivityInfo> getAvailableAliases(Context context, List<String> include) {
        PackageManager pm = context.getPackageManager();

        List<ActivityInfo> aliases = new ArrayList<>();

        try {
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES | PackageManager.GET_DISABLED_COMPONENTS);
            for(ActivityInfo activityInfo: info.activities) {
                // Only checks among the `activity-alias`s, for current enabled alias
                if(activityInfo.targetActivity != null) {
                    if(include == null || include.contains(Helper.getIconNameFromActivity(activityInfo.name))) {
                        aliases.add(activityInfo);
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return aliases;
    }

    public static List<String> getAvailableAliasNames(Context context, List<String> include) {
        List<ActivityInfo> aliases = getAvailableAliases(context, include);
        List<String> names = new ArrayList<>();
        for(ActivityInfo activityInfo: aliases) {
            names.add(Helper.getIconNameFromActivity(activityInfo.name));
        }

        return names;
    }

    public static Map<String, byte[]> getAvailableAliasIcons(Context context, List<String> include) {
        PackageManager pm = context.getPackageManager();

        List<ActivityInfo> aliases = getAvailableAliases(context, include);
        Map<String, byte[]> map = new HashMap<>();
        for(ActivityInfo activityInfo: aliases) {
            Drawable icon = activityInfo.loadIcon(pm);
            map.put(Helper.getIconNameFromActivity(activityInfo.name), Helper.drawableToByteArray(icon));
        }

        return map;
    }

    public static void enableIcon(Context context, String activityName, String toastMessage) {
        PackageManager pm = context.getPackageManager();
        String currentlyEnabledIcon = getCurrentEnabledAlias(context);

        if (currentlyEnabledIcon == null && activityName == null) {
            // Currently enabled and request to enable activities are both the default activities
            return;
        }

        List<ComponentName> components = Helper.getComponentNames(context, activityName);
        for(ComponentName component: components) {
            if(currentlyEnabledIcon != null && currentlyEnabledIcon.equals(component.getClassName())) return;
            Log.d(TAG,String.format("Changing enabled activity-alias from %s to %s", currentlyEnabledIcon != null ? currentlyEnabledIcon : "default", component.getClassName()));
            pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }

        List<ComponentName> componentsToDisable;
        if (currentlyEnabledIcon != null) {
            componentsToDisable = Arrays.asList(new ComponentName(context.getPackageName(), currentlyEnabledIcon));
        }
        else {
            componentsToDisable = Helper.getComponentNames(context, null);
        }
        for(ComponentName toDisable: componentsToDisable) {
            pm.setComponentEnabledSetting(toDisable, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }

        if(toastMessage != null) {
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, toastMessage, duration);
            toast.show();
        }

        Activity activity = FlutterDynamicIconPlugin.getActivity();
        if(activity != null) {
            activity.finish();

            Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

}

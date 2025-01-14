package noorg.nothing.nope.no;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.topjohnwu.superuser.Shell;

import noorg.nothing.nope.no.util.RepoLoader;
import noorg.nothing.nope.no.widget.IconListPreference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import static noorg.nothing.nope.no.Constants.getBaseDir;
import static noorg.nothing.nope.no.SettingsActivity.getDarkenFactor;
import static noorg.nothing.nope.no.XposedApp.WRITE_EXTERNAL_PERMISSION;
import static noorg.nothing.nope.no.XposedApp.darkenColor;
import static noorg.nothing.nope.no.XposedApp.getArch;
import static noorg.nothing.nope.no.XposedApp.getPreferences;
import static noorg.nothing.nope.no.adapter.LogsHelper.isMainUser;

public class SettingsFragment extends BasePreferenceFragment implements Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private final static int[] PRIMARY_COLORS = new int[]{
            Color.parseColor("#F44336"),
            Color.parseColor("#E91E63"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#673AB7"),
            Color.parseColor("#3F51B5"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#03A9F4"),
            Color.parseColor("#00BCD4"),
            Color.parseColor("#009688"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#8BC34A"),
            Color.parseColor("#CDDC39"),
            Color.parseColor("#FFEB3B"),
            Color.parseColor("#FFC107"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#FF5722"),
            Color.parseColor("#795548"),
            Color.parseColor("#9E9E9E"),
            Color.parseColor("#607D8B"),
            Color.parseColor("#FA7298")
    };
//    private static final File mDisableForceClientSafetyNetFlag = new File(getBaseDir() + "conf/disable_force_client_safetynet");
//    private static final File mPretendXposedInstallerFlag = new File(getBaseDir() + "conf/pretend_xposed_installer");
//    private static final File mHideEdXposedManagerFlag = new File(getBaseDir() + "conf/hide_edxposed_manager");
    private static final File mEnableResourcesFlag = new File(getBaseDir() + "conf/enable_resources");
//    private static final File mDisableHiddenAPIBypassFlag = new File(getBaseDir() + "conf/disable_hidden_api_bypass");
//    private static final File mDynamicModulesFlag = new File(getBaseDir() + "conf/dynamicmodules");
    private static final File mWhiteListModeFlag = new File(getBaseDir() + "conf/usewhitelist");
//    private static final File mDeoptBootFlag = new File(getBaseDir() + "conf/deoptbootimage");
    private static final File mDisableVerboseLogsFlag = new File(getBaseDir() + "conf/disable_verbose_log");
    private static final File mDisableModulesLogsFlag = new File(getBaseDir() + "conf/disable_modules_log");
    private static final File mVerboseLogProcessID = new File(getBaseDir() + "log/all.pid");
    private static final File mModulesLogProcessID = new File(getBaseDir() + "log/error.pid");
    private static final File mUseSandHookFlag = new File(getBaseDir(), "conf/use_sandhook");
    private static final File mDisableSandHookFlag = new File(getBaseDir(), "conf/disable_sandhook");
    private static final String DIALOG_FRAGMENT_TAG = "list_preference_dialog";
    @SuppressLint("StaticFieldLeak")
    static SwitchPreference navBar;
    private final Preference.OnPreferenceChangeListener iconChange = (preference, newValue) -> {
        String act = ".WelcomeActivity";
        String[] iconsValues = new String[]{"MlgmXyysd", "DVDAndroid", "Hjmodi", "Rovo", "Cornie", "RovoOld", "Staol"};

        PackageManager pm = requireActivity().getPackageManager();
        String packName = requireActivity().getPackageName();

        for (String s : iconsValues) {
            pm.setComponentEnabledSetting(new ComponentName(packName, packName + act + s), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }

        act += iconsValues[Integer.parseInt((String) newValue)];

//        ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription(getString(R.string.app_name),
//                requireContext().getDrawable(XposedApp.iconsValues[Integer.parseInt(Objects.requireNonNull(getPreferences().getString("custom_icon", "0")))]),
//                XposedApp.getColor(requireContext()));
        ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription(getString(R.string.app_name),
                XposedApp.drawableToBitmap(requireContext().getDrawable(XposedApp.iconsValues[Integer.parseInt(Objects.requireNonNull(getPreferences().getString("custom_icon", "0")))])),
                XposedApp.getColor(requireContext()));
        requireActivity().setTaskDescription(tDesc);

        pm.setComponentEnabledSetting(new ComponentName(requireContext(), packName + act), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        return true;
    };
    private Preference mClickedPreference;
    private Preference downloadLocation;
    private Preference stopVerboseLog;
    private Preference stopLog;

    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs);

        Preference colors = findPreference("colors");
        downloadLocation = findPreference("download_location");
        stopVerboseLog = findPreference("stop_verbose_log");
        stopLog = findPreference("stop_log");

        ListPreference customIcon = findPreference("custom_icon");
        navBar = findPreference("nav_bar");

        Preference release_type_global = findPreference("release_type_global");
        Objects.requireNonNull(release_type_global).setOnPreferenceChangeListener((preference, newValue) -> {
            RepoLoader.getInstance().setReleaseTypeGlobal((String) newValue);
            return true;
        });

//        Preference enhancement_status = findPreference("enhancement_status");
//        Objects.requireNonNull(enhancement_status).setSummary(StatusInstallerFragment.isEnhancementEnabled() ? R.string.settings_summary_enhancement_enabled : R.string.settings_summary_enhancement);

        SwitchPreference darkStatusBarPref = findPreference("dark_status_bar");
        Objects.requireNonNull(darkStatusBarPref).setOnPreferenceChangeListener((preference, newValue) -> {
            requireActivity().getWindow().setStatusBarColor(darkenColor(XposedApp.getColor(requireActivity()), (boolean) newValue ? 0.85f : 1f));
            return true;
        });

        SwitchPreference prefUseSandHook = findPreference("use_sandhook");
        if (!isMainUser(getContext()) || mDisableSandHookFlag.exists() || getArch().contains("x86")) {
            Objects.requireNonNull(prefUseSandHook).setEnabled(false);
        }

        Objects.requireNonNull(prefUseSandHook).setChecked(mUseSandHookFlag.exists());
        prefUseSandHook.setOnPreferenceChangeListener((preference, newValue) -> setFlag(mUseSandHookFlag, (boolean) newValue));

//        SwitchPreference prefPassClientSafetyNet = findPreference("pass_client_safetynet");
//        Objects.requireNonNull(prefPassClientSafetyNet).setChecked(!mDisableForceClientSafetyNetFlag.exists());
//        prefPassClientSafetyNet.setOnPreferenceChangeListener((preference, newValue) -> !setFlag(mDisableHiddenAPIBypassFlag, !(boolean) newValue));
//
//        SwitchPreference prefPretendXposedInstaller = findPreference("pretend_xposed_installer");
//        Objects.requireNonNull(prefPretendXposedInstaller).setChecked(mPretendXposedInstallerFlag.exists());
//        prefPretendXposedInstaller.setOnPreferenceChangeListener((preference, newValue) -> setFlag(mPretendXposedInstallerFlag, (boolean) newValue));
//
//        SwitchPreference prefHideEdXposedManager = findPreference("hide_edxposed_manager");
//        Objects.requireNonNull(prefHideEdXposedManager).setChecked(mHideEdXposedManagerFlag.exists());
//        prefHideEdXposedManager.setOnPreferenceChangeListener((preference, newValue) -> setFlag(mHideEdXposedManagerFlag, (boolean) newValue));

        SwitchPreference prefWhiteListMode = findPreference("white_list_switch");
        Objects.requireNonNull(prefWhiteListMode).setChecked(mWhiteListModeFlag.exists());
        prefWhiteListMode.setOnPreferenceChangeListener((preference, newValue) -> setFlag(mWhiteListModeFlag, (boolean) newValue));

        SwitchPreference prefVerboseLogs = findPreference("disable_verbose_log");
        Objects.requireNonNull(prefVerboseLogs).setChecked(mDisableVerboseLogsFlag.exists());
        prefVerboseLogs.setOnPreferenceChangeListener((preference, newValue) -> setFlag(mDisableVerboseLogsFlag, (boolean) newValue));

        SwitchPreference prefModulesLogs = findPreference("disable_modules_log");
        Objects.requireNonNull(prefModulesLogs).setChecked(mDisableModulesLogsFlag.exists());
        prefModulesLogs.setOnPreferenceChangeListener((preference, newValue) -> setFlag(mDisableModulesLogsFlag, (boolean) newValue));

//        SwitchPreference prefEnableDeopt = findPreference("enable_boot_image_deopt");
//        Objects.requireNonNull(prefEnableDeopt).setChecked(mDeoptBootFlag.exists());
//        prefEnableDeopt.setOnPreferenceChangeListener((preference, newValue) -> setFlag(mDeoptBootFlag, (boolean) newValue));
//
//        SwitchPreference prefDynamicResources = findPreference("is_dynamic_modules");
//        Objects.requireNonNull(prefDynamicResources).setChecked(mDynamicModulesFlag.exists());
//        prefDynamicResources.setOnPreferenceChangeListener((preference, newValue) -> setFlag(mDynamicModulesFlag, (boolean) newValue));

        SwitchPreference prefEnableResources = findPreference("enable_resources");
        Objects.requireNonNull(prefEnableResources).setChecked(mEnableResourcesFlag.exists());
        prefEnableResources.setOnPreferenceChangeListener((preference, newValue) -> setFlag(mEnableResourcesFlag, (boolean) newValue));

//        SwitchPreference prefDisableHiddenAPIBypass = findPreference("disable_hidden_api_bypass");
//        Objects.requireNonNull(prefDisableHiddenAPIBypass).setChecked(mDisableHiddenAPIBypassFlag.exists());
//        prefDisableHiddenAPIBypass.setOnPreferenceChangeListener((preference, newValue) -> setFlag(mDisableHiddenAPIBypassFlag, (boolean) newValue));

        Objects.requireNonNull(colors).setOnPreferenceClickListener(this);
        Objects.requireNonNull(customIcon).setOnPreferenceChangeListener(iconChange);
        downloadLocation.setOnPreferenceClickListener(this);

    }

    private boolean setFlag(File flag, boolean enabled) {
        if (enabled) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(flag.getPath());
            } catch (FileNotFoundException e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        try {
                            flag.createNewFile();
                        } catch (IOException e1) {
                            Toast.makeText(getActivity(), e1.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } else {
            flag.delete();
        }
        return (enabled == flag.exists());
    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        requireActivity().getWindow().setStatusBarColor(darkenColor(XposedApp.getColor(requireActivity()), getDarkenFactor()));
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("theme") || key.equals("nav_bar") || key.equals("ignore_chinese") || key.equals("pure_black"))
            requireActivity().recreate();

        if (key.equals("force_english"))
            Toast.makeText(getActivity(), getString(R.string.warning_language), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        SettingsActivity act = (SettingsActivity) getActivity();
        if (act == null)
            return false;

        if (preference.getKey().equals("colors")) {
            new ColorChooserDialog.Builder(act, R.string.choose_color)
                    .backButton(R.string.back)
                    .allowUserColorInput(false)
                    .customColors(PRIMARY_COLORS, null)
                    .doneButton(R.string.ok)
                    .preselect(XposedApp.getColor(act)).show();
        } else if (preference.getKey().equals(downloadLocation.getKey())) {
            if (checkPermissions()) {
                mClickedPreference = downloadLocation;
                return false;
            }

            new FolderChooserDialog.Builder(act)
                    .cancelButton(android.R.string.cancel)
                    .initialPath(XposedApp.getDownloadPath())
                    .show();
        } else if (preference.getKey().equals(stopVerboseLog.getKey())) {
            new Runnable() {
                @Override
                public void run() {
                    BaseFragment.areYouSure(requireActivity(), getString(R.string.settings_summary_stop_log), (d, w) -> Shell.su("pkill -P $(cat " + mVerboseLogProcessID.getAbsolutePath() + ")").exec(), (d, w) -> {
                    });
                }
            };
        } else if (preference.getKey().equals(stopLog.getKey())) {
            new Runnable() {
                @Override
                public void run() {
                    BaseFragment.areYouSure(requireActivity(), getString(R.string.settings_summary_stop_log), (d, w) -> Shell.su("pkill -P $(cat " + mModulesLogProcessID.getAbsolutePath() + ")").exec(), (d, w) -> {
                    });
                }
            };
        }
        return true;
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_PERMISSION);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mClickedPreference != null) {
                new android.os.Handler().postDelayed(() -> onPreferenceClick(mClickedPreference), 500);
            }
        } else {
            Toast.makeText(getActivity(), R.string.permissionNotGranted, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof IconListPreference) {
            final IconListPreference.IconListPreferenceDialog f =
                    IconListPreference.IconListPreferenceDialog.newInstance(preference.getKey());
            f.setTargetFragment(this, 0);
            f.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}

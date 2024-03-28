package net.kdt.pojavlaunch;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.kdt.pickafile.FileListView;

import net.kdt.pojavlaunch.modloaders.modpacks.api.CurseforgeApi;
import net.kdt.pojavlaunch.modloaders.modpacks.api.MCBBSApi;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModLoader;
import net.kdt.pojavlaunch.modloaders.modpacks.api.ModrinthApi;
import net.kdt.pojavlaunch.modloaders.modpacks.models.CurseManifest;
import net.kdt.pojavlaunch.modloaders.modpacks.models.MCBBSPackMeta;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModrinthIndex;
import net.kdt.pojavlaunch.utils.ZipUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PojavZHTools {
    public static String DIR_EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String DIR_GAME_MODPACK = null;
    public static String DIR_GAME_DEFAULT;
    private PojavZHTools() {
    }

    public static int calculateBufferSize(long fileSize) {
        int kb = 1024;
        int mb = kb * kb;
        if (fileSize <= kb) { // <=1KB 2KB
            return kb * 2;
        } else if (fileSize <= mb) { // <=1MB  1MB
            return mb;
        } else if (fileSize <= 10 * mb) { // <=10MB  5MB
            return 5 * mb;
        } else if (fileSize <= 50 * mb) { // <=50MB 10MB
            return 10 * mb;
        } else if (fileSize <= 100 * mb) { // <=100MB 20MB
            return 20 * mb;
        } else { // >100MB 30MB
            return 30 * mb;
        }
    }

    public static File getGameDirPath(String gameDir) {
        if (gameDir != null) {
            if (gameDir.startsWith(Tools.LAUNCHERPROFILES_RTPREFIX))
                return new File(gameDir.replace(Tools.LAUNCHERPROFILES_RTPREFIX, Tools.DIR_GAME_HOME + "/"));
            else
                return new File(Tools.DIR_GAME_HOME, gameDir);
        }
        return new File(DIR_GAME_DEFAULT);
    }

    public static void shareFile(Context context, String fileName, String filePath) {
        Uri contentUri = DocumentsContract.buildDocumentUri(context.getString(R.string.storageProviderAuthorities), filePath);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.setType("text/plain");

        Intent sendIntent = Intent.createChooser(shareIntent, fileName);
        context.startActivity(sendIntent);
    }

    public static void shareFileAlertDialog(Context context, File file) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);

        builder.setTitle(context.getString(R.string.zh_file_share));
        builder.setMessage(context.getString(R.string.zh_file_share_message) + "\n" + file.getName());

        //分享
        DialogInterface.OnClickListener shareListener = (dialog, which) -> shareFile(context, file.getName(), file.getAbsolutePath());

        builder.setPositiveButton(context.getString(R.string.zh_file_share), shareListener)
                .setNegativeButton(context.getString(android.R.string.cancel), null);

        builder.show();
    }

    public static DialogInterface.OnClickListener deleteFileListener(Activity activity, FileListView fileListView, File file) {
        String fileName = file.getName();
        return (dialog, which) -> {
            // 显示确认删除的对话框
            AlertDialog.Builder deleteConfirmation = new AlertDialog.Builder(activity);

            deleteConfirmation.setTitle(activity.getString(R.string.zh_file_tips));
            deleteConfirmation.setMessage(activity.getString(R.string.zh_file_delete) + "\n" + file.getName());
            deleteConfirmation.setPositiveButton(activity.getString(R.string.global_delete), (dialog1, which1) -> {
                boolean deleted = deleteFile(file);
                if (deleted) {
                    Toast.makeText(activity, activity.getString(R.string.zh_file_deleted) + fileName, Toast.LENGTH_SHORT).show();
                }
                fileListView.refreshPath();
            });
            deleteConfirmation.setNegativeButton(activity.getString(android.R.string.cancel), null);
            deleteConfirmation.show();
        };
    }

    public static DialogInterface.OnClickListener renameFileListener(Activity activity, FileListView fileListView, File file) {
        String fileParent = file.getParent();
        String fileName = file.getName();
        return (dialog, which) -> { //重命名
            android.app.AlertDialog.Builder renameBuilder = new android.app.AlertDialog.Builder(activity);
            String suffix = fileName.substring(fileName.lastIndexOf('.')); //防止修改后缀名，先将后缀名分离出去
            EditText input = new EditText(activity);
            input.setText(fileName.substring(0, fileName.lastIndexOf(suffix)));
            renameBuilder.setTitle(activity.getString(R.string.zh_file_rename));
            renameBuilder.setView(input);
            renameBuilder.setPositiveButton(activity.getString(R.string.zh_file_rename), (dialog1, which1) -> {
                String newName = input.getText().toString();
                if (!newName.isEmpty()) {
                    File newFile = new File(fileParent, newName + suffix);
                    boolean renamed = file.renameTo(newFile);
                    if (renamed) {
                        Toast.makeText(activity, activity.getString(R.string.zh_file_renamed) + file.getName() + " -> " + newName + suffix, Toast.LENGTH_SHORT).show();
                        fileListView.refreshPath();
                    }
                } else {
                    Toast.makeText(activity, activity.getString(R.string.zh_file_rename_empty), Toast.LENGTH_SHORT).show();
                }
            });
            renameBuilder.setNegativeButton(activity.getString(android.R.string.cancel), null);
            renameBuilder.show();
        };
    }

    public static boolean deleteFile(File file) {
        return file.delete();
    }

    public static boolean deleteDir(File dir) { //删除一个非空文件夹
        if (dir == null || !dir.exists()) return false;

        if (dir.isFile()) {
            return dir.delete();
        }

        File[] files = dir.listFiles();
        if (files == null) return false; //没有权限则无法删除

        for (File file : files) {
            if (file.isFile()) file.delete();
            else deleteDir(file);
        }

        return dir.delete();
    }

    public static ModLoader installModPack(Context context, int type, File zipFile) throws Exception {
        try (ZipFile modpackZipFile = new ZipFile(zipFile)) {
            String zipName = zipFile.getName();
            String packName = zipName.substring(0, zipName.lastIndexOf('.'));
            ModLoader modLoader;
            switch (type) {
                case 1: //curseforge
                    ZipEntry curseforgeEntry = modpackZipFile.getEntry("manifest.json");
                    CurseManifest curseManifest = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(curseforgeEntry)),
                            CurseManifest.class);

                    modLoader = curseforgeModPack(context, zipFile, packName);
                    createProfiles(packName, curseManifest.name, modLoader.getVersionId());

                    return modLoader;
                case 2: //mcbbs
                    ZipEntry mcbbsEntry = modpackZipFile.getEntry("mcbbs.packmeta");

                    MCBBSPackMeta mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(mcbbsEntry)),
                            MCBBSPackMeta.class);

                    modLoader = mcbbsModPack(zipFile, packName);
                    createProfiles(packName, mcbbsPackMeta.name, modLoader.getVersionId());

                    return modLoader;
                case 3: // modrinth
                    ModrinthIndex modrinthIndex = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(ZipUtils.getEntryStream(modpackZipFile, "modrinth.index.json")),
                            ModrinthIndex.class); // 用于获取创建实例所需的数据

                    modLoader = modrinthModPack(zipFile, packName);
                    createProfiles(packName, modrinthIndex.name, modLoader.getVersionId());

                    return modLoader;
                default:
                    Tools.runOnUiThread(() -> Toast.makeText(context, context.getString(R.string.zh_select_modpack_local_not_supported), Toast.LENGTH_SHORT).show());
                    return null;
            }
        } finally {
            DIR_GAME_MODPACK = null;
            deleteFile(zipFile); // 删除文件（虽然文件通常来说并不会很大）
        }
    }

    public static int determineModpack(File modpack) throws Exception {
        String zipName = modpack.getName();
        String suffix = zipName.substring(zipName.lastIndexOf('.'));
        try (ZipFile modpackZipFile = new ZipFile(modpack)) {
            if (suffix.equals(".zip")) {
                ZipEntry mcbbsEntry = modpackZipFile.getEntry("mcbbs.packmeta");
                ZipEntry curseforgeEntry = modpackZipFile.getEntry("manifest.json");
                if (mcbbsEntry == null && curseforgeEntry != null) {
                    CurseManifest curseManifest = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(curseforgeEntry)),
                            CurseManifest.class);
                    if (verifyManifest(curseManifest)) return 1; //curseforge
                } else if (mcbbsEntry != null) {
                    MCBBSPackMeta mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(mcbbsEntry)),
                            MCBBSPackMeta.class);
                    if (verifyMCBBSPackMeta(mcbbsPackMeta)) return 2; // mcbbs
                }
            } else if (suffix.equals(".mrpack")) {
                ZipEntry entry = modpackZipFile.getEntry("modrinth.index.json");
                if (entry != null) {
                    ModrinthIndex modrinthIndex = Tools.GLOBAL_GSON.fromJson(
                            Tools.read(modpackZipFile.getInputStream(entry)),
                            ModrinthIndex.class);
                    if (verifyModrinthIndex(modrinthIndex)) return 3; //modrinth
                }
            }
            return 0;
        }
    }

    private static ModLoader curseforgeModPack(Context context, File zipFile, String packName) throws Exception {
        CurseforgeApi curseforgeApi = new CurseforgeApi(context.getString(R.string.curseforge_api_key));
        return curseforgeApi.installCurseforgeZip(zipFile, new File(Tools.DIR_GAME_HOME, "custom_instances/" + packName));
    }

    private static ModLoader modrinthModPack(File zipFile, String packName) throws Exception {
        ModrinthApi modrinthApi = new ModrinthApi();
        return modrinthApi.installMrpack(zipFile, new File(Tools.DIR_GAME_HOME, "custom_instances/" + packName));
    }

    private static ModLoader mcbbsModPack(File zipFile, String packName) throws Exception {
        MCBBSApi mcbbsApi = new MCBBSApi();
        return mcbbsApi.installMCBBSZip(zipFile, new File(Tools.DIR_GAME_HOME, "custom_instances/" + packName));
    }

    private static void createProfiles(String modpackName, String profileName, String versionId) {
        MinecraftProfile profile = new MinecraftProfile();
        profile.gameDir = "./custom_instances/" + modpackName;
        profile.name = profileName;
        profile.lastVersionId = versionId;

        LauncherProfiles.mainProfileJson.profiles.put(modpackName, profile);
        LauncherProfiles.write();
    }

    public static boolean verifyManifest(CurseManifest manifest) { //检测是否为curseforge整合包(通过manifest.json内的数据进行判断)
        if (!"minecraftModpack".equals(manifest.manifestType)) return false;
        if (manifest.manifestVersion != 1) return false;
        if (manifest.minecraft == null) return false;
        if (manifest.minecraft.version == null) return false;
        if (manifest.minecraft.modLoaders == null) return false;
        return manifest.minecraft.modLoaders.length >= 1;
    }

    public static boolean verifyModrinthIndex(ModrinthIndex modrinthIndex) { //检测是否为modrinth整合包(通过modrinth.index.json内的数据进行判断)
        if (!"minecraft".equals(modrinthIndex.game)) return false;
        if (modrinthIndex.formatVersion != 1) return false;
        return modrinthIndex.dependencies != null;
    }

    public static boolean verifyMCBBSPackMeta(MCBBSPackMeta mcbbsPackMeta) { //检测是否为MCBBS整合包(通过mcbbs.packmeta内的数据进行判断)
        if (!"minecraftModpack".equals(mcbbsPackMeta.manifestType)) return false;
        if (mcbbsPackMeta.manifestVersion != 2) return false;
        if (mcbbsPackMeta.addons == null) return false;
        if (mcbbsPackMeta.addons[0].id == null) return false;
        return (mcbbsPackMeta.addons[0].version != null);
    }
}

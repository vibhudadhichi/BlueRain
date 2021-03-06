package com.technologx.bluerain.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.technologx.bluerain.R;
import com.technologx.bluerain.dialogs.BRDialogs;
import com.technologx.bluerain.fragments.ZooperFragment;
import com.technologx.bluerain.holders.ZooperButtonHolder;
import com.technologx.bluerain.holders.ZooperWidgetHolder;
import com.technologx.bluerain.holders.lists.FullListHolder;
import com.technologx.bluerain.models.ZooperWidget;
import com.technologx.bluerain.tasks.CopyFilesToStorage;
import com.technologx.bluerain.utilities.PermissionsUtils;
import com.technologx.bluerain.utilities.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ZooperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Drawable[] icons = new Drawable[2];
    private final Context context;
    private final Drawable wallpaper;
    private final View layout;
    private final boolean everythingInstalled;
    private ArrayList<ZooperWidget> widgets;
    private final ZooperFragment mFragment;
    private int extraCards = 0;

    public ZooperAdapter(Context context, View layout,
                         Drawable wallpaper, boolean appsInstalled, ZooperFragment mFragment) {
        this.context = context;
        this.layout = layout;
        this.wallpaper = wallpaper;
        setupWidgets();
        this.everythingInstalled = (appsInstalled && areAssetsInstalled());
        this.extraCards = this.everythingInstalled ? 0 : 2;
        this.mFragment = mFragment;
    }

    private void setupWidgets() {
        if (FullListHolder.get().zooperList().getList() != null) {
            if (widgets != null) {
                widgets.clear();
            } else {
                widgets = new ArrayList<>();
            }
            widgets.addAll(FullListHolder.get().zooperList().getList());
            notifyItemRangeInserted(0, widgets.size());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (!everythingInstalled) {
            switch (i) {
                case 0:
                case 1:
                    return new ZooperButtonHolder(
                            inflater.inflate(R.layout.item_zooper_button, parent, false),
                            new ZooperButtonHolder.OnZooperButtonClickListener() {
                                @Override
                                public void onClick(int position) {
                                    onButtonClick(position);
                                }
                            });
                default:
                    return new ZooperWidgetHolder(
                            inflater.inflate(R.layout.item_widget_preview, parent, false),
                            wallpaper);
            }
        } else {
            return new ZooperWidgetHolder(
                    inflater.inflate(R.layout.item_widget_preview, parent, false), wallpaper);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!everythingInstalled) {
            switch (position) {
                case 0:
                case 1:
                    ((ZooperButtonHolder) holder).setItem(context, position);
                    break;
                default:
                    ((ZooperWidgetHolder) holder).setItem(context, widgets.get(position - 2));
                    break;
            }
        } else {
            ((ZooperWidgetHolder) holder).setItem(context, widgets.get(position));
        }

    }

    @Override
    public int getItemCount() {
        return widgets != null ? widgets.size() + extraCards : extraCards;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private boolean areAssetsInstalled() {
        boolean assetsInstalled = false;

        String fileToIgnore1 = "material-design-iconic-font-v2.2.0.ttf",
                fileToIgnore2 = "materialdrawerfont.ttf",
                fileToIgnore3 = "materialdrawerfont-font-v5.0.0.ttf",
                fileToIgnore4 = "google-material-font-v2.2.0.1.original.ttf";

        AssetManager assetManager = context.getAssets();
        String[] files = null;
        String[] folders = new String[]{"fonts", "iconsets", "bitmaps"};

        for (String folder : folders) {
            try {
                files = assetManager.list(folder);
            } catch (IOException e) {
                //Do nothing
            }

            if (files != null && files.length > 0) {
                for (String filename : files) {
                    if (filename.contains(".")) {
                        if (!filename.equals(fileToIgnore1) && !filename.equals(fileToIgnore2)
                                && !filename.equals(fileToIgnore3) && !filename.equals
                                (fileToIgnore4)) {
                            File file = new File(Environment.getExternalStorageDirectory()
                                    + "/ZooperWidget/" + getFolderName(folder) + "/" + filename);
                            assetsInstalled = file.exists();
                        }
                    }
                }
            }
        }

        return assetsInstalled;
    }

    private String getFolderName(String folder) {
        switch (folder) {
            case "fonts":
                return "Fonts";
            case "iconsets":
                return "IconSets";
            case "bitmaps":
                return "Bitmaps";
            default:
                return folder;
        }
    }

    public void installAssets() {
        String[] folders = new String[]{"fonts", "iconsets", "bitmaps"};
        for (String folderName : folders) {
            String dialogContent =
                    context.getResources().getString(
                            R.string.copying_assets, getFolderName(folderName));
            MaterialDialog dialog = new MaterialDialog.Builder(context)
                    .content(dialogContent)
                    .progress(true, 0)
                    .cancelable(false)
                    .show();
            new CopyFilesToStorage(context, layout, dialog, folderName).execute();
        }
    }

    private void onButtonClick(int position) {
        switch (position) {
            case 0:
                //Open dialog
                ArrayList<String> apps = new ArrayList<>();
                if (!Utils.isAppInstalled(context, "org.zooper.zwpro")) {
                    apps.add(Utils.getStringFromResources(context, R.string
                            .zooper_app));
                }
                if (context.getResources().getBoolean(R.bool.mu_needed) &&
                        !Utils.isAppInstalled(context, "com.batescorp" +
                                ".notificationmediacontrols.alpha")) {
                    apps.add(Utils.getStringFromResources(context, R.string.mu_app));
                }
                if (context.getResources().getBoolean(R.bool.kolorette_needed) &&
                        !Utils.isAppInstalled(context, "com.arun.themeutil" +
                                ".kolorette")) {
                    apps.add(Utils.getStringFromResources(context, R.string
                            .kolorette_app));
                }
                if (apps.size() > 0) {
                    BRDialogs.showZooperAppsDialog(context, apps);
                } else {
                    if (mFragment != null) mFragment.showInstalledAppsSnackbar();
                }
                break;
            case 1:
                //Install assets
                if (!areAssetsInstalled()) {
                    PermissionsUtils.checkPermission(context, Manifest.permission
                            .WRITE_EXTERNAL_STORAGE, new
                            PermissionsUtils.PermissionRequestListener() {

                                @Override
                                public void onPermissionRequest() {
                                    PermissionsUtils.requestStoragePermission(
                                            (Activity) context);
                                }

                                @Override
                                public void onPermissionDenied() {
                                    BRDialogs.showPermissionNotGrantedDialog(context);
                                }

                                @Override
                                public void onPermissionCompletelyDenied() {
                                    BRDialogs.showPermissionNotGrantedDialog(context);
                                }

                                @Override
                                public void onPermissionGranted() {
                                    installAssets();
                                }
                            });
                } else {
                    if (mFragment != null) mFragment.showInstalledAssetsSnackbar();
                }
                break;
        }
    }

}
package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.DIR_EXTERNAL_STORAGE_PATH;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.kdt.pickafile.FileListView;
import com.kdt.pickafile.FileSelectedListener;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.io.File;

public class SelectFileFragment extends Fragment {
    public static final String TAG = "SelectFileFragment";
    public static final String BUNDLE_DEFAULT_PATH = "bundle_default_path";
    private Button mReturnButton, mRefreshButton;
    private FileListView mFileListView;
    private TextView mFilePathView;
    private String mDefaultPath;

    public SelectFileFragment() {
        super(R.layout.fragment_files);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        bindViews(view);
        parseBundle();

        mFileListView.lockPathAt(new File(mDefaultPath));
        mFileListView.setDialogTitleListener((title) -> mFilePathView.setText(removeLockPath(title)));
        mFileListView.refreshPath();

        mFileListView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

                builder.setTitle(getString(R.string.zh_file_select_file_tip));
                builder.setMessage(getString(R.string.zh_file_select_file_message));

                DialogInterface.OnClickListener selectFile = (dialog, i) -> {
                    DEFAULT_PREF.edit().putString("dirDefaultPath", path).apply();
                    ExtraCore.setValue(ExtraConstants.FILE_SELECTOR, removeLockPath(path));
                    Tools.removeCurrentFragment(requireActivity());
                };

                builder.setPositiveButton(getString(R.string.global_delete), selectFile)
                        .setNegativeButton(getString(android.R.string.cancel), null);

                builder.show();
            }

            @Override
            public void onItemLongClick(File file, String path) {
            }
        });

        mReturnButton.setOnClickListener(v -> requireActivity().onBackPressed());

        mRefreshButton.setOnClickListener(v -> mFileListView.refreshPath());
    }

    private String removeLockPath(String path) {
        return path.replace(mDefaultPath, ".");
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mFileListView = view.findViewById(R.id.zh_files);
        mFilePathView = view.findViewById(R.id.zh_files_current_path);

        view.findViewById(R.id.zh_files_add_file_button).setVisibility(View.GONE);
        view.findViewById(R.id.zh_files_create_folder_button).setVisibility(View.GONE);
        view.findViewById(R.id.zh_files_help_button).setVisibility(View.GONE);
    }

    private void parseBundle() {
        Bundle bundle = getArguments();
        if (bundle == null) return;
        mDefaultPath = bundle.getString(BUNDLE_DEFAULT_PATH, DIR_EXTERNAL_STORAGE_PATH);
    }
}
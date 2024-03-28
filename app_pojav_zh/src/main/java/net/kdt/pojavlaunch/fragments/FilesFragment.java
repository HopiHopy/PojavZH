package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.calculateBufferSize;
import static net.kdt.pojavlaunch.PojavZHTools.deleteFileListener;
import static net.kdt.pojavlaunch.PojavZHTools.getGameDirPath;
import static net.kdt.pojavlaunch.PojavZHTools.renameFileListener;
import static net.kdt.pojavlaunch.PojavZHTools.shareFile;
import static net.kdt.pojavlaunch.Tools.DIR_GAME_HOME;
import static net.kdt.pojavlaunch.Tools.getFileName;
import static net.kdt.pojavlaunch.fragments.SelectFileFragment.BUNDLE_DEFAULT_PATH;
import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.pickafile.FileListView;
import com.kdt.pickafile.FileSelectedListener;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class FilesFragment extends Fragment {
    public static final String TAG = "FilesFragment";
    public static final String BUNDLE_PATH = "bundle_path";
    private Button mReturnButton, mAddFileButton, mCreateFolderButton, mRefreshButton;
    private ImageButton mHelpButton;
    private FileListView mFileListView;
    private TextView mFilePathView;
    private String mPath;

    public FilesFragment() {
        super(R.layout.fragment_files);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String value = (String) ExtraCore.consumeValue(ExtraConstants.FILE_SELECTOR);
        if(value != null){
            Uri uri = Uri.fromFile(getGameDirPath(value));
            new CopyFile().execute(uri);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        bindViews(view);
        parseBundle();

        mFileListView.lockPathAt(new File(mPath));
        mFileListView.setDialogTitleListener((title)->mFilePathView.setText(removeLockPath(title)));
        mFileListView.refreshPath();

        mFileListView.setFileSelectedListener(new FileSelectedListener() {
            @Override
            public void onFileSelected(File file, String path) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                int caciocavallo = file.getPath().indexOf("caciocavallo");
                int lwjgl3 = file.getPath().indexOf("lwjgl3");

                builder.setTitle(getString(R.string.zh_file_tips));
                if (caciocavallo == -1 && lwjgl3 == -1) builder.setMessage(getString(R.string.zh_file_message));
                else builder.setMessage(getString(R.string.zh_file_message) + "\n" + getString(R.string.zh_file_message_main));

                //分享
                DialogInterface.OnClickListener shareListener = (dialog, which) -> shareFile(requireContext(), file.getName(), file.getAbsolutePath());

                builder.setPositiveButton(getString(R.string.global_delete), deleteFileListener(requireActivity(), mFileListView, file))
                        .setNegativeButton(getString(R.string.zh_file_rename), renameFileListener(requireActivity(), mFileListView, file))
                        .setNeutralButton(getString(R.string.zh_file_share), shareListener);

                builder.show();
            }

            @Override
            public void onItemLongClick(File file, String path) {
                PojavZHTools.shareFileAlertDialog(requireContext(), file);
            }
        });

        mReturnButton.setOnClickListener(v -> requireActivity().onBackPressed());
        mAddFileButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_DEFAULT_PATH, DEFAULT_PREF.getString("dirDefaultPath", PojavZHTools.DIR_EXTERNAL_STORAGE_PATH));

            Tools.swapFragment(requireActivity(), SelectFileFragment.class, SelectFileFragment.TAG, true, bundle);
        });

        mCreateFolderButton.setOnClickListener(v -> {
            EditText editText = new EditText(getContext());
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.folder_dialog_insert_name)
                    .setView(editText)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.folder_dialog_create, (dialog, which) -> {
                        File folder = new File(mFileListView.getFullPath(), editText.getText().toString());
                        boolean success = folder.mkdir();
                        if(success){
                            mFileListView.listFileAt(new File(mFileListView.getFullPath(),editText.getText().toString()));
                        }else{
                            mFileListView.refreshPath();
                        }
                    }).show();
        });
        mRefreshButton.setOnClickListener(v -> mFileListView.refreshPath());
        mHelpButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_files_tile));
            builder.setMessage(getString(R.string.zh_help_files_message));
            builder.setPositiveButton(getString(R.string.zh_help_ok), null);

            builder.show();
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class CopyFile extends AsyncTask<Uri, Void, Void> {
        @Override
        protected Void doInBackground(Uri... uris) {
            Uri fileUri = uris[0];
            String fileName = getFileName(requireContext(), fileUri);
            File inputFile = new File(Objects.requireNonNull(fileUri.getPath()));
            File outputFile = new File(mFileListView.getFullPath().getAbsolutePath(), fileName);
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri)) {
                if (inputStream != null) {
                    try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[calculateBufferSize(inputFile.length())];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast.makeText(requireContext(), getString(R.string.zh_file_added), Toast.LENGTH_SHORT).show();
            mFileListView.refreshPath();
        }
    }

    private String removeLockPath(String path){
        return path.replace(mPath, ".");
    }

    private void bindViews(@NonNull View view) {
        mReturnButton = view.findViewById(R.id.zh_files_return_button);
        mAddFileButton = view.findViewById(R.id.zh_files_add_file_button);
        mCreateFolderButton = view.findViewById(R.id.zh_files_create_folder_button);
        mRefreshButton = view.findViewById(R.id.zh_files_refresh_button);
        mHelpButton = view.findViewById(R.id.zh_files_help_button);
        mFileListView = view.findViewById(R.id.zh_files);
        mFilePathView = view.findViewById(R.id.zh_files_current_path);
    }

    private void parseBundle(){
        Bundle bundle = getArguments();
        if(bundle == null) return;
        mPath = bundle.getString(BUNDLE_PATH, DIR_GAME_HOME);
    }
}


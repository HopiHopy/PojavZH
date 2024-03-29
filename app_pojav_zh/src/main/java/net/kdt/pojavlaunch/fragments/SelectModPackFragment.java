package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavZHTools.calculateBufferSize;
import static net.kdt.pojavlaunch.Tools.getFileName;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.kdt.pojavlaunch.PojavZHTools;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.contracts.OpenDocumentWithExtension;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class SelectModPackFragment extends Fragment {
    public static final String TAG = "SelectModPackFragment";
    private ActivityResultLauncher<Object> openDocumentLauncher;
    private File modPackFile;

    public SelectModPackFragment(){
        super(R.layout.fragment_select_modpack);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDocumentLauncher = registerForActivityResult(
                new OpenDocumentWithExtension(null),
                result -> {
                    if (result != null) {
                        Toast.makeText(requireContext(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
                        //使用AsyncTask在后台线程中执行文件复制
                        new CopyFile().execute(result);
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.zh_modpack_button_search_modpack).setOnClickListener(v -> {
            if (PojavZHTools.DIR_GAME_MODPACK == null) {
                Tools.swapFragment(requireActivity(), SearchModFragment.class, SearchModFragment.TAG, false, null);
            } else {
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.zh_modpack_button_local_modpack).setOnClickListener(v -> {
            if (PojavZHTools.DIR_GAME_MODPACK == null) {
                Toast.makeText(requireActivity(), getString(R.string.zh_select_modpack_local_tip), Toast.LENGTH_SHORT).show();
                openDocumentLauncher.launch(null);
            } else {
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show();
            }
        });
        view.findViewById(R.id.zh_modpack_help_button).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle(getString(R.string.zh_help_modpack_title));
            builder.setMessage(getString(R.string.zh_help_modpack_message));
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
            modPackFile = new File(Tools.DIR_CACHE, fileName);
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri)) {
                if (inputStream != null) {
                    try (OutputStream outputStream = new FileOutputStream(modPackFile)) {
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
            PojavZHTools.DIR_GAME_MODPACK = modPackFile.getAbsolutePath();
            ExtraCore.setValue(ExtraConstants.INSTALL_LOCAL_MODPACK, true);
        }
    }
}

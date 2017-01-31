package de.mkrtchyan.recoverytools;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.sufficientlysecure.rootcommands.Toolbox;

import java.io.File;
import java.util.ArrayList;

import de.mkrtchyan.utils.Common;
import de.mkrtchyan.utils.FileChooserDialog;

/**
 * Copyright (c) 2016 Aschot Mkrtchyan
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class ScriptManagerFragment extends Fragment {

    private final String CMD_END = ";";
    File mStartFile;
    ArrayList<File> mFileList;
    ArrayAdapter<String> mFileNameAdapter;
    ListView mQueue;
    FileChooserDialog mFileChooser;
    String[] mAllowedEXT = {".zip"};
    private Context mContext;
    private View mRootView;

    public ScriptManagerFragment() {
        // Required empty public constructor
    }

    public static ScriptManagerFragment newInstance(RashrActivity activity, File ZIP) {
        ScriptManagerFragment fragment = new ScriptManagerFragment();
        fragment.setContext(activity);
        fragment.setStartFile(ZIP);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_script_manager, container, false);
        mFileNameAdapter = new ArrayAdapter<>(mContext, R.layout.custom_list_item);
        mFileList = new ArrayList<>();
        mQueue = (ListView) mRootView.findViewById(R.id.lvQueue);
        mQueue.setAdapter(mFileNameAdapter);
        mQueue.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mFileList.remove(position);
                mFileNameAdapter.clear();
                for (File i : mFileList) {
                    mFileNameAdapter.add(i.getName());
                }
            }
        });
        if (mStartFile != null) {
            if (mStartFile.exists()) {
                if (Common.stringEndsWithArray(mStartFile.getName(), mAllowedEXT)) {
                    addFileToQueue(mStartFile);
                } else {
                    Toast.makeText(mContext, R.string.wrong_format, Toast.LENGTH_SHORT).show();
                }
            }
        }

        AppCompatButton AddZip = (AppCompatButton) mRootView.findViewById(R.id.addZip);
        AddZip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFileChooser = new FileChooserDialog(mContext);
                File startFolder = Environment.getExternalStorageDirectory();
                if (mFileList.size() > 0) {
                    startFolder = mFileList.get(mFileList.size() - 1);
                    if (startFolder.isFile()) {
                        startFolder = startFolder.getParentFile();
                    }
                }
                mFileChooser.setStartFolder(startFolder);
                mFileChooser.setOnFileChooseListener(new FileChooserDialog.OnFileChooseListener() {
                    @Override
                    public void OnFileChoose(File file) {
                        addFileToQueue(file);
                    }
                });
                mFileChooser.setAllowedEXT(mAllowedEXT);
                mFileChooser.setBrowseUpAllowed(true);
                mFileChooser.setWarn(false);
                mFileChooser.show();
            }
        });
        AppCompatButton FlashZip = (AppCompatButton) mRootView.findViewById(R.id.bFlashZip);
        FlashZip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatCheckBox cbBakSystem = (AppCompatCheckBox) mRootView.findViewById(R.id.cbBackupSystem);
                AppCompatCheckBox cbBakData = (AppCompatCheckBox) mRootView.findViewById(R.id.cbBackupData);
                AppCompatCheckBox cbBakCache = (AppCompatCheckBox) mRootView.findViewById(R.id.cbBackupCache);
                AppCompatCheckBox cbBakRecovery = (AppCompatCheckBox) mRootView.findViewById(R.id.cbBackupRecovery);
                AppCompatCheckBox cbBakBoot = (AppCompatCheckBox) mRootView.findViewById(R.id.cbBackupBoot);
                AppCompatCheckBox cbSkipMD5 = (AppCompatCheckBox) mRootView.findViewById(R.id.cbSkipMD5);
                AppCompatEditText etBakName = (AppCompatEditText) mRootView.findViewById(R.id.etBackupName);
                AppCompatCheckBox cbWipeCache = (AppCompatCheckBox) mRootView.findViewById(R.id.cbWipeCache);
                AppCompatCheckBox cbWipeDalvik = (AppCompatCheckBox) mRootView.findViewById(R.id.cbWipeDalvik);
                AppCompatCheckBox cbWipeData = (AppCompatCheckBox) mRootView.findViewById(R.id.cbWipeData);
                final StringBuilder command = new StringBuilder();
                command.append("echo #####Script created by Rashr#####;");
                if (cbBakBoot.isChecked() || cbBakCache.isChecked() || cbBakData.isChecked()
                        || cbBakRecovery.isChecked() || cbBakSystem.isChecked()) {
                    command.append("backup ");
                    if (cbBakBoot.isChecked()) command.append("B");
                    if (cbBakCache.isChecked()) command.append("C");
                    if (cbBakData.isChecked()) command.append("D");
                    if (cbBakRecovery.isChecked()) command.append("R");
                    if (cbBakSystem.isChecked()) command.append("S");
                    if (cbSkipMD5.isChecked()) command.append("M");

                    CharSequence BackupName = etBakName.getText();
                    if (BackupName != null && !BackupName.equals("")) {
                        command.append(" ");
                        command.append(BackupName);
                    }
                    command.append(CMD_END);

                }

                if (cbWipeCache.isChecked()) command.append("wipe cache;");
                if (cbWipeDalvik.isChecked()) command.append("wipe dalvik;");
                if (cbWipeData.isChecked()) command.append("wipe data;");

                for (File i : mFileList) {
                    command.append("install ");
                    command.append(i.getAbsolutePath());
                    command.append(CMD_END);
                }

                if (!command.toString().equals("")) {
                    String commands = "";
                    int index = 0;
                    for (String i : command.toString().split(CMD_END)) {
                        if (!i.equals("")) {
                            if (index > 0) {
                                commands += index++ + ". " + i + "\n";
                            } else {
                                index++;
                            }
                        }
                    }
                    final AlertDialog.Builder CommandsPreview = new AlertDialog.Builder(mContext);
                    CommandsPreview.setTitle(R.string.recovery_script_review);
                    CommandsPreview.setPositiveButton(R.string.run, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                for (String split : command.toString().split(";")) {
                                    if (!split.equals("")) {
                                        RashrApp.SHELL.execCommand("echo " + split + " >> /cache/recovery/openrecoveryscript");
                                    }
                                }
                                RashrApp.TOOLBOX.reboot(Toolbox.REBOOT_RECOVERY);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    CommandsPreview.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    CommandsPreview.setMessage(commands);
                    CommandsPreview.show();
                } else {
                    Toast.makeText(mContext, "No job to do :)", Toast.LENGTH_LONG).show();
                }
            }
        });
        return mRootView;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setStartFile(File file) {
        mStartFile = file;
    }

    public void addFileToQueue(File file) {
        if (file.exists()) {
            mFileList.add(file);
            mFileNameAdapter.add(file.getName());
        }
    }
}

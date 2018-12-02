package com.ttdn.apptrimvideo;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class SimpleDirectoryChooserActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {


    private TextView txtCurrentPath;
    private ImageView btnBackFolder;
    private ImageView btnNewFolder;
    private Button btnOk;
    private Button btnCancel;

    private ListView listFolder;
    private ArrayAdapter adapter;
    private ArrayList<String> path = new ArrayList<>();
    private ArrayList<String> itemFolder = new ArrayList<>();

    private String initDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath();
    private Handler handler;


    private static final int UPDATE_VIEW = 1;
    public static final String INIT_DIRECTORY_EXTRA = "INIT_DIRECTORY_EXTRA";
    public static final String RESULT_DIRECTORY_EXTRA = "RESULT_DIRECTORY_EXTRA";
    public static final int RESULT_CODE_DIRECTORY_SELECTED = 8;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getInit();

        setContentView(R.layout.activity_simple_directory_chooser);

        txtCurrentPath = findViewById(R.id.txtCurrentPath);
        btnBackFolder = findViewById(R.id.btnBackFolder);
        btnNewFolder = findViewById(R.id.btnNewFolder);
        btnOk = findViewById(R.id.btnOK);
        btnCancel = findViewById(R.id.btnCancel);

        btnBackFolder.setOnClickListener(myOnClickListener);
        btnNewFolder.setOnClickListener(myOnClickListener);
        btnOk.setOnClickListener(myOnClickListener);
        btnCancel.setOnClickListener(myOnClickListener);

        listFolder = findViewById(R.id.listFolder);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemFolder);
        listFolder.setAdapter(adapter);
        listFolder.setOnItemClickListener(this);

        handler = new Handler(Looper.getMainLooper());
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.arg1 == UPDATE_VIEW) {
                    String path = (String) message.obj;
                    getDir(path);
                    return true;
                }
                return false;
            }
        });
        getDir(initDirectory);
    }

    private void getDir(String dirPath) {
        if (!dirPath.equals(File.separator)) {
            //!root
            txtCurrentPath.setText(dirPath);
        } else {
            txtCurrentPath.setText("Root");
        }

        File file = new File(dirPath);
        if (file.canWrite()) {
            btnOk.setEnabled(true);
            btnNewFolder.setEnabled(true);
            btnNewFolder.setColorFilter(getResources().getColor(R.color.colorAccent));
        } else {
            btnOk.setEnabled(false);
            btnNewFolder.setEnabled(false);
            btnNewFolder.setColorFilter(null);
        }

        File[] files = file.listFiles();
        itemFolder.clear();
        path.clear();


        if (file != null) {
            Arrays.sort(files, fileComparator);
            for (File f : files) {
                if (!f.isHidden() && f.canRead()) {
                    if (f.isDirectory()) {
                        path.add(f.getPath());
                        itemFolder.add(f.getName() + File.separator);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    Comparator<? super File> fileComparator = new Comparator<File>() {
        @Override
        public int compare(File file, File t1) {
            if (file.isDirectory()) {
                if (t1.isDirectory()) {
                    return String.valueOf(file.getName().toLowerCase()).compareTo(t1.getName().toLowerCase());
                } else {
                    return -1;
                }
            } else {
                if (t1.isDirectory()) {
                    return 1;
                } else {
                    return String.valueOf(file.getName().toLowerCase()).compareTo(t1.getName().toLowerCase());
                }
            }
        }
    };

    private View.OnClickListener myOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == btnBackFolder.getId()) {
                File file = new File(txtCurrentPath.getText().toString()).getParentFile();
                if (file != null && file.isDirectory()) {
                    getDir(file.getPath());
                }
            } else if (id == btnNewFolder.getId()) {
                createNewFolderDialog();
            } else if (id == btnOk.getId()) {
                Intent intent = new Intent();
                intent.putExtra(RESULT_DIRECTORY_EXTRA, txtCurrentPath.getText().toString());
                setResult(RESULT_CODE_DIRECTORY_SELECTED, intent);
                finish();
            } else if (id == btnCancel.getId()) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    };

    private void createNewFolderDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.new_folder_layout, null);

        EditText editText = view.findViewById(R.id.editNewFolder);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogAppCompatStyle);
        builder.setTitle("Create new folder")
                .setView(view)
                .setPositiveButton("OK", (dialog, which) -> {
                    if (!TextUtils.isEmpty(editText.getText().toString())) {
                        File file = new File(txtCurrentPath.getText().toString() + File.separator + editText.getText());
                        if (!file.exists()) {
                            if (!file.mkdir()) {
                                Toast.makeText(getApplicationContext(), "Can't create a new folder here", Toast.LENGTH_SHORT).show();
                            } else {
                                loadData(txtCurrentPath.getText().toString());
                            }
                        }
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void loadData(String path) {
        Message message = new Message();
        message.arg1 = UPDATE_VIEW;
        message.obj = path;
        handler.sendMessage(message);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String s = path.get(position);
        File file = new File(s);
        if (file.isDirectory()) {
            if (file.canRead()) {
                loadData(s);
            }
        }
    }

    public void getInit() {
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            initDirectory = intent.getStringExtra(INIT_DIRECTORY_EXTRA);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}

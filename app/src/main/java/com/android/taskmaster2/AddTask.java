package com.android.taskmaster2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.room.Room;

import android.content.Intent;
import android.os.FileUtils;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.Team;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AddTask extends AppCompatActivity {

    private static final int REQUEST_FOR_FILE = 188;
    public static final String TASK = "task-container";
    private static final String TAG = "AddTaskActivity";

    EditText taskTitle;
    private Button chooseFileBtn;
    String title;
    String exctension;

    private Button chooseFileBtn ;
    private TaskDao taskDao;
    String taskState;
    String theTeam;
    List<Team> TeamMembers;
    Team teamType;
    private String uploadedFileName;
    String title;
    String body;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

//        try {
//            Amplify.addPlugin(new AWSApiPlugin());
//            Amplify.addPlugin(new AWSDataStorePlugin());
//            Amplify.configure(getApplicationContext());
//            Log.i("Tutorial", "Initialized Amplify");
//        } catch (AmplifyException failure) {
//            Log.e("Tutorial", "Could not initialize Amplify", failure);
//        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();

        TeamMembers = new ArrayList<>();
        Amplify.API.query(ModelQuery.list(Team.class),
                response -> {
                    Log.i(TAG, "onCreate: Queeeeeery" + response.getData());
                    for (Team team : response.getData()) {
                        TeamMembers.add(team);
                    }
                },
                error -> Log.e(TAG, "onCreate: ERRRRRRRR" + error.toString())
        );

        Spinner spinner = findViewById(R.id.spinner);

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.task_state_menu, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                taskState = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                String taskState = (String) parent.getItemAtPosition(0);

            }
        });

        Spinner spinner2 = findViewById(R.id.teamspinner);

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.team_menu, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                theTeam = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                String theTeam = (String) parent.getItemAtPosition(0);

            }
        });

        chooseFileBtn = findViewById(R.id.uploadbtn);


        AppDB database = Room.databaseBuilder(getApplicationContext(), AppDB.class, TASK)
                .allowMainThreadQueries().build();
        taskDao = database.taskDao();

        Button addTaskBtn = AddTask.this.findViewById(R.id.addTaskBtn);

        addTaskBtn.setOnClickListener(v -> {


//            Team teams = Team .builder().name(theTeam).build();


            taskTitle = AddTask.this.findViewById(R.id.task_title_input);
            EditText taskDesc = AddTask.this.findViewById(R.id.task_desc);
            title = taskTitle.getText().toString();
            body = taskDesc.getText().toString();


//            if (!taskTitle.getText().toString().equals("") && !taskDesc.getText().toString().equals("")) {

                TaskItem taskItem = new TaskItem(title, body);
                taskItem.setState(taskState);
                taskDao.insertOneTask(taskItem);

                Log.i(TAG, "onCreate: BBBBBBBEEEFFFFFFOOOOOOOOOORRRR query");
                for (Team teamTests : TeamMembers
                ) {
                    if (teamTests.getName().equals(theTeam)) {
                        Log.i(TAG, "onCreate: teeeeeeeeeeeea" + theTeam);
                        teamType = teamTests;
                    }
                }


                com.amplifyframework.datastore.generated.model.TaskItem taskItem1 = com.amplifyframework.datastore.generated.model.TaskItem.builder()
                        .title(title)
                        .body(body)
                        .state(taskState)
                        .team(teamType)
                        .build();

                Amplify.API.mutate(ModelMutation.create(taskItem1),
                        response -> Log.i("MyAmplify", "Added" + response.getData()),
                        error -> Log.e("MyAmplifyApp", "Create failed", error));

//                Amplify.API.mutate(ModelMutation.create(teams),
//                        response -> Log.i("MyAmplify", "Added" + response.getData()),
//                        error -> Log.e("MyAmplifyApp", "Create failed", error));
//


                Toast.makeText(AddTask.this, "Submitted!!", Toast.LENGTH_SHORT).show();

//            } else {
//                Toast.makeText(AddTask.this, "Please fill the form", Toast.LENGTH_LONG).show();
//            }


        });

        chooseFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                taskTitle = AddTask.this.findViewById(R.id.task_title_input);
                title = taskTitle.getText().toString();
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose File");
                startActivityForResult(chooseFile, REQUEST_FOR_FILE);

                prefEditor.putString("fileType", exctension);
                prefEditor.apply();
            }
        });

        Button goHome = AddTask.this.findViewById(R.id.goHome);
        goHome.setOnClickListener(v -> {
            Intent intent = new Intent(AddTask.this, MainActivity.class);
            startActivity(intent);
        });

// Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Log.i("getIntent", "onCreate: " + type);
        Log.i("getIntent", "onCreate: " + (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                try {
                    onChooseFile((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM)); // Handle single image being sent
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
//        uploadFile();
        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            assert result.getData() != null;
                            onChooseFile(result.getData().getData());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });


    }


    //to get and save file -->

    private void chooseFileFromDevice() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose File");
        startActivityForResult(chooseFile, REQUEST_FOR_FILE);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FOR_FILE && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: returned from file explorer");
            Log.i(TAG, "onActivityResult: => " + data.getData());

            File uploadFile = new File(getApplicationContext().getFilesDir(), "uploadFile");

            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                FileUtils.copy(inputStream, new FileOutputStream(uploadFile));

            } catch (Exception exception) {
                Log.e(TAG, "onActivityResult: file upload failed" + exception.toString());
            }
            if (exctension.contains("image")||exctension.contains("image")){
                exctension=".png";
            }
            Amplify.Storage.uploadFile(
                    title+exctension,
                    uploadFile,
                    success -> Log.i(TAG, "uploadFileToS3: succeeded " + success.getKey()),
                    error -> Log.e(TAG, "uploadFileToS3: failed " + error.toString())
            );

        }

    }

    private void uploadFileToApiStorage(File uploadFile) {
        String key = taskTitle.toString().equals(null) ? "defualtTask.jpg" : taskTitle.getText().toString() + ".jpg";
        Amplify.Storage.uploadFile(
                key,
                uploadFile,
                success -> Log.i(TAG, "uploadFileToS3: succeeded " + success.getKey()),
                failure -> Log.e(TAG, "uploadFileToS3: failed " + failure.toString())
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void onChooseFile(Uri uri) throws IOException {
        uploadedFileName = new Date().toString() + "." + getMimeType(getApplicationContext(), uri);
        File uploadFile = new File(getApplicationContext().getFilesDir(), "uploadFile");
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileUtils.copy(inputStream, new FileOutputStream(uploadFile));
        } catch (Exception exception) {
            Log.e("onChooseFile", "onActivityResult: file upload failed" + exception.toString());
        }
        Amplify.Storage.uploadFile(
                uploadedFileName,
                uploadFile,
                success -> {
                    Log.i("onChooseFile", "uploadFileToS3: succeeded " + success.getKey());
                    Toast.makeText(getApplicationContext(), "Image Successfully Uploaded", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("onChooseFile", "uploadFileToS3: failed " + error.toString());
                    Toast.makeText(getApplicationContext(), "Image Upload failed", Toast.LENGTH_SHORT).show();
                }
        );
    }

    public static String getMimeType(Context context, Uri uri) {
        String extension;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
        return extension;
    }


}
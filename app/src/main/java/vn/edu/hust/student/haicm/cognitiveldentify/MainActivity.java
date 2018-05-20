package vn.edu.hust.student.haicm.cognitiveldentify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceAttribute;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.contract.Person;
import com.microsoft.projectoxford.face.contract.TrainingStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

//import com.github.chrisbanes.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";
    private android.support.v4.view.ViewPager viewPager;
    private android.support.design.widget.TabLayout tabLayout;
    private Bitmap mBitmap;
    private Toolbar toolbar;

    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("https://westcentralus.api.cognitive.microsoft.com/face/v1.0", "ee1588a59d8648fd9129189bb45fd25e");
    private final String personGroupId = "duonghai";

    private ImageView imageView;
    private Face[] facesDetected;
    private ImageButton btnCamera;

    private int index = 0;
    private Student[] namePerson;   // Store student information
    private int numberPerson = 0;
    private int[] indexName;
    private int index2 = 0;
    private int index3 = 0;

    // Respond code from Intent
    private final int PICK_IMAGE = 100;
    private final int OPEN_CAMERA = 111;
    Uri imageUri;

    // Bacground detecting task
    class detectTask extends AsyncTask<InputStream, String, Face[]> {
        private ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

        @SuppressLint("StringFormatInvalid")
        @Override
        protected Face[] doInBackground(InputStream... params) {
            try{
                publishProgress(getString(R.string.dt));
                Face[] results = faceServiceClient.detect(params[0],true,false, new FaceServiceClient.FaceAttributeType[] {
                        FaceServiceClient.FaceAttributeType.Age,
                        FaceServiceClient.FaceAttributeType.Gender,
                        FaceServiceClient.FaceAttributeType.Emotion});
                if (results == null)
                {
//                    Toast.makeText(MainActivity.this, "Detection Finished. Nothing detected", Toast.LENGTH_SHORT).show();
                    publishProgress(getString(R.string.dt_no));
                    return null;
                }
                publishProgress(String.format(getString(R.string.dt_ok), results.length));
                return results;
            } catch (Exception e) {
                //Toast.makeText(MainActivity.this, "Detection failed", Toast.LENGTH_SHORT).show();
                publishProgress(getString(R.string.dt_failed));
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            mDialog.show();
        }

        @Override
        protected void onPostExecute(Face[] faces) {
            try {
                mDialog.dismiss();
                numberPerson = 0;
                facesDetected = faces;
                for (Face face : facesDetected) {
                    numberPerson += 1;
                }
            }catch (Exception e){
                Toast.makeText(MainActivity.this, getString(R.string.dt_failed), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            mDialog.setMessage(values[0]);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_main);

        android.util.Log.d(TAG, "onCreate: Starting");

        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(R.string.app_name);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cp1);
        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(mBitmap);
        btnCamera = (ImageButton)findViewById(R.id.btnCamera);
        Button btnGallery = (Button)findViewById(R.id.btnDetect);
        Button btnIdentify = (Button)findViewById(R.id.btnIdentify);

        btnCamera.setOnClickListener(this);
        btnIdentify.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        imageView.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btnCamera:
                // Display dialog to select either take photo or pick from gallery
                selectImage();
                break;
            case R.id.btnDetect:
                // Detect Face
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                new detectTask().execute(inputStream);

                break;
            case R.id.btnIdentify:
                // Identify Face
                try {
                    index = 0;
                    index2 = 0;
                    index3 = 0;
                    namePerson = new Student[numberPerson];
                    indexName = new int[numberPerson];
                    for (int i = 0; i < numberPerson; i++) {
                        indexName[i] = 0;
                    }
                    final UUID[] faceIds = new UUID[facesDetected.length];
                    for (int i = 0; i < facesDetected.length; i++) {
                        faceIds[i] = facesDetected[i].faceId;
                        //System.out.println("aaaaaaaaaaaa"+facesDetected[i].faceId);
                    }

                    new IdentifycasionTask(personGroupId).execute(faceIds);
                }catch (Exception e){
                    Toast.makeText(this, getString(R.string.dt_done), Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.imageView:
                android.app.AlertDialog.Builder mBuilder = new android.app.AlertDialog.Builder(this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_view_image, null);
                PhotoView photoView = mView.findViewById(R.id.imageZoomView);
                photoView.setImageBitmap(mBitmap);
                mBuilder.setView(mView);
                android.app.AlertDialog mDialog = mBuilder.create();
                mDialog.show();

                break;
            default:
                break;
        }
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private void selectImage(){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.add_photo);
        builder.setIcon(R.drawable.ic_add_image);

        builder.setItems(R.array.select_item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                switch (i){
                    case 0:
                        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, OPEN_CAMERA);
                        break;
                    case 1:
                        openGallery();
                        break;
                    default:
                        dialogInterface.dismiss();
                        break;

                }
            }
        });

        builder.show();
    }

    private class IdentifycasionTask extends AsyncTask<UUID, String, IdentifyResult[]> {
        String personGroupId;
        private ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

        public IdentifycasionTask(String personGroupId) {
            this.personGroupId = personGroupId;
        }

        @Override
        protected IdentifyResult[] doInBackground(UUID... params) {
            try{
                publishProgress(getString(R.string.ident_pers_stt));
                TrainingStatus trainingStatus = faceServiceClient.getPersonGroupTrainingStatus(this.personGroupId);

                if(trainingStatus.status != TrainingStatus.Status.Succeeded){
                    publishProgress("Person group training status is "+trainingStatus.status);
                    return null;
                }

                publishProgress("Identifying...");

                return faceServiceClient.identity(personGroupId, params, 1);
            } catch (Exception ex){
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            mDialog.show();
        }

        @Override
        protected void onPostExecute(IdentifyResult[] identifyResults) {
            mDialog.dismiss();

            for (int i = 0; i < facesDetected.length; i++){
//            for(IdentifyResult identifyResult:identifyResults){
                try {
                    namePerson[index] = new Student("", facesDetected[i].faceId.toString());
                    index++;
//                    Toast.makeText(MainActivity.this, ""+identifyResult.candidates.get(0).personId, Toast.LENGTH_SHORT).show();
                    new PersonDetectionTask(personGroupId).execute(identifyResults[i].candidates.get(0).personId);
                    indexName[index2] = index - 1;
                    index2++;
//                    System.out.println("aaaaaaaaaaaaaaaaaa"+facesDetected[test].faceId);
//                    System.out.println(identifyResults[i].candidates.get(0));
                }catch (Exception ex){
//                    countNull++;
//                    System.out.println("aaaaaaaaaaaaaa"+facesDetected[test].faceId);
//                    namePerson[index] = new Student("", facesDetected[i].faceId.toString());
//                    index++;
                    //Toast.makeText(MainActivity.this, "Không nhận diện được", Toast.LENGTH_SHORT).show();
                    //new PersonDetectionTask(personGroupId).execute(identifyResults[i].candidates.get(0).personId);
//                    ImageView img = (ImageView)findViewById(R.id.imageView);
                    imageView.setImageBitmap(drawFaceRectangleOnBitmap(mBitmap, facesDetected, "", namePerson));
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            mDialog.setMessage(values[0]);
        }
    }

    private class PersonDetectionTask extends AsyncTask<UUID, String, Person>{
        private ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
        private String personGroupId;

        public PersonDetectionTask(String personGroupId) {
            this.personGroupId = personGroupId;
        }

        @Override
        protected Person doInBackground(UUID... params) {
            try {
                publishProgress(getString(R.string.ident_pers_stt));

                return faceServiceClient.getPerson(personGroupId, params[0]);
            } catch (Exception ex){
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            mDialog.show();
        }

        @Override
        protected void onPostExecute(Person person) {
            mDialog.dismiss();

            //Toast.makeText(MainActivity.this, ""+person.name, Toast.LENGTH_SHORT).show();
//            ImageView img = (ImageView)findViewById(R.id.imageView);

            try {
                namePerson[indexName[index3]].setName(person.name);
                index3++;
                imageView.setImageBitmap(drawFaceRectangleOnBitmap(mBitmap, facesDetected, person.name, namePerson));
                mBitmap = drawFaceRectangleOnBitmap(mBitmap, facesDetected, "", namePerson);

            }catch (Exception e){}
        }

        @Override
        protected void onProgressUpdate(String... values) {
            mDialog.setMessage(values[0]);
        }
    }

    private Bitmap drawFaceRectangleOnBitmap(Bitmap mBitmap, Face[] facesDetected, String name, Student[] newName) {
        Bitmap bitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);

        //Rectangle
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);

        int storeNumber = 0;
        for(storeNumber = 0; storeNumber < numberPerson; storeNumber++){
            if(namePerson[storeNumber] == null){
                break;
            }
        }

        if(facesDetected != null){
            if(storeNumber == numberPerson) {
                for (int i = 0; i < facesDetected.length; i++) {
                    FaceAttribute faceAttribute = facesDetected[i].faceAttributes;

                    String emotion = predictEmotion(faceAttribute.emotion.anger,
                            faceAttribute.emotion.contempt,
                            faceAttribute.emotion.disgust,
                            faceAttribute.emotion.fear,
                            faceAttribute.emotion.happiness,
                            faceAttribute.emotion.neutral,
                            faceAttribute.emotion.sadness,
                            faceAttribute.emotion.surprise);

                    FaceRectangle faceRectangle = facesDetected[i].faceRectangle;
                    //System.out.println("bbbbbbbbbbbbbbb" + facesDetected[i].faceId);
                    int store = 0;
                    for(int j = 0; j < numberPerson; j++){
                        if(facesDetected[i].faceId.toString().equals(namePerson[j].getId())){
                            store = j;
                            break;
                        }
                    }

                    String test = "\nCảm xúc: " + emotion + "\nTuổi: " + faceAttribute.age + "\nGiới tính: " + faceAttribute.gender;

                    String emotionInfo = "";
                    if (!namePerson[store].getName().equals("")){
                        emotionInfo += "Name: " + namePerson[store].getName();
                    }
                    emotionInfo += "\nCảm xúc: " + emotion + "\nGiới tính: " + (faceAttribute.gender.equals("male") ? "Nam" : "Nữ");
                    System.out.println("----------------------------------------------------------------------------------------------");
                    System.out.println(test);
                    canvas.drawRect(faceRectangle.left, faceRectangle.top, faceRectangle.left + faceRectangle.width, faceRectangle.top + faceRectangle.height, paint);

                    // Write information text
//                    if (namePerson[store].getName().equals("")) {
//                        drawTextOnCanvas(canvas, 30, ((faceRectangle.left + faceRectangle.width) / 2) + 100, (faceRectangle.top + faceRectangle.height) + 50, Color.RED, "NULL" + test);
//                    } else {
//                        drawTextOnCanvas(canvas, 30, ((faceRectangle.left + faceRectangle.width) / 2) + 100, (faceRectangle.top + faceRectangle.height) + 50, Color.RED, namePerson[store].getName()+test);
//                    }

                    drawTextOnCanvas(canvas, 40, ((faceRectangle.left + faceRectangle.width) / 2) + 100, (faceRectangle.top + faceRectangle.height) + 50, Color.RED, emotionInfo);
                }
            }
        }

        return bitmap;
    }

    private String predictEmotion(double anger, double contempt, double disgust, double fear, double happiness, double neutral, double sadness, double surprise) {
        double[] tmp = new double[8];
        tmp[0] = anger;
        tmp[1] = contempt;
        tmp[2] = disgust;
        tmp[3] = fear;
        tmp[4] = happiness;
        tmp[5] = neutral;
        tmp[6] = sadness;
        tmp[7] = surprise;

        double max = tmp[0];
        int position = 1;
        for(int i = 1; i < 8; i++){
            if(max < tmp[i]){
                max = tmp[i];
                position = i;
            }
        }

        switch (position){
            case 0: return getString(R.string.anger);
            case 1: return getString(R.string.contempt);
            case 2: return getString(R.string.disgust);
            case 3: return getString(R.string.fear);
            case 4: return getString(R.string.happiness);
            case 5: return getString(R.string.neutral);
            case 6: return getString(R.string.sadness);
            case 7: return getString(R.string.surprise);
            default: return "";
        }

    }

    private void drawTextOnCanvas(Canvas canvas, int textSize, int x, int y, int color, String name) {
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(textSize);

        float textWidth = paint.measureText(name);

        canvas.drawText(name, x - (textWidth/2), y - (textSize/2), paint);
    }

    //
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == OPEN_CAMERA) {
            mBitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(mBitmap);
        }

        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);

            try {
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//                // Log.d(TAG, String.valueOf(bitmap));

//                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Action menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            // setting
            case R.id.action_change_lang:
                showChangeLangDialog();
                break;
            //help
            case R.id.action_help:
                Intent help = new Intent(this, HelpActivity.class);
                startActivity(help);
                break;
            //about
            case R.id.action_about:
                // Inflate the about message contents
                View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_launcher_emoji);
                builder.setTitle(R.string.app_name);
                builder.setView(messageView);
                builder.create();
                builder.show();

                break;
            default:

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Change Language Dialog
    private void showChangeLangDialog() {

        //Array of language.

        final String [] listItems = {"Tiếng Việt", "English", "France", "한국어", "日本語", "Español (Spanish)", "中文(简体)", "Italiano", "Deutsch (German)", "Русский (Russia)"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.choose_lang);
        builder.setIcon(R.drawable.ic_change_lang);
        builder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        setLocale("vi");
                        recreate();
                        break;
                    case 1:
                        setLocale("en");
                        recreate();
                        break;
                    case 2:
                        setLocale("fr");
                        recreate();
                        break;
                    case 3:
                        setLocale("ko");
                        recreate();
                        break;
                    case 4:
                        setLocale("ja");
                        recreate();
                        break;
                    case 5:
                        setLocale("es");
                        recreate();
                        break;
                    case 6:
                        setLocale("zh");
                        recreate();
                        break;
                    case 7:
                        setLocale("it");
                        recreate();
                        break;
                    case 8:
                        setLocale("de");
                        recreate();
                        break;
                    case 9:
                        setLocale("ru");
                        recreate();
                        break;
                    default:
                        break;
                }

                // Dismiss alert dialog when language selected
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        // Show alert dialog
        alertDialog.show();
    }

    // Change locale to change language
    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        // Save data to share preference
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    //Load Language saved to shared preferences
    public void loadLocale(){
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);

        String language = preferences.getString("My_Lang", "");
        setLocale(language);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }



}
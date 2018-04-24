package vn.edu.hust.student.haicm.cognitiveldentify;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
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
import java.io.InputStream;
import java.util.UUID;

//import com.github.chrisbanes.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("https://westcentralus.api.cognitive.microsoft.com/face/v1.0", "e37c547b94de46a5a0bcabd572bfe40a");
    private final String personGroupId = "newtest";

    ImageView imageView;
    Bitmap mBitmap;
    Face[] facesDetected;
    ImageButton btnCamera;


    int index = 0;
    Student[] namePerson;
    int numberPerson = 0;
//    int countNull = 0;
    int[] indexName;
    int index2 = 0;
    int index3 = 0;

    class detectTask extends AsyncTask<InputStream, String, Face[]>{
        private ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

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
                    //Toast.makeText(MainActivity.this, "Detection Finished. Nothing detected", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_main);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cp1);
        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageBitmap(mBitmap);
        btnCamera = (ImageButton)findViewById(R.id.btnCamera);
        Button btnDetect = (Button)findViewById(R.id.btnDetectFace);
        Button btnIdentify = (Button)findViewById(R.id.btnIdentify);

        btnCamera.setOnClickListener(this);
        btnIdentify.setOnClickListener(this);
        btnDetect.setOnClickListener(this);
        imageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCamera:
                Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);

                break;
            case R.id.btnDetectFace:
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                new detectTask().execute(inputStream);

                break;
            case R.id.btnIdentify:
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
                    Toast.makeText(MainActivity.this, getString(R.string.dt_done), Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.imageView:
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_view_image, null);
                PhotoView photoView = mView.findViewById(R.id.imageZoomView);
                photoView.setImageBitmap(mBitmap);
                mBuilder.setView(mView);
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();

                break;
            default:
                break;
        }
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
                    ImageView img = (ImageView)findViewById(R.id.imageView);
                    img.setImageBitmap(drawFaceRectangleOnBitmap(mBitmap, facesDetected, "", namePerson));
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
            ImageView img = (ImageView)findViewById(R.id.imageView);

            try {
                namePerson[indexName[index3]].setName(person.name);
                index3++;
                img.setImageBitmap(drawFaceRectangleOnBitmap(mBitmap, facesDetected, person.name, namePerson));
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
        paint.setStrokeWidth(12);

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
                    System.out.println("----------------------------------------------------------------------------------------------");
                    System.out.println(test);
                    canvas.drawRect(faceRectangle.left, faceRectangle.top, faceRectangle.left + faceRectangle.width, faceRectangle.top + faceRectangle.height, paint);
                    if (namePerson[store].getName().equals("")) {
                        drawTextOnCanvas(canvas, 15, ((faceRectangle.left + faceRectangle.width) / 2) + 100, (faceRectangle.top + faceRectangle.height) + 50, Color.RED, "NULL" + test);
                    } else {
                        drawTextOnCanvas(canvas, 15, ((faceRectangle.left + faceRectangle.width) / 2) + 100, (faceRectangle.top + faceRectangle.height) + 50, Color.RED, namePerson[store].getName()+test);
                    }
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
            case 0: return "Giận dữ";
            case 1: return "Kinh thường";
            case 2: return "Kinh tởm";
            case 3: return "Ghê sợ";
            case 4: return "Hạnh phúc";
            case 5: return "Trung lập";
            case 6: return "Buồn bã";
            case 7: return "Ngạc nhiên";
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mBitmap = (Bitmap) data.getExtras().get("data");
        imageView.setImageBitmap(mBitmap);
    }
}
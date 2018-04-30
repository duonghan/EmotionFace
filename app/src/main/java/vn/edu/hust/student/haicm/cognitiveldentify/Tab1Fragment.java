package vn.edu.hust.student.haicm.cognitiveldentify;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import static android.app.Activity.RESULT_OK;

public class Tab1Fragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "Tab1Fragment";

    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("https://westcentralus.api.cognitive.microsoft.com/face/v1.0", "e37c547b94de46a5a0bcabd572bfe40a");
    private final String personGroupId = "newtest";

    private ImageView imageView;
    private Bitmap mBitmap;
    private Face[] facesDetected;
    private ImageButton btnCamera;

    private int index = 0;
    private Student[] namePerson;
    private int numberPerson = 0;
    private int[] indexName;
    private int index2 = 0;
    private int index3 = 0;
    private final int PICK_IMAGE = 100;
    private final int OPEN_CAMERA = 111;
    Uri imageUri;

    public Tab1Fragment() {
    }

    class detectTask extends AsyncTask<InputStream, String, Face[]> {
        private ProgressDialog mDialog = new ProgressDialog(getActivity());

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
                Toast.makeText(getActivity(), getString(R.string.dt_failed), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            mDialog.setMessage(values[0]);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab1_fragment, container, false);

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cp1);
        imageView = (ImageView)view.findViewById(R.id.imageView);
        imageView.setImageBitmap(mBitmap);
        btnCamera = (ImageButton)view.findViewById(R.id.btnCamera);
        Button btnGallery = (Button)view.findViewById(R.id.btnGallery);
        Button btnIdentify = (Button)view.findViewById(R.id.btnIdentify);



        btnCamera.setOnClickListener(this);
        btnIdentify.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        imageView.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCamera:
                Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, OPEN_CAMERA);

                break;
            case R.id.btnGallery:
                openGallery();

                break;
            case R.id.btnIdentify:

                // Detect Face

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                new Tab1Fragment.detectTask().execute(inputStream);

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

                    new Tab1Fragment.IdentifycasionTask(personGroupId).execute(faceIds);
                }catch (Exception e){
                    Toast.makeText(getActivity(), getString(R.string.dt_done), Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.imageView:
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
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

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    private class IdentifycasionTask extends AsyncTask<UUID, String, IdentifyResult[]> {
        String personGroupId;
        private ProgressDialog mDialog = new ProgressDialog(getActivity());

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
                    new Tab1Fragment.PersonDetectionTask(personGroupId).execute(identifyResults[i].candidates.get(0).personId);
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
        private ProgressDialog mDialog = new ProgressDialog(getActivity());
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
        paint.setStrokeWidth(2);

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
//            mBitmap = (Bitmap) data.getExtras().get("data");
        }
    }

}

package com.example.android.staysaferesq;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.microsoft.projectoxford.face.contract.VerifyResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class FaceActivity extends AppCompatActivity {
    final ArrayList <MapPerson> faceid=new ArrayList<>();

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_face);
//    }


    private final int PICK_IMAGE = 1;
    private ProgressDialog detectionProgressDialog;


    private final String apiEndpoint = "https://centralindia.api.cognitive.microsoft.com/face/v1.0";
    private final String subscriptionKey = "6a2ec8c91c03437caa4fe7d4949081e1";

    private final FaceServiceClient faceServiceClient =
            new FaceServiceRestClient(apiEndpoint, subscriptionKey);
//    String loginPhoneNumber="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        ImageButton button1 = (ImageButton)findViewById(R.id.button1);

        ImageButton button_back = (ImageButton)findViewById(R.id.button_back);
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
//                i.putExtra("phoneno1")
                startActivity(intent);
            }
        });

        Intent i=getIntent();
//        loginPhoneNumber = i.getStringExtra("phoneno1");

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(
                        intent, "Select Picture"), PICK_IMAGE);
            }
        });

        detectionProgressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), uri);
                ImageView imageView = (ImageView) findViewById(R.id.imageView1);
                imageView.setImageBitmap(bitmap);

                // Uncomment
                detectAndFrame(bitmap);
                String s="";
                for(MapPerson st:faceid){
                    s=s+st.face;
                }
                Log.v("retfaceids",s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    // Detect faces by uploading a face image.
// Frame faces after detection.
    private void detectAndFrame(final Bitmap imageBitmap) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());

        AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    null          // returnFaceAttributes:
                                /* new FaceServiceClient.FaceAttributeType[] {
                                    FaceServiceClient.FaceAttributeType.Age,
                                    FaceServiceClient.FaceAttributeType.Gender }
                                */
                            );
                            if (result == null){
                                publishProgress(
                                        "Detection Finished. Nothing detected");
                                return null;
                            }
                            publishProgress(String.format(
                                    "Detection Finished. %d face(s) detected",
                                    result.length));
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Detection failed: %s", e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        detectionProgressDialog.show();
                    }
                    @Override
                    protected void onProgressUpdate(String... progress) {
                        detectionProgressDialog.setMessage(progress[0]);
                    }
                    @Override
                    protected void onPostExecute(Face[] result) {
//                        ArrayList <String>faces1=new ArrayList<>();
                        detectionProgressDialog.dismiss();
                        if(!exceptionMessage.equals("")){
                            showError(exceptionMessage);
                        }
                        if (result == null) return;
                        for(Face face:result){
                            CheckUpdateFace checkUpdateFace = new CheckUpdateFace(face.faceId.toString());
                            try{
                                checkUpdateFace.execute().get();
                                faceid.addAll(checkUpdateFace.getFaceIDS());
                            }
                            catch (InterruptedException e){
                                e.printStackTrace();
                            }
                            catch (ExecutionException e){
                                e.printStackTrace();
                            }



                        }
                        ImageView imageView = findViewById(R.id.imageView1);
                        imageView.setImageBitmap(
                                drawFaceRectanglesOnBitmap(imageBitmap, result,faceid));
                        imageBitmap.recycle();
                    }
//                    public void getFaces(){
//                        return faces1;
//                    }
                };
        try{
            detectTask.execute(inputStream).get();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        catch (ExecutionException e){
            e.printStackTrace();
        }

//        faceid=detectTask.getFaces();
//        return faceid;

    }

    private void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }})
                .create().show();
    }




    private  Bitmap drawFaceRectanglesOnBitmap(
            Bitmap originalBitmap, Face[] faces,ArrayList<MapPerson> faceid1) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                Log.v("faceidis",face.faceId.toString()+faceid1.size());
//                CheckUpdateFace checkUpdateFace = CheckUpdateFace(login)
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);



                String getting="";
                Log.v("sizeoflist",Integer.toString(faceid1.size()));
                for(MapPerson mp:faceid1){
                    if(!mp.face.equals("*")){
                    VerifyResult answer=new VerifyResult();
                    UUID face11=UUID.fromString(mp.face);
                    UUID face22=UUID.fromString(face.faceId.toString());
                    RetrieveFeedTask temp=new RetrieveFeedTask(face11,face22,mp.phone);



                        try{
                            temp.execute().get();//.getResult();
                            answer=temp.getResult();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        if(answer.isIdentical){

                            CheckFaceSafe checkFaceSafe = new CheckFaceSafe(mp.phone);
                            try{
                                checkFaceSafe.execute().get();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }



//                        try{
//                            answer=faceServiceClient.verify(face11,face22);
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                        catch (ClientException e){
//                            e.printStackTrace();
//                        }
                        Log.v("myparams",mp.face+",,"+face.faceId.toString());
                        if(answer.isIdentical){
                            Toast.makeText(getApplicationContext(),"Match found, updating map.",Toast.LENGTH_SHORT);
                            Log.v("facemarkedsafe",mp.face);



                        }
                    }

                }



            }
        }
        return bitmap;
    }










//    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {
//        UUID face1,face2;
//        String phone;
//        VerifyResult ans;
//        public RetrieveFeedTask(UUID q,UUID p ,String phon){
//            super();
//            phone=phon;
//            face1=q;
//            face2=p;
//            ans=new VerifyResult();
//
//        }
//
//        private Exception exception;
//
//        protected void onPreExecute() {
////            progressBar.setVisibility(View.VISIBLE);
////            responseView.setText("");
//
//        }
//         VerifyResult getResult(){
//            return ans;
//        }
//        protected String doInBackground(Void... urls) {
////            String email = emailText.getText().toString();
//            // Do some validation here
////            FaceServiceClient faceServiceClient = new FaceServiceRestClient("");
//
//            try{
//                ans=faceServiceClient.verify(face1,face2);
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//            catch (ClientException e){
//                e.printStackTrace();
//            }
//            String a="no";
//            if(ans.isIdentical){
//                a="yes";
//                CheckFaceSafe checkFaceSafe = new CheckFaceSafe(phone);
//                try{
//                    checkFaceSafe.execute().get();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                Toast.makeText(getApplicationContext(),"Match found, marking safe.",Toast.LENGTH_SHORT);
//            }
//            Log.v("faceissame",a);
//
//            String getting="";
//            return getting;
//
//        }
//
//        protected void onPostExecute(String response) {
//            if(response == null) {
//                response = "THERE WAS AN ERROR";
//            }
////            progressBar.setVisibility(View.GONE);
//            Log.i("INFO1", response);
//
////            responseView.setText(response);
//
//            try {
//                JSONObject jsonObj = new JSONObject(response);
//                JSONArray ja =(JSONArray) jsonObj.get("value");
//
//                for(int i=0;i<ja.length();i++){
//                    JSONObject temp = (JSONObject)ja.get(i);
//                    Log.v("jsonface",temp.toString());
//
//
//                }
//
//
//            }
//            catch (JSONException e){
//                e.printStackTrace();
//                Toast.makeText(FaceActivity.this,"No JSON received",Toast.LENGTH_SHORT).show();
//            }
//
//
//
//
//
//
//
//        }
//    }


}

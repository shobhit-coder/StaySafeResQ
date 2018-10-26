package com.example.android.staysaferesq;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.VerifyResult;
import com.microsoft.projectoxford.face.rest.ClientException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

class RetrieveFeedTask extends AsyncTask<Void, Void, String> {
    UUID face1,face2;
    String phone;
    VerifyResult ans;
    public RetrieveFeedTask(UUID q,UUID p ,String phon){
        super();
        phone=phon;
        face1=q;
        face2=p;
        ans=new VerifyResult();

    }

    private Exception exception;

    protected void onPreExecute() {
//            progressBar.setVisibility(View.VISIBLE);
//            responseView.setText("");

    }
    VerifyResult getResult(){
        return ans;
    }
    protected String doInBackground(Void... urls) {
//            String email = emailText.getText().toString();
        // Do some validation here
            FaceServiceClient faceServiceClient = new FaceServiceRestClient("https://centralindia.api.cognitive.microsoft.com/face/v1.0","6a2ec8c91c03437caa4fe7d4949081e1");

        try{
            ans=faceServiceClient.verify(face1,face2);
        }catch (IOException e){
            e.printStackTrace();
        }
        catch (ClientException e){
            e.printStackTrace();
        }
        String a="no";
        if(ans.isIdentical){
            a="yes";
//            CheckFaceSafe checkFaceSafe = new CheckFaceSafe(phone);
//            try{
//                checkFaceSafe.execute().get();
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//            Toast.makeText(getApplicationContext(),"Match found, marking safe.",Toast.LENGTH_SHORT);
        }
        Log.v("faceissame",a);

        String getting="";
        return getting;

    }

    protected void onPostExecute(String response) {
        if(response == null) {
            response = "THERE WAS AN ERROR";
        }
//            progressBar.setVisibility(View.GONE);
        Log.i("INFO1", response);

//            responseView.setText(response);

        try {
            JSONObject jsonObj = new JSONObject(response);
            JSONArray ja =(JSONArray) jsonObj.get("value");

            for(int i=0;i<ja.length();i++){
                JSONObject temp = (JSONObject)ja.get(i);
                Log.v("jsonface",temp.toString());


            }


        }
        catch (JSONException e){
            e.printStackTrace();
//            Toast.makeText(,"No JSON received",Toast.LENGTH_SHORT).show();
        }







    }
}

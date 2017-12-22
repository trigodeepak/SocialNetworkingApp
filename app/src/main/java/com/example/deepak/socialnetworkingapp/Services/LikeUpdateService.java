package com.example.deepak.socialnetworkingapp.Services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;


public class LikeUpdateService extends IntentService {

    public LikeUpdateService(){
        super("name");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.e("Service","INside the sevice" );
        String Uid = String.valueOf( intent.getIntExtra( "Uid",0 ) );
        String postId = String.valueOf( intent.getIntExtra( "postId",0 ) );
        String Pid = String.valueOf( intent.getIntExtra("Pid",0) );
        String con_url = "https://socialnetworkapplication.000webhostapp.com/SocialNetwork/like.php";

        try {
            URL url = new URL(con_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);

            OutputStream outputStream  = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));

            String post_data= URLEncoder.encode("postId","UTF-8")+"="+ URLEncoder.encode(postId,"UTF-8")+"&"
                    + URLEncoder.encode("Uid","UTF-8")+"="+ URLEncoder.encode(Uid,"UTF-8")+"&"
                    + URLEncoder.encode("Pid","UTF-8")+"="+ URLEncoder.encode(Pid,"UTF-8");

            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e( "Service","The service finished" );

    }

}

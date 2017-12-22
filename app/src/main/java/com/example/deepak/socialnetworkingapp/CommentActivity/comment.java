package com.example.deepak.socialnetworkingapp.CommentActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deepak.socialnetworkingapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.widget.Toast.LENGTH_SHORT;

public class comment extends AppCompatActivity {
    private RecyclerView recyclerView;
    private comment_adapter mAdapter;
    private ImageView send_comment;
    private EditText et_comment_text;
    public int Uid;
    public int Pid;
    public int postId;
    private String FetchData;
    private ArrayList<comment_recycler> commentList = new ArrayList<>();

    private ProgressBar progressBar;
    private TextView progressText;
    //To show progress when the data is being fetched
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressText.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBar.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressText.setVisibility(show ? View.VISIBLE : View.GONE);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.comments_activity_outer );

        progressBar = (ProgressBar) findViewById( R.id.comment_progress );
        progressText = (TextView) findViewById( R.id.comment_progress_text );

        Uid = getIntent().getExtras().getInt("Uid");
        Pid = getIntent().getExtras().getInt("Pid");
        postId = getIntent().getExtras().getInt("postId");

        recyclerView = (RecyclerView) findViewById(R.id.comment_recycler_view);
        prepareCommentData();
        send_comment = (ImageView) findViewById(R.id.send_comment);
        et_comment_text = (EditText) findViewById(R.id.et_commenttext);

        send_comment.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(send_comment.getWindowToken(), 0);
                Toast.makeText(getApplicationContext(),"Commenting Please wait",LENGTH_SHORT).show();
                et_comment_text.clearFocus();
                MysqlCon mysqlCon = new MysqlCon();
                mysqlCon.execute("comment",et_comment_text.getText().toString(),String.valueOf(postId),String.valueOf(Uid),String.valueOf(Pid));
                et_comment_text.setText( "" );

            }});
    }

    private void prepareCommentData() {
        showProgress( true );
            MysqlConCommentShow mysqlConCommentShow = new MysqlConCommentShow();
            mysqlConCommentShow.execute( "getPostComments", String.valueOf( postId ) );
    }

    //Asynk task to add new comments to a post
    class MysqlCon extends AsyncTask<String,String,String>
    {
        @Override
        protected String doInBackground(String... params) {

            String type = params[0];
            String con_url = "https://socialnetworkapplication.000webhostapp.com/SocialNetwork/"+type+".php";

            String commenttext  = params[1];
            String postId = params[2];
            String Uid = params[3];
            String Pid = params[4];

            try {
                URL url = new URL(con_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);


                OutputStream outputStream  = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));

                String post_data= URLEncoder.encode("comment","UTF-8")+"="+ URLEncoder.encode(commenttext,"UTF-8")+"&"
                        +URLEncoder.encode("postId","UTF-8")+"="+ URLEncoder.encode(postId,"UTF-8")+"&"
                        +URLEncoder.encode("Pid","UTF-8")+"="+ URLEncoder.encode(Pid,"UTF-8")+"&"
                        + URLEncoder.encode("Uid","UTF-8")+"="+ URLEncoder.encode(Uid,"UTF-8");


                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream inputStream  = httpURLConnection.getInputStream();
                BufferedReader bufferedReader  = new BufferedReader(new InputStreamReader(inputStream,"ISO-8859-1"));
                String result="";
                String line;

                while((line=bufferedReader.readLine())!=null){
                    result+=line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            prepareCommentData();
            et_comment_text.setText(null);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {

        }
    }

    //Asynk to fetch comments data
    class MysqlConCommentShow extends AsyncTask<String,String,String>
    {
        @Override
        protected String doInBackground(String... params) {
            String type = params[0]; //getPostComments
            String con_url = "https://socialnetworkapplication.000webhostapp.com/SocialNetwork/"+type+".php";

            String postId = params[1];
            try {
                URL url = new URL(con_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);


                 OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                    String post_data = URLEncoder.encode("postId", "UTF-8") + "=" + URLEncoder.encode(postId, "UTF-8");


                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();

                InputStream inputStream  = httpURLConnection.getInputStream();
                BufferedReader bufferedReader  = new BufferedReader(new InputStreamReader(inputStream,"ISO-8859-1"));
                String result="";
                String line;

                while((line=bufferedReader.readLine())!=null){
                    result+=line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
                showProgress( false );
                FetchData = s;
                String result = "{\"comments\":" + FetchData + "}";
                commentList = parseCommentResult(result);
                mAdapter = new comment_adapter(commentList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(mAdapter);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
        }
    }

    private ArrayList<comment_recycler> parseCommentResult(String result) {
        ArrayList<comment_recycler> commentList = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("comments");
            for (int i = 0; i < jsonArray.length(); i++) {
                comment_recycler comment = new comment_recycler();
                JSONObject reader = jsonArray.getJSONObject(i);
                comment.setComment(reader.getString("comment_text"));
                comment.setImage(reader.getString("profile"));
                comment.setProfilename(reader.getString("name"));
                commentList.add(i,comment);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return commentList;
    }
}


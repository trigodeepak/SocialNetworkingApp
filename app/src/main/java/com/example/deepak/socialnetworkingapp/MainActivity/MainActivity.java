package com.example.deepak.socialnetworkingapp.MainActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.deepak.socialnetworkingapp.GetfriendsActivity.getfriends;
import com.example.deepak.socialnetworkingapp.LoginActivity;
import com.example.deepak.socialnetworkingapp.NotificationActivity.Notification_fragment;
import com.example.deepak.socialnetworkingapp.R;
import com.example.deepak.socialnetworkingapp.messages.message;
import com.example.deepak.socialnetworkingapp.upload;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import butterknife.BindView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener,LoaderManager.LoaderCallbacks<String> {

    private ArrayList<Post> postList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PostAdapter mAdapter;
    private String email;
    private static final int LOADER_ID = 50;
    private int flag=0;

    public String FetchData;
    public String Uname;
    public int Uid;
    public String Uprofilepicture;
    public String group_id;

    //new uploading post
    private ImageView mimageUpload;
    private EditText mstatusUpload;
    private Button mpostUpload;
    public String type;

    private static final int SELECT_PICTURE = 100;
    private String KEY_IMAGE = "image";
    Bitmap bitmap;

    @BindView(R.id.post_progress)
    ProgressBar progressBar;

    private TextView progressText;

    public void showProgress(final boolean show) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressText.setVisibility(show ? View.VISIBLE : View.GONE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        email = getIntent().getExtras().getString("email");
        Uname = getIntent().getExtras().getString("Uname");
        Uprofilepicture = getIntent().getExtras().getString("Uprofilepicture");
        Uid = getIntent().getExtras().getInt("Uid");
        group_id = getIntent().getExtras().getString("group_id");


        LayoutInflater inflater = (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate( R.layout.mainactivity_content_navigation, null);
        progressBar = (ProgressBar) view.findViewById( R.id.post_progress );
        progressText = (TextView) view.findViewById( R.id.post_progress_text );

        //navigation Drawer Activity to make the activity
        setContentView(R.layout.mainactivity_navigation );
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView useremail = (TextView) header.findViewById( R.id.user_email );
        ImageView userImage = (ImageView) header.findViewById( R.id.user_image );
        TextView username = (TextView) header.findViewById( R.id.user_name );
//        getUserDetails();
        useremail.setText(email);
        username.setText(Uname);
        Picasso.with( userImage.getContext())
                .load("https://socialnetworkapplication.000webhostapp.com/SocialNetwork/"+Uprofilepicture)
                .resize(50, 50).centerCrop()
                .into( userImage );

        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mstatusUpload = (EditText)findViewById(R.id.statusUpload);
        mstatusUpload.clearFocus();
        mimageUpload = (ImageView)findViewById(R.id.imageUpload);
        mpostUpload = (Button)findViewById(R.id.postUpload);
        mimageUpload.setOnClickListener(this);
        mpostUpload.setOnClickListener(this);

        //To create the recycler view
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_post);
        recyclerView.setHasFixedSize(true);
        preparePostData(1);

    }



    /* Choose an image from Gallery */
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    flag=1;
                    mimageUpload.setImageURI(selectedImageUri);
                }
            }
        }
    }

//    public String getPathFromURI(Uri contentUri) {
//        String res = null;
//        String[] proj = {MediaStore.Images.Media.DATA};
//        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
//        if (cursor.moveToFirst()) {
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            res = cursor.getString(column_index);
//        }
//        cursor.close();
//        return res;
//    }

    public String getStringImage(Bitmap bmp){
        bmp = ((BitmapDrawable) mimageUpload.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void uploadImage(){
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        String UPLOAD_URL = "https://socialnetworkapplication.000webhostapp.com/SocialNetwork/uploadPost.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        preparePostData(1);
                        Drawable img = getApplicationContext().getResources().getDrawable(R.drawable.ic_menu_camera);
                        mimageUpload.setImageDrawable(img);
                        mstatusUpload.setText(null);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();

                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image="";
                if(flag==1)
                image = image + getStringImage(bitmap);
                String text = mstatusUpload.getText().toString();

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();
                //Adding parameters
                params.put(KEY_IMAGE, image);
                params.put("email",email);
                params.put("text",text);
                params.put("group_id",group_id);
                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
        requestQueue.getCache().invalidate( UPLOAD_URL,true );
        stringRequest.setShouldCache( false );
    }

    @Override
    public void onClick(View v) {
        if(v == mimageUpload){
            Toast.makeText(MainActivity.this, "You clicked on ImageView", Toast.LENGTH_LONG).show();
            openImageChooser();
        }
        if(v == mpostUpload){
            uploadImage();
        }
    }


    private void preparePostData(int i) {
        //Function to populate the post list
        Bundle postQueryBundle = new Bundle( );
        postQueryBundle.putString( "type","post" );
        postQueryBundle.putString( "group_id",group_id );
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> postLoader = loaderManager.getLoader( LOADER_ID );
        if(i==1){
        loaderManager.initLoader( LOADER_ID, postQueryBundle, this ).forceLoad();}
        else{
            loaderManager.initLoader( LOADER_ID, postQueryBundle, this );
        }
    }

    public ArrayList<Post> parseResult(String result) {
        // Function to take json as string and parse it into the post list
        ArrayList<Post> postList = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("posts");
            for (int i = 0; i < jsonArray.length(); i++) {
                Post post = new Post();
                JSONObject reader = jsonArray.getJSONObject(i);
                post.setPostImage(reader.getString("post_image"));
                post.setPostText( reader.getString("post_text") );
                post.setProfileId( Integer.parseInt( reader.getString( "profile_id" ) ) );
                post.setPostTime(reader.getString( "post_time" ));
                post.setProfileImage(reader.getString("profile"));
                post.setProfileName(reader.getString( "name" ));
                post.setPostId( Integer.parseInt( reader.getString( "post_id" ) ) );
                post.setLiked( Boolean.parseBoolean( reader.getString( "is_liked") ) );
                post.setLikesNumber( reader.getString( "likes_count" ) );
                post.setCommentsNumber( reader.getString( "comments_count" ) );
                postList.add(i,post);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return postList;
    }

    //Functions for navigation drawer activity
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            startActivity(new Intent(MainActivity.this,upload.class).putExtra("email",email));

        } else if (id == R.id.nav_notification) {
            startActivity( new Intent( MainActivity.this,Notification_fragment.class ).putExtra( "Uid",Uid ) );

        } else if (id == R.id.nav_friends) {
            startActivity( new Intent( MainActivity.this,getfriends.class ).putExtra( "Uid",Uid ).putExtra("group_id",group_id));

        } else if (id == R.id.nav_messages) {
            startActivity(new Intent(MainActivity.this,message.class).putExtra("Uid",Uid));

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer( GravityCompat.START);
        return true;
    }


    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<String> onCreateLoader(int i, final Bundle bundle) {
        showProgress( true );
        return new AsyncTaskLoader<String>(this) {
            String jsonArray;
            @Override
            protected void onStartLoading() {
                if(bundle == null)
                    return;
                if(jsonArray != null ){
                    deliverResult( jsonArray );
                }
                else forceLoad();
            }

            @Override
            public String loadInBackground() {
                type = bundle.getString( "type" );
                String group_id = bundle.getString("group_id" );
                Log.i("LOADER EXECUTING","LOADER is executing");
                String con_url = "https://socialnetworkapplication.000webhostapp.com/SocialNetwork/"+type+".php";

                try {
                    URL url = new URL(con_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                    String post_data = URLEncoder.encode("group_id", "UTF-8") + "=" + URLEncoder.encode(group_id, "UTF-8");

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
            public void deliverResult(String data) {
                jsonArray = data;
                super.deliverResult( data );
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String s) {
        showProgress( false );
        Log.e("Result", s);
        FetchData = s;
        String result = "{\"posts\":" + FetchData + "}";
        Log.i("Json", result);
        postList = parseResult(result);
        mAdapter = new PostAdapter(postList,Uid);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {}
}


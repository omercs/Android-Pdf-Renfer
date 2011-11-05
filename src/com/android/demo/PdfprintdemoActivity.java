package com.android.demo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class PdfprintdemoActivity extends Activity {

	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
	private Button startBtn;
	private ProgressDialog mProgressDialogWindow;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//call base
		super.onCreate(savedInstanceState);
		//set layout main.xml to be layout for this activity
		setContentView(R.layout.main);
		//create references to the controls
		startBtn = (Button) findViewById(R.id.startBtn);
		//attach listener in code or you can do that in layout file.
		startBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startDownload();
			}
		});		 
	}

	//Called from button click event
	private void startDownload() {

		//some url to try download action
		String url = "http://www.adobe.com/pdf/pdfs/ISO32000-1PublicPatentLicense.pdf";
		//Async call to download file
		//You MUST call EXECUTE not doinbackground. Otherwise, it will not be ASYNC call. I spend some debugging time on that.
		new DownloadFileAsync().execute(url,"testing.pdf");
	}

	//Dialog setup
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS:
			mProgressDialogWindow = new ProgressDialog(this);
			mProgressDialogWindow.setMessage("Downloading file..");
			mProgressDialogWindow.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialogWindow.setCancelable(false);
			mProgressDialogWindow.show();
			return mProgressDialogWindow;
		default:
			return null;
		}
	}

	class DownloadFileAsync extends AsyncTask<String, String, String> {

		//Set dialod
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_DOWNLOAD_PROGRESS);
		}

		//do actual download
		@Override
		protected String doInBackground(String... aurl) {
			int count;

			String fullpath = "";
			try {

				URL url = new URL(aurl[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();

				int lenghtOfFile = conexion.getContentLength();
				Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);
				//Get directory
				File root = Environment.getExternalStorageDirectory();
				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output =  new FileOutputStream(new File(root,
						aurl[1]));

				byte data[] = new byte[1024];
				fullpath = root.getAbsolutePath()+"/"+aurl[1];
				Log.d("ANDRO_ASYNC","path:"+fullpath);
				long total = 0;

				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress("" + (int) ((total * 100) / lenghtOfFile));
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				input.close();
				Log.d("ANDRO_ASYNC", "File closed1: " + lenghtOfFile);
			} catch (Exception e) {
				Log.d("error",e.getMessage());
			}
			return fullpath;

		}

		/*We use this method to update progress bar*/
		protected void onProgressUpdate(String... progress) {
			Log.d("ANDRO_ASYNC", progress[0]);
			mProgressDialogWindow.setProgress(Integer.parseInt(progress[0]));
		}

		
		/* This method takes the return param from doinbackground call as input*/
		@Override
		protected void onPostExecute(String linkname) {
			mProgressDialogWindow.setMessage("Finished");
			dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
			Log.d("onPostExecute","Send:"+linkname);
			OpenFile(linkname);
		}
		
		/*We are firing intent to open this file with avaialble pdf viewer*/
		protected void OpenFile(String linkname)
		{
			File file = new File(linkname);

            if (file.exists()) {
            	Log.d("OpenFile","Exists:"+linkname);
                Uri path = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(path, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                try {
                    startActivity(intent);
                } 
                catch (ActivityNotFoundException e) {
                    Toast.makeText(PdfprintdemoActivity.this, 
                        "No Application Available to View PDF", 
                        Toast.LENGTH_SHORT).show();
                }
            }else
            {
            	Log.d("OpenFile","DOES NOT Exists:"+linkname);
            }
		}
	}
}
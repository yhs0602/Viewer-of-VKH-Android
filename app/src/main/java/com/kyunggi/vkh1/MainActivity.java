package com.kyunggi.vkh1;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import android.net.*;
///storage/emulated/0/Browsing software/Sectioned images
public class MainActivity extends Activity implements View.OnClickListener,View.OnTouchListener
{
	final int MALE_WHOLE_BODY=1;
	final int FEMALE_WHOLE_BODY=2;
	final int MALE_HEAD=3;
	final int FEMALE_PELVIS=4;
	int subject=MALE_WHOLE_BODY;

	HashMap<Integer,String> colormap=new HashMap<>();

	private static String TAG="VKH";
	
	static final File outputFile=new File("/sdcard/VKH_zip.zip");
	
	static final String urlPath="http://anatomy.dongguk.ac.kr/vkh/Browsing_software_(Male_whole_body_female_whole_body_male_head_female_pelvis)(ver.2).zip";
	
	static final String gitPath="https://github.com/KYHSGeekCode/Viewer-of-VKH-Android/blob/master/README.md";
	public String filenameS(int fr)
	{
		return "/sdcard/VKH/Browsing software/Segmented images/VK_" + subject + "_" + String.format("%04d", fr) + ".png";
	}
	public String filenameA(int fr)
	{
		return "/sdcard/VKH/Browsing software/Sectioned images/VK_" + subject + "_" + String.format("%04d", fr) + ".jpg";
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onMenuItemSelected(int featureid, MenuItem item)
	{

		switch (item.getItemId())
		{
			case R.id.item_male:
				subject = MALE_WHOLE_BODY;
				item.setChecked(true);		
				break;
			case R.id.item_female:
				item.setChecked(true);
				subject = FEMALE_WHOLE_BODY;
				break;
			case R.id.item_male_head:
				item.setChecked(true);
				subject = MALE_HEAD;
				break;
			case R.id.item_female_pelvis:
				subject = FEMALE_PELVIS;
				item.setChecked(true);
				break;
			case R.id.advanced:
				break;
		}
		maxFrame = numFrms[subject - 1];
		return super.onMenuItemSelected(featureid, item);
	}
	@Override
	public boolean onTouch(View p1, MotionEvent p2)
	{
		int action=p2.getAction();
		switch (action)
		{
			case MotionEvent.ACTION_UP:
				int totx=ivAnatomy.getWidth();
				int toty=ivAnatomy.getHeight();
				int x=(int)p2.getX();
				int y=(int)p2.getY();
				float pctX=(float)x / (float)totx;
				float pctY=(float)y / (float)toty;
				Bitmap segbit=BitmapFactory.decodeFile(filenameS(frame));
				x=(int)(pctX*segbit.getWidth());
				y=(int)(pctY*segbit.getHeight());
				//	Bitmap bitmap = ((BitmapDrawable)ivAnatomy.getDrawable()).getBitmap();
				int pixel = segbit.getPixel(x, y);
				Log.d(TAG,"Touch x "+x+" y"+y+" "+Integer.toHexString(pixel));
				tvDesc.setText(colormap.get(new Integer(Color.rgb(Color.red(pixel),Color.green(pixel),Color.blue(pixel)))));
				//Toast.makeText(this, "x=" + x + "y=" + y, 2).show();
				break;
		}
		// TODO: Implement this method
		return true;
	}

	@Override
	public void onClick(View p1)
	{
		// TODO: Implement this method
		switch (p1.getId())
		{
			case R.id.ibPlay:
				if(bPlaying)
				{
					runner.interrupt();
					bPlaying=false;
					ibPlay.setImageResource(android.R.drawable.ic_media_play);
				}else{
					runner.start();
					bPlaying=true;
					ibPlay.setImageResource(android.R.drawable.ic_media_pause);
				}
				break;
			case R.id.ibDirection:
				if (direction == Direction.UP)
				{
					direction = direction.DOWN;
					ibDirection.setImageResource(android.R.drawable.stat_sys_download);
				}
				else
				{
					direction = Direction.UP;
					ibDirection.setImageResource(android.R.drawable.stat_sys_upload);
				}
				break;
		}
		return ;
	}
	Thread runner=new Thread(new Runnable(){
			@Override
			public void run()
			{
				// TODO: Implement this method
				while (!Thread.interrupted())
				{							
					runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								// TODO: Implement this method
								//Toast.makeText(MainActivity.this,filename(frame),2).show();
								ivAnatomy.setImageBitmap(BitmapFactory.decodeFile(filenameA(frame)));
								return ;
							}							
						});
					try
					{
						Thread.sleep(200);
					}
					catch (InterruptedException e)
					{
						break;
					}
					if (direction == Direction.UP)
					{
						frame--;
						if (frame == -1)
						{
							break;
						}
					}
					else if (direction == Direction.DOWN)
					{
						++frame;
						if (frame == maxFrame + 1)
						{
							break;
						}
					}
				}
				return ;
			}		
		});
	ImageView ivAnatomy;
	ImageButton ibPlay;
	ImageButton ibDirection;
	TextView tvDesc;
	enum Direction
	{
		UP,
		DOWN
		};
	Direction direction=Direction.UP;
	int frame=0;
	int maxFrame=0;
	int [] numFrms=new int[4];
	boolean bPlaying=false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
				@Override
				public void uncaughtException(Thread p1, Throwable p2)
				{
					Toast.makeText(MainActivity.this,Log.getStackTraceString(p2),3).show();
					//requestAppPermissions(MainActivity.this);
					//String [] accs=getAccounts();
					SendErrorReport(p2);
					//	ori.uncaughtException(p1, p2);
					Log.wtf(TAG,"UncaughtException",p2);
					finish();
					return ;
				}
			});
		
		ivAnatomy = (ImageView) findViewById(R.id.ivAnatomy);
		ibPlay = (ImageButton) findViewById(R.id.ibPlay);
		ibDirection = (ImageButton) findViewById(R.id.ibDirection);
		ibPlay.setOnClickListener(this);
		ibDirection.setOnClickListener(this);
		tvDesc = (TextView) findViewById(R.id.tvDesc);
		ivAnatomy.setOnTouchListener(this);
		ERRORCODE ec= checkFiles();
		if(!ec.equals(ERRORCODE.OK))
		{
			switch(ec)
			{
				case DIRNOTEXISTS:
					
				case DIRNOTDIR:
					
				case FILESNULL:
					
			}
			AlertDialog.Builder builder=new AlertDialog.Builder(this);
			builder.setTitle("File Download").setMessage("The app needs data from ajou.ac.kr (800MB+). Proceed to download? (Please be sure to connect to wifi, otherwise you may get charge bomb.");
			builder.setPositiveButton("Download", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1,int  p2)
					{
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(gitPath));
						startActivity(intent);
						finish();
						/*
						if(!outputFile.exists())
							new DownLoadAsync().execute();
						else
							new UnZipAsync().execute();
						*/
						return;
					}		
			});
			builder.setNegativeButton("Close app", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1,int  p2)
					{
						finish();
						return ;
					}
				});
			builder.setCancelable(false);
			builder.show();
				return;
		}
		if (maxFrame == 0)
		{
			Toast.makeText(this, "error calculating frames!", 2).show();
			finish();
		}
		//	initColorMap();
		if (initColorMap() == false)
		{
			Toast.makeText(this, "error Initializing colors!", 2).show();
			finish();
		}
	}
	
	private boolean  initColorMap()
	{
		Log.d(TAG,"initColorMap");
		File colorFile=new File("/sdcard/VKH/Browsing software/color.txt");
		if (colorFile.exists())
		{
			try
			{
				FileInputStream fis=new FileInputStream(colorFile);
				DataInputStream dis=new DataInputStream(fis);
				try
				{
					String line=null;
					do{
						line = dis.readLine();
						if(line==null)
							break;
						String[] parsed=line.split("	");
						Log.d(TAG,"parsed "+parsed[0]+" "+parsed[1]+" "+parsed[2]+parsed[3]);
						Log.v(TAG,"length="+parsed.length);
						if (parsed.length >= 4)
						{
							int r=Integer.parseInt(parsed[1]);
							int g=Integer.parseInt(parsed[2]);
							int b=Integer.parseInt(parsed[3]);
							Log.d(TAG,"color "+ Integer.toHexString(Color.rgb(r,g,b)));
							//int b=Integer.parseInt(parsed[parsed.length - 1]);
							//int g=Integer.parseInt(parsed[parsed.length - 2]);
							//int r=Integer.parseInt(parsed[parsed.length - 3]);
//							StringBuilder sb=new StringBuilder();
//							for (int i=0;i < parsed.length - 4;++i)
//							{
//								sb.append(parsed[i]);
//								sb.append(" ");
//							}
//							sb.append(parsed[parsed.length - 4]);
							colormap.put(new Integer(Color.rgb(r, g, b)), parsed[0]);
						}
					}while(line != null);
					colormap.put(new Integer(0xFF000000),"Unknown");
					return true;
					//	Object output[] = Sscanf.scan(line, "%s %d %d %d", 1, 1,1);
					//int sub=((int)output[0]) - 1;
					//int frm=output[1];
				}
				catch (IOException e)
				{}
			}
			catch (FileNotFoundException e)
			{}
		}
		return false;
	}

	enum ERRORCODE
	{
		OK,
		DIRNOTEXISTS,
		DIRNOTDIR,
		FILESNULL	
	}
	private ERRORCODE checkFiles()
	{
		// TODO: Implement this method
		File dir=new File("/sdcard/VKH/Browsing software/Sectioned images/");
		if (dir.exists())
		{
			if (dir.isDirectory())
			{
				File[] files=dir.listFiles();
				if (files != null)
				{
					//int [] maxi=new int[4];
					int max=0;
					for (File file:files)
					{
						String nam=file.getName();
						//	String buffer = ;
						Object output[] = Sscanf.scan(nam, "VK_%d_%d.jpg", 1, 1);
						int sub=((int)output[0]) - 1;
						int frm=output[1];
						//System.out.println("parse count: " + output.length);
						//System.out.println("hex str1: " + (Long)output[0]);

						//nam=nam.replaceAll(".jpg","");
						//	Pattern p = Pattern.compile("VK_\\d+_\\d\\.jpg");
						//	Matcher m = p.matcher(file.getName());
						//int sub = Integer.parseInt( m.group(1));
						//	int frm= Integer.parseInt(m.group(2));
						int cur=numFrms[sub];
						if (cur < frm)
							numFrms[sub] = frm;
					}
					maxFrame = numFrms[0];
					//numFrames=max/5;
					//numFrames=files.length;
					//Toast.makeText(this, ""+numFrms[0]+"/"+numFrms[1]+"/"+numFrms[2]+"/"+numFrms[3], 2).show();
					return ERRORCODE.OK;
				}
				return ERRORCODE.FILESNULL;
			}
			return ERRORCODE.DIRNOTDIR;
		}
		return ERRORCODE.DIRNOTEXISTS;
	}
	class DownLoadAsync extends AsyncTask<Void, Integer, Void>
	{
		//String TAG = getClass().getSimpleName();
		//android.app.ProgressDialog.Builder builder;
		ProgressDialog progress;
		android.app.ProgressDialog dialog;
		protected void onPreExecute (){
			super.onPreExecute();
			Log.d(TAG + " PreExceute","On pre Exceute......");
			progress=new ProgressDialog(MainActivity.this);
			progress.setIndeterminate(false);
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);	
			progress.setMax(100);
			//builder=new android.app.ProgressDialog.Builder(MainActivity.this);
			progress.setTitle("Downloading..");//.setView(progress);
			progress.setCancelable(false);
			progress.show();
		}

		protected Void doInBackground(Void...disasmF) {
			Log.d(TAG + " DoINBackGround","On doInBackground...");
			final int buflen=65536;
			
			try {
				URL u=new URL(urlPath);
				URLConnection conn = u.openConnection();
				int contentLength = conn.getContentLength();
				int times=contentLength/buflen;
				int i=0;
				DataInputStream stream = new DataInputStream(u.openStream());
				byte[] buffer = new byte[buflen];
				DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
				while(stream.read(buffer,0,buflen)>0)
				{
					fos.write(buffer);
					++i;
					publishProgress(new Integer(i));
				}
				//stream.readFully(buffer);
				stream.close();	
				fos.flush();
				fos.close();
			} catch(final FileNotFoundException e) {
				runOnUiThread(new Runnable(){
						@Override
						public void run()
						{
							dialog.dismiss();
							ShowErrorDialog(MainActivity.this,"Failed to download the file.",e);
							return ;
						}
					});
					return null; // swallow a 404
			} catch (final IOException e) {
				runOnUiThread(new Runnable(){
						@Override
						public void run()
						{
							dialog.dismiss();
							ShowErrorDialog(MainActivity.this,"Failed to download the file.",e);
							return ;
						}
					});
				return null; // swallow a 404
			}
			return null;
		}

		protected void onProgressUpdate(Integer...a){
			super.onProgressUpdate(a);
			progress.setProgress(a[0]);
			//Log.d(TAG + " onProgressUpdate", "You are in progress update ... " + a[0]);
		}
		
		 protected void onPostExecute(Void result) {
			 super.onPostExecute(result);
			 dialog.dismiss();
			 new UnZipAsync().execute();
		 //Log.d(TAG + " onPostExecute", "" + result);
		 }
		 
	}
	class UnZipAsync extends AsyncTask<Void, Integer, Void>
	{
		//String TAG = getClass().getSimpleName();
		//android.app.AlertDialog.Builder builder;
		ProgressDialog progress;
		android.app.AlertDialog dialog;
		protected void onPreExecute (){
			super.onPreExecute();
			Log.d(TAG + " PreExceute","On pre Exceute......");
			progress=new ProgressDialog(MainActivity.this);
			progress.setIndeterminate(false);
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);	
			progress.setMax(100);
			progress.setTitle("Unzipping and placing..");//.setView(progress);
			progress.show();
		}

		protected Void doInBackground(Void...disasmF) {
			Log.d(TAG + " DoINBackGround","On doInBackground...");
			InputStream is;
			ZipInputStream zis;
			String path="/sdcard/VKH/";
			int total=10000;//I don't know how many files are there....
			int uzipped=0;
			try 
			{
				String filename;
				is = new FileInputStream(outputFile);
				zis = new ZipInputStream(new BufferedInputStream(is));          
				ZipEntry ze;
				byte[] buffer = new byte[1024];
				int count;
				
				while ((ze = zis.getNextEntry()) != null) 
				{
					// zapis do souboru
					filename = ze.getName();

					// Need to create directories if not exists, or
					// it will generate an Exception...
					if (ze.isDirectory()) {
						File fmd = new File(path + filename);
						fmd.mkdirs();
						continue;
					}

					FileOutputStream fout = new FileOutputStream(path + filename);

					// cteni zipu a zapis
					while ((count = zis.read(buffer)) != -1) 
					{
						fout.write(buffer, 0, count);             
					}
					fout.close();               
					zis.closeEntry();
					++uzipped;
					publishProgress(new Integer(uzipped));
				}

				zis.close();
			} 
			catch(final IOException e)
			{
				runOnUiThread(new Runnable(){
						@Override
						public void run()
						{
							dialog.dismiss();
							ShowErrorDialog(MainActivity.this,"Failed to unzip file",e);
							return ;
						}
					});
					e.printStackTrace();
				return null;
			}
			return null;
		}

		protected void onProgressUpdate(Integer...a){
			super.onProgressUpdate(a);
			progress.setProgress(a[0]);
			//Log.d(TAG + " onProgressUpdate", "You are in progress update ... " + a[0]);
		}

		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.dismiss();
			//Log.d(TAG + " onPostExecute", "" + result);
		}

	}
	private void ShowErrorDialog(Activity a,String title,final Throwable err)
	{
		android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(a);
		builder.setTitle(title);
		builder.setCancelable(false);
		builder.setMessage(Log.getStackTraceString(err));
		builder.setPositiveButton("OK", (DialogInterface.OnClickListener)null);
		builder.setNegativeButton("Send error report", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1,int  p2)
				{
					// TODO: Implement this method
					SendErrorReport(err);
					return ;
				}
			});
		builder.show();
	}
	private void SendErrorReport(Throwable error)
	{
		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		emailIntent.setType("plain/text");

		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
							 new String[] { "jourhyang123@gmail.com"/*"1641832e@fire.fundersclub.com"*/ });

		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
							 "Error report");
		StringBuilder content=new StringBuilder(Log.getStackTraceString(error));
		/*content.append("Emails:");
		 content.append(System.lineSeparator());
		 for(String s:accs)
		 {
		 content.append(s);
		 content.append(System.lineSeparator());
		 }*/
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
							 content.toString());

		startActivity(Intent.createChooser(emailIntent, "Send crash report as an issue by email"));
	}
	
	//https://stackoverflow.com/questions/3382996/how-to-unzip-files-programmatically-in-android
	private boolean unpackZip(String path, String zipname)
	{       
		InputStream is;
		ZipInputStream zis;
		try 
		{
			String filename;
			is = new FileInputStream(path + zipname);
			zis = new ZipInputStream(new BufferedInputStream(is));          
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;

			while ((ze = zis.getNextEntry()) != null) 
			{
				// zapis do souboru
				filename = ze.getName();

				// Need to create directories if not exists, or
				// it will generate an Exception...
				if (ze.isDirectory()) {
					File fmd = new File(path + filename);
					fmd.mkdirs();
					continue;
				}

				FileOutputStream fout = new FileOutputStream(path + filename);

				// cteni zipu a zapis
				while ((count = zis.read(buffer)) != -1) 
				{
					fout.write(buffer, 0, count);             
				}

				fout.close();               
				zis.closeEntry();
			}

			zis.close();
		} 
		catch(IOException e)
		{
			
			e.printStackTrace();
			return false;
		}

		return true;
	}
	//https://stackoverflow.com/questions/1714761/download-a-file-programmatically-on-android
    static void downloadFile(String url, File outputFile) {
		try {
			URL u = new URL(url);
			URLConnection conn = u.openConnection();
			int contentLength = conn.getContentLength();
			DataInputStream stream = new DataInputStream(u.openStream());
			byte[] buffer = new byte[contentLength];
			stream.readFully(buffer);
			stream.close();
			DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
			fos.write(buffer);
			fos.flush();
			fos.close();
		} catch(FileNotFoundException e) {
			
			return; // swallow a 404
		} catch (IOException e) {
			return; // swallow a 404
		}
	}
	
}

package com.zmosoft.flickrfree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ServiceConnection;

public class TransferProgress extends Activity implements OnClickListener {

    private ServiceConnection m_svc_upload = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			m_transfer_service = ((TransferService.TransferServiceBinder)service).getService();
			Intent broadcast_intent = new Intent();
			broadcast_intent.setAction(GlobalResources.INTENT_BIND_TRANSFER_SERVICE);
			getApplicationContext().sendBroadcast(broadcast_intent);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			m_transfer_service = null;
		}
    };
    
	// This is the receiver that we use to update the percentage progress display
    // for the current upload and/or download.
	public class PercentProgressUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals(GlobalResources.INTENT_UPLOAD_PROGRESS_UPDATE)) {
	        	Bundle extras = intent.getExtras();
	        	if (extras != null && extras.containsKey("percent")) {
		        	ListView lv = ((ListView)findViewById(R.id.TransferProgressList));
		        	if (lv != null && lv.getChildCount() > 0) {
			        	TextView status_text = (TextView)(lv.getChildAt(0).findViewById(R.id.TransferPictureStatus));
			        	if (status_text != null) {
			        		status_text.setVisibility(View.GONE);
			        	}
			        	ProgressBar progress = (ProgressBar)(lv.getChildAt(0).findViewById(R.id.TransferProgressBar));
			        	if (progress != null) {
			        		progress.setVisibility(View.VISIBLE);
				        	progress.setProgress((int)extras.getLong("percent"));
			        	}
		        	}
	        	}
	        }
	        else if (intent.getAction().equals(GlobalResources.INTENT_DOWNLOAD_PROGRESS_UPDATE)) {
	        	Bundle extras = intent.getExtras();
	        	if (extras != null && extras.containsKey("percent")) {
		        	ListView lv = ((ListView)findViewById(R.id.TransferProgressList));
		        	if (lv != null && lv.getChildCount() > 1) {
			        	TextView status_text = (TextView)(lv.getChildAt(1).findViewById(R.id.TransferPictureStatus));
			        	if (status_text != null) {
			        		status_text.setVisibility(View.GONE);
			        	}
			        	ProgressBar progress = (ProgressBar)(lv.getChildAt(1).findViewById(R.id.TransferProgressBar));
			        	if (progress != null) {
			        		progress.setVisibility(View.VISIBLE);
				        	progress.setProgress((int)extras.getLong("percent"));
			        	}
		        	}
	        	}
	        }
		}
	}

	// This is the receiver that we use to know when a transfer starts or
	// finishes so we can update the progress display.
	public class StatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals(GlobalResources.INTENT_UPLOAD_STARTED) ||
	        	intent.getAction().equals(GlobalResources.INTENT_UPLOAD_FINISHED) ||
	        	intent.getAction().equals(GlobalResources.INTENT_DOWNLOAD_STARTED) ||
	        	intent.getAction().equals(GlobalResources.INTENT_DOWNLOAD_FINISHED)) {
	            updateProgress();
	        }
		}
	}

	// This receiver is necessary to let us know when the Transfer Service has
	// been successfully bound so we can access it and update the progress
	// display.
	public class BindTransferServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals(GlobalResources.INTENT_BIND_TRANSFER_SERVICE)) {
	            updateProgress();
	        }
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.transfer_progress);
		
		bindTransferServiceReceiver();
	}
	
	public void bindTransferServiceReceiver() {
		m_bind_transfer_service_receiver = new BindTransferServiceReceiver();
		if (m_bind_transfer_service_receiver != null) {
			this.registerReceiver(m_bind_transfer_service_receiver, new IntentFilter(GlobalResources.INTENT_BIND_TRANSFER_SERVICE));
		}
		m_update_receiver = new PercentProgressUpdateReceiver();
		if (m_update_receiver != null) {
			this.registerReceiver(m_update_receiver, new IntentFilter(GlobalResources.INTENT_UPLOAD_PROGRESS_UPDATE));
		}
		
		this.bindService(new Intent(this, Uploader.class), m_svc_upload, Context.BIND_AUTO_CREATE);

        m_receiver = new StatusReceiver();
		if (m_receiver != null) {
			this.registerReceiver(m_receiver, new IntentFilter(GlobalResources.INTENT_UPLOAD_STARTED));
			this.registerReceiver(m_receiver, new IntentFilter(GlobalResources.INTENT_UPLOAD_FINISHED));
		}
	}

	public void unbindTransferServiceReceiver() {
		if (m_svc_upload != null) {
			this.unbindService(m_svc_upload);
		}
		if (m_receiver != null) {
			this.unregisterReceiver(m_receiver);
		}
		if (m_bind_transfer_service_receiver != null) {
			this.unregisterReceiver(m_bind_transfer_service_receiver);
		}
		if (m_update_receiver != null) {
			this.unregisterReceiver(m_update_receiver);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindTransferServiceReceiver();
	}
	
	@Override
	public void onClick(View v) {
	}
	
	public void addUpload(Bundle upload_info) {
		if (m_transfer_service != null && upload_info != null) {
			m_transfer_service.addUpload(upload_info);
		}
	}
	
	public void addDownload(Bundle download_info) {
		if (m_transfer_service != null && download_info != null) {
			m_transfer_service.addDownload(download_info);
		}
	}
	
	public void updateProgress() {
    	if (m_transfer_service != null) {
			ArrayList < HashMap<String, String> > transferlist = new ArrayList < HashMap<String,String> >();
			LinkedList<Bundle> upload_list = m_transfer_service.getUploads();
			LinkedList<Bundle> download_list = m_transfer_service.getDownloads();
			
			// If either the upload or download lists is empty, then be sure to cancel the appropriate
			// notification. If both are empty, then there's nothing to do here, so close the TransferProgress
			// Activity.
			if (upload_list.isEmpty()) {
				Toast.makeText(getApplicationContext(), "Upload(s) Completed", Toast.LENGTH_SHORT).show();
				((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(GlobalResources.UPLOADER_ID);
			}
			if (download_list.isEmpty()) {
				Toast.makeText(getApplicationContext(), "Download(s) Completed", Toast.LENGTH_SHORT).show();
				((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(GlobalResources.DOWNLOADER_ID);
			}
	        if (upload_list.isEmpty() && download_list.isEmpty()) {
				finish();
			}

	        // We want to interleave the list of uploads and downloads in the ListView.
			HashMap<String, String> m;
			Bundle b = null;
			while (!upload_list.isEmpty() || !download_list.isEmpty()) {
				if (!upload_list.isEmpty()) {
					m = new HashMap<String, String>();
					b = upload_list.remove();
					m.put("title", b.getString("title"));
					m.put("status", "Pending");
					transferlist.add(m);
				}
				if (!download_list.isEmpty()) {
					m = new HashMap<String, String>();
					b = download_list.remove();
					m.put("title", b.getString("title"));
					m.put("status", "Pending");
					transferlist.add(m);
				}
			}
			
			ListView lv = ((ListView)findViewById(R.id.TransferProgressList));
	        lv.setAdapter(new SimpleAdapter(
					this,
					transferlist,
					R.layout.transfer_progress_item,
					new String[]{"title","status"},
					new int[]{R.id.TransferPictureName, R.id.TransferPictureStatus}));
    	}
	}
	
	private StatusReceiver m_receiver = null;
	private BindTransferServiceReceiver m_bind_transfer_service_receiver = null;
	private PercentProgressUpdateReceiver m_update_receiver = null;
    private TransferService m_transfer_service = null;
}
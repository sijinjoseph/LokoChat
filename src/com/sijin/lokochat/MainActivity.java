package com.sijin.lokochat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kinvey.KCSClient;
import com.kinvey.KinveySettings;
import com.kinvey.util.ListCallback;
import com.kinvey.util.ScalarCallback;
import com.tapped.nfc.NfcUtils;

public class MainActivity extends FragmentActivity {

	private KCSClient kinveyClient;
	private NfcAdapter mNfcAdapter;

	private String mRoomName;
	private String mNickname;
	private Button mScanButton;
	private Button mSendButton;
	private EditText mNicknameEditText;
	private EditText mMessageEditText;
	private TextView mMessages;
	private String mRandomNickname;

	Handler mHandler = new Handler();
	Runnable runMessageRefresh = new Runnable() {
		@Override
		public void run() {
			log("tick");
			if (mRoomName != null && mNickname != null && mMessages != null) {
				kinveyClient.mappeddata("messages").all(MessageEntity.class,
						new ListCallback<MessageEntity>() {
							@Override
							public void onSuccess(
									final List<MessageEntity> results) {
								String allMessages = "";
								for (MessageEntity me : results) {
									if (me.getRoom().equals(mRoomName)) {
										allMessages += me.getNickname() + ": "
												+ me.getMessage() + "\r\n";
									}
								}

								mMessages.setText(allMessages);
							}

							@Override
							public void onFailure(Throwable error) {
								log("refresh messages failed", error);
							}

						});
				mHandler.postDelayed(this, 1000);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		KinveySettings settings = new KinveySettings("kid_eexo_BhCT5",
				"f9415127366340249d36369d1c54793f");
		kinveyClient = KCSClient.getInstance(this.getApplicationContext(),
				settings);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter == null) {
			Toast.makeText(this, "Sorry, NFC is not available on this device",
					Toast.LENGTH_SHORT).show();
			finish();
		}

		setContentView(R.layout.login_layout);

		mScanButton = (Button) findViewById(R.id.button_scan_tag);
		mNicknameEditText = (EditText) findViewById(R.id.editText1);
		
		if(mRandomNickname == null) {
			Random random = new Random(System.currentTimeMillis());
			mRandomNickname = "Anonymous" + random.nextInt(10000);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	@Override
	public void onResume() {
		super.onResume();
		if ((NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction()) || NfcAdapter.ACTION_NDEF_DISCOVERED
				.equals(getIntent().getAction()))) {
			processReadIntent(getIntent());
		}
		
		mNicknameEditText.setText(mRandomNickname);
		UpdateScanButton();
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(runMessageRefresh);
	}

	public void processReadIntent(Intent intent) {
		List<NdefMessage> intentMessages = NfcUtils
				.getMessagesFromIntent(intent);
		List<String> payloadStrings = new ArrayList<String>(
				intentMessages.size());

		for (NdefMessage message : intentMessages) {
			for (NdefRecord record : message.getRecords()) {
				byte[] payload = record.getPayload();
				String payloadString = new String(payload);

				if (!TextUtils.isEmpty(payloadString))
					payloadStrings.add(payloadString);
			}
		}

		if (payloadStrings.size() > 0) {
			mRoomName = payloadStrings.get(0);
			Toast.makeText(this, mRoomName, Toast.LENGTH_SHORT).show();

			UpdateScanButton();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_loko_chat, menu);
		return true;
	}

	public void onScanClick(View view) {
		mNickname = mNicknameEditText.getText().toString();

		setContentView(R.layout.chat_view_layout);

		mMessageEditText = (EditText) findViewById(R.id.editText1);
		mMessages = (TextView) findViewById(R.id.textView2);
		mSendButton = (Button) findViewById(R.id.buttonSend);
		TextView roomLabel = (TextView) findViewById(R.id.textView1);
		char[] label = ("Connected to " + mRoomName).toCharArray();
		roomLabel.setText(label, 0, label.length);

		mHandler.post(runMessageRefresh);
	}

	public void onMessageSend(View view) {
		mSendButton.setEnabled(false);

		String message = mMessageEditText.getText().toString();
		MessageEntity messageEntity = new MessageEntity(mRoomName, mNickname,
				message);
		kinveyClient.mappeddata("messages").save(messageEntity,
				new ScalarCallback<MessageEntity>() {
					@Override
					public void onSuccess(MessageEntity msg) {
						log("Saved message");
						mMessageEditText.setText("");
						mSendButton.setEnabled(true);
					}

					@Override
					public void onFailure(Throwable e) {
						log("Error saving message", e);
						mSendButton.setEnabled(true);
					}
				});
	}

	public void onLeaveRoom(View view) {
		mRoomName = null;
		UpdateScanButton();
		mHandler.removeCallbacks(runMessageRefresh);

		setContentView(R.layout.login_layout);
	}

	private void UpdateScanButton() {
		if (mRoomName != null) {
			mScanButton.setText("Chat @" + mRoomName);
			mScanButton.setEnabled(true);
		} else {
			mScanButton.setText(R.string.button_scan_tag_start_label);
			mScanButton.setEnabled(false);
		}
	}

	private void log(String message) {
		Log.i("LokoChat", message);
	}

	private void log(String message, Throwable e) {
		Log.e("LokoChat", message, e);
	}
}

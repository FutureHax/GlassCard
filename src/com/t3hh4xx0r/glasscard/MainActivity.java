package com.t3hh4xx0r.glasscard;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.MotionEvent;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.CameraManager;
import com.google.android.glass.timeline.TimelineManager;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class MainActivity extends Activity implements
		GestureDetector.BaseListener {
	private static final int REQ_IMAGE = 0;
	private static final int REQ_SPEECH = 1;
	Card rootCard;
	private GestureDetector mDetector;
	private AudioManager mAudioManager;
	final static int TAP_SPEAK = 0;
	final static int TAP_SEND = 1;
	int TAP_STATE = TAP_SPEAK;
	String imagePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mDetector = new GestureDetector(this).setBaseListener(this);
		rootCard = new Card(this);

		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, REQ_IMAGE);
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		return mDetector.onMotionEvent(event);
	}

	@Override
	public boolean onGesture(Gesture gesture) {
		if (gesture == Gesture.TAP) {
			mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
			if (TAP_STATE == TAP_SPEAK) {
				Intent intent = new Intent(
						RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				startActivityForResult(intent, REQ_SPEECH);
			} else {
				rootCard.setFootnote("");
				TimelineManager tManager = TimelineManager
						.from(MainActivity.this);
				tManager.insert(rootCard);
				finish();

			}
			return true;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQ_IMAGE) {
			File image = new File(
					data.getStringExtra(CameraManager.EXTRA_THUMBNAIL_FILE_PATH));

			if (!image.exists()) {
				return;
			}
			imagePath = data
					.getStringExtra(CameraManager.EXTRA_PICTURE_FILE_PATH);
			Uri imageUri = Uri.fromFile(image);
			rootCard.addImage(imageUri);
			rootCard.setFootnote("Tap to speak a message");
			setContentView(rootCard.toView());
		} else if (requestCode == REQ_SPEECH && resultCode == RESULT_OK) {
			List<String> results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			String spokenText = results.get(0);
			if (spokenText != null && spokenText.length() != 0) {
				rootCard.setText(spokenText);
				rootCard.setFootnote("Tap to send your card to the timeline");
				TAP_STATE = TAP_SEND;
			}
			setContentView(rootCard.toView());
		}
	}
}

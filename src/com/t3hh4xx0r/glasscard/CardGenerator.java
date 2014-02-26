package com.t3hh4xx0r.glasscard;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

public class CardGenerator {

	private String imageName;

	public class GeneratorTask extends AsyncTask<Void, Void, String> {
		Context c;
		String dir;
		String og_post_content;
		ProgressDialog progressSpinner;
		OnCardRequestedListener listener;

		@Override
		protected String doInBackground(Void... arg0) {
			try {
				File dirFile = new File(dir);
				if (!dirFile.exists()) {
					dirFile.mkdir();
				}

				Bitmap content_image = drawTextToBitmap(og_post_content);
				FileOutputStream out = new FileOutputStream(
						getImagePath(og_post_content));
				content_image.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				out.close();

				return getImagePath(og_post_content);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (progressSpinner.isShowing()) {
				progressSpinner.dismiss();
			}
			listener.onCardReturned(result);
		}

		private GeneratorTask(Context c, String dir, String og_post_content,
				OnCardRequestedListener listener) {
			super();
			this.c = c;
			this.dir = dir;
			this.listener = listener;
			this.og_post_content = og_post_content;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressSpinner = new ProgressDialog(this.c);
			progressSpinner.setTitle("Generating");
			progressSpinner.setIndeterminate(true);
			progressSpinner.setIcon(R.drawable.ic_launcher);
			progressSpinner.show();
		}

	}

	public static interface OnCardRequestedListener {
		public void onCardReturned(String path);
	}

	Context c;
	boolean shownCutWarning = false;

	String dir = Environment.getExternalStorageDirectory() + "/" + "PostCards/";

	public CardGenerator(Context c) {
		this.c = c;
	}

	public void generatePost(final String og_post_content,
			OnCardRequestedListener listener) {
		new GeneratorTask(c, dir, og_post_content, listener).execute();
	}

	private String generateText(String og_post_content) {
		StringBuilder sb = new StringBuilder();
		sb.append(og_post_content);
		sb.append(" #throughGlass");

		return sb.toString();
	}

	public Bitmap drawTextToBitmap(String post_text) {
		post_text = generateText(post_text);
		
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = Bitmap.createBitmap(500, 255, conf);

		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		
		draw(canvas, 40, 40, post_text);
		
		return bitmap;
	}

	private void draw(Canvas canvas, final int x, final int y, final String text) {
		TextPaint mTextPaint = new TextPaint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(100);

		StaticLayout mTextLayout = new StaticLayout(text, mTextPaint,
				canvas.getWidth() - x * 2, Alignment.ALIGN_NORMAL, 1.0f, 0.0f,
				false);

		while (mTextLayout.getHeight() > canvas.getHeight() - y * 2) {
			mTextPaint.setTextSize(mTextPaint.getTextSize() - 1);
			mTextLayout = new StaticLayout(text, mTextPaint, canvas.getWidth()
					- x * 2, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		}
		canvas.save();
		canvas.translate(
				(canvas.getWidth() / 2) - (mTextLayout.getWidth() / 2),
				(canvas.getHeight() / 2) - ((mTextLayout.getHeight() / 2)));
		mTextLayout.draw(canvas);
		canvas.restore();
	}

	String getImagePath(String content) {
		return dir + getImageName(content) + ".png";
	}

	String getImageName(String content) {
		if (imageName == null) {
			imageName = Integer.toString(content.hashCode());
		}
		return imageName;

	}
}

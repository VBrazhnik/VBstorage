package com.vbrazhnik.vbstorage.database;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.vbrazhnik.vbstorage.entities.Item;
import com.vbrazhnik.vbstorage.entities.Type;

import java.io.File;
import java.io.IOException;

public class DirectoryHelper {

	private static String path = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.vbrazhnik.vbstorage/";

	public static String createUniqueFilename () {
		return Long.toHexString(System.currentTimeMillis());
	}

	public static String getImagesDirectory()
	{
		return (path + "images/");
	}

	public static String getWEBPagesDirectory()
	{
		return (path + "webpages/");
	}

	public static String getAudioDirectory()
	{
		return (path + "audio/");
	}

	public static void createDirectories()
	{
		createNoMediaFile(getImagesDirectory());
		createNoMediaFile(getWEBPagesDirectory());
		createNoMediaFile(getAudioDirectory());
	}

	private static void createNoMediaFile (String directoryLocation) {
		File directory = new File(directoryLocation);
		if (!directory.exists()) {
			if (directory.mkdirs()) {
				try {
					new File(directory.getParent(), ".nomedia").createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getRealPathFromURI(Context context, Uri contentUri) {
		String result = null;
		Cursor cursor = null;
		try {
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri,  filePathColumn, null, null, null);
			if (cursor != null) {
				int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				result = cursor.getString(columnIndex);
			}
			return result;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public static boolean deleteFile(Item item)
	{
		if (item.getType() == Type.IMAGE.getCode())
			return new File(DirectoryHelper.getImagesDirectory() + item.getImagePath()).delete();
		else if (item.getType() == Type.AUDIO.getCode())
			return new File(DirectoryHelper.getAudioDirectory() + item.getAttachPath()).delete();
		else if (item.getType() == Type.WEB_PAGE.getCode())
			return new File(DirectoryHelper.getWEBPagesDirectory() + item.getAttachPath()).delete()
					&& new File(DirectoryHelper.getWEBPagesDirectory() + item.getImagePath()).delete();
		return false;
	}
}

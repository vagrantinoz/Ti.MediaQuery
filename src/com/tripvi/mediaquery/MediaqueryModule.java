/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package com.tripvi.mediaquery;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollDict;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.util.TiConvert;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.lang.Math;

import android.app.Activity;
import android.net.Uri;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.provider.MediaStore;
import android.media.ExifInterface;
import android.graphics.Matrix;
import android.os.Environment;



@Kroll.module(name="Mediaquery", id="com.tripvi.mediaquery")
public class MediaqueryModule extends KrollModule
{

	// Standard Debugging variables
	private static final String TAG = "MediaqueryModule";

	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;
	
	public MediaqueryModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(TAG, "inside onAppCreate");
		// put module init code that needs to run when the application is created
	}
	
	// Methods
	@Kroll.method
	public KrollDict queryAlbumList()
	{
		Log.d(TAG, "");
		Log.d(TAG, "queryAlbums called: ");
		
		// which image properties are we querying
		String[] projection = new String[]{
			"DISTINCT " + MediaStore.Images.Media.BUCKET_ID,
			MediaStore.Images.Media.DATE_TAKEN,
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
			MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATA,
		};
		
		String[] projection2 = new String[]{
			MediaStore.Images.Media.BUCKET_ID,
			MediaStore.Images.Media.SIZE,
		};
		
		String orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC";
		
		// Make the query.
		Activity activity = this.getActivity();
		Cursor cur = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
			projection, 
			MediaStore.Images.Media.BUCKET_ID + " IS NOT NULL) GROUP BY (" + MediaStore.Images.Media.BUCKET_ID, 
			null, 
			orderBy);

		Log.d(TAG," query count="+cur.getCount());
		
		// formatting result
		KrollDict result = new KrollDict(cur.getCount());
		HashMap<String, Object> obj = new HashMap<String, Object>();

		if (cur.moveToFirst()) {
			String id;
			String image_id;
			String bucket_id;
			String bucket;
			Long date;
			int count;
			String path;
			
			int idColumn = cur.getColumnIndex(MediaStore.Images.Media._ID);
			int bucketIdColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
			int bucketColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
			int dateColumn = cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
			int dataColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);
			
			int order = 0;
			do {
				// Get the field values
				id = cur.getString(idColumn);
				bucket_id = cur.getString(bucketIdColumn);
				bucket = cur.getString(bucketColumn);
				date = cur.getLong(dateColumn);
				path = cur.getString(dataColumn);
				
				Cursor cursorForCountPhoto = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
					projection2, 
					MediaStore.Images.Media.BUCKET_ID + " IS NOT NULL AND " + MediaStore.Images.Media.BUCKET_ID + " == " + bucket_id + " AND " + MediaStore.Images.Media.SIZE + " > 0", 
					null, 
					"");
				
				count = cursorForCountPhoto.getCount();
				cursorForCountPhoto.close();
				
				// Do something with the values.
				Log.d(TAG, id + " bucket_id=" + bucket_id + " bucket=" + bucket + "  date_taken=" + date);				
				// id, path, date_taken
				obj.put("id", bucket_id);
				obj.put("name", bucket);
				obj.put("dateTaken", date);
				obj.put("thumbnail_id", id);
				obj.put("photos_count", count);
				
				// exif
				try {
					ExifInterface exif = new ExifInterface(path);
					// orientation
					int orientation = Integer.parseInt(exif.getAttribute("Orientation"));
					obj.put("orientation", orientation);
				}
				catch (Exception e) {
					Log.e(TAG, "Exif - ERROR");
					Log.e(TAG, e.getMessage());
				}
				
				result.put(String.valueOf(order), new KrollDict(obj)); //add the item
				order++;
			} while (cur.moveToNext());
		}
		
		cur.close();
		
		return result;
	}
	
	// Methods
	@Kroll.method
	public KrollDict queryPhotos(Integer offset, Integer limit)
	{
		Log.d(TAG, "");
		Log.d(TAG, "queryPhotos called: ");

		//
		if (offset == null) offset = 0;
		if (limit == null) limit = 100;
		
		String where = MediaStore.Images.Media.SIZE + " > 0";//  The size of the file in bytes가 0 이상인 경우만 query
		String orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC LIMIT " + String.valueOf(limit) + " OFFSET " + String.valueOf(offset);
		
		String[] projection = new String[] {
			MediaStore.Images.Media.DATE_TAKEN,
			MediaStore.Images.Media.BUCKET_ID,
			MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATA,
			MediaStore.Images.Media.LATITUDE,
			MediaStore.Images.Media.LONGITUDE,
			MediaStore.Images.Media.SIZE,
			MediaStore.Images.Media.DATE_ADDED,
			MediaStore.Images.Media.BUCKET_ID,
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
			MediaStore.Images.Media.MINI_THUMB_MAGIC,
		};  
		
		Log.d(TAG, orderBy);
		
        // make managedQuery:
		Activity activity = this.getActivity();
		Cursor c = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where, null, orderBy);
		
		Log.d(TAG, "Media.images query result count = " + c.getCount());
        
		return getPhotos(activity, c);
	}
	
	// Methods
	@Kroll.method
	public KrollDict queryPhotosByAlbumId(String bucket_id, Integer offset, Integer limit) {
		Log.d(TAG, "");
		Log.d(TAG, "queryPhotosByAlbumId called: ");
		
		//
		if (offset == null) offset = 0;
		if (limit == null) limit = 100;
		
		String where = MediaStore.Images.Media.SIZE + " > 0 AND "
		+ MediaStore.Images.Media.BUCKET_ID + " == " + bucket_id; //  The size of the file in bytes가 0 이상인 경우만 query
		String orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC LIMIT " + String.valueOf(limit) + " OFFSET " + String.valueOf(offset);
		
		String[] projection = new String[] {
			MediaStore.Images.Media.DATE_TAKEN,
			MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATA,
			MediaStore.Images.Media.LATITUDE,
			MediaStore.Images.Media.LONGITUDE,
			MediaStore.Images.Media.SIZE,
			MediaStore.Images.Media.DATE_ADDED,
			MediaStore.Images.Media.BUCKET_ID,
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
			MediaStore.Images.Media.MINI_THUMB_MAGIC,
		};  
		
		Log.d(TAG, orderBy);
		
        // make managedQuery:
		Activity activity = this.getActivity();
		Cursor c = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where, null, orderBy);
		
		Log.d(TAG, "Media.images query result count = " + c.getCount());
        
		return getPhotos(activity, c);
	}
	
	@Kroll.method
	public KrollDict queryPhotosByDate(Object start, Object end)
	{
		Log.d(TAG, "");
		Log.d(TAG, "queryPhotosByDate called: ");

		Date minDate = TiConvert.toDate(start);
		Date maxDate = TiConvert.toDate(end);
		
		Log.d(TAG, "Start ~ end ::: ");
		Log.d(TAG, "Start :: " + String.valueOf(minDate.getTime()));
		Log.d(TAG, "End :: " + String.valueOf(maxDate.getTime()));
		
		String where = MediaStore.Images.Media.SIZE + " > 0 AND "
		+ MediaStore.Images.Media.DATE_TAKEN + " >= " + String.valueOf(minDate.getTime()) 
		+ " AND " + MediaStore.Images.Media.DATE_TAKEN + " < " + String.valueOf(maxDate.getTime()); 
		
		String orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC";

		String[] projection = new String[] {
			MediaStore.Images.Media.DATE_TAKEN,
			MediaStore.Images.Media._ID,
			MediaStore.Images.Media.DATA,
			MediaStore.Images.Media.LATITUDE,
			MediaStore.Images.Media.LONGITUDE,
			MediaStore.Images.Media.SIZE,
			MediaStore.Images.Media.DATE_ADDED,
			MediaStore.Images.Media.BUCKET_ID,
			MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
			MediaStore.Images.Media.MINI_THUMB_MAGIC,
		};
		
		Activity activity = this.getActivity();
		Cursor c = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, where, null, orderBy);
		
		Log.d(TAG, "Media.images query result count = " + c.getCount());

		return getPhotos(activity, c);
	}
	
	@Kroll.method
	public KrollDict queryPhotosByOneDate(Object date)
	{
		Log.d(TAG, "");
		Log.d(TAG, "queryPhotosByOneDate called: ");

		Date minDate = TiConvert.toDate(date);
		Date maxDate = new Date(minDate.getTime() + (60 * 60 * 24 * 1000));
		
		Log.d(TAG, "date ::: ");
		Log.d(TAG, "Start :: " + String.valueOf(minDate.getTime()));
		Log.d(TAG, "End :: " + String.valueOf(maxDate.getTime()));
		
		return queryPhotosByDate(date, maxDate);
	}
	
	public KrollDict getPhotos(Activity activity, Cursor c) {
		
		// formatting result
		KrollDict result = new KrollDict(c.getCount());
		HashMap<String, Object> obj = new HashMap<String, Object>();
        
		if (c.getCount() > 0) {
    		c.moveToFirst();
            
			for (Integer i=0; !c.isAfterLast(); i++) {
				
				String _id = c.getString(c.getColumnIndex(MediaStore.Images.Media._ID));
				String path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
				Long dateTaken = c.getLong(c.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
				
				// id, path, date_taken
				obj.put("id", _id);
				obj.put("path", path);
				obj.put("size", c.getString(c.getColumnIndex(MediaStore.Images.Media.SIZE)));
				obj.put("dateTaken", dateTaken);
				// album info
				obj.put("album_id", c.getString(c.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)));
				obj.put("album_name", c.getString(c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)));
				// gps info
				obj.put("lat", c.getFloat(c.getColumnIndex(MediaStore.Images.Media.LATITUDE)));
				obj.put("lng", c.getFloat(c.getColumnIndex(MediaStore.Images.Media.LONGITUDE)));
				
				// exif
				try {
					ExifInterface exif = new ExifInterface(path);
					// width, height
					obj.put("width", exif.getAttribute("ImageWidth"));
					obj.put("height", exif.getAttribute("ImageLength"));
					// gps processing method
					obj.put("gpsMethod", exif.getAttribute("GPSProcessingMethod"));
					// gps timestamp
					obj.put("gpsDate", exif.getAttribute("GPSDateStamp"));
					obj.put("gpsTime", exif.getAttribute("GPSTimeStamp"));
					// gps location
					float[] latlong = new float [] { 0.0f, 0.0f };
					exif.getLatLong(latlong);
					obj.put("exif_lat", latlong[0]);
					obj.put("exif_lng", latlong[1]);
					// orientation
					int orientation = Integer.parseInt(exif.getAttribute("Orientation"));
					obj.put("orientation", orientation);
					obj.put("rotate", (orientation == 6 || orientation == 8) ? "1" : "0");
				}
				catch (Exception e) {
					Log.e(TAG, "Exif - ERROR");
					Log.e(TAG, e.getMessage());
				}
                
				result.put(i.toString(), new KrollDict(obj)); //add the item

				c.moveToNext();
			}
		}
        
		c.close();
		
		
		return result;
	}
	
	@Kroll.method
	public KrollDict getThumbnail(String id) {
		Activity activity = this.getActivity();
		
		String[] projection2 = {
			MediaStore.Images.Thumbnails.DATA,
			MediaStore.Images.Thumbnails.IMAGE_ID,
			MediaStore.Images.Thumbnails.HEIGHT,
			MediaStore.Images.Thumbnails.WIDTH,
			MediaStore.Images.Thumbnails.KIND,
		};
	
		Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(
			activity.getContentResolver(),
			Long.parseLong(id),
			MediaStore.Images.Thumbnails.MINI_KIND,
			projection2
		);
		cursor.moveToFirst();
		
		if (cursor.getCount() > 0) {
			Log.d(TAG, "thumbnail( " + id + " ) already exist!!");
			
			HashMap<String, Object> thumbnailInfo = new HashMap<String, Object>();
			thumbnailInfo.put("image", cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA)));
			thumbnailInfo.put("width", cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Thumbnails.WIDTH)));
			thumbnailInfo.put("height", cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Thumbnails.HEIGHT)));
			
			return new KrollDict(thumbnailInfo);
		}
		else {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 16;
			
			Bitmap thumb = MediaStore.Images.Thumbnails.getThumbnail(activity.getContentResolver(), Long.parseLong(id), MediaStore.Images.Thumbnails.MINI_KIND, options);
			if (thumb != null) {
				Log.d(TAG, "thumbnail( " + id + " ) do not exist. but request 'thumbnail create'!!");
				return getThumbnail(id);
			}
			else {
				Log.w(TAG, "thumbnail( " + id + " ) is null!!");
				return null;
			}
		}
	}
	
	
	/*
	 * VIDEO는 album없이 그냥 전체목록을 가져와서 표시한다.
	 *
	 */
	@Kroll.method
	public KrollDict queryVideos(Integer offset, Integer limit) {
		
		Log.d(TAG, "");
		Log.d(TAG, "queryVideos called: ");
		
		//
		if (offset == null) offset = 0;
		if (limit == null) limit = 100;
		
		String where = MediaStore.Video.VideoColumns.SIZE + " > 0";//  The size of the file in bytes가 0 이상인 경우만 query
		String orderBy = MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC LIMIT " + String.valueOf(limit) + " OFFSET " + String.valueOf(offset);
		
		String[] projection = new String[] {
			MediaStore.Video.VideoColumns.DATE_TAKEN,
			MediaStore.Video.VideoColumns.BUCKET_ID,
	        MediaStore.Video.VideoColumns._ID,
	        MediaStore.Video.VideoColumns.TITLE,
	        MediaStore.Video.VideoColumns.ARTIST,
			MediaStore.Video.VideoColumns.DATA,
			MediaStore.Video.VideoColumns.LATITUDE,
			MediaStore.Video.VideoColumns.LONGITUDE,
			MediaStore.Video.VideoColumns.SIZE,
			MediaStore.Video.VideoColumns.DATE_ADDED,
			MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
			MediaStore.Video.VideoColumns.DURATION,
			MediaStore.Video.VideoColumns.RESOLUTION,
		};  
		
		Log.d(TAG, orderBy);
		
        // make managedQuery:
		Activity activity = this.getActivity();
		Cursor c = activity.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, where, null, orderBy);
		
		Log.d(TAG, "Media.videos query result count = " + c.getCount());
        
		
		// formatting result
		KrollDict result = new KrollDict(c.getCount());
		HashMap<String, Object> obj = new HashMap<String, Object>();
        
		if (c.getCount() > 0) {
    		c.moveToFirst();
            
			for (Integer i=0; !c.isAfterLast(); i++) {
				
				String _id = c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns._ID));
				String path = c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
				Long dateTaken = c.getLong(c.getColumnIndex(MediaStore.Video.VideoColumns.DATE_TAKEN));
				
				// id, path, date_taken
				obj.put("id", _id);
				obj.put("path", path);
				obj.put("size", c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns.SIZE)));
				obj.put("dateTaken", dateTaken);
				// album info
				obj.put("album_id", c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_ID)));
				obj.put("album_name", c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME)));
				// gps info
				obj.put("lat", c.getFloat(c.getColumnIndex(MediaStore.Video.VideoColumns.LATITUDE)));
				obj.put("lng", c.getFloat(c.getColumnIndex(MediaStore.Video.VideoColumns.LONGITUDE)));
				// video
				obj.put("title", c.getFloat(c.getColumnIndex(MediaStore.Video.VideoColumns.TITLE)));
				obj.put("artist", c.getFloat(c.getColumnIndex(MediaStore.Video.VideoColumns.ARTIST)));
				obj.put("duration", c.getFloat(c.getColumnIndex(MediaStore.Video.VideoColumns.DURATION)));
				obj.put("resolution", c.getString(c.getColumnIndex(MediaStore.Video.VideoColumns.RESOLUTION)));
				
				result.put(i.toString(), new KrollDict(obj)); //add the item
				
				c.moveToNext();
			}
		}
		
		c.close();
		
		return result;
	}
	
	@Kroll.method
	public KrollDict getVideoThumbnail(String id) {
		Activity activity = this.getActivity();
		HashMap<String, Object> obj = new HashMap<String, Object>();
		
		// create thumbnail
		Bitmap thumb = MediaStore.Video.Thumbnails.getThumbnail(activity.getContentResolver(), Long.parseLong(id), MediaStore.Video.Thumbnails.MICRO_KIND, null);
		if (thumb != null) {
			TiBlob blob = TiBlob.blobFromImage(thumb);
		
			obj.put("image", blob);
			obj.put("width", 96);
			obj.put("height", 96);
		
			return new KrollDict(obj);
		}
		else {
			return null;
		}
	}
}

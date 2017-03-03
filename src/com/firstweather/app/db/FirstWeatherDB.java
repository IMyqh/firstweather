package com.firstweather.app.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.firstweather.app.model.City;
import com.firstweather.app.model.County;
import com.firstweather.app.model.Province;

public class FirstWeatherDB {

	/**
	 * 数据库名
	 */
	public static final String DB_NAME="first_weather";
	
	/**
	 * 数据库版本
	 */
	public static final int VERSION=1;
	
	private static FirstWeatherDB firstWeatherDB;
	
	private SQLiteDatabase db;
	
	/**
	 * 将构造方法私有化
	 */
	private FirstWeatherDB(Context context){
		FirstWeatherOpenHelper dbHelper=new FirstWeatherOpenHelper(context, DB_NAME, null, VERSION);
		db=dbHelper.getWritableDatabase();
	}
	
	/**
	 * 获取FirstWeather的实例
	 */
	public synchronized static FirstWeatherDB getInstance(Context context){
		if(firstWeatherDB==null){
			firstWeatherDB=new FirstWeatherDB(context);
		}
		return firstWeatherDB;
	}
	
	/**
	 * 将Province实例存储到数据库
	 */
	public void saveProvince(Province province){
		if(province!=null){
			ContentValues values=new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
	}
	/**
	 * 从数据库读取全国所有的省份信息
	 */
	public List<Province> loadProvince(){
		List<Province> list=new ArrayList<Province>();
		Cursor cursor=db.query("Province", null, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				Province province=new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				list.add(province);
			}while(cursor.moveToNext());
		}
		if(cursor!=null){
			cursor.close();
		}
		return list;
	}
	
	/**
	 * 将City实例存储到数据库
	 */
	public void saveCity(City city){
		if(city!=null){
			ContentValues values=new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}
	}
	
	/**
	 * 从数据库读取某省下所有的城市信息
	 */
	public List<County> loadCounties(int cityId){
		List<County> list=new ArrayList<County>();
		Cursor cursor=db.query("County", null, "city_id=?", new String[]{String.valueOf(cityId)}, null, null,null);
		if(cursor.moveToFirst()){
			do{
				County county=new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setConntyName(cursor.getString(cursor.getColumnIndex("columnName")));
				county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
				county.setCityId(cityId);
				list.add(county);
			}while(cursor.moveToNext());
		}
		if(cursor!=null){
			cursor.close();
		}
		return list;
	}
	
	
	
	
	
	
	
	
	
	
	
}

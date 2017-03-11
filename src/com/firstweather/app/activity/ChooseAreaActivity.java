package com.firstweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firstweather.app.R;
import com.firstweather.app.db.FirstWeatherDB;
import com.firstweather.app.model.City;
import com.firstweather.app.model.County;
import com.firstweather.app.model.Province;
import com.firstweather.app.util.HttpCallbackListener;
import com.firstweather.app.util.HttpUtil;
import com.firstweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private FirstWeatherDB firstWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	/**
	 * 省列表
	 */
	private List<Province> provinceList;
	/**
	 * 市列表
	 */
	private List<City> cityList;
	/**
	 * 县列表
	 */
	private List<County> countyList;
	/**
	 * 选中的省份
	 */
	private Province selectedProvince;
	/**
	 * 选中的城市
	 */
	private City selecteCity;
	/**
	 * 当前选中的级别
	 */
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView) findViewById(R.id.list_view);
		titleText=(TextView) findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this, android.R.layout.
				simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		firstWeatherDB=FirstWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int index, long id) {

				if(currentLevel==LEVEL_PROVINCE){
					selectedProvince=provinceList.get(index);
					queryCitice();
				}else if(currentLevel==LEVEL_CITY){
					selecteCity=cityList.get(index);
					queryCounties();
				}
			}
		});
		queryProvinces();

	}

	/**
	 * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
	 */
	private void queryProvinces() {
		provinceList=firstWeatherDB.loadProvince();
		if (provinceList.size()>0) {
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		} else {

			queryFromServer(null,"province");
		}
		
	}
	/**
	 * 查询全国所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
	 */
	private void queryCitice() {
		cityList=firstWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size()>0) {
			dataList.clear();
			for(City city:cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
		
	}
	/**
	 * 查询全国所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
	 */
	private void queryCounties() {
		countyList=firstWeatherDB.loadCounties(selecteCity.getId());
		if (countyList.size()>0) {
			dataList.clear();
			for(County county:countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selecteCity.getCityName());
			currentLevel=LEVEL_COUNTY;
		} else {
			queryFromServer(selecteCity.getCityCode(),"county");
		}
		
	}

	/**
	 *根据传入的代号和类型从服务器上查询省市县数据 
	 */
	private void queryFromServer(final String code,final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result=false;
				if ("province".equals(type)) {
					result=Utility.handleProvincesResponse(firstWeatherDB, response);
				} else if("city".equals(type)) {
                    result=Utility.handleCitiesResponse(firstWeatherDB, response, 
                    		selectedProvince.getId());
				}else if("county".equals(type)){
					result=Utility.handleCountiesResponse(firstWeatherDB, response, 
							selecteCity.getId());
				}
				if(result){
					//通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if ("city".equals(type)) {
								queryCitice();
							} else if("county".equals(type)){
                                queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}

				});
			}
		});
	}

	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	private void closeProgressDialog() {
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	/**
	 * 捕获Back键，根据当前级别判断此时应该返回市列表，省列表还是直接退出。
	 */
	@Override
	public void onBackPressed() {
		if(currentLevel==LEVEL_COUNTY){
			queryCitice();
		}else if(currentLevel==LEVEL_CITY){
			queryProvinces();
		}else{
			finish();
		}
		super.onBackPressed();
	}

}

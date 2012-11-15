/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package org.ametro.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.app.GlobalSettings;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.tasks.BaseTask;
import org.ametro.catalog.storage.tasks.DownloadMapTask;
import org.ametro.catalog.storage.tasks.ImportMapTask;
import org.ametro.catalog.storage.tasks.UpdateMapTask;
import org.ametro.directory.CatalogMapSuggestion;
import org.ametro.model.TransportType;
import org.ametro.util.BitmapUtil;
import org.ametro.util.StringUtil;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TaskFailedList extends ListActivity {

	private CatalogStorage mStorage;
	ArrayList<BaseTask> mTasks;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ApplicationEx app = ((ApplicationEx)getApplicationContext());
		mStorage = app.getCatalogStorage();
		mTasks = mStorage.takeFailedTaskList();
		if(mTasks==null || mTasks.size()==0){
			finish();
			return;
		}
		final String languageCode = GlobalSettings.getLanguage(this);
		final Catalog mImport = mStorage.getCatalog(CatalogStorage.IMPORT);
		final Catalog mOnline = mStorage.getCatalog(CatalogStorage.ONLINE);
		ArrayList<FailedTaskListAdapter.DataHolder> data = new ArrayList<FailedTaskListAdapter.DataHolder>();
		for(BaseTask task : mTasks){
			FailedTaskListAdapter.DataHolder dh = new FailedTaskListAdapter.DataHolder();
			if(task instanceof UpdateMapTask){
				String systemName = (String)task.getTaskId();
				CatalogMap map = null;
				String status = null;
				if(mOnline!=null && task instanceof DownloadMapTask){
					map = mOnline.getMap(systemName);
					status = getString(R.string.msg_download_failed);
				}else if(mImport!=null && task instanceof ImportMapTask){
					map = mImport.getMap(systemName);
					status = getString(R.string.msg_import_failed);
				}
				if(map!=null){
					dh.City = map.getCity(languageCode);
					dh.Country = map.getCountry(languageCode);
					dh.ISO = map.getCountryISO();
					dh.Status = status;
					dh.Transports = map.getTransports();
				}else{
					CatalogMapSuggestion suggestion = CatalogMapSuggestion.create(this, new File(systemName), systemName, getString(R.string.msg_unknown_country), "", TransportType.UNKNOWN_ID);
					dh.City = suggestion.getCity(languageCode);
					dh.Country = suggestion.getCountry(languageCode);
					dh.ISO = suggestion.getCountryISO();
					dh.Status = status;
					dh.Transports = suggestion.getTransports();
				}
			}else{
				dh.City = task.toString();
				dh.Country = task.getFailReason().getMessage();
			}
			data.add(dh);
		}
		setListAdapter(new FailedTaskListAdapter(this,data));
	}


	protected void onListItemClick(android.widget.ListView l, View v, int position, long id) {
		String reason = StringUtil.toString( mTasks.get(position).getFailReason() );
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.msg_error_description_title)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setCancelable(true)
		.setMessage(reason)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	};

	private static class FailedTaskListAdapter extends BaseAdapter{

		private ArrayList<DataHolder> mData;
		private Context mContext;
		private LayoutInflater mInflater;

		private HashMap<String,Drawable> mIcons;
		protected HashMap<Integer,Drawable> mTransportTypes;
		private boolean mShowCountryFlags;
		private Drawable mNoCountryIcon;
		private float mDisplayScale;

		public static class DataHolder {
			public String Status;
			public String City;
			public String Country;
			public String ISO;
			public long Transports;
		}

		public static class ViewHolder {
			TextView mCity;
			TextView mCountry;
			TextView mCountryISO;
			TextView mStatus;
			ImageView mIsoIcon;
			LinearLayout mImageContainer;
			LinearLayout mCountryFlagContainer;
		}  

		public FailedTaskListAdapter(Context context, ArrayList<DataHolder> data) {
			mContext = context;
			mDisplayScale = mContext.getResources().getDisplayMetrics().density;
			mInflater = LayoutInflater.from(context);
			mShowCountryFlags = GlobalSettings.isCountryIconsEnabled(context);
			mNoCountryIcon = context.getResources().getDrawable(R.drawable.no_country);
			mIcons = new HashMap<String, Drawable>();
			mTransportTypes = TransportType.getIconsMap(context);
			mData = data;
		}

		public int getCount() {
			return mData.size();
		}

		public Object getItem(int position) {
			return mData.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup g) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.catalog_list_item, null);
				holder = new ViewHolder();
				holder.mCity = (TextView) convertView.findViewById(android.R.id.text1);
				holder.mCountry = (TextView) convertView.findViewById(R.id.country);
				holder.mStatus = (TextView) convertView.findViewById(R.id.state);
				holder.mCountryISO = (TextView) convertView.findViewById(R.id.country_iso);
				holder.mIsoIcon = (ImageView) convertView.findViewById(R.id.iso_icon);
				holder.mImageContainer = (LinearLayout) convertView.findViewById(R.id.icons);
				holder.mCountryFlagContainer = (LinearLayout) convertView.findViewById(R.id.country_flag_panel);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final DataHolder ref = mData.get(position);
			holder.mCity.setText(ref.City);
			holder.mCountry.setText(ref.Country);
			holder.mStatus.setText(ref.Status);

			if(mShowCountryFlags){
				final String iso = ref.ISO;
				if(iso!=null && iso!=""){
					Drawable d = mIcons.get(iso);
					if(d == null){
						File file = new File(Constants.ICONS_PATH, iso + ".png");
						if(file.exists()){
							Bitmap bmp = BitmapUtil.createScaledBitmap(file.getAbsolutePath(), mDisplayScale, false);
							d = new BitmapDrawable(bmp);
						}else{
							d = mNoCountryIcon;
						}
						mIcons.put(iso, d);
					}
					holder.mIsoIcon.setImageDrawable(d);
					holder.mCountryISO.setText( iso );
					holder.mCountryFlagContainer.setVisibility(View.VISIBLE);
				}else{
					holder.mCountryISO.setText( "" );
					holder.mCountryFlagContainer.setVisibility(View.INVISIBLE);
				}
			}else{
				holder.mCountryFlagContainer.setVisibility(View.GONE);
			}


			final LinearLayout ll = holder.mImageContainer;
			ll.removeAllViews();
			long transports = ref.Transports;
			int transportId = 1;
			while(transports>0){
				if((transports % 2)>0){
					ImageView img = new ImageView(mContext);
					img.setImageDrawable(mTransportTypes.get(transportId));
					ll.addView( img );
				}
				transports = transports >> 1;
				transportId = transportId << 1;
			}

			return convertView;
		}

	}
}

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

import static org.ametro.catalog.CatalogMapState.CORRUPTED;
import static org.ametro.catalog.CatalogMapState.DOWNLOAD;
import static org.ametro.catalog.CatalogMapState.DOWNLOADING;
import static org.ametro.catalog.CatalogMapState.DOWNLOAD_PENDING;
import static org.ametro.catalog.CatalogMapState.IMPORT;
import static org.ametro.catalog.CatalogMapState.IMPORTING;
import static org.ametro.catalog.CatalogMapState.IMPORT_PENDING;
import static org.ametro.catalog.CatalogMapState.INSTALLED;
import static org.ametro.catalog.CatalogMapState.NEED_TO_UPDATE;
import static org.ametro.catalog.CatalogMapState.NOT_SUPPORTED;
import static org.ametro.catalog.CatalogMapState.OFFLINE;
import static org.ametro.catalog.CatalogMapState.UPDATE;
import static org.ametro.catalog.CatalogMapState.UPDATE_NOT_SUPPORTED;
import static org.ametro.catalog.CatalogMapState.IMPORT_NEED_TO_UPDATE;
import static org.ametro.catalog.CatalogMapState.IMPORT_UPDATE;
import static org.ametro.catalog.CatalogMapState.CALCULATING;

import java.util.LinkedList;

import org.ametro.R;
import org.ametro.app.ApplicationEx;
import org.ametro.app.Constants;
import org.ametro.app.GlobalSettings;
import org.ametro.catalog.Catalog;
import org.ametro.catalog.CatalogMap;
import org.ametro.catalog.CatalogMapPair;
import org.ametro.catalog.ICatalogStateProvider;
import org.ametro.catalog.storage.CatalogEvent;
import org.ametro.catalog.storage.CatalogStorage;
import org.ametro.catalog.storage.CatalogStorageStateProvider;
import org.ametro.catalog.storage.ICatalogStorageListener;
import org.ametro.directory.CityDirectory;
import org.ametro.ui.adapters.CatalogAdapter;
import org.ametro.ui.dialog.AboutDialog;
import org.ametro.ui.dialog.LocationSearchDialog;
import org.zh.odn.trace.ObjectRelation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public abstract class BaseCatalogActivity extends Activity implements ICatalogStorageListener, ICatalogStateProvider, OnClickListener, OnFocusChangeListener, OnItemClickListener {

	protected static final int MODE_WAIT_NO_PROGRESS = 1;
	protected static final int MODE_WAIT = 2;
	protected static final int MODE_LIST = 3;
	protected static final int MODE_EMPTY = 4;
	
	protected int mMode;
	
	protected CatalogStorage mStorage;
	protected CatalogStorageStateProvider mStorageState;
	
	protected CatalogAdapter mAdapter;
	protected ListView mList;

	protected TextView mCounterTextView;
	protected TextView mMessageTextView;
	protected ProgressBar mProgressBar;
	
	protected View mActionBar;
	protected EditText mActionBarEditText;
	protected ImageButton mActionBarCancelButton;
	
	protected int mProgress;
	protected int mTotal;
	protected String mMessage;
	
	protected String mErrorMessage;

	protected Handler mUIEventDispacher = new Handler();

	protected final int MAIN_MENU_END = 800;
	
	private final int MAIN_MENU_SORT = 995;
	private final int MAIN_MENU_SEARCH = 996;
	private final int MAIN_MENU_REFRESH = 997;
	private final int MAIN_MENU_LOCATION = 998;
	private final int MAIN_MENU_SETTINGS = 999;
	private final int MAIN_MENU_ABOUT = 1000;

	private final int CONTEXT_MENU_SHOW_MAP = 1;
	private final int CONTEXT_MENU_SHOW_DETAILS = 2;
	private final int CONTEXT_MENU_DOWNLOAD = 3;
	private final int CONTEXT_MENU_IMPORT = 4;
	private final int CONTEXT_MENU_UPDATE = 5;
	private final int CONTEXT_MENU_DELETE = 6;
	private final int CONTEXT_MENU_DELETE_PMZ = 7;
	
	private final static int REQUEST_SDCARD = 996;
	private final static int REQUEST_DETAILS = 997;
	private final static int REQUEST_LOCATION = 998;
	private final static int REQUEST_SETTINGS = 999;
	
	protected Catalog mLocal;
	protected Catalog mRemote;
	
	protected int mLocalId;
	protected int mRemoteId;
	
	protected int mDiffMode;
	protected int mDiffColors;
	
	protected boolean mIsActionBarAnimated = false;
	
	/*package*/ LinkedList<CatalogEvent> mCatalogLoadedEvents = new LinkedList<CatalogEvent>();
	/*package*/ InputMethodManager mInputMethodManager;
	
	protected abstract int getEmptyListHeader();
	protected abstract int getEmptyListMessage();
	protected abstract boolean isCatalogProgressEnabled(int catalogId);
	protected abstract int getLocalCatalogId(); 
	protected abstract int getRemoteCatalogId(); 
	protected abstract int getDiffMode(); 
	protected abstract int getDiffColors();
	
	public boolean onSearchRequested() {
		if(mMode == MODE_LIST && mActionBar!=null && mActionBar.getVisibility() == View.GONE){
			setActionBarVisibility(true);
			return true;
		}
		return false;
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;		
		CatalogMapPair pair = (CatalogMapPair)mAdapter.getData(info.position);
		CatalogMap local = pair.getLocal();
		CatalogMap remote = pair.getRemote();
		int state = getCatalogState(local, remote);

		int pos = 0;
		
		menu.setHeaderTitle(R.string.context_menu_catalog_header);
		if(state == INSTALLED || state == OFFLINE || state == UPDATE || (state == CALCULATING && local.isAvailable() )){
			menu.add(0, CONTEXT_MENU_SHOW_MAP, pos++, R.string.context_menu_show_map);
		}
		menu.add(0, CONTEXT_MENU_SHOW_DETAILS, pos++, R.string.context_menu_show_map_details);
		if(state == DOWNLOAD){
			menu.add(0, CONTEXT_MENU_DOWNLOAD, pos++, R.string.context_menu_download);
		}
		if(state == IMPORT || state == IMPORT_UPDATE || state == IMPORT_NEED_TO_UPDATE){
			menu.add(0, CONTEXT_MENU_IMPORT, pos++, R.string.context_menu_import);
		}
		if(state == UPDATE){
			menu.add(0, CONTEXT_MENU_UPDATE, pos++, R.string.context_menu_update);
		}
		if(mLocalId == CatalogStorage.LOCAL && local!=null){
			menu.add(0, CONTEXT_MENU_DELETE, pos++, R.string.context_menu_delete);
		}
		if(mRemoteId == CatalogStorage.IMPORT && remote!=null){
			menu.add(0, CONTEXT_MENU_DELETE_PMZ, pos++, R.string.context_menu_delete_pmz);
		}
		super.onCreateContextMenu(menu, v, menuInfo);
		ObjectRelation.addRelation(this, menu, v, menuInfo);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();		
		CatalogMapPair pair = (CatalogMapPair)mAdapter.getData(info.position);
		final CatalogMap local = pair.getLocal();
		final CatalogMap remote = pair.getRemote();
		final int state = getCatalogState(local, remote);
		ObjectRelation.addRelation(this, item);
		switch (item.getItemId()) {
		case CONTEXT_MENU_SHOW_MAP:
			if(state == INSTALLED || state == OFFLINE || state == UPDATE){
				invokeFinish(local);
			}
			return true;
		case CONTEXT_MENU_SHOW_DETAILS:
			invokeMapDetails(local, remote,state);
			return true;
		case CONTEXT_MENU_IMPORT:
			mStorage.requestImport(remote.getSystemName());
			return true;
		case CONTEXT_MENU_DOWNLOAD:
			mStorage.requestDownload(remote.getSystemName());
			return true;
		case CONTEXT_MENU_UPDATE:
			mStorage.requestDownload(remote.getSystemName());
			return true;
		case CONTEXT_MENU_DELETE:
			showDeleteLocalMapDialog(local);				
			return true;
		case CONTEXT_MENU_DELETE_PMZ:
			showDeleteImportMapDialog(remote);				
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MAIN_MENU_SEARCH, Menu.NONE, R.string.menu_search).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, MAIN_MENU_REFRESH, Menu.NONE, R.string.menu_refresh_list).setIcon(android.R.drawable.ic_menu_rotate);
		menu.add(0, MAIN_MENU_LOCATION, Menu.NONE, R.string.menu_location).setIcon(android.R.drawable.ic_menu_mylocation);
		menu.add(0, MAIN_MENU_SORT, Menu.NONE, R.string.menu_sort).setIcon(android.R.drawable.ic_menu_sort_alphabetically);
		menu.add(0, MAIN_MENU_SETTINGS, 999, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MAIN_MENU_ABOUT, 1000, R.string.menu_about).setIcon(android.R.drawable.ic_menu_help);
		ObjectRelation.addRelation(this, menu);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MAIN_MENU_SEARCH).setEnabled(mMode == MODE_LIST);
		menu.findItem(MAIN_MENU_SORT).setEnabled(mMode == MODE_LIST);
		menu.findItem(MAIN_MENU_LOCATION).setVisible(mMode == MODE_LIST && GlobalSettings.isLocateUserEnabled(this));
		menu.findItem(MAIN_MENU_REFRESH).setEnabled( (mMode != MODE_WAIT) && (mMode != MODE_WAIT_NO_PROGRESS)  && !mStorage.hasTasks() );
		return super.onPrepareOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MAIN_MENU_SEARCH:
			onSearchRequested();
			return true;
		case MAIN_MENU_REFRESH:
			onCatalogRefresh();
			return true;
		case MAIN_MENU_SORT:
			onSortModeChange();
			return true;
		case MAIN_MENU_LOCATION:
			startActivityForResult(new Intent(this, LocationSearchDialog.class), REQUEST_LOCATION);
			return true;
		case MAIN_MENU_SETTINGS:
			startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
			return true;
		case MAIN_MENU_ABOUT:
			AboutDialog.show(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ObjectRelation.addRelation(this, data);
		switch (requestCode) {
		case REQUEST_SDCARD:
			if(resultCode != RESULT_OK){
				invokeFinish(null);
			}
			break;
		case REQUEST_SETTINGS:
			onSettingsChanged();
			break;
		case REQUEST_LOCATION:
			if(resultCode == RESULT_OK){
				Location location = data.getParcelableExtra(LocationSearchDialog.LOCATION);
				if(location!=null){
					onLocationSearch(location);
				}else{
					onLocationSearchUnknown();
				}
			}
			if(resultCode == RESULT_CANCELED){
				onLocationSearchCanceled();	
			}
			break;
		case REQUEST_DETAILS:
			if(resultCode == RESULT_OK){
				int operation = data.getIntExtra(MapDetailsActivity.EXTRA_RESULT, -1);
				if(mLocal!=null && operation == MapDetailsActivity.EXTRA_RESULT_OPEN){
					String systemName = data.getStringExtra(MapDetailsActivity.EXTRA_SYSTEM_NAME);
					if(systemName!=null){
						CatalogMap map = mLocal.getMap(systemName);
						if(map!=null){
							invokeFinish(map);
						}
					}
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		ObjectRelation.addRelation(this, event);
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mActionBar!=null && mActionBar.getVisibility() == View.VISIBLE){
				setActionBarVisibility(false);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInputMethodManager =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		mLocalId = getLocalCatalogId();
		mRemoteId = getRemoteCatalogId();
		mDiffMode = getDiffMode();
		mDiffColors = getDiffColors();
		mStorage = ((ApplicationEx)getApplicationContext()).getCatalogStorage();
		mStorageState = new CatalogStorageStateProvider(mStorage);
		setWaitView();
		ObjectRelation.addRelation(this, savedInstanceState);
	}

	protected void onResume() {
		startWatchingExternalStorage();
		mStorage.addCatalogStorageListener(this);
		mLocal = mStorage.getCatalog(mLocalId);
		mRemote = mStorage.getCatalog(mRemoteId);
		if (mLocal == null) { 
			mStorage.requestCatalog(mLocalId, false);
		}
		onCatalogsUpdated(false);
		super.onResume();
	}

	protected void onPause() {
		stopWatchingExternalStorage();
		mStorage.removeCatalogStorageListener(this);
		super.onPause();
	}
		
	protected void setEmptyView() {
		if(mMode==MODE_LIST){
			mInputMethodManager.hideSoftInputFromWindow(mActionBarEditText.getWindowToken(), 0);
		}
		if(mMode!=MODE_EMPTY){
			setContentView(R.layout.catalog_empty);
			((TextView)findViewById(R.id.header)).setText(getEmptyListHeader());
			((TextView)findViewById(R.id.message)).setText(getEmptyListMessage());
			mMode = MODE_EMPTY;
		}
	} 

	protected void setListView() {
		setContentView(R.layout.catalog_list);
		mList = (ListView)findViewById(R.id.list);
		mAdapter = getListAdapter(); 
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);
		registerForContextMenu(mList);

		mActionBar = (View)findViewById(R.id.actionbar);
		mActionBarEditText = (EditText)findViewById(R.id.actionbar_text);
		mActionBarCancelButton = (ImageButton)findViewById(R.id.actionbar_hide);
		mActionBarCancelButton.setOnClickListener(this);
		
		mActionBarEditText.addTextChangedListener(mActionTextWatcher);
		mActionBarEditText.setOnFocusChangeListener(this);
		
		mMode = MODE_LIST;
	}
	
	protected void setWaitView() {
		if(mMode==MODE_LIST){
			mInputMethodManager.hideSoftInputFromWindow(mActionBarEditText.getWindowToken(), 0);
		}
		if(mMode!=MODE_WAIT){
			setContentView(R.layout.operatoins_wait);
			mMessageTextView = (TextView)findViewById(R.id.message);
			mCounterTextView = (TextView)findViewById(R.id.counter);
			mProgressBar = (ProgressBar)findViewById(R.id.progress);
			mProgressBar.setIndeterminate(true);
			mMode = MODE_WAIT;
		}
	}

	public void onFocusChange(View v, boolean hasFocus) {
		if(v == mActionBarEditText){
			if(hasFocus){
				mInputMethodManager.showSoftInput(v, 0);
				Log.i(Constants.LOG_TAG_MAIN, "show IME");
			}else{
				mInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
				Log.i(Constants.LOG_TAG_MAIN, "hide IME");
			}
		}
		ObjectRelation.addRelation(this, v);
	}
	
	private void onSortModeChange() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.menu_sort);
		
		builder.setSingleChoiceItems(R.array.sort_modes, mAdapter.getSortMode()== CatalogAdapter.SORT_MODE_CITY ? 0 : 1  , new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				mAdapter.updateSort( item==0 ? CatalogAdapter.SORT_MODE_CITY : CatalogAdapter.SORT_MODE_COUNTRY );
				dialog.dismiss();
			}
		});			
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	private void onCatalogsUpdated(boolean refresh) {
		Catalog mPrimary = getPrimary(); 
		if (mPrimary!=null) {
			if (mPrimary.getMaps().size() > 0) {
				if (mMode != MODE_LIST) {
					setListView();
				}else{
					if (refresh) {
						setListView();
					}else{
						mAdapter.updateData(mStorage.getCatalog(mLocalId), mStorage.getCatalog(mRemoteId));
					}
				}
			} else {
				setEmptyView();
			}
		}
	}

	private int getPrimaryId() {
		return (mDiffMode == CatalogMapPair.DIFF_MODE_LOCAL) ? mLocalId : mRemoteId;
	}
	
	private int getSecondaryId() {
		return (mDiffMode == CatalogMapPair.DIFF_MODE_LOCAL) ? mRemoteId : mLocalId;
	}
	
	private Catalog getPrimary() {
		return (mDiffMode == CatalogMapPair.DIFF_MODE_LOCAL) ? mLocal : mRemote;
	}
	
	private Catalog getSecondary() {
		return (mDiffMode == CatalogMapPair.DIFF_MODE_LOCAL) ? mRemote : mLocal;
	}
	
	public void onCatalogFailed(int catalogId, String message)
	{
		if(GlobalSettings.isDebugMessagesEnabled(this)){
			mErrorMessage = message;
			mUIEventDispacher.post(mCatalogError);
		}
		ObjectRelation.addRelation(this, message);
	}

	public void onCatalogLoaded(int catalogId, Catalog catalog) {
		synchronized (mCatalogLoadedEvents) {
			CatalogEvent event = new CatalogEvent();
			event.CatalogId = catalogId;
			event.Catalog = catalog;
			mCatalogLoadedEvents.offer(event);
		}
		if(catalogId == getPrimaryId() && getSecondary()==null){
			mStorage.requestCatalog(getSecondaryId(), false);
		}
		mUIEventDispacher.post(mHandleCatalogLoadedEvents);
		ObjectRelation.addRelation(this, catalog);
	}
	
	public void onCatalogMapChanged(String systemName) {
		if(mMode == MODE_LIST){
			mUIEventDispacher.post(mUpdateList);
		}
		ObjectRelation.addRelation(this, systemName);
	}
	
	public void onCatalogMapDownloadFailed(String systemName, Throwable ex){
		if(mMode == MODE_LIST){
			mUIEventDispacher.post(mUpdateList);
		}
		ObjectRelation.addRelation(this, systemName, ex);
	}
	
	public void onCatalogMapImportFailed(String systemName, Throwable ex){
		if(mMode == MODE_LIST){
			mUIEventDispacher.post(mUpdateList);
		}
		ObjectRelation.addRelation(this, systemName, ex);
	}
	
	public void onCatalogMapDownloadDone(String systemName) {
		if(mMode == MODE_LIST){
			mUIEventDispacher.post(mUpdateList);
		}
		ObjectRelation.addRelation(this, systemName);
	}

	public void onCatalogMapImportDone(String systemName) {
		if(mMode == MODE_LIST){
			mUIEventDispacher.post(mUpdateList);
		}
	}
	
	public void onCatalogProgress(int catalogId, int progress, int total, String message)
	{
		if(isCatalogProgressEnabled(catalogId)){
			mProgress = progress;
			mTotal = total;
			mMessage = message;
			mUIEventDispacher.post(mUpdateProgress);
		}
		ObjectRelation.addRelation(this, message);
	}
	
	public void onCatalogMapDownloadProgress(String systemName, int progress, int total) {
	}
	
	public void onCatalogMapImportProgress(String systemName, int progress, int total) {
	}

	
	protected CatalogAdapter getListAdapter() {
		return new CatalogAdapter(
				this, 
				mStorage.getCatalog(mLocalId), 
				mStorage.getCatalog(mRemoteId),
				mDiffMode,
				mDiffColors,
				this,
				CatalogAdapter.SORT_MODE_COUNTRY);
	} 
	
	protected void onCatalogRefresh() {
		switch(mDiffMode){
		case CatalogMapPair.DIFF_MODE_LOCAL:
			mStorage.requestCatalog(mLocalId, true);
			break;
		case CatalogMapPair.DIFF_MODE_REMOTE:
			mStorage.requestCatalog(mRemoteId, true);
			break;
		}
		setWaitView();
	};
	
	protected CharSequence formatProgress(int mProgress, int mTotal) {
		return mProgress + "/" + mTotal;
	}
	
	protected void onSettingsChanged() {
		if(mMode == MODE_LIST){
			String oldLanguage = mAdapter.getLanguage();
			String newLanguage = GlobalSettings.getLanguage(this);
			if(!oldLanguage.equalsIgnoreCase(newLanguage)){
				mAdapter = getListAdapter(); 
				mList.setAdapter(mAdapter);
			}
		}
	}
	
	protected void onLocationSearch(Location location) {
		CityDirectory.Entity cityEntity = ApplicationEx.getInstance().getCityDirectory().getByLocation(location);
		if(cityEntity!=null){
			final int position = mAdapter.findItemPosition(cityEntity);
			if(position!=-1){
				CatalogMapPair pair = mAdapter.getData(position);
				String code = GlobalSettings.getLanguage(this);
				Toast.makeText(this, pair.getCity(code) + ", " + pair.getCountry(code) , Toast.LENGTH_LONG).show();
				mUIEventDispacher.post(new Runnable() {
					public void run() {
						mList.setSelection(position);
					}
				});
			}
		}
		ObjectRelation.addRelation(this, location);
	}

	protected void onLocationSearchCanceled() {}
	
	protected void onLocationSearchUnknown() {
		Toast.makeText(this,R.string.msg_location_unknown, Toast.LENGTH_SHORT).show();			
	}		
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		CatalogMapPair diff = (CatalogMapPair)mAdapter.getData(position);
		int state =  getCatalogState(diff.getLocal(), diff.getRemote());
		onCatalogMapClick(diff.getLocal(), diff.getRemote(), state);		
	}

	public boolean onCatalogMapClick(CatalogMap local, CatalogMap remote, int state) {
		ObjectRelation.addRelation(this, local, remote);
		switch(state){
		case OFFLINE:
		case INSTALLED:
		case UPDATE:
		case IMPORT:
		case DOWNLOAD:
		case DOWNLOAD_PENDING:
		case DOWNLOADING:
		case IMPORT_PENDING:
		case IMPORTING:
		case NEED_TO_UPDATE:
		case IMPORT_UPDATE:
		case IMPORT_NEED_TO_UPDATE:
			invokeMapDetails(local,remote,state);
			return true;
		case NOT_SUPPORTED:
		case UPDATE_NOT_SUPPORTED:
		case CORRUPTED:
			// do nothing
			return true;
		}		
		return false;
	}

	
	protected void invokeMapDetails(CatalogMap local, CatalogMap remote, int state) {
		Intent detailsIntent = new Intent(this, MapDetailsActivity.class);
		detailsIntent.putExtra(MapDetailsActivity.EXTRA_SYSTEM_NAME, (local!=null) ? local.getSystemName() : remote.getSystemName() );
		startActivityForResult(detailsIntent, REQUEST_DETAILS);
		ObjectRelation.addRelation(this, local, remote);
	}

	protected void invokeFinish(CatalogMap local) {
		if(local!=null){
			Intent viewIntent = new Intent();
			viewIntent.putExtra(Constants.EXTRA_SYSTEM_MAP_NAME, local.getSystemName());
			viewIntent.putExtra(Constants.EXTRA_TIMESTAMP, local.getTimestamp());
			Activity parent =  CatalogTabHostActivity.getInstance();
			if(parent!=null){
				parent.setResult(RESULT_OK, viewIntent);
				parent.finish();
			}else{
				setResult(RESULT_OK, viewIntent);
				finish();
			}
		}else{
			setResult(RESULT_CANCELED);
			finish();
		}
		ObjectRelation.addRelation(this, local);
	}

	public void onClick(View v) {
		if(v == mActionBarCancelButton){
			setActionBarVisibility(false);
		}
		ObjectRelation.addRelation(this, v);
	}
	
	private void setActionBarVisibility(boolean isVisible) {
		if(mIsActionBarAnimated){ 
			return;
		}
		final float scale = getResources().getDisplayMetrics().density;
		if(isVisible){
			if(mActionBar.getVisibility() == View.GONE){
				mIsActionBarAnimated = true;
				TranslateAnimation anim = new TranslateAnimation(0, 0, -50*scale , 0);
				anim.setDuration(250);
				anim.setAnimationListener(new AnimationListener() {
					public void onAnimationStart(Animation animation) {
					}
					public void onAnimationRepeat(Animation animation) {
					}
					public void onAnimationEnd(Animation animation) {
						mActionBarEditText.requestFocus();
						mIsActionBarAnimated = false;
					}
				});
				mActionBar.startAnimation(anim);
				mActionBar.setVisibility(View.VISIBLE);
			}
		}else{
			if(mActionBar.getVisibility() == View.VISIBLE){
				mIsActionBarAnimated = true;
				TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -50*scale);
				anim.setDuration(250);
				anim.setAnimationListener(new AnimationListener() {
					public void onAnimationStart(Animation animation) {
					}
					public void onAnimationRepeat(Animation animation) {
					}
					public void onAnimationEnd(Animation animation) {
						mActionBarEditText.setText("");
						mActionBar.setVisibility(View.GONE);
						mList.requestFocus();
						mIsActionBarAnimated = false;
					}
				});
				mActionBar.startAnimation(anim);
			}
		}
	}

	private Runnable mHandleCatalogLoadedEvents = new Runnable() {
		public void run() {
			synchronized (mCatalogLoadedEvents) {
				while(mCatalogLoadedEvents.size()>0){
					
					CatalogEvent event = mCatalogLoadedEvents.poll();
					int catalogId = event.CatalogId;
					
					Catalog catalog = event.Catalog;
					if(catalogId == mLocalId){
						mLocal = catalog;
						onCatalogsUpdated(false);
					}else  if(catalogId == mRemoteId){
						mRemote = catalog;
						onCatalogsUpdated(false);
					}
				}
			}
		}
	};
	
	private Runnable mUpdateList = new Runnable() {
		public void run() {
			if(mMode == MODE_LIST && mLocal!=null && mRemote!=null){
				mAdapter.updateData(mStorage.getCatalog(mLocalId), mStorage.getCatalog(mRemoteId));
				mAdapter.notifyDataSetChanged();
			}
		}
	};

	private Runnable mCatalogError = new Runnable() {
		public void run() {
			Toast.makeText(BaseCatalogActivity.this, mErrorMessage, Toast.LENGTH_LONG).show();
		}
	};
	
	private Runnable mUpdateProgress = new Runnable() {
		public void run() {
			if(mMode!=MODE_WAIT){
				setWaitView();
			}
			mMessageTextView.setText( mMessage );
			if(mProgress>0 && mTotal>0){
				mProgressBar.setIndeterminate(false);
				mProgressBar.setMax(mTotal);
				mProgressBar.setProgress(mProgress);
				mCounterTextView.setText( formatProgress(mProgress, mTotal) );
			}else{
				mProgressBar.setIndeterminate(true);
				mCounterTextView.setText(null);
			}
		}

	};

	private TextWatcher mActionTextWatcher = new TextWatcher() {
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mAdapter.getFilter().filter(s);
			ObjectRelation.addRelation(this, s);
		}
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		
		public void afterTextChanged(Editable s) {
		}
	};

	private void showDeleteLocalMapDialog(final CatalogMap map) {
		String code = GlobalSettings.getLanguage(this);
		String msg = String.format(getString(R.string.msg_delete_local_map_confirmation), map.getCity(code),map.getCountry(code));
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg)
		       .setCancelable(false)
		       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
			   			mStorage.deleteLocalMap(map.getSystemName());
		           }
		       })
		       .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   // put your code here 
		        	   dialog.cancel();
		           }
		       });
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
		ObjectRelation.addRelation(this, map);
	}

	private void showDeleteImportMapDialog(final CatalogMap map) {
		String code = GlobalSettings.getLanguage(this);
		String msg = String.format(getString(R.string.msg_delete_import_map_confirmation), map.getCity(code),map.getCountry(code));
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg)
		       .setCancelable(false)
		       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
			   			mStorage.deleteLocalMap(map.getSystemName());
		           }
		       })
		       .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   // put your code here 
		        	   dialog.cancel();
		           }
		       });
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
		ObjectRelation.addRelation(this, map);
	}

	private void updateExternalStorageState() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        mExternalStorageAvailable = mExternalStorageWriteable = true;
	    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        mExternalStorageAvailable = true;
	        mExternalStorageWriteable = false;
	    } else {
	        mExternalStorageAvailable = mExternalStorageWriteable = false;
	    }
	    
	    if(!(mExternalStorageAvailable && mExternalStorageWriteable)){
	    	startActivityForResult(new Intent(this,MediaUnmountedActivity.class), REQUEST_SDCARD);
	    }
	}

	private void startWatchingExternalStorage() {
	    mExternalStorageReceiver = new BroadcastReceiver() {
	        public void onReceive(Context context, Intent intent) {
	            updateExternalStorageState();
	        }
	    };
	    IntentFilter filter = new IntentFilter();
	    filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
	    filter.addAction(Intent.ACTION_MEDIA_REMOVED);
	    registerReceiver(mExternalStorageReceiver, filter);
	    updateExternalStorageState();
	}

	private void stopWatchingExternalStorage() {
	    unregisterReceiver(mExternalStorageReceiver);
	}	
	
	private BroadcastReceiver mExternalStorageReceiver;
	private boolean mExternalStorageAvailable = false;
	private boolean mExternalStorageWriteable = false;
	
	
}

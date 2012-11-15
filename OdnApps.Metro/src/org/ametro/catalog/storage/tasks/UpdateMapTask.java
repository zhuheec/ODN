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
package org.ametro.catalog.storage.tasks;

import org.zh.odn.trace.ObjectRelation;

import android.os.Parcel;

public abstract class UpdateMapTask extends BaseTask {

	protected final String mSystemName;

	public UpdateMapTask(String systemName) {
		this.mSystemName = systemName;
		ObjectRelation.addRelation(this.mSystemName, systemName);
	}
	
	public UpdateMapTask(Parcel in) {
		this.mSystemName = in.readString();
		ObjectRelation.addRelation(this.mSystemName, in);
	}

	public Object getTaskId() {
		return mSystemName;
	}

	public boolean isAsync() {
		return false;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(mSystemName);
		ObjectRelation.addRelation(this, out);
	}

}

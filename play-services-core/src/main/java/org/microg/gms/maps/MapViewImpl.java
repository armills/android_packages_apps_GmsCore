/*
 * Copyright 2013-2015 microG Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.microg.gms.maps;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;

import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.dynamic.ObjectWrapper;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.internal.IGoogleMapDelegate;
import com.google.android.gms.maps.internal.IMapViewDelegate;
import com.google.android.gms.maps.internal.IOnMapReadyCallback;

public class MapViewImpl extends IMapViewDelegate.Stub {
    private static final String TAG = "GmsMapViewImpl";

    private GoogleMapImpl map;
    private GoogleMapOptions options;
    private Context context;
    private IOnMapReadyCallback readyCallback;
    private boolean isReady = false;

    public MapViewImpl(Context context, GoogleMapOptions options) {
        this.context = context;
        this.options = options;
    }

    private GoogleMapImpl myMap() {
        if (map == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            map = new GoogleMapImpl(inflater.getContext(), options);
        }
        return map;
    }

    @Override
    public IGoogleMapDelegate getMap() throws RemoteException {
        return myMap();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) throws RemoteException {
        //myMap().onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onResume() throws RemoteException {
        Log.d(TAG, "onResume");
        synchronized (this) {
            isReady = true;
        }

        myMap().onResume();
        if (readyCallback != null) {
            try {
                readyCallback.onMapReady(map);
                readyCallback = null;
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }
    }

    @Override
    public void onPause() throws RemoteException {
        myMap().onPause();
        synchronized (this) {
            isReady = false;
        }
    }

    @Override
    public void onDestroy() throws RemoteException {
        myMap().onDestroy();
    }

    @Override
    public void onLowMemory() throws RemoteException {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) throws RemoteException {

    }

    @Override
    public IObjectWrapper getView() throws RemoteException {
        return ObjectWrapper.wrap(myMap().getView());
    }

    @Override
    public synchronized void addOnMapReadyCallback(final IOnMapReadyCallback callback) throws RemoteException {
        if (!isReady) {
            this.readyCallback = callback;
        } else {
            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        callback.onMapReady(map);
                    } catch (RemoteException e) {
                        Log.w(TAG, e);
                    }
                }
            });
        }
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (super.onTransact(code, data, reply, flags)) return true;
        Log.d(TAG, "onTransact [unknown]: " + code + ", " + data + ", " + flags);
        return false;
    }
}

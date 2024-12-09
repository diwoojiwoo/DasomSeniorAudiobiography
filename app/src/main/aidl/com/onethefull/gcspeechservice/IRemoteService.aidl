// IRemoteService.aidl
package com.onethefull.gcspeechservice;

// Declare any non-default types here with import statements

import com.onethefull.gcspeechservice.IRemoteServiceCallback;

interface IRemoteService {
	void registerCallback(IRemoteServiceCallback callback);
	void unregisterCallback(IRemoteServiceCallback callback);
	void startRecognizing(int sampleRate);
	void finishRecognizing();
	void recognize(in byte[] data, int size);
}
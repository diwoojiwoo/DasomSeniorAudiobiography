// IRemoteServiceCallback.aidl
package com.onethefull.gcspeechservice;

// Declare any non-default types here with import statements

interface IRemoteServiceCallback {

    void onSpeechRecognized(String text);

}
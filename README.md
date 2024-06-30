# Sample Implementation for SMSOTP Autoread on Android 
### The sample over here demonstrates how to use the google SMS Retriever API by google.
### You get to filter out any particular sender ID that you expect to read from

 - Please Note that with Implementations requiring  you to use the following permissions, you need very concrete reasons to defend them on google play console.
 - SMS Retriever API does not require any permissions, just registering the broadcast reciever at run time.

```
    <uses-permission android:name="android.permission.WRITE_SMS"/>
    <uses-permission android:name="android.permission.READ_SMS"/>
    <uses-permission android:name="android.permission.RECEIVE_SMS"/>

```

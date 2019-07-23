/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kakao.sdk.sample;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.sdk.sample.common.ChatData;
import com.kakao.sdk.sample.common.widget.KakaoToast;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.kakao.sdk.sample.ChatActivity_device.handler;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {


    View header;

    private String chat_user_name = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
    static String chat_room_name;
    static String chat_room_master;
    static String chat_room_master_nickname;
    static String chat_user_nickname;

    private DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("원거리 소통");
    static DatabaseReference ref;

//    private ChildEventListener childEventListener = new ChildEventListener() {
//        @Override
//        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//            chatConversation(dataSnapshot);
//        }
//
//        @Override
//        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//        }
//
//        @Override
//        public void onChildRemoved(DataSnapshot dataSnapshot) {
//        }
//
//        @Override
//        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//        }
//    };

    private DatabaseReference user_reference = FirebaseDatabase.getInstance().getReference("유저목록");
    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.d("TTTT", dataSnapshot.getKey());
            Log.d("TTTT", dataSnapshot.getValue().toString());
            get_kakao_info(dataSnapshot, chat_user_name);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };


    private DatabaseReference reference;
    private String chat_room;
    private String chat_user;
    private String chat_message;
    private String chat_phoneNumber;
    private String chat_function;
    private String chat_databaseReference;

    static boolean device_on = false;


    String[] perMissionList = {Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};

    private static final String TAG = "BluetoothChatFragment";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views


    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    static StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    static BluetoothChatService mChatService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("프로세스", "블루투스챗프레그먼트초기화");

        //reference1.addChildEventListener(childEventListener);
        setHasOptionsMenu(true);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        user_reference.addValueEventListener(valueEventListener);

        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<RoomsItem> set = new HashSet<RoomsItem>();
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()) {
                    DataSnapshot data = ((DataSnapshot) i.next());
                    String temp = data.getKey();
                    if (temp.split("&").length == 3) {
                        RoomsItem friends1 = new RoomsItem(R.drawable.icon, temp.split("&")[1], temp.split("&")[2], temp.split("&")[0]);
                        set.add(friends1);
                    }
                }
                    ChatActivity_device.data.clear();
                    ChatActivity_device.data.addAll(set);
                    ChatActivity_device.adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        /*
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {  Set<RoomsItem> set = new HashSet<RoomsItem>();
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()) {
                    DataSnapshot data = ((DataSnapshot) i.next());
                    String temp = data.getKey();

                    RoomsItem friends1 = new RoomsItem(R.drawable.icon, temp.split("&")[1],temp.split("&")[2], temp.split("&")[0]);
                    set.add(friends1);
                }
                ChatActivity_device.data.clear();
                ChatActivity_device.data.addAll(set);
                ChatActivity_device.adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
*/

        ChatActivity_device.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {

                    chat_room_master = ChatActivity_device.data.get(i).getRoom_master_id();
                    chat_room_master_nickname = ChatActivity_device.data.get(i).getRoom_master_name();
                    chat_room_name = ChatActivity_device.data.get(i).getRoom_name();

                    com.kakao.sdk.sample.common.ChatData chatData = new ChatData();
                    String temp = FirebaseDatabase.getInstance().getReference().push().toString();
                    chatData.setMsg(chat_user_nickname);
                    chatData.setFuntcion("방 입장");
                    chatData.setUser_name(chat_user_name);
                    chatData.setUser_phoneNumber("");
                    chatData.setUser_room(chat_room_name);
                    chatData.setDatabaseReference(temp);

                    ref = reference1.child(chat_room_master + "&" + chat_room_name + "&" + chat_room_master_nickname);
                    ref.child(temp.split("/")[temp.split("/").length - 1]).setValue(chatData);
                    ref.child(temp.split("/")[temp.split("/").length - 1]).removeValue();
                    Intent intent = new Intent(getContext().getApplicationContext(), ChatActivity_mode2.class);
                    startActivity(intent);
                    KakaoToast.makeToast(getActivity().getApplicationContext(), "방에 참가하였습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    KakaoToast.makeToast(getActivity().getApplicationContext(), "기기가 연결된 상태로는 입장하실수 없습니다. ", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
            //Log.d("프로세스", "setupChat");

        } else {
            //Log.d("프로세스", "setupChat");
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        Log.d("프로세스", "블루투스챗프레그먼트종료");
        if (ChatActivity_device.room_exit_ref != null) {
            com.kakao.sdk.sample.ChatData chatData = new com.kakao.sdk.sample.ChatData();
            String temp = reference1.push().toString();
            DatabaseReference ref = reference1.child(temp.split("/")[temp.split("/").length - 1]);
            chatData.setMsg("");
            chatData.setFuntcion("master_exit");
            chatData.setUser_name(chat_user_name);
            chatData.setUser_phoneNumber("");
            chatData.setUser_room(chat_room_name);
            chatData.setDatabaseReference(temp);
            ref.setValue(chatData);
            ref.removeValue();
            ChatActivity_device.room_exit_ref.removeValue();
            ChatActivity_device.room_exit_ref = null;
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }


    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);


        // Initialize the send button with a listener that for click events
        ChatActivity_device.mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (view != null) {
                    Log.d("TAG", "여기 안돌아감");
                    sendMessage("pairing/" + chat_user_name + "/" + chat_user_nickname);
                }
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        if (mChatService == null) {
            //Log.d("프로세스", "mChatService널이다");
            mChatService = new BluetoothChatService(getActivity(), mHandler);
        } else {
            //Log.d("프로세스", "mChatService널아니다");
        }

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            Log.d("AAAAA", msg.toString());
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    Log.d("AAAAA", "MESSAGE_STATE_CHANGE");
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    Log.d("AAAAA", "MESSAGE_WRITE");
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    if (readMessage.equals("연결가능")) {
                        KakaoToast.makeToast(getActivity().getApplicationContext(), "해당디바이스와 연결되었습니다.", Toast.LENGTH_SHORT).show();
                        device_on = true;
                        msg = handler.obtainMessage();
                        handler.sendMessage(msg);
                    }
                    if (readMessage.equals("연결불가능")) {
                        KakaoToast.makeToast(getActivity().getApplicationContext(), "해당 디바이스의 네트워크 연결이 원활하지 않습니다.", Toast.LENGTH_SHORT).show();
                        device_on = false;
                    }
                    if (readMessage.equals("네트워크 중단")) {
                        device_on = false;
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ChatActivity_mode2.mode2Activity);
                        builder1.setTitle("해당 디바이스의 네트워크 연결이 끊겼습니다.");
                        builder1.setMessage("디바이스 연결 다시 시도해주세요").setCancelable(false).setNegativeButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ChatActivity_mode2.mode2Activity.finish();
                            }
                        });
                        AlertDialog alertDialog1 = builder1.create();
                        alertDialog1.show();
                    }


                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    Log.d("AAAAA", "MESSAGE_DEVICE_NAME");
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    Log.d("AAAAA", "MESSAGE_TOAST");
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };


    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }


    void get_kakao_info(DataSnapshot dataSnapshot, String chat_user_name) {

        String deaf_temp = "";
        String nickname_temp = "";
        String userId_temp = "";
        String phoneNumber_temp = "";


        for (String str : dataSnapshot.getValue().toString().split(",")) {

            if (str.split("=")[0].equals(" msg")) {
                deaf_temp = str.split("=")[1];
            }

            if (str.split("=")[0].equals(" user_room")) {
                nickname_temp = str.split("=")[1];
            }

            if (str.split("=")[0].equals(" user_name")) {
                userId_temp = str.split("=")[1];
            }

            if (str.split("=")[0].equals(" user_phoneNumber")) {
                phoneNumber_temp = str.split("=")[1];
            }

            if (str.split("=")[0].equals(" function")) {


                if (userId_temp.equals(chat_user_name)) {
                    chat_user_nickname = nickname_temp;
                }
            }
        }
    }


    private void chatConversation(DataSnapshot dataSnapshot) {

        Set<RoomsItem> set = new HashSet<RoomsItem>();

        Iterator i = dataSnapshot.getChildren().iterator();

        while (i.hasNext()) {
            chat_databaseReference = (String) ((DataSnapshot) i.next()).getValue();
            chat_function = (String) ((DataSnapshot) i.next()).getValue();
            chat_message = (String) ((DataSnapshot) i.next()).getValue();
            chat_user = (String) ((DataSnapshot) i.next()).getValue();
            chat_phoneNumber = (String) ((DataSnapshot) i.next()).getValue();
            chat_room = (String) ((DataSnapshot) i.next()).getValue();
        }

        if (chat_function.equals("create_room") && chat_user.equals(chat_user_name)) {
            RoomsItem friends1 = new RoomsItem(R.drawable.icon, chat_room, chat_message, chat_user);
            set.add(friends1);
        }
        ChatActivity_device.data.clear();
        ChatActivity_device.data.addAll(set);
        ChatActivity_device.adapter.notifyDataSetChanged();
    }
}
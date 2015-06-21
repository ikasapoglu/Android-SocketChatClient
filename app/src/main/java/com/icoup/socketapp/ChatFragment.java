package com.icoup.socketapp;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;



public class ChatFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private EditText mInputMessageView;
    private RecyclerView mMessagesView;
    private List<com.icoup.socketapp.Message> mMessages = new ArrayList<>();
    private RecyclerView.Adapter mAdapter;

    private OnFragmentInteractionListener mListener;

    private Socket socket;
    {
        try{
            socket = IO.socket("http://192.168.1.6:3000");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }


    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        socket.connect();
        Log.e("Kullanıcı Durumu", "Katıldı");
        socket.on("message",gelenleriOku);
        }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);

        Button sendButton = (Button) view.findViewById(R.id.send_button);
        mInputMessageView = (EditText) view.findViewById(R.id.mesajyaz);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(); //
            }
        });
    }

    private void sendMessage(){
        String message = mInputMessageView.getText().toString().trim();
        mInputMessageView.setText("");

        addMessage(message);
        socket.emit("message", message);

    }

    private Emitter.Listener gelenleriOku = new Emitter.Listener(){
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject)args[0];
                    String message;
                    try {
                        message = data.getString("message").toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    addMessage(message);
                }
            });
        }
    };

    private void addMessage(String msj){

       mMessages.add(new com.icoup.socketapp.Message.Builder(com.icoup.socketapp.Message.TYPE_MESSAGE).message(msj).build());
        mAdapter = new MessageAdapter(mMessages);
        mAdapter.notifyItemInserted(0);
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }





    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        Log.e("Kullanıcı Durumu","Ayrıldı");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       mAdapter = new MessageAdapter( mMessages);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}

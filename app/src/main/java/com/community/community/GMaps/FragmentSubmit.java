package com.community.community.GMaps;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.community.community.MainActivity;
import com.community.community.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FragmentSubmit extends Fragment {

    OnAddPressedListener mCallback;

    private Button mSubmitBtn;
    private Button mCancelBtn;

    private View mView;

    private DatabaseReference mDatabase;

    private FragmentSubmit.CallImageButtonClickListener callImageButtonClickListener = new FragmentSubmit.CallImageButtonClickListener();
    private class CallImageButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

//                case R.id.marker_submit:
//                    Toast.makeText(getActivity(), "Submit", Toast.LENGTH_SHORT).show();
//                    break;
//                case R.id.cancel_marker:
//                    Toast.makeText(getActivity(), "Cancel", Toast.LENGTH_SHORT).show();
//                    break;
//
//                default:
//                    break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase =  FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_gmaps, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mSubmitBtn = (Button) mView.findViewById(R.id.marker_submit);
//        mSubmitBtn.setOnClickListener(callImageButtonClickListener);
//
//        mCancelBtn = (Button) mView.findViewById(R.id.cancel_marker);
//        mCancelBtn.setOnClickListener(callImageButtonClickListener);
    }

    // Container Activity must implement this interface
    public interface OnAddPressedListener {
        void onAddPressed();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnAddPressedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}

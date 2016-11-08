package com.javic.pokewhere.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.javic.pokewhere.R;
import com.javic.pokewhere.interfaces.OnFragmentListener;
import com.javic.pokewhere.util.Constants;


public class FragmentBlank extends Fragment {

    private Context mContext;

    private OnFragmentListener mListener;

    private View mView;

    public FragmentBlank() {
        // Required empty public constructor
    }

    public static FragmentBlank newInstance() {
        FragmentBlank fragment = new FragmentBlank();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if (context instanceof OnFragmentListener) {
            mListener = (OnFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_blank, container, false);

        // Inflate the layout for this fragment
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //mListener.onFragmentCreatedViewStatus(Constants.FRAGMENT_BLANK);
        mListener.showProgress(false);
    }
}

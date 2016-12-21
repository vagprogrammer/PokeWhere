package com.javic.pokewhere.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.javic.pokewhere.R;
import com.javic.pokewhere.util.PlayGifView;

public class FragmentTutorial extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    private int position;

    private TextView tutorial_title, tutorial_subtitle;

    //Fragment UI
    private View mView;
    private PlayGifView pGif;

    public FragmentTutorial() {
        // Required empty public constructor
    }

    public static FragmentTutorial newInstance(int position) {
        FragmentTutorial fragment = new FragmentTutorial();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_tutorial, container, false);

        tutorial_title = (TextView) mView.findViewById(R.id.tutorial_title);
        tutorial_subtitle= (TextView) mView.findViewById(R.id.tutorial_subtitle);

        pGif = (PlayGifView) mView.findViewById(R.id.viewGif);

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switch (position){
            case 0:
                tutorial_title.setText(getString(R.string.title_tutorial_moves));
                tutorial_subtitle.setText(getString(R.string.message_tutorial_moves));
                pGif.setImageResource(R.drawable.moves);
                break;

            case 1:
                tutorial_title.setText(getString(R.string.title_tutorial_pokebank));
                tutorial_subtitle.setText(getString(R.string.message_tutorial_pokebank));
                pGif.setImageResource(R.drawable.pokebank);
                break;
        }
    }

}

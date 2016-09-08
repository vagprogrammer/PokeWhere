package com.javic.pokewhere.interfaces;

/**
 * Created by vagprogrammer on 03/09/16.
 */

/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 */

public interface OnFragmentCreatedViewListener {

    void onFragmentCreatedViewStatus(Boolean status);
    void onFragmentActionPerform(int action);
}

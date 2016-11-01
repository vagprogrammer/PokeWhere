package com.javic.pokewhere;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.ToxicBakery.viewpager.transforms.BackgroundToForegroundTransformer;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.javic.pokewhere.adapters.AdapterPokemonDetail;
import com.javic.pokewhere.fragments.FragmentPokemonBank;
import com.javic.pokewhere.fragments.FragmentPokemonDetail;
import com.javic.pokewhere.models.LocalUserPokemon;
import com.nineoldandroids.animation.Animator;

import java.util.List;

public class ActivityPokemonDetail extends AppCompatActivity implements FragmentPokemonDetail.OnFragmentInteractionListener,Animator.AnimatorListener{

    //UI
    private AdapterPokemonDetail mAdapter;
    private ViewPager mViewPager;
    private ImageView leftArrow, rightArrow;
    //Listas
    private List<LocalUserPokemon> mLocalUserPokemonList;

    //Variables
    private int mIndex;
    private boolean isChanged = false;
    private Intent intentFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_detail);

        intentFrom = getIntent();

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_activity_pokemon_detail));

        Bundle args = getIntent().getExtras();

        if (args!=null){
            mIndex = args.getInt("index");
        }
        else{
            mIndex = 0;
        }

        leftArrow = (ImageView) findViewById(R.id.leftArrow);
        rightArrow = (ImageView) findViewById(R.id.rightArrow);

        mLocalUserPokemonList = FragmentPokemonBank.mLocalUserPokemonList;
        mViewPager = (ViewPager) findViewById(R.id.vp_slider);
        mViewPager.setClipToPadding(false);
        mAdapter = new AdapterPokemonDetail(this.getSupportFragmentManager(),mLocalUserPokemonList);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageTransformer(true, new BackgroundToForegroundTransformer());
        mViewPager.setCurrentItem(mIndex);


        if (mIndex>0){
            YoYo.with(Techniques.Flash)
                    .withListener(this)
                    .duration(1000)
                    .playOn(leftArrow);
        }
        else{
            leftArrow.setVisibility(View.GONE);
        }

        YoYo.with(Techniques.Flash)
                .withListener(this)
                .duration(1000)
                .playOn(rightArrow);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isChanged){
                    if (intentFrom!=null){
                        intentFrom.putExtra("resultado",true);
                        setResult(RESULT_OK, intentFrom);
                        finish();
                    }
                }
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (isChanged){

            if (intentFrom!=null){
                intentFrom.putExtra("resultado",true);
                setResult(RESULT_OK, intentFrom);
                finish();
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onChangedDetected(boolean isChanged) {
        this.isChanged = isChanged;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {}

    @Override
    public void onAnimationStart(Animator animation) {}

    @Override
    public void onAnimationEnd(Animator animation) {
        leftArrow.setVisibility(View.GONE);
        rightArrow.setVisibility(View.GONE);
    }

    @Override
    public void onAnimationCancel(Animator animation) {}

    @Override
    public void onAnimationRepeat(Animator animation) {}
}

package com.fyodorwolf.studybudy;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public final class DisplayNextView implements Animation.AnimationListener{
	
	private boolean mCurrentView;
	ImageView image1;
	ImageView image2;
	Activity sender;
	
	public DisplayNextView(boolean currentView, ImageView image1, ImageView image2, Activity sender) {
		mCurrentView = currentView;
		this.image1 = image1;
		this.image2 = image2;
		this.sender = sender;
	}
	
	@Override
	public void onAnimationStart(Animation animation) {}

	@Override
	public void onAnimationEnd(Animation animation) {
		image1.post(new SwapViews(mCurrentView, image1, image2));
	}

	@Override
	public void onAnimationRepeat(Animation animation){}

	
/********************************************************************************************************************************************
 * 							Private Classes		 																							*
 ********************************************************************************************************************************************/

	private class SwapViews implements Runnable{
		private boolean mIsFirstView;
		ImageView image1;
		ImageView image2;

		public SwapViews(boolean isFirstView, ImageView image1, ImageView image2) {
			mIsFirstView = isFirstView;
			this.image1 = image1;
			this.image2 = image2;
		}

		public void run() {
			final float centerX = image1.getWidth() / 2.0f;
			final float centerY = image1.getHeight() / 2.0f;
			Flip3dAnimation rotation;
			if (mIsFirstView) {
				image1.setVisibility(View.GONE);
				image2.setVisibility(View.VISIBLE);
				image2.bringToFront();
				image2.requestFocus();
				rotation = new Flip3dAnimation(-90, 0, centerX, centerY);
			} else {
				image2.setVisibility(View.GONE);
				image1.setVisibility(View.VISIBLE);
				image1.bringToFront();
				image1.requestFocus();
				rotation = new Flip3dAnimation(90, 0, centerX, centerY);
			}
			
			rotation.setDuration(500);
			rotation.setFillAfter(true);
			rotation.setInterpolator(new DecelerateInterpolator());
			rotation.setAnimationListener(new AnimationListener(){

				@Override
				public void onAnimationEnd(Animation animation) {
					((DeckActivity)sender).rotationComplete();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
			});
			if (mIsFirstView) {
				image2.startAnimation(rotation);
			} else {
				image1.startAnimation(rotation);
			}
		}
	}

}
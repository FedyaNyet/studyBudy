package com.fyodorwolf.studybudy;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.view.animation.DecelerateInterpolator;

public final class ViewSwapper implements Runnable{
	
	//used with Log.d(TAG,"some string");
	private static final String TAG = "ViewSwapper"; 

	public static final int ROTATE_LEFT = 1;
	public static final int ROTATE_RIGHT = 2;
	
	private View fromView;
	private View toView;
	private int _direction = ROTATE_LEFT;
	private long _duration = 800/2; //we have two animations;
	private ViewSwapperListener _myListener = new ViewSwapperListener(){
		@Override public void onViewSwapperStart() {}
		@Override public void onViewSwapperHalfComplete() {}
		@Override public void onViewSwapperComplete() {}
	};
	
	public ViewSwapper(View fromView, View toView) {
		this.fromView = fromView;
		this.toView = toView;
	}
	public void setDirection(int direction){
		this._direction = direction;
	}
	public void setDuration(long duration){
		//we have two animations...
		this._duration = duration/2;
	}

	public void addViewSwapperListener(ViewSwapperListener viewSwapperListener){
		this._myListener = viewSwapperListener;
	}
	
	@Override
	public void run() {
		this._myListener.onViewSwapperStart();
		fromView.setVisibility(View.VISIBLE);
		toView.setVisibility(View.GONE);
		fromView.bringToFront();
		fromView.requestFocus();
		
		final float centerX = fromView.getWidth() / 2.0f;
		final float centerY = fromView.getHeight() / 2.0f;
		
		final Flip3dAnimation rotation;
		if(this._direction == ViewSwapper.ROTATE_LEFT){
			rotation = new Flip3dAnimation(0, 90, centerX, centerY);
		}else{
			rotation = new Flip3dAnimation(0, -90, centerX, centerY);
		}
		rotation.setDuration(_duration);
		rotation.setInterpolator(new AccelerateInterpolator());
		rotation.setAnimationListener(new AnimationListener(){
			@Override public void onAnimationEnd(Animation animation) {
				_myListener.onViewSwapperHalfComplete();
				fromView.setVisibility(View.GONE);
				toView.setVisibility(View.VISIBLE);
				toView.bringToFront();
				toView.requestFocus();
				
				Flip3dAnimation rotation_continue;
				if(_direction == ViewSwapper.ROTATE_LEFT){
					rotation_continue = new Flip3dAnimation(-90, 0, centerX, centerY);
				}else{
					rotation_continue = new Flip3dAnimation(90, 0, centerX, centerY);
				}
				rotation_continue.setDuration(_duration);
				rotation_continue.setInterpolator(new DecelerateInterpolator());
				rotation_continue.setAnimationListener(new AnimationListener(){
					@Override public void onAnimationEnd(Animation animation) {
						_myListener.onViewSwapperComplete();
						//Make quick animation to restore the underlining view...
					}
					@Override public void onAnimationRepeat(Animation animation) {}
					@Override public void onAnimationStart(Animation animation) {}
				});
				toView.startAnimation(rotation_continue);
			}
			@Override public void onAnimationRepeat(Animation animation) {}
			@Override public void onAnimationStart(Animation animation) {}
		});
		fromView.startAnimation(rotation);
	}
	
	public interface ViewSwapperListener{
		public void onViewSwapperStart();
		public void onViewSwapperHalfComplete();
		public void onViewSwapperComplete();
	}
	
	private class Flip3dAnimation extends Animation{
		
		private final float _fromDegrees;
		private final float _toDegrees;
		private final float _centerX;
		private final float _centerY;
		private Camera _camera;
		
		public Flip3dAnimation(float fromDegrees, float toDegrees, float centerX, float centerY) {
			_fromDegrees = fromDegrees;
			_toDegrees = toDegrees;
			_centerX = centerX;
			_centerY = centerY;
		}
		
		@Override public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			_camera = new Camera();
		}
		
		@Override protected void applyTransformation(float interpolatedTime, Transformation t) {
			
			float degrees = _fromDegrees + ((_toDegrees - _fromDegrees) * interpolatedTime);
			final Matrix matrix = t.getMatrix();
			
			_camera.save();
			_camera.rotateY(degrees);
			_camera.getMatrix(matrix);
			_camera.restore();
			
			matrix.preTranslate(-_centerX, -_centerY);
			matrix.postTranslate(_centerX, _centerY);
		}
	}
}
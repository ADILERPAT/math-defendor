package nl.uva.mobilesystems.mathdefender;

import nl.uva.mobilesystems.mathdefender.gui.GUIConstants;
import nl.uva.mobilesystems.mathdefender.gui.SwipeListener;
import nl.uva.mobilesystems.mathdefender.gui.TexMan;
import nl.uva.mobilesystems.mathdefender.physics.PhConstants;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.controller.MultiTouch;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.app.Activity;
import android.graphics.Point;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Toast;

public class InitialActivity extends SimpleBaseGameActivity implements OnKeyListener{
	// ============================================
	//	DEBUG
	// =====================================
	
	boolean zenMode = false; //TObi: set it to false so you could star the game in your mode
	
	// ===========================================================
		// Constants
		// ===========================================================

	private static final float DEMO_VELOCITY = 100.0f;
    private static final int CAMERA_WIDTH = 470;
    private static final int CAMERA_HEIGHT = 270;

		// ===========================================================
		// Fields
		// ===========================================================
	private Camera mCamera;
	
	private GameModel gModel;
	
	

	public Text text; //how many waves are left;
		
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
		
	 
	public EngineOptions onCreateEngineOptions() {
		
		String mode;
		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			mode = extras.getString("mode");
		}
		else{
			mode = "NOMODE";
		}
		
		if(mode.equals("supermarket")){
			zenMode = false;
		}
		
		//set Camera here
		this.mCamera = new Camera(0, 0, GUIConstants.CAMERA_WIDTH, GUIConstants.CAMERA_HEIGHT);
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(GUIConstants.CAMERA_WIDTH, GUIConstants.CAMERA_HEIGHT), this.mCamera); 
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);

		//set Multi Touch in here
		if(MultiTouch.isSupported(this)) {
			if(MultiTouch.isSupportedDistinct(this)) {
				Toast.makeText(this, "MultiTouch detected --> Both controls will work properly!", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "MultiTouch detected, but your device has problems distinguishing between fingers.\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
		}
		return engineOptions;
	}

	 
	protected void onCreateResources() {
//		if(zenZode)
			TexMan.initializeTextures(this); //pass zen
		
	}

	 
	protected Scene onCreateScene() {
		
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		//Set SCENE [must be done before Setting our MODEL obviously]
		final Scene scene = new Scene();
//		scene.setBackground(new Background(0.05f, 0.8f, 0.8f));
		scene.setBackground(new SpriteBackground(0.05f, 0.8f, 0.8f, TexMan.getIt().mBackgroundSprite)); //different background
		
		
		//set our MathLevel here (will be calculated in separated thread)
		gModel = this.zenMode ?  new GameZenModel(this, scene) : new GameSuperMarketModel(this, scene); //that's a trick, in java you can use this expression [ variable = boolean ? valueIfTrue : valueIfFalse ]
			// (nrWaves, nrTowers, [Screen_X, Screen_Y], EnemyTexture, TowerTexture, Library-shit-buffer)
		gModel.setUpSimpleGame( new Point(GUIConstants.CAMERA_WIDTH, GUIConstants.CAMERA_HEIGHT),getVertexBufferObjectManager());
		
		
			
		//Create analog-control here
		final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(0, GUIConstants.CAMERA_HEIGHT - TexMan.getIt().mOnScreenControlBaseTextureRegion.getHeight(), this.mCamera, TexMan.getIt().mOnScreenControlBaseTextureRegion, TexMan.getIt().mOnScreenControlKnobTextureRegion, 0.1f, 200, this.getVertexBufferObjectManager(), new IAnalogOnScreenControlListener() {
			 
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				gModel.getPlayer().getPhysicsHanlder().setVelocity(pValueX * PhConstants.PLAYER_VELOCITY, pValueY * PhConstants.PLAYER_VELOCITY);
			}

			 
			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
				//what happens if you click on analog screen
				;
//				face.registerEntityModifier(new SequenceEntityModifier(new ScaleModifier(0.25f, 1, 1.5f), new ScaleModifier(0.25f, 1.5f, 1)));
			}
		});
		analogOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		analogOnScreenControl.getControlBase().setAlpha(0.5f);
		analogOnScreenControl.getControlBase().setScaleCenter(0, 128);
		analogOnScreenControl.getControlBase().setScale(1.25f);
		analogOnScreenControl.getControlKnob().setScale(1.25f);
		analogOnScreenControl.refreshControlKnobPosition();

		scene.setChildScene(analogOnScreenControl);
		
		scene.registerUpdateHandler(new IUpdateHandler(){

			 
			//it's for checking the collisions
			public void onUpdate(float pSecondsElapsed) {
				gModel.performGlobalCollisionTest();
//				for(AnimatedSprite enemy : gModel.getCurrentWaveObjects()){
//					if(player.collidesWith(enemy)){
//						gModel.removeObjectFromScene(enemy);
//					}
//				}
			}
			 
			public void reset() {
				; //nothing happens here so far?
			}
		});
		
		
		//set Swipe Detection
		SwipeListener swipeList = new SwipeListener(getApplicationContext());
			swipeList.addObjectPositionEventListener(gModel);
		scene.setOnSceneTouchListener(swipeList);

		return scene;
	}


	@Override
	public synchronized void onGameDestroyed() {
		super.onGameDestroyed();
		Log.d("onDest", "destroyed?");
		finish();
	}
	
	

	public boolean onKey(View v, int keyCode, KeyEvent event) {
		Log.d("keyEvent111", Integer.toString(keyCode));
		
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			switch(keyCode){
				case KeyEvent.KEYCODE_BACK: //kill the activity!
					Log.d("keyEVENT", "finish it!");
					finish();
				break;
			}
		}
			
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	/*
	 * PRIVATE METHODS GO DOWN ----------------------------------------------------
	 */
	
	
	
	
	
}

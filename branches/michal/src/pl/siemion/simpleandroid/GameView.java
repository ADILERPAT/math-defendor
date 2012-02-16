package pl.siemion.simpleandroid;

import pl.siemion.simpleandroid.gui.Colors;
import pl.siemion.simpleandroid.gui.Keypad;
import pl.siemion.simpleandroid.gui.Shapes;
import pl.siemion.simpleandroid.physics.PhConstants;
import pl.siemion.simpleandroid.physics.Player;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

class GameView extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener {
 
	/**------------------ Game physics variables BELOW*/
	
	private Player player;
	
	/** ------------------- Game Graphics variables BELOW */
	
	private Keypad keypad;
	
	private Point screenDimensions;
	
	/**
	 * Internal class that serves as an animation thread. For clarity it may be put into separate java file.
	 * @author siemionides
	 *
	 */
	class AnimationThread extends Thread {
    	

    	/** --------------- Game Animation/View variables BELOW */
    	/** Are we running ? */
    	private boolean mRun;
    	
        /** Used to figure out elapsed time between frames */
        private long mLastTime;      
        
        /** Variables for the counter */
        private int frameSamplesCollected = 0;
        private int frameSampleTime = 0;
        private int fps = 0;
        
        /** Handle to the surface manager object we interact with */
        private SurfaceHolder mSurfaceHolder;
                
        public AnimationThread(SurfaceHolder surfaceHolder) {
        	mSurfaceHolder = surfaceHolder;
        }

        
        /**
         * The actual game loop!
         */
        @Override
        public void run() {
        	boolean checkedScreenDimention = false;
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {   
                        if(!checkedScreenDimention){ //TODO this way of getting Screen dimensions is just stupid, but works. Change it later on!
                        	screenDimensions = new Point(getWidth(), getHeight());
                        	checkedScreenDimention = true;
                        }
                    	updatePhysics();
                        doDraw(c);
                    }
                }finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
        
        /**
         * Figures the gamestate based on the passage of
         * realtime. Called at the start of draw().
         * Only calculates the FPS for now.
         */
        private void updatePhysics() {
            long now = System.currentTimeMillis();
                        
            if (mLastTime != 0) {
            	
            	//Time difference between now and last time we were here
        		int time = (int) (now - mLastTime);
        		frameSampleTime += time;
        		frameSamplesCollected++;
        		
        		//After 10 frames
        		if (frameSamplesCollected == 10) {
        			
        			//Update the fps variable
	        		fps = (int) (10000 / frameSampleTime);
	        		
	        		//Reset the sampletime + frames collected
	        		frameSampleTime = 0;
	        		frameSamplesCollected = 0;
        		}        		
        	}
            
            mLastTime = now;
            
            /** ------------------- My physics */
            
            player.movePlayer();
            
            
        }
        
        /**
         * Draws to the provided Canvas.
         */
        private void doDraw(Canvas canvas) {
        	
//        	
        	
            // Draw the background color. Operations on the Canvas accumulate
            // so this is like clearing the screen. In a real game you can 
        	// put in a background image of course
        	canvas.drawColor(Colors.COLOR_BACKGROUD);
        	
        	//Draw fps center screen
        	
        	canvas.drawText(fps + " fps", getWidth() / 2, getHeight() / 2, Colors.PAINT_FPS);
        	
        	//Draw Key-PAD
        	canvas.drawRect(keypad.left.x, keypad.left.y, //left
				keypad.left.x + keypad.keySize, keypad.left.y + keypad.keySize,
				Colors.PAINT_KEYPAD);
        	
        	canvas.drawRect(keypad.right.x, keypad.right.y, //right
    				keypad.right.x + keypad.keySize, keypad.right.y + keypad.keySize,
    				Colors.PAINT_KEYPAD);
        	
        	canvas.drawRect(keypad.up.x, keypad.up.y, //up
    				keypad.up.x + keypad.keySize, keypad.up.y + keypad.keySize,
    				Colors.PAINT_KEYPAD);

        	canvas.drawRect(keypad.down.x, keypad.down.y, //down
    				keypad.down.x + keypad.keySize, keypad.down.y + keypad.keySize,
    				Colors.PAINT_KEYPAD);
        	
        	
        	//Draw Player
        	canvas.drawCircle(player.getX(), player.getY(), 30, Colors.PAINT_PLAYER);
        	canvas.drawText("100", player.getX()-5, player.getY()-5, Colors.PAINT_PLAYER_TEXT);
        	
	        
        	canvas.restore();            
        }
        
        /**
         * So we can stop/pauze the game loop
         */
        public void setRunning(boolean b) {
            mRun = b;
        }      
       
    }

    /** The thread that actually draws the animation */
    private AnimationThread thread;

    /**
     * Constructor.
     * @param context don't care for it so much
     * @param attrs dont care too
     */
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setOnTouchListener(this);
        
        //Player initializtion
        player = new Player (100, 100);
        player.setSpeedX(0.0f);
        player.setSpeedY(0.0f);
        
        //Keypad initialization
        
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        
        keypad = new Keypad();
        	keypad.setLeft(0 + Shapes.KEYPAD_HORIZONTAL_MARGIN, ((float)d.getHeight())/3)
        			.setRight(0 + Shapes.KEYPAD_HORIZONTAL_MARGIN, ((float)d.getHeight())/3*2)
        			.setDown(((float)d.getWidth())/3, 0 + Shapes.KEYPAD_VERTICAL_MARGIN)
        			.setUp(((float)d.getWidth())/3*2, Shapes.KEYPAD_VERTICAL_MARGIN)
        			.setSize(Shapes.KEYPAD_SIZE);

        	
        
        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new AnimationThread(holder);

    }
    
    
    /**
     * Obligatory method that belong to the:implements SurfaceHolder.Callback
     */
    
    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		
		
		
		switch(action){
		
		case MotionEvent.ACTION_DOWN:
			Log.v("action_down", x + " " + y + "" +  keypad.getWhichKeyWasTouched(x, y));
			switch(keypad.getWhichKeyWasTouched(x, y)){
			case Keypad.KEY_UP:
				player.setSpeedX(PhConstants.speedIncrease);
				break;
				
			case Keypad.KEY_DOWN:
				player.setSpeedX(-PhConstants.speedIncrease);
				break;
				
			case Keypad.KEY_LEFT:
				player.setSpeedY(-PhConstants.speedIncrease);
				break;
				
			case Keypad.KEY_RIGHT:
				player.setSpeedY(+PhConstants.speedIncrease);
				break;
				
			case Keypad.KEY_NONE:
				break;
			}
			
			
			break;
		case MotionEvent.ACTION_UP:
			switch(keypad.getWhichKeyWasTouched(x, y)){
			case Keypad.KEY_UP:
				player.setSpeedX(0);
				break;
				
			case Keypad.KEY_DOWN:
				player.setSpeedX(0);
				break;
				
			case Keypad.KEY_LEFT:
				player.setSpeedY(0);
				break;
				
			case Keypad.KEY_RIGHT:
				player.setSpeedY(0);
				break;
				
			case Keypad.KEY_NONE:
				break;
			}

			break;
		
		}
	return true;
		
	}
}
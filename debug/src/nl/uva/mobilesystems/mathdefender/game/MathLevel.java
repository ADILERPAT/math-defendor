package nl.uva.mobilesystems.mathdefender.game;

import java.util.Iterator;
import java.util.LinkedList;

import nl.uva.mobilesystems.mathdefender.physics.PhConstants;
import android.graphics.Point;

/**
 * It's rather container of player, enemies, waves etc. Need to think about it 
 * more later!
 * @author siemionides
 *
 */
public class MathLevel {
	
	/** Should be used in FIFO manner, so methods offer() and poll()*/
	public Player player; //TODO it's public as it was convenient, but it should be done private and setters/getters should be written as MathLevel methods
	
	private Wave currentWave;
	
	LinkedList<Wave> waves;
	
	/**
	 * This method checks for collisions (o-really!!)
	 */
	public void checkForCollisions(){
		
		if(this.currentWave != null){
			Iterator<Fragile> iter = this.currentWave.getObjects().iterator();
			Fragile enemy;
			while(iter.hasNext()){
				enemy = iter.next();
				if(calculateDistance(enemy, player) < (enemy.getSize() + player.getSize())){
					enemy.collisionDetected();
					player.collisionDetected();
					iter.remove();
				}
			}
		}
	
	}
	
	public Wave getCurrentWave(){
		return this.currentWave;
	}
	
	public MathLevel(Player player){
		this.player = player;
	}
	
	public void movePlayer(){
		this.player.moveIt();
	}
	
	public void moveWave(){
		//move all of the objects!
		if(this.currentWave != null){
			for(Fragile object : this.currentWave.objects)
				object.moveIt();
		
		//check wheter it shouldn't be next wave?
				
			if(this.currentWave.objects.peek().getLocation().y <= 0){
				this.currentWave = waves.poll();
			}
		}
		
	}
	
	/** Ultra important and bad-coding style method; Sets waves, enemies in there */
	public void generateWaves(int nrWaves, Point screenDimenstions){
		this.waves = new LinkedList<Wave>();
		for(int i=0; i<nrWaves; i++){
			LinkedList<Fragile>  tempEnemies = new LinkedList<Fragile>();
			for(int j=0; j< PhConstants.nrEnemiesInWave; j++){ //generating enemies
				int random = (int)(Math.random() * 1000);	//should be an integer number from 0 - 1000 
				int y = screenDimenstions.y; //the edge of a screen
				int x = screenDimenstions.x / (PhConstants.nrEnemiesInWave+1) * (j+1);	//so equal distribution on screen Width
				Enemy tempEnemy = new Enemy(x,y,PhConstants.ENEMY_SIZE, random);
				tempEnemies.add(tempEnemy);
			}
			Wave tempWave = new Wave(tempEnemies);
			waves.offer(tempWave);
		}
		currentWave = waves.poll();
	}
	
	private float calculateDistance(Fragile object1, Fragile object2){
		
		double distance = Math.sqrt( (object1.getLocation().x - object2.getLocation().x) * (object1.getLocation().x - object2.getLocation().x) 
								+ (object1.getLocation().y - object2.getLocation().y)* (object1.getLocation().y - object2.getLocation().y));
		return (float)distance;
	}
}

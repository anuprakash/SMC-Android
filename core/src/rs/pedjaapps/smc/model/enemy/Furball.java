package rs.pedjaapps.smc.model.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import rs.pedjaapps.smc.Assets;
import rs.pedjaapps.smc.model.GameObject;
import rs.pedjaapps.smc.model.Sprite;
import rs.pedjaapps.smc.model.World;
import rs.pedjaapps.smc.utility.CollisionManager;
import rs.pedjaapps.smc.utility.Constants;
import rs.pedjaapps.smc.utility.Utility;

/**
 * Created by pedja on 18.5.14..
 */
public class Furball extends Enemy
{

    public static final float VELOCITY = 1.5f;
    public static final float VELOCITY_TURN = 0.75f;
    public static final float POS_Z = 0.09f;

    private boolean turn;
    private float turnStartTime;

    private boolean turned = false;

    public Furball(World world, Vector2 size, Vector3 position)
    {
        super(world, size, position);
    }

    @Override
    public void loadTextures()
    {
        TextureAtlas atlas = Assets.manager.get(textureAtlas);
        Array<TextureRegion> rightFrames = /*atlas.getRegions();//*/new Array<TextureRegion>();
        Array<TextureRegion> leftFrames = /*atlas.getRegions();//*/new Array<TextureRegion>();

        for(int i = 1; i < 9; i++)
        {
            TextureRegion region = atlas.findRegion("walk-" + i);
            rightFrames.add(region);
            TextureRegion regionL = new TextureRegion(region);
            regionL.flip(true, false);
            leftFrames.add(regionL);
        }


        Assets.animations.put(textureAtlas, new Animation(0.07f, rightFrames));
        Assets.animations.put(textureAtlas + "_l", new Animation(0.07f, leftFrames));
        Assets.loadedRegions.put(textureAtlas + ":turn", atlas.findRegion("turn"));
    }

    @Override
    public void render(SpriteBatch spriteBatch)
    {
        TextureRegion frame = turn ? Assets.loadedRegions.get(textureAtlas + ":turn")
                : Assets.animations.get(direction == Direction.right ? textureAtlas : textureAtlas + "_l").getKeyFrame(stateTime, true);

        //spriteBatch.draw(frame, body.getPosition().x - getBounds().width/2, body.getPosition().y - getBounds().height/2, bounds.width, bounds.height);
        Utility.draw(spriteBatch, frame, bounds.x, bounds.y, bounds.height);
    }

    public void update(float deltaTime)
    {
		// Setting initial vertical acceleration 
        acceleration.y = Constants.GRAVITY;

        // Convert acceleration to frame time
        acceleration.scl(deltaTime);

        // apply acceleration to change velocity
        velocity.add(acceleration);

		// checking collisions with the surrounding blocks depending on Bob's velocity
        checkCollisionWithBlocks(deltaTime);

        // apply damping to halt nicely 
        //velocity.x *= DAMP;

        // ensure terminal velocity is not exceeded
        /*if (velocity.x > maxVelocity()) {
            velocity.x = maxVelocity();
        }
        if (velocity.x < -maxVelocity()) {
            velocity.x = -maxVelocity();
        }*/
		
        stateTime += deltaTime;
        if(stateTime - turnStartTime > 0.15f)
        {
            turnStartTime = 0;
            turn = false;
        }

		switch(direction)
		{
			case right:
				setVelocity(velocity.x =- (turn ? VELOCITY_TURN : VELOCITY), velocity.y);
				break;
			case left:
				setVelocity(velocity.x =+ (turn ? VELOCITY_TURN : VELOCITY), velocity.y);
				break;
		}
		turned = false;
    }
	
	@Override
	protected void handleCollision(GameObject object, boolean vertical)
	{
        super.handleCollision(object, vertical);
		if(!vertical)
		{
			if(((object instanceof Sprite && ((Sprite)object).getType() == Sprite.Type.massive
					&& object.getBody().y + object.getBody().height > body.y + 0.1f)
					|| object instanceof EnemyStopper)
                    && !turned)
			{
				//CollisionManager.resolve_objects(this, object, true);
                handleCollision(Enemy.ContactType.stopper);
			}
		}
	}

	@Override
	public void handleCollision(Enemy.ContactType contactType)
	{
		switch(contactType)
		{
			case stopper:
				direction = direction == Direction.right ? Direction.left : Direction.right;
                turnStartTime = stateTime;
                turn = true;
				velocity.x = velocity.x > 0 ? -velocity.x : Math.abs(velocity.x);
                turned = true;
				break;
		}
	}
	
}
package rs.papltd.smc.model.enemy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import rs.papltd.smc.Assets;
import rs.papltd.smc.utility.Utility;

/**
 * Created by pedja on 18.5.14..
 */
public class Eato extends Enemy
{
    public Eato(World world, Vector2 position, float width, float height)
    {
        super(world, position, width, height);
    }

    @Override
    public void loadTextures()
    {
        TextureAtlas atlas = Assets.manager.get(textureAtlas);
        Array<TextureAtlas.AtlasRegion> frames = atlas.getRegions();
        frames.add(atlas.findRegion(TKey.two.toString()));

        Assets.animations.put(textureAtlas, new Animation(0.18f, frames));
    }

    @Override
    public void render(SpriteBatch spriteBatch, float deltaTime)
    {
        updateStateTime(deltaTime);
        TextureRegion frame = Assets.animations.get(textureAtlas).getKeyFrame(stateTime, true);

        //spriteBatch.draw(frame, body.getPosition().x - getBounds().width/2, body.getPosition().y - getBounds().height/2, bounds.width, bounds.height);
        Utility.draw(spriteBatch, frame, body.getPosition().x - getBounds().width / 2, body.getPosition().y - getBounds().height / 2, bounds.height);
    }
}
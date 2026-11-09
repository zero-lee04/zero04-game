package io.jbnu.hw1;
import com.badlogic.gdx.Game;
public class MainScreen extends Game {
    @Override
    public void create() {
        setScreen(new GamingScreen(this));
    }
}

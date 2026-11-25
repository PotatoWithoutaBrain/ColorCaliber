package com.ColorCaliber.frontend.lwjgl3;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.ColorCaliber.frontend.Main;
import com.ColorCaliber.frontend.WalkingAnimationTest;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {

    /**
     * Set to true to launch the dedicated walking animation testing harness,
     * or false to launch the main ColorCaliber game.
     */
    private static final boolean TEST_ANIMATION_MODE = false;

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        ApplicationListener listener = TEST_ANIMATION_MODE ? new WalkingAnimationTest() : new Main();
        return new Lwjgl3Application(listener, getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle(TEST_ANIMATION_MODE ? "ColorCaliber - Walking Animation Tester" : "ColorCaliber - 2D Top-Down Bullet Hell");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(1280, 720);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

        return configuration;
    }
}

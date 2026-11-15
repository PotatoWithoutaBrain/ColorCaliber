package com.ColorCaliber.frontend.lwjgl3;

import com.ColorCaliber.frontend.WalkingAnimationTest;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Dedicated desktop launcher for the Walking Animation Tester.
 * Can be run directly from IntelliJ IDEA (Right click -> Run 'WalkingAnimationLauncher.main()').
 */
public class WalkingAnimationLauncher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return;

        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("ColorCaliber - Walking Animation Tester");
        configuration.useVsync(true);
        configuration.setForegroundFPS(60);
        configuration.setWindowedMode(1200, 800);
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

        new Lwjgl3Application(new WalkingAnimationTest(), configuration);
    }
}

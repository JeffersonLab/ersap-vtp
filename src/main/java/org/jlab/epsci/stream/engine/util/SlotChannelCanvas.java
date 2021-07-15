package org.jlab.epsci.stream.engine.util;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;

public class SlotChannelCanvas extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle( "Timeline Example" );

        Group root = new Group();
        Scene theScene = new Scene( root );
        primaryStage.setScene( theScene );

        Canvas canvas = new Canvas( 512, 512 );
        root.getChildren().add( canvas );

        GraphicsContext gc = canvas.getGraphicsContext2D();

        long lastUpdateTime = 0;

        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                if((currentNanoTime - lastUpdateTime) >= 1000000000.0) {

                    // clear the canvas
                    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                    // get byte array and draw slot/channel axes a square which side = fADC integral charge.
                    gc.fillRect(crate,slot,charge,charge);
                }
            }
        }.start();
        primaryStage.show();
    }
}

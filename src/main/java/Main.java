import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        BufferedImage image1 = ImageUtil.loadImage("images/image1.png");
        BufferedImage image12 = ImageUtil.loadImage("images/image2.png");
        Image result = SwingFXUtils.toFXImage(ImageUtil.compareTwoImages(image1, image12, 10), null);
        primaryStage.setTitle("Images comparator");
        StackPane root = new StackPane();
        root.getChildren().add(new ImageView(result));
        primaryStage.setScene(new Scene(root, 640, 480));
        primaryStage.show();
    }


    public static void main(String[] args) throws Exception {
        Main.launch(args);
    }
}

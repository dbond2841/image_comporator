import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class ImageUtil {

    public static BufferedImage loadImage(String path) throws Exception {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream stream = classLoader.getResourceAsStream(path);
            return ImageIO.read(stream);
        } catch (Exception e) {
            throw new ImageUtilException("Can't load the specified file.");
        }
    }

    private static boolean compareRgbByPercent(int first, int second, int percent) {
        if (percent <= 0)
            throw new IllegalArgumentException("Wrong input.");

        return Math.abs(((first - second)/ (float)second) * 100) > percent;
    }

    public static BufferedImage copyImage(BufferedImage source) {
        BufferedImage bufferedImage = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics graphics = bufferedImage.getGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return bufferedImage;
    }

    private static void getRectangleHeight(BufferedImage image1, BufferedImage image2, int x, int y, Rectangle rectangle, int percent) {
        int rgbFirst = image1.getRGB(x, y);
        int rgbSecond = image2.getRGB(x, y);

        if (rgbFirst != rgbSecond && compareRgbByPercent(rgbFirst, rgbSecond, percent)){
            rectangle.setLocation(x, y);
            rectangle.setSize((int) rectangle.getWidth(), (int) rectangle.getHeight() + 1);
            getRectangleHeight(image1, image2, x , y - 1, rectangle, percent);
        }
    }

    private static void getRectangleWidth(BufferedImage image1, BufferedImage image2, int x, int y,  Rectangle rectangle, int percent) {
        int rgbFirst = image1.getRGB(x, y);
        int rgbSecond = image2.getRGB(x, y);
        if (compareRgbByPercent(rgbFirst, rgbSecond, percent) && !rectangle.contains(x, y)){
            int newWidth = x - (int) rectangle.getX();
            rectangle.setSize(newWidth, (int) rectangle.getHeight());
        }
        if (x < image1.getWidth()-1) {
            getRectangleWidth(image1, image2, x + 1, y, rectangle, percent);
        }
    }

    private static void addOrChangeRectangle(Rectangle input, Collection<Rectangle> rectangles){
        if (rectangles.stream().filter(r -> r.intersects(input)).findAny().orElse(null) == null) {
            rectangles.add(input);
        }
        else {
            for (Rectangle r : rectangles){
                if (r.intersects(input)){
                    r.add(input);
                }
            }
        }
    }

    private static void mergeRectangles(Collection<Rectangle> rectangles) {
        List<Rectangle> rectangleList = new ArrayList<>(rectangles);
        for (Rectangle outer : rectangleList){
            for (Rectangle inner : rectangles){
                if (outer.intersects(inner) && !outer.equals(inner))
                    inner.add(outer);
            }
        }
    }

    private static void drawDifference(BufferedImage image, Collection<Rectangle> rects){
        rects.stream().forEach(rectangle -> {
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.RED);
            graphics.drawRect((int)rectangle.getX(), (int )rectangle.getY(), (int) rectangle.getWidth(), rectangle.height);
            graphics.dispose();
        });
    }



    public static BufferedImage compareTwoImages(BufferedImage image1, BufferedImage image2, int percent) throws Exception{

        if (image1 == null || image2 == null)
            throw new IllegalArgumentException("Image can't be null.");

        if( image1.getHeight() != image2.getHeight() || image1.getWidth() != image2.getWidth())
            throw new ImageUtilException("Images have different size.");


        List<Rectangle> rectangles = new ArrayList<>();
        for(int x = 0; x < image1.getWidth(); x++){
            for(int y = 0; y < image1.getHeight(); y++){
                int tempX = x;
                int tmpY = y;

                if (compareRgbByPercent(image1.getRGB(x, y), image2.getRGB(x, y), percent)
                        && rectangles.stream().filter(r -> r.contains(tempX, tmpY)).findAny().orElse(null) == null){

                    Rectangle rect = new Rectangle(0, 0, 0, 0);
                    getRectangleHeight(image1, image2,  x , y,  rect,  percent);

                    if (rect.getX() != 0 && rect.getY() != 0)
                        getRectangleWidth(image1, image2, (int) rect.getX(), (int) rect.getY(), rect,  percent);

                    addOrChangeRectangle(rect, rectangles);
                }
            }
        }
        mergeRectangles(rectangles);
        BufferedImage result = copyImage(image2);
        drawDifference(result, rectangles);
        return result;
    }


}

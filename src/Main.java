import controlP5.*;
import controlP5.Button;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Main window class.
 *
 * <p>A GUI for image processing in scientific settings.
 *
 * @author Eli Jelesko
 */
public class Main extends PApplet{

    private ControlP5 cp5;
    private GripPipeline imagePipeline;
    private Mat initialImage;
    private PImage displayImage, originalDisplayImage, colorDropperImg;
    private final int RANGE_HEIGHT = 40;
    private final int BACKGROUND_COLOR = 255; // White
    private int originalDisplayImageX, originalDisplayImageY, originalDisplayImageW, originalDisplayImageH;
    private int controlsX, controlsWidth, controlsHeight, controlsPadding, controlsStartY, controlsTextPadding;
    private boolean colorDropperEnbabled, hslVisited, rgbVisited;

    private Range hueRange, saturationRange, luminescenceRange;
    private Range redRange, greenRange, blueRange;
    private Toggle thresholdToggle;
    private Button colorDropperButton;

    private enum Threshold {
        HSL, RGB
    }
    private Threshold mode = Threshold.HSL;

    @Override
    public void settings() {
        size(1200, 800);
        controlsPadding = 20;
        controlsTextPadding = 40;
        controlsX = width - 400;
        controlsWidth = width - controlsX - controlsPadding -controlsPadding;
        controlsHeight = height - controlsPadding - controlsPadding;

        controlsStartY = 50;
    }

    @Override
    public void setup() {
        // Setup controllers
        cp5 = new ControlP5(this);
        colorDropperImg = loadImage("res/Editing-Color-Dropper-icon.png");

        hueRange = cp5.addRange("Hue")
                .setBroadcast(false)
                .setPosition(controlsX,controlsStartY)
                .setSize(controlsWidth,RANGE_HEIGHT)
                .setHandleSize(20)
                .setRange(0,180)
                .setRangeValues(0.0f,180.0f)
                .setColorCaptionLabel(BACKGROUND_COLOR)
                // after the initialization we turn broadcast back on again
                .setBroadcast(true);

        saturationRange = cp5.addRange("Saturation")
                .setBroadcast(false)
                .setPosition(controlsX,controlsStartY + controlsTextPadding + RANGE_HEIGHT)
                .setSize(controlsWidth,RANGE_HEIGHT)
                .setHandleSize(20)
                .setRange(0,255)
                .setRangeValues(0.0f,255.0f)
                .setColorCaptionLabel(BACKGROUND_COLOR)
                // after the initialization we turn broadcast back on again
                .setBroadcast(true);

        luminescenceRange = cp5.addRange("Luminescence")
                .setBroadcast(false)
                .setPosition(controlsX,controlsStartY + controlsTextPadding + RANGE_HEIGHT+ controlsTextPadding + RANGE_HEIGHT)
                .setSize(controlsWidth,RANGE_HEIGHT)
                .setHandleSize(20)
                .setRange(0,255)
                .setRangeValues(0.0f,255.0f)
                .setColorCaptionLabel(BACKGROUND_COLOR)
                // after the initialization we turn broadcast back on again
                .setBroadcast(true);

        redRange = cp5.addRange("Red")
                .setBroadcast(false)
                .setPosition(controlsX,controlsStartY)
                .setSize(controlsWidth,RANGE_HEIGHT)
                .setHandleSize(20)
                .setRange(0,255)
                .setRangeValues(0.0f,255.0f)
                .setColorCaptionLabel(BACKGROUND_COLOR)
                .setVisible(false)
                .setLock(true)
                // after the initialization we turn broadcast back on again
                .setBroadcast(true);

        greenRange = cp5.addRange("Green")
                .setBroadcast(false)
                .setPosition(controlsX,controlsStartY + controlsTextPadding + RANGE_HEIGHT)
                .setSize(controlsWidth,RANGE_HEIGHT)
                .setHandleSize(20)
                .setRange(0,255)
                .setRangeValues(0.0f,255.0f)
                .setColorCaptionLabel(BACKGROUND_COLOR)
                .setVisible(false)
                .setLock(true)
                // after the initialization we turn broadcast back on again
                .setBroadcast(true);

        blueRange = cp5.addRange("Blue")
                .setBroadcast(false)
                .setPosition(controlsX,controlsStartY + controlsTextPadding + RANGE_HEIGHT+ controlsTextPadding + RANGE_HEIGHT)
                .setSize(controlsWidth,RANGE_HEIGHT)
                .setHandleSize(20)
                .setRange(0,255)
                .setRangeValues(0.0f,255.0f)
                .setColorCaptionLabel(BACKGROUND_COLOR)
                .setVisible(false)
                .setLock(true)
                // after the initialization we turn broadcast back on again
                .setBroadcast(true);

        thresholdToggle = cp5.addToggle("thresholdMode")
                .setBroadcast(false)
                .setPosition(controlsX + controlsWidth/2 - 30, 4*controlsTextPadding + 3*RANGE_HEIGHT)
                .setSize(60, 30)
                .setValue(true)
                .setMode(ControlP5.SWITCH)
                .setBroadcast(true);

        colorDropperButton = cp5.addButton("colorDropperButton")
                .setPosition(controlsX + controlsWidth/2 + 90, 4*controlsTextPadding + 3*RANGE_HEIGHT)
                .setSize(30, 30)
                .setValue(0)
                .setImage(colorDropperImg)
                .setVisible(false)
                .setLock(true);

        // Initialize color dropper
        colorDropperEnbabled = false;

        // Initialize mode. Default is HSL
        mode = Threshold.HSL;

        // Setup pipeline
        imagePipeline = new GripPipeline();

        // Load inital image
        initialImage = Imgcodecs.imread("res/images/image.JPG");

        // Process image, should be all white since the ranges are maxed
        imagePipeline.process(initialImage);

        //displayImage = toPImage(imagePipeline.cvDilateOutput());
        //displayImage.resize(controlsX - controlsPadding, height);

        originalDisplayImage = toPImage(initialImage);
        originalDisplayImage.resize(controlsWidth, controlsWidth);

        displayImage = originalDisplayImage.copy();
        displayImage.resize(controlsX - controlsPadding, height);

        originalDisplayImageX = controlsX;
        originalDisplayImageY = height - originalDisplayImage.height - controlsPadding;
        originalDisplayImageH = originalDisplayImageW = controlsWidth;

        image(displayImage, 0.0f, 0.0f);
        image(originalDisplayImage, originalDisplayImageX, originalDisplayImageY);

        // Misc.
        hslVisited = false;
        rgbVisited = false;
    }

    @Override
    public void draw() {
        background(BACKGROUND_COLOR);

        image(displayImage, 0.0f, 0.0f);
        image(originalDisplayImage, originalDisplayImageX, originalDisplayImageY);

        // Draw range labels
        fill(0);
        if (mode == Threshold.HSL) {
            text(hueRange.getLabel(), hueRange.getPosition()[0], hueRange.getPosition()[1] - 10);
            text(saturationRange.getLabel(), saturationRange.getPosition()[0], saturationRange.getPosition()[1] - 10);
            text(luminescenceRange.getLabel(), luminescenceRange.getPosition()[0], luminescenceRange.getPosition()[1] - 10);
        }else if (mode == Threshold.RGB){
            text(redRange.getLabel(), redRange.getPosition()[0], redRange.getPosition()[1] - 10);
            text(greenRange.getLabel(), greenRange.getPosition()[0], greenRange.getPosition()[1] - 10);
            text(blueRange.getLabel(), blueRange.getPosition()[0], blueRange.getPosition()[1] - 10);
        }

        text("HSL",
                thresholdToggle.getPosition()[0] - thresholdToggle.getWidth()/2,
                thresholdToggle.getPosition()[1] + thresholdToggle.getHeight()/2 + 3);
        text("RGB",
                thresholdToggle.getPosition()[0] + thresholdToggle.getWidth() + 6,
                thresholdToggle.getPosition()[1] + thresholdToggle.getHeight()/2 + 3);

        text("Unprocessed Image", controlsX, height - originalDisplayImage.height - controlsPadding + controlsWidth + 12);

        // Change the cursor to the color dropper when in the appropriate area
        // which is below the threshold toggle and bounded the original image
        if (inRange(mouseX, originalDisplayImageX, originalDisplayImageX + originalDisplayImageW) &&
                inRange(mouseY, controlsStartY + 4*controlsTextPadding + 3*RANGE_HEIGHT, originalDisplayImageY + originalDisplayImageH) &&
                mode == Threshold.RGB && colorDropperEnbabled) {
            cursor(colorDropperImg, 0, 0);
        }else{
            cursor(ARROW);
        }
    }

    @Override
    public void mouseClicked() {
        // Color dropper effect
        int color = get(mouseX, mouseY);
        /*
        System.out.println(color);
        System.out.println("Red: " + red(color));
        System.out.println("Green: " + green(color));
        System.out.println("Blue: " + blue(color));
        */
        if (mode == Threshold.RGB) {
            if (inRange(mouseX, originalDisplayImageX, originalDisplayImageX + originalDisplayImageW) &&
            inRange(mouseY, originalDisplayImageY, originalDisplayImageY + originalDisplayImageH)) {
                final int radius = 20;
                redRange.setRangeValues(red(color) - radius, red(color) + radius);
                greenRange.setRangeValues(green(color) - radius, green(color) + radius);
                blueRange.setRangeValues(blue(color) - radius, blue(color) + radius);
            }
        }
    }

    /**
     * Event handler for the color dropper button
     * @param theValue
     */
    public void colorDropperButton(int theValue){
        colorDropperEnbabled = !colorDropperEnbabled;
    }

    /**
     * Event handler for the threshold switch toggle
     * @param theFlag
     */
    public void thresholdMode(boolean theFlag){
        switchModes();
    }

    private void switchModes(){
        if (mode == Threshold.HSL) {
            mode = Threshold.RGB;
            rgbVisited = true;
            imagePipeline.switchModes();

            hueRange.setVisible(false);
            saturationRange.setVisible(false);
            luminescenceRange.setVisible(false);
            redRange.setVisible(true);
            greenRange.setVisible(true);
            blueRange.setVisible(true);
            colorDropperButton.setVisible(true);

            hueRange.setLock(true);
            saturationRange.setLock(true);
            luminescenceRange.setLock(true);
            redRange.setLock(false);
            greenRange.setLock(false);
            blueRange.setLock(false);
            colorDropperButton.setLock(false);

        }else if (mode == Threshold.RGB) {
            mode = Threshold.HSL;
            hslVisited = true;
            imagePipeline.switchModes();

            hueRange.setVisible(true);
            saturationRange.setVisible(true);
            luminescenceRange.setVisible(true);
            redRange.setVisible(false);
            greenRange.setVisible(false);
            blueRange.setVisible(false);
            colorDropperButton.setVisible(false);

            hueRange.setLock(false);
            saturationRange.setLock(false);
            luminescenceRange.setLock(false);
            redRange.setLock(true);
            greenRange.setLock(true);
            blueRange.setLock(true);
            colorDropperButton.setLock(true);
        }

        // Update the display image to the other threshold method
        // If the other method has not been used, an unprocessed image is displayed
        // mode is what was just changed to
        if (mode == Threshold.RGB && !rgbVisited) {
            displayImage = originalDisplayImage.copy();
            displayImage.resize(controlsX - controlsPadding, height);
        }else if (mode == Threshold.RGB && rgbVisited) {
            imagePipeline.process(initialImage);
            displayImage = toPImage(imagePipeline.cvDilateOutput());
            displayImage.resize(controlsX - controlsPadding, height);
        }

        if (mode == Threshold.HSL && !hslVisited) {
            displayImage = originalDisplayImage.copy();
            displayImage.resize(controlsX - controlsPadding, height);
        }else if (mode == Threshold.HSL && hslVisited) {
            imagePipeline.process(initialImage);
            displayImage = toPImage(imagePipeline.cvDilateOutput());
            displayImage.resize(controlsX - controlsPadding, height);
        }
    }

    /**
     * Event handler for the ranges
     * @param controlEvent
     */
    public void controlEvent(ControlEvent controlEvent){
        boolean processingRequired = false; // Probably not necessary or clean

        if (mode == Threshold.HSL){
            if (controlEvent.isFrom("Hue")){
                imagePipeline.setHueThreshold(hueRange.getArrayValue());
                processingRequired = true;
            }else if (controlEvent.isFrom("Saturation")){
                imagePipeline.setSatThreshold(saturationRange.getArrayValue());
                processingRequired = true;
            }else if (controlEvent.isFrom("Luminescence")) {
                imagePipeline.setLumThreshold(luminescenceRange.getArrayValue());
                processingRequired = true;
            }
        }else if (mode == Threshold.RGB) {
            if (controlEvent.isFrom("Red")) {
                imagePipeline.setRedThreshold(redRange.getArrayValue());
                processingRequired = true;
            }else if (controlEvent.isFrom("Green")) {
                imagePipeline.setGreenThreshold(greenRange.getArrayValue());
                processingRequired = true;
            }else if (controlEvent.isFrom("Blue")) {
                imagePipeline.setBlueThreshold(blueRange.getArrayValue());
                processingRequired = true;
            }
        }

        if (processingRequired) {
            imagePipeline.process(initialImage);
            //displayImage = new PImage(toBufferedImage(imagePipeline.hslThresholdOutput()));
            displayImage = toPImage(imagePipeline.cvDilateOutput());
            displayImage.resize(controlsX - controlsPadding, height);
            //image(displayImage, 0.0f, 0.0f);
            processingRequired = false;
        }
    }

    /**
     * Converts the a opencv mat into a PImage quickly
     * Inspired by https://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/
     * @param m
     * @return
     */
    public PImage toPImage(Mat m){
        int type = PImage.ALPHA;
        if ( m.channels() > 1 ) {
            type = PImage.RGB;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b); // get all the pixels

        //System.out.println("Type: " + type);
        PImage image = createImage(m.cols(), m.rows(), type);
        //System.out.println("Pixels length: " + image.pixels.length + " b length: " + b.length);
        image.loadPixels();
        if (type == 1) {
            int pixId = 0;
            for (int i = 0; i < b.length; i += 3) {
                // KEEP THIS!!!
                image.pixels[pixId] = ((0xFF & b[i + 2]) << 16) | ((0xFF & b[i + 1]) << 8) | (0xFF & b[i]);
                pixId++;
            }
        }else if (type == 4) {
            for (int i = 0; i < b.length; i ++) {
                if (b[i] != -1){
                    image.pixels[i] = color(0);
                }else{
                    image.pixels[i] = color(255);
                }

            }
        }
        image.updatePixels();
        return image;
    }

    private boolean inRange(double value, double a, double b){
        if (a > b) {
            return (value <= a && value >= b);
        }

        return (value <= b && value >= a);

    }

    /**
     * Class's main method. Running this will run the program
     * @param args
     */
    public static void main(String[] args) {
        String[] applet = new String[] {"Main"};
        PApplet.main(applet);
    }
}

import controlP5.*;
import controlP5.Button;
import controlP5.Range;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import processing.core.PApplet;
import processing.core.PImage;
import processing.data.Table;
import processing.data.TableRow;

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    private int originalDisplayImageX, originalDisplayImageY, originalDisplayImageWidth, originalDisplayImageHeight;
    private int displayImageBoundaryX, displayImageBoundaryY, displayImageBoundaryWidth, displayImageBoundaryHeight;
    private int controlsX, controlsWidth, controlsHeight, controlsPadding, controlsStartY, controlsTextPadding;
    private int percentDone = 0;
    private boolean colorDropperEnbabled, hslVisited, rgbVisited, ROIEnabled;
    private boolean topCornerSelected = false;
    private boolean bottomCornerSelected = false;
    private boolean analysisDone = false;
    private String initalImageFilepath;
    private String allImagesFilepath;
    private String imgErrorMessage = "";

    private Range hueRange, saturationRange, luminescenceRange;
    private Range redRange, greenRange, blueRange;
    private Toggle thresholdToggle;
    private Button colorDropperButton;
    private Button confirmButton;
    private Button selectImagesButton;
    private Slider timeSlider;

    private File[] files;

    private Point topCorner;
    private Point bottomCorner;

    private enum Threshold {
        HSL, RGB
    }
    private Threshold mode = Threshold.HSL;

    private enum Stage {
        SELECT_INITAL_IMAGE, SELECT_VALUES, LOAD_IMAGES, RUN_ANALYSIS
    }
    private Stage stage = Stage.SELECT_INITAL_IMAGE;
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
                .setLock(true)
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
                .setLock(true)
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
                .setLock(true)
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
                .setLock(true)
                .setBroadcast(true);

        colorDropperButton = cp5.addButton("colorDropperButton")
                .setBroadcast(false)
                .setPosition(controlsX + controlsWidth/2 + 90, 4*controlsTextPadding + 3*RANGE_HEIGHT)
                .setSize(32, 32)
                .setValue(0)
                .setImage(colorDropperImg)
                .setVisible(false)
                .setLock(true)
                .setBroadcast(true);

        confirmButton = cp5.addButton("Confirm")
                .setBroadcast(false)
                .setPosition(controlsX + controlsWidth/2 - 36, 5*controlsTextPadding + 4*RANGE_HEIGHT - 20)
                .setSize(76, 50)
                .setValue(0)
                .setLock(true)
                .setBroadcast(true);

        confirmButton.getCaptionLabel().setSize(20);

        selectImagesButton = cp5.addButton("selectImage")
                .setBroadcast(false)
                .setPosition((controlsX - controlsPadding)/2, height/2)
                .setSize(100, 20)
                .setLabel("Select Image")
                .setBroadcast(true);

        timeSlider = cp5.addSlider("time")
                .setBroadcast(false)
                .setPosition(controlsX, 4*controlsTextPadding + 3*RANGE_HEIGHT)
                .setSize(controlsWidth, RANGE_HEIGHT)
                .setValue(5)
                .setRange(0, 600)
                .setLabel("")
                .setVisible(false)
                .setLock(true)
                .setBroadcast(true);

        selectImagesButton.getCaptionLabel().setSize(12);

        // Initialize color dropper
        colorDropperEnbabled = false;

        // Initialize ROI
        ROIEnabled = false;

        // Initialize mode. Default is HSL
        mode = Threshold.HSL;

        // Setup pipeline
        imagePipeline = new GripPipeline();

        // Load inital image
        //initialImage = Imgcodecs.imread("res/images/image.JPG");
        //Imgproc.resize(initialImage, initialImage, new Size(controlsX - controlsPadding, height));

        // Process image, should be all white since the ranges are maxed
        //imagePipeline.process(initialImage);

        //displayImage = toPImage(imagePipeline.cvDilateOutput());
        //displayImage.resize(controlsX - controlsPadding, height);

        //originalDisplayImage = toPImage(initialImage);
        //originalDisplayImage.resize(controlsWidth, controlsWidth);

        //displayImage = originalDisplayImage.copy();
        //displayImage.resize(controlsX - controlsPadding, height);

        originalDisplayImageX = controlsX;
        originalDisplayImageY = height - controlsWidth - controlsPadding;
        originalDisplayImageHeight = originalDisplayImageWidth = controlsWidth;

        displayImageBoundaryX = 0;
        displayImageBoundaryY = 0;
        displayImageBoundaryWidth = controlsX - controlsPadding;
        displayImageBoundaryHeight = height;

        // Misc.
        hslVisited = false;
        rgbVisited = false;

        topCorner = new Point(0,0);
        bottomCorner = new Point(displayImageBoundaryWidth, displayImageBoundaryHeight);
    }

    @Override
    public void draw() {
        background(BACKGROUND_COLOR);

        switch (stage){
            case SELECT_INITAL_IMAGE:
                // Draw image bounding boxes
                stroke(0);
                noFill();
                // Display image
                rect(displayImageBoundaryX, displayImageBoundaryY, displayImageBoundaryWidth, displayImageBoundaryHeight);
                // Original Image
                rect(originalDisplayImageX, originalDisplayImageY, originalDisplayImageWidth, originalDisplayImageHeight);

                // Directions
                fill(0);
                textSize(16);
                if (!imgErrorMessage.equals("")) {
                    text(imgErrorMessage, selectImagesButton.getPosition()[0] - (textWidth(imgErrorMessage) - selectImagesButton.getWidth())/2, selectImagesButton.getPosition()[1] - selectImagesButton.getHeight());
                }else {
                    String directions = "Select Image for testing";
                    text(directions, selectImagesButton.getPosition()[0] - (textWidth(directions) - selectImagesButton.getWidth())/2, selectImagesButton.getPosition()[1] - selectImagesButton.getHeight());
                }
                textSize(12);

                // Draw range labels
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

                text("Unprocessed Image", controlsX, height - originalDisplayImageHeight - controlsPadding + controlsWidth + 12);

                break;
            case SELECT_VALUES:
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
                if (inRange(mouseX, originalDisplayImageX, originalDisplayImageX + originalDisplayImageWidth) &&
                        inRange(mouseY, controlsStartY + 4 * controlsTextPadding + 3 * RANGE_HEIGHT, originalDisplayImageY + originalDisplayImageHeight) &&
                        mode == Threshold.RGB && colorDropperEnbabled) {

                    cursor(colorDropperImg, 0, 0);
                } else {
                    cursor(ARROW);
                }


                break;
            case LOAD_IMAGES:
                // If we have images
                if (files != null) {
                    try {
                        image(displayImage, 0.0f, 0.0f);
                    }catch (NullPointerException e){
                        imgErrorMessage = "Unable to load images. Please select a folder with images";
                        text(imgErrorMessage, selectImagesButton.getPosition()[0] - (textWidth(imgErrorMessage) - selectImagesButton.getWidth())/2, selectImagesButton.getPosition()[1] - selectImagesButton.getHeight());

                    }
                }else {
                    // Display image frame
                    noFill();
                    rect(displayImageBoundaryX, displayImageBoundaryY, displayImageBoundaryWidth, displayImageBoundaryHeight);

                    // Directions
                    fill(0);
                    String directions = "Select a folder with images for analysis";
                    text(directions, selectImagesButton.getPosition()[0] - (textWidth(directions) - selectImagesButton.getWidth())/2, selectImagesButton.getPosition()[1] - selectImagesButton.getHeight());
                }

                fill(0);
                textSize(24);
                // Directions
                text("Select a folder containing your\nimages then select your\nRegion of Interest\n\nPress Confirm to advance",
                        controlsX, controlsTextPadding);
                textSize(16);

                // Time slider label
                text("Time between images (sec)", timeSlider.getPosition()[0], timeSlider.getPosition()[1] - 10);
                if (files != null && inRange(mouseX, displayImageBoundaryX, displayImageBoundaryX + displayImageBoundaryWidth)) {
                    ROIEnabled = true;
                    cursor(CROSS);
                    colorDropperEnbabled = false;
                }else{
                    ROIEnabled = false;
                    cursor(ARROW);
                }


                break;
            case RUN_ANALYSIS:
                image(displayImage, 0.0f, 0.0f);

                if (!analysisDone) {
                    fill(0);
                    textSize(24);
                    // Directions
                    text("Analyzing images\n" + percentDone + "% done", controlsX, controlsTextPadding);
                }else {
                    fill(0);
                    textSize(24);
                    // Results
                    String doneMessage =
                            "Analysis finished sucessfully\n" +
                            "Please close the program\n" +
                            "Results are in results.csv and results.txt\n" +
                            "at project root";
                    fill(0, 255, 0);
                    rect(width/2 - textWidth(doneMessage)/2 - 12, height/2 - 48, textWidth(doneMessage) + 24, 24*8);
                    fill(0);
                    textAlign(CENTER);
                    text(doneMessage, width/2, height/2);
                }

                break;
        }
    }

    @Override
    public void mouseClicked() {
        // ROI
        if (ROIEnabled && inRange(mouseX, displayImageBoundaryX, displayImageBoundaryX + displayImageBoundaryWidth) &&
        inRange(mouseY, displayImageBoundaryY, displayImageBoundaryY + displayImageBoundaryHeight)) {
            // Stuff here
            // Because displayImage is from 0,0 it coordinates should be the windows
            if (!topCornerSelected) {
                topCornerSelected = true;
                topCorner = new Point(mouseX, mouseY);
            }else if (topCornerSelected && !bottomCornerSelected) {
                bottomCornerSelected = true;
                bottomCorner = new Point(mouseX, mouseY);
                System.out.println("Top Corner: x=" + topCorner.x + " y=" + topCorner.y);
                System.out.println("Bottom Corner: x=" + bottomCorner.x + " y=" + bottomCorner.y);

                Mat roiImage = new Mat();
                initialImage.copyTo(roiImage);
                imagePipeline.enableROI(topCorner, bottomCorner);
                displayImage = toPImage(imagePipeline.roiPreview(roiImage));

                topCornerSelected = false;
                bottomCornerSelected = false;
            }

        }

        // Color dropper effect
        int color = get(mouseX, mouseY);
        /*
        System.out.println(color);
        System.out.println("Red: " + red(color));
        System.out.println("Green: " + green(color));
        System.out.println("Blue: " + blue(color));
        */
        if (mode == Threshold.RGB) {
            if (inRange(mouseX, originalDisplayImageX, originalDisplayImageX + originalDisplayImageWidth) &&
            inRange(mouseY, originalDisplayImageY, originalDisplayImageY + originalDisplayImageHeight) &&
            colorDropperEnbabled) {
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

    public void selectImage(int theValue){
        System.out.println("Image select");
        // HACK ALERT!! Dumb library wont work when set invisible so it gets moved off screen
        //selectImagesButton.setPosition(width + 100, height + 100);

        if (stage == Stage.SELECT_INITAL_IMAGE) {
            selectInput("Select Sample Image", "fileSelector");
        }else if (stage == Stage.LOAD_IMAGES) {
            selectFolder("Select Images to Analyze", "folderSelector");
        }

    }

    /**
     * Method for getting the folder containing all the images
     * @param selection
     */
    public void folderSelector(File selection){
        if (selection == null) {
            // TODO Do something in this case
            System.out.println("Window closed");
        }else {
            System.out.println("Filepath: " + selection.getAbsolutePath());
            File directory = new File(selection.getAbsolutePath());
            if (directory.isDirectory()) {
                files = directory.listFiles();

                try {
                    displayImage = loadImage(files[files.length - 1].getAbsolutePath());
                    displayImage.resize(displayImageBoundaryWidth, displayImageBoundaryHeight);

                    // HACK ALERT!! Dumb library wont work when set invisible so it gets moved off screen
                    selectImagesButton.setPosition(width + 100, height + 100);
                } catch(NullPointerException e) {
                    //e.printStackTrace();
                    imgErrorMessage = "Unable to load images. Please select a folder with images";
                }

                //ROISwitch.setLock(true);
            }else {
                files = null;
            }
        }
    }

    /**
     * Method for getting the test image
     * @param selection
     */
    public void fileSelector(File selection){
        if (selection == null){
            System.out.println("Window closed");
        }else {
            System.out.println("Filepath: " + selection.getAbsolutePath());

            try {
                // Load inital image
                initialImage = Imgcodecs.imread(selection.getAbsolutePath());
                Imgproc.resize(initialImage, initialImage, new Size(controlsX - controlsPadding, height));

                // HACK ALERT!! Dumb library wont work when set invisible so it gets moved off screen
                selectImagesButton.setPosition(width + 100, height + 100);
                selectImagesButton.setLabel("Select Images");

                switchStage(Stage.SELECT_VALUES);
            }catch (Exception e){
                System.out.println("Invalid file loaded");
                imgErrorMessage = "Unable to load image. Please select a file that is an image";
            }
        }

    }
    
    private void switchStage(Stage newStage){
        switch (newStage){
            case SELECT_INITAL_IMAGE:
                // Lock everything down
                hueRange.setLock(true);
                saturationRange.setLock(true);
                luminescenceRange.setLock(true);
                redRange.setLock(true);
                greenRange.setLock(true);
                blueRange.setLock(true);
                colorDropperButton.setLock(true);
                confirmButton.setLock(true);
                thresholdToggle.setLock(true);

                // Bring back select image
                selectImagesButton.setPosition((controlsX - controlsPadding)/2, height/2);

                stage = Stage.SELECT_INITAL_IMAGE;

                break;
            case SELECT_VALUES:
                originalDisplayImage = toPImage(initialImage);
                originalDisplayImage.resize(controlsWidth, controlsWidth);

                displayImage = originalDisplayImage.copy();
                displayImage.resize(controlsX - controlsPadding, height);

                hueRange.setLock(false);
                saturationRange.setLock(false);
                luminescenceRange.setLock(false);
                redRange.setLock(true);
                greenRange.setLock(true);
                blueRange.setLock(true);
                colorDropperButton.setLock(true);
                confirmButton.setLock(false);
                thresholdToggle.setLock(false);

                stage = Stage.SELECT_VALUES;
                break;

            case LOAD_IMAGES:
                // Remove the old GUI elements
                hueRange.setVisible(false);
                saturationRange.setVisible(false);
                luminescenceRange.setVisible(false);
                redRange.setVisible(false);
                greenRange.setVisible(false);
                blueRange.setVisible(false);
                colorDropperButton.setVisible(false);
                thresholdToggle.setVisible(false);
                timeSlider.setVisible(true);

                hueRange.setLock(true);
                saturationRange.setLock(true);
                luminescenceRange.setLock(true);
                redRange.setLock(true);
                greenRange.setLock(true);
                blueRange.setLock(true);
                colorDropperButton.setLock(true);
                thresholdToggle.setLock(true);
                timeSlider.setLock(false);

                // Bring back select image
                selectImagesButton.setPosition((controlsX - controlsPadding)/2, height/2);

                stage = Stage.LOAD_IMAGES;

                break;
            case RUN_ANALYSIS:
                // Remove GUI
                timeSlider.setVisible(false);
                timeSlider.setLock(false);
                confirmButton.setVisible(false);
                confirmButton.setLock(true);

                // Run the analysis
                thread("analyze");

                stage = Stage.RUN_ANALYSIS;
                break;
            
                default:
                    System.out.println("You aren't going anywhere!");
                    break;
                
                
        }
    }

    private void switchModes(){
        if (mode == Threshold.HSL) {
            mode = Threshold.RGB;
            imagePipeline.switchThresholdModes();

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
            imagePipeline.switchThresholdModes();

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
            rgbVisited = true;
        }else if (mode == Threshold.RGB && rgbVisited) {
            imagePipeline.process(initialImage);
            displayImage = toPImage(imagePipeline.cvDilateOutput());
            displayImage.resize(controlsX - controlsPadding, height);
        }

        if (mode == Threshold.HSL && !hslVisited) {
            displayImage = originalDisplayImage.copy();
            displayImage.resize(controlsX - controlsPadding, height);
            hslVisited = true;
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

        if (controlEvent.isFrom("Confirm")) {
            System.out.println("Confirmed!");
            // Can't turn off confirm because it locks up all other elements for some reason
            if (stage == Stage.SELECT_VALUES) {
                switchStage(Stage.LOAD_IMAGES);
            }else if (stage == Stage.LOAD_IMAGES) {
                System.out.println("Move on!");
                switchStage(Stage.RUN_ANALYSIS);
            }
        }

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

    public void analyze(){
        // Contains the results
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
        DecimalFormat df = new DecimalFormat("#.###");
        String time = format.format(date);

        PrintWriter textOutput = createWriter("Results\\results_" + time + ".txt");
        Table csvOutput = new Table();
        csvOutput.addColumn("Filename");
        csvOutput.addColumn("Elapsed Time (sec)");
        csvOutput.addColumn("Highest Pixel");
        csvOutput.addColumn("Lowest Pixel");

        //Nice things
        percentDone = 0;

        double timeInterval = timeSlider.getValue();

        for (int i = 0; i< files.length; i++) {
            percentDone = (int)(((double)i / files.length) * 100.0);
            System.out.println((i / files.length) * 100.0);
            // Load in image
            Mat sourceImage = Imgcodecs.imread(files[i].getAbsolutePath());

            // Process the image
            imagePipeline.process(sourceImage);

            // Find contours
            ArrayList<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(imagePipeline.cvDilateOutput(), contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

            // Find largest contour
            MatOfPoint largestContour = contours.get(0);
            for (MatOfPoint contour : contours){
                if (Imgproc.contourArea(contour) > Imgproc.contourArea(largestContour)) {
                    largestContour = contour;
                }
            }

            // Get bounding box
            Rect boundingRect = Imgproc.boundingRect(largestContour);
            Point topCorner = new Point(boundingRect.x, boundingRect.y);
            Point bottomCorner = new Point(boundingRect.x + boundingRect.width, boundingRect.y + boundingRect.height);
            // Draw bounding box
            Imgproc.rectangle(sourceImage, topCorner, bottomCorner, new Scalar(0, 255, 0), 5);

            // Resize
            Mat outputImage = new Mat();
            Imgproc.resize(sourceImage, outputImage, new Size(displayImageBoundaryWidth, displayImageBoundaryHeight));

            // Record results
            //output.print("Image " + f.getName() + " lowest point : " + lowPoint[file] + " pixels");
            textOutput.println("Image " + files[i].getName() +
                    " highest pixel: " + boundingRect.y +
                    " lowest pixel: " + (boundingRect.y + boundingRect.height) +
                    " elapsed time: " + df.format(timeInterval * i));
            TableRow row = csvOutput.addRow();
            row.setString("Filename", files[i].getName());
            row.setString("Elapsed Time (sec)", df.format(timeInterval * i));
            row.setInt("Highest Pixel", boundingRect.y);
            row.setInt("Lowest Pixel", boundingRect.y + boundingRect.height);

            displayImage = toPImage(outputImage);
        }

        // Close the writers
        textOutput.flush();
        textOutput.close();
        saveTable(csvOutput, "Results\\results_" + time + ".csv");

        // When finished
        analysisDone = true;
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

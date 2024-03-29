import org.opencv.core.*;
import org.opencv.imgproc.*;

/**
* GripPipeline class.
*
* <p>An OpenCV pipeline generated by GRIP.
*
* @author GRIP, modified by Eli Jelesko
*/
public class GripPipeline {

	//Outputs
	private Mat cvGaussianblurOutput = new Mat();
	private Mat hslThresholdOutput = new Mat();
	private Mat rgbThresholdOutput = new Mat();
	private Mat cvErodeOutput = new Mat();
	private Mat cvDilateOutput = new Mat();
	private Mat roiOuput = new Mat();
	
	// Values
	private double[] hueThreshold = {0, 180};
	private double[] satThreshold = {0, 255};
	private double[] lumThreshold = {0, 255};

	private double[] redThreshold = {0, 255};
	private double[] greenThreshold = {0, 255};
	private double[] blueThreshold = {0, 255};

	private boolean hsl = true;
	private boolean roi = false;

	private Point roiTopCorner;
	private Point roiBottomCorner;

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	/**
	 * This is the primary method that runs the entire pipeline and updates the outputs.
	 */
	public void process(Mat source0) {
		//System.out.println("Image processing");

		// Step ROI (if enabled)
		Mat cvGaussianblurSrc;
		if (roi) {
			Mat roiSource = source0;
			ROI(roiSource);
			cvGaussianblurSrc = roiOuput;
		}else {
			cvGaussianblurSrc = source0;
		}

		// Step CV_GaussianBlur0:
		Size cvGaussianblurKsize = new Size(1, 1);
		double cvGaussianblurSigmax = 5.0;
		double cvGaussianblurSigmay = 5.0;
		int cvGaussianblurBordertype = Core.BORDER_DEFAULT;
		cvGaussianblur(cvGaussianblurSrc, cvGaussianblurKsize, cvGaussianblurSigmax, cvGaussianblurSigmay, cvGaussianblurBordertype, cvGaussianblurOutput);

		// Step HSL_Threshold0:

		if (hsl) {
			Mat hslThresholdInput = cvGaussianblurOutput;
			double[] hslThresholdHue = hueThreshold;
			double[] hslThresholdSaturation = satThreshold;
			double[] hslThresholdLuminance = lumThreshold;
			hslThreshold(hslThresholdInput, hslThresholdHue, hslThresholdSaturation, hslThresholdLuminance, hslThresholdOutput);
		}else{
			// Step RGB_Threshold0:
			Mat rgbThresholdInput = cvGaussianblurOutput;
			double[] rgbThresholdRed = redThreshold;
			double[] rgbThresholdGreen = greenThreshold;
			double[] rgbThresholdBlue = blueThreshold;
			rgbThreshold(rgbThresholdInput, rgbThresholdRed, rgbThresholdGreen, rgbThresholdBlue, rgbThresholdOutput);
		}
		// Step CV_erode0:
		Mat cvErodeSrc;
		if (hsl){
			cvErodeSrc = hslThresholdOutput;
		}else{
			cvErodeSrc = rgbThresholdOutput;
		}
		Mat cvErodeKernel = new Mat();
		Point cvErodeAnchor = new Point(-1, -1);
		double cvErodeIterations = 1.0;
		int cvErodeBordertype = Core.BORDER_CONSTANT;
		Scalar cvErodeBordervalue = new Scalar(-1);
		cvErode(cvErodeSrc, cvErodeKernel, cvErodeAnchor, cvErodeIterations, cvErodeBordertype, cvErodeBordervalue, cvErodeOutput);

		// Step CV_dilate0:
		Mat cvDilateSrc = cvErodeOutput;
		Mat cvDilateKernel = new Mat();
		Point cvDilateAnchor = new Point(-1, -1);
		double cvDilateIterations = 1.0;
		int cvDilateBordertype = Core.BORDER_CONSTANT;
		Scalar cvDilateBordervalue = new Scalar(-1);
		cvDilate(cvDilateSrc, cvDilateKernel, cvDilateAnchor, cvDilateIterations, cvDilateBordertype, cvDilateBordervalue, cvDilateOutput);

	}

	public void switchThresholdModes(){
		hsl = !hsl;
	}

	public void enableROI(Point topCorner, Point bottomCorner) {
		roiTopCorner = topCorner;
		roiBottomCorner = bottomCorner;
		roi = true;
	}
	public void disbleROI() {
		roi = false;
	}

	/**
	 * Setter for the hueThreshold
	 * @param hueThreshold double array of length 2 with the threshold bounds
	 */
	public void setHueThreshold(float[] hueThreshold){
		this.hueThreshold[0] = (double)hueThreshold[0];
		this.hueThreshold[1] = (double)hueThreshold[1];
	}

	/**
	 * Setter for the satThreshold
	 * @param satThreshold double array of length 2 with the threshold bounds
	 */
	public void setSatThreshold(float[] satThreshold){
		this.satThreshold[0] = (double)satThreshold[0];
		this.satThreshold[1] = (double)satThreshold[1];
	}

	/**
	 * Setter for the lumThreshold
	 * @param lumThreshold double array of length 2 with the threshold bounds
	 */
	public void setLumThreshold(float[] lumThreshold){
		this.lumThreshold[0] = (double)lumThreshold[0];
		this.lumThreshold[1] = (double)lumThreshold[1];
	}

	/**
	 * Setter for the redThreshold
	 * @param redThreshold double array of length 2 with the threshold bounds
	 */
	public void setRedThreshold(float[] redThreshold){
		this.redThreshold[0] = redThreshold[0];
		this.redThreshold[1] = redThreshold[1];
	}

	/**
	 * Setter for the greenThreshold
	 * @param greenThreshold double array of length 2 with the threshold bounds
	 */
	public void setGreenThreshold(float[] greenThreshold){
		this.greenThreshold[0] = greenThreshold[0];
		this.greenThreshold[1] = greenThreshold[1];
	}

	/**
	 * Setter for the blueThreshold
	 * @param blueThreshold double array of length 2 with the threshold bounds
	 */
	public void setBlueThreshold(float[] blueThreshold){
		this.blueThreshold[0] = blueThreshold[0];
		this.blueThreshold[1] = blueThreshold[1];
	}



	/**
	 * This method is a generated getter for the output of a CV_GaussianBlur.
	 * @return Mat output from CV_GaussianBlur.
	 */
	public Mat cvGaussianblurOutput() {
		return cvGaussianblurOutput;
	}

	/**
	 * This method is a generated getter for the output of a HSL_Threshold.
	 * @return Mat output from HSL_Threshold.
	 */
	public Mat hslThresholdOutput() {
		return hslThresholdOutput;
	}

	/**
	 * This method is a generated getter for the output of a RGB_Threshold.
	 * @return Mat output from RGB_Threshold.
	 */
	public Mat rgbThresholdOutput() {
		return rgbThresholdOutput;
	}

	/**
	 * This method is a generated getter for the output of a CV_erode.
	 * @return Mat output from CV_erode.
	 */
	public Mat cvErodeOutput() {
		return cvErodeOutput;
	}

	/**
	 * This method is a generated getter for the output of a CV_dilate.
	 * @return Mat output from CV_dilate.
	 */
	public Mat cvDilateOutput() {
		return cvDilateOutput;
	}

	public Mat roiOutput() {return roiOuput;}

	public Mat roiPreview(Mat input) {
		if (roi) {
			//ROI(input, roiTopCorner, roiBottomCorner);
			Imgproc.rectangle(input, roiTopCorner, roiBottomCorner, new Scalar(255, 0, 0), 2);
		}
		return input;
	}

	public Mat roiPreview(Mat input, Point topCorner, Point bottomCorner) {
	    Mat modifiedImage = input.clone();
        Imgproc.rectangle(modifiedImage, topCorner, bottomCorner, new Scalar(255, 0, 0), 2);
        return modifiedImage;
    }




	/**
	 * Performs a Gaussian blur on the image.
	 * @param src the image to blur.
	 * @param kSize the kernel size.
	 * @param sigmaX the deviation in X for the Gaussian blur.
	 * @param sigmaY the deviation in Y for the Gaussian blur.
	 * @param borderType pixel extrapolation method.
	 * @param dst the output image.
	 */
	private void cvGaussianblur(Mat src, Size kSize, double sigmaX, double sigmaY,
		int	borderType, Mat dst) {
		if (kSize == null) {
			kSize = new Size(1,1);
		}
		Imgproc.GaussianBlur(src, dst, kSize, sigmaX, sigmaY, borderType);
	}

	/**
	 * Segment an image based on hueThreshold, saturation, and luminance ranges.
	 *
	 * @param input The image on which to perform the HSL threshold.
	 * @param hue The min and max hueThreshold
	 * @param sat The min and max saturation
	 * @param lum The min and max luminance
	 * @param out The image in which to store the output.
	 */
	private void hslThreshold(Mat input, double[] hue, double[] sat, double[] lum,
		Mat out) {
		Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2HLS);
		Core.inRange(out, new Scalar(hue[0], lum[0], sat[0]),
			new Scalar(hue[1], lum[1], sat[1]), out);
	}

	/**
	 * Expands area of lower value in an image.
	 * @param src the Image to erode.
	 * @param kernel the kernel for erosion.
	 * @param anchor the center of the kernel.
	 * @param iterations the number of times to perform the erosion.
	 * @param borderType pixel extrapolation method.
	 * @param borderValue value to be used for a constant border.
	 * @param dst Output Image.
	 */
	private void cvErode(Mat src, Mat kernel, Point anchor, double iterations,
		int borderType, Scalar borderValue, Mat dst) {
		if (kernel == null) {
			kernel = new Mat();
		}
		if (anchor == null) {
			anchor = new Point(-1,-1);
		}
		if (borderValue == null) {
			borderValue = new Scalar(-1);
		}
		Imgproc.erode(src, dst, kernel, anchor, (int)iterations, borderType, borderValue);
	}

	/**
	 * Expands area of higher value in an image.
	 * @param src the Image to dilate.
	 * @param kernel the kernel for dilation.
	 * @param anchor the center of the kernel.
	 * @param iterations the number of times to perform the dilation.
	 * @param borderType pixel extrapolation method.
	 * @param borderValue value to be used for a constant border.
	 * @param dst Output Image.
	 */
	private void cvDilate(Mat src, Mat kernel, Point anchor, double iterations,
	int borderType, Scalar borderValue, Mat dst) {
		if (kernel == null) {
			kernel = new Mat();
		}
		if (anchor == null) {
			anchor = new Point(-1,-1);
		}
		if (borderValue == null){
			borderValue = new Scalar(-1);
		}
		Imgproc.dilate(src, dst, kernel, anchor, (int)iterations, borderType, borderValue);
	}


	/**
	 * Filter out an area of an image using a binary mask.
	 * @param input The image on which the mask filters.
	 * @param mask The binary image that is used to filter.
	 * @param output The image in which to store the output.
	 */
	private void mask(Mat input, Mat mask, Mat output) {
		mask.convertTo(mask, CvType.CV_8UC1);
		Core.bitwise_xor(output, output, output);
		input.copyTo(output, mask);
	}

	/**
	 * Segment an image based on color ranges.
	 * @param input The image on which to perform the RGB threshold.
	 * @param red The min and max red.
	 * @param green The min and max green.
	 * @param blue The min and max blue.
	 * @param out The image in which to store the output.
	 */
	private void rgbThreshold(Mat input, double[] red, double[] green, double[] blue,
							  Mat out) {
		Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2RGB);
		Core.inRange(out, new Scalar(red[0], green[0], blue[0]),
				new Scalar(red[1], green[1], blue[1]), out);
	}

	private void ROI(Mat input){
		if (roiTopCorner != null && roiBottomCorner != null) {
			System.out.println("ROI setup");
			ROI(input, roiTopCorner, roiBottomCorner);
		}
	}

	private void ROI(Mat input, Point topCorner, Point bottomCorner){
		System.out.println("ROI processing ran");
		// Draw a rectangle here

		Mat canvas = new Mat(input.rows(), input.cols(), input.type(), new Scalar(0));
		Imgproc.rectangle(canvas, topCorner, bottomCorner, new Scalar(255, 255, 255), Imgproc.FILLED);


		int x = (int)topCorner.x;
		int y = (int)topCorner.y;
		int w = (int)(bottomCorner.x - topCorner.x);
		int h = (int)(bottomCorner.y - topCorner.y);

		Rect roi = new Rect(x, y, w, h);
		mask(input, canvas, roiOuput);
	}

}


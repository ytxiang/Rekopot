package edu.sjsu.cmpe281.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.ValidationException;

import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.InvalidImageFormatException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.regions.*;
import com.amazonaws.util.IOUtils;

public class LicensePlateParser {

	AWSCredentialsProvider credProvider;

	Regions region;

	public LicensePlateParser(AWSCredentialsProvider credProvider, Regions region) {
		this.credProvider = credProvider;
		this.region = region;
	}

	public float isLicensePlate(File file) throws IOException, ValidationException {

		ByteBuffer imageBytes;
		try (InputStream inputStream = new FileInputStream(file)) {
			imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 0.0f;
		}

		DetectLabelsRequest labelsRequest = new DetectLabelsRequest()
				.withImage(new Image().withBytes(imageBytes));

		return isLicensePlate(labelsRequest);
	}


	public float isLicensePlate(String bucketName, String filePath) 
				throws ValidationException {

		DetectLabelsRequest labelsRequest = new DetectLabelsRequest()
				.withImage(new Image()
				.withS3Object(new S3Object()
				.withName(filePath).withBucket(bucketName)));

		return isLicensePlate(labelsRequest);
	}

	private float isLicensePlate(DetectLabelsRequest labelsRequest) 
				throws ValidationException {

		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
				.withRegion(region).withCredentials(credProvider).build();
		
		final float confidenceThreshold = 75.0f;

		try {
			DetectLabelsResult result = rekognitionClient.detectLabels(labelsRequest);
			List<Label> labels = result.getLabels();
			for (Label label : labels) {
				if (label.getName().equals("License Plate")) {
					if (label.getConfidence() >= confidenceThreshold)
						return 1.0f;
					return label.getConfidence() / confidenceThreshold;
				}
			}
		} catch (InvalidImageFormatException e) {
			throw new ValidationException("Request has invalid image format, only .JPG or .PNG image file is supported.");
		} catch (AmazonRekognitionException e) {
			e.printStackTrace();
		}
		return 0.0f;

	}


	/**
	 * Kevin Lai
	 * 008498282
	 * SJSU CMPE 281 Project 2
	 */
	public String detectLicensePlateNumber(String bucketName, String filePath) {

		// This is the regular expression used to match license plate numbers.
		String licensePattern = "[A-Za-z0-9]+";

		// This is the regular expression used to remove unmatched characters from detected text.
		String nonLicensePattern = "[^A-Za-z0-9]+";

		// This matcher will be used later in the matching according to the specified regular expression pattern
		Matcher outputMatcher;
		
		// This is to compile the regular expression pattern
		Pattern outputPattern = Pattern.compile(licensePattern);
		
		// This variable will store the license plate number
		String capturedLicensePlateNumber = "";

		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
				.withRegion(region).withCredentials(credProvider).build();
		
		DetectTextRequest textRequest = new DetectTextRequest()
				.withImage(new Image().withS3Object(new S3Object().withName(filePath).withBucket(bucketName)));

		try {
			DetectTextResult textResult = rekognitionClient.detectText(textRequest);
			List<TextDetection> textDetections = textResult.getTextDetections();
			TextDetection licensePlateNumberText = null;
			
			Float textDetectionArea = 0.0f;
			
			// After the loop has completed, only text detection with the largest found bounding box in the entire image will be saved for license plate number processing
			for (TextDetection text : textDetections) {

				if (!text.getType().equals("LINE"))
					continue;

				// Calculate the area of the bounding boxes for each text detection
				Float newTextArea = text.getGeometry().getBoundingBox().getWidth()
						* text.getGeometry().getBoundingBox().getHeight();

				// Look for the text detection with the largest bounding box
				if (newTextArea > textDetectionArea) {
					// Update each time a larger bounding box text is detected 
					textDetectionArea = newTextArea;
					// Store the text with the current largest found bounding box.
					licensePlateNumberText = text;
				}
			}
			
			if (licensePlateNumberText != null) {
				
				// This is used to remove all whitespaces, including gaps, in each line
				String cleanedText = licensePlateNumberText.getDetectedText().replaceAll(nonLicensePattern, "");
				
				// Apply the matching pattern to each line of text.
				outputMatcher = outputPattern.matcher(cleanedText);
				
				// If the matcher finds a match, then it will enter into this condition.
				if(outputMatcher.find()){
					// Append the matched license plate number to the output string
					capturedLicensePlateNumber += outputMatcher.group(0);
				}
			}
			
			return capturedLicensePlateNumber;
			
		} catch (AmazonRekognitionException e) {
			e.printStackTrace();
			System.out.println("bucketName: " + bucketName + ", filePath: " + filePath);
		}
		
		// If it fails to catch the exception, then it will just return empty string
		return "";
	}
	
}

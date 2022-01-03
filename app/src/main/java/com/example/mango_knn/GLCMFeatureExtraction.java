/**
 * Ardiansyah | http://ard.web.id
 *
 */

package com.example.mango_knn;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author Ardiansyah <ard333.ardiansyah@gmail.com>
 */
public class GLCMFeatureExtraction {
	
	private Bitmap image;
	private int[][] grayLeveledMatrix;
	private int grayLevel;
	private double contrast;
	private double homogenity;
	private double entropy;
	private double energy;
	private double dissimilarity;
	private double correlation;

	private double meanx = 0.0;
	private double meany = 0.0;
	private double stdevx = 0.0;
	private double stdevy = 0.0;

	public GLCMFeatureExtraction(String filePath, int grayLevel) throws IOException {
		FileInputStream fs = new FileInputStream(filePath);
		Bitmap fullImg = BitmapFactory.decodeStream(fs);
		this.image = getResizedBitmap(fullImg, 256, 256);
		this.grayLevel = grayLevel;
		grayLeveledMatrix = new int[this.image.getWidth()][this.image.getHeight()];
	}

	public Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight) {
		Bitmap resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

		float scaleX = (float)newWidth / (float)bitmap.getWidth();
		float scaleY = (float)newHeight / (float)bitmap.getHeight();
		float pivotX = 0;
		float pivotY = 0;

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);
		Canvas canvas = new Canvas(resizedBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG));

		return resizedBitmap;
	}

	public void calcBasicStats(double[][] matrix) {
		double[] px = new double[matrix.length];
		double[] py = new double[matrix.length];
		int i, j;

		meanx = 0.0;
		meany = 0.0;
		stdevx = 0.0;
		stdevy = 0.0;

		for (i = 0;  i < matrix.length; i++){
			px[i] = 0.0;
			py[i] = 0.0;
		}

		// sum the glcm rows to Px(i)
		for (i = 0;  i < matrix.length; i++) {
			for (j = 0; j < matrix.length; j++) {
				px[i] += matrix[i][j];
			}
		}

		// sum the glcm rows to Py(j)
		for (j = 0; j < matrix.length; j++) {
			for (i = 0; i < matrix.length; i++) {
				py[j] += matrix[i][j];
			}
		}

		// calculate meanx and meany
		for (i = 0; i < matrix.length; i++) {
			meanx += (i * px[i]);
			meany += (i * py[i]);
		}

		// calculate stdevx and stdevy
		for (i = 0; i < matrix.length; i++) {
			stdevx += ((Math.pow((i - meanx), 2)) * px[i]);
			stdevy += ((Math.pow((i - meany), 2)) * py[i]);
		}
	}

	public Bitmap getBitmap() {
		return image;
	}
	
	public void extract(int distance) {
		this.createGrayLeveledMatrix();
		
		//0°
		int[][] cm0 = createCoOccuranceMatrix(0, distance);
		double[][] cm0SN = normalizeMatrix(add(cm0, transposeMatrix(cm0)));
		
		//45°
		int[][] cm45 = createCoOccuranceMatrix(45, distance);
		double[][] cm45SN = normalizeMatrix(add(cm45, transposeMatrix(cm45)));
		
		//90°
		int[][] cm90 = createCoOccuranceMatrix(90, distance);
		double[][] cm90SN = normalizeMatrix(add(cm90, transposeMatrix(cm90)));
		
		//135°
		int[][] cm135 = createCoOccuranceMatrix(135, distance);
		double[][] cm135SN = normalizeMatrix(add(cm135, transposeMatrix(cm135)));
		
		this.contrast = (double) (calcContrast(cm0SN) + calcContrast(cm45SN) + calcContrast(cm90SN) + calcContrast(cm135SN)) / 4;
		this.homogenity = (double) (calcHomogenity(cm0SN) + calcHomogenity(cm45SN) + calcHomogenity(cm90SN) + calcHomogenity(cm135SN)) / 4;
		this.entropy = (double) (calcEntropy(cm0SN) + calcEntropy(cm45SN) + calcEntropy(cm90SN) + calcEntropy(cm135SN)) / 4;
		this.energy = (double) (calcEnergy(cm0SN) + calcEnergy(cm45SN) + calcEnergy(cm90SN) + calcEnergy(cm135SN)) / 4;
		this.dissimilarity = (double) (calcDissimilarity(cm0SN) + calcDissimilarity(cm45SN) + calcDissimilarity(cm90SN) + calcDissimilarity(cm135SN)) / 4;
		this.correlation = (double) (calcCorrelation(cm0SN) + calcCorrelation(cm45SN) + calcCorrelation(cm90SN) + calcCorrelation(cm135SN)) / 4;
		
	}
	
	private void createGrayLeveledMatrix() {
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				int grey = image.getPixel(i, j);
				int newR = ((grey & 0x00FF0000) >> 16);
				int newG = ((grey & 0x0000FF00) >> 8);
				int newB = (grey & 0x000000FF);
//				int newA = rgb.getAlpha();
//				int gr = (newR + newG + newB) / 3;
				int gr = (int)((float)newR * 0.3 + (float)newG * 0.59 + (float)newB * 0.11);
				if (gr > 255)
					gr = 255;
				if (gr < 0)
					gr = 0;
				
				if (grayLevel > 0 && grayLevel < 255) {
					grayLeveledMatrix[i][j] = gr * grayLevel / 255;
				} else {
					grayLeveledMatrix[i][j] = gr;
				}
			}
		}
	}
	
	private int[][] createCoOccuranceMatrix(int angle, int distance) { //distance = 1
		int[][] temp = new int[grayLevel+1][grayLevel+1];
		int startRow = 0;
		int startColumn = 0;
		int endColumn = 0;
		
		boolean validAngle = true;
		switch (angle) {
			case 0:
				startRow = 0;
				startColumn = 0;
				endColumn = grayLeveledMatrix[0].length - (2 * distance);
				break;
			case 45:
				startRow = distance;
				startColumn = 0;
				endColumn = grayLeveledMatrix[0].length - (2 * distance);
				break;
			case 90:
				startRow = distance;
				startColumn = 0;
				endColumn = grayLeveledMatrix[0].length - (1 * distance);
				break;
			case 135:
				startRow = distance;
				startColumn = distance;
				endColumn = grayLeveledMatrix[0].length - (1 * distance);
				break;
			default:
				validAngle = false;
				break;
		}
		
		if (validAngle) {
			for (int i = startRow; i < grayLeveledMatrix.length; i++) {
				for (int j = startColumn; j <= endColumn; j++) {
					switch (angle) {
						case 0:
							temp[grayLeveledMatrix[i][j]][grayLeveledMatrix[i][j + distance]]++;
							break;
						case 45:
							temp[grayLeveledMatrix[i][j]][grayLeveledMatrix[i - distance][j + distance]]++;
							break;
						case 90:
							temp[grayLeveledMatrix[i][j]][grayLeveledMatrix[i - distance][j]]++;
							break;
						case 135:
							temp[grayLeveledMatrix[i][j]][grayLeveledMatrix[i - distance][j - distance]]++;
							break;
					}
				}
			}
		}
		return temp;
	}
	
	private int[][] transposeMatrix(int [][] m){
		int[][] temp = new int[m[0].length][m.length];
		for (int i = 0; i < m.length; i++){
			for (int j = 0; j < m[0].length; j++){
				temp[j][i] = m[i][j];
			}
		}
		return temp;
	}
	
	private int[][] add(int [][] m2, int [][] m1){
		int[][] temp = new int[m1[0].length][m1.length];
		for (int i = 0; i < m1.length; i++){
			for (int j = 0; j < m1[0].length; j++){
				temp[j][i] = m1[i][j] + m2[i][j];
			}
		}
		return temp;
	}
	
	private int getTotal(int [][] m){
		int temp = 0;
		for (int i = 0; i < m.length; i++){
			for (int j = 0; j < m[0].length; j++){
				temp += m[i][j];
			}
		}
		return temp;
	}
	
	private double[][] normalizeMatrix(int [][] m){
		double[][] temp = new double[m[0].length][m.length];
		int total = getTotal(m);
		for (int i = 0; i < m.length; i++){
			for (int j = 0; j < m[0].length; j++){
				temp[j][i] = (double) m[i][j] / total;
			}
		}
		return temp;
	}
	
	private double calcContrast(double[][] matrix) {
		double temp = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				temp += matrix[i][j] * Math.pow(i-j, 2);
			}
		}
		return temp;
	}
	
	private double calcHomogenity(double[][] matrix) {
		double temp = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				temp += matrix[i][j] / (1+Math.pow(i-j, 2));
			}
		}
		return temp;
	}
	
	private double calcEntropy(double[][] matrix) {
		double temp = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				if (matrix[i][j] != 0) {
					temp += (matrix[i][j] * Math.log10(matrix[i][j])) * -1;
				}
			}
		}
		return temp;
	}
	
	private double calcEnergy(double[][] matrix) {
		double temp = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				temp += Math.pow(matrix[i][j], 2);
			}
		}
		return temp;
	}
	
	private double calcDissimilarity(double[][] matrix) {
		double temp = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				temp += matrix[i][j] * Math.abs(i-j);
			}
		}
		return temp;
	}

	private double calcCorrelation(double[][] matrix) {
		double temp = 0;

		calcBasicStats(matrix);

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				temp += matrix[i][j] * (((i - meanx) * (j - meany)) / (stdevx * stdevy));
			}
		}
		return temp;
	}

	public double getContrast() {
		return contrast;
	}

	public double getHomogenity() {
		return homogenity;
	}

	public double getEntropy() {
		return entropy;
	}

	public double getEnergy() {
		return energy;
	}

	public double getDissimilarity() {
		return dissimilarity;
	}

	public double getCorrelation() {
		return correlation;
	}
	
}

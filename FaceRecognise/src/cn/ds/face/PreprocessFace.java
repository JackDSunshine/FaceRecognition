/**
 * Project             :FaceRecognise project
 * Comments            :������õ�������
 * Version             :1.0
 * Modification history: number | time |   why  |  who
 * 1 | 2013-3-20 | ���� | jxm 
 * 2 | 2013-4-26 |�Ż����룬����ע���� |jxm 
 */
package cn.ds.face;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

import java.util.Vector;

import com.googlecode.javacv.cpp.opencv_core.CvRect;

import cn.ds.face.DetectObject;

public class PreprocessFace {

	private final double DESIRED_LEFT_EYE_X = 0.16;
	private final double DESIRED_LEFT_EYE_Y = 0.14;
	private final double FACE_ELLIPSE_CY = 0.40;
	private final double FACE_ELLIPSE_W = 0.52;//0.5;
	private final double FACE_ELLIPSE_H = 0.8;//0.80;

	private double widthScale = 1.3;
	private double heightScale = 1.1;

	final float EYE_SX = 0.16f;
	final float EYE_SY = 0.26f;
	final float EYE_SW = 0.30f;
	final float EYE_SH = 0.28f;


	private DetectObject detectObject;
	
	private CvMat topLeftOfFace = null;
	private CvMat topRightOfFace = null;
	
	private CvMat leftSide = null;
	private CvMat rightSide = null;
	private CvMat wholeFace = null;
	
	private CvMat rot_mat = null;
	private CvMat warped = null;
	private CvMat filtered = null;
	private CvMat mask = null;
	

	public PreprocessFace() {
		detectObject = new DetectObject();
	}

	/**
	 * Description :��������е�˫�۵�λ�ã����ڶ��������д���ȡ��ͷ���ȶ��ಿ�֣����ʶ�𾫶�
	 * 
	 * @param face
	 *            :������������
	 * @param eyeCascade1
	 *            ������������1�������۾�
	 * @param eyeCascade2
	 *            ������������1�����۾�
	 * @param leftEye
	 *            ��������������
	 * @param rightEye
	 *            ��������������
	 * @return void
	 */
	public void detectBothEyes(CvMat face, CascadeClassifier eyeCascade1,
			CascadeClassifier eyeCascade2, CvPoint leftEye, CvPoint rightEye) {

		/**
		 * ��������Ϊ�����������֣�������������۾�������λ��
		 */

		int leftX = Math.round(face.cols() * EYE_SX*0.625f);
		int topY = Math.round(face.rows() * EYE_SY);
		int widthX = Math.round(face.cols() * EYE_SW);
		int heightY = Math.round(face.rows() * EYE_SH);
		int rightX = (int) Math.round(face.cols() * (1.0 - EYE_SX - EYE_SW));

		// �������������������Ŀռ�
			topLeftOfFace = new CvMat();	
		
			topRightOfFace = new CvMat();	

		cvGetSubArr(face, topLeftOfFace, cvRect(leftX, topY, widthX, heightY));
		cvGetSubArr(face, topRightOfFace, cvRect(rightX, topY, widthX, heightY));

		// ������ż����۾��Ŀռ�
		CvRect leftEyeRect = new CvRect();
		CvRect rightEyeRect = new CvRect();

		// eyeCascade1�������������
		detectObject.detectLargestObject(topLeftOfFace, eyeCascade1,
				leftEyeRect, topLeftOfFace.cols());

		// eyeCascade1�������������
		detectObject.detectLargestObject(topRightOfFace, eyeCascade1,
				rightEyeRect, topRightOfFace.cols());

		// ��eyeCascade1��û�м�������������eyeCascade2�ټ��
		if (leftEyeRect.width() <= 0 && !eyeCascade2.empty()) {

			detectObject.detectLargestObject(topLeftOfFace, eyeCascade2,
					leftEyeRect, topLeftOfFace.cols());
		}

		// ��eyeCascade1��û�м�������������eyeCascade2�ټ��
		if (rightEyeRect.width() <= 0 && !eyeCascade2.empty()) {

			detectObject.detectLargestObject(topRightOfFace, eyeCascade2,
					rightEyeRect, topRightOfFace.cols());
		}

		// �������������򣬽�����洢��leftEyeRect�У�����洢����ԭͼ����Ϣ��������Ҫת���ָ�
		if (leftEyeRect.width() > 0) {
			leftEyeRect.x(leftEyeRect.x() + leftX);
			leftEyeRect.y(leftEyeRect.y() + topY);
			leftEye.x(leftEyeRect.x() + leftEyeRect.width() / 2);
			leftEye.y(leftEyeRect.y() + leftEyeRect.height() / 2);
		} else {
			// û�м������۵����򣬽������-1
			leftEye.x(-1);
			leftEye.y(-1);
		}

		// �������������򣬽�����洢��leftEyeRect�У�����洢����ԭͼ����Ϣ��������Ҫת���ָ�
		if (rightEyeRect.width() > 0) {
			rightEyeRect.x(rightEyeRect.x() + rightX);
			rightEyeRect.y(rightEyeRect.y() + topY);
			rightEye.x(rightEyeRect.x() + rightEyeRect.width() / 2);
			rightEye.y(rightEyeRect.y() + rightEyeRect.height() / 2);
		} else {
			// û�м������۵����򣬽������-1
			rightEye.x(-1);
			rightEye.y(-1);
		}

//		if(topLeftOfFace != null){
//			cvReleaseMat(topLeftOfFace);
//		}
//		if(topRightOfFace != null){
//			cvReleaseMat(topRightOfFace);
//		}
	}

	/**
	 * Description :ʹ�Ҷ�ͼ��ֱ��ͼ���⻯��ȡ�����ߵĸ���
	 * 
	 * @param face
	 *            :������������
	 * @return void
	 */
	public void equalizeLeftAndRightHalves(CvMat faceImg) {

		int w = faceImg.cols();
		int h = faceImg.rows();

		// 1) ȫ������
		wholeFace = cvCreateMat(faceImg.rows(), faceImg.cols(),
					faceImg.type());	
		cvEqualizeHist(faceImg, wholeFace);

		// 2) ����������
		int midX = w / 2;
		
			leftSide = new CvMat();	
			rightSide = new CvMat();	


		cvGetSubArr(faceImg, leftSide, cvRect(0, 0, midX, h));
		cvGetSubArr(faceImg, rightSide, cvRect(midX, 0, w - midX, h));
		cvEqualizeHist(leftSide, leftSide);
		cvEqualizeHist(rightSide, rightSide);

		// 3) ���ߺ��Ұ�ߺ�ȫ��һ���Ա�����һ��ƽ���Ĺ��ɡ�
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int v;
				if (x < w / 4) {
					v = (int) leftSide.get(y, x);
				} else if (x < w * 2 / 4) {
					int lv = (int) leftSide.get(y, x);
					int wv = (int) wholeFace.get(y, x);

					float f = (x - w * 1 / 4) / (float) (w * 0.25f);
					v = Math.round((1.0f - f) * lv + (f) * wv);
				} else if (x < w * 3 / 4) {
					int rv = (int) rightSide.get(y, x - midX);
					int wv = (int) wholeFace.get(y, x);

					float f = (x - w * 2 / 4) / (float) (w * 0.25f);
					v = Math.round((1.0f - f) * wv + (f) * rv);
				} else {
					v = (int) rightSide.get(y, x - midX);
				}
				faceImg.put(y, x, v);
			}
		}

//		if (leftSide != null)
//			cvReleaseMat(leftSide);
//		if (rightSide != null)
//			cvReleaseMat(rightSide);
//		if (wholeFace != null)
//			cvReleaseMat(wholeFace);
	}

	/**
	 * Description :�õ�������ͼƬ��������
	 * 
	 * @param srcImg
	 *            :ԭͼƬ
	 * @param desiredFaceWidth
	 *            :����������ͼƬ���
	 * @param faceCascade
	 *            :����������
	 * @param eyeCascade1
	 *            :�۾���������û�۾���
	 * @param eyeCascade2
	 *            :�۾������������۾���
	 * @param doLeftAndRightSeparately
	 *            :��������
	 * @param storeFaceRect
	 *            :�洢��������
	 * @param storeLeftEye
	 *            :�洢��������λ��
	 * @param storeRightEye
	 *            :�洢��������λ��
	 * @return CvMat :����������
	 */
	public CvMat getPreprocessedFace(CvMat srcImg, int desiredFaceWidth,int scaleWidth,
			CascadeClassifier faceCascade, CascadeClassifier eyeCascade1,
			CascadeClassifier eyeCascade2, boolean doLeftAndRightSeparately,
			CvRect storeFaceRect, CvPoint storeLeftEye, CvPoint storeRightEye) {

		int desiredFaceHeight = desiredFaceWidth;

		if (storeFaceRect != null)
			storeFaceRect.width(-1);
		if (storeLeftEye != null)
			storeLeftEye.x(-1);
		if (storeRightEye != null)
			storeRightEye.x(-1);

		// �������������
		CvRect faceRect = new CvRect();
		detectObject.detectLargestObject(srcImg, faceCascade, faceRect,
				scaleWidth);

		if (faceRect.width() > 0) {// ��⵽����

			// �洢��������
			if (storeFaceRect != null) {
				storeFaceRect.x(faceRect.x());
				storeFaceRect.y(faceRect.y());
				storeFaceRect.width(faceRect.width());
				storeFaceRect.height(faceRect.height());
			}

			// �õ���⵽������ͼƬ
			CvMat faceImg = new CvMat();
			cvGetSubArr(srcImg, faceImg, faceRect);

			// ת��Ϊ�Ҷ�ͼƬ
			CvMat gray = convertToGreyscale(faceImg);

			// �����۾�����λ��
			CvPoint leftEye = new CvPoint();
			CvPoint rightEye = new CvPoint();
			detectBothEyes(gray, eyeCascade1, eyeCascade2, leftEye, rightEye);

			// �洢����λ��
			if (storeLeftEye != null) {
				storeLeftEye.x(leftEye.x());
				storeLeftEye.y(leftEye.y());
			}

			// �洢����λ��
			if (storeRightEye != null) {
				storeRightEye.x(rightEye.x());
				storeRightEye.y(rightEye.y());
			}

			if (leftEye.x() >= 0 && rightEye.x() >= 0) {// ����ȫ����⵽�����Դ���
				CvMat dstImg = process(gray, leftEye, rightEye,
						desiredFaceWidth, desiredFaceHeight,
						doLeftAndRightSeparately);
				
				cvEqualizeHist(dstImg, dstImg);
				return dstImg;

			}

		}
		return null;
	}

	/**
	 * Description :�õ�����ͼƬ��������
	 * 
	 * @param srcImg
	 *            :ԭͼƬ
	 * @param desiredFaceWidth
	 *            :����������ͼƬ���
	 * @param faceCascade
	 *            :����������
	 * @param eyeCascade1
	 *            :�۾���������û�۾���
	 * @param eyeCascade2
	 *            :�۾������������۾���
	 * @param doLeftAndRightSeparately
	 *            :��������
	 * @param storeFaceRect
	 *            :�洢��������
	 * @param storeLeftEye
	 *            :�洢��������λ��
	 * @param storeRightEye
	 *            :�洢��������λ��
	 * @return Vector<CvMat> :�����Ķ�������ͼƬ
	 */
	public Vector<CvMat> getManyPreprocessedFaces(CvMat srcImg,
			int desiredFaceWidth,int scaleWidth, CascadeClassifier faceCascade,
			CascadeClassifier eyeCascade1, CascadeClassifier eyeCascade2,
			boolean doLeftAndRightSeparately, CvRect storeFaceRect,
			CvPoint storeLeftEye, CvPoint storeRightEye) {
		int desiredFaceHeight = desiredFaceWidth;

		if (storeFaceRect != null)
			storeFaceRect.width(-1);
		if (storeLeftEye != null)
			storeLeftEye.x(-1);
		if (storeRightEye != null)
			storeRightEye.x(-1);

		// ���Ҷ������
		CvRect facesRect = new CvRect();
		detectObject.detectManyObjects(srcImg, faceCascade, facesRect,
				scaleWidth);

		Vector<CvMat> dstFaces = new Vector<CvMat>();

		CvMat dstImg = null;
		for (int i = 0; i < facesRect.capacity(); i++) {

			CvRect faceRect = facesRect.position(i);

			if (faceRect.width() > 0) {// ��⵽������

				// �洢��������
				if (storeFaceRect != null) {
					storeFaceRect.position(i).x(facesRect.x());
					storeFaceRect.position(i).y(facesRect.y());
					storeFaceRect.position(i).width(facesRect.width());
					storeFaceRect.position(i).height(facesRect.height());
				}

				// �õ�����ͼƬ
				CvMat faceImg = new CvMat();
				cvGetSubArr(srcImg, faceImg, faceRect);

				// ת��Ϊ�Ҷ�ͼƬ
				CvMat gray = convertToGreyscale(faceImg);

				// ���������۾�
				CvPoint leftEye = new CvPoint();
				CvPoint rightEye = new CvPoint();
				detectBothEyes(gray, eyeCascade1, eyeCascade2, leftEye,
						rightEye);

				// �洢������������
				if (storeLeftEye != null) {
					storeLeftEye.position(i).x(leftEye.x());
					storeLeftEye.position(i).y(leftEye.y());
				}

				// �洢������������
				if (storeRightEye != null) {
					storeRightEye.position(i).x(rightEye.x());
					storeRightEye.position(i).y(rightEye.y());
				}

				// ˫��ȫ����⵽�����Խ��д���
				if (leftEye.x() >= 0 && rightEye.x() >= 0) {
					dstImg = process(gray, leftEye, rightEye, desiredFaceWidth,
							desiredFaceHeight, doLeftAndRightSeparately);

					// ���������������ӵ�Vector��
					dstFaces.add(dstImg);
				}
			}

		}
		return dstFaces;
	}

	/**
	 * Description :���ü��˫��������ͼƬ���÷����е���������λ����ͨ������õ���
	 * 
	 * @param srcImg
	 *            :ԭͼƬ
	 * @param desiredFaceWidth
	 *            :����������ͼƬ���
	 * @param faceCascade
	 *            :����������
	 * @param doLeftAndRightSeparately
	 *            :��������
	 * @return CvMat :����������ͼƬ
	 */
	public CvMat getPreprocessedFaceWithoutDetectEyes(CvMat srcImg,
			int desiredFaceWidth, int scaleWidth,CascadeClassifier faceCascade,
			boolean doLeftAndRightSeparately) {

		int desiredFaceHeight = desiredFaceWidth;

		// ������������
		CvRect faceRect = new CvRect();
		detectObject.detectLargestObject(srcImg, faceCascade, faceRect,
				scaleWidth);

		if (faceRect.width() > 0) {// �ҵ�����

			// ����detectLargestObject�����ҵõ������������������Ҫ��С������
			//CvRect smallFaceRect = getSmallFaceRect(faceRect);

			// �õ�����ͼƬ
			//CvMat faceImg = new CvMat();
			//cvGetSubArr(srcImg, faceImg, smallFaceRect);
			CvMat faceImg = new CvMat();
			cvGetSubArr(srcImg, faceImg, faceRect);
			// �ҶȻ�����
			//CvMat gray = convertToGreyscale(faceImg);
			
			CvMat gray = convertToGreyscale(faceImg);

			CvPoint leftEye = new CvPoint(), rightEye = new CvPoint();

			// ����˫�۵�����λ��
			calculateEyeCenter(gray, leftEye, rightEye);

			// ��������
			CvMat dstImg = process(gray, leftEye, rightEye, desiredFaceWidth,
					desiredFaceHeight, doLeftAndRightSeparately);

			return dstImg;
		}
		return null;
	}

	/**
	 * Description :���ü��˫��������ͼƬ���÷����е���������λ����ͨ������õ��ģ��õ��������
	 * 
	 * @param srcImg
	 *            :ԭͼƬ
	 * @param desiredFaceWidth
	 *            :����������ͼƬ���
	 * @param faceCascade
	 *            :����������
	 * @param doLeftAndRightSeparately
	 *            :��������
	 * @return CvMat :����������ͼƬ
	 */
	public Vector<CvMat> getManyPreprocessedFacesWithoutDetectEyes(
			CvMat srcImg, int desiredFaceWidth,int scaleWidth,CascadeClassifier faceCascade,
			boolean doLeftAndRightSeparately) {

		int desiredFaceHeight = desiredFaceWidth;

		// �õ���������
		CvRect facesRect = new CvRect();
		detectObject.detectManyObjects(srcImg, faceCascade, facesRect,
				scaleWidth);

		Vector<CvMat> dstFaces = new Vector<CvMat>();
		CvMat dstImg = null;

		for (int i = 0; i < facesRect.capacity(); i++) {

			if (facesRect.position(i).width() > 0) {

				CvRect faceRect = facesRect.position(i);

				// ����detectLargestObject�����ҵõ������������������Ҫ��С������
				//CvRect smallFaceRect = getSmallFaceRect(faceRect);

				// �õ�����ͼƬ
				CvMat faceImg = new CvMat();
				cvGetSubArr(srcImg, faceImg, faceRect);

				// �ҶȻ�
				CvMat gray = convertToGreyscale(faceImg);

				// �����۾�����λ��
				CvPoint leftEye = new CvPoint(), rightEye = new CvPoint();
				calculateEyeCenter(gray, leftEye, rightEye);

				dstImg = process(gray, leftEye, rightEye, desiredFaceWidth,
						desiredFaceHeight, doLeftAndRightSeparately);
				dstFaces.add(i, dstImg);
			}

		}
		return dstFaces;
	}

	/**
	 * Description :�õ�����Ҫ���������ͼƬ
	 * 
	 * @param srcImg
	 *            :ԭͼƬ
	 * @param desiredFaceWidth
	 *            :����������ͼƬ���
	 * @param faceCascade
	 *            :����������
	 * @return CvMat :�õ�����ͼƬ
	 */
	public CvMat getFaceWithoutProcess(CvMat srcImg, int desiredFaceWidth,int scaleWidth,
			CascadeClassifier faceCascade) {

		int desiredFaceHeight = desiredFaceWidth;
		// �����������
		CvRect faceRect = new CvRect();
		detectObject.detectLargestObject(srcImg, faceCascade, faceRect,
				scaleWidth);

		if (faceRect.width() > 0) {// ��⵽����

			
			// ��СͼƬ
			CvRect smallFaceRect = getSmallFaceRect(faceRect);
			

			// �õ�����ͼƬ
			CvMat faceImg = new CvMat();
			cvGetSubArr(srcImg, faceImg, smallFaceRect);

			// �ҶȻ�ͼƬ
			CvMat gray = convertToGreyscale(faceImg);

			// �ı�ͼƬ�ߴ�
			CvMat dstImg = cvCreateMat(desiredFaceHeight, desiredFaceWidth,
					gray.type());
			cvResize(gray, dstImg);

			cvEqualizeHist(dstImg, dstImg);

			return dstImg;
		}
		return null;
	}

	/**
	 * Description :�õ�����Ҫ���������ͼƬ,����
	 * 
	 * @param srcImg
	 *            :ԭͼƬ
	 * @param desiredFaceWidth
	 *            :����������ͼƬ���
	 * @param faceCascade
	 *            :����������
	 * @return Vector<CvMat> :�õ�����ͼƬ������
	 */
	public Vector<CvMat> getManyFacesWithoutProcess(CvMat srcImg,
			int desiredFaceWidth, int scaleWidth,CascadeClassifier faceCascade) {
		int desiredFaceHeight = desiredFaceWidth;

		// �����������
		CvRect facesRect = new CvRect();
		detectObject.detectManyObjects(srcImg, faceCascade, facesRect,
				scaleWidth);

		Vector<CvMat> dstFaces = new Vector<CvMat>();
		for (int i = 0; i < facesRect.capacity(); i++) {

			if (facesRect.position(i).width() > 0) {

				CvRect faceRect = facesRect.position(i);

				CvRect smallFaceRect = getSmallFaceRect(faceRect);

				// �õ�����ͼƬ
				CvMat faceImg = new CvMat();
				cvGetSubArr(srcImg, faceImg, smallFaceRect);

				// �ҶȻ�
				CvMat gray = convertToGreyscale(faceImg);

				// �ı�ͼƬ�ߴ�
				CvMat dstImg = cvCreateMat(desiredFaceHeight, desiredFaceWidth,
						gray.type());
				cvResize(gray, dstImg);

				cvEqualizeHist(dstImg, dstImg);

				dstFaces.add(i, dstImg);
			}

		}
		return dstFaces;
	}

	/**
	 * Description :����ͼƬ
	 * 
	 * @param gray
	 *            :�Ҷ�����ͼƬ
	 * @param leftEye
	 *            :��������λ��
	 * @param rightEye
	 *            :��������λ��
	 * @param desiredFaceWidth
	 *            :����������ͼƬ���
	 * @param desiredFaceHeight
	 *            :���������ĸ߶�
	 * @param doLeftAndRightSeparately
	 *            :�Ƿ�����������
	 * @return CvMat :����������ͼƬ
	 */
	public CvMat process(CvMat gray, CvPoint leftEye, CvPoint rightEye,
			int desiredFaceWidth, int desiredFaceHeight,
			boolean doLeftAndRightSeparately) {

		CvPoint2D32f eyesCenter = new CvPoint2D32f(
				(leftEye.x() + rightEye.x()) * 0.5f,
				(leftEye.y() + rightEye.y()) * 0.5f);
		// Get the angle between the 2 eyes.
		double dy = (rightEye.y() - leftEye.y());
		double dx = (rightEye.x() - leftEye.x());
		double len = Math.sqrt(dx * dx + dy * dy);
		double angle = Math.atan2(dy, dx) * 180.0 / Math.PI;

		// Hand measurements shown that the left eye center should
		// ideally be at roughly (0.19, 0.14) of a scaled face image.
		double DESIRED_RIGHT_EYE_X = (1.0f - DESIRED_LEFT_EYE_X);
		// Get the amount we need to scale the image to be the desired
		// fixed size we want.
		double desiredLen = (DESIRED_RIGHT_EYE_X - DESIRED_LEFT_EYE_X)
				* desiredFaceWidth;
		double scale = desiredLen / len;
		// Get the transformation matrix for rotating and scaling the
		// face to the desired angle & size.

		rot_mat = cvCreateMat(2, 3, CV_32F);	

		cv2DRotationMatrix(eyesCenter, angle, scale, rot_mat);
		// Shift the center of the eyes to be the desired center between
		// the eyes.
		rot_mat.put(0, 2, rot_mat.get(0, 2) + desiredFaceWidth * 0.5f
				- eyesCenter.x());
		rot_mat.put(1, 2, rot_mat.get(1, 2) + desiredFaceHeight
				* DESIRED_LEFT_EYE_Y - eyesCenter.y());

		warped = cvCreateMat(desiredFaceHeight, desiredFaceWidth, CV_8U);	

		// Mat warped = Mat(desiredFaceHeight, desiredFaceWidth, CV_8U,
		// cvScalar(128)); // Clear the output image to a default grey.
		cvWarpAffine(gray, warped, rot_mat);

		// Give the image a standard brightness and contrast, in case it
		// was too dark or had low contrast.
		if (!doLeftAndRightSeparately) {
			// Do it on the whole face.
			cvEqualizeHist(warped, warped);
		} else {
			// Do it seperately for the left and right sides of the
			// face.
			equalizeLeftAndRightHalves(warped);
		}

			filtered = cvCreateMat(warped.rows(), warped.cols(), CV_8U);	

		bilateralFilter(warped, filtered, 0, 20.0, 2.0, BORDER_DEFAULT);

		mask = cvCreateMat(warped.rows(), warped.cols(), CV_8U);	
		
		cvSet(mask, CV_RGB(0, 0, 0));
		// Mat mask = Mat(warped.size(), CV_8U, Scalar(0)); // Start
		// with an empty mask.
		CvPoint faceCenter = cvPoint(desiredFaceWidth / 2,
				(int) Math.round(desiredFaceHeight * FACE_ELLIPSE_CY));
		CvSize size = cvSize(
				(int) Math.round(desiredFaceWidth * FACE_ELLIPSE_W),
				(int) Math.round(desiredFaceHeight * FACE_ELLIPSE_H));
		cvEllipse(mask, faceCenter, size, 0, 0, 360, CV_RGB(255, 255, 255),
				CV_FILLED, 8, 0);
		// imshow("mask", mask);
		// Use the mask, to remove outside pixels.
		CvMat dstImg = cvCreateMat(warped.rows(), warped.cols(), CV_8U);
		cvSet(dstImg, CV_RGB(128, 128, 128));

		cvCopy(filtered, dstImg, mask);
		
		return dstImg;
	}

	/**
	 * Description :��ͼƬת��Ϊ�Ҷ�ͼ
	 * 
	 * @param src
	 *            : ��ת��ͼƬ
	 * @return CvMat :�����ĻҶ�ͼƬ
	 */
	public CvMat convertToGreyscale(CvMat src) {
		CvMat gray = cvCreateMat(src.rows(), src.cols(), CV_8U);
		if (src.channels() == 3) {
			cvCvtColor(src, gray, CV_BGR2GRAY);
		} else if (src.channels() == 4) {
			cvCvtColor(src, gray, CV_BGRA2GRAY);
		} else {
			cvCopy(src, gray);
		}
		return gray;
	}

	/**
	 * Description :����⵽������������С���������������Ĳ���
	 * 
	 * @param faceRect
	 *            : ����С����������
	 * @return CvRect :��С�����������
	 */
	public CvRect getSmallFaceRect(CvRect faceRect) {
		int w = (int) Math.round(faceRect.width() / widthScale);
		int h = (int) Math.round(faceRect.height() / heightScale);

		int x = faceRect.x() + (int) Math.round((faceRect.width() - w) * 0.5);
		int y = faceRect.y() + (int) Math.round((faceRect.height() - h) * 0.5);
		CvRect smallFaceRect = new CvRect(x, y, w, h);
		return smallFaceRect;
	}

	/**
	 * Description :�����۾�����
	 * 
	 * @param face
	 *            : ����ͼƬ
	 * @param leftEye
	 *            :�洢�������������λ��
	 * @param rightEye
	 *            :�洢�������������λ��
	 * @return void
	 */
	public void calculateEyeCenter(CvMat face, CvPoint leftEye, CvPoint rightEye) {
		int leftX = Math.round(face.cols() * EYE_SX);
		int topY = Math.round(face.rows() * EYE_SY);
		int widthX = Math.round(face.cols() * EYE_SW);
		int heightY = Math.round(face.rows() * EYE_SH);
		int rightX = (int) Math.round(face.cols() * (1.0 - EYE_SX - EYE_SW));

		if (leftEye == null) {
			leftEye = cvPoint(leftX + widthX / 2, topY + heightY / 2);
		} else {
			leftEye.x(leftX + widthX / 2);
			leftEye.y(topY + heightY / 2);
		}

		if (rightEye == null) {
			rightEye = cvPoint(rightX + widthX / 2, topY + heightY / 2);
		} else {
			rightEye.x(rightX + widthX / 2);
			rightEye.y(topY + heightY / 2);
		}

	}

}

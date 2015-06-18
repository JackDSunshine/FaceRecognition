/**
 * Project             :FaceRecognise project
 * Comments            :���ͼƬ���������
 * Version             :1.0
 * Modification history: number | time |   why  |  who
 * 1 | 2013-3-20 | ���� | jxm 
 * 2 | 2013-4-26 |�Ż����룬����ע���� |jxm 
 */
package cn.ds.face;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

public class DetectObject {

	private CvMat gray = null;
	private CvMat inputImg = null;

	/**
	 * Description :�Զ���ļ�ⷽ����ͨ�����������Ƽ��
	 * 
	 * @param img
	 *            :ͼƬ
	 * @param cascade
	 *            ������������
	 * @param objects
	 *            ���洢�����
	 * @param scaledWidth
	 *            ����������ͼƬ�Ŀ��
	 * @param flags
	 *            ��ָ������ģʽ
	 * @param minFeatureSize
	 *            ����С���ߴ�
	 * @param searchScaleFactor
	 *            ����ǰ��������̵�ɨ���У��������ڵı���ϵ��
	 * @param minNeighbors
	 *            �����ɼ��Ŀ������ھ��ε���С��
	 * @return void
	 */
	public void detectObjectsCustom(CvMat img, CascadeClassifier cascade,
			CvRect objects, int scaledWidth, int flags, CvSize minFeatureSize,
			float searchScaleFactor, int minNeighbors) {

		gray = cvCreateMat(img.rows(), img.cols(), CV_8U);

		if (img.channels() == 3) {// ���ͼƬ����gray�Ľ���ת��Ϊgray
			cvCvtColor(img, gray, CV_BGR2GRAY);
		} else {// ���ͼƬ��gray�ģ�ֱ�ӿ�����gray
			cvCopy(img, gray);
		}

		// ����ͼƬԭ���Ŀ�߱�ѹ��ͼƬ�����ڿ��ټ��
		// �õ���߱�
		float scale = img.cols() / (float) scaledWidth;
		if (img.cols() > scaledWidth) {// ��������ͼƬ�Ŀ����������ȣ�˵����Ҫѹ��
			int scaledHeight = Math.round(img.rows() / scale);
			inputImg = cvCreateMat(scaledHeight, scaledWidth, gray.type());
			cvResize(gray, inputImg, CV_INTER_LINEAR);
		} else {// ��������ͼƬ�Ŀ�С��������ȣ�˵��ͼƬ�Ѿ���С������Ҫѹ��
			inputImg = cvCreateMat(gray.rows(), gray.cols(), gray.type());
			cvCopy(gray, inputImg);
		}

		// ��ͼƬ�м������������洢��objects��
		cascade.detectMultiScale(inputImg, objects, searchScaleFactor,
				minNeighbors, flags, minFeatureSize, cvSize(0, 0));

		// ����֮ǰѹ����ͼƬ���������Ľ������ԭͼ�ϵ�������Ϣ��������Ҫ����ԭͼ��
		if (img.cols() > scaledWidth) {
			for (int i = 0; i < (int) objects.capacity(); i++) {
				objects.position(i).x(
						Math.round(objects.position(i).x() * scale));
				objects.position(i).y(
						Math.round(objects.position(i).y() * scale));
				objects.position(i).width(
						Math.round(objects.position(i).width() * scale));
				objects.position(i).height(
						Math.round(objects.position(i).height() * scale));
			}
		}

		// ȷ���ָ����������겻�ᳬ��
		for (int i = 0; i < (int) objects.capacity(); i++) {
			if (objects.position(i).x() < 0)
				objects.position(i).x(0);
			if (objects.position(i).y() < 0)
				objects.position(i).y(0);
			if (objects.position(i).x() + objects.position(i).width() > img
					.cols())
				objects.position(i).x(img.cols() - objects.position(i).width());
			if (objects.position(i).y() + objects.position(i).height() > img
					.rows())
				objects.position(i)
						.y(img.rows() - objects.position(i).height());
		}

		if (gray != null) {
			cvReleaseMat(gray);
		}
		if (inputImg != null) {
			cvReleaseMat(inputImg);
		}
	}

	/**
	 * Description :�õ�ͼƬ��������������
	 * 
	 * @param img
	 *            :ͼƬ
	 * @param cascade
	 *            ������������
	 * @param largestObject
	 *            ���洢�����
	 * @param scaledWidth
	 *            ����������ͼƬ�Ŀ��
	 * @return void
	 */
	public void detectLargestObject(CvMat img, CascadeClassifier cascade,
			CvRect largestObject, int scaledWidth) {

		// flag = 4��������ģʽ
		int flags = CV_HAAR_FIND_BIGGEST_OBJECT;

		// ��С�������Ϊ20x20
		CvSize minFeatureSize = cvSize(20, 20);

		// �����������Ϊ1.1��ָ������������������10%
		float searchScaleFactor = 1.1f;

		int minNeighbors = 4;

		// ����detectObjectsCustom������������������
		CvRect objects = new CvRect();
		detectObjectsCustom(img, cascade, objects, scaledWidth, flags,
				minFeatureSize, searchScaleFactor, minNeighbors);

		if (objects.capacity() > 0) {// ��⵽����

			// �����������Ϣ�洢
			largestObject.x(objects.position(0).x());
			largestObject.y(objects.position(0).y());
			largestObject.width(objects.position(0).width());
			largestObject.height(objects.position(0).height());
		} else {
			// û��⵽
			largestObject.x(-1);
			largestObject.y(-1);
			largestObject.width(-1);
			largestObject.height(-1);
		}
	}

	/**
	 * Description :�õ�ͼƬ�еĶ�������У���������
	 * 
	 * @param img
	 *            :ͼƬ
	 * @param cascade
	 *            ������������
	 * @param objects
	 *            ���洢�����
	 * @param scaledWidth
	 *            ����������ͼƬ�Ŀ��
	 * @return void
	 */
	public void detectManyObjects(CvMat img, CascadeClassifier cascade,
			CvRect objects, int scaledWidth) {

		// �������ŷ���������⣬��������ͼ��
		int flags = CV_HAAR_SCALE_IMAGE;

		// ��С�ߴ�
		CvSize minFeatureSize = cvSize(20, 20);

		// �����������Ϊ1.1��ָ������������������10%
		float searchScaleFactor = 1.1f;

		int minNeighbors = 4;

		// ����detectObjectsCustom�������
		detectObjectsCustom(img, cascade, objects, scaledWidth, flags,
				minFeatureSize, searchScaleFactor, minNeighbors);
	}
}

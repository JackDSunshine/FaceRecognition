/**
 * Project             :FaceRecognise project
 * Comments            :��װ����Ƶ����ʶ��Ľ�������
 * Version             :1.0
 * Modification history: number | time |   why  |  who
 * 1 | 2013-5-20 | ���� | jxm 
 */
package cn.ds.domain;

public class Progress {
private double totalSize;
private double processedSize;

public Progress(double totalSize, double processedSize) {
	super();
	this.totalSize = totalSize;
	this.processedSize = processedSize;
}
public int getStandardValue(int stand){
	return (int)((processedSize * stand) / totalSize);
}
public double getTotalSize() {
	return totalSize;
}
public void setTotalSize(double totalSize) {
	this.totalSize = totalSize;
}
public double getProcessedSize() {
	return processedSize;
}
public void setProcessedSize(double processedSize) {
	this.processedSize = processedSize;
}
}

/**
 * Project             :FaceRecognise project
 * Comments            :�����
 * Version             :1.0
 * Modification history: number | time |   why  |  who
 * 1 | 2013-4-20 | ���� | jxm 
 */
package cn.ds.control;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;


public class Index {
public static void main(String[] args) {
	try {
		UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceSaharaLookAndFeel");//����org.jvnet.substance.skinΪ������SubstanceSaharaLookAndFeelΪƤ������
		//JFrame.setDefaultLookAndFeelDecorated(true);
		//JDialog.setDefaultLookAndFeelDecorated(true);
		} catch (Exception ex) {
		ex.printStackTrace();
		}
	new LoginAction();
}
}

/**
 * Project             :FaceRecognise project
 * Comments            :�����û������࣬
 * Version             :1.0
 * Modification history: number | time |   why  |  who
 * 1 | 2013-4-20 | ���� | jxm 
 */
package cn.ds.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import cn.ds.domain.User;
import cn.ds.model.UpdateUserModel;
import cn.ds.service.RecogniseService;
import cn.ds.view.UpdateUserView;

public class UpdateUserAction implements ActionListener {

	// �޸��û���Ϣ������
	private UpdateUserView updateUserView;
	// �޸��û���Ϣģ��
	private UpdateUserModel updateUserModel;
	// Jtableģ��
	private DefaultTableModel model;
	// �к�
	private int row;
	// �޸��û�
	private User user;

	/**
	 * Description :���캯��
	 * 
	 * @param id
	 *            :�޸��û�ID
	 * @param row
	 *            �����û������к�
	 * @param model
	 *            ��Jtable ģ��
	 * @param mode
	 *            : �޸�ģʽ
	 * @return UpdateUserAction
	 */
	public UpdateUserAction(int id, int row, DefaultTableModel model, int mode) {
		updateUserModel = new UpdateUserModel();

		// ����ID�Ż���û�
		user = updateUserModel.getUserById(id);

		this.model = model;
		this.row = row;

		updateUserView = new UpdateUserView(this, user);

		if (mode != 0) {// ��ͨ�û������޸���Ƭ
			updateUserView.getButtonEntryPhotos().setEnabled(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new UpdateResponseThread(e.getActionCommand()).start();
	}

	/**
	 * Description :�ڲ��࣬����ť��Ӧ
	 */
	class UpdateResponseThread extends Thread {
		private String actionCommand;

		public UpdateResponseThread(String actionCommand) {
			this.actionCommand = actionCommand;
		}

		@Override
		public void run() {

			if (actionCommand.equals("buttonEntryPhotos")) {// ¼����Ƭ��ť��Ӧ
				updateUserView.getLabelShowResult().setText("����¼����Ƭ��");
				BlockingQueue<Boolean> result = new LinkedBlockingQueue<Boolean>();

				// ����Ϊ¼����Ƭģʽ
				updateUserModel.entryPhotos(result);

				try {
					result.take();
					user.setHavePhoto("��");

					updateUserView.getLblelEntryPhoto().setText("��");
					updateUserView.getLabelShowResult().setText("������");
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			} else if (actionCommand.equals("buttonSure")) {// ȷ����ť��Ӧ
				user.setAccount(updateUserView.getTextFieldAccount().getText());
				user.setPwd(new String(updateUserView.getPasswordField()
						.getPassword()));
				user.setName(updateUserView.getTextFieldName().getText());
				if (updateUserView.getRadioButton().isSelected()) {
					user.setSex("��");
				} else {
					user.setSex("Ů");
				}

				user.setDepartment(updateUserView.getTextFieldDepartment()
						.getText());
				user.setTel(updateUserView.getTextFieldTel().getText());
				user.setPicsPath("pics/");

				updateUserModel.updateUser(user);

				updateUserView.getLabelShowResult().setText("�ɹ�!");

				if (model != null) {// ˢ���б�
					if (SwingUtilities.isEventDispatchThread()) {

					} else {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								updateUserModel.refreshUserTableUpdate(model,
										row, user);
							}
						});
					}

				}
			} else if (actionCommand.equals("buttonReset")) {// ���ð�ť��Ӧ
				reset();
			} else if (actionCommand.equals("buttonQuit")) {// �˳���ť��Ӧ
				if (RecogniseService.getInstance() != null
						&& RecogniseService.getInstance().getState() != State.WAITING) {
					RecogniseService.getInstance(null).waitMode();
				}
				updateUserView.dispose();
			}
		}

	}

	public void reset() {
		updateUserView.getTextFieldAccount().setText("");
		updateUserView.getPasswordField().setText("");
		updateUserView.getTextFieldName().setText("");
		updateUserView.getTextFieldDepartment().setText("");
		updateUserView.getTextFieldTel().setText("");
	}

}

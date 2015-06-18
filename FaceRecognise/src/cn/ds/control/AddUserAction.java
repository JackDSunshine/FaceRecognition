/**
 * Project             :FaceRecognise project
 * Comments            :�����û������࣬
 * Version             :1.0
 * Modification history: number | time |   why  |  who
 * 1 | 2013-4-20 | ���� | jxm 
 * 2 | 2013-4-30 |���ӡ��Ƿ�����Ƭ����һ����ж� |jxm  
 */
package cn.ds.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import cn.ds.domain.User;
import cn.ds.model.AddUserModel;
import cn.ds.service.RecogniseService;
import cn.ds.utils.Page;
import cn.ds.view.AddUserView;
import cn.ds.view.ManageView;

public class AddUserAction implements ActionListener {
	
	//����û�����
	private AddUserView addUserView;
	//����û�ģ��
	private AddUserModel addUserModel;
	//JTableģ��
	private DefaultTableModel model;
	//
	private ManageView manageView;
	//��ʾҳ
	private Page page;
	//�Ƿ���¼����Ƭ��ʶ
	private boolean isEntryPhotos = false;

	/**
	 * Description :���캯��
	 * 
	 * @param model
	 *            :Jtableģ��
	 * @param page
	 *            ����ǰ������ҳ
	 * @return AddUserAction
	 */
	public AddUserAction(ManageView manageView,DefaultTableModel model, Page page) {
		addUserView = new AddUserView(this);

		this.manageView = manageView;
		this.model = model;
		this.page = page;

		addUserModel = new AddUserModel();
	}

	/**
	 * Description :�����û�����İ�ť��Ӧ����
	 * 
	 * @param e
	 *            :��Ӧ�¼�
	 * @return void
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		new AddUserResponseThread(e.getActionCommand()).start();
	}

	/**
	 * Description :�������û��������д��Ϣ����ʾ��Ϣ���
	 * 
	 * @return void
	 */
	public void reset() {
		addUserView.getTextFieldAccount().setText("");
		addUserView.getPasswordField().setText("");
		addUserView.getTextFieldName().setText("");
		addUserView.getTextFieldDepartment().setText("");
		addUserView.getTextFieldTel().setText("");
		addUserView.getLabelShowResult().setText("");
	}

	/**
	 * Description :�ڲ��࣬����ť��Ӧ
	 */
	class AddUserResponseThread extends Thread {
		private String actionCommand;

		public AddUserResponseThread(String actionCommand) {
			this.actionCommand = actionCommand;
		}

		@Override
		public void run() {
			if (actionCommand.equals("buttonEntryPhotos")) {// ¼����Ƭ��ť��Ӧ

				// ��ʾ��ʾ��Ϣ
				addUserView.getLabelShowResult().setText("����¼����Ƭ��");

				// ����RecogniseService¼����Ƭ��result������Ž��
				BlockingQueue<Boolean> result = new LinkedBlockingQueue<Boolean>();
				addUserModel.entryPhotos(result);

				try {
					// ȡ�����
					result.take();

					// ������Ƭ��ʶ��Ϊtrue
					isEntryPhotos = true;

					// ��ʾ��ʾ��Ϣ
					addUserView.getLabelHavePhoto().setText("��");
					addUserView.getLabelShowResult().setText("������");
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			} else if (actionCommand.equals("buttonSure")) {// ȷ����ť��Ӧ

				/*
				 * ��ȡ������û���Ϣ
				 */
				User user = new User();
				user.setId(addUserModel.getAddUserId());
				
				if("".equals(addUserView.getTextFieldAccount().getText())){
					JOptionPane.showMessageDialog(addUserView, "�û�������Ϊ��");	
				}else{
					user.setAccount(addUserView.getTextFieldAccount().getText());
					
					String pwd = new String(addUserView.getPasswordField()
							.getPassword());
					if("".equals(pwd)){
						JOptionPane.showMessageDialog(addUserView, "���벻��Ϊ��");	
					}else{
						user.setPwd(pwd);
						
						if("".equals(addUserView.getTextFieldName().getText())){
							user.setName("δ��д");	
						}else{
							user.setName(addUserView.getTextFieldName().getText());	
						}
						
						if (addUserView.getRadioButton().isSelected()) {
							user.setSex("��");
						} else {
							user.setSex("Ů");
						}

						if (isEntryPhotos) {
							user.setHavePhoto("��");
							isEntryPhotos = false;
						} else {
							user.setHavePhoto("��");
						}
						
						if("".equals(addUserView.getTextFieldDepartment().getText())){
							user.setDepartment("δ��д");	
						}else{
							user.setDepartment(addUserView.getTextFieldDepartment()
									.getText());
						}
						
						if("".equals(addUserView.getTextFieldTel().getText())){
							user.setTel("δ��д");	
						}else{
							user.setTel(addUserView.getTextFieldTel().getText());
						}
						
						user.setPicsPath("pics/");

						// ����û���Ϣ���������ݴ洢���洢�����Ϣ�����ļ��洢���洢��Ƭ��Ϣ��
						addUserModel.addUser(user);

						// ������ʾ��Ϣ
						addUserView.getLabelShowResult().setText("�ɹ���");
					}
				}
				
				
				
				
			} else if (actionCommand.equals("buttonReset")) {// ���ð�ť��Ӧ
				// ����
				reset();
			} else if (actionCommand.equals("buttonQuit")) {// �˳���ť��Ӧ
				if (RecogniseService.getInstance() != null
						&& RecogniseService.getInstance().getState() != State.WAITING) {
					// ��������RecogniseService��������״̬���ǵȴ�״̬����ô�ͽ�������Ϊ�ȴ�״̬
					RecogniseService.getInstance(null).waitMode();
				}

				// ����û�������ʧ
				addUserView.dispose();

				
				//page.setPageNow(1);
				if (page.getNowPageSize() < page.getPageSize()) {
					
					addUserModel.updatePage(page);
					
					if(SwingUtilities.isEventDispatchThread()){
						addUserModel.refreshUserTable(model, page);	
					}else{
						SwingUtilities.invokeLater(new Runnable() {
							
							@Override
							public void run() {
								addUserModel.refreshUserTable(model, page);
							}
						});
					}
					
			}else{
				addUserModel.updatePage(page);	
			}
				manageView.refreshUI();
				
			}

		}
	}
}

/**
 * Project             :FaceRecognise project
 * Comments            :��½������
 * Version             :1.0
 * Modification history: number | time |   why  |  who
 * 1 | 2013-4-20 | ���� | jxm 
 * 2 | 2013-8-16 | ��ӻس�����½���ܣ����ؽ���ʱ���ֶ�̬��ʾЧ�� | jxm 
 */
package cn.ds.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.Thread.State;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cn.ds.domain.User;
import cn.ds.model.LoginModel;
import cn.ds.service.RecogniseService;
import cn.ds.view.LoadingView;
import cn.ds.view.LoginView;

public class LoginAction implements ActionListener, ChangeListener, KeyListener {

	// ��½����
	private LoginView loginView;
	// RecogniseService����
	private RecogniseService recogniseService;
	// ���RecogniseService������
	private BlockingQueue<Integer> recogniseResult = new LinkedBlockingDeque<Integer>();
	// ��½ģ��
	private LoginModel userModel;

	/**
	 * Description :���캯��
	 * 
	 * @return LoginAction
	 */
	public LoginAction() {

		loginView = new LoginView(this);

		// ��дloginView���ڵĹرշ���
		loginView.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (RecogniseService.getInstance() != null) {
					// ����RecogniseService�߳�
					RecogniseService.getInstance().setFlag(false);

					// �ͷ�����ͷcapture��Դ
					RecogniseService.getInstance().releaseCapture();
				}

				System.exit(0);
			}
		});
		userModel = new LoginModel();
	}

	/**
	 * Description :��½����İ�ť��Ӧ����
	 * 
	 * @param e
	 *            :��Ӧ�¼�
	 * @return void
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("buttonLogin")) {// ��½��ť��Ӧ
			// if (this.loginView.getTabbedPane().getSelectedIndex() == 0) {//
			// �û����������½
			//
			// User tempUser = new User();
			// tempUser.setName(this.loginView.getTextFieldAccount().getText());
			// tempUser.setPwd(new String(this.loginView.getPasswordField()
			// .getPassword()));
			//
			// // ����û��Ƿ����
			// boolean result = userModel.checkUserByPwd(tempUser);
			//
			// if (!result) {// �����ڣ�������ʾ�Ի���
			// JOptionPane.showMessageDialog(loginView, "��������˺Ŵ���");
			// } else {// ����
			// loginView.dispose();
			//
			// if (tempUser.getId() == 0) {// ID��Ϊ0������Ա�û�
			//
			// // �ǹ���ģʽ��½������Ϊ1
			// // new ManageAction(1);
			// openManage(1);
			// } else {// ID�Ų�Ϊ0����ͨ�û�
			// // �ǹ���ģʽ��½������Ϊ1
			// new CommonUserAction(tempUser.getId(), 1);
			// }
			// }
			// } else if (this.loginView.getTabbedPane().getSelectedIndex() ==
			// 1)
			// {// ����ʶ���½ģʽ
			//
			// // ��RecogniseService������Ϊ����ʶ��ģʽ
			// RecogniseService.getInstance(recogniseResult).recognitionMode();
			//
			// try {
			// // ȡ�����
			// int r = recogniseResult.take();
			// if (r == 0) {// IDΪ0������Ա
			//
			// // �ǹ���ģʽ��½������Ϊ1
			// // new ManageAction(1);
			// openManage(1);
			//
			// loginView.dispose();
			//
			// RecogniseService.getInstance(null).waitMode();
			// } else if (r == -1) {// �����ڸ��ˣ�������ʾ�Ի���
			// JOptionPane.showMessageDialog(loginView, "δע���û���");
			// } else if (r == -2) {
			// JOptionPane.showMessageDialog(loginView,
			// "��δ������������£�����ʶ���ܲ�����");
			// } else {// ID�ŷ�0����ͨ�û�
			//
			// // �ǹ���ģʽ��½������Ϊ1
			// new CommonUserAction(r, 1);
			//
			// loginView.dispose();
			//
			// RecogniseService.getInstance(null).waitMode();
			// }
			//
			// } catch (Exception e2) {
			// e2.printStackTrace();
			// }
			// } else if (this.loginView.getTabbedPane().getSelectedIndex() ==
			// 2)
			// {// ����ģʽ��½
			//
			// User tempUser = new User();
			// tempUser.setName(this.loginView.getTextFieldMAccount()
			// .getText());
			// tempUser.setPwd(new String(this.loginView.getPasswordFieldM()
			// .getPassword()));
			//
			// // �ȼ���û����������Ƿ���ȷ
			// boolean result = userModel.checkUserByPwd(tempUser);
			//
			// if (result) {// �û�����������ȷ
			//
			// int r = -1;
			//
			// // ����ʶ��
			// RecogniseService.getInstance(recogniseResult)
			// .recognitionMode();
			//
			// try {
			// // ȡ�ý��
			// r = recogniseResult.take();
			// } catch (Exception e2) {
			// e2.printStackTrace();
			// }
			//
			// if (r == tempUser.getId()) {// �û��������������ʶ����һ��
			// if (r == 0) {// ����Ա
			//
			// // ����ģʽ������Ϊ0
			// // new ManageAction(0);
			// openManage(0);
			// } else {// ��ͨ�û�
			//
			// // ����ģʽ������Ϊ0
			// new CommonUserAction(r, 0);
			// }
			//
			// loginView.dispose();
			// RecogniseService.getInstance(null).waitMode();
			// } else if (r == -1) {// ����ʶ��δͨ��
			//
			// if ("��".equals(tempUser.getHavePhoto())) {
			// JOptionPane.showMessageDialog(loginView, "����ʶ��δͨ��");
			// } else if ("��".equals(tempUser.getHavePhoto())) {
			// /**
			// * ��Ϊ����Ա������������뵽����ģʽ����ϵͳ�İ�ȫ�����ɹ���Ա�е���
			// * ���������һ�ν���ϵͳ��������¼����Ƭ
			// */
			//
			// if (tempUser.getId() == 0) {
			// // ��ʾ��Ϣ����ʾ����Ա�����¼����Ƭ
			// JOptionPane.showMessageDialog(loginView,
			// "����Ա��ݣ���¼�������¼����Ƭ��ѵ��");
			//
			// // ����ģʽ���룬����Ϊ0
			// // new ManageAction(0);
			// openManage(0);
			//
			// loginView.dispose();
			// RecogniseService.getInstance(null).waitMode();
			// } else {
			// /**
			// * ��Ϊ��ͨ�û�����û�н�������ʶ�������£�Ϊ����߰�ȫ�ԣ�������������ģʽ��
			// * �����Ҫ�����������Ϣ�Ļ��� ����ȥ����Ա������
			// */
			// JOptionPane.showMessageDialog(loginView,
			// "δ¼����Ƭ��������������ģʽ");
			//
			// // ����趨��ͨ�û����Ե�½�����Խ������ע��ȥ��
			// // JOptionPane.showMessageDialog(loginView,
			// // "Ϊ��ǿ��ȫ�ԣ�����¼����Ƭ");
			// // new CommonUserAction(r, 0);
			// }
			// }
			//
			// } else if (r == -2) {
			//
			// if (tempUser.getId() == 0) {
			// // ��ʾ��Ϣ����ʾ����Ա�����¼����Ƭ
			// JOptionPane.showMessageDialog(loginView,
			// "����Ա����δ������������£�����ʶ���ܲ����ã��뾡�����������");
			//
			// // ����ģʽ���룬����Ϊ0
			// // new ManageAction(0);
			// openManage(0);
			//
			// loginView.dispose();
			// RecogniseService.getInstance(null).waitMode();
			// } else {
			// JOptionPane.showMessageDialog(loginView,
			// "��δ������������£�����ʶ���ܲ�����");
			// }
			// } else {// ����ʶ��ͨ�������Ǻ��û���������Ľ����һ��
			// JOptionPane.showMessageDialog(loginView,
			// "����ʶ�����¼�˺Ų���ͬһ��");
			// }
			//
			// } else {// �˺ż��������
			// JOptionPane.showMessageDialog(loginView, "��������˺Ŵ���");
			// }
			// }
			login();

		} else if (e.getActionCommand().equals("buttonCancle")) {// ȡ����½
			if (RecogniseService.getInstance() != null) {

				// ����RecogniseService�߳�
				RecogniseService.getInstance().setFlag(false);

				// �ͷ�����ͷcapture��Դ
				RecogniseService.getInstance().releaseCapture();

				// System.exit(0);
			}
			System.exit(0);

		}
	}

	/**
	 * Description :��ӦTabbedPane��ǩ�ı�
	 * 
	 * @param e
	 *            :��Ӧ�¼�
	 * @return void
	 */
	@Override
	public void stateChanged(ChangeEvent arg0) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				if (recogniseService == null) {// ���RecogniseServiceΪ�գ��򴴽�RecogniseService
					recogniseService = RecogniseService.getInstance(null);

				}

				if (LoginAction.this.loginView.getTabbedPane()
						.getSelectedIndex() > 0) {// ������ʶ���½ģʽ�͹���ģʽ����Ҫ����ʶ���������ȴ�����
					if (recogniseService.getState() == State.WAITING) {
						RecogniseService.getInstance(null).Resume();
					}
				} else if (LoginAction.this.loginView.getTabbedPane()
						.getSelectedIndex() == 0) {// ���л����û���������ģʽ������Ҫ����ʶ�������䴦�ڵȴ�״̬
					if (recogniseService.getState() != State.WAITING) {
						// recogniseService.setM_mode(MODES.MODE_WAIT);
						recogniseService.waitMode();
					}
				}

			}
		}).start();

	}

	public void login() {
		if (this.loginView.getTabbedPane().getSelectedIndex() == 0) {// �û����������½

			User tempUser = new User();
			tempUser.setName(this.loginView.getTextFieldAccount().getText());
			tempUser.setPwd(new String(this.loginView.getPasswordField()
					.getPassword()));

			// ����û��Ƿ����
			boolean result = userModel.checkUserByPwd(tempUser);

			if (!result) {// �����ڣ�������ʾ�Ի���
				JOptionPane.showMessageDialog(loginView, "��������˺Ŵ���");
			} else {// ����
				loginView.dispose();

				if (tempUser.getId() == 0) {// ID��Ϊ0������Ա�û�

					// �ǹ���ģʽ��½������Ϊ1
					// new ManageAction(1);
					openManage(1);
				} else {// ID�Ų�Ϊ0����ͨ�û�
					// �ǹ���ģʽ��½������Ϊ1
					//new CommonUserAction(tempUser.getId(), 1);
					openCommonUser(tempUser.getId(), 1);
				}
			}
		} else if (this.loginView.getTabbedPane().getSelectedIndex() == 1) {// ����ʶ���½ģʽ

			// ��RecogniseService������Ϊ����ʶ��ģʽ
			RecogniseService.getInstance(recogniseResult).recognitionMode();

			try {
				// ȡ�����
				int r = recogniseResult.take();
				if (r == 0) {// IDΪ0������Ա

					// �ǹ���ģʽ��½������Ϊ1
					// new ManageAction(1);
					openManage(1);

					loginView.dispose();

					RecogniseService.getInstance(null).waitMode();
				} else if (r == -1) {// �����ڸ��ˣ�������ʾ�Ի���
					JOptionPane.showMessageDialog(loginView, "δע���û���");
				} else if (r == -2) {
					JOptionPane.showMessageDialog(loginView,
							"��δ������������£�����ʶ���ܲ�����");
				} else {// ID�ŷ�0����ͨ�û�

					// �ǹ���ģʽ��½������Ϊ1
					//new CommonUserAction(r, 1);
					openCommonUser(r, 1);

					loginView.dispose();

					RecogniseService.getInstance(null).waitMode();
				}

			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} else if (this.loginView.getTabbedPane().getSelectedIndex() == 2) {// ����ģʽ��½

			User tempUser = new User();
			tempUser.setName(this.loginView.getTextFieldMAccount().getText());
			tempUser.setPwd(new String(this.loginView.getPasswordFieldM()
					.getPassword()));

			// �ȼ���û����������Ƿ���ȷ
			boolean result = userModel.checkUserByPwd(tempUser);

			if (result) {// �û�����������ȷ

				int r = -1;

				// ����ʶ��
				RecogniseService.getInstance(recogniseResult).recognitionMode();

				try {
					// ȡ�ý��
					r = recogniseResult.take();
				} catch (Exception e2) {
					e2.printStackTrace();
				}

				if (r == tempUser.getId()) {// �û��������������ʶ����һ��
					if (r == 0) {// ����Ա

						// ����ģʽ������Ϊ0
						// new ManageAction(0);
						openManage(0);
					} else {// ��ͨ�û�

						// ����ģʽ������Ϊ0
						//new CommonUserAction(r, 0);
						openCommonUser(r, 0);
					}

					loginView.dispose();
					RecogniseService.getInstance(null).waitMode();
				} else if (r == -1) {// ����ʶ��δͨ��

					if ("��".equals(tempUser.getHavePhoto())) {
						JOptionPane.showMessageDialog(loginView, "����ʶ��δͨ��");
					} else if ("��".equals(tempUser.getHavePhoto())) {
						/**
						 * ��Ϊ����Ա������������뵽����ģʽ����ϵͳ�İ�ȫ�����ɹ���Ա�е���
						 * ���������һ�ν���ϵͳ��������¼����Ƭ
						 */

						if (tempUser.getId() == 0) {
							// ��ʾ��Ϣ����ʾ����Ա�����¼����Ƭ
							JOptionPane.showMessageDialog(loginView,
									"����Ա��ݣ���¼�������¼����Ƭ��ѵ��");

							// ����ģʽ���룬����Ϊ0
							// new ManageAction(0);
							openManage(0);

							loginView.dispose();
							RecogniseService.getInstance(null).waitMode();
						} else {
							/**
							 * ��Ϊ��ͨ�û�����û�н�������ʶ�������£�Ϊ����߰�ȫ�ԣ�������������ģʽ��
							 * �����Ҫ�����������Ϣ�Ļ��� ����ȥ����Ա������
							 */
							JOptionPane.showMessageDialog(loginView,
									"δ¼����Ƭ��������������ģʽ");

							// ����趨��ͨ�û����Ե�½�����Խ������ע��ȥ��
							// JOptionPane.showMessageDialog(loginView,
							// "Ϊ��ǿ��ȫ�ԣ�����¼����Ƭ");
							// new CommonUserAction(r, 0);
						}
					}

				} else if (r == -2) {

					if (tempUser.getId() == 0) {
						// ��ʾ��Ϣ����ʾ����Ա�����¼����Ƭ
						JOptionPane.showMessageDialog(loginView,
								"����Ա����δ������������£�����ʶ���ܲ����ã��뾡�����������");

						// ����ģʽ���룬����Ϊ0
						// new ManageAction(0);
						openManage(0);

						loginView.dispose();
						RecogniseService.getInstance(null).waitMode();
					} else {
						JOptionPane.showMessageDialog(loginView,
								"��δ������������£�����ʶ���ܲ�����");
					}
				} else {// ����ʶ��ͨ�������Ǻ��û���������Ľ����һ��
					JOptionPane.showMessageDialog(loginView, "����ʶ�����¼�˺Ų���ͬһ��");
				}

			} else {// �˺ż��������
				JOptionPane.showMessageDialog(loginView, "��������˺Ŵ���");
			}
		}
	}

	public void openManage(int mode) {
		new OpenManageThread(mode).start();
	}

	class OpenManageThread extends Thread {
		private int mode;
		private BlockingQueue<Boolean> result = new LinkedBlockingQueue<Boolean>();

		public OpenManageThread(int mode) {
			this.mode = mode;
		}

		@Override
		public void run() {
			String content = "<html>����Ա�û������ã�<br/>�Ѿ���¼�ɹ����������ڼ��ع�����棬���Եȡ�</html>";

			LoadingView loadingView = new LoadingView(content);

			new ManageAction(mode, result);
			try {
				result.take();
			} catch (Exception e) {
				// TODO: handle exception
			}
			loadingView.dispose();
		}
	}

	public void openCommonUser(int id, int mode) {
		new OpenCommonUserThread(id, mode).start();
	}

	class OpenCommonUserThread extends Thread {
		private int mode;
		private int id;
		private BlockingQueue<Boolean> result = new LinkedBlockingQueue<Boolean>();

		public OpenCommonUserThread(int id, int mode) {
			this.mode = mode;
			this.id = id;
		}

		@Override
		public void run() {
			String content = "<html>���ã�<br/>�Ѿ���¼�ɹ����������ڼ��ع�����棬���Եȡ�</html>";

			LoadingView loadingView = new LoadingView(content);

			new CommonUserAction(id, mode,result);

			try {
				result.take();
			} catch (Exception e) {
				// TODO: handle exception
			}
			loadingView.dispose();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			login();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}
}

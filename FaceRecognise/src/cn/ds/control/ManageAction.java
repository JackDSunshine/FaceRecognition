/**
 * Project             :FaceRecognise project
 * Comments            :���������
 * Version             :1.0
 * Modification history: number | time |   why  |  who
 * 1 | 2013-4-20 | ���� | jxm 
 * 2 | 2013-8-11 | ���˫������Ƶʶ�����ļ���;���ɾ�������û���ť| jxm
 * 3 | 2013-8-13 | �޸ġ�ɾ�������û����й��ڽ�����µ�bug| jxm
 */
package cn.ds.control;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import cn.ds.domain.VideoTask;
import cn.ds.model.ManageModel;
import cn.ds.service.RecogniseService;
import cn.ds.service.ScheduleManage;
import cn.ds.service.VideoTasksManageThread;
import cn.ds.utils.Page;
import cn.ds.view.ManageView;

public class ManageAction implements ActionListener, ListSelectionListener,
		ChangeListener, MouseListener {
	// ����Ա����
	private ManageView manageView;
	// ����Աģ��
	private ManageModel manageModel;
	// ҳ�棬����Ϊ��һҳ��ÿҳ���10����¼
	private Page page = new Page(1, 10);
	// ��ѡ���������
	private int[] selectedRows;
	// ��Ƶʶ�����񼯺�
	private Map<Integer, VideoTask> videoTasks = new HashMap<Integer, VideoTask>();
	// ����������Ƶʶ�����񼯺�
	private Map<Integer, VideoTask> runningVideoTasks = new HashMap<Integer, VideoTask>();

	// �����������
	private int maxVideoTaskNum = 2;
	// �������
	private int videoTaskIndex = 0;
	// ��Ƶʶ����������߳�
	private VideoTasksManageThread videoTasksManageThread = null;
	// ���ȣ���ʾ�������߳�
	private ScheduleManage scheduleManage = null;

	private String strSearch;

	private FileNameExtensionFilter filterPhoto = new FileNameExtensionFilter(
			"ͼƬ", "bmp", "jpg");
	private JFileChooser fileChooser = new JFileChooser();
	private String batchPhotoProcessSavePath;
	private BlockingQueue<Boolean> batchRecogniseResult = new LinkedBlockingQueue<Boolean>();
	private List<String> photoPathList = new ArrayList<String>();
	private List<String> sourcePhotoList = new ArrayList<String>();
	private BlockingQueue<Boolean> loadViewResult;

	/**
	 * Description :���캯��
	 * 
	 * @param mode
	 *            : ��½ģʽ
	 * @return ManageAction
	 */
	public ManageAction(int mode, BlockingQueue<Boolean> loadViewResult) {

		this.loadViewResult = loadViewResult;

		manageModel = new ManageModel();

		if (mode == 0) {// ����ģʽ��½
			manageModel.updatePage(page);
			Object[][] users = manageModel.getUserByPage(page);
			manageView = new ManageView(this, users, page);
		} else {// �ǹ���ģʽ��½
			manageView = new ManageView(this, null, page);
			// ����ģʽ�����еİ�ť��������
			disableAllButton();
		}

		// ��дmanageView���ڵĹرշ���
		manageView.addWindowListener(new WindowAdapter() {

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
		try {
			this.loadViewResult.put(true);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * Description :�������İ�ť��Ӧ����
	 * 
	 * @param e
	 *            :��Ӧ�¼�
	 * @return void
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		new ManageResponseThread(e.getActionCommand()).start();

	}

	/**
	 * Description :Jtable�¼���Ӧ
	 * 
	 * @param e
	 *            :��Ӧ�¼�
	 * @return void
	 */
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		// �õ�ѡ���������
		selectedRows = manageView.getTable().getSelectedRows();

		// ����ɾ�����޸İ�ť
		manageView.getButtonDelete().setEnabled(true);
		manageView.getButtonUpdate().setEnabled(true);

	}

	/**
	 * Description :�ڲ��࣬����ť��Ӧ
	 */
	class ManageResponseThread extends Thread {
		private String actionCommand;

		public ManageResponseThread(String actionCommand) {
			this.actionCommand = actionCommand;
		}

		@Override
		public void run() {

			if (actionCommand.equals("buttonSeacher")) {// ��ѯ��ť��Ӧ

				strSearch = manageView.getTextFieldSearch().getText();

				// ����Ϊ��һҳ
				page.setPageNow(1);

				if ("".equals(strSearch)) {// ��ѯ�ֶ�Ϊ�գ���Ϊȫ����ѯ����һҳ��

					// //����Ϊ��һҳ
					// page.setPageNow(1);
					if (SwingUtilities.isEventDispatchThread()) {
						// ����Jtable
						manageModel.refreshUserTable(
								(DefaultTableModel) manageView.getTable()
										.getModel(), page);

					} else {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								// ����Jtable
								manageModel.refreshUserTable(
										(DefaultTableModel) manageView
												.getTable().getModel(), page);
								// ���������ؼ�
								manageView.refreshUI();

							}
						});
					}

				} else {// ��ѯ�ֶβ�Ϊ��

					if (SwingUtilities.isEventDispatchThread()) {
						// ��ʾȫ����ѯ�ֶν���������ҳ
						manageModel.refreshUserTable(
								(DefaultTableModel) manageView.getTable()
										.getModel(), manageModel
										.getUsersByAccount(strSearch));

					} else {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								// ��ʾȫ����ѯ�ֶν���������ҳ
								manageModel.refreshUserTable(
										(DefaultTableModel) manageView
												.getTable().getModel(),
										manageModel
												.getUsersByAccount(strSearch));
								// ȡ����ҳ��Ϣ
								manageView.getButtonNextPage()
										.setEnabled(false);
								manageView.getButtonPreviousPage().setEnabled(
										false);

								manageView.getLabelPage().setText("");

							}

						});
					}

				}
			} else if (actionCommand.equals("buttonTrain")) {// ѵ��ͼƬ��ť��Ӧ
				manageView.getButtonTrain().setEnabled(false);
				
				BlockingQueue<Boolean> result = new LinkedBlockingQueue<Boolean>();
				manageModel.train(result);

				try {
					boolean r = result.take();
					if (r) {// ����true���ɹ����������⣬������ʾ�Ի���
						JOptionPane.showMessageDialog(manageView, "�����������ϣ�");
					} else {// ����false������������ʧ�ܣ�������ʾ�Ի���
						JOptionPane.showMessageDialog(manageView,
								"������Ҫ��������Ƭ��Ϣ���ܸ���������");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				manageView.getButtonTrain().setEnabled(true);
			} else if (actionCommand.equals("buttonAdd")) {// ����û���ť��Ӧ

				// ����û�
				new AddUserAction(manageView, (DefaultTableModel) manageView
						.getTable().getModel(), page);
			} else if (actionCommand.equals("buttonUpdate")) {// �޸��û���Ϣ��ť��Ӧ
				if (selectedRows != null) {

					// �õ�ID�ţ�ֻȡһ��
					int id = Integer.valueOf(manageView.getTable()
							.getValueAt(selectedRows[0], 0).toString());
					// �޸��û�
					new UpdateUserAction(id, selectedRows[0],
							(DefaultTableModel) manageView.getTable()
									.getModel(), 0);

					manageView.getButtonUpdate().setEnabled(false);
				}

			} else if (actionCommand.equals("buttonDelete")) {// ɾ����ť��Ӧ

				if (selectedRows != null) {// ɾ��
					for (int i = 0; i < selectedRows.length; i++) {
						int sn = Integer.valueOf(manageView.getTable()
								.getValueAt(selectedRows[i], 0).toString());
						if (sn != 0) {
							manageModel.deleteUserById(sn);
						}

					}
				}
				manageModel.updatePage(page);

				if (page.getNowPageSize() == 0 && page.getPageNow() > 1) {
					page.setPageNow(page.getPageNow() - 1);
				}

				if (SwingUtilities.isEventDispatchThread()) {
					manageModel.refreshUserTable((DefaultTableModel) manageView
							.getTable().getModel(), page);

				} else {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							manageModel.refreshUserTable(
									(DefaultTableModel) manageView.getTable()
											.getModel(), page);
							manageView.refreshUI();
							manageView.getButtonDelete().setEnabled(false);

						}
					});
				}

			} else if (actionCommand.equals("buttonNextPage")) {// ��һҳ��ť��Ӧ

				page.setPageNow(page.getPageNow() + 1);
				if (SwingUtilities.isEventDispatchThread()) {
					// ˢ���б�
					manageModel.refreshUserTable((DefaultTableModel) manageView
							.getTable().getModel(), page);

				} else {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							manageModel.refreshUserTable(
									(DefaultTableModel) manageView.getTable()
											.getModel(), page);
							manageView.refreshUI();

						}
					});
				}
				manageView.refreshUI();
			} else if (actionCommand.equals("buttonPreviouPage")) {// ��һҳ��ť��Ӧ
				page.setPageNow(page.getPageNow() - 1);
				if (SwingUtilities.isEventDispatchThread()) {
					// ˢ���б�
					manageModel.refreshUserTable((DefaultTableModel) manageView
							.getTable().getModel(), page);

				} else {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							manageModel.refreshUserTable(
									(DefaultTableModel) manageView.getTable()
											.getModel(), page);
							manageView.refreshUI();

						}
					});
				}

			} else if (actionCommand.equals("buttonGo")) {// ��תҳ��
				String str = manageView.getTextFieldPageNum().getText();
				int pageNum;
				try {
					pageNum = Integer.parseInt(str);
					if (pageNum <= 0) {
						JOptionPane.showMessageDialog(manageView, "������Ǹ���");
					} else if (pageNum > page.getPageCount()) {
						JOptionPane
								.showMessageDialog(manageView, "��������ִ����ܵ�ҳ��");
					} else {
						// ���õ�ǰҳ��
						page.setPageNow(pageNum);
						if (SwingUtilities.isEventDispatchThread()) {
							// ˢ���б�
							manageModel.refreshUserTable(
									(DefaultTableModel) manageView.getTable()
											.getModel(), page);

						} else {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									manageModel.refreshUserTable(
											(DefaultTableModel) manageView
													.getTable().getModel(),
											page);
									manageView.refreshUI();

								}
							});
						}

					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(manageView, "����������");
				}
			} else if (actionCommand.equals("buttonAddVideoProcessTask")) {// �����Ƶ����ʶ������
				new AddVideoProcessTaskAction(videoTasksManageThread,
						videoTaskIndex);
				videoTaskIndex += 1;
			} else if (actionCommand.equals("buttonDeleteAllUsers")) {
				manageView.getButtonDeleteAllUsers().setEnabled(false);
				if (manageModel.deleteAllUsers()) {

					page.setRowCount(1);
					page.setPageNow(1);

					if (SwingUtilities.isEventDispatchThread()) {
						manageModel.refreshUserTable(
								(DefaultTableModel) manageView.getTable()
										.getModel(), page);

					} else {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								manageModel.refreshUserTable(
										(DefaultTableModel) manageView
												.getTable().getModel(), page);

							}
						});
					}
					manageView.refreshUI();
					manageView.getButtonDeleteAllUsers().setEnabled(true);

					JOptionPane.showMessageDialog(manageView, "�û���ȫ�����");
				}
			} else if (actionCommand.equals("buttonBatchReg")) {
				new BatchRegAction(manageView, (DefaultTableModel) manageView
						.getTable().getModel(), page);
			} else if (actionCommand.equals("buttonChooseSavePath")) {
				if (!"".equals(manageView.getTextFieldSavePath().getText())) {// ����Ƭ·����Ϊ�գ���ʹ�������Ӧ�ļ�ѡ�񴰿�
					fileChooser.setCurrentDirectory(new File(manageView
							.getTextFieldSavePath().getText()));
				} else {// ����Ƭ·��Ϊ�գ���ʹ�Ĭ�ϵ��ļ�ѡ�񴰿�
					fileChooser.setCurrentDirectory(new File("C:\\"));
				}

				// �����ļ�ѡ�������
				fileChooser.setFileFilter(filterPhoto);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fileChooser.showOpenDialog(manageView);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					batchPhotoProcessSavePath = fileChooser.getSelectedFile()
							.getAbsolutePath();
					if (!batchPhotoProcessSavePath.endsWith("\\")) {
						batchPhotoProcessSavePath += "\\";
					}
					manageView.getTextFieldSavePath().setText(
							batchPhotoProcessSavePath);
				}
			} else if (actionCommand.equals("buttonChooseToRecognisePhoto")) {
				if (!"".equals(manageView.getTextFieldPhotoToRecognise()
						.getText())) {// ����Ƭ·����Ϊ�գ���ʹ�������Ӧ�ļ�ѡ�񴰿�
					fileChooser.setCurrentDirectory(new File(manageView
							.getTextFieldPhotoToRecognise().getText()));
				} else {// ����Ƭ·��Ϊ�գ���ʹ�Ĭ�ϵ��ļ�ѡ�񴰿�
					fileChooser.setCurrentDirectory(new File("C:\\"));
				}
				//
				fileChooser.setMultiSelectionEnabled(true);
				// �����ļ�ѡ�������
				fileChooser.setFileFilter(filterPhoto);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = fileChooser.showOpenDialog(manageView);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] files = fileChooser.getSelectedFiles();
					if (files != null) {
						for (int i = 0; i < files.length; i++) {
							String dataPath = files[i].getAbsolutePath();
							photoPathList.add(dataPath);
						}
						manageView.getTextFieldPhotoToRecognise().setText(
								files[files.length - 1].getAbsolutePath());
					}

				}
			} else if (actionCommand.equals("buttonBatchFaceSure")) {
				if (batchPhotoProcessSavePath != null
						&& photoPathList.size() > 0) {
					float threshold = Float.parseFloat(manageView.getComboBox()
							.getSelectedItem().toString());

					if (manageView.getRadioButtonMaskFace().isSelected()) {
						manageModel.batchMaskedFaceRecognise(
								batchPhotoProcessSavePath, threshold,
								photoPathList, batchRecogniseResult);
					} else if (manageView.getRadioButtonUnmaskFace()
							.isSelected()) {

						manageModel.batchUnmaskedFaceRecognise(
								batchPhotoProcessSavePath, threshold,
								photoPathList, sourcePhotoList, 6, "_", "jpg",
								batchRecogniseResult);
					}
					try {
						batchRecogniseResult.take();
					} catch (Exception e) {
						e.printStackTrace();
					}
					photoPathList.clear();
					JOptionPane.showMessageDialog(manageView, "��ʶ�����");
					manageView.getButtonSeeResult().setEnabled(true);
				}
			} else if (actionCommand.equals("buttonSeeResult")) {
				if (batchPhotoProcessSavePath != null) {
					try {
						Desktop.getDesktop().open(
								new File(batchPhotoProcessSavePath));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			} else if (actionCommand.equals("radioButtonUnmaskFace")) {
				if (manageView.getRadioButtonUnmaskFace().isSelected()) {
					manageView.getButtonChooseSourcePhoto().setEnabled(true);
				}
			} else if (actionCommand.equals("buttonChooseSourcePhoto")) {
				if (!"".equals(manageView.getTextFieldSourcePhoto().getText())) {// ����Ƭ·����Ϊ�գ���ʹ�������Ӧ�ļ�ѡ�񴰿�
					fileChooser.setCurrentDirectory(new File(manageView
							.getTextFieldSourcePhoto().getText()));
				} else {// ����Ƭ·��Ϊ�գ���ʹ�Ĭ�ϵ��ļ�ѡ�񴰿�
					fileChooser.setCurrentDirectory(new File("C:\\"));
				}
				//
				fileChooser.setMultiSelectionEnabled(true);
				// �����ļ�ѡ�������
				fileChooser.setFileFilter(filterPhoto);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fileChooser.showOpenDialog(manageView);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] files = fileChooser.getSelectedFiles();
					if (files != null) {
						for (int i = 0; i < files.length; i++) {
							String dataPath = files[i].getAbsolutePath();
							sourcePhotoList.add(dataPath);
						}
						manageView.getTextFieldSourcePhoto().setText(
								files[files.length - 1].getAbsolutePath());
					}

				}
			}else if(actionCommand.equals("radioButtonMaskFace")){
				if (manageView.getRadioButtonMaskFace().isSelected()) {
					manageView.getButtonChooseSourcePhoto().setEnabled(false);
				}	
			}
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
	public void stateChanged(ChangeEvent e) {
		if (manageView.getTabbedPane().getSelectedIndex() == 1) {// �л�����Ƶ����ʶ������

			if (scheduleManage == null) {

				new Thread(new Runnable() {

					@Override
					public void run() {

						// �������ȣ���ʾ�������߳�
						scheduleManage = new ScheduleManage(videoTasks,
								(DefaultTableModel) manageView
										.getTableVideoProcessTask().getModel());
						scheduleManage.start();

						// ������������߳�
						videoTasksManageThread = new VideoTasksManageThread(
								videoTasks, runningVideoTasks, scheduleManage,
								maxVideoTaskNum);
						videoTasksManageThread.start();

					}
				}).start();
			}
		}

	}

	/**
	 * Description :���ð�ť����ʹ��
	 * 
	 * @return void
	 */
	public void disableAllButton() {
		manageView.getButtonNextPage().setEnabled(false);
		manageView.getButtonPreviousPage().setEnabled(false);
		manageView.getButtonAdd().setEnabled(false);
		manageView.getButtonSeacher().setEnabled(false);
		manageView.getButtonTrain().setEnabled(false);
		manageView.getButtonUpdate().setEnabled(false);
		manageView.getBtnGo().setEnabled(false);
		manageView.getButtonDeleteAllUsers().setEnabled(false);
		manageView.getButtonBatchReg().setEnabled(false);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (e.getClickCount() == 2) {
				int rowNum = manageView.getTableVideoProcessTask().getModel()
						.getRowCount() - 1;
				int taskId = Integer.valueOf(manageView
						.getTableVideoProcessTask().getValueAt(rowNum, 0)
						.toString());
				String path = videoTasks.get(taskId).getSavePath();
				try {
					Desktop.getDesktop().open(new File(path));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	class OpenManageView extends Thread {

		@Override
		public void run() {
			manageView = new ManageView(ManageAction.this, null, page);
		}

	}
}

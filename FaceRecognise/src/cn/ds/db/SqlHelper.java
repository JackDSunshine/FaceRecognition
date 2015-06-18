/**
 * Project             :FaceRecognise project
 * Comments            :�������ݿ�
 * Version             :1.0
 * Modification history: number | time |   why  |  who
 * 1 | 2013-3-22 | ���� | jxm 
 */
package cn.ds.db;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class SqlHelper {
	// �������
	private static Connection ct = null;
	private static PreparedStatement ps = null;
	private static ResultSet rs = null;

	// �������ݿ�Ĳ���
	private static String url = "";
	private static String username = "";
	private static String driver = "";
	private static String passwd = "";
	private static Properties pp = null;
	private static InputStream fis = null;

	// ����������ֻ��Ҫһ�Σ��þ�̬�����
	static {
		try {
			pp = new Properties();
			// fis = SqlHelper.class.getClassLoader().getResourceAsStream(
			// "oracle.properties");
			fis = new FileInputStream("dat/oracle.properties");
			pp.load(fis);
			url = pp.getProperty("url");
			username = pp.getProperty("username");
			driver = pp.getProperty("driver");
			passwd = pp.getProperty("passwd");
			Class.forName(driver);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			fis = null;// ��������վ������
		}

	}

	/**
	 * Description :�õ�����
	 * 
	 * @return Connection
	 */
	public Connection getConnection() {
		try {
			ct = DriverManager.getConnection(url, username, passwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ct;
	}

	/**
	 * Description :�ر���Դ
	 */
	public void close(ResultSet rs, Statement ps, Connection ct) {
		// �ر���Դ(�ȿ����)
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			rs = null;
		}
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			ps = null;
		}
		if (null != ct) {
			try {
				ct.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			ct = null;
		}
	}

	/**
	 * Description :ִ������ɾ���Ĳ���
	 * 
	 * @param sql
	 *            :sql���
	 * @param parameters
	 *            : �����б�
	 * @return boolean:ִ�н��
	 */
	public boolean executeUpdate(String sql, String[] parameters) {
		boolean flag = false;
		try {
			// ȡ���Զ��ύ����
			// ct.setAutoCommit(false);

			ct = getConnection();
			ps = ct.prepareStatement(sql);

			if (parameters != null) {
				for (int i = 0; i < parameters.length; i++) {
					ps.setString(i + 1, parameters[i]);
				}

			}
			if (ps.executeUpdate() >= 1) {
				flag = true;
			}
			// �ύ
			// ct.commit();
			// ct.setAutoCommit(true);// �ָ��ֳ�
		} catch (Exception e) {
			try {
				// �ع�
				// ct.rollback();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();// �����׶�
			// �׳��쳣
			// ���Դ���Ҳ���Բ�����
			throw new RuntimeException(e.getMessage());
		} finally {
			close(rs, ps, ct);
		}
		return flag;
	}

	public void batchexecute(String sql, String[][] parameters) {
		try {

			ct = getConnection();
			ps = ct.prepareStatement(sql);

			if (parameters != null) {
				for (int i = 0; i < parameters.length; i++) {
					String[] para = parameters[i]; 
					for(int j = 0;j < para.length;j++){
						ps.setString(j + 1, para[j]);	
					}
					ps.addBatch();
				}

			}
			ps.executeBatch();	

		} catch (Exception e) {
			try {
				// �ع�
				// ct.rollback();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();// �����׶�
			// �׳��쳣
			// ���Դ���Ҳ���Բ�����
			throw new RuntimeException(e.getMessage());
		} finally {
			close(rs, ps, ct);
		}
	}

	/**
	 * Description :ִ�в�ѯ����
	 * 
	 * @param sql
	 *            :sql���
	 * @param parameters
	 *            : �����б�
	 * @return ArrayList<Object[]>:���ز�ѯ���
	 */
	public ArrayList<Object[]> queryExecute(String sql, String[] parameters) {
		ArrayList<Object[]> arrayList = new ArrayList<Object[]>();
		try {
			ct = getConnection();
			ps = ct.prepareStatement(sql);
			if (parameters != null) {
				for (int i = 0; i < parameters.length; i++) {
					ps.setString(i + 1, parameters[i]);
				}
			}
			rs = ps.executeQuery();
			ResultSetMetaData resultSetMetaData = rs.getMetaData();
			int colum = resultSetMetaData.getColumnCount();
			while (rs.next()) {
				Object[] object = new Object[colum];
				for (int i = 0; i < colum; i++) {
					object[i] = rs.getObject(i + 1);
				}
				arrayList.add(object);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(rs, ps, ct);
		}
		return arrayList;
	}

}

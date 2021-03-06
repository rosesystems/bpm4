/*
 * Created on 2004. 11. 3.
 */
package org.uengine.persistence.dao;

import java.util.Calendar;
import java.util.Map;

import org.uengine.persistence.DAOType;
import org.uengine.persistence.processinstance.ProcessInstanceDAO;
import org.uengine.persistence.processinstance.ProcessInstanceRepositoryLocal;
import org.uengine.persistence.processvariable.ProcessVariableDAO;
import org.uengine.processmanager.ProcessTransactionContext;
import org.uengine.util.UEngineUtil;
import org.uengine.util.dao.*;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.UEngineException;

/**
 * @author Jinyoung Jang
 * @author Jong-uk Jeong
 */

public abstract class DAOFactory{
	public static String USE_CLASS_NAME;
	
	ConnectionFactory connectionFactory;
		public ConnectionFactory getConnectionFactory() {
			return connectionFactory;
		}
		public void setConnectionFactory(ConnectionFactory factory) {
			connectionFactory = factory;
		}

	abstract public WorkListDAO findWorkListDAOByEndpoint(Map options) throws Exception;
	abstract public WorkListDAO findWorkListDAOByTaskId(Map options) throws Exception;
	abstract public WorkListDAO createWorkListDAOForInsertCall(Map options) throws Exception;
	abstract public WorkListDAO createWorkListDAOForUpdate(Map options) throws Exception;
	abstract public KeyGeneratorDAO createKeyGenerator(String forWhat, Map options) throws Exception;
	abstract public ProcessInstanceDAO createProcessInstanceDAOForArchive() throws Exception;
	abstract public ProcessVariableDAO findProcessVariableDAOByInstanceId() throws Exception;
	abstract public Calendar getNow() throws Exception;

	abstract public String getSequenceSql(String seqName) throws Exception;
	
	abstract public String getDBMSProductName() throws Exception;

	public static DAOFactory getInstance(ConnectionFactory tc){
		DAOFactory daoFactory;
		try{
			USE_CLASS_NAME = GlobalContext.getPropertyString("daofactory.class");
			daoFactory = (DAOFactory)Class.forName(USE_CLASS_NAME).newInstance();
		}catch(Exception e){
			e.printStackTrace();
			daoFactory = new org.uengine.persistence.dao.HSQLDAOFactory();
		}
		
		daoFactory.setConnectionFactory(tc);
		
		return daoFactory;
	}
	
	/**
	 * @deprecated DAOFactory should be provided with ConnectionFactory.
	 */

	public static DAOFactory getInstance(){
		return getInstance(null);
	}
	
	protected Object create(Class whatKind, String sqlStmt) throws Exception{
		return ConnectiveDAO.createDAOImpl(getConnectionFactory(), sqlStmt, whatKind);
	}
	
	public Class getDAOTypeClass(Class clsType) throws UEngineException{
		Class actualDAOTypeCls;
		try {
			actualDAOTypeCls = Class.forName(
					clsType.getPackage().getName() + 
					"." +
					getDBMSProductName() + 
					UEngineUtil.getClassNameOnly(clsType)
				);
			
			return actualDAOTypeCls;

		} catch (Exception e) {
			throw new UEngineException("Failed to find proper DAO type: ", e);
		}			
			
	}
	
}

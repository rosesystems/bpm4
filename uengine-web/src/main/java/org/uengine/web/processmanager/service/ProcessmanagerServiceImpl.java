package org.uengine.web.processmanager.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.metaworks.FieldDescriptor;
import org.metaworks.ObjectType;
import org.metaworks.inputter.WebInputter;
import org.springframework.stereotype.Service;
import org.uengine.contexts.HtmlFormContext;
import org.uengine.kernel.Activity;
import org.uengine.kernel.DefaultProcessInstance;
import org.uengine.kernel.FormActivity;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ProcessVariable;
import org.uengine.kernel.RevisionInfo;
import org.uengine.kernel.Role;
import org.uengine.kernel.RoleMapping;
import org.uengine.processdesigner.ProcessDesigner;
import org.uengine.processmanager.ProcessDefinitionRemote;
import org.uengine.processmanager.ProcessManagerBean;
import org.uengine.util.UEngineUtil;
import org.uengine.web.login.vo.LoginVO;
import org.uengine.web.process.vo.TreeVO;
import org.uengine.web.processmanager.dao.ProcessmanagerDAO;
import org.uengine.web.processmanager.vo.ProcessDefinitionVO;
import org.uengine.web.processmanager.vo.ProcessInstanceRoleVO;
import org.uengine.web.processmanager.vo.ProcessInstanceVO;
import org.uengine.web.processmanager.vo.ProcessInstanceVariableChangeVO;
import org.uengine.web.processmanager.vo.ProcessInstanceVariableVO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import kr.go.nyj.util.ActivityInstanceContext;
import kr.go.nyj.util.CommonUtil;

@Service("processmanagerService")
public class ProcessmanagerServiceImpl implements ProcessmanagerService {

	private Logger log = Logger.getLogger(this.getClass());

	@Resource(name = "processmanagerDAO")
	private ProcessmanagerDAO processmanagerDAO;

	private ProcessManagerBean processManagerBean;

	/**
	 * <pre>
	 * 1. 개요 : 
	 * 2. 처리내용 :
	 * </pre>
	 * 
	 * @Method Name : selectOrganizationChart
	 * @date : 2016. 6. 20.
	 * @author : next3
	 * @history :
	 *          -----------------------------------------------------------------------
	 *          변경일 작성자 변경내용 ----------- -------------------
	 *          --------------------------------------- 2016. 6. 20. next3 최초 작성
	 *          -----------------------------------------------------------------------
	 * 
	 * @see org.uengine.web.organization.service.OrganizationService#selectOrganizationChart()
	 * @return
	 * @throws Exception
	 */

	@Override
	public List<ProcessDefinitionVO> selectProcessDefinitionObjectListByDefId(String defId) throws Exception {
		return processmanagerDAO.selectProcessDefinitionObjectListByDefId(defId);
	}

	@Override
	public Document createJnlpXmlDocument(LoginVO sessionVO, HttpServletRequest request) throws Exception {

		RevisionInfo revInfo = new RevisionInfo();
		revInfo.setAuthorName(sessionVO.getUserName());
		revInfo.setAuthorId(sessionVO.getUserId());
		revInfo.setAuthorCompany(sessionVO.getComCode());
		revInfo.setAuthorEmailAddress(sessionVO.getEmail());
		revInfo.setChangeTime(Calendar.getInstance());

		String authorInfo = GlobalContext.serialize(revInfo, RevisionInfo.class);
//       	authorInfo = authorInfo.replace("<", "&lt;").replace(">", "&gt;");

		String serverUrl = GlobalContext.getPropertyString("bpm_host", "");
		String serverPort = GlobalContext.getPropertyString("bpm_port", "");
		String contextPath = request.getContextPath();

		String codebase = "http://" + serverUrl + ":" + serverPort + contextPath + "/processmanager";
		String resouceRoot = "/resources/processmanager/jars/";

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("jnlp");
		rootElement.setAttribute("spec", "1.0+");
		rootElement.setAttribute("codebase", codebase);
		doc.appendChild(rootElement);

		/////////////////////////////////////////////////// information
		/////////////////////////////////////////////////// element//////////////////////////////////////////////////////////////
		Element information = doc.createElement("information");
		rootElement.appendChild(information);

		// <title>u|Engine Process Designer</title>
		Element title = doc.createElement("title");
		title.setTextContent("u|Engine Process Designer");
		information.appendChild(title);

		Element vendor = doc.createElement("vendor");
		vendor.setTextContent("uEngine.org");
		information.appendChild(vendor);

		Element homepage = doc.createElement("homepage");
		homepage.setAttribute("href", "http://www.uengine.org");
		information.appendChild(homepage);

		Element descriptionFull = doc.createElement("description");
		descriptionFull.setTextContent("u|Engine Process Designe");
		information.appendChild(descriptionFull);

		Element descriptionShort = doc.createElement("description");
		descriptionShort.setAttribute("kind", "short");
		descriptionShort.setTextContent("u|Engine Process Designe");
		information.appendChild(descriptionShort);

		Element offlineAllowed = doc.createElement("offline-allowed");
		information.appendChild(offlineAllowed);
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////// security//////////////////////////////////////////////////////////////
		Element security = doc.createElement("security");
		rootElement.appendChild(security);

		Element allPermissions = doc.createElement("all-permissions");
		security.appendChild(allPermissions);
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////// resources//////////////////////////////////////////////////////////////
		Element resources = doc.createElement("resources");
		rootElement.appendChild(resources);

		Element j2se = doc.createElement("j2se");
		j2se.setAttribute("version", "1.5+");
		j2se.setAttribute("initial-heap-size", "500M");
		j2se.setAttribute("max-heap-size", "1000M");
		resources.appendChild(j2se);

		String resourcesPath = request.getRealPath(resouceRoot);
		File resourceDir = new File(resourcesPath);
		if (resourceDir.exists() && resourceDir.isDirectory()) {
			resourceDir.list();
			Iterator<String> jarFileList = Arrays.asList(resourceDir.list()).iterator();
			while (jarFileList.hasNext()) {
				String jarFileName = jarFileList.next();
				File jarFile = new File(resourceDir + File.separator + jarFileName);
				if (jarFile.exists() && jarFile.isFile() && jarFileName.endsWith(".jar")) {
					// <jar href="signeduengine.jar"/>
					Element jar = doc.createElement("jar");
					jar.setAttribute("href", contextPath + resouceRoot + jarFileName);
					resources.appendChild(jar);
				}
			}
		} else {
			throw new Exception();
		}

		Element propertyBpmHost = doc.createElement("property");
		propertyBpmHost.setAttribute("name", "bpm_host");
		propertyBpmHost.setAttribute("value", serverUrl);
		resources.appendChild(propertyBpmHost);

		Element propertyBpmPort = doc.createElement("property");
		propertyBpmPort.setAttribute("name", "bpm_port");
		propertyBpmPort.setAttribute("value", serverPort);
		resources.appendChild(propertyBpmPort);
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////// application-desc//////////////////////////////////////////////////////////////
		Element applicatioDesc = doc.createElement("application-desc");
		applicatioDesc.setAttribute("main-class", "org.uengine.processdesigner.ProcessDesigner");
		rootElement.appendChild(applicatioDesc);

		Element argumentLocale = doc.createElement("argument");
		argumentLocale.setTextContent(sessionVO.getLocale());
		applicatioDesc.appendChild(argumentLocale);

		Element argumentfolderId = doc.createElement("argument");
		argumentfolderId.setTextContent(
				UEngineUtil.isNotEmpty(request.getParameter("folderId")) ? request.getParameter("folderId") : "null");
		applicatioDesc.appendChild(argumentfolderId);

		Element argumentdefId = doc.createElement("argument");
		argumentdefId.setTextContent(
				UEngineUtil.isNotEmpty(request.getParameter("defId")) ? request.getParameter("defId") : "null");
		applicatioDesc.appendChild(argumentdefId);

		Element argumentdefVerId = doc.createElement("argument");
		argumentdefVerId.setTextContent(
				UEngineUtil.isNotEmpty(request.getParameter("defVerId")) ? request.getParameter("defVerId") : "null");
		applicatioDesc.appendChild(argumentdefVerId);

		Element argumentAuthorInfo = doc.createElement("argument");
		argumentAuthorInfo.setTextContent(authorInfo);
		applicatioDesc.appendChild(argumentAuthorInfo);

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		return doc;
	}

	@Override
	public Document createJnlpXmlDocumentByInstanceId(LoginVO sessionVO, HttpServletRequest request) throws Exception {

		RevisionInfo revInfo = new RevisionInfo();
		revInfo.setAuthorName(sessionVO.getUserName());
		revInfo.setAuthorId(sessionVO.getUserId());
		revInfo.setAuthorCompany(sessionVO.getComCode());
		revInfo.setAuthorEmailAddress(sessionVO.getEmail());
		revInfo.setChangeTime(Calendar.getInstance());

		String authorInfo = GlobalContext.serialize(revInfo, RevisionInfo.class);
//       	authorInfo = authorInfo.replace("<", "&lt;").replace(">", "&gt;");

		String serverUrl = GlobalContext.getPropertyString("bpm_host", "");
		String serverPort = GlobalContext.getPropertyString("bpm_port", "");
		String contextPath = request.getContextPath();

		String codebase = "http://" + serverUrl + ":" + serverPort + contextPath + "/processmanager";
		String resouceRoot = "/resources/processmanager/jars/";

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("jnlp");
		rootElement.setAttribute("spec", "1.0+");
		rootElement.setAttribute("codebase", codebase);
		doc.appendChild(rootElement);

		/////////////////////////////////////////////////// information
		/////////////////////////////////////////////////// element//////////////////////////////////////////////////////////////
		Element information = doc.createElement("information");
		rootElement.appendChild(information);

		// <title>u|Engine Process Designer</title>
		Element title = doc.createElement("title");
		title.setTextContent("u|Engine Process Designer");
		information.appendChild(title);

		Element vendor = doc.createElement("vendor");
		vendor.setTextContent("uEngine.org");
		information.appendChild(vendor);

		Element homepage = doc.createElement("homepage");
		homepage.setAttribute("href", "http://www.uengine.org");
		information.appendChild(homepage);

		Element descriptionFull = doc.createElement("description");
		descriptionFull.setTextContent("u|Engine Process Designe");
		information.appendChild(descriptionFull);

		Element descriptionShort = doc.createElement("description");
		descriptionShort.setAttribute("kind", "short");
		descriptionShort.setTextContent("u|Engine Process Designe");
		information.appendChild(descriptionShort);

		Element offlineAllowed = doc.createElement("offline-allowed");
		information.appendChild(offlineAllowed);
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////// security//////////////////////////////////////////////////////////////
		Element security = doc.createElement("security");
		rootElement.appendChild(security);

		Element allPermissions = doc.createElement("all-permissions");
		security.appendChild(allPermissions);
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////// resources//////////////////////////////////////////////////////////////
		Element resources = doc.createElement("resources");
		rootElement.appendChild(resources);

		Element j2se = doc.createElement("j2se");
		j2se.setAttribute("version", "1.5+");
		j2se.setAttribute("initial-heap-size", "500M");
		j2se.setAttribute("max-heap-size", "1000M");
		resources.appendChild(j2se);

		String resourcesPath = request.getRealPath(resouceRoot);
		File resourceDir = new File(resourcesPath);
		if (resourceDir.exists() && resourceDir.isDirectory()) {
			resourceDir.list();
			Iterator<String> jarFileList = Arrays.asList(resourceDir.list()).iterator();
			while (jarFileList.hasNext()) {
				String jarFileName = jarFileList.next();
				File jarFile = new File(resourceDir + File.separator + jarFileName);
				if (jarFile.exists() && jarFile.isFile() && jarFileName.endsWith(".jar")) {
					// <jar href="signeduengine.jar"/>
					Element jar = doc.createElement("jar");
					jar.setAttribute("href", contextPath + resouceRoot + jarFileName);
					resources.appendChild(jar);
				}
			}
		} else {
			throw new Exception();
		}

		Element propertyBpmHost = doc.createElement("property");
		propertyBpmHost.setAttribute("name", "bpm_host");
		propertyBpmHost.setAttribute("value", serverUrl);
		resources.appendChild(propertyBpmHost);

		Element propertyBpmPort = doc.createElement("property");
		propertyBpmPort.setAttribute("name", "bpm_port");
		propertyBpmPort.setAttribute("value", serverPort);
		resources.appendChild(propertyBpmPort);
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/////////////////////////////////////////////////// application-desc//////////////////////////////////////////////////////////////
		Element applicatioDesc = doc.createElement("application-desc");
		applicatioDesc.setAttribute("main-class", "org.uengine.processdesigner.ProcessDesigner");
		rootElement.appendChild(applicatioDesc);

		Element argumentLocale = doc.createElement("argument");
		argumentLocale.setTextContent(sessionVO.getLocale());
		applicatioDesc.appendChild(argumentLocale);

		Element argumentfolderId = doc.createElement("argument");
		argumentfolderId.setTextContent("@ADHOC");
		applicatioDesc.appendChild(argumentfolderId);

		Element argumentdefId = doc.createElement("argument");
		argumentdefId.setTextContent(request.getParameter("instId"));
		applicatioDesc.appendChild(argumentdefId);

		Element argumentdefVerId = doc.createElement("argument");
		argumentdefVerId.setTextContent(GlobalContext.getPropertyString("bpm_host"));
		applicatioDesc.appendChild(argumentdefVerId);

		Element argumentAuthorInfo = doc.createElement("argument");
		argumentAuthorInfo.setTextContent(
				UEngineUtil.isNotEmpty(request.getParameter("defVerId")) ? request.getParameter("defVerId") : "null");
		applicatioDesc.appendChild(argumentAuthorInfo);

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		return doc;
	}

	public static void main(String[] args) throws Exception {
		String[] params = new String[5];
		params[0] = "ko";
		params[1] = "23";
		params[2] = "101";
		params[3] = "309";
		params[4] = "<org.uengine.kernel.RevisionInfo>        \n"
				+ "  <authorName>테스터</authorName>                     \n"
				+ "  <authorId>test</authorId>                           \n"
				+ "  <authorCompany>kriss</authorCompany>                \n"
				+ "  <changeTime>                                        \n"
				+ "    <time>1473133252545</time>                        \n"
				+ "    <timezone>GMT+09:00</timezone>                    \n"
				+ "  </changeTime>                                       \n"
				+ "  <version>0</version>                                \n"
				+ "</org.uengine.kernel.RevisionInfo>)                   \n";
		ProcessDesigner.main(params);
	}

	@Override
	public String getProcessDefinitionXPDStringWithVersion(String version) throws Exception {
		String def = null;

		try {
			processManagerBean = new ProcessManagerBean();

			def = processManagerBean.getProcessDefinition(version, "XPD");
		} finally {
			if (processManagerBean != null)
				try {
					processManagerBean.remove();
				} catch (Exception e) {
				}
		}
		return def;
	}

	@Override
	public String getProcessDefinitionXPDString(String defId) throws Exception {
		String versionId;
		if (defId.startsWith("[") && defId.endsWith("]"))
			versionId = processManagerBean
					.getProcessDefinitionProductionVersionByAlias(defId.substring(1, defId.length() - 1));
		else
			versionId = processManagerBean.getProcessDefinitionProductionVersion(defId);
		return getProcessDefinitionXPDStringWithVersion(versionId);
	}

	@Override
	public String getProcessDefinitionXPDStringWithInstanceId(String instanceId) throws Exception {
		String def = null;

		try {
			processManagerBean = new ProcessManagerBean();

			def = processManagerBean.getProcessDefinitionWithInstanceId(instanceId, "XPD");
		} finally {
			if (processManagerBean != null)
				try {
					processManagerBean.remove();
				} catch (Exception e) {
				}
		}
		return def;
	}

	@Override
	public String saveProcessDefinition(Map<String, String> parameterMap) throws Exception {

		String definition = parameterMap.get("definition");
		String version = parameterMap.get("version");
		String definitionName = parameterMap.get("definitionName");
		String savingFolder = parameterMap.get("folderId");
		String description = parameterMap.get("description");
		String alias = parameterMap.get("alias");
		String author = parameterMap.get("author");
		boolean autoProduction = "true".equals(parameterMap.get("autoProduction"));
		String belongingDefinitionId = parameterMap.get("defId");
		String objType = parameterMap.get("objType");
		String programId = parameterMap.get("programId");
		String duration = parameterMap.get("duration");

		try {
			processManagerBean = new ProcessManagerBean();
			String defVerId = processManagerBean.addProcessDefinitionWithProgramId(definitionName,
					Integer.parseInt(version), description, false, definition, savingFolder, belongingDefinitionId,
					alias, objType, programId, author, duration);
			if (autoProduction)
				processManagerBean.setProcessDefinitionProductionVersion(defVerId);

			// CommonUtil.setProcessDefinitionToNYJ(parameterMap, processManagerBean,
			// defVerId);

			processManagerBean.applyChanges();
			return defVerId;
		} catch (Exception e) {
			processManagerBean.cancelChanges();
			throw e;
		} finally {
			if (processManagerBean != null)
				try {
					processManagerBean.remove();
				} catch (Exception e) {
				}
		}

	}

	@Override
	public void saveProcessInstanceDefinition(Map<String, String> parameterMap) throws Exception {

		String definitionXML = parameterMap.get("definitionXML");
		String instanceId = parameterMap.get("instanceId");

		try {
			processManagerBean = new ProcessManagerBean();
			processManagerBean.changeProcessDefinition(instanceId, definitionXML);
			processManagerBean.applyChanges();
		} catch (Exception e) {
			processManagerBean.cancelChanges();
			throw e;
		} finally {
			if (processManagerBean != null)
				try {
					processManagerBean.remove();
				} catch (Exception e) {
				}
		}

	}

	@Override
	public List<TreeVO> getProcessTreeBycomCode(String comCode) throws Exception {
		return processmanagerDAO.getProcessTreeBycomCode(comCode);
	}

	@Override
	public String addFolder(Map<String, String> parameterMap) throws Exception {
		try {
			processManagerBean = new ProcessManagerBean();
			String folderName = parameterMap.get("folderName");
			String parentFolder = parameterMap.get("parentFolder");
			String comCode = parameterMap.get("comCode");
			String folderId = processManagerBean.addFolder(folderName, parentFolder, comCode);

			processManagerBean.applyChanges();
			return folderId;
		} catch (Exception e) {
			processManagerBean.cancelChanges();
			throw e;
		} finally {
			if (processManagerBean != null)
				try {
					processManagerBean.remove();
				} catch (Exception e) {
				}
		}
	}

	@Override
	public void moveFolder(String parent, String object) throws Exception {
		try {
			processManagerBean = new ProcessManagerBean();
			processManagerBean.moveFolder(object, parent);

			processManagerBean.applyChanges();
		} catch (Exception e) {
			processManagerBean.cancelChanges();
			throw e;
		} finally {
			if (processManagerBean != null)
				try {
					processManagerBean.remove();
				} catch (Exception e) {
				}
		}
	}

	@Override
	public void removeDefinition(String defId) throws Exception {
		try {
			processManagerBean = new ProcessManagerBean();
			processManagerBean.removeFolder(defId);

			processManagerBean.applyChanges();
		} catch (Exception e) {
			processManagerBean.cancelChanges();
			throw e;
		} finally {
			if (processManagerBean != null)
				try {
					processManagerBean.remove();
				} catch (Exception e) {
				}
		}
	}

	@Override
	public List<ProcessInstanceVO> getProcessInstanceListByComCode(List<Map<String, String>> params) throws Exception {
		Map<String, String> paramMap = UEngineUtil.convertParameterListToMap(params);
		String status = paramMap.get("status");
		if (!UEngineUtil.isNotEmpty(status)) {
			status = "All";
		}
		paramMap.remove("status");
		paramMap.put("status", status);
		return processmanagerDAO.getProcessInstanceListByStatusAndSearchResult(paramMap);
	}

	@Override
	public void removeProcessInstance(String instanceId) throws Exception {
		try {
			processManagerBean = new ProcessManagerBean();
			processManagerBean.removeProcessInstance(instanceId);

			processManagerBean.applyChanges();
		} catch (Exception e) {
			processManagerBean.cancelChanges();
			throw e;
		} finally {
			if (processManagerBean != null)
				try {
					processManagerBean.remove();
				} catch (Exception e) {
				}
		}
	}

	@Override
	public Document createDefinitionListXmlDocument() throws Exception {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("folder");
		rootElement.setAttribute("name", GlobalContext
				.getLocalizedMessage("activitytypes.org.uengine.kernel.processdesigner.label", "Process Definitions"));
		String parent = "-1";

		for (Element child : getChildElements(parent, doc)) {
			rootElement.appendChild(child);
		}

		doc.appendChild(rootElement);
		return doc;
	}

	private List<Element> getChildElements(String parent, Document doc) throws Exception {

		List<Element> childs = new ArrayList<Element>();

		List<ProcessDefinitionVO> defs = processmanagerDAO.getProcessDefinitionByParentId(parent);
		Element e = null;
		for (ProcessDefinitionVO def : defs) {
			if (def.getObjType().equals("folder")) {
				e = doc.createElement("folder");
				e.setAttribute("name", def.getDefName());
				for (Element childElement : getChildElements(def.getDefId(), doc)) {
					e.appendChild(childElement);
				}
				childs.add(e);
			} else if (def.getObjType().equals("process")) {
				e = doc.createElement("definition");
				e.setAttribute("name", def.getDefName());
				List<ProcessDefinitionVO> defVers = processmanagerDAO
						.selectProcessDefinitionObjectListByDefId(def.getDefId());
				for (ProcessDefinitionVO defVer : processmanagerDAO
						.selectProcessDefinitionObjectListByDefId(def.getDefId())) {
					String eName = defVer.getDefName() + " v" + defVer.getVersion();
					if (defVer.getVersion().equals(defVer.getProdVer())) {
						eName += "(production)";
					}
					String eValue = "[" + defVer.getAlias() + "]" + "@" + defVer.getDefVerId();
					Element childElement = doc.createElement("version");
					childElement.setAttribute("name", eName);
					childElement.setAttribute("value", eValue);
					e.appendChild(childElement);
				}
				childs.add(e);
			}
		}

		return childs;
	}

	@Override
	public void processVersionChange(List<Map<String, String>> params) throws Exception {
		Map<String, String> paramMap = UEngineUtil.convertParameterListToMap(params);
		processmanagerDAO.processVersionChange(paramMap);
	}

	@Override
	public Map<String, String> StringToMap(String body) throws Exception {
		StringTokenizer st = new StringTokenizer(body, "&");
		Map<String, String> map = new HashMap<String, String>();
		while (st.hasMoreTokens()) {
			String value = st.nextToken();
			StringTokenizer st2 = new StringTokenizer(value, "=");
			if (st2.countTokens() == 2) {
				int count = 0;
				String keyName = null;
				String mapValue = null;
				while (st2.hasMoreTokens()) {
					if (count == 0) {
						keyName = st2.nextToken();
					} else if (count == 1) {
						mapValue = st2.nextToken();
					}
					map.put(keyName, mapValue);
					count++;
				}
			}

		}
		return map;
	}

	@Override
	public void forwordMapIntoPage(HttpServletRequest request, Map<String, String> map) throws Exception {
		Iterator<String> iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String value = map.get(key);
			request.setAttribute(key, value);
		}
	}

	@Override
	public void stopProcessInstance(String instanceId) throws Exception {
		InitialContext context = new InitialContext();
		UserTransaction tx = (GlobalContext.useManagedTransaction
				? (UserTransaction) context.lookup(GlobalContext.USERTRANSACTION_JNDI_NAME)
				: null);
		try {
			if (tx != null) {
				tx.begin();
			}
			processManagerBean = new ProcessManagerBean();
			processManagerBean.stopProcessInstance(instanceId);
			processManagerBean.applyChanges();

			if (tx != null && tx.getStatus() != Status.STATUS_NO_TRANSACTION) {
				tx.commit();
			}
		} catch (Exception e) {
			processManagerBean.cancelChanges();

			if (tx != null && tx.getStatus() != Status.STATUS_NO_TRANSACTION)
				tx.rollback();
			throw e;
		} finally {
			processManagerBean.remove();
		}
	}

	@Override
	public void executeProcess(String instanceId) throws Exception {
		InitialContext context = new InitialContext();
		UserTransaction tx = (GlobalContext.useManagedTransaction
				? (UserTransaction) context.lookup(GlobalContext.USERTRANSACTION_JNDI_NAME)
				: null);
		try {
			if (tx != null) {
				tx.begin();
			}
			processManagerBean = new ProcessManagerBean();
			processManagerBean.executeProcess(instanceId);
			processManagerBean.applyChanges();

			if (tx != null && tx.getStatus() != Status.STATUS_NO_TRANSACTION) {
				tx.commit();
			}
		} catch (Exception e) {
			try {
				processManagerBean.cancelChanges();
			} catch (Exception ex) {
			}

			try {
				tx.rollback();
			} catch (Exception ex) {
			}
			throw e;
		} finally {
			try {
				processManagerBean.remove();
			} catch (Exception ex) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProcessInstanceVariableVO> getProcessInstanceVariables(String instanceId, String locale)
			throws Exception {

		ProcessDefinitionRemote pdr = null;
		ProcessVariable[] pvdrs = null;
		ProcessInstanceVariableVO processVariableVO = null;
		List<ProcessInstanceVariableVO> processVariables = null;
		try {
			processVariables = new ArrayList<ProcessInstanceVariableVO>();
			processManagerBean = new ProcessManagerBean();
			if (instanceId != null) {
				pdr = processManagerBean.getProcessDefinitionRemoteWithInstanceId(instanceId);
				ProcessInstance processInstance = processManagerBean.getProcessInstance(instanceId);
				if (processInstance != null) {
					((DefaultProcessInstance)processInstance).setVariables(processInstance.getAll(""));
					pvdrs = processInstance.getProcessDefinition().getProcessVariables();
				} else {
					pvdrs = pdr.getProcessVariableDescriptors();
				}

				for (int i = 0; i < pvdrs.length; i++) {
					processVariableVO = new ProcessInstanceVariableVO();
					processVariableVO.setVarName(pvdrs[i].getName());
					processVariableVO.setVarDisplayName(pvdrs[i].getDisplayNameString());
					processVariableVO.setVarType(pvdrs[i].getType() == null ? "" : pvdrs[i].getType().getName());
					if (instanceId != null) {
						Serializable data = processManagerBean.getProcessVariable(instanceId, "", pvdrs[i].getName());
						processVariableVO.setVarFormContextFilePath("null");
						if (data instanceof HtmlFormContext) {
							HtmlFormContext formContext = (HtmlFormContext) data;
							String formContextFilePath = formContext.getFilePath();
							processVariableVO.setVarFormContextFilePath(
									(formContextFilePath == null || "".equals(formContextFilePath)) ? "null"
											: formContextFilePath);
						}
						processVariableVO.setData(data);
					}
					processVariables.add(processVariableVO);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (processManagerBean != null) {
				try {
					processManagerBean.remove();
				} catch (Exception e) {
				}
			}
		}
		return processVariables;
	}

	@Override
	public String getProcessVariableInXML(String instanceId, String scope, String varKey) throws Exception {

		String xml = null;
		try {
			processManagerBean = new ProcessManagerBean();

			xml = processManagerBean.getProcessVariableInXML(instanceId, scope, varKey);
		} finally {
			if (processManagerBean != null)
				try {
					processManagerBean.remove();
				} catch (Exception e) {
				}
		}
		return xml;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ProcessInstanceVariableChangeVO> getProcessVariableForChange(String instanceId, String scope,
			String varKey, String clsName) throws Exception {

		List<ProcessInstanceVariableChangeVO> changeVOs = null;
		ProcessInstanceVariableChangeVO changeVO = null;
		try {
			processManagerBean = new ProcessManagerBean();

			Class outputCls = org.uengine.kernel.GlobalContext.getComponentClass(clsName);

			/*
			 * 20190319 skk
			 */

			ProcessInstance processInstance = processManagerBean.getProcessInstance(instanceId);
			if (processInstance != null) {
				((DefaultProcessInstance)processInstance).setVariables(processInstance.getAll(""));
			}

			//
			Object existingValue = processManagerBean.getProcessVariable(instanceId, "", varKey);
			ObjectType outputClsTable = new ObjectType(outputCls);
			FieldDescriptor[] arrayFieldDescriptors = outputClsTable.getFieldDescriptors();

			String[] columns = new String[arrayFieldDescriptors.length];
			for (int i = 0; i < arrayFieldDescriptors.length; i++) {
				columns[i] = arrayFieldDescriptors[i].getName();
			}

			changeVOs = new ArrayList<ProcessInstanceVariableChangeVO>();
			for (int i = 0; i < columns.length; i++) {
				changeVO = new ProcessInstanceVariableChangeVO();
				FieldDescriptor fieldDescriptor = outputClsTable.getFieldDescriptor(columns[i]);
				changeVO.setName(fieldDescriptor.getDisplayName());
				// changeVO.setName(fieldDescriptor.getName());
				WebInputter inputter = fieldDescriptor.getWebInputter();
				changeVO.setInputterHtml(inputter.getInputterHTML("", fieldDescriptor, existingValue, null));
				changeVOs.add(changeVO);
			}

		} finally {
			if (processManagerBean != null)
				try {
					processManagerBean.remove();
				} catch (Exception e) {
				}
		}
		return changeVOs;
	}

	@Override
	public void changeProcessVariable(String instanceId, String variableName, Object value) throws Exception {
		InitialContext context = new InitialContext();
		UserTransaction tx = (GlobalContext.useManagedTransaction
				? (UserTransaction) context.lookup(GlobalContext.USERTRANSACTION_JNDI_NAME)
				: null);

		try {
			if (tx != null) {
				tx.begin();
			}
			processManagerBean = new ProcessManagerBean();
			processManagerBean.setProcessVariable(instanceId, "", variableName, (Serializable) value);
			processManagerBean.applyChanges();

			if (tx != null && tx.getStatus() != Status.STATUS_NO_TRANSACTION) {
				tx.commit();
			}

		} catch (Exception e) {
			e.printStackTrace();
			try {
				processManagerBean.cancelChanges();
			} catch (Exception ex) {
			}

			try {
				if (tx != null && tx.getStatus() != Status.STATUS_NO_TRANSACTION) {
					tx.rollback();
				}
			} catch (Exception ex) {
			}
			throw e;
		} finally {
			try {
				processManagerBean.remove();
			} catch (Exception ex) {
			}
		}
	}

	@Override
	public String showFormInstance(String filePath) throws Exception {
		FileInputStream formFile = null;
		ByteArrayOutputStream bao = null;
		try {
			formFile = new FileInputStream(FormActivity.FILE_SYSTEM_DIR + filePath);
			bao = new ByteArrayOutputStream();
			UEngineUtil.copyStream(formFile, bao);
			return bao.toString("UTF-8");
		} catch (Exception e) {
			return "Empty!";
		} finally {
			if (formFile != null)
				try {
					formFile.close();
				} catch (Exception e) {
				}
			if (bao != null)
				try {
					bao.close();
				} catch (Exception e) {
				}
		}
	}

	@Override
	public String changeFormInstance(String contents, String instanceId, String variableName) throws Exception {

		String filePath = null;
		try {
			HashMap valueMap = (HashMap) GlobalContext.deserialize(contents);
			ProcessInstance instance = null;
			ProcessDefinition definition = null;

			instance = processManagerBean.getProcessInstance(instanceId);
			definition = instance.getProcessDefinition();
			ProcessVariable variable = definition.getProcessVariable(variableName);

			HtmlFormContext formCtx = (HtmlFormContext) instance.get(variableName);

			if (formCtx.getFilePath() == null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSS", Locale.KOREA);

				filePath = UEngineUtil.getCalendarDir();
				File dirToCreate = new File(FormActivity.FILE_SYSTEM_DIR + filePath);
				dirToCreate.mkdirs();

				String datePrefix = sdf.format(new Date());
				String fileName = instance.getInstanceId() + "_" + datePrefix + ".xml";
				File newFile = new File(FormActivity.FILE_SYSTEM_DIR + filePath + "/" + fileName);
				FileOutputStream fos = new FileOutputStream(newFile);
				GlobalContext.serialize(valueMap, fos, HashMap.class);
				fos.close();

				HtmlFormContext formDefInfo = (HtmlFormContext) variable.getDefaultValue();
				String[] formDefID = formDefInfo.getFormDefId().split("@");
				String formDefinitionVersionId = (String) valueMap.get("formdefinitionversionid");
				if (!UEngineUtil.isNotEmpty(formDefinitionVersionId))
					formDefinitionVersionId = formDefID[1];

				HtmlFormContext newFormCtx = new HtmlFormContext();
				newFormCtx.setFilePath(filePath + "/" + fileName);
				newFormCtx.setFormDefId(formDefID[0] + "@" + formDefID[1]);
				newFormCtx.setValueMap(valueMap);

				filePath = newFormCtx.getFilePath();

				variable.set(instance, "", newFormCtx);
			} else {

				FileOutputStream fos = null;
				fos = new FileOutputStream(FormActivity.FILE_SYSTEM_DIR + formCtx.getFilePath());
				GlobalContext.serialize(valueMap, fos, HashMap.class);
			}

			processManagerBean.applyChanges();

		} catch (Exception e) {
			e.printStackTrace();
			try {
				processManagerBean.cancelChanges();
			} catch (Exception ex) {
			}

			throw e;
		} finally {
			try {
				processManagerBean.remove();
			} catch (Exception ex) {
			}
		}

		return filePath;
	}

	@Override
	public List<ProcessInstanceRoleVO> getProcessRolesForChange(String instanceId, String locale) throws Exception {

		List<ProcessInstanceRoleVO> roleVOs = null;
		ProcessInstanceRoleVO roleVO = null;
		try {
			processManagerBean = new ProcessManagerBean();

			ProcessInstance instance = processManagerBean.getProcessInstance(instanceId);
			ProcessDefinitionRemote pdr = processManagerBean.getProcessDefinitionRemoteWithInstanceId(instanceId);
			Role[] roles = null;
			if (instance != null) {
				roles = instance.getProcessDefinition().getRoles();
			} else {
				roles = pdr.getRoles();
			}

			if (roles != null) {
				roleVOs = new ArrayList<ProcessInstanceRoleVO>();
				for (int i = 0; i < roles.length; i++) {
					Role role = roles[i];
					roleVO = new ProcessInstanceRoleVO();
					roleVO.setDisplayName(role.getDisplayName().getText(locale));
					String name = role.getName();
					roleVO.setName(name);
					RoleMapping roleMapping = processManagerBean.getRoleMappingObject(instanceId, name);
					roleVO.setRoleMapping(roleMapping);
					roleVO.setEndPoint(roleMapping != null ? roleMapping.getEndpoint() : "");
					roleVOs.add(roleVO);
				}
			}
		} finally {
			if (processManagerBean != null)
				try {
					processManagerBean.remove();
				} catch (Exception e) {
				}
		}
		return roleVOs;
	}
}
package org.uengine.web.monitoring.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.uengine.web.common.dao.AbstractDAO;
import org.uengine.web.monitoring.vo.MonitoringVO;
import org.uengine.web.organization.vo.OrganizationVO;


@Repository("monitoringDAO")
public class MonitoringDAO extends AbstractDAO {

	@SuppressWarnings("unchecked")
	public List<MonitoringVO> getNewWorkStatus(){
		return (List<MonitoringVO>)selectList("monitoring.selectNewWorkStatus");
	}
	
	@SuppressWarnings("unchecked")
	public List<MonitoringVO> getProgressStatusByTask(){
		return (List<MonitoringVO>)selectList("monitoring.selectProgressStatusByTask");
	}
	
	@SuppressWarnings("unchecked")
	public List<MonitoringVO> getProcessingDelayStatusByTask(){
		return (List<MonitoringVO>)selectList("monitoring.selectProcessingDelayStatusByTask");
	}
	@SuppressWarnings("unchecked")
	public List<MonitoringVO> getTaskCompletedAverageTime(){
		return (List<MonitoringVO>)selectList("monitoring.selectTaskCompletedAverageTime");
	}
	@SuppressWarnings("unchecked")
	public List<MonitoringVO> getProcessingStatusByTask(Map<String, String> map){
		return (List<MonitoringVO>)selectList("monitoring.selectProcessingStatusByTask", map);
	}
	
	@SuppressWarnings("unchecked")
	public List<MonitoringVO> getProcessingStatusByUser(Map<String, String> map){
		return (List<MonitoringVO>)selectList("monitoring.selectprocessingstatusbyuser", map);
	}
	
	@SuppressWarnings("unchecked")
	public List<MonitoringVO> getProcessingDelayStatus(Map<String, String> map){
		return (List<MonitoringVO>)selectList("monitoring.selectprocessingdelaystatus", map);
	}
	//getProcessingDelayStatusData
	
	
}

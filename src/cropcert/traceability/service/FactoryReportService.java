package cropcert.traceability.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;

import cropcert.traceability.ActionStatus;
import cropcert.traceability.Constants;
import cropcert.traceability.dao.FactoryReportDao;
import cropcert.traceability.model.Activity;
import cropcert.traceability.model.FactoryReport;
import cropcert.traceability.model.Lot;
import cropcert.traceability.util.UserUtil;
import cropcert.traceability.util.ValidationException;

public class FactoryReportService extends AbstractService<FactoryReport> {

	@Inject
	private ObjectMapper objectMappper;

	@Inject
	private LotService lotService;

	@Inject
	private ActivityService activityService;

	@Inject
	public FactoryReportService(FactoryReportDao dao) {
		super(dao);
	}

	public Map<String, Object> save(HttpServletRequest request, String jsonString)
			throws JsonParseException, JsonMappingException, IOException, JSONException, ValidationException {
		ActionStatus factoryStatus;
		JSONObject jsonObject = new JSONObject(jsonString);
		if (jsonObject.has(Constants.FINALIZE_FACTORY_STATUS)
				&& jsonObject.getBoolean(Constants.FINALIZE_FACTORY_STATUS))
			factoryStatus = ActionStatus.DONE;
		else
			factoryStatus = ActionStatus.EDIT;
		jsonObject.remove(Constants.FINALIZE_FACTORY_STATUS);
		
		FactoryReport factoryReport = objectMappper.readValue(jsonObject.toString(), FactoryReport.class);
		factoryReport.setIsDeleted(false);

		Long lotId = factoryReport.getLotId();
		factoryReport = save(factoryReport);
		
		Lot lot = lotService.findById(lotId);
		lot.setFactoryStatus(factoryStatus);
		lot.setFactoryReportId(factoryReport.getId());
		lotService.update(lot);

		/**
		 * save the activity here.
		 */
		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(factoryReport.getClass().getSimpleName(), factoryReport.getId(), userId,
				timestamp, Constants.FACTORY_REPORT, factoryReport.getLotId() + "");
		activity = activityService.save(activity);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("lot", lot);
		result.put("factoryReport", factoryReport);
		return result;
	}
	
	public Map<String, Object> update(HttpServletRequest request, String jsonString) throws JsonParseException, JsonMappingException, IOException, ValidationException, JSONException {
		ActionStatus factoryStatus;
		JSONObject jsonObject = new JSONObject(jsonString);
		if (jsonObject.has(Constants.FINALIZE_FACTORY_STATUS)
				&& jsonObject.getBoolean(Constants.FINALIZE_FACTORY_STATUS))
			factoryStatus = ActionStatus.DONE;
		else
			factoryStatus = ActionStatus.EDIT;
		jsonObject.remove(Constants.FINALIZE_FACTORY_STATUS);
		
		FactoryReport factoryReport = objectMappper.readValue(jsonObject.toString(), FactoryReport.class);
		factoryReport = update(factoryReport);
		
		Long lotId = factoryReport.getLotId();
		Lot lot = lotService.findById(lotId);
		lot.setFactoryStatus(factoryStatus);
		lot.setFactoryReportId(factoryReport.getId());
		lotService.update(lot);

		/**
		 * save the activity here.
		 */
		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(factoryReport.getClass().getSimpleName(), factoryReport.getId(), userId,
				timestamp, Constants.FACTORY_REPORT, factoryReport.getLotId() + "");
		activity = activityService.save(activity);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("lot", lot);
		result.put("factoryReport", factoryReport);
		return result;
	}
}

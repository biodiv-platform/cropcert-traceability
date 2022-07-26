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
import cropcert.traceability.dao.CuppingDao;
import cropcert.traceability.model.Activity;
import cropcert.traceability.model.Cupping;
import cropcert.traceability.model.Lot;
import cropcert.traceability.util.UserUtil;
import cropcert.traceability.util.ValidationException;

public class CuppingService extends AbstractService<Cupping> {

	@Inject
	private ObjectMapper objectMappper;

	@Inject
	private ActivityService activityService;

	@Inject
	private LotService lotService;

	@Inject
	public CuppingService(CuppingDao dao) {
		super(dao);
	}

	public Map<String, Object> save(HttpServletRequest request, String jsonString)
			throws JsonParseException, JsonMappingException, IOException, JSONException {

		JSONObject jsonObject = new JSONObject(jsonString);
		Long lotId = jsonObject.getLong("lotId");
		jsonObject.remove("lotId");

		Lot lot = lotService.findById(lotId);

		ActionStatus status = ActionStatus.EDIT;
		if (jsonObject.has(Constants.FINALIZE_CUPPING_STATUS) && !jsonObject.isNull(Constants.FINALIZE_CUPPING_STATUS)
				&& jsonObject.getBoolean(Constants.FINALIZE_CUPPING_STATUS)) {
			status = ActionStatus.DONE;
		}

		jsonObject.remove(Constants.FINALIZE_CUPPING_STATUS);
		Cupping cupping = objectMappper.readValue(jsonObject.toString(), Cupping.class);
		cupping.setLot(lot);
		cupping.setIsDeleted(false);
		cupping.setStatus(status);

		cupping = save(cupping);
		lot.getCuppings().add(cupping);
		lotService.update(lot);

		/**
		 * save the activity here.
		 */
		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(cupping.getClass().getSimpleName(), cupping.getId(), userId, timestamp,
				Constants.CUPPING, cupping.getId().toString());
		activity = activityService.save(activity);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("lot", lot);
		result.put("cupping", cupping);

		return result;
	}

	public Map<String, Object> update(HttpServletRequest request, String jsonString)
			throws JsonParseException, JsonMappingException, IOException, JSONException, ValidationException {

		JSONObject jsonObject = new JSONObject(jsonString);
		Long lotId = jsonObject.getLong("lotId");
		jsonObject.remove("lotId");

		ActionStatus status = ActionStatus.EDIT;
		if (jsonObject.has(Constants.FINALIZE_CUPPING_STATUS) && !jsonObject.isNull(Constants.FINALIZE_CUPPING_STATUS)
				&& jsonObject.getBoolean(Constants.FINALIZE_CUPPING_STATUS)) {
			status = ActionStatus.DONE;
		}

		jsonObject.remove(Constants.FINALIZE_CUPPING_STATUS);
		Cupping cupping = objectMappper.readValue(jsonObject.toString(), Cupping.class);
                if(ActionStatus.DONE.equals(cupping.getStatus()))
                    throw new ValidationException("Can't modify already completed cupping");
                Lot lot = lotService.findById(lotId);
		cupping.setStatus(status);
		cupping.setLot(lot);
		cupping = update(cupping);
		lotService.update(lot);

		/**
		 * save the activity here.
		 */
		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(cupping.getClass().getSimpleName(), cupping.getId(), userId, timestamp,
				Constants.CUPPING, cupping.getId().toString());
		activity = activityService.save(activity);

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("lot", lot);
		result.put("cupping", cupping);

		return result;
	}
}

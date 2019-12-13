package cropcert.traceability.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import cropcert.traceability.ActionStatus;
import cropcert.traceability.Constants;
import cropcert.traceability.LotStatus;
import cropcert.traceability.dao.LotDao;
import cropcert.traceability.model.Activity;
import cropcert.traceability.model.Batch;
import cropcert.traceability.model.Cupping;
import cropcert.traceability.model.Lot;
import cropcert.traceability.model.LotCreation;
import cropcert.traceability.util.UserUtil;

public class LotService extends AbstractService<Lot> {

	@Inject
	private ObjectMapper objectMappper;

	@Inject
	private ActivityService activityService;

	@Inject
	private BatchService batchService;

	@Inject
	private LotCreationService lotCreationService;

	@Inject
	private CuppingService cuppingService;

	@Inject
	public LotService(LotDao dao) {
		super(dao);
	}

	public Map<String, Object> saveInBulk(String jsonString, HttpServletRequest request)
			throws JsonParseException, JsonMappingException, IOException, JSONException {

		Map<String, Object> result = new HashMap<String, Object>();
		JSONObject jsonObject = new JSONObject(jsonString);

		JSONArray jsonArray = (JSONArray) jsonObject.remove("batchIds");
		if (jsonArray.length() <= 0)
			throw new JSONException("Should content batch ids");

		Lot lot = objectMappper.readValue(jsonObject.toString(), Lot.class);
		lot.setLotStatus(LotStatus.AT_CO_OPERATIVE);
		lot.setDeleted(false);

		lot = save(lot);

		Timestamp timestamp = lot.getCreatedOn();

		Long lotId = lot.getId();

		// update the name, by appending the lot id to name
		String lotName = lot.getLotName() + "_" + lotId;
		lot.setLotName(lotName);
		update(lot);

		String userId = UserUtil.getUserDetails(request).getId();

		List<Batch> batches = new ArrayList<Batch>();

		// Add traceability for the lot creation.
		for (int i = 0; i < jsonArray.length(); i++) {
			Long batchId = jsonArray.getLong(i);

			LotCreation lotCreation = new LotCreation();
			lotCreation.setBatchId(batchId);
			lotCreation.setLotId(lotId);
			lotCreation.setUserId(userId);
			lotCreation.setTimestamp(timestamp);
			lotCreation.setNote("");
			lotCreation.setIsDeleted(false);

			// update the batch activity..
			Batch batch = batchService.findById(batchId);
			if (batch == null) {
				throw new JSONException("Invalid batch id found");
			}
			batches.add(batch);
			batch.setLotId(lotId);
			batchService.update(batch);
			lotCreationService.save(lotCreation);
		}

		// Add activity of lot creation.
		if (timestamp == null) {
			timestamp = new Timestamp(new Date().getTime());
		}
		Activity activity = new Activity(lot.getClass().getSimpleName(), lotId, userId, timestamp,
				Constants.LOT_CREATION, lot.getLotName());
		activity = activityService.save(activity);

		result.put("lot", lot);
		result.put("batches", batches);
		return result;
	}

	public String updateTimeToFactory(String jsonString, HttpServletRequest request)
			throws JsonProcessingException, JSONException, IOException {
		JSONObject jsonObject = new JSONObject(jsonString);
		JSONArray jsonArray = jsonObject.getJSONArray("ids");

		Timestamp timestamp = new Timestamp(new Date().getTime());

		Timestamp timeToFactory;
		if (jsonObject.has(Constants.TIME_TO_FACTORY))
			timeToFactory = new Timestamp((Long) jsonObject.get(Constants.TIME_TO_FACTORY));
		else
			timeToFactory = new Timestamp(new Date().getTime());

		for (int i = 0; i < jsonArray.length(); i++) {
			Long id = jsonArray.getLong(i);
			Lot lot = findById(id);
			lot.setTimeToFactory(timeToFactory);
			lot.setLotStatus(LotStatus.AT_FACTORY);
			lot = update(lot);

			String userId = UserUtil.getUserDetails(request).getId();
			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.TIME_TO_FACTORY, timeToFactory.toString());
			activity = activityService.save(activity);
		}
		return "Updated succesfully";
	}

	public Lot updateCoopAction(String jsonString, HttpServletRequest request) throws JSONException {
		JSONObject jsonObject = new JSONObject(jsonString);

		Long id = jsonObject.getLong("id");
		Lot lot = findById(id);

		if (lot == null)
			throw new ValidationException("Lot not found");

		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());

		Float weightLeavingCooperative = lot.getWeightLeavingCooperative();
		Float mcLeavingCooperative = lot.getMcLeavingCooperative();
		Timestamp timeToFactory = lot.getTimeToFactory();

		if (jsonObject.has(Constants.WEIGHT_LEAVING_COOPERATIVE)) {
			if (jsonObject.isNull(Constants.WEIGHT_LEAVING_COOPERATIVE))
				weightLeavingCooperative = null;
			else
				weightLeavingCooperative = Float
						.parseFloat(jsonObject.get(Constants.WEIGHT_LEAVING_COOPERATIVE).toString());

			lot.setWeightLeavingCooperative(weightLeavingCooperative);
			lot.setLotStatus(LotStatus.AT_CO_OPERATIVE);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.WEIGHT_LEAVING_COOPERATIVE, weightLeavingCooperative + "");
			activity = activityService.save(activity);
		}
		if (jsonObject.has(Constants.MC_LEAVING_COOPERATIVE)) {
			if (jsonObject.isNull(Constants.MC_LEAVING_COOPERATIVE))
				mcLeavingCooperative = null;
			else
				mcLeavingCooperative = Float.parseFloat(jsonObject.get(Constants.MC_LEAVING_COOPERATIVE).toString());

			lot.setMcLeavingCooperative(mcLeavingCooperative);
			lot.setLotStatus(LotStatus.AT_CO_OPERATIVE);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.MC_LEAVING_COOPERATIVE, mcLeavingCooperative + "");
			activity = activityService.save(activity);
		}
		if (jsonObject.has(Constants.TIME_TO_FACTORY)) {
			if (jsonObject.isNull(Constants.TIME_TO_FACTORY))
				timeToFactory = null;
			else
				timeToFactory = new Timestamp(jsonObject.getLong(Constants.TIME_TO_FACTORY));

			lot.setTimeToFactory(timeToFactory);
			lot.setLotStatus(LotStatus.AT_CO_OPERATIVE);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.TIME_TO_FACTORY, timeToFactory.toString());
			activity = activityService.save(activity);
		}

		if (jsonObject.has(Constants.FINALIZE_COOP_STATUS)) {

			Boolean finalizeCoopStatus = jsonObject.getBoolean(Constants.FINALIZE_COOP_STATUS);
			if (finalizeCoopStatus) {
				if (weightLeavingCooperative == null || mcLeavingCooperative == null || timeToFactory == null) {
					throw new ValidationException("Update the values first");
				}
				lot.setCoopStatus(ActionStatus.DONE);
				lot.setLotStatus(LotStatus.IN_TRANSPORT);

				Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
						Constants.FINALIZE_COOP_STATUS, ActionStatus.DONE.toString());
				activity = activityService.save(activity);
			}
		}

		update(lot);
		return lot;
	}

	public Lot updateFactoryAction(String jsonString, HttpServletRequest request) throws JSONException {
		JSONObject jsonObject = new JSONObject(jsonString);

		Long id = jsonObject.getLong("id");
		Lot lot = findById(id);

		if (lot == null)
			throw new ValidationException("Lot not found");

		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());

		Float weightArrivingFactory = lot.getWeightArrivingFactory();
		Float mcArrivingFactory = lot.getMcArrivingFactory();

		Timestamp millingTime = lot.getMillingTime();

		Float weightLeavingFactory = lot.getWeightLeavingFactory();
		Float mcLeavingFactory = lot.getMcLeavingFactory();

		if (jsonObject.has(Constants.WEIGHT_ARRIVING_FACTORY)) {
			if (jsonObject.isNull(Constants.WEIGHT_ARRIVING_FACTORY))
				weightArrivingFactory = null;
			else
				weightArrivingFactory = Float.parseFloat(jsonObject.get(Constants.WEIGHT_ARRIVING_FACTORY).toString());

			lot.setWeightArrivingFactory(weightArrivingFactory);
			lot.setLotStatus(LotStatus.AT_FACTORY);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.WEIGHT_ARRIVING_FACTORY, weightArrivingFactory + "");
			activity = activityService.save(activity);
		}
		if (jsonObject.has(Constants.WEIGHT_LEAVING_FACTORY)) {
			if (jsonObject.isNull(Constants.WEIGHT_LEAVING_FACTORY))
				weightLeavingFactory = null;
			else
				weightLeavingFactory = Float.parseFloat(jsonObject.get(Constants.WEIGHT_LEAVING_FACTORY).toString());

			lot.setWeightLeavingFactory(weightLeavingFactory);
			lot.setLotStatus(LotStatus.AT_FACTORY);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.WEIGHT_LEAVING_FACTORY, weightLeavingFactory + "");
			activity = activityService.save(activity);
		}
		if (jsonObject.has(Constants.MC_ARRIVING_FACTORY)) {
			if (jsonObject.isNull(Constants.MC_ARRIVING_FACTORY))
				mcArrivingFactory = null;
			else
				mcArrivingFactory = Float.parseFloat(jsonObject.get(Constants.MC_ARRIVING_FACTORY).toString());

			lot.setMcArrivingFactory(mcArrivingFactory);
			lot.setLotStatus(LotStatus.AT_FACTORY);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.MC_ARRIVING_FACTORY, mcArrivingFactory + "");
			activity = activityService.save(activity);
		}
		if (jsonObject.has(Constants.MC_LEAVING_FACTORY)) {
			if (jsonObject.isNull(Constants.MC_LEAVING_FACTORY))
				mcLeavingFactory = null;
			else
				mcLeavingFactory = Float.parseFloat(jsonObject.get(Constants.MC_LEAVING_FACTORY).toString());

			lot.setMcLeavingFactory(mcLeavingFactory);
			lot.setLotStatus(LotStatus.AT_FACTORY);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.MC_LEAVING_FACTORY, mcLeavingFactory + "");
			activity = activityService.save(activity);
		}
		if (jsonObject.has(Constants.MILLING_TIME)) {
			if (jsonObject.isNull(Constants.MILLING_TIME))
				millingTime = null;
			else
				millingTime = new Timestamp(jsonObject.getLong(Constants.MILLING_TIME));

			lot.setMillingTime(millingTime);
			lot.setLotStatus(LotStatus.AT_FACTORY);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.MILLING_TIME, millingTime.toString());
			activity = activityService.save(activity);
		}
		if (jsonObject.has(Constants.DISPATCH_TIME)) {
			if (jsonObject.isNull(Constants.DISPATCH_TIME))
				millingTime = null;
			else
				millingTime = new Timestamp(jsonObject.getLong(Constants.DISPATCH_TIME));

			lot.setLotStatus(LotStatus.AT_FACTORY);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.DISPATCH_TIME, millingTime.toString());
			activity = activityService.save(activity);
		}
		
		if (jsonObject.has(Constants.FINALIZE_MILLING_STATUS)) {

			Boolean finalizeMillingStatus = jsonObject.getBoolean(Constants.FINALIZE_MILLING_STATUS);
			if (finalizeMillingStatus) {
				if (weightArrivingFactory == null || weightLeavingFactory == null || mcArrivingFactory == null
						|| mcLeavingFactory == null || millingTime == null) {
					throw new ValidationException("Update the values first");
				}
				lot.setMillingStatus(ActionStatus.DONE);
				lot.setLotStatus(LotStatus.AT_UNION);

				Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
						Constants.FINALIZE_MILLING_STATUS, ActionStatus.DONE.toString());
				activity = activityService.save(activity);
			}
		}

		update(lot);
		return lot;
	}

	public Lot updateWeightArrivingFactory(String jsonString, HttpServletRequest request)
			throws JsonProcessingException, JSONException, IOException {
		JSONObject jsonObject = new JSONObject(jsonString);

		Long id = jsonObject.getLong("id");
		Lot lot = findById(id);

		float weightArrivingFactory = Float.parseFloat(jsonObject.get(Constants.WEIGHT_ARRIVING_FACTORY).toString());

		lot.setWeightArrivingFactory(weightArrivingFactory);
		lot.setLotStatus(LotStatus.AT_FACTORY);
		lot = update(lot);

		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
				Constants.WEIGHT_LEAVING_COOPERATIVE, weightArrivingFactory + "");
		activity = activityService.save(activity);

		return lot;
	}

	public Lot updateMCArrivingFactory(String jsonString, HttpServletRequest request)
			throws JsonProcessingException, JSONException, IOException {
		JSONObject jsonObject = new JSONObject(jsonString);

		Long id = jsonObject.getLong("id");
		Lot lot = findById(id);

		float mcArrivingFactory = Float.parseFloat(jsonObject.get(Constants.MC_ARRIVING_FACTORY).toString());

		lot.setMcArrivingFactory(mcArrivingFactory);
		lot.setLotStatus(LotStatus.AT_FACTORY);
		lot = update(lot);

		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
				Constants.MC_ARRIVING_FACTORY, mcArrivingFactory + "");
		activity = activityService.save(activity);

		return lot;
	}

	public Lot updateWeightLeavingFactory(String jsonString, HttpServletRequest request)
			throws JsonProcessingException, JSONException, IOException {
		JSONObject jsonObject = new JSONObject(jsonString);

		Long id = jsonObject.getLong("id");
		Lot lot = findById(id);

		float weightLeavingFactory = Float.parseFloat(jsonObject.get(Constants.WEIGHT_LEAVING_FACTORY).toString());

		lot.setWeightLeavingFactory(weightLeavingFactory);
		lot.setLotStatus(LotStatus.AT_FACTORY);
		lot = update(lot);

		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
				Constants.WEIGHT_LEAVING_FACTORY, weightLeavingFactory + "");
		activity = activityService.save(activity);

		return lot;
	}

	public Lot updateMCLeavingFactory(String jsonString, HttpServletRequest request)
			throws JsonProcessingException, JSONException, IOException {
		JSONObject jsonObject = new JSONObject(jsonString);

		Long id = jsonObject.getLong("id");
		Lot lot = findById(id);

		float mcLeavingFactory = Float.parseFloat(jsonObject.get(Constants.MC_LEAVING_FACTORY).toString());

		lot.setMcLeavingFactory(mcLeavingFactory);
		lot.setLotStatus(LotStatus.AT_FACTORY);
		lot = update(lot);

		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
				Constants.MC_LEAVING_FACTORY, mcLeavingFactory + "");
		activity = activityService.save(activity);

		return lot;
	}

	public Lot updateMillingTime(String jsonString, HttpServletRequest request)
			throws JsonProcessingException, JSONException, IOException {
		JSONObject jsonObject = new JSONObject(jsonString);

		Long id = jsonObject.getLong("id");
		Lot lot = findById(id);

		Timestamp millingTime = new Timestamp((Long) jsonObject.get(Constants.MILLING_TIME));

		lot.setMillingTime(millingTime);
		lot.setLotStatus(LotStatus.AT_FACTORY);
		lot = update(lot);

		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
				Constants.MILLING_TIME, millingTime.toString());
		activity = activityService.save(activity);

		return lot;
	}

	public Lot updateOutTurn(String jsonString, HttpServletRequest request)
			throws JsonProcessingException, JSONException, IOException {
		JSONObject jsonObject = new JSONObject(jsonString);

		Long id = jsonObject.getLong("id");
		Lot lot = findById(id);

		String outTurnString = jsonObject.get(Constants.OUT_TURN).toString();
		Float outTurn = Float.valueOf(outTurnString);

		lot.setOutTurn(outTurn);
		lot.setLotStatus(LotStatus.AT_FACTORY);
		lot = update(lot);

		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
				Constants.OUT_TURN, outTurn.toString());
		activity = activityService.save(activity);

		return lot;
	}

	public String dispatchToUnion(String jsonString, HttpServletRequest request)
			throws JsonProcessingException, JSONException, IOException {
		JSONObject jsonObject = new JSONObject(jsonString);
		JSONArray jsonArray = jsonObject.getJSONArray("ids");

		Timestamp dispatchTime = new Timestamp((Long) jsonObject.get(Constants.DISPATCH_TIME));

		for (int i = 0; i < jsonArray.length(); i++) {
			Long lotId = jsonArray.getLong(i);

			Lot lot = findById(lotId);
			lot.setLotStatus(LotStatus.AT_UNION);
			lot = update(lot);

			String userId = UserUtil.getUserDetails(request).getId();
			Timestamp timestamp = new Timestamp(new Date().getTime());
			Activity activity = new Activity(lot.getClass().getSimpleName(), lotId, userId, timestamp,
					Constants.DISPATCH_TIME, dispatchTime.toString());
			activity = activityService.save(activity);
		}
		return "Dispatched to union succesful";
	}

	public Lot updateGRNNumer(String jsonString, HttpServletRequest request)
			throws JsonProcessingException, JSONException, IOException {
		JSONObject jsonObject = new JSONObject(jsonString);

		Long id = jsonObject.getLong("id");
		Lot lot = findById(id);

		String grnNumber = jsonObject.get(Constants.GRN_NUMBER).toString();
		Timestamp grnTimestamp = new Timestamp((Long) jsonObject.get(Constants.GRN_TIME));

		lot.setGrnNumber(grnNumber);
		lot.setLotStatus(LotStatus.AT_UNION);
		lot.setGrnTimestamp(grnTimestamp);
		lot = update(lot);

		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
				Constants.GRN_NUMBER, grnNumber);
		activity = activityService.save(activity);

		return lot;
	}

	public boolean checkForDuplicate(String jsonString) throws JSONException {
		JSONObject jsonObject = new JSONObject(jsonString);
		String grnNumber = jsonObject.get(Constants.GRN_NUMBER).toString();
		try {
			findByPropertyWithCondtion("grnNumber", grnNumber, "=");
		} catch (NoResultException e) {
			return false;
		}
		return true;
	}

	public List<Lot> getByCoCodes(String coCodes, Integer limit, Integer offset) {
		Object[] values = coCodes.split(",");
		Long[] longValues = new Long[values.length];
		for (int i = 0; i < values.length; i++) {
			longValues[i] = Long.parseLong(values[i].toString());
		}
		return dao.getByPropertyfromArray("coCode", longValues, limit, offset, "createdOn desc");
		// return ((LotDao) dao).getByPropertyfromArray("coCode", longValues, lotStatus,
		// limit, offset);
	}

	public List<Lot> getByStatusAndUnion(String lotStatusString, String coCodes, Integer limit, Integer offset) {
		LotStatus lotStatus = LotStatus.fromValue(lotStatusString);
		Object[] values = coCodes.split(",");
		Long[] longValues = new Long[values.length];
		for (int i = 0; i < values.length; i++) {
			longValues[i] = Long.parseLong(values[i].toString());
		}
		return ((LotDao) dao).getByPropertyfromArray("coCode", longValues, lotStatus, limit, offset);
	}

	public List<Long> getLotOrigins(String lotId) {
		return lotCreationService.getLotOrigins(lotId);
	}

	public List<Batch> getByLotId(String lotId, Integer limit, Integer offset) {
		return lotCreationService.getByLotId(lotId, limit, offset);
	}

	public List<Map<String, Object>> getAllWithCupping(Integer limit, Integer offset) {
		List<Lot> lots;
		if (limit == -1 || offset == -1)
			lots = findAll();
		else
			lots = findAll(limit, offset);

		List<Map<String, Object>> lotWithCuppings = new ArrayList<Map<String, Object>>();
		for (Lot lot : lots) {
			Map<String, Object> lotWithCupping = new HashMap<String, Object>();
			List<Cupping> cuppings = cuppingService.getByPropertyWithCondtion("lotId", lot.getId(), "=", -1, -1);
			lotWithCupping.put("lot", lot);
			lotWithCupping.put("cuppings", cuppings);
			lotWithCuppings.add(lotWithCupping);
		}
		return lotWithCuppings;
	}

	public Response finalizeCoopActions(Long id) {
		Lot lot = findById(id);
		if (lot != null) {
			if (lot.getTimeToFactory() == null || lot.getWeightLeavingCooperative() == null
					|| lot.getMcLeavingCooperative() == null)
				return Response.status(Status.NO_CONTENT)
						.entity(new HashMap<String, String>().put("error", "Update all the fields first")).build();

			lot.setCoopStatus(ActionStatus.DONE);
			return Response.ok().entity(lot).build();
		}
		return Response.status(Status.NO_CONTENT)
				.entity(new HashMap<String, String>().put("error", "Error in finalizing the cooperative action"))
				.build();
	}

	public Response finalizeMillingActions(Long id) {
		Lot lot = findById(id);
		if (lot != null) {
			if (lot.getWeightArrivingFactory() == null || lot.getWeightLeavingFactory() == null
					|| lot.getMcArrivingFactory() == null || lot.getMcLeavingFactory() == null
					|| lot.getMillingTime() == null)
				return Response.status(Status.NO_CONTENT)
						.entity(new HashMap<String, String>().put("error", "Update all the fields first")).build();

			lot.setMillingStatus(ActionStatus.DONE);
			return Response.ok().entity(lot).build();
		}
		return Response.status(Status.NO_CONTENT)
				.entity(new HashMap<String, String>().put("error", "Error in finalizing the Milling action")).build();
	}

	public Response finalizeFactoryActions(Long id) {
		Lot lot = findById(id);
		if (lot != null) {
			if (lot.getFactoryReportId() == null)
				return Response.status(Status.NO_CONTENT)
						.entity(new HashMap<String, String>().put("error", "Update all the fields first")).build();

			lot.setFactoryStatus(ActionStatus.DONE);
			return Response.ok().entity(lot).build();
		}
		return Response.status(Status.NO_CONTENT)
				.entity(new HashMap<String, String>().put("error", "Error in finalizing the Milling action")).build();
	}
}

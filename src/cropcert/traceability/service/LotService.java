package cropcert.traceability.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import javax.ws.rs.core.HttpHeaders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.user.controller.UserServiceApi;
import com.strandls.user.pojo.Role;
import com.strandls.user.pojo.User;

import cropcert.entities.ApiException;
import cropcert.entities.api.CooperativeApi;
import cropcert.entities.api.UserApi;
import cropcert.entities.model.Cooperative;
import cropcert.traceability.ActionStatus;
import cropcert.traceability.Constants;
import cropcert.traceability.LotStatus;
import cropcert.traceability.dao.LotDao;
import cropcert.traceability.filter.Permissions;
import cropcert.traceability.model.Activity;
import cropcert.traceability.model.Batch;
import cropcert.traceability.model.CoopActionData;
import cropcert.traceability.model.Cupping;
import cropcert.traceability.model.FactoryReport;
import cropcert.traceability.model.GRNNumberData;
import cropcert.traceability.model.Lot;
import cropcert.traceability.model.LotCreation;
import cropcert.traceability.model.LotList;
import cropcert.traceability.model.MillingActionData;
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
	private QualityReportService qualityService;

	@Inject
	private FactoryReportService factoryReportService;

	@Inject
	private UserServiceApi userServiceApi;

	@Inject
	private UserApi userApi;

	@Inject
	private CooperativeApi cooperativeApi;

	@Inject
	public LotService(LotDao dao) {
		super(dao);
	}

	public List<LotList> getLotList(HttpServletRequest request, String coCodes, Integer limit, Integer offset) {

		Object[] values = coCodes.split(",");
		Long[] longValues = new Long[values.length];
		Map<Long, Cooperative> cooperatives = new HashMap<>();
		for (int i = 0; i < values.length; i++) {
			longValues[i] = Long.parseLong(values[i].toString());

			Long coCode = longValues[i];
			Cooperative cooperative = null;

			try {
				cooperative = cooperativeApi.findByCode(coCode);
			} catch (ApiException e) {
				e.printStackTrace();
			}

			cooperatives.put(coCode, cooperative);
		}
		List<Lot> lots = dao.getByPropertyfromArray("coCode", longValues, limit, offset, "createdOn desc");

		List<LotList> lotLists = new ArrayList<>();
		for (Lot lot : lots) {
			FactoryReport factoryReport = null;
			try {
				factoryReport = factoryReportService.findByPropertyWithCondtion("lotId", lot.getId(), "=");
			} catch (NoResultException e) {
				// Don't do anything here.
			}
			Long coCode = lot.getCoCode();
			Cooperative cooperative = cooperatives.get(coCode);
			LotList lotList = new LotList(lot, factoryReport, cooperative);
			lotLists.add(lotList);
		}
		return lotLists;
	}

	public Map<String, Object> getShowPage(Long lotId) {
		Map<String, Object> pageInfo = new HashMap<String, Object>();
		try {
			Lot lot = dao.findById(lotId);
			pageInfo.put("lot", lot);
			List<Activity> activities = activityService.getByLotId(lotId, -1, -1);
			pageInfo.put("activities", activities);
			Set<String> userIds = new HashSet<String>();
			for (Activity activity : activities) {
				userIds.add(activity.getUserId());
			}

			List<User> users = new ArrayList<User>();
			for (String id : userIds) {

				User user = userServiceApi.getUser(id);
				users.add(user);

			}
			pageInfo.put("users", users);
			pageInfo.put("cupping_report", cuppingService.getByPropertyWithCondtion("lot.id", lotId, "=", -1, -1));
			pageInfo.put("quality_report", qualityService.getByPropertyWithCondtion("lotId", lotId, "=", -1, -1));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return pageInfo;
	}

	public Map<String, Object> saveInBulk(String jsonString, HttpServletRequest request)
			throws JsonParseException, JsonMappingException, IOException, JSONException {

		Map<String, Object> result = new HashMap<>();
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
		activityService.save(activity);

		result.put("lot", lot);
		result.put("batches", batches);
		return result;
	}

	public Lot updateCoopAction(CoopActionData coopActionData, HttpServletRequest request) throws JSONException {

		Long id = coopActionData.getId();
		Lot lot = findById(id);

		if (lot == null)
			throw new ValidationException("Lot not found");

		if (ActionStatus.DONE.equals(lot.getCoopStatus()))
			throw new ValidationException("Status is already done");

		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());

		Float weightLeavingCooperative = lot.getWeightLeavingCooperative();
		Float mcLeavingCooperative = lot.getMcLeavingCooperative();
		Timestamp timeToFactory = lot.getTimeToFactory();

		if (isDifferent(weightLeavingCooperative, coopActionData.getWeightLeavingCooperative())) {
			weightLeavingCooperative = coopActionData.getWeightLeavingCooperative();
			lot.setWeightLeavingCooperative(weightLeavingCooperative);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.WEIGHT_LEAVING_COOPERATIVE, weightLeavingCooperative + "");
			activityService.save(activity);
		}

		if (isDifferent(mcLeavingCooperative, coopActionData.getMcLeavingCooperative())) {
			mcLeavingCooperative = coopActionData.getMcLeavingCooperative();
			lot.setMcLeavingCooperative(mcLeavingCooperative);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.MC_LEAVING_COOPERATIVE, mcLeavingCooperative + "");
			activityService.save(activity);
		}

		if (isDifferent(timeToFactory, coopActionData.getTimeToFactory())) {
			timeToFactory = coopActionData.getTimeToFactory();
			lot.setTimeToFactory(timeToFactory);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.TIME_TO_FACTORY, timeToFactory + "");
			activityService.save(activity);
		}

		if (coopActionData.getFinalizeCoopStatus() != null && coopActionData.getFinalizeCoopStatus()) {
			if (weightLeavingCooperative == null || mcLeavingCooperative == null || timeToFactory == null) {
				throw new ValidationException("Update the values first");
			}
			lot.setCoopStatus(ActionStatus.DONE);
			lot.setMillingStatus(ActionStatus.ADD);
			lot.setLotStatus(LotStatus.IN_TRANSPORT);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.FINALIZE_COOP_STATUS, ActionStatus.DONE.toString());
			activityService.save(activity);
		}

		if (weightLeavingCooperative == null && mcLeavingCooperative == null && timeToFactory == null)
			lot.setCoopStatus(ActionStatus.ADD);
		else if (!ActionStatus.DONE.equals(lot.getCoopStatus()))
			lot.setCoopStatus(ActionStatus.EDIT);
		else
			lot.setCoopStatus(ActionStatus.DONE);

		return update(lot);
	}

	public Lot updateMillingAction(MillingActionData millingActionData, HttpServletRequest request) {
		Long id = millingActionData.getId();
		Lot lot = findById(id);

		if (lot == null)
			throw new ValidationException("Lot not found");

		if (ActionStatus.DONE.equals(lot.getMillingStatus()))
			throw new ValidationException("Status is already done");

		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());

		Float weightArrivingFactory = lot.getWeightArrivingFactory();
		Float mcArrivingFactory = lot.getMcArrivingFactory();

		Timestamp millingTime = lot.getMillingTime();
		Long unionCode = lot.getUnionCode();

		Float weightLeavingFactory = lot.getWeightLeavingFactory();
		Float mcLeavingFactory = lot.getMcLeavingFactory();

		// Update the weight arriving factory
		if (isDifferent(lot.getWeightArrivingFactory(), millingActionData.getWeightArrivingFactory())) {
			weightArrivingFactory = millingActionData.getWeightArrivingFactory();
			lot.setWeightArrivingFactory(weightArrivingFactory);
			lot.setLotStatus(LotStatus.AT_FACTORY);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.WEIGHT_ARRIVING_FACTORY, lot.getWeightArrivingFactory() + "");
			activityService.save(activity);
		}

		// Update the weight leaving factory
		if (isDifferent(lot.getWeightLeavingFactory(), millingActionData.getWeightLeavingFactory())) {
			weightLeavingFactory = millingActionData.getWeightLeavingFactory();
			lot.setWeightLeavingFactory(weightLeavingFactory);
			lot.setLotStatus(LotStatus.AT_FACTORY);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.WEIGHT_LEAVING_FACTORY, lot.getWeightLeavingFactory() + "");
			activityService.save(activity);
		}

		// Update the mc arriving factory
		if (isDifferent(lot.getMcArrivingFactory(), millingActionData.getMcArrivingFactory())) {
			mcArrivingFactory = millingActionData.getMcArrivingFactory();
			lot.setMcArrivingFactory(mcArrivingFactory);
			lot.setLotStatus(LotStatus.AT_FACTORY);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.MC_ARRIVING_FACTORY, lot.getMcArrivingFactory() + "");
			activityService.save(activity);
		}

		// Update the mc leaving factory
		if (isDifferent(lot.getMcLeavingFactory(), millingActionData.getMcLeavingFactory())) {
			mcLeavingFactory = millingActionData.getMcLeavingFactory();
			lot.setMcLeavingFactory(mcLeavingFactory);
			lot.setLotStatus(LotStatus.AT_FACTORY);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.MC_LEAVING_FACTORY, lot.getMcLeavingFactory() + "");
			activityService.save(activity);
		}

		if (isDifferent(lot.getMillingTime(), millingActionData.getMillingTime())) {
			millingTime = millingActionData.getMillingTime();
			lot.setMillingTime(millingTime);
			lot.setLotStatus(LotStatus.AT_FACTORY);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.MILLING_TIME, millingActionData.getMillingTime() + "");
			activityService.save(activity);
		}

		if (isDifferent(lot.getUnionCode(), millingActionData.getUnionCode())) {
			unionCode = millingActionData.getUnionCode();
			lot.setUnionCode(unionCode);
			lot.setLotStatus(LotStatus.AT_FACTORY);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.UNION_CODE, millingActionData.getUnionCode() + "");
			activityService.save(activity);
		}

		if (millingActionData.getFinalizeMillingStatus() != null && millingActionData.getFinalizeMillingStatus()) {
			if (weightArrivingFactory == null || weightLeavingFactory == null || mcArrivingFactory == null
					|| mcLeavingFactory == null || millingTime == null || unionCode == null) {
				throw new ValidationException("Update the values first");
			}

			lot.setMillingStatus(ActionStatus.DONE);
			lot.setGrnStatus(ActionStatus.ADD);
			lot.setLotStatus(LotStatus.IN_TRANSPORT);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.FINALIZE_MILLING_STATUS, ActionStatus.DONE.toString());
			activityService.save(activity);
		}

		if (weightArrivingFactory == null && weightLeavingFactory == null && mcArrivingFactory == null
				&& mcLeavingFactory == null && millingTime == null && unionCode == null)
			lot.setMillingStatus(ActionStatus.ADD);
		else if (!ActionStatus.DONE.equals(lot.getMillingStatus()))
			lot.setMillingStatus(ActionStatus.EDIT);
		else
			lot.setMillingStatus(ActionStatus.DONE);

		return update(lot);
	}

	public Lot updateGRNNumer(GRNNumberData grnNumberData, HttpServletRequest request) {

		Long id = grnNumberData.getId();
		Lot lot = findById(id);

		if (lot == null)
			throw new ValidationException("Lot not found");

		if (ActionStatus.DONE.equals(lot.getGrnStatus()))
			throw new ValidationException("Status is already done");

		Timestamp grnTimestamp = lot.getGrnTimestamp();
		String grnNumber = lot.getGrnNumber();
		Float weight = lot.getWeightAtGrn();
		Float mc = lot.getMcAtGrn();
		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());

		if (isDifferent(grnNumber, grnNumberData.getGrnNumber())) {
			grnNumber = grnNumberData.getGrnNumber();

			lot.setGrnNumber(grnNumber);
			lot.setLotStatus(LotStatus.AT_UNION);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.GRN_NUMBER, grnNumber);
			activityService.save(activity);
		}
		if (isDifferent(grnTimestamp, grnNumberData.getGrnTimestamp())) {
			grnTimestamp = grnNumberData.getGrnTimestamp();

			lot.setGrnTimestamp(grnTimestamp);
			lot.setLotStatus(LotStatus.AT_UNION);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.GRN_TIME, grnTimestamp + "");
			activityService.save(activity);
		}
		if (isDifferent(weight, grnNumberData.getWeightAtGrn())) {
			weight = grnNumberData.getWeightAtGrn();
			lot.setWeightAtGrn(weight);
			lot.setLotStatus(LotStatus.AT_UNION);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.WEIGHT_AT_GRN, grnTimestamp + "");
			activityService.save(activity);
		}
		if (isDifferent(mc, grnNumberData.getMcAtGrn())) {
			mc = grnNumberData.getMcAtGrn();
			lot.setMcAtGrn(mc);
			lot.setLotStatus(LotStatus.AT_UNION);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.MC_AT_GRN, grnTimestamp + "");
			activityService.save(activity);
		}
		if (grnNumberData.getFinalizeGrnStatus() != null && grnNumberData.getFinalizeGrnStatus()) {
			if (grnNumber == null || grnTimestamp == null || weight == null || mc == null) {
				throw new ValidationException("Update all value first");
			}

			lot.setGrnStatus(ActionStatus.DONE);
			lot.setFactoryStatus(ActionStatus.ADD);
			lot.setGreenAnalysisStatus(ActionStatus.ADD);
			for (Cupping cupping : lot.getCuppings()) {
				cupping.setStatus(ActionStatus.ADD);
			}

			lot.setLotStatus(LotStatus.AT_UNION);

			Activity activity = new Activity(lot.getClass().getSimpleName(), lot.getId(), userId, timestamp,
					Constants.FINALIZE_GRN_STATUS, ActionStatus.DONE.toString());
			activityService.save(activity);
		}

		if (grnNumber == null && grnTimestamp == null) {
			lot.setGrnStatus(ActionStatus.ADD);
		} else if (!ActionStatus.DONE.equals(lot.getGrnStatus())) {
			lot.setGrnStatus(ActionStatus.EDIT);
		} else {
			lot.setGrnStatus(ActionStatus.DONE);
		}

		lot = update(lot);
		return lot;
	}

	public boolean checkForDuplicate(GRNNumberData grnNumberData) {
		if (grnNumberData.getId() == null)
			throw new ValidationException("Id not found");

		String grnNumber = grnNumberData.getGrnNumber();
		try {
			Lot lot = findByPropertyWithCondtion(Constants.GRN_NUMBER, grnNumber, "=");
			return !lot.getId().equals(grnNumberData.getId());
		} catch (NoResultException e) {
			return false;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Lot> getByCoCodes(HttpServletRequest request, String coCodes, Integer limit, Integer offset) {
		Map<String, Object> userData;

		try {
			userData = userApi.getUser(request.getHeader(HttpHeaders.AUTHORIZATION));
		
		Map<String, Object> user = (Map<String, Object>) userData.get("user");
		User userRole = objectMappper.readValue(objectMappper.writeValueAsString(user), User.class);

		for (Role role : userRole.getRoles()) {
			switch (role.getAuthority()) {
			case Permissions.CO_PERSON:
				Long coCode = Long.parseLong(userData.get("coCode").toString());
				return dao.getByPropertyWithCondtion("coCode", coCode, "=", limit, offset, "createdOn desc");
			case Permissions.UNION:
				Long unionCode = Long.parseLong(userData.get("unionCode").toString());
				return dao.getByPropertyWithCondtion("unionCode", unionCode, "=", limit, offset, "createdOn desc");
			case Permissions.ADMIN:
				Object[] values = coCodes.split(",");
				Long[] longValues = new Long[values.length];
				for (int i = 0; i < values.length; i++) {
					longValues[i] = Long.parseLong(values[i].toString());
				}
				return dao.getByPropertyfromArray("coCode", longValues, limit, offset, "createdOn desc");
			default:
				return new ArrayList<Lot>();
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ArrayList();
		

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

		List<Map<String, Object>> lotWithCuppings = new ArrayList<>();
		for (Lot lot : lots) {
			Map<String, Object> lotWithCupping = new HashMap<>();
			List<Cupping> cuppings = cuppingService.getByPropertyWithCondtion("lotId", lot.getId(), "=", -1, -1);
			lotWithCupping.put("lot", lot);
			lotWithCupping.put("cuppings", cuppings);
			lotWithCuppings.add(lotWithCupping);
		}
		return lotWithCuppings;
	}
}

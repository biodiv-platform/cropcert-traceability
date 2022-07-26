package cropcert.traceability.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cropcert.entities.ApiException;
import cropcert.entities.api.UserApi;
import cropcert.traceability.ActionStatus;
import cropcert.traceability.BatchType;
import cropcert.traceability.Constants;
import cropcert.traceability.dao.BatchDao;
import cropcert.traceability.filter.Permissions;
import cropcert.traceability.model.Activity;
import cropcert.traceability.model.Batch;
import cropcert.traceability.model.BatchCreation;
import cropcert.traceability.util.UserUtil;
import cropcert.traceability.util.ValidationException;

public class BatchService extends AbstractService<Batch> {

	@Inject
	private ObjectMapper objectMappper;

	@Inject
	private ActivityService activityService;

	@Inject
	private BatchCreationService batchCreationService;

	@Inject
	private BatchDao batchDao;

	@Inject
	private UserApi userApi;

	@Inject
	public BatchService(BatchDao dao) {
		super(dao);
	}

	public Batch save(String jsonString, HttpServletRequest request)
			throws JsonParseException, JsonMappingException, IOException, JSONException, ValidationException {
		JSONObject jsonObject = new JSONObject(jsonString);

		JSONArray farmerContributions = (JSONArray) jsonObject.remove("farmerContributions");

		Batch batch = objectMappper.readValue(jsonObject.toString(), Batch.class);
		batch.setIsDeleted(false);

		if (farmerContributions != null && farmerContributions.length() > 0)
			batchValidation(batch, farmerContributions);

		// update the transfer time stamp
		Timestamp createdOn = batch.getCreatedOn();
		if (createdOn == null) {
			createdOn = new Timestamp(new Date().getTime());
			batch.setCreatedOn(createdOn);
		}
		if (BatchType.DRY.equals(batch.getType())) {
			batch.setIsReadyForLot(true);
			batch.setBatchStatus(ActionStatus.NOTAPPLICABLE);
		} else {
			batch.setIsReadyForLot(false);
			batch.setBatchStatus(ActionStatus.ADD);
		}
		batch = save(batch);

		String userId = UserUtil.getUserDetails(request).getId();

		if (farmerContributions != null)
			for (int i = 0; i < farmerContributions.length(); i++) {
				jsonObject = farmerContributions.getJSONObject(i);
				BatchCreation batchCreation = objectMappper.readValue(jsonObject.toString(), BatchCreation.class);
				batchCreation.setBatchId(batch.getId());
				batchCreation.setUserId(userId);
				if (batchCreation.getTimestamp() == null) {
					batchCreation.setTimestamp(createdOn);
				}
				batchCreationService.save(batchCreation);
			}

		// Update the batch name, with batch id as well
		String batchName = batch.getBatchName() + "_" + batch.getId();
		batch.setBatchName(batchName);
		update(batch);

		/**
		 * save the activity here.
		 */
		Activity activity = new Activity(batch.getClass().getSimpleName(), batch.getId(), userId, createdOn,
				Constants.BATCH_CREATION, batch.getBatchName());
		activity = activityService.save(activity);

		return batch;
	}

	private void batchValidation(Batch batch, JSONArray farmerContributions) throws JSONException, ValidationException {
		// TODO Auto-generated method stub
		float batchWeight = batch.getQuantity();

		float contributionWeight = 0;
		for (int i = 0; i < farmerContributions.length(); i++) {
			contributionWeight += farmerContributions.getJSONObject(i).getDouble("weight");
		}

		if (batchWeight != contributionWeight) {
			throw new ValidationException("Farmer contribution and the batch weight are not matching");
		}
	}

	public Batch updateWetBatch(String jsonString) throws JSONException, ValidationException {
		JSONObject jsonObject = new JSONObject(jsonString);
		Long id = jsonObject.getLong("id");
		Batch batch = findById(id);

		if (batch == null)
			throw new ValidationException("Batch not found");

		if (BatchType.DRY.equals(batch.getType()))
			throw new ValidationException("Found dry batch");

		if (batch.getBatchStatus().equals(ActionStatus.DONE))
			throw new ValidationException("Can't update the complited batch");

		Timestamp startTime = batch.getStartTime();
		Timestamp fermentationEndTime = batch.getFermentationEndTime();
		Timestamp dryingEndTime = batch.getDryingEndTime();
		Float perchmentQuantity = batch.getPerchmentQuantity();

		if (jsonObject.has(Constants.START_TIME)) {
			if (jsonObject.isNull(Constants.START_TIME))
				startTime = null;
			else
				startTime = new Timestamp(jsonObject.getLong(Constants.START_TIME));
		}

		if (jsonObject.has(Constants.FERMENTATION_END_TIME)) {
			if (jsonObject.isNull(Constants.FERMENTATION_END_TIME))
				fermentationEndTime = null;
			else
				fermentationEndTime = new Timestamp(jsonObject.getLong(Constants.FERMENTATION_END_TIME));
		}

		if (jsonObject.has(Constants.DRYING_END_TIME)) {
			if (jsonObject.isNull(Constants.DRYING_END_TIME))
				dryingEndTime = null;
			else
				dryingEndTime = new Timestamp(jsonObject.getLong(Constants.DRYING_END_TIME));
		}

		if (startTime != null) {
			if (fermentationEndTime != null && startTime.compareTo(fermentationEndTime) > 0) {
				throw new ValidationException("Start time can't after fermentation time");
			}
			if (dryingEndTime != null && startTime.compareTo(dryingEndTime) > 0) {
				throw new ValidationException("Start time can't after drying time");
			}
		}
		if (fermentationEndTime != null) {
			if (startTime != null && startTime.compareTo(fermentationEndTime) > 0) {
				throw new ValidationException("Fermentation time can't be before start time");
			}
			if (dryingEndTime != null && fermentationEndTime.compareTo(dryingEndTime) > 0) {
				throw new ValidationException("Fermentation time can't after drying time");
			}
		}
		if (dryingEndTime != null) {
			if (startTime != null && startTime.compareTo(dryingEndTime) > 0) {
				throw new ValidationException("Drying time can't be before start time");
			}
			if (fermentationEndTime != null && fermentationEndTime.compareTo(dryingEndTime) > 0) {
				throw new ValidationException("Drying time can't be before fermentation end time");
			}
		}

		if (jsonObject.has(Constants.PERCHMENT_QUANTITY)) {
			if (jsonObject.isNull(Constants.PERCHMENT_QUANTITY))
				perchmentQuantity = 0f;
			else
				perchmentQuantity = (float) jsonObject.getDouble(Constants.PERCHMENT_QUANTITY);
		}

		batch.setStartTime(startTime);
		batch.setFermentationEndTime(fermentationEndTime);
		batch.setDryingEndTime(dryingEndTime);
		batch.setPerchmentQuantity(perchmentQuantity);

		if (jsonObject.has(Constants.FINALIZE_BATCH) && jsonObject.getBoolean(Constants.FINALIZE_BATCH) == true) {

			if (batch.getPerchmentQuantity() == null || batch.getPerchmentQuantity() == 0) {
				throw new ValidationException("Update the perchment quantity");
			}
			batch.setIsReadyForLot(true);
			batch.setBatchStatus(ActionStatus.DONE);
		}

		if (startTime == null && fermentationEndTime == null && dryingEndTime == null
				&& (perchmentQuantity == null || perchmentQuantity == 0f))
			batch.setBatchStatus(ActionStatus.ADD);
		else if (!ActionStatus.DONE.equals(batch.getBatchStatus()))
			batch.setBatchStatus(ActionStatus.EDIT);

		return update(batch);
	}

	@SuppressWarnings("rawtypes")
	public List getAllBatches(HttpServletRequest request, String objectList, int limit, int offset)
			throws NumberFormatException {

		Map<String, Object> userData = new HashMap<>();
		try {
			userData = userApi.getUser(request.getHeader(HttpHeaders.AUTHORIZATION));
		} catch (ApiException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> user = (Map<String, Object>) userData.get("user");
		String role = (String) user.get("role");

		switch (role) {
		case Permissions.ADMIN:
		case Permissions.CO_PERSON:
			Object[] values = objectList.split(",");
			Long[] ccCodes = new Long[values.length];
			for (int i = 0; i < values.length; i++) {
				ccCodes[i] = Long.parseLong(values[i].toString());
			}
			return batchDao.getBatchesForCooperative(ccCodes, limit, offset);
		case Permissions.UNION:
			Long unionCode = Long.parseLong(userData.get("unionCode").toString());
			return batchDao.getBatchesForUnion(unionCode, limit, offset);
		default:
			break;
		}
		return null;
	}

}

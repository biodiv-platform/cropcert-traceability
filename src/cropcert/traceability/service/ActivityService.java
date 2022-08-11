package cropcert.traceability.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Inject;

import cropcert.traceability.dao.ActivityDao;
import cropcert.traceability.model.Activity;
import cropcert.traceability.model.Lot;

public class ActivityService extends AbstractService<Activity> {

	@Inject
	private ObjectMapper objectMappper;

	@Inject
	public ActivityService(ActivityDao dao) {
		super(dao);
	}

	public Activity save(String jsonString)
			throws JsonParseException, JsonMappingException, IOException, JSONException {
		Activity batch = objectMappper.readValue(jsonString, Activity.class);
		batch.setIsDeleted(false);

		// update the transfer time stamp
		Timestamp transferTimestamp = batch.getTimestamp();
		if (transferTimestamp == null) {
			transferTimestamp = new Timestamp(new Date().getTime());
			batch.setTimestamp(transferTimestamp);
		}
		batch = save(batch);
		return batch;
	}

	public List<Activity> getByLotId(Long lotId, int limit, int offset) {
		List<Activity> activities;
		if (lotId == -1) {
			String[] properties = { "objectType" };
			Object[] values = { Lot.class.getSimpleName() };
			activities = dao.getByMultiplePropertyWithCondtion(properties, values, limit, offset);
		} else {
			String[] properties = { "objectType", "objectId" };
			Object[] values = { Lot.class.getSimpleName(), lotId };
			activities = dao.getByMultiplePropertyWithCondtion(properties, values, limit, offset);
		}

		return activities;
	}
}

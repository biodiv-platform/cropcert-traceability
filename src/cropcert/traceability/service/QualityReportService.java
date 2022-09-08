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
import cropcert.traceability.dao.QualityReportDao;
import cropcert.traceability.model.Activity;
import cropcert.traceability.model.Lot;
import cropcert.traceability.model.QualityReport;
import cropcert.traceability.util.UserUtil;
import cropcert.traceability.util.ValidationException;

public class QualityReportService extends AbstractService<QualityReport> {

	@Inject
	private ObjectMapper objectMappper;

	@Inject
	private LotService lotService;

	@Inject
	private ActivityService activityService;

	@Inject
	public QualityReportService(QualityReportDao dao) {
		super(dao);
	}

	public Map<String, Object> save(HttpServletRequest request, String jsonString)
			throws JsonParseException, JsonMappingException, IOException, JSONException, ValidationException {
		
		ActionStatus greenStatus;
		JSONObject jsonObject = new JSONObject(jsonString);
		if (jsonObject.has(Constants.FINALIZE_GREEN_STATUS)
				&& jsonObject.getBoolean(Constants.FINALIZE_GREEN_STATUS))
			greenStatus = ActionStatus.DONE;
		else
			greenStatus = ActionStatus.EDIT;
		jsonObject.remove(Constants.FINALIZE_GREEN_STATUS);
		
		QualityReport qualityReport = objectMappper.readValue(jsonObject.toString(), QualityReport.class);
		qualityReport.setIsDeleted(false);

		validationCheck(qualityReport);

		qualityReport = save(qualityReport);
		
		Long lotId = qualityReport.getLotId();
		Lot lot = lotService.findById(lotId);
		lot.setGreenAnalysisStatus(greenStatus);
		lot.setGreenAnalysisId(qualityReport.getId());
		lotService.update(lot);

		/**
		 * save the activity here.
		 */
		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(qualityReport.getClass().getSimpleName(), qualityReport.getId(), userId,
				timestamp, Constants.GREEN_ANALYSIS, qualityReport.getLotId() + "");
		activity = activityService.save(activity);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("lot", lot);
		result.put("qualityReport", qualityReport);

		return result;
	}

	public Map<String, Object> update(HttpServletRequest request, String jsonString)
			throws JsonParseException, JsonMappingException, IOException, ValidationException, JSONException {
		ActionStatus greenStatus;
		JSONObject jsonObject = new JSONObject(jsonString);
		if (jsonObject.has(Constants.FINALIZE_GREEN_STATUS)
				&& jsonObject.getBoolean(Constants.FINALIZE_GREEN_STATUS))
			greenStatus = ActionStatus.DONE;
		else
			greenStatus = ActionStatus.EDIT;
		jsonObject.remove(Constants.FINALIZE_GREEN_STATUS);
		
		QualityReport qualityReport = objectMappper.readValue(jsonObject.toString(), QualityReport.class);
		validationCheck(qualityReport);
		qualityReport = update(qualityReport);
		
		Long lotId = qualityReport.getLotId();
		Lot lot = lotService.findById(lotId);
		lot.setGreenAnalysisStatus(greenStatus);
		lot.setGreenAnalysisId(qualityReport.getId());
		lotService.update(lot);
		
		/**
		 * save the activity here.
		 */
		String userId = UserUtil.getUserDetails(request).getId();
		Timestamp timestamp = new Timestamp(new Date().getTime());
		Activity activity = new Activity(qualityReport.getClass().getSimpleName(), qualityReport.getId(), userId,
				timestamp, Constants.GREEN_ANALYSIS, qualityReport.getLotId() + "");
		activity = activityService.save(activity);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("lot", lot);
		result.put("qualityReport", qualityReport);

		return result;
	}

	private void validationCheck(QualityReport qualityReport) throws ValidationException {
		float grading = 100f;
		if (Constants.WET.equals(qualityReport.getCoffeeType())) {
			grading = qualityReport.getGradeAA() + qualityReport.getGradeA() + qualityReport.getGradeB()
					+ qualityReport.getGradeAB() + qualityReport.getGradeC() + qualityReport.getGradePB()
					+ qualityReport.getGradeTriage();
		} else if (Constants.DRY.equals(qualityReport.getCoffeeType())) {
			grading = 100;
		} else {
			throw new ValidationException("Invalid coffee type");
		}
		// Severe defects
		float defects = qualityReport.getFullBlack() + qualityReport.getFullSour() + qualityReport.getPods()
				+ qualityReport.getFungasDamaged() + qualityReport.getEm() + qualityReport.getSevereInsect();
		// Less severe defects
		defects += qualityReport.getPartialBlack() + qualityReport.getPartialSour() + qualityReport.getPatchment()
				+ qualityReport.getFloatersChalky() + qualityReport.getImmature() + qualityReport.getWithered()
				+ qualityReport.getShells() + qualityReport.getBrokenChipped() + qualityReport.getHusks()
				+ qualityReport.getPinHole();

		// Back end check for grading, defects should not be more than grading.
		if (defects > grading) {
			throw new ValidationException("Defects should not be more than grading");
		}
		// update the transfer time stamp
		Timestamp transferTimestamp = qualityReport.getTimestamp();
		if (transferTimestamp == null) {
			transferTimestamp = new Timestamp(new Date().getTime());
			qualityReport.setTimestamp(transferTimestamp);
		}
		Long lotId = qualityReport.getLotId();
		if (lotId == null) {
			throw new ValidationException("Missing lotId");
		}
	}
}

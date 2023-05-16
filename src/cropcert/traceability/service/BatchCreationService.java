package cropcert.traceability.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import cropcert.traceability.dao.BatchCreationDao;
import cropcert.traceability.model.BatchCreation;

public class BatchCreationService extends AbstractService<BatchCreation> {

	@Inject
	public BatchCreationService(BatchCreationDao dao) {
		super(dao);
	}

	public List<Long> getByBatchId(String batchIdString, Integer limit, Integer offset) {
		Long batchId = Long.parseLong(batchIdString);

		List<BatchCreation> batchCreations = getByPropertyWithCondtion("batchId", batchId, "=", limit, offset);

		List<Long> farmers = new ArrayList<>();
		for (int i = 0; i < batchCreations.size(); i++) {
			BatchCreation batchCreation = batchCreations.get(i);
			farmers.add(batchCreation.getFarmerId());
		}

		return farmers;
	}
}

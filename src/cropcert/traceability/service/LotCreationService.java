package cropcert.traceability.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import cropcert.traceability.dao.LotCreationDao;
import cropcert.traceability.model.Batch;
import cropcert.traceability.model.LotCreation;

public class LotCreationService extends AbstractService<LotCreation> {

	@Inject
	private BatchService batchService;

	@Inject
	public LotCreationService(LotCreationDao dao) {
		super(dao);
	}

	public List<Batch> getByLotId(String lotIdString, Integer limit, Integer offset) {
		Long lotId = Long.parseLong(lotIdString);
		List<LotCreation> lotCreations = getByPropertyWithCondtion("lotId", lotId, "=", limit, offset);
		List<Batch> batches = new ArrayList<>();
		for (int i = 0; i < lotCreations.size(); i++) {
			LotCreation lotCreation = lotCreations.get(i);
			Long batchId = lotCreation.getBatchId();
			Batch batch = batchService.findById(batchId);
			batches.add(batch);
		}
		return batches;
	}

	public List<Long> getLotOrigins(String lotIdString) {
		Long lotId = Long.parseLong(lotIdString);
		List<LotCreation> lotCreations = getByPropertyWithCondtion("lotId", lotId, "=", -1, -1);
		Set<Long> ccCodes = new HashSet<>();
		for (int i = 0; i < lotCreations.size(); i++) {
			LotCreation lotCreation = lotCreations.get(i);
			Long batchId = lotCreation.getBatchId();
			Batch batch = batchService.findById(batchId);
			ccCodes.add(batch.getCcCode());
		}
		return new ArrayList<>(ccCodes);
	}
}

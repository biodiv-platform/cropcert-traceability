package cropcert.traceability.model;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import cropcert.traceability.LotStatus;

public class LotList {

	private Long id;
	private String lotName;
	private Long coCode;
	private float inputWeightInFactory;
	private float highGradeWeight;
	private LotStatus lotStatus;
	private Set<Float> qualityScores = new LinkedHashSet<>();
	private String grnNumber;
	private Timestamp createdOn;

	public LotList(Lot lot, FactoryReport factoryReport) {
		this.id = lot.getId();
		this.lotName = lot.getLotName();
		this.coCode = lot.getCoCode();
		if(factoryReport != null) {
			this.inputWeightInFactory = factoryReport.getInputWeight();
			this.highGradeWeight = factoryReport.getHighGradeWeight();
		}
		this.lotStatus = lot.getLotStatus();
		this.qualityScores = getQualityScore(lot.getCuppings());
		this.grnNumber = lot.getGrnNumber();
		this.createdOn = lot.getCreatedOn();
	}

	private Set<Float> getQualityScore(Set<Cupping> cuppings) {
		Set<Float> Scores = new HashSet<>();
		if (cuppings == null || cuppings.isEmpty())
			return Scores;
		for (Cupping cupping : cuppings) {
			float score = cupping.getAcidity() + cupping.getAfterTaste() + cupping.getBalance() + cupping.getBody()
					+ cupping.getCleanCup() - cupping.getFault() + cupping.getFlavour() + cupping.getFragranceAroma()
					+ cupping.getOverAll() + cupping.getSweetness() - cupping.getTaint() + cupping.getUniformity();
			Scores.add(score);
		}
		return Scores;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public Long getCoCode() {
		return coCode;
	}

	public void setCoCode(Long coCode) {
		this.coCode = coCode;
	}

	public float getInputWeightInFactory() {
		return inputWeightInFactory;
	}

	public void setInputWeightInFactory(float inputWeightInFactory) {
		this.inputWeightInFactory = inputWeightInFactory;
	}

	public float getHighGradeWeight() {
		return highGradeWeight;
	}

	public void setHighGradeWeight(float highGradeWeight) {
		this.highGradeWeight = highGradeWeight;
	}

	public LotStatus getLotStatus() {
		return lotStatus;
	}

	public void setLotStatus(LotStatus lotStatus) {
		this.lotStatus = lotStatus;
	}

	public Set<Float> getQualityScores() {
		return qualityScores;
	}

	public void setQualityScores(Set<Float> qualityScores) {
		this.qualityScores = qualityScores;
	}

	public String getGrnNumber() {
		return grnNumber;
	}

	public void setGrnNumber(String grnNumber) {
		this.grnNumber = grnNumber;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

}

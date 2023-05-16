package cropcert.traceability.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import cropcert.traceability.ActionStatus;
import cropcert.traceability.LotStatus;
import io.swagger.annotations.ApiModel;

@Entity
@Table(name = "lot")
@XmlRootElement
@JsonIgnoreProperties
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@ApiModel("Lot")
public class Lot implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1639867883436385824L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lot_id_generator")
	@SequenceGenerator(name = "lot_id_generator", sequenceName = "lot_id_seq", allocationSize = 50)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@Column(name = "lot_name")
	private String lotName;

	@Column(name = "co_code")
	private Long coCode;

	@Column(name = "union_code")
	private Long unionCode;

	@Column(name = "quantity")
	private Float quantity;

	@Column(name = "type")
	private String type;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "time_to_factory")
	private Timestamp timeToFactory;

	@Column(name = "weight_leaving_cooperative")
	private Float weightLeavingCooperative;

	@Column(name = "mc_leaving_cooperative")
	private Float mcLeavingCooperative;

	@Column(name = "coop_status")
	private ActionStatus coopStatus = ActionStatus.ADD;

	@Column(name = "weight_arriving_factory")
	private Float weightArrivingFactory;

	@Column(name = "mc_arriving_factory")
	private Float mcArrivingFactory;

	@Column(name = "milling_time")
	private Timestamp millingTime;

	@Column(name = "weight_Leaving_factory")
	private Float weightLeavingFactory;

	@Column(name = "mc_Leaving_factory")
	private Float mcLeavingFactory;

	@Column(name = "out_turn")
	private Float outTurn;

	@Column(name = "milling_status")
	private ActionStatus millingStatus = ActionStatus.NOTAPPLICABLE;

	@Column(name = "grn_number")
	private String grnNumber;

	@Column(name = "grn_timestamp")
	private Timestamp grnTimestamp;

	@Column(name = "weight_at_grn")
	private Float weightAtGrn;

	@Column(name = "mc_at_grn")
	private Float mcAtGrn;

	@Column(name = "grn_status")
	private ActionStatus grnStatus = ActionStatus.NOTAPPLICABLE;

	@Column(name = "factory_report_id")
	private Long factoryReportId;

	@Column(name = "factory_status")
	private ActionStatus factoryStatus = ActionStatus.NOTAPPLICABLE;

	@Column(name = "green_analysis_id")
	private Long greenAnalysisId;

	@Column(name = "green_analysis_status")
	private ActionStatus greenAnalysisStatus = ActionStatus.NOTAPPLICABLE;

	@OneToMany(mappedBy = "lot", fetch = FetchType.EAGER, targetEntity = Cupping.class)
	@OrderBy("id")
	private Set<Cupping> cuppings = new LinkedHashSet<>();

	@Column(name = "lot_status")
	private LotStatus lotStatus;

	@Column(name = "is_deleted")
	private Boolean isDeleted;

	public Lot() {
	}

	public Lot(Long lotId) {
		this.id = lotId;
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

	public Long getUnionCode() {
		return unionCode;
	}

	public void setUnionCode(Long unionCode) {
		this.unionCode = unionCode;
	}

	public Float getQuantity() {
		return quantity;
	}

	public void setQuantity(Float quantity) {
		this.quantity = quantity;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public Timestamp getTimeToFactory() {
		return timeToFactory;
	}

	public void setTimeToFactory(Timestamp timeToFactory) {
		this.timeToFactory = timeToFactory;
	}

	public Float getWeightLeavingCooperative() {
		return weightLeavingCooperative;
	}

	public void setWeightLeavingCooperative(Float weightLeavingCooperative) {
		this.weightLeavingCooperative = weightLeavingCooperative;
	}

	public Float getMcLeavingCooperative() {
		return mcLeavingCooperative;
	}

	public void setMcLeavingCooperative(Float mcLeavingCooperative) {
		this.mcLeavingCooperative = mcLeavingCooperative;
	}

	public ActionStatus getCoopStatus() {
		return coopStatus;
	}

	public void setCoopStatus(ActionStatus coopStatus) {
		this.coopStatus = coopStatus;
	}

	/*
	 * Factory action
	 */
	public Float getWeightArrivingFactory() {
		return weightArrivingFactory;
	}

	public void setWeightArrivingFactory(Float weightArrivingFactory) {
		this.weightArrivingFactory = weightArrivingFactory;
	}

	public Float getMcArrivingFactory() {
		return mcArrivingFactory;
	}

	public void setMcArrivingFactory(Float mcArrivingFactory) {
		this.mcArrivingFactory = mcArrivingFactory;
	}

	public Timestamp getMillingTime() {
		return millingTime;
	}

	public void setMillingTime(Timestamp millingTime) {
		this.millingTime = millingTime;
	}

	public Float getWeightLeavingFactory() {
		return weightLeavingFactory;
	}

	public void setWeightLeavingFactory(Float weightLeavingFactory) {
		this.weightLeavingFactory = weightLeavingFactory;
	}

	public Float getMcLeavingFactory() {
		return mcLeavingFactory;
	}

	public void setMcLeavingFactory(Float mcLeavingFactory) {
		this.mcLeavingFactory = mcLeavingFactory;
	}

	public Float getOutTurn() {
		return outTurn;
	}

	public void setOutTurn(Float outTurn) {
		this.outTurn = outTurn;
	}

	public ActionStatus getMillingStatus() {
		return millingStatus;
	}

	public void setMillingStatus(ActionStatus millingStatus) {
		this.millingStatus = millingStatus;
	}

	/*
	 * Action at union factory
	 */

	public String getGrnNumber() {
		return grnNumber;
	}

	public void setGrnNumber(String grnNumber) {
		this.grnNumber = grnNumber;
	}

	public Timestamp getGrnTimestamp() {
		return grnTimestamp;
	}

	public void setGrnTimestamp(Timestamp grnTimestamp) {
		this.grnTimestamp = grnTimestamp;
	}

	public Float getWeightAtGrn() {
		return weightAtGrn;
	}

	public void setWeightAtGrn(Float weightAtGrn) {
		this.weightAtGrn = weightAtGrn;
	}

	public Float getMcAtGrn() {
		return mcAtGrn;
	}

	public void setMcAtGrn(Float mcAtGrn) {
		this.mcAtGrn = mcAtGrn;
	}

	public ActionStatus getGrnStatus() {
		return grnStatus;
	}

	public void setGrnStatus(ActionStatus grnStatus) {
		this.grnStatus = grnStatus;
	}

	public Long getFactoryReportId() {
		return factoryReportId;
	}

	public void setFactoryReportId(Long factoryReportId) {
		this.factoryReportId = factoryReportId;
	}

	public ActionStatus getFactoryStatus() {
		return factoryStatus;
	}

	public void setFactoryStatus(ActionStatus factoryStatus) {
		this.factoryStatus = factoryStatus;
	}

	/*
	 * Quality report
	 */
	public Long getGreenAnalysisId() {
		return greenAnalysisId;
	}

	public void setGreenAnalysisId(Long greenAnalysisId) {
		this.greenAnalysisId = greenAnalysisId;
	}

	public ActionStatus getGreenAnalysisStatus() {
		return greenAnalysisStatus;
	}

	public void setGreenAnalysisStatus(ActionStatus greenAnalysisStatus) {
		this.greenAnalysisStatus = greenAnalysisStatus;
	}

	public Set<Cupping> getCuppings() {
		return cuppings;
	}

	public void setCuppings(Set<Cupping> cuppings) {
		this.cuppings = cuppings;
	}

	public LotStatus getLotStatus() {
		return lotStatus;
	}

	public void setLotStatus(LotStatus lotStatus) {
		this.lotStatus = lotStatus;
	}

	public Boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	@Override
	public Lot clone() throws CloneNotSupportedException {
		return (Lot) super.clone();
	}

}

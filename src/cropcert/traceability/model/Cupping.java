package cropcert.traceability.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import cropcert.traceability.ActionStatus;
import io.swagger.annotations.ApiModel;

@Entity
@Table(name = "report_cupping") // , uniqueConstraints = @UniqueConstraint(columnNames = {"lot_id", "cupper"}))
@XmlRootElement
@JsonIgnoreProperties
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@ApiModel("Cupping")
public class Cupping implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1639867883436385824L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cupping_id_generator")
	@SequenceGenerator(name = "cupping_id_generator", sequenceName = "cupping_id_seq", allocationSize = 50)
	@Column(name = "id", updatable = false, nullable = false)
	private Long id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER, targetEntity = Lot.class)
	private Lot lot;

	@Column(name = "lot_name")
	private String lotName;

	@Column(name = "cupping_date")
	private Date date;

	@Column(name = "time_stamp")
	private Timestamp timestamp;

	@Column(name = "cfa")
	private String cfa;

	@Column(name = "cc_name")
	private String ccName;

	@Column(name = "cupper", nullable = false)
	private String cupper;

	@Column(name = "sample_type")
	private String sampleType;

	@Column(name = "fragrance_aroma")
	private float fragranceAroma;

	@Column(name = "flavour")
	private float flavour;

	@Column(name = "acidity")
	private float acidity;

	@Column(name = "body")
	private float body;

	@Column(name = "after_taste")
	private float afterTaste;

	@Column(name = "balance")
	private float balance;

	@Column(name = "sweetness")
	private float sweetness;

	@Column(name = "uniformity")
	private float uniformity;

	@Column(name = "clean_cup")
	private float cleanCup;

	@Column(name = "overAll")
	private float overAll;

	@Column(name = "taint")
	private float taint;

	@Column(name = "fault")
	private float fault;

	@Column(name = "notes")
	private String notes;

	@Column(name = "status")
	private ActionStatus status = ActionStatus.NOTAPPLICABLE;

	@Column(name = "is_deleted")
	private Boolean isDeleted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Lot getLot() {
		return lot;
	}

	public void setLot(Lot lot) {
		this.lot = lot;
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public String getCfa() {
		return cfa;
	}

	public void setCfa(String cfa) {
		this.cfa = cfa;
	}

	public String getCcName() {
		return ccName;
	}

	public void setCcName(String ccName) {
		this.ccName = ccName;
	}

	public String getCupper() {
		return cupper;
	}

	public void setCupper(String cupper) {
		this.cupper = cupper;
	}

	public String getSampleType() {
		return sampleType;
	}

	public void setSampleType(String sampleType) {
		this.sampleType = sampleType;
	}

	public float getFragranceAroma() {
		return fragranceAroma;
	}

	public void setFragranceAroma(float fragranceAroma) {
		this.fragranceAroma = fragranceAroma;
	}

	public float getFlavour() {
		return flavour;
	}

	public void setFlavour(float flavour) {
		this.flavour = flavour;
	}

	public float getAcidity() {
		return acidity;
	}

	public void setAcidity(float acidity) {
		this.acidity = acidity;
	}

	public float getBody() {
		return body;
	}

	public void setBody(float body) {
		this.body = body;
	}

	public float getAfterTaste() {
		return afterTaste;
	}

	public void setAfterTaste(float afterTaste) {
		this.afterTaste = afterTaste;
	}

	public float getBalance() {
		return balance;
	}

	public void setBalance(float balance) {
		this.balance = balance;
	}

	public float getSweetness() {
		return sweetness;
	}

	public void setSweetness(float sweetness) {
		this.sweetness = sweetness;
	}

	public float getUniformity() {
		return uniformity;
	}

	public void setUniformity(float uniformity) {
		this.uniformity = uniformity;
	}

	public float getCleanCup() {
		return cleanCup;
	}

	public void setCleanCup(float cleanCup) {
		this.cleanCup = cleanCup;
	}

	public float getOverAll() {
		return overAll;
	}

	public void setOverAll(float overAll) {
		this.overAll = overAll;
	}

	public float getTaint() {
		return taint;
	}

	public void setTaint(float taint) {
		this.taint = taint;
	}

	public float getFault() {
		return fault;
	}

	public void setFault(float fault) {
		this.fault = fault;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public ActionStatus getStatus() {
		return status;
	}

	public void setStatus(ActionStatus status) {
		this.status = status;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}

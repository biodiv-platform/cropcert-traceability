package cropcert.traceability;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "batchType")
@XmlEnum
public enum BatchType {

	@XmlEnumValue("DRY")
	DRY("DRY"),
	@XmlEnumValue("WET")
	WET("WET");
	
	private String value;
	
	BatchType(String value) {
		this.value = value;
	}
	
	public static BatchType fromValue(String value) {
		for(BatchType pageType : BatchType.values()) {
			if(pageType.value.equals(value))
				return pageType;
		}
		throw new IllegalArgumentException(value);
	}
}

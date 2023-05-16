package cropcert.traceability;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


@XmlType(name = "status")
@XmlEnum
public enum LotStatus {
	
	@XmlEnumValue("AT_COLLECTION_CENTER")
	AT_COLLECTION_CENTER("AT_COLLECTION_CENTER"),
	@XmlEnumValue("AT_CO_OPERATIVE")
	AT_CO_OPERATIVE("AT_CO_OPERATIVE"),
	@XmlEnumValue("IN_TRANSPORT")
	IN_TRANSPORT("IN_TRANSPORT"),
	@XmlEnumValue("AT_FACTORY")
	AT_FACTORY("AT_FACTORY"),
	@XmlEnumValue("AT_UNION")
	AT_UNION("AT_UNION");
	
	private String value;
	
	LotStatus(String value) {
		this.value = value;
	}
	
	public static LotStatus fromValue(String value) {
		for(LotStatus status : LotStatus.values()) {
			if(status.value.equals(value))
				return status;
		}
		throw new IllegalArgumentException(value);
	}
}
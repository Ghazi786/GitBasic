/**
 * 
 */
package  com.jio.crm.dms.modules.upload.helper;

/**
 * @author Ghajnafar.Shahid
 *
 */
@SuppressWarnings("squid:S3066")
public enum UOLColumnEnum {
	ICCID("iccid", "* SIM Number/ICCID(eg. `8991874100888888922)"), 
	CIRCLE("circleId", "* Circle"), 
	TIER("tier", "* Activation Type[Prepaid(1) -Postpaid(2)]"), 
	DEVICE_SERIAL_NUMBER("serialNo", "Device Serial Number"), 
	STATIC_IP("isStaticIpBased", "Static IP Required (Y / N)"),
	DEVICE_ID("deviceId", "* MSISDN"),
	SERVICE_TYPE("serviceType", "Service Type"),
	CARD_TYPE("cardType", "Card Type");

	private String value;

	private String key;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	private UOLColumnEnum(String key, String value) {
		this.key = key;
		this.value = value;
	}
}

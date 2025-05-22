package com.jio.crm.dms.node.es;

//for reference
public class RecordMetaData {
	private long rank;
	private String name;
	private String prmid;
	private String parentid;
	private String circle;
	private String city;
	private String jiopoint;
	private String jiocenter;
	private String productType;

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	private long no_of_sale;
	private long total_amount;

	public long getRank() {
		return rank;
	}

	public void setRank(long rank) {
		this.rank = rank;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCircle() {
		return circle;
	}

	public void setCircle(String circle) {
		this.circle = circle;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPrmid() {
		return prmid;
	}

	public void setPrmid(String prmid) {
		this.prmid = prmid;
	}

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		this.parentid = parentid;
	}

	public String getJiopoint() {
		return jiopoint;
	}

	public void setJiopoint(String jiopoint) {
		this.jiopoint = jiopoint;
	}

	public String getJiocenter() {
		return jiocenter;
	}

	public void setJiocenter(String jiocenter) {
		this.jiocenter = jiocenter;
	}

	public long getNo_of_sale() {
		return no_of_sale;
	}

	public void setNo_of_sale(long no_of_sale) {
		this.no_of_sale = no_of_sale;
	}

	public long getTotal_amount() {
		return total_amount;
	}

	public void setTotal_amount(long total_amount) {
		this.total_amount = total_amount;
	}

}

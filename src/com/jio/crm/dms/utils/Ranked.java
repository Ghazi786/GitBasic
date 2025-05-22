package com.jio.crm.dms.utils;

public class Ranked {

	private long rank;
	private String name;
	private String prmid;
	private String parentprmid;
	private String circle;
	private String city;
	private String jiopoint;
	private String jiocentre;
	private String producttype;
	private long noofsale;
	private long totalamount;

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

	public String getParentprmid() {
		return parentprmid;
	}

	public void setParentprmid(String parentprmid) {
		this.parentprmid = parentprmid;
	}

	public String getJiopoint() {
		return jiopoint;
	}

	public void setJiopoint(String jiopoint) {
		this.jiopoint = jiopoint;
	}

	public String getJiocentre() {
		return jiocentre;
	}

	public void setJiocentre(String jiocentre) {
		this.jiocentre = jiocentre;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public long getNoofsale() {
		return noofsale;
	}

	public void setNoofsale(long noofsale) {
		this.noofsale = noofsale;
	}

	public long getTotalamount() {
		return totalamount;
	}

	public void setTotalamount(long totalamount) {
		this.totalamount = totalamount;
	}

	@Override
	public String toString() {
		return "Ranked data [rank=" + rank + ", name=" + name + "]";
	}
}

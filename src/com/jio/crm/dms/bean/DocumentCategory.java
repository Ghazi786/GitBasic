/**
 * 
 */
package com.jio.crm.dms.bean;

import java.util.List;

import com.elastic.search.annotation.Entity;
import com.elastic.search.annotation.Id;

/**
 * @author Ghajnafar.Shahid
 *
 */
@Entity(name = "document_category")
public class DocumentCategory {

	@Id
	private String id;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private List<String> documentCategory;

	public List<String> getDocumentCategory() {
		return documentCategory;
	}

	public void setDocumentCategory(List<String> documentCategory) {
		this.documentCategory = documentCategory;
	}
	

}

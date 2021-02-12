package org.mosip.dataprovider.models;

import java.io.Serializable;
import java.util.List;

public class DynamicFieldModel  implements Serializable{

	 private static final long serialVersionUID = 1L;
	String id;
	String name;
	String langCode;
	String dataType;
	String description;
	String createdBy;
	String updatedBy;
	String createdOn;
	String updatedOn;
	Boolean active;
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	public String getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
	public String getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(String updatedOn) {
		this.updatedOn = updatedOn;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	List<DynamicFieldValueModel> fieldVal;
	
	public List<DynamicFieldValueModel> getFieldVal() {
		return fieldVal;
	}
	public void setFieldVal(List<DynamicFieldValueModel> fieldVal) {
		this.fieldVal = fieldVal;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLangCode() {
		return langCode;
	}
	public void setLangCode(String langcode) {
		this.langCode = langcode;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String datatype) {
		this.dataType = datatype;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
}

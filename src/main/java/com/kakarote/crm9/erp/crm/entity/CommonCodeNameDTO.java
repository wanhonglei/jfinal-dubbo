package com.kakarote.crm9.erp.crm.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用Code Name DTO
 * @author xiaowen.wu
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonCodeNameDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6165683452433128872L;
	
	private String code;
	private String name;

}

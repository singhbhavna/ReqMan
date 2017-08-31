package com.reqman.dao;

import java.util.List;


import com.reqman.vo.RequesttypeVo;

public interface requesttypeMasterInterface {
	
	
public int saverequesttype(String name) throws Exception;
	
	public int saverequesttype(String requesttypeName, Boolean status, String emailId) throws Exception;
	
	public List<RequesttypeVo> getRequesttypeDetails(String emailId) throws Exception;
	
	public RequesttypeVo getUserRequesttypeById(String requesttypeId) throws Exception;
	
	public int updateUserRequesttypeById(String requesttypeId, boolean status) throws Exception;
}
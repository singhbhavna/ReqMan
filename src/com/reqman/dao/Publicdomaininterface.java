package com.reqman.dao;

import java.util.List;

import org.primefaces.model.UploadedFile;

import com.reqman.vo.PublicemaildomainVo;

public interface Publicdomaininterface {

	public int save(List<PublicemaildomainVo> publicdonainList, String userName) throws Exception;

	//public	PublicemaildomainVo getemaildomainfile()throws Exception;

	public int quickrequestcheckval(String emailid)throws Exception;

}

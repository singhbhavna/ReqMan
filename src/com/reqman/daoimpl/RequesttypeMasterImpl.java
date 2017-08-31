package com.reqman.daoimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import com.reqman.common.HibernateUtil;
import com.reqman.dao.requesttypeMasterInterface;
import com.reqman.pojo.Category;
import com.reqman.pojo.Requesttype;
import com.reqman.pojo.Usercategory;
import com.reqman.pojo.Userrequesttype;
import com.reqman.pojo.Users;
import com.reqman.vo.CategoryVo;
import com.reqman.vo.RequesttypeVo;

public class RequesttypeMasterImpl implements requesttypeMasterInterface{

	@Override
	public int saverequesttype(String name) throws Exception {
		// TODO Auto-generated method stub
		 Session session = null;
	        SessionFactory hsf = null;
	        Transaction tx = null;
	    Requesttype requesttype = null;
	        int result = 0;
	        try {
	        	session = HibernateUtil.getSession();
	            tx = session.beginTransaction();
	            requesttype = (Requesttype)session.createCriteria(Requesttype.class)
	            		.add(Restrictions.eq("name", name.toLowerCase().trim()).ignoreCase())
	            		.uniqueResult();
	            
	            if(requesttype != null && requesttype.getStatus() != null && requesttype.getStatus().booleanValue() == true){
	            	result = 1;
	            }
	            else if(requesttype != null && requesttype.getStatus() != null && requesttype.getStatus().booleanValue() == false){
	            	result = 2;
	            }
	            else
	            {
	            	requesttype=new Requesttype();
	            	requesttype.setName(name);	            
	            	requesttype.setCreatedby("SYSTEM");	            
	            	requesttype.setDatecreated(new Date());
	            	requesttype.setStatus(true);
	            	session.save(requesttype);
	            	tx.commit();
	            	result = 3;
	            }
	        } catch (Exception e) {
	        	if(tx != null)
	            tx.rollback();
	            e.printStackTrace();
	            result = 4;
	            throw new Exception(e);
	        } finally {
	        	if(session != null)
	            session.close();
	        }
			
			return result;
	 	}

	@Override
	public int saverequesttype(String requesttypeName, Boolean status,
			String emailId) throws Exception {
		// TODO Auto-generated method stub
		 Session session = null;
	        SessionFactory hsf = null;
	        Transaction tx = null;
	        Users users = null;
	        int result = 0;
	        Requesttype requesttype = null;
	        Userrequesttype userrequesttype = null;
	        try {
	        	session = HibernateUtil.getSession();
	            tx = session.beginTransaction();
	            users = (Users)session.createCriteria(Users.class)
	            		.add(Restrictions.eq("emailid", emailId.toLowerCase().trim()).ignoreCase())
	            		.uniqueResult();
	            
	            requesttype = (Requesttype)session.createCriteria(Requesttype.class)
	            		.add(Restrictions.eq("name", requesttypeName.toLowerCase().trim()).ignoreCase())
	            		.uniqueResult();
	            
	            if(requesttype != null)
	            {
	            	Userrequesttype userrequesttypeExist = null;
	            	if(requesttype.getUserrequesttypes() != null && requesttype.getUserrequesttypes().size() != 0){
	            		for(Userrequesttype userrequesttypeDB : requesttype.getUserrequesttypes())
	            		{
	            			if(userrequesttypeDB != null && userrequesttypeDB.getUsers() != null && userrequesttypeDB.getUsers().getEmailid() != null){
	            				if(userrequesttypeDB.getUsers().getEmailid().trim().equalsIgnoreCase(emailId))
	            				{
	            					userrequesttypeExist = userrequesttypeDB;
	            					break;
	            				}
	            			}
	            		}
	            	}
	            	
	            	if(userrequesttypeExist != null && userrequesttypeExist.getStatus().equals(true))
	            	{
	            		result = 1;
	            	}
	            	else if(userrequesttypeExist != null && userrequesttypeExist.getStatus().equals(false))
	            	{
	            		result = 2;
	            	}
	            	else
	            	{
	            		userrequesttype = new Userrequesttype();
	                	userrequesttype.setRequesttype(requesttype);
	                	userrequesttype.setUsers(users);
	                	userrequesttype.setStatus(true);
	                	
	                	session.save(userrequesttype);
	                	
	                	result = 3;
	                	tx.commit();
	            	}
	            }
	            else
	            {
	            	requesttype = new Requesttype();
	            	requesttype.setName(requesttypeName.trim());
	            	requesttype.setStatus(status);
	            	requesttype.setCreatedby(emailId);
	            	requesttype.setDatecreated(new Date());
	            	
	            	session.save(requesttype);
	            	
	            	userrequesttype = new Userrequesttype();
	            	userrequesttype.setRequesttype(requesttype);
	            	userrequesttype.setUsers(users);
	            	userrequesttype.setStatus(true);
	            	
	            	session.save(userrequesttype);
	            	
	            	result = 3;
	            	tx.commit();
	            }
	            
	        } catch (Exception e) {
	        	if(tx != null)
	            tx.rollback();
	            e.printStackTrace();
	            result = 3;
	            throw new Exception(e);
	        } finally {
	        	if(session != null)
	            session.close();
	        }
			
			return result;
	 	
		}

	@SuppressWarnings("unchecked")
	public List<RequesttypeVo> getRequesttypeDetails(String emailId)
			throws Exception {
		// TODO Auto-generated method stub
		List<RequesttypeVo> requesttypeList = new ArrayList<RequesttypeVo>();
		Users usersTemp = null;
	    Session session = null;
	    Transaction tx = null;
	    RequesttypeVo requesttypeVo = null;
		try
		{
           	session = HibernateUtil.getSession();
            tx = session.beginTransaction();
            usersTemp = (Users)session.createCriteria(Users.class)
            		.add(Restrictions.eq("emailid", emailId.toLowerCase().trim()).ignoreCase())
            		.uniqueResult();
            
            if(usersTemp != null){
            	
            	int counter = 1;
            	if(usersTemp.getUserrequesttypes() != null && usersTemp.getUserrequesttypes().size() != 0)
            	{
            		for(Userrequesttype userrequesttypeDB : usersTemp.getUserrequesttypes())
            		{
            			if(userrequesttypeDB != null && userrequesttypeDB.getRequesttype() != null 
            					&& userrequesttypeDB.getRequesttype().getStatus() == true)
            			{
            				requesttypeVo = new RequesttypeVo();
            				requesttypeVo.setSrNo(counter);
            				requesttypeVo.setName(userrequesttypeDB.getRequesttype().getName());
            				requesttypeVo.setUserRequesttypeId(userrequesttypeDB.getId());
                			if(userrequesttypeDB.getStatus().equals(true))
                			{
                				requesttypeVo.setStatus("Active");
                			}
                			else
                			{
                				requesttypeVo.setStatus("InActive");
                			}
                			counter++;
                			requesttypeList.add(requesttypeVo);
            			}
            		}
            	}
            	tx.commit();
            }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
        	if(session != null)
            session.close();
	    }

		return requesttypeList;
	}
	

	@Override
	public RequesttypeVo getUserRequesttypeById(String requesttypeId)
			throws Exception {
		// TODO Auto-generated method stub
		Session session = null;
	    Transaction tx = null;
RequesttypeVo requesttypeVo = new RequesttypeVo();
	    Userrequesttype userrequesttype = null;
		try
		{
           	session = HibernateUtil.getSession();
            tx = session.beginTransaction();
            userrequesttype = (Userrequesttype)
            			session.createCriteria(Userrequesttype.class)
            			.add(Restrictions.eq("id", Integer.valueOf(requesttypeId)))
            			.uniqueResult();
            
            int counter = 1;
            if(userrequesttype != null){
            	requesttypeVo.setSrNo(counter);
            	requesttypeVo.setName(userrequesttype.getRequesttype().getName());
            	requesttypeVo.setUserRequesttypeId(userrequesttype.getId());
    			if(userrequesttype.getStatus() == true)
    			{
    				requesttypeVo.setStatus("Active");
    			}
    			else
    			{
    				requesttypeVo.setStatus("InActive");
    			}
    			
    			tx.commit();
            }
 		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(tx != null){
				tx.rollback();
			}
		}
		finally 
		{
        	if(session != null)
            session.close();
	    }

		return requesttypeVo;
	
	}
	@Override
	public int updateUserRequesttypeById(String requesttypeId, boolean status)
			throws Exception {
		// TODO Auto-generated method stub
		Session session = null;
	    Transaction tx = null;
	    Userrequesttype userrequesttype = null;
	    int result = 0;
		try
		{
           	session = HibernateUtil.getSession();
            tx = session.beginTransaction();
            userrequesttype = (Userrequesttype)
            			session.createCriteria(Userrequesttype.class)
            			.add(Restrictions.eq("id", Integer.valueOf(requesttypeId)))
            			.uniqueResult();
            
            if(userrequesttype != null){
            	userrequesttype.setStatus(status);
            	session.update(userrequesttype);
    			tx.commit();
    			result = 1;
            }
 		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(tx != null){
				tx.rollback();
			}
			result = 2;
		}
		finally 
		{
        	if(session != null)
            session.close();
	    }

		return result;
	
	
	}
	

	

}
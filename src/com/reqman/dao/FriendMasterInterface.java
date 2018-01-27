package com.reqman.dao;

import java.util.List;

import org.primefaces.model.chart.PieChartModel;

import com.reqman.pojo.Category;
import com.reqman.pojo.Userfriendlist;
import com.reqman.pojo.Users;
import com.reqman.vo.FriendVo;
import com.reqman.vo.ProjectVo;
import com.reqman.vo.UserVo;



public interface FriendMasterInterface {

	
	public int savefriend(String firstname, String lastname, String emailid,String password, String shortname,String hashkey) throws Exception;
	
	
	public int savefriend(String frienduser, Boolean status, String userName,String friendfirstname,String friendlastname,String password,String friendshortname,String haskey )throws Exception;

	public	List<FriendVo>getUsersDetails(String userName)throws Exception;
	
	
	
public FriendVo getUserFriendById(String friendId) throws Exception;
	
	public int updateUserFriendById(String friendId, boolean status) throws Exception;


	public List<FriendVo> getUsersStatus(String userName)throws Exception;


	public List<FriendVo> getUsersfasleStatus(String userName)throws Exception;


	public int updateFriend(String oldValue, String newValue,
			Integer updatefriendId)throws Exception;


	public List<UserVo> AllUsers(String userName)throws Exception;




	
}

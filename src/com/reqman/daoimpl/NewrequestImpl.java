package com.reqman.daoimpl;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.model.UploadedFile;

import com.reqman.common.HibernateUtil;
import com.reqman.dao.NewrequestInterface;
import com.reqman.daoimpl.query.CountUsersquery;
import com.reqman.daoimpl.query.GetRolequery;
import com.reqman.daoimpl.query.NewRequestquery;
import com.reqman.daoimpl.query.Updaterequestquery;
import com.reqman.pojo.Category;
import com.reqman.pojo.Request;
import com.reqman.pojo.Requestnotes;
import com.reqman.pojo.Usercategory;
import com.reqman.pojo.Userfriendlist;
import com.reqman.pojo.Userproject;
import com.reqman.pojo.Userrequesttype;
import com.reqman.pojo.Users;
import com.reqman.util.Dateconverter;
import com.reqman.util.newRequestsend;
import com.reqman.vo.AdminRequestVo;
import com.reqman.vo.DatasummaryVo;
import com.reqman.vo.MonthlysummeryemailVo;
import com.reqman.vo.NewrequestVo;
import com.reqman.vo.QuickcreaterequestVo;
import com.reqman.vo.UpdatestatusVo;
import com.reqman.vo.UserVo;
import com.reqman.vo.dailyDuedatewisesendRequestVo;
import com.reqman.vo.requestNoteVo;

import java.net.UnknownHostException;

public class NewrequestImpl implements NewrequestInterface {

	private final String schemaName = HibernateUtil.schemaName;
	private static final String Good = null;

	// for save impl
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public int save(String title, String description, Integer usercategory,byte[] bFile, String filedata1, Integer userproject,
			Integer userrequesttype, UploadedFile attachment, String userName, Date completiondate,
			Integer[] userfriendlist, String estimatedeffort, Integer weightage, String priority) throws Exception {

		Session session = null;
		Transaction tx = null;
		int result = 0;
		Request requestNew = null;
		Request requestDB = null;
		Object row = null;
		Integer serialrequestnumber = null;
		SQLQuery query = null;
		String sqlQuery = "";
		String roleId = "";
		Integer userid = 0;
		StringBuffer sb = new StringBuffer();

		List<Request> requestList = new ArrayList<Request>();
		try {

			session = HibernateUtil.getSession();

			// prepare the request list object
			requestList = getRequestDetails(title, description, usercategory, userproject, userrequesttype, attachment,
					userName, completiondate, userfriendlist, session, estimatedeffort, weightage, priority);

			if (requestList != null && requestList.size() != 0) {
				tx = session.beginTransaction();

				sb.append("select max(serialid) FROM ");

				if (schemaName != null && !schemaName.trim().equals("")) {

					sb.append(schemaName);
					sb.append(".");
				}

				sb.append("request as r ");
				sb.append("where createdby= '" + userName + "'");

				sqlQuery = sb.toString();
				query = session.createSQLQuery(sqlQuery);
				row = query.uniqueResult();
				serialrequestnumber = (Integer) row;

				for (Request request : requestList) {
					// checking the request object
					Criteria crit = session.createCriteria(Request.class);
					if (request != null && request.getUsercategory() != null) {
						crit.add(Restrictions.eq("usercategory", request.getUsercategory()));
					}
					if (request != null && request.getUserproject() != null) {
						crit.add(Restrictions.eq("userproject", request.getUserproject()));
					}
					if (request != null && request.getUserrequesttype() != null) {
						crit.add(Restrictions.eq("userrequesttype", request.getUserrequesttype()));
					}

					if (request != null && request.getUserfriendlist() != null) {
						crit.add(Restrictions.eq("userfriendlist", request.getUserfriendlist()));
					}

					crit.add(Restrictions.eq("title", title.trim().toLowerCase()).ignoreCase());

					crit.add(Restrictions.in("requeststatus", new Integer[] { 1, 2, 3, 4, 7 }));

					crit.add(Restrictions.eq("createdby", userName.trim().toLowerCase()).ignoreCase());

					requestDB = (Request) crit.uniqueResult();

					// if request exist then update the request object
					if (requestDB != null) {
						result = 1;
					} else if (requestDB != null && requestDB.getStatus().booleanValue() == false) {
						result = 2;
					}
					// if request not exist then insert the request object
					else {

						if (serialrequestnumber == null) {
							serialrequestnumber = 0;
						}
						serialrequestnumber = serialrequestnumber + 1;

						request.setSerialid(serialrequestnumber);

						session.save(request);
						result = 3;

					}
				}

				tx.commit();
			}

		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			result = 4;
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}

		return result;

	}

	// for save impl
	private List<Request> getRequestDetails(String title, String description, Integer usercategory, Integer userproject,
			Integer userrequesttype, UploadedFile attachment, String userName, Date completiondate,
			Integer[] userfriendlist, Session session, String estimatedeffort, Integer weightage, String priority)
			throws Exception {

		Users users = null;
		Userproject userproject1 = null;
		Usercategory usercategory1 = null;
		Userrequesttype userrequesttype1 = null;
		Userfriendlist userfriendlistTemp = null;
		Request request = null;
		Users usersTemp = null;
		Userfriendlist userfriendlist1 = null;
		Integer frienduserid = null;
		Integer userid = null;
		List<Request> requestList = new ArrayList<Request>();

		try {

			newRequestsend nr = new newRequestsend();

			String projectname = null;
			String categoryname = null;
			String typename = null;
			String friendname = null;
			String friendemailid = null;
			String duedate = null;
			String requestorname = null;

			if (userfriendlist != null && userfriendlist.length != 0) {
				for (int friendlist : userfriendlist) {

					request = new Request();

					projectname = "";
					categoryname = "";
					typename = "";
					friendname = "";
					friendemailid = "";
					duedate = "";
					requestorname = "";

					if (userName != null && !userName.trim().equals("")) {
						users = (Users) session.createCriteria(Users.class)
								.add(Restrictions.eq("emailid", userName.toLowerCase().trim()).ignoreCase())
								.uniqueResult();

						request.setCreatedby(userName.trim());

						if (users.getFirstname() != null && !users.getFirstname().equals("")) {
							requestorname = users.getFirstname().substring(0, 1).toUpperCase()
									+ users.getFirstname().substring(1).toLowerCase();
						} else {
							requestorname = users.getEmailid().substring(0, 1).toUpperCase()
									+ users.getEmailid().substring(1).toLowerCase();
						}

					}

					if (userproject != null) {
						userproject1 = (Userproject) session.createCriteria(Userproject.class)
								.add(Restrictions.eq("id", userproject)).uniqueResult();

						request.setUserproject(userproject1);
						projectname = userproject1.getProject().getName();
					}

					if (userrequesttype != null) {
						userrequesttype1 = (Userrequesttype) session.createCriteria(Userrequesttype.class)
								.add(Restrictions.eq("id", userrequesttype)).uniqueResult();

						request.setUserrequesttype(userrequesttype1);

						typename = userrequesttype1.getRequesttype().getName();
					}

					if (usercategory != null) {
						usercategory1 = (Usercategory) session.createCriteria(Usercategory.class)
								.add(Restrictions.eq("id", usercategory)).uniqueResult();

						request.setUsercategory(usercategory1);

						categoryname = usercategory1.getCategory().getName();

					}

					request.setTitle(title != null ? title.trim() : "");
					request.setCompletionpercentage(0);

					/*
					 * usersTemp = (Users) session.createCriteria(Users.class)
					 * .add(Restrictions.eq("emailid",
					 * userName.toLowerCase().trim())) .uniqueResult();
					 */
					userid = users.getId();

					userfriendlist1 = (Userfriendlist) session.createCriteria(Userfriendlist.class)
							.add(Restrictions.eq("usersByFriendid.id", friendlist))
							.add(Restrictions.eq("usersByUserid.id", userid)).uniqueResult();
					frienduserid = userfriendlist1.getId();

					if (userid == frienduserid) {
						request.setRequeststatus(2);
					} else {
						request.setRequeststatus(1);
					}
					request.setStatus(true);
					request.setDatecreated(new Date());
					request.setDatemodified(new Date());
					request.setModifiedby(userName);
					request.setPriority(priority);
					request.setWeightage(weightage);
					request.setEstimatedeffort(estimatedeffort);

					request.setDescription(description != null ? description.trim() : "");

					if (attachment != null && attachment.getContents() != null && !attachment.getFileName().isEmpty()) {
						request.setAttachment(attachment.getContents());
						  System.out.println("Finished"+attachment.getContents());
						
						request.setAttachmentstatus(true);
					} else {
						request.setAttachmentstatus(false);
					}

					if (attachment != null && attachment.getFileName() != null && !attachment.getFileName().isEmpty()) {
						request.setFilename(attachment.getFileName());
					}

					if (completiondate != null) {
						request.setCompletiondate(completiondate);

						duedate = Dateconverter.convertDateToStringDDMMDDYYYY(completiondate);
					}

					userfriendlistTemp = (Userfriendlist) session.createCriteria(Userfriendlist.class)
							.add(Restrictions.eq("usersByUserid.id", users.getId()))
							.add(Restrictions.eq("usersByFriendid.id", friendlist)).uniqueResult();

					request.setUserfriendlist(userfriendlistTemp);
					friendemailid = userfriendlistTemp.getUsersByFriendid().getEmailid();

					if (userfriendlistTemp.getUsersByFriendid() != null
							&& userfriendlistTemp.getUsersByFriendid().getFirstname() != null
							&& !userfriendlistTemp.getUsersByFriendid().getFirstname().trim().equals("")) {
						friendname = userfriendlistTemp.getUsersByFriendid().getFirstname().substring(0, 1)
								.toUpperCase()
								+ userfriendlistTemp.getUsersByFriendid().getFirstname().substring(1).toLowerCase();
					} else {
						friendname = userfriendlistTemp.getUsersByFriendid().getEmailid().substring(0, 1).toUpperCase()
								+ userfriendlistTemp.getUsersByFriendid().getEmailid().substring(1).toLowerCase();
					}

					nr.createnewrequest(friendemailid, friendname, requestorname, title, description, duedate,
							projectname, categoryname, typename, priority, weightage, estimatedeffort);

					requestList.add(request);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
		return requestList;
	}

	// for display data in the grid
	@SuppressWarnings("unchecked")
	public List<NewrequestVo> getNewrequestDetails(String userName, Date startDate, Date endDate) throws Exception {

		List<NewrequestVo> requestList = new ArrayList<NewrequestVo>();

		List<requestNoteVo> requestnoteList = null;
		Users usersTemp = null;
		Session session = null;
		Transaction tx = null;
		Request request = null;
		NewrequestVo newrequestVo = null;
		requestNoteVo requestnoteVo = null;
		// RequesttypeMasterImpl reinf = new RequesttypeMasterImpl();
		GetRolequery reinf = new GetRolequery();
		List<Integer> requestIdList = new ArrayList<Integer>();
		NewRequestquery newRequestquery = new NewRequestquery();
		String roleName = "";

		try {

			roleName = reinf.getRoleNameByLoginId(userName.toLowerCase());

			requestIdList = reinf.getRequestListByRole(roleName, userName.toLowerCase());

			if (requestIdList != null && requestIdList.size() != 0) {
				session = HibernateUtil.getSession();
				tx = session.beginTransaction();

				Criteria crit = session.createCriteria(Request.class);
				crit.add(Restrictions.in("id", requestIdList));
				// search date range
				if (startDate != null && endDate != null) {
					crit.add(Restrictions.ge("datecreated", Dateconverter.getMinTimeByDate(startDate)));
					crit.add(Restrictions.lt("datecreated", Dateconverter.getMaxTimeByDate(endDate)));

				}

				List<Request> requesPojoList = (List<Request>) crit.list();
				String emailid = "";
				String userCategory = "";
				String userProject = "";
				String userRequestType = "";
				String firstName = "";
				String lastName = "";
				String name = "";
				Integer countvalue = 0;
				Integer attachmentstatus = 0;
				Integer teammembercompletion = null;
				Integer requestperformance = null;

				if (requesPojoList != null && requesPojoList.size() != 0) {

					for (Request requestDB : requesPojoList) {

						if (requestDB.getRequeststatus() == 1 || requestDB.getRequeststatus() == 2
								|| requestDB.getRequeststatus() == 3 || requestDB.getRequeststatus() == 4
								|| requestDB.getRequeststatus() == 5 || requestDB.getRequeststatus() == 7) {
							emailid = "";
							userCategory = "";
							userProject = "";
							userRequestType = "";
							newrequestVo = new NewrequestVo();
							firstName = "";
							lastName = "";
							name = "";

							countvalue = newRequestquery.countRequestnoteStatus(requestDB.getId());
							teammembercompletion = newRequestquery.expectedcompletionpercentage(requestDB.getId());
							attachmentstatus = newRequestquery.attachmentstatustrue(requestDB.getId());
							requestperformance = newRequestquery.performance(requestDB.getId());

							requestnoteList = new ArrayList<requestNoteVo>();

							Hibernate.initialize(requestDB.getUsercategory());
							Hibernate.initialize(requestDB.getUserproject());
							Hibernate.initialize(requestDB.getUserrequesttype());
							Hibernate.initialize(requestDB.getUserfriendlist());
							Hibernate.initialize(requestDB.getRequestnoteses());
							Hibernate.initialize(request);

							if (requestDB != null && requestDB.getUsercategory() != null
									&& requestDB.getUsercategory().getCategory() != null) {
								userCategory = requestDB.getUsercategory().getCategory().getName() != null
										? requestDB.getUsercategory().getCategory().getName() : "";
							}

							if (requestDB != null && requestDB.getUserproject() != null
									&& requestDB.getUserproject().getProject() != null) {
								userProject = requestDB.getUserproject().getProject().getName() != null
										? requestDB.getUserproject().getProject().getName() : "";
							}

							if (requestDB != null && requestDB.getUserrequesttype() != null
									&& requestDB.getUserrequesttype().getRequesttype() != null) {
								userRequestType = requestDB.getUserrequesttype().getRequesttype() != null
										? requestDB.getUserrequesttype().getRequesttype().getName() : "";
							}

							if (requestDB != null && requestDB.getUserfriendlist() != null
									&& requestDB.getUserfriendlist().getUsersByFriendid() != null) {

								emailid = requestDB.getUserfriendlist().getUsersByFriendid().getEmailid() != null
										? requestDB.getUserfriendlist().getUsersByFriendid().getEmailid() : "";
								firstName = requestDB.getUserfriendlist().getUsersByFriendid().getFirstname() != null
										? requestDB.getUserfriendlist().getUsersByFriendid().getFirstname() : "";

								lastName = requestDB.getUserfriendlist().getUsersByFriendid().getLastname() != null
										? requestDB.getUserfriendlist().getUsersByFriendid().getLastname() : "";

								if (firstName != null && !firstName.trim().equals("")) {
									name = firstName.trim();
								}

								if (lastName != null && !lastName.trim().equals("")) {
									name = name + " " + lastName.trim();
								}
							}

							if (countvalue != 0) {

								newrequestVo.setRequesternotescount(countvalue);
							}

							if (teammembercompletion != null && teammembercompletion <= 100) {
								newrequestVo.setExpectedcompletion(teammembercompletion);
							} else if (teammembercompletion != null && teammembercompletion > 100) {
								newrequestVo.setExpectedcompletion(100);
							} else {
								newrequestVo.setExpectedcompletion(0);
							}

							if (attachmentstatus != 0) {

								newrequestVo.setAttachmentstatus(attachmentstatus);
							}
							
							
							if (requestperformance != null && requestperformance <= 100) {
								newrequestVo.setPerformance(requestperformance);
							} else if (requestperformance != null && requestperformance > 100) {
								newrequestVo.setPerformance(100);
							} else {
								newrequestVo.setPerformance(0);
							}
							

							//newrequestVo.setPerformance(requestperformance);
							newrequestVo.setSerialid(requestDB.getSerialid());
							newrequestVo.setTitle(requestDB.getTitle() != null ? requestDB.getTitle().trim() : "");
							newrequestVo.setDescription(
									requestDB.getDescription() != null ? requestDB.getDescription().trim() : "");
							newrequestVo.setChangedate(requestDB.getCompletiondate() != null
									? Dateconverter.convertDateToStringDDMMDDYYYY(requestDB.getCompletiondate()) : "");
							newrequestVo.setCreateddate(requestDB.getDatecreated() != null
									? Dateconverter.convertDateToStringDDMMDDYYYY(requestDB.getDatecreated()) : "");

							if (!name.equalsIgnoreCase("")) {
								newrequestVo.setFriendName(name);
							} else {
								newrequestVo.setFriendName(emailid);
							}
							if (userCategory.equalsIgnoreCase("")) {
								newrequestVo.setUsercategory("General");
							} else {
								newrequestVo.setUsercategory(userCategory);
							}
							if (userProject.equalsIgnoreCase("")) {
								newrequestVo.setUserproject("General");
							} else {
								newrequestVo.setUserproject(userProject);
							}
							if (userRequestType.equalsIgnoreCase("")) {
								newrequestVo.setUserrequesttype("General");
							} else {
								newrequestVo.setUserrequesttype(userRequestType);
							}
							newrequestVo.setCompletionpercentage(requestDB.getCompletionpercentage());
							// newrequestVo.setNoteList(requestDB.getRequestnoteses());

							if (requestDB.getRequeststatus() == 2) {
								newrequestVo.setStage("Accepted");
							} else if (requestDB.getRequeststatus() == 3) {
								newrequestVo.setStage("Returned");
							} else if (requestDB.getRequeststatus() == 1) {
								newrequestVo.setStage("Requested");
							} else if (requestDB.getRequeststatus() == 4) {
								newrequestVo.setStage("In-progress");
							}

							else if (requestDB.getRequeststatus() == 5) {
								newrequestVo.setStage("Completed");
							} else if (requestDB.getRequeststatus() == 6) {
								newrequestVo.setStage("Cancel");
							} else if (requestDB.getRequeststatus() == 7) {
								newrequestVo.setStage("Hold");
							} else if (requestDB.getRequeststatus() == 8) {
								newrequestVo.setStage("Close");
							}

							if (requestDB != null && requestDB.getStatus() != null
									&& requestDB.getStatus().booleanValue() == true) {
								newrequestVo.setStatus("Active");
							} else {
								newrequestVo.setStatus("In-Active");
							}

							newrequestVo.setNewRequestId(requestDB.getId());

							String createdbyfirstname = "";
							String createdbylastname = "";
							String createdbyname = "";
							String createdbyemailid = "";

							if (requestDB.getRequestnoteses() != null && requestDB.getRequestnoteses().size() != 0) {

								for (Requestnotes requestnotes : requestDB.getRequestnoteses()) {
									createdbyfirstname = "";
									createdbylastname = "";
									createdbyname = "";
									createdbyemailid = "";

									if (requestnotes.getCreatedby() != null) {

										usersTemp = (Users) session.createCriteria(Users.class)
												.add(Restrictions.eq("emailid", requestnotes.getCreatedby()))
												.uniqueResult();
										createdbyemailid = usersTemp.getEmailid() != null ? usersTemp.getEmailid() : "";
										createdbyfirstname = usersTemp.getFirstname() != null ? usersTemp.getFirstname()
												: "";

										createdbylastname = usersTemp.getLastname() != null ? usersTemp.getLastname()
												: "";

										if (createdbyfirstname != null && !createdbyfirstname.trim().equals("")) {
											createdbyname = createdbyfirstname.trim();
										}

										if (createdbylastname != null && !createdbylastname.trim().equals("")) {
											createdbyname = createdbyname + " " + createdbylastname.trim();
										} else {
											createdbyname = createdbyemailid;
										}

									}

									requestnoteVo = new requestNoteVo();
									requestnoteVo.setCreatedby(createdbyname);

									// requestnoteVo.setCreatedby(requestnotes.getCreatedby()
									// !=null ?
									// requestnotes.getCreatedby().trim() : ""
									// );
									requestnoteVo.setNoteId(requestnotes.getId());
									requestnoteVo.setMessage(
											requestnotes.getMessage() != null ? requestnotes.getMessage().trim() : "");
									requestnoteVo.setCreatedon(requestnotes.getCreatedon() != null
											? Dateconverter.convertDateToStringDDMMDDYYYY(requestnotes.getCreatedon())
											: "");
									requestnoteVo.setTime(requestnotes.getCreatedon() != null
											? Dateconverter.convertTimeToStringhhmmss(requestnotes.getCreatedon())
											: "");

									requestnoteList.add(requestnoteVo);
									Collections.sort(requestnoteList, requestNoteVo.NoteIdComparator);
								}
							}

							newrequestVo.setNoteList(requestnoteList);
							requestList.add(newrequestVo);

						}

					}
					tx.commit();
				}

			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}

			e.printStackTrace();
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}

		return requestList;
	}

	// for show value in modify page
	@Override
	public NewrequestVo getRequestById(String requestId) throws Exception {
		// TODO Auto-generated method stub

		Session session = null;
		Transaction tx = null;
		NewrequestVo newrequestVo = new NewrequestVo();
		Request request = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			request = (Request) session.createCriteria(Request.class)
					.add(Restrictions.eq("id", Integer.valueOf(requestId))).uniqueResult();
			String emailid = "";
			String userCategory = "";
			String userProject = "";
			String userRequestType = "";
			String status = "";
			String firstName = "";
			String lastName = "";
			String name = "";
			Hibernate.initialize(request);
			if (request != null) {
				if (request.getAttachment() != null && request.getAttachment().length != 0) {
					newrequestVo.setFile(request.getAttachment());
				}

				if (request != null && request.getUsercategory() != null
						&& request.getUsercategory().getCategory() != null) {
					userCategory = request.getUsercategory().getCategory().getName() != null
							? request.getUsercategory().getCategory().getName() : "";
				}

				if (request != null && request.getUserproject() != null
						&& request.getUserproject().getProject() != null) {
					userProject = request.getUserproject().getProject().getName() != null
							? request.getUserproject().getProject().getName() : "";
				}

				if (request != null && request.getUserrequesttype() != null
						&& request.getUserrequesttype().getRequesttype() != null) {
					userRequestType = request.getUserrequesttype().getRequesttype() != null
							? request.getUserrequesttype().getRequesttype().getName() : "";
				}
				if (request.getFilename() != null && !request.getFilename().trim().equals("")) {
					newrequestVo.setFileName(request.getFilename().trim());
				}
				if (request != null && request.getUserfriendlist() != null
						&& request.getUserfriendlist().getUsersByFriendid() != null) {
					emailid = request.getUserfriendlist().getUsersByFriendid().getEmailid() != null
							? request.getUserfriendlist().getUsersByFriendid().getEmailid() : "";

					firstName = request.getUserfriendlist().getUsersByFriendid().getFirstname() != null
							? request.getUserfriendlist().getUsersByFriendid().getFirstname() : "";

					lastName = request.getUserfriendlist().getUsersByFriendid().getLastname() != null
							? request.getUserfriendlist().getUsersByFriendid().getLastname() : "";

					if (firstName != null && !firstName.trim().equals("")) {
						name = firstName.trim();
					}

					if (lastName != null && !lastName.trim().equals("")) {
						name = name + " " + lastName.trim();
					}
				}
				newrequestVo.setTitle(request.getTitle());
				newrequestVo.setNewRequestId(request.getId());

				if (!name.equalsIgnoreCase("")) {
					newrequestVo.setFriendName(name);
				} else {
					newrequestVo.setFriendName(emailid);
				}

				if (userCategory.equalsIgnoreCase("")) {
					newrequestVo.setUsercategory("General");
				} else {
					newrequestVo.setUsercategory(userCategory);
				}
				if (userProject.equalsIgnoreCase("")) {
					newrequestVo.setUserproject("General");
				} else {
					newrequestVo.setUserproject(userProject);
				}
				if (userRequestType.equalsIgnoreCase("")) {
					newrequestVo.setUserrequesttype("General");
				} else {
					newrequestVo.setUserrequesttype(userRequestType);
				}
				newrequestVo.setDescription(request.getDescription());
				newrequestVo.setCompletiondate(request.getCompletiondate());
				newrequestVo.setFileName(request.getFilename());
				newrequestVo.setCompletionpercentage(request.getCompletionpercentage());
				newrequestVo.setRating(request.getRating());
				newrequestVo.setFeedback(request.getFeedback());
				newrequestVo.setPriority(request.getPriority());
				newrequestVo.setWeightage(request.getWeightage());
				newrequestVo.setEstimatedeffort(request.getEstimatedeffort());
				newrequestVo.setActualeffort(request.getActualeffort());
				if (request.getRequeststatus() == 1) {
					newrequestVo.setStage("Requested");
				} else if (request.getRequeststatus() == 2) {
					newrequestVo.setStage("Accepted");
				} else if (request.getRequeststatus() == 3) {
					newrequestVo.setStage("Returned");
				} else if (request.getRequeststatus() == 4) {
					newrequestVo.setStage("In-progress");
				} else if (request.getRequeststatus() == 5) {
					newrequestVo.setStage("Completed");
				} else if (request.getRequeststatus() == 6) {
					newrequestVo.setStage("Cancel");
				} else if (request.getRequeststatus() == 7) {
					newrequestVo.setStage("Hold");
				} else if (request.getRequeststatus() == 8) {
					newrequestVo.setStage("Close");
				}

				if (request.getStatus() == true) {
					newrequestVo.setStatus("Active");
				} else {
					newrequestVo.setStatus("In-Active");
				}
				tx.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			if (session != null)
				session.close();
		}

		return newrequestVo;

	}

	// for update the value
	@Override
	public int updateRequestById(String requestId, Boolean status, String description, Date completiondate,
			UploadedFile attachment, int completionpercentage, Integer stage, String message, String userName,
			Integer userproject, Integer usercategory, Integer userrequesttype, Integer userfriend, Integer rating,
			String feedback, String estimatedeffort, Integer weightage, String priority) throws Exception {
		// TODO Auto-generated method stub
		Session session = null;
		Transaction tx = null;
		Userproject userproject1 = null;
		Usercategory usercategory1 = null;
		Userrequesttype userrequesttype1 = null;
		Userfriendlist userfriend1 = null;
		NewrequestVo newrequestVo = new NewrequestVo();
		Request requestworkflow = null;
		Request request = null;
		int result = 0;
		Requestnotes requestnotes = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			requestworkflow = (Request) session.createCriteria(Request.class)
					.add(Restrictions.eq("id", Integer.valueOf(requestId))).uniqueResult();

			if (requestworkflow != null) {
				requestworkflow.setStatus(status);
				requestworkflow.setDescription(description);

				if (completiondate != null) {
					requestworkflow.setCompletiondate(completiondate);

				}

				if (userproject != null) {
					userproject1 = (Userproject) session.createCriteria(Userproject.class)
							.add(Restrictions.eq("id", userproject)).uniqueResult();

					requestworkflow.setUserproject(userproject1);
				}

				if (userrequesttype != null) {
					userrequesttype1 = (Userrequesttype) session.createCriteria(Userrequesttype.class)
							.add(Restrictions.eq("id", userrequesttype)).uniqueResult();

					requestworkflow.setUserrequesttype(userrequesttype1);
					;
				}

				if (usercategory != null) {
					usercategory1 = (Usercategory) session.createCriteria(Usercategory.class)
							.add(Restrictions.eq("id", usercategory)).uniqueResult();

					requestworkflow.setUsercategory(usercategory1);
				}

				if (userfriend != null) {
					userfriend1 = (Userfriendlist) session.createCriteria(Userfriendlist.class)
							.add(Restrictions.eq("id", userfriend)).uniqueResult();

					requestworkflow.setUserfriendlist(userfriend1);
				}
				if (stage != null && stage == 8) {
					requestworkflow.setRequeststatus(stage);
					requestworkflow.setApproveddate(new Date());
					requestworkflow.setApprovedby(userName);
					requestworkflow.setRating(rating);
					requestworkflow.setFeedback(feedback);
					
				} else {
					requestworkflow.setRequeststatus(stage);
				}
				if (stage == 4) {
					requestworkflow.setCompletionpercentage(completionpercentage);
				}
				requestworkflow.setDatemodified(new Date());
				requestworkflow.setModifiedby(userName);
				
				requestworkflow.setPriority(priority);
				requestworkflow.setWeightage(weightage);
				requestworkflow.setEstimatedeffort(estimatedeffort);
				session.update(requestworkflow);

				if (message != null && !message.trim().equals("")) {
					requestnotes = new Requestnotes();
					requestnotes.setRequest(requestworkflow);
					requestnotes.setMessage(message);
					requestnotes.setCreatedby(userName);
					requestnotes.setCreatedon(new Date());
					requestnotes.setRequeststatus(true);
					requestnotes.setTeammemberstatus(false);
					session.save(requestnotes);

				}
				tx.commit();
				result = 1;

			}
		} catch (Exception e) {
			e.printStackTrace();
			if (tx != null) {
				tx.rollback();
			}
			result = 2;
		} finally {
			if (session != null)
				session.close();
		}

		return result;

	}

	/*
	 * 
	 * @Override public List<NewrequestVo> getallproject(String userName) throws
	 * Exception { // TODO Auto-generated method stub List<NewrequestVo>
	 * requestList = new ArrayList<NewrequestVo>();
	 * 
	 * List<requestNoteVo> requestnoteList = null; Users usersTemp = null;
	 * Session session = null; Transaction tx = null; Request request = null;
	 * NewrequestVo newrequestVo = null; requestNoteVo requestnoteVo = null; try
	 * { session = HibernateUtil.getSession(); tx = session.beginTransaction();
	 * usersTemp = (Users) session.createCriteria(Users.class)
	 * .add(Restrictions.eq("emailid",
	 * userName.toLowerCase().trim()).ignoreCase()).uniqueResult();
	 * 
	 * if (usersTemp != null) {
	 * 
	 * @SuppressWarnings("unchecked") List<Request> requesPojoList =
	 * (List<Request>) session.createCriteria(Request.class)
	 * .add(Restrictions.eq("createdby",
	 * userName.toLowerCase().trim()).ignoreCase()).list();
	 * 
	 * String userCategory = ""; String userProject = ""; String userRequestType
	 * = ""; String firstName = ""; String lastName = ""; String name = ""; if
	 * (requesPojoList != null && requesPojoList.size() != 0) { for (Request
	 * requestDB : requesPojoList) { if (requestDB.getUserproject() != null &&
	 * requestDB.getUserproject().getProjectaccess() == true) {
	 * 
	 * userCategory = ""; userProject = ""; userRequestType = ""; newrequestVo =
	 * new NewrequestVo(); firstName = ""; lastName = ""; name = "";
	 * requestnoteList = new ArrayList<requestNoteVo>();
	 * 
	 * Hibernate.initialize(requestDB.getUsercategory());
	 * Hibernate.initialize(requestDB.getUserproject());
	 * Hibernate.initialize(requestDB.getUserrequesttype());
	 * Hibernate.initialize(requestDB.getUserfriendlist());
	 * Hibernate.initialize(requestDB.getRequestnoteses()); if (requestDB !=
	 * null && requestDB.getUsercategory() != null &&
	 * requestDB.getUsercategory().getCategory() != null) { userCategory =
	 * requestDB.getUsercategory().getCategory().getName() != null ?
	 * requestDB.getUsercategory().getCategory().getName() : ""; }
	 * 
	 * if (requestDB != null && requestDB.getUserproject() != null &&
	 * requestDB.getUserproject().getProject() != null) { userProject =
	 * requestDB.getUserproject().getProject().getName() != null ?
	 * requestDB.getUserproject().getProject().getName() : ""; }
	 * 
	 * if (requestDB != null && requestDB.getUserrequesttype() != null &&
	 * requestDB.getUserrequesttype().getRequesttype() != null) {
	 * userRequestType = requestDB.getUserrequesttype().getRequesttype() != null
	 * ? requestDB.getUserrequesttype().getRequesttype().getName() : ""; }
	 * 
	 * if (requestDB != null && requestDB.getUserfriendlist() != null &&
	 * requestDB.getUserfriendlist().getUsersByFriendid() != null) { firstName =
	 * requestDB.getUserfriendlist().getUsersByFriendid().getFirstname() != null
	 * ? requestDB.getUserfriendlist().getUsersByFriendid().getFirstname() : "";
	 * 
	 * lastName =
	 * requestDB.getUserfriendlist().getUsersByFriendid().getLastname() != null
	 * ? requestDB.getUserfriendlist().getUsersByFriendid().getLastname() : "";
	 * 
	 * if (firstName != null && !firstName.trim().equals("")) { name =
	 * firstName.trim(); }
	 * 
	 * if (lastName != null && !lastName.trim().equals("")) { name = name + " "
	 * + lastName.trim(); } }
	 * 
	 * newrequestVo.setTitle(requestDB.getTitle() != null ?
	 * requestDB.getTitle().trim() : ""); newrequestVo.setDescription(
	 * requestDB.getDescription() != null ? requestDB.getDescription().trim() :
	 * ""); newrequestVo.setChangedate(requestDB.getCompletiondate() != null ?
	 * Dateconverter
	 * .convertDateToStringDDMMDDYYYY(requestDB.getCompletiondate()) : "");
	 * newrequestVo.setFriendName(name);
	 * newrequestVo.setUsercategory(userCategory);
	 * newrequestVo.setUserproject(userProject);
	 * newrequestVo.setUserrequesttype(userRequestType);
	 * newrequestVo.setCompletionpercentage
	 * (requestDB.getCompletionpercentage()); //
	 * newrequestVo.setNoteList(requestDB.getRequestnoteses());
	 * 
	 * if (requestDB.getRequeststatus() == 2) {
	 * newrequestVo.setStage("Accepted"); } else if
	 * (requestDB.getRequeststatus() == 3) { newrequestVo.setStage("Returned");
	 * } else if (requestDB.getRequeststatus() == 1) {
	 * newrequestVo.setStage("Requested"); } else if
	 * (requestDB.getRequeststatus() == 4) {
	 * newrequestVo.setStage("In-progress"); }
	 * 
	 * else if (requestDB.getRequeststatus() == 5) {
	 * newrequestVo.setStage("Complete"); } else if
	 * (requestDB.getRequeststatus() == 6) { newrequestVo.setStage("Cancel"); }
	 * else if (requestDB.getRequeststatus() == 7) {
	 * newrequestVo.setStage("Hold"); } else if (requestDB.getRequeststatus() ==
	 * 8) { newrequestVo.setStage("Close"); }
	 * 
	 * if (requestDB != null && requestDB.getStatus() != null &&
	 * requestDB.getStatus().booleanValue() == true) {
	 * newrequestVo.setStatus("Active"); } else {
	 * newrequestVo.setStatus("In-Active"); }
	 * 
	 * newrequestVo.setNewRequestId(requestDB.getId());
	 * 
	 * if (requestDB.getRequestnoteses() != null &&
	 * requestDB.getRequestnoteses().size() != 0) {
	 * 
	 * for (Requestnotes requestnotes : requestDB.getRequestnoteses()) {
	 * firstName = ""; lastName = ""; name = "";
	 * 
	 * if (usersTemp != null && requestnotes.getCreatedby() != null) { usersTemp
	 * = (Users) session.createCriteria(Users.class)
	 * .add(Restrictions.eq("emailid", requestnotes.getCreatedby()))
	 * .uniqueResult();
	 * 
	 * firstName = usersTemp.getFirstname() != null ? usersTemp.getFirstname() :
	 * "";
	 * 
	 * lastName = usersTemp.getLastname() != null ? usersTemp.getLastname() :
	 * "";
	 * 
	 * if (firstName != null && !firstName.trim().equals("")) { name =
	 * firstName.trim(); }
	 * 
	 * if (lastName != null && !lastName.trim().equals("")) { name = name + " "
	 * + lastName.trim(); }
	 * 
	 * }
	 * 
	 * requestnoteVo = new requestNoteVo();
	 * 
	 * // Collections.sort(requestnoteList,requestNoteVo.NoteIdComparator // );
	 * requestnoteVo.setCreatedby(name); //
	 * requestnoteVo.setCreatedby(requestnotes.getCreatedby() // !=null ? //
	 * requestnotes.getCreatedby().trim() : "" // );
	 * requestnoteVo.setNoteId(requestnotes.getId()); requestnoteVo.setMessage(
	 * requestnotes.getMessage() != null ? requestnotes.getMessage().trim() :
	 * ""); requestnoteVo.setCreatedon(requestnotes.getCreatedon() != null ?
	 * Dateconverter.convertDateToStringDDMMDDYYYY(requestnotes.getCreatedon())
	 * : ""); requestnoteVo.setTime(requestnotes.getCreatedon() != null ?
	 * Dateconverter.convertTimeToStringhhmmss(requestnotes.getCreatedon()) :
	 * "");
	 * 
	 * requestnoteList.add(requestnoteVo); Collections.sort(requestnoteList,
	 * requestNoteVo.NoteIdComparator); } }
	 * 
	 * newrequestVo.setNoteList(requestnoteList); requestList.add(newrequestVo);
	 * } }
	 * 
	 * }
	 * 
	 * tx.commit(); } } catch (Exception e) { if (tx != null) { tx.rollback(); }
	 * 
	 * e.printStackTrace(); throw new Exception(e); } finally { if (session !=
	 * null) session.close(); }
	 * 
	 * return requestList;
	 * 
	 * }
	 */

	// for get grid data and send email
	@Override
	public List<NewrequestVo> getNewrequestDetailsforemail(String userName, String title, String description,
			String userproject, String usercategory, String userrequesttype, String friendname, String changedate,
			Float completionpercentage, Integer stage) throws Exception {
		// TODO Auto-generated method stub
		List<NewrequestVo> requestList = new ArrayList<NewrequestVo>();

		List<requestNoteVo> requestnoteList = null;
		Users usersTemp = null;
		Session session = null;
		Transaction tx = null;
		Request request = null;
		NewrequestVo newrequestVo = null;
		requestNoteVo requestnoteVo = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			usersTemp = (Users) session.createCriteria(Users.class)
					.add(Restrictions.eq("emailid", userName.toLowerCase().trim()).ignoreCase()).uniqueResult();

			if (usersTemp != null) {

				/*
				 * List<Request> requesPojoList = (List<Request>) session
				 * .createCriteria(Request.class)
				 * .add(Restrictions.eq("createdby",
				 * userName.toLowerCase().trim()).ignoreCase()) .list();
				 */
				Criteria crit = session.createCriteria(Request.class);
				crit.add(Restrictions.eq("createdby", userName.toLowerCase().trim()).ignoreCase());
				// search stage in progress=4
				crit.add(Restrictions.in("requeststatus", new Integer[] { 1, 2, 3, 4, 5, 6 }));

				@SuppressWarnings("unchecked")
				List<Request> requesPojoList = (List<Request>) crit.list();

				String userCategory = "";
				String userProject = "";
				String userRequestType = "";
				String firstName = "";
				String lastName = "";
				String name = "";
				String emailid = "";
				if (requesPojoList != null && requesPojoList.size() != 0) {

					for (Request requestDB : requesPojoList) {

						userCategory = "";
						userProject = "";
						userRequestType = "";
						newrequestVo = new NewrequestVo();
						firstName = "";
						lastName = "";
						name = "";
						emailid = "";
						requestnoteList = new ArrayList<requestNoteVo>();
						Hibernate.initialize(requestDB.getUsercategory());
						Hibernate.initialize(requestDB.getUserproject());
						Hibernate.initialize(requestDB.getUserrequesttype());
						Hibernate.initialize(requestDB.getUserfriendlist());
						Hibernate.initialize(requestDB.getRequestnoteses());
						Hibernate.initialize(request);
						if (requestDB != null && requestDB.getUsercategory() != null
								&& requestDB.getUsercategory().getCategory() != null) {
							userCategory = requestDB.getUsercategory().getCategory().getName() != null
									? requestDB.getUsercategory().getCategory().getName() : "";
						}

						if (requestDB != null && requestDB.getUserproject() != null
								&& requestDB.getUserproject().getProject() != null) {
							userProject = requestDB.getUserproject().getProject().getName() != null
									? requestDB.getUserproject().getProject().getName() : "";
						}

						if (requestDB != null && requestDB.getUserrequesttype() != null
								&& requestDB.getUserrequesttype().getRequesttype() != null) {
							userRequestType = requestDB.getUserrequesttype().getRequesttype() != null
									? requestDB.getUserrequesttype().getRequesttype().getName() : "";
						}

						if (requestDB != null && requestDB.getUserfriendlist() != null
								&& requestDB.getUserfriendlist().getUsersByFriendid() != null) {

							emailid = requestDB.getUserfriendlist().getUsersByFriendid().getEmailid() != null
									? requestDB.getUserfriendlist().getUsersByFriendid().getEmailid() : "";
							firstName = requestDB.getUserfriendlist().getUsersByFriendid().getFirstname() != null
									? requestDB.getUserfriendlist().getUsersByFriendid().getFirstname() : "";

							lastName = requestDB.getUserfriendlist().getUsersByFriendid().getLastname() != null
									? requestDB.getUserfriendlist().getUsersByFriendid().getLastname() : "";

							if (firstName != null && !firstName.trim().equals("")) {
								name = firstName.trim();
							}

							if (lastName != null && !lastName.trim().equals("")) {
								name = name + " " + lastName.trim();
							}
						}

						newrequestVo.setTitle(requestDB.getTitle() != null ? requestDB.getTitle().trim() : "");
						newrequestVo.setDescription(
								requestDB.getDescription() != null ? requestDB.getDescription().trim() : "");
						newrequestVo.setChangedate(requestDB.getCompletiondate() != null
								? Dateconverter.convertDateToStringDDMMDDYYYY(requestDB.getCompletiondate()) : "");

						if (!name.equalsIgnoreCase("")) {
							newrequestVo.setFriendName(name);
						} else {
							newrequestVo.setFriendName(emailid);
						}
						newrequestVo.setUsercategory(userCategory);
						newrequestVo.setUserproject(userProject);
						newrequestVo.setUserrequesttype(userRequestType);
						newrequestVo.setCompletionpercentage(requestDB.getCompletionpercentage());
						// newrequestVo.setNoteList(requestDB.getRequestnoteses());

						if (requestDB.getRequeststatus() == 2) {
							newrequestVo.setStage("Accepted");
						} else if (requestDB.getRequeststatus() == 3) {
							newrequestVo.setStage("Returned");
						} else if (requestDB.getRequeststatus() == 1) {
							newrequestVo.setStage("Requested");
						} else if (requestDB.getRequeststatus() == 4) {
							newrequestVo.setStage("In-progress");
						}

						else if (requestDB.getRequeststatus() == 5) {
							newrequestVo.setStage("Completed");
						} else if (requestDB.getRequeststatus() == 6) {
							newrequestVo.setStage("Cancel");
						} else if (requestDB.getRequeststatus() == 7) {
							newrequestVo.setStage("Hold");
						} else if (requestDB.getRequeststatus() == 8) {
							newrequestVo.setStage("Close");
						}

						if (requestDB != null && requestDB.getStatus() != null
								&& requestDB.getStatus().booleanValue() == true) {
							newrequestVo.setStatus("Active");
						} else {
							newrequestVo.setStatus("In-Active");
						}

						newrequestVo.setNewRequestId(requestDB.getId());

						requestList.add(newrequestVo);

					}

					tx.commit();
				}

			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}

			e.printStackTrace();
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}

		return requestList;
	}

	// for display closed or cancled request in the different grid
	@Override
	public List<NewrequestVo> getColserequestDetails(String userName) throws Exception {
		// TODO Auto-generated method stub
		List<NewrequestVo> requestList = new ArrayList<NewrequestVo>();

		List<requestNoteVo> requestnoteList = null;
		Users usersTemp = null;
		Session session = null;
		Transaction tx = null;
		Request request = null;
		NewrequestVo newrequestVo = null;
		requestNoteVo requestnoteVo = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			usersTemp = (Users) session.createCriteria(Users.class)
					.add(Restrictions.eq("emailid", userName.toLowerCase().trim()).ignoreCase()).uniqueResult();

			if (usersTemp != null) {

				@SuppressWarnings("unchecked")
				List<Request> requesPojoList = (List<Request>) session.createCriteria(Request.class)
						.add(Restrictions.eq("createdby", userName.toLowerCase().trim()).ignoreCase()).list();

				int project = 0;
				int category = 0;
				int requesttype = 0;
				int userfriend = 0;
				String userCategory = "";
				String userProject = "";
				String userRequestType = "";
				String firstName = "";
				String lastName = "";
				String name = "";
				String emailid = "";
				if (requesPojoList != null && requesPojoList.size() != 0) {

					for (Request requestDB : requesPojoList) {

						if (requestDB.getRequeststatus() == 6 || requestDB.getRequeststatus() == 8) {

							project = 0;
							category = 0;
							requesttype = 0;
							userfriend = 0;
							userCategory = "";
							userProject = "";
							userRequestType = "";
							newrequestVo = new NewrequestVo();
							firstName = "";
							lastName = "";
							name = "";
							emailid = "";

							requestnoteList = new ArrayList<requestNoteVo>();
							Hibernate.initialize(requestDB.getUsercategory());
							Hibernate.initialize(requestDB.getUserproject());
							Hibernate.initialize(requestDB.getUserrequesttype());
							Hibernate.initialize(requestDB.getUserfriendlist());
							Hibernate.initialize(requestDB.getRequestnoteses());
							Hibernate.initialize(request);
							if (requestDB != null && requestDB.getUsercategory() != null
									&& requestDB.getUsercategory().getCategory() != null) {
								userCategory = requestDB.getUsercategory().getCategory().getName() != null
										? requestDB.getUsercategory().getCategory().getName() : "";

								category = requestDB.getUsercategory().getCategory().getId();
							}

							if (requestDB != null && requestDB.getUserproject() != null
									&& requestDB.getUserproject().getProject() != null) {
								userProject = requestDB.getUserproject().getProject().getName() != null
										? requestDB.getUserproject().getProject().getName() : "";

								project = requestDB.getUserproject().getProject().getId();
							}

							if (requestDB != null && requestDB.getUserrequesttype() != null
									&& requestDB.getUserrequesttype().getRequesttype() != null) {
								userRequestType = requestDB.getUserrequesttype().getRequesttype().getName() != null
										? requestDB.getUserrequesttype().getRequesttype().getName() : "";

								requesttype = requestDB.getUserrequesttype().getRequesttype().getId();
							}

							if (requestDB != null && requestDB.getUserfriendlist() != null
									&& requestDB.getUserfriendlist().getUsersByFriendid() != null) {
								userfriend = requestDB.getUserfriendlist().getUsersByFriendid().getId();

								firstName = requestDB.getUserfriendlist().getUsersByFriendid().getFirstname() != null
										? requestDB.getUserfriendlist().getUsersByFriendid().getFirstname() : "";

								lastName = requestDB.getUserfriendlist().getUsersByFriendid().getLastname() != null
										? requestDB.getUserfriendlist().getUsersByFriendid().getLastname() : "";
								emailid = requestDB.getUserfriendlist().getUsersByFriendid().getEmailid() != null
										? requestDB.getUserfriendlist().getUsersByFriendid().getEmailid() : "";

								if (firstName != null && !firstName.trim().equals("")) {
									name = firstName.trim();
								}

								if (lastName != null && !lastName.trim().equals("")) {
									name = name + " " + lastName.trim();
								}
							}

							newrequestVo.setTitle(requestDB.getTitle() != null ? requestDB.getTitle().trim() : "");
							newrequestVo.setDescription(
									requestDB.getDescription() != null ? requestDB.getDescription().trim() : "");
							newrequestVo.setChangedate(requestDB.getCompletiondate() != null
									? Dateconverter.convertDateToStringDDMMDDYYYY(requestDB.getCompletiondate()) : "");

							if (!name.equalsIgnoreCase("")) {
								newrequestVo.setFriendName(name);
							} else {
								newrequestVo.setFriendName(emailid);
							}
							if (userCategory.equalsIgnoreCase("")) {
								newrequestVo.setUsercategory("General");
							} else {
								newrequestVo.setUsercategory(userCategory);
							}
							if (userProject.equalsIgnoreCase("")) {
								newrequestVo.setUserproject("General");
							} else {
								newrequestVo.setUserproject(userProject);
							}
							if (userRequestType.equalsIgnoreCase("")) {
								newrequestVo.setUserrequesttype("General");
							} else {
								newrequestVo.setUserrequesttype(userRequestType);
							}
							newrequestVo.setCompletionpercentage(requestDB.getCompletionpercentage());
							newrequestVo.setProject(project);
							newrequestVo.setCategory(category);
							newrequestVo.setRequesttype(requesttype);
							newrequestVo.setUserfriend(userfriend);
							newrequestVo.setModifydate(requestDB.getDatemodified());

							if (requestDB.getRequeststatus() == 2) {
								newrequestVo.setStage("Accepted");
							} else if (requestDB.getRequeststatus() == 3) {
								newrequestVo.setStage("Returned");
							} else if (requestDB.getRequeststatus() == 1) {
								newrequestVo.setStage("Requested");
							} else if (requestDB.getRequeststatus() == 4) {
								newrequestVo.setStage("In-progress");
							}

							else if (requestDB.getRequeststatus() == 5) {
								newrequestVo.setStage("Completed");
							} else if (requestDB.getRequeststatus() == 6) {
								newrequestVo.setStage("Cancelled");
							} else if (requestDB.getRequeststatus() == 7) {
								newrequestVo.setStage("Hold");
							} else if (requestDB.getRequeststatus() == 8) {
								newrequestVo.setStage("Closed");
							}

							if (requestDB != null && requestDB.getStatus() != null
									&& requestDB.getStatus().booleanValue() == true) {
								newrequestVo.setStatus("Active");
							} else {
								newrequestVo.setStatus("In-Active");
							}

							newrequestVo.setNewRequestId(requestDB.getId());
							String createdbyfirstname = "";
							String createdbylastname = "";
							String createdbyname = "";
							String createdbyemailid = "";

							if (requestDB.getRequestnoteses() != null && requestDB.getRequestnoteses().size() != 0) {

								for (Requestnotes requestnotes : requestDB.getRequestnoteses()) {
									createdbyfirstname = "";
									createdbylastname = "";
									createdbyname = "";
									createdbyemailid = "";

									if (requestnotes.getCreatedby() != null) {
										usersTemp = (Users) session.createCriteria(Users.class)
												.add(Restrictions.eq("emailid", requestnotes.getCreatedby()))
												.uniqueResult();

										createdbyemailid = usersTemp.getEmailid() != null ? usersTemp.getEmailid() : "";

										createdbyfirstname = usersTemp.getFirstname() != null ? usersTemp.getFirstname()
												: "";

										createdbylastname = usersTemp.getLastname() != null ? usersTemp.getLastname()
												: "";

										if (createdbyfirstname != null && !createdbyfirstname.trim().equals("")) {
											createdbyname = createdbyfirstname.trim();
										}

										if (createdbylastname != null && !createdbylastname.trim().equals("")) {
											createdbyname = createdbyname + " " + createdbylastname.trim();
										} else {
											createdbyname = createdbyemailid;
										}

									}

									requestnoteVo = new requestNoteVo();

									requestnoteVo.setCreatedby(createdbyname);
									// requestnoteVo.setCreatedby(requestnotes.getCreatedby()
									// !=null ?
									// requestnotes.getCreatedby().trim() : ""
									// );
									requestnoteVo.setNoteId(requestnotes.getId());
									requestnoteVo.setMessage(
											requestnotes.getMessage() != null ? requestnotes.getMessage().trim() : "");
									requestnoteVo.setCreatedon(requestnotes.getCreatedon() != null
											? Dateconverter.convertDateToStringDDMMDDYYYY(requestnotes.getCreatedon())
											: "");
									requestnoteVo.setTime(requestnotes.getCreatedon() != null
											? Dateconverter.convertTimeToStringhhmmss(requestnotes.getCreatedon())
											: "");

									requestnoteList.add(requestnoteVo);
									Collections.sort(requestnoteList, requestNoteVo.NoteIdComparator);
								}
							}

							newrequestVo.setNoteList(requestnoteList);
							requestList.add(newrequestVo);

						}
					}
				}

				tx.commit();

			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}

			e.printStackTrace();
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}

		return requestList;
	}

	// save feedback and rating
	@Override
	public int savefeedbackratingById(String requestId, String userName, Integer rating, String feedback, Integer stage)
			throws Exception {
		// TODO Auto-generated method stub

		Session session = null;
		Transaction tx = null;
		Request requestworkflow = null;
		Request request = null;
		int result = 0;
		Requestnotes requestnotes = null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			requestworkflow = (Request) session.createCriteria(Request.class)
					.add(Restrictions.eq("id", Integer.valueOf(requestId))).uniqueResult();

			if (requestworkflow != null) {

				requestworkflow.setRating(rating);
				requestworkflow.setFeedback(feedback);
				requestworkflow.setRequeststatus(stage);
				session.update(requestworkflow);

				tx.commit();
				result = 1;

			}
		} catch (Exception e) {
			e.printStackTrace();
			if (tx != null) {
				tx.rollback();
			}
			result = 2;
		} finally {
			if (session != null)
				session.close();
		}

		return result;

	}

	// for app admin or account admin request grid
	@SuppressWarnings("unchecked")
	@Override
	public List<AdminRequestVo> getadminrequestDetails(String userName, Date startDate, Date endDate) throws Exception {
		// TODO Auto-generated method stub

		List<AdminRequestVo> requestList = new ArrayList<AdminRequestVo>();

		List<requestNoteVo> requestnoteList = null;
		Users usersTemp = null;
		Session session = null;
		Transaction tx = null;
		Request request = null;
		AdminRequestVo adminrequestVo = null;
		requestNoteVo requestnoteVo = null;
		GetRolequery reinf = new GetRolequery();
		List<Integer> requestIdList = new ArrayList<Integer>();
		NewRequestquery newRequestquery = new NewRequestquery();
		String roleName = "";
		try {

			roleName = reinf.getRoleNameByLoginId(userName.toLowerCase().trim());

			requestIdList = reinf.getAdminRequestListByRole(roleName, userName.toLowerCase().trim());

			if (requestIdList != null && requestIdList.size() != 0) {
				session = HibernateUtil.getSession();
				tx = session.beginTransaction();

				Criteria crit = session.createCriteria(Request.class);
				crit.add(Restrictions.in("id", requestIdList));
				// search date range
				if (startDate != null && endDate != null) {
					crit.add(Restrictions.ge("datecreated", Dateconverter.getMinTimeByDate(startDate)));
					crit.add(Restrictions.lt("datecreated", Dateconverter.getMaxTimeByDate(endDate)));

				}
				List<Request> requesPojoList = (List<Request>) crit.list();
				String emailid = "";
				String userCategory = "";
				String userProject = "";
				String userRequestType = "";
				String firstName = "";
				String lastName = "";
				String name = "";

				Integer attachmentstatus = 0;
				Integer teammembercompletion = null;

				if (requesPojoList != null && requesPojoList.size() != 0) {

					for (Request requestDB : requesPojoList) {

						teammembercompletion = newRequestquery.expectedcompletionpercentage(requestDB.getId());
						attachmentstatus = newRequestquery.attachmentstatustrue(requestDB.getId());

						if (requestDB.getRequeststatus() == 1 || requestDB.getRequeststatus() == 2
								|| requestDB.getRequeststatus() == 3 || requestDB.getRequeststatus() == 4
								|| requestDB.getRequeststatus() == 5 || requestDB.getRequeststatus() == 7) {
							emailid = "";
							userCategory = "";
							userProject = "";
							userRequestType = "";
							adminrequestVo = new AdminRequestVo();
							firstName = "";
							lastName = "";
							name = "";
							requestnoteList = new ArrayList<requestNoteVo>();
							Hibernate.initialize(requestDB.getUsercategory());
							Hibernate.initialize(requestDB.getUserproject());
							Hibernate.initialize(requestDB.getUserrequesttype());
							Hibernate.initialize(requestDB.getUserfriendlist());
							Hibernate.initialize(requestDB.getRequestnoteses());
							Hibernate.initialize(request);
							if (requestDB != null && requestDB.getUsercategory() != null
									&& requestDB.getUsercategory().getCategory() != null) {
								userCategory = requestDB.getUsercategory().getCategory().getName() != null
										? requestDB.getUsercategory().getCategory().getName() : "";
							}

							if (requestDB != null && requestDB.getUserproject() != null
									&& requestDB.getUserproject().getProject() != null) {
								userProject = requestDB.getUserproject().getProject().getName() != null
										? requestDB.getUserproject().getProject().getName() : "";
							}

							if (requestDB != null && requestDB.getUserrequesttype() != null
									&& requestDB.getUserrequesttype().getRequesttype() != null) {
								userRequestType = requestDB.getUserrequesttype().getRequesttype() != null
										? requestDB.getUserrequesttype().getRequesttype().getName() : "";
							}

							if (requestDB != null && requestDB.getUserfriendlist() != null
									&& requestDB.getUserfriendlist().getUsersByFriendid() != null) {

								emailid = requestDB.getUserfriendlist().getUsersByFriendid().getEmailid() != null
										? requestDB.getUserfriendlist().getUsersByFriendid().getEmailid() : "";
								firstName = requestDB.getUserfriendlist().getUsersByFriendid().getFirstname() != null
										? requestDB.getUserfriendlist().getUsersByFriendid().getFirstname() : "";

								lastName = requestDB.getUserfriendlist().getUsersByFriendid().getLastname() != null
										? requestDB.getUserfriendlist().getUsersByFriendid().getLastname() : "";

								if (firstName != null && !firstName.trim().equals("")) {
									name = firstName.trim();
								}

								if (lastName != null && !lastName.trim().equals("")) {
									name = name + " " + lastName.trim();
								}
							}

							if (teammembercompletion != null && teammembercompletion <= 100) {
								adminrequestVo.setExpectedcompletion(teammembercompletion);
							} else if (teammembercompletion != null && teammembercompletion > 100) {
								adminrequestVo.setExpectedcompletion(100);
							}

							else {
								adminrequestVo.setExpectedcompletion(0);
							}

							if (attachmentstatus != 0) {

								adminrequestVo.setAttachmentstatus(attachmentstatus);
							}

							adminrequestVo.setTitle(requestDB.getTitle() != null ? requestDB.getTitle().trim() : "");
							adminrequestVo.setDescription(
									requestDB.getDescription() != null ? requestDB.getDescription().trim() : "");
							adminrequestVo.setChangedate(requestDB.getCompletiondate() != null
									? Dateconverter.convertDateToStringDDMMDDYYYY(requestDB.getCompletiondate()) : "");

							adminrequestVo.setCreateddate(requestDB.getDatecreated() != null
									? Dateconverter.convertDateToStringDDMMDDYYYY(requestDB.getDatecreated()) : "");

							if (!name.equalsIgnoreCase("")) {
								adminrequestVo.setFriendName(name);
							} else {
								adminrequestVo.setFriendName(emailid);
							}
							if (userCategory.equalsIgnoreCase("")) {
								adminrequestVo.setUsercategory("General");
							} else {
								adminrequestVo.setUsercategory(userCategory);
							}
							if (userProject.equalsIgnoreCase("")) {
								adminrequestVo.setUserproject("General");
							} else {
								adminrequestVo.setUserproject(userProject);
							}
							if (userRequestType.equalsIgnoreCase("")) {
								adminrequestVo.setUserrequesttype("General");
							} else {
								adminrequestVo.setUserrequesttype(userRequestType);
							}
							adminrequestVo.setCompletionpercentage(requestDB.getCompletionpercentage());
							// adminrequestVo.setNoteList(requestDB.getRequestnoteses());

							if (requestDB.getRequeststatus() == 2) {
								adminrequestVo.setStage("Accepted");
							} else if (requestDB.getRequeststatus() == 3) {
								adminrequestVo.setStage("Returned");
							} else if (requestDB.getRequeststatus() == 1) {
								adminrequestVo.setStage("Requested");
							} else if (requestDB.getRequeststatus() == 4) {
								adminrequestVo.setStage("In-progress");
							}

							else if (requestDB.getRequeststatus() == 5) {
								adminrequestVo.setStage("Completed");
							} else if (requestDB.getRequeststatus() == 6) {
								adminrequestVo.setStage("Cancel");
							} else if (requestDB.getRequeststatus() == 7) {
								adminrequestVo.setStage("Hold");
							} else if (requestDB.getRequeststatus() == 8) {
								adminrequestVo.setStage("Close");
							}

							if (requestDB != null && requestDB.getStatus() != null
									&& requestDB.getStatus().booleanValue() == true) {
								adminrequestVo.setStatus("Active");
							} else {
								adminrequestVo.setStatus("In-Active");
							}

							adminrequestVo.setNewRequestId(requestDB.getId());

							String createdbyfirstname = "";
							String createdbylastname = "";
							String createdbyname = "";
							String createdbyemailid = "";

							if (requestDB.getRequestnoteses() != null && requestDB.getRequestnoteses().size() != 0) {

								for (Requestnotes requestnotes : requestDB.getRequestnoteses()) {
									createdbyfirstname = "";
									createdbylastname = "";
									createdbyname = "";
									createdbyemailid = "";

									if (requestnotes.getCreatedby() != null) {

										usersTemp = (Users) session.createCriteria(Users.class)
												.add(Restrictions.eq("emailid", requestnotes.getCreatedby()))
												.uniqueResult();
										createdbyemailid = usersTemp.getEmailid() != null ? usersTemp.getEmailid() : "";
										createdbyfirstname = usersTemp.getFirstname() != null ? usersTemp.getFirstname()
												: "";

										createdbylastname = usersTemp.getLastname() != null ? usersTemp.getLastname()
												: "";

										if (createdbyfirstname != null && !createdbyfirstname.trim().equals("")) {
											createdbyname = createdbyfirstname.trim();
										}

										if (createdbylastname != null && !createdbylastname.trim().equals("")) {
											createdbyname = createdbyname + " " + createdbylastname.trim();
										} else {
											createdbyname = createdbyemailid;
										}

									}

									requestnoteVo = new requestNoteVo();
									requestnoteVo.setCreatedby(createdbyname);

									// requestnoteVo.setCreatedby(requestnotes.getCreatedby()
									// !=null ?
									// requestnotes.getCreatedby().trim() : ""
									// );
									requestnoteVo.setNoteId(requestnotes.getId());
									requestnoteVo.setMessage(
											requestnotes.getMessage() != null ? requestnotes.getMessage().trim() : "");
									requestnoteVo.setCreatedon(requestnotes.getCreatedon() != null
											? Dateconverter.convertDateToStringDDMMDDYYYY(requestnotes.getCreatedon())
											: "");
									requestnoteVo.setTime(requestnotes.getCreatedon() != null
											? Dateconverter.convertTimeToStringhhmmss(requestnotes.getCreatedon())
											: "");

									requestnoteList.add(requestnoteVo);
									Collections.sort(requestnoteList, requestNoteVo.NoteIdComparator);
								}
							}

							adminrequestVo.setNoteList(requestnoteList);
							requestList.add(adminrequestVo);

						}

					}
					tx.commit();
				}

			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}

			e.printStackTrace();
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}

		return requestList;
	}

	// for get grid data and send email

	@Override
	public List<MonthlysummeryemailVo> getTotalaveragebyrequester(String emailid) throws Exception {
		// TODO Auto-generated method stub

		List<MonthlysummeryemailVo> requestList = new ArrayList<MonthlysummeryemailVo>();
		Users usersTemp = null;
		Session session = null;
		Transaction tx = null;
		MonthlysummeryemailVo monthlysummeryemailVo = null;
		NewRequestquery newrequestquery = new NewRequestquery();
		Updaterequestquery updaterequestquery = new Updaterequestquery();
		BigInteger Totalreuestreceive = null;
		BigDecimal Totalaveragereceive = null;
		BigInteger Totalreuestraise = null;
		BigDecimal Totalaverageraise = null;
		try {

			Totalreuestreceive = updaterequestquery.gettotalRequestreceive(emailid.toLowerCase().trim());

			Totalaveragereceive = updaterequestquery.getTotalaveragereceive(emailid.toLowerCase().trim());

			Totalreuestraise = newrequestquery.gettotalRequestByLoginId(emailid.toLowerCase().trim());
			Totalaverageraise = newrequestquery.getTotalaveragebyrequester(emailid.toLowerCase().trim());

			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			usersTemp = (Users) session.createCriteria(Users.class)
					.add(Restrictions.eq("emailid", emailid.toLowerCase().trim()).ignoreCase()).uniqueResult();

			if (usersTemp != null) {

				monthlysummeryemailVo = new MonthlysummeryemailVo();

				monthlysummeryemailVo.setTotalrequestraise(Totalreuestraise);
				monthlysummeryemailVo.setFeedbacktoraise(Totalaverageraise);

				monthlysummeryemailVo.setTotalrequestreceive(Totalreuestreceive);
				monthlysummeryemailVo.setFeedbacktoreceive(Totalaveragereceive);

				requestList.add(monthlysummeryemailVo);

				tx.commit();

			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}

			e.printStackTrace();
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}

		return requestList;
	}

	// for get getAdminsummary

	@Override
	public List<MonthlysummeryemailVo> getAdminsummarybyrequester(String emailid) throws Exception {
		// TODO Auto-generated method stub

		List<MonthlysummeryemailVo> requestList = new ArrayList<MonthlysummeryemailVo>();
		Users usersTemp = null;
		Session session = null;
		Transaction tx = null;
		MonthlysummeryemailVo monthlysummeryemailVo = null;
		NewRequestquery newrequestquery = new NewRequestquery();
		Updaterequestquery updaterequestquery = new Updaterequestquery();
		CountUsersquery countUsersquery = new CountUsersquery();

		BigInteger AdminTotalreuestraise = null;
		BigInteger Count_user_accountwise = null;
		BigDecimal Averageratingofallrequestaccount = null;
		try {
			AdminTotalreuestraise = newrequestquery.getAdminRequestsummaryt(emailid.toLowerCase().trim());
			Count_user_accountwise = countUsersquery.getAdminusercount(emailid.toLowerCase().trim());
			Averageratingofallrequestaccount = newrequestquery.getAdminRequestAverger(emailid.toLowerCase().trim());
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			usersTemp = (Users) session.createCriteria(Users.class)
					.add(Restrictions.eq("emailid", emailid.toLowerCase().trim()).ignoreCase()).uniqueResult();

			if (usersTemp != null) {

				monthlysummeryemailVo = new MonthlysummeryemailVo();

				monthlysummeryemailVo.setTotalrequestraise(AdminTotalreuestraise);
				monthlysummeryemailVo.setCountuser(Count_user_accountwise);
				monthlysummeryemailVo.setFeedbacktoraise(Averageratingofallrequestaccount);

				requestList.add(monthlysummeryemailVo);

				tx.commit();

			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}

			e.printStackTrace();
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}

		return requestList;
	}
	
	
	// Send email for daily duedate before two days
	@Override
	public List<dailyDuedatewisesendRequestVo> getduedatesendrequest(String emailid) throws Exception {
		// TODO Auto-generated method stub

		List<dailyDuedatewisesendRequestVo> requestList1 = new ArrayList<dailyDuedatewisesendRequestVo>();
		Users usersTemp = null;
		Users usersTemp1 = null;
		Session session = null;
		Transaction tx = null;
		dailyDuedatewisesendRequestVo dailyduedatewisesendrequestVo = null;
		NewRequestquery newrequestquery = new NewRequestquery();
		List<dailyDuedatewisesendRequestVo> gettotalrequestduedatewise = null;
		try {
			gettotalrequestduedatewise = newrequestquery.getduedaterequest(emailid.toLowerCase().trim());

			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			usersTemp = (Users) session.createCriteria(Users.class)
					.add(Restrictions.eq("emailid", emailid.toLowerCase().trim()).ignoreCase()).uniqueResult();

			if (usersTemp != null) {

				for (dailyDuedatewisesendRequestVo dailysendrequesyDB : gettotalrequestduedatewise) {

					dailyduedatewisesendrequestVo = new dailyDuedatewisesendRequestVo();

					dailyduedatewisesendrequestVo.setTitle(dailysendrequesyDB.getTitle());
					dailyduedatewisesendrequestVo.setTeammemberemailid(dailysendrequesyDB.getTeammemberemailid());
					dailyduedatewisesendrequestVo.setCompletionpercentage(dailysendrequesyDB.getCompletionpercentage());

					String teammemberemailid = dailyduedatewisesendrequestVo.getTeammemberemailid();

					usersTemp1 = (Users) session.createCriteria(Users.class)
							.add(Restrictions.eq("emailid", teammemberemailid.toLowerCase().trim()).ignoreCase())
							.uniqueResult();

					if (usersTemp1 != null) {

						if (usersTemp1.getFirstname() != null) {
							dailyduedatewisesendrequestVo.setName(usersTemp1.getFirstname());
						} else {

							dailyduedatewisesendrequestVo.setName(teammemberemailid);
						}
					}

					requestList1.add(dailyduedatewisesendrequestVo);

				}
				tx.commit();

			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}

			e.printStackTrace();
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}

		return requestList1;
	}

	@Override
	public List<DatasummaryVo> getdatasummary(String userName, Date startDate, Date endDate) throws Exception {
		// TODO Auto-generated method stub
		List<DatasummaryVo> requestList1 = new ArrayList<DatasummaryVo>();
		NewRequestquery newrequestquery = new NewRequestquery();
		CountUsersquery countusersquery = new CountUsersquery();

		DatasummaryVo datasummaryVo = null;
		Session session = null;
		Transaction tx = null;
		int result = 0;
		Users usersTemp = null;
		List<DatasummaryVo> getdatasummaryVo = null;
		BigInteger alluserscount = null;
		try {
			getdatasummaryVo = newrequestquery.gettotalrequest(startDate, endDate);
			alluserscount = countusersquery.getAlluserscount(userName);
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			usersTemp = (Users) session.createCriteria(Users.class)
					.add(Restrictions.eq("emailid", userName.toLowerCase().trim()).ignoreCase()).uniqueResult();

			if (usersTemp != null && getdatasummaryVo != null && getdatasummaryVo.size() != 0) {

				for (DatasummaryVo datasummaryDB : getdatasummaryVo) {

					datasummaryVo = new DatasummaryVo();

					datasummaryVo.setRequest_accepted(datasummaryDB.getRequest_accepted());
					datasummaryVo.setRequest_closed(datasummaryDB.getRequest_closed());
					datasummaryVo.setRequest_completed(datasummaryDB.getRequest_completed());
					datasummaryVo.setRequest_created(datasummaryDB.getRequest_created());
					datasummaryVo.setRequest_modified(datasummaryDB.getRequest_modified());

					datasummaryVo.setUsers(alluserscount);
					requestList1.add(datasummaryVo);

				}
				tx.commit();

			}
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}

			e.printStackTrace();
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}

		return requestList1;
	}
		
	@SuppressWarnings("unused")
	@Override
	public void getRequestStatusUpdateByrequestId(String requestId) throws Exception {
		// TODO Auto-generated method stub

		Requestnotes requestnotes = null;
		Session session = null;
		Transaction tx = null;
		requestNoteVo requestnoteVo = null;
		Request request = null;
		Requestnotes requestnotesDb1 = null;
		Integer getrequestid = 0;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();

			getrequestid = Integer.parseInt(requestId);

			@SuppressWarnings("unchecked")
			List<Requestnotes> requestnotestemp = (List<Requestnotes>) session.createCriteria(Requestnotes.class)
					.add(Restrictions.eq("request.id", getrequestid)).list();

			if (requestnotestemp != null && requestnotestemp.size() != 0) {

				for (Requestnotes requestnotesDb : requestnotestemp) {

					requestnotesDb1 = (Requestnotes) session.createCriteria(Requestnotes.class)
							.add(Restrictions.eq("id", requestnotesDb.getId())).uniqueResult();

					if (requestnotesDb1 != null) {

						// requestnotesDb1.setTeammemberstatus(true);
						requestnotesDb1.setRequeststatus(true);
						session.update(requestnotesDb1);
					}

				}
				tx.commit();
			}

		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}

			e.printStackTrace();
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}
	}

	@SuppressWarnings({ "null", "unused" })
	@Override
	public int quickrequestsave(List<QuickcreaterequestVo> quickrequestList, String userName) throws Exception {
		// TODO Auto-generated method stub

		Session session = null;
		Transaction tx = null;
		int result = 0;
		Request requestDB = null;
		Userfriendlist userfriendlistTemp = null;
		Request request = null;
		Users users = null;
		String friendname = null;
		String friendemailid = null;
		Users usersTemp = null;
		Userfriendlist userfriendlist1 = null;
		Integer frienduserid = null;
		Integer getuserid = null;
		Object row = null;
		Integer serialrequestnumber = null;
		SQLQuery query = null;
		String sqlQuery = "";
		String roleId = "";
		Integer userid = 0;
		StringBuffer sb = new StringBuffer();

		String projectname = "";
		String categoryname = "";
		String typename = "";
		String duedate = "";
		String requestorname = "";
		String title = "";
		String description = "";
		String priority = "";
		Integer weightage = null;
		String estimatedeffort = "";
		Date completiondate = null;

		try {

			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			requestorname = "";

			newRequestsend nr = new newRequestsend();

			sb.append("select max(serialid) FROM ");

			if (schemaName != null && !schemaName.trim().equals("")) {

				sb.append(schemaName);
				sb.append(".");
			}

			sb.append("request as r ");
			sb.append("where createdby= '" + userName + "'");

			sqlQuery = sb.toString();
			query = session.createSQLQuery(sqlQuery);
			row = query.uniqueResult();
			serialrequestnumber = (Integer) row;

			users = (Users) session.createCriteria(Users.class)
					.add(Restrictions.eq("emailid", userName.toLowerCase().trim()).ignoreCase()).uniqueResult();

			if (users.getFirstname() != null && !users.getFirstname().equals("")) {
				requestorname = users.getFirstname().substring(0, 1).toUpperCase()
						+ users.getFirstname().substring(1).toLowerCase();
			} else {
				requestorname = users.getEmailid();
			}

			if (users != null) {

				if (quickrequestList != null && quickrequestList.size() != 0) {

					for (QuickcreaterequestVo quickrequest : quickrequestList) {

						projectname = "";
						categoryname = "";
						typename = "";
						friendname = "";
						friendemailid = "";
						duedate = "";
						title = "";

						userfriendlistTemp = (Userfriendlist) session.createCriteria(Userfriendlist.class)
								.add(Restrictions.eq("usersByUserid.id", users.getId()))
								.add(Restrictions.eq("usersByFriendid.id", quickrequest.getUserfriendlist()))
								.uniqueResult();
						friendemailid = userfriendlistTemp.getUsersByFriendid().getEmailid();

						if (userfriendlistTemp.getUsersByFriendid() != null
								&& userfriendlistTemp.getUsersByFriendid().getFirstname() != null
								&& !userfriendlistTemp.getUsersByFriendid().getFirstname().trim().equals("")) {
							friendname = userfriendlistTemp.getUsersByFriendid().getFirstname().substring(0, 1)
									.toUpperCase()
									+ userfriendlistTemp.getUsersByFriendid().getFirstname().substring(1).toLowerCase();
						} else {
							friendname = userfriendlistTemp.getUsersByFriendid().getEmailid().substring(0, 1)
									.toUpperCase()
									+ userfriendlistTemp.getUsersByFriendid().getEmailid().substring(1).toLowerCase();
						}

						Criteria crit = session.createCriteria(Request.class);

						if (quickrequest != null && quickrequest.getUserfriendlist() != null
								&& userfriendlistTemp != null) {

							crit.add(Restrictions.eq("userfriendlist", userfriendlistTemp));
						}
						crit.add(Restrictions.in("requeststatus", new Integer[] { 1, 2, 3, 4, 7 }));
						crit.add(Restrictions.eq("title", quickrequest.getTitlelist().trim().toLowerCase())
								.ignoreCase());

						crit.add(Restrictions.eq("createdby", userName.trim().toLowerCase()).ignoreCase());

						requestDB = (Request) crit.uniqueResult();

						if (requestDB != null) {
							result = 1;
						} else if (requestDB != null && requestDB.getStatus().booleanValue() == false) {
							result = 2;
						}
						// if request not exist then insert the request object
						else {
							request = new Request();

							if (serialrequestnumber == null) {
								serialrequestnumber = 0;
							}
							serialrequestnumber = serialrequestnumber + 1;

							request.setSerialid(serialrequestnumber);

							request.setUserfriendlist(userfriendlistTemp);
							title = quickrequest.getTitlelist();
							request.setTitle(quickrequest.getTitlelist());
							request.setCreatedby(userName.trim());

							usersTemp = (Users) session.createCriteria(Users.class)
									.add(Restrictions.eq("emailid", userName.toLowerCase().trim())).uniqueResult();
							getuserid = usersTemp.getId();

							frienduserid = userfriendlistTemp.getId();

							if (getuserid == frienduserid) {
								request.setRequeststatus(2);
							} else {
								request.setRequeststatus(1);
							}

							request.setStatus(true);
							request.setDatecreated(new Date());
							request.setDatemodified(new Date());
							request.setModifiedby(userName);
							request.setCompletiondate(quickrequest.getCompletiondate());
							completiondate = quickrequest.getCompletiondate();
							if (completiondate != null) {

								duedate = Dateconverter.convertDateToStringDDMMDDYYYY(completiondate);
							}
							session.save(request);

							nr.createnewrequest(friendemailid, friendname, requestorname, title, description, duedate,
									projectname, categoryname, typename, priority, weightage, estimatedeffort);
							result = 3;

						}

					}

				}
				tx.commit();

			}

		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			result = 4;
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}

		return result;

	}

	@SuppressWarnings("unused")
	@Override
	public int quickrequestcheckval(String titlelist, List<UserVo> selectedUsers, String userName) throws Exception {
		// TODO Auto-generated method stub
		Session session = null;
		Transaction tx = null;
		int result = 0;
		Request requestDB = null;
		Userfriendlist userfriendlistTemp = null;
		Request request = null;
		Users users = null;

		try {

			session = HibernateUtil.getSession();
			tx = session.beginTransaction();

			users = (Users) session.createCriteria(Users.class)
					.add(Restrictions.eq("emailid", userName.toLowerCase().trim()).ignoreCase()).uniqueResult();

			if (users != null) {

				if (titlelist != null && selectedUsers != null && selectedUsers.size() != 0) {

					for (UserVo userVo : selectedUsers) {

						userfriendlistTemp = (Userfriendlist) session.createCriteria(Userfriendlist.class)
								.add(Restrictions.eq("usersByUserid.id", users.getId()))
								.add(Restrictions.eq("usersByFriendid.id", userVo.getUserId())).uniqueResult();

						Criteria crit = session.createCriteria(Request.class);

						crit.add(Restrictions.eq("userfriendlist", userfriendlistTemp));

						crit.add(Restrictions.eq("title", titlelist.trim().toLowerCase()).ignoreCase());

						crit.add(Restrictions.eq("createdby", userName.trim().toLowerCase()).ignoreCase());
						crit.add(Restrictions.in("requeststatus", new Integer[] { 1, 2, 3, 4, 7 }));

						requestDB = (Request) crit.uniqueResult();
					}
					if (requestDB != null) {
						result = 1;
					} else if (requestDB != null && requestDB.getStatus().booleanValue() == false) {
						result = 2;

					}

				} else {
					result = 3;
				}

				tx.commit();
			}

		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			result = 4;
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}

		return result;
	}

	
	public int soundrecoder(byte[] data,String filedata1) throws Exception {

		Session session = null;
		SessionFactory hsf = null;
		Transaction tx = null;
		Users users = null;
		int result = 0;
		Request request = null;
		Userfriendlist	userfriendlist =null;
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			
			
				request = new Request();
				
				if (data != null  && !filedata1.isEmpty()) {
					request.setAttachment(data);
					  System.out.println("Finished"+data);
					
					request.setAttachmentstatus(true);
				} else {
					request.setAttachmentstatus(false);
				}

				if (data != null && filedata1 != null && !filedata1.isEmpty()) {
					request.setFilename(filedata1);
				}

				
				
				
				request.setRequeststatus(1);
				request.setTitle("ragh1wwq1");
				request.setCreatedby("hemantraghav012@gmail.com");
				userfriendlist = (Userfriendlist) session.createCriteria(Userfriendlist.class)
					.add(Restrictions.eq("id", 25)).uniqueResult();

				request.setUserfriendlist(userfriendlist);
		request.setStatus(true);
		request.setDatecreated(new Date());
				session.save(request);

				

				result = 3;
				tx.commit();
			

		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			result = 3;
			throw new Exception(e);
		} finally {
			if (session != null)
				session.close();
		}

		return result;

	}

	@Override
	public int updateRequestonGrid(String oldValue, String newValue, Integer updaterequestId,String userName) throws Exception {
		// TODO Auto-generated method stub
		Session session = null;
		Transaction tx = null;
		Request requestworkflow = null;
		int result = 0;
		
		try {
			session = HibernateUtil.getSession();
			tx = session.beginTransaction();
			requestworkflow = (Request) session.createCriteria(Request.class)
					.add(Restrictions.eq("id", Integer.valueOf(updaterequestId))).uniqueResult();

			if (requestworkflow != null && newValue != null) {
				
				//Integer stage = Integer.parseInt(newValue);	
				
				if (oldValue != null && newValue != null) {
					
					if(newValue.equals("Hold")){
						requestworkflow.setRequeststatus(7);
						
					}
					else if(newValue.equals("Cancel")){
						requestworkflow.setRequeststatus(7);
						
					}else if(newValue.equals("Close")){
						requestworkflow.setRequeststatus(8);
						requestworkflow.setApproveddate(new Date());
						requestworkflow.setApprovedby(userName);
						
					}
					
                requestworkflow.setDatemodified(new Date());
				
			
				session.update(requestworkflow);

				}
				tx.commit();
				result = 1;

			}
		} catch (Exception e) {
			e.printStackTrace();
			if (tx != null) {
				tx.rollback();
			}
			result = 2;
		} finally {
			if (session != null)
				session.close();
		}

		return result;

	}
	}

	
	
	
	


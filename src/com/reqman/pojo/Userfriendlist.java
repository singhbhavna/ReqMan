package com.reqman.pojo;

// Generated 13 Aug, 2017 10:25:05 PM by Hibernate Tools 4.3.1

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * Userfriendlist generated by hbm2java
 */
@Entity
@Table(name = "userfriendlist")
public class Userfriendlist implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7672874640318531848L;
	private int id;
	private Users users;
	private Integer userid;
	private Boolean status;
	private Date datecreated;
	private String createdby;
	private Requestworkflow requestworkflow;

	public Userfriendlist() {
	}

	public Userfriendlist(Users users) {
		this.users = users;
	}

	public Userfriendlist(Users users, Integer userid, Boolean status,
			Date datecreated, String createdby, Requestworkflow requestworkflow) {
		this.users = users;
		this.userid = userid;
		this.status = status;
		this.datecreated = datecreated;
		this.createdby = createdby;
		this.requestworkflow = requestworkflow;
	}

	@GenericGenerator(name = "generator", strategy = "foreign", parameters = @Parameter(name = "property", value = "users"))
	@Id
	@GeneratedValue(generator = "generator")
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	public Users getUsers() {
		return this.users;
	}

	public void setUsers(Users users) {
		this.users = users;
	}

	@Column(name = "userid")
	public Integer getUserid() {
		return this.userid;
	}

	public void setUserid(Integer userid) {
		this.userid = userid;
	}

	@Column(name = "status")
	public Boolean getStatus() {
		return this.status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "datecreated", length = 29)
	public Date getDatecreated() {
		return this.datecreated;
	}

	public void setDatecreated(Date datecreated) {
		this.datecreated = datecreated;
	}

	@Column(name = "createdby", length = 50)
	public String getCreatedby() {
		return this.createdby;
	}

	public void setCreatedby(String createdby) {
		this.createdby = createdby;
	}

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "request")
	public Requestworkflow getRequestworkflow() {
		return this.requestworkflow;
	}

	public void setRequestworkflow(Requestworkflow requestworkflow) {
		this.requestworkflow = requestworkflow;
	}

}
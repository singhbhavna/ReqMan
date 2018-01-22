package com.reqman.pojo;

// Generated 18 Jan, 2018 11:02:49 PM by Hibernate Tools 4.3.1

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * Users generated by hbm2java
 */
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "emailid"))
public class Users implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4055860975154521586L;
	private int id;
	private Account account;
	private String emailid;
	private String password;
	private String firstname;
	private String lastname;
	private String shortname;
	private Boolean status;
	private String createdby;
	private Date createdon;
	private Date lastlogin;
	private byte[] photo;
	private String hashkey;
	private String emailstatus;
	private Set<Userroles> userroleses = new HashSet<Userroles>(0);
	private Set<Userfriendlist> userfriendlistsForUserid = new HashSet<Userfriendlist>(
			0);
	private Set<Userusertype> userusertypes = new HashSet<Userusertype>(0);
	private Set<Customerpayment> customerpayments = new HashSet<Customerpayment>(
			0);
	private Set<Userrequesttype> userrequesttypes = new HashSet<Userrequesttype>(
			0);
	private Set<Userfriendlist> userfriendlistsForFriendid = new HashSet<Userfriendlist>(
			0);
	private Set<Userproject> userprojects = new HashSet<Userproject>(0);
	private Set<Usercategory> usercategories = new HashSet<Usercategory>(0);

	public Users() {
	}

	public Users(int id) {
		this.id = id;
	}

	public Users(int id, Account account, String emailid, String password,
			String firstname, String lastname, String shortname,
			Boolean status, String createdby, Date createdon, Date lastlogin,
			byte[] photo, String hashkey, String emailstatus,
			Set<Userroles> userroleses,
			Set<Userfriendlist> userfriendlistsForUserid,
			Set<Userusertype> userusertypes,
			Set<Customerpayment> customerpayments,
			Set<Userrequesttype> userrequesttypes,
			Set<Userfriendlist> userfriendlistsForFriendid,
			Set<Userproject> userprojects, Set<Usercategory> usercategories) {
		this.id = id;
		this.account = account;
		this.emailid = emailid;
		this.password = password;
		this.firstname = firstname;
		this.lastname = lastname;
		this.shortname = shortname;
		this.status = status;
		this.createdby = createdby;
		this.createdon = createdon;
		this.lastlogin = lastlogin;
		this.photo = photo;
		this.hashkey = hashkey;
		this.emailstatus = emailstatus;
		this.userroleses = userroleses;
		this.userfriendlistsForUserid = userfriendlistsForUserid;
		this.userusertypes = userusertypes;
		this.customerpayments = customerpayments;
		this.userrequesttypes = userrequesttypes;
		this.userfriendlistsForFriendid = userfriendlistsForFriendid;
		this.userprojects = userprojects;
		this.usercategories = usercategories;
	}

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accountid")
	public Account getAccount() {
		return this.account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	@Column(name = "emailid", unique = true, length = 100)
	public String getEmailid() {
		return this.emailid;
	}

	public void setEmailid(String emailid) {
		this.emailid = emailid;
	}

	@Column(name = "password", length = 12)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "firstname", length = 50)
	public String getFirstname() {
		return this.firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	@Column(name = "lastname", length = 50)
	public String getLastname() {
		return this.lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	@Column(name = "shortname", length = 100)
	public String getShortname() {
		return this.shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	@Column(name = "status")
	public Boolean getStatus() {
		return this.status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	@Column(name = "createdby", length = 50)
	public String getCreatedby() {
		return this.createdby;
	}

	public void setCreatedby(String createdby) {
		this.createdby = createdby;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createdon", length = 29)
	public Date getCreatedon() {
		return this.createdon;
	}

	public void setCreatedon(Date createdon) {
		this.createdon = createdon;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "lastlogin", length = 29)
	public Date getLastlogin() {
		return this.lastlogin;
	}

	public void setLastlogin(Date lastlogin) {
		this.lastlogin = lastlogin;
	}

	@Column(name = "photo")
	public byte[] getPhoto() {
		return this.photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	@Column(name = "hashkey")
	public String getHashkey() {
		return this.hashkey;
	}

	public void setHashkey(String hashkey) {
		this.hashkey = hashkey;
	}

	@Column(name = "emailstatus")
	public String getEmailstatus() {
		return this.emailstatus;
	}

	public void setEmailstatus(String emailstatus) {
		this.emailstatus = emailstatus;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "users")
	public Set<Userroles> getUserroleses() {
		return this.userroleses;
	}

	public void setUserroleses(Set<Userroles> userroleses) {
		this.userroleses = userroleses;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "usersByUserid")
	public Set<Userfriendlist> getUserfriendlistsForUserid() {
		return this.userfriendlistsForUserid;
	}

	public void setUserfriendlistsForUserid(
			Set<Userfriendlist> userfriendlistsForUserid) {
		this.userfriendlistsForUserid = userfriendlistsForUserid;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "users")
	public Set<Userusertype> getUserusertypes() {
		return this.userusertypes;
	}

	public void setUserusertypes(Set<Userusertype> userusertypes) {
		this.userusertypes = userusertypes;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "users")
	public Set<Customerpayment> getCustomerpayments() {
		return this.customerpayments;
	}

	public void setCustomerpayments(Set<Customerpayment> customerpayments) {
		this.customerpayments = customerpayments;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "users")
	public Set<Userrequesttype> getUserrequesttypes() {
		return this.userrequesttypes;
	}

	public void setUserrequesttypes(Set<Userrequesttype> userrequesttypes) {
		this.userrequesttypes = userrequesttypes;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "usersByFriendid")
	public Set<Userfriendlist> getUserfriendlistsForFriendid() {
		return this.userfriendlistsForFriendid;
	}

	public void setUserfriendlistsForFriendid(
			Set<Userfriendlist> userfriendlistsForFriendid) {
		this.userfriendlistsForFriendid = userfriendlistsForFriendid;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "users")
	public Set<Userproject> getUserprojects() {
		return this.userprojects;
	}

	public void setUserprojects(Set<Userproject> userprojects) {
		this.userprojects = userprojects;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "users")
	public Set<Usercategory> getUsercategories() {
		return this.usercategories;
	}

	public void setUsercategories(Set<Usercategory> usercategories) {
		this.usercategories = usercategories;
	}

}

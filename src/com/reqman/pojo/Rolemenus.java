package com.reqman.pojo;

// Generated 22 Aug, 2017 7:11:21 PM by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Rolemenus generated by hbm2java
 */
@Entity
@Table(name = "rolemenus", uniqueConstraints = @UniqueConstraint(columnNames = {
		"roleid", "menuid" }))
public class Rolemenus implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4793245400328080010L;
	private int id;
	private Menu menu;
	private Roles roles;

	public Rolemenus() {
	}

	public Rolemenus(int id) {
		this.id = id;
	}

	public Rolemenus(int id, Menu menu, Roles roles) {
		this.id = id;
		this.menu = menu;
		this.roles = roles;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menuid")
	public Menu getMenu() {
		return this.menu;
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "roleid")
	public Roles getRoles() {
		return this.roles;
	}

	public void setRoles(Roles roles) {
		this.roles = roles;
	}

}

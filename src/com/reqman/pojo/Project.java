package com.reqman.pojo;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.reqman.common.HibernateSessionFactory;
import com.reqman.common.HibernateUtil;
import com.reqman.common.HibernateUtilH;




@ManagedBean
@SessionScoped
@Entity
@Table(name="Project")
public class Project implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -3058673729691320988L;
	@Id
	@Column(name="project",length = 80)
	private String project;
	@Column(name="status",length = 45)
	private String status;
	@Column(name="email", length = 100)
	private String email;	
	
	
	
	
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status =status;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	
	public String submit(){	
		  Session session; 
	    String result=null;
	    SessionFactory hsf = HibernateUtilH.getSessionFactory();
        session = hsf.openSession();	
		try{
			//create a student object			
		this.setEmail(email);
		this.setProject(project);
		this.setStatus("yes");	
			
			//start a transaction 
			session.beginTransaction();
			//save the student object			
	result=(String)	session.save(this);
			//commit transaction
			session.getTransaction().commit();
			System.out.println("done");
			if(result!=null){
				return "new_project";
			}else{
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_WARN,
								"AlreadyExist project_name",
								"Please Enter a another_name"));
				return "new_project";
			}			
		}	catch(Exception e){
			System.out.print(e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_WARN,
							"AlreadyExist project_name",
							"Please Enter a another_name"));
			return "new_project";
		}
			
        }

	
	}
	
	



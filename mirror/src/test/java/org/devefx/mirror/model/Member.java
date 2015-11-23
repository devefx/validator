package org.devefx.mirror.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.devefx.mirror.annotation.Column;
import org.devefx.mirror.annotation.Entity;
import org.devefx.mirror.annotation.Table;

@Table("t06_member_info")
public class Member implements Serializable {
	private static final long serialVersionUID = 8786490202861415267L;
	
	private @Column int id;
	private @Column String username;
	private @Column String password;
	private @Column String email;
	private @Column("register_tm") Date registerTime;
	private @Entity("member_id") List<Equity> equities;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Date getRegisterTime() {
		return registerTime;
	}
	public void setRegisterTime(Date registerTime) {
		this.registerTime = registerTime;
	}
	public List<Equity> getEquities() {
		return equities;
	}
	public void setEquities(List<Equity> equities) {
		this.equities = equities;
	}
}

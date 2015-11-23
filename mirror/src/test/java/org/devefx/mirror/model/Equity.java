package org.devefx.mirror.model;

import org.devefx.mirror.annotation.Column;
import org.devefx.mirror.annotation.Entity;
import org.devefx.mirror.annotation.Table;

@Table("t06_equity")
public class Equity {
	private @Column Integer id;
	private @Entity("member_id") Member member;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Member getMember() {
		return member;
	}
	public void setMember(Member member) {
		this.member = member;
	}
}

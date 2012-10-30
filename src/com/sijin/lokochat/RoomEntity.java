package com.sijin.lokochat;

import java.util.Arrays;
import java.util.List;

import com.kinvey.persistence.mapping.MappedEntity;
import com.kinvey.persistence.mapping.MappedField;

public class RoomEntity implements MappedEntity {

	private String id;
	private Long numUsers;

	public RoomEntity() {
	}

	public RoomEntity(String id, Long numUsers) {
		this.id = id;
		this.numUsers = numUsers;
	}

	@Override
	public List<MappedField> getMapping() {
		return Arrays.asList(new MappedField[] { new MappedField("id", "_id"),
				new MappedField("num_users", "numUsers"), });
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getNumUsers() {
		return numUsers;
	}

	public void setNumUsers(Long numUsers) {
		this.numUsers = numUsers;
	}
}

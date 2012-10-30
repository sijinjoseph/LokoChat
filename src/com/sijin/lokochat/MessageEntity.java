package com.sijin.lokochat;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.kinvey.persistence.mapping.MappedEntity;
import com.kinvey.persistence.mapping.MappedField;

public class MessageEntity implements MappedEntity {
	private String id;
	private String room;
	private String nickname;
	private String message;

	public MessageEntity() {
		id = UUID.randomUUID().toString();
	}

	public MessageEntity(String room, String nickname, String message) {
		id = UUID.randomUUID().toString();
		this.room = room;
		this.nickname = nickname;
		this.message = message;
	}

	@Override
	public List<MappedField> getMapping() {
		return Arrays.asList(new MappedField[] { new MappedField("id", "_id"),
				new MappedField("room", "room"),
				new MappedField("nickname", "nickname"),
				new MappedField("message", "message") });
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

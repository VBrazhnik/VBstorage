package com.vbrazhnik.vbstorage.events;

import com.vbrazhnik.vbstorage.entities.Tag;

public class TagCreatedEvent{

	private Tag tag;

	public TagCreatedEvent(Tag tag){
		this.tag = tag;
	}

	public Tag getTag(){
		return tag;
	}
}

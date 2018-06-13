package com.vbrazhnik.vbstorage.events;

import com.vbrazhnik.vbstorage.entities.Tag;

public class TagDeletedEvent{

	private Tag tag;

	public TagDeletedEvent(Tag tag){
		this.tag = tag;
	}

	public Tag getTag(){
		return tag;
	}
}

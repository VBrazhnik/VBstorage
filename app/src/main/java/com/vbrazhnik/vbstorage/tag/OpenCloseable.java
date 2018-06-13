package com.vbrazhnik.vbstorage.tag;

interface OpenCloseable  {

	void close();

	void open();

	boolean isOpen();

}

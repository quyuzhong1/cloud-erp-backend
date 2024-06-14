package com.erp.server.dmp.inout.utils;

import com.baomidou.mybatisplus.core.toolkit.Sequence;

public class IdSequenceUtils {
	
	private static Sequence sequence = new Sequence();
	
	public static String getId() {
		long nextId = sequence.nextId();
		return Long.valueOf(nextId).toString();
	}
}

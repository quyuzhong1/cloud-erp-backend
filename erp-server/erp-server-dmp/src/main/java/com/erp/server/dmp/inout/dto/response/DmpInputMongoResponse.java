package com.erp.server.dmp.inout.dto.response;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class DmpInputMongoResponse extends DmpInputFdsResponse{
	/**
	 * mongo业务信息
	 */
	private List<Map> dmpInputMongoEntityList;
}

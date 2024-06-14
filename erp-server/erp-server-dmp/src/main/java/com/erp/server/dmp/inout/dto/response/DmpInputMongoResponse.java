package com.erp.server.dmp.inout.dto.response;

import java.util.List;

import com.erp.server.dmp.inout.dto.base.DmpInputMongoBaseEntity;

import lombok.Data;

@Data
public class DmpInputMongoResponse extends DmpInputFdsResponse{
	/**
	 * 文件上传信息
	 */
	private List<DmpInputMongoBaseEntity> dmpInputMongoBaseEntityList;
}

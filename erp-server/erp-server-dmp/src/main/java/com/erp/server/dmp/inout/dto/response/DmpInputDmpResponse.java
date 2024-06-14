package com.erp.server.dmp.inout.dto.response;

import java.util.List;

import com.erp.server.dmp.inout.dto.base.DmpInputDmpBaseEntity;

import lombok.Data;

@Data
public class DmpInputDmpResponse extends DmpInputMongoResponse{
	/**
	 * 文件上传信息
	 */
	private List<DmpInputDmpBaseEntity> dmpInputDmpBaseEntityList;
}

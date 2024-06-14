package com.erp.server.dmp.inout.dto.response;

import java.util.List;

import com.erp.model.dmp.entity.DmpInputTaskFileEntity;

import lombok.Data;

@Data
public class DmpInputFdsResponse extends DmpInputInitResponse{
	/**
	 * 文件上传信息
	 */
	private List<DmpInputTaskFileEntity> dmpInputTaskFileEntityList;
}

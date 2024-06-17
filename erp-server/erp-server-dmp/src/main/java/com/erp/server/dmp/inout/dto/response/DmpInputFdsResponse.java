package com.erp.server.dmp.inout.dto.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;

import lombok.Data;

@Data
public class DmpInputFdsResponse extends DmpInputInitResponse{
	/**
	 * 文件上传信息
	 */
	private Map<DmpCfgInputConvertEntity , List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMaps = new HashMap<>();
}

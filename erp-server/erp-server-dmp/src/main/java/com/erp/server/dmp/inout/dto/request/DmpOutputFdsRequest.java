package com.erp.server.dmp.inout.dto.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskFileEntity;

import lombok.Data;

/**
 * 输出任务fds状态请求参数
 * @author Administrator
 *
 */
@Data
public class DmpOutputFdsRequest extends DmpOutputInitRequest{
	/**
	 * 文件上传信息
	 */
	private Map<DmpCfgInputConvertEntity , List<DmpInputTaskFileEntity>> convertInputTaskFileEntityListMaps = new HashMap<>();
}

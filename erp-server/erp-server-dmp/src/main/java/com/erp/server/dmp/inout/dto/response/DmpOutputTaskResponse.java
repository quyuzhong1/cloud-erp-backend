package com.erp.server.dmp.inout.dto.response;

import java.util.List;

import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpCfgOutputDetailEntity;
import com.erp.model.dmp.entity.DmpCfgOutputEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;

import lombok.Data;

/**
 * 输出任务状态响应参数
 * @author Administrator
 *
 */
@Data
public class DmpOutputTaskResponse extends DmpOutputResponse{
	private DmpCfgOutputEntity dmpCfgOutputEntity;
	private DmpCfgOutputDetailEntity dmpCfgOutputDetailEntity;
	private List<DmpOutputTaskRecordEntity> outputData;
	private DmpCfgInputConvertEntity dmpCfgInputConvertEntity;
}

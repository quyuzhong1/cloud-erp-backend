package com.erp.server.wms.service;
import com.erp.model.wms.entity.WmsDataCompareTaskEntity;
import com.common.business.service.SuperService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.SetNextDTO;

/**
 * <p>
 * 数据对比任务 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
 */
public interface WmsDataCompareTaskService extends SuperService<WmsDataCompareTaskEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-03-20
    * @param dto
    * @return
    */
	WmsDataCompareTaskDTO.ViewDTO add(WmsDataCompareTaskDTO.AddDTO dto);
	
	String downloadSystemData(BaseIdDTO dto);
	
	ApiResult<?> setNext(SetNextDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-03-20
    * @param dto
    * @return
    */
    Boolean update(WmsDataCompareTaskDTO.UpdateDTO dto);


}

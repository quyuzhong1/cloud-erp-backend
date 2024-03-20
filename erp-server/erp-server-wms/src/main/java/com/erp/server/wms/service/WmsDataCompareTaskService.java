package com.erp.server.wms.service;
import com.erp.model.wms.entity.WmsDataCompareTaskEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;

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
    BaseResultDTO.AddDTO add(WmsDataCompareTaskDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-03-20
    * @param dto
    * @return
    */
    Boolean update(WmsDataCompareTaskDTO.UpdateDTO dto);


}

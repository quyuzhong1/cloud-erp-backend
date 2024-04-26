package com.erp.server.wms.service;
import com.erp.model.wms.entity.WmsDataCompareTempEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.WmsDataCompareTempDTO;

/**
 * <p>
 * 数据对比对比加工临时表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
 */
public interface WmsDataCompareTempService extends SuperService<WmsDataCompareTempEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-03-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(WmsDataCompareTempDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-03-20
    * @param dto
    * @return
    */
    Boolean update(WmsDataCompareTempDTO.UpdateDTO dto);

    void deleteData(String taskId);
}

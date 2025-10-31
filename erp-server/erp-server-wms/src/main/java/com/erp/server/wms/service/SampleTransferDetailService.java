package com.erp.server.wms.service;
import com.erp.model.wms.entity.SampleTransferDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleTransferDetailDTO;

import java.util.List;

/**
 * <p>
 * 样品转移单明细表 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-10-28
 */
public interface SampleTransferDetailService extends SuperService<SampleTransferDetailEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleTransferDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-10-28
    * @param dto
    * @return
    */
    Boolean update(SampleTransferDetailDTO.UpdateDTO dto);

    /**
    * 根据主表ID查询明细列表
    * @author wuhaotian
    * @date: 2025-10-28
    * @param mainId 主表ID
    * @return 明细列表
    */
    List<SampleTransferDetailEntity> listByMainId(String mainId);

}

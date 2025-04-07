package com.erp.server.dmp.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.entity.AfterSaleProgressEntity;

import java.util.List;

/**
 * <p>
 * 售后进度记录表 服务类
 * </p>
 *
 * @author jack
 * @since 2025-04-03
 */
public interface AfterSaleProgressService extends SuperService<AfterSaleProgressEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-04-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AfterSaleProgressDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-04-03
    * @param dto
    * @return
    */
    Boolean update(AfterSaleProgressDTO.UpdateDTO dto);


    List<AfterSaleProgressEntity> getByMainIds(List<String> ids);

    Boolean updateStatus(String id, String node, String remark);
}

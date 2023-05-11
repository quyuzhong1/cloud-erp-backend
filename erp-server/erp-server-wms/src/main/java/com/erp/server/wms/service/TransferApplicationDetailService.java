package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.TransferApplicationDetailDTO;
import com.erp.model.wms.entity.TransferApplicationDetailEntity;

import java.util.List;

/**
 * <p>
 * 调拨申请单明细表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface TransferApplicationDetailService extends SuperService<TransferApplicationDetailEntity> {
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/5/10 19:49
     * @param details
     * @param mainId
     */
    void add(List<TransferApplicationDetailDTO.AddDTO> details, String mainId);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/5/11 10:35
     * @param details
     * @param mainId
     */
    void update(List<TransferApplicationDetailDTO.UpdateDTO> details, String mainId);
    /**
     * @description: 根据主表ids删除
     * @author Will
     * @date: 2023/5/11 10:42
     * @param mainIds
     */
    void removeByMainIds(List<String> mainIds);
    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/4/13 17:35
     * @param mainId
     * @return List<TransferApplicationDetailEntity>
     */
    List<TransferApplicationDetailEntity> listByMainId(String mainId);

    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/4/13 17:35
     * @param mainIds
     * @return List<TransferApplicationDetailEntity>
     */
    List<TransferApplicationDetailEntity> listByMainIds(List<String> mainIds);
}

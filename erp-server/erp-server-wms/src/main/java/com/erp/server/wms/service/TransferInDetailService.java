package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.erp.model.wms.dto.TransferInDetailDTO;
import com.erp.model.wms.entity.TransferInDetailEntity;

import java.util.List;

/**
 * <p>
 * 分布式调入单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface TransferInDetailService extends SuperService<TransferInDetailEntity> {

    /**
     * 添加明细
     * @author yl
     * @date 2023-05-26 15:01
     * @param id
     * @param detailList
     * @return void
     */
    void add(String id, List<TransferInDetailDTO.AddDTO> detailList);

    
    /**
     * 删除明细
     * @author yl
     * @date 2023-05-29 8:53
     * @param mainIds
     * @return void
     */
    void removeByMainIdList(List<String> mainIds);

    
    /**
     * 根据主表id 获取到详情
     * @author yl
     * @date 2023-05-29 11:45
     * @param mainId 主表id
     * @return java.util.List<com.erp.model.wms.dto.TransferInDetailDTO.ViewDTO>
     */
    List<TransferInDetailDTO.ViewDTO> listByMainId(String mainId);
}

package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.model.wms.dto.VirtualInventoryDetailHisDTO;
import com.erp.model.wms.entity.VirtualInventoryDetailHisEntity;

import java.util.List;

/**
 * <p>
 * 虚拟仓库存历史信息 服务类
 * </p>
 *
 * @author will
 * @since 2024-12-03
 */
public interface VirtualInventoryDetailHisService extends SuperService<VirtualInventoryDetailHisEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO addOrUpdate(VirtualInventoryDetailHisDTO.AddDTO dto);


    /**
     * 根据paramDTO参数查询
     * @author will
     * @date 2024/12/6 11:50
     * @param paramDTO
     * @return List<VirtualInventoryHisEntity>
     */
    List<VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO> listByParam(VirtualInventoryDetailHisDTO.ParamDTO paramDTO);
    /**
     * 历史库存任务保存
     * @author will
     * @date 2024/12/9 19:23
     */
    void hisVirtualInventoryJob(String jobParam);
    /**
     * 详情历史库龄明细
     * @author will
     * @date 2024/12/10 17:35
     * @param dto
     * @return viewHisInventoryAgeDetailDTO
     */
    VirtualInventoryAgeDTO.viewHisInventoryAgeDetailDTO getHisInventoryAgeDetail(VirtualInventoryAgeDTO.HisInventoryAgeDetailParamDTO dto);
}

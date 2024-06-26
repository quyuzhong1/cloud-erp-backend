package com.erp.server.wms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.renovation.PickingWaveDTO;
import com.erp.model.wms.entity.PickingWaveEntity;

import java.util.List;

public interface PickingWaveService extends SuperService<PickingWaveEntity> {

    /**
     * 新增
     */
    BaseResultDTO.AddDTO add(PickingWaveDTO.AddDTO dto);
    /**
     * 统计发货批次的数量
     */
    int countDelivery(PermissionsDTO param);

    /**
     * 根据状态获取
     * @param status
     * @return
     */
    List<String> listDeliveryIdByStatus(String status);

    /**
     * 通过波次号/拣货车号查询波次
     * @param code 波次号/拣货车号
     */
    PickingWaveEntity getByCodeOrCarCode(String code);

    /**
     * 获取波次明细
     * @param waveId 波次号
     */
    List<PickingWaveDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId);

    /**
     * 获取波次明细
     * @param waveId 波次号
     * @param basketNo 篮号
     */
    List<PickingWaveDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId, String basketNo);

    /**
     * 获取波次明细
     * @param waveId 波次号
     * @param basketNo 篮号
     * @param skuId sku
     */
    List<PickingWaveDTO.PickingWaveDetailDTO> listDetailByMainId(String waveId, String basketNo, String skuId);
}

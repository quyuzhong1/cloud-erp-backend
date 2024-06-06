package com.erp.server.dmp.service;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ThirdMappingStrategy {
    boolean supports(String type);

    BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO addDTO);

    ThirdMappingDTO.MappingViewDTO view(ThirdMappingDTO.ViewParamDTO viewParamDTO);

    /**
     * 删除海外仓绑定数据
     *
     * @param existMapping
     * @param warehouseId
     * @param warehouseCode
     * @param warehouseName
     * @param disabled
     */
    void saveOrDeleteFeignBind(ThirdMappingEntity existMapping, String warehouseId, String warehouseCode, String warehouseName, boolean disabled);

    void addOrUpdate(ThirdMappingEntity thirdMappingEntity, ThirdMappingEntity existMapping, WarehouseDTO.ListDTO warehouse);

    /**
     * 删除绑定数据
     *
     * @param existMappingList
     */
    void deleteBinded(List<ThirdMappingEntity> existMappingList);

    void makeThirdMappingDto(ThirdMappingDTO.AddDTO addDTO, ThirdMappingDTO.ThirdAddDTO thirdAddDTO, WarehouseDTO.ListDTO warehouse);

//    /**
//     * 保存新增数据
//     *
//     * @param addDTO
//     * @param thirdList
//     * @param resultUpdatedList
//     */
//    void saveAddDto(ThirdMappingDTO.AddDTO addDTO, List<ThirdMappingDTO.ThirdAddDTO> thirdList, List<ThirdMappingDTO.ThirdAddDTO> resultUpdatedList);
}


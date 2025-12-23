package com.erp.server.dmp.service;

import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.wms.dto.WarehouseDTO;

import java.util.List;

/**
 * <p>
 * 第三方系统映射关系表 服务类
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
public interface ThirdMappingService extends SuperService<ThirdMappingEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author hyj
     * @date: 2024-05-17
     */
    BaseResultDTO.AddDTO add(ThirdMappingDTO.AddDTO dto);

//    /**
//     * 删除绑定数据
//     *
//     * @param existMappingList
//     */
//    void deleteBinded(List<ThirdMappingEntity> existMappingList);
//
//    /**
//     * 删除海外仓绑定数据
//     *
//     * @param existMapping
//     * @param warehouseId
//     * @param warehouseCode
//     * @param warehouseName
//     * @param disabled
//     */
//    void saveOrDeleteFeignBind(ThirdMappingEntity existMapping, String warehouseId, String warehouseCode, String warehouseName, boolean disabled);

    /**
     * 预览
     *
     * @param viewParamDTO
     * @return
     * @author hyj
     * @date: 2024-05-17
     */
    ThirdMappingDTO.MappingViewDTO view(ThirdMappingDTO.ViewParamDTO viewParamDTO);

    /**
     * 根据类型和系统id获取数据
     *
     * @param type  类型
     * @param sysId 系统id
     * @return
     */
    List<ThirdMappingEntity> getList(String type, String sysId);

    void addOrUpdate(ThirdMappingEntity thirdMappingEntity, ThirdMappingEntity existMapping, WarehouseDTO.ListDTO warehouse);

    /**
     * 查找系统绑定的第三方信息
     *
     * @param thirdMappingEntity
     * @return
     */
    ThirdMappingEntity getByTypeAndSysIdAndSysType(ThirdMappingEntity thirdMappingEntity);

    /**
     * 查找第三方系统绑定的信息
     *
     * @param thirdMappingEntity
     * @return
     */
    ThirdMappingEntity getByTypeAndThirdId(ThirdMappingEntity thirdMappingEntity);

    /**
     * 根据erp内部仓库/店铺的id查询映射关系
     *
     * @param sysId   erp内部仓库/店铺ID
     * @param sysType
     * @return ThirdMappingEntity 三方映射实体
     * @date: 2024-05-27
     * @author: tanmujin
     */
    ThirdWarehouseEntity getBySysId(String sysId, String sysType);

    Boolean getWhetherBind(ThirdMappingDTO.ViewParamDTO viewParamDTO);

    List<ThirdMappingEntity> getByThirdId(ThirdMappingDTO.ViewParamDTO viewParamDTO);

    BaseResultDTO.AddDTO batchAdd(ThirdMappingDTO.FeignMappingDTO feignMappingDTO);

    /**
     * 根据系统id获取绑定关系
     * @param sysIds
     * @return
     */
    List<ThirdMappingEntity> getListBySysIds(List<String> sysIds);

    /**
     * 根据系统id获取仓库绑定信息
     * @param sysIds
     * @return
     */
    List<ThirdMappingEntity> getVwListBySysIds(List<String> sysIds);

    List<ThirdMappingDTO.WarehouseMappingDTO> listMappingBySysIds(List<String> warehouseIdList, String sysType);

    ThirdMappingEntity getByThirdCodeAndType(String warehouseNo, String sysType, String type);

    ThirdMappingEntity getShopByThirdCode(String platformShop, String sysType);
}

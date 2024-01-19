package com.erp.server.tms.service;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TransferLogisticsChannelDTO;

import java.util.List;

/**
 * <p>
 * 中转报关服务商渠道表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
public interface TransferLogisticsChannelService extends SuperService<TransferLogisticsChannelEntity> {

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2024/1/19 14:26
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    BaseResultDTO.AddDTO add(TransferLogisticsChannelDTO.AddDTO dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2024/1/19 14:26
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean update(TransferLogisticsChannelDTO.UpdateDTO dto);

    /**
     * 物流渠道列表
     * @Author Luo_WG
     * @Date 2024/1/19 14:27
     * @param logisticsSupplierIds
     * @return java.util.List<com.erp.model.tms.dto.TransferLogisticsChannelDTO.ListSelectDTO>
     **/
    List<TransferLogisticsChannelDTO.ListSelectDTO> listLogisticsChannel(List<String> logisticsSupplierIds);

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2024/1/19 14:27
     * @param id
     * @return com.erp.model.tms.dto.TransferLogisticsChannelDTO.ViewDTO
     **/
    TransferLogisticsChannelDTO.ViewDTO view(String id);

    /**
     * 删除渠道
     * @Author Luo_WG
     * @Date 2024/1/19 14:27
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO delete(String id);

    /**
     * 更改启用停用状态
     * @Author Luo_WG
     * @Date 2024/1/19 14:27
     * @param id
     * @param disabled
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO updateStatus(String id, Boolean disabled);

    /**
     * 删除渠道根据来源id
     * @Author Luo_WG
     * @Date 2024/1/19 14:27
     * @param sourceIdList
     * @return void
     **/
    void removeByMainIdList(List<String> sourceIdList);

    /**
     * 获取到所有的渠道
     * @Author Luo_WG
     * @Date 2024/1/19 14:28
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.DisabledDTO>
     **/
    List<BaseDropDownDTO.DisabledDTO> listAll();

    /**
     * 根据供应商id 获取启用禁用的列表
     * @Author Luo_WG
     * @Date 2024/1/19 14:28
     * @param supplierId
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO.CodeDTO>
     **/
    List<BaseIdDTO.CodeDTO> listBySupplierId(String supplierId);

    /**
     * 根据主标id查询渠道信息
     * @Author Luo_WG
     * @Date 2024/1/19 14:48
     * @param mainIds
     * @return java.util.List<com.erp.model.tms.entity.TransferLogisticsChannelEntity>
     **/
    List<TransferLogisticsChannelEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据物流商id 获取渠道
     * @Author Luo_WG
     * @Date 2024/1/19 17:56
     * @param transferLogisticsSupplierId
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.DisabledDTO>
     **/
    List<BaseDropDownDTO.DisabledDTO> listByLogisticsSupplierId(String transferLogisticsSupplierId);
}

package com.erp.server.tms.service;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.service.SuperService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.TransferLogisticsAuthDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.entity.TransferLogisticsAuthEntity;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物流授权表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
public interface TransferLogisticsAuthService extends SuperService<TransferLogisticsAuthEntity> {

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2024/1/19 11:34
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    BaseResultDTO.AddDTO add(TransferLogisticsAuthDTO.AddDTO dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2024/1/19 11:34
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.UpdateDTO
     **/
    BaseResultDTO.UpdateDTO update(TransferLogisticsAuthDTO.UpdateDTO dto);

    /**
     * 获取到授权详情
     * @Author Luo_WG
     * @Date 2024/1/19 11:34
     * @param id
     * @return com.erp.model.tms.dto.TransferLogisticsAuthDTO.ViewDTO
     **/
    TransferLogisticsAuthDTO.ViewDTO view(String id);

    /**
     * 取消授权
     * @Author Luo_WG
     * @Date 2024/1/19 11:34
     * @param mainId
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO cancel(String mainId);

    /**
     * 根据渠道id 获取授权信息
     * @Author Luo_WG
     * @Date 2024/1/19 11:35
     * @param channelId
     * @return com.erp.model.tms.dto.TransferLogisticsAuthDTO.ViewDTO
     **/
    TransferLogisticsAuthDTO.ViewDTO getViewByChannelId(String channelId);

    /**
     * 根据授权id组装授权信息
     * @Author Luo_WG
     * @Date 2024/1/19 11:35
     * @param authId
     * @param logisticsPlatform
     * @return java.util.Map<java.lang.String,java.lang.String>
     **/
    Map<String, String> getTransferLogisticsAuthConfig(String authId, String logisticsPlatform);

    /**
     * 根据平台编号组装授权信息
     * @Author Luo_WG
     * @Date 2024/1/19 11:35
     * @param platform
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.String>>
     **/
    List<Map<String, String>> getTransferLogisticsAuthByPlatform(String platform);

    /**
     * 授权完成后 同步销售渠道
     * @Author Luo_WG
     * @Date 2024/1/19 11:36
     * @param logisticsPlatform
     * @param authId
     * @param mainId
     * @return void
     **/
    void syncUpdateSaleChannel(String logisticsPlatform, String authId, String mainId);

    /**
     * 根据供应商id 获取信息
     * @Author Luo_WG
     * @Date 2024/1/19 11:36
     * @param id
     * @param mainId
     * @return com.erp.model.tms.entity.TransferLogisticsAuthEntity
     **/
    TransferLogisticsAuthEntity getByMainId(String id, String mainId);

    /**
     * 根据供应商id 获取信息
     * @Author Luo_WG
     * @Date 2024/1/19 11:36
     * @param mainIds
     * @return com.erp.model.tms.entity.TransferLogisticsAuthEntity
     **/
    List<TransferLogisticsAuthEntity> listByMainIds(List<String> mainIds);

    /**
     * 根据id获取授权信息
     * @Author Luo_WG
     * @Date 2024/1/19 11:37
     * @param channelId
     * @return com.erp.model.tms.dto.TransferLogisticsSupplierDTO.AuthDTO
     **/
    TransferLogisticsSupplierDTO.AuthDTO getAuthByChannelId(String channelId);

    /**
     * 授权物流信息
     * @Author Luo_WG
     * @Date 2024/1/19 11:37
     * @param id
     * @param logisticsPlatform
     * @return com.common.core.controller.vo.ApiResult
     **/
    ApiResult<Object>authLogistics(String id, String logisticsPlatform);

    /**
     * 根据授权信息进行校验
     * @Author Luo_WG
     * @Date 2024/1/19 11:37
     * @param logisticsPlatform
     * @param authConfig
     * @return com.common.core.controller.vo.ApiResult
     **/
    ApiResult<Object>authLogistics(String logisticsPlatform,Map<String, String> authConfig);

    /**
     * 修改物流授权状态
     * @Author Luo_WG
     * @Date 2024/1/19 11:38
     * @param mainId
     * @param authStatus
     * @return void
     **/
    void updateLogisticsAuthStatus(String mainId, String authStatus);

    /**
     * 根据授权id组装授权信息
     * @Author Luo_WG
     * @Date 2024/1/20 9:44
     * @param authId
     * @param logisticsPlatform
     * @return java.util.Map<java.lang.String,java.lang.String>
     **/
    Map<String, String> getLogisticsAuthConfig(String authId,String logisticsPlatform);

}

package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;

import java.util.List;

/**
 * <p>
 * b2c发货拦截单 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
public interface SoB2cDeliveryInterceptService extends SuperService<SoB2cDeliveryInterceptEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-12-13
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoB2cDeliveryInterceptDTO.AddDTO dto);

    /**
     * 获取状态统计
     * @Author Luo_WG
     * @Date 2023/12/14 10:01
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO.TabListDTO>
     **/
    List<SoB2cDeliveryInterceptDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/12/14 11:03
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO.ListDTO>
     **/
    PagingVO<SoB2cDeliveryInterceptDTO.ListDTO> paging(PagingDTO<SoB2cDeliveryInterceptDTO.PagingParamDTO> dto);

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2023/12/14 11:15
     * @param id
     * @return com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO.ViewDTO
     **/
    SoB2cDeliveryInterceptDTO.ViewDTO view(String id);

    /**
     * 物流拦截
     * @Author Luo_WG
     * @Date 2023/12/14 11:34
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO logisticsIntercept(String id);

    /**
     * 拦截结果确认
     * @Author Luo_WG
     * @Date 2023/12/25 17:13
     * @param dto
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO interceptResultConfirm(SoB2cDeliveryInterceptDTO.InterceptResultConfirmDTO dto, String id);

    /**
     * 查询是否拦截
     * @Author Luo_WG
     * @Date 2023/12/14 18:32
     * @param sourceIdList
     * @return void
     **/
    List<SoB2cDeliveryInterceptDTO.IsInterceptDTO> listIsIntercept(List<String> sourceIdList);

    /**
     * 根据来源id查询拦截单
     * @Author Luo_WG
     * @Date 2024/1/3 19:21
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity>
     **/
    List<SoB2cDeliveryInterceptEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 根据发货id查询拦截单
     **/
    List<SoB2cDeliveryInterceptEntity> listByDeliveryIds(List<String> deliveryIds);
    /**
     * 根据来源id修改拦截单状态
     * @Author Luo_WG
     * @Date 2024/1/3 19:36
     * @param sourceIds
     * @param status
     * @return void
     **/
    Boolean updateHandleStatus(List<String> sourceIds, String status);

    List<SoB2cDeliveryInterceptEntity> listByStatus(String code);

    BatchResultDTO handleSuccess(SoB2cDeliveryEntity dto, String id, List<SoB2cDeliveryInterceptDTO.InterceptInventoryDTO> interceptInventoryDTOList);

    List<SoB2cDeliveryInterceptDTO.InterceptInventoryDTO> interceptSuccessView(List<String> ids);

    BatchResultDTO interceptSuccess(SoB2cDeliveryInterceptDTO.InterceptSuccessDTO dto, String id);

    BatchResultDTO interceptFailure(String id, Boolean isAutoOut);
}

package com.erp.server.oms.service;

import com.common.business.dto.PlatformFulfillOrderDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import com.erp.model.oms.dto.SoMultiChannelDetailDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoMultiChannelEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.SoOutstockDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 多渠道订单主表 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-08-20
 */
public interface SoMultiChannelService extends SuperService<SoMultiChannelEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoMultiChannelDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    Boolean update(SoMultiChannelDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author zdy
    * @date: 2025-08-20
    * @param pagingParamDTO
    * @return PagingVO<SoMultiChannelDTO.ListDTO>>
    */
    PagingVO<SoMultiChannelDTO.ListDTO> paging(PagingDTO<SoMultiChannelDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author zdy
    * @date: 2025-08-20
    * @param dto
    * @return List<SoMultiChannelDTO.TabListDTO>>
    */
    List<SoMultiChannelDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author zdy
    * @date: 2025-08-20
    * @param id
    * @return
    */
    SoMultiChannelDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author zdy
    * @date: 2025-08-20
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(SoMultiChannelDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author zdy
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    void updateAndSubmit(SoMultiChannelDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author zdy
     * @date: 2025-08-20
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author zdy
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author zdy
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author zdy
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author zdy
    * @date: 2025-08-20
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author zdy
    * @date: 2025-08-20
    * @param dto
    * @param response
    * @return
    */
    void exportList(SoMultiChannelDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, SoMultiChannelEntity entity);

    List<SoMultiChannelDTO.SoViewDTO> listSoMultiChannel(List<String> ids, String deliveryWarehouseId, String shopId);

    /**
     * 构建新增DTO
     *
     * @param dto
     * @param id
     * @param shopInfoEntity
     * @param soB2cEntity
     * @param channelEntity
     * @return
     */
    SoMultiChannelDTO.AddDTO buildAddDTO(SoMultiChannelDTO.SaveDTO dto, String id, ShopInfoEntity shopInfoEntity, SoB2cEntity soB2cEntity, LogisticsChannelEntity channelEntity);

    /**
     * 更新多渠道订单创建状态
     * @param createResultDTO
     */
    void updateSoMultiChannel(SoMultiChannelDTO.CreateResultDTO createResultDTO);

    /**
     * 重新创建
     * @param entity
     * @return
     */
    BatchResultDTO reCreate(SoMultiChannelEntity entity);

    /**
     * 发货拦截
     * @param entity
     * @param isCancel 是否取消创建
     * @param isValidate 是否作废多渠道订单
     * @return
     */
    BatchResultDTO deliveryIntercept(SoMultiChannelEntity entity, Boolean isCancel, Boolean isValidate, String remark);

    /**
     * 根据销售订单获取多渠道订单
     * @param id
     * @param isContainDelete 是否包含已删除
     * @return
     */
    SoMultiChannelEntity getBySoId(String id, Boolean isContainDelete);

    SoMultiChannelEntity getByDeliveryCode(String deliveryCode);

    /**
     * 查询多渠道发货状态为空和PING状态数据
     * @return
     */
    List<SoMultiChannelEntity> queryMultiChannelDeliveryStatus();

    /**
     * 根据发货单编号查询销售出库单生成DTO
     * @param deliveryCode
     * @return
     */
    SoOutstockDTO.GenerateB2cDTO getSoOutstockGenerateB2cDTO(String deliveryCode);

    /**
     * 更新销售出库单标识
     * @param dto
     */
    void updateSoOutstock(PlatformSoOutStockDTO dto);

    /**
     * 更新发货状态
     * @param bean
     * @param soMultiChannelEntity
     */
    void updateSoMultiChannelStatus(PlatformFulfillOrderDTO bean, SoMultiChannelEntity soMultiChannelEntity);

    /**
     * 更新多渠道订单出库数量
     * @param outstockQtyDTOList
     */
    void updateSoMultiOutstockQty(List<SoMultiChannelDetailDTO.OutstockQtyDTO> outstockQtyDTOList);

    /**
     * 构建多渠道订单发货单DTO
     * @param dto
     * @return
     */
    SoB2cDTO.SaveSoB2cDistributionDTO buildDistributionDTO(SoMultiChannelDTO.SaveDTO dto);

    /**
     * 根据销售订单ID查询最新的多渠道订单
     * @param soIds
     * @param createStatus
     * @return
     */
    List<SoMultiChannelEntity> getLastBySoId(List<String> soIds, String createStatus);
}

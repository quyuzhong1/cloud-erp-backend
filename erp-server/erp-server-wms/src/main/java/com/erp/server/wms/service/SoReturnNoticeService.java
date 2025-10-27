package com.erp.server.wms.service;

import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cReturnDTO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.wms.dto.SoReturnNoticeDTO;
import com.erp.model.wms.entity.SoReturnNoticeEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 退货通知单
 * @author Luo_WG
 * @since 2023-05-10
 */
public interface SoReturnNoticeService extends SuperService<SoReturnNoticeEntity> {
    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnNoticeDTO.PagingViewDTO>
     **/
    PagingVO<SoReturnNoticeDTO.PagingView> paging(PagingDTO<SoReturnNoticeDTO.PagingParam> pagingParamDTO);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:13
     * @param dto dto
     * @return java.util.List<com.erp.model.wms.dto.SoReturnNoticeDTO.WarehouseReceiveCountDTO>
     **/
    List<SoReturnNoticeDTO.StatusCountDTO> listCount(PermissionsDTO dto);

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    String add(SoReturnNoticeDTO.Add dto);

    /**
     * 新增
     **/
    String addB2c(SoReturnNoticeDTO.Add dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 14:51
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(SoReturnNoticeDTO.Update dto);

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     * @param id id
     * @return com.erp.model.wms.dto.SoReturnNoticeDTO.ViewDTO
     **/
    SoReturnNoticeDTO.View view(String id);

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/14 10:04
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean submit(List<String> ids);

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean addAndSubmit(SoReturnNoticeDTO.Add dto);

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean updateAndSubmit(SoReturnNoticeDTO.Update dto);

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     **/
    BatchResultDTO approve(SoReturnNoticeEntity entity, String type, String comment, Boolean isNeedProcess);

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param entity
     * @return java.lang.Boolean
     **/
    BatchResultDTO disApprove(SoReturnNoticeEntity entity);

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean cancelProcess(ApproveDTO.BatchCancelProcessDTO dto);

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @param remark remark
     * @return java.lang.Boolean
     **/
    Boolean invalid(List<String> ids, String remark);

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean delete(List<String> ids);

    /**
     * @description: 原子批量删除销售退货通知单
     * @author Will
     * @date: 2023/5/17 15:15
     * @param ids
     * @param returnDetails
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> deleteByIds(List<String> ids, boolean returnDetails);

    /**
     * 删除单个实体
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param entity
     * @return BatchResultDTO
     **/
    BatchResultDTO deleteEntity(SoReturnNoticeEntity entity);

    /**
     * 导出
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     **/
    Boolean exportExcel(@RequestBody SoReturnNoticeDTO.PagingParam dto);

    /**
     * 下推退货通知单-保存
     * @Author Luo_WG
     * @Date 2023/5/11 11:23
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateSoReturnNoticeSave(List<SoReturnDTO.GenerateSoReturnNoticeView> list);

    /**
     * 下推退货签收单-列表查询
     * @Author Luo_WG
     * @Date 2023/5/11 11:32
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.SoReturnNoticeDTO.GenerateSoReturnReceiveView>
     **/
    List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> generateSoDeliveryView(List<String> ids);

    /**
     * 根据来源id查询销售退货通知单主表
     * @Author Luo_WG
     * @Date 2023/5/15 12:18
     * @param sourceIds sourceIds
     * @return
     **/
    List<SoReturnNoticeEntity> listBySourceId(List<String> sourceIds);


    PagingVO<SoReturnNoticeDTO.PagingView> exportSoReturnNotice(PagingDTO<SoReturnNoticeDTO.PagingParam> dto);

    void generateSoB2cReturnNotice(List<SoB2cReturnDTO.GenerateSoReturnNoticeView> list);

    BigDecimal calLocalCurrency(BigDecimal exchangeRate, BigDecimal returnAmount);

    BigDecimal calReturnAmount(BigDecimal amount, Integer qty, Integer returnQty);

    /**
     * 根据ID列表获取实体Map
     * @param ids
     * @return Map<String, SoReturnNoticeEntity>
     */
    Map<String, SoReturnNoticeEntity> mapByIds(List<String> ids);
}

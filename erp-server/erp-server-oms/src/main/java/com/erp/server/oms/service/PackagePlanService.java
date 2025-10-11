package com.erp.server.oms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.PackagePlanEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.PackagePlanDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 组包计划主表 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-10-09
 */
public interface PackagePlanService extends SuperService<PackagePlanEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-10-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PackagePlanDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-10-09
    * @param dto
    * @return
    */
    Boolean update(PackagePlanDTO.UpdateDTO dto);

    /**
     * 创建大包
     * @param dto
     * @return
     */
    WorkflowTaskRecordDTO.MqResponseDTO createSupply(WorkflowTaskRecordDTO.MqRequestDTO dto);

    /**
     * 往大包中添加箱子
     * @param dto
     * @return
     */
    WorkflowTaskRecordDTO.MqResponseDTO addBoxToSupply(WorkflowTaskRecordDTO.MqRequestDTO dto);

    /**
     * 往大包中添加订单
     * @param dto
     * @return
     */
    WorkflowTaskRecordDTO.MqResponseDTO addOrderToSupply(WorkflowTaskRecordDTO.MqRequestDTO dto);

    /**
     * 获取订单标签和物流跟踪号
     * @param dto
     * @return
     */
    WorkflowTaskRecordDTO.MqResponseDTO getOrderSticker(WorkflowTaskRecordDTO.MqRequestDTO dto);

    /**
     * 将供货单转入已完成
     * @param dto
     * @return
     */
    WorkflowTaskRecordDTO.MqResponseDTO moveSupplyToDelivery(WorkflowTaskRecordDTO.MqRequestDTO dto);

    /**
     * 获取跨境运输标签
     * @param dto
     * @return
     */
    WorkflowTaskRecordDTO.MqResponseDTO getCrossSticker(WorkflowTaskRecordDTO.MqRequestDTO dto);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PagingVO<PackagePlanDTO.PagingViewDTO> paging(PagingDTO<PackagePlanDTO.PagingParamDTO> dto);

    /**
     * 导出分页查询
     * @param dto
     * @return
     */
    PagingVO<PackagePlanDTO.ExportDTO> exportPaging(PagingDTO<PackagePlanDTO.PagingParamDTO> dto);

    /**
     * 导出
     * @param dto
     */
    void listExport(PackagePlanDTO.PagingParamDTO dto);

    void batchPrint(List<String> ids, HttpServletResponse response);
}

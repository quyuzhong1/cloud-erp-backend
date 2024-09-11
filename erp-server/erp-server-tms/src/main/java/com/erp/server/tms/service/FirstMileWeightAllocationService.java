package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;

import java.util.List;

/**
 * <p>
 * 头程重量分摊 服务类
 * </p>
 *
 * @author tmj
 * @since 2024-08-20
 */
public interface FirstMileWeightAllocationService extends SuperService<FirstMileWeightAllocationEntity> {

    /**
     * 分页查询
     * @param dto
     * @return
     * @date: 2024-08-22
     * @author: tanmujin
     */
    PagingVO<FirstMileWeightAllocationDTO.ViewDTO> paging(PagingDTO<FirstMileWeightAllocationDTO.PagingParamDTO> dto);

    /**
     * 导出Excel
     *
     * @param dto
     * @return
     * @date: 2024-08-22
     * @author: tanmujin
     */
    void exportExcel(FirstMileWeightAllocationDTO.ExportParamDTO dto);

    /**
     * tab页统计
     * @param
     * @return
     * @date: 2024-08-22
     * @author: tanmujin
     */
    List<FirstMileWeightAllocationDTO.TabDTO> tabList();

    /**
     * 重量重算
     * @param logisticsBillId 头程物流单ID
     * @date: 2024-08-22
     * @author: tanmujin
     */
    BatchResultDTO weightReCompute(String logisticsBillId);

    /**
     * 删除
     * @param id
     * @date: 2024-08-22
     * @author: tanmujin
     */
    BatchResultDTO deleteByLogisticsBillId(String id);

    /**
     * 根据物流单号查询重量分摊
     * @param logisticsBillIds 物流单号集合
     * @return
     * @date: 2024-08-23
     * @author: tanmujin
     */
    List<FirstMileWeightAllocationEntity> listByLogisticsBillIds(List<String> logisticsBillIds);

    /**
     * 根据发货单id获取重量分摊记录
     * @param sourceIds
     * @param statusList  CostAllocationStatusEnum
     * @return
     */
    List<FirstMileWeightAllocationEntity> listBySourceIds(List<String> sourceIds, List<String> statusList);

    /**
     * 新增重量分摊
     */
    BatchResultDTO add(String logisticsBillId);
}

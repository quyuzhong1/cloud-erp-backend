package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 头程费用分摊 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-08-20
 */
public interface FirstMileCostAllocationService extends SuperService<FirstMileCostAllocationEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(FirstMileCostAllocationDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-08-20
    * @param dto
    * @return
    */
    Boolean update(FirstMileCostAllocationDTO.UpdateDTO dto);

    /**
     * 分页统计
     * @param dto
     * @return
     */
    List<FirstMileCostAllocationDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PagingVO<FirstMileCostAllocationDTO.PagingVO> paging(PagingDTO<FirstMileCostAllocationDTO.PagingParamDTO> dto);

    /**
     * 删除
     * @param entity
     * @return
     */
    BatchResultDTO delete(FirstMileCostAllocationEntity entity);

    /**
     * 导出excel
     * @param dto
     * @param response
     */
    void exportExcel(FirstMileCostAllocationDTO.PagingParamDTO dto, HttpServletResponse response);

//    /**
//     * 根据sku成本id查询费用分摊记录
//     * @param skuCostId
//     * @return
//     */
//    List<FirstMileCostAllocationEntity> getBySkuCostId(String skuCostId);

    /**
     * 计算分摊费用
     *
     * @param entity
     * @param firstMileDeliveryEntity
     * @param firstMileDeliveryDetailEntityList
     * @return
     */
    BatchResultDTO calcAllocatedCost(FirstMileCostAllocationEntity entity, FirstMileDeliveryEntity firstMileDeliveryEntity,List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList);

    /**
     * 根据发货单获取全部费用分摊记录
     *
     * @param sourceIds
     * @param reportPeriodId
     * @return
     */
    List<FirstMileCostAllocationEntity> listBySourceIds(List<String> sourceIds, String reportPeriodId);

    /**
     * 自动生成费用分摊
     * @param startDate
     * @param endDate
     */
    void autoGenerateFirstMileCostAllocation(LocalDate startDate, LocalDate endDate);
}

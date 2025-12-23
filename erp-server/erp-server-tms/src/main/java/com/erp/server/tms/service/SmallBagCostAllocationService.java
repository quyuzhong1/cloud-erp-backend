package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;
import com.erp.model.tms.entity.SmallBagCostAllocationEntity;

import java.util.List;

/**
 * <p>
 * 小包费用分摊 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-12-02
 */
public interface SmallBagCostAllocationService extends SuperService<SmallBagCostAllocationEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-12-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SmallBagCostAllocationDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-12-02
    * @param dto
    * @return
    */
    Boolean update(SmallBagCostAllocationDTO.UpdateDTO dto);
    
    List<SmallBagCostAllocationDTO.TabListDTO> tabList(PermissionsDTO dto);
    
    PagingVO<SmallBagCostAllocationDTO.ListDTO> paging(PagingDTO<SmallBagCostAllocationDTO.PagingParamDTO> dto);
    
    BatchResultDTO updateReportStatus(String id , String reportDate , String reportStatus);
    
    BatchResultDTO reAllocation(String id);
    
    BatchResultDTO delete(String id);
    
    Boolean exportExcel(SmallBagCostAllocationDTO.PagingParamDTO dto);
    
    BatchResultDTO pushBigTable(String id);

    /**
     * 根据核算期间查询小包分摊记录
     * @param reportPeriodStr
     * @param reportStatus
     * @return
     */
    List<SmallBagCostAllocationEntity> listByReportPeriodStr(String reportPeriodStr, String reportStatus);
    /**
     * 查询小包费用分摊
     * @author will
     * @date 2025/12/10 14:40
     * @param paramDTO
     * @return List<SmallBagCostDTO>
     */
    List<SmallBagCostAllocationDTO.SmallBagCostDTO> listSmallBagCost(SmallBagCostAllocationDTO.SmallBagCostParamDTO paramDTO);
}

package com.erp.server.tms.service;
import com.erp.model.tms.entity.SmallBagCostAllocationEntity;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;

import java.util.List;

import com.common.business.dto.base.*;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;

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
    
    BatchResultDTO pushBigTable(String id);

}

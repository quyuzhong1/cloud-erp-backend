package com.erp.server.tms.service;
import java.util.List;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO;
import com.erp.model.tms.entity.TransferDeclareCostAllocationEntity;

/**
 * <p>
 * 中转费用分摊 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
 */
public interface TransferDeclareCostAllocationService extends SuperService<TransferDeclareCostAllocationEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TransferDeclareCostAllocationDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-12-03
    * @param dto
    * @return
    */
    Boolean update(TransferDeclareCostAllocationDTO.UpdateDTO dto);

    List<TransferDeclareCostAllocationDTO.TabListDTO> tabList(PermissionsDTO dto);
    
    PagingVO<TransferDeclareCostAllocationDTO.ListDTO> paging(PagingDTO<TransferDeclareCostAllocationDTO.PagingParamDTO> dto);
    
    BatchResultDTO updateReportStatus(String id , String reportDate , String reportStatus);
    
    BatchResultDTO reAllocation(String id);
    
    BatchResultDTO delete(String id);
    
    BatchResultDTO pushBigTable(String id);
}

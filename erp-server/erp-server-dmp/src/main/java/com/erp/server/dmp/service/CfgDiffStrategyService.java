package com.erp.server.dmp.service;
import java.util.List;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO;
import com.erp.model.dmp.entity.CfgDiffStrategyEntity;

/**
 * <p>
 * 差异策略配置基础信息 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
 */
public interface CfgDiffStrategyService extends SuperService<CfgDiffStrategyEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-11-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(CfgDiffStrategyDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-11-11
    * @param dto
    * @return
    */
    Boolean update(CfgDiffStrategyDTO.UpdateDTO dto);

    List<CfgDiffStrategyDTO.TabListDTO> tabList(PermissionsDTO dto);
    
    PagingVO<CfgDiffStrategyDTO.ViewDTO> paging(PagingDTO<CfgDiffStrategyDTO.PagingParamDTO> dto);
    
    Boolean exportExcel(CfgDiffStrategyDTO.ExpotParamDTO dto);
    
    void batchOp(CfgDiffStrategyDTO.BatchOpDTO dto);
    
    CfgDiffStrategyDTO.UpdateDTO view(String id);
}

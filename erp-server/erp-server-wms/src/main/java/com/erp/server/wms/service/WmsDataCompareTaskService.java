package com.erp.server.wms.service;
import java.util.List;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.SetNextDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.SetNextViewDTO;
import com.erp.model.wms.entity.WmsDataCompareTaskEntity;
import com.erp.model.wms.entity.WmsDataCompareTempEntity;

/**
 * <p>
 * 数据对比任务 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
 */
public interface WmsDataCompareTaskService extends SuperService<WmsDataCompareTaskEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-03-20
    * @param dto
    * @return
    */
	WmsDataCompareTaskDTO.AddViewDTO add(WmsDataCompareTaskDTO.AddDTO dto);
	
	String downloadSystemData(BaseIdDTO dto);
	
	SetNextViewDTO setNext(SetNextDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-03-20
    * @param dto
    * @return
    */
    Boolean update(WmsDataCompareTaskDTO.UpdateDTO dto);

    /**
     * 高级查询分页查询
     * @param dto
     * @return
     */
    PagingVO<WmsDataCompareTaskDTO.ViewDTO> paging(PagingDTO<WmsDataCompareTaskDTO.PagingParamDTO> dto);
    
    void saveTempTable(String importId , List<WmsDataCompareTempEntity> wmsDataCompareTempEntityList , Integer currParseOffset , boolean isLast);
}

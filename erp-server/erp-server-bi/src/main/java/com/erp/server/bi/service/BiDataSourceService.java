package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.bi.dto.BiDataSourceDTO;
import com.erp.model.bi.entity.BiDataSourceEntity;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/19 9:29
 */
public interface BiDataSourceService extends IService<BiDataSourceEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/19 9:35
     * @param dto
     * @return PagingVO<BiDataSourceDTO>
     */
    PagingVO<BiDataSourceDTO> paging(PagingDTO<AdvanceSearchDTO> dto);
    /**
     * @description: 数据源新增
     * @author Will
     * @date: 2022/12/19 10:50
     * @param list
     * @return Boolean
     */
    Boolean batchAddBiDataSource(List<BiDataSourceDTO> list);
}

package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.DmpReturnOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.dmp.entity.DmpReturnOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Cloud
 * @Entity com.erp.model.plm.entity.DmpReturnOrderInfo
 */
@Mapper
public interface DmpReturnOrderInfoMapper extends BaseMapper<DmpReturnOrderInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/13 16:18
     * @param query
     * @param params
     * @return IPage<DmpReturnOrderInfoDTO>
     */
    IPage<DmpReturnOrderInfoDTO> paging(Page query, DmpReturnOrderInfoSearchDTO params);
}





package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.DmpOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoDTO;
import com.erp.model.bi.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Entity com.erp.model.plm.entity.DmpOrderInfo
 */
@Mapper
public interface DmpOrderInfoMapper extends BaseMapper<DmpOrderInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/13 15:50
     * @param query
     * @param params
     * @return IPage
     */
    IPage<DmpOrderInfoDTO> paging(Page query, DmpReturnOrderInfoSearchDTO params);
}





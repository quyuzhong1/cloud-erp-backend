package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.DmpRefundInfoDTO;
import com.erp.model.bi.dto.DmpRefundInfoSearchDTO;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.erp.model.plm.entity.DmpRefundInfo
 */
@Mapper
public interface DmpRefundInfoMapper extends BaseMapper<DmpRefundInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 12:21
     * @param query
     * @param params
     * @return IPage<DmpRefundInfoDTO>
     */
    IPage<DmpRefundInfoDTO> paging(Page query,@Param("params") DmpRefundInfoSearchDTO params);
}





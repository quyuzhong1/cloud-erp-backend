package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.bi.dto.DmpShopChangeLogDTO;
import com.erp.model.dmp.entity.DmpShopChangeLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 14:31
 */
@Mapper
public interface DmpShopChangeLogMapper extends BaseMapper<DmpShopChangeLogEntity> {

    IPage<DmpShopChangeLogDTO> paging(Page query,@Param("params") AdvanceSearchDTO params);
}

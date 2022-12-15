package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.DmpShopInfoShowDTO;
import com.erp.model.bi.dto.DmpShopInfoSearchDTO;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 14:43
 */
@Mapper
public interface DmpShopInfoMapper extends BaseMapper<DmpShopInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 14:57
     * @param query
     * @param params
     * @return IPage<DmpShopInfoDTO>
     */
    IPage<DmpShopInfoShowDTO> paging(Page query, @Param("params") DmpShopInfoSearchDTO params);
}

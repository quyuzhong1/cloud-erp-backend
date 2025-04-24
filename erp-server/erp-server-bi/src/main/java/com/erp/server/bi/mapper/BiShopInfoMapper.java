package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.DmpShopInfoExcelDTO;
import com.erp.model.dmp.dto.DmpShopInfoShowDTO;
import com.erp.model.dmp.dto.DmpShopInfoSearchDTO;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/14 14:43
 */
@Mapper
public interface BiShopInfoMapper extends BaseMapper<BiShopInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 14:57
     * @param query
     * @param params
     * @return IPage<DmpShopInfoDTO>
     */
    IPage<DmpShopInfoShowDTO> paging(Page<Object> query, @Param("params") DmpShopInfoSearchDTO params);
    /**
     * @description: 查询所有店铺数据导出
     * @author Will
     * @date: 2022/12/15 17:20
     * @param params
     * @return List<DmpShopInfoExcelDTO>
     */
    List<DmpShopInfoExcelDTO> getAllDmpShopInfo(@Param("params") DmpShopInfoSearchDTO params);
}

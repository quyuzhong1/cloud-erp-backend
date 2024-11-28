package com.erp.server.wms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 海外物流商仓库 Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Mapper
public interface OverseasProviderWarehouseMapper extends BaseMapper<OverseasProviderWarehouseEntity> {

    /**
     * @Description 根据仓库ids 获取对应数据
     * @author lambda
     * @date 2023-12-13 16:11
     * @Param warehouseIds
     * @Return
     */
    List<OverseasProviderWarehouseDTO.ViewDTO> listByWarehouseIdList(@Param("warehouseIds") List<String> warehouseIds);

    IPage<ThirdWarehouseDTO.PageSelectDTO> pagingSelect(Page query, @Param("params") OverseasProviderWarehouseDTO.SelectDTO params);

    /**
     * 高级查询
     * @param compareCodeSplicingValueSql 参数
     */
    List<String> listProviderWarehouseBySql(@Param("sql") String compareCodeSplicingValueSql);
}

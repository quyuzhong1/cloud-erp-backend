package com.erp.server.dmp.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 第三方系统仓库表 Mapper 接口
 * </p>
 *
 * @author hyj
 * @since 2024-05-17
 */
@Mapper
public interface ThirdWarehouseMapper extends BaseMapper<ThirdWarehouseEntity> {

    IPage<ThirdWarehouseDTO.PageSelectDTO> pagingSelect(Page query, ThirdWarehouseDTO.SelectDTO params);

    /**
     * 根据分类和第三方id获取
     * @param thirdId
     * @param category
     * @return
     */
    ThirdWarehouseEntity getByWarehouseId(@Param("thirdId") String thirdId, @Param("category")String category);

    /**
     * 查询映射列表
     * @param params
     * @return
     */
    List<ThirdWarehouseDTO.QueryMapDTO> listQueryMapping(@Param("params") ThirdWarehouseDTO.QueryMapParamDTO params);
}

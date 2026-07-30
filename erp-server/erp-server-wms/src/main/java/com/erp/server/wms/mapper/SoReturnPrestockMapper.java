package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SoReturnPrestockDTO;
import com.erp.model.wms.entity.SoReturnPrestockEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 预入库单主表 Mapper 接口
 * </p>
 *
 * @author auto
 * @since 2026-06-30
 */
@Mapper
public interface SoReturnPrestockMapper extends BaseMapper<SoReturnPrestockEntity> {

    /**
     * 分页查询
     *
     * @param page   分页参数
     * @param params 查询条件
     * @return 分页结果
     */
    IPage<SoReturnPrestockDTO.PagingView> paging(Page<SoReturnPrestockDTO.PagingView> page,
                                                  @Param("params") SoReturnPrestockDTO.PagingParam params);
}

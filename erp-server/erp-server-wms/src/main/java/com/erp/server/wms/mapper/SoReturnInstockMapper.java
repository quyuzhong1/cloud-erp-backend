package com.erp.server.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Mapper
public interface SoReturnInstockMapper extends BaseMapper<SoReturnInstockEntity> {

    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/5/19 10:43
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.wms.dto.SoReturnInstockDTO.PagingView>
     **/
    IPage<SoReturnInstockDTO.PagingView> paging(Page query, @Param("params") SoReturnInstockDTO.PagingParam params);
}

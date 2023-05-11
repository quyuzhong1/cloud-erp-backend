package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.SoReturnEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Mapper
public interface SoReturnMapper extends BaseMapper<SoReturnEntity> {

    /**
     * 列表分页查询
     * @Author Luo_WG
     * @Date 2023/5/11 17:53
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.oms.dto.SoReturnDTO.PagingParam>
     **/
    IPage<SoReturnDTO.PagingView> paging(Page query, SoReturnDTO.PagingParam params);
}

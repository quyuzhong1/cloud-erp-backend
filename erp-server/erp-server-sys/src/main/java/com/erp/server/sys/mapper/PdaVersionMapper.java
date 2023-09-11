package com.erp.server.sys.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.PdaVersionDTO;
import com.erp.model.sys.entity.PdaVersionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-14
 */
@Mapper
public interface PdaVersionMapper extends BaseMapper<PdaVersionEntity> {

    /**
     * 列表分页查询
     * @Author Luo_WG
     * @Date 2023/9/11 16:02
     * @param query
     * @param params
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.sys.dto.PdaVersionDTO.PagingDTO>
     **/
    IPage<PdaVersionDTO.PagingDTO> paging(Page query, PdaVersionDTO.PagingParamDTO params);
}

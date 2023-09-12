package com.erp.server.sys.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.PdaVersionDTO;
import com.erp.model.sys.entity.PdaVersionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


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

    /**
     * 获取pda最新版本
     * @Author Luo_WG
     * @Date 2023/9/12 12:29
     * @param userId
     * @return com.erp.model.sys.entity.PdaVersionEntity
     **/
    PdaVersionEntity getPdaVersion(@Param("userId") String userId);
}

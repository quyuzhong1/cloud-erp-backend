package com.erp.server.sys.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.entity.KingdeeOperatorRefPostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 金蝶业务员表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Mapper
public interface KingdeeOperatorRefPostMapper extends BaseMapper<KingdeeOperatorRefPostEntity> {

    /**
     * 分页查询
     * @description
     * @param
     * @return
     * @date 2024-03-15 11:59
     * @author Lambda
     */
    IPage<KingdeeOperatorRefPostDTO.PagingViewDTO> paging(Page query, @Param("params")KingdeeOperatorRefPostDTO.PagingParamDTO paramDTO);
}

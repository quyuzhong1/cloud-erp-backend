package com.erp.server.sys.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.KingdeeDepartmentDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 金蝶岗位表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Mapper
public interface KingdeePostMapper extends BaseMapper<KingdeePostEntity> {

    /**
     * 分页查询
     * @description
     * @param
     * @return
     * @date 2024-03-13 18:53
     * @author Lambda
     */
    IPage<KingdeePostDTO.PagingViewDTO> paging(Page query, @Param("params")KingdeePostDTO.PagingParamDTO paramDTO);
}

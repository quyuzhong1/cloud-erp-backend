package com.erp.server.sys.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.KingdeeDepartmentDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Mapper
public interface KingdeeDepartmentMapper extends BaseMapper<KingdeeDepartmentEntity> {

    /**
     * 分页查询
     * @description
     * @param
     * @return
     * @date 2024-03-11 18:25
     * @author Lambda
     */
    IPage<KingdeeDepartmentDTO.PagingViewDTO> paging(Page query, @Param("params")KingdeeDepartmentDTO.PagingParamDTO paramDTO);
}

package com.erp.server.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.sys.dto.DictBasicAllDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 字典表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
@Mapper
public interface DictBasicMapper extends BaseMapper<DictBasicEntity> {
	IPage<DictBasicAllDTO.ViewDTO> paging(Page query, @Param("params") DictBasicAllDTO.PagingParamDTO params);
}

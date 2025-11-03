package com.erp.server.dmp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.dmp.entity.DictBasicEntity;
import com.erp.model.sys.dto.DictBasicAllDTO;

/**
 * 字典表 Mapper 接口
 *
 * @author will
 * @since 2023-05-08
 */
@Mapper
public interface DictBasicMapper extends BaseMapper<DictBasicEntity> {
	IPage<DictBasicAllDTO.ViewDTO> paging(Page query, @Param("params") DictBasicAllDTO.PagingParamDTO params);
}

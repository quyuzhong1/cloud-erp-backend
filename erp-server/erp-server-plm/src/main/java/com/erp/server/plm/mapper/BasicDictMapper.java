package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.sys.dto.DictBasicAllDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * plm 字典表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface BasicDictMapper extends BaseMapper<BasicDictEntity> {
	IPage<DictBasicAllDTO.ViewDTO> paging(Page query, @Param("params") DictBasicAllDTO.PagingParamDTO params);
}

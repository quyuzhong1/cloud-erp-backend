package com.erp.server.tms.mapper;
import com.erp.model.sys.dto.DictBasicAllDTO;
import com.erp.model.tms.entity.DictBasicEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 字典表 Mapper 接口
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Mapper
public interface DictBasicMapper extends BaseMapper<DictBasicEntity> {
	IPage<DictBasicAllDTO.ViewDTO> paging(Page query, @Param("params") DictBasicAllDTO.PagingParamDTO params);
}

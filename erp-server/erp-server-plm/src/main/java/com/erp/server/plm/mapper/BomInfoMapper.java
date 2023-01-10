package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.BomSearchPagingDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.vo.BomPagingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * bom 信息表(BomInfo)表数据库访问层
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
@Mapper
public interface BomInfoMapper  extends BaseMapper<BomInfoEntity> {


    Integer getMaxSequence();

    IPage<BomPagingVO> paging(Page query, @Param("param") BomSearchPagingDTO params);
}


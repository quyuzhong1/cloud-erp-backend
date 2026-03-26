package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.entity.BomChangeEntity;
import com.erp.model.plm.vo.BomChangePagingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 变更信息表(ProductChange)表数据库访问层
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@Mapper
public interface BomChangeMapper extends BaseMapper<BomChangeEntity> {


    IPage<BomChangePagingVO> paging(Page query, @Param("searchList") List<String> changeSearch, @Param("params") SearchPagingDTO params);

    List<String> getChangeSearchCondition(@Param("searchKeyword") String searchKeyword);
}


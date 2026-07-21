package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.CfgProductForbiddenWordDTO;
import com.erp.model.plm.entity.CfgProductForbiddenWordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 产品违禁词库 Mapper 接口
 * </p>
 */
@Mapper
public interface CfgProductForbiddenWordMapper extends BaseMapper<CfgProductForbiddenWordEntity> {

    List<CfgProductForbiddenWordDTO.TabListDTO> tabList();

    IPage<CfgProductForbiddenWordDTO.ListDTO> paging(Page query, @Param("params") CfgProductForbiddenWordDTO.PagingParamDTO params);

    List<CfgProductForbiddenWordDTO.ListDTO> listAll(@Param("params") CfgProductForbiddenWordDTO.PagingParamDTO params);

    CfgProductForbiddenWordEntity getByNormalizedWord(@Param("word") String word);

    Integer countByNormalizedWord(@Param("word") String word, @Param("excludeId") String excludeId);

    List<String> listEnabledWords();

    LocalDateTime getEnabledWordsMaxUpdateTime();
}

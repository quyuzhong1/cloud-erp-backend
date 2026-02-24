package com.erp.server.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Lambda
 * @Classname DictCurrencyMapper

 * @Date 2023-03-21 17:20
 * @Created by yl
 */
@Mapper
public interface DictCurrencyMapper  extends BaseMapper<DictCurrencyEntity> {
    List<CurrencyDTO.ViewDTO> getList();

    IPage<CurrencyDTO.ViewDTO> pagingSelect(@Param("query") Page query, @Param("params") CurrencyDTO.SelectDTO params);
}

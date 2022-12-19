package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.vo.SalesBaseVO;
import com.erp.model.bi.vo.SalesVO;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Classname SalesOrderServiceMapper
 * @Description TODO
 * @Date 2022-12-16 11:09
 * @Created by yl
 */
@Mapper
public interface SalesOrderServiceMapper  extends BaseMapper<DmpOrderInfoEntity> {
    List<Map<String, Object>> getMonthSales();

    List<SalesVO> getBySku(@Param("params") BiFilterDTO dto);

    List<SalesBaseVO> getLastThirtyDays(@Param("params") BiFilterDTO dto);


    List<SalesVO> getBySpu(BiFilterDTO dto);
}

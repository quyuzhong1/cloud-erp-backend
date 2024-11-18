package com.erp.server.bi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.dto.DmpRefundInfoDTO;
import com.erp.model.dmp.dto.DmpRefundInfoSearchDTO;
import com.erp.model.dmp.entity.BiRefundInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.DmpRefundInfo
 */
@Mapper
public interface BiRefundInfoMapper extends BaseMapper<BiRefundInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 12:21
     * @param query
     * @param params
     * @return IPage<DmpRefundInfoDTO>
     */
    IPage<DmpRefundInfoDTO> paging(Page<Object> query,@Param("params") DmpRefundInfoSearchDTO params);
    /**
     * @description: 导出
     * @author Will
     * @date: 2022/12/15 10:38
     * @param params
     * @return List<DmpRefundInfoDTO>
     */
    List<DmpRefundInfoDTO> getAllRefundInfo(@Param("params") DmpRefundInfoSearchDTO params);

    /**
     * 获取退款金额
     * @param dto
     * @return
     */
    BigDecimal getRefundOrderAmount(@Param("params") BiFilterDTO dto);

    /**
     * 获取年退款金额
     * @param dto
     * @param yearStr
     * @return
     */
    BigDecimal getYearRefundOrderAmount(@Param("params")BiFilterDTO dto, @Param("year") String yearStr);
}





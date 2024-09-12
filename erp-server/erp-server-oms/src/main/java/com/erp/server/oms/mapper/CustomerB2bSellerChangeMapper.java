package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import com.erp.model.oms.dto.excel.CustomerB2bSellerExcelDTO;
import com.erp.model.oms.entity.CustomerB2bSellerChangeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * b2b客户销售员变更单 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-01-31
 */
@Mapper
public interface CustomerB2bSellerChangeMapper extends BaseMapper<CustomerB2bSellerChangeEntity> {

    List<CustomerB2bSellerChangeDTO.TabFlagDTO> countByTabFlag();

    IPage<CustomerB2bSellerChangeDTO.ListDTO> paging(Page query, @Param("params")CustomerB2bSellerChangeDTO.ParamDTO dto);

    List<CustomerB2bSellerExcelDTO> export(@Param("params") CustomerB2bSellerChangeDTO.ParamDTO dto);
    Page<CustomerB2bSellerExcelDTO> export(@Param("page") Page<CustomerB2bSellerExcelDTO> page,@Param("params") CustomerB2bSellerChangeDTO.ParamDTO dto);
}

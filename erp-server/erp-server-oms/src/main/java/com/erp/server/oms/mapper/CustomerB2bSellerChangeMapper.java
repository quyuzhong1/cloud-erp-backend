package com.erp.server.oms.mapper;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import com.erp.model.oms.entity.CustomerB2bSellerChangeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

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

    List<CustomerB2bSellerChangeDTO.ListDTO> paging(@Param("params") CustomerB2bSellerChangeDTO.ParamDTO dto);

    List<CustomerB2bSellerChangeDTO.TabFlagDTO> countByTabFlag();
}

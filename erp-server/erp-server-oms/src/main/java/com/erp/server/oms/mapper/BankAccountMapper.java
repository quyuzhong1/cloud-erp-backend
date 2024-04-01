package com.erp.server.oms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.BankAccountDTO;
import com.erp.model.oms.entity.BankAccountEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * <p>
 * 银行账号 Mapper 接口
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-07-04
 */
@Mapper
public interface BankAccountMapper extends BaseMapper<BankAccountEntity> {


    IPage<BankAccountDTO.PagingViewDTO> paging(Page query, @Param("params")BankAccountDTO.PagingParamDTO params);
}

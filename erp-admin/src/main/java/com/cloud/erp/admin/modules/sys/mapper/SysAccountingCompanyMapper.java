package com.cloud.erp.admin.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.erp.admin.modules.sys.entity.SysAccountingCompanyEntity;
import com.erp.common.dto.BasePagingSearchDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Classname SysAccountingCompanyMapper
 * @Description TODO
 * @Date 2022-07-12 9:50
 * @Created by yl
 */
@Mapper
public interface SysAccountingCompanyMapper  extends BaseMapper<SysAccountingCompanyEntity> {

    IPage<SysAccountingCompanyEntity> paging(Page query,@Param("params") BasePagingSearchDTO params);
}

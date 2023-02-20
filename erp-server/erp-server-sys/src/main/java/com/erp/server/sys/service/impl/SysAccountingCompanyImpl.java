package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.business.dto.base.BatchStateDTO;
import com.erp.common.business.dto.base.PagingDTO;
import com.erp.common.business.dto.base.StateDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.common.business.vo.PagingVO;
import com.erp.model.sys.dto.CompanyPagingSearchDTO;
import com.erp.model.sys.dto.SysAccountingCompanyDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.server.sys.mapper.SysAccountingCompanyMapper;
import com.erp.server.sys.service.SysAccountingCompanyService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @Classname SysAccountingCompanyImpl
 * @Description TODO
 * @Date 2022-07-12 9:52
 * @Created by yl
 */
@Service
public class SysAccountingCompanyImpl extends ServiceImpl<SysAccountingCompanyMapper, SysAccountingCompanyEntity> implements SysAccountingCompanyService {

    /**
     * 保存公司信息
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-12 10:12
     */

    @Override
    public boolean saveCompany(SysAccountingCompanyDTO dto) {
        SysAccountingCompanyEntity entity = new SysAccountingCompanyEntity();
        BeanMapperUtils.copy(dto, entity);
        return this.save(entity);
    }

    /**
     * 修改公司信息
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-12 10:15
     */

    @Override
    public boolean updateCompany(SysAccountingCompanyDTO dto) {
        SysAccountingCompanyEntity entity = new SysAccountingCompanyEntity();
        BeanMapperUtils.copy(dto, entity);
        return this.updateById(entity);
    }

    /**
     * 更改状态
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-12 10:30
     */

    @Override
    public boolean updateCompanyState(StateDTO dto) {
        SysAccountingCompanyEntity entity = this.getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        entity.setCompanyState(Integer.valueOf(dto.getState()));
        return this.updateById(entity);
    }

    /**
     * 分页获取公司数据
     *
     * @param dto
     * @return com.cloud.erp.common.common.vo.PagingVO<com.cloud.erp.admin.modules.sys.entity.SysAccountingCompanyEntity>
     * @author yl
     * @date 2022-07-12 11:34
     */

    @Override
    public PagingVO paging(PagingDTO<CompanyPagingSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        CompanyPagingSearchDTO params = dto.getParams();
        String searchType = params.getSearchType();
        String searchTypeStr="company_name,contact_name,contact_address,currency";
        List<String> searchTypeList = Arrays.asList(searchTypeStr.split(","));
        if (!searchTypeList.contains(searchType)) {
            throw new ServiceException(ApiError.ERROR_9022);
        }
        IPage pageData = baseMapper.paging(query, params);

        return new PagingVO(pageData);
    }


    /**
     * 批量修改 核算公司状态
     *
     * @param dto
     * @return boolean
     * @author yl
     * @date 2022-07-29 10:10
     */
    @Override
    public boolean batchUpdateCompanyState(BatchStateDTO dto) {
        LambdaUpdateWrapper<SysAccountingCompanyEntity> updateWrapper = new LambdaUpdateWrapper<>();
        List<String> ids = dto.getIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            updateWrapper.in(SysAccountingCompanyEntity::getId, dto.getIds());
            updateWrapper.set(SysAccountingCompanyEntity::getCompanyState, dto.getState());
            return this.update(updateWrapper);
        }
        return false;

    }


}

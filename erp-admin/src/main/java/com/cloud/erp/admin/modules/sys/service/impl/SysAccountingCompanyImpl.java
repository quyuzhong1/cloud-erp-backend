package com.cloud.erp.admin.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.admin.modules.sys.dto.SysAccountingCompanyDTO;
import com.cloud.erp.admin.modules.sys.entity.SysAccountingCompanyEntity;
import com.cloud.erp.admin.modules.sys.mapper.SysAccountingCompanyMapper;
import com.cloud.erp.admin.modules.sys.service.SysAccountingCompanyService;
import com.cloud.erp.common.common.ApiError;
import com.cloud.erp.common.common.dto.BasePagingSearchDTO;
import com.cloud.erp.common.common.dto.PagingDTO;
import com.cloud.erp.common.common.dto.StateDTO;
import com.cloud.erp.common.common.exception.ServiceException;
import com.cloud.erp.common.common.vo.PagingVO;
import com.cloud.erp.common.utils.BeanMapperUtils;
import org.springframework.stereotype.Service;

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
     * @author yl
     * @date 2022-07-12 10:15
     * @param dto
     * @return boolean
     */

    @Override
    public boolean updateCompany(SysAccountingCompanyDTO dto) {
        SysAccountingCompanyEntity entity = new SysAccountingCompanyEntity();
        BeanMapperUtils.copy(dto, entity);
        return this.updateById(entity);
    }

    /**
     * 更改状态
     * @author yl
     * @date 2022-07-12 10:30
     * @param dto
     * @return boolean
     */

    @Override
    public boolean updateCompanyState(StateDTO dto) {
        SysAccountingCompanyEntity entity=this.getById(dto.getId());
        if(Objects.isNull(entity)){
            throw new ServiceException(ApiError.ERROR_9014);
        }
        entity.setCompanyState(Integer.valueOf(dto.getState()));
        return this.updateById(entity);
    }

    /**
     * 分页获取公司数据
     * @author yl
     * @date 2022-07-12 11:34
     * @param dto
     * @return com.cloud.erp.common.common.vo.PagingVO<com.cloud.erp.admin.modules.sys.entity.SysAccountingCompanyEntity>
     */

    @Override
    public PagingVO paging(PagingDTO<BasePagingSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BasePagingSearchDTO params=dto.getParams();
        IPage pageData= baseMapper.paging(query,params);

        return new PagingVO(pageData);
    }


}

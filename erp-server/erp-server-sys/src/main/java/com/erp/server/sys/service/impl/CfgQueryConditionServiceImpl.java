package com.erp.server.sys.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.entity.CfgQueryConditionEntity;
import com.erp.server.sys.mapper.CfgQueryConditionMapper;
import com.erp.server.sys.service.CfgQueryConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.CfgQueryConditionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 查询条件配置表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-01-03
 */
@Slf4j
@Service
public class CfgQueryConditionServiceImpl extends SuperServiceImpl<CfgQueryConditionMapper, CfgQueryConditionEntity> implements CfgQueryConditionService {

    @Autowired
    private CommonService commonService;

    @Override
    public Boolean add(CfgQueryConditionDTO.AddDTO dto) {
        CfgQueryConditionEntity entity = new CfgQueryConditionEntity();
        BeanUtil.copyProperties(dto,entity);
        return this.save(entity);
    }

    @Override
    public Boolean update(CfgQueryConditionDTO.UpdateDTO dto) {
        CfgQueryConditionEntity entity = this.getById(dto.getId());
        BeanUtil.copyProperties(dto,entity,"id");
        return this.updateById(entity);
    }

    @Override
    public List<CfgQueryConditionDTO.ViewDTO> getQueryCondition(String code) {
        List<CfgQueryConditionDTO.ViewDTO> viewDTO = baseMapper.getQueryConditionByCode(code);
        return viewDTO;
    }

    @Override
    public PagingVO<CfgQueryConditionDTO.ListDTO> paging(PagingDTO<CfgQueryConditionDTO.SearchParamDTO> pagingDTO) {
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<CfgQueryConditionDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        return new PagingVO(pageData);
    }

    @Override
    public Boolean delete(CfgQueryConditionDTO.UpdateDTO updateDTO) {
        return this.removeById(updateDTO.getId());
    }
}

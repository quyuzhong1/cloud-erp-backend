package com.erp.server.sys.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.sys.dto.CfgQueryOptionDTO;
import com.erp.model.sys.entity.CfgQueryOptionEntity;
import com.erp.server.sys.mapper.CfgQueryOptionMapper;
import com.erp.server.sys.service.CfgQueryOptionService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 查询option配置表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-01-04
 */
@Slf4j
@Service
public class CfgQueryOptionServiceImpl extends SuperServiceImpl<CfgQueryOptionMapper, CfgQueryOptionEntity> implements CfgQueryOptionService {


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgQueryOptionDTO.AddDTO addDTO) {
        if(StringUtils.isBlank(addDTO.getSelectLabel())){
            addDTO.setSelectLabel("value");
        }
        if(StringUtils.isBlank(addDTO.getSelectValue())){
            addDTO.setSelectValue("code");
        }
        if(StringUtils.isBlank(addDTO.getSelectDisabled())){
            addDTO.setSelectDisabled("disabled");
        }
        CfgQueryOptionEntity cfgQueryOptionEntity = new CfgQueryOptionEntity();
        BeanMapperUtils.copy(addDTO, cfgQueryOptionEntity);

        // 数据处理
        handleData(cfgQueryOptionEntity);

        log.info("开始新增查询option配置单");
        boolean save = super.save(cfgQueryOptionEntity);
        if(!save) {
            throw new ServiceException("option配置单保存失败");
        }
        return new BaseResultDTO.AddDTO(cfgQueryOptionEntity.getId(), cfgQueryOptionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgQueryOptionDTO.UpdateDTO updateDTO) {
        CfgQueryOptionEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "查询option配置单"));
        CfgQueryOptionEntity cfgQueryOptionEntity =  BeanMapperUtils.map(CfgQueryOptionEntity.class, updateDTO);

        // 数据处理
        handleData(cfgQueryOptionEntity);
        log.info("编辑 开始修改查询option配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgQueryOptionEntity);
        if(!save) {
            throw new ServiceException("option配置单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public void delete(BaseIdsDTO.IdsDTO dto) {
        this.removeByIds(dto.getIds());
    }

    @Override
    public PagingVO<CfgQueryOptionDTO.ListDTO> paging(PagingDTO<CfgQueryOptionDTO.ParamDTO> pagingDTO) {
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<CfgQueryOptionDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        return new PagingVO(pageData);
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgQueryOptionEntity cfgQueryOptionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

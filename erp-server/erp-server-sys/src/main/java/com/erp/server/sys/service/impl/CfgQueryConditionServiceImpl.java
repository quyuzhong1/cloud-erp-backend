package com.erp.server.sys.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDisplayTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.CfgQueryConditionDTO;
import com.erp.model.sys.entity.CfgQueryConditionEntity;
import com.erp.server.sys.mapper.CfgQueryConditionMapper;
import com.erp.server.sys.service.CfgQueryConditionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
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


    @Override
    public Boolean add(CfgQueryConditionDTO.AddDTO dto) {
        List<CfgQueryConditionEntity> dbEntityList = this.listByCode(dto.getCode());
        if(dbEntityList.stream().anyMatch(v->v.getValue().equals(dto.getValue()) && v.getDisplayType().equals(dto.getDisplayType()))){
            throw new ServiceException("已存在配置字段,无法重复新增");
        }
        if(QueryDisplayTypeEnum.TAB.getCode().equals(dto.getDisplayType()) && dbEntityList.stream().anyMatch(v->v.getDisplayType().equals(QueryDisplayTypeEnum.TAB.getCode()))){
            throw new ServiceException("tabFlag类型字段只能配置一个");
        }
        if(QueryDisplayTypeEnum.EXPORT.getCode().equals(dto.getDisplayType()) && dbEntityList.stream().anyMatch(v->v.getDisplayType().equals(QueryDisplayTypeEnum.EXPORT.getCode()))){
            throw new ServiceException("导出类型字段只能配置一个");
        }
        CfgQueryConditionEntity entity = new CfgQueryConditionEntity();
        BeanUtil.copyProperties(dto,entity);
        return this.save(entity);
    }

    @Override
    public Boolean update(CfgQueryConditionDTO.UpdateDTO dto) {
        CfgQueryConditionEntity entity = this.getById(dto.getId());
        BeanUtil.copyProperties(dto,entity,"id");
        List<CfgQueryConditionEntity> dbEntityList = this.listByCode(dto.getCode());
        return this.updateById(entity);
    }

    @Override
    public List<CfgQueryConditionDTO.ViewDTO> getQueryCondition(String code) {
        List<CfgQueryConditionDTO.ViewDTO> viewList = baseMapper.getQueryConditionByCode(code);
        viewList.forEach(v->{
            if(v.getIsExtend()){
                v.getCompareList().removeIf(item -> item.getLogic().equals(QueryConditionEnum.IS_NULL.getCompareCode()) || item.getLogic().equals(QueryConditionEnum.NOT_NULL.getCompareCode()));
            }
            if("select".equals(v.getType())){
                v.getCompareList().removeIf(item -> item.getLogic().equals(QueryConditionEnum.CONTAINS.getCompareCode())
                        || item.getLogic().equals(QueryConditionEnum.NOT_CONTAINS.getCompareCode())
                        || item.getLogic().equals(QueryConditionEnum.ENDS_WITH.getCompareCode())
                        || item.getLogic().equals(QueryConditionEnum.STARTS_WITH.getCompareCode()));
            }
        });
        return viewList;
    }

    @Override
    public PagingVO<CfgQueryConditionDTO.ListDTO> paging(PagingDTO<CfgQueryConditionDTO.SearchParamDTO> pagingDTO) {
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<CfgQueryConditionDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        return new PagingVO(pageData);
    }

    @Override
    public PagingVO<CfgQueryConditionDTO.MenuDTO> menuPaging(PagingDTO<CfgQueryConditionDTO.MenuSearchParamDTO> pagingDTO) {
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<CfgQueryConditionDTO.MenuDTO> pageData = this.baseMapper.menuPaging(query, pagingDTO.getParams());
        return new PagingVO(pageData);
    }

    @Override
    public Boolean delete(BaseIdsDTO.IdsDTO idsDTO) {
        return this.removeByIds(idsDTO.getIds());
    }

    @Override
    public CfgQueryConditionEntity getByCodeAndField(String code, String field) {
        LambdaQueryWrapper<CfgQueryConditionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CfgQueryConditionEntity::getCode, code);
        queryWrapper.eq(CfgQueryConditionEntity::getValue,field);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public List<CfgQueryConditionEntity> listByCode(String code) {
        LambdaQueryWrapper<CfgQueryConditionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CfgQueryConditionEntity::getCode, code);
        return this.list(queryWrapper);
    }
}

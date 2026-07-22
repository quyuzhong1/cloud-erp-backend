package com.erp.server.sys.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.RedisCacheConstants;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    public static final String CREATE_TIME_NAME = "创建时间";
    public static final String CREATE_TIME = "create_time";

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = RedisCacheConstants.SYS_CFG_QUERY_CONDITION_BY_CODE, allEntries = true)
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
        boolean save = this.save(entity);
        // 查询相同页面编码记录重新排序
        reIndexAndUpdate(dto, "");
        return save;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = RedisCacheConstants.SYS_CFG_QUERY_CONDITION_BY_CODE, allEntries = true)
    public Boolean update(CfgQueryConditionDTO.UpdateDTO dto) {
        CfgQueryConditionEntity entity = this.getById(dto.getId());
        BeanUtil.copyProperties(dto,entity,"id");
        List<CfgQueryConditionEntity> dbEntityList = this.listByCode(dto.getCode());
        boolean result = this.updateById(entity);
        // 查询相同页面编码和大于序号的记录重新排序
        reIndexAndUpdate(dto, dto.getId());
        return result;
    }

    @Override
    @Cacheable(cacheNames = RedisCacheConstants.SYS_CFG_QUERY_CONDITION_BY_CODE, key = "#code", sync = true)
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
    @CacheEvict(cacheNames = RedisCacheConstants.SYS_CFG_QUERY_CONDITION_BY_CODE, allEntries = true)
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reIndexAndUpdate(CfgQueryConditionDTO.CommonDTO dto, String id) {
        List<CfgQueryConditionEntity> list = this.lambdaQuery()
                .eq(CfgQueryConditionEntity::getCode, dto.getCode())
                .ne(StringUtils.isNotBlank(id), CfgQueryConditionEntity::getId, id)
                .orderByAsc(CfgQueryConditionEntity::getIndex)
                .list();
        if(CollectionUtils.isEmpty(list)){
            return true;
        }
        // tab类型从1000开始
        int tabIndex = 1000;
        // 导出从2000开始
        int exportIndex = 2000;
        // 当前序号
        int curIndex = 1;
        // 提交的序号
        int submitIndex = dto.getIndex();
        // 存在创建时间
        boolean hasCreateTime = (dto.getValue().contains(CREATE_TIME) && CREATE_TIME_NAME.equalsIgnoreCase(dto.getLabel()))
                || list.stream().anyMatch(e-> e.getValue().contains(CREATE_TIME) && CREATE_TIME_NAME.equalsIgnoreCase(e.getLabel()));

        // 重新排序
        for (CfgQueryConditionEntity curEntity : list) {
            // tab字段
            if (curEntity.getIsExtend() && curEntity.getValue().contains("tab")){
                curEntity.setIndex(tabIndex);
                tabIndex ++ ;
                continue;
            }
            // 导出字段
            if ("export".equalsIgnoreCase(curEntity.getDisplayType())){
                curEntity.setIndex(exportIndex);
                exportIndex ++ ;
                continue;
            }

            // 创建时间固定序号是3
            if (curEntity.getValue().contains(CREATE_TIME) && CREATE_TIME_NAME.equalsIgnoreCase(curEntity.getLabel())){
                curEntity.setIndex(3);
                continue;
            }

            // 和提交的序号相同 + 1
            if (curIndex == submitIndex || (hasCreateTime && 3 == curIndex)){
                curIndex ++;
            }
            curEntity.setIndex(curIndex);
            curIndex ++ ;
        }
        return updateBatchById(list);
    }
}

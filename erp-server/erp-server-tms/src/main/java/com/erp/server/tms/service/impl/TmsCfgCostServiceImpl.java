package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.constant.SqlConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.TmsCfgCostDTO;
import com.erp.model.tms.entity.CfgReconciliationFieldEntity;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.model.tms.entity.TmsCfgCostEntity;
import com.erp.model.tms.entity.TmsCostDetailEntity;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.server.tms.mapper.TmsCfgCostMapper;
import com.erp.server.tms.service.CfgReconciliationFieldService;
import com.erp.server.tms.service.DictBasicService;
import com.erp.server.tms.service.TmsCfgCostService;
import com.erp.server.tms.service.TmsCostDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 费用管理配置表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-15
 */
@Slf4j
@Service
public class TmsCfgCostServiceImpl extends SuperServiceImpl<TmsCfgCostMapper, TmsCfgCostEntity> implements TmsCfgCostService {

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private CfgReconciliationFieldService cfgReconciliationFieldService;

    @Resource
    private TmsCostDetailService tmsCostDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsCfgCostDTO.AddDTO addDTO) {
        TmsCfgCostEntity tmsCfgCostEntity = new TmsCfgCostEntity();
        BeanMapperUtils.copy(addDTO, tmsCfgCostEntity);

        //数据校验
        checkData(tmsCfgCostEntity);

        // 数据处理
        handleData(tmsCfgCostEntity);

        log.info("开始新增费用管理配置单");
        boolean save = super.save(tmsCfgCostEntity);
        if(!save) {
            throw new ServiceException("费用管理配置单保存失败");
        }
        //更新其他相同归属和分类的默认状态
        updateDefault(tmsCfgCostEntity);
        return new BaseResultDTO.AddDTO(tmsCfgCostEntity.getId(), tmsCfgCostEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsCfgCostDTO.UpdateDTO updateDTO) {
        TmsCfgCostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "费用管理配置单"));
        TmsCfgCostEntity tmsCfgCostEntity =  BeanMapperUtils.map(TmsCfgCostEntity.class, updateDTO);
        //数据校验
        checkData(tmsCfgCostEntity);
        // 数据处理
        handleData(tmsCfgCostEntity);
        log.info("编辑 开始修改费用管理配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsCfgCostEntity);
        if(!save) {
            throw new ServiceException("费用管理配置单保存失败");
        }
        //更新其他相同归属和分类的默认状态
        updateDefault(tmsCfgCostEntity);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<TmsCfgCostDTO.ListDTO> paging(PagingDTO<TmsCfgCostDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsCfgCostDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public TmsCfgCostDTO.ViewDTO view(String id) {
        TmsCfgCostEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到费用管理数据"));
        TmsCfgCostDTO.ViewDTO data = BeanMapperUtils.map(TmsCfgCostDTO.ViewDTO.class, entity);
        return data;
    }

    @Override
    public BatchResultDTO delete(String id) {
        TmsCfgCostEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到费用管理数据"));

        //校验对账字段是否引用
        List<CfgReconciliationFieldEntity> cfgReconciliationFieldList = cfgReconciliationFieldService.listByCfgCostIdList(Arrays.asList(id));
        if (CollUtil.isNotEmpty(cfgReconciliationFieldList)) {
            throw new ServiceException("费用已被对账单字段配置使用不支持删除");
        }
        //校验物流费用是否引用
        List<TmsCostDetailEntity> tmsCostDetailList = tmsCostDetailService.listByCfgCostIdList(Arrays.asList(id));
        if (CollUtil.isNotEmpty(tmsCostDetailList)) {
            throw new ServiceException("费用已被物流单使用不支持删除");
        }

        // 删除主单数据
        log.info("删除 开始删除费用管理数据，id：【{}】", id);
        this.removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }

    @Override
    public List<TmsCfgCostDTO.DropDownDTO> listDropDown(TmsCfgCostDTO.DropDownParamDTO dto) {
        List<TmsCfgCostEntity> list = lambdaQuery().eq(CharSequenceUtil.isNotBlank(dto.getDictCostAttribution()), TmsCfgCostEntity::getDictCostAttribution, dto.getDictCostAttribution())
                .list();
        if (CollectionUtil.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        return BeanMapperUtils.copyList(TmsCfgCostDTO.DropDownDTO.class,list);
    }

    /**
     * @description: 根据费用归属和分类查询
     * @author Will
     * @date: 2024/3/21 9:44
     * @param dictCostAttribution
     * @return TmsCfgCostEntity
     */
    @Override
    public List<TmsCfgCostEntity> listCostAttributionAndCategory(String dictCostAttribution ,String dictCostCategory) {
        return lambdaQuery().eq(TmsCfgCostEntity::getDictCostAttribution, dictCostAttribution)
                .eq(CharSequenceUtil.isNotBlank(dictCostCategory), TmsCfgCostEntity::getDictCostCategory, dictCostCategory)
                .orderByDesc(TmsCfgCostEntity::getIsDefault)
                .list();
    }

    @Override
    public List<TmsCfgCostEntity> listByCostNameList(List<String> costNameList) {
        if (CollectionUtil.isEmpty(costNameList)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(TmsCfgCostEntity::getCostName,costNameList)
                .list();
    }

    @Override
    public List<TmsCfgCostEntity> listByCostAttribution(String dictCostAttribution) {
        return lambdaQuery().eq(TmsCfgCostEntity::getDictCostAttribution,dictCostAttribution).list();
    }

    /**
     * 新增修改数据校验
     */
    private void checkData(TmsCfgCostEntity tmsCfgCostEntity) {
        TmsCfgCostEntity old = getByCostName(tmsCfgCostEntity);
        if (ObjUtil.isNotEmpty(old) && !CharSequenceUtil.equals(tmsCfgCostEntity.getId(),old.getId())) {
            throw new ServiceException(ApiError.ERROR_CFG_COST_EXIST,tmsCfgCostEntity.getCostName());
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(TmsCfgCostEntity tmsCfgCostEntity) {
    
    }

    /**
     * @description: 分页查询数据处理
     * @author Will
     * @date: 2024/3/21 9:52
     * @param list
     */
    private void fillList(List<TmsCfgCostDTO.ListDTO> list) {
       if (CollectionUtil.isEmpty(list)) {
           return;
       }
       //字典数据
        List<DictBasicEntity> basicList = dictBasicService.getByKeyList(Arrays.asList(DictBasicEnum.DICT_COST_CATEGORY.getType(), DictBasicEnum.DICT_COST_ATTRIBUTION.getType()));

        for (TmsCfgCostDTO.ListDTO listDTO : list) {

            //费用归属
            String dictCostAttributionName = basicList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), DictBasicEnum.DICT_COST_ATTRIBUTION.getType())
                    && CharSequenceUtil.equals(obj.getCode(), listDTO.getDictCostAttribution())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setDictCostAttributionName(dictCostAttributionName);
            //费用分类
            String dictCostCategoryName = basicList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), DictBasicEnum.DICT_COST_CATEGORY.getType())
                            && CharSequenceUtil.equals(obj.getCode(), listDTO.getDictCostCategory())).findFirst()
                    .flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setDictCostCategoryName(dictCostCategoryName);
        }
    }

    /**
     * @description: 更新默认数据
     * @author Will
     * @date: 2024/3/21 9:45
     * @param tmsCfgCostEntity
     */
    private void updateDefault (TmsCfgCostEntity tmsCfgCostEntity) {
        Boolean isDefault = tmsCfgCostEntity.getIsDefault();
        if (!isDefault) {
            return;
        }
        //查询已存在数据
        List<TmsCfgCostEntity> oldList = listCostAttributionAndCategory(tmsCfgCostEntity.getDictCostAttribution(),tmsCfgCostEntity.getDictCostCategory());
        List<TmsCfgCostEntity> tmsCfgCostEntityList = oldList.stream().filter(obj -> !CharSequenceUtil.equals(tmsCfgCostEntity.getId(), obj.getId()) && obj.getIsDefault())
                .collect(Collectors.toList());
        if (CollectionUtil.isEmpty(tmsCfgCostEntityList)) {
            return;
        }
        tmsCfgCostEntityList.forEach(obj -> obj.setIsDefault(Boolean.FALSE));
        this.updateBatchById(tmsCfgCostEntityList);
    }

    /**
     * @description: 根据名称查询
     * @author Will
     * @date: 2024/3/21 10:08
     * @param tmsCfgCostEntity
     * @return TmsCfgCostEntity
     */
    private TmsCfgCostEntity getByCostName(TmsCfgCostEntity tmsCfgCostEntity) {
        return lambdaQuery()
                .eq(TmsCfgCostEntity::getCostName,tmsCfgCostEntity.getCostName())
                .last(SqlConstants.LIMIT_1)
                .one();
    }
}

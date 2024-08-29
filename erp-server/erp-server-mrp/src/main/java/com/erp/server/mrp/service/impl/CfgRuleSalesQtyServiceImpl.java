package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgRuleSalesDenoisingDTO;
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import com.erp.model.mrp.dto.CfgRuleSalesQtyDTO;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingEntity;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaEntity;
import com.erp.model.mrp.entity.CfgRuleSalesQtyEntity;
import com.erp.model.mrp.enums.CfgRuleSalesFormulaTypeEnum;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleSalesQtyMapper;
import com.erp.server.mrp.service.CfgRuleSalesDenoisingService;
import com.erp.server.mrp.service.CfgRuleSalesFormulaService;
import com.erp.server.mrp.service.CfgRuleSalesQtyService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 销量（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleSalesQtyServiceImpl extends SuperServiceImpl<CfgRuleSalesQtyMapper, CfgRuleSalesQtyEntity> implements CfgRuleSalesQtyService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleSalesFormulaService cfgRuleSalesFormulaService;

    @Autowired
    private CfgRuleSalesDenoisingService cfgRuleSalesDenoisingService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchUpdate(CfgRuleSalesQtyDTO.UpdateDTO updateDTO) {
        Boolean isCfgSame = updateDTO.getIsCfgSame();

        //编辑常规品
        updateDTO.getConventionalDetail().setIsCfgSame(isCfgSame).setType(CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode());
        this.update(updateDTO.getConventionalDetail());

        //编辑新品
        updateDTO.getNewDetail().setIsCfgSame(isCfgSame).setType(CfgRuleStockingRatioTypeEnum.NEW.getCode());
        this.update(updateDTO.getNewDetail());
        return Boolean.TRUE;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleSalesQtyDTO.UpdateDetailDTO updateDetailDTO) {
        CfgRuleSalesQtyEntity cfgRuleSalesQtyEntity =  BeanMapperUtils.map(CfgRuleSalesQtyEntity.class, updateDetailDTO);
        //旧数据
        CfgRuleSalesQtyEntity old = super.getById(updateDetailDTO.getId());
        if (ObjectUtil.isNotEmpty(old)) {
            cfgRuleSalesQtyEntity.setId(old.getId());
        }

        // 数据处理
        handleData(cfgRuleSalesQtyEntity);

        boolean save = super.saveOrUpdate(cfgRuleSalesQtyEntity);
        if(!save) {
            throw new ServiceException("销量（规则设置）保存失败");
        }

        //销量信息调整
        List<CfgRuleSalesFormulaDTO.UpdateDTO> salesFormulaList = handleSalesFormula(updateDetailDTO);
        cfgRuleSalesFormulaService.update(salesFormulaList,cfgRuleSalesQtyEntity.getId());

        //销量去噪
        cfgRuleSalesDenoisingService.update(updateDetailDTO.getSalesDenoisingList(),cfgRuleSalesQtyEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录销量（规则设置）日志数据，id：【{}】", cfgRuleSalesQtyEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleSalesQtyEntity.getId(), "销量（规则设置）");
        operateLogService.addModuleOperateLogByObj(old, cfgRuleSalesQtyEntity, ModuleTypeEnum.CFG_RULE_COMMON.getCode(), cfgRuleSalesQtyEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public CfgRuleSalesQtyDTO.ViewDTO view(String platformType) {
        CfgRuleSalesQtyDTO.ViewDTO viewDTO = new CfgRuleSalesQtyDTO.ViewDTO();

        //销量信息
        List<CfgRuleSalesQtyEntity> list = this.getByPlatformType(platformType);
        if (CollectionUtils.isEmpty(list)) {
            return  viewDTO;
        }
        List<String> salesQtyIdList = list.stream().map(CfgRuleSalesQtyEntity::getId).collect(Collectors.toList());

        //日销量数据
        List<CfgRuleSalesFormulaEntity> salesFormulaList = cfgRuleSalesFormulaService.listBySalesQtyIdList(salesQtyIdList);

        //销量去噪
        List<CfgRuleSalesDenoisingEntity> salesDenoisingList = cfgRuleSalesDenoisingService.listBySalesQtyIdList(salesQtyIdList);

        for (CfgRuleSalesQtyEntity cfgRuleSalesQtyEntity : list) {
            CfgRuleSalesQtyDTO.ViewDetailDTO viewDetailDTO = new CfgRuleSalesQtyDTO.ViewDetailDTO();
            BeanMapperUtils.copy(cfgRuleSalesQtyEntity,viewDetailDTO);
            //默认日销量
            CfgRuleSalesFormulaEntity defaultSalesFormula = salesFormulaList.stream().filter(obj ->
                    StrUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode()) && StrUtil.equals(obj.getSalesQtyId(),cfgRuleSalesQtyEntity.getId())
            ).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(defaultSalesFormula)) {
                CfgRuleSalesFormulaDTO.ViewDTO defaultViewDTO = BeanMapperUtils.map(CfgRuleSalesFormulaDTO.ViewDTO.class, defaultSalesFormula);
                viewDetailDTO.setDefaultSalesQtyDTO(defaultViewDTO);
            }
            //动态日销量
            List<CfgRuleSalesFormulaEntity> dynamicSalesFormulaList = salesFormulaList.stream().filter(obj ->
                    StrUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode()) && StrUtil.equals(obj.getSalesQtyId(),cfgRuleSalesQtyEntity.getId())
            ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(dynamicSalesFormulaList)) {
                List<CfgRuleSalesFormulaDTO.ViewDTO> dynamicViewList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.ViewDTO.class, dynamicSalesFormulaList);
                viewDetailDTO.setDynamicSalesQtyList(dynamicViewList);
            }
            //固定日销量
            List<CfgRuleSalesFormulaEntity> fixedSalesFormulaList = salesFormulaList.stream().filter(obj ->
                    StrUtil.equals(obj.getType(), CfgRuleSalesFormulaTypeEnum.FIXED.getCode()) && StrUtil.equals(obj.getSalesQtyId(),cfgRuleSalesQtyEntity.getId())
            ).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(fixedSalesFormulaList)) {
                List<CfgRuleSalesFormulaDTO.ViewDTO> fixedViewList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.ViewDTO.class, fixedSalesFormulaList);
                viewDetailDTO.setFixedSalesQtyList(fixedViewList);
            }
            //去噪信息
            List<CfgRuleSalesDenoisingEntity> denoisingList = salesDenoisingList.stream().filter(obj -> StrUtil.equals(obj.getSalesQtyId(), cfgRuleSalesQtyEntity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(denoisingList)) {
                List<CfgRuleSalesDenoisingDTO.ViewDTO> salesDenoisingViewList = BeanMapperUtils.copyList(CfgRuleSalesDenoisingDTO.ViewDTO.class, denoisingList);
                viewDetailDTO.setSalesDenoisingList(salesDenoisingViewList);
            }
            //明细赋值
            if (StrUtil.equals(cfgRuleSalesQtyEntity.getType(),CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode())) {
                viewDTO.setConventionalDetail(viewDetailDTO);
            } else {
                viewDTO.setNewDetail(viewDetailDTO);
            }
        }
        viewDTO.setIsCfgSame(list.get(0).getIsCfgSame());
        return viewDTO;
    }

    @Override
    public void deleteByRefId(String refId) {

    }

    /**
     * 销量信息合并
     * @author will
     * @date 2024/8/27 9:49
     * @param updateDTO
     * @return List<UpdateDTO>
     */
    private List<CfgRuleSalesFormulaDTO.UpdateDTO> handleSalesFormula (CfgRuleSalesQtyDTO.UpdateDetailDTO updateDTO) {
        List<CfgRuleSalesFormulaDTO.UpdateDTO> list = new ArrayList<>();
        //默认日销量
        CfgRuleSalesFormulaDTO.UpdateDTO defaultDTO = BeanMapperUtils.map(CfgRuleSalesFormulaDTO.UpdateDTO.class, updateDTO.getDefaultSalesQtyDTO());
        defaultDTO.setType(CfgRuleSalesFormulaTypeEnum.DEFAULT.getCode()).setPriority(MathUtil.THREE);
        list.add(defaultDTO);
        //动态日销量
        if (CollectionUtils.isNotEmpty(updateDTO.getDynamicSalesQtyList())) {
            List<CfgRuleSalesFormulaDTO.UpdateDTO> dynamicList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.UpdateDTO.class, updateDTO.getDynamicSalesQtyList());
            dynamicList.forEach(obj -> obj.setType(CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode()).setPriority(MathUtil.TWO));
            list.addAll(dynamicList);
        }
        //固定日销量
        if (CollectionUtils.isNotEmpty(updateDTO.getFixedSalesQtyList())) {
            List<CfgRuleSalesFormulaDTO.UpdateDTO> fixedList = BeanMapperUtils.copyList(CfgRuleSalesFormulaDTO.UpdateDTO.class, updateDTO.getFixedSalesQtyList());
            fixedList.forEach(obj -> obj.setType(CfgRuleSalesFormulaTypeEnum.FIXED.getCode()).setPriority(MathUtil.ONE));
            list.addAll(fixedList);
        }
        return list;
    }

    /**
     * 根据平台类型查询
     * @author will
     * @date 2024/8/24 9:29
     * @param platformType
     * @return List<CfgRuleSalesQtyEntity>
     */
    private List<CfgRuleSalesQtyEntity> getByPlatformType (String platformType) {
       return lambdaQuery().eq(CfgRuleSalesQtyEntity::getPlatformType,platformType).list();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleSalesQtyEntity cfgRuleSalesQtyEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

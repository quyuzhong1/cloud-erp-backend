package com.erp.server.tms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.tms.dto.ShippingTemplateCostSettingDTO;
import com.erp.model.tms.entity.ShippingTemplateCostSettingEntity;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.ShippingSideEnum;
import com.erp.server.tms.mapper.ShippingTemplateOtherCostMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.ShippingTemplateOtherCostDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@Service
public class ShippingTemplateOtherCostServiceImpl extends SuperServiceImpl<ShippingTemplateOtherCostMapper, ShippingTemplateOtherCostEntity> implements ShippingTemplateOtherCostService {
    @Resource
    private OperateLogService operateLogService;



    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private ShippingTemplateCostSettingService shippingTemplateCostSettingService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(List<ShippingTemplateOtherCostDTO.AddDTO> otherCostList, String mainId) {
        if (CollectionUtils.isEmpty(otherCostList)) {
            return Boolean.TRUE;
        }
        List<ShippingTemplateOtherCostEntity> list = BeanMapperUtils.copyList(ShippingTemplateOtherCostEntity.class, otherCostList);
        //验证必填信息
        handleData(list,mainId);

        boolean save = this.saveBatch(list);
        if(!save) {
            throw new ServiceException("运费规则单保存失败");
        }
        //新增计算设置
        addOrUpdateCostSetting(list);
        return Boolean.TRUE;
    }


    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<ShippingTemplateOtherCostDTO.UpdateDTO> otherCostList, String mainId) {
        if (otherCostList == null) {
            otherCostList = new ArrayList<>();
        }
        List<ShippingTemplateOtherCostEntity> list = BeanMapperUtils.copyList(ShippingTemplateOtherCostEntity.class, otherCostList);
        //数据格式化
        handleData(list,mainId);
        //新增日志
        addOperateLog(list,mainId);
        //新增或修改
        this.saveOrUpdateBatch(list);
        //新增计算设置
        addOrUpdateCostSetting(list);
        return Boolean.TRUE;
    }



    @Override
    public List<ShippingTemplateOtherCostEntity> listByMainId(String mainId) {
        return  lambdaQuery()
                .eq(ShippingTemplateOtherCostEntity::getMainId,mainId)
                .orderByAsc(ShippingTemplateOtherCostEntity::getId)
                .list();
    }

    @Override
    public List<ShippingTemplateOtherCostEntity> listByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            return Collections.EMPTY_LIST;
        }
        return  lambdaQuery().in(ShippingTemplateOtherCostEntity::getMainId,mainIdList).list();
    }

    @Override
    public IPage<ShippingCalculationDTO.ListDTO> paging(Page query, ShippingCalculationDTO.PagingParamDTO params) {
        BigDecimal volume = MathUtil.multiplyWithTwo(MathUtil.multiplyWithTwo(params.getLength(), params.getWidth()), params.getHeight());
        params.setVolume(volume);
        return baseMapper.paging(query,params);
    }

    @Override
    public List<ShippingCalculationDTO.ListDTO> listByExportExcel(ShippingCalculationDTO.PagingParamDTO params) {
        BigDecimal volume = MathUtil.multiplyWithTwo(MathUtil.multiplyWithTwo(params.getLength(), params.getWidth()), params.getHeight());
        params.setVolume(volume);
        return baseMapper.listByExportExcel(params);
    }

    /**
     * 获取到所有相关费用的信息
     * @param params
     * @return
     */
    @Override
    public List<ShippingCalculationDTO.ListDTO> listRefCost(SoB2cDTO.ShippingCalculationDTO params) {
        return baseMapper.listRefCost(params);
    }

    @Override
    public void deleteByMainId(String mainId) {
        List<ShippingTemplateOtherCostEntity> list = this.listByMainId(mainId);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //删除计算设置
        List<String> ids = list.stream().map(ShippingTemplateOtherCostEntity::getId).collect(Collectors.toList());
        shippingTemplateCostSettingService.deleteByOtherCostIds(ids);
        //删除其他费用数据
        this.removeByIds(ids);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<ShippingTemplateOtherCostEntity> list,String mainId) {

        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicEnum.SHIPPING_TEMPLATE_COST.getType());

        for (ShippingTemplateOtherCostEntity otherCostEntity : list) {
            //主表id
            otherCostEntity.setMainId(mainId);
            //保存数据存json字符串
            otherCostEntity.setExtendJson(ObjectUtil.isEmpty(otherCostEntity.getExtendJsonDto()) ? "" : JSONUtil.toJsonStr(otherCostEntity.getExtendJsonDto()));
            //费用名称
            String name = dictList.stream().filter(obj -> obj.getCode().equals(otherCostEntity.getDictCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            otherCostEntity.setDictName(name);
            //费用设置值
            if (ObjectUtil.isEmpty(otherCostEntity.getCostSettingValue())) {
                otherCostEntity.setCostSettingValue(BigDecimal.ZERO);
            }
        }
    }

    /**
     * @description: 新增或修改计算设置
     * @author Will
     * @date: 2023/11/8 10:50
     * @param list
     */
    private void addOrUpdateCostSetting(List<ShippingTemplateOtherCostEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return ;
        }
        List<ShippingTemplateCostSettingDTO.AddDTO> addList = new ArrayList<>();
        for (ShippingTemplateOtherCostEntity otherCostEntity : list) {
            if (CollectionUtils.isEmpty(otherCostEntity.getSettingList())) {
                continue;
            }
            for (String value : otherCostEntity.getSettingList()) {
                ShippingTemplateCostSettingDTO.AddDTO addDTO = new ShippingTemplateCostSettingDTO.AddDTO();
                addDTO.setCode(value);
                addDTO.setOtherCostId(otherCostEntity.getId());
                addDTO.setCalculationMethod(otherCostEntity.getCalculationMethod());
                addList.add(addDTO);
            }
        }
        List<String> otherCostIdList = list.stream().map(ShippingTemplateOtherCostEntity::getId).collect(Collectors.toList());
        shippingTemplateCostSettingService.add(addList,otherCostIdList);
    }

    /**
     * @description: 添加操作日志
     * @author Will
     * @date: 2023/11/8 11:00
     * @param list
     * @param mainId
     */
    private void addOperateLog(List<ShippingTemplateOtherCostEntity> list,String mainId) {
        //原明细数据
        List<ShippingTemplateOtherCostEntity> oldList = this.listByMainId(mainId);
        List<String> costIdList = oldList.stream().map(ShippingTemplateOtherCostEntity::getId).collect(Collectors.toList());
        List<ShippingTemplateCostSettingEntity> settingList = shippingTemplateCostSettingService.listByOtherCostIds(costIdList);
        for (ShippingTemplateOtherCostEntity entity : list) {
            ShippingTemplateOtherCostEntity old = oldList.stream().filter(obj -> obj.getId().equals(entity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(old)) {
                throw new ServiceException(ApiError.ERROR_SHIPPING_OTHER_COST_NOT_EXIST);
            }
            List<String> settingCodeList = settingList.stream().filter(obj -> obj.getOtherCostId().equals(entity.getId())).map(ShippingTemplateCostSettingEntity::getCode).collect(Collectors.toList());
            old.setSettingList(settingCodeList);

            //数值设置
            String oldExtendJson = old.getExtendJson();
            String newExtendJson = JSONUtil.toJsonStr(entity.getExtendJsonDto());
            for (ShippingSideEnum shippingSideEnum : ShippingSideEnum.values()) {
                //原始数据格式化中文
                oldExtendJson = oldExtendJson.replace(shippingSideEnum.getCode(),shippingSideEnum.getName());
                //新数据格式化中文
                newExtendJson = newExtendJson.replace(shippingSideEnum.getCode(),shippingSideEnum.getName());
            }
            old.setExtendJsonLog(oldExtendJson);
            entity.setExtendJsonLog(newExtendJson);
            //操作日志
            operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(),mainId,"",String.format("【%s】",old.getDictName()));
        }
    }
}

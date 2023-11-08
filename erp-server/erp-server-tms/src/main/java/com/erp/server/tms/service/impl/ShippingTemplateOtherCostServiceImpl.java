package com.erp.server.tms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.ShippingRegionCityDTO;
import com.erp.model.tms.dto.ShippingTemplateCostSettingDTO;
import com.erp.model.tms.entity.ShippingTemplateOtherCostEntity;
import com.erp.model.tms.entity.ShippingTemplateRuleEntity;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.server.tms.mapper.ShippingTemplateOtherCostMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.ShippingTemplateOtherCostDTO;
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
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Resource
    private DictBasicService dictBasicService;

    @Autowired
    private ShippingTemplateCostSettingService shippingTemplateCostSettingService;

    @GlobalTransactional(rollbackFor = Exception.class)
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
        //新增城市分区
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
        return Boolean.TRUE;
    }
    
  

    @Override
    public List<ShippingTemplateOtherCostEntity> listByMainId(String mainId) {
        return  lambdaQuery().eq(ShippingTemplateOtherCostEntity::getMainId,mainId).list();
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
        for (ShippingTemplateOtherCostEntity entity : list) {
            ShippingTemplateOtherCostEntity old = oldList.stream().filter(obj -> obj.getId().equals(entity.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(old)) {
                throw new ServiceException(ApiError.ERROR_SHIPPING_TEMPLATE_OTHER_COST);
            }
            //操作日志
            operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(),mainId,"",String.format("【%s】",old.getDictName()));
        }
    }
}

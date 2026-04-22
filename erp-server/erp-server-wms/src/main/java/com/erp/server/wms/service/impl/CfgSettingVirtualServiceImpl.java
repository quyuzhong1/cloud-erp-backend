package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.dto.SpElExpressionDTO;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.CfgRuleOutDTO;
import com.erp.model.wms.dto.CfgSettingVirtualDTO;
import com.erp.model.wms.dto.CfgSettingVirtualValueDTO;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.entity.CfgRuleOutEntity;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.erp.model.wms.enums.CfgRuleOutEnum;
import com.erp.model.wms.enums.CfgSettingVirtualEnum;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.server.wms.mapper.CfgRuleOutMapper;
import com.erp.server.wms.service.CfgSettingService;
import com.erp.server.wms.service.CfgSettingVirtualService;
import com.erp.server.wms.service.DictBasicService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * 虚拟仓配置
 * @author will
 * @date 2024/9/23 16:09
 */
@Slf4j
@Service
public class CfgSettingVirtualServiceImpl  implements CfgSettingVirtualService {

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SpElServer spElServer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BaseResultDTO.AddDTO addVirtual(CfgSettingVirtualDTO.AddDTO addDTO) {
        // 数据处理
        List<CfgSettingEntity> cfgSettingList = handleData(addDTO);

        log.info("开始新增系统虚拟仓配置管理");
        boolean save = cfgSettingService.saveOrUpdateBatch(cfgSettingList);
        if(!save) {
            throw new ServiceException("系统虚拟仓配置管理保存失败");
        }
        return new BaseResultDTO.AddDTO("", "");
    }

    @Override
    public CfgSettingVirtualDTO.ViewDTO viewVirtual() {
        CfgSettingVirtualDTO.ViewDTO viewDTO = new CfgSettingVirtualDTO.ViewDTO();
        List<DictBasicDTO.ListDTO> dictList = dictBasicService.getByKey(DictBasicEnum.CFG_SETTING_VIRTUAL.getKey());
        if (CollectionUtils.isEmpty(dictList)) {
            return viewDTO;
        }
        //查询已有配置信息
        List<CfgSettingEntity> list = cfgSettingService.listCfgSetting();
        if (CollectionUtils.isEmpty(list)) {
            return viewDTO;
        }
        for (CfgSettingEntity cfgSetting : list) {
            handleViewEnum(cfgSetting,viewDTO);
        }
        return viewDTO;
    }

    @Override
    public CfgSettingVirtualDTO.MatchVirtualTransferResultDTO matchTransferRule(CfgSettingVirtualDTO.MatchVirtualTransferRuleDTO dto) {

        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("inWarehouseCode", dto.getInWarehouseCode());
        detailMap.put("outWarehouseCode", dto.getOutWarehouseCode());
        Map<String, Object> map = new HashMap<>();
        map.put("detailList", Collections.singletonList(detailMap));
        map.put("inWarehouseCode", dto.getInWarehouseCode());
        map.put("outWarehouseCode", dto.getOutWarehouseCode());

        CfgSettingVirtualDTO.ViewDTO viewDTO = this.viewVirtual();
        CfgSettingVirtualValueDTO.VirtualTransferSettingDTO virtualTransferSettingDTO = viewDTO.getVirtualTransferSettingDTO();
        if(Objects.isNull(virtualTransferSettingDTO)){
            return new CfgSettingVirtualDTO.MatchVirtualTransferResultDTO(false, "");
        }
        List<CfgSettingVirtualValueDTO.VirtualTransferConditionElement> virtualTransferConditionElements = virtualTransferSettingDTO.getConditionElementList();
        if(CollectionUtils.isEmpty(virtualTransferConditionElements)){
            return new CfgSettingVirtualDTO.MatchVirtualTransferResultDTO(false, "");
        }
        List<ConditionElement> conditionList = BeanMapper.copyList(virtualTransferConditionElements, ConditionElement.class);
        Boolean matchResult = spElServer.matchExpressionByConditionList(conditionList, map,"");

        if(matchResult){
            return new CfgSettingVirtualDTO.MatchVirtualTransferResultDTO(Boolean.TRUE,virtualTransferSettingDTO.getTransferDirection());
        }
        return new CfgSettingVirtualDTO.MatchVirtualTransferResultDTO(false, "");
    }


    /**
     * 数据处理
     * @author will
     * @date 2024/9/23 16:25
     * @param addDTO
     * @return List<CfgSettingEntity>
     */
    private List<CfgSettingEntity> handleData(CfgSettingVirtualDTO.AddDTO addDTO) {
        List<CfgSettingEntity> list = new ArrayList<>();
        List<DictBasicDTO.ListDTO> dictList = dictBasicService.getByKey(DictBasicEnum.CFG_SETTING_VIRTUAL.getKey());
        if (CollectionUtils.isEmpty(dictList)) {
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
        }
        //查询已有配置信息
        List<CfgSettingEntity> cfgSettingList = cfgSettingService.listCfgSetting();

        for (DictBasicDTO.ListDTO listDTO : dictList) {
            //添加数据
            CfgSettingEntity entity = handleAddEnum(listDTO, addDTO,cfgSettingList);
            if (Objects.nonNull(entity)){
                list.add(entity);
            }
        }
        return  list;
    }

    /**
     * 新增数据处理
     * @author will
     * @date 2024/9/23 16:25
     * @param listDTO
     * @param addDTO
     * @param cfgSettingList
     * @return CfgSettingEntity
     */
    private CfgSettingEntity handleAddEnum (DictBasicDTO.ListDTO listDTO,CfgSettingVirtualDTO.AddDTO addDTO,List<CfgSettingEntity> cfgSettingList) {
        CfgSettingEntity entity = new CfgSettingEntity();
        //系统配置json
        JSONObject jsonObject = new JSONObject();
        CfgSettingVirtualEnum cfgSettingEnum = CfgSettingVirtualEnum.getEnum(listDTO.getValue());
        if (Objects.isNull(cfgSettingEnum)){
            return null;
        }
        switch (cfgSettingEnum) {
            case SALES_DASHBOARD:
                jsonObject = ObjectUtil.isEmpty(addDTO.getSalesDashboardDTO()) ? null : JSONUtil.parseObj(addDTO.getSalesDashboardDTO());
                break;
            case REPORT_ORDER_DEMAND:
                jsonObject = ObjectUtil.isEmpty(addDTO.getReportOrderDemandDTO()) ? null : JSONUtil.parseObj(addDTO.getReportOrderDemandDTO());
                break;
            case VIRTUAL_RULE:
                jsonObject = ObjectUtil.isEmpty(addDTO.getVirtualRuleDTO()) ? null : JSONUtil.parseObj(addDTO.getVirtualRuleDTO());
                break;
            case VIRTUAL_TRANSFER:
                this.checkVirtualTransferRule(addDTO.getVirtualTransferSettingDTO());
                jsonObject = ObjectUtil.isEmpty(addDTO.getVirtualTransferSettingDTO()) ? null : JSONUtil.parseObj(addDTO.getVirtualTransferSettingDTO());
                break;
            default:
                break;
        }
        if (ObjectUtil.isEmpty(jsonObject)) {
            return null;
        }
        //查询是否是修改
        String id = cfgSettingList.stream().filter(obj -> CharSequenceUtil.equals(obj.getKey(),listDTO.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        entity.setId(id);
        entity.setIndex(listDTO.getSort());
        entity.setKey(listDTO.getValue());
        entity.setDataJson(jsonObject);
        return entity;
    }

    /**
     * 查看详情数据处理
     * @author will
     * @date 2024/9/23 16:24
     * @param cfgSetting
     * @param viewDTO
     */
    private void handleViewEnum (CfgSettingEntity cfgSetting,CfgSettingVirtualDTO.ViewDTO viewDTO) {
        //获取枚举
        CfgSettingVirtualEnum cfgSettingEnum = CfgSettingVirtualEnum.getEnum(cfgSetting.getKey());
        if (ObjectUtil.isEmpty(cfgSettingEnum)) {
            return;
        }
        switch (cfgSettingEnum) {
            case SALES_DASHBOARD:
                CfgSettingVirtualValueDTO.SalesDashboardDTO salesDashboardDTO = BeanUtil.toBean(cfgSetting.getDataJson(), CfgSettingVirtualValueDTO.SalesDashboardDTO.class);
                viewDTO.setSalesDashboardDTO(salesDashboardDTO);
                break;
            case REPORT_ORDER_DEMAND:
                CfgSettingVirtualValueDTO.ReportOrderDemandDTO reportOrderDemandDTO = BeanUtil.toBean(cfgSetting.getDataJson(), CfgSettingVirtualValueDTO.ReportOrderDemandDTO.class);
                viewDTO.setReportOrderDemandDTO(reportOrderDemandDTO);
                break;
            case VIRTUAL_RULE:
                CfgSettingVirtualValueDTO.VirtualRuleDTO virtualRuleDTO = BeanUtil.toBean(cfgSetting.getDataJson(), CfgSettingVirtualValueDTO.VirtualRuleDTO.class);
                viewDTO.setVirtualRuleDTO(virtualRuleDTO);
                break;
            case VIRTUAL_TRANSFER:
                CfgSettingVirtualValueDTO.VirtualTransferSettingDTO virtualTransferSettingDTO = BeanUtil.toBean(cfgSetting.getDataJson(), CfgSettingVirtualValueDTO.VirtualTransferSettingDTO.class);
                viewDTO.setVirtualTransferSettingDTO(virtualTransferSettingDTO);
                break;
            default:
                break;
        }
    }


    /**
     * 校验中转配置表达式是否合法
     */
    private void checkVirtualTransferRule(CfgSettingVirtualValueDTO.VirtualTransferSettingDTO virtualTransferSettingDTO) {
        if(Objects.isNull(virtualTransferSettingDTO)){
            return;
        }
        List<CfgSettingVirtualValueDTO.VirtualTransferConditionElement> conditionList = virtualTransferSettingDTO.getConditionElementList();
        if(CollUtil.isEmpty(conditionList)){
            return;
        }
        List<ConditionElement> conditionElementList = new ArrayList<>(conditionList.size());
        for (CfgSettingVirtualValueDTO.VirtualTransferConditionElement element : conditionList) {
            ConditionElement conditionElement = new ConditionElement();
            BeanMapper.copy(element, conditionElement);
            conditionElement.setValue(element.getValue());
            conditionElementList.add(conditionElement);
        }

        SpElExpressionDTO splElDTO = spElServer.getConditionExpression(conditionElementList, Map.class);
        String expression = splElDTO.getExpression();
        Boolean checkResult = spElServer.checkExpressionIsEnabled(expression);
        if (!checkResult) {
            throw new ServiceException(ApiError.COMMON_RULE_EXPRESSION_ERROR);
        }
    }
}

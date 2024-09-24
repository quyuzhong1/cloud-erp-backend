package com.erp.server.tms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.CfgSettingDTO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.model.tms.enums.CostAllocationEnum;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.WeightAllocationEnum;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.server.tms.mapper.CfgSettingMapper;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.DictBasicService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
/**
 * <p>
 * 系统配置管理 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-02-29
 */
@Slf4j
@Service
public class CfgSettingServiceImpl extends SuperServiceImpl<CfgSettingMapper, CfgSettingEntity> implements CfgSettingService {
    @Autowired
    private DictBasicService dictBasicService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgSettingDTO.AddDTO addDTO) {
        // 数据处理
        List<CfgSettingEntity> cfgSettingList = handleData(addDTO);

        log.info("开始新增系统配置管理");
        boolean save = super.saveOrUpdateBatch(cfgSettingList);
        if(!save) {
            throw new ServiceException("系统配置管理保存失败");
        }
        return new BaseResultDTO.AddDTO("", "");
    }

    @Override
    public CfgSettingDTO.ViewDTO view() {
        CfgSettingDTO.ViewDTO viewDTO = new CfgSettingDTO.ViewDTO();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicEnum.CFG_SETTING.getType());
        if (CollectionUtils.isEmpty(dictList)) {
            return viewDTO;
        }
        //查询已有配置信息
        List<CfgSettingEntity> list = listCfgSetting();
        if (CollectionUtils.isEmpty(list)) {
            return viewDTO;
        }
        for (CfgSettingEntity cfgSetting : list) {
            handleViewEnum(cfgSetting,viewDTO);
        }
        return viewDTO;
    }
    /**
     * @description: 格式化枚举信息
     * @author Will
     * @date: 2024/1/11 15:15
     * @param cfgSetting
     * @param viewDTO
     */
    private void handleViewEnum (CfgSettingEntity cfgSetting, CfgSettingDTO.ViewDTO viewDTO) {

        CfgSettingEnum cfgSettingEnum = CfgSettingEnum.getEnum(cfgSetting.getKey());
        switch (cfgSettingEnum) {
            case LOGISTICS_PRODUCT_DEST_DECLARE_PRICE:
                //无值时默认给null
                if (ObjectUtil.isEmpty(cfgSetting.getDataJson())) {
                    viewDTO.setLogisticsProductDestDeclarePrices(null);
                    break;
                }
                List<CfgSettingValueDTO.LogisticsProductDestDeclarePrice> prices = JSONUtil.toList(cfgSetting.getDataJson().getJSONArray("data"), CfgSettingValueDTO.LogisticsProductDestDeclarePrice.class);
                viewDTO.setLogisticsProductDestDeclarePrices(prices);
                break;
            case NOTIC:
                //无值时默认给null
                if (ObjectUtil.isEmpty(cfgSetting.getDataJson())) {
                    viewDTO.setNoticeDTO(null);
                    break;
                }
                CfgSettingValueDTO.NoticeDTO noticeDTO = JSONUtil.toBean(cfgSetting.getDataJson(),CfgSettingValueDTO.NoticeDTO.class);
                viewDTO.setNoticeDTO(noticeDTO);
                break;
            case RECONCILIATION_CYCLE:
                //无值时默认给null
                if (ObjectUtil.isEmpty(cfgSetting.getDataJson())) {
                    viewDTO.setReconciliationCycleDTO(null);
                    break;
                }
                CfgSettingValueDTO.ReconciliationCycleDTO reconciliationCycleDTO = JSONUtil.toBean(cfgSetting.getDataJson(),CfgSettingValueDTO.ReconciliationCycleDTO.class);
                viewDTO.setReconciliationCycleDTO(reconciliationCycleDTO);
                break;
            case BILL_AUTO_ADD:
                //无值时默认给null
                if (ObjectUtil.isEmpty(cfgSetting.getDataJson())) {
                    viewDTO.setBillAutoAddDTO(null);
                    break;
                }
                CfgSettingValueDTO.BillAutoAddDTO billAutoAddDTO = JSONUtil.toBean(cfgSetting.getDataJson(),CfgSettingValueDTO.BillAutoAddDTO.class);
                viewDTO.setBillAutoAddDTO(billAutoAddDTO);
                break;
            case ALLOCATION_SETTING:
                //无值时默认给null
                if (ObjectUtil.isEmpty(cfgSetting.getDataJson())) {
                    //默认按照原型展示默认值
                    viewDTO.setAllocationSettingDTO(getDefaultAllocationSetting());
                    break;
                }
                CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO = JSONUtil.toBean(cfgSetting.getDataJson(),CfgSettingValueDTO.AllocationSettingDTO.class);
                viewDTO.setAllocationSettingDTO(allocationSettingDTO);
                break;
            default:
                break;
        }
    }

    private CfgSettingValueDTO.AllocationSettingDTO getDefaultAllocationSetting() {
        CfgSettingValueDTO.AllocationSettingDTO dto = new CfgSettingValueDTO.AllocationSettingDTO();

        dto.setWeightFirstAllocation(WeightAllocationEnum.OUTSTOCK_CHARGED_WEIGHT.getCode());
        dto.setWeightPackageAllocation(WeightAllocationEnum.SUPPLIER_CHARGED_WEIGHT.getCode());

        dto.setFirstShippingCost(CostAllocationEnum.WEIGHT_ALLOCATION.getCode());
        dto.setFirstTariffFee(CostAllocationEnum.COST_ALLOCATION.getCode());
        dto.setFirstOtherTaxFee(CostAllocationEnum.COST_ALLOCATION.getCode());
        dto.setFirstOtherFee(CostAllocationEnum.WEIGHT_ALLOCATION.getCode());

        dto.setPackageShippingCost(CostAllocationEnum.WEIGHT_ALLOCATION.getCode());
        dto.setPackageTariffFee(CostAllocationEnum.COST_ALLOCATION.getCode());
        dto.setPackageOtherFee(CostAllocationEnum.WEIGHT_ALLOCATION.getCode());
        return dto;
    }

    @Override
    public CfgSettingEntity getByKey(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        CfgSettingEntity entity = baseMapper.getByKey(key);
        return entity;
    }

    /**
    * 新增修改处理数据
    */
    private List<CfgSettingEntity> handleData(CfgSettingDTO.AddDTO addDTO) {
        List<CfgSettingEntity> list = new ArrayList<>();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicEnum.CFG_SETTING.getType());
        if (CollectionUtils.isEmpty(dictList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //查询已有配置信息
        List<CfgSettingEntity> cfgSettingList = listCfgSetting();

        for (DictBasicDTO.ViewDTO listDTO : dictList) {
            //添加数据
            CfgSettingEntity entity = handleAddEnum(listDTO, addDTO,cfgSettingList);
            if (Objects.nonNull(entity)){
                list.add(entity);
            }
        }
        return  list;
    }
    /**
     * @description: 格式化枚举信息
     * @author zdy
     * @date: 2024/1/11 15:15
     * @param viewDTO
     * @param addDTO
     * @param cfgSettingList
     */
    private CfgSettingEntity handleAddEnum (DictBasicDTO.ViewDTO viewDTO, CfgSettingDTO.AddDTO addDTO, List<CfgSettingEntity> cfgSettingList) {
        CfgSettingEntity entity = new CfgSettingEntity();
        //系统配置json
        JSONObject jsonObject = new JSONObject();
        CfgSettingEnum cfgSettingEnum = CfgSettingEnum.getEnum(viewDTO.getCode());
        if (Objects.isNull(cfgSettingEnum)){
            return null;
        }
        switch (cfgSettingEnum) {
            case LOGISTICS_PRODUCT_DEST_DECLARE_PRICE:
                JSONArray jsonArray = JSONUtil.parseArray(addDTO.getLogisticsProductDestDeclarePrices());
                jsonObject.putOpt("data", jsonArray);
                break;
            case NOTIC:
                 jsonObject = JSONUtil.parseObj(addDTO.getNoticeDTO());
                break;
            case RECONCILIATION_CYCLE:
                //周期时清空日期
                if (ObjectUtil.isNotEmpty(addDTO.getReconciliationCycleDTO()) && ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(addDTO.getReconciliationCycleDTO().getDeclareReconciliationType())) {
                    addDTO.getReconciliationCycleDTO().setDeclareReconciliationDate(null);
                }
                //周期时清空日期
                if (ObjectUtil.isNotEmpty(addDTO.getReconciliationCycleDTO()) && ReconciliationTypeEnum.CREAT_BY_MONTH.getCode().equals(addDTO.getReconciliationCycleDTO().getFirstMileReconciliationType())) {
                    addDTO.getReconciliationCycleDTO().setFirstMileReconciliationDate(null);
                }
                jsonObject = JSONUtil.parseObj(addDTO.getReconciliationCycleDTO());
                break;
            case BILL_AUTO_ADD:
                jsonObject = JSONUtil.parseObj(addDTO.getBillAutoAddDTO());
                break;
            case ALLOCATION_SETTING:
                //无值时默认给null
                CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO = addDTO.getAllocationSettingDTO();
                if (ObjectUtil.isEmpty(allocationSettingDTO)) {
                    //默认按照原型展示默认值
                    allocationSettingDTO = getDefaultAllocationSetting();
                }
                jsonObject = JSONUtil.parseObj(allocationSettingDTO);
                break;
            default:
                break;
        }
        //查询是否是修改
        String id = cfgSettingList.stream().filter(obj -> StrUtil.equals(obj.getKey(),viewDTO.getCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        entity.setId(id);
        entity.setIndex(viewDTO.getIndex());
        entity.setKey(viewDTO.getCode());
        entity.setDataJson(jsonObject);
        return entity;
    }

    /**
     * @description: 查询未禁用配置
     * @author zdy
     * @date: 2024/1/11 15:57
     * @return List<CfgSettingEntity>
     */
    private List<CfgSettingEntity> listCfgSetting () {
        List<CfgSettingEntity> list = baseMapper.listCfgSetting();
        return list;
    }
}

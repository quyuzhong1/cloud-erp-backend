package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.SysAccountingCompanyDTO;
import com.erp.model.tms.dto.CfgSettingDTO;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.rpc.sys.feign.SysFeign;
import com.erp.server.tms.mapper.CfgSettingMapper;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.DictBasicService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private static final String DATA_KEY = "data";
    private static final Pattern CONTRACT_AGREEMENT_NO_PATTERN = Pattern.compile("^[A-Za-z]+$");

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SysFeign sysFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
    public  BaseResultDTO.AddDTO addByKey(CfgSettingDTO.AddByKeyDTO addDTO) {
        // 数据处理
        CfgSettingEnum cfgSettingEnum = CfgSettingEnum.getEnum(addDTO.getKey());
        if (Objects.isNull(cfgSettingEnum)){
            throw new ServiceException("系统配置类型不存在");
        }

        //系统配置json
        JSONObject jsonObject = new JSONObject();
        switch (cfgSettingEnum) {
            case BILL_AUTO_ADD:
                jsonObject = JSONUtil.parseObj(addDTO.getBillAutoAddDTO());
                break;
            case CONTRACT_AGREEMENT_NO:
                if (ObjectUtil.isEmpty(addDTO.getContractAgreementNoList())) {
                    addDTO.setContractAgreementNoList(Collections.emptyList());
                }
                handleContractAgreementNoList(addDTO.getContractAgreementNoList());
                JSONArray contractAgreementNoArray = JSONUtil.parseArray(addDTO.getContractAgreementNoList());
                jsonObject.putOpt(DATA_KEY, contractAgreementNoArray);
                break;
            default:
                throw new ServiceException("系统配置类型不正确");
        }
        //查询是否是修改
        CfgSettingEntity entity = getByKey(addDTO.getKey());
        if(Objects.isNull(entity)){
            entity = new CfgSettingEntity();
            entity.setKey(addDTO.getKey());
        }
        entity.setDataJson(jsonObject);
        log.info("开始新增系统配置管理");
        boolean save = super.saveOrUpdate(entity);
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

    @Override
    public CfgSettingDTO.ViewDTO getSetting(String key) {
        CfgSettingEnum cfgSettingEnum = checkCfgSettingKey(key);
        CfgSettingDTO.ViewDTO viewDTO = new CfgSettingDTO.ViewDTO();
        CfgSettingEntity cfgSetting = getByKey(cfgSettingEnum.getCode());
        if (Objects.isNull(cfgSetting)) {
            return viewDTO;
        }
        handleViewEnum(cfgSetting, viewDTO);
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
        if (Objects.isNull(cfgSettingEnum)) {
            return;
        }
        switch (cfgSettingEnum) {
            case LOGISTICS_PRODUCT_DEST_DECLARE_PRICE:
                //无值时默认给null
                if (ObjectUtil.isEmpty(cfgSetting.getDataJson())) {
                    viewDTO.setLogisticsProductDestDeclarePrices(null);
                    break;
                }
                List<CfgSettingValueDTO.LogisticsProductDestDeclarePrice> prices = JSONUtil.toList(cfgSetting.getDataJson().getJSONArray(DATA_KEY), CfgSettingValueDTO.LogisticsProductDestDeclarePrice.class);
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
            case CONTRACT_AGREEMENT_NO:
                if (ObjectUtil.isEmpty(cfgSetting.getDataJson()) || ObjectUtil.isEmpty(cfgSetting.getDataJson().getJSONArray(DATA_KEY))) {
                    viewDTO.setContractAgreementNoList(Collections.emptyList());
                    break;
                }
                List<CfgSettingValueDTO.ContractAgreementNoDTO> contractAgreementNoList = JSONUtil.toList(cfgSetting.getDataJson().getJSONArray(DATA_KEY), CfgSettingValueDTO.ContractAgreementNoDTO.class);
                contractAgreementNoList.sort(Comparator.comparing(CfgSettingValueDTO.ContractAgreementNoDTO::getIndex, Comparator.nullsLast(Comparator.naturalOrder())));
                viewDTO.setContractAgreementNoList(contractAgreementNoList);
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
        dto.setFirstOrgId(CostAllocationOrgTypeEnum.BILL_ORG.getCode());
        dto.setFirstWarehouseId(CharSequenceUtil.EMPTY);

        dto.setPackageShippingCost(CostAllocationEnum.WEIGHT_ALLOCATION.getCode());
        dto.setPackageTariffFee(CostAllocationEnum.COST_ALLOCATION.getCode());
        dto.setPackageOtherFee(CostAllocationEnum.WEIGHT_ALLOCATION.getCode());
        dto.setPackageOrgId(CostAllocationOrgTypeEnum.BILL_ORG.getCode());
        dto.setPackageWarehouseId(CharSequenceUtil.EMPTY);

        dto.setTransferTariffFee(CostAllocationEnum.COST_ALLOCATION.getCode());
        dto.setTransferOrgId(CharSequenceUtil.EMPTY);
        dto.setTransferWarehouseId(CharSequenceUtil.EMPTY);
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
            throw new ServiceException(ApiError.BILL_SELECTION_REQUIRED);
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
                jsonObject.putOpt(DATA_KEY, jsonArray);
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
                return null;
        }
        //查询是否是修改
        String id = cfgSettingList.stream().filter(obj -> CharSequenceUtil.equals(obj.getKey(),viewDTO.getCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        entity.setId(id);
        entity.setIndex(viewDTO.getIndex());
        entity.setKey(viewDTO.getCode());
        entity.setDataJson(jsonObject);
        return entity;
    }

    private CfgSettingEnum checkCfgSettingKey(String key) {
        if (CharSequenceUtil.isBlank(key)) {
            throw new ServiceException(ApiError.COMMON_CFG_SETTING_KEY, key);
        }
        CfgSettingEnum cfgSettingEnum = CfgSettingEnum.getEnum(key);
        if (Objects.isNull(cfgSettingEnum)) {
            throw new ServiceException(ApiError.COMMON_CFG_SETTING_KEY, key);
        }
        return cfgSettingEnum;
    }

    private void handleContractAgreementNoList(List<CfgSettingValueDTO.ContractAgreementNoDTO> contractAgreementNoList) {
        if (CollectionUtils.isEmpty(contractAgreementNoList)) {
            return;
        }
        Integer index = 0;
        Map<String, SysAccountingCompanyDTO.ListDTO> companyMap = listEnabledAccountingCompanyMap();
        Set<String> uniqueKeySet = new HashSet<>();
        for (CfgSettingValueDTO.ContractAgreementNoDTO dto : contractAgreementNoList) {

            if (Objects.isNull(dto)) {
                throw new ServiceException("合同协议号配置不能为空");
            }
            if (CharSequenceUtil.isBlank(dto.getCompanyId())) {
                throw new ServiceException("核算公司不能为空");
            }
            String contractAgreementNo = dto.getContractAgreementNo();
            if (Objects.nonNull(contractAgreementNo)) {
                contractAgreementNo = contractAgreementNo.trim();
                dto.setContractAgreementNo(contractAgreementNo);
            }
            if (CharSequenceUtil.isBlank(contractAgreementNo)) {
                throw new ServiceException("合同协议号不能为空");
            }
            if (!CONTRACT_AGREEMENT_NO_PATTERN.matcher(contractAgreementNo).matches()) {
                throw new ServiceException("合同协议号只能输入英文字母");
            }
            SysAccountingCompanyDTO.ListDTO company = companyMap.get(dto.getCompanyId());
            if (Objects.isNull(company)) {
                throw new ServiceException("核算公司不存在或已禁用");
            }
            dto.setCompanyName(company.getCompanyName());
            String uniqueKey = dto.getCompanyId() + "_" + contractAgreementNo;
            if (!uniqueKeySet.add(uniqueKey)) {
                throw new ServiceException("核算公司和合同协议号不可重复");
            }

            dto.setIndex(index++);
        }
    }

    private Map<String, SysAccountingCompanyDTO.ListDTO> listEnabledAccountingCompanyMap() {
        ApiResult<List<SysAccountingCompanyDTO.ListDTO>> companyResult = sysFeign.companyList("");
        if (Objects.isNull(companyResult) || !companyResult.isSuccess()) {
            throw new ServiceException("获取核算公司列表失败");
        }
        List<SysAccountingCompanyDTO.ListDTO> companyList = Optional.ofNullable(companyResult.getData()).orElse(Collections.emptyList());
        return companyList.stream()
                .filter(company -> Objects.nonNull(company) && CharSequenceUtil.isNotBlank(company.getId()) && !Boolean.TRUE.equals(company.getDisabled()))
                .collect(Collectors.toMap(SysAccountingCompanyDTO.ListDTO::getId, Function.identity(), (first, second) -> first));
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

    /**
     * @description: 查询未禁用配置
     * @author jack
     * @date: 2025-03-31
     * @return List<CfgSettingEntity>
     */
    @Override
    public List<CfgSettingEntity> listCfgSettingByKeys(List<String> keys) {
        return lambdaQuery().in(CfgSettingEntity::getKey, keys).eq(CfgSettingEntity::getDisabled, Boolean.FALSE).eq(CfgSettingEntity::getIsDeleted, Boolean.FALSE).list();
    }

    /**
     * @description: 查询费用分摊配置禁用配置
     * @author jack
     * @date: 2025-03-31
     * @return CfgSettingValueDTO.AllocationSettingDTO
     */
    @Override
    public CfgSettingValueDTO.AllocationSettingDTO getCfgSettingByAllocationSetting() {
        return view().getAllocationSettingDTO();
    }
}

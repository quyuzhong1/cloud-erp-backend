package com.erp.server.scm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.CfgSupplierSalesConditionDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.server.scm.mapper.CfgSupplierSalesMapper;
import com.erp.server.scm.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_CFG_SUPPLIER_SALES_REPORT;

/**
 * <p>
 * 销量设置 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-13
 */
@Slf4j
@Service
public class CfgSupplierSalesServiceImpl extends SuperServiceImpl<CfgSupplierSalesMapper, CfgSupplierSalesEntity> implements CfgSupplierSalesService {
    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private CfgSupplierSalesConditionService cfgSupplierSalesConditionService;

    @Resource
    private SupplierService supplierService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private Validator validator;
    @Resource
    private SupplierRefUserService supplierRefUserService;

    @Resource
    private SupplierPurchaseQuantityService supplierPurchaseQuantityService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgSupplierSalesDTO.CommonDTO addDTO) {
        String supplierId = addDTO.getSupplierId();

        Integer count = lambdaQuery()
                .eq(CfgSupplierSalesEntity::getSupplierId, supplierId)
                .eq(CfgSupplierSalesEntity::getIsDeleted, Boolean.FALSE)
                .count();
        if(count > 0){
            throw new ServiceException("供应商销量设置已存在，请勿重复新增");
        }

        handleData(addDTO);

        CfgSupplierSalesEntity cfgSupplierSalesEntity = new CfgSupplierSalesEntity();
        BeanMapperUtils.copy(addDTO, cfgSupplierSalesEntity);

        log.info("开始新增销量设置");
        boolean save = super.save(cfgSupplierSalesEntity);
        if(!save) {
            throw new ServiceException("销量设置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("新增-【{}】的配置信息", addDTO.getSupplierName());

        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), cfgSupplierSalesEntity.getId(), "新增操作");

        //处理配置条件
        saveCondition(addDTO, cfgSupplierSalesEntity.getId());

        return new BaseResultDTO.AddDTO(cfgSupplierSalesEntity.getId(), cfgSupplierSalesEntity.getId());
    }

    private void saveCondition(CfgSupplierSalesDTO.CommonDTO addDTO, String id) {
        //sku配置
        List<CfgSupplierSalesConditionDTO.ConditionDTO> skuList = addDTO.getSkuList();
        handleAddConditionList(skuList);
        if(CollUtil.isNotEmpty(skuList)){
            cfgSupplierSalesConditionService.saveRuleCondition(id, skuList, RuleTypeEnum.SKU.getCode());
        }else{
            throw new ServiceException("SKU查看配置不能为空");
        }

        //可销库存配置
        List<CfgSupplierSalesConditionDTO.ConditionDTO> saleableStockList = addDTO.getSaleableStockList();
        handleAddConditionList(saleableStockList);
        if(CollUtil.isNotEmpty(saleableStockList)){
            if(Objects.equals(addDTO.getWarehouseType(),CfgSupplierSalesConditionWarehouseTypeEnum.PHYSICALWAREHOUSE.getCode())){
                cfgSupplierSalesConditionService.saveRuleCondition(id, saleableStockList, RuleTypeEnum.PHYSICALWAREHOUSE.getCode());
            }
            if(Objects.equals(addDTO.getWarehouseType(),CfgSupplierSalesConditionWarehouseTypeEnum.VIRTUALWAREHOUSE.getCode())){
                cfgSupplierSalesConditionService.saveRuleCondition(id, saleableStockList, RuleTypeEnum.VIRTUALWAREHOUSE.getCode());
            }
        }

        //销量统计配置
        List<CfgSupplierSalesConditionDTO.ConditionDTO> salesStatisticList = addDTO.getSalesStatisticList();
        handleAddConditionList(salesStatisticList);
        if(CollUtil.isNotEmpty(salesStatisticList)){
            cfgSupplierSalesConditionService.saveRuleCondition(id, salesStatisticList, RuleTypeEnum.SALESSTATISTIC.getCode());
        }

        //通知配置
        List<CfgSupplierSalesConditionDTO.ConditionDTO> noticeList = addDTO.getNoticeList();
        handleAddConditionList(noticeList);
        if(CollUtil.isNotEmpty(noticeList)){
            cfgSupplierSalesConditionService.saveRuleCondition(id, noticeList, RuleTypeEnum.NOTICE.getCode());
        }

        if(Objects.nonNull(addDTO.getBlackCondition())){
            cfgSupplierSalesConditionService.saveRuleCondition(id, Arrays.asList(addDTO.getBlackCondition()), RuleTypeEnum.BLACK.getCode());
        }
    }


    private void handleAddConditionList(List<CfgSupplierSalesConditionDTO.ConditionDTO> conditionList) {
        if(CollUtil.isEmpty(conditionList)){
            return ;
        }
        CfgSupplierSalesConditionDTO.ConditionDTO conditionDTO = conditionList.stream().filter(e -> StringUtils.isBlank(e.getField()) && StringUtils.isBlank(e.getCompare())).findFirst().orElse(null);
        if(Objects.nonNull(conditionDTO)){
            conditionList.remove(conditionDTO);
        }
        if(CollUtil.isNotEmpty(conditionList)){
            for (CfgSupplierSalesConditionDTO.ConditionDTO condition : conditionList) {
                Set<ConstraintViolation<CfgSupplierSalesConditionDTO.ConditionDTO>> violations = validator.validate(condition);
                if (!violations.isEmpty()) {
                    ConstraintViolation<CfgSupplierSalesConditionDTO.ConditionDTO> firstViolation = violations.iterator().next();
                    String message = firstViolation.getMessage();
                    throw new ServiceException(message); // 或者自定义异常处理
                }
            }
        }
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgSupplierSalesDTO.CommonDTO addOrUpdateDTO) {
        CfgSupplierSalesEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销量设置"));

        Integer count = lambdaQuery()
                .eq(CfgSupplierSalesEntity::getSupplierId, addOrUpdateDTO.getSupplierId())
                .eq(CfgSupplierSalesEntity::getIsDeleted, Boolean.FALSE)
                .ne(CfgSupplierSalesEntity::getId,addOrUpdateDTO.getId())
                .count();
        if(count > 0){
            throw new ServiceException("供应商销量设置已存在，请勿重复新增");
        }

        // 数据处理
        handleData(addOrUpdateDTO);

        CfgSupplierSalesEntity cfgSupplierSalesEntity =  BeanMapperUtils.map(CfgSupplierSalesEntity.class, addOrUpdateDTO);

        log.info("编辑 开始修改销量设置数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgSupplierSalesEntity);
        if(!save) {
            throw new ServiceException("销量设置保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录销量设置日志数据，id：【{}】", cfgSupplierSalesEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑了【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), "销量设置");
        moduleOperateLogService.addModuleOperateLogByObj(old, cfgSupplierSalesEntity, ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), cfgSupplierSalesEntity.getId(),"", msg);

        //处理配置条件
        updateCondition(addOrUpdateDTO, cfgSupplierSalesEntity.getId());
        return Boolean.TRUE;
    }


    private void updateCondition(CfgSupplierSalesDTO.CommonDTO addDTO, String id) {
        // 参数校验
        if (addDTO == null) {
            throw new ServiceException("参数不能为空");
        }

        // SKU配置
        List<CfgSupplierSalesConditionDTO.ConditionDTO> skuList = addDTO.getSkuList();
        handleUpdateConditionList(skuList);
        if (CollUtil.isNotEmpty(skuList)) {
            cfgSupplierSalesConditionService.updateRuleCondition(id, skuList, ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), RuleTypeEnum.SKU.getCode());
        } else {
            throw new ServiceException("SKU查看配置不能为空");
        }


        if(Objects.equals(addDTO.getWarehouseType(),CfgSupplierSalesConditionWarehouseTypeEnum.VIRTUALWAREHOUSE.getCode())){
            // 可销库存配置--虚拟仓
            processAndSaveCondition(addDTO.getSaleableStockList(), id, RuleTypeEnum.VIRTUALWAREHOUSE.getCode(), "删除了可销库存配置虚拟仓条件");

            deleteExistingCondition(id, RuleTypeEnum.PHYSICALWAREHOUSE.getCode(), "删除了可销库存配置实体仓条件");
        }else {
            // 可销库存配置--实体仓
            processAndSaveCondition(addDTO.getSaleableStockList(), id, RuleTypeEnum.PHYSICALWAREHOUSE.getCode(), "删除了可销库存配置实体仓条件");

            deleteExistingCondition(id, RuleTypeEnum.VIRTUALWAREHOUSE.getCode(), "删除了可销库存配置虚拟仓条件");
        }

        // 销量统计配置
        processAndSaveCondition(addDTO.getSalesStatisticList(), id, RuleTypeEnum.SALESSTATISTIC.getCode(), "删除了销量统计配置条件");

        // 通知配置
        processAndSaveCondition(addDTO.getNoticeList(), id, RuleTypeEnum.NOTICE.getCode(), "删除了通知配置条件");

        // 黑名单配置
        if (Objects.nonNull(addDTO.getBlackCondition())) {
            cfgSupplierSalesConditionService.updateRuleCondition(id, Arrays.asList(addDTO.getBlackCondition()), ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), RuleTypeEnum.BLACK.getCode());
        } else {
            deleteExistingCondition(id, RuleTypeEnum.BLACK.getCode(), "删除了黑名单条件");
        }
    }

    // 封装通用处理逻辑
    private void processAndSaveCondition(List<CfgSupplierSalesConditionDTO.ConditionDTO> list, String id, String ruleType, String logDesc) {
        handleUpdateConditionList(list);
        if (CollUtil.isNotEmpty(list)) {
            cfgSupplierSalesConditionService.updateRuleCondition(id, list, ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), ruleType);
        } else {
            deleteExistingCondition(id, ruleType, logDesc);
        }
    }

    // 删除单个类型条件
    private void deleteExistingCondition(String id, String sourceType, String logDesc) {
        Integer count = cfgSupplierSalesConditionService.lambdaQuery()
                .eq(CfgSupplierSalesConditionEntity::getSalesSettingId, id)
                .eq(CfgSupplierSalesConditionEntity::getSourceType, sourceType)
                .eq(CfgSupplierSalesConditionEntity::getIsDeleted, false)
                .count();
        if (count > 0) {
            cfgSupplierSalesConditionService.lambdaUpdate()
                    .eq(CfgSupplierSalesConditionEntity::getSalesSettingId, id)
                    .eq(CfgSupplierSalesConditionEntity::getSourceType, sourceType)
                    .set(CfgSupplierSalesConditionEntity::getIsDeleted, true)
                    .update();
            moduleOperateLogService.addModuleOperateLog(logDesc, ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), id, "编辑操作");
        }
    }

    // 删除多个类型条件
    private void deleteExistingConditions(String id, List<String> sourceTypes, String logDesc) {
        Integer count = cfgSupplierSalesConditionService.lambdaQuery()
                .eq(CfgSupplierSalesConditionEntity::getSalesSettingId, id)
                .in(CfgSupplierSalesConditionEntity::getSourceType, sourceTypes)
                .eq(CfgSupplierSalesConditionEntity::getIsDeleted, false)
                .count();
        if (count > 0) {
            cfgSupplierSalesConditionService.lambdaUpdate()
                    .eq(CfgSupplierSalesConditionEntity::getSalesSettingId, id)
                    .in(CfgSupplierSalesConditionEntity::getSourceType, sourceTypes)
                    .set(CfgSupplierSalesConditionEntity::getIsDeleted, true)
                    .update();
            moduleOperateLogService.addModuleOperateLog(logDesc, ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), id, "编辑操作");
        }
    }


    private void handleUpdateConditionList(List<CfgSupplierSalesConditionDTO.ConditionDTO> conditionList) {
        if(CollUtil.isEmpty(conditionList)){
            return ;
        }

        CfgSupplierSalesConditionDTO.ConditionDTO conditionDTO = conditionList.stream().filter(e ->StringUtils.isBlank(e.getId()) && StringUtils.isBlank(e.getField()) && StringUtils.isBlank(e.getCompare())).findFirst().orElse(null);
        if(Objects.nonNull(conditionDTO)){
            conditionList.remove(conditionDTO);
        }

        if(conditionList.size() == 1 ){ //表示只有一条，需要判断是需要删除
            conditionDTO = conditionList.stream().filter(e ->StringUtils.isNotBlank(e.getId()) && StringUtils.isBlank(e.getField()) && StringUtils.isBlank(e.getCompare())).findFirst().orElse(null);
            if(Objects.nonNull(conditionDTO)){
                conditionList.remove(conditionDTO);
            }
        }

        if(CollUtil.isNotEmpty(conditionList)){
            for (CfgSupplierSalesConditionDTO.ConditionDTO condition : conditionList) {
                Set<ConstraintViolation<CfgSupplierSalesConditionDTO.ConditionDTO>> violations = validator.validate(condition);
                if (!violations.isEmpty()) {
                    ConstraintViolation<CfgSupplierSalesConditionDTO.ConditionDTO> firstViolation = violations.iterator().next();
                    String message = firstViolation.getMessage();
                    throw new ServiceException(message); // 或者自定义异常处理
                }
            }
        }
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(CfgSupplierSalesDTO.CommonDTO dto) {
        //供应商名称
        SupplierEntity supplierEntity = supplierService.getByIdOpt(dto.getSupplierId()).orElseThrow(() -> new ServiceException("未找到供应商数据"));
        dto.setSupplierCode(supplierEntity.getCode());
        dto.setSupplierName(supplierEntity.getName());

        //处理销量比列
        String salesRatioType = dto.getSalesRatioType();
        if(Objects.equals(salesRatioType,CfgSupplierSalesSalesRatioTypeEnum.PURCHASERATIO.getCode())){

        }else if(Objects.equals(salesRatioType,CfgSupplierSalesSalesRatioTypeEnum.SALESSTATISTICRATIO.getCode())){
            if(Objects.isNull(dto.getSalesRatio()) ){
                throw new ServiceException("请填写销量比例");
            }
            if(dto.getSalesRatio().compareTo(BigDecimal.ZERO)<=0 ){
                throw new ServiceException("请填写销量比例大于0");
            }
            if(dto.getSalesRatio().compareTo(new BigDecimal(100))>0){
                throw new ServiceException("请填写销量比例范围小于等于100");
            }
        }else {
            throw new ServiceException("请选择正确的销量比例类型");
        }

        //字段显示
        dto.setDisplayField(String.join(",", dto.getDisplayFieldList()));


        //黑名单列表
        if(Boolean.TRUE.equals(dto.getIsBlack())){
            if(CollUtil.isEmpty(dto.getBlackList())){
                throw new ServiceException("勾选黑名单SKU，则黑名单sku列表不能为空");
            }
            CfgSupplierSalesConditionDTO.ConditionDTO black = new CfgSupplierSalesConditionDTO.ConditionDTO();
            black.setField("skuNo");
            black.setCompare("inList");
            black.setLogic("");
            black.setSalesSettingId("");
            black.setValueType("String");
            black.setValue(String.join(",",  dto.getBlackList()));
            dto.setBlackCondition(black);
        }

        List<CfgSupplierSalesConditionDTO.ConditionDTO> noticeList = dto.getNoticeList();
        handleUpdateConditionList(noticeList);
        //通知配置校验
        if(Boolean.TRUE.equals(dto.getNoticeEnabled())){
            if(CollUtil.isEmpty(noticeList)){
                throw new ServiceException("勾选通知配置，则通知配置规则条件不能为空");
            }
        }
    }



    @Override
    public CfgSupplierSalesDTO.ViewDTO view(String id) {
        CfgSupplierSalesEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到销量设置数据"));
        // 数据填充处理
        return fillOne(entity);

    }

    private CfgSupplierSalesDTO.ViewDTO fillOne(CfgSupplierSalesEntity entity) {
        CfgSupplierSalesDTO.ViewDTO data = new CfgSupplierSalesDTO.ViewDTO();
        BeanMapper.copy(entity,data);
        data.setPermissionName(CfgSupplierSalesPermissionEnum.getName(data.getPermission()));

        data.setDailySalesTypeName(CfgSupplierSalesDailySalesTypeEnum.getName(data.getDailySalesType()));

        data.setSalesRatioTypeName(CfgSupplierSalesSalesRatioTypeEnum.getName(data.getSalesRatioType()));

        data.setDimensionName(CfgSupplierSalesDimensionEnum.getName(data.getDimension()));

        data.setWarehouseTypeName(CfgSupplierSalesConditionWarehouseTypeEnum.getName(data.getWarehouseType()));

        //字段显示
        if(StringUtils.isNotBlank(entity.getDisplayField())){
            List<String> displayFieldList = Arrays.asList(entity.getDisplayField().split(","));
            data.setDisplayFieldList(displayFieldList);

            List<String> displayFieldNameList = Optional.ofNullable(displayFieldList)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(CfgSupplierSalesDisplayFieldEnum::getName)
                    .collect(Collectors.toList());
            data.setDisplayFieldNameList(displayFieldNameList);
        }

        //查询配置
        List<CfgSupplierSalesConditionEntity> list = cfgSupplierSalesConditionService.list(Wrappers.<CfgSupplierSalesConditionEntity>lambdaQuery()
                .eq(CfgSupplierSalesConditionEntity::getSalesSettingId, entity.getId()));

        Map<String, List<CfgSupplierSalesConditionEntity>> map = list.stream().collect(Collectors.groupingBy(CfgSupplierSalesConditionEntity::getSourceType));

        //仓库配置
        String warehouseType = entity.getWarehouseType();
        if(map.containsKey(warehouseType)){
            List<CfgSupplierSalesConditionEntity> value = map.get(warehouseType);
            List<CfgSupplierSalesConditionDTO.View> conditionList = value.stream()
                    .sorted(Comparator.comparingInt(CfgSupplierSalesConditionEntity::getIndex))
                    .map(e -> BeanMapperUtils.map(CfgSupplierSalesConditionDTO.View.class, e))
                    .collect(Collectors.toList());
            data.setSaleableStockList(conditionList);
        }else{
            data.setSaleableStockList(Arrays.asList(new CfgSupplierSalesConditionDTO.View()));
        }

        RuleTypeEnum[] values = RuleTypeEnum.values();
        for (RuleTypeEnum ruleTypeEnum : values) {
            String key = ruleTypeEnum.getCode();
            if(map.containsKey(key)){
                List<CfgSupplierSalesConditionEntity> value = map.get(key);

                List<CfgSupplierSalesConditionDTO.View> conditionList = value.stream()
                        .sorted(Comparator.comparingInt(CfgSupplierSalesConditionEntity::getIndex))
                        .map(e -> BeanMapperUtils.map(CfgSupplierSalesConditionDTO.View.class, e))
                        .collect(Collectors.toList());

                if(Objects.equals(key,RuleTypeEnum.SKU.getCode())){
                    data.setSkuList(conditionList);
                }
                if(Objects.equals(key,RuleTypeEnum.SALESSTATISTIC.getCode())){
                    data.setSalesStatisticList(conditionList);
                }
                if(Objects.equals(key,RuleTypeEnum.NOTICE.getCode())){
                    data.setNoticeList(conditionList);
                }
                if(Objects.equals(key,RuleTypeEnum.BLACK.getCode())){
                    data.setIsBlack(Boolean.TRUE);

                    String sku = conditionList.get(0).getValue();
                    List<String> skuNoList = Arrays.asList(sku.split(","));
                    List<SkuVO> productDetailEntities = plmTaskFeign.listBySkuNoList(skuNoList);
                    List<String> blackList = productDetailEntities.stream().map(SkuVO::getSkuNo).collect(Collectors.toList());
                    List<String> blackNameList = productDetailEntities.stream().map(SkuVO::getSkuName).collect(Collectors.toList());
                    data.setBlackList(blackList);
                    data.setBlackNameList(blackNameList);
                }
            }else {
                List<CfgSupplierSalesConditionDTO.View> conditionList = Arrays.asList(new CfgSupplierSalesConditionDTO.View());
                if(Objects.equals(key,RuleTypeEnum.SKU.getCode())){
                    data.setSkuList(conditionList);
                }
                if(Objects.equals(key,RuleTypeEnum.SALESSTATISTIC.getCode())){
                    data.setSalesStatisticList(conditionList);
                }
                if(Objects.equals(key,RuleTypeEnum.NOTICE.getCode())){
                    data.setNoticeList(conditionList);
                }
            }
        }
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        CfgSupplierSalesEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到销量设置数据"));
        // 删除主单数据
        super.removeById(id);
        // 删除子表
        cfgSupplierSalesConditionService.lambdaUpdate()
                .set(CfgSupplierSalesConditionEntity::getIsDeleted, Boolean.TRUE)
                .eq(CfgSupplierSalesConditionEntity::getSalesSettingId, id)
                .update();
        // 删除日志数据
        String msg = StrUtil.format("用户【{}】操作【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), "销量设置");
        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), entity.getId(), "销量设置删除");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO enable(String id, Boolean disabled) {
        CfgSupplierSalesEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到销量设置数据"));
        if(!entity.getDisabled().equals(disabled)){
            lambdaUpdate()
                    .set(CfgSupplierSalesEntity::getDisabled, disabled)
                    .eq(CfgSupplierSalesEntity::getId, id)
                    .update();
            // 日志
            String msg = StrUtil.format("启用状态由【{}】改为【{}】 ", Objects.equals(entity.getDisabled(), Boolean.FALSE) ? "启用" : "停用",Objects.equals(disabled, Boolean.FALSE) ? "启用" : "停用");
            moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), entity.getId(), "更新销量设置");
        }
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }


    @Override
    public PagingVO<CfgSupplierSalesDTO.ListDTO> paging(PagingDTO<CfgSupplierSalesDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<CfgSupplierSalesDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<CfgSupplierSalesDTO.ListDTO> records) {
        for (CfgSupplierSalesDTO.ListDTO record : records) {


            record.setPermissionName(CfgSupplierSalesPermissionEnum.getName(record.getPermission()));

            record.setDailySalesTypeName(CfgSupplierSalesDailySalesTypeEnum.getName(record.getDailySalesType()));

            record.setSalesRatioTypeName(CfgSupplierSalesSalesRatioTypeEnum.getName(record.getSalesRatioType()));

            record.setNoticeEnabledName(Boolean.TRUE.equals(record.getNoticeEnabled()) ? "是" : "否");

            record.setDimensionName(CfgSupplierSalesDimensionEnum.getName(record.getDimension()));

            record.setDisabledName(Boolean.TRUE.equals(record.getDisabled()) ? "禁用" : "启用");

            if(Objects.equals(record.getSalesRatioType(), CfgSupplierSalesSalesRatioTypeEnum.SALESSTATISTICRATIO.getCode())){
                record.setSalesRatioStr(CfgSupplierSalesSalesRatioTypeEnum.SALESSTATISTICRATIO.getName()+record.getSalesRatio().setScale(2).toString() + "%");
            }else {
                record.setSalesRatioStr(CfgSupplierSalesSalesRatioTypeEnum.PURCHASERATIO.getName());
            }
        }
    }

    @Override
    public void exportList(CfgSupplierSalesDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("销量设置导出", EXPORT_SCM_CFG_SUPPLIER_SALES_REPORT.getCode(), pagingParamDTO);
    }

    //查询所有的启动未删除的销量设置
    @Override
    public List<CfgSupplierSalesDTO.ListAllDTO> listAll() {
        List<CfgSupplierSalesEntity> list = lambdaQuery()
                .eq(CfgSupplierSalesEntity::getIsDeleted,Boolean.FALSE)
                .list();
        if(CollUtil.isEmpty(list)){
            return Collections.emptyList();
        }

        List<String> ids = list.stream().map(CfgSupplierSalesEntity::getId).collect(Collectors.toList());

        List<CfgSupplierSalesDTO.ListAllDTO> datas = BeanMapper.copyList(list, CfgSupplierSalesDTO.ListAllDTO.class);

        //查询配置
        List<CfgSupplierSalesConditionEntity> conditionList = cfgSupplierSalesConditionService.list(Wrappers.<CfgSupplierSalesConditionEntity>lambdaQuery()
                .in(CfgSupplierSalesConditionEntity::getSalesSettingId, ids));

        Map<String, List<CfgSupplierSalesConditionEntity>> conditionMap = conditionList.stream().collect(Collectors.groupingBy(CfgSupplierSalesConditionEntity::getSalesSettingId));
        for (CfgSupplierSalesDTO.ListAllDTO data : datas) {

            List<CfgSupplierSalesConditionEntity> salesConditionEntities = conditionMap.get(data.getId());
            if(CollUtil.isNotEmpty(salesConditionEntities)){
                Map<String, List<CfgSupplierSalesConditionEntity>> map = salesConditionEntities.stream().collect(Collectors.groupingBy(CfgSupplierSalesConditionEntity::getSourceType));

                //仓库配置
                String warehouseType = data.getWarehouseType();
                if(map.containsKey(warehouseType)){
                    data.setSaleableStockList(map.get(warehouseType));
                }

                //其余配置（包括sku黑名单）
                RuleTypeEnum[] values = RuleTypeEnum.values();
                for (RuleTypeEnum ruleTypeEnum : values) {
                    String key = ruleTypeEnum.getCode();
                    if(map.containsKey(key)){
                        List<CfgSupplierSalesConditionEntity> value = map.get(key);

                        if(Objects.equals(key,RuleTypeEnum.SKU.getCode())){
                            data.setSkuList(value);
                        }
                        if(Objects.equals(key,RuleTypeEnum.SALESSTATISTIC.getCode())){
                            data.setSalesStatisticList(value);
                        }
                        if(Objects.equals(key,RuleTypeEnum.NOTICE.getCode())){
                            data.setNoticeList(value);
                        }
                        if(Objects.equals(key,RuleTypeEnum.BLACK.getCode())){
                            data.setIsBlack(Boolean.TRUE);

                            data.setBlackList(value);
                        }
                    }
                }
            }
        }
        return datas;
    }

    @Override
    public List<String> getDisplayField() {
        LoginUser login = UserContext.getDefaultLoginUser();

        String uid = login.getUid();

        SupplierRefUserEntity supplierRefUser = supplierRefUserService.lambdaQuery()
                .eq(SupplierRefUserEntity::getUid, uid)
                .eq(SupplierRefUserEntity::getDisabled, Boolean.FALSE)
                .eq(SupplierRefUserEntity::getIsDeleted, Boolean.FALSE)
                .last(" limit 1 ")
                .one();

        if(Objects.nonNull(supplierRefUser) && StringUtils.isNotBlank(supplierRefUser.getSupplierId())){
            CfgSupplierSalesEntity cfgSupplierSalesEntity = lambdaQuery().eq(CfgSupplierSalesEntity::getSupplierId, supplierRefUser.getSupplierId()).last(" limit 1").one();
            if(Objects.nonNull(cfgSupplierSalesEntity)){
                List<CfgSupplierSalesDisplayFieldEnum> list = Arrays.asList(CfgSupplierSalesDisplayFieldEnum.values());

                List<String> allFieldList = list.stream().map(CfgSupplierSalesDisplayFieldEnum::getCode).collect(Collectors.toList());

                List<String> existFieldList = Arrays.asList(cfgSupplierSalesEntity.getDisplayField().split(","));

                // 差集 = allFieldList - existFieldList
                List<String> diffFieldList = allFieldList.stream()
                        .filter(field -> !existFieldList.contains(field))
                        .collect(Collectors.toList());

                if(diffFieldList.contains(CfgSupplierSalesDisplayFieldEnum.SALE_STATE.getCode())){
                    diffFieldList.add("saleStateName");
                }
                return diffFieldList;
            }
        }

        return Collections.emptyList();
    }
}

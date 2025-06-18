package com.erp.server.scm.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.CfgSupplierSalesConditionDTO;
import com.erp.model.scm.entity.CfgSupplierSalesConditionEntity;
import com.erp.model.scm.entity.CfgSupplierSalesEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.server.scm.mapper.CfgSupplierSalesMapper;
import com.erp.server.scm.service.CfgSupplierSalesConditionService;
import com.erp.server.scm.service.CfgSupplierSalesService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SupplierService;
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgSupplierSalesDTO.CommonDTO addDTO) {
        handleData(addDTO);

        CfgSupplierSalesEntity cfgSupplierSalesEntity = new CfgSupplierSalesEntity();
        BeanMapperUtils.copy(addDTO, cfgSupplierSalesEntity);

        log.info("开始新增销量设置");
        boolean save = super.save(cfgSupplierSalesEntity);
        if(!save) {
            throw new ServiceException("销量设置保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销量设置" , cfgSupplierSalesEntity.getId());

        moduleOperateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), cfgSupplierSalesEntity.getId(), "新增操作");

        //处理配置条件
        saveCondition(addDTO, cfgSupplierSalesEntity.getId());

        return new BaseResultDTO.AddDTO(cfgSupplierSalesEntity.getId(), cfgSupplierSalesEntity.getId());
    }

    private void saveCondition(CfgSupplierSalesDTO.CommonDTO addDTO, String id) {
        //sku配置
        if(CollUtil.isNotEmpty(addDTO.getSkuList())){
            cfgSupplierSalesConditionService.saveRuleCondition(id, addDTO.getSkuList(), RuleTypeEnum.SKU.getCode(),"");
        }

        //可销库存配置
        if(CollUtil.isNotEmpty(addDTO.getSaleableStockList()) ){
            if(Objects.equals(addDTO.getWarehouseType(),CfgSupplierSalesConditionWarehouseTypeEnum.PHYSICALWAREHOUSE.getCode())){
                cfgSupplierSalesConditionService.saveRuleCondition(id, addDTO.getSaleableStockList(), RuleTypeEnum.PHYSICALWAREHOUSE.getCode(), addDTO.getWarehouseType());
            }
            if(Objects.equals(addDTO.getWarehouseType(),CfgSupplierSalesConditionWarehouseTypeEnum.VIRTUALWAREHOUSE.getCode())){
                cfgSupplierSalesConditionService.saveRuleCondition(id, addDTO.getSaleableStockList(), RuleTypeEnum.VIRTUALWAREHOUSE.getCode(), addDTO.getWarehouseType());
            }
        }

        //销量统计配置
        if(CollUtil.isNotEmpty(addDTO.getSalesStatisticList())){
            cfgSupplierSalesConditionService.saveRuleCondition(id, addDTO.getSalesStatisticList(), RuleTypeEnum.SALESSTATISTIC.getCode(),"");
        }

        //通知配置
        if(CollUtil.isNotEmpty(addDTO.getNoticeList())){
            cfgSupplierSalesConditionService.saveRuleCondition(id, addDTO.getNoticeList(), RuleTypeEnum.NOTICE.getCode(),"");
        }

        if(Objects.nonNull(addDTO.getBlackCondition())){
            cfgSupplierSalesConditionService.saveRuleCondition(id, Arrays.asList(addDTO.getBlackCondition()), RuleTypeEnum.BLACK.getCode(),"");
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
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgSupplierSalesEntity.getId(), "销量设置");
        moduleOperateLogService.addModuleOperateLogByObj(old, cfgSupplierSalesEntity, ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), cfgSupplierSalesEntity.getId(),"", msg);

        //处理配置条件
        updateCondition(addOrUpdateDTO, cfgSupplierSalesEntity.getId());
        return Boolean.TRUE;
    }


    private void updateCondition(CfgSupplierSalesDTO.CommonDTO addDTO, String id) {
        //sku配置
        cfgSupplierSalesConditionService.updateRuleCondition(id, addDTO.getSkuList(), ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), RuleTypeEnum.SKU.getCode(),"");

        //可销库存配置
        cfgSupplierSalesConditionService.updateRuleCondition(id, addDTO.getSaleableStockList(), ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), RuleTypeEnum.PHYSICALWAREHOUSE.getCode(),addDTO.getWarehouseType());

        cfgSupplierSalesConditionService.updateRuleCondition(id, addDTO.getSaleableStockList(), ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), RuleTypeEnum.VIRTUALWAREHOUSE.getCode(),addDTO.getWarehouseType());

        //销量统计配置
        cfgSupplierSalesConditionService.updateRuleCondition(id, addDTO.getSalesStatisticList(), ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), RuleTypeEnum.SALESSTATISTIC.getCode(),"");

        //通知配置
        cfgSupplierSalesConditionService.updateRuleCondition(id, addDTO.getNoticeList(), ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), RuleTypeEnum.NOTICE.getCode(),"");

        cfgSupplierSalesConditionService.updateRuleCondition(id, Arrays.asList(addDTO.getBlackCondition()), ModuleTypeEnum.CFG_SUPPLIER_SALES.getCode(), RuleTypeEnum.BLACK.getCode(),"");
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
            //todo 查询供应商的采购比例
        }else if(Objects.equals(salesRatioType,CfgSupplierSalesSalesRatioTypeEnum.SALESSTATISTICRATIO.getCode())){
            if(Objects.isNull(dto.getSalesRatio()) ){
                throw new ServiceException("请填写销量比例");
            }
            if(dto.getSalesRatio().compareTo(BigDecimal.ZERO)<=0 || dto.getSalesRatio().compareTo(BigDecimal.ONE)>0){
                throw new ServiceException("请填写销量比例范围0-1");
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

        for (Map.Entry<String, List<CfgSupplierSalesConditionEntity>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<CfgSupplierSalesConditionEntity> value = entry.getValue();

            List<CfgSupplierSalesConditionDTO.View> conditionList = value.stream()
                    .sorted(Comparator.comparingInt(CfgSupplierSalesConditionEntity::getIndex))
                    .map(e -> BeanMapperUtils.map(CfgSupplierSalesConditionDTO.View.class, e))
                    .collect(Collectors.toList());

            if(Objects.equals(key,RuleTypeEnum.SKU.getCode())){
                data.setSkuList(conditionList);
            }
            if(Objects.equals(key,RuleTypeEnum.PHYSICALWAREHOUSE.getCode()) || Objects.equals(key,RuleTypeEnum.VIRTUALWAREHOUSE.getCode())){
                data.setWarehouseType(value.get(0).getWarehouseType());
                data.setWarehouseTypeName(CfgSupplierSalesConditionWarehouseTypeEnum.getName(value.get(0).getWarehouseType()));
                data.setSaleableStockList(conditionList);
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
            String msg = StrUtil.format("用户【{}】操作【{}】单据变更为【{}】 ", UserContext.getDefaultLoginUser().getUserName(),  "销量设置",Objects.equals(disabled, Boolean.FALSE) ? "启用" : "停用");
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
        }
    }

    @Override
    public void exportList(CfgSupplierSalesDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("销量设置导出", EXPORT_SCM_CFG_SUPPLIER_SALES_REPORT.getCode(), pagingParamDTO);
    }


}

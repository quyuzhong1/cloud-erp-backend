package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.entity.BaseEntity;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.CfgReconciliationFieldDTO;
import com.erp.model.tms.dto.excel.FirstMileReconciliationStandardExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.listener.FirstMileReconciliationStandardExcelListener;
import com.erp.server.tms.mapper.TmsFirstMileReconciliationDetailMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 头程对账单明细 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Slf4j
@Service
public class TmsFirstMileReconciliationDetailServiceImpl extends SuperServiceImpl<TmsFirstMileReconciliationDetailMapper, TmsFirstMileReconciliationDetailEntity> implements TmsFirstMileReconciliationDetailService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private CommonService commonService;
    @Lazy
    @Resource
    private ShippingTemplateService shippingTemplateService;
    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private TmsCfgCostService tmsCfgCostService;
    @Resource
    private TmsCostDetailService tmsCostDetailService;
    @Lazy
    @Resource
    private TmsFirstMileLogisticService tmsFirstMileLogisticService;
    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Resource
    private TmsFirstMileReconciliationService tmsFirstMileReconciliationService;
    @Resource
    private CfgReconciliationFieldService cfgReconciliationFieldService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private SysUserFeign sysUserFeign;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsFirstMileReconciliationDetailDTO.AddDTO addDTO) {
        TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity = new TmsFirstMileReconciliationDetailEntity();
        BeanMapperUtils.copy(addDTO, tmsFirstMileReconciliationDetailEntity);

        // 数据处理
        handleData(tmsFirstMileReconciliationDetailEntity);

        log.info("开始新增头程对账单明细");
        boolean save = super.save(tmsFirstMileReconciliationDetailEntity);
        if (!save) {
            throw new ServiceException("头程对账单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "头程对账单明细", tmsFirstMileReconciliationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsFirstMileReconciliationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsFirstMileReconciliationDetailEntity.getId(), tmsFirstMileReconciliationDetailEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsFirstMileReconciliationDetailDTO.UpdateDTO updateDTO) {
        TmsFirstMileReconciliationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "头程对账单明细"));
        TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity = BeanMapperUtils.map(TmsFirstMileReconciliationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(tmsFirstMileReconciliationDetailEntity);
        log.info("编辑 开始修改头程对账单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsFirstMileReconciliationDetailEntity);
        if (!save) {
            throw new ServiceException("头程对账单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录头程对账单明细日志数据，id：【{}】", tmsFirstMileReconciliationDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsFirstMileReconciliationDetailEntity.getId(), "头程对账单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsFirstMileReconciliationDetailEntity, null, tmsFirstMileReconciliationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<TmsFirstMileReconciliationDetailDTO.ListDTO> paging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<?> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsFirstMileReconciliationDetailDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillList(List<TmsFirstMileReconciliationDetailDTO.ListDTO> records) {
        // 查询
        for (TmsFirstMileReconciliationDetailDTO.ListDTO record : records) {

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id, String status) {
        TmsFirstMileReconciliationDetailEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到头程对账单明细数据"));
        if (!StrUtil.equals(entity.getStatus(), ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())) {
            throw new ServiceException("只有待对账数据支持更新对账");
        }
        lambdaUpdate().eq(TmsFirstMileReconciliationDetailEntity::getId, id)
                .set(TmsFirstMileReconciliationDetailEntity::getStatus, status)
                .update();
        // 记录主单操作日志
        operateLogService.addModuleOperateLog(StrUtil.format("头程对账单【{}】更新状态为【{}】", entity.getSourceCode(), TmsB2cDeclareReconciliationStatusEnum.getName(status)), ModuleTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode(), entity.getId(), "更新状态操作");
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public TmsFirstMileReconciliationDetailDTO.ImportDTO importFile(TmsFirstMileReconciliationDetailDTO.ExcelImportDTO excelImportDTO, HttpServletResponse response) {
        TmsFirstMileReconciliationEntity mainEntity = tmsFirstMileReconciliationService.getByIdOpt(excelImportDTO.getId()).orElseThrow(() -> new ServiceException("未找到头程对账单主数据"));
        switch (excelImportDTO.getTypeEnum()) {
            case STANDARD:
                return importStandardFile(excelImportDTO.getExcelFile(), mainEntity);
            case CONFIG:
//                return importConfigFile(excelImportDTO.getExcelFile(), response, mainEntity);
            default:
                throw new ServiceException("输入导入的类型有误");
        }
    }

    @Override
    public PagingVO<TmsFirstMileReconciliationDetailDTO.ListDTO> waitReconciliationPaging(PagingDTO<TmsFirstMileReconciliationDetailDTO.PagingParamDTO> pagingParamDTO) {
        // 查询已签收（待对账）
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<?> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsFirstMileReconciliationDetailDTO.ListDTO> pageData = tmsFirstMileLogisticService.waitReconciliationPaging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillWaitReconciliationList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportList(TmsFirstMileReconciliationDetailDTO.ExportDTO param, HttpServletResponse response) {
        List<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> list = this.baseMapper.listExport(param);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 明细数据处理
        fillWaitReconciliationList(list);

        // 主数据处理
        fillMainInfo(list);

        // 导出数据
        String excelPath = "excel/tmsFirstMileReconciliationDetail.xlsx";
        String name = "头程对账单明细导出";
        try {
            new ExcelPrintUtils().patchExport(list,
                    response,
                    StrUtil.builder().append(DateUtil.nowExcelFileFormat()).append(name).toString(),
                    excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
    }

    private void fillMainInfo(List<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> list) {
        for (TmsFirstMileReconciliationDetailDTO.ExportDetailDTO data : list) {
            //审核状态名称
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
        }
    }

    @Override
    public List<TmsFirstMileReconciliationDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery()
                .in(TmsFirstMileReconciliationDetailEntity::getMainId, mainIds)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Boolean.TRUE;
        }
        List<TmsFirstMileReconciliationDetailEntity> list = BeanMapperUtils.copyList(TmsFirstMileReconciliationDetailEntity.class, detailList);

        handleUpdateData(list, mainId);

        //原明细数据被删除的需要清除mainId
        List<TmsFirstMileReconciliationDetailEntity> oldList = this.listByMainIds(Collections.singletonList(mainId));
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (!CollectionUtils.isEmpty(deleteIds)) {
            List<TmsFirstMileReconciliationDetailEntity> deleteList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = deleteList.stream().map(obj -> new Pair<>(mainId, obj.getTransportNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个头程对账明细【%s】", ModuleTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode(), pairList, "编辑操作");
            //更新主表id
            if (!CollectionUtils.isEmpty(deleteList)) {
                deleteList.forEach(obj -> obj.setMainId(""));
                list.addAll(deleteList);
            }
        }

        log.info("编辑 开始修改报关对账单数据，id：【{}】", mainId);
        boolean save = super.saveOrUpdateBatch(list);
        if (!save) {
            throw new ServiceException("采购对账单明细保存失败");
        }
        List<String> billIds = list.stream().map(TmsFirstMileReconciliationDetailEntity::getSourceId).distinct().collect(Collectors.toList());
        // 批量更新物流单状态
        boolean update = logisticsBillCostService.lambdaUpdate()
                .set(LogisticsBillCostEntity::getReconciliationStatus, ReconciliationStatusEnum.TO_BE_CONFIRM.getCode())
                .in(LogisticsBillCostEntity::getLogisticsBillId, billIds)
                .update();
        if (!update) {
            throw new ServiceException("更新物流单状态失败!请重试");
        }

        //更新费用信息
//        addOrUpdateCost(list);
        return Boolean.TRUE;
    }

    @Override
    public void fillDetailList(List<TmsFirstMileReconciliationDetailDTO.ListDTO> viewDTOList, String currency, String currencySymbol) {
        //店铺信息
        List<String> shopIdList = viewDTOList.stream()
                .map(TmsFirstMileReconciliationDetailDTO.ListDTO::getShopId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> shopMap = shopInfoFeign.listShopInfoByIds(shopIdList)
                .stream()
                .collect(Collectors.toMap(ShopInfoEntity::getId, ShopInfoEntity::getName));
        //国家信息
        List<String> countryIdList = viewDTOList.stream()
                .flatMap(route -> Stream.of(route.getFromCountry(), route.getToCountry()))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> countryMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(countryIdList)) {
            countryMap = sysDictFeign.listCountryByIds(countryIdList)
                    .stream()
                    .collect(Collectors.toMap(BaseEntity::getId, DictCountryEntity::getNameCn));
        }
        // 计费方式
        List<String> channelIds = viewDTOList.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());
        List<ShippingTemplateEntity> templateList = shippingTemplateService.getByChannelIds(channelIds);
        Map<String, List<ShippingTemplateEntity>> templateMap = templateList.stream()
                .collect(Collectors.groupingBy(ShippingTemplateEntity::getLogisticsChannelId));

        for (TmsFirstMileReconciliationDetailDTO.ListDTO viewDTO : viewDTOList) {
            if (null == viewDTO.getBillingMethod()){
                // 计费方式
                /// TODO 历史还是当前
                List<ShippingTemplateEntity> shippingTemplateList = templateMap.get(viewDTO.getLogisticsChannelId());
                if (!CollectionUtils.isEmpty(shippingTemplateList)) {
                    ShippingTemplateEntity shippingTemplateEntity = shippingTemplateList.stream().findFirst().orElse(null);
                    viewDTO.setBillingMethod(shippingTemplateEntity.getBillingMethod());
                    viewDTO.setBillingMethodName(EnumMessage.getNameByCode(ShippingBillingMethodEnum.class, shippingTemplateEntity.getBillingMethod()));
                }
            }
            if (StringUtils.isBlank(viewDTO.getSourceType())){
                viewDTO.setSourceType(SourceTypeEnum.LOGISTICS_BILL.getCode());
            }
            //店铺名称
            viewDTO.setShopName(shopMap.getOrDefault(viewDTO.getShopId(), ""));
            //国家名称
            viewDTO.setFromCountryName(countryMap.getOrDefault(viewDTO.getFromCountry(), ""));
            viewDTO.setToCountryName(countryMap.getOrDefault(viewDTO.getToCountry(), ""));
            // 默认预计
            if (null == viewDTO.getType()){
                viewDTO.setType(DetailReconciliationTypeEnum.ESTIMATED.getCode());
            }
            viewDTO.setTypeName(DetailReconciliationTypeEnum.getNameByCode(viewDTO.getType()));

            // 运输状态
            FmLogisticTrackStatusEnum statusEnum = FmLogisticTrackStatusEnum.getNameByCode(viewDTO.getTransportStatus());
            viewDTO.setTransportStatusName(null == statusEnum ? "" : statusEnum.getName());

            // 对账状态
            viewDTO.setStatusName(TmsB2cDeclareReconciliationStatusEnum.getName(viewDTO.getStatus()));
            // 明细币别
            viewDTO.setCurrencySymbol(currencySymbol);
//            viewDTO.setCurrency(currency);
            // 来源单号=业务单号
//            viewDTO.setBusinessCode(viewDTO.getSourceCode());
            // 默认
            if (null == viewDTO.getStatus()){
                viewDTO.setStatus(ReconciliationStatusEnum.TO_BE_CONFIRM.getCode());
                viewDTO.setStatusName(ReconciliationStatusEnum.TO_BE_CONFIRM.getName());
            }
        }
    }

    @Override
    public List<TmsFirstMileReconciliationDetailDTO.ListDTO> addWaitReconciliation(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        // 查询已签收（待对账）
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceList = tmsFirstMileLogisticService.listByMainIds(sourceIds);
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }
        // 只显示已签收未生成对账单
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceFilterList = sourceList.stream()
                .filter(e -> e.getReconciliationStatus().equalsIgnoreCase(ReconciliationStatusEnum.TO_BE_GENERATED.getCode()) && e.getTransportStatus().equalsIgnoreCase(FmLogisticTrackStatusEnum.SIGN.getCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sourceFilterList)) {
            return Collections.emptyList();
        }
        // 数据处理
        fillWaitReconciliationList(sourceFilterList);

        List<TmsFirstMileReconciliationDetailDTO.ListDTO> resultList = new LinkedList<>();
        // 生成差异和对比数据
        for (TmsFirstMileReconciliationDetailDTO.ListDTO sourceListDTO : sourceFilterList) {
            // 根据预计DTO生成：预计, 实际, 差异
            List<TmsFirstMileReconciliationDetailDTO.ListDTO> curAllTyoeList = generateAllTypeDTO(sourceListDTO);
            // 添加到结果
            resultList.addAll(curAllTyoeList);
        }
        return resultList;
    }

    /**
     * 根据预计DTO生成：预计, 实际, 差异
     */
    @Override
    public List<TmsFirstMileReconciliationDetailDTO.ListDTO> generateAllTypeDTO(TmsFirstMileReconciliationDetailDTO.ListDTO sourceListDTO) {
        // 实际
        TmsFirstMileReconciliationDetailDTO.ListDTO actualListDTO = new TmsFirstMileReconciliationDetailDTO.ListDTO();
        BeanUtils.copyProperties(sourceListDTO, actualListDTO);
        actualListDTO.setType(DetailReconciliationTypeEnum.ACTUAL.getCode());
        actualListDTO.setTypeName(DetailReconciliationTypeEnum.ACTUAL.getName());

        // 差异
        TmsFirstMileReconciliationDetailDTO.ListDTO diffListDTO = new TmsFirstMileReconciliationDetailDTO.ListDTO();
        BeanUtils.copyProperties(sourceListDTO, diffListDTO);
        diffListDTO.setType(DetailReconciliationTypeEnum.DIFF.getCode());
        diffListDTO.setTypeName(DetailReconciliationTypeEnum.DIFF.getName());
        return Arrays.asList(sourceListDTO, actualListDTO, diffListDTO);
    }

    /**
     * 重新计算差异值
     */
    private void generateDiff(
            TmsFirstMileReconciliationDetailDTO.ListDTO estimatedListDTO,
            TmsFirstMileReconciliationDetailDTO.ListDTO actualListDTO,
            TmsFirstMileReconciliationDetailDTO.ListDTO diffListDTO
    ) {
        // 重新计算差异值
        // 总物流费用
        diffListDTO.setTotalLogisticsCost(estimatedListDTO.getTotalLogisticsCost().subtract(actualListDTO.getTotalLogisticsCost()));
        // 实际重量【箱包装重量】
        diffListDTO.setActualWeight(estimatedListDTO.getActualWeight().subtract(actualListDTO.getActualWeight()));
        // 体积重
        diffListDTO.setVolumeWeight(estimatedListDTO.getVolumeWeight().subtract(actualListDTO.getVolumeWeight()));
        // 计费重
        diffListDTO.setBillingWeight(estimatedListDTO.getBillingWeight().subtract(actualListDTO.getBillingWeight()));
        // 物流运费用【预计物流费用】
        diffListDTO.setShippingCost(estimatedListDTO.getShippingCost().subtract(actualListDTO.getShippingCost()));
        // 报关费用【预计报关费用】
        diffListDTO.setDeclareCost(estimatedListDTO.getDeclareCost().subtract(actualListDTO.getDeclareCost()));
        // 其他费用【预计其他费用】
        diffListDTO.setOtherCost(estimatedListDTO.getOtherCost().subtract(actualListDTO.getOtherCost()));
    }


    private List<String> getDeleteIds(List<TmsFirstMileReconciliationDetailEntity> newList, List<TmsFirstMileReconciliationDetailEntity> oldList) {
        List<String> newIds = newList.stream()
                .map(TmsFirstMileReconciliationDetailEntity::getId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        List<String> oldIds = oldList.stream()
                .map(TmsFirstMileReconciliationDetailEntity::getId)
                .collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    private void handleUpdateData(List<TmsFirstMileReconciliationDetailEntity> list, String mainId) {
        // 统计预计费用
        Map<String, List<TmsFirstMileReconciliationDetailEntity>> sourceDetailMap = list.stream()
                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailEntity::getSourceId));

        // 查询对应物流单信息
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceList = tmsFirstMileLogisticService.listByMainIds(new ArrayList<>(sourceDetailMap.keySet()));

        Map<String, TmsFirstMileReconciliationDetailDTO.ListDTO> sourceMap = sourceList
                .stream()
                .collect(Collectors.toMap(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId, Function.identity()));
        // 校验状态
        for (Map.Entry<String, List<TmsFirstMileReconciliationDetailEntity>> entry : sourceDetailMap.entrySet()) {
            TmsFirstMileReconciliationDetailDTO.ListDTO listDTO = sourceMap.get(entry.getKey());
            if (null == listDTO) {
                throw new ServiceException("物流单不存在,sourceId=" + entry.getKey());
            }
            if (!ReconciliationStatusEnum.TO_BE_GENERATED.getCode().equalsIgnoreCase(listDTO.getReconciliationStatus())){
                throw new ServiceException("该物流单已生成对账单,物流运单号=" + listDTO.getTransportNo());
            }
            if (!FmLogisticTrackStatusEnum.SIGN.getCode().equalsIgnoreCase(listDTO.getTransportStatus())) {
                throw new ServiceException("该物流单未签收完成,物流运单号=" + listDTO.getTransportNo());
            }
        }
        // 补充基础信息
        fillWaitReconciliationList(sourceList);

        // 复制
        BeanUtils.copyProperties(sourceList, list);
        list.forEach(e ->
                e.setMainId(mainId)
        );
    }

    private void fillWaitReconciliationList(List<? extends TmsFirstMileReconciliationDetailDTO.ListDTO> records) {
        // 统计预计费用
        List<String> logisticsBillIds = records.stream()
                .map(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId)
                .distinct()
                .collect(Collectors.toList());
        List<TmsCostDetailEntity> costList = tmsCostDetailService.sumCostByMainIdAndCostId(LogisticsBillCostTypeEnum.ESTIMATED.getCode(), logisticsBillIds);

        // 计费方式
        List<String> channelIds = records.stream().map(TmsFirstMileReconciliationDetailDTO.ListDTO::getLogisticsChannelId).distinct().collect(Collectors.toList());
        List<ShippingTemplateEntity> templateList = shippingTemplateService.getByChannelIds(channelIds);
        Map<String, List<ShippingTemplateEntity>> templateMap = templateList.stream()
                .collect(Collectors.groupingBy(ShippingTemplateEntity::getLogisticsChannelId));

        //国家信息
        List<String> countryIdList = records.stream()
                .flatMap(route -> Stream.of(route.getFromCountry(), route.getToCountry()))
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> countryMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(countryIdList)) {
            countryMap = sysDictFeign.listCountryByIds(countryIdList)
                    .stream()
                    .collect(Collectors.toMap(BaseEntity::getId, DictCountryEntity::getNameCn));
        }
        // 头程物流费用配置
        List<TmsCfgCostEntity> tmsCfgCostList = tmsCfgCostService.listByCostAttribution(DictCostAttributionEnum.FIRST_MILE.getCode());
        Map<String, List<TmsCfgCostEntity>> tmsCfgCostGroupMap = tmsCfgCostList
                .stream()
                .collect(Collectors.groupingBy(TmsCfgCostEntity::getDictCostCategory));

        for (TmsFirstMileReconciliationDetailDTO.ListDTO record : records) {
            record.setSourceType(SourceTypeEnum.LOGISTICS_BILL.getCode());
            // 出库单号=发货单号
            // 待对账类型都是预估
            if (null == record.getType()) {
                record.setType(DetailReconciliationTypeEnum.ESTIMATED.getCode());
            }
            record.setTypeName(DetailReconciliationTypeEnum.getNameByCode(record.getType()));
            // 补充单位
            if (StringUtils.isBlank(record.getVolumeWeightUnit())) {
                record.setVolumeWeightUnit(record.getActualWeightUnit());
            }
            if (StringUtils.isBlank(record.getBillingWeightUnit())) {
                record.setBillingWeightUnit(record.getActualWeightUnit());
            }

            // 运输状态
            FmLogisticTrackStatusEnum statusEnum = FmLogisticTrackStatusEnum.getNameByCode(record.getTransportStatus());
            record.setTransportStatusName(null == statusEnum ? "" : statusEnum.getName());
            // 计费方式
            /// TODO 历史还是当前
            List<ShippingTemplateEntity> shippingTemplateList = templateMap.get(record.getLogisticsChannelId());
            if (!CollectionUtils.isEmpty(shippingTemplateList)) {
                ShippingTemplateEntity shippingTemplateEntity = shippingTemplateList.stream().findFirst().orElse(null);
                record.setBillingMethod(shippingTemplateEntity.getBillingMethod());
                record.setBillingMethodName(EnumMessage.getNameByCode(ShippingBillingMethodEnum.class, shippingTemplateEntity.getBillingMethod()));
            } else {
                record.setBillingMethod("");
                record.setBillingMethodName("");
            }
            record.setToCountryName(countryMap.getOrDefault(record.getToCountry(), ""));
            record.setFromCountryName(countryMap.getOrDefault(record.getFromCountry(), ""));

            // 按配置分组统计费用
            List<TmsCostDetailEntity> currentCostList = costList.stream().filter(e -> e.getMainId().equalsIgnoreCase(record.getSourceId())).collect(Collectors.toList());
            Map<String, BigDecimal> costIdSumMap = currentCostList.stream()
                    .collect(Collectors.groupingBy(TmsCostDetailEntity::getCfgCostId,
                            Collectors.reducing(BigDecimal.ZERO, TmsCostDetailEntity::getCostValue, BigDecimal::add)));

            // 物流运费
            List<TmsCfgCostEntity> shippingCostList = tmsCfgCostGroupMap.getOrDefault(DictCostCategoryEnum.SHIPPING_COST.getCode(), Collections.emptyList());
            BigDecimal shippingCost = shippingCostList.stream()
                    .map(e -> costIdSumMap.getOrDefault(e.getId(), BigDecimal.ZERO))
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            record.setShippingCost(shippingCost);

            // 报关费
            List<TmsCfgCostEntity> declareCostList = tmsCfgCostGroupMap.getOrDefault(DictCostCategoryEnum.DECLARE_COST.getCode(), Collections.emptyList());
            BigDecimal declareCost = declareCostList.stream()
                    .map(e -> costIdSumMap.getOrDefault(e.getId(), BigDecimal.ZERO))
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            record.setDeclareCost(declareCost);

            // 其他费用
            List<TmsCfgCostEntity> otherCostList = tmsCfgCostGroupMap.getOrDefault(DictCostCategoryEnum.OTHER_COST.getCode(), Collections.emptyList());
            BigDecimal otherCost = otherCostList.stream()
                    .map(e -> costIdSumMap.getOrDefault(e.getId(), BigDecimal.ZERO))
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            record.setOtherCost(otherCost);

            // 合计费用
            BigDecimal totalCost = costIdSumMap.values().stream().reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            record.setTotalLogisticsCost(totalCost);

            // 对账状态
            TmsB2cDeclareReconciliationStatusEnum status = null == record.getStatus() ? TmsB2cDeclareReconciliationStatusEnum.WAIT_CONFIRM :
                    TmsB2cDeclareReconciliationStatusEnum.getByCode(record.getStatus());
            record.setStatus(status.getCode());
            record.setStatusName(status.getName());
        }

    }


    /**
     * 新增修改处理数据
     */
    private void handleData(TmsFirstMileReconciliationDetailEntity tmsFirstMileReconciliationDetailEntity) {
        // 验证数据 & 数据赋值
    }

    private TmsFirstMileReconciliationDetailDTO.ImportDTO importStandardFile(MultipartFile excelFile, TmsFirstMileReconciliationEntity mainEntity) {
        FirstMileReconciliationStandardExcelListener excelListenerUtil = new FirstMileReconciliationStandardExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), FirstMileReconciliationStandardExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        TmsFirstMileReconciliationDetailDTO.ImportDTO importDTO = new TmsFirstMileReconciliationDetailDTO.ImportDTO();

        //验证导入数据是否为空
        List<FirstMileReconciliationStandardExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<FirstMileReconciliationStandardExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<FirstMileReconciliationStandardExcelDTO> errorList = excelListenerUtil.getErrorList();
        //导入数据保存
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> successImortList = handleImportStandardData(successList, errorList, mainEntity);

        String url = "";
        if (!CollectionUtils.isEmpty(errorList)) {
            String fileName = "头程对账单错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, FirstMileReconciliationStandardExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successImortList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }


    /**
     * 标准版导入数据处理
     */
    private List<TmsFirstMileReconciliationDetailDTO.ListDTO> handleImportStandardData(List<FirstMileReconciliationStandardExcelDTO> successList,
                                                                                       List<FirstMileReconciliationStandardExcelDTO> errorList, TmsFirstMileReconciliationEntity mainEntity) {
        if (CollectionUtils.isEmpty(successList)) {
            return Collections.emptyList();
        }
        // 结果
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> resultList = new LinkedList<>();
        // 币种
        String currency = mainEntity.getCurrency();
        String currencySymbol = this.getCurrencySymbol(currency);
        // 对应物流单(可能未保存)

        List<String> transportNoList = successList.stream().map(FirstMileReconciliationStandardExcelDTO::getTransportNo).distinct().collect(Collectors.toList());


        // 查询原物流单信息
        List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceLogisticList = tmsFirstMileLogisticService.listByTransportNoListAndSupplierIds(
                transportNoList,
                Collections.singletonList(mainEntity.getLogisticsSupplierId()));
        // 补充来源信息
        this.fillWaitReconciliationList(sourceLogisticList);
        Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>> sourceLogisticMap = sourceLogisticList
                .stream()
                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getTransportNo));

        // 物流跟踪单
        // 原对数据库账明细信息
        List<TmsFirstMileReconciliationDetailEntity> oldDetailList = this.listByMainIds(Collections.singletonList(mainEntity.getId()));

        List<TmsFirstMileReconciliationDetailDTO.ListDTO> viewDTOList = BeanMapperUtils.copyList(TmsFirstMileReconciliationDetailDTO.ListDTO.class, oldDetailList);
        // 补充基础信息
        this.fillDetailList(viewDTOList, currency, currencySymbol);

        // 按分组Map<物流运单号, Map<来源物流ID, 当前明细数组>>
//        Map<String, Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>>> oldDbGroupMap = viewDTOList
//                .stream()
//                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getTrackNo,
//                        Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId)));
        // 按分组Map<物流运单号, 当前明细数组>
        Map<String, List<TmsFirstMileReconciliationDetailDTO.ListDTO>> oldDbGroupMap = viewDTOList
                .stream()
                .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getTransportNo));

        //配置信息
        Map<String, CfgReconciliationFieldDTO.ErpFieldDropDownDTO> cfgErpFieldMap = cfgReconciliationFieldService.erpFieldList(Collections.singletonList(DictBasicEnum.CFG_FIRST_MILE_ERP_FIELD.getType()))
                .stream()
                .collect(Collectors.toMap(CfgReconciliationFieldDTO.ErpFieldDropDownDTO::getErpFieldName, Function.identity()));


        for (FirstMileReconciliationStandardExcelDTO excelDTO : successList) {
            // 对应物流单
            List<TmsFirstMileReconciliationDetailDTO.ListDTO> sourceDetailDTO = sourceLogisticMap.get(excelDTO.getTransportNo());
            if (CollectionUtils.isEmpty(sourceDetailDTO)) {
                excelDTO.setErrorMsg(StrUtil.format("未找到物流运单号【{}】的物流单", excelDTO.getTransportNo()));
                errorList.add(excelDTO);
                continue;
            }

            List<TmsFirstMileReconciliationDetailDTO.ListDTO> currentTrackNoList = oldDbGroupMap.get(excelDTO.getTransportNo());
            if (CollectionUtils.isEmpty(currentTrackNoList)) {
                // 生成当前物流单的所有明细
                currentTrackNoList = sourceDetailDTO.stream()
                        .map(this::generateAllTypeDTO)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
            }

            CfgReconciliationFieldDTO.ErpFieldDropDownDTO erpFieldDropDownDTO = cfgErpFieldMap.getOrDefault(excelDTO.getCostName(), null);
            if (null == erpFieldDropDownDTO) {
                excelDTO.setErrorMsg(StrUtil.format("字段配置中未找到费用项【{}】", excelDTO.getCostName()));
                errorList.add(excelDTO);
                continue;
            }

            //存在费用并且数量大于0
            if (MathUtil.compareTo(MathUtil.valueOf(excelDTO.getCostValue()), MathUtil.ZERO) <= MathUtil.ZERO) {
                excelDTO.setErrorMsg(StrUtil.format("费用金额【{}】必须大于0", excelDTO.getCostValue()));
                errorList.add(excelDTO);
                continue;
            }

            // 按sourceId分组
            Map<String, Map<String, TmsFirstMileReconciliationDetailDTO.ListDTO>> sourceListMap = currentTrackNoList.stream()
                    .collect(Collectors.groupingBy(TmsFirstMileReconciliationDetailDTO.ListDTO::getSourceId,
                            Collectors.toMap(TmsFirstMileReconciliationDetailDTO.ListDTO::getType, Function.identity())));

            for (Map.Entry<String, Map<String, TmsFirstMileReconciliationDetailDTO.ListDTO>> entry : sourceListMap.entrySet()) {
                // 预计
                TmsFirstMileReconciliationDetailDTO.ListDTO estimatedListDTO = entry.getValue().get(DetailReconciliationTypeEnum.ESTIMATED.getCode());
                // 实际
                TmsFirstMileReconciliationDetailDTO.ListDTO actualListDTO = entry.getValue().get(DetailReconciliationTypeEnum.ACTUAL.getCode());
                // 差异
                TmsFirstMileReconciliationDetailDTO.ListDTO diffListDTO = entry.getValue().get(DetailReconciliationTypeEnum.DIFF.getCode());

                // 设置实际为当前值
                if (SourceTypeEnum.TMS_CFG_COST.getCode().equalsIgnoreCase(erpFieldDropDownDTO.getSourceType())) {
                    DictCostCategoryEnum categoryEnum = DictCostCategoryEnum.getByCode(erpFieldDropDownDTO.getSourceCodeValue());
                    switch (categoryEnum) {
                        case SHIPPING_COST:
                            actualListDTO.setShippingCost(new BigDecimal(excelDTO.getCostValue()));
                        case DECLARE_COST:
                            actualListDTO.setDeclareCost(new BigDecimal(excelDTO.getCostValue()));
                        case OTHER_COST:
                            actualListDTO.setOtherCost(new BigDecimal(excelDTO.getCostValue()));
                    }
                } else if (SourceTypeEnum.DICT_BASIC.getCode().equalsIgnoreCase(erpFieldDropDownDTO.getSourceType())) {
                    // 根据字段名设置
                    ReflectUtil.setFieldValue(actualListDTO, erpFieldDropDownDTO.getSourceCodeValue(), new BigDecimal(excelDTO.getCostValue()));
                }
                // 重新计算差异
                generateDiff(estimatedListDTO, actualListDTO, diffListDTO);
                // 添加到当前结果
                resultList.addAll(Arrays.asList(estimatedListDTO, actualListDTO, diffListDTO));
            }
        }
        return resultList;
    }

    @Override
    public String getCurrencySymbol(String currency) {
        if (StringUtils.isBlank(currency)) {
            return currency;
        }

        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Collections.singletonList(currency));
        //币别符号
        return currencyList
                .stream()
                .filter(obj -> StrUtil.equals(obj.getId(), currency))
                .findFirst()
                .flatMap(obj -> Optional.ofNullable(obj.getSymbol()))
                .orElse("");

    }


}

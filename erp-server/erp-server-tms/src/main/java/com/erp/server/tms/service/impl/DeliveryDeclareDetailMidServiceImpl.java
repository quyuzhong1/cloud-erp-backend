package com.erp.server.tms.service.impl;

import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.*;
import io.seata.spring.annotation.GlobalTransactional;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.tms.dto.DeliveryDeclareDetailMidDTO;
import com.erp.model.tms.enums.DeclareStatusEnum;
import com.erp.model.tms.enums.DeliveryDeclareDetailMidGenerateStatusEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.SoDeliveryNoticeFeign;
import com.erp.rpc.wms.feign.WmsFirstMileDeliveryFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.tms.mapper.DeliveryDeclareDetailMidMapper;
import com.erp.server.tms.service.DeliveryDeclareDetailMidService;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.TmsDeclareBillService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 报关明细中间表服务实现类
 *
 * @author jack
 * @since 2026-04-27
 */
@Slf4j
@Service
public class DeliveryDeclareDetailMidServiceImpl extends SuperServiceImpl<DeliveryDeclareDetailMidMapper, DeliveryDeclareDetailMidEntity>
        implements DeliveryDeclareDetailMidService {

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private WmsFirstMileDeliveryFeign wmsFirstMileDeliveryFeign;
    @Resource
    private SoDeliveryNoticeFeign soDeliveryNoticeFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private WmsWarehouseFeign wmsWarehouseFeign;
    @Resource
    @Lazy
    private TmsDeclareBillService tmsDeclareBillService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(DeliveryDeclareDetailMidDTO.AddDTO addDTO) {
        DeliveryDeclareDetailMidEntity entity = BeanMapperUtils.map(DeliveryDeclareDetailMidEntity.class, addDTO);
        handleData(entity);
        boolean save = super.save(entity);
        if (!save) {
            throw new ServiceException(ApiError.BILL_SAVE_FAIL, "报关明细中间表");
        }
        operateLogService.addModuleOperateLog("新增报关明细中间表",
                null, entity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(entity.getId(), entity.getId());
    }

    @Override
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(DeliveryDeclareDetailMidDTO.UpdateDTO addOrUpdateDTO) {
        DeliveryDeclareDetailMidEntity old = super.getById(addOrUpdateDTO.getId());
        if (Objects.isNull(old)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "报关明细中间表");
        }
        DeliveryDeclareDetailMidEntity entity = BeanMapperUtils.map(DeliveryDeclareDetailMidEntity.class, addOrUpdateDTO);
        handleData(entity);
        boolean save = super.updateById(entity);
        if (!save) {
            throw new ServiceException(ApiError.BILL_UPDATE_FAILED);
        }
        operateLogService.addModuleOperateLogByObj(old, entity, null, entity.getId(), "更新报关明细中间表");
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<DeliveryDeclareDetailMidDTO.ListDTO> paging(PagingDTO<DeliveryDeclareDetailMidDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<DeliveryDeclareDetailMidDTO.ListDTO> query = new Page<DeliveryDeclareDetailMidDTO.ListDTO>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DeliveryDeclareDetailMidDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<DeliveryDeclareDetailMidDTO.ListDTO>(pageData);
        }
        fillList(pageData.getRecords());
        return new PagingVO<DeliveryDeclareDetailMidDTO.ListDTO>(pageData);
    }

    @Override
    public List<DeliveryDeclareDetailMidDTO.TabListDTO> tabList(PermissionsDTO param) {
        DeliveryDeclareDetailMidDTO.PagingParamDTO searchParam = new DeliveryDeclareDetailMidDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<DeliveryDeclareDetailMidDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        List<DeliveryDeclareDetailMidDTO.TabListDTO> result = new ArrayList<>();
        result.add(new DeliveryDeclareDetailMidDTO.TabListDTO("all","全部",0));

        DeliveryDeclareDetailMidDTO.TabListDTO wait = list.stream().filter(e -> Objects.equals(DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode(), e.getTabFlag()))
                .findFirst().orElse(new DeliveryDeclareDetailMidDTO.TabListDTO(DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode(),DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getName(),0));
        DeliveryDeclareDetailMidDTO.TabListDTO finish = list.stream().filter(e -> Objects.equals(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode(), e.getTabFlag()))
                .findFirst().orElse(new DeliveryDeclareDetailMidDTO.TabListDTO(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getCode(),DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getName(),0));

        wait.setTabFlagName(DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getName());
        result.add(wait);
        finish.setTabFlagName(DeliveryDeclareDetailMidGenerateStatusEnum.FINISH.getName());
        result.add(finish);
        return result;
    }

    @Override
    public void exportList(DeliveryDeclareDetailMidDTO.ExportDTO param, HttpServletResponse response) {
        List<DeliveryDeclareDetailMidDTO.ListDTO> list = this.baseMapper.listExport(param);
        if(CollUtil.isEmpty(list)) {
           return;
        }
        // 数据处理
        fillList(list);

        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/deliveryDeclareDetailMid.xlsx";
        String name = "报关明细中间单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }

    @Override
    public List<DeliveryDeclareDetailMidEntity> listByDeclareBillIdList(List<String> declareBillIdList) {
        return baseMapper.listByDeclareBillIdList(declareBillIdList);
    }

    @Override
    public List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listSourceByDeclareIdList(List<String> declareBillIdList) {
        return baseMapper.listSourceByDeclareIdList(declareBillIdList);
    }

    @Override
    public Boolean deleteDeliveryDeclareDetailMid(List<String> sourceIds) {
        if (CollUtil.isEmpty(sourceIds)) {
            return  Boolean.TRUE;
        }
        return lambdaUpdate().in(DeliveryDeclareDetailMidEntity::getSourceId,sourceIds).remove();
    }

    /**
     * 按报关单id恢复中间表为待生成状态。
     *
     * @param declareBillIds 报关单id集合
     * @return 是否处理成功
     */
    @Override
    public Boolean restoreWaitGenerateByDeclareBillIds(List<String> declareBillIds) {
        if (CollUtil.isEmpty(declareBillIds)) {
            return Boolean.TRUE;
        }
        // 恢复生成状态，并解除与已删除报关单的关联。
        return lambdaUpdate()
                .in(DeliveryDeclareDetailMidEntity::getDeclareId, declareBillIds)
                .set(DeliveryDeclareDetailMidEntity::getGenerateStatus, DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode())
                .set(DeliveryDeclareDetailMidEntity::getDeclareId, "")
                .set(DeliveryDeclareDetailMidEntity::getDeclareCode, "")
                .set(DeliveryDeclareDetailMidEntity::getDeclareDetailId, "")
                .update();
    }

    /**
     * 按中间表id恢复为待生成状态。
     *
     * @param ids 中间表id集合
     * @return 是否处理成功
     */
    @Override
    public Boolean restoreWaitGenerateByIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        // 编辑报关单删掉来源明细时，只恢复对应中间表行，不影响其它来源行。
        return lambdaUpdate()
                .in(DeliveryDeclareDetailMidEntity::getId, ids)
                .set(DeliveryDeclareDetailMidEntity::getGenerateStatus, DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode())
                .set(DeliveryDeclareDetailMidEntity::getDeclareId, "")
                .set(DeliveryDeclareDetailMidEntity::getDeclareCode, "")
                .set(DeliveryDeclareDetailMidEntity::getDeclareDetailId, "")
                .update();
    }

    /**
     * 查询报关明细中间表详情
     *
     * @param id 主键id
     * @return 报关明细中间表详情
     * @throws ServiceException 数据不存在时抛出
     * @author jack
     * @date 2026-04-29
     */
    @Override
    public DeliveryDeclareDetailMidDTO.ViewDTO view(String id) {
        DeliveryDeclareDetailMidEntity entity = super.getByIdOpt(id)
                .orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "报关明细中间表"));
        DeliveryDeclareDetailMidDTO.ViewDTO viewDTO = BeanMapperUtils.map(DeliveryDeclareDetailMidDTO.ViewDTO.class, entity);
        if (CharSequenceUtil.isBlank(viewDTO.getTransferWarehouseNames())) {
            viewDTO.setTransferWarehouseNames(getTransferWarehouseNames(viewDTO.getTransferWarehouseIds()));
        }
        return viewDTO;
    }

    /**
     * 合并后预览
     *
     * @param ids 报关明细中间表id集合
     * @return 合并后预览列表
     * @throws ServiceException 校验失败时抛出
     * @author jack
     * @date 2026-05-06
     */
    @Override
    public List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeAfterPreview(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_REQUIRED);
        }
        List<String> distinctIds = ids.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(distinctIds)) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_REQUIRED);
        }
        List<DeliveryDeclareDetailMidEntity> entityList = super.listByIds(distinctIds);
        if (CollectionUtils.isEmpty(entityList) || entityList.size() != distinctIds.size()) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_NOT_FOUND);
        }
        if (entityList.stream().anyMatch(item -> !CharSequenceUtil.equals(item.getGenerateStatus(),
                DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode()))) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_STATUS_LIMIT);
        }
        Set<String> sourceTypeSet = entityList.stream()
                .map(DeliveryDeclareDetailMidEntity::getSourceType)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (sourceTypeSet.size() != 1) {
            throw new ServiceException(ApiError.LOGISTICS_DECLARE_DETAIL_MID_PREVIEW_SOURCE_TYPE_CONFLICT);
        }
        String sourceType = sourceTypeSet.iterator().next();

        List<String> sourceIds = entityList.stream()
                .map(DeliveryDeclareDetailMidEntity::getSourceId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = new ArrayList<>();
        if(Objects.equals(sourceType, SourceTypeEnum.FIRST_MILE_DELIVERY.getCode())){
            sourceDetailList = wmsFirstMileDeliveryFeign.listBeforePushFmDeclare(new TmsDeclareBillDTO.PushDeclareBeforeParamDTO(Boolean.TRUE, sourceIds));
        }else if(Objects.equals(sourceType, SourceTypeEnum.SO_DELIVERY_NOTICE.getCode())){
            sourceDetailList = soDeliveryNoticeFeign.listBeforePushB2bDeclare(new TmsDeclareBillDTO.PushDeclareBeforeParamDTO(Boolean.TRUE, sourceIds));
        }
        sourceDetailList.stream().forEach(e -> e.setSourceType(sourceType));
        return tmsDeclareBillService.autoMergeDeclareBillView(
                new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(Boolean.TRUE, sourceDetailList));
    }

    /**
     * 自动生成报关明细中间表
     *
     * @param list 自动生成参数
     * @return 是否生成成功
     * @throws ServiceException 自动生成失败时抛出
     * @author jack
     * @date 2026-04-29
     */
    @Override
    @DistributeLocker(businessType = "tmsAutoGenerateDeclareMidData", keyName = "list.sourceId", waiteTime = 60)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public Boolean autoGenerateMidData(List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> list) {
        // 自动生成依赖来源明细和来源类型，缺一则不触发生成。
        if (CollUtil.isEmpty(list)) {
            log.warn("自动生成报关明细中间表失败：来源明细为空");
            return Boolean.FALSE;
        }
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetailList = list.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(sourceDetailList)) {
            log.warn("自动生成报关明细中间表失败：有效来源明细为空");
            return Boolean.FALSE;
        }
        Set<String> sourceTypeSet = sourceDetailList.stream()
                .map(TmsDeclareBillDTO.SourceDeliveryDetailDTO::getSourceType)
                .filter(CharSequenceUtil::isNotBlank)
                .collect(Collectors.toSet());
        if (sourceTypeSet.size() != 1) {
            log.warn("自动生成报关明细中间表失败：来源类型不唯一，sourceTypeSet={}", sourceTypeSet);
            return Boolean.FALSE;
        }
        String sourceType = sourceTypeSet.iterator().next();
        if (!Objects.equals(sourceType, SourceTypeEnum.FIRST_MILE_DELIVERY.getCode())
                && !Objects.equals(sourceType, SourceTypeEnum.SO_DELIVERY_NOTICE.getCode())) {
            log.warn("自动生成报关明细中间表失败：来源类型不支持，sourceType={}", sourceType);
            return Boolean.FALSE;
        }
        List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeDeclareBillList = tmsDeclareBillService.autoMergeDeclareBillView(
                new TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO(Boolean.FALSE, sourceDetailList));
        if (CollUtil.isEmpty(mergeDeclareBillList)) {
            log.warn("自动生成报关明细中间表失败：合并报关结果为空，sourceType={}，sourceDetailCount={}", sourceType, sourceDetailList.size());
            return Boolean.FALSE;
        }
        return batchAddMergeDetail(mergeDeclareBillList);
    }

    @Override
    public Boolean batchAddMergeDetail(List<TmsDeclareBillDTO.MergeDeclareBillDTO> list) {
        String sourceType = list.get(0).getDeclareBillList().get(0).getSourceDeliveryDetailList().get(0).getSourceType();
        if(Objects.equals(sourceType, SourceTypeEnum.FIRST_MILE_DELIVERY.getCode())){
            return tmsDeclareBillService.batchAddMergeDetail(SourceTypeEnum.FM_DECLARE_BILL.getCode(),list, Boolean.TRUE);
        }else if(Objects.equals(sourceType, SourceTypeEnum.SO_DELIVERY_NOTICE.getCode())){
            return tmsDeclareBillService.batchAddMergeDetail(SourceTypeEnum.B2B_DECLARE_BILL.getCode(),list, Boolean.TRUE);
        }else {
            log.warn("批量保存自动合并报关明细失败：来源类型不支持，sourceType={}", sourceType);
            return Boolean.FALSE;
        }
    }

    @Override
    public List<DeliveryDeclareDetailMidEntity> listBySourceIdList(List<String> sourceIds) {
        return lambdaQuery()
                .in(DeliveryDeclareDetailMidEntity::getSourceId, sourceIds)
                .list();
    }

    /**
     * 查询商品物流信息映射
     *
     * @param skuIds 商品id集合
     * @return 商品id到商品物流信息的映射
     * @throws RuntimeException 远程调用异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    private Map<String, ProductDetailDTO.ProductLogisticDTO> getProductLogisticsMap(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        // 从PLM批量获取最新商品物流申报信息，避免分页列表逐行远程调用。
        List<ProductDetailDTO.ProductLogisticDTO> list = plmTaskFeign.listProductLogisticsByIds(skuIds);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getSkuId()))
                .collect(Collectors.toMap(ProductDetailDTO.ProductLogisticDTO::getSkuId, item -> item, (o1, o2) -> o1));
    }

    /**
     * 处理中间表默认数据
     *
     * @param entity 报关明细中间表实体
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private void handleData(DeliveryDeclareDetailMidEntity entity) {
        if (CharSequenceUtil.isBlank(entity.getDeclareStatus())) {
            entity.setDeclareStatus(DeclareStatusEnum.WAIT.getCode());
        }
        if (CharSequenceUtil.isBlank(entity.getGenerateStatus())) {
            entity.setGenerateStatus(DeliveryDeclareDetailMidGenerateStatusEnum.WAIT.getCode());
        }
        if (CharSequenceUtil.isBlank(entity.getTransferWarehouseNames())) {
            entity.setTransferWarehouseNames(getTransferWarehouseNames(entity.getTransferWarehouseIds()));
        }
    }

    /**
     * 填充分页列表展示数据
     *
     * @param list 分页列表
     * @throws RuntimeException 远程查询商品物流信息异常时抛出
     * @author jack
     * @date 2026-04-29
     */
    private void fillList(List<DeliveryDeclareDetailMidDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, ProductDetailDTO.ProductLogisticDTO> productLogisticMap = getProductLogisticsMap(list.stream()
                .map(DeliveryDeclareDetailMidDTO.ListDTO::getSkuId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
        Map<String, String> transferWarehouseNameMap = getTransferWarehouseNameMap(list.stream()
                .map(DeliveryDeclareDetailMidDTO.ListDTO::getTransferWarehouseIds)
                .collect(Collectors.toList()));
        for (DeliveryDeclareDetailMidDTO.ListDTO data : list) {
            data.setDeclareStatusName(DeclareStatusEnum.getName(data.getDeclareStatus()));
            data.setGenerateStatusName(DeliveryDeclareDetailMidGenerateStatusEnum.getName(data.getGenerateStatus()));
            if (CharSequenceUtil.isBlank(data.getTransferWarehouseNames())) {
                data.setTransferWarehouseNames(buildTransferWarehouseNames(data.getTransferWarehouseIds(), transferWarehouseNameMap));
            }
            fillLatestProductLogistic(data, productLogisticMap.get(data.getSkuId()));
        }
    }

    /**
     * 获取中转仓名称
     *
     * @param transferWarehouseIds 中转仓id
     * @return 中转仓名称
     * @throws RuntimeException 远程调用异常时抛出
     * @author jack
     * @date 2026-05-06
     */
    private String getTransferWarehouseNames(String transferWarehouseIds) {
        if (CharSequenceUtil.isBlank(transferWarehouseIds)) {
            return "";
        }
        return buildTransferWarehouseNames(transferWarehouseIds,
                getTransferWarehouseNameMap(Collections.singletonList(transferWarehouseIds)));
    }

    /**
     * 获取中转仓名称映射
     *
     * @param transferWarehouseIdsList 中转仓id集合
     * @return 中转仓名称映射
     * @throws RuntimeException 远程调用异常时抛出
     * @author jack
     * @date 2026-05-06
     */
    private Map<String, String> getTransferWarehouseNameMap(List<String> transferWarehouseIdsList) {
        if (CollectionUtils.isEmpty(transferWarehouseIdsList)) {
            return Collections.emptyMap();
        }
        List<String> warehouseIds = transferWarehouseIdsList.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .flatMap(item -> CharSequenceUtil.split(item, ",").stream())
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(warehouseIds)) {
            return Collections.emptyMap();
        }
        List<WarehouseDTO.ListDTO> warehouseList = wmsWarehouseFeign.listByIds(warehouseIds);
        if (CollectionUtils.isEmpty(warehouseList)) {
            return Collections.emptyMap();
        }
        return warehouseList.stream()
                .filter(item -> CharSequenceUtil.isNotBlank(item.getId()))
                .collect(Collectors.toMap(WarehouseDTO.ListDTO::getId,
                        item -> CharSequenceUtil.blankToDefault(item.getName(), ""),
                        (oldValue, newValue) -> oldValue));
    }

    /**
     * 构建中转仓名称
     *
     * @param transferWarehouseIds 中转仓id
     * @param transferWarehouseNameMap 中转仓名称映射
     * @return 中转仓名称
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-05-06
     */
    private String buildTransferWarehouseNames(String transferWarehouseIds, Map<String, String> transferWarehouseNameMap) {
        if (CharSequenceUtil.isBlank(transferWarehouseIds) || CollUtil.isEmpty(transferWarehouseNameMap)) {
            return "";
        }
        List<String> warehouseNameList = new ArrayList<String>();
        for (String warehouseId : CharSequenceUtil.split(transferWarehouseIds, ",")) {
            if (CharSequenceUtil.isBlank(warehouseId)) {
                continue;
            }
            String warehouseName = transferWarehouseNameMap.get(warehouseId);
            if (CharSequenceUtil.isNotBlank(warehouseName)) {
                warehouseNameList.add(warehouseName);
            }
        }
        if (CollectionUtils.isEmpty(warehouseNameList)) {
            return "";
        }
        return String.join(",", warehouseNameList);
    }

    /**
     * 填充最新商品物流信息
     *
     * @param data 列表数据
     * @param productLogisticDTO 商品物流信息
     * @throws RuntimeException 当前方法不主动抛出业务异常
     * @author jack
     * @date 2026-04-29
     */
    private void fillLatestProductLogistic(DeliveryDeclareDetailMidDTO.ListDTO data,
                                           ProductDetailDTO.ProductLogisticDTO productLogisticDTO) {
        if (Objects.isNull(productLogisticDTO)) {
            return;
        }
        data.setLatestHsCode(CharSequenceUtil.blankToDefault(productLogisticDTO.getCustomsCode(), ""));
        data.setLatestProductNameCn(CharSequenceUtil.blankToDefault(productLogisticDTO.getDeclareChineseName(), ""));
        data.setLatestDeclareElement(CharSequenceUtil.blankToDefault(productLogisticDTO.getDeclareElement(), ""));
        data.setLatestUnit(CharSequenceUtil.blankToDefault(productLogisticDTO.getDeclareUnit(), ""));
        data.setLatestUnitPrice(productLogisticDTO.getPrice());
    }
}

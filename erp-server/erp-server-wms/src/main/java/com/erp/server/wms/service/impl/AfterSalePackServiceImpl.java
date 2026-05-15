package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.AfterSalePackStatusEnum;
import com.common.business.enums.AfterSalePackTypeEnum;
import com.common.business.enums.BooleanEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.AfterSalePackDTO;
import com.erp.model.wms.dto.AfterSalePackDetailDTO;
import com.erp.model.wms.entity.AfterSalePackDetailEntity;
import com.erp.model.wms.entity.AfterSalePackEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.rpc.plm.feign.ProductDetailFeign;
import com.erp.server.wms.mapper.AfterSalePackMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 售后装箱表 服务实现类
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
@Slf4j
@Service
public class AfterSalePackServiceImpl extends SuperServiceImpl<AfterSalePackMapper, AfterSalePackEntity> implements AfterSalePackService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private ProductDetailFeign productDetailFeign;

    @Resource
    private AfterSalePackDetailService afterSalePackDetailService;

    @Resource
    private PoReturnService poReturnService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> boxCodeApplication(AfterSalePackDTO.BoxCodeApplicationDTO dto) {
        if (dto.getApplicationQty() <= 0) {
            throw new ServiceException("申请数量需大于0");
        }
        List<AfterSalePackEntity> afterSalePackEntityList = new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < dto.getApplicationQty(); i++) {
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_BOX);
            result.add(code);
            AfterSalePackEntity afterSalePackEntity = new AfterSalePackEntity();
            afterSalePackEntity.setCode(code);
            afterSalePackEntity.setType(dto.getType());
            afterSalePackEntityList.add(afterSalePackEntity);
        }
        boolean save = super.saveBatch(afterSalePackEntityList);
        if (!save) {
            throw new ServiceException("售后装箱单保存失败");
        }
        for (AfterSalePackEntity afterSalePackEntity : afterSalePackEntityList) {
            // 操作日志
            String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "售后装箱单", afterSalePackEntity.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE_PACK.getName(), afterSalePackEntity.getId(), "新增操作");
        }
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AfterSalePackDTO.AddDTO addDTO) {
        AfterSalePackEntity afterSalePackEntity = new AfterSalePackEntity();
        BeanMapperUtils.copy(addDTO, afterSalePackEntity);
        log.info("开始新增售后装箱单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_BOX);
        afterSalePackEntity.setCode(code);
        // 处理装箱明细数据
        if (CollectionUtils.isNotEmpty(addDTO.getDetailList())) {
            handleAfterSalePackDetail(addDTO.getDetailList(), afterSalePackEntity);
            afterSalePackEntity.setPackStatus(AfterSalePackStatusEnum.PENDING.getCode());
        } else {
            afterSalePackEntity.setSkuSpeciesQty(0);
            afterSalePackEntity.setTotalQty(0);
            afterSalePackEntity.setPackStatus(AfterSalePackStatusEnum.WAIT_PACKING.getCode());
        }
        boolean save = super.save(afterSalePackEntity);
        if (!save) {
            throw new ServiceException("售后装箱单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "售后装箱单", afterSalePackEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE_PACK.getName(), afterSalePackEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(afterSalePackEntity.getId(), code);
    }

    private void handleAfterSalePackDetail(List<AfterSalePackDetailDTO.UpdateDTO> detailList, AfterSalePackEntity afterSalePackEntity) {
        // 查询sku信息
        List<String> skuNos = detailList.stream().map(AfterSalePackDetailDTO.UpdateDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productList = productDetailFeign.listBySkuNos(skuNos);
        Set<String> skuNoSet = productList.stream().map(ProductDetailEntity::getSkuNo).filter(Objects::nonNull).collect(Collectors.toSet());
        List<String> skuNotInProduct = detailList.stream()
                .map(AfterSalePackDetailDTO.UpdateDTO::getSkuNo)
                .filter(Objects::nonNull)
                .filter(skuNo -> !skuNoSet.contains(skuNo))
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(skuNotInProduct)) {
            throw new ServiceException("售后装箱单保存失败，以下SKU不存在：" + String.join(",", skuNotInProduct));
        }
        // 查询仓位信息
        List<String> warehouseLocationCodes = detailList.stream().map(AfterSalePackDetailDTO.UpdateDTO::getOutWarehouseLocationCode).distinct().collect(Collectors.toList());
        List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.lambdaQuery()
                .in(WarehouseLocationEntity::getCode, warehouseLocationCodes)
                .eq(WarehouseLocationEntity::getDisabled, false)
                .list();
        Set<String> warehouseLocationCodeSet = warehouseLocationEntityList.stream()
                .map(WarehouseLocationEntity::getCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<String> notExistWarehouseLocationCodeList = detailList.stream()
                .map(AfterSalePackDetailDTO.UpdateDTO::getOutWarehouseLocationCode)
                .filter(Objects::nonNull)
                .filter(outWarehouseLocationCode -> !warehouseLocationCodeSet.contains(outWarehouseLocationCode))
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notExistWarehouseLocationCodeList)) {
            throw new ServiceException("售后装箱单保存失败，以下仓位不存在：" + String.join(",", notExistWarehouseLocationCodeList));
        }
        Map<String, ProductDetailEntity> productMap = productList.stream().collect(Collectors.toMap(ProductDetailEntity::getSkuNo, item -> item));
        // 查询售后装箱明细信息
        List<AfterSalePackDetailEntity> afterSalePackDetailEntityList = afterSalePackDetailService.lambdaQuery().eq(AfterSalePackDetailEntity::getMainId, afterSalePackEntity.getId()).list();
        Map<String, AfterSalePackDetailEntity> map = afterSalePackDetailEntityList.stream().collect(Collectors.toMap(BaseEntity::getId, item -> item));
        List<String> ids = detailList.stream().map(AfterSalePackDetailDTO.UpdateDTO::getId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(ids)) {
            List<String> deleteDetailList = afterSalePackDetailEntityList.stream()
                    .map(AfterSalePackDetailEntity::getId)
                    .filter(Objects::nonNull)
                    .filter(id -> !ids.contains(id))
                    .distinct()
                    .collect(Collectors.toList());
            afterSalePackDetailService.removeByIds(deleteDetailList);
        }
        Map<String, WarehouseLocationEntity> warehouseLocationMap = warehouseLocationEntityList.stream().collect(Collectors.toMap(WarehouseLocationEntity::getCode, item -> item));
        List<AfterSalePackDetailEntity> detailEntityList = new ArrayList<>();
        List<AfterSalePackDetailEntity> addList = new ArrayList<>();
        List<AfterSalePackDetailEntity> updateList = new ArrayList<>();
        for (AfterSalePackDetailDTO.UpdateDTO dto : detailList) {
            ProductDetailEntity productDetailEntity = productMap.get(dto.getSkuNo());
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationMap.get(dto.getOutWarehouseLocationCode());
            if (StringUtils.isNotBlank(dto.getId())) {
                AfterSalePackDetailEntity detailEntity = map.get(dto.getId());
                if (ObjectUtil.isNull(detailEntity)) {
                    throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后装箱明细");
                }
                detailEntity.setSkuId(productDetailEntity.getId());
                detailEntity.setSkuNo(dto.getSkuNo());
                detailEntity.setOutWarehouseLocationId(warehouseLocationEntity.getId());
                detailEntity.setPackQty(dto.getPackQty());
                detailEntityList.add(detailEntity);
                updateList.add(detailEntity);
            } else {
                AfterSalePackDetailEntity afterSalePackDetailEntity = new AfterSalePackDetailEntity();
                BeanMapperUtils.copy(dto, afterSalePackDetailEntity);
                afterSalePackDetailEntity.setMainId(afterSalePackEntity.getId());
                afterSalePackDetailEntity.setSkuId(productDetailEntity.getId());
                afterSalePackDetailEntity.setOutWarehouseLocationId(warehouseLocationEntity.getId());
                detailEntityList.add(afterSalePackDetailEntity);
                addList.add(afterSalePackDetailEntity);
            }
        }
        afterSalePackEntity.setSkuSpeciesQty(productMap.size());
        afterSalePackEntity.setTotalQty(detailList.stream().mapToInt(AfterSalePackDetailDTO.UpdateDTO::getPackQty).sum());
        afterSalePackDetailService.saveOrUpdateBatch(detailEntityList);
        // 这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(afterSalePackEntity.getId(), obj.getSkuNo())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.AFTER_SALE_PACK.getCode(), addPairList, "编辑操作");
        // 修改的
        for (AfterSalePackDetailEntity update : updateList) {
            AfterSalePackDetailEntity old = map.get(update.getId());
            if (old != null) {
                operateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.AFTER_SALE_PACK.getCode(), afterSalePackEntity.getId(), "", "");
            }
        }
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AfterSalePackDTO.UpdateDTO addOrUpdateDTO) {
        AfterSalePackEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "箱唛"));
        // 箱唛状态等于复审中或者已封箱状态，不可修改
        if (AfterSalePackStatusEnum.UNDER_REVIEW.getCode().equals(old.getPackStatus()) || AfterSalePackStatusEnum.SEALED_BOX.getCode().equals(old.getPackStatus())) {
            throw new ServiceException("箱唛状态等于复审中或者已封箱状态，不可修改");
        }
        if (Boolean.TRUE.equals(old.getIsUse())) {
            throw new ServiceException("该箱唛已被单据使用，不可修改");
        }
        AfterSalePackEntity afterSalePackEntity = BeanMapperUtils.map(AfterSalePackEntity.class, addOrUpdateDTO);
        log.info("编辑 开始修改售后装箱单数据，单号：【{}】", old.getCode());
        // 处理装箱明细数据
        if (CollectionUtils.isNotEmpty(addOrUpdateDTO.getDetailList())) {
            handleAfterSalePackDetail(addOrUpdateDTO.getDetailList(), afterSalePackEntity);
            afterSalePackEntity.setPackStatus(AfterSalePackStatusEnum.PENDING.getCode());
        } else {
            afterSalePackEntity.setSkuSpeciesQty(0);
            afterSalePackEntity.setTotalQty(0);
            afterSalePackEntity.setPackStatus(AfterSalePackStatusEnum.WAIT_PACKING.getCode());
        }
        boolean save = super.updateById(afterSalePackEntity);
        if (!save) {
            throw new ServiceException("售后装箱单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录售后装箱单日志数据，单号：【{}】", afterSalePackEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSalePackEntity.getCode(), "售后装箱单");
        operateLogService.addModuleOperateLogByObj(old, afterSalePackEntity, ModuleTypeEnum.AFTER_SALE_PACK.getName(), afterSalePackEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean submit(AfterSalePackDTO.UpdateDTO addOrUpdateDTO) {
        // 如果明细行数据为空，不可提交
        if (CollectionUtils.isEmpty(addOrUpdateDTO.getDetailList())) {
            throw new ServiceException("请添加售后装箱明细");
        }
        AfterSalePackEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "箱唛"));
        // 箱唛状态等于复审中或者已封箱状态，不可提交审核
        if (AfterSalePackStatusEnum.UNDER_REVIEW.getCode().equals(old.getPackStatus()) || AfterSalePackStatusEnum.SEALED_BOX.getCode().equals(old.getPackStatus())) {
            throw new ServiceException("箱唛状态等于复审中或者已封箱状态，不可提交审核");
        }
        if (Boolean.TRUE.equals(old.getIsUse())) {
            throw new ServiceException("该箱唛已被单据使用，不可提交审核");
        }
        AfterSalePackEntity afterSalePackEntity = BeanMapperUtils.map(AfterSalePackEntity.class, addOrUpdateDTO);
        log.info("确定提审 开始修改售后装箱单数据，单号：【{}】", old.getCode());
        // 处理装箱明细数据
        if (CollectionUtils.isNotEmpty(addOrUpdateDTO.getDetailList())) {
            handleAfterSalePackDetail(addOrUpdateDTO.getDetailList(), afterSalePackEntity);
        }
        afterSalePackEntity.setPackStatus(AfterSalePackStatusEnum.UNDER_REVIEW.getCode());
        boolean save = super.updateById(afterSalePackEntity);
        if (!save) {
            throw new ServiceException("售后装箱单确定提审失败");
        }
        // 记录主单操作日志
        log.info("确定提审 开始记录售后装箱单日志数据，单号：【{}】", afterSalePackEntity.getCode());
        String msg = StrUtil.format("用户【{}】确定提审单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSalePackEntity.getCode(), "售后装箱单");
        operateLogService.addModuleOperateLogByObj(old, afterSalePackEntity, ModuleTypeEnum.AFTER_SALE_PACK.getName(), afterSalePackEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean reject(AfterSalePackDTO.UpdateDTO addOrUpdateDTO) {
        AfterSalePackEntity afterSalePackEntity = super.getById(addOrUpdateDTO.getId());
        afterSalePackEntity = Optional.ofNullable(afterSalePackEntity).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "箱唛"));
        // 箱唛状态不等于复审中，不可驳回复审
        if (!AfterSalePackStatusEnum.UNDER_REVIEW.getCode().equals(afterSalePackEntity.getPackStatus())) {
            throw new ServiceException("箱唛状态等于【{}】状态，不可复核驳回", AfterSalePackStatusEnum.getByName(afterSalePackEntity.getPackStatus()));
        }
        if (Boolean.TRUE.equals(afterSalePackEntity.getIsUse())) {
            throw new ServiceException("该箱唛已被单据使用，不可复核驳回");
        }
        log.info("复核驳回 开始修改售后装箱单数据，单号：【{}】", afterSalePackEntity.getCode());
        afterSalePackEntity.setPackStatus(AfterSalePackStatusEnum.REVIEW_REJECT.getCode());
        afterSalePackEntity.setRejectDescription(addOrUpdateDTO.getRejectDescription());
        boolean save = super.updateById(afterSalePackEntity);
        if (!save) {
            throw new ServiceException("售后装箱单复核驳回失败");
        }
        // 记录主单操作日志
        log.info("复核驳回 开始记录售后装箱单日志数据，单号：【{}】", afterSalePackEntity.getCode());
        String msg = StrUtil.format("用户【{}】复核驳回单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSalePackEntity.getCode(), "售后装箱单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE_PACK.getName(), afterSalePackEntity.getId(), "复核驳回");
        return Boolean.TRUE;
    }

    @Override
    public Boolean confirm(AfterSalePackDTO.UpdateDTO addOrUpdateDTO) {
        // 如果明细行数据为空，不可确定并封箱
        if (CollectionUtils.isEmpty(addOrUpdateDTO.getDetailList())) {
            throw new ServiceException("请添加售后装箱明细");
        }
        AfterSalePackEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "箱唛"));
        // 箱唛状态不等于复审中，不可确定并封箱
        if (!AfterSalePackStatusEnum.UNDER_REVIEW.getCode().equals(old.getPackStatus())) {
            throw new ServiceException("箱唛状态不等于复审中，不可确定并封箱");
        }
        if (Boolean.TRUE.equals(old.getIsUse())) {
            throw new ServiceException("该箱唛已被单据使用，不可确定并封箱");
        }
        AfterSalePackEntity afterSalePackEntity = BeanMapperUtils.map(AfterSalePackEntity.class, addOrUpdateDTO);
        log.info("确定并封箱 开始修改售后装箱单数据，单号：【{}】", old.getCode());
        // 处理装箱明细数据
        if (CollectionUtils.isNotEmpty(addOrUpdateDTO.getDetailList())) {
            handleAfterSalePackDetail(addOrUpdateDTO.getDetailList(), afterSalePackEntity);
        }
        afterSalePackEntity.setPackStatus(AfterSalePackStatusEnum.SEALED_BOX.getCode());
        boolean save = super.updateById(afterSalePackEntity);
        if (!save) {
            throw new ServiceException("售后装箱单确定并封箱失败");
        }
        // 记录主单操作日志
        log.info("确定并封箱 开始记录售后装箱单日志数据，单号：【{}】", afterSalePackEntity.getCode());
        String msg = StrUtil.format("用户【{}】确定并封箱单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSalePackEntity.getCode(), "售后装箱单");
        operateLogService.addModuleOperateLogByObj(old, afterSalePackEntity, ModuleTypeEnum.AFTER_SALE_PACK.getName(), afterSalePackEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<AfterSalePackDTO.ListDTO> paging(PagingDTO<AfterSalePackDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AfterSalePackDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(AfterSalePackDTO.ExportDTO param, HttpServletResponse response) {
        List<AfterSalePackDTO.ListDTO> list = this.baseMapper.listExport(param);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);
        // 导出数据
        StringBuilder sb = new StringBuilder();
        String excelPath = "excel/afterSalePack.xlsx";
        String name = "售后装箱单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }

    @Override
    public AfterSalePackDTO.ViewDTO view(String id) {
        AfterSalePackEntity afterSalePackEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后装箱单数据"));
        AfterSalePackDTO.ViewDTO data = BeanMapperUtils.map(AfterSalePackDTO.ViewDTO.class, afterSalePackEntity);
        data.setTypeName(AfterSalePackTypeEnum.getByName(data.getType()));
        // 查询采购退货信息
        PoReturnEntity poReturnEntity = poReturnService.getById(data.getSourceId());
        if (ObjectUtil.isNotEmpty(poReturnEntity)) {
            data.setSupplierId(poReturnEntity.getSupplierId());
            data.setSupplierName(poReturnEntity.getSupplierName());
        }
        // 查询明细数据
        List<AfterSalePackDetailEntity> afterSalePackDetailEntityList = afterSalePackDetailService.lambdaQuery()
                .eq(AfterSalePackDetailEntity::getMainId, id)
                .list();
        if (CollectionUtils.isNotEmpty(afterSalePackDetailEntityList)) {
            // 查询拣货仓位信息
            List<String> outWarehouseLocationIds = afterSalePackDetailEntityList.stream().map(AfterSalePackDetailEntity::getOutWarehouseLocationId).distinct().collect(Collectors.toList());
            List<WarehouseLocationEntity> outWarehouseLocationEntityList = warehouseLocationService.listByIds(outWarehouseLocationIds);
            Map<String, WarehouseLocationEntity> outWarehouseLocationMap = outWarehouseLocationEntityList.stream().collect(Collectors.toMap(WarehouseLocationEntity::getId, item -> item));
            // 查询移入仓位信息
            List<String> inWarehouseLocationIds = afterSalePackDetailEntityList.stream().map(AfterSalePackDetailEntity::getInWarehouseLocationId).distinct().collect(Collectors.toList());
            List<WarehouseLocationEntity> inWarehouseLocationEntityList = warehouseLocationService.listByIds(inWarehouseLocationIds);
            Map<String, WarehouseLocationEntity> inWarehouseLocationMap = inWarehouseLocationEntityList.stream().collect(Collectors.toMap(WarehouseLocationEntity::getId, item -> item));
            List<AfterSalePackDetailDTO.ViewDTO> viewDTOList = new ArrayList<>();
            for (AfterSalePackDetailEntity afterSalePackDetailEntity : afterSalePackDetailEntityList) {
                AfterSalePackDetailDTO.ViewDTO viewDTO = new AfterSalePackDetailDTO.ViewDTO();
                BeanMapperUtils.copy(afterSalePackDetailEntity, viewDTO);
                WarehouseLocationEntity outWarehouseLocationEntity = outWarehouseLocationMap.get(afterSalePackDetailEntity.getOutWarehouseLocationId());
                if (ObjectUtil.isNotEmpty(outWarehouseLocationEntity)) {
                    viewDTO.setOutWarehouseLocationCode(outWarehouseLocationEntity.getCode());
                    viewDTO.setOutWarehouseLocationName(outWarehouseLocationEntity.getName());
                }
                WarehouseLocationEntity inWarehouseLocationEntity = inWarehouseLocationMap.get(afterSalePackDetailEntity.getInWarehouseLocationId());
                if (ObjectUtil.isNotEmpty(inWarehouseLocationEntity)) {
                    viewDTO.setInWarehouseLocationCode(inWarehouseLocationEntity.getCode());
                    viewDTO.setInWarehouseLocationName(inWarehouseLocationEntity.getName());
                }
                viewDTOList.add(viewDTO);
            }
            data.setDetailViewDTOList(viewDTOList);
        }
        return data;
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<AfterSalePackDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for (AfterSalePackDTO.ListDTO data : list) {
            data.setTypeName(AfterSalePackTypeEnum.getByName(data.getType()));
            data.setIsUseName(data.getIsUse() ? "已使用" : "未使用");
            data.setIsDifferenceName(BooleanEnum.getByCode(data.getIsDifference()));
            data.setPackStatusName(AfterSalePackStatusEnum.getByName(data.getPackStatus()));
            data.setIsMoveWarehouseName(BooleanEnum.getByCode(data.getIsMoveWarehouse()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> delete(BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        List<AfterSalePackEntity> afterSalePackEntityList = super.listByIds(dto.getIds());
        Map<String, AfterSalePackEntity> map = afterSalePackEntityList.stream().collect(Collectors.toMap(BaseEntity::getId, item -> item));
        for (String id : dto.getIds()) {
            AfterSalePackEntity afterSalePackEntity = map.get(id);
            BatchResultDTO deleteResult;
            if (ObjectUtil.isEmpty(afterSalePackEntity)) {
                deleteResult = BatchResultDTO.fail(id, id, "售后装箱不存在, 删除失败");
            } else if (afterSalePackEntity.getIsUse()) {
                deleteResult = BatchResultDTO.fail(id, afterSalePackEntity.getCode(), "该箱唛已被单据使用, 删除失败");
            } else {
                // 删除数据
                this.removeById(id);
                // 删除明细数据
                afterSalePackDetailService.lambdaUpdate().eq(AfterSalePackDetailEntity::getMainId, id).remove();
                deleteResult = BatchResultDTO.success(id, afterSalePackEntity.getCode(), "删除成功");
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS;
    }

    @Override
    public AfterSalePackDTO.ViewDTO viewByCode(String code) {
        AfterSalePackEntity afterSalePackEntity = this.lambdaQuery()
                .eq(AfterSalePackEntity::getCode, code)
                .one();
        if (ObjectUtil.isEmpty(afterSalePackEntity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后装箱单");
        }
        return this.view(afterSalePackEntity.getId());
    }

}

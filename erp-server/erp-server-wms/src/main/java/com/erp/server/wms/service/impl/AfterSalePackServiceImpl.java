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
    private AfterSalePackDetailService afterSalePackingDetailService;

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
        List<AfterSalePackEntity> afterSalePackingEntityList = new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (int i = 0; i < dto.getApplicationQty(); i++) {
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_BOX);
            result.add(code);
            AfterSalePackEntity afterSalePackingEntity = new AfterSalePackEntity();
            afterSalePackingEntity.setCode(code);
            afterSalePackingEntity.setType(dto.getType());
            afterSalePackingEntityList.add(afterSalePackingEntity);
        }
        boolean save = super.saveBatch(afterSalePackingEntityList);
        if (!save) {
            throw new ServiceException("售后装箱单保存失败");
        }
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AfterSalePackDTO.AddDTO addDTO) {
        AfterSalePackEntity afterSalePackingEntity = new AfterSalePackEntity();
        BeanMapperUtils.copy(addDTO, afterSalePackingEntity);
        log.info("开始新增售后装箱单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_BOX);
        afterSalePackingEntity.setCode(code);
        // 处理装箱明细数据
        handleAfterSalePackDetail(addDTO.getDetailList(), afterSalePackingEntity);
        boolean save = super.save(afterSalePackingEntity);
        if (!save) {
            throw new ServiceException("售后装箱单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "售后装箱单", afterSalePackingEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.AFTER_SALE_PACKING.getName(), afterSalePackingEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(afterSalePackingEntity.getId(), code);
    }

    private void handleAfterSalePackDetail(List<AfterSalePackDetailDTO.UpdateDTO> detailList, AfterSalePackEntity afterSalePackingEntity) {
        if (CollectionUtils.isNotEmpty(detailList)) {
            // 查询sku信息
            List<String> skuIds = detailList.stream().map(AfterSalePackDetailDTO.UpdateDTO::getSkuId).distinct().collect(Collectors.toList());
            List<ProductDetailEntity> productList = productDetailFeign.listByIds(skuIds);
            Map<String, ProductDetailEntity> productMap = productList.stream().collect(Collectors.toMap(BaseEntity::getId, item -> item));
            // 查询售后装箱明细信息
            List<String> ids = detailList.stream().map(AfterSalePackDetailDTO.UpdateDTO::getId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            Map<String, AfterSalePackDetailEntity> map = new HashMap<>();
            if (CollectionUtils.isNotEmpty(ids)) {
                List<AfterSalePackDetailEntity> afterSalePackingDetailEntityList = afterSalePackingDetailService.listByIds(ids);
                map = afterSalePackingDetailEntityList.stream().collect(Collectors.toMap(BaseEntity::getId, item -> item));
            }
            // 查询仓位信息
            List<String> warehouseLocationIds = detailList.stream().map(AfterSalePackDetailDTO.UpdateDTO::getWarehouseLocationId).distinct().collect(Collectors.toList());
            List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.listByIds(warehouseLocationIds);
            Map<String, WarehouseLocationEntity> warehouseLocationMap = warehouseLocationEntityList.stream().collect(Collectors.toMap(WarehouseLocationEntity::getId, item -> item));
            List<AfterSalePackDetailEntity> detailEntityList = new ArrayList<>();
            for (AfterSalePackDetailDTO.UpdateDTO dto : detailList) {
                ProductDetailEntity productDetailEntity = productMap.get(dto.getSkuId());
                if (ObjectUtil.isNull(productDetailEntity)) {
                    throw new ServiceException("商品不存在");
                }
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationMap.get(dto.getWarehouseLocationId());
                if (ObjectUtil.isNull(warehouseLocationEntity)) {
                    throw new ServiceException("仓位不存在");
                }
                if (StringUtils.isNotBlank(dto.getId())) {
                    AfterSalePackDetailEntity detailEntity = map.get(dto.getId());
                    if (ObjectUtil.isNull(detailEntity)) {
                        throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后装箱明细");
                    }
                    detailEntity.setSkuId(dto.getSkuId());
                    detailEntity.setSkuNo(productDetailEntity.getSkuNo());
                    detailEntity.setWarehouseLocationId(dto.getWarehouseLocationId());
                    detailEntity.setPackQty(dto.getPackQty());
                    detailEntityList.add(detailEntity);
                } else {
                    AfterSalePackDetailEntity afterSalePackingDetailEntity = new AfterSalePackDetailEntity();
                    BeanMapperUtils.copy(dto, afterSalePackingDetailEntity);
                    afterSalePackingDetailEntity.setMainId(afterSalePackingEntity.getId());
                    afterSalePackingDetailEntity.setSkuNo(productDetailEntity.getSkuNo());
                    detailEntityList.add(afterSalePackingDetailEntity);
                }
            }
            afterSalePackingEntity.setSkuSpeciesQty(productMap.size());
            afterSalePackingEntity.setTotalQty(detailList.stream().mapToInt(AfterSalePackDetailDTO.UpdateDTO::getPackQty).sum());
            afterSalePackingEntity.setPackStatus(Boolean.TRUE);
            afterSalePackingDetailService.saveBatch(detailEntityList);
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
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "售后装箱单"));
        if (Boolean.TRUE.equals(old.getUsageStatus())) {
            throw new ServiceException("该箱唛已被单据使用，不可修改");
        }
        AfterSalePackEntity afterSalePackingEntity = BeanMapperUtils.map(AfterSalePackEntity.class, addOrUpdateDTO);
        log.info("编辑 开始修改售后装箱单数据，单号：【{}】", old.getCode());
        // 处理装箱明细数据
        handleAfterSalePackDetail(addOrUpdateDTO.getDetailList(), afterSalePackingEntity);
        boolean save = super.updateById(afterSalePackingEntity);
        if (!save) {
            throw new ServiceException("售后装箱单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录售后装箱单日志数据，单号：【{}】", afterSalePackingEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), afterSalePackingEntity.getCode(), "售后装箱单");
        operateLogService.addModuleOperateLogByObj(old, afterSalePackingEntity, ModuleTypeEnum.AFTER_SALE_PACKING.getName(), afterSalePackingEntity.getId(), msg);
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
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/afterSalePacking.xlsx";
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
        AfterSalePackEntity afterSalePackingEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到售后装箱单数据"));
        AfterSalePackDTO.ViewDTO data = BeanMapperUtils.map(AfterSalePackDTO.ViewDTO.class, afterSalePackingEntity);
        data.setTypeName(AfterSalePackTypeEnum.getByName(data.getType()));
        // 查询采购退货信息
        PoReturnEntity poReturnEntity = poReturnService.getById(data.getSourceId());
        if (ObjectUtil.isNotEmpty(poReturnEntity)) {
            data.setSupplierId(poReturnEntity.getSupplierId());
            data.setSupplierName(poReturnEntity.getSupplierName());
        }
        // 查询明细数据
        List<AfterSalePackDetailEntity> afterSalePackingDetailEntityList = afterSalePackingDetailService.lambdaQuery()
                .eq(AfterSalePackDetailEntity::getMainId, id)
                .list();
        if (CollectionUtils.isNotEmpty(afterSalePackingDetailEntityList)) {
            // 查询仓位信息
            List<String> warehouseLocationIds = afterSalePackingDetailEntityList.stream().map(AfterSalePackDetailEntity::getWarehouseLocationId).distinct().collect(Collectors.toList());
            List<WarehouseLocationEntity> warehouseLocationEntityList = warehouseLocationService.listByIds(warehouseLocationIds);
            Map<String, WarehouseLocationEntity> warehouseLocationMap = warehouseLocationEntityList.stream().collect(Collectors.toMap(WarehouseLocationEntity::getId, item -> item));
            List<AfterSalePackDetailDTO.ViewDTO> viewDTOList = new ArrayList<>();
            for (AfterSalePackDetailEntity afterSalePackDetailEntity : afterSalePackingDetailEntityList) {
                AfterSalePackDetailDTO.ViewDTO viewDTO = new AfterSalePackDetailDTO.ViewDTO();
                BeanMapperUtils.copy(afterSalePackDetailEntity, viewDTO);
                WarehouseLocationEntity warehouseLocationEntity = warehouseLocationMap.get(afterSalePackDetailEntity.getWarehouseLocationId());
                if (ObjectUtil.isNotEmpty(warehouseLocationEntity)) {
                    viewDTO.setWarehouseLocationCode(warehouseLocationEntity.getCode());
                    viewDTO.setWarehouseLocationName(warehouseLocationEntity.getName());
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
            data.setUsageStatusName(data.getUsageStatus() ? "已使用" : "未使用");
            data.setIsDifferenceName(BooleanEnum.getByCode(data.getIsDifference()));
            data.setPackStatusName(data.getPackStatus() ? "已装箱" : "未装箱");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> delete(BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        List<AfterSalePackEntity> afterSalePackingEntityList = super.listByIds(dto.getIds());
        Map<String, AfterSalePackEntity> map = afterSalePackingEntityList.stream().collect(Collectors.toMap(BaseEntity::getId, item -> item));
        for (String id : dto.getIds()) {
            AfterSalePackEntity afterSalePackingEntity = map.get(id);
            BatchResultDTO deleteResult;
            if (ObjectUtil.isEmpty(afterSalePackingEntity)) {
                deleteResult = BatchResultDTO.fail(id, id, "售后装箱不存在, 删除失败");
            } else if (afterSalePackingEntity.getUsageStatus()) {
                deleteResult = BatchResultDTO.fail(id, afterSalePackingEntity.getCode(), "该箱唛已被单据使用, 删除失败");
            } else {
                // 删除数据
                this.removeById(id);
                // 删除明细数据
                afterSalePackingDetailService.lambdaUpdate().eq(AfterSalePackDetailEntity::getMainId, id).remove();
                deleteResult = BatchResultDTO.success(id, afterSalePackingEntity.getCode(), "删除成功");
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

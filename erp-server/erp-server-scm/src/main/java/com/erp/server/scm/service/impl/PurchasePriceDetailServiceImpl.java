package com.erp.server.scm.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.dto.PurchasePriceChangeDetailDTO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.dto.excel.PurchasePriceDetailImportExcelDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.kingdee.SyncKingdeePurchasePriceService;
import com.erp.server.scm.listener.PurchasePriceDetailExcelListener;
import com.erp.server.scm.mapper.PurchasePriceDetailMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceService;
import com.erp.server.scm.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品采购价格明细表 服务实现类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
@Slf4j
@Service
public class PurchasePriceDetailServiceImpl extends SuperServiceImpl<PurchasePriceDetailMapper, PurchasePriceDetailEntity> implements PurchasePriceDetailService {


    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchasePriceService priceService;

    @Resource
    private SyncKingdeePurchasePriceService syncKingdeePurchasePriceService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private SupplierService supplierService;

    /**
     * 添加明细
     *
     * @param purchasePriceId
     * @param purchasePriceDetailList
     * @return void
     * @author yl
     * @date 2023-03-24 15:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPriceDetail(String purchasePriceId, List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList) {
        if (CollectionUtils.isEmpty(purchasePriceDetailList)) {
            return;
        }
        List<PurchasePriceDetailEntity> addList = BeanMapper.copyList(purchasePriceDetailList, PurchasePriceDetailEntity.class);
        List<String> skuIds = addList.stream().map(PurchasePriceDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);

        PurchasePriceEntity purchasePriceEntity = priceService.getById(purchasePriceId);
        if (ObjectUtils.isEmpty(purchasePriceEntity)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        //验证时间
        checkPurchasePriceDetail(purchasePriceEntity.getSupplierId(),purchasePriceEntity.getPurchaseOrgId(),addList);
        for (PurchasePriceDetailEntity item : addList) {
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            if (skuVO != null) {
                item.setSkuNo(skuVO.getSkuNo());
                item.setProductName(skuVO.getSkuName());
            }
            item.setPurchasePriceId(purchasePriceId);
            //税率
            BigDecimal taxRate = item.getTaxRate();
            if (taxRate != null) {
                BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                item.setTaxRate(rate);
            }
            item.setCurrency(purchasePriceEntity.getCurrency());
            item.setPricingUserId(purchasePriceEntity.getPricingUserId());
        }
        this.saveBatch(addList);
        //标记SKU
        plmTaskFeign.updateOccupyStatus(skuIds);
    }

    /**
     * @description: 验证采购价目明细
     * @author Will
     * @date: 2024/1/15 9:35
     * @param list
     */
    @Override
    public void checkPurchasePriceDetail (String supplierId,String purchaseOrgId,List<PurchasePriceDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //查询供应商信息
        List<String> skuIdList = list.stream().map(PurchasePriceDetailEntity::getSkuId).collect(Collectors.toList());
        List<PurchasePriceDetailDTO.ViewDTO> purchaseDetailList = listCheckPurchasePriceDetail(supplierId, purchaseOrgId, skuIdList);
        if (CollectionUtils.isNotEmpty(purchaseDetailList)) {
            List<String> oldIdList = list.stream().map(PurchasePriceDetailEntity::getId).collect(Collectors.toList());
            purchaseDetailList = purchaseDetailList.stream().filter(obj -> !oldIdList.contains(obj.getId())).collect(Collectors.toList());
        }

        for (int i = 0;i < list.size();i++) {
            PurchasePriceDetailEntity entity = list.get(i);
            //检验失效时间需要大于生效时间
            if (entity.getExpireDate().isBefore(entity.getEffectiveDate())) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_PRICE_DATE,entity.getSkuNo());
            }
            //校验录入数据是否存在时间重叠
            for (int j = 0;j < list.size();j++) {
                PurchasePriceDetailEntity detailEntity = list.get(j);
                if (i == j || !StrUtil.equals(entity.getSkuId(),detailEntity.getSkuId())) {
                    continue;
                }
                //验证是否重叠
                checkOverlap(entity,detailEntity);
            }

            List<PurchasePriceDetailEntity> oldList = purchaseDetailList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), entity.getSkuId())).map(obj -> BeanMapperUtils.map(PurchasePriceDetailEntity.class,obj) ).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(oldList)) {
                continue;
            }
            //验证是否重叠
            oldList.stream().forEach(obj -> checkOverlap(entity,obj));
        }
    }

    /**
     * @description: 验证是否重叠
     * @author Will
     * @date: 2024/3/1 14:17
     * @param entity
     * @param detailEntity
     */
    private void checkOverlap (PurchasePriceDetailEntity entity,PurchasePriceDetailEntity detailEntity) {
        //区间重叠时
        if (entity.getMinQty().compareTo(detailEntity.getMaxQty()) < MathUtil.ZERO
                && detailEntity.getMinQty().compareTo(entity.getMaxQty()) < MathUtil.ZERO ) {
            //时间不能重叠
            boolean overlap = LocalDateUtil.isOverlap(entity.getEffectiveDate(), entity.getExpireDate(), detailEntity.getEffectiveDate(), detailEntity.getExpireDate());
            if (overlap) {
                throw new ServiceException(ApiError.ERROR_PURCHASE_PRICE_DATE_OVERLAP,entity.getSkuNo());
            }
        }
        //时间重叠时
        boolean overlap = LocalDateUtil.isOverlap(entity.getEffectiveDate(), entity.getExpireDate(), detailEntity.getEffectiveDate(), detailEntity.getExpireDate());
        if (overlap) {
            //区间不能重叠
            if (entity.getMinQty().compareTo(detailEntity.getMaxQty()) < MathUtil.ZERO
                    && detailEntity.getMinQty().compareTo(entity.getMaxQty()) < MathUtil.ZERO ) {
                throw new ServiceException(ApiError.ERROR_INTERVAL_SUPPLIER_OVERLAP);
            }
        }
    }

    /**
     * 根据价目表id 获取产品明细信息
     *
     * @param purchasePriceId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-27 9:48
     */
    @Override
    public List<PurchasePriceDetailDTO.ViewDTO> getByPurchasePriceId(String purchasePriceId) {
        List<PurchasePriceDetailEntity> list = this.getListByPurchasePriceId(purchasePriceId);
        List<PurchasePriceDetailDTO.ViewDTO> viewList = BeanMapper.copyList(list, PurchasePriceDetailDTO.ViewDTO.class);
        //处理采购价目明细信息
        handlePurchasePriceDetail(viewList);
        return viewList;
    }

    @Override
    public List<PurchasePriceDetailDTO.ViewDTO> listByPurchasePriceIds(PurchasePriceChangeDetailDTO.SkuChangeParamDTO dto) {
        List<PurchasePriceDetailEntity> list = this.listGetListByPurchasePriceId(dto);

        List<PurchasePriceDetailDTO.ViewDTO> viewList = BeanMapper.copyList(list, PurchasePriceDetailDTO.ViewDTO.class);
        //处理采购价目明细信息
        handlePurchasePriceDetail(viewList);

        //根据id查询供应商
        List<String> supplierIds = list.stream().map(req -> req.getSupplierId()).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierEntities = supplierService.listByIds(supplierIds);

        //设置供应商名称
        for (PurchasePriceDetailDTO.ViewDTO viewDTO : viewList) {
            SupplierEntity supplierEntity = supplierEntities.stream().filter(req -> viewDTO.getSupplierId().equals(req.getId())).findFirst().orElse(new SupplierEntity());
            viewDTO.setSupplierName(supplierEntity.getName());
        }
        return viewList;
    }

    @Override
    public List<PurchasePriceDetailDTO.ViewDTO> listByPurchasePriceDetailIds(List<String> purchasePriceDetailIds) {
        List<PurchasePriceDetailEntity> list = baseMapper.listDetailByIds(purchasePriceDetailIds);
        List<PurchasePriceDetailDTO.ViewDTO> viewList = BeanMapper.copyList(list, PurchasePriceDetailDTO.ViewDTO.class);
        //处理采购价目明细信息
        handlePurchasePriceDetail(viewList);
        return viewList;
    }

    /**
     * 根据价目表id 和详情表id 集合获取对应数据
     *
     * @param purchasePriceId
     * @param purchasePriceDetailIds 采购价目详情表id 集合
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-27 9:48
     */
    @Override
    public List<PurchasePriceDetailDTO.ViewDTO> getPriceDetail(String purchasePriceId, List<String> purchasePriceDetailIds) {
        LambdaQueryWrapper<PurchasePriceDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchasePriceDetailEntity::getPurchasePriceId, purchasePriceId);
        if (CollectionUtils.isNotEmpty(purchasePriceDetailIds)) {
            queryWrapper.notIn(PurchasePriceDetailEntity::getId, purchasePriceDetailIds);
        }
        List<PurchasePriceDetailEntity> list = this.list(queryWrapper);
        List<PurchasePriceDetailDTO.ViewDTO> viewList = BeanMapper.copyList(list, PurchasePriceDetailDTO.ViewDTO.class);
        return viewList;
    }

    /**
     * 修改产品明细
     *
     * @param purchasePriceId
     * @param purchasePriceDetailList
     * @return void
     * @author yl
     * @date 2023-03-27 11:18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePriceDetail(String purchasePriceId, List<PurchasePriceDetailDTO.UpdateDTO> purchasePriceDetailList) {
        if (CollectionUtils.isEmpty(purchasePriceDetailList)) {
            return;
        }
        List<PurchasePriceDetailEntity> dbList = this.getListByPurchasePriceId(purchasePriceId);
        //获取到删除id集合
        List<String> deleteIdList = getDeleteIds(purchasePriceDetailList, dbList);
        List<PurchasePriceDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());


        this.removeByIds(deleteIdList);
        List<String> skuIds = purchasePriceDetailList.stream().map(PurchasePriceDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<PurchasePriceDetailEntity> saveOrUpdateList = new ArrayList<>(purchasePriceDetailList.size());

        PurchasePriceEntity purchasePriceEntity = priceService.getById(purchasePriceId);
        if (ObjectUtils.isEmpty(purchasePriceEntity)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        for (PurchasePriceDetailDTO.UpdateDTO item : purchasePriceDetailList) {
            PurchasePriceDetailEntity entity = new PurchasePriceDetailEntity();
            BeanMapper.copy(item, entity);
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            if (skuVO != null) {
                entity.setSkuNo(skuVO.getSkuNo());
                entity.setProductName(skuVO.getSpuName());
            }
            entity.setPurchasePriceId(purchasePriceId);
            //税率
            BigDecimal taxRate = item.getTaxRate();
            if (taxRate != null) {
                BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                entity.setTaxRate(rate);
            }
            entity.setCurrency(purchasePriceEntity.getCurrency());
            entity.setPricingUserId(purchasePriceEntity.getPricingUserId());
            saveOrUpdateList.add(entity);
        }

        //验证时间
        checkPurchasePriceDetail(purchasePriceEntity.getSupplierId(),purchasePriceEntity.getPurchaseOrgId(),saveOrUpdateList);

        //这是要添加的
        List<PurchasePriceDetailEntity> addList = saveOrUpdateList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //这是修改的
        List<PurchasePriceDetailEntity> updateList = saveOrUpdateList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(purchasePriceId, obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_PRICE.getCode(), removePairList, "编辑操作");

        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(purchasePriceId, obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.PURCHASE_PRICE.getCode(), addPairList, "编辑操作");

        //修改的
        for (PurchasePriceDetailEntity update : updateList) {
            String id = update.getId();
            PurchasePriceDetailEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                moduleOperateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.PURCHASE_PRICE.getCode(), purchasePriceId, "", "");
            }
        }
        this.saveOrUpdateBatch(saveOrUpdateList);

        //标记SKU
        plmTaskFeign.updateOccupyStatus(skuIds);
    }


    /**
     * 下载模板
     *
     * @param response
     * @return void
     * @author yl
     * @date 2023-03-27 16:08
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/purchasePriceDetail.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("warehouse downloadTemplate  出错了 e==", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }

    }

    /**
     * 导入产品信息
     *
     * @param excelFile
     * @return com.erp.model.scm.dto.PurchasePriceDetailDTO.ImportDTO
     * @author yl
     * @date 2023-03-27 16:51
     */
    @Override
    public PurchasePriceDetailDTO.ImportDTO importFile(MultipartFile excelFile, List<String> skuIds, HttpServletResponse response) {
        //查询所有审核通过的sku
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        PurchasePriceDetailExcelListener excelListenerUtil = new PurchasePriceDetailExcelListener(skuList);
        try {
            EasyExcel.read(excelFile.getInputStream(), PurchasePriceDetailImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }
        PurchasePriceDetailDTO.ImportDTO result = new PurchasePriceDetailDTO.ImportDTO();
        //导入数据处理
        List<PurchasePriceDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();

        //导出错误数据
        List<PurchasePriceDetailImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        result.setSuccessList(successList);
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "采购价目详情错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, PurchasePriceDetailImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        result.setErrorUrl(url);
        return result;
    }


    /**
     * 批量更改禁用状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 10:03
     */
    @Override
    public Boolean updateDisabled(UpdateStateDTO.BatchUpdateDTO dto) {
        List<String> ids = dto.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<PurchasePriceDetailEntity> detailList = this.listByIds(ids);
        Boolean disabled = dto.getDisabled();
        long count = detailList.stream().filter(d -> !d.getDisabled() == disabled).count();
        if (count != detailList.size()) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        detailList.forEach(d -> d.setDisabled(disabled));

        this.updateBatchById(detailList);

        //金蝶更新分录禁用
        DmpPushTaskEntity pushTaskEntity = syncKingdeePurchasePriceService.syncDataDetailToKingdee(detailList, disabled);
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Arrays.asList(pushTaskEntity));
            }
        });
        return Boolean.TRUE;
    }


    /**
     * 查询供应商的 已有的sku信息
     *
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceDetailDTO.AddDTO>
     * @author yl
     * @date 2023-04-06 9:37
     */
    @Override
    public List<PurchasePriceDetailDTO.ViewDTO> listCheckPurchasePriceDetail(String supplierId, String purchaseOrgId, List<String> skuIdList) {
        List<String> statusList = new ArrayList<>(4);
        statusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        statusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        statusList.add(ApproveStatusEnum.APPROVE.getStatus());
        statusList.add(ApproveStatusEnum.REJECT.getStatus());
        List<PurchasePriceDetailDTO.ViewDTO> list = baseMapper.listCheckPurchasePriceDetail(supplierId, statusList, purchaseOrgId, skuIdList);
        return list;
    }

    @Override
    public List<PurchasePriceDetailDTO.AddDTO> listBySupplierId(List<String> supplierIds, List<String> detailIds, List<String> skuIdList) {
        List<String> statusList = new ArrayList<>(4);
        statusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        statusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        statusList.add(ApproveStatusEnum.APPROVE.getStatus());
        statusList.add(ApproveStatusEnum.REJECT.getStatus());
        List<PurchasePriceDetailDTO.AddDTO> list = baseMapper.listBySupplierId(supplierIds, statusList, detailIds, skuIdList);
        return list;
    }


    /**
     * 采购价目表 点击变更报价 获取到详情
     *
     * @param purchasePriceDetailIds
     * @return com.erp.model.scm.dto.PurchasePriceChangeDTO.ViewDTO
     * @author yl
     * @date 2023-04-06 12:03
     */
    @Override
    public PurchasePriceChangeDTO.ViewDTO priceChangeDetail(List<String> purchasePriceDetailIds) {
        PurchasePriceChangeDTO.ViewDTO viewDTO = new PurchasePriceChangeDTO.ViewDTO();
        List<PurchasePriceDetailDTO.ViewDTO> viewList = this.listByPurchasePriceDetailIds(purchasePriceDetailIds);
        if (CollectionUtils.isEmpty(viewList)) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND_PURCHASE_PRICE_DETAIL);
        }

        //查询价目信息
        List<String> purchasePriceIds = viewList.stream().map(PurchasePriceDetailDTO.ViewDTO::getPurchasePriceId).collect(Collectors.toList());
        List<PurchasePriceEntity> purchasePriceEntities = priceService.listByIds(purchasePriceIds);

        //校验审核状态才可以修改
        for (PurchasePriceEntity purchasePriceEntity : purchasePriceEntities) {
            String approveStatus = purchasePriceEntity.getApproveStatus().getStatus();
            if (!approveStatus.equals(ApproveStatusEnum.APPROVE.getStatus())) {
                throw new ServiceException(ApiError.ERROR_98029);
            }
        }

        //只有相同的采购组织可以批量变更报价
        long purchaseOrgCount = purchasePriceEntities.stream().map(req -> req.getPurchaseOrgId()).distinct().count();
        if (purchaseOrgCount > 1) {
            throw new ServiceException(ApiError.PURCHASE_ORG_NOT_REPEAT);
        }

        viewDTO.setPurchaseOrgId(purchasePriceEntities.get(0).getPurchaseOrgId());
        viewDTO.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //查询产品信息
        List<String> skuIds = viewList.stream().map(PurchasePriceDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);

        //查询供应商
        List<String> supplierIds = viewList.stream().map(req -> req.getSupplierId()).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierEntities = supplierService.listByIds(supplierIds);

        List<PurchasePriceChangeDetailDTO.ViewDTO> resultList = new ArrayList<>(viewList.size());
        for (PurchasePriceDetailDTO.ViewDTO item : viewList) {
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
            PurchasePriceChangeDetailDTO.ViewDTO result = new PurchasePriceChangeDetailDTO.ViewDTO();
            result.setOldCurrency(item.getCurrency());
            result.setCurrency(item.getCurrency());
            result.setCurrencySymbol(item.getCurrencySymbol());
            result.setOldTaxPrice(item.getTaxPrice());
            result.setOldTaxRate(item.getTaxRate());
            result.setOldEffectiveDate(item.getEffectiveDate());
            result.setDeliveryDay(item.getDeliveryDay());
            result.setPurchasePriceDetailId(item.getId());
            result.setMinQty(item.getMinQty());
            result.setMaxQty(item.getMaxQty());
            result.setProductName(skuVO.getSkuName());
            result.setSkuNo(item.getSkuNo());
            result.setSkuId(item.getSkuId());
            //采购价目信息
            PurchasePriceEntity purchasePriceEntity = purchasePriceEntities.stream().filter(req -> item.getPurchasePriceId().equals(req.getId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(purchasePriceEntity)) {
                throw new ServiceException(ApiError.ERROR_98024);
            }

            //供应商名称
            SupplierEntity supplierEntity = supplierEntities.stream().filter(req -> item.getSupplierId().equals(req.getId())).findFirst().orElse(new SupplierEntity());
            result.setSupplierId(purchasePriceEntity.getSupplierId());
            result.setSupplierName(supplierEntity.getName());
            result.setPriceCode(purchasePriceEntity.getCode());
            resultList.add(result);
        }
        viewDTO.setPurchasePriceChangeDetailList(resultList);
        return viewDTO;
    }


    /**
     * 根据采购价目表id 获取到采购价目变更的明细
     *
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.PurchasePriceChangeDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-04-06 18:54
     */
    @Override
    public List<PurchasePriceChangeDetailDTO.ViewDTO> listPriceChangeDetail(PurchasePriceChangeDetailDTO.SkuChangeParamDTO dto) {

        List<PurchasePriceDetailDTO.ViewDTO> list = this.listByPurchasePriceIds(dto);

        List<String> skuIds = list.stream().map(PurchasePriceDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);



        List<PurchasePriceChangeDetailDTO.ViewDTO> resultList = new ArrayList<>(list.size());
        for (PurchasePriceDetailDTO.ViewDTO item : list) {
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
            PurchasePriceChangeDetailDTO.ViewDTO result = new PurchasePriceChangeDetailDTO.ViewDTO();
            result.setMaxQty(item.getMaxQty());
            result.setMinQty(item.getMinQty());
            result.setOldTaxRate(item.getTaxRate());
            result.setOldTaxPrice(item.getTaxPrice());
            result.setOldCurrency(item.getCurrency());
            result.setSkuId(item.getSkuId());
            result.setSkuNo(item.getSkuNo());
            result.setProductName(skuVO.getSkuName());
            result.setPurchasePriceDetailId(item.getId());
            result.setSupplierId(item.getSupplierId());
            result.setSupplierName(item.getSupplierName());
            result.setPriceCode(item.getPriceCode());
            result.setEffectiveDate(item.getEffectiveDate());
            result.setDisabled(item.getDisabled());
            result.setPurchaseOrgName(item.getPurchaseOrgName());
            resultList.add(result);
        }
        return resultList;
    }

    @Override
    public void updateKingdeeDetailId(JSONArray list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Object obj : list) {
            JSONObject jsonObject = JSONUtil.parseObj(obj);
            String detailId = (String) jsonObject.get("detailId");
            String kingdeeDetailId = (String) jsonObject.get("kingdeeDetailId");
            this.lambdaUpdate()
                    .set(PurchasePriceDetailEntity::getKingdeeDetailId, kingdeeDetailId)
                    .eq(PurchasePriceDetailEntity::getId, detailId)
                    .update();
        }

    }


    /**
     * 获取根据主表id
     *
     * @param mainId
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-05-05 16:41
     */
    @Override
    public List<PurchasePriceDetailEntity> listDetailByMainId(String mainId) {
        if (StringUtils.isBlank(mainId)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(PurchasePriceDetailEntity::getPurchasePriceId, mainId).list();
    }

    @Override
    public List<PurchasePriceDetailEntity> listDetailByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(PurchasePriceDetailEntity::getPurchasePriceId, mainIds).list();
    }

    @Override
    public List<PurchasePriceDetailEntity> getBySupplierIdAndStatus(String supplierId,String purchaseOrgId, List<String> statusList) {
        return this.baseMapper.getBySupplierAndStatus(supplierId,purchaseOrgId, statusList);
    }

    @Override
    public void updateDetail(PurchasePriceDetailEntity purchasePriceDetailEntity, PurchasePriceDetailEntity old) {
        lambdaUpdate().set(PurchasePriceDetailEntity::getDeliveryDay, purchasePriceDetailEntity.getDeliveryDay())
                .set(PurchasePriceDetailEntity::getMinQty, purchasePriceDetailEntity.getMinQty())
                .set(PurchasePriceDetailEntity::getMaxQty, purchasePriceDetailEntity.getMaxQty())
                .set(PurchasePriceDetailEntity::getCurrency, purchasePriceDetailEntity.getCurrency())
                .set(PurchasePriceDetailEntity::getTaxPrice, purchasePriceDetailEntity.getTaxPrice())
                .set(PurchasePriceDetailEntity::getTaxRate, purchasePriceDetailEntity.getTaxRate())
                .set(PurchasePriceDetailEntity::getDisabled, purchasePriceDetailEntity.getDisabled())
                .eq(PurchasePriceDetailEntity::getId, purchasePriceDetailEntity.getId())
                .update();

        if (old != null) {
            moduleOperateLogService.addModuleOperateLogByObj(old, purchasePriceDetailEntity, ModuleTypeEnum.PURCHASE_PRICE.getCode(), old.getPurchasePriceId(), "", "");
        }
    }


    /**
     * 获取到删除的集合
     *
     * @param purchasePriceDetailList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-03-27 11:27
     */
    private List<String> getDeleteIds(List<PurchasePriceDetailDTO.UpdateDTO> purchasePriceDetailList, List<PurchasePriceDetailEntity> dbList) {
        List<String> ids = purchasePriceDetailList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchasePriceDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(PurchasePriceDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<PurchasePriceDetailEntity> getListByPurchasePriceId(String purchasePriceId) {
        LambdaQueryWrapper<PurchasePriceDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchasePriceDetailEntity::getPurchasePriceId, purchasePriceId);
        return this.list(queryWrapper);

    }


    private List<PurchasePriceDetailEntity> listGetListByPurchasePriceId(PurchasePriceChangeDetailDTO.SkuChangeParamDTO dto) {
        if (CollectionUtils.isEmpty(dto.getPurchasePriceIds())) {
            return Collections.emptyList();
        }
        List<PurchasePriceDetailEntity> detail = baseMapper.listGetListByPurchasePriceIds(dto);
        return detail;
    }

    @Override
    public List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> getTaxPrice(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto) {
        Pair<String, List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>> pair = listPurchaseTaxPriceView(dto);
        String error = pair.getKey();
        if (StringUtils.isNotBlank(error)) {
            throw new ServiceException(new ApiResult(1, error));
        }
        return pair.getValue();
    }


    //TODO 重写计算方法
    @Override
    public List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> batchGetTaxPrice(List<PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO> list) {
        List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> updateList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        //采购组织Id
        List<String> purchaseOrgIdList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getPurchaseOrgId())).map(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO::getPurchaseOrgId).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> companyList = sysUserFeign.getAccountingCompanyList(purchaseOrgIdList);
        if (CollectionUtils.isEmpty(companyList)){
            return Collections.emptyList();
        }

        //skuId
        List<String> skuIdList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSkuId())).map(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO::getSkuId).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);
        if (CollectionUtils.isEmpty(skuVOList)) {
            return Collections.emptyList();
        }
        //供应商Id
        List<String> supplierIdList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSupplierId())).map(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO::getSupplierId).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierEntityList = supplierService.listByIds(supplierIdList);
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(supplierEntityList)) {
            return Collections.emptyList();
        }
        //采购数量-需要根据sku进行汇总
        Map<String, Integer> skuQtyList = list.stream().filter(e -> Objects.nonNull(e) && StrUtil.isNotBlank(e.getSkuId())
                        && StrUtil.isNotBlank(e.getSupplierId()) && Objects.nonNull(e.getPurchaseQty()))
                .collect(Collectors.groupingBy(e -> e.getSkuId() + "_" + e.getSupplierId(), Collectors.summingInt(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO::getPurchaseQty)));
        List<Integer> purchaseQtyList = skuQtyList.values().stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(purchaseQtyList)) {
            return Collections.emptyList();
        }
        //查询对应采购价目
        List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> viewList = this.batchGetTaxPrice(skuIdList,supplierIdList,purchaseQtyList,purchaseOrgIdList);
        if (CollectionUtils.isEmpty(viewList)){
            //未查到结果，直接返回
            return Collections.emptyList();
        }
        for(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO updateDTO : list){
            if (StrUtil.isBlank(updateDTO.getPurchaseOrgId()) || StrUtil.isBlank(updateDTO.getSkuId()) || StrUtil.isBlank(updateDTO.getSupplierId()) || Objects.isNull(updateDTO.getPurchaseQty())){
                continue;
            }
            PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO dto = new PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO();
            //获取sku汇总数量
            Integer purchaseQty = skuQtyList.getOrDefault(updateDTO.getSkuId() + "_" + updateDTO.getSupplierId(), null);
            PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO viewDTO = viewList.stream().filter(obj -> StrUtil.isNotBlank(updateDTO.getSkuId())
                            && StrUtil.isNotBlank(obj.getSkuId()) && obj.getSkuId().equals(updateDTO.getSkuId())
                            && StrUtil.isNotBlank(obj.getSupplierId()) && StrUtil.isNotBlank(updateDTO.getSupplierId()) && obj.getSupplierId().equals(updateDTO.getSupplierId())
                            && StrUtil.isNotBlank(obj.getPurchaseOrgId()) && StrUtil.isNotBlank(updateDTO.getPurchaseOrgId()) && StrUtil.equals(obj.getPurchaseOrgId(),updateDTO.getPurchaseOrgId())
                            && Objects.nonNull(purchaseQty) && (purchaseQty >= obj.getMinQty() && obj.getMaxQty() > purchaseQty))
                    .findFirst().orElse(null);
            if (Objects.nonNull(viewDTO)){
                BeanMapperUtils.copy(viewDTO, dto);
                dto.setCurrencySymbol(CurrencyEnum.getSymbolByCode(viewDTO.getCurrency()));
                updateList.add(dto);
            }
        }
        return updateList;
    }

    @Override
    public void updateDetailRemark(List<String> ids, String remark) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        this.lambdaUpdate()
                .in(PurchasePriceDetailEntity::getId, ids)
                .set(PurchasePriceDetailEntity::getRemark, remark)
                .update(new PurchasePriceDetailEntity());
    }

    @Override
    public Boolean disabled(BaseIdsDTO.IdsDTO dto) {
        UpdateStateDTO.BatchUpdateDTO updateDTO = new UpdateStateDTO.BatchUpdateDTO();
        updateDTO.setIds(dto.getIds());
        updateDTO.setDisabled(Boolean.TRUE);
        return updateDisabled(updateDTO);
    }

    @Override
    public Boolean enable(BaseIdsDTO.IdsDTO dto) {
        UpdateStateDTO.BatchUpdateDTO updateDTO = new UpdateStateDTO.BatchUpdateDTO();
        updateDTO.setIds(dto.getIds());
        updateDTO.setDisabled(Boolean.FALSE);
        return updateDisabled(updateDTO);
    }

    @Override
    public List<PurchasePriceDetailDTO.PurchaseTaxPriceBatchViewDTO> batchGetTaxPrice(List<String> skuIdList, List<String> supplierIdList, List<Integer> purchaseQtyList, List<String> purchaseOrgIdList) {
        PurchasePriceDetailDTO.PurchaseTaxPriceBatchSearchDTO dto = new PurchasePriceDetailDTO.PurchaseTaxPriceBatchSearchDTO();
        dto.setSkuIdList(skuIdList);
        dto.setSupplierIdList(supplierIdList);
        dto.setPurchaseQtyList(purchaseQtyList);
        dto.setPurchaseOrgIdList(purchaseOrgIdList);
        if (CollectionUtils.isEmpty(dto.getPurchaseOrgIdList()) && CollectionUtils.isEmpty(dto.getSkuIdList()) && CollectionUtils.isEmpty(dto.getSupplierIdList())&& CollectionUtils.isEmpty(dto.getPurchaseQtyList())) {
            return Collections.emptyList();
        }
        return baseMapper.batchGetTaxPrice(dto);
    }


    @Override
    public Pair<String, List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO>> listPurchaseTaxPriceView(PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto) {
        List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> resultList = new ArrayList<>();

        List<ProductDetailEntity> skuList = plmTaskFeign.getByIdList(Arrays.asList(dto.getSkuId()));
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //采购价目表
        List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> list = baseMapper.getTaxPrice(dto);
        if (CollectionUtils.isNotEmpty(list)) {
            resultList.addAll(list);
        }

        //未找到报价信息
        if (CollectionUtils.isEmpty(resultList)) {
            if (StringUtils.isNotBlank(dto.getSupplierId())) {
                String error = String.format("SKU【%s】未找到数量【%s】的供应商报价信息", skuList.get(0).getSkuNo(), dto.getPurchaseQty());
                log.error(error);
                return new Pair<>(error, resultList);
            }
            return new Pair<>("", new ArrayList<>());
        }
        List<String> currencyList = resultList.stream().map(PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> viewList = sysUserFeign.listByCurrency(currencyList);
        if (CollectionUtils.isEmpty(viewList)) {
            String error = "未发现币种对应符号";
            log.error(error);
            return new Pair<>(error, resultList);
        }
        for (PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO viewDTO : resultList) {
            CurrencyDTO.ViewDTO currencyDTO = viewList.stream().filter(obj -> obj.getId().equals(viewDTO.getCurrency())).findFirst().orElse(null);
            viewDTO.setCurrencySymbol(currencyDTO.getSymbol());
        }
        return new Pair<>("", resultList);
    }

    /**
     * @param viewList
     * @description: 处理采购价目明细信息
     * @author Will
     * @date: 2023/7/17 12:12
     */
    private void handlePurchasePriceDetail(List<PurchasePriceDetailDTO.ViewDTO> viewList) {
        if (CollectionUtils.isEmpty(viewList)) {
            return;
        }
        List<String> currencyIdList = viewList.stream().map(PurchasePriceDetailDTO.ViewDTO::getCurrency).collect(Collectors.toList());
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        BigDecimal hundred = new BigDecimal("100");

        for (PurchasePriceDetailDTO.ViewDTO item : viewList) {
            //币种
            String currency = item.getCurrency();
            String currencySymbol = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("￥");
            item.setCurrencySymbol(currencySymbol);
            BigDecimal taxRate = item.getTaxRate();
            if (taxRate != null) {
                item.setTaxRate(taxRate.multiply(hundred));
            }
            Integer minQty = item.getMinQty();
            Integer maxQty = item.getMaxQty();
            if (minQty == 0 && maxQty == 0) {
                item.setMinQty(null);
                item.setMaxQty(null);
            }
        }
    }

}

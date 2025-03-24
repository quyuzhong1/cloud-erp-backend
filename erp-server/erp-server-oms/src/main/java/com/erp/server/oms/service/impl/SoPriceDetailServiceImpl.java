package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.oms.dto.SoPriceChangeDTO;
import com.erp.model.oms.dto.SoPriceChangeDetailDTO;
import com.erp.model.oms.dto.SoPriceDetailDTO;
import com.erp.model.oms.dto.excel.SoPriceDetailImportExcelDTO;
import com.erp.model.oms.entity.SoPriceDetailEntity;
import com.erp.model.oms.entity.SoPriceEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.listener.SoPriceDetailExcelListener;
import com.erp.server.oms.mapper.SoPriceDetailMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoPriceDetailService;
import com.erp.server.oms.service.SoPriceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售价目表明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-03-24
 */
@Slf4j
@Service
public class SoPriceDetailServiceImpl extends SuperServiceImpl<SoPriceDetailMapper, SoPriceDetailEntity> implements SoPriceDetailService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService moduleOperateLogService;

    @Resource
    private SoPriceService priceService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    /**
     * 添加明细
     *
     * @param SoPriceId
     * @param SoPriceDetailList
     * @return void
     * @author yl
     * @date 2023-03-24 15:02
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPriceDetail(String SoPriceId, List<SoPriceDetailDTO.AddDTO> SoPriceDetailList) {
        if (CollectionUtils.isEmpty(SoPriceDetailList)) {
            return;
        }
        List<SoPriceDetailEntity> addList = BeanMapper.copyList(SoPriceDetailList, SoPriceDetailEntity.class);
        List<String> skuIds = addList.stream().map(SoPriceDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);

       SoPriceEntity soPriceEntity = priceService.getById(SoPriceId);
        if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(soPriceEntity)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        //验证时间
        checkSoPriceDetail(soPriceEntity.getSupplierId(),soPriceEntity.getSoOrgId(),addList);
        for (SoPriceDetailEntity item : addList) {
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            if (skuVO != null) {
                item.setSkuNo(skuVO.getSkuNo());
                item.setProductName(skuVO.getSkuName());
            }
            item.setMainId(SoPriceId);
            //税率
            BigDecimal taxRate = item.getTaxRate();
            if (taxRate != null) {
                BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                item.setTaxRate(rate);
            }
            item.setCurrency(soPriceEntity.getCurrency());
            item.setPricingUserId(soPriceEntity.getPricingUserId());
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
    public void checkSoPriceDetail (String supplierId,String SoOrgId,List<SoPriceDetailEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //查询供应商信息
        List<String> skuIdList = list.stream().map(SoPriceDetailEntity::getSkuId).collect(Collectors.toList());
        List<SoPriceDetailDTO.ViewDTO> SoDetailList = listCheckSoPriceDetail(supplierId, SoOrgId, skuIdList);
        if (CollectionUtils.isNotEmpty(SoDetailList)) {
            List<String> oldIdList = list.stream().map(SoPriceDetailEntity::getId).collect(Collectors.toList());
            SoDetailList = SoDetailList.stream().filter(obj -> !oldIdList.contains(obj.getId())).collect(Collectors.toList());
        }

        for (int i = 0;i < list.size();i++) {
            SoPriceDetailEntity entity = list.get(i);
            //检验失效时间需要大于生效时间
            if (entity.getExpireDate().isBefore(entity.getEffectiveDate())) {
                throw new ServiceException(ApiError.ERROR_SO_PRICE_DATE,entity.getSkuNo());
            }
            //校验录入数据是否存在时间重叠
            for (int j = 0;j < list.size();j++) {
                SoPriceDetailEntity detailEntity = list.get(j);
                if (i == j || !StrUtil.equals(entity.getSkuId(),detailEntity.getSkuId())) {
                    continue;
                }
                //验证是否重叠
                checkOverlap(entity,detailEntity);
            }

            List<SoPriceDetailEntity> oldList = SoDetailList.stream().filter(obj -> StrUtil.equals(obj.getSkuId(), entity.getSkuId())).map(obj -> BeanMapperUtils.map(SoPriceDetailEntity.class,obj) ).collect(Collectors.toList());
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
    private void checkOverlap (SoPriceDetailEntity entity,SoPriceDetailEntity detailEntity) {
        //区间重叠时
        if (entity.getMinQty().compareTo(detailEntity.getMaxQty()) < MathUtil.ZERO
                && detailEntity.getMinQty().compareTo(entity.getMaxQty()) < MathUtil.ZERO ) {
            //时间不能重叠
            boolean overlap = LocalDateUtil.isOverlap(entity.getEffectiveDate(), entity.getExpireDate(), detailEntity.getEffectiveDate(), detailEntity.getExpireDate());
            if (overlap) {
                throw new ServiceException(ApiError.ERROR_SO_PRICE_DATE_OVERLAP,entity.getSkuNo());
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
     * @param SoPriceId
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-27 9:48
     */
    @Override
    public List<SoPriceDetailDTO.ViewDTO> getBySoPriceId(String SoPriceId) {
        List<SoPriceDetailEntity> list = this.getListBySoPriceId(SoPriceId);
        List<SoPriceDetailDTO.ViewDTO> viewList = BeanMapper.copyList(list, SoPriceDetailDTO.ViewDTO.class);
        //处理采购价目明细信息
        handleSoPriceDetail(viewList);
        return viewList;
    }

    @Override
    public List<SoPriceDetailDTO.ViewDTO> listBySoPriceIds(SoPriceChangeDetailDTO.SkuChangeParamDTO dto) {
        List<SoPriceDetailEntity> list = this.listGetListBySoPriceId(dto);

        List<SoPriceDetailDTO.ViewDTO> viewList = BeanMapper.copyList(list, SoPriceDetailDTO.ViewDTO.class);
        //处理采购价目明细信息
        handleSoPriceDetail(viewList);

        //根据id查询供应商
        List<String> supplierIds = list.stream().map(req -> req.getSupplierId()).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierEntities = supplierService.listByIds(supplierIds);

        //设置供应商名称
        for (SoPriceDetailDTO.ViewDTO viewDTO : viewList) {
            SupplierEntity supplierEntity = supplierEntities.stream().filter(req -> viewDTO.getSupplierId().equals(req.getId())).findFirst().orElse(new SupplierEntity());
            viewDTO.setSupplierName(supplierEntity.getName());
        }
        return viewList;
    }

    @Override
    public List<SoPriceDetailDTO.ViewDTO> listBySoPriceDetailIds(List<String> SoPriceDetailIds) {
        List<SoPriceDetailEntity> list = baseMapper.listDetailByIds(SoPriceDetailIds);
        List<SoPriceDetailDTO.ViewDTO> viewList = BeanMapper.copyList(list, SoPriceDetailDTO.ViewDTO.class);
        //处理采购价目明细信息
        handleSoPriceDetail(viewList);
        return viewList;
    }

    /**
     * 根据价目表id 和详情表id 集合获取对应数据
     *
     * @param SoPriceId
     * @param SoPriceDetailIds 采购价目详情表id 集合
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.UpdateDTO>
     * @author yl
     * @date 2023-03-27 9:48
     */
    @Override
    public List<SoPriceDetailDTO.ViewDTO> getPriceDetail(String SoPriceId, List<String> SoPriceDetailIds) {
        LambdaQueryWrapper<SoPriceDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoPriceDetailEntity::getMainId, SoPriceId);
        if (CollectionUtils.isNotEmpty(SoPriceDetailIds)) {
            queryWrapper.notIn(SoPriceDetailEntity::getId, SoPriceDetailIds);
        }
        List<SoPriceDetailEntity> list = this.list(queryWrapper);
        List<SoPriceDetailDTO.ViewDTO> viewList = BeanMapper.copyList(list, SoPriceDetailDTO.ViewDTO.class);
        return viewList;
    }

    /**
     * 修改产品明细
     *
     * @param SoPriceId
     * @param SoPriceDetailList
     * @return void
     * @author yl
     * @date 2023-03-27 11:18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePriceDetail(String SoPriceId, List<SoPriceDetailDTO.UpdateDTO> SoPriceDetailList) {
        if (CollectionUtils.isEmpty(SoPriceDetailList)) {
            return;
        }
        List<SoPriceDetailEntity> dbList = this.getListBySoPriceId(SoPriceId);
        //获取到删除id集合
        List<String> deleteIdList = getDeleteIds(SoPriceDetailList, dbList);
        List<SoPriceDetailEntity> removeList = dbList.stream().filter(r -> deleteIdList.contains(r.getId())).collect(Collectors.toList());


        this.removeByIds(deleteIdList);
        List<String> skuIds = SoPriceDetailList.stream().map(SoPriceDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<SoPriceDetailEntity> saveOrUpdateList = new ArrayList<>(SoPriceDetailList.size());

       SoPriceEntity soPriceEntity = priceService.getById(SoPriceId);
        if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(soPriceEntity)) {
            throw new ServiceException(ApiError.ERROR_98024);
        }
        for (SoPriceDetailDTO.UpdateDTO item : SoPriceDetailList) {
            SoPriceDetailEntity entity = new SoPriceDetailEntity();
            BeanMapper.copy(item, entity);
            String skuId = item.getSkuId();
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
            if (skuVO != null) {
                entity.setSkuNo(skuVO.getSkuNo());
            }
            entity.setMainId(SoPriceId);
            //税率
            BigDecimal taxRate = item.getTaxRate();
            if (taxRate != null) {
                BigDecimal rate = taxRate.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                entity.setTaxRate(rate);
            }
            entity.setCurrency(soPriceEntity.getCurrency());
            entity.setPricingUserId(soPriceEntity.getPricingUserId());
            saveOrUpdateList.add(entity);
        }

        //验证时间
        checkSoPriceDetail(soPriceEntity.getSupplierId(),soPriceEntity.getSoOrgId(),saveOrUpdateList);

        //这是要添加的
        List<SoPriceDetailEntity> addList = saveOrUpdateList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());

        //这是修改的
        List<SoPriceDetailEntity> updateList = saveOrUpdateList.stream().filter(c -> StringUtils.isNotBlank(c.getId())).collect(Collectors.toList());
        //这是删除
        List<Pair<String, String>> removePairList = removeList.stream().map(obj -> new Pair<>(SoPriceId, obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_PRICE.getCode(), removePairList, "编辑操作");

        //这是添加
        List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(SoPriceId, obj.getSkuNo())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_PRICE.getCode(), addPairList, "编辑操作");

        //修改的
        for (SoPriceDetailEntity update : updateList) {
            String id = update.getId();
            SoPriceDetailEntity old = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (old != null) {
                moduleOperateLogService.addModuleOperateLogByObj(old, update, ModuleTypeEnum.SO_PRICE.getCode(), SoPriceId, "", "");
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
        String path = "classpath:excel/SoPriceDetail.xlsx";
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
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
     * @return com.erp.model.scm.dto.SoPriceDetailDTO.ImportDTO
     * @author yl
     * @date 2023-03-27 16:51
     */
    @Override
    public SoPriceDetailDTO.ImportDTO importFile(MultipartFile excelFile, List<String> skuIds, HttpServletResponse response) {
        //查询所有审核通过的sku
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        SoPriceDetailExcelListener excelListenerUtil = new SoPriceDetailExcelListener(skuList);
        try {
            EasyExcel.read(excelFile.getInputStream(), SoPriceDetailImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        }
        SoPriceDetailDTO.ImportDTO result = new SoPriceDetailDTO.ImportDTO();
        //导入数据处理
        List<SoPriceDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();

        //导出错误数据
        List<SoPriceDetailImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        result.setSuccessList(successList);
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "采购价目详情错误.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SoPriceDetailImportExcelDTO.class);
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDisabled(UpdateStateDTO.BatchUpdateDTO dto) {
        List<String> ids = dto.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<SoPriceDetailEntity> detailList = this.listByIds(ids);
        Boolean disabled = dto.getDisabled();
        long count = detailList.stream().filter(d -> !d.getDisabled() == disabled).count();
        if (count != detailList.size()) {
            throw new ServiceException(ApiError.ERROR_98027);
        }
        detailList.forEach(d -> d.setDisabled(disabled));

        this.updateBatchById(detailList);

        return Boolean.TRUE;
    }


    /**
     * 查询供应商的 已有的sku信息
     *
     * @param supplierId
     * @return java.util.List<com.erp.model.scm.dto.SoPriceDetailDTO.AddDTO>
     * @author yl
     * @date 2023-04-06 9:37
     */
    @Override
    public List<SoPriceDetailDTO.ViewDTO> listCheckSoPriceDetail(String supplierId, String SoOrgId, List<String> skuIdList) {
        List<String> statusList = new ArrayList<>(4);
        statusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        statusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        statusList.add(ApproveStatusEnum.APPROVE.getStatus());
        statusList.add(ApproveStatusEnum.REJECT.getStatus());
        List<SoPriceDetailDTO.ViewDTO> list = baseMapper.listCheckSoPriceDetail(supplierId, statusList, SoOrgId, skuIdList);
        return list;
    }

    @Override
    public List<SoPriceDetailDTO.AddDTO> listBySupplierId(List<String> supplierIds, List<String> detailIds, List<String> skuIdList) {
        List<String> statusList = new ArrayList<>(4);
        statusList.add(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        statusList.add(ApproveStatusEnum.APPROVE_ING.getStatus());
        statusList.add(ApproveStatusEnum.APPROVE.getStatus());
        statusList.add(ApproveStatusEnum.REJECT.getStatus());
        List<SoPriceDetailDTO.AddDTO> list = baseMapper.listBySupplierId(supplierIds, statusList, detailIds, skuIdList);
        return list;
    }


    /**
     * 采购价目表 点击变更报价 获取到详情
     *
     * @param SoPriceDetailIds
     * @return com.erp.model.scm.dto.SoPriceChangeDTO.ViewDTO
     * @author yl
     * @date 2023-04-06 12:03
     */
    @Override
    public SoPriceChangeDTO.ViewDTO priceChangeDetail(List<String> SoPriceDetailIds) {
        SoPriceChangeDTO.ViewDTO viewDTO = new SoPriceChangeDTO.ViewDTO();
        List<SoPriceDetailDTO.ViewDTO> viewList = this.listBySoPriceDetailIds(SoPriceDetailIds);
        if (CollectionUtils.isEmpty(viewList)) {
            throw new ServiceException(ApiError.ERROR_NOT_FOUND_SO_PRICE_DETAIL);
        }

        //查询价目信息
        List<String> SoPriceIds = viewList.stream().map(SoPriceDetailDTO.ViewDTO::getSoPriceId).collect(Collectors.toList());
        List<SoPriceEntity> SoPriceEntities = priceService.listByIds(SoPriceIds);

        //校验审核状态才可以修改
        for (SoPriceEntity soPriceEntity : SoPriceEntities) {
            String approveStatus = soPriceEntity.getApproveStatus().getStatus();
            if (!approveStatus.equals(ApproveStatusEnum.APPROVE.getStatus())) {
                throw new ServiceException(ApiError.ERROR_98029);
            }
        }

        //只有相同的采购组织可以批量变更报价
        long SoOrgCount = SoPriceEntities.stream().map(req -> req.getSoOrgId()).distinct().count();
        if (SoOrgCount > 1) {
            throw new ServiceException(ApiError.So_ORG_NOT_REPEAT);
        }

        viewDTO.setSoOrgId(SoPriceEntities.get(0).getSoOrgId());
        viewDTO.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //查询产品信息
        List<String> skuIds = viewList.stream().map(SoPriceDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);

        //查询供应商
        List<String> supplierIds = viewList.stream().map(req -> req.getSupplierId()).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierEntities = supplierService.listByIds(supplierIds);

        List<SoPriceChangeDetailDTO.ViewDTO> resultList = new ArrayList<>(viewList.size());
        for (SoPriceDetailDTO.ViewDTO item : viewList) {
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
            SoPriceChangeDetailDTO.ViewDTO result = new SoPriceChangeDetailDTO.ViewDTO();
            result.setOldCurrency(item.getCurrency());
            result.setCurrency(item.getCurrency());
            result.setCurrencySymbol(item.getCurrencySymbol());
            result.setOldTaxPrice(item.getTaxPrice());
            result.setOldTaxRate(item.getTaxRate());
            result.setOldEffectiveDate(item.getEffectiveDate());
            result.setDeliveryDay(item.getDeliveryDay());
            result.setSoPriceDetailId(item.getId());
            result.setMinQty(item.getMinQty());
            result.setMaxQty(item.getMaxQty());
            result.setProductName(skuVO.getSkuName());
            result.setSkuNo(item.getSkuNo());
            result.setSkuId(item.getSkuId());
            //采购价目信息
           SoPriceEntity soPriceEntity = SoPriceEntities.stream().filter(req -> item.getSoPriceId().equals(req.getId())).findFirst().orElse(null);
            if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(soPriceEntity)) {
                throw new ServiceException(ApiError.ERROR_98024);
            }

            //供应商名称
            SupplierEntity supplierEntity = supplierEntities.stream().filter(req -> item.getSupplierId().equals(req.getId())).findFirst().orElse(new SupplierEntity());
            result.setSupplierId(soPriceEntity.getSupplierId());
            result.setSupplierName(supplierEntity.getName());
            result.setPriceCode(soPriceEntity.getCode());
            resultList.add(result);
        }
        viewDTO.setSoPriceChangeDetailList(resultList);
        return viewDTO;
    }


    /**
     * 根据采购价目表id 获取到采购价目变更的明细
     *
     * @param dto
     * @return java.util.List<com.erp.model.scm.dto.SoPriceChangeDetailDTO.ViewDTO>
     * @author yl
     * @date 2023-04-06 18:54
     */
    @Override
    public List<SoPriceChangeDetailDTO.ViewDTO> listPriceChangeDetail(SoPriceChangeDetailDTO.SkuChangeParamDTO dto) {

        List<SoPriceDetailDTO.ViewDTO> list = this.listBySoPriceIds(dto);

        List<String> skuIds = list.stream().map(SoPriceDetailDTO.ViewDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);



        List<SoPriceChangeDetailDTO.ViewDTO> resultList = new ArrayList<>(list.size());
        for (SoPriceDetailDTO.ViewDTO item : list) {
            SkuVO skuVO = skuList.stream().filter(s -> s.getSkuId().equals(item.getSkuId())).findFirst().orElse(new SkuVO());
            SoPriceChangeDetailDTO.ViewDTO result = new SoPriceChangeDetailDTO.ViewDTO();
            result.setMaxQty(item.getMaxQty());
            result.setMinQty(item.getMinQty());
            result.setOldTaxRate(item.getTaxRate());
            result.setOldTaxPrice(item.getTaxPrice());
            result.setOldCurrency(item.getCurrency());
            result.setSkuId(item.getSkuId());
            result.setSkuNo(item.getSkuNo());
            result.setProductName(skuVO.getSkuName());
            result.setSoPriceDetailId(item.getId());
            result.setSupplierId(item.getSupplierId());
            result.setSupplierName(item.getSupplierName());
            result.setPriceCode(item.getPriceCode());
            result.setEffectiveDate(item.getEffectiveDate());
            result.setDisabled(item.getDisabled());
            result.setSoOrgName(item.getSoOrgName());
            resultList.add(result);
        }
        return resultList;
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
    public List<SoPriceDetailEntity> listDetailByMainId(String mainId) {
        if (StringUtils.isBlank(mainId)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().eq(SoPriceDetailEntity::getMainId, mainId).list();
    }

    @Override
    public List<SoPriceDetailEntity> listDetailByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(SoPriceDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public List<SoPriceDetailEntity> getBySupplierIdAndStatus(String supplierId,String SoOrgId, List<String> statusList) {
        return this.baseMapper.getBySupplierAndStatus(supplierId,SoOrgId, statusList);
    }

    @Override
    public void updateDetail(SoPriceDetailEntity soPriceDetailEntity, SoPriceDetailEntity old) {
        lambdaUpdate().set(SoPriceDetailEntity::getDeliveryDay, soPriceDetailEntity.getDeliveryDay())
                .set(SoPriceDetailEntity::getMinQty, soPriceDetailEntity.getMinQty())
                .set(SoPriceDetailEntity::getMaxQty, soPriceDetailEntity.getMaxQty())
                .set(SoPriceDetailEntity::getCurrency, soPriceDetailEntity.getCurrency())
                .set(SoPriceDetailEntity::getTaxPrice, soPriceDetailEntity.getTaxPrice())
                .set(SoPriceDetailEntity::getTaxRate, soPriceDetailEntity.getTaxRate())
                .set(SoPriceDetailEntity::getDisabled, soPriceDetailEntity.getDisabled())
                .eq(SoPriceDetailEntity::getId, soPriceDetailEntity.getId())
                .update();

        if (old != null) {
            moduleOperateLogService.addModuleOperateLogByObj(old, soPriceDetailEntity, ModuleTypeEnum.SO_PRICE.getCode(), old.getMainId(), "", "");
        }
    }


    /**
     * 获取到删除的集合
     *
     * @param SoPriceDetailList
     * @param dbList
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2023-03-27 11:27
     */
    private List<String> getDeleteIds(List<SoPriceDetailDTO.UpdateDTO> SoPriceDetailList, List<SoPriceDetailEntity> dbList) {
        List<String> ids = SoPriceDetailList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SoPriceDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> dbIds = dbList.stream().map(SoPriceDetailEntity::getId).collect(Collectors.toList());
        return dbIds.stream().filter(s -> !ids.contains(s)).collect(Collectors.toList());
    }


    private List<SoPriceDetailEntity> getListBySoPriceId(String SoPriceId) {
        LambdaQueryWrapper<SoPriceDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SoPriceDetailEntity::getMainId, SoPriceId);
        queryWrapper.orderByDesc(SoPriceDetailEntity::getId);
        return this.list(queryWrapper);

    }


    private List<SoPriceDetailEntity> listGetListBySoPriceId(SoPriceChangeDetailDTO.SkuChangeParamDTO dto) {
        if (CollectionUtils.isEmpty(dto.getSoPriceIds())) {
            return Collections.emptyList();
        }
        List<SoPriceDetailEntity> detail = baseMapper.listGetListBySoPriceIds(dto);
        return detail;
    }

    @Override
    public List<SoPriceDetailDTO.SoTaxPriceViewDTO> getTaxPrice(SoPriceDetailDTO.SoTaxPriceSearchDTO dto) {
        Pair<String, List<SoPriceDetailDTO.SoTaxPriceViewDTO>> pair = listSoTaxPriceView(dto);
        String error = pair.getKey();
        if (StringUtils.isNotBlank(error)) {
            throw new ServiceException(new ApiResult(1, error));
        }
        return pair.getValue();
    }

    @Override
    public List<SoPriceDetailDTO.SoTaxPriceBatchViewDTO> batchGetTaxPrice(List<SoPriceDetailDTO.SoTaxPriceSearchDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        //skuId
        List<String> skuIdList = list.stream().map(SoPriceDetailDTO.SoTaxPriceSearchDTO::getSkuId).distinct().collect(Collectors.toList());
        //供应商Id
        List<String> supplierIdList = list.stream().map(SoPriceDetailDTO.SoTaxPriceSearchDTO::getSupplierId).distinct().collect(Collectors.toList());
        //采购数量
        List<Integer> SoQtyList = list.stream().map(SoPriceDetailDTO.SoTaxPriceSearchDTO::getSoQty).distinct().collect(Collectors.toList());
        //采购组织Id
        List<String> SoOrgIdList = list.stream().map(SoPriceDetailDTO.SoTaxPriceSearchDTO::getSoOrgId).distinct().collect(Collectors.toList());


        SoPriceDetailDTO.SoTaxPriceBatchSearchDTO dto = new SoPriceDetailDTO.SoTaxPriceBatchSearchDTO();
        dto.setSkuIdList(skuIdList);
        dto.setSupplierIdList(supplierIdList);
        dto.setSoQtyList(SoQtyList);
        dto.setSoOrgIdList(SoOrgIdList);
        //报价信息
        List<SoPriceDetailDTO.SoTaxPriceBatchViewDTO> viewList = baseMapper.batchGetTaxPrice(dto);
        //币种信息
        List<String> currencyList = viewList.stream().map(SoPriceDetailDTO.SoTaxPriceViewDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);


        List<SoPriceDetailDTO.SoTaxPriceBatchViewDTO> resultList = new ArrayList<>();
        for (SoPriceDetailDTO.SoTaxPriceSearchDTO searchDTO : list) {
            SoPriceDetailDTO.SoTaxPriceBatchViewDTO SoTaxPriceViewDTO = new SoPriceDetailDTO.SoTaxPriceBatchViewDTO();
            if (CollectionUtils.isNotEmpty(viewList)) {
                SoPriceDetailDTO.SoTaxPriceBatchViewDTO viewDTO = viewList.stream().filter(obj -> obj.getSkuId().equals(searchDTO.getSkuId())
                                && obj.getSupplierId().equals(searchDTO.getSupplierId())
                                && StrUtil.equals(obj.getSoOrgId(),searchDTO.getSoOrgId())
                                && (searchDTO.getSoQty() >= obj.getMinQty() && obj.getMaxQty() > searchDTO.getSoQty()))
                        .findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(viewDTO)) {
                    BeanMapperUtils.copy(viewDTO, SoTaxPriceViewDTO);
                    //币种符号
                    if (CollectionUtils.isNotEmpty(currencyViewList)) {
                        CurrencyDTO.ViewDTO currencyDTO = currencyViewList.stream().filter(obj -> obj.getId().equals(viewDTO.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
                        SoTaxPriceViewDTO.setCurrencySymbol(currencyDTO.getSymbol());
                    }
                }
            }
            SoTaxPriceViewDTO.setSkuId(searchDTO.getSkuId());
            SoTaxPriceViewDTO.setSupplierId(searchDTO.getSupplierId());
            SoTaxPriceViewDTO.setSoQty(searchDTO.getSoQty());
            resultList.add(SoTaxPriceViewDTO);
        }
        return resultList;
    }

    @Override
    public void updateDetailRemark(List<String> ids, String remark) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        this.lambdaUpdate()
                .in(SoPriceDetailEntity::getId, ids)
                .set(SoPriceDetailEntity::getRemark, remark)
                .update(new SoPriceDetailEntity());
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
    public List<SoPriceDetailDTO.SoTaxPriceBatchViewDTO> batchGetTaxPrice(List<String> skuIdList, List<String> supplierIdList, List<Integer> SoQtyList, List<String> SoOrgIdList) {
        SoPriceDetailDTO.SoTaxPriceBatchSearchDTO dto = new SoPriceDetailDTO.SoTaxPriceBatchSearchDTO();
        dto.setSkuIdList(skuIdList);
        dto.setSupplierIdList(supplierIdList);
        dto.setSoQtyList(SoQtyList);
        dto.setSoOrgIdList(SoOrgIdList);
        if (CollectionUtils.isEmpty(dto.getSoOrgIdList()) && CollectionUtils.isEmpty(dto.getSkuIdList()) && CollectionUtils.isEmpty(dto.getSupplierIdList())&& CollectionUtils.isEmpty(dto.getSoQtyList())) {
            return Collections.emptyList();
        }
        return baseMapper.batchGetTaxPrice(dto);
    }


    @Override
    public Pair<String, List<SoPriceDetailDTO.SoTaxPriceViewDTO>> listSoTaxPriceView(SoPriceDetailDTO.SoTaxPriceSearchDTO dto) {
        List<SoPriceDetailDTO.SoTaxPriceViewDTO> resultList = new ArrayList<>();

        List<ProductDetailEntity> skuList = plmTaskFeign.getByIdList(Arrays.asList(dto.getSkuId()));
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //采购价目表
        List<SoPriceDetailDTO.SoTaxPriceViewDTO> list = baseMapper.getTaxPrice(dto);
        if (CollectionUtils.isNotEmpty(list)) {
            resultList.addAll(list);
        }

        //未找到报价信息
        if (CollectionUtils.isEmpty(resultList)) {
            if (StringUtils.isNotBlank(dto.getSupplierId())) {
                String error = String.format("SKU【%s】未找到数量【%s】的供应商报价信息", skuList.get(0).getSkuNo(), dto.getSoQty());
                log.error(error);
                return new Pair<>(error, resultList);
            }
            return new Pair<>("", new ArrayList<>());
        }
        List<String> currencyList = resultList.stream().map(SoPriceDetailDTO.SoTaxPriceViewDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> viewList = sysUserFeign.listByCurrency(currencyList);
        if (CollectionUtils.isEmpty(viewList)) {
            String error = "未发现币种对应符号";
            log.error(error);
            return new Pair<>(error, resultList);
        }
        for (SoPriceDetailDTO.SoTaxPriceViewDTO viewDTO : resultList) {
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
    private void handleSoPriceDetail(List<SoPriceDetailDTO.ViewDTO> viewList) {
        if (CollectionUtils.isEmpty(viewList)) {
            return;
        }
        List<String> currencyIdList = viewList.stream().map(SoPriceDetailDTO.ViewDTO::getCurrency).collect(Collectors.toList());
        //币种信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIdList);
        BigDecimal hundred = new BigDecimal("100");

        for (SoPriceDetailDTO.ViewDTO item : viewList) {
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

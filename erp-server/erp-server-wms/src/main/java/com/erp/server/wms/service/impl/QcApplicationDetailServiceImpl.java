package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.excel.PurchaseOrderImportExcelDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.QcApplicationDetailDTO;
import com.erp.model.wms.dto.excel.QcApplicationImportExcelDTO;
import com.erp.model.wms.entity.QcApplicationDetailEntity;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.listener.QcApplicationExcelListener;
import com.erp.server.wms.mapper.QcApplicationDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcApplicationDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>
 * 质检申请单明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
@Slf4j
@Service
public class QcApplicationDetailServiceImpl extends SuperServiceImpl<QcApplicationDetailMapper, QcApplicationDetailEntity> implements QcApplicationDetailService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private FileFeign fileFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(List<QcApplicationDetailDTO.AddDTO> detailList, String mainId,String sourceId) {
        List<QcApplicationDetailEntity> qcApplicationDetailList = BeanUtil.copyToList(detailList, QcApplicationDetailEntity.class);

        // 数据处理
        handleData(qcApplicationDetailList,mainId,sourceId);

        log.info("开始新增质检申请单明细单");
        boolean save = super.saveBatch(qcApplicationDetailList);
        if(!save) {
            throw new ServiceException("质检申请单明细单保存失败");
        }
        return Boolean.TRUE;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<QcApplicationDetailDTO.UpdateDTO> detailList, String mainId,String sourceId) {
        if (CollUtil.isNotEmpty(detailList)) {
            throw new ServiceException(ApiError.QC_APPLICATION_DETAIL_NOT_EXIST);
        }
        List<QcApplicationDetailEntity> qcApplicationDetailList = BeanUtil.copyToList(detailList, QcApplicationDetailEntity.class);

        //原明细数据
        List<QcApplicationDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<QcApplicationDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.QC_APPLICATION.getCode(),pairList,"编辑操作");
            List<QcApplicationDetailEntity> list = lambdaQuery().in(QcApplicationDetailEntity::getMainId, deleteIds).list();
            list.forEach(req -> req.setIsDeleted(Boolean.TRUE));
            this.removeByIds(deleteIds);
        }

        // 数据处理
        handleData(qcApplicationDetailList,mainId,sourceId);
        log.info("编辑 开始修改质检申请单明细单数据，id：【{}】", mainId);
        boolean save = super.updateBatchById(qcApplicationDetailList);
        if(!save) {
            throw new ServiceException("质检申请单明细单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public QcApplicationDetailDTO.ImportDTO importExcel(QcApplicationDetailDTO.ImportParamDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());

        QcApplicationExcelListener excelListenerUtil = new QcApplicationExcelListener();

        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes),  PurchaseOrderImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        //验证导入数据是否为空
        List<QcApplicationImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.FILE_DATA_REQUIRED);
        }
        QcApplicationDetailDTO.ImportDTO importDTO = new QcApplicationDetailDTO.ImportDTO();
        //导入数据处理
        List<QcApplicationImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<QcApplicationImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理数据
        List<QcApplicationDetailDTO.ImportResultDTO> importResultList = handleImportSuccessList(successList, errorList,dto.getSourceId());

        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "质检申请单错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, QcApplicationImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(importResultList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    @Override
    public List<QcApplicationDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(QcApplicationDetailEntity::getMainId, mainId).list();
    }


    @Override
    public List<QcApplicationDetailDTO.ImportResultDTO> handleImportSuccessList(List<QcApplicationImportExcelDTO> successList, List<QcApplicationImportExcelDTO> errorList,String sourceId) {
        if (CollectionUtils.isEmpty(successList)) {
            return Collections.emptyList();
        }
        List<QcApplicationDetailDTO.ImportResultDTO> resultList = new ArrayList<>();

        List<PurchaseOrderDetailEntity> detailList = new ArrayList<>();
        if (CharSequenceUtil.isNotBlank(sourceId)) {
            //查询采购订单数据
             detailList = FeignQuery.create(PurchaseOrderDetailEntity.class).eq(PurchaseOrderDetailEntity::getPurchaseOrderId, sourceId).list();
        }

        //SKU信息
        List<String> skuNoList = successList.stream().map(QcApplicationImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuPurchaseBySkuNos(skuNoList);
        Map<String, SkuVO> skuMap = CollUtil.isEmpty(skuVOS) ? new HashMap<>() : skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuNo, obj -> obj));

        for ( QcApplicationImportExcelDTO data : successList) {
            QcApplicationDetailDTO.ImportResultDTO resultDTO = new QcApplicationDetailDTO.ImportResultDTO();
            BeanUtil.copyProperties(data, resultDTO);

            PurchaseOrderDetailEntity purchaseOrderDetailEntity = detailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), data.getSkuNo())).findFirst().orElse(null);
            if ( ObjectUtils.isEmpty(purchaseOrderDetailEntity)) {
                data.setErrorMsg("采购订单中不存在该SKU");
                errorList.add(data);
                continue;
            }
            //sku信息
            SkuVO skuVO = skuMap.get(data.getSkuNo());
            if (ObjectUtil.isEmpty(skuVO)) {
                data.setErrorMsg("未找到SKU信息");
                errorList.add(data);
                continue;
            }
            if ( !ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(skuVO.getStatus()))  {

            }

            resultDTO.setProductName(skuVO.getSkuName());
            resultDTO.setEan(skuVO.getEan());
            resultDTO.setSourceDetailId(purchaseOrderDetailEntity.getId());



            resultList.add(resultDTO);
        }

        return resultList;
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<QcApplicationDetailDTO.UpdateDTO> newList, List<QcApplicationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(QcApplicationDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(QcApplicationDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(List<QcApplicationDetailEntity> qcApplicationDetailList,String mainId,String sourceId) {
        if (CollUtil.isEmpty(qcApplicationDetailList)) {
            return;
        }

        //添加操作日志
        List<QcApplicationDetailEntity> addList = qcApplicationDetailList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //新增不需要添加新增SKU的日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.QC_APPLICATION.getCode(), addPairList, "编辑操作");
        }

        //sku信息
        List<String> skuIdList = qcApplicationDetailList.stream().map(QcApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);
        Map<String, String> skuMap = CollUtil.isEmpty(skuList) ? new HashMap<>() : skuList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getSkuNo));

        //查询来源采购订单数据
        PurchaseOrderSupplierEntity poSupplierEntity = CharSequenceUtil.isBlank(sourceId) ? new PurchaseOrderSupplierEntity() : FeignQuery.getById(PurchaseOrderSupplierEntity.class, sourceId);

        for (QcApplicationDetailEntity data : qcApplicationDetailList) {
               //校验供应商信息
                if (CharSequenceUtil.isNotBlank(data.getSupplierId()) && !CharSequenceUtil.equals(data.getSupplierId(),poSupplierEntity.getSupplierId())) {
                    throw new ServiceException(ApiError.QC_APPLICATION_SUPPLIER_NOT_DIFF);
                }
                //sku编码
                data.setSkuNo(skuMap.get(data.getSkuId()));

                //校验申请质检数量 TODO


            //操作日志
            if (StringUtils.isNotBlank(data.getId())) {
                QcApplicationDetailEntity old = this.getById(data.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.QC_APPLICATION_NOT_EXIST);
                }
                operateLogService.addModuleOperateLogByObj(old,data, ModuleTypeEnum.QC_APPLICATION.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }


    }

   /**
    * 分页查询、导出 数据处理
   */
   private void fillList(List<QcApplicationDetailDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(QcApplicationDetailDTO.ListDTO data : list) {
        // TODO 其他如需要显示名称的字段赋值
        }
   }
}

package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.QcProductDTO;
import com.erp.model.wms.dto.QcProductLogDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.entity.QcProductEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.mapper.QcProductMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.QcProductService;
import com.erp.server.wms.service.WmsAttachmentService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Service
public class QcProductServiceImpl extends SuperServiceImpl<QcProductMapper, QcProductEntity> implements QcProductService {

    @Resource
    private WmsAttachmentService wmsAttachmentService;


    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private OperateLogService operateLogService;

    /**
     * 质检产品信息 暂存
     *
     * @param billId
     * @param qcProduct
     * @return void
     * @author yl
     * @date 2023-04-19 9:45
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void add(String billId, QcProductDTO.AddDTO qcProduct,String skuId) {
        if(CharSequenceUtil.isBlank(skuId)){
            throw new ServiceException(ApiError.ERROR_95107);
        }
        QcProductEntity qcProductEntity = new QcProductEntity();
        BeanMapper.copy(qcProduct, qcProductEntity);
        QcProductEntity oldEntity = null;
        String id = qcProduct.getId();
        if (CharSequenceUtil.isBlank(id)) {
            id = IdWorker.getIdStr();
        }else{
            oldEntity = getById(id);
        }
        qcProductEntity.setMainId(billId);
        qcProductEntity.setSkuId(skuId);
        qcProductEntity.setId(id);

        List<String> skuIdList = new ArrayList<>();
        skuIdList.add(skuId);
        if (!Objects.isNull(oldEntity)) {
            skuIdList.add(oldEntity.getSkuId());
        }
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(skuIdList);

        SkuVO skuVO = skuVOList.stream().filter(s -> s.getSkuId().equals(skuId)).findFirst().orElse(null);
        if(skuVO!=null){
            qcProductEntity.setProductGrade(skuVO.getProductGrade());
            qcProductEntity.setVariantProperty(skuVO.getVariantProperty());
            qcProductEntity.setSkuNo(skuVO.getSkuNo());
        }
        //产品信息
        List<String> productImageNameList = qcProduct.getProductImageNameList();
        List<String> productImageUrlList = qcProduct.getProductImageUrlList();
        wmsAttachmentService.batchSave(productImageUrlList, productImageNameList, WmsConstant.QC_PRODUCT, id);
        //外箱信息
        List<String> boxImageNameList = qcProduct.getBoxImageNameList();
        List<String> boxImageUrlList = qcProduct.getBoxImageUrlList();
        wmsAttachmentService.batchSave(boxImageUrlList, boxImageNameList, WmsConstant.QC_BOX, id);
        this.saveOrUpdate(qcProductEntity);

        //标记SKU
        plmTaskFeign.updateOccupyStatus(Collections.singletonList(qcProductEntity.getSkuId()));

        //操作日志
        if (Objects.isNull(oldEntity)) {
            operateLogService.addModuleOperateLog("新增了质检单的产品信息", ModuleTypeEnum.QC_ORDER.getCode(),billId , "新增操作");
        }else {
            QcProductLogDTO oldQcProductLogDTO = new QcProductLogDTO();
            QcProductLogDTO newQcProductLogDTO = new QcProductLogDTO();
            BeanMapper.copy(oldEntity, oldQcProductLogDTO);
            BeanMapper.copy(qcProductEntity, newQcProductLogDTO);

            SkuVO oldSkuVO = skuVOList.stream().filter(s -> s.getSkuId().equals(oldQcProductLogDTO.getSkuId())).findFirst().orElse(null);
            if(oldSkuVO!=null){
                oldQcProductLogDTO.setSkuName(oldSkuVO.getSkuName());
            }
            SkuVO newSkuVO = skuVOList.stream().filter(s -> s.getSkuId().equals(newQcProductLogDTO.getSkuId())).findFirst().orElse(null);
            if(newSkuVO!=null){
                newQcProductLogDTO.setSkuName(newSkuVO.getSkuName());
            }
            operateLogService.addModuleOperateLogByObj(oldQcProductLogDTO, newQcProductLogDTO, ModuleTypeEnum.QC_ORDER.getCode(), billId, "","编辑了质检单的产品信息");
        }
    }


    /**
     * 根据质检单 获取 质检产品信息
     *
     * @param billId
     * @return com.erp.model.wms.dto.QcProductDTO.ViewDTO
     * @author yl
     * @date 2023-04-19 12:01
     */
    @Override
    public QcProductDTO.ViewDTO getByMainId(String billId) {
        QcProductEntity product = this.getByBillId(billId);
        QcProductDTO.ViewDTO productView = new QcProductDTO.ViewDTO();
        if (product != null) {
            BeanMapper.copy(product, productView);
            List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(Collections.singletonList(product.getSkuId()));
            List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(Collections.singletonList(product.getId()));
            List<String> boxImageUrlList = attachmentList.stream().filter(b -> b.getType().equals(WmsConstant.QC_BOX)).map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            List<String> boxNameList = attachmentList.stream().filter(b -> b.getType().equals(WmsConstant.QC_BOX)).map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());

            List<String> productImageUrlList = attachmentList.stream().filter(b -> b.getType().equals(WmsConstant.QC_PRODUCT)).map(WmsAttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList());
            List<String> productNameList = attachmentList.stream().filter(b -> b.getType().equals(WmsConstant.QC_PRODUCT)).map(WmsAttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList());
            productView.setBoxImageNameList(boxNameList);
            productView.setBoxImageUrlList(boxImageUrlList);

            productView.setProductImageNameList(productNameList);
            productView.setProductImageUrlList(productImageUrlList);
            SkuVO skuVO = skuVOList.stream().filter(s -> s.getSkuId().equals(productView.getSkuId())).findFirst().orElse(null);
            if(skuVO!=null){
                productView.setProductGrade(skuVO.getProductGrade());
                productView.setProductName(skuVO.getSkuName());
                productView.setSkuNo(skuVO.getSkuNo());
                productView.setVariantProperty(skuVO.getVariantProperty());
            }

        }

        return productView;
    }


    /**
     * 获取到质检产品信息
     *
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.QcProductEntity>
     * @author yl
     * @date 2023-04-20 16:26
     */
    @Override
    public List<QcProductEntity> getByMainIdList(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(QcProductEntity::getMainId, mainIds).list();
    }

    /**
     * 根据质检单id 集合 获取删除数据
     *
     * @param mainIdList
     * @return void
     * @author yl
     * @date 2023-04-25 16:15
     */
    @Override
    public void removeByMainIds(List<String> mainIdList) {
        if (CollectionUtils.isEmpty(mainIdList)) {
            LambdaQueryWrapper<QcProductEntity> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.in(QcProductEntity::getMainId, mainIdList);
            this.remove(queryWrapper);
        }
    }


    private QcProductEntity getByBillId(String billId) {
        LambdaQueryWrapper<QcProductEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QcProductEntity::getMainId, billId);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);

    }
}


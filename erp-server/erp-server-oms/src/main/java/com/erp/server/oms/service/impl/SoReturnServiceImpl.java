package com.erp.server.oms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.dto.SoReturnDetailDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.oms.enums.SOReturnChangeListTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoReturnNoticeEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.SoReturnNoticeFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.oms.mapper.SoReturnMapper;
import com.erp.server.oms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  退货单服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnServiceImpl extends SuperServiceImpl<SoReturnMapper, SoReturnEntity> implements SoReturnService {

    @Resource
    private SoReturnDetailService soReturnDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CommonService commonService;

    @Resource
    private SoReturnNoticeFeign soReturnNoticeFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    @Override
    public PagingVO<SoReturnDTO.PagingView> paging(PagingDTO<SoReturnDTO.PagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoReturnDTO.PagingView> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        //明细数据
        List<SoReturnDTO.PagingView> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(SoReturnDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        List<String> detailIds = records.stream().map(SoReturnDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.list();
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySourceDetailId(detailIds);
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(detailIds);
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setSourceCode(null);
                    obj.setType(null);
                    obj.setApproveStatusName(null);
                    obj.setApproveStatus(null);
                    obj.setInvalidStatusName(null);
                    obj.setInvalidStatus(null);
                    obj.setCustomerName(null);
                    obj.setSalesOrgName(null);
                    obj.setSellerName(null);
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus().getStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                obj.setTypeName(BillTypeEnum.getName(obj.getType()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_95107);
                }
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(soDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_99006);
                }
                obj.setProductName(productDetailEntity.getName());
                obj.setSalesQty(soDetailEntity.getQty());
                Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSourceDetailId().equals(obj.getSourceDetailId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
                obj.setDeliveryQty(actualQty);
                obj.setUnDeliveryQty(soDetailEntity.getQty() - actualQty);
                obj.setUnit(productDetailEntity.getUnitName());
                obj.setSalesAmount(soDetailEntity.getAmount());
                CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
                obj.setCustomerName(customerInfoEntity.getName());
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoReturnDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        SOReturnChangeListTypeEnum[] values = SOReturnChangeListTypeEnum.values();
        List<SoReturnDTO.StatusCountDTO> list = new ArrayList<>();
        for (SOReturnChangeListTypeEnum item : values) {
            SoReturnDTO.PagingParam pagingParam = new SoReturnDTO.PagingParam();
            pagingParam.setParam(dto.getParam());
            SoReturnDTO.StatusCountDTO resultDTO = new SoReturnDTO.StatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (SOReturnChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (SOReturnChangeListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            if (SOReturnChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                pagingParam.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(pagingParam);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public String add(SoReturnDTO.Add dto) {
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoService.getById(dto.getSourceId());
        SoReturnEntity soReturnEntity = new SoReturnEntity();
        BeanMapperUtils.copy(soInfoEntity, soReturnEntity);

        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.list();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        soReturnEntity.setCustomerName(customerInfoEntity.getName());
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.THDD, BusinessNoTypeEnum.CODE_THDD.getCode()));
        soReturnEntity.setCode(code);
        soReturnEntity.setSourceId(dto.getSourceId());
        soReturnEntity.setSourceCode(soInfoEntity.getCode());
        soReturnEntity.setSourceType(dto.getSourceType());
        soReturnEntity.setBillDate(dto.getBillDate());
        soReturnEntity.setReceiverName(dto.getReceiverName());
        soReturnEntity.setTelNumber(dto.getTelNumber());
        soReturnEntity.setAddressTypeDict(dto.getReceiveAddressDict());
        soReturnEntity.setDeliveryModeDict(dto.getDeliveryModeDict());
        soReturnEntity.setCurrency(dto.getCurrency());
        soReturnEntity.setCurrencySymbol(dto.getCurrencySymbol());
        soReturnEntity.setIsTax(dto.getIsTax());
        soReturnEntity.setAddressTypeDict(dto.getAddressTypeDict());
        this.save(soReturnEntity);
        soReturnDetailService.add(dto, soReturnEntity.getId());
        return soReturnEntity.getId();
    }

    @Override
    public Boolean update(SoReturnDTO.Update dto) {
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoService.getById(dto.getSourceId());
        SoReturnEntity soReturnEntity = new SoReturnEntity();
        BeanMapperUtils.copy(soInfoEntity, soReturnEntity);
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.list();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        soReturnEntity.setCustomerName(customerInfoEntity.getName());
        soReturnEntity.setId(dto.getId());
        soReturnEntity.setSourceId(dto.getSourceId());
        soReturnEntity.setSourceCode(soInfoEntity.getCode());
        soReturnEntity.setBillDate(dto.getBillDate());
        soReturnEntity.setReceiverName(dto.getReceiverName());
        soReturnEntity.setTelNumber(dto.getTelNumber());
        soReturnEntity.setAddressTypeDict(dto.getReceiveAddressDict());
        soReturnEntity.setDeliveryModeDict(dto.getDeliveryModeDict());
        soReturnEntity.setCurrency(dto.getCurrency());
        soReturnEntity.setCurrencySymbol(dto.getCurrencySymbol());
        soReturnEntity.setIsTax(dto.getIsTax());
        soReturnEntity.setAddressTypeDict(dto.getAddressTypeDict());
        boolean flag = this.updateById(soReturnEntity);
        soReturnDetailService.update(dto);
        return flag;
    }

    @Override
    public SoReturnDTO.View view(String id) {
        SoReturnDTO.View viewDTO = new SoReturnDTO.View();
        SoReturnEntity soReturnEntity = this.getById(id);
        BeanMapperUtils.copy(soReturnEntity, viewDTO);
        //创库保存详情表的集合
        List<SoReturnDetailDTO.View> detailViewDTOS = new ArrayList<>();
        List<SoReturnDetailEntity> detailEntityList = soReturnDetailService.listDetailByMainId(id);
        SoInfoEntity soInfoEntity = soInfoService.getById(soReturnEntity.getSourceId());
        BeanMapperUtils.copy(soInfoEntity, viewDTO);
        //获取sku的id集合
        List<String> skuIdList = detailEntityList.stream().map(SoReturnDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        //获取销售单详情表id集合
        List<String> orderDetailIds = detailEntityList.stream().map(SoReturnDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(orderDetailIds);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setInvalidStatusName(InvalidStatusEnum.getName(viewDTO.getInvalidStatus()));
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.list();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(soInfoEntity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        soReturnEntity.setCustomerName(customerInfoEntity.getName());
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySourceDetailId(orderDetailIds);
        for (SoReturnDetailEntity detailEntity : detailEntityList) {
            SoReturnDetailDTO.View detailView = new SoReturnDetailDTO.View();
            BeanMapperUtils.copy(detailEntity, detailView);
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(detailEntity.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            detailView.setProductName(productDetailEntity.getName());
            detailView.setSalesQty(soDetailEntity.getQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSourceDetailId().equals(detailEntity.getSourceDetailId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            detailView.setDeliveryQty(actualQty);
            detailView.setUnDeliveryQty(soDetailEntity.getQty() - actualQty);
            detailViewDTOS.add(detailView);
        }
        viewDTO.setDetailList(detailViewDTOS);
        return viewDTO;
    }

    @Override
    public Boolean submit(List<String> ids) {
        List<SoReturnEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(entityList)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //未作废、待提交、审核不通过才可以提交
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98010);
        }

        //TODO 待加审核流程
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个销售退货订单【%s】", ModuleTypeEnum.SO_RETURN.getCode(), pairList, "提交操作");

        //更新审核状态
        lambdaUpdate().set(SoReturnEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(SoReturnEntity::getId, ids)
                .update();
        return Boolean.TRUE;
    }

    @Override
    public Boolean addAndSubmit(SoReturnDTO.Add dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return this.submit(Arrays.asList(id));
    }

    @Override
    public Boolean updateAndSubmit(SoReturnDTO.Update dto) {
        Boolean update = this.update(dto);
        if (update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    public Boolean approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        List<SoReturnEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //判断是否是审核中的状态
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        //TODO 待加审核流程
        if (ApproveTypeEnum.PASS.getStatus().equals(baseApproveParamDTO.getType())) {
            LoginUser userInfo = commonService.getUserInfo();
            //审核通过
            lambdaUpdate().set(SoReturnEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoReturnEntity::getApproveUserId, userInfo.getUid())
                    .set(SoReturnEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoReturnEntity::getApproveTime, LocalDateTime.now())
                    .in(SoReturnEntity::getId, ids)
                    .update();
        } else {
            //审核不通过
            lambdaUpdate().set(SoReturnEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .in(SoReturnEntity::getId, ids)
                    .update();
        }
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个销售退货订单", ApproveTypeEnum.getName(baseApproveParamDTO.getType())).concat("【%s】").concat(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "审核操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean disApprove(List<String> ids) {
        List<SoReturnEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //已审核支持反审核
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())
        ).count();
        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_99003);
        }
        //TODO 待加审核流程

        //下推入库单不能反审核
        List<SoReturnNoticeEntity> soReturnNoticeEntities = soReturnNoticeFeign.listBySourceId(ids);
        if (CollectionUtils.isNotEmpty(soReturnNoticeEntities)) {
            throw new ServiceException(ApiError.ERROR_92012);
        }
        //修改状态为待提交
        lambdaUpdate().set(SoReturnEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoReturnEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个销售退货订单【%s】", ModuleTypeEnum.SO_RETURN.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<SoReturnEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核中可以撤销
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.APPROVE_ING.getStatus())
        ).count();

        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        //撤销现有流程
        workflowFeign.cancelProcess(ids);
        //修改状态为待提交
        lambdaUpdate().set(SoReturnEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoReturnEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("发货通知单【%s】取消流程", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "取消流程操作");

        return Boolean.TRUE;
    }

    @Override
    public Boolean invalid(List<String> ids, String remark) {
        List<SoReturnEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //审核不通过 待提交可以作废
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && (entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                || entity.getApproveStatus().equals(ApproveStatusEnum.REJECT.getStatus()))
        ).count();

        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98005);
        }

        //修改状态为待提交
        lambdaUpdate().set(SoReturnEntity::getInvalidStatus, Boolean.TRUE)
                .set(SoReturnEntity::getInvalidRemark, remark)
                .in(SoReturnEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个发货通知单【%s】，作废原因：".concat(remark), ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), pairList, "作废操作");
        /*deliveryNoticeEntityList.forEach(req -> {
            //修改到货状态
            deliveryNoticeEntityList.updateArrivalState(req.getPurchaseOrderId());
        });*/
        return Boolean.TRUE;
    }

    @Override
    public Boolean delete(List<String> ids) {
        List<SoReturnEntity> entityList = this.listByIds(ids);
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //待提交支持删除
        long count = entityList.stream().filter(entity -> entity.getInvalidStatus() == false
                && entity.getApproveStatus().equals(ApproveStatusEnum.WAIT_SUBMIT.getStatus())
        ).count();
        if (count != entityList.size()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //删除详情表
        soReturnDetailService.delete(ids);
        boolean flag = this.removeByIds(ids);
        //删除主表
        return flag;
    }

    @Override
    public Boolean exportExcel(SoReturnDTO.PagingParam dto, HttpServletResponse response) {
        List<SoReturnDTO.PagingView> pagingViews = baseMapper.soDeliveryNoticeExportExcel(dto);
        //获取sku的id集合
        List<String> skuIdList = pagingViews.stream().map(SoReturnDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = pagingViews.stream().map(SoReturnDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(orderDetailIds);
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySourceDetailId(orderDetailIds);
        List<CustomerInfoEntity> customerInfoEntities = customerInfoService.list();
        for (SoReturnDTO.PagingView pagingView : pagingViews) {
            pagingView.setApproveStatusName(ApproveStatusEnum.getName(pagingView.getApproveStatus().getStatus()));
            pagingView.setInvalidStatusName(InvalidStatusEnum.getName(pagingView.getInvalidStatus()));
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(pagingView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(pagingView.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            pagingView.setProductName(productDetailEntity.getName());
            pagingView.setSalesQty(soDetailEntity.getQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSourceDetailId().equals(pagingView.getSourceDetailId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            pagingView.setDeliveryQty(actualQty);
            pagingView.setUnDeliveryQty(soDetailEntity.getQty() - actualQty);
            pagingView.setUnit(productDetailEntity.getUnitName());
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(pagingView.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            pagingView.setCustomerName(customerInfoEntity.getName());
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/SoReturnExport.xlsx";
        String name = "销售退货订单";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(pagingViews, response, sb.toString(), excelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }

    @Override
    public List<SoReturnDTO.GenerateSoReturnNoticeView> generateSoReturnNoticeView(List<String> ids) {
        List<SoReturnDTO.GenerateSoReturnNoticeView> list = baseMapper.generateSoReturnNoticeView(ids);
        //获取sku的id集合
        List<String> skuIdList = list.stream().map(SoReturnDTO.GenerateSoReturnNoticeView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = list.stream().map(SoReturnDTO.GenerateSoReturnNoticeView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByIds(orderDetailIds);
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySourceDetailId(orderDetailIds);
        for (SoReturnDTO.GenerateSoReturnNoticeView generateSoReturnNoticeView : list) {
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(generateSoReturnNoticeView.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            generateSoReturnNoticeView.setSalesQty(soDetailEntity.getQty());
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> detail.getSourceDetailId().equals(generateSoReturnNoticeView.getSourceDetailId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            generateSoReturnNoticeView.setDeliveryQty(actualQty);
            generateSoReturnNoticeView.setReturnQty(soDetailEntity.getQty());
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(generateSoReturnNoticeView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            generateSoReturnNoticeView.setProductName(productDetailEntity.getName());
            generateSoReturnNoticeView.setSourceDetailId(generateSoReturnNoticeView.getSourceDetailId());
        }
        return list;
    }

    @Override
    public List<SoReturnEntity> listSoReturnByApproveStatus() {
        return lambdaQuery().eq(SoReturnEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus()).list();
    }
}

package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.oms.enums.SOReturnChangeListTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SoReturnReceiveMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 采购退货签收单
 * @author LUO_WG
 * @since 2023-05-10
 */
@Service
public class SoReturnReceiveServiceImpl extends SuperServiceImpl<SoReturnReceiveMapper, SoReturnReceiveEntity> implements SoReturnReceiveService {
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoReturnReceiveDetailService soReturnReceiveDetailService;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CommonService commonService;

    @Resource
    private QcInfoService qcInfoService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private SoReturnNoticeService soReturnNoticeService;

    @Override
    public PagingVO<SoReturnReceiveDTO.PagingView> paging(PagingDTO<SoReturnReceiveDTO.PagingParam> pagingParamDTO) {
        pagingParamDTO.getParams().setParam(pagingParamDTO.getParam());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoReturnReceiveDTO.PagingView> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        //明细数据
        List<SoReturnReceiveDTO.PagingView> records = pageData.getRecords();
        //获取sku的id集合
        List<String> skuIdList = records.stream().map(SoReturnReceiveDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //详情表id集合
        List<String> detailIds = records.stream().map(SoReturnReceiveDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(detailIds);

        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setCode(null);
                    obj.setSourceCode(null);
                    obj.setCustomerName(null);
                    obj.setInventoryOrgName(null);
                    obj.setApproveStatus(null);
                    obj.setApproveStatusName(null);
                    obj.setInvalidStatus(null);
                    obj.setInvalidStatusName(null);
                }
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                obj.setReturnTypeDictName(ReturnTypeEnum.getName(obj.getReturnTypeDict()));
                ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(obj.getSkuId())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(productDetailEntity)) {
                    throw new ServiceException(ApiError.ERROR_95107);
                }
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(obj.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
                obj.setProductName(productDetailEntity.getName());
                obj.setSalesQty(soDetailEntity.getQty());
                CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(obj.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
                obj.setCustomerName(customerInfoEntity.getName());
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<SoReturnReceiveDTO.StatusCountDTO> listCount(PermissionsDTO dto) {
        SOReturnChangeListTypeEnum[] values = SOReturnChangeListTypeEnum.values();
        List<SoReturnReceiveDTO.StatusCountDTO> list = new ArrayList<>();
        for (SOReturnChangeListTypeEnum item : values) {
            SoReturnReceiveDTO.PagingParam pagingParam = new SoReturnReceiveDTO.PagingParam();
            pagingParam.setParam(dto.getParam());
            SoReturnReceiveDTO.StatusCountDTO resultDTO = new SoReturnReceiveDTO.StatusCountDTO();
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
    public String add(SoReturnReceiveDTO.Add dto) {
        //获取退货单信息
        SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(dto.getSourceId());
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soReturnEntity.getSourceId());
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getInventoryOrgId());

        SoReturnReceiveEntity entity = new SoReturnReceiveEntity();
        BeanMapperUtils.copy(soInfoEntity, entity);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        entity.setCustomerName(customerInfoEntity.getName());
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.THQS, BusinessNoTypeEnum.CODE_THQS.getCode()));
        entity.setCode(code);
        entity.setSourceId(dto.getSourceId());
        entity.setSourceCode(soReturnEntity.getCode());
        entity.setSourceType(soReturnEntity.getSourceType());
        entity.setInventoryOrgId(dto.getInventoryOrgId());
        entity.setInventoryOrgName(sysAccountingCompanyEntity.getCompanyName());
        entity.setBillDate(dto.getBillDate());
        if (StringUtils.isNotBlank(dto.getWarehouseKeeperId())) {
            //获取用户信息
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperName(userDTO.getUserName());
        }
        this.save(entity);
        soReturnReceiveDetailService.add(dto, entity.getId());
        return soReturnEntity.getId();
    }

    @Override
    public Boolean update(SoReturnReceiveDTO.Update dto) {
        //获取退货单信息
        SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(dto.getSourceId());
        //获取销售单信息
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soReturnEntity.getSourceId());
        //获取核算公司
        SysAccountingCompanyEntity sysAccountingCompanyEntity = sysUserFeign.getCompanyById(dto.getInventoryOrgId());

        SoReturnReceiveEntity entity = new SoReturnReceiveEntity();
        BeanMapperUtils.copy(soInfoEntity, entity);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        entity.setCustomerName(customerInfoEntity.getName());

        entity.setId(dto.getId());
        entity.setSourceId(dto.getSourceId());
        entity.setSourceCode(soReturnEntity.getCode());
        entity.setSourceType(soReturnEntity.getSourceType());
        entity.setInventoryOrgId(dto.getInventoryOrgId());
        entity.setInventoryOrgName(sysAccountingCompanyEntity.getCompanyName());
        entity.setBillDate(dto.getBillDate());
        if (StringUtils.isNotBlank(dto.getWarehouseKeeperId())) {
            //获取用户信息
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperId(dto.getWarehouseKeeperId());
            entity.setWarehouseKeeperName(userDTO.getUserName());
        }
        boolean save = this.updateById(entity);
        soReturnReceiveDetailService.update(dto);
        return save;
    }

    @Override
    public SoReturnReceiveDTO.View view(String id) {
        SoReturnReceiveDTO.View viewDTO = new SoReturnReceiveDTO.View();
        SoReturnReceiveEntity entity = this.getById(id);
        BeanMapperUtils.copy(entity, viewDTO);
        //创库保存详情表的集合
        List<SoReturnReceiveDetailDTO.View> detailViewDTOS = new ArrayList<>();
        List<SoReturnReceiveDetailEntity> detailEntityList = soReturnReceiveDetailService.listDetailByMainId(id);
        SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(entity.getSourceId());
        BeanMapperUtils.copy(soInfoEntity, viewDTO);
        //获取sku的id集合
        List<String> skuIdList = detailEntityList.stream().map(SoReturnReceiveDetailEntity::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的销售单详情表id集合
        List<String> orderDetailIds = detailEntityList.stream().map(SoReturnReceiveDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        viewDTO.setInvalidStatusName(InvalidStatusEnum.getName(viewDTO.getInvalidStatus()));
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
        viewDTO.setCustomerName(customerInfoEntity.getName());
        for (SoReturnReceiveDetailEntity detailEntity : detailEntityList) {
            SoReturnReceiveDetailDTO.View detailView = new SoReturnReceiveDetailDTO.View();
            BeanMapperUtils.copy(detailEntity, detailView);
            //产品sku信息
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            detailView.setProductName(productDetailEntity.getName());
            //销售单信息
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            detailView.setSalesQty(soDetailEntity.getQty());
            detailViewDTOS.add(detailView);
        }
        viewDTO.setDetailList(detailViewDTOS);
        return viewDTO;
    }

    @Override
    public Boolean submit(List<String> ids) {
        List<SoReturnReceiveEntity> entityList = this.listByIds(ids);
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
        operateLogService.batchAddModuleOperateLog("提交了一个销售退货通知单【%s】", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), pairList, "提交操作");

        //更新审核状态
        lambdaUpdate().set(SoReturnReceiveEntity::getApproveStatus, ApproveStatusEnum.APPROVE_ING.getStatus())
                .in(SoReturnReceiveEntity::getId, ids)
                .update();
        return Boolean.TRUE;
    }

    @Override
    public Boolean addAndSubmit(SoReturnReceiveDTO.Add dto) {
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        return this.submit(Arrays.asList(id));
    }

    @Override
    public Boolean updateAndSubmit(SoReturnReceiveDTO.Update dto) {
        Boolean update = this.update(dto);
        if (update) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    public Boolean approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        List<SoReturnReceiveEntity> entityList = this.listByIds(ids);
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
            lambdaUpdate().set(SoReturnReceiveEntity::getApproveStatus, ApproveStatusEnum.APPROVE.getStatus())
                    .set(SoReturnReceiveEntity::getApproveUserId, userInfo.getUid())
                    .set(SoReturnReceiveEntity::getApproveUserName, userInfo.getUserName())
                    .set(SoReturnReceiveEntity::getApproveTime, LocalDateTime.now())
                    .in(SoReturnReceiveEntity::getId, ids)
                    .update();
        } else {
            //审核不通过
            lambdaUpdate().set(SoReturnReceiveEntity::getApproveStatus, ApproveStatusEnum.REJECT.getStatus())
                    .in(SoReturnReceiveEntity::getId, ids)
                    .update();
        }
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个销售退货通知单", ApproveTypeEnum.getName(baseApproveParamDTO.getType())).concat("【%s】").concat(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), pairList, "审核操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean disApprove(List<String> ids) {
        List<SoReturnReceiveEntity> entityList = this.listByIds(ids);
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

        //下推质检单不能反审核
        List<QcInfoEntity> qcBySourceId = qcInfoService.listQCBySourceIds(ids);
        if (CollectionUtils.isNotEmpty(qcBySourceId)) {
            throw new ServiceException(ApiError.ERROR_99042);
        }

        //修改状态为待提交
        lambdaUpdate().set(SoReturnReceiveEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoReturnReceiveEntity::getId, ids)
                .update();

        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个销售退货通知单【%s】", ModuleTypeEnum.SO_RETURN_NOTICE.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean cancelProcess(List<String> ids) {
        List<SoReturnReceiveEntity> entityList = this.listByIds(ids);
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
        lambdaUpdate().set(SoReturnReceiveEntity::getApproveStatus, ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                .in(SoReturnReceiveEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("退货签收单【%s】取消流程", ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), pairList, "取消流程操作");

        return Boolean.TRUE;
    }

    @Override
    public Boolean invalid(List<String> ids, String remark) {
        List<SoReturnReceiveEntity> entityList = this.listByIds(ids);
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
        lambdaUpdate().set(SoReturnReceiveEntity::getInvalidStatus, Boolean.TRUE)
                .set(SoReturnReceiveEntity::getInvalidRemark, remark)
                .in(SoReturnReceiveEntity::getId, ids)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = entityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个退货签收单【%s】，作废原因：".concat(remark), ModuleTypeEnum.SO_RETURN_RECEIVE.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean delete(List<String> ids) {
        List<SoReturnReceiveEntity> entityList = this.listByIds(ids);
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
        soReturnReceiveDetailService.delete(ids);
        boolean flag = this.removeByIds(ids);
        //删除主表
        return flag;
    }

    @Override
    public Boolean exportExcel(SoReturnReceiveDTO.PagingParam dto, HttpServletResponse response) {
        List<SoReturnReceiveDTO.PagingView> pagingViews = baseMapper.soReturnReceiveExportExcel(dto);
        //获取sku的id集合
        List<String> skuIdList = pagingViews.stream().map(SoReturnReceiveDTO.PagingView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = pagingViews.stream().map(SoReturnReceiveDTO.PagingView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);
        List<CustomerInfoEntity> customerInfoEntities = customerFeign.listCustomer();
        for (SoReturnReceiveDTO.PagingView pagingView : pagingViews) {
            pagingView.setApproveStatusName(ApproveStatusEnum.getName(pagingView.getApproveStatus()));
            pagingView.setInvalidStatusName(InvalidStatusEnum.getName(pagingView.getInvalidStatus()));
            pagingView.setReturnTypeDictName(ReturnTypeEnum.getName(pagingView.getReturnTypeDict()));
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(pagingView.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(productDetailEntity)) {
                throw new ServiceException(ApiError.ERROR_95107);
            }
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(pagingView.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            pagingView.setProductName(productDetailEntity.getName());
            pagingView.setSalesQty(soDetailEntity.getQty());
            CustomerInfoEntity customerInfoEntity = customerInfoEntities.stream().filter(req -> req.getId().equals(pagingView.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            pagingView.setCustomerName(customerInfoEntity.getName());
        }
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/soReturnReceiveExport.xlsx";
        String name = "销售退货签收单";
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
    public Boolean generateSoReturnReceiveSave(List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> list) {
        Boolean flag = Boolean.TRUE;
        List<String> soReturnNoticeIdList = list.stream().map(SoReturnNoticeDTO.GenerateSoReturnReceiveView::getMainId).distinct().collect(Collectors.toList());
        List<String> soDetailIdList = list.stream().map(SoReturnNoticeDTO.GenerateSoReturnReceiveView::getSourceDetailId).distinct().collect(Collectors.toList());
        long count = soReturnNoticeService.listByIds(soReturnNoticeIdList).stream().filter(req -> !ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_92021);
        }
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(soDetailIdList);
        if (CollectionUtils.isEmpty(soDetailEntities)) {
            throw new ServiceException(ApiError.ERROR_92015);
        }
        for (String id : soReturnNoticeIdList) {
            List<SoReturnNoticeDTO.GenerateSoReturnReceiveView> viewList = list.stream().filter(req -> req.getMainId().equals(id)).collect(Collectors.toList());
            SoReturnNoticeEntity noticeEntity = soReturnNoticeService.getById(id);
            SoReturnReceiveDTO.Add dto = new SoReturnReceiveDTO.Add();
            dto.setSourceId(id);
            dto.setInventoryOrgId(noticeEntity.getInventoryOrgId());
            dto.setWarehouseKeeperId(noticeEntity.getWarehouseKeeperId());
            dto.setReturnDate(noticeEntity.getBillDate());
            dto.setBillDate(LocalDate.now());
            List<SoReturnReceiveDetailDTO.Add> detailList = dto.getDetailList();
            for (SoReturnNoticeDTO.GenerateSoReturnReceiveView view : viewList) {
                SoReturnReceiveDetailDTO.Add detailAddDTO = new SoReturnReceiveDetailDTO.Add();
                detailAddDTO.setReturnQty(view.getReturnQty());
                detailAddDTO.setReceiveQty(view.getReturnQty());
                detailAddDTO.setReturnReasonDict(view.getReturnReasonDict());
                detailAddDTO.setReturnTypeDict(view.getReturnTypeDict());
                detailAddDTO.setRemark(view.getRemark());
                detailAddDTO.setSourceDetailId(view.getId());
                detailList.add(detailAddDTO);
            }
            dto.setDetailList(detailList);
            String noticeId = this.add(dto);
            if (StringUtils.isBlank(noticeId)) {
                flag = Boolean.FALSE;
            }
        }
        return flag;
    }

    @Override
    public List<SoReturnReceiveDTO.GenerateSoReturnInstockView> generateSoReturnInstockView(List<String> ids) {
        List<SoReturnReceiveDTO.GenerateSoReturnInstockView> list = baseMapper.generateSoReturnInstockView(ids);
        //获取界面传过来的采购单详情表id集合
        List<String> orderDetailIds = list.stream().map(SoReturnReceiveDTO.GenerateSoReturnInstockView::getSourceDetailId).collect(Collectors.toList());
        //获取销售单详情信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByIds(orderDetailIds);
        //获取sku的id集合
        List<String> skuIdList = list.stream().map(SoReturnReceiveDTO.GenerateSoReturnInstockView::getSkuId).collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        for (SoReturnReceiveDTO.GenerateSoReturnInstockView view : list) {
            //销售单信息
            SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(detail -> detail.getId().equals(view.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            view.setSalesQty(soDetailEntity.getQty());
            view.setStockInQty(view.getReceiveQty());
            view.setSellableQty(view.getReceiveQty());
            view.setUnSellableQty(MathUtil.ZERO);
            //产品sku信息
            ProductDetailEntity productDetailEntity = productDetailEntityList.stream().filter(entityClass -> entityClass.getId().equals(view.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            view.setProductName(productDetailEntity.getName());
        }
        return null;
    }

    @Override
    public List<SoReturnReceiveEntity> listBySourceIds(List<String> ids) {
        return lambdaQuery().in(SoReturnReceiveEntity::getSourceId, ids).list();
    }
}

package com.erp.server.scm.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.excel.SalesDemandImportExcelDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.listener.SalesDemandExcelListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.serveice.SuperServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.dto.SalesDemandDetailDTO;
import com.erp.model.scm.dto.excel.SalesDemandExportExcelDTO;
import com.erp.model.scm.entity.SalesDemandDetailEntity;
import com.erp.model.scm.entity.SalesDemandEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.mapper.SalesDemandMapper;
import com.erp.server.scm.service.CommonService;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SalesDemandDetailService;
import com.erp.server.scm.service.SalesDemandService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售需求主表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Service
@Slf4j
public class SalesDemandServiceImpl extends SuperServiceImpl<SalesDemandMapper, SalesDemandEntity> implements SalesDemandService {

    @Resource
    private SalesDemandDetailService salesDemandDetailService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private CommonService commonService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Override
    public PagingVO<SalesDemandDTO.ListDTO> paging(PagingDTO<SalesDemandDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<SalesDemandDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        //清空明细数据
        List<SalesDemandDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            List<String> ids = records.stream().map(SalesDemandDTO.ListDTO::getId).collect(Collectors.toList());
            //查询流程id判断是否存在流程 TODO

            List<String> list = new ArrayList<>();
            records.forEach(obj -> {
                boolean contains = list.contains(obj.getId());
                if (contains) {
                    obj.setId(null);
                    obj.setCode(null);
                    obj.setShopName(null);
                    obj.setApproveStatusName(null);
                    obj.setInvalidStatusName(null);
                    obj.setIsFirstMassProduct(null);
                    obj.setStockReason(null);
                    obj.setApproveStatusName(null);
                    obj.setCreateUserName(null);
                    return;
                }
                list.add(obj.getId());
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(SalesDemandDTO.AddDTO dto) {
        SalesDemandEntity entity = new SalesDemandEntity();
        BeanMapperUtils.copy(dto,entity);
        //校验明细是否有重复sku
        checkAddDetailsRepeatSku(dto.getDetails());
        //处理数据id
        doOpHandleDataId(dto.getApplyUserId(),dto.getApplyDeptId(),dto.getShopId(),entity);
        log.info("备货申请单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.BH, BusinessNoTypeEnum.CODE_BH.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个备货申请单【%s】",code), ModuleTypeEnum.SALES_DEMAND.getCode(),entity.getId(),"新增操作");
            //新增明细
            salesDemandDetailService.add(dto.getDetails(),entity.getId());
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SalesDemandDTO.UpdateDTO dto) {
        SalesDemandEntity entity = new SalesDemandEntity();
        BeanMapperUtils.copy(dto,entity);
        //校验明细是否有重复sku
        checkUpdateDetailsRepeatSku(dto.getDetails(),dto.getId());
        //处理数据id
        doOpHandleDataId(dto.getApplyUserId(),dto.getApplyDeptId(),dto.getShopId(),entity);

        log.info("备货申请单修改，id=【{}】", dto.getId());
        SalesDemandDTO.UpdateDTO old = new SalesDemandDTO.UpdateDTO();
        SalesDemandDTO.ViewDTO view = this.view(dto.getId());
        BeanMapperUtils.copy(view,old);
        //操作日志
        moduleOperateLogService.addModuleOperateLogByObj(old,dto,ModuleTypeEnum.SALES_DEMAND.getCode(),entity.getId(),"","");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        salesDemandDetailService.update(dto.getDetails(),entity.getId());
        return Boolean.TRUE;
    }

    @Override
    public SalesDemandDTO.ViewDTO view(String id) {
        SalesDemandDTO.ViewDTO dto = new SalesDemandDTO.ViewDTO();

        //主表信息
        SalesDemandEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98001);
        }
        BeanMapperUtils.copy(entity,dto);

        //明细信息
        List<SalesDemandDetailEntity> entityDetails = salesDemandDetailService.listBySalesDemandId(id);
        if (CollectionUtils.isEmpty(entityDetails)) {
            throw new ServiceException(ApiError.ERROR_98002);
        }
        List<SalesDemandDetailDTO.UpdateDTO> details = BeanMapperUtils.copyList(SalesDemandDetailDTO.UpdateDTO.class, entityDetails);
        dto.setDetails(details);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids,String reason) {
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("备货申请单作废，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新
        lambdaUpdate().in(SalesDemandEntity::getId,ids)
                .set(SalesDemandEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(SalesDemandEntity::getInvalidTime, LocalDateTime.now())
                .set(SalesDemandEntity::getRemark,reason)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("作废了一个备货申请单", ModuleTypeEnum.SALES_DEMAND.getCode(),pairList,"作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        String type = baseApproveParamDTO.getType();

        log.info("备货申请单【{}】，ids=【{}】",ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));

        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过 TODO(判断是否存在流程)

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(ids,ApproveStatusEnum.APPROVE.getStatus());
        }else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(ids,ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog(String.format("审核了一个备货申请单【%s】",ApproveTypeEnum.getName(type)), ModuleTypeEnum.SALES_DEMAND.getCode(),pairList,"审核操作");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(String id) {
        SalesDemandEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98001);
        }
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("备货申请单撤销流程，id=【{}】", id);

        //撤销现有流程
        workflowFeign.cancelProcess(id);

        //更新单据为待提交
        updateApproveStatus(Arrays.asList(id),ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        moduleOperateLogService.addModuleOperateLog(String.format("备货申请单【%s】取消流程",entity.getCode()), ModuleTypeEnum.SALES_DEMAND.getCode(),entity.getId(),"取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(SalesDemandDTO.SearchParamDTO dto, HttpServletResponse response) {
        List<SalesDemandExportExcelDTO> exportExcelList = baseMapper.listExportExcel(dto);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/salesDemandExport.xlsx";
        String name = "备货申请单";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(exportExcelList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //审核中和已审核允许反审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        log.info("备货申请单反审核，ids=【{}】", JSONUtil.toJsonStr(ids));

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatus(ids,ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("反审核了一个备货申请单", ModuleTypeEnum.SALES_DEMAND.getCode(),pairList,"反审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //待审核允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("备货申请单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        salesDemandDetailService.removeBySalesDemandIds(ids);
        //删除主表数据
        return  this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //待提交并且未作废允许提交
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus()) ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("备货申请单提交，ids=【{}】", JSONUtil.toJsonStr(ids));

        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids,ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("提交了一个备货申请单", ModuleTypeEnum.SALES_DEMAND.getCode(),pairList,"提交操作");
        return Boolean.TRUE;
    }

    @Override
    public  List<SalesDemandDetailDTO.ExcelDTO> importFile(MultipartFile excelFile, HttpServletResponse response) {
        //查询所有审核通过的sku
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        //查询所有审核通过并启用的仓库
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listApproveWarehouse();

        SalesDemandExcelListener excelListenerUtil = new SalesDemandExcelListener(skuList,warehouseList);
        try {
            EasyExcel.read(excelFile.getInputStream(), SalesDemandImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！",e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！",e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<SalesDemandImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<SalesDemandDetailDTO.ExcelDTO> dataList = excelListenerUtil.getDataList();
        //导出错误数据
        List<SalesDemandImportExcelDTO> list = excelListenerUtil.getErrorList();
        if (list.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/salesDemandError.xlsx";
            String name = "salesDemand";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
        }
        return dataList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(SalesDemandDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isNotBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        return this.submit(Arrays.asList(id));
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId (String applyUserId,String applyDeptId,String shopId,SalesDemandEntity entity) {
        //申请人
        if (StringUtils.isNotBlank(applyUserId)) {
            FindUserDTO applyUser = sysUserFeign.getUserByUserId(applyUserId);
            if (ObjectUtils.isEmpty(applyUser)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            entity.setApplyUserName(applyUser.getUserName());
        }
        //申请部门
        if (StringUtils.isNotBlank(applyDeptId)) {
            SysDepartmentDTO depart = sysUserFeign.getUserDeptById(applyDeptId);
            if (ObjectUtils.isEmpty(depart)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setApplyDeptName(depart.getName());
        }
        //店铺
        if (StringUtils.isNotBlank(shopId)) {
            DmpShopInfoDTO dmpShopInfoDTO = dmpTaskFeign.getShopById(shopId);
            if (ObjectUtils.isEmpty(dmpShopInfoDTO)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setShopName(dmpShopInfoDTO.getName());
        }
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids,String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(SalesDemandEntity::getId,ids)
                .set(SalesDemandEntity::getApproveStatus,approveStatus)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids,String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();

        this.lambdaUpdate().in(SalesDemandEntity::getId,ids)
                .set(SalesDemandEntity::getApproveUserId,userInfo.getUid())
                .set(SalesDemandEntity::getApproveUserName,userInfo.getUserName())
                .set(SalesDemandEntity::getApproveStatus,approveStatus)
                .set(SalesDemandEntity::getApproveTime,LocalDateTime.now())
                .update();
    }


    /**
     * 根据ids查询数据
     */
    private List<SalesDemandEntity>  getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<SalesDemandEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98001);
        }
        return list;
    }

    /**
     * 新增验证sku是否重复
     */
    private void checkAddDetailsRepeatSku(List<SalesDemandDetailDTO.AddDTO> list) {
        Map<String, List<SalesDemandDetailDTO.AddDTO>> map = list.stream().collect(Collectors.groupingBy(SalesDemandDetailDTO.AddDTO::getSkuId));
        for (Map.Entry<String, List<SalesDemandDetailDTO.AddDTO>> entry: map.entrySet()) {
            List<SalesDemandDetailDTO.AddDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1,"sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }
    }

    /**
     * 编辑验证sku是否重复
     */
    private void checkUpdateDetailsRepeatSku(List<SalesDemandDetailDTO.UpdateDTO> list,String salesDemandId) {
        Map<String, List<SalesDemandDetailDTO.UpdateDTO>> map = list.stream().collect(Collectors.groupingBy(SalesDemandDetailDTO.UpdateDTO::getSkuId));
        for (Map.Entry<String, List<SalesDemandDetailDTO.UpdateDTO>> entry: map.entrySet()) {
            List<SalesDemandDetailDTO.UpdateDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1,"录入sku编码【".concat(value.get(0).getSkuNo()).concat("】存在重复")));
            }
            SalesDemandDetailEntity entity = salesDemandDetailService.getBySalesDemandIdAndSkuId(salesDemandId, entry.getKey());
            if (ObjectUtils.isNotEmpty(entity) && !entity.getId().equals(value.get(0).getId())) {
                throw new ServiceException(new ApiResult(1,"sku编码【".concat(value.get(0).getSkuNo()).concat("】已存在")));
            }
        }
    }

}

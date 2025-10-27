package com.erp.server.scm.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.BiShopInfoDTO;
import com.erp.model.plm.enums.FirstMassProductTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.dto.SalesDemandDetailDTO;
import com.erp.model.scm.dto.excel.SalesDemandExportExcelDTO;
import com.erp.model.scm.dto.excel.SalesDemandImportExcelDTO;
import com.erp.model.scm.entity.SalesDemandDetailEntity;
import com.erp.model.scm.entity.SalesDemandEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseTableFlagEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.listener.SalesDemandExcelListener;
import com.erp.server.scm.mapper.SalesDemandMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SalesDemandDetailService;
import com.erp.server.scm.service.SalesDemandService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SALES_DEMAND;

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
    private SysUserFeign sysUserFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Override
    public PagingVO<SalesDemandDTO.ListDTO> paging(PagingDTO<SalesDemandDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<SalesDemandDTO.ListDTO> pageData = baseMapper.paging(query, pagingDTO.getParams());
        //清空明细数据
        List<SalesDemandDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(obj -> {
                obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
                obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
                obj.setFirstMassProductName(FirstMassProductTypeEnum.getName(obj.getFirstMassProduct()));
            });
        }
        return new PagingVO(pageData);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public String add(SalesDemandDTO.AddDTO dto) {
        SalesDemandEntity entity = new SalesDemandEntity();
        BeanMapperUtils.copy(dto, entity);
        //校验明细是否有重复sku
        //checkAddDetailsRepeatSku(dto.getDetails());
        //处理数据id
        doOpHandleDataId(dto.getApplyUserId(), dto.getApplyDeptId(), dto.getShopId(), entity);
        log.info("备货申请单新增");
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.BH, BusinessNoTypeEnum.CODE_BH.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_BH);
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            moduleOperateLogService.addModuleOperateLog(String.format("新增了一个备货申请单【%s】", code), ModuleTypeEnum.SALES_DEMAND.getCode(), entity.getId(), "新增操作");
            //新增明细
            salesDemandDetailService.add(dto.getDetails(), entity.getId());
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SalesDemandDTO.UpdateDTO dto) {
        SalesDemandEntity entity = new SalesDemandEntity();
        BeanMapperUtils.copy(dto, entity);
        List<SalesDemandDetailDTO.UpdateDTO> details = dto.getDetails();
        //校验明细是否有重复sku
        //checkUpdateDetailsRepeatSku(details, dto.getId());
        //处理数据id
        doOpHandleDataId(dto.getApplyUserId(), dto.getApplyDeptId(), dto.getShopId(), entity);

        log.info("备货申请单修改，id=【{}】", dto.getId());

        //添加日志
        SalesDemandEntity old = this.getById(dto.getId());
        moduleOperateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.SALES_DEMAND.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        salesDemandDetailService.update(details, entity.getId());
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
        BeanMapperUtils.copy(entity, dto);

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
    public Boolean invalid(List<String> ids, String reason) {
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
        lambdaUpdate().in(SalesDemandEntity::getId, ids)
                .set(SalesDemandEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(SalesDemandEntity::getInvalidTime, LocalDateTime.now())
                .set(SalesDemandEntity::getInvalidRemark, reason)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("作废了一个备货申请单【%s】，作废原因：".concat(reason), ModuleTypeEnum.SALES_DEMAND.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(SalesDemandEntity entity, String type, String comment, Boolean isNeedProcess) {
        //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98006.msg);
        }
        log.info("备货申请单【{}】，id=【{}】", ApproveTypeEnum.getName(type), entity.getId());
        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过 TODO(判断是否存在流程)

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.APPROVE.getStatus());
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(Collections.singletonList(entity.getId()), ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        moduleOperateLogService.addModuleOperateLog(String.format("审核【%s】了一个备货申请单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.SALES_DEMAND.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(ApproveDTO.BatchCancelProcessDTO dto) {
        List<String> ids = dto.getIds();
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("备货申请单撤销流程，id=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatus(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("备货申请单【%s】取消流程", ModuleTypeEnum.SALES_DEMAND.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(SalesDemandDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("备货申请单数据", EXPORT_SCM_SALES_DEMAND.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(SalesDemandEntity entity) {
        //已审核允许反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),ApiError.ERROR_98014.msg);
        }
        log.info("备货申请单反审核，id=【{}】", entity.getId());

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatus(Collections.singletonList(entity.getId()), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        moduleOperateLogService.addModuleOperateLog(String.format("反审核了一个备货申请单【%s】", entity.getCode()), ModuleTypeEnum.SALES_DEMAND.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> delete(List<String> ids) {
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //待提交允许删除
//        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
//        if (count > 0) {
//            throw new ServiceException(ApiError.ERROR_98009);
//        }
        List<SalesDemandEntity> removeList=new ArrayList<>();
        List<BatchResultDTO> resultDTOList=new ArrayList<>();
        for (SalesDemandEntity entity : list) {
            if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus())){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98009.msg));
                continue;
            }
            removeList.add(entity);
            resultDTOList.add(BatchResultDTO.success(entity.getId(), entity.getCode(),"删除成功"));
        }
        List<String> removeIdList = removeList.stream().map(SalesDemandEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(removeIdList)){
            return resultDTOList;
        }
        log.info("备货申请单删除，ids=【{}】", JSONUtil.toJsonStr(removeIdList));
        //删除明细数据
        salesDemandDetailService.removeBySalesDemandIds(removeIdList);
        //删除操作日志
        moduleOperateLogService.removeByBusinessIds(removeIdList);
        //删除主表数据
        this.removeByIds(removeIdList);
        return resultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<SalesDemandEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("备货申请单提交，ids=【{}】", JSONUtil.toJsonStr(ids));

        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        moduleOperateLogService.batchAddModuleOperateLog("提交了一个备货申请单【%s】", ModuleTypeEnum.SALES_DEMAND.getCode(), pairList, "提交操作");
        return Boolean.TRUE;
    }

    @Override
    public SalesDemandDetailDTO.ImportDTO importFile(MultipartFile excelFile, List<String> skuIds, HttpServletResponse response) {
        //查询所有审核通过的sku
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        //查询所有审核通过并启用的仓库
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listApproveWarehouse();

        SalesDemandExcelListener excelListenerUtil = new SalesDemandExcelListener(skuList, wmsTaskFeign, skuIds);
        try {
            EasyExcel.read(excelFile.getInputStream(), SalesDemandImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<SalesDemandImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        SalesDemandDetailDTO.ImportDTO importDTO = new SalesDemandDetailDTO.ImportDTO();
        //导入数据处理
        List<SalesDemandDetailDTO.AddDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<SalesDemandImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "备货申请错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SalesDemandImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(successList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(SalesDemandDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        return this.submit(Arrays.asList(id));
    }

    @Override
    public Boolean updateAndSubmit(SalesDemandDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    public List<ListStatusCountDTO.SalesDemandCountDTO> listCount(PermissionsDTO dto) {
        SalesDemandDTO.SearchParamDTO searchParamDTO = new SalesDemandDTO.SearchParamDTO();
        searchParamDTO.setPermissionSql(dto.getPermissionSql());
        List<ListStatusCountDTO.SalesDemandCountDTO> list = new ArrayList<>();
        ListStatusCountDTO.SalesDemandCountDTO resultDTO = new ListStatusCountDTO.SalesDemandCountDTO();
        searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
        Integer count = this.baseMapper.listCount(searchParamDTO);
        resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
        resultDTO.setType(PurchaseTableFlagEnum.TO_BE_APPROVE.getCode());
        list.add(resultDTO);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateSalesDemand(ValidList<SalesDemandDTO.GenerateSalesDemandDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //备货申请单明细
        List<String> sourceDetailIds = list.stream().map(SalesDemandDTO.GenerateSalesDemandDTO::getSourceDetailId).collect(Collectors.toList());
        List<SalesDemandDetailEntity> salesDemandDetailList = salesDemandDetailService.listBySourceDetailIds(sourceDetailIds);

        Map<String, List<SalesDemandDTO.GenerateSalesDemandDTO>> map = list.stream().collect(Collectors.groupingBy(SalesDemandDTO.GenerateSalesDemandDTO::getSourceId));
        for (Map.Entry<String, List<SalesDemandDTO.GenerateSalesDemandDTO>> entry : map.entrySet()) {
            List<SalesDemandDTO.GenerateSalesDemandDTO> value = entry.getValue();
            SalesDemandDTO.AddDTO addDTO = new SalesDemandDTO.AddDTO();
            addDTO.setApplyDate(value.get(0).getApplyDate());
            addDTO.setSourceId(value.get(0).getSourceId());
            addDTO.setSourceCode(value.get(0).getSourceCode());
            addDTO.setSourceType(value.get(0).getSourceType());
            List<SalesDemandDetailDTO.AddDTO> addDetailList = new ArrayList<>();
            for (SalesDemandDTO.GenerateSalesDemandDTO dto : value) {
                if (CollectionUtils.isNotEmpty(salesDemandDetailList)) {
                    //已下推数量
                    Integer totalQty = salesDemandDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(dto.getSourceDetailId())).map(SalesDemandDetailEntity::getPlanStockQty).reduce(MathUtil.ZERO, Integer::sum);
                    if (dto.getPlanStockQty().intValue() > dto.getQty().intValue() - totalQty.intValue()) {
                        throw new ServiceException(ApiError.ERROR_98062.code, String.format(ApiError.ERROR_98062.msg, dto.getSourceCode(), dto.getSkuNo(), dto.getQty().intValue() - totalQty.intValue()));
                    }
                }

                //备货申请明细
                SalesDemandDetailDTO.AddDTO addDetailDTO = new SalesDemandDetailDTO.AddDTO();
                addDetailDTO.setSourceDetailId(dto.getSourceDetailId());
                addDetailDTO.setSkuId(dto.getSkuId());
                addDetailDTO.setSkuNo(dto.getSkuNo());
                addDetailDTO.setPlanStockQty(dto.getPlanStockQty());
                addDetailDTO.setDestWarehouseId(dto.getWarehouseId());
                addDetailDTO.setRemark(dto.getRemark());
                addDetailDTO.setFirstMassProduct(dto.getFirstMassProduct());
                addDetailList.add(addDetailDTO);
            }
            addDTO.setDetails(addDetailList);
            this.add(addDTO);
        }
        return Boolean.TRUE;
    }


    /**
     * 根据sourceIds 获取下推数量
     *
     * @param sourceIds
     * @return java.lang.Integer
     * @author yl
     * @date 2023-05-29 16:40
     */
    @Override
    public Integer getPushDownBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return 0;
        }

        return this.lambdaQuery().in(SalesDemandEntity::getSourceId,sourceIds).
                eq(SalesDemandEntity::getInvalidStatus,Boolean.FALSE).count();
    }

    @Override
    public PagingVO<SalesDemandExportExcelDTO> exportSalesDemand(PagingDTO<SalesDemandDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<SalesDemandExportExcelDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        return new PagingVO<>(page);
    }

    @Override
    public List<SalesDemandEntity> listBySourceIds(List<String> sourceIds) {
        return lambdaQuery().in(SalesDemandEntity::getSourceId, sourceIds)
                .eq(SalesDemandEntity::getInvalidStatus,Boolean.FALSE)
                .eq(SalesDemandEntity::getIsDeleted,Boolean.FALSE)
                .list();
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId(String applyUserId, String applyDeptId, String shopId, SalesDemandEntity entity) {
        //申请人
        if (StringUtils.isNotBlank(applyUserId)) {
            FindUserDTO applyUser = sysUserFeign.getUserByUserId(applyUserId);
            if (ObjectUtils.isEmpty(applyUser)) {
                throw new ServiceException(ApiError.USER_NOT_EXIST);
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
            BiShopInfoDTO dmpShopInfoDTO = dmpTaskFeign.getShopById(shopId);
            if (ObjectUtils.isEmpty(dmpShopInfoDTO)) {
                throw new ServiceException(ApiError.ERROR_9029);
            }
            entity.setShopName(dmpShopInfoDTO.getName());
        }
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(SalesDemandEntity::getId, ids)
                .set(SalesDemandEntity::getApproveStatus, approveStatus)
                .set(SalesDemandEntity::getApproveUserId, "")
                .set(SalesDemandEntity::getApproveUserName, "")
                .set(SalesDemandEntity::getApproveTime, null)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        this.lambdaUpdate().in(SalesDemandEntity::getId, ids)
                .set(SalesDemandEntity::getApproveUserId, userInfo.getUid())
                .set(SalesDemandEntity::getApproveUserName, userInfo.getUserName())
                .set(SalesDemandEntity::getApproveStatus, approveStatus)
                .set(SalesDemandEntity::getApproveTime, LocalDateTime.now())
                .update();
    }


    /**
     * 根据ids查询数据
     */
    private List<SalesDemandEntity> getList(List<String> ids) {
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
        for (Map.Entry<String, List<SalesDemandDetailDTO.AddDTO>> entry : map.entrySet()) {
            List<SalesDemandDetailDTO.AddDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1, "sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }
    }

    /**
     * 编辑验证sku是否重复
     */
    private void checkUpdateDetailsRepeatSku(List<SalesDemandDetailDTO.UpdateDTO> list, String salesDemandId) {
        Map<String, List<SalesDemandDetailDTO.UpdateDTO>> map = list.stream().collect(Collectors.groupingBy(SalesDemandDetailDTO.UpdateDTO::getSkuId));
        for (Map.Entry<String, List<SalesDemandDetailDTO.UpdateDTO>> entry : map.entrySet()) {
            List<SalesDemandDetailDTO.UpdateDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1, "录入sku编码【".concat(value.get(0).getSkuNo()).concat("】存在重复")));
            }
            SalesDemandDetailEntity entity = salesDemandDetailService.getBySalesDemandIdAndSkuId(salesDemandId, entry.getKey());
            if (ObjectUtils.isNotEmpty(entity) && !entity.getId().equals(value.get(0).getId())) {
                value.forEach(obj -> obj.setId(entity.getId()));
                //throw new ServiceException(new ApiResult(1,"sku编码【".concat(value.get(0).getSkuNo()).concat("】已存在")));
            }
        }
    }

}

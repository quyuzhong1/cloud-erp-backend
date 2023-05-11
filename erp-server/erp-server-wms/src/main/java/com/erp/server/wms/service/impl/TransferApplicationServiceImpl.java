package com.erp.server.wms.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
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
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.TransferApplicationDTO;
import com.erp.model.wms.dto.TransferApplicationDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.TransferApplicationEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.TransferApplicationMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-10
 */
@Slf4j
@Service
public class TransferApplicationServiceImpl extends SuperServiceImpl<TransferApplicationMapper, TransferApplicationEntity> implements TransferApplicationService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private TransferApplicationDetailService transferApplicationDetailService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private CommonService commonService;

    @Override
    public PagingVO<TransferApplicationDTO.ListDTO> paging(PagingDTO<TransferApplicationDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setParam(pagingDTO.getParam());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<TransferApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<TransferApplicationDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据处理
        doOpHandlePurchaseStockIn(records);
        List<String> list = new ArrayList<>();
        //清空明细数据
        records.forEach(obj -> {
            boolean contains = list.contains(obj.getId());
            if (contains) {
                obj.setCode(null);
                obj.setApproveStatus(null);
                obj.setApproveStatusName(null);
                obj.setInvalidStatus(null);
                obj.setInvalidStatusName(null);
                obj.setCreateUserName(null);
                return;
            }
            list.add(obj.getId());
        });
        return new PagingVO(pageData);
    }

    @Override
    public List<TransferApplicationDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PurchaseChangeListTypeEnum[] values = PurchaseChangeListTypeEnum.values();
        List<TransferApplicationDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PurchaseChangeListTypeEnum item : values) {
            TransferApplicationDTO.SearchParamDTO searchParamDTO = new TransferApplicationDTO.SearchParamDTO();
            searchParamDTO.setParam(dto.getParam());
            TransferApplicationDTO.ListStatusCountDTO resultDTO = new TransferApplicationDTO.ListStatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (PurchaseChangeListTypeEnum.TO_BE_APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseChangeListTypeEnum.APPROVE.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            if (PurchaseChangeListTypeEnum.REJECT.getCode().equals(item.getCode())) {
                searchParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.listCount(searchParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setSearchType(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public String add(TransferApplicationDTO.AddDTO dto) {
        TransferApplicationEntity entity = new TransferApplicationEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        doOpHandleDataId(dto.getInWarehouseId(), dto.getOutWarehouseId(), dto.getApplyUserId(), entity);
        log.info("调拨申请单新增");
        //生成单号
        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.DBSQ, BusinessNoTypeEnum.CODE_DBSQ.getCode()));
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个调拨申请单【%s】", code), ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), entity.getId(), "新增操作");
            //新增明细
            transferApplicationDetailService.add(dto.getDetails(), entity.getId());
        }
        return entity.getId();
    }

    @Override
    public String addAndSubmit(TransferApplicationDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        this.submit(Arrays.asList(id));
        return id;
    }

    @Override
    public Boolean update(TransferApplicationDTO.UpdateDTO dto) {
        TransferApplicationEntity entity = new TransferApplicationEntity();
        BeanMapperUtils.copy(dto, entity);
        List<TransferApplicationDetailDTO.UpdateDTO> details = dto.getDetails();
        //处理数据id
        doOpHandleDataId(dto.getInWarehouseId(), dto.getOutWarehouseId(), dto.getApplyUserId(), entity);

        log.info("调拨申请单修改，id=【{}】", dto.getId());

        //添加日志
        TransferApplicationEntity old = this.getById(dto.getId());
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        transferApplicationDetailService.update(details, entity.getId());
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateAndSubmit(TransferApplicationDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("调拨申请单提交，ids=【{}】", JSONUtil.toJsonStr(ids));

        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个调拨申请单【%s】", ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), pairList, "提交操作");
        return Boolean.TRUE;
    }

    @Override
    public TransferApplicationDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //待提交允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("调拨申请删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        transferApplicationDetailService.removeByMainIds(ids);
        //删除操作日志
        operateLogService.removeByBusinessIds(ids);
        //删除主表数据
        return this.removeByIds(ids);
    }

    @Override
    public Boolean invalid(List<String> ids, String reason) {
        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("采购入库单作废，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新
        lambdaUpdate().in(TransferApplicationEntity::getId, ids)
                .set(TransferApplicationEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(TransferApplicationEntity::getInvalidRemark, reason)
                .update();
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个调拨申请单【%s】，作废原因：".concat(reason), ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    public void approve(BaseApproveParamDTO baseApproveParamDTO) {
        List<String> ids = baseApproveParamDTO.getIds();
        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }

        String type = baseApproveParamDTO.getType();

        log.info("调拨申请单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));

        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            //审核通过 TODO(判断是否存在流程)

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(ids, ApproveStatusEnum.APPROVE.getStatus());
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(ids, ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个调拨申请单", ApproveTypeEnum.getName(type)).concat("【%s】").concat(StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.PO_INSTOCK.getCode(), pairList, "审核操作");
    }

    @Override
    public Boolean disApprove(List<String> ids) {
        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //已审核允许反审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        log.info("调拨申请单反审核，ids=【{}】", JSONUtil.toJsonStr(ids));

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个调拨申请单【%s】", ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("调拨申请单撤销流程，id=【{}】", ids);

        //撤销现有流程
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("调拨申请单【%s】取消流程", ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(TransferApplicationDTO.SearchParamDTO dto, HttpServletResponse response) {
        List<TransferApplicationDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        doOpHandlePurchaseStockIn(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/transferApplication.xlsx";
        String name = "调拨申请单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> viewGenerateTransferInfo(List<String> ids) {
        return null;
    }

    @Override
    public List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> viewGenerateTransferOut(List<String> ids) {
        return null;
    }


    /**
     * @param records
     * @description: 处理数据
     * @author Will
     * @date: 2023/4/19 18:58
     */
    private void doOpHandlePurchaseStockIn(List<TransferApplicationDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(TransferApplicationDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);

        for (TransferApplicationDTO.ListDTO obj : records) {
            //产品名称
            if (CollectionUtils.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
                obj.setProductName(productName);
            }
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
        }
    }

    /**
     * 处理数据id
     */
    private void doOpHandleDataId(String inWarehouseId, String outWarehouseId, String applyUserId, TransferApplicationEntity entity) {

        //申请人
        if (StringUtils.isNotBlank(applyUserId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(applyUserId);
            if (ObjectUtils.isEmpty(userDTO)) {
                throw new ServiceException(ApiError.ERROR_9011);
            }
            entity.setApplyUserName(userDTO.getUserName());
        }
        //仓库信息
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(inWarehouseId,outWarehouseId));

        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //调入仓库
        String inWarehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getInWarehouseId())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
        entity.setInWarehouseName(inWarehouseName);
        //调出仓库
        String outWarehouseName = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getOutWarehouseName())).map(WarehouseDTO.UpdateDTO::getName).findFirst().orElse(null);
        entity.setOutWarehouseName(outWarehouseName);
    }

    /**
     * 根据ids查询数据
     */
    private List<TransferApplicationEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<TransferApplicationEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99043);
        }
        return list;
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(TransferApplicationEntity::getId, ids)
                .set(TransferApplicationEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = commonService.getUserInfo();

        this.lambdaUpdate().in(TransferApplicationEntity::getId, ids)
                .set(TransferApplicationEntity::getApproveUserId, userInfo.getUid())
                .set(TransferApplicationEntity::getApproveUserName, userInfo.getUserName())
                .set(TransferApplicationEntity::getApproveStatus, approveStatus)
                .set(TransferApplicationEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
     * 反审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForDisApprove(List<String> ids, String approveStatus) {

        this.lambdaUpdate().in(TransferApplicationEntity::getId, ids)
                .set(TransferApplicationEntity::getApproveStatus, approveStatus)
                .set(TransferApplicationEntity::getApproveUserId, "")
                .set(TransferApplicationEntity::getApproveUserName, "")
                .set(TransferApplicationEntity::getApproveTime, null)
                .update();
    }
}

package com.erp.server.wms.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.BusinessNoConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PurchaseChangeListTypeEnum;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
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
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
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

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private PickingDetailService pickingDetailService;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private TransferOutService transferOutService;

    @Resource
    private TransferInfoDetailService transferInfoDetailService;

    @Resource
    private TransferOutDetailService transferOutDetailService;


    @Override
    public PagingVO<TransferApplicationDTO.ListDTO> paging(PagingDTO<TransferApplicationDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<TransferApplicationDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<TransferApplicationDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据处理
        doOpHandleData(records);
        List<String> list = new ArrayList<>();
        //清空明细数据
        records.forEach(obj -> {
            boolean contains = list.contains(obj.getId());
            if (contains) {
                obj.setCode(null);
                obj.setTransferDirection(null);
                obj.setTransferDirectionName(null);
                obj.setApproveStatusName(null);
                obj.setInvalidStatus(null);
                obj.setInvalidStatusName(null);
                obj.setApplyUserName(null);
                obj.setApproveUserName(null);
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
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
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
    @Transactional(rollbackFor = Exception.class)
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
            transferApplicationDetailService.add(dto.getDetailList(), entity.getId());
        }
        return entity.getId();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(TransferApplicationDTO.UpdateDTO dto) {

        TransferApplicationEntity old = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(old)) {
            throw new ServiceException(ApiError.ERROR_99043);
        }
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(old.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }

        TransferApplicationEntity entity = new TransferApplicationEntity();
        BeanMapperUtils.copy(dto, entity);
        List<TransferApplicationDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        //处理数据id
        doOpHandleDataId(dto.getInWarehouseId(), dto.getOutWarehouseId(), dto.getApplyUserId(), entity);

        log.info("调拨申请单修改，id=【{}】", dto.getId());

        //添加日志
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        transferApplicationDetailService.update(detailList, entity.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(TransferApplicationDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        //验证调出入仓库是否相同
        for (TransferApplicationEntity entity : list) {
            if (entity.getInWarehouseId().equals(entity.getOutWarehouseId())) {
                throw new ServiceException(new ApiResult(ApiError.ERROR_98069.code,String.format(ApiError.ERROR_98069.msg,entity.getCode())));
            }
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
        TransferApplicationDTO.ViewDTO viewDTO = new TransferApplicationDTO.ViewDTO();
        //主表信息
        TransferApplicationEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99043);
        }
        BeanMapperUtils.copy(entity, viewDTO);
        List<TransferApplicationDetailEntity> detailList = transferApplicationDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99044);
        }
        List<TransferApplicationDetailDTO.ViewDTO> viewDetailList = BeanMapperUtils.copyList(TransferApplicationDetailDTO.ViewDTO.class, detailList);

        //调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());
        if (CollectionUtils.isNotEmpty(transferDirectionList)) {
            String name = transferDirectionList.stream().filter(obj -> obj.getValue().equals(viewDTO.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse(null);
            viewDTO.setTransferDirectionName(name);
        }

        //产品信息
        List<String> skuIds = detailList.stream().map(TransferApplicationDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        for (TransferApplicationDetailDTO.ViewDTO viewDetailDTO : viewDetailList) {
            //产品名称
            if (CollectionUtils.isNotEmpty(skuList)) {
                String productName = skuList.stream().filter(e -> e.getSkuId().equals(viewDetailDTO.getSkuId())).map(SkuVO::getSkuName).findFirst().orElse(null);
                viewDetailDTO.setProductName(productName);
            }
            //根据组织、仓库、sku查询可用库存
            Integer curInventoryQty = inventoryService.getUsableInventoryTotal(viewDTO.getOutWarehouseId(), viewDetailDTO.getSkuId());
            viewDetailDTO.setCurInventoryQty(curInventoryQty);
        }
        viewDTO.setDetailList(viewDetailList);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //待提交并且未作废允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
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
    @Transactional(rollbackFor = Exception.class)
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
        log.info("调拨申请单作废，ids=【{}】", JSONUtil.toJsonStr(ids));

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
    @Transactional(rollbackFor = Exception.class)
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
            log.info("调拨申请单【{}】审核通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));
            //审核通过 TODO(判断是否存在流程)

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(ids, ApproveStatusEnum.APPROVE.getStatus());
            //审核通过后生成拣货明细
            generatePickingDetail(list);
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            log.info("调拨申请单【{}】审核不通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(ids));
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(ids, ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(String.format("审核【%s】了一个调拨申请单", ApproveTypeEnum.getName(type)).concat("【%s】").concat(StringUtils.isNotBlank(baseApproveParamDTO.getComment()) ? String.format(",意见：%s", baseApproveParamDTO.getComment()) : ""), ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), pairList, "审核操作");
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> ids) {
        //根据ids查询
        List<TransferApplicationEntity> list = getList(ids);
        //已审核允许反审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        List<TransferInfoEntity> transferInfoList = transferInfoService.listBySourceIds(ids);
        if (CollectionUtils.isNotEmpty(transferInfoList)) {
            throw  new ServiceException(ApiError.ERROR_99045);
        }
        List<TransferOutEntity> transferOutList = transferOutService.listBySourceIds(ids);
        if (CollectionUtils.isNotEmpty(transferOutList)) {
            throw  new ServiceException(ApiError.ERROR_99046);
        }

        log.info("调拨申请单反审核，ids=【{}】", JSONUtil.toJsonStr(ids));

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //回扣库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.TRANSFER_APPLY,ids);
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);
        //删除拣货明细
        pickingDetailService.deleteBySourceId(ids);
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("反审核了一个调拨申请单【%s】", ModuleTypeEnum.TRANSFER_APPLICATION.getCode(), pairList, "反审核操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        doOpHandleData(list);
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
        List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> list = viewGenerateData(ids);
        return list;
    }

    @Override
    public List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> viewGenerateTransferOut(List<String> ids) {
        List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> list = viewGenerateData(ids);
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateTransferInfo(ValidList<TransferApplicationDTO.GenerateTransferInfoDTO> validList) {
        List<TransferApplicationDTO.GenerateTransferInfoDTO> list = validList.getList();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //生成下推单据
        generateTransferData(list,MathUtil.ZERO);
        return Boolean.TRUE;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateTransferOut(ValidList<TransferApplicationDTO.GenerateTransferInfoDTO> validList) {
        List<TransferApplicationDTO.GenerateTransferInfoDTO> list = validList.getList();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        //生成下推单据
        generateTransferData(list,MathUtil.ONE);
        return Boolean.TRUE;
    }

    /**
     * @description: 调拨申请单下推保存
     * @author Will
     * @date: 2023/5/30 16:15
     * @param list
     * @param type 0、直接调拨。1、分步式调出
     */
    private void generateTransferData(List<TransferApplicationDTO.GenerateTransferInfoDTO> list,Integer type) {
        //调拨申请单主表信息
        List<String> sourceIds = list.stream().map(TransferApplicationDTO.GenerateTransferInfoDTO::getSourceId).distinct().collect(Collectors.toList());
        List<TransferApplicationEntity> transferApplicationList = this.listByIds(sourceIds);
        if (CollectionUtils.isEmpty(transferApplicationList)) {
            throw new ServiceException(ApiError.ERROR_99043);
        }
        long count = transferApplicationList.stream().filter(obj -> !ApproveStatusEnum.APPROVE.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99064);
        }

        List<String> sourceDetailIds = list.stream().map(TransferApplicationDTO.GenerateTransferInfoDTO::getSourceDetailId).distinct().collect(Collectors.toList());
        //拣货明细信息
        List<PickingDetailEntity> detailList = pickingDetailService.listByIds(sourceDetailIds);

        //直接调拨明细
        List<TransferInfoDetailEntity> transferInfoDetailList = transferInfoDetailService.listSourceDetailIds(sourceDetailIds);
        //分步式调出明细
        List<TransferOutDetailEntity> transferOutDetailList = transferOutDetailService.listSourceDetailIds(sourceDetailIds);

        //根据来源id分组生成下推直接调拨单
        Map<String, List<TransferApplicationDTO.GenerateTransferInfoDTO>> map = list.stream().collect(Collectors.groupingBy(TransferApplicationDTO.GenerateTransferInfoDTO::getSourceId));

        for (Map.Entry<String, List<TransferApplicationDTO.GenerateTransferInfoDTO>> entry : map.entrySet()) {
            List<TransferApplicationDTO.GenerateTransferInfoDTO> value = entry.getValue();
            TransferApplicationDTO.GenerateTransferInfoDTO transferInfoDTO = value.get(0);

            //调拨方向
            String transferDirection = transferApplicationList.stream().filter(obj -> obj.getId().equals(transferInfoDTO.getSourceId())).map(TransferApplicationEntity::getTransferDirection).findFirst().orElse("");

            //直接调拨单
            if (MathUtil.ZERO.equals(type)) {
                TransferInfoDTO.AddDTO addInfoDTO = new TransferInfoDTO.AddDTO();
                BeanMapperUtils.copy(transferInfoDTO,addInfoDTO);
                addInfoDTO.setTransferDirection(transferDirection);
                addInfoDTO.setRemark(null);
                List<TransferInfoDetailDTO.AddDTO> addDetailList = new ArrayList<>();
                for (TransferApplicationDTO.GenerateTransferInfoDTO dto : value) {
                    //验证明细是否已经被调拨
                    checkGenerateTransfer(transferInfoDetailList,transferOutDetailList,detailList,dto);
                    TransferInfoDetailDTO.AddDTO addDetailDTO = new TransferInfoDetailDTO.AddDTO();
                    BeanMapperUtils.copy(dto,addDetailDTO);
                    addDetailList.add(addDetailDTO);
                }
                addInfoDTO.setDetailList(addDetailList);
                //新增直接调拨单
                transferInfoService.add(addInfoDTO);
            }

            //分步式调出单
            if (MathUtil.ONE.equals(type)) {
                //分步式调出单
                TransferOutDTO.AddDTO addOutDTO = new TransferOutDTO.AddDTO();
                BeanMapperUtils.copy(transferInfoDTO,addOutDTO);
                addOutDTO.setTransferDirection(transferDirection);
                addOutDTO.setRemark(null);
                List<TransferOutDetailDTO.AddDTO> addDetailList = new ArrayList<>();
                for (TransferApplicationDTO.GenerateTransferInfoDTO dto : value) {
                    //验证明细是否已经被调拨
                    checkGenerateTransfer(transferInfoDetailList,transferOutDetailList,detailList,dto);
                    TransferOutDetailDTO.AddDTO addDetailDTO = BeanMapperUtils.map(TransferOutDetailDTO.AddDTO.class,dto);
                    addDetailList.add(addDetailDTO);
                }
                addOutDTO.setDetailList(addDetailList);
                //新增直接调拨单
                transferOutService.add(addOutDTO);
            }
        }

    }

    /**
     * @description: 验证是否被调拨
     * @author Will
     * @date: 2023/5/18 10:10
     * @param transferInfoDetailList
     * @param transferOutDetailList
     * @param dto
     */
    private void checkGenerateTransfer (List<TransferInfoDetailEntity> transferInfoDetailList,List<TransferOutDetailEntity> transferOutDetailList,List<PickingDetailEntity> detailList,TransferApplicationDTO.GenerateTransferInfoDTO dto) {
        //来源明细id
        String sourceDetailId = dto.getSourceDetailId();
        //sku编码
        String skuNo = dto.getSkuNo();

        //拣货数量
        Integer pickingQty = detailList.stream().filter(obj -> obj.getId().equals(sourceDetailId)).map(PickingDetailEntity::getQty).findFirst().orElse(MathUtil.ZERO);

        //直接调拨数量
       Integer transferInfoQty = MathUtil.ZERO;

       //分步式调出数量
        Integer transferOutQty = MathUtil.ZERO;

        //直接调拨
        if (CollectionUtils.isNotEmpty(transferInfoDetailList)) {
            //已调拨数量
            transferInfoQty = transferInfoDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(sourceDetailId)).map(TransferInfoDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            if (pickingQty.intValue() == transferInfoQty.intValue()) {
                throw new ServiceException(ApiError.ERROR_99051.code, String.format(ApiError.ERROR_99051.msg,dto.getSourceCode(), skuNo));
            }
        }
        //分步式调出
        if (CollectionUtils.isNotEmpty(transferOutDetailList)) {
            //已调拨数量
            transferOutQty = transferOutDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(sourceDetailId)).map(TransferOutDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            if (pickingQty.intValue() == transferOutQty.intValue()) {
                throw new ServiceException(ApiError.ERROR_99055.code, String.format(ApiError.ERROR_99055.msg,dto.getSourceCode(), skuNo));
            }
        }
        //调拨数量校验（直接调拨数量+分步式调出数量+本次调拨数量 不能大于 拣货数量）
        if (transferInfoQty.intValue() + transferOutQty.intValue() + dto.getQty().intValue() > pickingQty.intValue()) {
            throw new ServiceException(ApiError.ERROR_99050.code, String.format(ApiError.ERROR_99050.msg,dto.getSourceCode(), skuNo, pickingQty.intValue() - transferOutQty.intValue() - transferOutQty.intValue()));

        }
    }

    @Override
    public List<PickingDetailDTO.ListDTO> listPickingDetail(PickingDetailDTO.SearchParamDTO dto) {
        return pickingDetailService.listPickingDetailBySourceId(dto);
    }


    /**
     * @description: 下推数据查询
     * @author Will
     * @date: 2023/5/12 9:12
     * @param ids
     * @return List<ViewGenerateTransferInfoDTO>
     */
    private List<TransferApplicationDTO.ViewGenerateTransferInfoDTO>  viewGenerateData(List<String> ids) {
        List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> list = baseMapper.viewGenerateTransferInfo(ids);
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<String> sourceDetailIds = list.stream().map(TransferApplicationDTO.ViewGenerateTransferInfoDTO::getSourceDetailId).collect(Collectors.toList());

        List<String> skuIds = list.stream().map(TransferApplicationDTO.ViewGenerateTransferInfoDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(skuIds);

        //调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());

        //直接调拨明细
        List<TransferInfoDetailEntity> transferInfoDetailList = transferInfoDetailService.listSourceDetailIds(sourceDetailIds);
        //分步式调出明细
        List<TransferOutDetailEntity> transferOutDetailList = transferOutDetailService.listSourceDetailIds(sourceDetailIds);


        List<TransferApplicationDTO.ViewGenerateTransferInfoDTO> resultList = new ArrayList<>();
        for (TransferApplicationDTO.ViewGenerateTransferInfoDTO dto : list ) {

            //直接调拨数量
            Integer transferInfoQty = MathUtil.ZERO;
            //分步式调出数量
            Integer transferOutQty = MathUtil.ZERO;

            //直接调拨
            if (CollectionUtils.isNotEmpty(transferInfoDetailList)) {
                //已调拨数量
                transferInfoQty = transferInfoDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(dto.getSourceDetailId())).map(TransferInfoDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //分步式调出
            if (CollectionUtils.isNotEmpty(transferOutDetailList)) {
                //已调拨数量
                transferOutQty = transferOutDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(dto.getSourceDetailId())).map(TransferOutDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            //完成调拨不显示
            if (transferInfoQty.intValue() + transferOutQty.intValue() >=  dto.getQty().intValue()) {
                continue;
            }

            //产品名称
            if (CollectionUtils.isNotEmpty(productDetailList)) {
                String productName = productDetailList.stream().filter(e -> e.getId().equals(dto.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
                dto.setProductName(productName);
            }
            //调拨方向名称
            if (CollectionUtils.isNotEmpty(transferDirectionList)) {
                String transferDirectionName = transferDirectionList.stream().filter(e -> e.getValue().equals(dto.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse("");
                dto.setTransferDirectionName(transferDirectionName);
            }

            dto.setSourceType(SourceTypeEnum.TRANSFER_APPLICATION.getCode());
            resultList.add(dto);
        }

        return resultList;
    }

    /**
     * @description: 生成拣货明细
     * @author Will
     * @date: 2023/5/12 10:45
     * @param list
     */
    private void generatePickingDetail (List<TransferApplicationEntity> list) {
        //生成拣货明细
        List<String> ids = list.stream().map(TransferApplicationEntity::getId).collect(Collectors.toList());
        List<TransferApplicationDetailEntity> detailList = transferApplicationDetailService.listByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99044);
        }
        //拣货明细集合
        List<PickingDetailDTO.CommonDTO> addList = new ArrayList<>();
        for (TransferApplicationEntity entity :list) {
            List<TransferApplicationDetailEntity> detailEntities = detailList.stream().filter(obj -> obj.getMainId().equals(entity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailEntities)) {
                throw new ServiceException(ApiError.ERROR_99044);
            }
            for (TransferApplicationDetailEntity detailEntity : detailEntities) {
                //查询可用库存生成拣货明细
                PickingDetailDTO.InventoryParamDTO dto = new PickingDetailDTO.InventoryParamDTO(entity.getOutOrgId(),entity.getOutOrgName(),entity.getOutWarehouseId(),
                        entity.getOutWarehouseName(),detailEntity.getSkuId(),detailEntity.getSkuNo(),detailEntity.getQty());
                List<InventoryEntity> inventoryList = inventoryService.listPickingDetailInventory(dto);

                List<PickingDetailDTO.CommonDTO> pickingDetailList = BeanMapperUtils.copyList(PickingDetailDTO.CommonDTO.class, inventoryList);

                List<InOutStockDTO>  inOutStockList = new ArrayList<>();
                for ( PickingDetailDTO.CommonDTO addDTO : pickingDetailList) {
                    addDTO.setSourceId(entity.getId());
                    addDTO.setSourceCode(entity.getCode());
                    addDTO.setSourceType(SourceTypeEnum.TRANSFER_APPLICATION.getCode());
                    addDTO.setSourceDetailId(detailEntity.getId());
                    addDTO.setUnit(detailEntity.getUnit());
                    addDTO.setWarehouseName(entity.getOutWarehouseName());
                    addDTO.setOrgName(entity.getOutOrgName());

                    //调拨操作请求实体
                    InOutStockDTO inOutStockDTO = new InOutStockDTO();
                    inOutStockDTO.setSourceType(InventorySourceTypeEnum.TRANSFER_APPLY);
                    inOutStockDTO.setSourceId(entity.getId());
                    inOutStockDTO.setSourceCode(entity.getCode());
                    inOutStockDTO.setSourceDetailId(detailEntity.getId());
                    inOutStockDTO.setBillDate(entity.getBillDate());
                    inOutStockDTO.setSkuId(addDTO.getSkuId());
                    inOutStockDTO.setSkuNo(addDTO.getSkuNo());
                    inOutStockDTO.setQty(addDTO.getQty());
                    inOutStockDTO.setWarehouseId(entity.getOutWarehouseId());
                    inOutStockDTO.setWarehouseLocation(addDTO.getWarehouseLocation());
                    inOutStockList.add(inOutStockDTO);
                }
                addList.addAll(pickingDetailList);

                //减少可用库存，添加冻结库存
                InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
                inventoryInOutStockDTO.setMembers(inOutStockList);
                inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.TRANSFER_APPLY.getCode());
                //更新库存
                inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
            }
        }
        //添加拣货明细数据
        pickingDetailService.add(addList);
    }

    /**
     * @param records
     * @description: 处理数据
     * @author Will
     * @date: 2023/4/19 18:58
     */
    private void doOpHandleData(List<TransferApplicationDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(TransferApplicationDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);
        if (CollectionUtils.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //调拨方向
        List<DictBasicDTO.ListDTO> transferDirectionList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_DIRECTION.getKey());
        if (CollectionUtils.isEmpty(transferDirectionList)) {
            throw new ServiceException(ApiError.ERROR_99049);
        }

        for (TransferApplicationDTO.ListDTO obj : records) {
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
            if (StringUtils.isBlank(productName)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            obj.setProductName(productName);

            //调拨方向名称
            String transferDirectionName = transferDirectionList.stream().filter(e -> e.getValue().equals(obj.getTransferDirection())).map(DictBasicDTO.ListDTO::getName).findFirst().orElse("");
            if (StringUtils.isBlank(transferDirectionName)) {
                throw new ServiceException(ApiError.ERROR_99049);
            }
            obj.setTransferDirectionName(transferDirectionName);

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
            if (ObjectUtils.isNotEmpty(userDTO)) {
                entity.setApplyUserName(userDTO.getUserName());
            }
        }
        //仓库信息
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Arrays.asList(inWarehouseId,outWarehouseId));

        if (CollectionUtils.isEmpty(warehouseList)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //调入仓库
        WarehouseEntity inWarehouse = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getInWarehouseId())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(inWarehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //调出仓库
        WarehouseEntity outWarehouse = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getOutWarehouseId())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(outWarehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(inWarehouse.getOrgId(), outWarehouse.getOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        entity.setInWarehouseName(inWarehouse.getName());
        entity.setInOrgId(inWarehouse.getOrgId());
        //调入组织名称
        String inOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(inWarehouse.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        entity.setInOrgName(inOrgName);
        entity.setOutOrgId(outWarehouse.getOrgId());
        entity.setOutWarehouseName(outWarehouse.getName());
        //调出组织名称
        String outOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(outWarehouse.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        entity.setOutOrgName(outOrgName);
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

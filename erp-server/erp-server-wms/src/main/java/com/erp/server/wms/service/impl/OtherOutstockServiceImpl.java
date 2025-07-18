package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.DictKindgeeConstant;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.constant.CfgApiAuthContant;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.sys.dto.DictKingdeeDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.excel.OtherOutStockImportExcelDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import com.erp.model.wms.enums.OutstockTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpPushWdtFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.convert.OtherOutStockConverter;
import com.erp.server.wms.kingdee.SyncKingdeeOtherOutstockService;
import com.erp.server.wms.listener.OtherOutStockExcelListener;
import com.erp.server.wms.mapper.OtherOutstockMapper;
import com.erp.server.wms.query.OtherOutstockQueryHandler;
import com.erp.server.wms.service.*;
import com.erp.server.wms.wdt.SyncWdtOtherInStockService;
import com.erp.server.wms.wdt.SyncWdtOtherOutStockService;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.python.google.common.collect.Lists;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_OTHER_OUT_STOCK;

/**
 *  服务实现类
 *
 * @author will
 * @since 2023-05-10
 */
@Slf4j
@Service
public class OtherOutstockServiceImpl extends SuperServiceImpl<OtherOutstockMapper, OtherOutstockEntity> implements OtherOutstockService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private OtherOutstockDetailService otherOutstockDetailService;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private OtherOutstockCustomerService otherOutstockCustomerService;

    @Resource
    private SyncKingdeeOtherOutstockService syncKingdeeOtherOutstockService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private OtherOutstockQueryHandler otherOutstockQueryHandler;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private SyncWdtOtherOutStockService syncWdtOtherOutStockService;

    @Resource
    private SyncWdtOtherInStockService syncWdtOtherInStockService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private SysDictFeign sysDictFeign;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpPushWdtFeign dmpPushWdtFeign;
    @Resource
    private AbstractWdtService abstractWdtService;

    @Override
    public PagingVO<OtherOutstockDTO.ListDTO> paging(PagingDTO<OtherOutstockDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<OtherOutstockDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<OtherOutstockDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据处理
        doOpHandleData(records);
        return new PagingVO(pageData);
    }

    @Override
    public List<OtherOutstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PageListTypeEnum[] values = PageListTypeEnum.values();
        List<OtherOutstockDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PageListTypeEnum item : values) {
            if (PageListTypeEnum.WAIT_SUBMIT.equals(item)) {
                continue;
            }
            OtherOutstockDTO.SearchParamDTO searchParamDTO = new OtherOutstockDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            OtherOutstockDTO.ListStatusCountDTO resultDTO = new OtherOutstockDTO.ListStatusCountDTO();
            String tabSql = otherOutstockQueryHandler.getTabSql(item.getCode());
            HashMap<String,String> map = new HashMap<>();
            map.put("default",tabSql);
            searchParamDTO.setSqlMap(map);
            Integer count = this.baseMapper.listCount(searchParamDTO);
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setTabFlagName(item.getName());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String add(OtherOutstockDTO.AddDTO dto) {
        OtherOutstockEntity entity = new OtherOutstockEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        doOpHandleDataId(dto.getWarehouseId(), dto.getReceiveOrgId(), dto.getWarehouseKeeperId(),dto.getReceiverId(),dto.getDeptId(), entity);
        log.info("其他出库单新增");
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QTCK, BusinessNoTypeEnum.CODE_QTCK.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QTCK);
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个其他出库单【%s】", code), ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), entity.getId(), "新增操作");
            //新增客户信息
            otherOutstockCustomerService.add(dto.getOtherOutstockCustomer(),entity.getId());
            //新增明细
            otherOutstockDetailService.add(dto.getDetailList(), entity.getId());
        }
        return entity.getId();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String addAndSubmit(OtherOutstockDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        this.submit(id,Boolean.TRUE);
        return id;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String addAndApprove(OtherOutstockDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        this.submit(id,Boolean.FALSE);
        //审核
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setIds(Collections.singletonList(id));
        baseApproveParamDTO.setType(ApproveTypeEnum.PASS.getStatus());
        baseApproveParamDTO.setComment("");
        this.approve(id,baseApproveParamDTO.getType(),baseApproveParamDTO.getComment());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(OtherOutstockDTO.UpdateDTO dto) {
        OtherOutstockEntity old = this.getById(dto.getId());
        if (ObjectUtils.isEmpty(old)) {
            throw new ServiceException(ApiError.ERROR_99052);
        }
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(old.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }

        OtherOutstockEntity entity = new OtherOutstockEntity();
        BeanMapperUtils.copy(dto, entity);
        List<OtherOutstockDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        //处理数据id
        doOpHandleDataId(dto.getWarehouseId(), dto.getReceiveOrgId(), dto.getWarehouseKeeperId(),dto.getReceiverId(),entity.getDeptId(), entity);

        log.info("其他出库单修改，id=【{}】", dto.getId());

        //添加日志
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //更新其他出库客户
        otherOutstockCustomerService.update(dto.getOtherOutstockCustomer(),entity.getId());
        //更新明细数据
        otherOutstockDetailService.update(detailList, entity.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(OtherOutstockDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        BatchResultDTO submit = this.submit(dto.getId(), Boolean.TRUE);
        return  submit.getSuccess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO submit(String id,Boolean isProcess) {
        //根据ids查询
        OtherOutstockEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99061);
        }
        // 待提交或审核不通过并且未作废允许提交
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getCode().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("提交 开始修改其他出库单状态数据，id：【{}】", id);
        //更新审核状态
        updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动其他出库单流程，id=：【{}】", entity.getId());
        if (isProcess) {
            startProcess(entity);
        }
        // 记录操作日志
        log.info("提交 开始记录其他出库单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "其他出库单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    /**
     * 启动流程
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(OtherOutstockEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.OTHER_OUTSTOCK.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }

    @Override
    public OtherOutstockDTO.ViewDTO view(String id) {
        OtherOutstockDTO.ViewDTO viewDTO = new OtherOutstockDTO.ViewDTO();
        //主表信息
        OtherOutstockEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99061);
        }
        BeanMapperUtils.copy(entity, viewDTO);

        //库存方向
        viewDTO.setInventoryDirectionName(InventoryDirectionEnum.getName(viewDTO.getInventoryDirection()));
        viewDTO.setTypeName(this.getTypeNameByCode(viewDTO.getType()));
        viewDTO.setOutTypeName(this.getOutTypeNameByCode(viewDTO.getOutTypeName()));
        //客户信息
        OtherOutstockCustomerEntity customerEntity = otherOutstockCustomerService.getByMainId(id);
        if (ObjectUtils.isEmpty(customerEntity)) {
            throw new ServiceException(ApiError.ERROR_99063);
        }
        OtherOutstockCustomerDTO.UpdateDTO customerDTO = new OtherOutstockCustomerDTO.UpdateDTO();
        BeanMapperUtils.copy(customerEntity,customerDTO);
        viewDTO.setOtherOutstockCustomer(customerDTO);

        //明细信息
        List<OtherOutstockDetailEntity> detailList = otherOutstockDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99062);
        }
        List<OtherOutstockDetailDTO.ViewDTO> viewDetailList = BeanMapperUtils.copyList(OtherOutstockDetailDTO.ViewDTO.class, detailList);

        //产品信息
        List<String> skuIds = detailList.stream().map(OtherOutstockDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Collections.singletonList(entity.getWarehouseId()));

        //组织
        InventoryDTO.ParamDTO param = new InventoryDTO.ParamDTO();
        param.setOrgIdList(Collections.singletonList(viewDTO.getInventoryOrgId()));
        param.setSkuIdList(skuIds);
        param.setWarehouseIdList(Collections.singletonList(viewDTO.getWarehouseId()));
        //库存信息
        List<InventoryEntity> inventoryInfoList = inventoryService.listInventoryByParam(param);

        for (OtherOutstockDetailDTO.ViewDTO viewDetailDTO : viewDetailList) {
            //产品名称
            if (CollectionUtils.isNotEmpty(skuList)) {
                SkuVO skuVO = skuList.stream().filter(e -> e.getSkuId().equals(viewDetailDTO.getSkuId())).findFirst().orElse(new SkuVO());
                viewDetailDTO.setProductName(skuVO.getSkuName());
                viewDetailDTO.setVariantProperty(skuVO.getVariantProperty());
            }
            //根据组织、仓库、sku查询可用库存
            Integer curInventoryQty = inventoryInfoList.stream().filter(obj -> obj.getSkuId().equals(viewDetailDTO.getSkuId())
                            && InventoryStatusEnum.USABLE.getCode().equals(obj.getDictInventoryStatus())
                    && obj.getWarehouseLocation().equals(viewDetailDTO.getWarehouseLocation()))
                    .map(InventoryEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
            viewDetailDTO.setCurInventoryQty(curInventoryQty);
            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(viewDetailDTO.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            viewDetailDTO.setWarehouseLocationName(warehouseLocationEntity.getName());
        }
        viewDTO.setDetailList(viewDetailList);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<OtherOutstockEntity> list = getList(ids);
        //待提交并且未作废允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus() ).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("其他出库单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除其他出库客户
        otherOutstockCustomerService.removeByMainIds(ids);
        //删除明细数据
        otherOutstockDetailService.removeByMainIds(ids);
        //删除操作日志
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的其他出库单", UserContext.getDefaultLoginUser().getUserName(), list.stream().map(OtherOutstockEntity::getCode).collect(Collectors.joining(",")));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), pairList, "删除操作");

        //发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_DELETE.getCode());
        //删除主表数据
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean invalid(List<String> ids, String reason) {
        //根据ids查询
        List<OtherOutstockEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("其他出库单作废，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新
        lambdaUpdate().in(OtherOutstockEntity::getId, ids)
                .set(OtherOutstockEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(OtherOutstockEntity::getInvalidRemark, reason)
                .update();

        //发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_INVALID.getCode());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个其他出库单【%s】，作废原因：".concat(reason), ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO approve(String id, String type, String comment) {
        //根据id查询
        OtherOutstockEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99061);
        }
        //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        ApproveOneDTO dto = new ApproveOneDTO(id, type, comment);
        approveProcess(entity, dto);
        log.info("其他出库单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(id));

        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个其他出库单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(CharSequenceUtil.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"其他出库单审核");
    }

    /**
     * 审核流程处理
     * @param entity
     * @param dto
     */
    private void approveProcess(OtherOutstockEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.OTHER_OUTSTOCK.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.ApproveResultDTO> approveResult = workflowFeign.approve(approveDTO);
        Integer code = approveResult.getCode();
        if (200 != code) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        ProcessManagementDTO.ApproveResultDTO data = approveResult.getData();
        if (ObjectUtil.isEmpty(data.getIsExistProcess()) || !data.getIsExistProcess()) {
            // 无需走流程的数据则直接更新状态
            approveEnd(dto, entity);
        }
    }

    /**
     * variablesMap值赋值
     * @author will
     * @date 2025/5/21 10:51
     * @param entity
     * @return Map<String,Object>
     */
    private Map<String,Object> getVariablesMap(OtherOutstockEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<OtherOutstockDetailEntity> detailList = otherOutstockDetailService.listByMainId(entity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99048);
        }
        variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));

        //出库客户信息
        OtherOutstockCustomerEntity otherOutstockCustomerEntity = otherOutstockCustomerService.getByMainId(entity.getId());
        if (ObjectUtil.isEmpty(otherOutstockCustomerEntity)) {
            throw new ServiceException(ApiError.ERROR_99063);
        }
        Map<String, Object> customerMap = BeanUtil.beanToMap(otherOutstockCustomerEntity);
        variablesMap.putAll(customerMap);
        //SKU
        String skuNo = detailList.stream().map(OtherOutstockDetailEntity::getSkuNo).collect(Collectors.joining(","));
        variablesMap.put("skuNo", skuNo);
        //总计数量
        Integer actualQtyTotal = detailList.stream().map(OtherOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
        variablesMap.put("actualQtyTotal", actualQtyTotal);
        return variablesMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, OtherOutstockEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }

        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        //更新单据状态
        updateApproveStatusForApprove(Collections.singletonList(entity.getId()), approveStatus.getStatus());

        //审核通过
        if (dto.getType().equals(ApproveType.PASS)) {
            //更新库存
            updateInventoryTransCore(entity);

            //发送金蝶
            sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());
            //同步旺店通
            if("ordinary".equalsIgnoreCase(entity.getInventoryDirection())){
                syncApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_APPROVE);
            }else {
                syncDisApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_APPROVE);
            }
        }
        return Boolean.TRUE;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO disApprove(String id) {
        //根据id查询
        OtherOutstockEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99061);
        }
        //已审核允许反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        log.info("其他出库单反审核，ids=【{}】", JSONUtil.toJsonStr(id));

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatusForDisApprove(Collections.singletonList(id), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //回扣库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.OTHER_OUTSTOCK,Collections.singletonList(id));
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);

        //发送金蝶
        sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());

        //发送旺店通
        if("ordinary".equalsIgnoreCase(entity.getInventoryDirection())){
            syncDisApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_DISAPPROVE);
        }else {
            syncApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_DISAPPROVE);
        }

        //操作日志
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("反审核了一个其他出库单【{}】",entity.getCode()), ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"其他出库单反审核");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<OtherOutstockEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("其他出库单撤销流程，id=【{}】", ids);

        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.OTHER_OUTSTOCK.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("其他出库单【%s】取消流程", ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(OtherOutstockDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("其他出库单导出", EXPORT_WMS_OTHER_OUT_STOCK.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return  this.lambdaUpdate()
                .eq(OtherOutstockEntity::getId,id)
                .set(CharSequenceUtil.isNotBlank(syncKingdeeId),OtherOutstockEntity::getSyncKingdeeId,syncKingdeeId)
                .update();
    }

    /**
     * @description: 其他出库变更库存
     * @author Will
     * @date: 2023/5/22 17:26
     * @param entity
     */
    private void updateInventoryTransCore (OtherOutstockEntity entity) {
        //其他入库明细
        List<OtherOutstockDetailEntity> detailList = otherOutstockDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99062);
        }
        List<InOutStockDTO>  inOutStockList = new ArrayList<>();
        for (OtherOutstockDetailEntity detailEntity : detailList) {

            //操作请求实体
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.OTHER_OUTSTOCK);
            inOutStockDTO.setSourceId(entity.getId());
            inOutStockDTO.setSourceCode(entity.getCode());
            inOutStockDTO.setSourceDetailId(detailEntity.getId());
            inOutStockDTO.setBillDate(entity.getBillDate());
            inOutStockDTO.setSkuId(detailEntity.getSkuId());
            inOutStockDTO.setSkuNo(detailEntity.getSkuNo());
            inOutStockDTO.setQty(detailEntity.getActualQty());
            inOutStockDTO.setWarehouseId(entity.getWarehouseId());
            inOutStockDTO.setWarehouseLocation(detailEntity.getWarehouseLocation());
            inOutStockList.add(inOutStockDTO);
        }
        //其他出库减少库存
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setParamList(inOutStockList);
        //根据库存方向判断
        String code = InventoryBusinessTypeEnum.OTHER_OUT.getCode();
        if (InventoryDirectionEnum.RETURN_GOODS.getCode().equals(entity.getInventoryDirection())) {
            code = InventoryBusinessTypeEnum.OTHER_OUT_RETURN_GOODS.getCode();
        }
        inventoryInOutStockDTO.setBusinessType(code);
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
    }

    /**
     * @description: 列表数据格式化
     * @author Will
     * @date: 2023/5/19 15:15
     * @param records
     */
    private void doOpHandleData(List<OtherOutstockDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(OtherOutstockDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);
        List<DictKingdeeDTO.ListDTO> typeList = this.kingdeeTypeListByTypeName(DictKindgeeConstant.OTHER_TYPE_NAME);
        List<DictKingdeeDTO.ListDTO> outTypeList = this.kingdeeTypeListByTypeName(DictKindgeeConstant.OTHER_OUT_TYPE_NAME);

        //最新审核人
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = listCurApprove(records);

        for (OtherOutstockDTO.ListDTO obj : records) {
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
            obj.setProductName(productName);

            obj.setTypeName(typeList.stream().filter(v->v.getCode().equals(obj.getType())).findFirst().orElse(new DictKingdeeDTO.ListDTO()).getName());
            if(CharSequenceUtil.isBlank(obj.getTypeName())){
                //历史数据
                obj.setTypeName(EnumMessage.getNameByCode(OutstockTypeEnum.class,obj.getType()));
            }
            obj.setOutTypeName(outTypeList.stream().filter(v->v.getCode().equals(obj.getOutType())).findFirst().orElse(new DictKingdeeDTO.ListDTO()).getName());
            //库存方向名称
            obj.setInventoryDirectionName(InventoryDirectionEnum.getName(obj.getInventoryDirection()));
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(obj.getId()) && org.apache.commons.lang3.StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                obj.setApproveUserName(curApprove);
            }
        }
    }

    private void doOpHandleDataId (String warehouseId, String receiveOrgId, String warehouseKeeperId,String receiverId,String deptId, OtherOutstockEntity entity) {
        //用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(warehouseKeeperId,receiverId));
        if (CollectionUtils.isNotEmpty(userList)) {
            //仓管员
            String warehouseKeeperName = userList.stream().filter(obj -> obj.getUserId().equals(warehouseKeeperId)).map(FindUserDTO::getUserName).findFirst().orElse("");
            entity.setWarehouseKeeperName(warehouseKeeperName);
            //领料员
            String receiveOrgName = userList.stream().filter(obj -> obj.getUserId().equals(receiverId)).map(FindUserDTO::getUserName).findFirst().orElse("");
            entity.setReceiverName(receiveOrgName);
        }
        //仓库信息
        WarehouseEntity warehouseEntity = warehouseService.getById(warehouseId);
        if  (ObjectUtils.isEmpty(warehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        entity.setWarehouseName(warehouseEntity.getName());
        //库存组织
        String inventoryOrgId = warehouseEntity.getOrgId();
        entity.setInventoryOrgId(inventoryOrgId);
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(inventoryOrgId, receiveOrgId));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        //库存组织名称
        String inventoryOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(inventoryOrgId)).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        entity.setInventoryOrgName(inventoryOrgName);
        //收料组织名称
        String receiveOrgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(receiveOrgId)).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        entity.setReceiveOrgName(receiveOrgName);

        //部门信息
        if (CharSequenceUtil.isNotBlank(deptId)) {
            SysDepartmentDTO sysDepartmentDTO = sysUserFeign.getUserDeptById(deptId);
            if (ObjectUtils.isNotEmpty(sysDepartmentDTO)) {
                entity.setDeptName(sysDepartmentDTO.getName());
            }
        }
    }

    /**
     * 根据ids查询数据
     */
    private List<OtherOutstockEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<OtherOutstockEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99061);
        }
        return list;
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(String id, String approveStatus) {
        //更新审核状态
        lambdaUpdate().eq(OtherOutstockEntity::getId, id)
                .set(OtherOutstockEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        this.lambdaUpdate().in(OtherOutstockEntity::getId, ids)
                .set(OtherOutstockEntity::getApproveUserId, userInfo.getUid())
                .set(OtherOutstockEntity::getApproveUserName, userInfo.getUserName())
                .set(OtherOutstockEntity::getApproveStatus, approveStatus)
                .set(OtherOutstockEntity::getApproveTime, LocalDateTime.now())
                .update();
    }

    /**
     * 反审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForDisApprove(List<String> ids, String approveStatus) {

        this.lambdaUpdate().in(OtherOutstockEntity::getId, ids)
                .set(OtherOutstockEntity::getApproveStatus, approveStatus)
                .set(OtherOutstockEntity::getApproveUserId, "")
                .set(OtherOutstockEntity::getApproveUserName, "")
                .set(OtherOutstockEntity::getApproveTime, null)
                .update();
    }

    @Override
    public PagingVO<OtherOutstockDTO.PdaListDTO> PdaPaging(PagingDTO<OtherOutstockDTO.PdaSearchParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        OtherOutstockDTO.PdaSearchParamDTO params = pagingParamDTO.getParams();
        List<String> approveStatusList = params.getApproveStatusList();
        if (approveStatusList.contains(ApproveStatusEnum.APPROVE.getCode())) {
            List<LocalDate> dateList = new ArrayList<>();
            LocalDate now = LocalDate.now();
            dateList.add(now.minusDays(30));
            dateList.add(now);
            params.setBillDateList(dateList);
        }
        IPage<OtherOutstockDTO.PdaListDTO> pageData = this.baseMapper.pdaPaging(query, params);
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        List<OtherOutstockDTO.PdaListDTO> records = pageData.getRecords();
        //主键id
        List<String> ids = records.stream().map(req -> req.getId()).collect(Collectors.toList());
        //查询详情
        List<OtherOutstockDetailEntity> otherOutstockDetailEntities = otherOutstockDetailService.listByMainIds(ids);
        for (OtherOutstockDTO.PdaListDTO record : records) {
            record.setApproveStatusName(ApproveStatusEnum.getName(record.getApproveStatus()));
            List<OtherOutstockDetailEntity> detailEntities = otherOutstockDetailEntities.stream().filter(obj -> obj.getMainId().equals(record.getId())).collect(Collectors.toList());
            List<OtherOutstockDTO.PdaItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, OtherOutstockDTO.PdaItemDTO.class);
            record.setDetailCount(itemDTOList.size());
            record.setItemList(itemDTOList);
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<OtherOutstockDTO.PdaListStatusCountDTO> PdaListCount(PermissionsDTO dto) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        List<OtherOutstockDTO.PdaListStatusCountDTO> list = new ArrayList<>();
        for (PdaTabFlagEnum item : values) {
            OtherOutstockDTO.SearchParamDTO pagingParamDTO = new OtherOutstockDTO.SearchParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            OtherOutstockDTO.PdaListStatusCountDTO resultDTO = new OtherOutstockDTO.PdaListStatusCountDTO();
            Integer count = MathUtil.ZERO;
            if (PdaTabFlagEnum.WAIT_SUBMIT_AND_REJECT.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.REJECT.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE_ING.getCode().equals(item.getCode())) {
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE.getCode().equals(item.getCode())) {
                List<LocalDate> dateList = new ArrayList<>();
                dateList.add(startDate);
                dateList.add(endDate);
                pagingParamDTO.setBillDateList(dateList);
                pagingParamDTO.setApproveStatusList(Collections.singletonList(ApproveStatusEnum.APPROVE.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
            resultDTO.setTabFlag(item.getCode());
            list.add(resultDTO);
        }
        return list;
    }

    @Override
    public String generateByOverseasInbound(OverseasWarehouseInboundEntity entity, List<OverseasWarehouseInboundDetailEntity> detailEntityList, String remark,Boolean isOnwayWarehouse) {
        //目的仓
        WarehouseEntity destWarehouse = warehouseService.getById(entity.getToWarehouseId());
        //如果目的仓没有配置在途归属仓，需要提示：目的仓没有配置在途归属仓库，请在【仓库列表】配置后再审核
        if (CharSequenceUtil.isBlank(destWarehouse.getOnwayWarehouseId())) {
            throw new ServiceException(ApiError.ONWAY_WAREHOUSE_NOT_EXIST);
        }
        OtherOutstockDTO.AddDTO addDTO = this.buildLossMainDto(destWarehouse,isOnwayWarehouse,entity.getCreateUserId());
        List<OtherOutstockDetailDTO.AddDTO> detailAddDTOList = new ArrayList<>();
        for (OverseasWarehouseInboundDetailEntity detailEntity : detailEntityList) {
            OtherOutstockDetailDTO.AddDTO detailAddDTO = new OtherOutstockDetailDTO.AddDTO();
            detailAddDTO.setSkuId(detailEntity.getSkuId());
            detailAddDTO.setSkuNo(detailEntity.getSkuNo());
            detailAddDTO.setActualQty(Math.abs(detailEntity.getDiffQty()));
            detailAddDTO.setRemark(remark);
            detailAddDTOList.add(detailAddDTO);
        }
        addDTO.setDetailList(detailAddDTOList);
        return this.addAndApprove(addDTO);
    }

    /**
     * 封装报损出库单主记录
     */
    @Override
    public OtherOutstockDTO.AddDTO buildLossMainDto(WarehouseEntity warehouse,Boolean isOnwayWarehouse,String userId){
        //如果目的仓没有配置在途归属仓，需要提示：目的仓没有配置在途归属仓库，请在【仓库列表】配置后再审核
        if (CharSequenceUtil.isBlank(warehouse.getOnwayWarehouseId())) {
            throw new ServiceException(ApiError.ONWAY_WAREHOUSE_NOT_EXIST);
        }
        OtherOutstockDTO.AddDTO addDTO = new OtherOutstockDTO.AddDTO();
        //出库日期
        addDTO.setBillDate(LocalDate.now());
        //库存方向：普通
        addDTO.setInventoryDirection(InventoryDirectionEnum.ORDINARY.getCode());
        //发货仓库id
        addDTO.setWarehouseId(isOnwayWarehouse?warehouse.getOnwayWarehouseId():warehouse.getId());
        //部门
        SysDepartmentUserNumberDTO sysDepartmentUserNumberDTO =  sysUserFeign.getDeptByUserId(userId);
        addDTO.setDeptId(sysDepartmentUserNumberDTO.getDepartmentId());
        //处理类型
        List<DictKingdeeDTO.ListDTO> typeList = sysDictFeign.listByTypeName(DictKindgeeConstant.OTHER_TYPE_NAME);
        List<DictKingdeeDTO.ListDTO> outTypeList = sysDictFeign.listByTypeName(DictKindgeeConstant.OTHER_OUT_TYPE_NAME);
        DictKingdeeDTO.ListDTO typeDTO = typeList.stream().filter(v->v.getName().equals(DictKindgeeConstant.OTHER_OUT_MATERIAL_PICKING)).findFirst().orElse(new DictKingdeeDTO.ListDTO());
        // 业务类型
        addDTO.setType(typeDTO.getCode());
        addDTO.setTypeName(typeDTO.getName());
        //出库类型
        DictKingdeeDTO.ListDTO outTypeDTO = outTypeList.stream().filter(v->v.getName().equals(DictKindgeeConstant.OTHER_OUT_RECEIVE_THE_DIFFERENCE)).findFirst().orElse(new DictKingdeeDTO.ListDTO());
        addDTO.setOutType(outTypeDTO.getCode());
        addDTO.setOutTypeName(outTypeDTO.getName());

        addDTO.setReceiveOrgId(warehouse.getOrgId());
        return addDTO;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/otherOutstockTemplate.xlsx";
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
            log.error(" downloadTemplate 下载失败 e={}", e.getMessage());
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        OtherOutStockExcelListener excelListenerUtil = new OtherOutStockExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), OtherOutStockImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<OtherOutStockImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<OtherOutStockImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<OtherOutStockImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportOutStockFile(successList, errorList);

        if (errorList.isEmpty()) {
            return Boolean.TRUE;
        }
        String excelPath = "excel/otherOutstockError.xlsx";
        String name = "otherOutstockError";
        try {
            new ExcelPrintUtils().patchExport(errorList,
                    response,
                    StrUtil.builder().append(DateUtil.nowExcelFileFormat()).append(name).toString(),
                    excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95125);
        }
        return Boolean.FALSE;

    }

    public void handleImportOutStockFile (List<OtherOutStockImportExcelDTO> successList,List<OtherOutStockImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        List<String> skuNoList = successList.stream().map(OtherOutStockImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());

        // 库存方向Map
        Map<String, InventoryDirectionEnum> inventoryDirectionMap = Arrays.stream(InventoryDirectionEnum.values())
                .collect(Collectors.toMap(InventoryDirectionEnum::getName, Function.identity()));

        // 发货仓库
        List<String> warehouseNameList = successList.stream().map(OtherOutStockImportExcelDTO::getWarehouseName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listByNames(warehouseNameList);
        Map<String, WarehouseDTO.ListDTO> warehouseMap = warehouseList
                .stream()
                .collect(Collectors.toMap(WarehouseDTO.ListDTO::getName, Function.identity()));

        // 仓位
        List<String> warehouseIds = warehouseList.stream().map(WarehouseDTO.ListDTO::getId).collect(Collectors.toList());
        Map<String, List<WarehouseLocationEntity>> warehousrLocationMap = warehouseLocationService.listByWarehouseIds(warehouseIds)
                .stream()
                .collect(Collectors.groupingBy(WarehouseLocationEntity::getWarehouseId));

        // 领料人
        List<String> userNameList = successList.stream()
                .map(OtherOutStockImportExcelDTO::getReceiverName)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<FindUserDTO>> userMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userNameList)){
            userMap = sysUserFeign.listUserByUserNames(userNameList, UserTypeEnum.ERP.code)
                    .stream()
                    .collect(Collectors.groupingBy(FindUserDTO::getUserName));
        }

        // 领料部门
        List<SysDepartmentDTO> deptList = sysUserFeign.getDeptList();
        Map<String, List<SysDepartmentDTO>> deptMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(deptList)){
             deptMap = deptList
                    .stream()
                    .collect(Collectors.groupingBy(SysDepartmentDTO::getName));
        }

        // 客户名称
        List<String> customerNameList = successList.stream()
                .map(OtherOutStockImportExcelDTO::getCustomerName)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, CustomerDTO.ReceiveInfoDTO> customMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(customerNameList)){
            customMap = customerFeign.listDTOByNameList(customerNameList)
                    .stream()
                    .collect(Collectors.toMap(CustomerDTO.ReceiveInfoDTO::getName, Function.identity()));
        }

        // 领料组织
        Map<String, BaseIdDTO> orgMap = sysUserFeign.listAccountingCompany()
                .stream()
                .collect(Collectors.toMap(BaseIdDTO::getName, Function.identity()));

        //sku Map
        Map<String, SkuVO> existSkuMap = plmTaskFeign.listBySkuNoList(skuNoList)
                .stream()
                .collect(Collectors.toMap(SkuVO::getSkuNo, Function.identity()));

        //仓库配置
        CfgApiAuthEntity cfgApiAuthEntity = dmpTaskFeign.getByKey(new CfgApiAuthDTO.FeignDTO(CfgApiAuthContant.WAREHOUSE_LOCATION_VALIDATE));
        List<String> warehouseIdList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(cfgApiAuthEntity)) {
            CfgApiAuthDTO.WarehouseLocationValidateDTO warehouseLocationValidateDTO = JSONUtil.toBean(cfgApiAuthEntity.getValue(), CfgApiAuthDTO.WarehouseLocationValidateDTO.class);
            warehouseIdList = Arrays.stream(warehouseLocationValidateDTO.getWarehouseIds().split(",")).collect(Collectors.toList());
        }

        // 标记不可删除的skuId
        Set<String> signSkuIds = new HashSet<>();

        // 可保存处理的列表
        List<OtherOutstockEntity> canHandleList = new ArrayList<>();

        List<DictKingdeeDTO.ListDTO> typeList = this.kingdeeTypeListByTypeName(DictKindgeeConstant.OTHER_TYPE_NAME);
        List<DictKingdeeDTO.ListDTO> outTypeList = this.kingdeeTypeListByTypeName(DictKindgeeConstant.OTHER_OUT_TYPE_NAME);
        //获取当前操作人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        // 校验和处理
        for (OtherOutStockImportExcelDTO importExcelDTO : successList) {
            DictKingdeeDTO.ListDTO typeDTO = typeList.stream().filter(v->v.getName().equals(importExcelDTO.getType())).findFirst().orElse(null);
            if (null == typeDTO){
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】业务类型不存在", importExcelDTO.getType()));
                errorList.add(importExcelDTO);
                continue;
            }
            DictKingdeeDTO.ListDTO outTypeDTO = outTypeList.stream().filter(v->v.getName().equals(importExcelDTO.getOutType())).findFirst().orElse(new DictKingdeeDTO.ListDTO());

            Integer actualQty = Integer.valueOf(importExcelDTO.getActualQtyStr());

            LocalDate billDate = CharSequenceUtil.isBlank(importExcelDTO.getBillDateStr()) ? LocalDate.now() : LocalDate.parse(importExcelDTO.getBillDateStr(), DateTimeFormatter.ofPattern("yyyy/M/d"));

            // 库存方向Map
            InventoryDirectionEnum inventoryDirectionEnum = inventoryDirectionMap.get(importExcelDTO.getInventoryDirection());

            // 发货仓库
            WarehouseDTO.ListDTO warehouseDTO = warehouseMap.get(importExcelDTO.getWarehouseName());
            if (null == warehouseDTO){
                importExcelDTO.setErrorMsg(ApiError.WAREHOUSE_NOT_EXIST_NO_PERMISSION.msg);
                errorList.add(importExcelDTO);
                continue;
            }
            if (null != warehouseDTO.getApproveStatus() && !ApproveStatusEnum.APPROVE.equals(warehouseDTO.getApproveStatus())){
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】仓库未审核通过", importExcelDTO.getWarehouseName()));
                errorList.add(importExcelDTO);
                continue;
            }
            if (null != warehouseDTO.getDisabled() && warehouseDTO.getDisabled()){
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】仓库未启用", importExcelDTO.getWarehouseName()));
                errorList.add(importExcelDTO);
                continue;
            }

            String currentWarehouseId = warehouseDTO.getId();
            WarehouseLocationEntity locationEntity = null;
            if (CharSequenceUtil.isNotBlank(importExcelDTO.getWarehouseLocation())){
                List<WarehouseLocationEntity> locationList = warehousrLocationMap.get(currentWarehouseId);
                if (CollectionUtils.isEmpty(locationList)){
                    importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】仓位不存在", importExcelDTO.getWarehouseLocation()));
                    errorList.add(importExcelDTO);
                    continue;
                }
                locationEntity = locationList.stream().filter(e -> e.getName().equalsIgnoreCase(importExcelDTO.getWarehouseLocation())).findFirst().orElse(null);
                if (null == locationEntity){
                    importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】仓位不存在", importExcelDTO.getWarehouseLocation()));
                    errorList.add(importExcelDTO);
                    continue;
                }
            }

            // 领料人
            FindUserDTO userDTO = null;
            if (CharSequenceUtil.isNotBlank(importExcelDTO.getReceiverName())){
                List<FindUserDTO> userDTOList = userMap.get(importExcelDTO.getReceiverName());
                if (CollectionUtils.isEmpty(userDTOList)){
                    importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】领料人不存在", importExcelDTO.getReceiverName()));
                    errorList.add(importExcelDTO);
                    continue;
                } else {
                    userDTO = userDTOList.stream().findFirst().orElse(null);
                }
            }

            // 部领料门
            SysDepartmentDTO departmentDTO = null;
            List<SysDepartmentDTO> currrentDeptList = deptMap.get(importExcelDTO.getDeptName());
            if (!CollectionUtils.isEmpty(currrentDeptList)){
                departmentDTO = currrentDeptList.stream().findFirst().orElse(null);
            }
            if (null == departmentDTO){
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】领料部门不存在", importExcelDTO.getDeptName()));
                errorList.add(importExcelDTO);
                continue;
            }

            // 领料组织
            BaseIdDTO orgDTO = orgMap.get(importExcelDTO.getReceiveOrgName());
            if(null == orgDTO){
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】领料组织不存在", importExcelDTO.getReceiveOrgName()));
                errorList.add(importExcelDTO);
                continue;
            }

            // 产品SKU
            SkuVO skuVO = existSkuMap.get(importExcelDTO.getSkuNo());
            if (null == skuVO){
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("SKU【{}】不存在或未审核通过", importExcelDTO.getSkuNo()));
                errorList.add(importExcelDTO);
                continue;
            }
            if (null != skuVO.getStatus() && !Objects.equals(ProductDetailStatusEnum.APPROVAL_PASS.getCode(), skuVO.getStatus())){
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("SKU【{}】不存在或未审核通过", importExcelDTO.getSkuNo()));
                errorList.add(importExcelDTO);
                continue;
            }


            // 客户名称
            OtherOutstockCustomerEntity addCustomerEntity = new OtherOutstockCustomerEntity();
            if (CharSequenceUtil.isNotBlank(importExcelDTO.getCustomerName())){
                CustomerDTO.ReceiveInfoDTO customerDTO = customMap.get(importExcelDTO.getCustomerName());
                if (null == customerDTO){
                    importExcelDTO.setErrorMsg(CharSequenceUtil.format("客户【{}】不存在", importExcelDTO.getCustomerName()));
                    errorList.add(importExcelDTO);
                    continue;
                } else {
                    addCustomerEntity = OtherOutStockConverter.INSTANCE.convertCustomerEntity(customerDTO);
                }
            }

            //仓位必填验证
            //判断仓位是否需要必填
            if (warehouseIdList.contains(warehouseDTO.getId())) {
                if (CharSequenceUtil.isBlank(importExcelDTO.getWarehouseLocation()) || null == locationEntity){
                    importExcelDTO.setErrorMsg(CharSequenceUtil.format(" 仓库【{}】下仓位不能为空", importExcelDTO.getWarehouseName()));
                    errorList.add(importExcelDTO);
                    continue;
                }
                if (CharSequenceUtil.isBlank(locationEntity.getCode())){
                    importExcelDTO.setErrorMsg(CharSequenceUtil.format(" 仓库【{}】下仓位不能为空", importExcelDTO.getWarehouseName()));
                    errorList.add(importExcelDTO);
                    continue;
                }
            }

            try {
                // 主体
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QTCK);
                OtherOutstockEntity entity = OtherOutStockConverter.INSTANCE.combineAddEntity(
                        importExcelDTO,
                        typeDTO.getCode(),
                        outTypeDTO.getCode(),
                        billDate,
                        inventoryDirectionEnum,
                        warehouseDTO,
                        locationEntity,
                        departmentDTO,
                        userDTO,
                        orgDTO,
                        code,
                        "",
                        userInfo.getUid(),
                        userInfo.getUserName()
                );
                // 明细
                OtherOutstockDetailEntity detailEntity = OtherOutStockConverter.INSTANCE.combineDetailEntity(importExcelDTO, skuVO, locationEntity, actualQty);
                entity.setDetailEntityList(Collections.singletonList(detailEntity));
                // 客户信息
                entity.setCustomerEntity(addCustomerEntity);

                entity.setImportExcelDTO(importExcelDTO);
                canHandleList.add(entity);
                // 添加标记
                signSkuIds.add(skuVO.getSkuId());
            } catch (Exception e) {
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("转换异常【{}】", ExceptionUtil.stacktraceToOneLineString(e, 255)));
                errorList.add(importExcelDTO);
            }
        }

        // 数量分组
        List<List<OtherOutstockEntity>> partitionList = Lists.partition(canHandleList, 1000);

        // 批量保存
        for (List<OtherOutstockEntity> currentList : partitionList) {
            try {
                // 处理批量保存
                this.importBatchSave(currentList);
            } catch (Exception e) {
                List<OtherOutStockImportExcelDTO> errorSaveList = canHandleList.stream().map(dto  ->
                        {
                            OtherOutStockImportExcelDTO errorSaveImportExcelDTO = dto.getImportExcelDTO();
                            errorSaveImportExcelDTO.setErrorMsg(CharSequenceUtil.format("保存异常【{}】", ExceptionUtil.stacktraceToOneLineString(e, 255)));
                            return errorSaveImportExcelDTO;
                        }
                ).collect(Collectors.toList());
                errorList.addAll(errorSaveList);
            }
        }
        //标记SKU
        if (CollectionUtils.isNotEmpty(signSkuIds)) {
            plmTaskFeign.updateOccupyStatus(new ArrayList<>(signSkuIds));
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importBatchSave(List<OtherOutstockEntity> mainEntityList) {
        if (!this.saveBatch(mainEntityList)){
            throw new ServiceException("主信息批量保存失败");
        }
        // 明细
        List<OtherOutstockDetailEntity> detailEntityList = mainEntityList.stream().map(e -> {
            e.getDetailEntityList().forEach(detail -> detail.setMainId(e.getId()));
            return e.getDetailEntityList();
        }).flatMap(List::stream).collect(Collectors.toList());

        // 客户
        List<OtherOutstockCustomerEntity> customerEntityList = mainEntityList.stream().map(e -> {
            OtherOutstockCustomerEntity customerEntity = e.getCustomerEntity();
            customerEntity.setMainId(e.getId());
            return customerEntity;
        }).collect(Collectors.toList());

        List<Pair<String, String>> pairList = mainEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        // 批量保存操作日志
        operateLogService.batchAddModuleOperateLog("导入一个其他出库单【%s】", ModuleTypeEnum.OTHER_INSTOCK.getCode(),pairList,"新增操作");

        if (!otherOutstockDetailService.saveBatch(detailEntityList)){
            throw new ServiceException("明细信息批量保存失败");
        }

        if (!otherOutstockCustomerService.saveBatch(customerEntityList)){
            throw new ServiceException("客户信息批量保存失败");
        }
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<OtherOutstockEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeOtherOutstockService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(resultList);
            }
        });
    }

    /**
     * 同步审核操作到旺店通
     *
     * @param entity          其他出库单
     * @param syncOperateEnum 操作代码：approve/disApprove
     * @return
     * @author: tanmujin
     */
    private void syncApproveInfoToWdt(OtherOutstockEntity entity, SyncOperateEnum syncOperateEnum) {
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(Collections.singletonList(entity.getWarehouseId()), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        List<OtherOutstockDetailEntity> detailList = otherOutstockDetailService.listByMainId(entity.getId());
        List<CreateOtherStockoutRequest.GoodsList> goodsList = new ArrayList<>();
        for (OtherOutstockDetailEntity detailEntity : detailList) {
            CreateOtherStockoutRequest.GoodsList goods = new CreateOtherStockoutRequest.GoodsList();
            goods.setSpecNo(detailEntity.getSkuNo());
            goods.setNum(BigDecimal.valueOf(detailEntity.getActualQty()));
            goods.setPositionNo(detailEntity.getWarehouseLocation());
            goods.setWarehouseId(entity.getWarehouseId());
            goodsList.add(goods);
        }
        abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), goodsList, SourceTypeEnum.OTHER_OUTSTOCK);
    }

    /**
     * 将其他出库单的反审核操作转换为其他入库单推送到旺店通
     *
     * @param entity          其他出库单
     * @param syncOperateEnum
     * @return void
     * @date: 2024-05-20
     * @author: tanmujin
     */
    private void syncDisApproveInfoToWdt(OtherOutstockEntity entity, SyncOperateEnum syncOperateEnum) {
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(Collections.singletonList(entity.getWarehouseId()), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        List<OtherOutstockDetailEntity> detailList = otherOutstockDetailService.listByMainId(entity.getId());
        List<CreateOtherStockinRequest.GoodsList> goodsList = new ArrayList<>(detailList.size());
        for (OtherOutstockDetailEntity detailEntity : detailList) {
            CreateOtherStockinRequest.GoodsList goods = new CreateOtherStockinRequest.GoodsList();
            goods.setSpecNo(detailEntity.getSkuNo());
            goods.setNum(BigDecimal.valueOf(detailEntity.getActualQty()));
            goods.setPositionNo(detailEntity.getWarehouseLocation());
            goods.setWarehouseId(entity.getWarehouseId());
            goodsList.add(goods);
        }

        abstractWdtService.transfer(syncOperateEnum, entity.getId(), entity.getCode(), goodsList, SourceTypeEnum.OTHER_INSTOCK);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndAdd(OtherOutstockDTO.AddDTO generateDTO) {
        List<String> uniqueIds = generateDTO.getDetailList().stream().map(OtherOutstockDetailDTO.AddDTO::getRemark)
                .distinct()
                .collect(Collectors.toList());
        // 已存在不新增
        Integer count = otherOutstockDetailService.lambdaQuery()
                .in(OtherOutstockDetailEntity::getRemark, uniqueIds)
                .count();
        if (count > 0){
            log.warn("【{}】已生成其他出库单跳过", uniqueIds);
            return;
        }
        this.addAndApprove(generateDTO);
    }

    @Override
    public PagingVO<OtherOutstockDTO.ListDTO> exportOtherOutStock(PagingDTO<OtherOutstockDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<OtherOutstockDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            doOpHandleData(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<OtherOutstockEntity> listByCodes(List<String> list) {
        return this.list(new QueryWrapper<OtherOutstockEntity>().lambda().in(OtherOutstockEntity::getCode, list).eq(OtherOutstockEntity::getIsDeleted, Boolean.FALSE));
    }

    @Override
    public void updateApproveStatus(OtherOutstockDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO) {
        String approveStatus = updateApprovalStatusDTO.getApproveStatus();
        OtherOutstockEntity otherOutstockEntity = updateApprovalStatusDTO.getOtherOutstockEntity();
        this.lambdaUpdate().in(OtherOutstockEntity::getId, Collections.singletonList( otherOutstockEntity.getId()))
                .set(OtherOutstockEntity::getApproveUserId, otherOutstockEntity.getApproveUserId())
                .set(OtherOutstockEntity::getApproveUserName, otherOutstockEntity.getApproveUserName())
                .set(OtherOutstockEntity::getApproveStatus, approveStatus)
                .set(OtherOutstockEntity::getApproveTime, otherOutstockEntity.getApproveTime())
                .update();
    }

    private String getTypeNameByCode(String code){
        if(CharSequenceUtil.isBlank(code)){
            return "";
        }
        DictKingdeeDTO.ListDTO listDTO =  sysDictFeign.getByCode(DictKindgeeConstant.OTHER_TYPE_NAME,code);
        if(CharSequenceUtil.isBlank(listDTO.getName())){
            //历史数据
            return EnumMessage.getNameByCode(OutstockTypeEnum.class,code);
        }
        return listDTO.getName();
    }

    private List<DictKingdeeDTO.ListDTO> kingdeeTypeListByTypeName(String typeName){
        if(CharSequenceUtil.isBlank(typeName)){
            return new ArrayList<>();
        }
        List<DictKingdeeDTO.ListDTO> list = sysDictFeign.listByTypeName(typeName);
        return list;
    }

    private String getOutTypeNameByCode(String code){
        if(CharSequenceUtil.isBlank(code)){
            return "";
        }
        DictKingdeeDTO.ListDTO listDTO =  sysDictFeign.getByCode(DictKindgeeConstant.OTHER_OUT_TYPE_NAME,code);
        return listDTO.getName();
    }


    /**
     * 查询当前审核人
     * @author will
     * @date 2025/7/17 14:53
     * @param records
     * @return ApiResult<List<CurApproveInfoDTO>>
     */
    private  ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listCurApprove (List<OtherOutstockDTO.ListDTO> records) {
        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        records.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.OTHER_OUTSTOCK.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }
        return listApiResult;
    }
}

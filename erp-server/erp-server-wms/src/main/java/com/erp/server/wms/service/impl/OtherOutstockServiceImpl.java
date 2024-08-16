package com.erp.server.wms.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.rpc.dmp.feign.DmpPushWdtFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.DictKindgeeConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.constant.CfgApiAuthContant;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
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
import com.erp.model.wms.dto.OtherOutstockCustomerDTO;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.dto.OtherOutstockDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
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
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        this.submit(Arrays.asList(id));
        return id;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String addAndApprove(OtherOutstockDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        this.submit(Arrays.asList(id));
        //审核
        BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
        baseApproveParamDTO.setIds(Arrays.asList(id));
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
        return this.submit(Arrays.asList(dto.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        //根据ids查询
        List<OtherOutstockEntity> list = getList(ids);
        //待提交或审核不通过并且未作废允许提交
        long count = list.stream().filter(obj -> (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("其他出库单提交，ids=【{}】", JSONUtil.toJsonStr(ids));

        //启动流程 TODO

        //更新审核状态
        updateApproveStatus(ids, ApproveStatusEnum.APPROVE_ING.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("提交了一个其他出库单【%s】", ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), pairList, "提交操作");
        return Boolean.TRUE;
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
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Arrays.asList(entity.getWarehouseId()));

        //组织
        InventoryDTO.ParamDTO param = new InventoryDTO.ParamDTO();
        param.setOrgIdList(Arrays.asList(viewDTO.getInventoryOrgId()));
        param.setSkuIdList(skuIds);
        param.setWarehouseIdList(Arrays.asList(viewDTO.getWarehouseId()));
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
        String msg = StrUtil.format("用户【{}】删除了单据编号为【{}】的其他出库单", UserContext.getDefaultLoginUser().getUserName(), list.stream().map(OtherOutstockEntity::getCode).collect(Collectors.joining(",")));
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

        log.info("其他出库单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(id));

        //审核通过
        if (ApproveTypeEnum.PASS.getStatus().equals(type)) {
            log.info("其他出库单【{}】审核通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(id));
            //审核通过 TODO(判断是否存在流程)

            //更新单据(后面有流程了调用监听可删)
            updateApproveStatusForApprove(Arrays.asList(id), ApproveStatusEnum.APPROVE.getStatus());
            //更新库存
            updateInventoryTransCore(entity);

            //发送金蝶
            sendPushTask(Arrays.asList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());
            //同步旺店通
            if(entity.getInventoryDirection().equalsIgnoreCase("ordinary")){
                syncApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_APPROVE);
            }else {
                syncDisApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_APPROVE);
            }
        } else if (ApproveTypeEnum.REJECT.getStatus().equals(type)) {
            log.info("其他出库单【{}】审核不通过，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(id));
            //中止当前审核流程

            //更新单据状态
            updateApproveStatusForApprove(Arrays.asList(id), ApproveStatusEnum.REJECT.getStatus());
        }
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个其他出库单【%s】", ApproveTypeEnum.getName(type),entity.getCode()).concat(StringUtils.isNotBlank(comment) ? String.format(",意见：%s", comment) : ""), ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), entity.getId(), "审核操作");
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"其他出库单审核");
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
        updateApproveStatusForDisApprove(Arrays.asList(id), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //回扣库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.OTHER_OUTSTOCK,Arrays.asList(id));
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);

        //发送金蝶
        sendPushTask(Arrays.asList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());

        //发送旺店通
        if(entity.getInventoryDirection().equalsIgnoreCase("ordinary")){
            syncDisApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_DISAPPROVE);
        }else {
            syncApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_DISAPPROVE);
        }

        //操作日志
        operateLogService.addModuleOperateLog(StrUtil.format("反审核了一个其他出库单【{}】",entity.getCode()), ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), entity.getId(), "反审核操作");
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
        workflowFeign.cancelProcess(ids);

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("其他出库单【%s】取消流程", ModuleTypeEnum.OTHER_OUTSTOCK.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(OtherOutstockDTO.SearchParamDTO dto, HttpServletResponse response) {
        List<OtherOutstockDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        doOpHandleData(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/otherOutstock.xlsx";
        String name = "其他出库单导出";
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
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return  this.lambdaUpdate()
                .eq(OtherOutstockEntity::getId,id)
                .set(StringUtils.isNotBlank(syncKingdeeId),OtherOutstockEntity::getSyncKingdeeId,syncKingdeeId)
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
        for (OtherOutstockDTO.ListDTO obj : records) {
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse(null);
            obj.setProductName(productName);

            obj.setTypeName(typeList.stream().filter(v->v.getCode().equals(obj.getType())).findFirst().orElse(new DictKingdeeDTO.ListDTO()).getName());
            if(StringUtils.isBlank(obj.getTypeName())){
                //历史数据
                obj.setTypeName(EnumMessage.getNameByCode(OutstockTypeEnum.class,obj.getType()));
            }
            obj.setOutTypeName(outTypeList.stream().filter(v->v.getCode().equals(obj.getOutType())).findFirst().orElse(new DictKingdeeDTO.ListDTO()).getName());
            //库存方向名称
            obj.setInventoryDirectionName(InventoryDirectionEnum.getName(obj.getInventoryDirection()));
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
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
        if (StringUtils.isNotBlank(deptId)) {
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
    private void updateApproveStatus(List<String> ids, String approveStatus) {
        //更新审核状态
        lambdaUpdate().in(OtherOutstockEntity::getId, ids)
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
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE_ING.getStatus()));
                count = this.baseMapper.pdaListCount(pagingParamDTO);
            }
            if (PdaTabFlagEnum.APPROVE.getCode().equals(item.getCode())) {
                List<LocalDate> dateList = new ArrayList<>();
                dateList.add(startDate);
                dateList.add(endDate);
                pagingParamDTO.setBillDateList(dateList);
                pagingParamDTO.setApproveStatusList(Arrays.asList(ApproveStatusEnum.APPROVE.getStatus()));
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
        if (org.apache.commons.lang3.StringUtils.isBlank(destWarehouse.getOnwayWarehouseId())) {
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
        if (org.apache.commons.lang3.StringUtils.isBlank(warehouse.getOnwayWarehouseId())) {
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
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
        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listApproveWarehouse();
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
        // 校验和处理
        for (OtherOutStockImportExcelDTO importExcelDTO : successList) {
            DictKingdeeDTO.ListDTO typeDTO = typeList.stream().filter(v->v.getName().equals(importExcelDTO.getType())).findFirst().orElse(new DictKingdeeDTO.ListDTO());
            DictKingdeeDTO.ListDTO outTypeDTO = outTypeList.stream().filter(v->v.getName().equals(importExcelDTO.getOutType())).findFirst().orElse(new DictKingdeeDTO.ListDTO());

            Integer actualQty = Integer.valueOf(importExcelDTO.getActualQtyStr());

            LocalDate billDate = StringUtils.isBlank(importExcelDTO.getBillDateStr()) ? LocalDate.now() : LocalDate.parse(importExcelDTO.getBillDateStr(), DateTimeFormatter.ofPattern("yyyy/M/d"));

            // 库存方向Map
            InventoryDirectionEnum inventoryDirectionEnum = inventoryDirectionMap.get(importExcelDTO.getInventoryDirection());

            // 发货仓库
            WarehouseDTO.ListDTO warehouseDTO = warehouseMap.get(importExcelDTO.getWarehouseName());
            if (null == warehouseDTO){
                importExcelDTO.setErrorMsg(StrUtil.format("【{}】仓库不存在", importExcelDTO.getWarehouseName()));
                errorList.add(importExcelDTO);
                continue;
            }
            if (null != warehouseDTO.getApproveStatus() && !ApproveStatusEnum.APPROVE.equals(warehouseDTO.getApproveStatus())){
                importExcelDTO.setErrorMsg(StrUtil.format("【{}】仓库未审核通过", importExcelDTO.getWarehouseName()));
                errorList.add(importExcelDTO);
                continue;
            }
            if (null != warehouseDTO.getDisabled() && warehouseDTO.getDisabled()){
                importExcelDTO.setErrorMsg(StrUtil.format("【{}】仓库未启用", importExcelDTO.getWarehouseName()));
                errorList.add(importExcelDTO);
                continue;
            }

            String currentWarehouseId = warehouseDTO.getId();
            WarehouseLocationEntity locationEntity = null;
            if (StringUtils.isNotBlank(importExcelDTO.getWarehouseLocation())){
                List<WarehouseLocationEntity> locationList = warehousrLocationMap.get(currentWarehouseId);
                if (CollectionUtils.isEmpty(locationList)){
                    importExcelDTO.setErrorMsg(StrUtil.format("【{}】仓位不存在", importExcelDTO.getWarehouseLocation()));
                    errorList.add(importExcelDTO);
                    continue;
                }
                locationEntity = locationList.stream().filter(e -> e.getName().equalsIgnoreCase(importExcelDTO.getWarehouseLocation())).findFirst().orElse(null);
                if (null == locationEntity){
                    importExcelDTO.setErrorMsg(StrUtil.format("【{}】仓位不存在", importExcelDTO.getWarehouseLocation()));
                    errorList.add(importExcelDTO);
                    continue;
                }
            }

            // 领料人
            FindUserDTO userDTO = null;
            if (StringUtils.isNotBlank(importExcelDTO.getReceiverName())){
                List<FindUserDTO> userDTOList = userMap.get(importExcelDTO.getReceiverName());
                if (CollectionUtils.isEmpty(userDTOList)){
                    importExcelDTO.setErrorMsg(StrUtil.format("【{}】领料人不存在", importExcelDTO.getReceiverName()));
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
                importExcelDTO.setErrorMsg(StrUtil.format("【{}】领料部门不存在", importExcelDTO.getDeptName()));
                errorList.add(importExcelDTO);
                continue;
            }

            // 领料组织
            BaseIdDTO orgDTO = orgMap.get(importExcelDTO.getReceiveOrgName());
            if(null == orgDTO){
                importExcelDTO.setErrorMsg(StrUtil.format("【{}】领料组织不存在", importExcelDTO.getReceiveOrgName()));
                errorList.add(importExcelDTO);
                continue;
            }

            // 产品SKU
            SkuVO skuVO = existSkuMap.get(importExcelDTO.getSkuNo());
            if (null == skuVO){
                importExcelDTO.setErrorMsg(StrUtil.format("SKU【{}】不存在或未审核通过", importExcelDTO.getSkuNo()));
                errorList.add(importExcelDTO);
                continue;
            }
            if (null != skuVO.getStatus() && !Objects.equals(ProductDetailStatusEnum.APPROVAL_PASS.getCode(), skuVO.getStatus())){
                importExcelDTO.setErrorMsg(StrUtil.format("SKU【{}】不存在或未审核通过", importExcelDTO.getSkuNo()));
                errorList.add(importExcelDTO);
                continue;
            }


            // 客户名称
            OtherOutstockCustomerEntity addCustomerEntity = new OtherOutstockCustomerEntity();
            if (StringUtils.isNotBlank(importExcelDTO.getCustomerName())){
                CustomerDTO.ReceiveInfoDTO customerDTO = customMap.get(importExcelDTO.getCustomerName());
                if (null == customerDTO){
                    importExcelDTO.setErrorMsg(StrUtil.format("客户【{}】不存在", importExcelDTO.getCustomerName()));
                    errorList.add(importExcelDTO);
                    continue;
                } else {
                    addCustomerEntity = OtherOutStockConverter.INSTANCE.convertCustomerEntity(customerDTO);
                }
            }

            //仓位必填验证
            //判断仓位是否需要必填
            if (warehouseIdList.contains(warehouseDTO.getId())) {
                if (StringUtils.isBlank(importExcelDTO.getWarehouseLocation()) || null == locationEntity){
                    importExcelDTO.setErrorMsg(StrUtil.format(" 仓库【{}】下仓位不能为空", importExcelDTO.getWarehouseName()));
                    errorList.add(importExcelDTO);
                    continue;
                }
                if (StringUtils.isBlank(locationEntity.getCode())){
                    importExcelDTO.setErrorMsg(StrUtil.format(" 仓库【{}】下仓位不能为空", importExcelDTO.getWarehouseName()));
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
                        code
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
                importExcelDTO.setErrorMsg(StrUtil.format("转换异常【{}】", ExceptionUtil.stacktraceToOneLineString(e, 255)));
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
                            errorSaveImportExcelDTO.setErrorMsg(StrUtil.format("保存异常【{}】", ExceptionUtil.stacktraceToOneLineString(e, 255)));
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

    private String getTypeNameByCode(String code){
        if(StringUtils.isBlank(code)){
            return "";
        }
        DictKingdeeDTO.ListDTO listDTO =  sysDictFeign.getByCode(DictKindgeeConstant.OTHER_TYPE_NAME,code);
        if(StringUtils.isBlank(listDTO.getName())){
            //历史数据
            return EnumMessage.getNameByCode(OutstockTypeEnum.class,code);
        }
        return listDTO.getName();
    }

    private List<DictKingdeeDTO.ListDTO> kingdeeTypeListByTypeName(String typeName){
        if(StringUtils.isBlank(typeName)){
            return new ArrayList<>();
        }
        List<DictKingdeeDTO.ListDTO> list = sysDictFeign.listByTypeName(typeName);
        return list;
    }

    private String getOutTypeNameByCode(String code){
        if(StringUtils.isBlank(code)){
            return "";
        }
        DictKingdeeDTO.ListDTO listDTO =  sysDictFeign.getByCode(DictKindgeeConstant.OTHER_OUT_TYPE_NAME,code);
        return listDTO.getName();
    }

}

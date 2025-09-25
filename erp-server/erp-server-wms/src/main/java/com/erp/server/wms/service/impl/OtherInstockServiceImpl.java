package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataIdempotent;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.ApproveType;
import com.common.business.constant.ThirdConstants;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
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
import com.erp.model.dmp.dto.DmpSoPrestockDetailDTO;
import com.erp.model.dmp.dto.DmpSoPrestockInfoDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import com.erp.model.oms.dto.WorkflowTaskRecordDTO;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.PageListTypeEnum;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.model.wms.dto.OtherInstockDetailDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.OtherInStockImportExcelDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryBatchUnApproveDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.InstockTypeEnum;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpPushWdtFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ExhibitionOrderFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.convert.OtherInStockConverter;
import com.erp.server.wms.kingdee.SyncKingdeeOtherInstockService;
import com.erp.server.wms.listener.OtherInStockExcelListener;
import com.erp.server.wms.mapper.OtherInstockMapper;
import com.erp.server.wms.mapper.WdtWarehouseLocationMappingMapper;
import com.erp.server.wms.query.OtherInstockQueryHandler;
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
import org.springframework.context.annotation.Lazy;
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

import static cn.hutool.json.XMLTokener.entity;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_OTHER_IN_STOCK;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
@Slf4j
@Service
public class OtherInstockServiceImpl extends SuperServiceImpl<OtherInstockMapper, OtherInstockEntity> implements OtherInstockService {

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private OtherInstockDetailService otherInstockDetailService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @Resource
    private WorkflowFeign workflowFeign;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private SyncKingdeeOtherInstockService syncKingdeeOtherInstockService;

    @Resource
    private WarehouseLocationService warehouseLocationService;

    @Resource
    private OtherInstockQueryHandler otherInstockQueryHandler;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private SyncWdtOtherInStockService syncWdtOtherInstockService;

    @Resource
    private SyncWdtOtherOutStockService syncWdtOtherOutStockService;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private DmpPushWdtFeign dmpPushWdtFeign;
    @Resource
    private AbstractWdtService abstractWdtService;
    @Resource
    private WdtWarehouseLocationMappingMapper wdtWarehouseLocationMappingMapper;
    @Resource
    private SoOutstockService soOutstockService;

    @Lazy
    @Resource
    private OtherInstockService service;

    @Resource
    private ExhibitionOrderFeign exhibitionOrderFeign;

    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Override
    public PagingVO<OtherInstockDTO.ListDTO> paging(PagingDTO<OtherInstockDTO.SearchParamDTO> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        if (CollectionUtils.isNotEmpty(pagingDTO.getParams().getApproveStatusList())) {
            pagingDTO.getParams().setInvalidStatus(Boolean.FALSE);
        }
        IPage<OtherInstockDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingDTO.getParams());
        List<OtherInstockDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据处理
        doOpHandleData(records);
        return new PagingVO(pageData);
    }

    @Override
    public List<OtherInstockDTO.ListStatusCountDTO> listCount(PermissionsDTO dto) {
        PageListTypeEnum[] values = PageListTypeEnum.values();
        List<OtherInstockDTO.ListStatusCountDTO> list = new ArrayList<>();
        for (PageListTypeEnum item : values) {
            if (PageListTypeEnum.WAIT_SUBMIT.equals(item)) {
                continue;
            }
            OtherInstockDTO.SearchParamDTO searchParamDTO = new OtherInstockDTO.SearchParamDTO();
            searchParamDTO.setPermissionSql(dto.getPermissionSql());
            OtherInstockDTO.ListStatusCountDTO resultDTO = new OtherInstockDTO.ListStatusCountDTO();
            String tabSql = otherInstockQueryHandler.getTabSql(item.getCode());
            HashMap<String, String> map = new HashMap<>();
            map.put("default", tabSql);
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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public String addAndApprove(OtherInstockEntity entity, Boolean isPushWdt) {
        //生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QTRK);
        entity.setCode(code);
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个其他入库单【%s】", code), ModuleTypeEnum.OTHER_INSTOCK.getCode(), entity.getId(), "新增操作");
            String id = entity.getId();
            //新增明细
            entity.getDetailEntityList().forEach(v->v.setMainId(id));
            otherInstockDetailService.saveBatch(entity.getDetailEntityList());
            //标记SKU
            List<String> skuIds = entity.getDetailEntityList().stream().map(OtherInstockDetailEntity::getSkuId).collect(Collectors.toList());
            plmTaskFeign.updateOccupyStatus(skuIds);
            //提交
            this.submit(id,Boolean.FALSE);
            //审核
            BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
            baseApproveParamDTO.setIds(Collections.singletonList(id));
            baseApproveParamDTO.setType(ApproveTypeEnum.PASS.getStatus());
            baseApproveParamDTO.setComment("");
            this.approve(id, baseApproveParamDTO.getType(), baseApproveParamDTO.getComment(), isPushWdt);
            return id;
        }
        return entity.getId();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public String disApproveAndGenerate(String dbId, DmpSoPrestockInfoDTO.PrestockDTO dto) {
        service.disApprove(dbId, false);
        service.delete(Collections.singletonList(dbId));
        OtherInstockEntity otherInstockEntity = this.buildWdtPreStock(dto);
        return service.addAndApprove(otherInstockEntity, false);
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public String add(OtherInstockDTO.AddDTO dto) {
        OtherInstockEntity entity = new OtherInstockEntity();
        BeanMapperUtils.copy(dto, entity);
        //处理数据id
        doOpHandleDataId(dto.getWarehouseId(), dto.getReceiverId(), dto.getWarehouseKeeperId(), dto.getDeptId(), entity);
        log.info("其他入库单新增");
        //生成单号
//        String code = sysUserFeign.getBusinessNo(new SysCodeDTO(BusinessNoConstant.QTRK, BusinessNoTypeEnum.CODE_QTRK.getCode()));
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QTRK);
        entity.setCode(code);

        if(StringUtils.isBlank(entity.getSourceType())){
            //默认手动新增
            entity.setSourceType(SourceTypeEnum.SELF_ADD.getCode());
        }
        //新增主表数据
        boolean save = this.save(entity);
        if (save) {
            //操作日志
            operateLogService.addModuleOperateLog(String.format("新增了一个其他入库单【%s】", code), ModuleTypeEnum.OTHER_INSTOCK.getCode(), entity.getId(), "新增操作");
            //新增明细
            otherInstockDetailService.add(dto.getDetailList(), entity.getId());
        }
        return entity.getId();
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public String addAndSubmit(OtherInstockDTO.AddDTO dto) {
        //新增
        String id = this.add(dto);
        if (CharSequenceUtil.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        //提交
        this.submit(id,dto.getIsProcess());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(OtherInstockDTO.UpdateDTO dto) {
        OtherInstockEntity entity = new OtherInstockEntity();
        BeanMapperUtils.copy(dto, entity);
        List<OtherInstockDetailDTO.UpdateDTO> detailList = dto.getDetailList();
        //处理数据id
        doOpHandleDataId(dto.getWarehouseId(), dto.getReceiverId(), dto.getWarehouseKeeperId(), dto.getDeptId(), entity);

        log.info("其他入库单修改，id=【{}】", dto.getId());

        //添加日志
        OtherInstockEntity old = this.getById(dto.getId());
        if(!entity.getReturnLogisticCode().equals(old.getReturnLogisticCode())){
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("退货物流单号从{}修改为{}",old.getReturnLogisticCode(),entity.getReturnLogisticCode()), ModuleTypeEnum.OTHER_INSTOCK.getCode(), old.getId(), "编辑");
        }
        operateLogService.addModuleOperateLogByObj(old, entity, ModuleTypeEnum.OTHER_INSTOCK.getCode(), entity.getId(), "", "");
        //更新主表数据
        this.updateById(entity);
        //更新明细数据
        otherInstockDetailService.update(detailList, entity.getId());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(OtherInstockDTO.UpdateDTO dto) {
        //修改
        this.update(dto);
        //提交
        BatchResultDTO submit = this.submit(dto.getId(), Boolean.TRUE);
        return submit.getSuccess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO submit(String id,Boolean isProcess) {
        //根据ids查询
        OtherInstockEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99059);
        }
        // 待提交或审核不通过并且未作废允许提交
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getCode().equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.getCode().equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        log.info("提交 开始修改其他入库单状态数据，id：【{}】", id);
        //更新审核状态
        updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());
        
        log.info("提交 开始启动其他入库单流程，id=：【{}】", entity.getId());
        if (isProcess) {
            startProcess(entity);
        }
        // 记录操作日志
        log.info("提交 开始记录其他入库单日志数据，id：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "其他入库单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.OTHER_INSTOCK.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    /**
     * 启动流程
     * @param entity
     * @return void
     * @Date 2023/7/4 10:07
     **/

    public void startProcess(OtherInstockEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.OTHER_INSTOCK.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(getVariablesMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }


    @Override
    public OtherInstockDTO.ViewDTO view(String id) {
        OtherInstockDTO.ViewDTO viewDTO = new OtherInstockDTO.ViewDTO();
        //主表信息
        OtherInstockEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_99047);
        }
        BeanMapperUtils.copy(entity, viewDTO);
        List<OtherInstockDetailEntity> detailList = otherInstockDetailService.listByMainId(id);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99048);
        }
        List<OtherInstockDetailDTO.ViewDTO> viewDetailList = BeanMapperUtils.copyList(OtherInstockDetailDTO.ViewDTO.class, detailList);

        viewDTO.setTypeName(InstockTypeEnum.getByCode(entity.getType()));
        //库存方向
        viewDTO.setInventoryDirectionName(InventoryDirectionEnum.getName(viewDTO.getInventoryDirection()));

        //产品信息
        List<String> skuIds = detailList.stream().map(OtherInstockDetailEntity::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIds);
        List<WarehouseLocationEntity> warehouseLocationEntities = warehouseLocationService.listByWarehouseIds(Collections.singletonList(entity.getWarehouseId()));
        for (OtherInstockDetailDTO.ViewDTO viewDetailDTO : viewDetailList) {
            //产品名称
            if (CollectionUtils.isNotEmpty(skuList)) {
                SkuVO skuVO = skuList.stream().filter(e -> e.getSkuId().equals(viewDetailDTO.getSkuId())).findFirst().orElse(new SkuVO());
                viewDetailDTO.setProductName(skuVO.getSkuName());
                viewDetailDTO.setVariantProperty(skuVO.getVariantProperty());
            }


            WarehouseLocationEntity warehouseLocationEntity = warehouseLocationEntities.stream().filter(req -> req.getCode().equals(viewDetailDTO.getWarehouseLocation())).findFirst().orElse(new WarehouseLocationEntity());
            viewDetailDTO.setWarehouseLocationName(warehouseLocationEntity.getName());
        }
        viewDTO.setDetailList(viewDetailList);
        viewDTO.setApproveStatusName(ApproveStatusEnum.getName(viewDTO.getApproveStatus()));
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<BatchResultDTO> deleteByIds(List<String> ids, boolean returnDetails) {
        //根据ids查询
        List<OtherInstockEntity> list = getList(ids);
        //待提交并且未作废允许删除
//        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus()).count();
//        if (count > 0) {
//            throw new ServiceException(ApiError.ERROR_98009);
//        }
        List<OtherInstockEntity> removeList=new ArrayList<>();
        List<BatchResultDTO> resultDTOList=new ArrayList<>();
        for (OtherInstockEntity entity : list) {
            if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) || entity.getInvalidStatus()){
                resultDTOList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), ApiError.ERROR_98009.msg));
                continue;
            }
            removeList.add(entity);
            resultDTOList.add(BatchResultDTO.success(entity.getId(), entity.getCode(),"删除成功"));
        }
        List<String> removeIdList = removeList.stream().map(OtherInstockEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(removeIdList)){
            return resultDTOList;
        }
        log.info("其他入库单删除，ids=【{}】", JSONUtil.toJsonStr(removeIdList));
        //删除明细数据
        otherInstockDetailService.removeByMainIds(removeIdList);
        //删除操作日志
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的其他入库单", UserContext.getDefaultLoginUser().getUserName(), removeList.stream().map(OtherInstockEntity::getCode).collect(Collectors.joining(",")));
        List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.OTHER_INSTOCK.getCode(), pairList, "删除操作");
        //发送金蝶
        sendPushTask(removeList,SyncOperateEnum.OPERATE_DELETE.getCode());
        //删除主表数据
        boolean result = this.removeByIds(removeIdList);
        if (!result){
            throw new ServiceException(ApiError.ERROR_DATA_DELETE_ERROR);
        }
        // 返回成功结果
        return resultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean delete(List<String> ids) {
        //根据ids查询
        List<OtherInstockEntity> list = getList(ids);
        //待提交并且未作废允许删除
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) || obj.getInvalidStatus()).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        log.info("其他入库单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        otherInstockDetailService.removeByMainIds(ids);
        //删除操作日志
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的其他入库单", UserContext.getDefaultLoginUser().getUserName(), list.stream().map(OtherInstockEntity::getCode).collect(Collectors.joining(",")));
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.OTHER_INSTOCK.getCode(), pairList, "删除操作");
        //发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_DELETE.getCode());
        //删除主表数据
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO deleteEntity(OtherInstockEntity entity) {
        //待提交并且未作废允许删除
        if (!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus()) || entity.getInvalidStatus()) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        List<String> ids = Collections.singletonList(entity.getId());
        log.info("其他入库单删除，ids=【{}】", JSONUtil.toJsonStr(ids));
        //删除明细数据
        otherInstockDetailService.removeByMainIds(ids);
        //删除操作日志
        String msg = CharSequenceUtil.format("用户【{}】删除了单据编号为【{}】的其他入库单", UserContext.getDefaultLoginUser().getUserName(), entity.getCode());
        List<Pair<String, String>> pairList = Collections.singletonList(new Pair<>(entity.getId(), entity.getCode()));
        operateLogService.batchAddModuleOperateLog(msg, ModuleTypeEnum.OTHER_INSTOCK.getCode(), pairList, "删除操作");
        //发送金蝶
        sendPushTask(Collections.singletonList(entity), SyncOperateEnum.OPERATE_DELETE.getCode());
        //删除主表数据
        boolean result = this.removeByIds(ids);
        if (result) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "删除成功");
        } else {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "删除失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public Boolean invalid(List<String> ids, String reason) {
        //根据ids查询
        List<OtherInstockEntity> list = getList(ids);
        //非待提交和审核不通过不能作废
        long count = list.stream().filter(obj -> !ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(obj.getApproveStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98005);
        }
        long invalidCount = list.stream().filter(obj -> InvalidStatusEnum.VOIDED.getStatus().equals(obj.getInvalidStatus())).count();
        if (invalidCount > 0) {
            throw new ServiceException(ApiError.ERROR_98012);
        }
        log.info("其他入库单作废，ids=【{}】", JSONUtil.toJsonStr(ids));

        //更新
        lambdaUpdate().in(OtherInstockEntity::getId, ids)
                .set(OtherInstockEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
                .set(OtherInstockEntity::getInvalidRemark, reason)
                .update();

        //作废发送金蝶
        sendPushTask(list,SyncOperateEnum.OPERATE_INVALID.getCode());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("作废了一个其他入库单【%s】，作废原因：".concat(reason), ModuleTypeEnum.OTHER_INSTOCK.getCode(), pairList, "作废操作");
        return Boolean.TRUE;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO approve(String id, String type, String comment, Boolean isPushWdt){
        //根据ids查询
        OtherInstockEntity entity = this.getById(id);
        //审核中允许审核
        if (!ApproveStatusEnum.APPROVE_ING.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        log.info("其他入库单【{}】，ids=【{}】", ApproveTypeEnum.getName(type), JSONUtil.toJsonStr(id));
        // 调用流程审核
        ApproveOneDTO dto = new ApproveOneDTO(id, type, comment);
        entity.setIsPushWdt(isPushWdt);
        approveProcess(entity, dto);
        //操作日志
        operateLogService.addModuleOperateLog(String.format("审核【%s】了一个其他入库单【%s】,【%s】", ApproveTypeEnum.getName(type), entity.getCode(), CharSequenceUtil.isNotBlank(comment) ? String.format("意见：%s", comment) : ""), ModuleTypeEnum.OTHER_INSTOCK.getCode(), entity.getId(), "审核操作");

        return BatchResultDTO.success(entity.getId(), entity.getCode(), "其他入库单审核");
    }

    /**
     * 审核流程处理
     *
     * @param entity
     * @param dto
     */
    private void approveProcess(OtherInstockEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.OTHER_INSTOCK.getCode());
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
            dto.setVariablesMap(BeanUtil.beanToMap(entity));
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
    private Map<String,Object> getVariablesMap(OtherInstockEntity entity) {
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        List<OtherInstockDetailEntity> detailList = otherInstockDetailService.listByMainId(entity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99059);
        }
        variablesMap.put(ThirdConstants.DETAIL_LIST, BeanUtil.copyToList(detailList,Map.class));
        //SKU
        String skuNo = detailList.stream().map(OtherInstockDetailEntity::getSkuNo).collect(Collectors.joining(","));
        variablesMap.put("skuNo", skuNo);
        //总计数量
        Integer actualQtyTotal = detailList.stream().map(OtherInstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
        variablesMap.put("actualQtyTotal", actualQtyTotal);
        return variablesMap;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, OtherInstockEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        //查询传入流程数据
        OtherInstockEntity mapToBean = BeanUtil.toBean(dto.getVariablesMap(), OtherInstockEntity.class);

        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        //更新单据(后面有流程了调用监听可删)
        updateApproveStatusForApprove(Collections.singletonList(entity.getId()), approveStatus.getStatus(), null);

        //审核通过
        if (dto.getType().equals(ApproveType.PASS)) {
            //更新库存
            updateInventoryTransCore(entity);
            //审核发送金蝶
            sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_APPROVE.getCode());
            if(mapToBean.getIsPushWdt()){
                //推送旺店通
                if("ordinary".equalsIgnoreCase(entity.getInventoryDirection())){
                    syncApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_APPROVE);
                }else {
                    syncDisApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_APPROVE);
                }
            }
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO disApprove(String id, Boolean isPushWdt) {
        //根据ids查询
        OtherInstockEntity entity = this.getById(id);
        //已审核允许反审核
        if (!ApproveStatusEnum.APPROVE.getStatus().equals(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }

        log.info("其他入库单反审核，ids=【{}】", JSONUtil.toJsonStr(id));

        //取回流程 TODO

        //更新单据为待提交
        updateApproveStatusForDisApprove(Collections.singletonList(id), ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //回扣库存
        InventoryBatchUnApproveDTO inventoryBatchUnApproveDTO = new InventoryBatchUnApproveDTO(InventorySourceTypeEnum.OTHER_INSTOCK, Collections.singletonList(id));
        inventoryTransCoreService.batchUnApprove(inventoryBatchUnApproveDTO);

        //反审核发送金蝶
        sendPushTask(Collections.singletonList(entity),SyncOperateEnum.OPERATE_DISAPPROVE.getCode());

        if(isPushWdt){
            //发送旺店通
            if("ordinary".equalsIgnoreCase(entity.getInventoryDirection())){
                syncDisApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_DISAPPROVE);
            }else {
                syncApproveInfoToWdt(entity, SyncOperateEnum.OPERATE_DISAPPROVE);
            }
        }
        //操作日志
        operateLogService.addModuleOperateLog(CharSequenceUtil.format("反审核了一个其他入库单【{}】", entity.getCode()), ModuleTypeEnum.OTHER_INSTOCK.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "其他入库单反审核");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelProcess(List<String> ids) {
        //根据ids查询
        List<OtherInstockEntity> list = getList(ids);
        //审核中允许审核
        long count = list.stream().filter(obj -> !ApproveStatusEnum.APPROVE_ING.getStatus().equals(obj.getApproveStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        log.info("其他入库单撤销流程，id=【{}】", ids);

        //撤销现有流程
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ids.forEach(obj -> {
            ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
            revokeDTO.setBusinessId(obj);
            revokeDTO.setBusinessKey(SourceTypeEnum.OTHER_INSTOCK.getCode());
            revokeDTO.setUserId(userInfo.getUid());
            workflowFeign.revokeProcess(revokeDTO);
        });

        //更新单据为待提交
        updateApproveStatusForDisApprove(ids, ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        //操作日志
        List<Pair<String, String>> pairList = list.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog("其他入库单【%s】取消流程", ModuleTypeEnum.OTHER_INSTOCK.getCode(), pairList, "取消流程操作");
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcel(OtherInstockDTO.SearchParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("其他入库单导出", EXPORT_WMS_OTHER_IN_STOCK.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(OtherInstockEntity::getId, id)
                .set(CharSequenceUtil.isNotBlank(syncKingdeeId), OtherInstockEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    /**
     * @param entity
     * @description: 库存变更
     * @author Will
     * @date: 2023/5/19 14:36
     */
    private void updateInventoryTransCore(OtherInstockEntity entity) {
        //其他入库明细
        List<OtherInstockDetailEntity> detailList = otherInstockDetailService.listByMainId(entity.getId());
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_99060);
        }
        List<InOutStockDTO> inOutStockList = new ArrayList<>();
        for (OtherInstockDetailEntity detailEntity : detailList) {
            //操作请求实体
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.OTHER_INSTOCK);
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
        //其他入库增加库存
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setParamList(inOutStockList);
        //根据库存方向判断
        String code = InventoryBusinessTypeEnum.OTHER_IN.getCode();
        if (InventoryDirectionEnum.RETURN_GOODS.getCode().equals(entity.getInventoryDirection())) {
            code = InventoryBusinessTypeEnum.OTHER_IN_RETURN_GOODS.getCode();
        }
        inventoryInOutStockDTO.setBusinessType(code);
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);

    }

    /**
     * @param records
     * @description: 处理数据
     * @author Will
     * @date: 2023/4/19 18:58
     */
    private void doOpHandleData(List<OtherInstockDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        List<String> ids = records.stream().map(OtherInstockDTO.ListDTO::getSkuId).collect(Collectors.toList());
        //产品信息
        List<ProductDetailEntity> productDetailList = plmTaskFeign.getByIdList(ids);
        if (CollectionUtils.isEmpty(productDetailList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        //最新审核人
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = listCurApprove(records);

        for (OtherInstockDTO.ListDTO obj : records) {
            //产品名称
            String productName = productDetailList.stream().filter(e -> e.getId().equals(obj.getSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
            obj.setProductName(productName);

            //库存方向名称
            obj.setInventoryDirectionName(InventoryDirectionEnum.getName(obj.getInventoryDirection()));

            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(InvalidStatusEnum.getName(obj.getInvalidStatus()));
            obj.setTypeName(InstockTypeEnum.getByCode(obj.getType()));

            //最新审核人
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(obj.getId()) && org.apache.commons.lang3.StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                obj.setApproveUserName(curApprove);
            }
        }
    }


    /**
     * 处理数据id
     */
    private void doOpHandleDataId(String warehouseId, String receiverId, String warehouseKeeperId, String deptId, OtherInstockEntity entity) {

        //用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(warehouseKeeperId, receiverId));

        //仓管员
        if (CharSequenceUtil.isNotBlank(warehouseKeeperId)) {
            FindUserDTO userDTO = userList.stream().filter(obj -> obj.getUserId().equals(warehouseKeeperId)).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(userDTO)) {
                entity.setWarehouseKeeperName(userDTO.getUserName());
            }
        }
        //领料员
        if (CharSequenceUtil.isNotBlank(receiverId)) {
            FindUserDTO userDTO = userList.stream().filter(obj -> obj.getUserId().equals(receiverId)).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(userDTO)) {
                entity.setReceiverName(userDTO.getUserName());
            }
        }

        //仓库信息
        WarehouseEntity warehouse = warehouseService.getById(warehouseId);
        if (ObjectUtils.isEmpty(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        entity.setWarehouseName(warehouse.getName());

        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(warehouse.getOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        //仓库组织名称
        String orgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(warehouse.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        entity.setOrgId(warehouse.getOrgId());
        entity.setOrgName(orgName);

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
    private List<OtherInstockEntity> getList(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }
        List<OtherInstockEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99059);
        }
        return list;
    }

    /**
     * 更新审核状态
     */
    private void updateApproveStatus(String id, String approveStatus) {
        //更新审核状态
        lambdaUpdate().eq(OtherInstockEntity::getId, id)
                .set(OtherInstockEntity::getApproveStatus, approveStatus)
                .update();
    }

    /**
     * 审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForApprove(List<String> ids, String approveStatus, LocalDateTime approveTime) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if(Objects.isNull(approveTime)){
            approveTime = LocalDateTime.now();
        }
        this.lambdaUpdate().in(OtherInstockEntity::getId, ids)
                .set(OtherInstockEntity::getApproveUserId, userInfo.getUid())
                .set(OtherInstockEntity::getApproveUserName, userInfo.getUserName())
                .set(OtherInstockEntity::getApproveStatus, approveStatus)
                .set(OtherInstockEntity::getApproveTime, approveTime)
                .update();
    }

    /**
     * 反审核后更新审核状态、审核人、审核时间
     */
    private void updateApproveStatusForDisApprove(List<String> ids, String approveStatus) {

        this.lambdaUpdate().in(OtherInstockEntity::getId, ids)
                .set(OtherInstockEntity::getApproveStatus, approveStatus)
                .set(OtherInstockEntity::getApproveUserId, "")
                .set(OtherInstockEntity::getApproveUserName, "")
                .set(OtherInstockEntity::getApproveTime, null)
                .update();
    }

    @Override
    public PagingVO<OtherInstockDTO.PdaListDTO> PdaPaging(PagingDTO<OtherInstockDTO.PdaSearchParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        OtherInstockDTO.PdaSearchParamDTO params = pagingParamDTO.getParams();
        List<String> approveStatusList = params.getApproveStatusList();
        if (approveStatusList.contains(ApproveStatusEnum.APPROVE.getCode())) {
            List<LocalDate> dateList = new ArrayList<>();
            LocalDate now = LocalDate.now();
            dateList.add(now.minusDays(30));
            dateList.add(now);
            params.setBillDateList(dateList);
        }
        IPage<OtherInstockDTO.PdaListDTO> pageData = this.baseMapper.pdaPaging(query, pagingParamDTO.getParams());
        if (CollectionUtils.isEmpty(pageData.getRecords())) {
            return new PagingVO(new Page());
        }
        List<OtherInstockDTO.PdaListDTO> records = pageData.getRecords();
        //主键id
        List<String> ids = records.stream().map(req -> req.getId()).collect(Collectors.toList());
        //查询详情
        List<OtherInstockDetailEntity> otherInstockDetailEntities = otherInstockDetailService.listByMainIds(ids);
        for (OtherInstockDTO.PdaListDTO record : records) {
            record.setApproveStatusName(ApproveStatusEnum.getName(record.getApproveStatus()));
            List<OtherInstockDetailEntity> detailEntities = otherInstockDetailEntities.stream().filter(obj -> obj.getMainId().equals(record.getId())).collect(Collectors.toList());
            List<OtherInstockDTO.PdaItemDTO> itemDTOList = BeanMapper.copyList(detailEntities, OtherInstockDTO.PdaItemDTO.class);
            record.setDetailCount(itemDTOList.size());
            record.setItemList(itemDTOList);
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<OtherInstockDTO.PdaListStatusCountDTO> pdaListCount(PermissionsDTO dto) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        PdaTabFlagEnum[] values = PdaTabFlagEnum.values();
        List<OtherInstockDTO.PdaListStatusCountDTO> list = new ArrayList<>();
        for (PdaTabFlagEnum item : values) {
            OtherInstockDTO.SearchParamDTO pagingParamDTO = new OtherInstockDTO.SearchParamDTO();
            pagingParamDTO.setPermissionSql(dto.getPermissionSql());
            pagingParamDTO.setInvalidStatus(Boolean.FALSE);
            OtherInstockDTO.PdaListStatusCountDTO resultDTO = new OtherInstockDTO.PdaListStatusCountDTO();
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public String addAndApprove(OtherInstockDTO.AddDTO dto) {
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
        this.approve(id, baseApproveParamDTO.getType(), baseApproveParamDTO.getComment(), true);
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateByOverseasInbound(OverseasWarehouseInboundEntity entity, List<OverseasWarehouseInboundDetailEntity> detailEntityList, String remark, boolean isTransitWarehouse) {
        //目的仓
        WarehouseEntity destWarehouse = warehouseService.getById(entity.getToWarehouseId());
        OtherInstockDTO.AddDTO addDTO = this.buildOverFlowMainDto(destWarehouse, isTransitWarehouse, entity);
        List<OtherInstockDetailDTO.AddDTO> detailAddDTOList = new ArrayList<>();
        for (OverseasWarehouseInboundDetailEntity detailEntity : detailEntityList) {
            OtherInstockDetailDTO.AddDTO detailAddDTO = new OtherInstockDetailDTO.AddDTO();
            detailAddDTO.setSkuId(detailEntity.getSkuId());
            detailAddDTO.setSkuNo(detailEntity.getSkuNo());
            detailAddDTO.setActualQty(Math.abs(detailEntity.getDiffQty()));
            detailAddDTO.setRemark(remark);
            detailAddDTO.setSourceDetailId(detailEntity.getId());
            detailAddDTOList.add(detailAddDTO);
        }
        addDTO.setDetailList(detailAddDTOList);
        return this.addAndApprove(addDTO);
    }


    /**
     * 封装报损出库单主记录
     */
    public OtherInstockDTO.AddDTO buildOverFlowMainDto(WarehouseEntity warehouse, boolean isTransitWarehouse, OverseasWarehouseInboundEntity entity) {
        //如果目的仓没有配置在途归属仓，需要提示：目的仓没有配置在途归属仓库，请在【仓库列表】配置后再审核
        if (CharSequenceUtil.isBlank(warehouse.getOnwayWarehouseId()) && isTransitWarehouse) {
            throw new ServiceException(ApiError.ONWAY_WAREHOUSE_NOT_EXIST);
        }
        OtherInstockDTO.AddDTO addDTO = new OtherInstockDTO.AddDTO();
        //出库日期
        addDTO.setBillDate(LocalDate.now());
        //库存方向：普通
        addDTO.setInventoryDirection(InventoryDirectionEnum.ORDINARY.getCode());
        //发货仓库id
        if (isTransitWarehouse) {
            addDTO.setWarehouseId(warehouse.getOnwayWarehouseId());
        } else {
            addDTO.setWarehouseId(warehouse.getId());
        }
        //部门
        SysDepartmentUserNumberDTO sysDepartmentUserNumberDTO = sysUserFeign.getDeptByUserId(entity.getCreateUserId());
        addDTO.setDeptId(sysDepartmentUserNumberDTO.getDepartmentId());
        //出库类型：报损
        addDTO.setType(InstockTypeEnum.REPORT_OVERFLOW.getCode());
        addDTO.setTypeName(InstockTypeEnum.REPORT_OVERFLOW.getName());
        //来源：海外仓入库单
        addDTO.setSourceType(SourceTypeEnum.OVERSEAS_INBOUND.getCode());
        addDTO.setSourceId(entity.getId());
        addDTO.setSourceCode(entity.getCode());
        return addDTO;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "excel/otherInstockTemplate.xlsx";
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
        OtherInStockExcelListener excelListenerUtil = new OtherInStockExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), OtherInStockImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<OtherInStockImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<OtherInStockImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<OtherInStockImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportEndReceiveFile(successList, errorList);

        if (errorList.isEmpty()) {
            return Boolean.TRUE;
        }
        String excelPath = "excel/otherInstockError.xlsx";
        String name = "otherInstockError";
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

    private void handleImportEndReceiveFile(List<OtherInStockImportExcelDTO> successList, List<OtherInStockImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        List<String> skuNoList = successList.stream().map(OtherInStockImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        // 库存类型Map
        Map<String, InstockTypeEnum> inStockTypeMap = Arrays.stream(InstockTypeEnum.values())
                .collect(Collectors.toMap(InstockTypeEnum::getName, Function.identity()));

        // 库存方向Map
        Map<String, InventoryDirectionEnum> inventoryDirectionMap = Arrays.stream(InventoryDirectionEnum.values())
                .collect(Collectors.toMap(InventoryDirectionEnum::getName, Function.identity()));

        // 发货仓库
        List<String> warehouseNameList = successList.stream().map(OtherInStockImportExcelDTO::getWarehouseName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listByNames(warehouseNameList);
        Map<String, WarehouseDTO.ListDTO> warehouseMap = warehouseList
                .stream()
                .collect(Collectors.toMap(WarehouseDTO.ListDTO::getName, Function.identity()));

        // 仓位
        List<String> warehouseIds = warehouseList.stream().map(WarehouseDTO.ListDTO::getId).collect(Collectors.toList());
        Map<String, List<WarehouseLocationEntity>> warehousrLocationMap = warehouseLocationService.listByWarehouseIds(warehouseIds)
                .stream()
                .collect(Collectors.groupingBy(WarehouseLocationEntity::getWarehouseId));

        // 领料部门
        Map<String, List<SysDepartmentDTO>> deptMap = sysUserFeign.getDeptList()
                .stream()
                .collect(Collectors.groupingBy(SysDepartmentDTO::getName));

        //sku Map
        Map<String, SkuVO> existSkuMap = plmTaskFeign.listBySkuNoList(skuNoList)
                .stream()
                .collect(Collectors.toMap(SkuVO::getSkuNo, Function.identity()));

        // 验收员
        List<String> userNameList = successList.stream()
                .map(OtherInStockImportExcelDTO::getReceiverName)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, List<FindUserDTO>> userMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userNameList)) {
            userMap = sysUserFeign.listUserByUserNames(userNameList, UserTypeEnum.ERP.code)
                    .stream()
                    .collect(Collectors.groupingBy(FindUserDTO::getUserName));
        }

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
        List<OtherInstockEntity> canHandleList = new ArrayList<>();
        //获取当前用户
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        // 校验和处理
        for (OtherInStockImportExcelDTO importExcelDTO : successList) {
            // 库存类型Map
            InstockTypeEnum inStockTypeEnum = inStockTypeMap.get(importExcelDTO.getType());

            Integer actualQty = Integer.valueOf(importExcelDTO.getActualQtyStr());

            LocalDate billDate = CharSequenceUtil.isBlank(importExcelDTO.getBillDateStr()) ? LocalDate.now() : LocalDate.parse(importExcelDTO.getBillDateStr(), DateTimeFormatter.ofPattern("yyyy/M/d"));

            // 库存方向Map
            InventoryDirectionEnum inventoryDirectionEnum = inventoryDirectionMap.get(importExcelDTO.getInventoryDirection());

            // 发货仓库
            WarehouseDTO.ListDTO warehouseDTO = warehouseMap.get(importExcelDTO.getWarehouseName());
            if (null == warehouseDTO) {
                importExcelDTO.setErrorMsg(ApiError.WAREHOUSE_NOT_EXIST_NO_PERMISSION.msg);
                errorList.add(importExcelDTO);
                continue;
            }
            if (null != warehouseDTO.getApproveStatus() && !ApproveStatusEnum.APPROVE.equals(warehouseDTO.getApproveStatus())) {
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】仓库未审核通过", importExcelDTO.getWarehouseName()));
                errorList.add(importExcelDTO);
                continue;
            }
            if (null != warehouseDTO.getDisabled() && warehouseDTO.getDisabled()) {
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】仓库未启用", importExcelDTO.getWarehouseName()));
                errorList.add(importExcelDTO);
                continue;
            }

            String currentWarehouseId = warehouseDTO.getId();
            WarehouseLocationEntity locationEntity = null;
            if (CharSequenceUtil.isNotBlank(importExcelDTO.getWarehouseLocation())) {
                List<WarehouseLocationEntity> locationList = warehousrLocationMap.get(currentWarehouseId);
                if (CollectionUtils.isEmpty(locationList)) {
                    importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】仓位不存在", importExcelDTO.getWarehouseLocation()));
                    errorList.add(importExcelDTO);
                    continue;
                }
                locationEntity = locationList.stream().filter(e -> e.getName().equalsIgnoreCase(importExcelDTO.getWarehouseLocation())).findFirst().orElse(null);
                if (null == locationEntity) {
                    importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】仓位不存在", importExcelDTO.getWarehouseLocation()));
                    errorList.add(importExcelDTO);
                    continue;
                }
            }

            // 验收员
            FindUserDTO userDTO = null;
            if (CharSequenceUtil.isNotBlank(importExcelDTO.getReceiverName())) {
                List<FindUserDTO> userDTOList = userMap.get(importExcelDTO.getReceiverName());
                if (CollectionUtils.isEmpty(userDTOList)) {
                    importExcelDTO.setErrorMsg(CharSequenceUtil.format("【{}】验收员不存在", importExcelDTO.getReceiverName()));
                    errorList.add(importExcelDTO);
                    continue;
                } else {
                    userDTO = userDTOList.stream().findFirst().orElse(null);
                }
            }

            // 部门
            SysDepartmentDTO departmentDTO = null;
            List<SysDepartmentDTO> currrentDeptList = deptMap.get(importExcelDTO.getDeptName());
            if (!CollectionUtils.isEmpty(currrentDeptList)) {
                departmentDTO = currrentDeptList.stream().findFirst().orElse(null);
            }
            if (null == departmentDTO) {
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("部门【{}】不存在", importExcelDTO.getDeptName()));
                errorList.add(importExcelDTO);
                continue;
            }

            SkuVO skuVO = existSkuMap.get(importExcelDTO.getSkuNo());
            if (null == skuVO) {
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("SKU【{}】不存在或未审核通过", importExcelDTO.getSkuNo()));
                errorList.add(importExcelDTO);
                continue;
            }
            if (null != skuVO.getStatus() && !Objects.equals(ProductDetailStatusEnum.APPROVAL_PASS.getCode(), skuVO.getStatus())) {
                importExcelDTO.setErrorMsg(CharSequenceUtil.format("SKU【{}】不存在或未审核通过", importExcelDTO.getSkuNo()));
                errorList.add(importExcelDTO);
                continue;
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
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_QTRK);
                OtherInstockEntity entity = OtherInStockConverter.INSTANCE.combineAddEntity(
                        importExcelDTO,
                        inStockTypeEnum,
                        billDate,
                        inventoryDirectionEnum,
                        warehouseDTO,
                        locationEntity,
                        departmentDTO,
                        userDTO,
                        code,
                        userInfo.getUid(),
                        userInfo.getUserName()
                );

                // 明细
                OtherInstockDetailEntity detailEntity = OtherInStockConverter.INSTANCE.combineDetailEntity(importExcelDTO, skuVO, locationEntity, actualQty);
                entity.setDetailEntityList(Collections.singletonList(detailEntity));
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
        List<List<OtherInstockEntity>> partitionList = Lists.partition(canHandleList, 1000);

        // 批量保存
        for (List<OtherInstockEntity> currentList : partitionList) {
            try {
                // 处理批量保存
                importBatchSave(currentList);
            } catch (Exception e) {
                List<OtherInStockImportExcelDTO> errorSaveList = canHandleList.stream().map(dto  ->
                        {
                            OtherInStockImportExcelDTO errorSaveImportExcelDTO = dto.getImportExcelDTO();
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
    public void importBatchSave(List<OtherInstockEntity> mainEntityList) {
        if (!this.saveBatch(mainEntityList)){
            throw new ServiceException("主信息批量保存失败");
        }
        List<OtherInstockDetailEntity> detailEntityList = mainEntityList.stream().map(e -> {
            e.getDetailEntityList().forEach(detail -> detail.setMainId(e.getId()));
            return e.getDetailEntityList();
        }).flatMap(List::stream).collect(Collectors.toList());

        List<Pair<String, String>> pairList = mainEntityList.stream().map(obj -> new Pair<>(obj.getId(), obj.getCode())).collect(Collectors.toList());
        // 批量保存操作日志
        operateLogService.batchAddModuleOperateLog("导入一个其他入库单【%s】", ModuleTypeEnum.OTHER_INSTOCK.getCode(),pairList,"新增操作");

        if (!otherInstockDetailService.saveBatch(detailEntityList)){
            throw new ServiceException("明细信息批量保存失败");
        }
    }

    @Override
    public PagingVO<OtherInstockDTO.ListDTO> exportOtherInStock(PagingDTO<OtherInstockDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<OtherInstockDTO.ListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            doOpHandleData(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    @Override
    public List<OtherInstockEntity> listByCodes(List<String> list) {
        return this.list(new QueryWrapper<OtherInstockEntity>().lambda().in(OtherInstockEntity::getCode, list).eq(OtherInstockEntity::getIsDeleted,Boolean.FALSE));
    }

    @Override
    public void updateApproveStatus(OtherInstockDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO) {
         String approveStatus = updateApprovalStatusDTO.getApproveStatus();
        OtherInstockEntity otherInstockEntity = updateApprovalStatusDTO.getOtherInstockEntity();
        this.lambdaUpdate().in(OtherInstockEntity::getId, Collections.singletonList(otherInstockEntity.getId()))
                .set(OtherInstockEntity::getApproveUserId, otherInstockEntity.getApproveUserId())
                .set(OtherInstockEntity::getApproveUserName, otherInstockEntity.getApproveUserName())
                .set(OtherInstockEntity::getApproveStatus, approveStatus)
                .set(OtherInstockEntity::getApproveTime, otherInstockEntity.getApproveTime())
                .update();
    }

    @Override
    @DataIdempotent(keyIdName = "dto.thirdCode")
    public void syncWdtPreInstock(DmpSoPrestockInfoDTO.PrestockDTO dto) {
        if(Objects.isNull(dto.getCheckTime())){
            log.warn("{}旺店通审核时间为空",dto.getThirdCode());
            return;
        }
        if(CollectionUtils.isEmpty(dto.getDetailList())){
            log.warn("{}旺店通明细为空",dto.getThirdCode());
            return;
        }
        //查询其他入库单是否已存在
        OtherInstockEntity dbEntity = this.getOne(Wrappers.<OtherInstockEntity>lambdaQuery()
                        .eq(OtherInstockEntity::getThirdCode, dto.getThirdCode())
                        .eq(OtherInstockEntity::getThirdPlatform,PlatformDictEnum.WDT.getCode())
                ,false);
        if(Objects.nonNull(dbEntity)){
//            if(Objects.nonNull(dbEntity.getApproveTime()) && dto.getCheckTime().isAfter(dbEntity.getApproveTime())){
//                
//            }
            //反审核重新生成
            service.disApproveAndGenerate(dbEntity.getId(),dto);
        }else{
            //查询是否有已审核的销售退货入库单
            SoReturnInstockEntity entity = soReturnInstockService.getOne(Wrappers.<SoReturnInstockEntity>lambdaQuery()
                    .eq(SoReturnInstockEntity::getThirdCode, dto.getThirdCode())
                    .eq(SoReturnInstockEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getCode()),false);
            //单据已经存在
            if (ObjectUtil.isNotEmpty(entity)){
                log.warn("{} 销售退货入库单已存在",dto.getThirdCode());
                return;
            }
            OtherInstockEntity otherInstockEntity = this.buildWdtPreStock(dto);
            service.addAndApprove(otherInstockEntity, false);
        }
    }

    @Override
    public OtherInstockEntity getByThirdCode(String thirdCode, InventoryDirectionEnum inventoryDirectionEnum) {
        return this.getOne(Wrappers.<OtherInstockEntity>lambdaQuery()
                .eq(OtherInstockEntity::getThirdCode, thirdCode)
                .eq(OtherInstockEntity::getApproveStatus,ApproveStatusEnum.APPROVE.getCode())
                .eq(OtherInstockEntity::getInventoryDirection,inventoryDirectionEnum.getCode()),false);
    }

    @Override
    public void generateOpposite(OtherInstockEntity dbOtherInstockEntity, String code) {
        OtherInstockEntity otherInstockEntity = OtherInStockConverter.INSTANCE.copy(dbOtherInstockEntity);
        otherInstockEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        otherInstockEntity.setSyncKingdeeId("");
        otherInstockEntity.setInventoryDirection(InventoryDirectionEnum.RETURN_GOODS.getCode());
        List<OtherInstockDetailEntity> dbDetailList = otherInstockDetailService.listByMainId(dbOtherInstockEntity.getId());
        List<OtherInstockDetailEntity> detailList = OtherInStockConverter.INSTANCE.copyDetailList(dbDetailList);
        otherInstockEntity.setDetailEntityList(detailList);
        otherInstockEntity.setRemark(code);
        service. addAndApprove(otherInstockEntity, false);
    }

    private OtherInstockEntity buildWdtPreStock(DmpSoPrestockInfoDTO.PrestockDTO dto) {
        OtherInstockEntity otherInstockEntity = new OtherInstockEntity();
        ThirdMappingDTO.ViewParamDTO viewParamDTO = new ThirdMappingDTO.ViewParamDTO();
        viewParamDTO.setType(ThirdSysTypeEnum.WAREHOUSE.getCode());
        viewParamDTO.setSysType(PlatformDictEnum.WDT.getCode());
        viewParamDTO.setThirdId(dto.getWarehouseId());
        List<ThirdMappingEntity> thirdList = dmpThirdMappingFeign.getByThirdId(viewParamDTO);
        if(thirdList.isEmpty()){
            log.warn("{}旺店通未配置仓库映射",dto.getThirdCode());
            throw new ServiceException("旺店通仓库未配置仓库映射");
        }
        String warehouseId = thirdList.get(0).getSysId();

        WarehouseEntity warehouseEntity = warehouseService.getById(warehouseId);
        if(Objects.isNull(warehouseEntity)){
            throw new ServiceException("旺店通映射的系统仓库为空");
        }
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(warehouseEntity.getOrgId()));
        if (CollectionUtils.isEmpty(accountingCompanyList)) {
            throw new ServiceException(ApiError.ERROR_9014);
        }
        List<SysDepartmentEntity> sysDepartmentEntity = sysUserFeign.getDeptByIds(Collections.singletonList("1675799739955679233"));
        if (CollectionUtils.isEmpty(sysDepartmentEntity)) {
            throw new ServiceException("获取不到仓储部门信息");
        }
        List<String> skuList = dto.getDetailList().stream().map(DmpSoPrestockDetailDTO.PrestockDetailDTO::getSkuNo).collect(Collectors.toList());
        List<SkuVO> skuNoList = plmTaskFeign.listBySkuNoList(skuList);
        //仓库组织名称
        String orgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(warehouseEntity.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
        otherInstockEntity.setBillDate(dto.getCheckTime().toLocalDate());
        otherInstockEntity.setInventoryDirection(InventoryDirectionEnum.ORDINARY.getCode());
        otherInstockEntity.setWarehouseId(warehouseEntity.getId());
        otherInstockEntity.setWarehouseName(warehouseEntity.getName());
        otherInstockEntity.setReturnLogisticCode(dto.getLogisticsNo());
        otherInstockEntity.setOrgId(warehouseEntity.getOrgId());
        otherInstockEntity.setApproveTime(dto.getCheckTime());
        otherInstockEntity.setOrgName(orgName);
        otherInstockEntity.setDeptId(sysDepartmentEntity.get(0).getId());
        otherInstockEntity.setDeptName(sysDepartmentEntity.get(0).getName());
        otherInstockEntity.setType(InstockTypeEnum.THREE_NO_PRODUCT_PRE_INSTOCK.getCode());
        otherInstockEntity.setThirdCode(dto.getThirdCode());
        otherInstockEntity.setThirdPlatform(PlatformDictEnum.WDT.getCode());
        otherInstockEntity.setRemark(dto.getRemark());
        otherInstockEntity.setSourceType(SourceTypeEnum.WDT_SO_PRESTOCK.getCode());
        otherInstockEntity.setSourceId(dto.getId());
        otherInstockEntity.setSourceCode(dto.getThirdCode());
        List<OtherInstockDetailEntity> detailEntityList = new ArrayList<>();
        for (DmpSoPrestockDetailDTO.PrestockDetailDTO prestockDetailDTO : dto.getDetailList()) {
            OtherInstockDetailEntity detailEntity = new OtherInstockDetailEntity();
            SkuVO skuVO = skuNoList.stream().filter(v->v.getSkuNo().equals(prestockDetailDTO.getSkuNo())).findFirst().orElseThrow(()-> new ServiceException(CharSequenceUtil.format("{}旺店通产品映射未找到",prestockDetailDTO.getSkuNo())));
            detailEntity.setSkuId(skuVO.getSkuId());
            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setActualQty(prestockDetailDTO.getQty());
            detailEntity.setUnit(skuVO.getUnitName());
            detailEntity.setWarehouseLocation(WmsConstant.WDT_NULL_LOCATION.contains(prestockDetailDTO.getWarehouseLocation()) ? "" : prestockDetailDTO.getWarehouseLocation());
            detailEntity.setRemark(prestockDetailDTO.getRemark());
            detailEntity.setSourceDetailId(prestockDetailDTO.getId());
            detailEntityList.add(detailEntity);
        }
        otherInstockEntity.setDetailEntityList(detailEntityList);
        return otherInstockEntity;
    }

    /**
     * @description: 推送金蝶
     * @author Will
     * @date: 2024/5/20 12:41
     * @param list
     */
    private void sendPushTask (List<OtherInstockEntity> list, String operate) {
        //审核通过发送金蝶
        List<DmpPushTaskEntity> resultList = new ArrayList<>();
        list.forEach(obj -> {
            DmpPushTaskEntity pushTaskEntity = syncKingdeeOtherInstockService.syncDataToKingdee(obj, operate);
            resultList.add(pushTaskEntity);
        });
        //推送金蝶
//        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//            @Override
//            public void afterCommit() {
//                dmpMqFeign.sendTask(resultList);
//            }
//        });
    }

    /**
     * @param entity      其他入库单
     * @param operateEnum 操作代码: 审核/反审核
     * @return void
     * @date: 2024-05-24
     * @author: tanmujin
     */
    private void syncApproveInfoToWdt(OtherInstockEntity entity, SyncOperateEnum operateEnum) {
        List<OtherInstockDetailEntity> detailList = otherInstockDetailService.listByMainId(entity.getId());

        List<CreateOtherStockinRequest.GoodsList> goodsList = new ArrayList<>();
        for (OtherInstockDetailEntity detailEntity : detailList) {
            CreateOtherStockinRequest.GoodsList goods = new CreateOtherStockinRequest.GoodsList();
            goods.setSpecNo(detailEntity.getSkuNo());
            goods.setNum(BigDecimal.valueOf(detailEntity.getActualQty()));
            goods.setPositionNo(detailEntity.getWarehouseLocation());
            goods.setWarehouseId(entity.getWarehouseId());
            goodsList.add(goods);
        }

        abstractWdtService.transfer(operateEnum, entity.getId(), entity.getCode(), goodsList, SourceTypeEnum.OTHER_INSTOCK);
    }

    /**
     * 将其他入库单的反审核操作转换为其他出库单同步给旺店通
     *
     * @param entity      其他入库单
     * @param operateEnum 操作代码: 审核/反审核
     * @return void
     * @date: 2024-05-20
     * @author: tanmujin
     */
    private void syncDisApproveInfoToWdt(OtherInstockEntity entity, SyncOperateEnum operateEnum) {
        List<OtherInstockDetailEntity> detailList = otherInstockDetailService.listByMainId(entity.getId());

        List<CreateOtherStockoutRequest.GoodsList> goodsList = new ArrayList<>(detailList.size());
        for (OtherInstockDetailEntity detailEntity : detailList) {
            CreateOtherStockoutRequest.GoodsList goods = new CreateOtherStockoutRequest.GoodsList();
            goods.setSpecNo(detailEntity.getSkuNo());
            goods.setNum(BigDecimal.valueOf(detailEntity.getActualQty()));
            goods.setPositionNo(detailEntity.getWarehouseLocation());
            goods.setWarehouseId(entity.getWarehouseId());
            goodsList.add(goods);
        }

        abstractWdtService.transfer(operateEnum, entity.getId(), entity.getCode(), goodsList, SourceTypeEnum.OTHER_OUTSTOCK);
    }

    /**
     * 查询当前审核人
     * @author will
     * @date 2025/7/17 14:53
     * @param records
     * @return ApiResult<List<CurApproveInfoDTO>>
     */
    private  ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listCurApprove (List<OtherInstockDTO.ListDTO> records) {
        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        records.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.OTHER_INSTOCK.getCode(), obj.getId()));
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

    @Override
    public List<OtherInstockDTO.ListDTO> viewAssociatedDocuments(BaseIdDTO dto) {
        List<OtherInstockDTO.ListDTO> list = baseMapper.viewAssociatedDocuments(dto);
        // 格式化入库单数据
        formatOtherInstock(list);
        return list;
    }

    /**
     * @description: 格式化列表数据
     * @author Will
     * @date: 2024/12/19 10:16
     * @param records
     */
    private void formatOtherInstock(List<OtherInstockDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        
        // 获取所有SKU ID
        List<String> skuIds = records.stream()
                .map(OtherInstockDTO.ListDTO::getSkuNo)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询SKU信息
        Map<String, String> productNameMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(skuIds)) {
            try {
                List<ProductDetailEntity> skuList = plmTaskFeign.listBySkuNos(skuIds);
                if (CollectionUtils.isNotEmpty(skuList)) {
                    productNameMap = skuList.stream()
                            .collect(Collectors.toMap(ProductDetailEntity::getSkuNo, ProductDetailEntity::getName));
                }
            } catch (Exception e) {
                log.warn("获取SKU信息失败，错误：{}", e.getMessage());
            }
        }
        
        // 查询流程id判断是否存在流程
        Map<String, String> finalProductNameMap = productNameMap;
        records.forEach(obj -> {
            obj.setApproveStatusName(ApproveStatusEnum.getName(obj.getApproveStatus()));
            obj.setInvalidStatusName(obj.getInvalidStatus() ? "已作废" : "未作废");
            
            // 设置产品名称
            if (Objects.nonNull(obj.getSkuId())) {
                String productName =
                        finalProductNameMap.get(obj.getSkuNo());
                obj.setProductName(productName != null ? productName : "");
            }
        });
    }

    @Override
    public Map<String, OtherInstockEntity> mapByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<OtherInstockEntity> list = this.listByIds(ids);
        return list.stream().collect(Collectors.toMap(OtherInstockEntity::getId, Function.identity()));
    }




    @Override
    public List<ExhibitionOrderDTO.DownstreamListDTO> listOtherInstockByExhibitionId(String exhibitionId) {
        List<ExhibitionOrderDTO.DownstreamListDTO> resultList = baseMapper.listOtherInstockInByExhibitionId(exhibitionId);
        if(CollUtil.isEmpty(resultList)){
            return Collections.emptyList();
        }

        List<String> skuIds = resultList.stream().map(ExhibitionOrderDTO.DownstreamListDTO::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.create(ProductDetailEntity.class).in(ProductDetailEntity::getId, skuIds).list();
        Map<String, String> skuMap = skuList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getName));

        for (ExhibitionOrderDTO.DownstreamListDTO listDTO : resultList) {
            listDTO.setApproveStatusName(ApproveStatusEnum.getName(listDTO.getApproveStatus()));
            listDTO.setInvalidStatusName(InvalidStatusEnum.getName(listDTO.getInvalidStatus()));
            listDTO.setProductName(skuMap.getOrDefault(listDTO.getSkuId(),""));
        }
        return resultList;
    }


    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTaskRecordDTO.MqResponseDTO generateOtherAddAndSubmit(WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO mqResponseDTO = new WorkflowTaskRecordDTO.MqResponseDTO();
        Map<String, Object> data = dto.getData();
        //校验data是否为空
        if (ObjectUtil.isEmpty(data)) {
            mqResponseDTO.setErrorMsg("data为空");
            return mqResponseDTO;
        }
        if(!data.containsKey("exhibitionOrderId") || Objects.isNull(data.get("exhibitionOrderId"))){
            mqResponseDTO.setErrorMsg("exhibitionOrderId为空");
            return mqResponseDTO;
        }
        String exhibitionOrderId;
        try {
            exhibitionOrderId = String.valueOf(data.get("exhibitionOrderId"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("exhibitionOrderId 类型转换失败");
            log.warn("exhibitionOrderId 类型转换失败: {}", data.get("exhibitionOrderId"));
            return mqResponseDTO;
        }

        ExhibitionOrderDTO.DownstreamDTO downstreamDTO = null;
        try{
            downstreamDTO = exhibitionOrderFeign.generateDownstreamByExhibitionOrder(exhibitionOrderId);
        }catch (Exception e){
            log.error("构建其他入库单和销售出库单入参失败，exhibitionOrderId: {}", exhibitionOrderId, e);
            mqResponseDTO.setErrorMsg(e.getMessage());
            return mqResponseDTO;
        }

        try{
            // 生成其他入库单
            OtherInstockDTO.AddDTO otherInstockAddDTO = downstreamDTO.getOtherInstockAddDTO();
            OtherInstockService bean = SpringUtil.getBean(OtherInstockService.class);
            String otherInstockId = bean.addAndSubmit(otherInstockAddDTO);
            if(StringUtils.isBlank(otherInstockId)){
                throw new ServiceException("展会订单自动生成其他入库单失败");
            }

            String comment = "展会订单自动审核通过";
            // 审核通过其他入库单
            BatchResultDTO result = approve(otherInstockId, ApproveTypeEnum.PASS.getStatus(), comment, Boolean.FALSE);
            if (!result.getSuccess()) {
                throw new ServiceException(result.getMsg());
            }

            //生成销售出库单
            Boolean save = soOutstockService.addB2bPushDownNo(downstreamDTO.getGenerateSoOutstockViewDTOList());
            if(!save){
                throw new ServiceException("展会订单自动生成销售出库单失败");
            }

            List<SoOutstockEntity> soOutstockEntities = soOutstockService.lambdaQuery().eq(SoOutstockEntity::getSoId, downstreamDTO.getSoId()).list();
            List<String> soOutstockIds = soOutstockEntities.stream().map(SoOutstockEntity::getId).collect(Collectors.toList());

            //提审
            soOutstockService.submit(soOutstockIds,Boolean.FALSE);

            Map<String, Object> map = new HashMap<>();
            map.put("otherInstockId", otherInstockId);
            map.put("soOutstockId", soOutstockIds.get(0));
            mqResponseDTO.setData(map);

        }catch (Exception e){
            log.error("生成其他入库单和销售出库单失败，exhibitionOrderId: {}", exhibitionOrderId, e);
            mqResponseDTO.setErrorMsg(e.getMessage());
            return mqResponseDTO;
        }
        return mqResponseDTO;
    }


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public WorkflowTaskRecordDTO.MqResponseDTO autoOtherDisApprove(WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO mqResponseDTO = new WorkflowTaskRecordDTO.MqResponseDTO();
        Map<String, Object> data = dto.getData();
        //校验data是否为空
        if (ObjectUtil.isEmpty(data)) {
            mqResponseDTO.setErrorMsg("data为空");
            return mqResponseDTO;
        }

        if(!data.containsKey("soId") || Objects.isNull(data.get("soId")) || !data.containsKey("exhibitionOrderId") || Objects.isNull(data.get("exhibitionOrderId")) ){
            mqResponseDTO.setErrorMsg("soId或exhibitionOrderId为空");
            return mqResponseDTO;
        }
        String soId;
        String exhibitionOrderId;
        try {
            soId = String.valueOf(data.get("soId"));
            exhibitionOrderId = String.valueOf(data.get("exhibitionOrderId"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("soId或exhibitionOrderId类型转换失败");
            log.warn("soId 类型转换失败: {} 或 exhibitionOrderId 类型转换失败: {}", data.get("soId"), data.get("exhibitionOrderId"));
            return mqResponseDTO;
        }

        //销售出库单
        List<SoOutstockEntity> soOutstockEntities = soOutstockService.lambdaQuery().eq(SoOutstockEntity::getSoId, soId).list();
        if(CollUtil.isNotEmpty(soOutstockEntities)){
            StringBuilder sb = new StringBuilder();
            List<BatchResultDTO> resultDTOS = new ArrayList<>(soOutstockEntities.size());
            for (SoOutstockEntity entity : soOutstockEntities) {
                try {
                    BatchResultDTO result = soOutstockService.disApprove(entity, Boolean.TRUE);
                    if(!result.getSuccess()){
                        sb.append(StrUtil.format("销售出库单反审核失败,soOutstockId:{} ;",entity.getId()));
                    }
                    resultDTOS.add(result);
                }catch (Exception e){
                    sb.append(StrUtil.format("销售出库单反审核失败,soOutstockId:{},e:{};",entity.getId(),e.getMessage()));
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
                }
            }

            if (resultDTOS.stream().allMatch(BatchResultDTO::getSuccess)) {
                List<String> ids = soOutstockEntities.stream().map(SoOutstockEntity::getId).collect(Collectors.toList());
                String idsStr = soOutstockEntities.stream()
                        .map(SoOutstockEntity::getId)
                        .collect(Collectors.joining(","));
                try {
                    Boolean delete = soOutstockService.delete(ids);
                    if(!delete){
                        mqResponseDTO.setErrorMsg(StrUtil.format("销售出库单删除失败,soOutstockId:{}",idsStr));
                        log.warn(sb.toString());
                        return mqResponseDTO;
                    }
                }catch (Exception e){
                    mqResponseDTO.setErrorMsg(StrUtil.format("销售出库单删除失败,soOutstockId:{},e:{}",idsStr,e.getMessage()));
                    log.warn(sb.toString());
                    return mqResponseDTO;
                }
            }else {
                mqResponseDTO.setErrorMsg(sb.toString());
                log.warn(sb.toString());
                return mqResponseDTO;
            }
        }

        //其他入库单
        List<OtherInstockEntity> otherInstockEntities = lambdaQuery().eq(OtherInstockEntity::getSourceId, exhibitionOrderId).list();
        if(CollUtil.isNotEmpty(otherInstockEntities)){
            OtherInstockService bean = SpringUtil.getBean(OtherInstockService.class);
            StringBuilder sb = new StringBuilder();
            List<BatchResultDTO> resultDTOS = new ArrayList<>(otherInstockEntities.size());
            for (OtherInstockEntity entity : otherInstockEntities) {
                try {
                    BatchResultDTO result = bean.disApprove(entity.getId(), Boolean.TRUE);
                    if(!result.getSuccess()){
                        sb.append(StrUtil.format("其他入库单反审核失败,otherInstockId:{} ;",entity.getId()));
                    }
                    resultDTOS.add(result);
                }catch (Exception e){
                    log.error("其他入库单反审核失败",e);
                    sb.append(StrUtil.format("其他入库单反审核失败,otherInstockId:{},e:{} ;",entity.getId(),e.getMessage()));
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
                }
            }

            if (resultDTOS.stream().allMatch(BatchResultDTO::getSuccess)) {
                List<String> ids = otherInstockEntities.stream().map(OtherInstockEntity::getId).collect(Collectors.toList());
                String idsStr = otherInstockEntities.stream()
                        .map(OtherInstockEntity::getId)
                        .collect(Collectors.joining(","));
                try {
                    Boolean delete = bean.delete(ids);
                    if(!delete){
                        mqResponseDTO.setErrorMsg(StrUtil.format("其他入库单删除失败,soOutstockId:{}",idsStr));
                        log.warn(sb.toString());
                        return mqResponseDTO;
                    }
                }catch (Exception e){
                    mqResponseDTO.setErrorMsg(StrUtil.format("其他入库单删除失败,soOutstockId:{},e:{}",idsStr,e.getMessage()));
                    log.error("其他入库单删除失败",e);
                    return mqResponseDTO;
                }
            }else {
                mqResponseDTO.setErrorMsg(sb.toString());
                log.warn(sb.toString());
                return mqResponseDTO;
            }
        }

        mqResponseDTO.setData(data);
        return mqResponseDTO;
    }

    /**
     * 生成其他入库单和销售出库单并审批流程
     *
     * @param dto MQ请求数据传输对象，包含流程审批所需的数据
     * @return MQ响应数据传输对象，包含处理结果和错误信息
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public WorkflowTaskRecordDTO.MqResponseDTO generateOtherApprove(WorkflowTaskRecordDTO.MqRequestDTO dto) {
        WorkflowTaskRecordDTO.MqResponseDTO mqResponseDTO = new WorkflowTaskRecordDTO.MqResponseDTO();
        Map<String, Object> data = dto.getData();
        //校验data是否为空
        if (ObjectUtil.isEmpty(data)) {
            mqResponseDTO.setErrorMsg("data为空");
            return mqResponseDTO;
        }

        if(!data.containsKey("otherInstockId") || Objects.isNull(data.get("otherInstockId")) || !data.containsKey("soOutstockId") || Objects.isNull(data.get("soOutstockId")) ){
            mqResponseDTO.setErrorMsg("otherInstockId或soOutstockId为空");
            return mqResponseDTO;
        }
        String otherInstockId;
        String soOutstockId;
        try {
            otherInstockId = String.valueOf(data.get("otherInstockId"));
            soOutstockId = String.valueOf(data.get("soOutstockId"));
        } catch (Exception e) {
            mqResponseDTO.setErrorMsg("otherInstockId或soOutstockId类型转换失败");
            log.warn("otherInstockId 类型转换失败: {} 或 soOutstockId 类型转换失败: {}", data.get("otherInstockId"),data.get("soOutstockId"));
            return mqResponseDTO;
        }

        try{

            String comment = "展会订单自动审核通过";
            //审核通过
            ApproveOneDTO approveOneDTO = new ApproveOneDTO();
            approveOneDTO.setId(soOutstockId);
            approveOneDTO.setType(ApproveTypeEnum.PASS.getStatus());
            approveOneDTO.setComment(comment);
            BatchResultDTO approve = soOutstockService.approve(approveOneDTO);
            if (!approve.getSuccess()) {
                throw new ServiceException(approve.getMsg());
            }
        }catch (Exception e){
            log.error("生成其他入库单和销售出库单失败，otherInstockId: {}，soOutstockId: {}", otherInstockId,soOutstockId, e);
            mqResponseDTO.setErrorMsg(e.getMessage());
            return mqResponseDTO;
        }
        return mqResponseDTO;
    }
}

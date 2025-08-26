package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.plm.dto.ProductSkuDTO;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.wms.dto.OtherOutstockCustomerDTO;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.model.wms.dto.OtherOutstockDetailDTO;
import com.erp.model.wms.dto.SampleRecipientDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.common.business.dto.FindUserDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.SampleRecipientExecStatusEnum;
import com.erp.model.wms.enums.SampleUsageEnum;
import com.erp.model.wms.enums.SampleUsageScopeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.wms.mapper.SampleRecipientMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.erp.model.wms.dto.excel.SampleRecipientExcelDTO;
import com.erp.server.wms.listener.SampleRecipientExcelListener;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import java.io.ByteArrayInputStream;
import java.io.File;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * 样品领用单 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@Service
public class SampleRecipientServiceImpl extends SuperServiceImpl<SampleRecipientMapper, SampleRecipientEntity> implements SampleRecipientService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private SampleRecipientDetailService sampleRecipientDetailService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private OtherOutstockService otherOutstockService;
    @Autowired
    private OtherOutstockDetailService otherOutstockDetailService;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    
    @Autowired
    private SysUserFeign sysUserFeign;
    
    @Autowired
    private LogisticsFeign logisticsFeign;

    @Autowired
    private FileFeign fileFeign;
    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private RedissonClient redissonClient;

    // 缓存相关常量
    private static final String CACHE_WAREHOUSE_NAME_TO_ID = "sample_recipient:warehouse_name_to_id:";
    private static final String CACHE_USER_NAME_TO_ID = "sample_recipient:user_name_to_id:";
    private static final String CACHE_DEPT_NAME_TO_ID = "sample_recipient:dept_name_to_id:";
    private static final String CACHE_ORG_NAME_TO_ID = "sample_recipient:org_name_to_id:";
    private static final String CACHE_SKU_NO_TO_ID = "sample_recipient:sku_no_to_id:";
    private static final String CACHE_SKU_ID_TO_PRODUCT_NAME = "sample_recipient:sku_id_to_product_name:";

    // 缓存过期时间：1小时
    private static final long CACHE_EXPIRE_TIME = 3600;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleRecipientDTO.AddDTO addDTO) {
        SampleRecipientEntity sampleRecipientEntity = new SampleRecipientEntity();
        BeanMapperUtils.copy(addDTO, sampleRecipientEntity);


        // 数据处理
        handleData(sampleRecipientEntity);

        log.info("开始新增样品领用单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YPLY);
        sampleRecipientEntity.setCode(code);
        sampleRecipientEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
        boolean save = super.save(sampleRecipientEntity);
        if(!save) {
            throw new ServiceException("样品领用单保存失败");
        }

        // 库存校验
        if (CollUtil.isNotEmpty(addDTO.getDetailList())) {
            validateRecipientQuantity(sampleRecipientEntity.getWarehouseId(), addDTO.getDetailList());

            // 保存明细数据
            List<SampleRecipientDetailEntity> detailEntities = new ArrayList<>();
            for (SampleRecipientDTO.ProductDTO productDTO : addDTO.getDetailList()) {
                SampleRecipientDetailEntity detailEntity = new SampleRecipientDetailEntity();
                detailEntity.setMainId(sampleRecipientEntity.getId());
                detailEntity.setSkuNo(productDTO.getSkuNo());
                detailEntity.setSkuId(productDTO.getSkuId());
                detailEntity.setProductName(productDTO.getProductName());
                detailEntity.setRecipientQty(productDTO.getQuantity());
                detailEntity.setDeliveryQty(0); // 初始已出库数量为0
                detailEntity.setExecStatus(SampleRecipientExecStatusEnum.WAIT_OUTSTOCK.getExecStatus()); // 初始状态为待出库
                detailEntity.setRemark(productDTO.getRemark());
                detailEntities.add(detailEntity);
            }
            
            // 批量保存明细数据
            if (CollUtil.isNotEmpty(detailEntities)) {
                boolean detailSaveResult = sampleRecipientDetailService.saveBatch(detailEntities);
                if (!detailSaveResult) {
                    throw new ServiceException("样品领用单明细保存失败");
                }
                log.info("样品领用单明细保存成功，共保存{}条明细", detailEntities.size());
            }
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品领用单" , sampleRecipientEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RECIPIENT.getCode(), sampleRecipientEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(sampleRecipientEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleRecipientDTO.UpdateDTO addOrUpdateDTO) {
        SampleRecipientEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品领用单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        SampleRecipientEntity sampleRecipientEntity =  BeanMapperUtils.map(SampleRecipientEntity.class, addOrUpdateDTO);
        // 数据处理
        handleData(sampleRecipientEntity);
        
        // 库存校验
        if (CollUtil.isNotEmpty(addOrUpdateDTO.getDetailList())) {
            validateRecipientQuantity(sampleRecipientEntity.getWarehouseId(), addOrUpdateDTO.getDetailList());
        }
        
        log.info("编辑 开始修改样品领用单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(sampleRecipientEntity);
        if(!save) {
            throw new ServiceException("样品领用单保存失败");
        }
        // 修改明细数据（增量更新）
        if (CollUtil.isNotEmpty(addOrUpdateDTO.getDetailList())) {
            // 查询已存在的明细数据
            List<SampleRecipientDetailEntity> existingDetails = sampleRecipientDetailService.lambdaQuery()
                .eq(SampleRecipientDetailEntity::getMainId, addOrUpdateDTO.getId())
                .list();
            
            // 构建已存在明细的Map，key为skuId，value为明细实体
            Map<String, SampleRecipientDetailEntity> existingDetailMap = existingDetails.stream()
                .collect(Collectors.toMap(SampleRecipientDetailEntity::getSkuId, item -> item));
            
            // 处理明细数据：新增、更新、删除
            List<SampleRecipientDetailEntity> toSave = new ArrayList<>();
            List<String> toDelete = new ArrayList<>();
            Set<String> processedSkuIds = new HashSet<>();
            
            for (SampleRecipientDTO.ProductDTO productDTO : addOrUpdateDTO.getDetailList()) {
                String skuId = productDTO.getSkuId();
                processedSkuIds.add(skuId);
                
                SampleRecipientDetailEntity existingDetail = existingDetailMap.get(skuId);
                
                if (existingDetail != null) {
                    // 更新已存在的明细
                    existingDetail.setProductName(productDTO.getProductName());
                    existingDetail.setRecipientQty(productDTO.getQuantity());
                    existingDetail.setRemark(productDTO.getRemark());
                    existingDetail.setSkuNo(productDTO.getSkuNo());
                    existingDetail.setSkuId(productDTO.getSkuId());
                    // 注意：不重置已出库数量和执行状态，保持业务连续性
                    toSave.add(existingDetail);
                } else {
                    // 新增明细
                    SampleRecipientDetailEntity newDetail = new SampleRecipientDetailEntity();
                    newDetail.setMainId(addOrUpdateDTO.getId());
                    newDetail.setSkuNo(productDTO.getSkuNo());
                    newDetail.setSkuId(skuId);
                    newDetail.setProductName(productDTO.getProductName());
                    newDetail.setRecipientQty(productDTO.getQuantity());
                    newDetail.setDeliveryQty(0); // 新明细初始已出库数量为0
                    newDetail.setExecStatus(SampleRecipientExecStatusEnum.WAIT_OUTSTOCK.getExecStatus()); // 初始状态为待出库
                    newDetail.setRemark(productDTO.getRemark());
                    toSave.add(newDetail);
                }
            }
            
            // 找出需要删除的明细（在新列表中不存在的）
            for (SampleRecipientDetailEntity existingDetail : existingDetails) {
                if (!processedSkuIds.contains(existingDetail.getSkuId())) {
                    toDelete.add(existingDetail.getId());
                }
            }
            
            // 执行删除操作
            if (!toDelete.isEmpty()) {
                boolean deleteResult = sampleRecipientDetailService.lambdaUpdate()
                    .in(SampleRecipientDetailEntity::getId, toDelete)
                    .remove();
                
                if (!deleteResult) {
                    log.warn("删除样品领用单明细数据失败，ids：{}", toDelete);
                }
                log.info("删除样品领用单明细数据成功，共删除{}条明细", toDelete.size());
            }
            
            // 执行保存/更新操作
            if (!toSave.isEmpty()) {
                boolean saveResult = sampleRecipientDetailService.saveOrUpdateBatch(toSave);
                if (!saveResult) {
                    throw new ServiceException("样品领用单明细保存失败");
                }
                log.info("样品领用单明细更新成功，共处理{}条明细", toSave.size());
            }
        }

        // 记录主单操作日志
            log.info("编辑 开始记录样品领用单日志数据，单号：【{}】", sampleRecipientEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleRecipientEntity.getCode(), "样品领用单");
        operateLogService.addModuleOperateLogByObj(old, sampleRecipientEntity, ModuleTypeEnum.SAMPLE_RECIPIENT.getCode(), sampleRecipientEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public PagingVO<SampleRecipientDTO.ListDTO> paging(PagingDTO<SampleRecipientDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SampleRecipientDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<SampleRecipientDTO.TabListDTO> tabList(PermissionsDTO param) {
        SampleRecipientDTO.PagingParamDTO searchParam = new SampleRecipientDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        
        // 使用一个SQL查询获取所有状态的统计数量
        List<SampleRecipientDTO.TabListDTO> list = baseMapper.getAllStatusCounts(param.getPermissionSql());
        
        // 计算合计数量
        int totalCount = list.stream().mapToInt(SampleRecipientDTO.TabListDTO::getCount).sum();
        list.add(new SampleRecipientDTO.TabListDTO("all", totalCount));
        
        return list;
    }

    @Override
    public Boolean exportList(SampleRecipientDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("样品领用单导出", EXPORT_WMS_SAMPLE_RECIPIENT_REPORT.getCode(), param);
        return true;
    }

    @Override
    public PagingVO<SampleRecipientDTO.ListDTO> getSampleRecipientPageData(PagingDTO<SampleRecipientDTO.ExportDTO> dto) {
        Page<SampleRecipientDTO.ExportDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<SampleRecipientDTO.ListDTO> pageData = this.baseMapper.listExport(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        SampleRecipientEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到样品领用单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改样品领用单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        log.info("提交 开始启动样品领用单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录样品领用单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品领用单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RECIPIENT.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(SampleRecipientDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(SampleRecipientDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        SampleRecipientEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品领用单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RECIPIENT.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(SampleRecipientEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.SAMPLE_RECIPIENT.getCode());
        approveDTO.setApproveType(ApproveTypeEnum.getByCode(dto.getType()));
        approveDTO.setComment(dto.getComment());
        approveDTO.setUserId(userInfo.getUid());
        approveDTO.setVariablesMap(BeanUtil.beanToMap(entity));
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

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        SampleRecipientEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品领用单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);
        // TODO 检查是否有下推单据（如果支持下推的话）明细数据

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品领用单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RECIPIENT.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(SampleRecipientEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        // TODO 下游盘点计划单反审核
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        SampleRecipientEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品领用单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除明细数据
        boolean detailDeleteResult = sampleRecipientDetailService.lambdaUpdate()
            .eq(SampleRecipientDetailEntity::getMainId, id)
            .remove();
        
        if (!detailDeleteResult) {
            log.warn("删除样品领用单明细数据失败，id：{}", id);
        }

        // 删除主单数据
        log.info("删除 开始删除样品领用单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除样品领用单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品领用单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除样品领用单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        SampleRecipientEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品领用单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(entity.getApproveStatus().getStatus()) && !ApproveStatusEnum.REJECT.getStatus().equals(entity.getApproveStatus().getStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改样品领用单状态数据，id：【{}】", id);
        lambdaUpdate().eq(SampleRecipientEntity::getId, id)
            .set(SampleRecipientEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(SampleRecipientEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品领用单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RECIPIENT.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        SampleRecipientEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品领用单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改样品领用单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品领用单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RECIPIENT.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.SAMPLE_RECIPIENT.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    /**
    * 结束领用
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO finishRecipient(String id) {
        SampleRecipientEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到样品领用单数据"));
        
        // 验证结束领用条件
        validateFinishRecipient(entity);
        
        // 更新单据状态为已结束
        log.info("结束领用 开始修改样品领用单状态，id：【{}】", id);
        // 注意：已删除status字段，使用其他方式标记结束状态
        // 可以通过添加结束时间字段或其他业务字段来标识
        List<SampleRecipientDetailEntity> list = sampleRecipientDetailService.lambdaQuery().eq(SampleRecipientDetailEntity::getMainId, entity.getId()).eq(SampleRecipientDetailEntity::getIsDeleted, false).list();
        for (SampleRecipientDetailEntity sampleRecipientDetailEntity : list) {
            sampleRecipientDetailEntity.setExecStatus(SampleRecipientExecStatusEnum.COMPLETE_OUTSTOCK.getExecStatus());
        }
        sampleRecipientDetailService.saveOrUpdateBatch(list);
        // 操作日志
        log.info("结束领用 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据结束领用操作", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "样品领用单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SAMPLE_RECIPIENT.getCode(), entity.getId(), "结束领用操作");
        
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.UPDATE);
    }

    /**
    * 验证结束领用条件
    */
    private void validateFinishRecipient(SampleRecipientEntity entity) {
        // 只有已审核通过的单据允许结束领用
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException("只有已审核通过的样品领用单能结束领用");
        }
        
        // 检查明细状态，只有部分出库的样品领用单能结束领用
        List<SampleRecipientDetailEntity> detailList = sampleRecipientDetailService.lambdaQuery()
            .eq(SampleRecipientDetailEntity::getMainId, entity.getId())
            .list();
        
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException("样品领用单明细不能为空");
        }
        
        // 检查是否有部分出库状态的明细
        boolean hasPartOutstock = detailList.stream()
            .anyMatch(detail -> SampleRecipientExecStatusEnum.PART_OUTSTOCK.getExecStatus().equals(detail.getExecStatus()));
        
        if (!hasPartOutstock) {
            throw new ServiceException("只有部分出库的样品领用单能结束领用");
        }
        
        // 检查是否所有明细都是"已出库"或"待出库"
        boolean allCompletedOrWaiting = detailList.stream()
            .allMatch(detail -> 
                SampleRecipientExecStatusEnum.COMPLETE_OUTSTOCK.getExecStatus().equals(detail.getExecStatus()) ||
                SampleRecipientExecStatusEnum.WAIT_OUTSTOCK.getExecStatus().equals(detail.getExecStatus())
            );
        
        if (allCompletedOrWaiting) {
            throw new ServiceException("只有部分出库的样品领用单能结束领用");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, SampleRecipientEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理

        return Boolean.TRUE;
    }

    @Override
    public SampleRecipientDTO.ViewDTO view(String id) {
        SampleRecipientEntity sampleRecipientEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到样品领用单数据"));
        SampleRecipientDTO.ViewDTO data = BeanMapperUtils.map(SampleRecipientDTO.ViewDTO.class, sampleRecipientEntity);
        // 数据填充处理
        fillOne(data);
        // 查询明细数据
        List<SampleRecipientDetailEntity> detailList = sampleRecipientDetailService.lambdaQuery()
            .eq(SampleRecipientDetailEntity::getMainId, id)
            .list();
        
        // 将明细数据转换为DTO格式
        List<SampleRecipientDTO.ProductDTO> productList = new ArrayList<>();
        if (CollUtil.isNotEmpty(detailList)) {
            // 构建库存查询参数
            List<InventoryDTO.InventoryBySkuIdAndWarehouseDTO> inventoryParams = new ArrayList<>();
            for (SampleRecipientDetailEntity detail : detailList) {
                InventoryDTO.InventoryBySkuIdAndWarehouseDTO param = new InventoryDTO.InventoryBySkuIdAndWarehouseDTO();
                param.setWarehouseId(sampleRecipientEntity.getWarehouseId());
                param.setSkuId(detail.getSkuId());
                inventoryParams.add(param);
            }

            List<InventoryDTO.InventoryViewQtyDTO> inventoryList = inventoryService.getInventoryQty(inventoryParams);
            Map<String, InventoryDTO.InventoryViewQtyDTO> inventoryMap = inventoryList.stream()
                    .collect(Collectors.toMap(InventoryDTO.InventoryViewQtyDTO::getSkuId, item -> item));
            for (SampleRecipientDetailEntity detail : detailList) {
                SampleRecipientDTO.ProductDTO productDTO = new SampleRecipientDTO.ProductDTO();
                productDTO.setId(detail.getId());
                productDTO.setSkuNo(detail.getSkuNo());
                productDTO.setSkuId(detail.getSkuId());
                productDTO.setProductName(detail.getProductName());
                productDTO.setQuantity(detail.getRecipientQty());
                productDTO.setRemark(detail.getRemark());
                productDTO.setUsableQty(inventoryMap.get(detail.getSkuId())!=null?inventoryMap.get(detail.getSkuId()).getUsableQty():0);
                productList.add(productDTO);
            }
        }
        
        // 设置明细数据到ViewDTO中
        data.setDetailList(productList);
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(SampleRecipientEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.SAMPLE_RECIPIENT.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        startDTO.setVariablesMap(BeanUtil.beanToMap(entity));
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(SampleRecipientDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
    * 审核更新审核信息
    * @param id
    * @param approveStatus
    */
    public void updateForApprove(String id, String approveStatus) {
        //当前登录人
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        this.lambdaUpdate().eq(SampleRecipientEntity::getId, id)
            .set(SampleRecipientEntity::getApproveUserId, userInfo.getUid())
            .set(SampleRecipientEntity::getApproveUserName, userInfo.getUserName())
            .set(SampleRecipientEntity::getApproveStatus, approveStatus)
            .set(SampleRecipientEntity::getApproveTime, LocalDateTime.now())
            .update(new SampleRecipientEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(SampleRecipientEntity::getId, id)
            .set(SampleRecipientEntity::getApproveUserId, "")
            .set(SampleRecipientEntity::getApproveUserName, "")
            .set(SampleRecipientEntity::getApproveStatus, approveStatus)
            .set(SampleRecipientEntity::getApproveTime, null)
            .update(new SampleRecipientEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(SampleRecipientEntity::getId, id)
        .set(SampleRecipientEntity::getApproveStatus, approveStatus)
        .update(new SampleRecipientEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<SampleRecipientDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(list.stream().map(SampleRecipientDTO.ListDTO::getWarehouseId).collect(Collectors.toList()));
        Map<String, String> warehouseNameMap = warehouseList.stream()
            .collect(Collectors.toMap(WarehouseDTO.UpdateDTO::getId, WarehouseDTO.UpdateDTO::getName));
        

        
        // 属性赋值
        for(SampleRecipientDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            data.setUsage(SampleUsageEnum.getName(data.getUsage()));
            data.setUsageScope(SampleUsageScopeEnum.getName(data.getUsage()));
            // 设置仓库名称
            data.setWarehouseName(warehouseNameMap.get(data.getWarehouseId()));
            data.setExecStatusName(SampleRecipientExecStatusEnum.getName(data.getExecStatus()));
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(SampleRecipientEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SampleRecipientEntity sampleRecipientEntity) {
        // 查询用户信息
        List<String> userIds = new ArrayList<>();
        userIds.add(sampleRecipientEntity.getUserId());
        userIds.add(sampleRecipientEntity.getUseUserId());
        Map<String, String> userNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(userIds)) {
            try {
                List<SysDepartmentUserNumberDTO> userList = sysUserFeign.listDeptUserByUserIdList(userIds);
                if (CollUtil.isNotEmpty(userList)) {
                    userNameMap = userList.stream()
                            .collect(Collectors.toMap(SysDepartmentUserNumberDTO::getUserId, SysDepartmentUserNumberDTO::getUserName));
                }
            } catch (Exception e) {
                log.warn("查询用户信息失败，错误：{}", e.getMessage());
            }
        }

        sampleRecipientEntity.setUserName(userNameMap.get(sampleRecipientEntity.getUserId()));
        sampleRecipientEntity.setUseUserName(userNameMap.get(sampleRecipientEntity.getUseUserId()));
    }

    /**
     * 校验领用数量（新增和修改场景通用）
     * 实时库存-冻结库存-领用数量>0，否则报错"可领用库存不足"
     */
    private void validateRecipientQuantity(String warehouseId, List<SampleRecipientDTO.ProductDTO> detailList) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }

        // 构建库存查询参数
        List<InventoryDTO.InventoryBySkuIdAndWarehouseDTO> inventoryParams = new ArrayList<>();
        for (SampleRecipientDTO.ProductDTO detail : detailList) {
            InventoryDTO.InventoryBySkuIdAndWarehouseDTO param = new InventoryDTO.InventoryBySkuIdAndWarehouseDTO();
            param.setWarehouseId(warehouseId);
            param.setSkuId(detail.getSkuId());
            inventoryParams.add(param);
        }

        // 查询库存信息
        List<InventoryDTO.InventoryViewQtyDTO> inventoryList = inventoryService.getInventoryQty(inventoryParams);
        Map<String, InventoryDTO.InventoryViewQtyDTO> inventoryMap = inventoryList.stream()
            .collect(Collectors.toMap(InventoryDTO.InventoryViewQtyDTO::getSkuId, item -> item));

        // 校验每个明细的库存
        for (SampleRecipientDTO.ProductDTO detail : detailList) {
            InventoryDTO.InventoryViewQtyDTO inventory = inventoryMap.get(detail.getSkuId());
            if (inventory == null) {
                throw new ServiceException(String.format("SKU【%s】在仓库【%s】中不存在库存信息", detail.getSkuNo(), warehouseId));
            }

            // 计算可领用库存：可用库存 - 领用数量
            int availableQty = inventory.getUsableQty() - detail.getQuantity();
            if (availableQty < 0) {
                throw new ServiceException(String.format("SKU【%s】可领用库存不足，可用库存：%d，领用数量：%d", 
                    detail.getSkuNo(), inventory.getUsableQty(), detail.getQuantity()));
            }

                    log.info("SKU【{}】库存校验通过，可用库存：{}，领用数量：{}，剩余可领用库存：{}", 
            detail.getSkuNo(), inventory.getUsableQty(), detail.getQuantity(), availableQty);
    }


}


        /**
     * 查询SKU成本
     */
        @Override
    public List<SampleRecipientDTO.SkuDTO> querySkuCost(SampleRecipientDTO.SkuCostQueryDTO dto) {
        try {
            log.info("开始查询SKU成本，参数：{}", JSONUtil.toJsonStr(dto));
            
            // 参数校验
            if (CollUtil.isEmpty(dto.getDetailList())) {
                throw new ServiceException("SKU成本查询明细列表不能为空");
            }
            
            // 1. 根据SKU编号列表查询SKU基本信息
            List<String> skuNoList = dto.getDetailList().stream()
                    .map(SampleRecipientDTO.SkuCostQueryDetailDTO::getSkuNo)
                    .collect(Collectors.toList());
            
            List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);
            if (CollUtil.isEmpty(skuList)) {
                log.warn("未找到SKU信息，skuNoList：{}", skuNoList);
                return Collections.emptyList();
            }
            
            // 2. 构建调用LogisticsFeign的参数
            List<InventorySkuCostDTO.QueryDetailDTO> queryDetailList = new ArrayList<>();
            for (SampleRecipientDTO.SkuCostQueryDetailDTO detail : dto.getDetailList()) {
                // 找到对应的SKU信息
                SkuVO sku = skuList.stream()
                        .filter(s -> s.getSkuNo().equals(detail.getSkuNo()))
                        .findFirst()
                        .orElse(null);
                
                if (sku != null) {
                    InventorySkuCostDTO.QueryDetailDTO queryDetail = InventorySkuCostDTO.QueryDetailDTO.builder()
                            .skuId(sku.getSkuId())
                            .warehouseId(detail.getWarehouseId())
                            .build();
                    queryDetailList.add(queryDetail);
                }
            }
            
            if (CollUtil.isEmpty(queryDetailList)) {
                log.warn("没有有效的查询参数");
                return Collections.emptyList();
            }
            
            // 3. 调用LogisticsFeign查询SKU成本
            List<InventorySkuCostDTO.SkuCostDTO> skuCostList = logisticsFeign.listSkuCostByDetailList(queryDetailList);
            
            // 4. 组装返回结果
            List<SampleRecipientDTO.SkuDTO> result = new ArrayList<>();
            for (SampleRecipientDTO.SkuCostQueryDetailDTO detail : dto.getDetailList()) {
                SampleRecipientDTO.SkuDTO skuDTO = new SampleRecipientDTO.SkuDTO();
                skuDTO.setSkuNo(detail.getSkuNo());
                
                // 找到对应的SKU信息
                SkuVO sku = skuList.stream()
                        .filter(s -> s.getSkuNo().equals(detail.getSkuNo()))
                        .findFirst()
                        .orElse(null);
                if (sku != null) {
                    skuDTO.setSkuId(sku.getSkuId());
                }
                
                // 找到对应的成本信息
                InventorySkuCostDTO.SkuCostDTO skuCost = skuCostList.stream()
                        .filter(cost -> cost.getSkuId().equals(sku != null ? sku.getSkuId() : null) 
                                && cost.getWarehouseId().equals(detail.getWarehouseId()))
                        .findFirst()
                        .orElse(null);
                
                if (skuCost != null) {
                    // 使用材料成本作为SKU成本
                    skuDTO.setSkuCost(skuCost.getProductCost() != null ? skuCost.getProductCost() : BigDecimal.ZERO);
                } else {
                    skuDTO.setSkuCost(BigDecimal.ZERO);
                }
                
                result.add(skuDTO);
                }
            
            log.info("SKU成本查询完成，共查询到{}条记录", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("查询SKU成本失败，参数：{}，错误：{}", JSONUtil.toJsonStr(dto), e.getMessage(), e);
            throw new ServiceException("查询SKU成本失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取SKU列表（支持高级查询和模糊搜索）
     */
    @Override
    public PagingVO<SampleRecipientDTO.SkuListResponseDTO> getSkuList(SampleRecipientDTO.SkuListQueryDTO dto) {
        try {
            log.info("开始获取SKU列表，参数：{}", JSONUtil.toJsonStr(dto));

            // 参数校验
            if (StrUtil.isBlank(dto.getSearchKeyword())) {
                return new PagingVO<>();
            }

            // 1. 调用PLM系统获取审核通过的SKU列表
            // 构建查询参数，只查询审核通过的产品
            ProductSkuDTO productSkuDTO = new ProductSkuDTO();
            productSkuDTO.setRemoteSearchSku(dto.getSearchKeyword());
            productSkuDTO.setStatusList(Collections.singletonList(2)); // 2表示审核通过

            PagingDTO<ProductSkuDTO> pagingDTO = new PagingDTO<>();
            pagingDTO.setParams(productSkuDTO);
            pagingDTO.setCurrPage(dto.getCurrPage());
            pagingDTO.setPageSize(dto.getPageSize());

            // 调用PLM系统的listSku接口
            PagingVO<ProductDetailDTO.SkuDTO> plmResult = plmTaskFeign.listSku(pagingDTO);

            if (plmResult == null || CollUtil.isEmpty(plmResult.getList())) {
                log.info("PLM系统未返回SKU数据");
                return new PagingVO<>();
            }

            // 2. 查询库存信息
            List<String> skuIds = plmResult.getList().stream()
                    .map(ProductDetailDTO.SkuDTO::getSkuId)
                    .collect(Collectors.toList());

            Map<String, InventoryDTO.RealQtyDTO> inventoryMap = new HashMap<>();
            if (StrUtil.isNotBlank(dto.getWarehouseId()) && CollUtil.isNotEmpty(skuIds)) {
                try {
                    // 查询指定仓库的库存信息，使用现有的getRealQty方法
                    List<String> warehouseIds = Collections.singletonList(dto.getWarehouseId());
                    List<String> inventoryStatusList = Collections.singletonList(InventoryStatusEnum.USABLE.getCode()); // 只查询可用库存
                    List<InventoryDTO.RealQtyDTO> inventoryList = inventoryService.getRealQty(skuIds, warehouseIds, inventoryStatusList);
                    if (CollUtil.isNotEmpty(inventoryList)) {
                        inventoryMap = inventoryList.stream()
                                .collect(Collectors.toMap(InventoryDTO.RealQtyDTO::getSkuId, Function.identity()));
                    }
                } catch (Exception e) {
                    log.warn("查询库存信息失败，错误：{}", e.getMessage());
                }
            }

            // 3. 组装返回结果
            List<SampleRecipientDTO.SkuListResponseDTO> resultList = new ArrayList<>();
            for (ProductDetailDTO.SkuDTO plmSku : plmResult.getList()) {
                SampleRecipientDTO.SkuListResponseDTO responseDTO = new SampleRecipientDTO.SkuListResponseDTO();
                responseDTO.setSkuId(plmSku.getSkuId());
                responseDTO.setSkuNo(plmSku.getSkuNo());
                responseDTO.setProductName(plmSku.getProductName());
                responseDTO.setSpuNo(plmSku.getSpuNo());
                responseDTO.setRetailPrice(plmSku.getRetailPrice());

                // 设置库存信息
                InventoryDTO.RealQtyDTO inventory = inventoryMap.get(plmSku.getSkuId());
                if (inventory != null) {
                    responseDTO.setAvailableQty(inventory.getRealQty());
                    responseDTO.setFrozenQty(0); // RealQtyDTO没有冻结库存字段，设为0
                    responseDTO.setTotalQty(inventory.getRealQty());
                } else {
                    responseDTO.setAvailableQty(0);
                    responseDTO.setFrozenQty(0);
                    responseDTO.setTotalQty(0);
                }

                resultList.add(responseDTO);
            }

            // 4. 构建分页结果
            PagingVO<SampleRecipientDTO.SkuListResponseDTO> result = new PagingVO<>(resultList,plmResult.getTotalCount(),plmResult.getPageSize(),plmResult.getCurrPage());

            log.info("SKU列表查询完成，共查询到{}条记录", resultList.size());
            return result;

        } catch (Exception e) {
            log.error("获取SKU列表失败，参数：{}，错误：{}", JSONUtil.toJsonStr(dto), e.getMessage(), e);
            throw new ServiceException("获取SKU列表失败：" + e.getMessage());
        }
    }

    /**
     * 下推其他出库单查询
     */
    @Override
    public List<SampleRecipientDTO.ViewGenerateOutboundOrderDTO> viewGenerateOutboundOrder(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return new ArrayList<>();
        }
        
        List<SampleRecipientDTO.ViewGenerateOutboundOrderDTO> result = new ArrayList<>();
        
        try {
            // 查询样品领用单主表信息
            List<SampleRecipientEntity> mainList = this.lambdaQuery()
                .in(SampleRecipientEntity::getId, ids)
                .list();
            
            if (CollUtil.isEmpty(mainList)) {
                return result;
            }
            
            // 查询样品领用单明细数据
            List<SampleRecipientDetailEntity> detailList = sampleRecipientDetailService.lambdaQuery()
                .in(SampleRecipientDetailEntity::getMainId, ids)
                .list();
            
            if (CollUtil.isEmpty(detailList)) {
                return result;
            }
            
            // 构建主表ID到主表信息的映射
            Map<String, SampleRecipientEntity> mainMap = mainList.stream()
                .collect(Collectors.toMap(SampleRecipientEntity::getId, Function.identity()));
            
            // 获取所有SKU ID
            List<String> skuIds = detailList.stream()
                .map(SampleRecipientDetailEntity::getSkuId)
                .distinct()
                .collect(Collectors.toList());
            
            // 获取所有仓库ID
            List<String> warehouseIds = mainList.stream()
                .map(SampleRecipientEntity::getWarehouseId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
            
            // 查询仓库信息
            Map<String, String> warehouseNameMap = new HashMap<>();
            if (CollUtil.isNotEmpty(warehouseIds)) {
                try {
                    List<WarehouseEntity> warehouseList = warehouseService.lambdaQuery()
                        .in(WarehouseEntity::getId, warehouseIds)
                        .list();
                    if (CollUtil.isNotEmpty(warehouseList)) {
                        warehouseNameMap = warehouseList.stream()
                            .collect(Collectors.toMap(WarehouseEntity::getId, WarehouseEntity::getName));
                    }
                } catch (Exception e) {
                    log.warn("查询仓库信息失败，错误：{}", e.getMessage());
                }
            }
            
            // 查询库存信息
            Map<String, Integer> inventoryMap = new HashMap<>();
            if (CollUtil.isNotEmpty(skuIds) && CollUtil.isNotEmpty(warehouseIds)) {
                for (String skuId : skuIds) {
                    for (String warehouseId : warehouseIds) {
                        try {
                            Integer inventoryQty = inventoryService.getUsableInventoryTotal(warehouseId, skuId);
                            String key = skuId + "_" + warehouseId;
                            inventoryMap.put(key, inventoryQty != null ? inventoryQty : 0);
                        } catch (Exception e) {
                            log.warn("查询库存失败，SKU ID：{}，仓库ID：{}，错误：{}", skuId, warehouseId, e.getMessage());
                            String key = skuId + "_" + warehouseId;
                            inventoryMap.put(key, 0);
                        }
                    }
                }
            }
            
            // 组装返回数据
            for (SampleRecipientDetailEntity detail : detailList) {
                SampleRecipientEntity main = mainMap.get(detail.getMainId());
                if (main == null) {
                    continue;
                }
                
                SampleRecipientDTO.ViewGenerateOutboundOrderDTO dto = new SampleRecipientDTO.ViewGenerateOutboundOrderDTO();
                
                // 设置基本信息
                dto.setSourceId(main.getId());
                dto.setSourceCode(main.getCode());
                dto.setSourceDetailId(detail.getId());
                dto.setSourceType(SourceTypeEnum.SAMPLE_RECIPIENT.getCode());
                
                // 设置SKU信息
                dto.setSkuNo(detail.getSkuNo());
                dto.setProductName(detail.getProductName());
                
                // 设置仓库信息
                dto.setWarehouseId(main.getWarehouseId());
                dto.setWarehouseName(warehouseNameMap.getOrDefault(main.getWarehouseId(), ""));
                
                // 设置领用人信息
                dto.setUserName(main.getUserName());
                dto.setUserId(main.getUserId());
                
                // 设置数量信息
                Integer recipientQty = detail.getRecipientQty() != null ? detail.getRecipientQty() : 0;
                Integer deliveryQty = detail.getDeliveryQty() != null ? detail.getDeliveryQty() : 0;
                dto.setReservedQty(recipientQty - deliveryQty); // 待出库数量 = 领用数量 - 已出库数量
                dto.setDeliveryQty(deliveryQty);
                
                // 设置即时可用库存
                String inventoryKey = detail.getSkuId() + "_" + main.getWarehouseId();
                Integer inventoryQty = inventoryMap.getOrDefault(inventoryKey, 0);
                dto.setCurInventoryQty(inventoryQty);
                
                // 设置其他字段
                dto.setBillDate(main.getRecipientDate());
                dto.setRemark(detail.getRemark());
                
                result.add(dto);
            }
            
            log.info("下推其他出库单查询完成，查询到{}条记录", result.size());
            
        } catch (Exception e) {
            log.error("下推其他出库单查询失败，错误：{}", e.getMessage(), e);
        }
        
        return result;
    }

    /**
     * 下推其他出库单保存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> generateOutboundOrder(SampleRecipientDTO.ListGenerateOutboundOrderDTO dto) {
        if (CollUtil.isEmpty(dto.getList())) {
            return new ArrayList<>();
        }
        
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        
        try {
            // 按照 sourceId 进行分组
            Map<String, List<SampleRecipientDTO.ViewGenerateOutboundOrderDTO>> groupedBySourceId = dto.getList().stream()
                .collect(Collectors.groupingBy(SampleRecipientDTO.ViewGenerateOutboundOrderDTO::getSourceId));
            
            // 遍历分组后的数据，为每个领用单创建一个其他出库单
            for (Map.Entry<String, List<SampleRecipientDTO.ViewGenerateOutboundOrderDTO>> entry : groupedBySourceId.entrySet()) {
                String sourceId = entry.getKey();
                List<SampleRecipientDTO.ViewGenerateOutboundOrderDTO> items = entry.getValue();
                
                try {
                    // 为每个领用单创建一个其他出库单，详情数据为列表数据
                    BatchResultDTO resultDTO = createOtherOutboundOrderBySourceId(sourceId, items);
                    resultDTOS.add(resultDTO);
                    
                } catch (Exception e) {
                    log.error("下推其他出库单失败，sourceId: {}, error: {}", sourceId, e.getMessage(), e);
                    // 获取第一个项目的信息用于错误返回
                    SampleRecipientDTO.ViewGenerateOutboundOrderDTO firstItem = items.get(0);
                    BatchResultDTO resultDTO = BatchResultDTO.fail(
                        sourceId, 
                        firstItem.getSourceCode(), 
                        e.getMessage()
                    );
                    resultDTOS.add(resultDTO);
                }
            }
        } catch (Exception e) {
            log.error("下推其他出库单批量处理失败", e);
            // 如果批量处理失败，返回第一个失败的结果
            if (!dto.getList().isEmpty()) {
                SampleRecipientDTO.ViewGenerateOutboundOrderDTO firstItem = dto.getList().get(0);
                BatchResultDTO resultDTO = BatchResultDTO.fail(
                    firstItem.getSourceId(), 
                    firstItem.getSourceCode(), 
                    e.getMessage()
                );
                resultDTOS.add(resultDTO);
            }
        }
        
        return resultDTOS;
    }
    
    /**
     * 为指定领用单创建其他出库单（包含多个明细）
     * @param sourceId 领用单ID
     * @param items 明细列表
     * @return 创建结果
     */
    private BatchResultDTO createOtherOutboundOrderBySourceId(String sourceId, List<SampleRecipientDTO.ViewGenerateOutboundOrderDTO> items) {
        try {
            // 查询样品领用单主表信息
            SampleRecipientEntity sampleRecipient = this.getById(sourceId);
            if (sampleRecipient == null) {
                return BatchResultDTO.fail(sourceId, items.get(0).getSourceCode(), "样品领用单不存在");
            }
            
            // 构建其他出库单主表数据
            OtherOutstockDTO.AddDTO addDTO = new OtherOutstockDTO.AddDTO();
            
            // 获取第一个项目的基础信息
            SampleRecipientDTO.ViewGenerateOutboundOrderDTO firstItem = items.get(0);
            
            // 基础信息映射
            addDTO.setBillDate(firstItem.getBillDate() != null ? firstItem.getBillDate() : LocalDate.now()); // 出库日期
            addDTO.setInventoryDirection("ordinary"); // 库存方向：固定为"普通"
            addDTO.setWarehouseId(firstItem.getWarehouseId()); // 发货仓库
            addDTO.setType("0"); // 业务类型：固定为"物料领用"
            addDTO.setOutType("样品领用"); // 出库类型：固定为"样品领用"
            addDTO.setReceiverId(firstItem.getUserId()); // 领料人ID
            addDTO.setReceiveOrgId(sampleRecipient.getPickOrgId()); // 领料组织ID
            addDTO.setDeptId(sampleRecipient.getDeptId()); // 领料部门ID
            addDTO.setProcessApplyCode(firstItem.getSourceCode()); // 流程申请单号：样品领用单号
            addDTO.setRemark("样品领用单【下推】其他出库单"); // 备注
            
            // 构建客户信息
            OtherOutstockCustomerDTO.AddDTO customerDTO = new OtherOutstockCustomerDTO.AddDTO();
            customerDTO.setReceiveAddress(sampleRecipient.getReceiveAddress()); // 收货地址
            customerDTO.setReceiverName(sampleRecipient.getReceiverName()); // 收货人
            customerDTO.setTelNumber(sampleRecipient.getReceivePhone()); // 联系电话
            addDTO.setOtherOutstockCustomer(customerDTO);
            
            // 构建明细信息 - 包含所有明细项
            List<OtherOutstockDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (SampleRecipientDTO.ViewGenerateOutboundOrderDTO item : items) {
                // 查询样品领用单明细信息
                SampleRecipientDetailEntity detail = sampleRecipientDetailService.getById(item.getSourceDetailId());
                if (detail == null) {
                    log.warn("样品领用单明细不存在，sourceDetailId: {}", item.getSourceDetailId());
                    continue;
                }
                
                OtherOutstockDetailDTO.AddDTO detailDTO = new OtherOutstockDetailDTO.AddDTO();
                detailDTO.setSkuId(detail.getSkuId()); // SKU ID
                detailDTO.setSkuNo(item.getSkuNo()); // SKU编号
                detailDTO.setActualQty(item.getOutQty() != null ? item.getOutQty() : item.getReservedQty()); // 实发数量：出库数量或待出库数量
                detailDTO.setWarehouseLocation(item.getWarehouseLocation()); // 仓位
                detailDTO.setRemark(StringUtils.isNotBlank(item.getRemark()) ? item.getRemark() : "样品领用单【下推】其他出库单填写的备注"); // 出库备注
                
                detailList.add(detailDTO);
            }
            
            if (detailList.isEmpty()) {
                return BatchResultDTO.fail(sourceId, firstItem.getSourceCode(), "没有有效的明细数据");
            }
            
            addDTO.setDetailList(detailList);
            
            // 调用其他出库单服务创建出库单 并审核通过
            String outboundOrderId = otherOutstockService.add(addDTO);
            if (CharSequenceUtil.isBlank(outboundOrderId)) {
                throw new ServiceException(ApiError.ERROR_1019);
            }
            
            if (StringUtils.isNotBlank(outboundOrderId)) {
                // 设置来源字段到其他出库单主表
                OtherOutstockEntity outboundOrder = otherOutstockService.getById(outboundOrderId);
                if (outboundOrder != null) {
                    outboundOrder.setSourceType(SourceTypeEnum.SAMPLE_RECIPIENT.getCode());
                    outboundOrder.setSourceId(sourceId);
                    outboundOrder.setSourceCode(firstItem.getSourceCode());
                    otherOutstockService.updateById(outboundOrder);
                }
                // 获取其他出库单明细列表
                List<OtherOutstockDetailEntity> otherOutstockDetailEntities = otherOutstockDetailService.listByMainId(outboundOrderId);
                
                // 构建 skuId 到 sourceDetailId 的映射关系
                Map<String, String> skuIdToSourceDetailIdMap = new HashMap<>();
                for (SampleRecipientDTO.ViewGenerateOutboundOrderDTO item : items) {
                    // 查询样品领用单明细信息
                    SampleRecipientDetailEntity detail = sampleRecipientDetailService.getById(item.getSourceDetailId());
                    if (detail != null) {
                        skuIdToSourceDetailIdMap.put(detail.getSkuId(), item.getSourceDetailId());
                    }
                }
                
                // 批量更新其他出库单明细的 sourceDetailId 字段
                List<OtherOutstockDetailEntity> toUpdateDetails = new ArrayList<>();
                for (OtherOutstockDetailEntity otherOutstockDetailEntity : otherOutstockDetailEntities) {
                    String sourceDetailId = skuIdToSourceDetailIdMap.get(otherOutstockDetailEntity.getSkuId());
                    if (sourceDetailId != null) {
                        otherOutstockDetailEntity.setSourceDetailId(sourceDetailId);
                        toUpdateDetails.add(otherOutstockDetailEntity);
                    }
                }
                
                // 批量更新明细
                if (!toUpdateDetails.isEmpty()) {
                    otherOutstockDetailService.updateBatchById(toUpdateDetails);
                    log.info("成功更新其他出库单明细的 sourceDetailId 字段，共更新{}条明细", toUpdateDetails.size());
                }
                // 提交
                otherOutstockService.submit(outboundOrderId, Boolean.FALSE);
                // 审核
                BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
                baseApproveParamDTO.setIds(Collections.singletonList(outboundOrderId));
                baseApproveParamDTO.setType(ApproveTypeEnum.PASS.getStatus());
                baseApproveParamDTO.setComment("");
                otherOutstockService.approve(outboundOrderId, baseApproveParamDTO.getType(), baseApproveParamDTO.getComment());

                log.info("成功创建其他出库单，ID：{}，来源：{}，明细数量：{}",
                        outboundOrderId, firstItem.getSourceCode(), detailList.size());
                return BatchResultDTO.success(sourceId, firstItem.getSourceCode(), "下推其他出库单成功");
            } else {
                return BatchResultDTO.fail(sourceId, firstItem.getSourceCode(), "创建其他出库单失败");
            }
            
        } catch (Exception e) {
            log.error("创建其他出库单失败，sourceId: {}, error: {}", sourceId, e.getMessage(), e);
            throw e;
        }
    }
    /**
     * 根据已出库数量和领用数量计算执行状态
     * 根据表格规则：
     * - 已出库数量=0：待出库
     * - 0 < 已出库数量 < 借用数量：部分出库
     * - 待出库数量=0：已出库
     *
     * @param deliveryQty 已出库数量
     * @param recipientQty 领用数量
     * @return 执行状态
     */
    private String calculateExecStatus(Integer deliveryQty, Integer recipientQty) {
        if (deliveryQty == null || deliveryQty == 0) {
            return SampleRecipientExecStatusEnum.WAIT_OUTSTOCK.getExecStatus(); // 待出库
        } else if (deliveryQty < recipientQty) {
            return SampleRecipientExecStatusEnum.PART_OUTSTOCK.getExecStatus(); // 部分出库
        } else {
            return SampleRecipientExecStatusEnum.COMPLETE_OUTSTOCK.getExecStatus(); // 已出库
        }
    }

    /**
     * 下载模板
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        // 下载样品领用单导入模板
        String path = "classpath:excel/sampleRecipientTemplate.xlsx";
        String excelName = "样品领用单导入模板.xlsx";
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
            log.info("开始下载样品领用单导入模板");
        } catch (Exception e) {
            log.error("样品领用单导入模板下载失败", e);
            throw new ServiceException("下载模板失败：" + e.getMessage());
        }
    }

    /**
     * 异步导入
     */
    @Override
    public Boolean importExcel(BaseDTO.ImportDTO dto) {
        downloadTaskFeign.saveImportTask("样品领用单导入", IMPORT_WMS_SAMPLE_RECIPIENT.getCode(), dto);
        return Boolean.TRUE;
    }

    /**
     * 导入样品领用单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importSampleRecipient(BaseDTO.ImportDTO dto) {
        SampleRecipientExcelListener excelListenerUtil = new SampleRecipientExcelListener(dto.getTaskId(), dto.getImportType(), dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), SampleRecipientExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());

        // 导出错误数据
        List<SampleRecipientExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollUtil.isNotEmpty(errorList)) {
            String fileName = "样品领用单错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, SampleRecipientExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }

        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);

        // 处理成功的数据
        List<SampleRecipientExcelDTO> successList = excelListenerUtil.getSuccessList();
        if (CollUtil.isNotEmpty(successList)) {
            // 在导入前预加载缓存，提升性能
            preloadCacheForImport(successList);

            handleImportSuccessList(successList, errorList, dto.getImportType());
        }
    }

    /**
     * 处理导入成功的数据
     */
    private void handleImportSuccessList(List<SampleRecipientExcelDTO> successList, List<SampleRecipientExcelDTO> errorList, String importType) {
        try {
            // 按主表信息分组处理
            Map<String, List<SampleRecipientExcelDTO>> groupedData = successList.stream()
                .collect(Collectors.groupingBy(data -> 
                    data.getRecipientDate() + "_" + data.getUsage() + "_" + data.getWarehouseName() + "_" + 
                    data.getUserName() + "_" + data.getDeptName() + "_" + data.getPickOrgName() + "_" + 
                    data.getUsageScope() + "_" + data.getRemark()
                ));
            
            for (Map.Entry<String, List<SampleRecipientExcelDTO>> entry : groupedData.entrySet()) {
                List<SampleRecipientExcelDTO> groupData = entry.getValue();
                if (CollUtil.isNotEmpty(groupData)) {
                    try {
                        // 创建样品领用单
                        createSampleRecipientFromExcel(groupData);
                    } catch (Exception e) {
                        log.error("创建样品领用单失败", e);
                        // 将失败的数据移到错误列表
                        for (SampleRecipientExcelDTO data : groupData) {
                            data.setErrorMsg("创建样品领用单失败：" + e.getMessage());
                            errorList.add(data);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理导入数据失败", e);
            throw new ServiceException("处理导入数据失败：" + e.getMessage());
        }
    }

    /**
     * 从Excel数据创建样品领用单
     */
    private void createSampleRecipientFromExcel(List<SampleRecipientExcelDTO> groupData) {
        if (CollUtil.isEmpty(groupData)) {
            return;
        }
        
        SampleRecipientExcelDTO firstData = groupData.get(0);
        
        // 查询仓库ID
        String warehouseId = getWarehouseIdByName(firstData.getWarehouseName());
        if (StrUtil.isBlank(warehouseId)) {
            throw new ServiceException("仓库【" + firstData.getWarehouseName() + "】不存在");
        }
        
        // 查询用户ID
        String userId = getUserIdByName(firstData.getUserName());
        if (StrUtil.isBlank(userId)) {
            throw new ServiceException("用户【" + firstData.getUserName() + "】不存在");
        }
        
        // 查询部门ID
        String deptId = getDeptIdByName(firstData.getDeptName());
        if (StrUtil.isBlank(deptId)) {
            throw new ServiceException("部门【" + firstData.getDeptName() + "】不存在");
        }
        
        // 查询组织ID
        String pickOrgId = getOrgIdByName(firstData.getPickOrgName());
        if (StrUtil.isBlank(pickOrgId)) {
            throw new ServiceException("组织【" + firstData.getPickOrgName() + "】不存在");
        }
        
        // 创建样品领用单主表
        SampleRecipientEntity sampleRecipientEntity = new SampleRecipientEntity();
        sampleRecipientEntity.setRecipientDate(firstData.getRecipientDate());
        sampleRecipientEntity.setUsage(firstData.getUsage());
        sampleRecipientEntity.setWarehouseId(warehouseId);
        sampleRecipientEntity.setUserId(userId);
        sampleRecipientEntity.setDeptId(deptId);
        sampleRecipientEntity.setPickOrgId(pickOrgId);
        sampleRecipientEntity.setUsageScope(firstData.getUsageScope());
        sampleRecipientEntity.setRemark(firstData.getRemark());
        sampleRecipientEntity.setIsDelivery(false); // 默认不邮寄
        
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YPLY);
        sampleRecipientEntity.setCode(code);
        sampleRecipientEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
        
        // 保存主表
        boolean save = super.save(sampleRecipientEntity);
        if (!save) {
            throw new ServiceException("样品领用单保存失败");
        }
        
        // 创建明细数据
        List<SampleRecipientDetailEntity> detailEntities = new ArrayList<>();
        for (SampleRecipientExcelDTO data : groupData) {
            // 查询SKU信息
            String skuId = getSkuIdBySkuNo(data.getSkuNo());
            if (StrUtil.isBlank(skuId)) {
                throw new ServiceException("SKU【" + data.getSkuNo() + "】不存在");
            }
            
            SampleRecipientDetailEntity detailEntity = new SampleRecipientDetailEntity();
            detailEntity.setMainId(sampleRecipientEntity.getId());
            detailEntity.setSkuNo(data.getSkuNo());
            detailEntity.setSkuId(skuId);
            detailEntity.setProductName(getProductNameBySkuId(skuId));
            detailEntity.setRecipientQty(data.getRecipientQty());
            detailEntity.setDeliveryQty(0);
            detailEntity.setExecStatus(SampleRecipientExecStatusEnum.WAIT_OUTSTOCK.getExecStatus());
            detailEntity.setRemark(data.getDetailRemark());
            detailEntities.add(detailEntity);
        }
        
        // 保存明细数据
        if (CollUtil.isNotEmpty(detailEntities)) {
            boolean detailSaveResult = sampleRecipientDetailService.saveBatch(detailEntities);
            if (!detailSaveResult) {
                throw new ServiceException("样品领用单明细保存失败");
            }
        }
        
        log.info("成功创建样品领用单，单号：{}，明细数量：{}", code, detailEntities.size());
    }

    /**
     * 根据仓库名称查询仓库ID（带缓存）
     */
    private String getWarehouseIdByName(String warehouseName) {
        if (StrUtil.isBlank(warehouseName)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_WAREHOUSE_NAME_TO_ID + warehouseName;
        String warehouseId = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(warehouseId)) {
            return warehouseId;
        }

        try {
            // 调用仓库服务根据名称查询仓库信息
            List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listWarehouseByParams(
                    WarehouseDTO.ListParamDTO.builder()
                            .warehouseName(warehouseName)
                            .showByAuth(false)
                            .build()
            );

            if (CollUtil.isNotEmpty(warehouseList)) {
                // 返回第一个匹配的仓库ID
                String result = warehouseList.get(0).getId();
                // 缓存结果
                redissonClient.getBucket(cacheKey).set(result, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                return result;
            }

            log.warn("未找到仓库名称：{}", warehouseName);
            return null;
        } catch (Exception e) {
            log.error("查询仓库ID失败，仓库名称：{}，错误：{}", warehouseName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据用户名称查询用户ID（带缓存）
     */
    private String getUserIdByName(String userName) {
        if (StrUtil.isBlank(userName)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_USER_NAME_TO_ID + userName;
        String userId = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(userId)) {
            return userId;
        }

        try {
            // 调用用户服务根据名称查询用户信息
            List<FindUserDTO> userList = sysUserFeign.listUserByUserNames(
                    Collections.singletonList(userName),
                    "1" // 用户类型：1表示内部用户
            );

            if (CollUtil.isNotEmpty(userList)) {
                // 返回第一个匹配的用户ID
                String result = userList.get(0).getUserId();
                // 缓存结果
                redissonClient.getBucket(cacheKey).set(result, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                return result;
            }

            log.warn("未找到用户名称：{}", userName);
            return null;
        } catch (Exception e) {
            log.error("查询用户ID失败，用户名称：{}，错误：{}", userName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据部门名称查询部门ID（带缓存）
     */
    private String getDeptIdByName(String deptName) {
        if (StrUtil.isBlank(deptName)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_DEPT_NAME_TO_ID + deptName;
        String deptId = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(deptId)) {
            return deptId;
        }

        try {
            // 调用部门服务根据名称查询部门信息
            List<String> deptIds = sysUserFeign.getDeptIdsByName(deptName);

            if (CollUtil.isNotEmpty(deptIds)) {
                // 返回第一个匹配的部门ID
                String result = deptIds.get(0);
                // 缓存结果
                redissonClient.getBucket(cacheKey).set(result, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                return result;
            }

            log.warn("未找到部门名称：{}", deptName);
            return null;
        } catch (Exception e) {
            log.error("查询部门ID失败，部门名称：{}，错误：{}", deptName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据组织名称查询组织ID（带缓存）
     */
    private String getOrgIdByName(String orgName) {
        if (StrUtil.isBlank(orgName)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_ORG_NAME_TO_ID + orgName;
        String orgId = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(orgId)) {
            return orgId;
        }

        try {
            // 调用组织服务根据名称查询组织信息
            SysAccountingCompanyEntity companyId = sysUserFeign.getCompanyByName(orgName);

            if (!Objects.isNull(companyId)&&StrUtil.isNotBlank(companyId.getId())) {
                // 缓存结果
                redissonClient.getBucket(cacheKey).set(companyId.getId(), CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                return companyId.getId();
            }

            log.warn("未找到组织名称：{}", orgName);
            return null;
        } catch (Exception e) {
            log.error("查询组织ID失败，组织名称：{}，错误：{}", orgName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据SKU编号查询SKU ID（带缓存）
     */
    private String getSkuIdBySkuNo(String skuNo) {
        if (StrUtil.isBlank(skuNo)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_SKU_NO_TO_ID + skuNo;
        String skuId = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(skuId)) {
            return skuId;
        }

        try {
            // 调用PLM系统根据SKU编号查询SKU信息
            List<ProductDetailEntity> productDetailEntities = plmTaskFeign.listBySkuNos(
                    Collections.singletonList(skuNo)
            );

            if (CollUtil.isNotEmpty(productDetailEntities)) {
                // 返回第一个匹配的SKU ID
                String result = productDetailEntities.get(0).getName();
                // 缓存结果
                redissonClient.getBucket(cacheKey).set(result, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                return result;
            }

            log.warn("未找到SKU编号：{}", skuNo);
            return null;
        } catch (Exception e) {
            log.error("查询SKU ID失败，SKU编号：{}，错误：{}", skuNo, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 根据SKU ID查询产品名称（带缓存）
     */
    private String getProductNameBySkuId(String skuId) {
        if (StrUtil.isBlank(skuId)) {
            return null;
        }

        // 先从缓存获取
        String cacheKey = CACHE_SKU_ID_TO_PRODUCT_NAME + skuId;
        String productName = (String) redissonClient.getBucket(cacheKey).get();
        if (StrUtil.isNotBlank(productName)) {
            return productName;
        }

        try {
            // 调用PLM系统根据SKU ID查询SKU信息
            List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(
                    Collections.singletonList(skuId)
            );

            if (CollUtil.isNotEmpty(skuList)) {
                // 返回第一个匹配的产品名称
                String result = skuList.get(0).getProductId();
                if (StrUtil.isNotBlank(result)) {
                    // 缓存结果
                    redissonClient.getBucket(cacheKey).set(result, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
                    return result;
                } else {
                    return "未知产品";
                }
            }

            log.warn("未找到SKU ID：{}", skuId);
            return "未知产品";
        } catch (Exception e) {
            log.error("查询产品名称失败，SKU ID：{}，错误：{}", skuId, e.getMessage(), e);
            return "未知产品";
        }
    }

    /**
     * 批量预加载缓存（导入前调用，提升导入性能）
     */
    public void preloadCacheForImport(List<SampleRecipientExcelDTO> excelDataList) {
        if (CollUtil.isEmpty(excelDataList)) {
            return;
        }

        try {
            log.info("开始预加载缓存，数据量：{}", excelDataList.size());

            // 收集所有需要查询的唯一值
            Set<String> warehouseNames = new HashSet<>();
            Set<String> userNames = new HashSet<>();
            Set<String> deptNames = new HashSet<>();
            Set<String> orgNames = new HashSet<>();
            Set<String> skuNos = new HashSet<>();

            for (SampleRecipientExcelDTO data : excelDataList) {
                if (StrUtil.isNotBlank(data.getWarehouseName())) {
                    warehouseNames.add(data.getWarehouseName());
                }
                if (StrUtil.isNotBlank(data.getUserName())) {
                    userNames.add(data.getUserName());
                }
                if (StrUtil.isNotBlank(data.getDeptName())) {
                    deptNames.add(data.getDeptName());
                }
                if (StrUtil.isNotBlank(data.getPickOrgName())) {
                    orgNames.add(data.getPickOrgName());
                }
                if (StrUtil.isNotBlank(data.getSkuNo())) {
                    skuNos.add(data.getSkuNo());
                }
            }

            // 批量预加载仓库缓存
            if (CollUtil.isNotEmpty(warehouseNames)) {
                preloadWarehouseCache(warehouseNames);
            }

            // 批量预加载用户缓存
            if (CollUtil.isNotEmpty(userNames)) {
                preloadUserCache(userNames);
            }

            // 批量预加载部门缓存
            if (CollUtil.isNotEmpty(deptNames)) {
                preloadDeptCache(deptNames);
            }

            // 批量预加载组织缓存
            if (CollUtil.isNotEmpty(orgNames)) {
                preloadOrgCache(orgNames);
            }

            // 批量预加载SKU缓存
            if (CollUtil.isNotEmpty(skuNos)) {
                preloadSkuCache(skuNos);
            }

            log.info("缓存预加载完成");

        } catch (Exception e) {
            log.error("预加载缓存失败", e);
            // 预加载失败不影响正常导入流程
        }
    }

    /**
     * 批量预加载仓库缓存
     */
    private void preloadWarehouseCache(Set<String> warehouseNames) {
        try {
            for (String warehouseName : warehouseNames) {
                // 检查缓存中是否已存在
                String cacheKey = CACHE_WAREHOUSE_NAME_TO_ID + warehouseName;
                if (redissonClient.getBucket(cacheKey).isExists()) {
                    continue;
                }

                // 查询并缓存
                getWarehouseIdByName(warehouseName);
            }
        } catch (Exception e) {
            log.warn("预加载仓库缓存失败", e);
        }
    }

    /**
     * 批量预加载用户缓存
     */
    private void preloadUserCache(Set<String> userNames) {
        try {
            for (String userName : userNames) {
                // 检查缓存中是否已存在
                String cacheKey = CACHE_USER_NAME_TO_ID + userName;
                if (redissonClient.getBucket(cacheKey).isExists()) {
                    continue;
                }

                // 查询并缓存
                getUserIdByName(userName);
            }
        } catch (Exception e) {
            log.warn("预加载用户缓存失败", e);
        }
    }

    /**
     * 批量预加载部门缓存
     */
    private void preloadDeptCache(Set<String> deptNames) {
        try {
            for (String deptName : deptNames) {
                // 检查缓存中是否已存在
                String cacheKey = CACHE_DEPT_NAME_TO_ID + deptName;
                if (redissonClient.getBucket(cacheKey).isExists()) {
                    continue;
                }

                // 查询并缓存
                getDeptIdByName(deptName);
            }
        } catch (Exception e) {
            log.warn("预加载部门缓存失败", e);
        }
    }

    /**
     * 批量预加载组织缓存
     */
    private void preloadOrgCache(Set<String> orgNames) {
        try {
            for (String orgName : orgNames) {
                // 检查缓存中是否已存在
                String cacheKey = CACHE_ORG_NAME_TO_ID + orgName;
                if (redissonClient.getBucket(cacheKey).isExists()) {
                    continue;
                }

                // 查询并缓存
                getOrgIdByName(orgName);
            }
        } catch (Exception e) {
            log.warn("预加载组织缓存失败", e);
        }
    }

    /**
     * 批量预加载SKU缓存
     */
    private void preloadSkuCache(Set<String> skuNos) {
        try {
            for (String skuNo : skuNos) {
                // 检查缓存中是否已存在
                String cacheKey = CACHE_SKU_NO_TO_ID + skuNo;
                if (redissonClient.getBucket(cacheKey).isExists()) {
                    continue;
                }

                // 查询并缓存
                getSkuIdBySkuNo(skuNo);
            }
        } catch (Exception e) {
            log.warn("预加载SKU缓存失败", e);
        }
    }

    /**
     * 清除所有相关缓存
     */
    public void clearAllCache() {
//        try {
//            // 清除仓库缓存
//            Set<String> warehouseKeys = redissonClient.getKeys().getKeysByPattern(CACHE_WAREHOUSE_NAME_TO_ID + "*");
//            if (CollUtil.isNotEmpty(warehouseKeys)) {
//                redissonClient.getKeys().delete(warehouseKeys);
//            }
//
//            // 清除用户缓存
//            Set<String> userKeys = redissonClient.getKeys().getKeysByPattern(CACHE_USER_NAME_TO_ID + "*");
//            if (CollUtil.isNotEmpty(userKeys)) {
//                redissonClient.getKeys().delete(userKeys);
//            }
//
//            // 清除部门缓存
//            Set<String> deptKeys = redissonClient.getKeys().getKeysByPattern(CACHE_DEPT_NAME_TO_ID + "*");
//            if (CollUtil.isNotEmpty(deptKeys)) {
//                redissonClient.getKeys().delete(deptKeys);
//            }
//
//            // 清除组织缓存
//            Set<String> orgKeys = redissonClient.getKeys().getKeysByPattern(CACHE_ORG_NAME_TO_ID + "*");
//            if (CollUtil.isNotEmpty(orgKeys)) {
//                redissonClient.getKeys().delete(orgKeys);
//            }
//
//            // 清除SKU缓存
//            Set<String> skuKeys = redissonClient.getKeys().getKeysByPattern(CACHE_SKU_NO_TO_ID + "*");
//            if (CollUtil.isNotEmpty(skuKeys)) {
//                redissonClient.getKeys().delete(skuKeys);
//            }
//
//            // 清除产品名称缓存
//            Set<String> productKeys = redissonClient.getKeys().getKeysByPattern(CACHE_SKU_ID_TO_PRODUCT_NAME + "*");
//            if (CollUtil.isNotEmpty(productKeys)) {
//                redissonClient.getKeys().delete(productKeys);
//            }
//
//            log.info("所有缓存清除完成");
//        } catch (Exception e) {
//            log.error("清除缓存失败", e);
//        }
    }

    /**
     * 增加已出库数量（其他出库单审核通过时调用）
     * 
     * @param detailId 样品领用单明细ID
     * @param qty 出库数量
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean increaseDeliveryQty(String detailId, Integer qty) {
        try {
            log.info("开始增加样品领用单明细已出库数量，明细ID：{}，数量：{}", detailId, qty);
            
            // 查询样品领用单明细信息
            SampleRecipientDetailEntity detail = sampleRecipientDetailService.getById(detailId);
            if (detail == null) {
                log.warn("样品领用单明细不存在，明细ID：{}", detailId);
                return false;
            }
            
            // 计算新的已出库数量
            Integer newDeliveryQty = detail.getDeliveryQty() + qty;
            
            // 更新已出库数量
            detail.setDeliveryQty(newDeliveryQty);
            
            // 根据已出库数量和领用数量的关系，更新执行状态
            String newExecStatus = calculateExecStatus(newDeliveryQty, detail.getRecipientQty());
            detail.setExecStatus(newExecStatus);
            
            // 更新样品领用单明细
            boolean updated = sampleRecipientDetailService.updateById(detail);
            
            if (updated) {
                log.info("成功更新样品领用单明细已出库数量和执行状态，明细ID：{}，增加数量：{}，新执行状态：{}", 
                    detailId, qty, newExecStatus);
                return true;
            } else {
                log.warn("更新样品领用单明细已出库数量失败，明细ID：{}", detailId);
                return false;
            }
        } catch (Exception e) {
            log.error("增加样品领用单明细已出库数量失败，明细ID：{}，错误：{}", detailId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 减少已出库数量（其他出库单反审核时调用）
     * 
     * @param detailId 样品领用单明细ID
     * @param qty 出库数量
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean decreaseDeliveryQty(String detailId, Integer qty) {
        try {
            log.info("开始减少样品领用单明细已出库数量，明细ID：{}，数量：{}", detailId, qty);
            
            // 查询样品领用单明细信息
            SampleRecipientDetailEntity detail = sampleRecipientDetailService.getById(detailId);
            if (detail == null) {
                log.warn("样品领用单明细不存在，明细ID：{}", detailId);
                return false;
            }
            
            // 计算新的已出库数量（不能小于0）
            Integer newDeliveryQty = Math.max(detail.getDeliveryQty() - qty, 0);
            
            // 更新已出库数量
            detail.setDeliveryQty(newDeliveryQty);
            
            // 根据已出库数量和领用数量的关系，更新执行状态
            String newExecStatus = calculateExecStatus(newDeliveryQty, detail.getRecipientQty());
            detail.setExecStatus(newExecStatus);
            
            // 更新样品领用单明细
            boolean updated = sampleRecipientDetailService.updateById(detail);
            
            if (updated) {
                log.info("成功减少样品领用单明细已出库数量和执行状态，明细ID：{}，减少数量：{}，新执行状态：{}", 
                    detailId, qty, newExecStatus);
                return true;
            } else {
                log.warn("减少样品领用单明细已出库数量失败，明细ID：{}", detailId);
                return false;
            }
        } catch (Exception e) {
            log.error("减少样品领用单明细已出库数量失败，明细ID：{}，错误：{}", detailId, e.getMessage(), e);
            return false;
        }
    }


}

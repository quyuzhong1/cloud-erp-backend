package com.erp.server.fms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.validator.ValidList;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.fms.dto.*;
import com.erp.model.fms.dto.excel.AssetAcceptExcelDTO;
import com.erp.model.fms.entity.AssetAcceptDetailEntity;
import com.erp.model.fms.entity.AssetAcceptEntity;
import com.erp.model.fms.entity.AssetAcceptPersonEntity;
import com.erp.model.fms.entity.AttachmentEntity;
import com.erp.model.fms.enums.*;
import com.erp.model.fms.enums.UnitEnum;
import com.erp.model.scm.dto.AssetPurchaseOrderDTO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.AssetPurchaseOrderFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.fms.listener.AssetAcceptExcelListener;
import com.erp.server.fms.mapper.AssetAcceptMapper;
import com.erp.server.fms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.common.core.controller.vo.ApiResult.success;

/**
 * <p>
 * 资产验收表 服务实现类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
@Slf4j
@Service
public class AssetAcceptServiceImpl extends SuperServiceImpl<AssetAcceptMapper, AssetAcceptEntity> implements AssetAcceptService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private WorkflowFeign workflowFeign;
    @Autowired
    private AssetAcceptPersonService assetAcceptPersonService;
    @Autowired
    private AssetAcceptDetailService assetAcceptDetailService;
    @Autowired
    private AttachmentService attachmentService;
    @Autowired
    private AssetCardService assetCardService;
    @Autowired
    private DownloadTaskFeign downloadTaskFeign;
    @Autowired
    private AssetPurchaseOrderFeign assetPurchaseOrderFeign;
    @Autowired
    private PlmTaskFeign plmTaskFeign;
    @Autowired
    private FileFeign fileFeign;
    @Autowired
    private SysUserFeign sysUserFeign;
    @Autowired
    private AssetLocationService assetLocationService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(AssetAcceptDTO.AddDTO addDTO) {
        // 验证明细不能为空
        if (CollUtil.isEmpty(addDTO.getDetailList())) {
            throw new ServiceException("资产验收明细不能为空，至少需要一条明细数据");
        }
        
        AssetAcceptEntity assetAcceptEntity = new AssetAcceptEntity();
        BeanMapperUtils.copy(addDTO, assetAcceptEntity);
        assetAcceptEntity.setSourceCode(addDTO.getPurchaseCode());

        // 如果填写了模具采购订单号，查询订单信息并设置来源（只在新增时且有订单号时才查询）
        if (StringUtils.isNotBlank(addDTO.getPurchaseCode())) {
            try {
                ApiResult<AssetPurchaseOrderDTO.DetailWithSkuDTO> orderResult =
                    assetPurchaseOrderFeign.getByCode(addDTO.getPurchaseCode());
                
                if (orderResult != null && orderResult.isSuccess() && orderResult.getData() != null) {
                    AssetPurchaseOrderDTO.DetailWithSkuDTO orderData = orderResult.getData();
                    
                    // 设置来源信息
                    assetAcceptEntity.setSourceId(orderData.getId());
                    assetAcceptEntity.setSourceCode(orderData.getCode());
                    assetAcceptEntity.setSourceType(SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode());
                    
                    // 设置供应商信息（如果DTO中没有提供，则从订单中获取）
                    if (StringUtils.isBlank(addDTO.getSupplierId()) && StringUtils.isNotBlank(orderData.getSupplierId())) {
                        assetAcceptEntity.setSupplierId(orderData.getSupplierId());
                        assetAcceptEntity.setSupplierName(orderData.getSupplierName());
                    }
                } else {
                    log.warn("根据模具采购订单号{}查询订单失败: {}", 
                        addDTO.getPurchaseCode(), 
                        orderResult != null ? orderResult.getMsg() : "返回结果为空");
                }
            } catch (Exception e) {
                log.error("查询模具采购订单{}失败", addDTO.getPurchaseCode(), e);
            }
        }

        // 数据处理
        handleData(assetAcceptEntity);

        log.info("开始新增资产验收单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YSD);
        assetAcceptEntity.setCode(code);
        boolean save = super.save(assetAcceptEntity);
        if(!save) {
            throw new ServiceException("资产验收单保存失败");
        }

        // 保存验收人员数据
        if (CollUtil.isNotEmpty(addDTO.getPersonList())) {
            List<AssetAcceptPersonEntity> personEntities = new ArrayList<>();
            for (AssetAcceptPersonDTO.AddDTO personDTO : addDTO.getPersonList()) {
                AssetAcceptPersonEntity personEntity = new AssetAcceptPersonEntity();
                personEntity.setAssetAcceptId(assetAcceptEntity.getId());
                personEntity.setPersonType(personDTO.getPersonType());
                personEntity.setUserId(personDTO.getUserId());
                personEntity.setUserName(personDTO.getUserName());
                personEntities.add(personEntity);
            }
            
            if (CollUtil.isNotEmpty(personEntities)) {
                boolean personSaveResult = assetAcceptPersonService.saveBatch(personEntities);
                if (!personSaveResult) {
                    throw new ServiceException("资产验收人员保存失败");
                }
                log.info("资产验收人员保存成功，共保存{}条人员", personEntities.size());
            }
        }

        // 保存验收明细数据
        if (CollUtil.isNotEmpty(addDTO.getDetailList())) {
            // 收集所有skuId用于批量查询
            List<String> skuIds = addDTO.getDetailList().stream()
                    .map(AssetAcceptDetailDTO.AddDTO::getSkuId)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 批量查询SKU信息，建立skuId -> skuNo的映射
            Map<String, String> skuIdToNoMap = new HashMap<>();
            if (CollUtil.isNotEmpty(skuIds)) {
                try {
                    List<com.erp.model.plm.entity.ProductDetailEntity> skuInfoList = plmTaskFeign.getByIdList(skuIds);
                    if (CollUtil.isNotEmpty(skuInfoList)) {
                        skuIdToNoMap = skuInfoList.stream()
                                .filter(sku -> StringUtils.isNotBlank(sku.getId()) && StringUtils.isNotBlank(sku.getSkuNo()))
                                .collect(Collectors.toMap(
                                        com.erp.model.plm.entity.ProductDetailEntity::getId,
                                        com.erp.model.plm.entity.ProductDetailEntity::getSkuNo,
                                        (old, newVal) -> newVal
                                ));
                    }
                } catch (Exception e) {
                    log.error("批量查询SKU信息失败", e);
                }
            }
            
            List<AssetAcceptDetailEntity> detailEntities = new ArrayList<>();
            for (AssetAcceptDetailDTO.AddDTO detailDTO : addDTO.getDetailList()) {
                AssetAcceptDetailEntity detailEntity = new AssetAcceptDetailEntity();
                detailEntity.setMainId(assetAcceptEntity.getId());
                detailEntity.setSourceDetailId(detailDTO.getSourceDetailId());
                detailEntity.setSkuId(detailDTO.getSkuId());
                // 根据skuId设置skuNo
                if (StringUtils.isNotBlank(detailDTO.getSkuId())) {
                    detailEntity.setSkuNo(skuIdToNoMap.get(detailDTO.getSkuId()));
                }
                detailEntity.setProductName(detailDTO.getProductName());
                detailEntity.setAcceptQty(detailDTO.getAcceptQty());
                // 设置资产卡片关联状态，如果为空则默认为"未生成"
                String assetCardStatus = StringUtils.isNotBlank(detailDTO.getAssetCardStatus()) 
                    ? detailDTO.getAssetCardStatus() 
                    : AssetCardStatusEnum.NOT_GENERATED.getStatus();
                detailEntity.setAssetCardStatus(assetCardStatus);
                // DTO使用新字段名，Entity使用旧字段名进行映射
                detailEntity.setPendingQty(detailDTO.getPendingAcceptQty());
                detailEntity.setAcceptedQty(detailDTO.getAcceptedQty());
                detailEntity.setAcceptableQty(detailDTO.getAvailableAcceptQty());
                detailEntity.setAssetLocationId(detailDTO.getAssetLocationId());
                detailEntity.setUseDeptName(detailDTO.getUseDeptName());
                detailEntity.setUseDeptId(detailDTO.getUseDeptId());
                detailEntity.setCostType(detailDTO.getCostType());
                detailEntity.setRemark(detailDTO.getRemark());
                detailEntities.add(detailEntity);
            }
            
            if (CollUtil.isNotEmpty(detailEntities)) {
                boolean detailSaveResult = assetAcceptDetailService.saveBatch(detailEntities);
                if (!detailSaveResult) {
                    throw new ServiceException("资产验收明细保存失败");
                }
                log.info("资产验收明细保存成功，共保存{}条明细", detailEntities.size());
            }
        }

        // 保存附件
        addAttachment(addDTO, assetAcceptEntity);

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "资产验收单" , assetAcceptEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), assetAcceptEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(assetAcceptEntity.getId(), code);
    }

    /**
     * 添加附件信息
     * @param addDTO 包含附件URL和名称列表的数据传输对象
     * @param assetAcceptEntity 资产验收实体
     */
    private void addAttachment(AssetAcceptDTO.AddDTO addDTO, AssetAcceptEntity assetAcceptEntity) {
        //附件集合
        List<String> attachmentUrlList = addDTO.getAttachmentUrlList();
        //附件名
        List<String> attachmentNameList = addDTO.getAttachmentNameList();
        List<AttachmentEntity> batchAttachmentList = new ArrayList<>(10);
        if (CollUtil.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()) {
            Class<AssetAcceptEntity> entityClass = AssetAcceptEntity.class;
            TableName tableName = entityClass.getDeclaredAnnotation(TableName.class);
            //获取到表名
            String type = tableName.value();
            for (int i = 0; i < attachmentUrlList.size(); i++) {
                AttachmentEntity attachment = new AttachmentEntity();
                attachment.setAttachUrl(attachmentUrlList.get(i));
                attachment.setAttachName(attachmentNameList.get(i));
                attachment.setBusinessId(assetAcceptEntity.getId());
                attachment.setType(type);
                batchAttachmentList.add(attachment);
            }
            if (CollUtil.isNotEmpty(batchAttachmentList)) {
                attachmentService.saveBatch(batchAttachmentList);
                log.info("资产验收单附件保存成功，共保存{}个附件", batchAttachmentList.size());
            }
        }
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(AssetAcceptDTO.UpdateDTO addOrUpdateDTO) {
        // 验证明细不能为空
        if (CollUtil.isEmpty(addOrUpdateDTO.getDetailList())) {
            throw new ServiceException("资产验收明细不能为空，至少需要一条明细数据");
        }
        
        AssetAcceptEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "资产验收单"));
        // 待提交和审核不通过允许修改
        if (!ApproveStatusEnum.allowUpdateStatus(old.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_1029);
        }
        AssetAcceptEntity assetAcceptEntity =  BeanMapperUtils.map(AssetAcceptEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(assetAcceptEntity);
        log.info("编辑 开始修改资产验收单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(assetAcceptEntity);
        if(!save) {
            throw new ServiceException("资产验收单保存失败");
        }
        // 修改验收人员数据（增量更新）
        if (CollUtil.isNotEmpty(addOrUpdateDTO.getPersonList())) {
            // 查询已存在的人员数据
            List<AssetAcceptPersonEntity> existingPersons = assetAcceptPersonService.lambdaQuery()
                .eq(AssetAcceptPersonEntity::getAssetAcceptId, addOrUpdateDTO.getId())
                .list();

            // 构建已存在人员的Map，key为personType，value为人员实体
            Map<String, AssetAcceptPersonEntity> existingPersonMap = existingPersons.stream()
                .collect(Collectors.toMap(AssetAcceptPersonEntity::getPersonType, item -> item));

            // 处理人员数据：新增、更新、删除
            List<AssetAcceptPersonEntity> toSavePersons = new ArrayList<>();
            List<String> toDeletePersons = new ArrayList<>();
            Set<String> processedPersonTypes = new HashSet<>();

            for (AssetAcceptPersonDTO.AddDTO personDTO : addOrUpdateDTO.getPersonList()) {
                String personType = personDTO.getPersonType();
                processedPersonTypes.add(personType);

                AssetAcceptPersonEntity existingPerson = existingPersonMap.get(personType);

                if (existingPerson != null) {
                    // 更新已存在的人员
                    existingPerson.setUserId(personDTO.getUserId());
                    existingPerson.setUserName(personDTO.getUserName());
                    toSavePersons.add(existingPerson);
                } else {
                    // 新增人员
                    AssetAcceptPersonEntity newPerson = new AssetAcceptPersonEntity();
                    newPerson.setAssetAcceptId(addOrUpdateDTO.getId());
                    newPerson.setPersonType(personType);
                    newPerson.setUserId(personDTO.getUserId());
                    newPerson.setUserName(personDTO.getUserName());
                    toSavePersons.add(newPerson);
                }
            }

            // 找出需要删除的人员（在新列表中不存在的）
            for (AssetAcceptPersonEntity existingPerson : existingPersons) {
                if (!processedPersonTypes.contains(existingPerson.getPersonType())) {
                    toDeletePersons.add(existingPerson.getId());
                }
            }

            // 执行删除操作
            if (!toDeletePersons.isEmpty()) {
                // 查询要删除的人员信息用于日志记录
                List<AssetAcceptPersonEntity> deletePersons = existingPersons.stream()
                    .filter(person -> toDeletePersons.contains(person.getId()))
                    .collect(Collectors.toList());

                boolean deleteResult = assetAcceptPersonService.lambdaUpdate()
                    .in(AssetAcceptPersonEntity::getId, toDeletePersons)
                    .remove();

                if (!deleteResult) {
                    log.warn("删除资产验收人员数据失败，ids：{}", toDeletePersons);
                } else {
                    // 添加删除日志
                    List<Pair<String, String>> deletePairList = deletePersons.stream()
                        .map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getPersonType()))
                        .collect(Collectors.toList());
                    operateLogService.batchAddModuleOperateLog("删除验收人员【%s】", ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), deletePairList, "编辑操作");
                    log.info("删除资产验收人员数据成功，共删除{}条人员", toDeletePersons.size());
                }
            }

            // 执行保存/更新操作
            if (!toSavePersons.isEmpty()) {
                // 分离新增和更新的人员
                List<AssetAcceptPersonEntity> addPersonList = toSavePersons.stream()
                    .filter(e -> StringUtils.isBlank(e.getId()))
                    .collect(Collectors.toList());
                List<AssetAcceptPersonEntity> updatePersonList = toSavePersons.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getId()))
                    .collect(Collectors.toList());

                boolean saveResult = assetAcceptPersonService.saveOrUpdateBatch(toSavePersons);
                if (!saveResult) {
                    throw new ServiceException("资产验收人员保存失败");
                }

                // 添加新增日志
                if (CollUtil.isNotEmpty(addPersonList)) {
                    List<Pair<String, String>> addPairList = addPersonList.stream()
                        .map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getPersonType()))
                        .collect(Collectors.toList());
                    operateLogService.batchAddModuleOperateLog("添加验收人员【%s】", ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), addPairList, "编辑操作");
                }

                // 添加更新日志
                if (CollUtil.isNotEmpty(updatePersonList)) {
                    for (AssetAcceptPersonEntity updatePerson : updatePersonList) {
                        AssetAcceptPersonEntity oldPerson = existingPersons.stream()
                            .filter(e -> Objects.equals(e.getId(), updatePerson.getId()))
                            .findFirst()
                            .orElse(null);
                        if (Objects.nonNull(oldPerson)) {
                            operateLogService.addModuleOperateLogByObj(oldPerson, updatePerson, ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), addOrUpdateDTO.getId(), String.format("编辑验收人员【%s】", oldPerson.getPersonType()));
                        }
                    }
                }

                log.info("资产验收人员保存成功，共保存{}条人员", toSavePersons.size());
            }
        }

        // 修改验收明细数据（增量更新）
        if (CollUtil.isNotEmpty(addOrUpdateDTO.getDetailList())) {
            // 查询已存在的明细数据
            List<AssetAcceptDetailEntity> existingDetails = assetAcceptDetailService.lambdaQuery()
                .eq(AssetAcceptDetailEntity::getMainId, addOrUpdateDTO.getId())
                .list();

            // 构建已存在明细的Map，key为sourceDetailId，value为明细实体
            Map<String, AssetAcceptDetailEntity> existingDetailMap = existingDetails.stream()
                .collect(Collectors.toMap(AssetAcceptDetailEntity::getSourceDetailId, item -> item));

            // 处理明细数据：新增、更新、删除
            List<AssetAcceptDetailEntity> toSaveDetails = new ArrayList<>();
            List<String> toDeleteDetails = new ArrayList<>();
            Set<String> processedSourceDetailIds = new HashSet<>();

            for (AssetAcceptDetailDTO.AddDTO detailDTO : addOrUpdateDTO.getDetailList()) {
                String sourceDetailId = detailDTO.getSourceDetailId();
                processedSourceDetailIds.add(sourceDetailId);

                AssetAcceptDetailEntity existingDetail = existingDetailMap.get(sourceDetailId);

                if (existingDetail != null) {
                    // 更新已存在的明细
                    existingDetail.setSkuId(detailDTO.getSkuId());
                    existingDetail.setProductName(detailDTO.getProductName());
                    existingDetail.setAcceptQty(detailDTO.getAcceptQty());
                    // 设置资产卡片关联状态，如果为空则保持原值或默认为"未生成"
                    String assetCardStatus = StringUtils.isNotBlank(detailDTO.getAssetCardStatus()) 
                        ? detailDTO.getAssetCardStatus() 
                        : (StringUtils.isNotBlank(existingDetail.getAssetCardStatus()) 
                            ? existingDetail.getAssetCardStatus() 
                            : AssetCardStatusEnum.NOT_GENERATED.getStatus());
                    existingDetail.setAssetCardStatus(assetCardStatus);
                    // DTO使用新字段名，Entity使用旧字段名进行映射
                    existingDetail.setPendingQty(detailDTO.getPendingAcceptQty());
                    existingDetail.setAcceptedQty(detailDTO.getAcceptedQty());
                    existingDetail.setAcceptableQty(detailDTO.getAvailableAcceptQty());
                    existingDetail.setAssetLocationId(detailDTO.getAssetLocationId());
                    existingDetail.setUseDeptName(detailDTO.getUseDeptName());
                    existingDetail.setUseDeptId(detailDTO.getUseDeptId());
                    existingDetail.setCostType(detailDTO.getCostType());
                    existingDetail.setRemark(detailDTO.getRemark());
                    toSaveDetails.add(existingDetail);
                } else {
                    // 新增明细
                    AssetAcceptDetailEntity newDetail = new AssetAcceptDetailEntity();
                    newDetail.setMainId(addOrUpdateDTO.getId());
                    newDetail.setSourceDetailId(sourceDetailId);
                    newDetail.setSkuId(detailDTO.getSkuId());
                    newDetail.setProductName(detailDTO.getProductName());
                    newDetail.setAcceptQty(detailDTO.getAcceptQty());
                    // 设置资产卡片关联状态，如果为空则默认为"未生成"
                    String assetCardStatus = StringUtils.isNotBlank(detailDTO.getAssetCardStatus()) 
                        ? detailDTO.getAssetCardStatus() 
                        : AssetCardStatusEnum.NOT_GENERATED.getStatus();
                    newDetail.setAssetCardStatus(assetCardStatus);
                    // DTO使用新字段名，Entity使用旧字段名进行映射
                    newDetail.setPendingQty(detailDTO.getPendingAcceptQty());
                    newDetail.setAcceptedQty(detailDTO.getAcceptedQty());
                    newDetail.setAcceptableQty(detailDTO.getAvailableAcceptQty());
                    newDetail.setAssetLocationId(detailDTO.getAssetLocationId());
                    newDetail.setUseDeptName(detailDTO.getUseDeptName());
                    newDetail.setUseDeptId(detailDTO.getUseDeptId());
                    newDetail.setCostType(detailDTO.getCostType());
                    newDetail.setRemark(detailDTO.getRemark());
                    toSaveDetails.add(newDetail);
                }
            }

            // 找出需要删除的明细（在新列表中不存在的）
            for (AssetAcceptDetailEntity existingDetail : existingDetails) {
                if (!processedSourceDetailIds.contains(existingDetail.getSourceDetailId())) {
                    toDeleteDetails.add(existingDetail.getId());
                }
            }

            // 执行删除操作
            if (!toDeleteDetails.isEmpty()) {
                // 查询要删除的明细信息用于日志记录
                List<AssetAcceptDetailEntity> deleteDetails = existingDetails.stream()
                    .filter(detail -> toDeleteDetails.contains(detail.getId()))
                    .collect(Collectors.toList());

                boolean deleteResult = assetAcceptDetailService.lambdaUpdate()
                    .in(AssetAcceptDetailEntity::getId, toDeleteDetails)
                    .remove();

                if (!deleteResult) {
                    log.warn("删除资产验收明细数据失败，ids：{}", toDeleteDetails);
                } else {
                    // 添加删除日志
                    List<Pair<String, String>> deletePairList = deleteDetails.stream()
                        .map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuId()))
                        .collect(Collectors.toList());
                    operateLogService.batchAddModuleOperateLog("删除验收明细【%s】", ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), deletePairList, "编辑操作");
                    log.info("删除资产验收明细数据成功，共删除{}条明细", toDeleteDetails.size());
                }
            }

            // 执行保存/更新操作
            if (!toSaveDetails.isEmpty()) {
                // 分离新增和更新的明细
                List<AssetAcceptDetailEntity> addDetailList = toSaveDetails.stream()
                    .filter(e -> StringUtils.isBlank(e.getId()))
                    .collect(Collectors.toList());
                List<AssetAcceptDetailEntity> updateDetailList = toSaveDetails.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getId()))
                    .collect(Collectors.toList());

                boolean saveResult = assetAcceptDetailService.saveOrUpdateBatch(toSaveDetails);
                if (!saveResult) {
                    throw new ServiceException("资产验收明细保存失败");
                }

                // 添加新增日志
                if (CollUtil.isNotEmpty(addDetailList)) {
                    List<Pair<String, String>> addPairList = addDetailList.stream()
                        .map(obj -> new Pair<>(addOrUpdateDTO.getId(), obj.getSkuId()))
                        .collect(Collectors.toList());
                    operateLogService.batchAddModuleOperateLog("添加验收明细【%s】", ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), addPairList, "编辑操作");
                }

                // 添加更新日志
                if (CollUtil.isNotEmpty(updateDetailList)) {
                    for (AssetAcceptDetailEntity updateDetail : updateDetailList) {
                        AssetAcceptDetailEntity oldDetail = existingDetails.stream()
                            .filter(e -> Objects.equals(e.getId(), updateDetail.getId()))
                            .findFirst()
                            .orElse(null);
                        if (Objects.nonNull(oldDetail)) {
                            operateLogService.addModuleOperateLogByObj(oldDetail, updateDetail, ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), addOrUpdateDTO.getId(), String.format("编辑验收明细【%s】", oldDetail.getSkuId()));
                        }
                    }
                }

                log.info("资产验收明细保存成功，共保存{}条明细", toSaveDetails.size());
            }
        }

        // 更新附件
        updateAttachment(addOrUpdateDTO, old);

        // 记录主单操作日志
            log.info("编辑 开始记录资产验收单日志数据，单号：【{}】", assetAcceptEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), assetAcceptEntity.getCode(), "资产验收单");
        operateLogService.addModuleOperateLogByObj(old, assetAcceptEntity, ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), assetAcceptEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 更新附件信息
     * @param addOrUpdateDTO 包含附件URL和名称列表的更新数据传输对象
     * @param old 旧的资产验收信息实体，用于获取业务ID
     */
    private void updateAttachment(AssetAcceptDTO.UpdateDTO addOrUpdateDTO, AssetAcceptEntity old) {
        List<String> attachmentUrlList = addOrUpdateDTO.getAttachmentUrlList();
        List<String> attachmentNameList = addOrUpdateDTO.getAttachmentNameList();
        if (CollUtil.isNotEmpty(attachmentUrlList) && attachmentUrlList.size() == attachmentNameList.size()){
            List<AttachmentDTO.UpdateDTO> oldAttachmentList = attachmentService.getByBusinessIds(Arrays.asList(old.getId()));
            if(CollUtil.isNotEmpty(oldAttachmentList)){
                // 处理删除的数据
                List<AttachmentDTO.UpdateDTO> remove = oldAttachmentList.stream()
                        .filter(oldAttachment -> !attachmentUrlList.contains(oldAttachment.getAttachUrl()))
                        .collect(Collectors.toList());
                if(CollUtil.isNotEmpty(remove)){
                    attachmentService.deleteByUrlList(remove.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
                    log.info("资产验收单删除附件成功，共删除{}个附件", remove.size());
                }
            }

            //处理需要新增的数据
            List<String> oldUrlList = oldAttachmentList.stream()
                    .map(AttachmentDTO.UpdateDTO::getAttachUrl)
                    .collect(Collectors.toList());

            if (CollUtil.isNotEmpty(attachmentUrlList)) {
                Class<AssetAcceptEntity> entityClass = AssetAcceptEntity.class;
                TableName tableName = entityClass.getDeclaredAnnotation(TableName.class);
                //获取到表名
                String type = tableName.value();

                List<AttachmentEntity> batchAttachmentList = new ArrayList<>();
                for (int i = 0; i < attachmentUrlList.size(); i++) {
                    if(!oldUrlList.contains(attachmentUrlList.get(i))){
                        AttachmentEntity addAttachment = new AttachmentEntity();
                        addAttachment.setAttachUrl(attachmentUrlList.get(i));
                        addAttachment.setAttachName(attachmentNameList.get(i));
                        addAttachment.setBusinessId(old.getId());
                        addAttachment.setType(type);
                        batchAttachmentList.add(addAttachment);
                    }
                }

                if(CollUtil.isNotEmpty(batchAttachmentList)){
                    attachmentService.saveBatch(batchAttachmentList);
                    log.info("资产验收单新增附件成功，共新增{}个附件", batchAttachmentList.size());
                }
            }
        }
    }

    @Override
    public PagingVO<AssetAcceptDTO.ListDTO> paging(PagingDTO<AssetAcceptDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AssetAcceptDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<AssetAcceptDTO.TabListDTO> tabList(PermissionsDTO param) {
        AssetAcceptDTO.PagingParamDTO searchParam = new AssetAcceptDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        
        // 使用一个SQL查询获取所有状态的统计数量
        List<AssetAcceptDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        
        // 设置tabFlagName
        list.stream().forEach(e -> {
            e.setTabFlagName(ApproveStatusEnum.getTableName(e.getTabFlag()));
        }); 
        
        // 获取状态列表，确保所有状态都存在
        List<String> statusList = ApproveStatusEnum.getStatusList();
        List<String> existStatusList = list.stream().map(AssetAcceptDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        
        // 不存在的状态赋值为0
        statusList.parallelStream().forEach(status -> {
            if(!existStatusList.contains(status)) {
                AssetAcceptDTO.TabListDTO newTab = new AssetAcceptDTO.TabListDTO(status, ApproveStatusEnum.getTableName(status), 0);
                list.add(newTab);
            }
        });
        
        // 按照指定顺序排序：待提交、审核中、已审核、不通过
        List<String> orderList = Arrays.asList("waitSubmit", "approveIng", "approve", "reject");
        list.sort((a, b) -> {
            int indexA = orderList.indexOf(a.getTabFlag());
            int indexB = orderList.indexOf(b.getTabFlag());
            if (indexA == -1) indexA = Integer.MAX_VALUE;
            if (indexB == -1) indexB = Integer.MAX_VALUE;
            return Integer.compare(indexA, indexB);
        });
        
        // 计算合计数量并添加"全部"标签
        int totalCount = list.stream().mapToInt(AssetAcceptDTO.TabListDTO::getCount).sum();
        AssetAcceptDTO.TabListDTO allTab = new AssetAcceptDTO.TabListDTO("all", "全部", totalCount);
        list.add(0, allTab); // 添加到第一位
        
        return list;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO submit(String id) {
        AssetAcceptEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("未找到资产验收单数据");
        }
        validateSubmit(entity);
        // 更新单据审核状态
        log.info("提交 开始修改资产验收单状态数据，id：【{}】", id);
        this.updateApproveStatus(id, ApproveStatusEnum.APPROVE_ING.getStatus());

        // TODO 启动流程（如果需要的话）
        log.info("提交 开始启动资产验收单流程，id=：【{}】", entity.getId());
        startProcess(entity);
        // 记录操作日志
        log.info("提交 开始记录资产验收单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据提交审核 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产验收单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), entity.getId(), "提交操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.SUBMIT);
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addAndSubmit(AssetAcceptDTO.AddDTO dto) {
        // 新增
        BaseResultDTO.AddDTO result = this.add(dto);
        // 提交
        this.submit(result.getId());
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndSubmit(AssetAcceptDTO.UpdateDTO dto) {
        // 修改
        this.update(dto);
        // 提交
        this.submit(dto.getId());
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO approve(ApproveOneDTO dto) {
        ApproveTypeEnum approveType = ApproveTypeEnum.getByCode(dto.getType());
        if(Objects.equals(approveType, ApproveTypeEnum.REJECT) && StrUtils.isEmpty(dto.getComment())) {
            throw new ServiceException(ApiError.REJECT_COMMENT_NOT_EMPTY);
        }
        AssetAcceptEntity entity = getById(dto.getId());
        // 审核中的数据允许审核
        if(!Objects.equals(entity.getApproveStatus(), ApproveStatusEnum.APPROVE_ING)) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        // 调用流程审核
        approveProcess(entity, dto);
        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据审核操作  审核结果：【{}】 审核意见 ：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产验收单", approveType.getName(), dto.getComment());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), entity.getId(), "审核操作");
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(approveType);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.approveStatus(approveStatus));
    }

    /**
    * 审核流程处理
    * @param entity
    * @param dto
    */
    private void approveProcess(AssetAcceptEntity entity, ApproveOneDTO dto) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        ProcessManagementDTO.ApproveDTO approveDTO = new ProcessManagementDTO.ApproveDTO();
        approveDTO.setBusinessId(entity.getId());
        approveDTO.setBusinessKey(SourceTypeEnum.ASSET_ACCEPTANCE.getCode());
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO disApprove(String id) {
        AssetAcceptEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产验收单单数据"));
        // 反审核条件判断
        validateDisApprove(entity);

        // 更新审核信息
        updateForDisApprove(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        // 操作日志
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据反审核操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产验收单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), entity.getId(), "反审核操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DISAPPROVE);
    }

    private Boolean validateDisApprove(AssetAcceptEntity entity) {
        // 已审核支持反审核
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98014);
        }
        
        // 检查是否已经下推资产卡片，如果已经下推则不允许反审核
        List<AssetAcceptDetailEntity> detailList = assetAcceptDetailService.lambdaQuery()
            .eq(AssetAcceptDetailEntity::getMainId, entity.getId())
            .eq(AssetAcceptDetailEntity::getAssetCardStatus, AssetCardStatusEnum.GENERATED.getStatus())
            .list();
        
        if (CollUtil.isNotEmpty(detailList)) {
            throw new ServiceException("该验收单已生成资产卡片，不允许反审核");
        }
        
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        AssetAcceptEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产验收单数据"));
        // 只有待提交数据允许删除
        if (!Objects.equals(ApproveStatusEnum.WAIT_SUBMIT, entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98032);
        }
        // 删除验收人员数据
        boolean personDeleteResult = assetAcceptPersonService.lambdaUpdate()
            .eq(AssetAcceptPersonEntity::getAssetAcceptId, id)
            .remove();

        if (!personDeleteResult) {
            log.warn("删除资产验收人员数据失败，id：{}", id);
        } else {
            log.info("删除资产验收人员数据成功，id：{}", id);
        }

        // 删除验收明细数据
        boolean detailDeleteResult = assetAcceptDetailService.lambdaUpdate()
            .eq(AssetAcceptDetailEntity::getMainId, id)
            .remove();

        if (!detailDeleteResult) {
            log.warn("删除资产验收明细数据失败，id：{}", id);
        } else {
            log.info("删除资产验收明细数据成功，id：{}", id);
        }

        // 删除附件
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(Arrays.asList(id));
        if(CollUtil.isNotEmpty(attachmentList)){
            List<String> urlList = attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            attachmentService.deleteByUrlList(urlList);
            log.info("删除资产验收单附件成功，共删除{}个附件", urlList.size());
        }

        // 删除主单数据
        log.info("删除 开始删除资产验收单主单数据，id：【{}】", id);
        super.removeById(id);
        // 删除日志数据
        log.info("删除 开始删除资产验收单日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产验收单");
        operateLogService.addModuleOperateLog(msg, null, entity.getCode(), "删除资产验收单数据");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);
    }
    /**
    * 作废
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO invalid(String id, String remark) {
        AssetAcceptEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产验收单数据"));
        // 待提交或审核不通过并且未作废允许作废
        if ((!ApproveStatusEnum.WAIT_SUBMIT.equals(entity.getApproveStatus()) && !ApproveStatusEnum.REJECT.equals(entity.getApproveStatus())) || !InvalidStatusEnum.NOT_VOIDED.getStatus().equals(entity.getInvalidStatus())) {
           throw new ServiceException(ApiError.ERROR_98005);
        }
        log.info("作废 开始修改资产验收单状态数据，id：【{}】", id);
        lambdaUpdate().eq(AssetAcceptEntity::getId, id)
            .set(AssetAcceptEntity::getInvalidStatus, InvalidStatusEnum.VOIDED.getStatus())
            .set(AssetAcceptEntity::getInvalidRemark, remark)
            .update();

        log.info("作废 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据作废操作 作废原因：【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产验收单", remark);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), entity.getId(), "作废操作");
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.INVALID);
     }

    /**
    * 撤销
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancelProcess(String id) {
        AssetAcceptEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到资产验收单数据"));
        // 只有审核中的单据允许撤销
        if (!Objects.equals(entity.getApproveStatus().getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus())) {
            throw new ServiceException(ApiError.ERROR_98007);
        }
        // TODO 撤销流程
        log.info("撤销 开始撤销流程，id：【{}】",id);

        log.info("撤销 开始修改资产验收单状态，id：【{}】", id);
        updateApproveStatus(id, ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        //操作日志
        log.info("撤销 开始记录操作日志，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】单据撤销流程操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "资产验收单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ASSET_ACCEPTANCE.getCode(), entity.getId(), "取消流程操作");
        ProcessManagementDTO.RevokeDTO revokeDTO = new ProcessManagementDTO.RevokeDTO();
        revokeDTO.setBusinessId(entity.getId());
        revokeDTO.setBusinessKey(SourceTypeEnum.ASSET_ACCEPTANCE.getCode());
        revokeDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        workflowFeign.revokeProcess(revokeDTO);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.CANCEL_PROCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveEnd(ApproveOneDTO dto, AssetAcceptEntity entity) {
        if (ObjectUtil.isEmpty(entity)) {
            return Boolean.TRUE;
        }
        ApproveStatusEnum approveStatus = ApproveStatusEnum.transferApproveType(dto.getType());
        updateForApprove(entity.getId(), approveStatus.getStatus());
        // todo 明细数据处理 上下游数据处理
        rewriteAssetPurchaseOrder(entity);

        return Boolean.TRUE;
    }

    public void rewriteAssetPurchaseOrder(AssetAcceptEntity entity){

        List<AssetAcceptDetailEntity> detailList = assetAcceptDetailService.lambdaQuery()
                .eq(AssetAcceptDetailEntity::getMainId, entity.getId())
                .list();
        for (AssetAcceptDetailEntity detailEntity : detailList) {
            AssetPurchaseOrderDTO.rewritePurchaseOrderDTO rewritePurchaseOrderDTO = new AssetPurchaseOrderDTO.rewritePurchaseOrderDTO();
            //计算同一采购明细行的已验收数量
            List<AssetAcceptDetailEntity> sameSoureDetailList = assetAcceptDetailService.lambdaQuery()
                    .eq(AssetAcceptDetailEntity::getSourceDetailId, detailEntity.getSourceDetailId())
                    .list();

            int totalAcceptedQty = sameSoureDetailList.stream()
                    .mapToInt(obj -> obj.getAcceptedQty())
                    .sum();
            totalAcceptedQty += detailEntity.getAcceptQty();
            rewritePurchaseOrderDTO.setDetailId(detailEntity.getSourceDetailId());
            rewritePurchaseOrderDTO.setAcceptedQty(new BigDecimal(totalAcceptedQty));
            assetPurchaseOrderFeign.rewriteAssetPurchaseOrder(rewritePurchaseOrderDTO);
        }

    }

    @Override
    public AssetAcceptDTO.ViewDTO view(String id) {
        AssetAcceptEntity assetAcceptEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到资产验收单数据"));
        AssetAcceptDTO.ViewDTO data = new AssetAcceptDTO.ViewDTO();
        BeanUtil.copyProperties(assetAcceptEntity, data);
        data.setApproveStatus(assetAcceptEntity.getApproveStatus().getCode());
        // 数据填充处理
        fillOne(data);
        
        // 查询相关的附件信息
        List<AttachmentDTO.UpdateDTO> attachmentList = attachmentService.getByBusinessIds(Arrays.asList(id));
        if(CollUtil.isNotEmpty(attachmentList)){
            // 分别提取附件名称和URL列表设置到返回对象中
            data.setAttachmentNameList(attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachName).collect(Collectors.toList()));
            data.setAttachmentUrlList(attachmentList.stream().map(AttachmentDTO.UpdateDTO::getAttachUrl).collect(Collectors.toList()));
        }
        
        return data;
    }
    /**
    * 启动流程
    *
    * @param entity
    * @return void
    * @Date 2023/7/4 10:07
    **/

    public void startProcess(AssetAcceptEntity entity) {
        ProcessManagementDTO.StartDTO startDTO = new ProcessManagementDTO.StartDTO();
        startDTO.setBusinessId(entity.getId());
        startDTO.setBusinessCode(entity.getCode());
        startDTO.setBusinessKey(SourceTypeEnum.ASSET_ACCEPTANCE.getCode());
        startDTO.setBusinessName(entity.getCode());
        startDTO.setUserId(UserContext.getDefaultLoginUser().getUid());
        
        // 查询人员列表并按personType分组，同一personType的userId用逗号分割
        List<AssetAcceptPersonEntity> personList = assetAcceptPersonService.lambdaQuery()
                .eq(AssetAcceptPersonEntity::getAssetAcceptId, entity.getId())
                .list();
        
        Map<String, String> personTypeUserIdMap = new HashMap<>();
        if (CollUtil.isNotEmpty(personList)) {
            personTypeUserIdMap = personList.stream()
                    .collect(Collectors.groupingBy(
                            AssetAcceptPersonEntity::getPersonType,
                            Collectors.mapping(
                                    AssetAcceptPersonEntity::getUserId,
                                    Collectors.joining(",")
                            )
                    ));
        }
        
        // 合并entity和人员信息到variablesMap
        Map<String, Object> variablesMap = BeanUtil.beanToMap(entity);
        variablesMap.putAll(personTypeUserIdMap);
        startDTO.setVariablesMap(variablesMap);
        
        ApiResult<ProcessManagementDTO.StartResultDTO> result = workflowFeign.start(startDTO);
        if (!result.isSuccess()) {
            throw new ServiceException(result.getMsg());
        }
    }
    private void fillOne(AssetAcceptDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        String id = data.getId();

        // 填充验收组织名称
        if (StringUtils.isNotBlank(data.getAcceptOrgId()) && StringUtils.isBlank(data.getAcceptOrgName())) {
            try {
                List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(data.getAcceptOrgId()));
                if (CollUtil.isNotEmpty(orgList)) {
                    data.setAcceptOrgName(orgList.get(0).getName());
                }
            } catch (Exception e) {
                log.error("查询验收组织信息失败，orgId: {}", data.getAcceptOrgId(), e);
            }
        }

        // 填充验收人姓名
        if (StringUtils.isNotBlank(data.getAcceptUserId()) && StringUtils.isBlank(data.getAcceptUserName())) {
            try {
                FindUserDTO user = sysUserFeign.getUserByUserId(data.getAcceptUserId());
                if (ObjectUtil.isNotEmpty(user)) {
                    data.setAcceptUserName(user.getUserName());
                }
            } catch (Exception e) {
                log.error("查询验收人信息失败，userId: {}", data.getAcceptUserId(), e);
            }
        }

        // 填充验收部门名称
        if (StringUtils.isNotBlank(data.getAcceptDeptId()) && StringUtils.isBlank(data.getAcceptDeptName())) {
            try {
                List<com.erp.model.sys.entity.SysDepartmentEntity> deptList = sysUserFeign.getDeptByIds(Arrays.asList(data.getAcceptDeptId()));
                if (CollUtil.isNotEmpty(deptList)) {
                    data.setAcceptDeptName(deptList.get(0).getName());
                }
            } catch (Exception e) {
                log.error("查询验收部门信息失败，deptId: {}", data.getAcceptDeptId(), e);
            }
        }

        // 填充审核人姓名
        if (StringUtils.isNotBlank(data.getApproveUserId()) && StringUtils.isBlank(data.getApproveUserName())) {
            try {
                FindUserDTO user = sysUserFeign.getUserByUserId(data.getApproveUserId());
                if (ObjectUtil.isNotEmpty(user)) {
                    data.setApproveUserName(user.getUserName());
                }
            } catch (Exception e) {
                log.error("查询审核人信息失败，userId: {}", data.getApproveUserId(), e);
            }
        }

        // 查询验收人员数据
        List<AssetAcceptPersonEntity> personList = assetAcceptPersonService.lambdaQuery()
                .eq(AssetAcceptPersonEntity::getAssetAcceptId, id)
                .list();

        if (CollUtil.isNotEmpty(personList)) {
            List<AssetAcceptPersonDTO.ViewDTO> personViewList = personList.stream()
                    .map(person -> {
                        AssetAcceptPersonDTO.ViewDTO personView = new AssetAcceptPersonDTO.ViewDTO();
                        BeanMapperUtils.copy(person, personView);
                        return personView;
                    })
                    .collect(Collectors.toList());
            data.setPersonList(personViewList);
        }

        // 查询验收明细数据
        List<AssetAcceptDetailEntity> detailList = assetAcceptDetailService.lambdaQuery()
                .eq(AssetAcceptDetailEntity::getMainId, id)
                .list();

        if (CollUtil.isNotEmpty(detailList)) {
            // 收集所有需要查询的资产位置ID和部门ID
            List<String> assetLocationIds = detailList.stream()
                    .map(AssetAcceptDetailEntity::getAssetLocationId)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<String> useDeptIds = detailList.stream()
                    .map(AssetAcceptDetailEntity::getUseDeptId)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 批量查询资产位置信息
            Map<String, String> assetLocationMap = new HashMap<>();
            if (CollUtil.isNotEmpty(assetLocationIds)) {
                try {
                    List<com.erp.model.fms.entity.AssetLocationEntity> locationList = assetLocationService.listByIds(assetLocationIds);
                    if (CollUtil.isNotEmpty(locationList)) {
                        assetLocationMap = locationList.stream()
                                .filter(loc -> StringUtils.isNotBlank(loc.getId()) && StringUtils.isNotBlank(loc.getAddress()))
                                .collect(Collectors.toMap(
                                        com.erp.model.fms.entity.AssetLocationEntity::getId,
                                        com.erp.model.fms.entity.AssetLocationEntity::getAddress,
                                        (v1, v2) -> v1
                                ));
                    }
                } catch (Exception e) {
                    log.error("批量查询资产位置信息失败", e);
                }
            }
            
            // 批量查询部门信息
            Map<String, String> deptMap = new HashMap<>();
            if (CollUtil.isNotEmpty(useDeptIds)) {
                try {
                    List<com.erp.model.sys.entity.SysDepartmentEntity> deptList = sysUserFeign.getDeptByIds(useDeptIds);
                    if (CollUtil.isNotEmpty(deptList)) {
                        deptMap = deptList.stream()
                                .filter(dept -> StringUtils.isNotBlank(dept.getId()) && StringUtils.isNotBlank(dept.getName()))
                                .collect(Collectors.toMap(
                                        com.erp.model.sys.entity.SysDepartmentEntity::getId,
                                        com.erp.model.sys.entity.SysDepartmentEntity::getName,
                                        (v1, v2) -> v1
                                ));
                    }
                } catch (Exception e) {
                    log.error("批量查询部门信息失败", e);
                }
            }
            
            // 查询采购订单明细（用于获取采购数量）
            Map<String, Integer> purchaseQtyMap = new HashMap<>();
            if (StringUtils.isNotBlank(data.getSourceId())) {
                try {
                    ApiResult<List<AssetPurchaseOrderDTO.DetailForAcceptDTO>> apiResult =
                            assetPurchaseOrderFeign.queryDetailsForAccept(data.getSourceId());
                    if (apiResult.isSuccess() && CollUtil.isNotEmpty(apiResult.getData())) {
                        purchaseQtyMap = apiResult.getData().stream()
                                .filter(d -> StringUtils.isNotBlank(d.getId()) && d.getPurchaseQty() != null)
                                .collect(Collectors.toMap(
                                        AssetPurchaseOrderDTO.DetailForAcceptDTO::getId,
                                        AssetPurchaseOrderDTO.DetailForAcceptDTO::getPurchaseQty,
                                        (v1, v2) -> v1
                                ));
                    }
                } catch (Exception e) {
                    log.error("查询采购订单明细失败", e);
                }
            }
            
            // 查询所有关联相同采购订单的验收单明细（用于计算数量）
            List<AssetAcceptDetailEntity> allAcceptDetailList = new ArrayList<>();
            Map<String, String> acceptStatusMap = new HashMap<>();
            if (StringUtils.isNotBlank(data.getSourceId())) {
                // 查询所有来自同一采购订单的验收单
                List<AssetAcceptEntity> allAcceptList = this.lambdaQuery()
                        .eq(AssetAcceptEntity::getSourceId, data.getSourceId())
                        .eq(AssetAcceptEntity::getIsDeleted, false)
                        .list();
                
                if (CollUtil.isNotEmpty(allAcceptList)) {
                    List<String> allAcceptIds = allAcceptList.stream()
                            .map(AssetAcceptEntity::getId)
                            .collect(Collectors.toList());
                    
                    // 构建审核状态映射
                    acceptStatusMap = allAcceptList.stream()
                            .collect(Collectors.toMap(AssetAcceptEntity::getId, x -> x.getApproveStatus().getStatus()));
                    
                    // 查询所有验收明细
                    allAcceptDetailList = assetAcceptDetailService.lambdaQuery()
                            .in(AssetAcceptDetailEntity::getMainId, allAcceptIds)
                            .eq(AssetAcceptDetailEntity::getIsDeleted, false)
                            .list();
                }
            }
            
            // 创建副本用于lambda使用
            final Map<String, String> finalAssetLocationMap = assetLocationMap;
            final Map<String, String> finalDeptMap = deptMap;
            final Map<String, Integer> finalPurchaseQtyMap = purchaseQtyMap;
            final List<AssetAcceptDetailEntity> finalAllAcceptDetailList = allAcceptDetailList;
            final Map<String, String> finalAcceptStatusMap = acceptStatusMap;
            
            List<AssetAcceptDetailDTO.ViewDTO> detailViewList = detailList.stream()
                    .map(detail -> {
                        AssetAcceptDetailDTO.ViewDTO detailView = new AssetAcceptDetailDTO.ViewDTO();
                        BeanUtil.copyProperties(detail, detailView);
                        
                        // 填充资产位置名称
                        if (StringUtils.isNotBlank(detail.getAssetLocationId())) {
                            String locationName = finalAssetLocationMap.get(detail.getAssetLocationId());
                            if (StringUtils.isNotBlank(locationName)) {
                                detailView.setAssetLocationName(locationName);
                            }
                        }
                        
                        // 填充部门名称
                        if (StringUtils.isNotBlank(detail.getUseDeptId()) && StringUtils.isBlank(detail.getUseDeptName())) {
                            detailView.setUseDeptName(finalDeptMap.get(detail.getUseDeptId()));
                        }
                        
                        // 计算数量（参考queryMoldPurchaseOrderDetails的逻辑）
                        if (StringUtils.isNotBlank(detail.getSourceDetailId())) {
                            int purchaseQty = finalPurchaseQtyMap.getOrDefault(detail.getSourceDetailId(), 0);
                            int approvedAcceptQty = 0;  // 已审核的验收数量
                            int processingAcceptQty = 0; // 待提交、审核中、审核不通过的验收数量

                            for (AssetAcceptDetailEntity acceptDetail : finalAllAcceptDetailList) {
                                if (!detail.getSourceDetailId().equals(acceptDetail.getSourceDetailId())) {
                                    continue;
                                }

                                String approveStatus = finalAcceptStatusMap.get(acceptDetail.getMainId());
                                if (approveStatus == null) {
                                    continue;
                                }

                                Integer acceptQty = acceptDetail.getAcceptQty() != null ? acceptDetail.getAcceptQty() : 0;

                                // 已审核
                                if (ApproveStatusEnum.APPROVE.getStatus().equals(approveStatus)) {
                                    approvedAcceptQty += acceptQty;
                                }
                                // 待提交、审核中、审核不通过
                                else if (ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(approveStatus) 
                                        || ApproveStatusEnum.APPROVE_ING.getStatus().equals(approveStatus) 
                                        || ApproveStatusEnum.REJECT.getStatus().equals(approveStatus)) {
                                    processingAcceptQty += acceptQty;
                                }
                            }

                            // 注释：
                            // 0. 采购数量
                            detailView.setPurchaseQty(purchaseQty);
                            
                            // 1. 已验收数量 = 已审核资产验收单验收数量
                            detailView.setAcceptedQty(approvedAcceptQty);
                            
                            // 2. 待验收数量 = 采购数量 - 已验收数量
                            int pendingAcceptQty = purchaseQty - approvedAcceptQty;
                            detailView.setPendingAcceptQty(Math.max(pendingAcceptQty, 0));
                            
                            // 3. 可验收数量 = 待验收数量 - 待提交、审核中、审核不通过的资产验收单验收数量
                            int availableAcceptQty = pendingAcceptQty - processingAcceptQty;
                            detailView.setAvailableAcceptQty(Math.max(availableAcceptQty, 0));
                        }
                        
                        return detailView;
                    })
                    .collect(Collectors.toList());
            data.setDetailList(detailViewList);
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
        this.lambdaUpdate().eq(AssetAcceptEntity::getId, id)
            .set(AssetAcceptEntity::getApproveUserId, userInfo.getUid())
            .set(AssetAcceptEntity::getApproveUserName, userInfo.getUserName())
            .set(AssetAcceptEntity::getApproveStatus, approveStatus)
            .set(AssetAcceptEntity::getApproveTime, LocalDateTime.now())
            .update(new AssetAcceptEntity());
     }

    /**
    * 反审核更新审核信息
    * @param id
    * @param approveStatus
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateForDisApprove(String id, String approveStatus) {
        this.lambdaUpdate().eq(AssetAcceptEntity::getId, id)
            .set(AssetAcceptEntity::getApproveUserId, "")
            .set(AssetAcceptEntity::getApproveUserName, "")
            .set(AssetAcceptEntity::getApproveStatus, approveStatus)
            .set(AssetAcceptEntity::getApproveTime, null)
            .update(new AssetAcceptEntity());
        }

    /**
    * 更新审核状态
    */
    @Transactional(rollbackFor = Exception.class)
    public void updateApproveStatus(String id, String approveStatus) {
        lambdaUpdate().eq(AssetAcceptEntity::getId, id)
        .set(AssetAcceptEntity::getApproveUserId, "")
        .set(AssetAcceptEntity::getApproveUserName, "")
        .set(AssetAcceptEntity::getApproveStatus, approveStatus)
        .set(AssetAcceptEntity::getApproveTime, null)
        .update(new AssetAcceptEntity());
    }

    /**
    * 分页查询、导出 数据处理
    */
    private void fillList(List<AssetAcceptDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
           return;
        }

        //最新审核人
        ValidList<ProcessManagementDTO.HistoryActivityDTO> dtoList = new ValidList<>();
        list.forEach(obj -> {
            dtoList.add(new ProcessManagementDTO.HistoryActivityDTO(SourceTypeEnum.ASSET_ACCEPTANCE.getCode(), obj.getId()));
        });
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = null;
        if (CollectionUtils.isNotEmpty(dtoList)) {
            listApiResult = workflowFeign.curApprover(dtoList);
            Integer code = listApiResult.getCode();
            if (200 != code) {
                throw new ServiceException(new ApiResult(ApiError.DEFAULT.code, listApiResult.getMsg()));
            }
        }

        // 属性赋值
        for(AssetAcceptDTO.ListDTO data : list) {
            data.setApproveStatusName(ApproveStatusEnum.getName(data.getApproveStatus()));
            data.setInvalidStatusName(InvalidStatusEnum.getName(data.getInvalidStatus()));
            
            // 设置验收人中文名称（从数据库中已有的 acceptUserName 字段获取）
            data.setAcceptPersonNames(data.getAcceptUserName());

            // 设置来源类型为"采购收货"（资产验收单创建的卡片）
            if (StringUtils.isBlank(data.getSourceType())) {
                data.setSourceType("采购收货");
            }
            
            // 设置资产卡片关联状态名称（使用枚举）
            data.setAssetCardStatusName(AssetCardStatusEnum.getName(data.getAssetCardStatus()));

            //最新审核人：先判断流程中的审核人是否存在，如果存在则使用流程中的，否则保持数据库原值
            if (CollectionUtils.isNotEmpty(listApiResult.getData())) {
                String curApprove = listApiResult.getData().stream().filter(e -> e.getBusinessId().equals(data.getId()) && StringUtils.isNotBlank(e.getCurApproveName())).map(ProcessManagementDTO.CurApproveInfoDTO::getCurApproveName).collect(Collectors.joining(","));
                if (StringUtils.isNotBlank(curApprove)) {
                    data.setApproveUserName(curApprove);
                }
            }
        }
    }
    /**
    * 分页查询、导出 数据处理
    */
    private void validateSubmit(AssetAcceptEntity entity) {
        // 待提交或审核不通过并且未作废允许提交
        if(!ApproveStatusEnum.allowUpdateStatus(entity.getApproveStatus())) {
            throw new ServiceException(ApiError.ERROR_98010);
        }
        return;
    }

    /**
    * 转资产卡片
    * @author wuht
    * @date: 2025-10-11
    * @param detailId 明细ID
    * @return
    */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO transferToAssetCard(String detailId) {
        // 获取验收明细
        AssetAcceptDetailEntity detail = assetAcceptDetailService.getByIdOpt(detailId).orElseThrow(() -> new ServiceException("资产验收明细不存在"));
        
        // 验证明细状态：只有待生成状态的明细才能下推
        if (AssetCardStatusEnum.GENERATED.getStatus().equals(detail.getAssetCardStatus())) {
            throw new ServiceException("只有已审核且资产卡片关联状态为待生成的数据才能下推!");
        }
        
        // 获取主表信息
        AssetAcceptEntity assetAcceptEntity = super.getByIdOpt(detail.getMainId()).orElseThrow(() -> new ServiceException("资产验收单不存在"));
        
        // 验证状态：只有已审核的资产验收单才能转资产卡片
        if (!ApproveStatusEnum.APPROVE.equals(assetAcceptEntity.getApproveStatus())) {
            throw new ServiceException("只有已审核的资产验收单才能转资产卡片");
        }
        
        // 生成资产卡片
        List<String> generatedCardIds = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        
        // 根据数量拆分生成资产卡片
        Integer acceptQty = detail.getAcceptQty();
        for (int i = 0; i < acceptQty; i++) {
            AssetCardDTO.AddDTO cardAddDTO = buildAssetCardFromAcceptDetail(assetAcceptEntity, detail, currentDate);
            BaseResultDTO.AddDTO cardResult = assetCardService.addAndSubmit(cardAddDTO);
            generatedCardIds.add(cardResult.getId());
        }
        
        // 更新明细状态为已生成
        detail.setAssetCardStatus(AssetCardStatusEnum.GENERATED.getStatus());
        assetAcceptDetailService.updateById(detail);
        
        log.info("资产验收明细{}转资产卡片成功，生成{}张资产卡片", detailId, generatedCardIds.size());
        return BatchResultDTO.success(detailId, detail.getProductName(), "转资产卡片成功，生成" + generatedCardIds.size() + "张资产卡片");
    }
    
    /**
     * 根据验收明细构建资产卡片
     */
    private AssetCardDTO.AddDTO buildAssetCardFromAcceptDetail(AssetAcceptEntity acceptEntity, AssetAcceptDetailEntity detail, LocalDate currentDate) {
        AssetCardDTO.AddDTO cardDTO = new AssetCardDTO.AddDTO();
        
        // 基础信息映射
        cardDTO.setSourceCode(acceptEntity.getCode()); // 来源单号
        cardDTO.setSourceType(SourceTypeEnum.ASSET_ACCEPTANCE.getCode()); // 卡片来源
        cardDTO.setSourceId(acceptEntity.getId()); // 来源ID
        cardDTO.setOrgId(acceptEntity.getAcceptOrgId()); // 资产组织 = 验收组织
        cardDTO.setOrgName(acceptEntity.getAcceptOrgName()); // 资产组织名称
        cardDTO.setUnit(UnitEnum.PCS.getName()); // 计量单位默认值
        cardDTO.setType(AssetCategoryEnum.MACHINERY.getName()); // 资产类别默认值
        cardDTO.setStatus(AssetStatusEnum.NORMAL.getName()); // 资产状态默认值
        cardDTO.setChangeMethod(ChangeMethodEnum.PURCHASE.getName()); // 变动方式默认值
        cardDTO.setName(detail.getProductName()); // 资产名称 = 产品名称
        cardDTO.setStartUseDate(currentDate); // 开始使用日期 = 操作日期
        cardDTO.setQty(1); // 数量 = 1（每个明细项拆分为多个卡片）

        return cardDTO;
    }

    /**
    * 获取资产验收表分页数据（用于异步导出）
    * @author wuht
    * @date: 2025-10-11
    * @param dto 分页参数
    * @return
    */
    @Override
    public PagingVO<AssetAcceptDTO.ListDTO> getAssetAcceptPageData(PagingDTO<AssetAcceptDTO.ExportDTO> dto) {
        // 创建分页对象
        Page<AssetAcceptDTO.ListDTO> page = new Page<>(dto.getCurrPage(), dto.getPageSize());
        // 调用现有的分页查询方法
        IPage<AssetAcceptDTO.ListDTO> pageData = this.baseMapper.listExport(page, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>();
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    /**
    * 查询添加明细
    * @author wuht
    * @date: 2025-10-11
    * @param dto 查询参数
    * @return
    */
    @Override
    public AssetAcceptDTO.AddDetailResultDTO queryAddDetail(AssetAcceptDTO.AddDetailQueryDTO dto) {
        AssetAcceptDTO.AddDetailResultDTO result = new AssetAcceptDTO.AddDetailResultDTO();
        List<AssetAcceptDTO.AddDetailItemDTO> detailList = new ArrayList<>();
        
        // 场景1：编辑模式 - 由模具采购订单下推生成
        if (StringUtils.isNotBlank(dto.getAssetAcceptId())) {
            AssetAcceptEntity assetAccept = getByIdOpt(dto.getAssetAcceptId())
                .orElseThrow(() -> new ServiceException("资产验收单不存在"));
            
            // 判断是否为模具采购订单下推
            if (SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode().equals(assetAccept.getSourceType())) {
                result.setIsFromMoldPurchaseOrder(true);
                result.setMoldPurchaseOrderCode(assetAccept.getSourceCode());
                
                // 查询模具采购订单的明细范围
                detailList = queryMoldPurchaseOrderDetails(assetAccept.getSourceId(), dto);
                
                // 查询已有的验收明细，标记为已选中（场景2：模具采购订单已添加明细，则弹框明细为勾选状态）
                List<AssetAcceptDetailEntity> existingDetails = assetAcceptDetailService.lambdaQuery()
                    .eq(AssetAcceptDetailEntity::getMainId, dto.getAssetAcceptId())
                    .eq(AssetAcceptDetailEntity::getIsDeleted, false)
                    .list();
                
                Set<String> existingSkuNos = existingDetails.stream()
                    .map(AssetAcceptDetailEntity::getSkuNo)
                    .collect(Collectors.toSet());
                
                // 标记已选中的明细
                for (AssetAcceptDTO.AddDetailItemDTO item : detailList) {
                    if (existingSkuNos.contains(item.getSkuNo())) {
                        item.setSelected(true);
                    }
                }
            } else {
                // 手动新增，查询资产SKU（场景3：手动新增生成，则弹出产品弹框，过滤非资产SKU）
                detailList = queryAssetSkuDetails(dto);
            }
        } else {
            // 新增模式
            if (StringUtils.isNotBlank(dto.getAssetPurchaseOrderId())) {
                // 场景1：由模具采购订单下推生成，则编辑添加明细时只允许选择模具采购订单所选模具范围
                result.setIsFromMoldPurchaseOrder(true);
                detailList = queryMoldPurchaseOrderDetails(dto.getAssetPurchaseOrderId(), dto);
            } else {
                // 场景3：手动新增生成，则弹出产品弹框，过滤非资产SKU
                detailList = queryAssetSkuDetails(dto);
            }
        }
        
        result.setList(detailList);
        return result;
    }

    /**
    * 查询模具采购订单明细
    * @author wuht
    * @date: 2025-10-11
    * @param assetPurchaseOrderId 资产采购订单ID
    * @param queryDTO 查询参数
    * @return
    */
    private List<AssetAcceptDTO.AddDetailItemDTO> queryMoldPurchaseOrderDetails(String assetPurchaseOrderId, AssetAcceptDTO.AddDetailQueryDTO queryDTO) {
        List<AssetAcceptDTO.AddDetailItemDTO> detailList = new ArrayList<>();
        
        try {
            // 调用资产采购订单服务查询明细
            ApiResult<List<AssetPurchaseOrderDTO.DetailForAcceptDTO>> apiResult =
                assetPurchaseOrderFeign.queryDetailsForAccept(assetPurchaseOrderId);
            
            if (!apiResult.isSuccess() || CollUtil.isEmpty(apiResult.getData())) {
                return detailList;
            }

            // 收集所有采购订单明细ID
            List<String> purchaseDetailIds = apiResult.getData().stream()
                    .map(AssetPurchaseOrderDTO.DetailForAcceptDTO::getId)
                    .collect(Collectors.toList());

            // 查询所有关联的验收单明细，计算数量
            List<AssetAcceptDetailEntity> acceptDetailList = assetAcceptDetailService.lambdaQuery()
                    .in(AssetAcceptDetailEntity::getSourceDetailId, purchaseDetailIds)
                    .eq(AssetAcceptDetailEntity::getIsDeleted, false)
                    .list();

            // 获取所有验收单主表ID
            List<String> acceptMainIds = acceptDetailList.stream()
                    .map(AssetAcceptDetailEntity::getMainId)
                    .distinct()
                    .collect(Collectors.toList());

            // 查询验收单主表，获取审核状态
            Map<String, String> acceptStatusMap = new HashMap<>();
            if (CollUtil.isNotEmpty(acceptMainIds)) {
                List<AssetAcceptEntity> acceptMainList = this.lambdaQuery()
                        .in(AssetAcceptEntity::getId, acceptMainIds)
                        .eq(AssetAcceptEntity::getIsDeleted, false)
                        .list();
                acceptStatusMap = acceptMainList.stream()
                        .collect(Collectors.toMap(AssetAcceptEntity::getId, x->x.getApproveStatus().getStatus()));
            }

            // 转换数据并计算数量
            for (AssetPurchaseOrderDTO.DetailForAcceptDTO detail : apiResult.getData()) {
                AssetAcceptDTO.AddDetailItemDTO item = new AssetAcceptDTO.AddDetailItemDTO();
                item.setSourceDetailId(detail.getId()); // 设置来源单据明细ID（模具采购订单明细ID）
                item.setSkuId(detail.getSkuId());
                item.setSkuNo(detail.getSkuNo());
                item.setProductName(detail.getProductName());
                item.setMoldCode(detail.getMoldCode());
                item.setMoldName(detail.getMoldName());
                item.setPurchaseQty(detail.getPurchaseQty());
                item.setIsUrgent(detail.getIsUrgent());
                item.setRemark(detail.getRemark());

                // 计算数量
                int purchaseQty = detail.getPurchaseQty() != null ? detail.getPurchaseQty() : 0;
                int approvedAcceptQty = 0;  // 已审核的验收数量
                int processingAcceptQty = 0; // 待提交、审核中、审核不通过的验收数量

                for (AssetAcceptDetailEntity acceptDetail : acceptDetailList) {
                    if (!detail.getId().equals(acceptDetail.getSourceDetailId())) {
                        continue;
                    }

                    String approveStatus = acceptStatusMap.get(acceptDetail.getMainId());
                    if (approveStatus == null) {
                        continue;
                    }

                    Integer acceptQty = acceptDetail.getAcceptQty() != null ? acceptDetail.getAcceptQty() : 0;

                    // 已审核
                    if (ApproveStatusEnum.APPROVE.getStatus().equals(approveStatus)) {
                        approvedAcceptQty += acceptQty;
                    }
                    // 待提交、审核中、审核不通过
                    else if (ApproveStatusEnum.WAIT_SUBMIT.getStatus().equals(approveStatus) 
                            || ApproveStatusEnum.APPROVE_ING.getStatus().equals(approveStatus) 
                            || ApproveStatusEnum.REJECT.getStatus().equals(approveStatus)) {
                        processingAcceptQty += acceptQty;
                    }
                }

                // 注释：
                // 1. 已验收数量 = 已审核资产验收单验收数量
                item.setAcceptedQty(approvedAcceptQty);
                
                // 2. 待验收数量 = 采购数量 - 已验收数量
                int pendingAcceptQty = purchaseQty - approvedAcceptQty;
                item.setPendingAcceptQty(Math.max(pendingAcceptQty, 0));
                
                // 3. 可验收数量 = 待验收数量 - 待提交、审核中、审核不通过的资产验收单验收数量
                int availableAcceptQty = pendingAcceptQty - processingAcceptQty;
                item.setAvailableAcceptQty(Math.max(availableAcceptQty, 0));

                detailList.add(item);
            }
        } catch (Exception e) {
            log.error("查询资产采购订单明细失败, assetPurchaseOrderId: {}", assetPurchaseOrderId, e);
        }
        
        return detailList;
    }

    /**
    * 查询资产SKU明细（场景3：手动新增生成，则弹出产品弹框，过滤非资产SKU）
    * @author wuht
    * @date: 2025-10-11
    * @param queryDTO 查询参数
    * @return
    */
    private List<AssetAcceptDTO.AddDetailItemDTO> queryAssetSkuDetails(AssetAcceptDTO.AddDetailQueryDTO queryDTO) {
        List<AssetAcceptDTO.AddDetailItemDTO> detailList = new ArrayList<>();
        
        try {
            // 1. 调用PLM系统获取审核通过的资产SKU列表
            com.erp.model.plm.dto.ProductSkuDTO productSkuDTO = new com.erp.model.plm.dto.ProductSkuDTO();
            
            // 参考 SkuListQueryDTO 的传参方式
            if (StringUtils.isNotBlank(queryDTO.getSearchKeyword())) {
                productSkuDTO.setRemoteSearchSku(queryDTO.getSearchKeyword());
            }
            
            productSkuDTO.setStatusList(Collections.singletonList(2)); // 2表示审核通过
            productSkuDTO.setPropertyList(Collections.singletonList("资产")); //过滤资产类型

            com.common.business.dto.base.PagingDTO<com.erp.model.plm.dto.ProductSkuDTO> pagingDTO = new com.common.business.dto.base.PagingDTO<>();
            pagingDTO.setParams(productSkuDTO);
            pagingDTO.setCurrPage(queryDTO.getCurrPage());
            pagingDTO.setPageSize(queryDTO.getPageSize()); // 最多支持200行

            // 调用PLM系统的listSku接口
            com.common.business.vo.PagingVO<com.erp.model.plm.dto.ProductDetailDTO.SkuDTO> plmResult = plmTaskFeign.listSku(pagingDTO);

            if (plmResult == null || CollUtil.isEmpty(plmResult.getList())) {
                log.info("PLM系统未返回资产SKU数据");
                return detailList;
            }

            // 2. 转换为返回格式
            for (com.erp.model.plm.dto.ProductDetailDTO.SkuDTO skuDTO : plmResult.getList()) {
                AssetAcceptDTO.AddDetailItemDTO item = new AssetAcceptDTO.AddDetailItemDTO();
                item.setSkuId(skuDTO.getSkuId());
                item.setSkuNo(skuDTO.getSkuNo());
                item.setProductName(skuDTO.getProductName());
                item.setAvailableAcceptQty(0); // 根据业务需求设置
                detailList.add(item);
            }
            
        } catch (Exception e) {
            log.error("查询资产SKU明细失败, searchKeyword: {}", queryDTO.getSearchKeyword(), e);
        }
        
        return detailList;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(AssetAcceptEntity assetAcceptEntity) {
        // 根据验收人ID查询用户信息并回填中文名
        if (StringUtils.isNotBlank(assetAcceptEntity.getAcceptUserId())) {
            try {
                FindUserDTO user = sysUserFeign.getUserByUserId(assetAcceptEntity.getAcceptUserId());
                if (user != null && StringUtils.isNotBlank(user.getUserName())) {
                    assetAcceptEntity.setAcceptUserName(user.getUserName());
                }
            } catch (Exception e) {
                log.error("查询验收人用户信息失败，userId: {}", assetAcceptEntity.getAcceptUserId(), e);
            }
        }
        // TODO 验证数据 & 数据赋值
    }

    /**
     * 导入Excel数据
     * @author wuht
     * @date: 2025-10-11
     * @param dto 导入参数
     * @return
     */
    @Override
    public Boolean importExcel(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("资产验收表导入", FileTaskEventEnum.IMPORT_FMS_ASSET_ACCEPT.getCode(), dto);
        return Boolean.TRUE;
    }

    /**
     * 导入资产验收表
     * @author wuht
     * @date: 2025-10-11
     * @param dto 导入参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importAssetAccept(BaseDTO.ImportDTO dto) {
        AssetAcceptExcelListener excelListenerUtil = new AssetAcceptExcelListener(dto.getTaskId(), dto.getImportType(), dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), AssetAcceptExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<AssetAcceptExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollUtil.isNotEmpty(errorList)) {
            String fileName = "资产验收表错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, AssetAcceptExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    /**
     * 处理导入成功的数据
     * @author wuht
     * @date: 2025-10-11
     * @param successList 成功数据列表
     * @param errorNoList 错误序号列表
     * @param errorList2 错误数据列表
     * @param importType 导入类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    public void handleImportSuccessList(List<AssetAcceptExcelDTO> successList, 
                                       List<String> errorNoList, 
                                       List<AssetAcceptExcelDTO> errorList2, 
                                       String importType) {
        if (CollUtil.isEmpty(successList)) {
            return;
        }
        if (StringUtils.isBlank(importType)){
            //给个默认值
            importType = ImportTypeEnum.ADD.getCode();
        }

        if (CollUtil.isNotEmpty(errorNoList)) {
            successList = successList.stream().filter(e -> StringUtils.isNotBlank(e.getSerialNumber()) && !errorNoList.contains(e.getSerialNumber())).collect(Collectors.toList());

            // 全部返回到错误列表
            List<AssetAcceptExcelDTO> collect = successList.stream().filter(e -> StringUtils.isBlank(e.getSerialNumber()) || errorNoList.contains(e.getSerialNumber())).collect(Collectors.toList());
            errorList2.addAll(collect);
        }

        AssetAcceptServiceImpl bean = ApplicationContextUtils.getBean(AssetAcceptServiceImpl.class);

        // 按序号分组
        Map<String, List<AssetAcceptExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(AssetAcceptExcelDTO::getSerialNumber));
        for (Map.Entry<String, List<AssetAcceptExcelDTO>> entry : collect.entrySet()) {
            List<AssetAcceptExcelDTO> value = entry.getValue();
            AssetAcceptExcelDTO importMainDTO = value.get(0);
            
            try {
                AssetAcceptDTO.AddDTO addDTO = new AssetAcceptDTO.AddDTO();
                BeanMapperUtils.copy(importMainDTO, addDTO);
                
                // 设置验收日期
                addDTO.setAcceptDate(importMainDTO.getAcceptDate());
                // 模具采购订单号：如果为空则设置默认值（因为是必填字段）
                addDTO.setPurchaseCode(StringUtils.isNotBlank(importMainDTO.getPurchaseCode()) 
                    ? importMainDTO.getPurchaseCode() 
                    : "手动创建");

            // 如果填写了模具采购订单号，查询订单信息并设置来源
            Map<String, String> skuToDetailIdMap = new HashMap<>();
            if (StringUtils.isNotBlank(importMainDTO.getPurchaseCode())) {
                try {
                    ApiResult<AssetPurchaseOrderDTO.DetailWithSkuDTO> orderResult =
                        assetPurchaseOrderFeign.getByCode(importMainDTO.getPurchaseCode());
                    
                    if (orderResult != null && orderResult.isSuccess() && orderResult.getData() != null) {
                        AssetPurchaseOrderDTO.DetailWithSkuDTO orderData = orderResult.getData();
                        
                        // 设置来源信息
                        addDTO.setSourceId(orderData.getId());
                        addDTO.setSourceCode(orderData.getCode());
                        addDTO.setSourceType(SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode());
                        
                        // 设置供应商信息
                        if (StringUtils.isNotBlank(orderData.getSupplierId())) {
                            addDTO.setSupplierId(orderData.getSupplierId());
                            addDTO.setSupplierName(orderData.getSupplierName());
                        }
                        
                        // 构建SKU到明细ID的映射
                        if (CollUtil.isNotEmpty(orderData.getDetailList())) {
                            skuToDetailIdMap = orderData.getDetailList().stream()
                                .filter(detail -> StringUtils.isNotBlank(detail.getSkuNo()))
                                .collect(Collectors.toMap(
                                    detail -> detail.getSkuNo(),
                                    detail -> detail.getId(),
                                    (existing, replacement) -> existing
                                ));
                        }
                    } else {
                        log.warn("根据模具采购订单号{}查询订单失败: {}", 
                            importMainDTO.getPurchaseCode(), 
                            orderResult != null ? orderResult.getMsg() : "返回结果为空");
                    }
                } catch (Exception e) {
                    log.error("查询模具采购订单{}失败", importMainDTO.getPurchaseCode(), e);
                }
            }
            addDTO.setIsNeedSeal(importMainDTO.getIsNeedSeal());
            addDTO.setAcceptOrgId(importMainDTO.getAcceptOrgId());
            addDTO.setAcceptOrgName(importMainDTO.getAcceptOrgName());
            addDTO.setAcceptUserId(importMainDTO.getAcceptUserId());
            addDTO.setAcceptUserName(importMainDTO.getAcceptUserName());
            addDTO.setAcceptDeptId(importMainDTO.getAcceptDeptId());
            addDTO.setAcceptDeptName(importMainDTO.getAcceptDeptName());
            // 验收说明：如果为空则设置默认值（因为是必填字段）
            addDTO.setAcceptDesc(StringUtils.isNotBlank(importMainDTO.getAcceptDesc()) 
                ? importMainDTO.getAcceptDesc() 
                : "资产验收");
            
            // 如果没有设置供应商信息（查询订单失败或未填订单号），则标记为错误
            if (StringUtils.isBlank(addDTO.getSupplierId())) {
                String errorMsg = "无法获取供应商信息，请确保模具采购订单号正确且订单已审核通过";
                for (AssetAcceptExcelDTO dto : value) {
                    dto.setErrorMsg(errorMsg);
                    errorList2.add(dto);
                }
                continue; // 跳过这组数据
            }

            // 构建验收人员列表
            List<AssetAcceptPersonDTO.AddDTO> personList = new ArrayList<>();
            if (StringUtils.isNotBlank(importMainDTO.getPurchaseDevId())) {
                AssetAcceptPersonDTO.AddDTO person = new AssetAcceptPersonDTO.AddDTO();
                person.setPersonType(PersonTypeEnum.PURCHASE_DEV.getCode());
                person.setUserId(importMainDTO.getPurchaseDevId());
                person.setUserName(importMainDTO.getPurchaseDevName());
                personList.add(person);
            }
            if (StringUtils.isNotBlank(importMainDTO.getQualityEngineerId())) {
                AssetAcceptPersonDTO.AddDTO person = new AssetAcceptPersonDTO.AddDTO();
                person.setPersonType(PersonTypeEnum.QUALITY_ENGINEER.getCode());
                person.setUserId(importMainDTO.getQualityEngineerId());
                person.setUserName(importMainDTO.getQualityEngineerName());
                personList.add(person);
            }
            if (StringUtils.isNotBlank(importMainDTO.getStructureEngineerId())) {
                AssetAcceptPersonDTO.AddDTO person = new AssetAcceptPersonDTO.AddDTO();
                person.setPersonType(PersonTypeEnum.STRUCTURE_ENGINEER.getCode());
                person.setUserId(importMainDTO.getStructureEngineerId());
                person.setUserName(importMainDTO.getStructureEngineerName());
                personList.add(person);
            }
            if (StringUtils.isNotBlank(importMainDTO.getProductManagerId())) {
                AssetAcceptPersonDTO.AddDTO person = new AssetAcceptPersonDTO.AddDTO();
                person.setPersonType(PersonTypeEnum.PRODUCT_MANAGER.getCode());
                person.setUserId(importMainDTO.getProductManagerId());
                person.setUserName(importMainDTO.getProductManagerName());
                personList.add(person);
            }
            if (StringUtils.isNotBlank(importMainDTO.getProjectManagerId())) {
                AssetAcceptPersonDTO.AddDTO person = new AssetAcceptPersonDTO.AddDTO();
                person.setPersonType(PersonTypeEnum.PROJECT_MANAGER.getCode());
                person.setUserId(importMainDTO.getProjectManagerId());
                person.setUserName(importMainDTO.getProjectManagerName());
                personList.add(person);
            }
            addDTO.setPersonList(personList);

            // 构建验收明细列表
            List<AssetAcceptDetailDTO.AddDTO> detailList = new ArrayList<>();
            for (AssetAcceptExcelDTO importDTO : value) {
                AssetAcceptDetailDTO.AddDTO detailDTO = new AssetAcceptDetailDTO.AddDTO();
                
                // 根据SKU编号从映射中获取来源明细ID
                if (StringUtils.isNotBlank(importDTO.getSkuNo()) && skuToDetailIdMap.containsKey(importDTO.getSkuNo())) {
                    detailDTO.setSourceDetailId(skuToDetailIdMap.get(importDTO.getSkuNo()));
                }
                
                detailDTO.setSkuId(importDTO.getSkuId());
                detailDTO.setProductName(importDTO.getProductName());
                detailDTO.setAcceptQty(importDTO.getAcceptQty());
                detailDTO.setAssetCardStatus(AssetCardStatusEnum.NOT_GENERATED.getStatus());
                // DTO使用新字段名
                detailDTO.setPurchaseQty(0);
                detailDTO.setPendingAcceptQty(0);
                detailDTO.setAcceptedQty(0);
                detailDTO.setAvailableAcceptQty(importDTO.getAcceptQty());
                detailDTO.setAssetLocationId(importDTO.getAssetLocationId());
                detailDTO.setUseDeptName(importDTO.getUseDeptName());
                detailDTO.setUseDeptId(importDTO.getUseDeptId());
                detailDTO.setCostType(importDTO.getCostType());
                detailDTO.setRemark(importDTO.getRemark());
                detailList.add(detailDTO);
            }
            addDTO.setDetailList(detailList);

                if (ImportTypeEnum.ADD.getCode().equals(importType)) {
                    bean.add(addDTO);
                }
            } catch (Exception e) {
                // 保存失败，添加到错误列表
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.length() > 200) {
                    errorMsg = errorMsg.substring(0, 200);
                }
                for (AssetAcceptExcelDTO dto : value) {
                    dto.setErrorMsg(errorMsg);
                    errorList2.add(dto);
                }
                log.error("导入第{}条资产验收单失败", importMainDTO.getSerialNumber(), e);
            }
        }
    }

    /**
     * 下载导入模板
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/assetAcceptTemplate.xlsx";
        String excelName = "资产验收单导入模板.xlsx";
        com.common.core.utils.ExcelUtil.downloadTemplate(path, excelName, response);
    }

    @Override
    public ApiResult<List<AssetAcceptDTO.AssetPurchaseOrderRefListDTO>> getAcceptByDetailId(String detailId) {
        List<AssetAcceptDetailEntity> list = assetAcceptDetailService.lambdaQuery()
                .eq(AssetAcceptDetailEntity::getSourceDetailId, detailId)
                .eq(AssetAcceptDetailEntity::getIsDeleted, Boolean.FALSE)
                .list();
        if (!list.isEmpty()) {
            AssetAcceptEntity assetAcceptEntity = this.lambdaQuery()
                    .eq(AssetAcceptEntity::getId, list.get(0).getMainId())
                    .eq(AssetAcceptEntity::getIsDeleted, Boolean.FALSE).one();
            List<AssetAcceptDTO.AssetPurchaseOrderRefListDTO> assetAcceptDetailEntityList = new ArrayList<>();
            for (AssetAcceptDetailEntity detailEntity : list) {
                AssetAcceptDTO.AssetPurchaseOrderRefListDTO assetPurchaseOrderRefListDTO = new AssetAcceptDTO.AssetPurchaseOrderRefListDTO();
                assetPurchaseOrderRefListDTO.setAssetAcceptCode(assetAcceptEntity.getCode());
                assetPurchaseOrderRefListDTO.setApproveStatuts(assetAcceptEntity.getApproveStatus().getCode());
                assetPurchaseOrderRefListDTO.setApproveStatutsName(assetAcceptEntity.getApproveStatus().getName());
                assetPurchaseOrderRefListDTO.setInvalidStatus(assetAcceptEntity.getInvalidStatus());
                assetPurchaseOrderRefListDTO.setInvalidStatusName(InvalidStatusEnum.getName(assetAcceptEntity.getInvalidStatus()));
                assetPurchaseOrderRefListDTO.setSkuId(detailEntity.getSkuId());
                assetPurchaseOrderRefListDTO.setSkuNo(detailEntity.getSkuNo());
                assetPurchaseOrderRefListDTO.setProductName(detailEntity.getProductName());
                assetPurchaseOrderRefListDTO.setAcceptDate(assetAcceptEntity.getAcceptDate());
                assetPurchaseOrderRefListDTO.setAcceptQty(new BigDecimal(detailEntity.getAcceptQty()));
                assetPurchaseOrderRefListDTO.setRemark(detailEntity.getRemark());
                assetPurchaseOrderRefListDTO.setApproveUserId(StringUtils.isNotBlank(assetAcceptEntity.getApproveUserId()) ? assetAcceptEntity.getApproveUserId() : null);
                assetPurchaseOrderRefListDTO.setApproveUserName(assetAcceptEntity.getApproveUserName());

                assetAcceptDetailEntityList.add(assetPurchaseOrderRefListDTO);
            }
            return success(assetAcceptDetailEntityList);
        }
        return success();
    }


    @Override
    public Boolean generateAssetAccept(List<AssetPurchaseOrderDTO.GenerateAssetAcceptDTO> dtoList) {
        if (dtoList.isEmpty()) {
            return Boolean.FALSE;
        }
        AssetAcceptEntity assetAcceptEntity = new AssetAcceptEntity();
        assetAcceptEntity.setSourceId(dtoList.get(0).getId());
        assetAcceptEntity.setSourceCode(dtoList.get(0).getCode());
        assetAcceptEntity.setSourceType(SourceTypeEnum.ASSET_PURCHASE_ORDER.getCode());
        assetAcceptEntity.setAcceptDate(LocalDate.now());
        log.info("开始通过下推新增资产验收单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_YSD);
        assetAcceptEntity.setCode(code);
        boolean save = this.save(assetAcceptEntity);

        if(!save) {
            throw new ServiceException("资产验收单保存失败");
        }
        List<AssetAcceptDetailEntity> assetAcceptDetailEntityList = new ArrayList<>();
        for (AssetPurchaseOrderDTO.GenerateAssetAcceptDTO generateAssetAcceptDTO : dtoList) {
            AssetAcceptDetailEntity detailEntity = new AssetAcceptDetailEntity();
            BeanUtils.copyProperties(generateAssetAcceptDTO,detailEntity,"id");
            detailEntity.setMainId(assetAcceptEntity.getId());
            detailEntity.setSourceDetailId(generateAssetAcceptDTO.getDetailId());
            detailEntity.setSkuId(generateAssetAcceptDTO.getAssetId());
            detailEntity.setSkuNo(generateAssetAcceptDTO.getAssetCode());
            detailEntity.setProductName(generateAssetAcceptDTO.getAssetName());
            detailEntity.setAcceptQty(generateAssetAcceptDTO.getAcceptQty() != null ? generateAssetAcceptDTO.getAcceptQty().intValue() : 0);
            // 1. 已验收数量 = 已审核资产验收单验收数量
            detailEntity.setAcceptedQty(generateAssetAcceptDTO.getAcceptedQty() != null ? generateAssetAcceptDTO.getAcceptedQty().intValue() : 0);
            // 2. 待验收数量 = 采购数量 - 已验收数量
            detailEntity.setPendingQty(generateAssetAcceptDTO.getPendingQty() != null ? generateAssetAcceptDTO.getPendingQty().intValue() : 0);
            // 3. 可验收数量 = 待验收数量 - 待提交、审核中、审核不通过的资产验收单验收数量
            detailEntity.setAcceptableQty(generateAssetAcceptDTO.getAcceptableQty() != null ? generateAssetAcceptDTO.getAcceptableQty().intValue() : 0);

            assetAcceptDetailEntityList.add(detailEntity);
        }

        return assetAcceptDetailService.saveBatch(assetAcceptDetailEntityList);
    }

}

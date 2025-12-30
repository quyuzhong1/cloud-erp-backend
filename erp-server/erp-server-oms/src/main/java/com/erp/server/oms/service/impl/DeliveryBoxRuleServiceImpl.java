package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.DeliveryBoxRuleDTO;
import com.erp.model.oms.dto.DeliveryBoxRuleDetailDTO;
import com.erp.model.oms.dto.excel.DeliveryBoxRuleImportExcelDTO;
import com.erp.model.oms.entity.DeliveryBoxRuleDetailEntity;
import com.erp.model.oms.entity.DeliveryBoxRuleEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.listener.DeliveryBoxRuleExcelListener;
import com.erp.server.oms.mapper.DeliveryBoxRuleMapper;
import com.erp.server.oms.service.DeliveryBoxRuleDetailService;
import com.erp.server.oms.service.DeliveryBoxRuleService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.servlet.http.HttpServletResponse;
import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-11-24
 */
@Slf4j
@Service
public class DeliveryBoxRuleServiceImpl extends SuperServiceImpl<DeliveryBoxRuleMapper, DeliveryBoxRuleEntity> implements DeliveryBoxRuleService {

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DeliveryBoxRuleDetailService deliveryBoxRuleDetailService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private FileFeign fileFeign;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DeliveryBoxRuleDTO.AddDTO addDTO) {
        DeliveryBoxRuleEntity deliveryBoxRuleEntity = new DeliveryBoxRuleEntity();
        BeanMapperUtils.copy(addDTO, deliveryBoxRuleEntity);

        // 数据处理
        handleAddData(deliveryBoxRuleEntity);

        log.info("开始新增");
        boolean save = super.save(deliveryBoxRuleEntity);
        if(!save) {
            throw new ServiceException("箱规保存失败");
        }

        Boolean saveDetail = deliveryBoxRuleDetailService.save(addDTO.getDeliveryBoxRuleDetailDTOList(), deliveryBoxRuleEntity.getId());
        if(!saveDetail) {
            throw new ServiceException("箱规明细保存失败");
        }

        // 操作日志
        for (DeliveryBoxRuleDetailDTO.AddDTO dto : addDTO.getDeliveryBoxRuleDetailDTOList()) {
            String msg = StrUtil.format("用户【{}】新增SKU【{}】,单箱数量【{}】", UserContext.getDefaultLoginUser().getUserName(), deliveryBoxRuleEntity.getSkuNo() , dto.getPerBoxQty());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_BOX_RULE.getCode(), deliveryBoxRuleEntity.getId(), "新增操作");
        }

        return new BaseResultDTO.AddDTO(deliveryBoxRuleEntity.getId(), deliveryBoxRuleEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DeliveryBoxRuleDTO.UpdateDTO addOrUpdateDTO) {
        DeliveryBoxRuleEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, ""));
        this.lambdaUpdate().set(DeliveryBoxRuleEntity::getUpdateUserId,UserContext.getDefaultLoginUser().getUid())
                .set(DeliveryBoxRuleEntity::getUpdateUserName,UserContext.getDefaultLoginUser().getUserName())
                .set(DeliveryBoxRuleEntity::getUpdateTime,LocalDateTime.now())
                .eq(DeliveryBoxRuleEntity::getId,addOrUpdateDTO.getId())
                .update();

        //只更新明细
        return deliveryBoxRuleDetailService.update(addOrUpdateDTO.getDeliveryBoxRuleDetailDTOList(),old.getId());

    }

    @Override
    public DeliveryBoxRuleDTO.ViewDTO view(String id) {
        DeliveryBoxRuleEntity deliveryBoxRuleEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到数据"));
        DeliveryBoxRuleDTO.ViewDTO data = BeanMapperUtils.map(DeliveryBoxRuleDTO.ViewDTO.class, deliveryBoxRuleEntity);
        List<DeliveryBoxRuleDetailEntity> detailList = deliveryBoxRuleDetailService.lambdaQuery()
                .eq(DeliveryBoxRuleDetailEntity::getMainId, id)
                .eq(DeliveryBoxRuleDetailEntity::getIsDeleted, Boolean.FALSE)
                .list();
        List<DeliveryBoxRuleDetailDTO.ViewDTO> dtoList = BeanMapperUtils.copyList(DeliveryBoxRuleDetailDTO.ViewDTO.class, detailList);
        fillViewList(dtoList);
        data.setDeliveryBoxRuleDetailDTOList(dtoList);
        return data;
    }

    public void fillViewList(List<DeliveryBoxRuleDetailDTO.ViewDTO> dtoList){
        for (DeliveryBoxRuleDetailDTO.ViewDTO detailDTO : dtoList) {
            detailDTO.setInvalidStatusName(detailDTO.getInvalidStatus().equals(InvalidStatusEnum.VOIDED.getStatus()) ? "已作废" : "未作废");
        }

    }

    @Override
    public PagingVO<DeliveryBoxRuleDTO.ListDTO> paging(PagingDTO<DeliveryBoxRuleDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<DeliveryBoxRuleDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<DeliveryBoxRuleDTO.ListDTO> list) {
        for (DeliveryBoxRuleDTO.ListDTO listDTO : list) {
            listDTO.setInvalidStatusName(listDTO.getInvalidStatus().equals(InvalidStatusEnum.VOIDED.getStatus()) ? "已作废" : "未作废");
        }
    }

    @Override
    public Boolean importFile(BaseDTO.ImportDTO dto,HttpServletResponse response) {
        //审核通过的sku
        List<SkuVO> skuVOList = plmTaskFeign.listApproveSku();

        if(StringUtils.isNotBlank(dto.getUserId())){
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(dto.getUserId());
            if(Objects.nonNull(findUserDTO)){
                LoginUser user = new LoginUser();
                user.setUid(findUserDTO.getUserId());
                user.setUserName(findUserDTO.getUserName());
                user.setRealName(findUserDTO.getRealName());
                user.setUserAccount(findUserDTO.getMobile());
                user.setMobile(findUserDTO.getMobile());
                UserContext.setLoginUser(user);
            }
        }

        DeliveryBoxRuleExcelListener excelListenerUtil = new DeliveryBoxRuleExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),skuVOList);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), DeliveryBoxRuleImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            List<DeliveryBoxRuleImportExcelDTO> errorList = excelListenerUtil.getErrorList();
            if (CollectionUtils.isEmpty(errorList)) {
                return true;
            }
            String fileName = "发货箱规错误信息";
            ExcelUtil.export(fileName, "task", errorList, DeliveryBoxRuleImportExcelDTO.class, response);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        return false;
    }

    public void importDeliveryBoxRule(BaseDTO.ImportDTO dto) {
        //审核通过的sku
        List<SkuVO> skuVOList = plmTaskFeign.listApproveSku();

        if(StringUtils.isNotBlank(dto.getUserId())){
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(dto.getUserId());
            if(Objects.nonNull(findUserDTO)){
                LoginUser user = new LoginUser();
                user.setUid(findUserDTO.getUserId());
                user.setUserName(findUserDTO.getUserName());
                user.setRealName(findUserDTO.getRealName());
                user.setUserAccount(findUserDTO.getMobile());
                user.setMobile(findUserDTO.getMobile());
                UserContext.setLoginUser(user);
            }
        }

        DeliveryBoxRuleExcelListener excelListenerUtil = new DeliveryBoxRuleExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount(),skuVOList);
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), DeliveryBoxRuleImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<DeliveryBoxRuleImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "发货箱规错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, DeliveryBoxRuleImportExcelDTO.class);
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

    @Override
    public void exportList(DeliveryBoxRuleDTO.ExportDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("导出发货箱规", EXPORT_OMS_DELIVERY_BOX_RULE.getCode(), dto);
    }

    @Override
    public List<DeliveryBoxRuleDTO.ListBoxRuleBySkuDTO> listBoxRuleBySku(List<DeliveryBoxRuleDTO.SkuDTO> skuList) {
        List<DeliveryBoxRuleDTO.ListBoxRuleBySkuDTO> viewDTOList = new ArrayList<>();

        // 批量查询SKU信息
        List<String> skuNoList = skuList.stream()
                .map(DeliveryBoxRuleDTO.SkuDTO::getSkuNo)
                .collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNoList);
        Map<String, SkuVO> skuInfoMap = skuVOList.stream()
                .collect(Collectors.toMap(SkuVO::getSkuNo, Function.identity()));

        for (DeliveryBoxRuleDTO.SkuDTO skuDTO : skuList) {
            SkuVO skuInfo = skuInfoMap.get(skuDTO.getSkuNo());
            if (skuInfo == null) {
                throw new ServiceException(ApiError.PRODUCT_NOT_FOUND_SKU, skuDTO.getSkuNo());
            }

            DeliveryBoxRuleEntity deliveryBoxRuleEntity = this.lambdaQuery()
                    .eq(DeliveryBoxRuleEntity::getSkuNo, skuDTO.getSkuNo())
                    .one();

            DeliveryBoxRuleDTO.ListBoxRuleBySkuDTO viewDTO = createDefaultViewDTO(skuInfo);
            List<DeliveryBoxRuleDetailDTO.ListBoxRuleBySkuDetailDTO> detailViews = new ArrayList<>();
            if (deliveryBoxRuleEntity != null) {
                BeanUtils.copyProperties(deliveryBoxRuleEntity, viewDTO);
                // 查询所有箱规明细
                List<DeliveryBoxRuleDetailEntity> detailEntities = deliveryBoxRuleDetailService.lambdaQuery()
                        .eq(DeliveryBoxRuleDetailEntity::getMainId, deliveryBoxRuleEntity.getId())
                        .list();

                detailViews.addAll(BeanMapperUtils.copyList(DeliveryBoxRuleDetailDTO.ListBoxRuleBySkuDetailDTO.class, detailEntities));

                if (!detailViews.isEmpty()) {
                    // 检查是否已经存在单箱数量为1的明细
                    boolean hasDefaultBox = detailViews.stream().anyMatch(d -> d.getPerBoxQty() != null && d.getPerBoxQty() == 1);

                    if (!hasDefaultBox) {
                        // 创建默认箱规明细（放在最后）
                        DeliveryBoxRuleDetailDTO.ListBoxRuleBySkuDetailDTO defaultDetail = new DeliveryBoxRuleDetailDTO.ListBoxRuleBySkuDetailDTO();
                        defaultDetail.setDeliverySkuId(deliveryBoxRuleEntity.getSkuId());
                        defaultDetail.setDeliverySkuNo(deliveryBoxRuleEntity.getSkuNo());
                        defaultDetail.setDeliveryProductName(deliveryBoxRuleEntity.getProductName());
                        defaultDetail.setPerBoxQty(1);

                        // 最大优先级+1
                        int maxSort = detailViews.stream()
                                .mapToInt(d -> d.getSort() == null ? 0 : d.getSort())
                                .max()
                                .orElse(0);
                        defaultDetail.setSort(maxSort + 1);

                        detailViews.add(defaultDetail);
                    }

                    // 对明细列表按sort正序排序
                    detailViews.sort(Comparator.comparingInt(
                            d -> d.getSort() == null ? 0 : d.getSort()
                    ));
                }
                viewDTO.setDeliveryBoxRuleDetailDTOList(detailViews);
            } else {
                // 如果没有有效明细，则创建一个单箱数量为1的箱规
                DeliveryBoxRuleDetailDTO.ListBoxRuleBySkuDetailDTO defaultDetail = new DeliveryBoxRuleDetailDTO.ListBoxRuleBySkuDetailDTO();
                defaultDetail.setDeliverySkuId(viewDTO.getSkuId());
                defaultDetail.setDeliverySkuNo(viewDTO.getSkuNo());
                defaultDetail.setDeliveryProductName(viewDTO.getProductName());
                defaultDetail.setPerBoxQty(1);
                defaultDetail.setSort(1);
                defaultDetail.setInvalidStatus(InvalidStatusEnum.NOT_VOIDED.getStatus());
                detailViews.add(defaultDetail);
                viewDTO.setDeliveryBoxRuleDetailDTOList(detailViews);
            }
            viewDTOList.add(viewDTO);
        }

        return viewDTOList;
    }

    /**
     * 创建默认视图DTO（当没有箱规时使用）
     */
    private DeliveryBoxRuleDTO.ListBoxRuleBySkuDTO createDefaultViewDTO(SkuVO skuInfo) {
        DeliveryBoxRuleDTO.ListBoxRuleBySkuDTO viewDTO = new DeliveryBoxRuleDTO.ListBoxRuleBySkuDTO();
        viewDTO.setSkuId(skuInfo.getSkuId());
        viewDTO.setSkuNo(skuInfo.getSkuNo());
        viewDTO.setProductName(skuInfo.getSkuName());
        viewDTO.setDeliveryBoxRuleDetailDTOList(new ArrayList<>());
        return viewDTO;
    }

    @Override
    @Transactional
    public void handleImportSuccessList(List<DeliveryBoxRuleDTO.ImportDTO> successList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        // 按 serialNumber 分组
        Map<String, List<DeliveryBoxRuleDTO.ImportDTO>> groupedBySerialNumber = successList.stream()
                .collect(Collectors.groupingBy(DeliveryBoxRuleDTO.ImportDTO::getSkuNo));

        try {
            for (Map.Entry<String, List<DeliveryBoxRuleDTO.ImportDTO>> entry : groupedBySerialNumber.entrySet()) {
                String serialNumber = entry.getKey();
                List<DeliveryBoxRuleDTO.ImportDTO> importDTOList = entry.getValue();

                if (CollectionUtils.isEmpty(importDTOList)) {
                    continue;
                }

                // 取第一个元素作为主表数据
                DeliveryBoxRuleDTO.ImportDTO firstImportDTO = importDTOList.get(0);
                DeliveryBoxRuleEntity deliveryBoxRuleEntity = this.lambdaQuery().eq(DeliveryBoxRuleEntity::getSkuNo, firstImportDTO.getSkuNo()).one();
                DeliveryBoxRuleEntity entity = new DeliveryBoxRuleEntity();
                if (Objects.isNull(deliveryBoxRuleEntity)) {
                    entity.setSkuId(firstImportDTO.getSkuId());
                    entity.setSkuNo(firstImportDTO.getSkuNo());
                    entity.setProductName(firstImportDTO.getProductName());

                    // 保存主表
                    boolean save = super.save(entity);
                    if (!save) {
                        throw new ServiceException("发货箱规导入保存失败");
                    }
                }

                // 处理明细数据
                List<DeliveryBoxRuleDetailEntity> deliveryBoxRuleDetailEntityList = new ArrayList<>();
                String deliveryBoxRuleId = Objects.isNull(deliveryBoxRuleEntity) ? entity.getId() : deliveryBoxRuleEntity.getId();

                for (DeliveryBoxRuleDTO.ImportDTO importDTO : importDTOList) {
                    List<DeliveryBoxRuleDetailDTO.DetailImportDTO> detailImportDTOList = importDTO.getDetailImportDTOList();
                    for (DeliveryBoxRuleDetailDTO.DetailImportDTO detailImportDTO : detailImportDTOList) {
                        DeliveryBoxRuleDetailEntity deliveryBoxRuleDetailEntity = new DeliveryBoxRuleDetailEntity();
                        BeanMapperUtils.copy(detailImportDTO, deliveryBoxRuleDetailEntity);
                        deliveryBoxRuleDetailEntity.setMainId(deliveryBoxRuleId); // 关联主表ID
                        deliveryBoxRuleDetailEntityList.add(deliveryBoxRuleDetailEntity);
                    }
                }

                // 查询旧数据
                List<DeliveryBoxRuleDetailEntity> oldList = deliveryBoxRuleDetailService.lambdaQuery()
                        .eq(DeliveryBoxRuleDetailEntity::getMainId, Objects.isNull(deliveryBoxRuleEntity) ? entity.getId() : deliveryBoxRuleEntity.getId())
                        .eq(DeliveryBoxRuleDetailEntity::getInvalidStatus,InvalidStatusEnum.NOT_VOIDED.getStatus())
                        .list();
                
                //需要重写equals，跳过了不需要修改的对象
                deliveryBoxRuleDetailEntityList.removeIf(oldList::contains);

                Map<String, DeliveryBoxRuleDetailEntity> oldMap = oldList.stream()
                        .collect(Collectors.toMap(DeliveryBoxRuleDetailEntity::getId, Function.identity()));

                List<DeliveryBoxRuleDetailEntity> addList = new ArrayList<>();
                List<DeliveryBoxRuleDetailEntity> updateList = new ArrayList<>();
                for (DeliveryBoxRuleDetailEntity deliveryBoxRuleDetailEntity : deliveryBoxRuleDetailEntityList) {
                    if (!oldList.isEmpty()) {
                        Set<String> deliverySkuNoSet = oldList.stream().map(item -> item.getDeliverySkuNo()).collect(Collectors.toSet());
                        if (deliverySkuNoSet.contains(deliveryBoxRuleDetailEntity.getDeliverySkuNo())) {
                            updateList.add(deliveryBoxRuleDetailEntity);
                        } else {
                            addList.add(deliveryBoxRuleDetailEntity);
                        }
                    } else {
                        addList.add(deliveryBoxRuleDetailEntity);
                    }
                }

                Iterator<Map.Entry<String, DeliveryBoxRuleDetailEntity>> iterator = oldMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, DeliveryBoxRuleDetailEntity> mapEntity = iterator.next();
                    DeliveryBoxRuleDetailEntity oldEntity = mapEntity.getValue();

                    List<Pair<String, String>> updatePairs = updateList.stream()
                            .filter(newEntity -> newEntity.getDeliverySkuId().equals(oldEntity.getDeliverySkuId()))
                            .map(newEntity -> new Pair<>(
                                    deliveryBoxRuleId,
                                    String.format(
                                            "修改发货SKU从【%s】为【%s】，单箱数量从【%s】为【%s】",
                                            oldEntity.getDeliverySkuNo(),
                                            newEntity.getDeliverySkuNo(),
                                            oldEntity.getPerBoxQty(),
                                            newEntity.getPerBoxQty()
                                    )
                            ))
                            .collect(Collectors.toList());

                    if (!updatePairs.isEmpty()) {
                        operateLogService.batchAddModuleOperateLog(
                                "%s",
                                ModuleTypeEnum.DELIVERY_BOX_RULE.getCode(),
                                updatePairs,
                                "编辑操作"
                        );
                    }
                }

                // 记录新增日志
                List<Pair<String, String>> addPairs = addList.stream()
                        .map(obj -> new Pair<>(deliveryBoxRuleId,
                                obj.getDeliverySkuNo() + "】" + ",单箱数量【" + obj.getPerBoxQty() + "】"
                        ))
                        .collect(Collectors.toList());
                operateLogService.batchAddModuleOperateLog(
                        "新增了发货SKU【%s",
                        ModuleTypeEnum.DELIVERY_BOX_RULE.getCode(),
                        addPairs,
                        "新增操作"
                );

                if (!addList.isEmpty()) {
                    //新增
                    for (DeliveryBoxRuleDetailEntity deliveryBoxRuleDetailEntity : addList) {
                        deliveryBoxRuleDetailEntity.setMainId(deliveryBoxRuleId);
                        deliveryBoxRuleDetailEntity.setInvalidStatus(InvalidStatusEnum.NOT_VOIDED.getStatus());
                    }

                    boolean addSuccess = deliveryBoxRuleDetailService.saveBatch(addList);

                    if (!addSuccess) {
                        throw new ServiceException(ApiError.WH_BOX_RULE_BATCH_UPDATE_FAILED);
                    }
                }

                if (!updateList.isEmpty()) {
                    //更新
                    for (DeliveryBoxRuleDetailEntity deliveryBoxRuleDetailEntity : updateList) {
                        boolean updateSuccess = deliveryBoxRuleDetailService.lambdaUpdate()
                                .set(DeliveryBoxRuleDetailEntity::getPerBoxQty,deliveryBoxRuleDetailEntity.getPerBoxQty())
                                .set(DeliveryBoxRuleDetailEntity::getSort,deliveryBoxRuleDetailEntity.getSort())
                                .eq(DeliveryBoxRuleDetailEntity::getDeliverySkuNo,deliveryBoxRuleDetailEntity.getDeliverySkuNo())
                                .eq(DeliveryBoxRuleDetailEntity::getInvalidStatus,InvalidStatusEnum.NOT_VOIDED.getStatus())
                                .update();

                        if (!updateSuccess) {
                            throw new ServiceException(ApiError.WH_BOX_RULE_BATCH_ADD_FAILED);
                        }
                    }
                }

            }
        } catch (Exception e) {
            throw new ServiceException("发货箱规导入失败", e);
        }
    }

    private boolean isDataChanged(DeliveryBoxRuleDetailEntity oldEntity, DeliveryBoxRuleDetailEntity newEntity) {
        return Objects.equals(oldEntity.getDeliverySkuNo(), newEntity.getDeliverySkuNo())
                && (!Objects.equals(oldEntity.getPerBoxQty(), newEntity.getPerBoxQty())
                || !Objects.equals(oldEntity.getSort(), newEntity.getSort()));
    }

    /**
    * 新增处理数据
    */
    private void handleAddData(DeliveryBoxRuleEntity deliveryBoxRuleEntity) {
        Integer count = this.lambdaQuery().eq(DeliveryBoxRuleEntity::getSkuId, deliveryBoxRuleEntity.getSkuId()).count();
        if (count > 0) {
            throw new ServiceException(ApiError.WH_BOX_RULE_SKU_EXISTS);
        }
    }

    /**
     * 新增处理数据
     */
    private void handleUpdateData(DeliveryBoxRuleEntity deliveryBoxRuleEntity) {

    }
}

package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.DynamicDataSourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.DynamicDataSourceThreadLocal;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.entity.B2bThirdDeliveryDetailEntity;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.B2BDeliveryPushTypeEnum;
import com.erp.model.wms.enums.ThirdDeliveryStatusEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.B2bThirdDeliveryConverter;
import com.erp.server.wms.mapper.B2bThirdDeliveryMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_B2B_THIRD_DELIVERY_REPORT;

/**
 * <p>
 * B2B三方发货单 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
 */
@Slf4j
@Service
public class B2bThirdDeliveryServiceImpl extends SuperServiceImpl<B2bThirdDeliveryMapper, B2bThirdDeliveryEntity> implements B2bThirdDeliveryService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private SoInfoFeign soInfoFeign;
    @Resource
    private B2bThirdDeliveryDetailService b2bThirdDeliveryDetailService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;
    @Resource
    private SyncB2bThirdWarehouseService syncB2bThirdWarehouseService;
    @Resource
    private VirtualInventoryTransCoreService virtualInventoryTransCoreService;
    @Resource
    private WmsAttachmentService wmsAttachmentService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(B2bThirdDeliveryDTO.AddDTO addDTO) {
        B2bThirdDeliveryEntity b2bThirdDeliveryEntity = new B2bThirdDeliveryEntity();
        BeanMapperUtils.copy(addDTO, b2bThirdDeliveryEntity);

        // 数据处理
        handleData(b2bThirdDeliveryEntity);

        log.info("开始新增B2B三方发货单");
        // 生成单号
        //此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SFFH);
        b2bThirdDeliveryEntity.setCode(code);
        boolean save = super.save(b2bThirdDeliveryEntity);
        if(!save) {
            throw new ServiceException("B2B三方发货单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2B三方发货单" , b2bThirdDeliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), b2bThirdDeliveryEntity.getId(), "新增操作");
        // 新增明细
        List<B2bThirdDeliveryDetailEntity> detailEntityList = b2bThirdDeliveryDetailService.batchAdd(b2bThirdDeliveryEntity.getId(), addDTO.getDetailList());
        //新增附件
        wmsAttachmentService.batchSave(addDTO.getAttachList(), ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(),b2bThirdDeliveryEntity.getId());
        //推送本地消息表
        syncB2bThirdWarehouseService.syncB2bThirdWarehouseCreate(b2bThirdDeliveryEntity, detailEntityList,ThirdDeliveryStatusEnum.CREATING.getCode());
        //冻结库存
        freezeVirtualInventory(b2bThirdDeliveryEntity, detailEntityList, Boolean.FALSE);
        return new BaseResultDTO.AddDTO(b2bThirdDeliveryEntity.getId(), code);
    }

    /**
     * 冻结虚拟库存
     * @author will
     * @date 2024/6/12 10:58
     * @param entity
     * @param detailEntityList
     * @param isRollback 是否回退冻结操作？
     */
    private void freezeVirtualInventory (B2bThirdDeliveryEntity entity, List<B2bThirdDeliveryDetailEntity> detailEntityList, Boolean isRollback) {
        List<VirtualInventoryStockDTO.OutInStockDTO> paramList = new ArrayList<>();
        for (B2bThirdDeliveryDetailEntity detailEntity : detailEntityList) {
            VirtualInventoryStockDTO.OutInStockDTO outInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
            outInStockDTO.setSourceType(InventorySourceTypeEnum.B2B_THIRD_DELIVERY);
            outInStockDTO.setSourceId(entity.getId());
            outInStockDTO.setSourceCode(entity.getCode());
            outInStockDTO.setSourceDetailId(detailEntity.getId());
            outInStockDTO.setBillDate(LocalDate.now());
            outInStockDTO.setSkuId(detailEntity.getDeliverySkuId());
            outInStockDTO.setSkuNo(detailEntity.getDeliverySkuNo());
            outInStockDTO.setQty(detailEntity.getBoxQty());
            outInStockDTO.setWarehouseId(entity.getDeliveryWarehouseId());
            if (CharSequenceUtil.isBlank(entity.getVirtualWarehouseId())) {
                continue;
            }
            outInStockDTO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
            paramList.add(outInStockDTO);
        }
        //无虚拟仓库不扣虚拟库存
        if (CollectionUtils.isEmpty(paramList)) {
            return;
        }
        //添加冻结库存
        VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
        dto.setParamList(paramList);
        if (isRollback){
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.B2B_THIRD_DELIVERY_ROLLBACK.getCode());
        }else {
            dto.setBusinessType(VirtualInventoryBusinessTypeEnum.B2B_THIRD_DELIVERY.getCode());
        }
        //更新库存
        virtualInventoryTransCoreService.approve(dto);
        String msg = StrUtil.format("【{}】单据单号为【{}】创建订单时冻结虚拟库存", "B2B三方发货单" , entity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), entity.getId(), "冻结虚拟库存");
    }
    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(B2bThirdDeliveryDTO.UpdateDTO addOrUpdateDTO) {
        B2bThirdDeliveryEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2B三方发货单"));
        if (!ThirdDeliveryStatusEnum.FAILED.getCode().equals(old.getStatus())){
            throw new ServiceException("只有创建失败允许编辑");
        }
        B2bThirdDeliveryEntity b2bThirdDeliveryEntity =  BeanMapperUtils.map(B2bThirdDeliveryEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(b2bThirdDeliveryEntity);
        log.info("编辑 开始修改B2B三方发货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(b2bThirdDeliveryEntity);
        if(!save) {
            throw new ServiceException("B2B三方发货单更新失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录B2B三方发货单日志数据，单号：【{}】", b2bThirdDeliveryEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), b2bThirdDeliveryEntity.getCode(), "B2B三方发货单");
        operateLogService.addModuleOperateLogByObj(old, b2bThirdDeliveryEntity, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), b2bThirdDeliveryEntity.getId(), msg);
        // 新增明细
        List<B2bThirdDeliveryDetailEntity> detailEntityList = b2bThirdDeliveryDetailService.batchAdd(b2bThirdDeliveryEntity.getId(), addOrUpdateDTO.getDetailList());
        //新增附件
        wmsAttachmentService.batchSave(addOrUpdateDTO.getAttachList(), ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(),b2bThirdDeliveryEntity.getId());
        //推送本地消息表
        syncB2bThirdWarehouseService.syncB2bThirdWarehouseCreate(b2bThirdDeliveryEntity, detailEntityList,ThirdDeliveryStatusEnum.CREATING.getCode());
        //冻结库存
        freezeVirtualInventory(b2bThirdDeliveryEntity, detailEntityList, Boolean.FALSE);
        return Boolean.TRUE;
    }

    @Override
    public List<B2bThirdDeliveryDTO.TabListDTO> tabList() {
        List<B2bThirdDeliveryDTO.TabListDTO> list = baseMapper.tabList();
        List<B2bThirdDeliveryDTO.TabListDTO> tabListDTOS = new ArrayList<>();
        for (ThirdDeliveryStatusEnum value : ThirdDeliveryStatusEnum.values()) {
            B2bThirdDeliveryDTO.TabListDTO tabListDTO = list.stream().filter(e -> e.getTabFlag().equals(value.getCode())).findFirst().orElse(null);
            if (Objects.nonNull(tabListDTO)){
                tabListDTO.setTabFlagName(ThirdDeliveryStatusEnum.getName(tabListDTO.getTabFlag()));
                tabListDTOS.add(tabListDTO);
            }else {
                tabListDTOS.add(B2bThirdDeliveryDTO.TabListDTO.builder().tabFlag(value.getCode()).tabFlagName(value.getName()).count(0).build());
            }
        }
        int sum = list.stream().mapToInt(B2bThirdDeliveryDTO.TabListDTO::getCount).sum();
        tabListDTOS.add(B2bThirdDeliveryDTO.TabListDTO.builder().tabFlag("all").tabFlagName("全部").count(sum).build());
        return tabListDTOS;
    }

    @Override
    public PagingVO<B2bThirdDeliveryDTO.PagingViewDTO> paging(PagingDTO<B2bThirdDeliveryDTO.PagingParamDTO> dto) {
        Page<B2bThirdDeliveryDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        DynamicDataSourceTypeEnum dynamicDataSourceTypeEnum = DynamicDataSourceThreadLocal.get();
        String dynamicDataSource = "";
        if(dynamicDataSourceTypeEnum != null) {
            dynamicDataSource = dynamicDataSourceTypeEnum.getCode();
        }
        dto.getParams().setDynamicDataSource(dynamicDataSource);
        IPage<B2bThirdDeliveryDTO.PagingViewDTO> pageData = baseMapper.paging(query, dto.getParams());
        List<B2bThirdDeliveryDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public void export(B2bThirdDeliveryDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("B2B三方发货单", EXPORT_WMS_B2B_THIRD_DELIVERY_REPORT.getCode(), dto);
    }

    @Override
    public B2bThirdDeliveryDTO.ViewDTO view(String id, String soId) {
        if (CharSequenceUtil.isBlank(id)){
            return soInfoFeign.getB2bThirdDeliveryView(soId);
        }else {
            B2bThirdDeliveryEntity entity = this.getById(id);
            if (Objects.isNull(entity)){
                throw new ServiceException(ApiError.NOT_EXIST,"B2B三方发货单");
            }
            List<B2bThirdDeliveryDetailEntity> detailEntityList = b2bThirdDeliveryDetailService.listByMainIds(Collections.singletonList(id));
            B2bThirdDeliveryDTO.ViewDTO viewDTO = B2bThirdDeliveryConverter.INSTANCE.toB2bThirdDeliveryViewDTO(entity, detailEntityList);
            viewDTO.setAttachList(wmsAttachmentService.getByBusinessIds(Collections.singletonList(id),ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode()));
            return viewDTO;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String id, String status, String errorMsg, String platformOrderCode, String remark, String trackNo) {
        B2bThirdDeliveryEntity old = this.getById(id);
        if (Objects.isNull(old)){
            log.warn("单据【{}】不存在", id);
            return;
        }
        if (status.equals(old.getStatus())){return;}
        this.lambdaUpdate().set(B2bThirdDeliveryEntity::getStatus,status)
                .set(CharSequenceUtil.isNotBlank(errorMsg), B2bThirdDeliveryEntity::getErrorMessage, errorMsg)
                .set(CharSequenceUtil.isNotBlank(platformOrderCode), B2bThirdDeliveryEntity::getPlatformOrderCode, platformOrderCode)
                .set(CharSequenceUtil.isNotBlank(remark), B2bThirdDeliveryEntity::getRemark, remark)
                .set(CharSequenceUtil.isNotBlank(trackNo), B2bThirdDeliveryEntity::getTrackNo, trackNo)
                .eq(B2bThirdDeliveryEntity::getId, id).update();
        String msg = CharSequenceUtil.format("用户【{}】更新了B2B三方发货单【{}】的状态由【{}】改为【{}】", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), ThirdDeliveryStatusEnum.getName(old.getStatus()), ThirdDeliveryStatusEnum.getName(status));
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), old.getId(), "更新操作");

        if (ThirdDeliveryStatusEnum.FAILED.getCode().equals(status) || ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(status)){

            List<B2bThirdDeliveryDetailEntity> detailEntityList = b2bThirdDeliveryDetailService.listByMainIds(Collections.singletonList(id));
            //冻结库存释放
            freezeVirtualInventory(old, detailEntityList, Boolean.TRUE);
            //已发货数据释放
            List<SoDetailDTO.UpdateDeliveryStatusDTO> paramList = new ArrayList<>(detailEntityList.size());
            detailEntityList.forEach(e -> {
                SoDetailDTO.UpdateDeliveryStatusDTO statusDTO = new SoDetailDTO.UpdateDeliveryStatusDTO();
                statusDTO.setId(e.getSoDetailId());
                statusDTO.setDeliveryQty(e.getDeliveryQty());
                paramList.add(statusDTO);
            });
            soInfoFeign.updateDeliveryStatus(paramList);
        }else if (ThirdDeliveryStatusEnum.SHIPPED.getCode().equals(status)){
            //生成销售出库单
            this.createSoOutstockByDeliveryId(id);
        }
    }

    /**
     * 异步生成销售出库单
     * @param id
     */
    @Async
    public void createSoOutstockByDeliveryId(String id) {
        //TODO 生成销售出库单
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO deliveryIntercept(String id, String remark) {
        B2bThirdDeliveryEntity entity = this.getById(id);
        if (Objects.isNull(entity)){
            throw new ServiceException(ApiError.NOT_EXIST,"B2B三方发货单");
        }
        //只有待发货允许发货拦截
        if (!ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode().equals(entity.getStatus())){
            throw new ServiceException(ApiError.ERROR_THIRD_DELIVERY_INTERCEPT);
        }
        if (entity.getIsApiDelivery()){
            //调三方仓
            syncB2bThirdWarehouseService.syncB2bThirdWarehouseCancel(entity,ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode());
            updateStatus(id, ThirdDeliveryStatusEnum.INTERCEPTING.getCode(), "", "",remark, "");
        }else {
            //直接拦截成功
            updateStatus(id, ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode(), "", "",remark, "");
            String msg = CharSequenceUtil.format("用户【{}】提交发货拦截申请成功,拦截成功", UserContext.getDefaultLoginUser().getUserName());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.B2B_THIRD_DELIVERY.getCode(), entity.getId(), "发货拦截");
        }
        return BatchResultDTO.success(id, entity.getCode(), "提交发货拦截成功");
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(B2bThirdDeliveryEntity b2bThirdDeliveryEntity) {
        if (CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getCountryName()) && CharSequenceUtil.isNotBlank(b2bThirdDeliveryEntity.getCountryId())){
            DictCountryEntity countryEntity = FeignQuery.getById(DictCountryEntity.class, b2bThirdDeliveryEntity.getCountryId());
            b2bThirdDeliveryEntity.setCountryName(Objects.nonNull(countryEntity) ? countryEntity.getNameCn() : "");
        }
        if (CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getDeliveryWarehouseName()) && CharSequenceUtil.isNotBlank(b2bThirdDeliveryEntity.getDeliveryWarehouseId())){
            WarehouseEntity warehouseEntity = FeignQuery.getById(WarehouseEntity.class, b2bThirdDeliveryEntity.getDeliveryWarehouseId());
            b2bThirdDeliveryEntity.setDeliveryWarehouseName(Objects.nonNull(warehouseEntity) ? warehouseEntity.getName() : "");
        }
        if (CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getWarehouseOrgName()) && CharSequenceUtil.isNotBlank(b2bThirdDeliveryEntity.getWarehouseOrgId())){
            List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Collections.singletonList(b2bThirdDeliveryEntity.getWarehouseOrgId()));
            b2bThirdDeliveryEntity.setWarehouseOrgName(CollUtil.isNotEmpty(accountingCompanyList) ? accountingCompanyList.get(0).getName() : "");
        }
        if (CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getLogisticsChannelName()) && CharSequenceUtil.isNotBlank(b2bThirdDeliveryEntity.getLogisticsChannelId())){
            LogisticsChannelEntity channelEntity = FeignQuery.getById(LogisticsChannelEntity.class, b2bThirdDeliveryEntity.getLogisticsChannelId());
            b2bThirdDeliveryEntity.setLogisticsChannelName(Objects.nonNull(channelEntity) ? channelEntity.getName() : "");
            b2bThirdDeliveryEntity.setLogisticsChannelCode(Objects.nonNull(channelEntity) ? channelEntity.getCode() : "");
        }
        if (CharSequenceUtil.isBlank(b2bThirdDeliveryEntity.getStatus())){
            b2bThirdDeliveryEntity.setStatus(ThirdDeliveryStatusEnum.CREATING.getCode());
        }
        //标识是否推送api仓库
        if (CharSequenceUtil.isNotBlank(b2bThirdDeliveryEntity.getDeliveryWarehouseId())){
            List<OverseasProviderWarehouseDTO.ViewDTO> viewDTOS = overseasProviderWarehouseService.listByWarehouseIdList(Collections.singletonList(b2bThirdDeliveryEntity.getDeliveryWarehouseId()));
            Boolean isApiDelivery = Boolean.FALSE;
            if (CollUtil.isNotEmpty(viewDTOS)){
                OverseasProviderWarehouseDTO.ViewDTO viewDTO = viewDTOS.stream().filter(e -> !e.getDisabled()).findFirst().orElse(null);
                isApiDelivery = Objects.nonNull(viewDTO) ? viewDTO.getIsB2BApiDelivery() : Boolean.FALSE;
                b2bThirdDeliveryEntity.setThirdWarehouseCode(Objects.nonNull(viewDTO) ? viewDTO.getPlatformWarehouseCode() : "");
            }
            b2bThirdDeliveryEntity.setIsApiDelivery(isApiDelivery);
        }
        //状态修改
        if (Objects.isNull(b2bThirdDeliveryEntity.getIsApiDelivery()) || !b2bThirdDeliveryEntity.getIsApiDelivery()){
            b2bThirdDeliveryEntity.setStatus(ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode());
            b2bThirdDeliveryEntity.setPushType(B2BDeliveryPushTypeEnum.MANUAL.getCode());
        }else {
            b2bThirdDeliveryEntity.setStatus(ThirdDeliveryStatusEnum.CREATING.getCode());
            b2bThirdDeliveryEntity.setPushType(B2BDeliveryPushTypeEnum.API.getCode());
        }
    }
}

package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.SupplierCategoryEnum;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.model.tms.dto.TransferLogisticsChannelDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.entity.TransferLogisticsAuthEntity;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.LogisticsAuthStatusEnum;
import com.erp.model.tms.enums.TransferLogisticsAuthStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.server.tms.convert.TransferLogisticsChannelConverter;
import com.erp.server.tms.convert.TransferLogisticsSupplierConverter;
import com.erp.server.tms.handler.TransferLogisticsRegistry;
import com.erp.server.tms.mapper.TransferLogisticsSupplierMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TRANSFER_LOGISTICS_SUPPLIER;

/**
 * <p>
 * 中转报关服务商表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@Service
public class TransferLogisticsSupplierServiceImpl extends SuperServiceImpl<TransferLogisticsSupplierMapper, TransferLogisticsSupplierEntity> implements TransferLogisticsSupplierService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private TransferLogisticsChannelService transferLogisticsChannelService;

    @Resource
    private TransferLogisticsAuthService transferLogisticsAuthService;

    @Resource
    private TransferLogisticsRegistry transferLogisticsRegistry;

    @Resource
    private TransferDeclareService transferDeclareService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    @Lazy
    private TransferLogisticsSupplierServiceImpl supplierService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferLogisticsSupplierDTO.AddDTO addDTO) {
        TransferLogisticsSupplierEntity logisticsSupplierEntity = new TransferLogisticsSupplierEntity();
        BeanMapperUtils.copy(addDTO, logisticsSupplierEntity);

        // 数据处理
        handleData(logisticsSupplierEntity);

        boolean save = super.save(logisticsSupplierEntity);
        if (!save) {
            throw new ServiceException("物流商保存失败");
        }
        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物理商单", logisticsSupplierEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TRANSFER_LOGISTICS_SUPPLIER.getCode(), logisticsSupplierEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(logisticsSupplierEntity.getId(), logisticsSupplierEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferLogisticsSupplierDTO.UpdateDTO updateDTO) {
        TransferLogisticsSupplierEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流商单"));
        TransferLogisticsSupplierEntity logisticsSupplierEntity = BeanMapperUtils.map(TransferLogisticsSupplierEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsSupplierEntity);
        log.info("编辑 开始修改物流商单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsSupplierEntity);
        if (!save) {
            throw new ServiceException("物流商单保存失败");
        }
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsSupplierEntity.getId(), "物理商单");
        operateLogService.addModuleOperateLogByObj(old, logisticsSupplierEntity, ModuleTypeEnum.TRANSFER_LOGISTICS_SUPPLIER.getCode(), logisticsSupplierEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<TransferLogisticsSupplierDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<TransferLogisticsSupplierDTO.TabListDTO> list = baseMapper.tabList(dto.getPermissionSql());
        List<DictBasicDTO.ViewDTO> typeList = dictBasicService.getByKey(DictBasicEnum.TRANSFER_LOGISTICS_AUTH_STATUS.getType());
        List<TransferLogisticsSupplierDTO.TabListDTO> resultList = new ArrayList<>(typeList.size());
        TransferLogisticsSupplierDTO.TabListDTO all = new TransferLogisticsSupplierDTO.TabListDTO();
        all.setTabFlagName("全部");
        all.setTabFlag("all");
        all.setCount(list.size());
        resultList.add(all);
        for (DictBasicDTO.ViewDTO item : typeList) {
            TransferLogisticsSupplierDTO.TabListDTO tab = new TransferLogisticsSupplierDTO.TabListDTO();
            String type = item.getCode();
            tab.setTabFlag(type);
            tab.setTabFlagName(item.getName());
            Integer count = list.stream().filter(l -> l.getTabFlag().equals(type)).
                    map(TransferLogisticsSupplierDTO.TabListDTO::getCount).findFirst().orElse(0);
            tab.setCount(count);
            resultList.add(tab);
        }
        return resultList;
    }

    @Override
    public PagingVO<TransferLogisticsSupplierDTO.PagingViewDTO> paging(PagingDTO<TransferLogisticsSupplierDTO.PagingParamDTO> dto) {
        TransferLogisticsSupplierDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<TransferLogisticsSupplierDTO.PagingViewDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<TransferLogisticsSupplierDTO.PagingViewDTO> pageData = baseMapper.paging(query, params);
        List<TransferLogisticsSupplierDTO.PagingViewDTO> list = pageData.getRecords();
        fillPagingData(list);
        return new PagingVO<>(pageData);
    }

    @Override
    public List<TransferLogisticsSupplierDTO.ChannelViewDTO> listChannelView(String id, String name) {
        TransferLogisticsSupplierEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流商"));
        //查询渠道信息
        List<TransferLogisticsChannelEntity> channelEntityList = transferLogisticsChannelService.listByMainIds(Arrays.asList(id));
        List<TransferLogisticsChannelDTO.ViewDTO> allChannelList = BeanMapperUtils.copyList(TransferLogisticsChannelDTO.ViewDTO.class, channelEntityList);

        //设置返回的渠道值
        List<TransferLogisticsSupplierDTO.ChannelViewDTO> viewList = new ArrayList<>(10);
        TransferLogisticsSupplierDTO.ChannelViewDTO channelView = new TransferLogisticsSupplierDTO.ChannelViewDTO();
        channelView.setChannelList(allChannelList);
        viewList.add(channelView);
        return viewList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        TransferLogisticsSupplierEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流商"));
        // 检查订单是否引用
        Boolean flag = transferDeclareService.checkExistTransferLogisticsSupplier(id);
        if (flag) {
            throw new ServiceException(ApiError.EXIST_TRANSFER_LOGISTICS_SUPPLIER_NOT_DELETE);
        }

        supplierService.removeById(id);

        //删除渠道根据来源id
        transferLogisticsChannelService.removeByMainIdList(Arrays.asList(id));
        if(Objects.isNull(entity)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }
        return BatchResultDTO.success(entity.getId(), entity.getSupplierName(), OperationTypeEnum.DELETE);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO sync(String id) {
        TransferLogisticsSupplierEntity logisticsSupplier = this.getById(id);
        if (Objects.isNull(logisticsSupplier)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "物流商");
        }
        String authStatus = logisticsSupplier.getAuthStatus();
        String alreadyCode = LogisticsAuthStatusEnum.ALREADY.getCode();
        if (!alreadyCode.equals(authStatus)) {
            throw new ServiceException(ApiError.NOT_SYNC_BY_NOT_AUTH);
        }
        TransferLogisticsAuthEntity authEntity = transferLogisticsAuthService.getByMainId("", id);
        if (Objects.isNull(authEntity)) {
            throw new ServiceException(ApiError.NOT_SYNC_BY_NOT_AUTH);
        }
        String logisticsPlatform = authEntity.getLogisticsPlatform();

        Integer totalSize = 0;
        TransferLogisticsService service = transferLogisticsRegistry.getHandler(logisticsPlatform);
        ApiResult<List<TransferLogisticsChannelEntity>> shippingMethodList = service.getShippingMethodList(authEntity.getId());
        if (shippingMethodList.isSuccess()) {
            shippingMethodList.getData().forEach(logisticsSaleChannelEntity -> {
                logisticsSaleChannelEntity.setMainId(id);
                transferLogisticsChannelService.saveOrUpdateChannel(logisticsSaleChannelEntity);
            });
            totalSize = shippingMethodList.getData().size();
        }

        return BatchResultDTO.success(logisticsSupplier.getId(), logisticsSupplier.getSupplierName(), "同步成功" + totalSize + "个渠道");
    }


    @Override
    public Boolean export(TransferLogisticsSupplierDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("中转报关服务商列表", EXPORT_TMS_TRANSFER_LOGISTICS_SUPPLIER.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<BaseDropDownDTO.DisabledDTO> listAll() {
        List<TransferLogisticsSupplierEntity> list = this.list();
        String already = TransferLogisticsAuthStatusEnum.ALREADY.getCode();
        for (TransferLogisticsSupplierEntity transferLogisticsSupplierEntity : list) {
            String authStatus = transferLogisticsSupplierEntity.getAuthStatus();
            if (!already.equals(authStatus)) {
                transferLogisticsSupplierEntity.setDisabled(Boolean.TRUE);
            }
        }
        List<BaseDropDownDTO.DisabledDTO> resultList = TransferLogisticsSupplierConverter.INSTANCE.convertBySupplierDown(list);
        return resultList;
    }

    @Override
    public List<BaseDropDownDTO.DisabledDTO> listAlreadyAll() {
        List<TransferLogisticsSupplierEntity> list = lambdaQuery().eq(TransferLogisticsSupplierEntity::getAuthStatus, TransferLogisticsAuthStatusEnum.ALREADY.getCode()).list();
        String already = TransferLogisticsAuthStatusEnum.ALREADY.getCode();
        for (TransferLogisticsSupplierEntity transferLogisticsSupplierEntity : list) {
            String authStatus = transferLogisticsSupplierEntity.getAuthStatus();
            if (!already.equals(authStatus)) {
                transferLogisticsSupplierEntity.setDisabled(Boolean.TRUE);
            }
        }
        List<BaseDropDownDTO.DisabledDTO> resultList = TransferLogisticsSupplierConverter.INSTANCE.convertBySupplierDown(list);
        return resultList;
    }

    @Override
    public Boolean updateDisabledBySupplierId(TransferLogisticsSupplierDTO.UpdateDisabledDTO dto) {
        return this.lambdaUpdate().eq(TransferLogisticsSupplierEntity::getSupplierId, dto.getSupplierId()).
                set(TransferLogisticsSupplierEntity::getDisabled, dto.getDisabled()).update();
    }

    @Override
    public List<BaseChildDTO.ListChildTreeDTO> tree() {
        List<TransferLogisticsSupplierEntity> dbList = this.list();
        List<BaseChildDTO.ListChildTreeDTO> list = TransferLogisticsSupplierConverter.INSTANCE.convertTree(dbList);
        List<TransferLogisticsChannelEntity> allChannelList = transferLogisticsChannelService.list();
        String already = TransferLogisticsAuthStatusEnum.ALREADY.getCode();

        for (BaseChildDTO.ListChildTreeDTO item : list) {
            String id = item.getId();
            TransferLogisticsSupplierEntity logisticsSupplier = dbList.stream().filter(d -> d.getId().equals(id)).findFirst().orElse(null);
            if (Objects.nonNull(logisticsSupplier)) {
                String authStatus = logisticsSupplier.getAuthStatus();
                if (!already.equals(authStatus)) {
                    item.setDisabled(Boolean.TRUE);
                }
            }
            List<TransferLogisticsChannelEntity> channelList = allChannelList.stream().
                    filter(c -> c.getMainId().equals(id)).sorted(Comparator.comparing(TransferLogisticsChannelEntity::getDisabled)).
                    collect(Collectors.toList());
            List<BaseChildDTO.ListChildTreeDTO> childrenList = TransferLogisticsChannelConverter.INSTANCE.convertTree(channelList);
            item.setChildren(childrenList);
        }
        //排序
        list = list.stream().sorted(Comparator.comparing(BaseChildDTO.ListChildTreeDTO::getDisabled)).collect(Collectors.toList());
        list.forEach(v->{
            v.setChildren(v.getChildren().stream().sorted(Comparator.comparing(BaseChildDTO.ListChildTreeDTO::getDisabled)).collect(Collectors.toList()));
        });
        return list;
    }


    @Override
    public List<TransferLogisticsSupplierDTO.AuthDTO> listAllAuth() {

        return baseMapper.listAllAuth();
    }

    @Override
    public List<TransferLogisticsSupplierDTO.AuthDTO> listAuthByMainIds(List<String> transferSupplierIdList) {
        if(CollectionUtils.isEmpty(transferSupplierIdList)){
            return Collections.emptyList();
        }
        return baseMapper.listAuthByMainIds(transferSupplierIdList);
    }

    @Override
    public List<BaseIdDTO.CodeDTO> listBySupplierId(String supplierId) {
        return baseMapper.listBySupplierId(supplierId);
    }

    @Override
    public PagingVO<TransferLogisticsSupplierDTO.PagingViewDTO> exportTransferLogisticsSupplier(PagingDTO<TransferLogisticsSupplierDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<TransferLogisticsSupplierDTO.PagingViewDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            fillPagingData(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    /**
     * 填充分页数据
     *
     * @param list
     */
    private void fillPagingData(List<TransferLogisticsSupplierDTO.PagingViewDTO> list) {
        for (TransferLogisticsSupplierDTO.PagingViewDTO item : list) {
            Boolean disabled = item.getDisabled();
            String disabledName = Objects.nonNull(disabled) && !disabled ? "启用" : "禁用";
            item.setDisabledName(disabledName);
            String authStatus = item.getAuthStatus();
            String authStatusName = TransferLogisticsAuthStatusEnum.getName(authStatus);
            item.setAuthStatusName(authStatusName);

            //获取服务商编号
            TransferLogisticsAuthEntity authEntity = transferLogisticsAuthService.getByMainId("", item.getId());
            if (ObjectUtil.isNotEmpty(authEntity)) {
                String logisticsPlatform = authEntity.getLogisticsPlatform();
                item.setLogisticsPlatform(logisticsPlatform);
            }
        }
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(TransferLogisticsSupplierEntity logisticsSupplierEntity) {
        String supplierId = logisticsSupplierEntity.getSupplierId();
        SupplierEntity supplier = scmTaskFeign.getSupplierById(supplierId);
        if (Objects.isNull(supplier)) {
            throw new ServiceException("供应商不存在");
        }
        //供应商分类
        List<String> supplierCategoryList = Arrays.asList(
                SupplierCategoryEnum.LOGISTICS.getCode(),
                SupplierCategoryEnum.SELF_LOGISTICS.getCode(),
                SupplierCategoryEnum.PLATFORM_LOGISTICS.getCode(),
                SupplierCategoryEnum.CUSTOMER_LOGISTICS.getCode(),
                SupplierCategoryEnum.WAREHOUSE_LOGISTICS.getCode(),
                SupplierCategoryEnum.OTHER_LOGISTICS.getCode()
        );
        List<DictBasicEntity> list = FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getStatus,Boolean.TRUE).eq(DictBasicEntity::getType, com.erp.model.scm.enums.DictBasicEnum.SUPPLIER_CATEGORY.getType()).in(DictBasicEntity::getValue, supplierCategoryList).list();
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("供应商分类不存在物流供应商");
        }
        List<String> categoryIdList = list.stream().map(DictBasicEntity::getId).distinct().collect(Collectors.toList());
        if (!categoryIdList.contains(supplier.getCategoryId())) {
            throw new ServiceException("供应商分类不为物流供应商");
        }
        logisticsSupplierEntity.setSupplierName(supplier.getName());

    }
}

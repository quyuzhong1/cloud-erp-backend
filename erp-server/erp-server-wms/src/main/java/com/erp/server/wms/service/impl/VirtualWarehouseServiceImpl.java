package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictPartitionEntity;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.VirtualWarehouseDTO;
import com.erp.model.wms.dto.VirtualWarehouseRelationDTO;
import com.erp.model.wms.entity.VirtualWarehouseChannelEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.entity.VirtualWarehouseRelationEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.VitualWarehouseChannelTypeEnum;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.OmsDropDownFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.wms.mapper.VirtualWarehouseMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_WAREHOUSE_REPORT;

/**
 * <p>
 * 虚拟仓 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-06-02
 */
@Slf4j
@Service
public class VirtualWarehouseServiceImpl extends SuperServiceImpl<VirtualWarehouseMapper, VirtualWarehouseEntity> implements VirtualWarehouseService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private VirtualWarehouseRelationService virtualWarehouseRelationService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private VirtualInventoryService virtualInventoryService;
    @Resource
    private VirtualWarehouseChannelService virtualWarehouseChannelService;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private OmsDropDownFeign omsDropDownFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private VirtualWarehouseChannelPartitionRefService  virtualWarehouseChannelPartitionRefService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(VirtualWarehouseDTO.AddDTO addDTO) {
        VirtualWarehouseEntity virtualWarehouseEntity = new VirtualWarehouseEntity();
        BeanMapperUtils.copy(addDTO, virtualWarehouseEntity);

        // 数据处理
        handleData(virtualWarehouseEntity, addDTO.getWarehouseIdList(), addDTO.getThirdMappingList());

        log.info("开始新增虚拟仓");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XNC);
        virtualWarehouseEntity.setCode(code);
        boolean save = super.save(virtualWarehouseEntity);
        if (!save) {
            throw new ServiceException("虚拟仓保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "虚拟仓", virtualWarehouseEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE.getCode(), virtualWarehouseEntity.getId(), "新增操作");
        //绑定信息
        //新增关联渠道
//        virtualWarehouseChannelService.batchAdd(bindChannel(addDTO.getChannelList(), virtualWarehouseEntity.getId()));
        //新增关联仓库
        virtualWarehouseRelationService.batchAdd(bindRelation(addDTO.getWarehouseIdList(), virtualWarehouseEntity.getId()));
        //新增关联外部仓
        dmpThirdMappingFeign.add(bindThirdMapping(addDTO.getThirdMappingList(), virtualWarehouseEntity));

        return new BaseResultDTO.AddDTO(virtualWarehouseEntity.getId(), code);
    }

    /**
     * 关联外部参日志
     * @author will
     * @date 2024/7/18 16:43
     * @param thirdMappingList
     * @param virtualWarehouseEntity
     */
    private void addThirdMappingOperateLog (List<ThirdMappingDTO.AddDTO> thirdMappingList,VirtualWarehouseEntity virtualWarehouseEntity) {
        //更新数据
        List<String> thirdIdList = thirdMappingList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getThirdId())).map(ThirdMappingDTO.AddDTO::getThirdId).distinct().collect(Collectors.toList());
        List<ThirdWarehouseEntity> thirdWarehouseList = CollectionUtils.isEmpty(thirdIdList) ?
                new ArrayList<>() : FeignQuery.create(ThirdWarehouseEntity.class).in(ThirdWarehouseEntity::getWarehouseId).list();

        //虚拟仓关联第三方信息
        List<ThirdMappingEntity> oldList = dmpThirdMappingFeign.getListBySysIds(Collections.singletonList(virtualWarehouseEntity.getId()));

        //修改前信息
        Boolean isChange = Boolean.FALSE;
        List<String> oldChannelMsg = oldList.stream().map(obj -> CharSequenceUtil.format("{}-{}", ThirdSysTypeEnum.getNameByCode(obj.getThirdSysType()), obj.getThirdName())).collect(Collectors.toList());
        //修改后信息
        List<String> newChannelMsg = new ArrayList<>();
        //不处理id为空的数据
        List<ThirdMappingDTO.AddDTO> thirdMappings = thirdMappingList.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getThirdId())).collect(Collectors.toList());
        for (ThirdMappingDTO.AddDTO addDTO : thirdMappings) {
            //拼接日志
            String thirdWarehouseName = thirdWarehouseList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), addDTO.getThirdId())).map(ThirdWarehouseEntity::getName).findFirst().orElse("");
            String msg = CharSequenceUtil.format("{}-{};",ThirdSysTypeEnum.getNameByCode(addDTO.getThirdSysType()),thirdWarehouseName);
            newChannelMsg.add(msg);
            if (!oldChannelMsg.contains(msg)) {
                isChange =  Boolean.TRUE;
            }
        }
        //size不一致或者有变更
        if (oldChannelMsg.size() != newChannelMsg.size() || isChange) {
            String msg = CharSequenceUtil.format("关联仓库：从【{}】修改为【{}】",StrUtil.join(";",oldChannelMsg),StrUtil.join(";",newChannelMsg));
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.VIRTUAL_WAREHOUSE.getCode(), virtualWarehouseEntity.getId(), "编辑信息");
        }
    }

    /**
     * 新增关联外部仓
     *
     * @param thirdMappingList
     * @return
     */
    private ThirdMappingDTO.AddDTO bindThirdMapping(List<ThirdMappingDTO.AddDTO> thirdMappingList, VirtualWarehouseEntity virtualWarehouseEntity) {
        ThirdMappingDTO.AddDTO addDTO = new ThirdMappingDTO.AddDTO();
        addDTO.setSysId(virtualWarehouseEntity.getId());
        addDTO.setSysName(virtualWarehouseEntity.getName());
        addDTO.setType(ThirdSysTypeEnum.VIRTUAL_WAREHOUSE.getCode());
        List<ThirdMappingDTO.ThirdAddDTO> thirdList = new ArrayList<>();
        List<ThirdMappingDTO.AddDTO> collect = thirdMappingList.stream().filter(item -> CharSequenceUtil.isNotBlank(item.getThirdId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            collect.forEach(thirdMapping -> {
                ThirdMappingDTO.ThirdAddDTO thirdAddDTO = new ThirdMappingDTO.ThirdAddDTO();
                thirdAddDTO.setSysType(PlatformDictEnum.WDT.getCode());
                thirdAddDTO.setThirdId(thirdMapping.getThirdId());
//                thirdAddDTO.setThirdName(virtualWarehouseEntity.getName());
//                thirdAddDTO.setThirdCode(virtualWarehouseEntity.getCode());
//                thirdAddDTO.setThirdInfoId(virtualWarehouseEntity.getId());
                thirdList.add(thirdAddDTO);
            });
        }
        addDTO.setThirdList(thirdList);
        return addDTO;
    }


    /**
     * 新增关联仓库
     *
     * @param warehouseIdList
     * @param virtualWarehouseEntityId
     * @return
     */
    private VirtualWarehouseRelationDTO.BatchAddDTO bindRelation(List<String> warehouseIdList, String virtualWarehouseEntityId) {
        VirtualWarehouseRelationDTO.BatchAddDTO batchAddDTO = new VirtualWarehouseRelationDTO.BatchAddDTO();
        batchAddDTO.setWarehouseIdList(warehouseIdList);
        batchAddDTO.setVirtualWarehouseId(virtualWarehouseEntityId);
        return batchAddDTO;
    }
    /**
     * 修改
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(VirtualWarehouseDTO.UpdateDTO updateDTO) {
        VirtualWarehouseEntity old = Optional.ofNullable(super.getById(updateDTO.getId())).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "虚拟仓"));
        VirtualWarehouseEntity virtualWarehouseEntity = BeanMapperUtils.map(VirtualWarehouseEntity.class, updateDTO);

        // 数据处理
        handleData(virtualWarehouseEntity, updateDTO.getWarehouseIdList(), updateDTO.getThirdMappingList());
        log.info("编辑 开始修改虚拟仓数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(virtualWarehouseEntity);
        if (!save) {
            throw new ServiceException("虚拟仓保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录虚拟仓日志数据，单号：【{}】", virtualWarehouseEntity.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "虚拟仓");
        operateLogService.addModuleOperateLogByObj(old, virtualWarehouseEntity, ModuleTypeEnum.VIRTUAL_WAREHOUSE.getCode(), virtualWarehouseEntity.getId(), msg);
        //新增关联仓库
        virtualWarehouseRelationService.batchAdd(bindRelation(updateDTO.getWarehouseIdList(), virtualWarehouseEntity.getId()));
        //外部仓日志
        addThirdMappingOperateLog(updateDTO.getThirdMappingList(),old);
        //新增关联外部仓
        dmpThirdMappingFeign.add(bindThirdMapping(updateDTO.getThirdMappingList(), virtualWarehouseEntity));

        return Boolean.TRUE;
    }

    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<VirtualWarehouseDTO.ListDTO> paging(PagingDTO<VirtualWarehouseDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualWarehouseDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        return new PagingVO(pageData);
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(VirtualWarehouseEntity virtualWarehouseEntity, List<String> warehouseIdList, List<ThirdMappingDTO.AddDTO> thirdMappingList) {
        //校验名称全局唯一
        VirtualWarehouseEntity existVm = baseMapper.selectOne(new LambdaQueryWrapper<VirtualWarehouseEntity>().eq(VirtualWarehouseEntity::getName, virtualWarehouseEntity.getName()));
        if (Objects.nonNull(existVm) && !Objects.equals(existVm.getId(), virtualWarehouseEntity.getId())) {
            throw new ServiceException(ApiError.ERROR_VMNAME_EXIST);
        }
        //校验实体仓绑定是否变更
        if (CharSequenceUtil.isNotBlank(virtualWarehouseEntity.getId())) {
            //获取原始绑定关系
            List<VirtualWarehouseRelationEntity> warehouseRelationList = virtualWarehouseRelationService.getByVirtualWarehouseId(virtualWarehouseEntity.getId());
            if (CollectionUtils.isNotEmpty(warehouseRelationList)) {
                VirtualWarehouseRelationEntity virtualWarehouseRelationEntity = warehouseRelationList.get(0);
                if (!Objects.equals(virtualWarehouseRelationEntity.getWarehouseId(), warehouseIdList.get(0))) {
                    //修改了虚拟仓实体仓绑定关系，校验库存
                    VirtualInventoryDTO.VirtualInventoryQtyDTO virtualInventoryQtyDTO = new VirtualInventoryDTO.VirtualInventoryQtyDTO();
                    virtualInventoryQtyDTO.setVirtualWarehouseId(virtualWarehouseRelationEntity.getVirtualWarehouseId());
                    virtualInventoryQtyDTO.setWarehouseId(virtualWarehouseRelationEntity.getWarehouseId());
                    Integer vwUsableQty = virtualInventoryService.findUsableQtyByQtyDto(virtualInventoryQtyDTO);
                    if (Objects.nonNull(vwUsableQty) && vwUsableQty > 0) {
                        WarehouseEntity warehouseEntity = warehouseService.getById(virtualWarehouseRelationEntity.getWarehouseId());
                        throw new ServiceException(ApiError.ERROR_VWWSTOCK_NOTEMPRY, warehouseEntity.getName());
                    }
                }
            }
        } else {
            virtualWarehouseEntity.setDisabled(false);
        }

        if (CollectionUtils.isNotEmpty(thirdMappingList)) {
            //校验关联外部仓
            checkDmpThirdMapping(virtualWarehouseEntity.getId(), virtualWarehouseEntity.getName(), thirdMappingList);
        }

    }

    private void checkDmpThirdMapping(String virtualWarehouseId, String virtualWarehouseName, List<ThirdMappingDTO.AddDTO> thirdMappingList) {
        ThirdMappingDTO.AddDTO addDTO = thirdMappingList.get(0);
        ThirdMappingDTO.ViewParamDTO viewParamDTO = new ThirdMappingDTO.ViewParamDTO();
        viewParamDTO.setType(ThirdSysTypeEnum.VIRTUAL_WAREHOUSE.getCode());
        viewParamDTO.setSysType(PlatformDictEnum.WDT.getCode());
        viewParamDTO.setThirdId(addDTO.getThirdId());
        List<ThirdMappingEntity> thirdList = dmpThirdMappingFeign.getByThirdId(viewParamDTO);
        if (CollectionUtils.isNotEmpty(thirdList)) {
            long count = thirdList.stream().filter(item -> !Objects.equals(item.getSysId(), virtualWarehouseId)).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_THIRD_VIRTUAL_WAREHOUSE_BINDED, thirdList.get(0).getThirdName(), thirdList.stream().map(ThirdMappingEntity::getSysName).collect(Collectors.joining()));
            }
        }
    }

    /**
     * 修改状态
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateState(VirtualWarehouseDTO.UpdateStateDTO updateStateDTO) {
        VirtualWarehouseEntity vwEntity = Optional.ofNullable(this.getById(updateStateDTO.getId())).orElseThrow(() -> new ServiceException(ApiError.ERROR_VIRTUAL_WAREHOUSE_NOT_EXIST));

        //获取原始状态
        Boolean dbDisabled = vwEntity.getDisabled();
        if (dbDisabled.equals(updateStateDTO.getDisabled())) {
            throw new ServiceException(ApiError.ERROR_SAME_DISABLED);
        }
        //如果原始启用状态变成禁用状态时需要校验虚拟仓库存
        if (Boolean.FALSE.equals(dbDisabled)) {
            //获取虚拟仓库存
            VirtualInventoryDTO.VirtualInventoryQtyDTO virtualInventoryQtyDTO = new VirtualInventoryDTO.VirtualInventoryQtyDTO();
            virtualInventoryQtyDTO.setVirtualWarehouseId(updateStateDTO.getId());
            Integer vmUsableQty = virtualInventoryService.findUsableQtyByQtyDto(virtualInventoryQtyDTO);
            if (Objects.nonNull(vmUsableQty) && vmUsableQty > 0) {
                throw new ServiceException(ApiError.ERROR_VWSTOCK_NOTEMPRY);
            }
            //禁用时如果绑定第三方仓，需要清除
            dmpThirdMappingFeign.add(bindThirdMapping(new ArrayList<>(), vwEntity));
        } else {
            //原始禁用状态变成启用时，校验当前虚拟仓绑定的渠道是否已被选择
            List<VirtualWarehouseChannelEntity> channelEntityList = virtualWarehouseChannelService.getByVirtualWarehouseId(vwEntity.getId());
            virtualWarehouseChannelService.checkBoundChannel(channelEntityList, Boolean.FALSE);
        }
        VirtualWarehouseEntity virtualWarehouseEntity = new VirtualWarehouseEntity();
        virtualWarehouseEntity.setId(updateStateDTO.getId());
        virtualWarehouseEntity.setDisabled(updateStateDTO.getDisabled());
        virtualWarehouseEntity.setName(vwEntity.getName());
        baseMapper.updateById(virtualWarehouseEntity);

        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), vwEntity.getCode(), "虚拟仓");
        operateLogService.addModuleOperateLogByObj(vwEntity, virtualWarehouseEntity, ModuleTypeEnum.VIRTUAL_WAREHOUSE.getCode(), virtualWarehouseEntity.getId(), msg);
        return Boolean.TRUE;
    }

    /**
     * 预览
     *
     * @param id
     * @return
     */
    @Override
    public VirtualWarehouseDTO.ViewDTO view(String id) {
        VirtualWarehouseEntity vmEntity = Optional.ofNullable(this.getById(id)).orElseThrow(() -> new ServiceException(ApiError.ERROR_VIRTUAL_WAREHOUSE_NOT_EXIST));
        VirtualWarehouseDTO.ViewDTO viewDTO = new VirtualWarehouseDTO.ViewDTO();
        BeanUtils.copyProperties(vmEntity, viewDTO);
        //获取关联仓库
        List<VirtualWarehouseRelationEntity> warehouseRelationList = virtualWarehouseRelationService.getByVirtualWarehouseId(id);
        viewDTO.setWarehouseIdList(warehouseRelationList.stream().map(VirtualWarehouseRelationEntity::getWarehouseId).collect(Collectors.toList()));
        if (CollUtil.isNotEmpty(viewDTO.getWarehouseIdList())){
            List<WarehouseEntity> warehouseEntityList = FeignQuery.getByIds(WarehouseEntity.class, viewDTO.getWarehouseIdList());
            viewDTO.setWarehouseNameList(warehouseEntityList.stream().map(WarehouseEntity::getName).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList()));
        }

        //获取关联外部仓
        ThirdMappingDTO.ViewParamDTO viewParamDTO = new ThirdMappingDTO.ViewParamDTO();
        viewParamDTO.setSysId(id);
        viewParamDTO.setType(ThirdSysTypeEnum.VIRTUAL_WAREHOUSE.getCode());
        ThirdMappingDTO.MappingViewDTO view = dmpThirdMappingFeign.view(viewParamDTO);
        if (Objects.nonNull(view)) {
            viewDTO.setThirdMappingList(view.getThirdList());
        }
        return viewDTO;
    }

    /**
     * 获取关联渠道
     *
     * @param key
     * @param id
     * @return
     */
    @Override
    public List<VirtualWarehouseDTO.Tree> tree(String key, String id) {
        List<BaseDropDownDTO.Tree> tree = omsDropDownFeign.tree(key);
        List<VirtualWarehouseDTO.Tree> trees = BeanMapperUtils.copyList(VirtualWarehouseDTO.Tree.class, tree);

        //获取当前已经绑定的所有渠道
        List<VirtualWarehouseDTO.BindChannelDto> bindedDictPlatform = virtualWarehouseChannelService.getBindedDictPlatform();
        if (CollectionUtils.isEmpty(bindedDictPlatform)) {
            return trees;
        }
        Map<String, List<VirtualWarehouseDTO.BindChannelDto>> allBindedMap = bindedDictPlatform.stream().collect(Collectors.groupingBy(VirtualWarehouseDTO.BindChannelDto::getDictPlatform));
        //获取当前虚拟仓绑定的渠道
        List<VirtualWarehouseChannelEntity> curVwChannelList = virtualWarehouseChannelService.getByVirtualWarehouseId(id);
        List<String> curChannelPlatformList = curVwChannelList.stream().map(VirtualWarehouseChannelEntity::getDictPlatform).collect(Collectors.toList());
        for (VirtualWarehouseDTO.Tree dictPlatform : trees) {
            List<VirtualWarehouseDTO.ChildTree> childTreeList = dictPlatform.getChildTreeList();
            if (CollectionUtils.isEmpty(childTreeList)) {
                continue;
            }
            childTreeList.forEach(childTree -> {
                List<VirtualWarehouseDTO.BindChannelDto> bindChannelDtos = allBindedMap.get(childTree.getCode());
                if (Objects.nonNull(bindChannelDtos)) {
                    String bindedType = bindChannelDtos.get(0).getType();
                    if (CollectionUtils.isNotEmpty(curVwChannelList)) {
                        if (curChannelPlatformList.contains(childTree.getCode())) {
                            if (Objects.equals(bindedType, VitualWarehouseChannelTypeEnum.PLATFORM.getCode())) {
                                //如果是本虚拟仓绑定平台，则当前行都可选
                                childTree.setDisabled(false);
                                childTree.setShopDisabled(false);
                                childTree.setPlatformDisabled(false);
                            } else {
                                //如果是本虚拟仓绑定店铺，判断是否有其他虚拟仓绑定当前渠道的店铺类型
                                List<VirtualWarehouseDTO.BindChannelDto> bindChannelDtoList = bindChannelDtos.stream()
                                        .filter(item -> !Objects.equals(item.getVirtualWarehouseId(), id)
                                                && Objects.equals(item.getType(), VitualWarehouseChannelTypeEnum.SHOP.getCode()))
                                        .collect(Collectors.toList());
                                if (CollectionUtils.isNotEmpty(bindChannelDtoList)) {
                                    childTree.setDisabled(false);
                                    childTree.setShopDisabled(false);
                                    childTree.setPlatformDisabled(true);
                                } else {
                                    childTree.setDisabled(false);
                                    childTree.setShopDisabled(false);
                                    childTree.setPlatformDisabled(false);
                                }
                            }
                        } else {
                            if (Objects.equals(bindedType, VitualWarehouseChannelTypeEnum.PLATFORM.getCode())) {
                                //如果是非本虚拟仓绑定需要设置为不可选
                                childTree.setDisabled(true);
                                childTree.setShopDisabled(true);
                                childTree.setPlatformDisabled(true);
                            } else {
                                //如果是非本虚拟仓绑定需要设置平台为不可选
                                childTree.setDisabled(false);
                                childTree.setShopDisabled(false);
                                childTree.setPlatformDisabled(true);
                            }
                        }
                    } else {
                        //如果是别的虚拟仓已绑定，如果是绑定平台，当前渠道不可选，如果是店铺则只禁用平台
                        if (Objects.equals(bindedType, VitualWarehouseChannelTypeEnum.PLATFORM.getCode())) {
                            childTree.setShopDisabled(true);
                            childTree.setPlatformDisabled(true);
                            childTree.setDisabled(true);
                        } else {
                            childTree.setShopDisabled(false);
                            childTree.setPlatformDisabled(true);
                            childTree.setDisabled(false);
                        }
                    }
                } else {
                    childTree.setDisabled(false);
                    childTree.setShopDisabled(false);
                    childTree.setPlatformDisabled(false);
                }
            });
        }
        return trees;
    }

    /**
     * 展示已选择的店铺
     *
     * @param dto
     * @return
     */
    @Override
    public PagingVO<ShopDTO.ListDTO> pagingSelect(PagingDTO<VirtualWarehouseDTO.ShopSelectDTO> dto) {
        //已有按平台，则不展示店铺下拉值
        Integer count = virtualWarehouseChannelService.lambdaQuery().eq(VirtualWarehouseChannelEntity::getDictPlatform, dto.getParams().getDictPlatform())
                .eq(VirtualWarehouseChannelEntity::getType, VitualWarehouseChannelTypeEnum.PLATFORM.getCode()).count();
        if (Objects.nonNull(count) && count > 0){
            return new PagingVO<>();
        }
        PagingDTO<ShopDTO.SelectDTO> shopDto = new PagingDTO<>();
        shopDto.setPageSize(dto.getPageSize());
        shopDto.setCurrPage(dto.getCurrPage());
        ShopDTO.SelectDTO selectDTO = new ShopDTO.SelectDTO();
        BeanUtils.copyProperties(dto.getParams(), selectDTO);
        shopDto.setParams(selectDTO);
        PagingVO<ShopDTO.ListDTO> pagingSelect = shopInfoFeign.pagingSelect(shopDto);
        if (CollectionUtils.isEmpty(pagingSelect.getList())) {
            return new PagingVO<>();
        }
        //获取当前平台下绑定的店铺
        List<String> bindedShopList = virtualWarehouseChannelService.getBindedShopByDictPlatform(dto.getParams().getDictPlatform());
        if (CollectionUtils.isEmpty(bindedShopList)) {
            return pagingSelect;
        }
        //获取当前虚拟仓绑定的店铺
        List<VirtualWarehouseChannelEntity> warehouseChannelEntities = virtualWarehouseChannelService.getByVirtualWarehouseId(dto.getParams().getId()).stream()
                .filter(item -> Objects.equals(item.getType(), VitualWarehouseChannelTypeEnum.SHOP.getCode())).collect(Collectors.toList());
        List<String> shopIds = warehouseChannelEntities.stream().map(VirtualWarehouseChannelEntity::getRelationId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        pagingSelect.getList().forEach(shop -> {
            if (bindedShopList.contains(((ShopDTO.ListDTO) shop).getId())) {
                if (CollectionUtils.isNotEmpty(warehouseChannelEntities) && CollectionUtils.isNotEmpty(shopIds) && shopIds.contains(((ShopDTO.ListDTO) shop).getId())) {
                    ((ShopDTO.ListDTO) shop).setDisabled(false);
                } else {
                    ((ShopDTO.ListDTO) shop).setDisabled(true);
                }
            } else {
                ((ShopDTO.ListDTO) shop).setDisabled(false);
            }
        });
        return pagingSelect;
    }

    /**
     * 根据名称查询
     *
     * @param nameList
     * @return
     */
    @Override
    public List<VirtualWarehouseDTO.VwDTO> getByNames(List<String> nameList) {
        return baseMapper.getByNames(nameList);
    }

    @Override
    public PagingVO<VirtualWarehouseDTO.SelectDTO> warehousePagingSelect(PagingDTO<VirtualWarehouseDTO.WarehouseSelectDTO> dto) {
        IPage<VirtualWarehouseDTO.SelectDTO> pageData = this.baseMapper.warehousePagingSelect(dto);
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        return new PagingVO(pageData);
    }

    @Override
    public List<VirtualWarehouseDTO.SelectDTO> warehouseSelectList(PagingDTO<VirtualWarehouseDTO.WarehouseSelectDTO> dto) {
        return baseMapper.warehouseSelectList(dto);
    }

    @Override
    public List<VirtualWarehouseDTO.SelectDTO> listByParam(VirtualWarehouseDTO.SearchDTO searchDTO) {
        if(CharSequenceUtil.isBlank(searchDTO.getDictPlatform()) && CharSequenceUtil.isNotBlank(searchDTO.getRelationId())){
            ShopInfoEntity shopInfoEntity = FeignQuery.getById(ShopInfoEntity.class,searchDTO.getRelationId());
            if(Objects.nonNull(shopInfoEntity)){
                searchDTO.setDictPlatform(shopInfoEntity.getDictPlatform());
            }else{
                throw new ServiceException("通过关联id查询平台为空");
            }
        }
        return baseMapper.listByParam(searchDTO);
    }

    @Override
    public List<String> listWarehouseBySql(String compareCodeSplicingValueSql) {
        return baseMapper.listWarehouseBySql(compareCodeSplicingValueSql);
    }

    @Override
    public Boolean exportExcel(VirtualWarehouseDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("虚拟仓库设置导出", EXPORT_WMS_VIRTUAL_WAREHOUSE_REPORT.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<VirtualWarehouseDTO.ExportDTO> exportVirtualWarehouse(PagingDTO<VirtualWarehouseDTO.PagingParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualWarehouseDTO.ExportDTO> pageData = this.baseMapper.exportVirtualWarehouse(query, dto.getParams());
        handleExport(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public List<VirtualWarehouseDTO.ViewWarehouseDTO> listWarehouseInfoByIds(List<String> virtualWarehouseIdList) {
        if (CollUtil.isEmpty(virtualWarehouseIdList)){
            return Collections.emptyList();
        }
        return baseMapper.listWarehouseInfoByIds(virtualWarehouseIdList);
    }

    @Override
    public List<VirtualWarehouseEntity> listByNameList(List<String> virtualWarehouseNameList) {
        if (CollUtil.isEmpty(virtualWarehouseNameList)){
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(VirtualWarehouseEntity::getName, virtualWarehouseNameList).list();
    }

    /**
     * 导出数据处理
     * @param list
     */
    private void handleExport (List<VirtualWarehouseDTO.ExportDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        //店铺信息
        List<String> shopIdList = list.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getShopId())).map(VirtualWarehouseDTO.ExportDTO::getShopId).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopList = FeignQuery.getByIds(ShopInfoEntity.class, shopIdList);

        //平台信息
        List<String> platformList = list.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getDictPlatform())).map(VirtualWarehouseDTO.ExportDTO::getDictPlatform).distinct().collect(Collectors.toList());
        List<DictBasicEntity> dictPlatformList = FeignQuery.create(DictBasicEntity.class).in(DictBasicEntity::getValue,platformList).list();

        //区域信息
        List<String> partitionIdList = list.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getPartitionId())).map(VirtualWarehouseDTO.ExportDTO::getPartitionId).distinct().collect(Collectors.toList());
        List<DictPartitionEntity> partitionList = FeignQuery.getByIds(DictPartitionEntity.class,partitionIdList);

        //虚拟仓关联第三方信息
        List<String> virtualWarehouseIdList = list.stream().map(VirtualWarehouseDTO.ExportDTO::getId).distinct().collect(Collectors.toList());
        List<ThirdMappingEntity> thirdMappingList = dmpThirdMappingFeign.getListBySysIds(virtualWarehouseIdList);

        for (VirtualWarehouseDTO.ExportDTO exportDTO : list) {
            exportDTO.setDisabledStr(exportDTO.getDisabled() ? "禁用" : "启用");

            //平台名称
            DictBasicEntity dictPlatform = dictPlatformList.stream().filter(obj -> CharSequenceUtil.equals(obj.getValue(), exportDTO.getDictPlatform())).findFirst().orElse(new DictBasicEntity());
            String dictPlatformName = CharSequenceUtil.equals(exportDTO.getType(), VitualWarehouseChannelTypeEnum.PLATFORM.getCode()) && CharSequenceUtil.isBlank(exportDTO.getDictPlatform()) ? "全部" : dictPlatform.getName();
            exportDTO.setDictPlatformName(dictPlatformName);
            exportDTO.setTypeName(DictBasicTypeEnum.getName(dictPlatform.getSubType()));

            //店铺名称
            String shopName = shopList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), exportDTO.getShopId())).map(ShopInfoEntity::getName).findFirst().orElse("");
            shopName = CharSequenceUtil.isNotBlank(dictPlatformName) && CharSequenceUtil.isBlank(exportDTO.getShopId()) ? "全部" : shopName;
            exportDTO.setShopName(shopName);


            //区域名称
            String partitionName = partitionList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), exportDTO.getPartitionId())).map(DictPartitionEntity::getName).findFirst().orElse("");
            partitionName = CharSequenceUtil.isNotBlank(dictPlatformName) && CharSequenceUtil.isBlank(exportDTO.getPartitionId()) ? "全部" : partitionName;
            exportDTO.setPartitionName( partitionName);

            //虚拟仓关联仓库名称
            ThirdMappingEntity thirdMappingEntity = thirdMappingList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSysId(), exportDTO.getId())).findFirst().orElse(new ThirdMappingEntity());
            exportDTO.setOutSideVirtualWarehouseName(thirdMappingEntity.getThirdName());
            exportDTO.setOutSidePlatformName(EnumMessage.getNameByCode(PlatformDictEnum.class, thirdMappingEntity.getThirdSysType()));
        }
    }
}

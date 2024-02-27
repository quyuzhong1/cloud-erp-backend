package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.*;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseMappingDTO;
import com.erp.model.wms.dto.excel.WarehouseExcelDTO;
import com.erp.model.wms.dto.excel.WarehouseExportExcelDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.entity.WarehouseMappingEntity;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.WmsRedisKeyEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.kingdee.SyncKingdeeWarehouseService;
import com.erp.server.wms.listener.WarehouseExcelListener;
import com.erp.server.wms.mapper.WarehouseMapper;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 仓库表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
@Service
@Slf4j
public class WarehouseServiceImpl extends SuperServiceImpl<WarehouseMapper, WarehouseEntity> implements WarehouseService {

    @Resource
    private DictBasicService dictBasicService;


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SyncKingdeeWarehouseService syncKingdeeWarehouseService;

    @Autowired
    private RedisService redisService;
    @Resource
    private WarehouseLocationService warehouseLocationService;


    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private OverseasProviderService overseasProviderService;

    @Resource
    private WarehouseMappingService warehouseMappingService;


    @Override
    public List<WarehouseDTO.UpdateDTO> listWarehouseByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<WarehouseEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return BeanMapperUtils.copyList(WarehouseDTO.UpdateDTO.class, list);
    }

    @Override
    public List<WarehouseDTO.ListDTO> listApproveWarehouse() {
        List<WarehouseEntity> list = this.list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<WarehouseDTO.ListDTO> resultList = BeanMapperUtils.copyList(WarehouseDTO.ListDTO.class, list);

        // 查询仓库关联服务商
        Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap = overseasProviderService.mapByWarehouseIds();
        // 填充信息
        this.fillListData(resultList, warehouseBindMap);

        return resultList.stream().sorted(Comparator.comparing(WarehouseDTO.ListDTO::getDisabled)).collect(Collectors.toList());
    }

    /**
     * 填充列表信息
     */
    private void fillListData(List<WarehouseDTO.ListDTO> resultList, Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap) {
        List<ApproveStatusEnum> statusList = Collections.singletonList(ApproveStatusEnum.APPROVE);

        List<String> orgIds = resultList.stream().map(WarehouseDTO.ListDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);

        for (WarehouseDTO.ListDTO listDTO : resultList) {
            // 组织信息
            String orgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(listDTO.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
            listDTO.setOrgName(orgName);
            if (!statusList.contains(listDTO.getApproveStatus())) {
                listDTO.setDisabled(true);
            }
            //平台信息
            OmsPlatformEnum platformEnum = this.checkAndGetPlatformInfo(listDTO, warehouseBindMap);
            listDTO.setDictPlatform(null == platformEnum ? "" : platformEnum.getCode());
            listDTO.setPlatformName(null == platformEnum ? "" : platformEnum.getName());
        }
    }

    @Override
    public List<WarehouseDTO.ListTreeDTO> listTree() {
        List<WarehouseDTO.ListDTO> list = listApproveWarehouse();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<WarehouseDTO.ListTreeDTO> resultList = handleWarehouseTree(list);
        return resultList;
    }

    @Override
    public List<WarehouseDTO.ListTreeDTO> listTreeByParams(WarehouseDTO.ListParamDTO dto) {
        List<WarehouseDTO.ListDTO> list = listWarehouseByParams(dto);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<WarehouseDTO.ListTreeDTO> resultList = handleWarehouseTree(list);
        return resultList;
    }

    @Override
    public void assertDisabled(List<WarehouseDTO.WarehouseDisabledAssertDTO> assertList) {
        if (CollectionUtil.isEmpty(assertList)) {
            return;
        }
        List<WarehouseDTO.WarehouseDisabledAssertDTO> invalidList = new ArrayList<>();
        assertList.stream().forEach(item -> {
            // 仓库禁用/未审核
            if (StrUtil.isBlank(item.getWarehouseId())) {
                throw new ServiceException("仓库id不能为空");
            }
            WarehouseDTO.UpdateDTO warehouse = detailWithCache(item.getWarehouseId());
            if (ObjectUtil.isNotEmpty(warehouse)) {
                item.setWarehouseName(item.getWarehouseName());
            }
            if (ObjectUtil.isNotEmpty(warehouse) && (warehouse.getDisabled() || !ApproveStatusEnum.APPROVE.getStatus().equals(warehouse.getApproveStatusCode()))) {
                invalidList.add(item);
                return;
            }
            // 库区禁用/未审核
            if (StrUtil.isNotBlank(item.getWarehouseArea())) {
                WarehouseLocationEntity area = warehouseLocationService.findArea(item.getWarehouseId(), item.getWarehouseArea());
                if (ObjectUtil.isEmpty(area) || area.getDisabled()) {
                    invalidList.add(item);
                    return;
                }
            }
            // 仓位禁用/未审核
            if (StrUtil.isNotBlank(item.getWarehouseLocation())) {
                WarehouseLocationEntity location = warehouseLocationService.findByWarehouseIdAndCode(item.getWarehouseId(), item.getWarehouseLocation());
                if (ObjectUtil.isEmpty(location) || location.getDisabled()) {
                    invalidList.add(item);
                }
            }
        });
        if (CollectionUtil.isNotEmpty(invalidList)) {
            List<String> warehouseNameList = invalidList.stream().map(WarehouseDTO.WarehouseDisabledAssertDTO::getWarehouseName).distinct().collect(Collectors.toList());
            List<String> warehoseAreaList = invalidList.stream().map(WarehouseDTO.WarehouseDisabledAssertDTO::getWarehouseArea).distinct().collect(Collectors.toList());
            List<String> warehosueLocationList = invalidList.stream().map(WarehouseDTO.WarehouseDisabledAssertDTO::getWarehouseArea).distinct().collect(Collectors.toList());
            warehouseNameList = CollectionUtil.isNotEmpty(warehouseNameList) ? warehouseNameList : Collections.EMPTY_LIST;
            warehoseAreaList = CollectionUtil.isNotEmpty(warehoseAreaList) ? warehoseAreaList : Collections.EMPTY_LIST;
            warehosueLocationList = CollectionUtil.isNotEmpty(warehosueLocationList) ? warehosueLocationList : Collections.EMPTY_LIST;
            throw new ServiceException(ApiError.WAREHOUSE_AREA_LOCATION_DISABLED, JSONUtil.toJsonStr(warehouseNameList), JSONUtil.toJsonStr(warehoseAreaList), JSONUtil.toJsonStr(warehosueLocationList));
        }
    }

    @Override
    public List<WarehouseDTO.ListDTO> listOverseasWarehouse() {
        // 查询仓库关联服务商
        Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap = overseasProviderService.mapByWarehouseIds();
        if (warehouseBindMap.isEmpty()) {
            return Collections.emptyList();
        }
        // 查询对应仓库
        Set<String> ids = warehouseBindMap.keySet();
        List<WarehouseEntity> list = lambdaQuery()
                .in(WarehouseEntity::getId, ids)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<WarehouseDTO.ListDTO> resultList = BeanMapperUtils.copyList(WarehouseDTO.ListDTO.class, list);
        // 填充其他信息
        this.fillListData(resultList, warehouseBindMap);

        return resultList.stream()
                .sorted(Comparator.comparing(WarehouseDTO.ListDTO::getDisabled))
                .collect(Collectors.toList());


    }

    /**
     * 检查绑定海外仓库服务商信息
     */
    private OmsPlatformEnum checkAndGetPlatformInfo(WarehouseDTO.ListDTO listDTO, Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap) {
        List<OverseasProviderDTO.ListWithWarehouseDTO> listWithWarehouseDTOS = warehouseBindMap.get(listDTO.getId());
        if (CollectionUtils.isNotEmpty(listWithWarehouseDTOS)) {
            // 取指定服务商，否则取第一个
            OverseasProviderDTO.ListWithWarehouseDTO warehouseDTO = listWithWarehouseDTOS.stream().findFirst().orElse(null);
            // 设置平台信息
            if (null != warehouseDTO) {
                return OmsPlatformEnum.getByCode(warehouseDTO.getCode());
            }
        }
        return null;
    }


    /**
     * 添加仓库
     *
     * @param dto
     * @return com.erp.model.wms.entity.WarehouseEntity
     * @author yl
     * @date 2023-03-22 10:17
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String add(WarehouseDTO.AddDTO dto) {
        //检查名称
        checkName(null, dto.getName());
        //检查金蝶编号
        checkKingdeeWarehouseCode("", dto.getKingdeeWarehouseCode());
        WarehouseEntity warehouse = new WarehouseEntity();
        BeanMapper.copy(dto, warehouse);

        //如果设置了在途仓，获取匹配在途仓名称
        if (StringUtils.isNotBlank(dto.getOnwayWarehouseId())) {
            WarehouseEntity entity = this.getById(dto.getOnwayWarehouseId());
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_ONWAY_WAREHOUSE_NOT_EXIST);
            }
            warehouse.setOnwayWarehouseName(entity.getName());
        }

        Boolean result = this.save(warehouse);
        if (result) {

            //如果设置了第三方仓绑定
            if (StringUtils.isNotBlank(dto.getThirdWarehouseName())) {
                WarehouseMappingEntity checkThirdWarehouseNameExist = warehouseMappingService.checkThirdWarehouseNameExist(dto.getThirdWarehouseName(), PlatformDictEnum.ALI_EXPRESS.getCode());
                if (ObjectUtil.isNotEmpty(checkThirdWarehouseNameExist)) {
                    WarehouseEntity entity = this.getById(checkThirdWarehouseNameExist.getWarehouseId());
                    throw new ServiceException(ApiError.THIRD_WAREHOUSE_NAME_EXIST, PlatformDictEnum.ALI_EXPRESS.getCode(), dto.getThirdWarehouseName(), entity.getName());
                }
                WarehouseMappingDTO.AddDTO addDTO = new WarehouseMappingDTO.AddDTO();
                addDTO.setName(dto.getThirdWarehouseName());
                addDTO.setWarehouseId(warehouse.getId());
                addDTO.setDictPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
                warehouseMappingService.add(addDTO);
            }

            return warehouse.getId();
        }
        return "";
    }


    /**
     * 修改仓库
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:08
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateWarehouse(WarehouseDTO.UpdateDTO dto) {
        // 删除缓存
        removeCache(Collections.singletonList(dto.getId()));
        //仓库id
        String warehouseId = dto.getId();
        WarehouseEntity warehouse = this.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99001);
        }
        String code = dto.getKingdeeWarehouseCode();
        String name = dto.getName();
        checkName(warehouseId, name);
        checkKingdeeWarehouseCode(warehouseId, code);
        BeanMapper.copy(dto, warehouse);

        //如果设置了在途仓，获取匹配在途仓名称
        if (StringUtils.isNotBlank(dto.getOnwayWarehouseId())) {
            WarehouseEntity entity = this.getById(dto.getOnwayWarehouseId());
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_ONWAY_WAREHOUSE_NOT_EXIST);
            }
            warehouse.setOnwayWarehouseName(entity.getName());
        }

        Boolean result = this.updateById(warehouse);
        if (result) {
            //如果设置了第三方仓绑定
            WarehouseMappingDTO.MappingViewDTO mappingViewByDictPlatform = warehouseMappingService.getMappingViewByDictPlatform(warehouseId, PlatformDictEnum.ALI_EXPRESS.getCode());

            WarehouseMappingEntity checkThirdWarehouseNameExist = warehouseMappingService.checkThirdWarehouseNameExist(dto.getThirdWarehouseName(), PlatformDictEnum.ALI_EXPRESS.getCode());

            WarehouseMappingDTO.UpdateDTO updateDTO = new WarehouseMappingDTO.UpdateDTO();
            if (ObjectUtil.isNotEmpty(mappingViewByDictPlatform)) {
                updateDTO.setId(mappingViewByDictPlatform.getId());
                if (ObjectUtil.isNotEmpty(checkThirdWarehouseNameExist) && !checkThirdWarehouseNameExist.getId().equals(mappingViewByDictPlatform.getId())) {
                    throw new ServiceException(ApiError.THIRD_WAREHOUSE_NAME_EXIST, PlatformDictEnum.ALI_EXPRESS.getCode(), dto.getThirdWarehouseName());
                }
            } else {
                if (ObjectUtil.isNotEmpty(checkThirdWarehouseNameExist)) {
                    throw new ServiceException(ApiError.THIRD_WAREHOUSE_NAME_EXIST, PlatformDictEnum.ALI_EXPRESS.getCode(), dto.getThirdWarehouseName());
                }
            }
            updateDTO.setName(dto.getThirdWarehouseName());
            updateDTO.setWarehouseId(warehouse.getId());
            updateDTO.setDictPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
            warehouseMappingService.update(updateDTO);
            return warehouseId;
        }
        return "";
    }


    /**
     * 提交并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:16
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAndSubmit(WarehouseDTO.AddDTO dto) {
        String warehouseId = this.add(dto);
        if (StringUtils.isBlank(warehouseId)) {
            throw new ServiceException(ApiError.ERROR_1019);
        }
        Boolean result = this.submit(Arrays.asList(warehouseId));
        return result;
    }


    /**
     * 仓库提交审核
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:31
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        // 删除缓存
        removeCache(ids);
        List<WarehouseEntity> list = this.listByIds(ids);
        //待审核
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();

        //审核不通过
        String rejectStatus = ApproveStatusEnum.REJECT.getStatus();

        //审核中
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();

        List<String> statusList = new ArrayList<>(2);
        statusList.add(rejectStatus);
        statusList.add(waitSubmitStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_WAIT_SUBMIT_TO_APPROVE_ING);
        }
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(ingStatus));
        return result;
    }


    /**
     * 更改仓库状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:43
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean updateStatus(UpdateStateDTO dto) {
        // 删除缓存
        removeCache(Collections.singletonList(dto.getId()));
        //仓库id
        String warehouseId = dto.getId();
        WarehouseEntity warehouse = this.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99001);
        }
        warehouse.setDisabled(dto.getState());
        this.updateById(warehouse);

        //发送金蝶
        if (dto.getState()) {
            List<ShopInfoEntity> shopInfoEntities = shopInfoFeign.listShopInfoByWarehouseIds(Arrays.asList(dto.getId()));
            if (CollectionUtils.isNotEmpty(shopInfoEntities)) {
                throw new ServiceException(ApiError.SHOP_INFO_EXIST_WAREHOUSE_NOT_DISABLE, shopInfoEntities.get(MathUtil.ZERO).getName());
            }
            syncKingdeeWarehouseService.syncDataToKingdee(warehouse, SyncOperateEnum.OPERATE_DISABLE.getCode());
        } else {
            syncKingdeeWarehouseService.syncDataToKingdee(warehouse, SyncOperateEnum.OPERATE_ENABLE.getCode());
        }
        return Boolean.TRUE;
    }

    /**
     * 审核仓库
     *
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:45
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean approve(BaseApproveParamDTO dto) {
        List<String> warehouseIds = dto.getIds();
        // 删除缓存
        removeCache(warehouseIds);

        List<WarehouseEntity> list = this.listByIds(warehouseIds);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        String ingStatus = ApproveStatusEnum.APPROVE_ING.getStatus();
        long count = list.stream().filter(s -> !ingStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98006);
        }
        if (dto.getType().equals(WmsConstant.PASS)) {
            //审核通过
            String approveStatus = ApproveStatusEnum.APPROVE.getStatus();
            Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(approveStatus));

            //审核通过后发送金蝶
            list.forEach(obj -> syncKingdeeWarehouseService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_APPROVE.getCode()));
            return result;
        } else {
            //审核不通过
            String rejectStatus = ApproveStatusEnum.REJECT.getStatus();
            Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(rejectStatus));
            return result;
        }


    }


    /**
     * 反审核
     *
     * @param warehouseIds
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:59
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean disApprove(List<String> warehouseIds) {
        // 删除缓存
        removeCache(warehouseIds);

        List<WarehouseEntity> list = this.listByIds(warehouseIds);


        //审核通过
        String approveStatus = ApproveStatusEnum.APPROVE.getStatus();

        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();

        List<String> statusList = new ArrayList<>(2);
        statusList.add(approveStatus);
        long count = list.stream().filter(s -> !statusList.contains(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99003);
        }

        //仓库已绑定店铺不允许反审核
        List<ShopInfoEntity> shopInfoEntities = shopInfoFeign.listShopInfoByWarehouseIds(warehouseIds);
        for (WarehouseEntity warehouseEntity : list) {
            ShopInfoEntity shopInfoEntity = shopInfoEntities.stream().filter(req -> req.getWarehouseId().equals(warehouseEntity.getId())).distinct().findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(shopInfoEntity)) {
                throw new ServiceException(ApiError.SHOP_INFO_EXIST_WAREHOUSE_NOT_DISAPPROVE, shopInfoEntity.getName());
            }
        }

        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus));

        //反审核后发送金蝶
        list.forEach(obj -> syncKingdeeWarehouseService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DISAPPROVE.getCode()));
        return result;
    }


    /**
     * 批量删除
     *
     * @param ids
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 12:12
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<String> ids) {
        // 删除缓存
        removeCache(ids);

        List<WarehouseEntity> list = this.listByIds(ids);
        //待提交
        String waitSubmitStatus = ApproveStatusEnum.WAIT_SUBMIT.getStatus();
        long count = list.stream().filter(s -> !waitSubmitStatus.equals(s.getApproveStatus().getStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_98009);
        }
        //审核通过后发送金蝶
        list.forEach(obj -> syncKingdeeWarehouseService.syncDataToKingdee(obj, SyncOperateEnum.OPERATE_DELETE.getCode()));
        return this.removeByIds(ids);
    }


    /**
     * 获取仓库详情
     *
     * @param warehouseId
     * @return com.erp.model.wms.dto.WarehouseDTO.UpdateDTO
     * @author yl
     * @date 2023-03-22 14:30
     */
    @Override
    public WarehouseDTO.UpdateDTO view(String warehouseId) {
        WarehouseEntity warehouse = this.getById(warehouseId);
        if (Objects.isNull(warehouse)) {
            throw new ServiceException(ApiError.ERROR_99001);
        }
        WarehouseDTO.UpdateDTO dto = new WarehouseDTO.UpdateDTO();
        BeanMapper.copy(warehouse, dto);
        ApproveStatusEnum approveStatusEnum = warehouse.getApproveStatus();
        dto.setApproveStatusCode(approveStatusEnum.getStatus());

        WarehouseMappingDTO.MappingViewDTO mappingViewByDictPlatform = warehouseMappingService.getMappingViewByDictPlatform(warehouseId, PlatformDictEnum.ALI_EXPRESS.getCode());
        if (ObjectUtil.isNotEmpty(mappingViewByDictPlatform)) {
            dto.setThirdWarehouseName(mappingViewByDictPlatform.getThirdWarehouseName());
        }
        return dto;
    }


    /**
     * 分页获取仓库数据
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseDTO.PagingViewDTO>
     * @author yl
     * @date 2023-03-22 14:51
     */
    @Override
    public PagingVO<WarehouseDTO.PagingViewDTO> paging(PagingDTO<WarehouseDTO.PagingParamDTO> dto) {
        WarehouseDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<WarehouseDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        //获取到字典数据类型
        List<String> dictTypeList = new ArrayList<>(3);
        dictTypeList.add(DictBasicEnum.WAREHOUSE_TYPE.getKey());
        dictTypeList.add(DictBasicEnum.WAREHOUSE_MANAGE_TYPE.getKey());
        dictTypeList.add(DictBasicEnum.GEOGRAPHY_LOCATION.getKey());
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(dictTypeList);
        List<String> userIdList = list.stream().map(WarehouseDTO.PagingViewDTO::getChargeId).distinct().collect(Collectors.toList());
        //获取用户信息
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
        List<String> orgIdList = list.stream().map(WarehouseDTO.PagingViewDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);

        for (WarehouseDTO.PagingViewDTO item : list) {
            //类型id
            String typeId = item.getTypeId();
            String typeName = dictBasicList.stream().filter(d -> d.getId().equals(typeId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setTypeName(typeName);
            //审核名
            ApproveStatusEnum statusEnum = item.getApproveStatus();
            item.setApproveStatusName(statusEnum.getName());
            item.setApproveStatusCode(statusEnum.getStatus());
            //负责人id
            String chargeId = item.getChargeId();
            String userName = userList.stream().filter(u -> chargeId.equals(u.getUserId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
            item.setChargeName(userName);
            //组织id
            String orgId = item.getOrgId();
            String orgName = orgList.stream().filter(o -> orgId.equals(o.getId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setOrgName(orgName);
            //经营类型
            String warehouseManageType = item.getWarehouseManageType();
            String warehouseManageTypeName = dictBasicList.stream().filter(d -> warehouseManageType.equals(d.getValue())).
                    map(DictBasicEntity::getName).findFirst().orElse("");
            item.setWarehouseManageTypeName(warehouseManageTypeName);

            //地理位置
            String geographyLocation = item.getGeographyLocation();
            String geographyLocationName = dictBasicList.stream().filter(d -> geographyLocation.equals(d.getValue())).
                    map(DictBasicEntity::getName).findFirst().orElse("");
            item.setGeographyLocationName(geographyLocationName);
        }
        return new PagingVO<>(pageData);
    }


    /**
     * 导出仓库数据
     *
     * @param dto
     * @param response
     * @return void
     * @author yl
     * @date 2023-03-22 16:08
     */
    @Override
    public void exportWarehouse(WarehouseDTO.ExportDTO dto, HttpServletResponse response) {
        //获取导出数据
        List<WarehouseDTO.PagingViewDTO> viewList = baseMapper.getExport(dto);
        List<WarehouseExportExcelDTO> resultList = new ArrayList<>(viewList.size());
        if (CollectionUtils.isNotEmpty(viewList)) {
            //获取到字典数据类型
            List<String> dictTypeList = new ArrayList<>(3);
            dictTypeList.add(DictBasicEnum.WAREHOUSE_TYPE.getKey());
            dictTypeList.add(DictBasicEnum.WAREHOUSE_MANAGE_TYPE.getKey());
            dictTypeList.add(DictBasicEnum.GEOGRAPHY_LOCATION.getKey());
            List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(dictTypeList);
            List<String> userIdList = viewList.stream().map(WarehouseDTO.PagingViewDTO::getChargeId).distinct().collect(Collectors.toList());
            //获取用户信息
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
            List<String> orgIdList = viewList.stream().map(WarehouseDTO.PagingViewDTO::getOrgId).collect(Collectors.toList());
            List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);

            List<String> warehouseIds = viewList.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
            List<WarehouseMappingDTO.MappingViewDTO> mappingViewDTOS = warehouseMappingService.listMappingViewByWarehouseIds(warehouseIds);

            for (WarehouseDTO.PagingViewDTO item : viewList) {
                WarehouseExportExcelDTO excelDTO = new WarehouseExportExcelDTO();
                BeanMapper.copy(item, excelDTO);
                //类型id
                String typeId = item.getTypeId();
                String typeName = dictBasicList.stream().filter(d -> d.getId().equals(typeId)).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                excelDTO.setTypeName(typeName);

                //审核名
                ApproveStatusEnum statusEnum = item.getApproveStatus();
                excelDTO.setApproveStatusName(statusEnum.getName());

                //负责人id
                String chargeId = item.getChargeId();
                String userName = userList.stream().filter(u -> chargeId.equals(u.getUserId())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getUserName())).orElse("");
                excelDTO.setChargeName(userName);

                //组织id
                String orgId = item.getOrgId();
                String orgName = orgList.stream().filter(o -> orgId.equals(o.getId())).findFirst().
                        flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                excelDTO.setOrgName(orgName);
                excelDTO.setEnabled(item.getDisabled() ? "停用" : "启用");
                excelDTO.setIsVirtual(item.getIsVirtual() ? "是" : "否");
                excelDTO.setOnwayWarehouseName(item.getOnwayWarehouseName());
                WarehouseMappingDTO.MappingViewDTO mappingViewDTO = mappingViewDTOS.stream().filter(req -> req.getWarehouseId().equals(item.getId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(mappingViewDTO)) {
                    excelDTO.setThirdWarehouseName(mappingViewDTO.getThirdWarehouseName());
                }
                //经营类型
                String warehouseManageType = item.getWarehouseManageType();
                String warehouseManageTypeName = dictBasicList.stream().filter(d -> warehouseManageType.equals(d.getValue())).
                        map(DictBasicEntity::getName).findFirst().orElse("");
                item.setWarehouseManageTypeName(warehouseManageTypeName);

                //地理位置
                String geographyLocation = item.getGeographyLocation();
                String geographyLocationName = dictBasicList.stream().filter(d -> geographyLocation.equals(d.getValue())).
                        map(DictBasicEntity::getName).findFirst().orElse("");
                item.setGeographyLocationName(geographyLocationName);

                resultList.add(excelDTO);


            }
        }
        String fileName = "仓库数据";
        ExcelUtil.export(fileName, "warehouse", resultList, WarehouseExportExcelDTO.class, response);


    }


    /**
     * 下载仓库模板
     *
     * @param response
     * @return void
     * @author yl
     * @date 2023-03-22 17:06
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/warehouse.xlsx";
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
            log.error("warehouse downloadTemplate  出错了 e==", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }


    /**
     * 导入仓库数据
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 17:17
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        //获取到字典数据类型
        List<String> dictTypeList = new ArrayList<>(3);
        dictTypeList.add(DictBasicEnum.WAREHOUSE_TYPE.getKey());
        dictTypeList.add(DictBasicEnum.WAREHOUSE_MANAGE_TYPE.getKey());
        dictTypeList.add(DictBasicEnum.GEOGRAPHY_LOCATION.getKey());
        List<DictBasicEntity> dictBasicList = dictBasicService.getByKeyList(dictTypeList);
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(new ArrayList<>());


        List<WarehouseEntity> warehouseList = this.list();
        WarehouseExcelListener excelListenerUtil = new WarehouseExcelListener(this, dictBasicList, userList, orgList, warehouseList, warehouseMappingService);
        try {
            EasyExcel.read(excelFile.getInputStream(), WarehouseExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("仓库导入错误！", e);
            return Boolean.FALSE;
        }
        List<WarehouseExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "仓库错误信息";
            ExcelUtil.export(fileName, "warehouseError", errorList, WarehouseExcelDTO.class, response);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }


    /**
     * 修改并审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-28 11:17
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateAndSubmit(WarehouseDTO.UpdateDTO dto) {
        String id = this.updateWarehouse(dto);
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1020);
        }
        return this.submit(Arrays.asList(id));

    }

    @Override
    public Boolean updateSyncKingdeeId(String id, String syncKingdeeId) {
        return this.lambdaUpdate()
                .eq(WarehouseEntity::getId, id)
                .set(StringUtils.isNotBlank(syncKingdeeId), WarehouseEntity::getSyncKingdeeId, syncKingdeeId)
                .update();
    }

    @Override
    public WarehouseDTO.UpdateDTO detailWithCache(String id) {
        if (StrUtils.isEmpty(id)) {
            return null;
        }
        String redisKey = WmsRedisKeyEnum.WMS_WAREHOUSE_DETAIL_ID.keyBuilder(id);
        Object obj = redisService.getCacheObject(redisKey);
        if (Objects.nonNull(obj)) {
            // 判断是否空缓存
            if (Objects.equals(RedisService.EMPTY_CACHE_VALUE, obj)) {
                log.info("从redis缓存中查询到仓库信息，仓库id:{}，内容为空", id);
                return new WarehouseDTO.UpdateDTO();
            }
            log.info("从redis缓存中查询到仓库信息，仓库id:{}，内容:{}", id, obj);
            WarehouseDTO.UpdateDTO warehouseDTO = (WarehouseDTO.UpdateDTO) obj;
            return warehouseDTO;
        }
        // 从数据库中查询
        WarehouseEntity warehouse = this.getById(id);
        if (Objects.isNull(warehouse)) {
            log.info("从数据库中没有查询到仓库信息，仓库id:{}，缓存空", id);
            redisService.setCacheObject(redisKey, RedisService.EMPTY_CACHE_VALUE, RedisService.ONE_DAY_CACHE_TIME, TimeUnit.SECONDS);
            return new WarehouseDTO.UpdateDTO();
        }
        WarehouseDTO.UpdateDTO dto = new WarehouseDTO.UpdateDTO();
        BeanMapper.copy(warehouse, dto);
        ApproveStatusEnum approveStatusEnum = warehouse.getApproveStatus();
        dto.setApproveStatusCode(approveStatusEnum.getStatus());
        redisService.setCacheObject(redisKey, dto, RedisService.ONE_DAY_CACHE_TIME, TimeUnit.SECONDS);
        return dto;
    }

    /**
     * 根据金蝶仓库code 获取到对应仓库信息
     *
     * @param kingdeeWarehouseCodeList
     * @return java.util.List<com.erp.model.wms.entity.WarehouseEntity>
     * @author yl
     * @date 2023-06-27 16:34
     */
    @Override
    public List<WarehouseEntity> listByKingdeeCodeList(List<String> kingdeeWarehouseCodeList) {
        if (CollectionUtils.isEmpty(kingdeeWarehouseCodeList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(WarehouseEntity::getKingdeeWarehouseCode, kingdeeWarehouseCodeList).list();
    }


    @Override
    public List<WarehouseDTO.ListDTO> listWarehouseByParams(WarehouseDTO.ListParamDTO dto) {
        List<WarehouseEntity> list = lambdaQuery()
                .eq(StringUtils.isNotBlank(dto.getWarehouseName()), WarehouseEntity::getName, dto.getWarehouseName())
                .in(CollectionUtils.isNotEmpty(dto.getOrgIdList()), WarehouseEntity::getOrgId, dto.getOrgIdList())
                .in(CollectionUtils.isNotEmpty(dto.getWarehouseIdList()), WarehouseEntity::getId, dto.getWarehouseIdList())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<WarehouseDTO.ListDTO> resultList = BeanMapperUtils.copyList(WarehouseDTO.ListDTO.class, list);
        // 查询仓库关联服务商
        Map<String, List<OverseasProviderDTO.ListWithWarehouseDTO>> warehouseBindMap = overseasProviderService.mapByWarehouseIds();

        // 填充信息
        this.fillListData(resultList, warehouseBindMap);

        return resultList.stream()
                .filter(e -> StringUtils.isBlank(dto.getDictPlatform()) ||
                        (StringUtils.isNotBlank(dto.getDictPlatform()) && e.getDictPlatform().equalsIgnoreCase(dto.getDictPlatform()))
                )
                .sorted(Comparator.comparing(WarehouseDTO.ListDTO::getDisabled))
                .collect(Collectors.toList());
    }

    @Override
    public PagingVO<WarehouseDTO.PagingNoPermissionDTO> pagingNoPermission(PagingDTO<WarehouseDTO.PagingDTO> dto) {
        WarehouseDTO.PagingDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<WarehouseDTO.PagingNoPermissionDTO> pageData = baseMapper.pagingNoPermission(query, params);
        List<WarehouseDTO.PagingNoPermissionDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO(pageData);
        }
        //获取到仓库类型
        List<DictBasicDTO.ListDTO> dictBasicList = dictBasicService.getByKey(DictBasicEnum.WAREHOUSE_TYPE.getKey());
        //获取用户信息
        List<String> orgIdList = list.stream().map(WarehouseDTO.PagingNoPermissionDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);

        for (WarehouseDTO.PagingNoPermissionDTO item : list) {
            //类型id
            String typeId = item.getTypeId();
            String typeName = dictBasicList.stream().filter(d -> d.getId().equals(typeId)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setTypeName(typeName);
            //组织id
            String orgId = item.getOrgId();
            String orgName = orgList.stream().filter(o -> orgId.equals(o.getId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setOrgName(orgName);
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public PagingVO<WarehouseDTO.PagingProductViewDTO> pagingProduct(PagingDTO<WarehouseDTO.PagingProductDTO> dto) {
        WarehouseDTO.PagingProductDTO params = dto.getParams();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<WarehouseDTO.PagingProductViewDTO> pageData = baseMapper.pagingProduct(query, params);
        List<WarehouseDTO.PagingProductViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        //获取组织信息
        List<String> orgIdList = list.stream().map(WarehouseDTO.PagingProductViewDTO::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);
        list.stream().map(item -> {
            //组织id
            String orgId = item.getOrgId();
            String orgName = orgList.stream().filter(o -> orgId.equals(o.getId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setOrgName(orgName);
            return item;
        }).collect(Collectors.toList());
        return new PagingVO<>(pageData);
    }


    /**
     * 更改状态
     *
     * @param list
     * @param statusEnum
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:41
     */
    private Boolean updateApproveStatus(List<WarehouseEntity> list, ApproveStatusEnum statusEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(s -> s.setApproveStatus(statusEnum));
            return this.updateBatchById(list);
        }
        return true;
    }


    /**
     * 检查金蝶code 是否重复
     *
     * @param kingdeeWarehouseCode
     * @return void
     * @author yl
     * @date 2023-03-22 10:27
     */
    private void checkKingdeeWarehouseCode(String id, String kingdeeWarehouseCode) {
        LambdaQueryWrapper<WarehouseEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(WarehouseEntity::getId, id);
        }
        queryWrapper.eq(WarehouseEntity::getKingdeeWarehouseCode, kingdeeWarehouseCode);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_99000);
        }

    }

    /**
     * 检查仓库名是否存在
     *
     * @param id
     * @param name
     * @return void
     * @author yl
     * @date 2023-03-22 10:25
     */
    private void checkName(String id, String name) {
        LambdaQueryWrapper<WarehouseEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(WarehouseEntity::getId, id);
        }
        queryWrapper.eq(WarehouseEntity::getName, name);
        queryWrapper.last("LIMIT 1");
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_DUPLICATION_NAME);
        }
    }

    private void removeCache(List<String> ids) {
        List<String> redisKeys = Lists.newArrayList();
        ids.stream().forEach(id -> {
            String redisKey = WmsRedisKeyEnum.WMS_WAREHOUSE_DETAIL_ID.keyBuilder(id);
            redisKeys.add(redisKey);
        });

        redisService.deleteObject(redisKeys);
    }

    /**
     * @param list
     * @return List<ListTreeDTO>
     * @description: 处理下拉数据
     * @author Will
     * @date: 2023/9/6 15:59
     */
    private List<WarehouseDTO.ListTreeDTO> handleWarehouseTree(List<WarehouseDTO.ListDTO> list) {

        //获取到仓库类型
        List<DictBasicDTO.ListDTO> dictBasicList = dictBasicService.getByKey(DictBasicEnum.WAREHOUSE_TYPE.getKey());

        List<WarehouseDTO.ListTreeDTO> resultList = new ArrayList<>();
        Map<String, List<WarehouseDTO.ListDTO>> map = list.stream().collect(Collectors.groupingBy(WarehouseDTO.ListDTO::getTypeId));
        for (Map.Entry<String, List<WarehouseDTO.ListDTO>> entry : map.entrySet()) {
            WarehouseDTO.ListTreeDTO listTreeDTO = new WarehouseDTO.ListTreeDTO();
            String key = entry.getKey();
            listTreeDTO.setId(key);
            //类型id
            String typeName = dictBasicList.stream().filter(d -> d.getId().equals(key)).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listTreeDTO.setName(typeName);
            listTreeDTO.setListDTO(entry.getValue());
            resultList.add(listTreeDTO);
        }
        return resultList;
    }
}

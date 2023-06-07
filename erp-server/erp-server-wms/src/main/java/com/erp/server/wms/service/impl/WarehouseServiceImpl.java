package com.erp.server.wms.service.impl;

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
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.business.service.RedisService;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.WarehouseExcelDTO;
import com.erp.model.wms.dto.excel.WarehouseExportExcelDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.WmsRedisKeyEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.constant.WmsConstant;
import com.erp.server.wms.kingdee.SyncKingdeeWarehouseService;
import com.erp.server.wms.listener.WarehouseExcelListener;
import com.erp.server.wms.mapper.WarehouseMapper;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.WarehouseService;
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
import java.time.LocalDateTime;
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


    @Override
    public List<WarehouseDTO.UpdateDTO> listWarehouseByIds(List<String> ids) {
        List<WarehouseEntity> list = this.listByIds(ids);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return BeanMapperUtils.copyList(WarehouseDTO.UpdateDTO.class, list);
    }

    @Override
    public List<WarehouseDTO.ListDTO> listApproveWarehouse() {
        List<ApproveStatusEnum> statusList = new ArrayList<>(2);
        statusList.add(ApproveStatusEnum.APPROVE);
        statusList.add(ApproveStatusEnum.REJECT);
        List<WarehouseEntity> list = this.list();
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<WarehouseDTO.ListDTO> resultList = BeanMapperUtils.copyList(WarehouseDTO.ListDTO.class, list);
        List<String> orgIds = list.stream().map(WarehouseEntity::getOrgId).collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);
        for (WarehouseDTO.ListDTO listDTO : resultList) {
            String orgName = accountingCompanyList.stream().filter(obj -> obj.getId().equals(listDTO.getOrgId())).map(BaseIdDTO.CodeDTO::getName).findFirst().orElse("");
            listDTO.setOrgName(orgName);
            if (statusList.contains(listDTO.getApproveStatus())) {
                listDTO.setDisabled(true);
            }
        }

        return resultList;
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
        Boolean result = this.save(warehouse);
        if (result) {
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
        checkKingdeeWarehouseCode(warehouseId,code);
        BeanMapper.copy(dto, warehouse);
        Boolean result = this.updateById(warehouse);
        if (result) {
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
            syncKingdeeWarehouseService.syncDataToKingdee(warehouse, SyncKingdeeOperateEnum.OPERATE_DISABLE.getCode());
        } else {
            syncKingdeeWarehouseService.syncDataToKingdee(warehouse, SyncKingdeeOperateEnum.OPERATE_ENABLE.getCode());
        }
        return Boolean.TRUE;
    }

    /**
     * 审核仓库
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-22 11:45
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
            list.forEach(obj -> syncKingdeeWarehouseService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_APPROVE.getCode()));
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
        Boolean result = this.updateApproveStatus(list, ApproveStatusEnum.getByStatus(waitSubmitStatus));

        //反审核后发送金蝶
        list.forEach(obj -> syncKingdeeWarehouseService.syncDataToKingdee(obj, SyncKingdeeOperateEnum.OPERATE_DISAPPROVE.getCode()));
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
        //获取到仓库类型
        List<DictBasicDTO.ListDTO> dictBasicList = dictBasicService.getByKey(DictBasicEnum.WAREHOUSE_TYPE.getKey());
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
            //获取到仓库类型
            List<DictBasicDTO.ListDTO> dictBasicList = dictBasicService.getByKey(DictBasicEnum.WAREHOUSE_TYPE.getKey());
            List<String> userIdList = viewList.stream().map(WarehouseDTO.PagingViewDTO::getChargeId).distinct().collect(Collectors.toList());
            //获取用户信息
            List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(userIdList);
            List<String> orgIdList = viewList.stream().map(WarehouseDTO.PagingViewDTO::getOrgId).collect(Collectors.toList());
            List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);

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
        //获取到仓库类型
        List<DictBasicDTO.ListDTO> dictBasicList = dictBasicService.getByKey(DictBasicEnum.WAREHOUSE_TYPE.getKey());
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(new ArrayList<>());
        List<WarehouseEntity> warehouseList = this.list();
        WarehouseExcelListener excelListenerUtil = new WarehouseExcelListener(this, dictBasicList, userList, orgList,warehouseList);
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
    public Boolean updateSyncKingdeeStatus(String id, String syncKingdeeStatus, String syncKingdeeId) {
        return  this.lambdaUpdate()
                .eq(WarehouseEntity::getId,id)
                .set(StringUtils.isNotBlank(syncKingdeeStatus),WarehouseEntity::getSyncKingdeeStatus,syncKingdeeStatus)
                .set(StringUtils.isNotBlank(syncKingdeeStatus),WarehouseEntity::getSyncKingdeeTime, LocalDateTime.now())
                .set(StringUtils.isNotBlank(syncKingdeeId),WarehouseEntity::getSyncKingdeeId,syncKingdeeId)
                .update();
    }

    @Override
    public WarehouseDTO.UpdateDTO detailWithCache(String id) {
        if(StrUtils.isEmpty(id)) {
            return null;
        }
        String redisKey = WmsRedisKeyEnum.WMS_WAREHOUSE_DETAIL_ID.keyBuilder(id);
        Object obj = redisService.getCacheObject(redisKey);
        if(Objects.nonNull(obj)) {
            // 判断是否空缓存
            if(Objects.equals(RedisService.EMPTY_CACHE_VALUE,obj)) {
                log.info("从redis缓存中查询到仓库信息，仓库id:{}，内容为空",id);
                return new WarehouseDTO.UpdateDTO();
            }
            log.info("从redis缓存中查询到仓库信息，仓库id:{}，内容:{}",id,obj);
            WarehouseDTO.UpdateDTO warehouseDTO = (WarehouseDTO.UpdateDTO) obj;
            return warehouseDTO;
        }
        // 从数据库中查询
        WarehouseEntity warehouse = this.getById(id);
        if (Objects.isNull(warehouse)) {
            log.info("从数据库中没有查询到仓库信息，仓库id:{}，缓存空",id);
            redisService.setCacheObject(redisKey,RedisService.EMPTY_CACHE_VALUE, RedisService.ONE_DAY_CACHE_TIME, TimeUnit.SECONDS);
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
        ids.stream().forEach(id->{
            String redisKey = WmsRedisKeyEnum.WMS_WAREHOUSE_DETAIL_ID.keyBuilder(id);
            redisKeys.add(redisKey);
        });

        redisService.deleteObject(redisKeys);
    }
}

package com.erp.server.dmp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;

import cn.hutool.core.util.StrUtil;
import com.common.business.vo.LoginUser;
import com.erp.model.dmp.dto.excel.FirstMileInTransitAdjustExcelDTO;
import com.erp.model.dmp.dto.excel.FirstMileInTransitInitExcelDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.excel.FbaTransitExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.dmp.listener.FirstMileInTransitAdjustExcelListener;
import com.erp.server.dmp.listener.FirstMileInTransitInitExcelListener;
import com.erp.model.dmp.entity.doris.AdsErpFirstMileInTransitDiffEntity;
import com.erp.server.dmp.mapper.doris.AdsErpFirstMileInTransitDiffMapper;
import com.erp.server.dmp.service.AdsErpFirstMileInTransitDiffService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import com.erp.server.dmp.utils.RestCloudApiUtil;
import io.seata.common.util.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.AdsErpFirstMileInTransitDiffDTO;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 平台在途报告 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-11-13
 */
@Slf4j
@Service
public class AdsErpFirstMileInTransitDiffServiceImpl extends SuperServiceImpl<AdsErpFirstMileInTransitDiffMapper, AdsErpFirstMileInTransitDiffEntity> implements AdsErpFirstMileInTransitDiffService {

    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<AdsErpFirstMileInTransitDiffDTO.ListDTO> paging(PagingDTO<AdsErpFirstMileInTransitDiffDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<AdsErpFirstMileInTransitDiffDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }


    @Override
    public Boolean exportList(AdsErpFirstMileInTransitDiffDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("平台在途报表Excel导出", FileTaskEventEnum.EXPORT_ADS_ERP_FIRST_MILE_INTRANSIT_DIFF.getCode(), param);
        return true;
    }

    @Override
    public AdsErpFirstMileInTransitDiffDTO.ViewDTO view(String id) {
        AdsErpFirstMileInTransitDiffEntity adsErpFirstMileInTransitDiffEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到平台在途报告数据"));
        AdsErpFirstMileInTransitDiffDTO.ViewDTO data = BeanMapperUtils.map(AdsErpFirstMileInTransitDiffDTO.ViewDTO.class, adsErpFirstMileInTransitDiffEntity);
        // 数据填充处理
        fillOne(data);
        // TODO 查询明细数据（如果有的话）
        return data;
    }

    private void fillOne(AdsErpFirstMileInTransitDiffDTO.ViewDTO data) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(AdsErpFirstMileInTransitDiffEntity adsErpFirstMileInTransitDiffEntity) {
        // TODO 验证数据 & 数据赋值
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<AdsErpFirstMileInTransitDiffDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for (AdsErpFirstMileInTransitDiffDTO.ListDTO data : list) {
            // TODO 其他如需要显示名称的字段赋值
        }
    }

    @Override
    public Boolean adjustTransitQty(AdsErpFirstMileInTransitDiffDTO.AdjustDTO adjustDTO) {
        AdsErpFirstMileInTransitDiffEntity entity = this.getById(adjustDTO.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST, "平台在途报告记录");
        }
        //存在下期在途记录不能调整
        LocalDateTime reportMonth = LocalDateTimeUtil.parse(entity.getCheckMonthQuery(), "yyyy-MM-dd HH:mm:ss");
        //下个月
        LocalDateTime nextMonth = reportMonth.minusMonths(-1);
        Integer count = this.lambdaQuery()
                .eq(AdsErpFirstMileInTransitDiffEntity::getExecStatus, "doing")
                .count();
        if (count > 0) {
            throw new ServiceException("存在【{}】在途核对数据，不能调整本月在途数量", nextMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        AdsErpFirstMileInTransitDiffDTO.FirstMileShipmentChangeFDTO newEntity = buildChangeEntity("firstMileAdjust", entity, adjustDTO.getAdjustQty(), entity.getAdjustReason(), entity.getCheckMonth(), reportMonth);

        int afterAdjustQty = entity.getEndPeriodTransitQty() + adjustDTO.getAdjustQty();
        if (afterAdjustQty < 0) {
            throw new ServiceException("期末在途(调整后)不能小于0");
        }
        //异步生成上月期末数据
        requestRestCloudOds(Collections.singletonList(newEntity));
        String msg = CharSequenceUtil.format("编辑期末在途调整数量从【{}】变为【{}】,调整原因：【{}】", entity.getAfterEndPeriodTransitQty(), afterAdjustQty, adjustDTO.getAdjustReason());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ADS_ERP_FIRST_MILE_IN_TRANSIT_DIFF.getCode(), entity.getId(), "编辑操作");
        return Boolean.TRUE;
    }

    private AdsErpFirstMileInTransitDiffDTO.FirstMileShipmentChangeFDTO buildChangeEntity(
            String changeBillType,
            AdsErpFirstMileInTransitDiffEntity entity,
            Integer adjustQty,
            String adjustReason,
            String checkMonth,
            LocalDateTime checkMonthQuery
    ) {
        AdsErpFirstMileInTransitDiffDTO.FirstMileShipmentChangeFDTO newEntity = new AdsErpFirstMileInTransitDiffDTO.FirstMileShipmentChangeFDTO();
        newEntity.setDataId(IdWorker.getIdStr());
        newEntity.setCheckMonth(checkMonth);
        newEntity.setCheckMonthQuery(checkMonthQuery.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        newEntity.setSourceSystem(entity.getSourceSystem());
        newEntity.setSourcePlatform(entity.getSourceSystem());
        newEntity.setAccountCode(entity.getAccountCode());
        newEntity.setNextLevelId(entity.getNextLevelId());
        newEntity.setBillTopic(changeBillType);
        newEntity.setPlatformShipmentId(entity.getShipmentId());
        newEntity.setPlatformShipmentCode(entity.getShipmentCode());
        newEntity.setShopId(entity.getShopId());
        newEntity.setShopName(entity.getShopName());
        newEntity.setCustomerId(entity.getCustomerId());
        newEntity.setCustomerName(entity.getCustomerName());
        newEntity.setShipmentStatus(entity.getShipmentStatus());
        newEntity.setShipmentCreateTime(entity.getShipmentCreateTime());
        newEntity.setWarehouseId(entity.getWarehouseId());
        newEntity.setWarehouseName(entity.getWarehouseName());
        newEntity.setPlatformSpuNo(entity.getPlatformSpuNo());
        newEntity.setPlatformSkuNo(entity.getPlatformSkuNo());
        newEntity.setIntransitWarehouseId(entity.getIntransitWarehouseId());
        newEntity.setIntransitWarehouseName(entity.getIntransitWarehouseName());
        newEntity.setPlatformSpuNo(entity.getPlatformSpuNo());
        newEntity.setPlatformSkuNo(entity.getPlatformSkuNo());
        newEntity.setPlatformSkuNo(entity.getPlatformSkuNo());
        newEntity.setSkuId(entity.getSkuId());
        newEntity.setSkuNo(entity.getSkuNo());
        newEntity.setChangeQty(adjustQty);
        newEntity.setRemark(adjustReason);
        LoginUser defaultLoginUser = UserContext.getDefaultLoginUser();
        newEntity.setUpdateUserId(defaultLoginUser.getUid());
        newEntity.setUpdateUserName(defaultLoginUser.getUserName());
        newEntity.setCreateUserId(defaultLoginUser.getUid());
        newEntity.setCreateUserName(defaultLoginUser.getUserName());
        return newEntity;
    }

    @Override
    public Boolean importInitFile(MultipartFile excelFile, HttpServletResponse response) {
        FirstMileInTransitInitExcelListener excelListenerUtil = new FirstMileInTransitInitExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), FirstMileInTransitInitExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        } catch (Exception e) {
            log.error("导入数据错误！", e);
            throw new ServiceException(ApiError.ERROR_1012);
        }
        List<FirstMileInTransitInitExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        } else if (excelDateList.size() > 5000) {
            throw new ServiceException(ApiError.ERROR_EXCEL_IMPORT_SIZE);
        }
        List<FirstMileInTransitInitExcelDTO> errorList = excelListenerUtil.getErrorList();
        List<FirstMileInTransitInitExcelDTO> successList = excelListenerUtil.getSuccessList();
        //异步生成上月期末数据
        List<AdsErpFirstMileInTransitDiffDTO.FirstMileShipmentChangeFDTO> newListEntity = successList.stream().map(e ->
                buildChangeEntity(
                        "firstMileInit",
                        e.getEntity(),
                        Integer.parseInt(e.getInitTransitQty()),
                        "",
                        e.getReportMonth(),
                        LocalDateTimeUtil.parse(e.getReportMonth().concat(" 00:00:00"), "yyyy-MM-dd HH:mm:ss")
                )).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(newListEntity)) {
            //异步生成上月期末数据
            return requestRestCloudOds(newListEntity);
        }

        if (CollUtil.isNotEmpty(errorList)) {
            String fileName = "FBA期初在途错误数据.xlsx";
            ExcelUtil.export(fileName, "error", errorList, FbaTransitExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return true;
    }

    @Override
    public Boolean importAdjustFile(MultipartFile excelFile, HttpServletResponse response) {
        FirstMileInTransitAdjustExcelListener excelListenerUtil = new FirstMileInTransitAdjustExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), FirstMileInTransitAdjustExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        } catch (Exception e) {
            log.error("导入数据错误！", e);
            throw new ServiceException(ApiError.ERROR_1012);
        }
        List<FirstMileInTransitAdjustExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        } else if (excelDateList.size() > 5000) {
            throw new ServiceException(ApiError.ERROR_EXCEL_IMPORT_SIZE);
        }
        List<FirstMileInTransitAdjustExcelDTO> errorList = excelListenerUtil.getErrorList();
        List<FirstMileInTransitAdjustExcelDTO> successList = excelListenerUtil.getSuccessList();
        //异步生成上月期末数据
        List<AdsErpFirstMileInTransitDiffDTO.FirstMileShipmentChangeFDTO> newListEntity = successList.stream().map(e ->
                buildChangeEntity(
                        "firstMileAdjust",
                        e.getEntity(),
                        Integer.parseInt(e.getAdjustQty()),
                        e.getRemark(),
                        e.getReportMonth(),
                        LocalDateTimeUtil.parse(e.getReportMonth().concat(" 00:00:00"), "yyyy-MM-dd HH:mm:ss")
                )).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(newListEntity)) {
            return requestRestCloudOds(newListEntity);
        }
        if (CollUtil.isNotEmpty(errorList)) {
            String fileName = "FBA在途调整错误数据.xlsx";
            ExcelUtil.export(fileName, "error", errorList, FirstMileInTransitAdjustExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return true;
    }

    @Override
    public BatchResultDTO updateRemark(String id, String remark) {
        AdsErpFirstMileInTransitDiffEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("平台在途报告数据"));

        // 删除主单数据
        log.info("删除 金蝶库存主单数据，id：【{}】", id);
        this.lambdaUpdate()
                .set(AdsErpFirstMileInTransitDiffEntity::getRemark, remark)
                .eq(AdsErpFirstMileInTransitDiffEntity::getId, id)
                .update();
        // 删除日志数据
        log.info("删除 开始平台在途报告日志数据，id：【{}】", id);
        String msg = StrUtil.format("用户【{}】单号为【{}】的【{}】更新备注操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getId(), "平台在途报告");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.ADS_ERP_FIRST_MILE_IN_TRANSIT_DIFF.getCode(), entity.getId(), "更新备注平台在途报告");
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.UPDATE);
    }

    private boolean requestRestCloudOds(List<AdsErpFirstMileInTransitDiffDTO.FirstMileShipmentChangeFDTO> newListEntity) {
        Map<String, Object> data = new HashMap<>();
        JSONArray jsonArray = (JSONArray) JSON.toJSON(newListEntity);
        data.put("data", jsonArray);
        log.debug("请求数据json:{}", JSON.toJSONString(data));
        boolean reCreate = RestCloudApiUtil.requestRestCloud(
                "ods_erp/ods_flow_erp_shipment_change", data, true);
        if (reCreate) {
            List<String> shipmentIdList = newListEntity.stream()
                    .map(AdsErpFirstMileInTransitDiffDTO.FirstMileShipmentChangeFDTO::getPlatformShipmentId)
                    .distinct().collect(Collectors.toList());
            List<String> checkMonthList = newListEntity.stream()
                    .map(AdsErpFirstMileInTransitDiffDTO.FirstMileShipmentChangeFDTO::getCheckMonth)
                    .distinct().collect(Collectors.toList());
            boolean update = lambdaUpdate()
                    .in(AdsErpFirstMileInTransitDiffEntity::getCheckMonth, checkMonthList)
                    .in(AdsErpFirstMileInTransitDiffEntity::getShipmentId, shipmentIdList)
                    .set(AdsErpFirstMileInTransitDiffEntity::getExecStatus, "doing")
                    .set(AdsErpFirstMileInTransitDiffEntity::getExecStatusName, "执行中")
                    .setSql(" finish_time = null ")
                    .update();
            if (!update) {
                log.error("更新平台在途报告执行状态失败，shipmentIdList：{}", shipmentIdList);
            }
        }
        return reCreate;
    }
}

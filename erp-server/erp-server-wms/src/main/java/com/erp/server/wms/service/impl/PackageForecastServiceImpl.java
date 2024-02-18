package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.enums.PackageStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.wms.dto.PackageForecastDetailDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.model.wms.enums.PackagePrintStatusEnum;
import com.erp.model.wms.enums.PackageUploadStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.ForecastFeign;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TransferDeclareFeign;
import com.erp.server.wms.convert.PackageForecastConverter;
import com.erp.server.wms.mapper.PackageForecastDetailMapper;
import com.erp.server.wms.mapper.PackageForecastMapper;
import com.erp.server.wms.service.PackageForecastDetailService;
import com.erp.server.wms.service.PackageForecastService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import com.erp.tms.aliexpress.service.AliExpressHandoverService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.PackageForecastDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 组包预报表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
 */
@Slf4j
@Service
public class PackageForecastServiceImpl extends SuperServiceImpl<PackageForecastMapper, PackageForecastEntity> implements PackageForecastService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private PackageForecastDetailService packageForecastDetailService;

    @Autowired
    private ForecastFeign forecastFeign;

    @Autowired
    private LogisticsFeign logisticsFeign;

    @Autowired
    private LogisticsAuthFeign logisticsAuthFeign;

    @Autowired
    private TransferDeclareFeign transferDeclareFeign;

    @Autowired
    private SoB2cFeign soB2cFeign;

    @Autowired
    private DmpTaskFeign dmpTaskFeign;

    @Autowired
    private AliExpressHandoverService aliExpressHandoverService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PackageForecastDTO.AddDTO addDTO) {
        PackageForecastEntity packageForecastEntity = new PackageForecastEntity();
        BeanMapperUtils.copy(addDTO, packageForecastEntity);
        // 数据处理
        handleData(packageForecastEntity);

        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZB);
        packageForecastEntity.setCode(code);
        boolean save = super.save(packageForecastEntity);
        if (!save) {
            throw new ServiceException("组包预报单保存失败");
        }
        String id = packageForecastEntity.getId();
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "组包预报单", packageForecastEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKAGE_FORECAST.getCode(), id, "新增操作");
        packageForecastDetailService.add(id, addDTO.getDetailList());

        return new BaseResultDTO.AddDTO(packageForecastEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PackageForecastDTO.UpdateDTO updateDTO) {
        PackageForecastEntity entity = super.getById(updateDTO.getId());
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单"));
        entity.setBillDate(updateDTO.getBillDate());
        boolean save = super.updateById(entity);
        if (!save) {
            throw new ServiceException("组包预报单保存失败");
        }
        packageForecastDetailService.update(entity.getId(), entity.getLogisticsSupplierId(), updateDTO.getDetailIdList());
        return Boolean.TRUE;
    }

    /**
     * tab 列表
     *
     * @param
     * @return
     */
    @Override
    public List<PackageForecastDTO.TabListDTO> tabList() {
        List<PackageForecastDTO.TabListDTO> resultList = new ArrayList<>(5);
        List<PackageForecastDTO.TabListDTO> tabListList = baseMapper.tabList();
        PackageForecastDTO.TabListDTO all = new PackageForecastDTO.TabListDTO();
        all.setTabFlag("all");
        all.setTabName("全部");
        Integer allCount = tabListList.stream().mapToInt(PackageForecastDTO.TabListDTO::getCount).sum();
        all.setCount(allCount);
        resultList.add(all);
        PackageUploadStatusEnum cancel = PackageUploadStatusEnum.CANCEL;
        for (PackageUploadStatusEnum item : PackageUploadStatusEnum.values()) {
            if (!cancel.equals(item)) {
                PackageForecastDTO.TabListDTO tabDTO = new PackageForecastDTO.TabListDTO();
                String tabCode = item.getCode();
                tabDTO.setTabFlag(item.getCode());
                tabDTO.setTabName(item.getName());
                Integer count = tabListList.stream().filter(t -> tabCode.equals(t.getTabFlag())).
                        map(PackageForecastDTO.TabListDTO::getCount).findFirst().orElse(0);
                tabDTO.setCount(count);
                resultList.add(tabDTO);
            }
        }
        return resultList;
    }

    @Override
    public PagingVO<PackageForecastDTO.PagingViewDTO> paging(PagingDTO<PackageForecastDTO.PagingParamDTO> dto) {
        PackageForecastDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<PackageForecastDTO.PagingViewDTO> list = pageData.getRecords();
        //处理分页数据
        fillPaging(list);
        return new PagingVO<>(pageData);

    }


    @Override
    public PackageForecastDTO.ViewDTO view(String id) {
        PackageForecastDTO.ViewDTO viewDTO = new PackageForecastDTO.ViewDTO();
        PackageForecastEntity packageForecast = this.getById(id);
        if (Objects.isNull(packageForecast)) {
            new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单");
        }
        BeanMapperUtils.copy(packageForecast, viewDTO);
        String uploadStatus = packageForecast.getUploadStatus();
        viewDTO.setUploadStatusName(PackageUploadStatusEnum.getName(uploadStatus));
        String printStatus = packageForecast.getPrintStatus();
        viewDTO.setPrintStatusName(PackagePrintStatusEnum.getName(printStatus));
        //获取详情
        List<PackageForecastDetailDTO.ViewDTO> detailList = packageForecastDetailService.listDetailViewByMainId(id);
        viewDTO.setDetailList(detailList);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单");
        }
        String successCode = PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode();
        if (successCode.equals(entity.getUploadStatus())) {
            throw new ServiceException("已上传成功,无法删除");
        }
        this.removeById(id);
        packageForecastDetailService.removeByMainId(id);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);

    }



    /**
     * 取消上传
     * @param id
     * @return
     */
    @Override
    public BatchResultDTO cancel(String id) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单");
        }
        String successCode = PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode();
        if (!successCode.equals(entity.getUploadStatus())) {
            throw new ServiceException("仅上传成功可操作");
        }
        entity.setUploadStatus(PackageUploadStatusEnum.CANCEL.getCode());
        this.updateById(entity);

        return BatchResultDTO.success(entity.getId(), entity.getCode(), "取消上传");

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO upload(String id, String collectMode, String collectAddressId) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单");
        }
        //待上传
        String wait = PackageUploadStatusEnum.WAIT.getCode();
        //上传失败
        String failure = PackageUploadStatusEnum.UPLOAD_FAILURE.getCode();

        List<String> uploadStatusList = Arrays.asList(wait, failure);
        //上传状态
        String uploadStatus = entity.getUploadStatus();
        if(!uploadStatusList.contains(uploadStatus)){
            throw new ServiceException("仅待上传/上传失败可操作");
        }
        //物流地址
        LogisticsAddressEntity addressEntity = logisticsFeign.getLogisticsAddressById(collectAddressId);
        if (Objects.isNull(addressEntity)) {
            throw new ServiceException("揽收地址不存在");
        }
        //物流商
        String supplierId = entity.getLogisticsSupplierId();
        LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthFeign.getAuthBySupplierId(supplierId);
        if (Objects.isNull(authDTO)) {
            throw new ServiceException("物流商不存在");
        }
        String addressName=addressEntity.getName();
        entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
        entity.setCollectMode(collectMode);
        entity.setCollectAddressId(collectAddressId);
        entity.setCollectAddress(addressName);

        String logisticsPlatform = authDTO.getLogisticsPlatform();
        //如果这里是速卖通的话就 对接平台
        if (logisticsPlatform.equals(PlatformDictEnum.ALI_EXPRESS.getCode())) {
              addBigPackage(logisticsPlatform,entity);
        }
        this.updateById(entity);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "上传");
    }

    public void addBigPackage(String logisticsPlatform, PackageForecastEntity entity) {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(logisticsPlatform);
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient=  dmpTaskFeign.getCfgAppClient(findDTO);
        if(Objects.nonNull(cfgAppClient)){
            Map<String, String> authMap=new HashMap<>(4);


        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO forecast(String id, String transferLogisticsSupplierId, String transferLogisticsChannelId) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单");
        }
        List<String> uploadStatusList = new ArrayList<>(2);
        uploadStatusList.add(PackageUploadStatusEnum.NOT.getCode());
        uploadStatusList.add(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
        String uploadStatus = entity.getUploadStatus();
        if (!uploadStatusList.contains(uploadStatus)) {
            new ServiceException("只有无需上传和上传成功的组包 才能中转报关");
        }
        List<PackageForecastDetailEntity> detailList = packageForecastDetailService.listDbByMainId(id);
        TransferDeclareDTO.AddDTO addDTO = new TransferDeclareDTO.AddDTO();
        addDTO.setTransferLogisticsSupplierId(transferLogisticsSupplierId);
        addDTO.setTransferChannelId(transferLogisticsChannelId);
        addDTO.setDeliveryLogisticsSupplierId(entity.getLogisticsSupplierId());
        addDTO.setGenerateTime(LocalTime.now());
        List<TransferDeclareDetailDTO.AddDTO> addDetailList= PackageForecastConverter.INSTANCE.convertDeclareDetail(detailList);
        addDTO.setDetailList(addDetailList);
        BaseResultDTO.AddDTO result = transferDeclareFeign.add(addDTO);
        String transferStatus = TransferStatusEnum.ALREADY.getCode();
        if (StringUtils.isNotBlank(result.getId())) {
            List<String> soIdList=detailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
            UpdateStateDTO.UpdateByStrStatusDTO dto = new UpdateStateDTO.UpdateByStrStatusDTO();
            dto.setStatus(transferStatus);
            dto.setIds(soIdList);
            soB2cFeign.updateTransferStatus(dto);
        }
        entity.setTransferStatus(transferStatus);
        this.updateById(entity);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);

    }

    @Override
    public Boolean exportExcel(PackageForecastDTO.ExportDTO dto, HttpServletResponse response) {
        List<PackageForecastDTO.PagingViewDTO> list = baseMapper.listExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //处理分页数据
        fillPaging(list);

        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/packageForecast.xlsx";
        String name = "组包预报列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("组包预报导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

    }




    /**
     * 填充分页数据
     *
     * @param list
     */
    private void fillPaging(List<PackageForecastDTO.PagingViewDTO> list) {
        for (PackageForecastDTO.PagingViewDTO item : list) {
            String uploadStatus = item.getUploadStatus();
            String uploadStatusName = PackageUploadStatusEnum.getName(uploadStatus);
            item.setUploadStatusName(uploadStatusName);
            String printStatus = item.getPrintStatus();
            String printStatusName = PackagePrintStatusEnum.getName(printStatus);
            item.setPrintStatusName(printStatusName);
            //第三方交接单号
            String handoverNo = item.getHandoverNo();
            //第三方组包号
            String platformPackageNo = item.getPlatformPackageNo();
            String platformNo = handoverNo + "/" + platformPackageNo;
            item.setPlatformNo(platformNo);
            BigDecimal totalPackageWeight=item.getTotalPackageWeight();
            String totalPackageWeightUnit=item.getTotalPackageWeightUnit();
            String totalPackageWeightStr=totalPackageWeight+totalPackageWeightUnit;
            item.setTotalPackageWeightStr(totalPackageWeightStr);
            BigDecimal weight=item.getWeight();
            String weightUnit=item.getWeightUnit();
            String weightStr=weight+weightUnit;
            item.setWeightStr(weightStr);
        }
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(PackageForecastEntity entity) {
        String logisticsSupplierId = entity.getLogisticsSupplierId();
        SettingForecastDTO.FindByLogisticsSupplierDTO dto = new SettingForecastDTO.FindByLogisticsSupplierDTO();
        dto.setOrderTime(LocalDateTime.now());
        dto.setLogisticsSupplierId(logisticsSupplierId);
        SettingForecastDTO.ForecastStatusDTO forecastStatus = forecastFeign.getByLogisticsSupplier(dto);
        String uploadStatus = PackageUploadStatusEnum.NOT.getCode();
        if (Objects.nonNull(forecastStatus)) {
            String packageStatus = forecastStatus.getPackageStatus();
            //表示要组包啊
            if (!PackageStatusEnum.NOT.getCode().equals(packageStatus)) {
                uploadStatus = PackageUploadStatusEnum.WAIT.getCode();
            }
        }
        entity.setUploadStatus(uploadStatus);

    }
}

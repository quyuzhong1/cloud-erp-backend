package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.KolSampleCostDTO;
import com.erp.model.oms.dto.excel.KolSampleCostImportExcelDTO;
import com.erp.model.oms.entity.KolSampleCostEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.oms.listener.KolSampleCostExcelListener;
import com.erp.server.oms.mapper.KolSampleCostMapper;
import com.erp.server.oms.service.KolSampleCostService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 寄样费用表 服务实现类
 * </p>
 *
 * @author will
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolSampleCostServiceImpl extends SuperServiceImpl<KolSampleCostMapper, KolSampleCostEntity> implements KolSampleCostService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;



    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolSampleCostDTO.AddDTO addDTO) {
        KolSampleCostEntity kolSampleCostEntity = new KolSampleCostEntity();
        BeanMapperUtils.copy(addDTO, kolSampleCostEntity);

        // 数据处理
        handleData(kolSampleCostEntity);

        log.info("开始新增寄样费用单");
        boolean save = super.save(kolSampleCostEntity);
        if(!save) {
            throw new ServiceException("寄样费用单保存失败");
        }

        return new BaseResultDTO.AddDTO(kolSampleCostEntity.getId(), kolSampleCostEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolSampleCostDTO.UpdateDTO addOrUpdateDTO) {
        KolSampleCostEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "寄样费用单"));
        KolSampleCostEntity kolSampleCostEntity =  BeanMapperUtils.map(KolSampleCostEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolSampleCostEntity);
        log.info("编辑 开始修改寄样费用单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolSampleCostEntity);
        if(!save) {
            throw new ServiceException("寄样费用单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<KolSampleCostDTO.ListDTO> paging(PagingDTO<KolSampleCostDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<Object> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<KolSampleCostDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public void updateCost(KolSampleCostDTO.UpdateCostDTO dto) {

    }

    @Override
    public void exportList(KolSampleCostDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("寄样费用导出", FileTaskEventEnum.EXPORT_OMS_KOL_SAMPLE_COST_REPORT.getCode(), param);
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        KolSampleCostExcelListener excelListenerUtil = new KolSampleCostExcelListener();

        try {
            EasyExcel.read(excelFile.getInputStream(), KolSampleCostImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        //验证导入数据是否为空
        List<KolSampleCostImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        //导入数据处理
        List<KolSampleCostImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<KolSampleCostImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理校验导入成功数据
        handleImportSuccessList(successList, errorList);

        if (errorList.isEmpty()) {
            return Boolean.TRUE;
        }
        String excelPath = "excel/kolSampleCostError.xlsx";
        String name = "kolSampleCostError";
        try {
            new ExcelPrintUtils().patchExport(errorList,
                    response,
                    StrUtil.builder().append(DateUtil.nowExcelFileFormat()).append(name).toString(),
                    excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95125);
        }
        return Boolean.FALSE;
    }


    /**
     * 处理导入数据
     * @author will
     * @date 2025/12/8 19:03
     * @param successList
     * @param errorList
     * @return void
     */
    private void handleImportSuccessList(List<KolSampleCostImportExcelDTO> successList, List<KolSampleCostImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        List<String> soCodeList = successList.stream().map(KolSampleCostImportExcelDTO::getSoCode).distinct().collect(Collectors.toList());
        List<KolSampleCostEntity> kolSampleCostList = listBySoCodeList(soCodeList);

        for (KolSampleCostImportExcelDTO importExcelDTO : successList) {
                List<KolSampleCostEntity> costList = kolSampleCostList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSoCode(), importExcelDTO.getSoCode())).collect(Collectors.toList());
                if (CollUtil.isEmpty(costList)) {
                    importExcelDTO.setErrorMsg("未找到对应的销售订单号：" + importExcelDTO.getSoCode());
                    errorList.add(importExcelDTO);
                    continue;
                }
                 //计算总数量
                Integer totalQty = costList.stream().map(KolSampleCostEntity::getQty).reduce(Integer.SIZE, Integer::sum);


                for (KolSampleCostEntity entity : costList) {
                    BigDecimal cost = MathUtil.divide(MathUtil.valueOf(entity.getQty()), MathUtil.valueOf(totalQty))
                            .multiply(MathUtil.valueOf(importExcelDTO.getAmountStr()))
                            .multiply(MathUtil.valueOf(importExcelDTO.getExchangeRateStr()));
                    if (CharSequenceUtil.equals(importExcelDTO.getFeeType(),"物流费")) {
                        //“尾程-运费”=当前行SKU实发数量/同一销售单号所有SKU实发数量*原币金额*汇率
                        entity.setShippingCost(cost);
                    }
                    if (CharSequenceUtil.equals(importExcelDTO.getFeeType(),"订单费用")) {
                        //“尾程-其他费用”=当前行SKU实发数量/同一销售单号所有SKU实发数量*原币金额*汇率

                    }
                }
        }
    }

    /**
     * 根据销售订单号列表查询寄样费用数据
     * @author will
     * @date 2025/12/9 09:19
     * @param soCodeList
     * @return List<KolSampleCostEntity>
     */
    private List<KolSampleCostEntity> listBySoCodeList(List<String> soCodeList) {
        if (CollectionUtils.isEmpty(soCodeList)) {
            return CollUtil.newArrayList();
        }
        return lambdaQuery().eq(KolSampleCostEntity::getSoCode, soCodeList).list();
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<KolSampleCostDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(KolSampleCostEntity kolSampleCostEntity) {
    // TODO 验证数据 & 数据赋值
    }
}

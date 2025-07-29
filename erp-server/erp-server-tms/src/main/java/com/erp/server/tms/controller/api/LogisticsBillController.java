package com.erp.server.tms.controller.api;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.enums.TrackQueryTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsBillCostEntity;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.server.tms.query.LogisticsBillQueryHandler;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 物流单
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@RestController
@LogSystemModule("物流单")
@RequestMapping("/logisticsBill")
public class LogisticsBillController extends BaseController {

    @Resource
    private LogisticsBillService logisticsBillService;

    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    @Resource
    private LogisticsTrackService logisticsTrackService;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    /**
     * tab 列表
     *
     * @param dto
     * @author yl
     * @date 2023-11-09 10:54
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "lb.shop_id",
            menuCode = "tms:logisticsBill:paging",
            tableAlias = "lb"
    )
    public ApiResult<List<LogisticsBillDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<LogisticsBillDTO.TabListDTO> tabList = logisticsBillService.tabList(dto);
        return success(tabList);
    }


    /**
     * 分页
     *
     * @param dto
     * @author yl
     * @date 2023-11-09 10:54
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            shopTableField = "lb.shop_id",
            menuCode = "tms:logisticsBill:paging",
            tableAlias = "lb"
    )
    @WebAdvanceQuery(handler = LogisticsBillQueryHandler.class)
    public ApiResult<PagingVO<LogisticsBillDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<LogisticsBillDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsBillDTO.PagingVO> pagingVO = logisticsBillService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 导出
     *
     * @param dto
     * @author yl
     * @date 2023-11-09 10:54
     */
    @PostMapping("/export")
    public ApiResult<Object>exportExcel(@RequestBody @Valid LogisticsBillDTO.PagingParamDTO dto) {
        Boolean result = logisticsBillService.exportExcel(dto);
        return result ? success() : failure();
    }

    /**
     * 获取物流轨迹明细
     * @param logisticsChannelId 物流渠道id
     * @param trackNo 跟踪号
     * @param transportNo 运单号
     * @return
     */
    @GetMapping("/getTrackInfo")
    public ApiResult<LogisticsTrackDTO.ViewDTO> listTrack(@RequestParam(value = "logisticsChannelId",required = false) String logisticsChannelId,
                                                          @RequestParam(value = "trackNo",required = false) String trackNo,
                                                          @RequestParam(value = "transportNo",required = false) String transportNo) {
        if (StringUtils.isBlank(trackNo) && StringUtils.isBlank(transportNo)){
            return failure("运单号和跟踪号不能同时为空");
        }
        //默认使用运单号
        String number = transportNo;
        //根据渠道设置进行判断使用哪个字段
        if (CharSequenceUtil.isNotBlank(logisticsChannelId)){
            LogisticsChannelEntity channelEntity = logisticsChannelService.getById(logisticsChannelId);
            if (Objects.nonNull(channelEntity)){
                number = TrackQueryTypeEnum.TRACK_NO.getCode().equals(channelEntity.getTrackQueryType()) && CharSequenceUtil.isNotBlank(trackNo) ? trackNo : transportNo;
            }
        }
        if (CharSequenceUtil.isBlank(number) && CharSequenceUtil.isNotBlank(trackNo)){
            number = trackNo;
        }
        LogisticsTrackDTO.ViewDTO list = logisticsTrackService.listByTrackNo(number);
        return success(list);

    }

    /**
     * 批量状态更新
     *
     * @return
     */
    @PostMapping("/batchUpdateStatus")
    public ApiResult<List<BatchResultDTO>> batchUpdate(@RequestBody @Valid LogisticsBillDTO.BatchUpdateStatusDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        String trackStatus = dto.getTrackStatus();
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = logisticsBillDetailService.updateStatus(id,trackStatus,dto.getTrackTime(),dto.getTrackDesc());
            } catch (Exception e) {
                log.error("物流商更改状态失败{}", e);
                LogisticsBillDetailEntity entity = logisticsBillDetailService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "物流商不存在, 状态更改失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getTrackNo(), e.getMessage());
            }
            resultDTOS.add(result);

        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);

    }

    /**
     * 初始化头程发货单业务单号
     * @return
     */
    @PostMapping("/initLogisticsBillBusinessCode")
    public ApiResult<Object>initLogisticsBillBusinessCode(){
        logisticsBillService.initLogisticsBillBusinessCode();
        return success();
    }

    /**
     * 删除没有销售出库单/发货单的物流单
     * @return
     */
    @GetMapping("/deleteLogisticsBillNoOutstock")
    public ApiResult<Object> deleteLogisticsBillNoOutstock(@RequestParam(value = "orderType") String orderType){
        logisticsBillService.deleteLogisticsBillNoOutstock(orderType);
        return success();
    }
    /**
     * 添加物流单明细并补充物流费用
     * @return
     */
    @GetMapping("/addNoLogisticsBillDetailByBill")
    public ApiResult<Object> addNoLogisticsBillDetailByBill(){
        logisticsBillService.addNoLogisticsBillDetailByBill();
        return success();
    }
    /**
     * 下载物流轨迹模板
     */
    @GetMapping("/exportTrackTemplate")
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载小包物流单模板")
    public ApiResult<Object> exportTrackTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "excel/tmsTrackLogistics.xlsx";
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
        return success();
    }

    /**
     * 小包物流单导入
     */
    @PostMapping("/importTrack")
    @LogAction(value = LogActionEnum.IMPORT, desc = "小包物流单导入")
    public ApiResult<Boolean> importTrack(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) throws Exception {
        return success(logisticsBillService.importTrack(excelFile,response));
    }

    /**
     * 生成自发货费用/尾程费用
     */
    @PostMapping("/generateBillCost")
    @LogAction(value = LogActionEnum.INSERT, desc = "生成自发货费用/尾程费用")
    public ApiResult<List<BatchResultDTO>> generateBillCost(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<LogisticsBillEntity> logisticsBillEntityList = logisticsBillService.listByIds(ids);
        List<LogisticsBillDetailEntity> logisticsBillDetailEntityList = logisticsBillDetailService.listByMainIds(ids);
        List<LogisticsBillCostEntity> logisticsBillCostEntityList = logisticsBillCostService.listByLogisticsBillIdList(ids);
        for (String id : dto.getIds()) {
            LogisticsBillEntity entity = logisticsBillEntityList.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id, id, "该物流单不存在"));
                continue;
            }
            LogisticsBillCostEntity logisticsBillCostEntity = logisticsBillCostEntityList.stream().filter(e -> e.getLogisticsBillId().equals(id)).findFirst().orElse(null);
            if (Objects.nonNull(logisticsBillCostEntity)){
                resultDTOS.add(BatchResultDTO.fail(id, entity.getTransportNo(), "该物流单已生成费用"));
                continue;
            }
            List<LogisticsBillDetailEntity> detailEntityList = logisticsBillDetailEntityList.stream().filter(e -> e.getMainId().equals(id)).collect(Collectors.toList());
            if (CollUtil.isEmpty(detailEntityList)){
                resultDTOS.add(BatchResultDTO.fail(id, entity.getTransportNo(), "该物流单无明细"));
                continue;
            }
            try {
                logisticsBillService.addLogisticsBillCost(entity, detailEntityList);
                resultDTOS.add(BatchResultDTO.success(entity.getId(), entity.getTransportNo()));
            } catch (Exception e) {
                log.error("生成自发货费用/尾程费用失败{}", e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getTransportNo(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);

    }
}

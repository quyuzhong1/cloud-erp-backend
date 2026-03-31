package com.erp.server.tms.controller.api;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsThirdChannelRefEntity;
import com.erp.server.tms.query.LogisticsThirdChannelRefQueryHandler;
import com.erp.server.tms.service.LogisticsBaseService;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.erp.server.tms.service.LogisticsThirdChannelRefService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 物流-第三方渠道关系表
 *
 * @author zdy
 * @since 2025-05-29
 */
@Slf4j
@RestController
@LogSystemModule("物流-第三方渠道关系表")
@RequestMapping("/logisticsThirdChannelRef")
public class LogisticsThirdChannelRefController extends BaseController {

    @Resource
    private LogisticsThirdChannelRefService logisticsThirdChannelRefService;
    @Resource
    private LogisticsBaseService logisticsBaseService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-05-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流-第三方渠道关系表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsThirdChannelRefDTO.AddDTO dto) {
        return success(logisticsThirdChannelRefService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-05-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流-第三方渠道关系表修改")
    public ApiResult<?> update(@RequestBody @Validated LogisticsThirdChannelRefDTO.UpdateDTO dto) {
        logisticsThirdChannelRefService.update(dto);
        return success();
    }


    /**
     * 分页
     *
     * @param dto
     * @author zdy
     * @date 2025-07-30
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = LogisticsThirdChannelRefQueryHandler.class)
    public ApiResult<PagingVO<LogisticsThirdChannelRefDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<LogisticsThirdChannelRefDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsThirdChannelRefDTO.PagingVO> pagingVO = logisticsThirdChannelRefService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 查看详情
     * @author zdy
     * @date: 2025/7/31 9:10
     * @param id
     * @return ApiResult<LogisticsThirdChannelRefDTO.ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<LogisticsThirdChannelRefDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(logisticsThirdChannelRefService.view(id));
    }

    /**
     * 批量删除
     * @author zdy
     * @date: 2025/7/31 9:10
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "物流-第三方渠道关系表删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<LogisticsThirdChannelRefEntity> entityList = logisticsThirdChannelRefService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            LogisticsThirdChannelRefEntity refEntity = entityList.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(refEntity)) {
                deleteResult = BatchResultDTO.fail(id, id, "物流-第三方渠道关系表数据不存在, 删除失败");
                resultDTOS.add(deleteResult);
                continue;
            }
            try {
                deleteResult = logisticsThirdChannelRefService.delete(refEntity);
            }catch (Exception e){
                log.error("物流-第三方渠道关系表删除失败",e);
                deleteResult = BatchResultDTO.fail(refEntity.getLogisticsChannelName(), refEntity.getLogisticsSupplierName(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出
     * @author zdy
     * @date: 2025/7/31 9:10
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "物流-第三方渠道关系表导出")
    public ApiResult<?> export(@RequestBody @Validated LogisticsThirdChannelRefDTO.PagingParamDTO dto) {
        return success(logisticsThirdChannelRefService.export(dto));
    }

    /**
     * 下载第三方渠道关系导入模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载第三方渠道关系导入模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        logisticsThirdChannelRefService.downloadTemplate(response);
        return success();
    }
    /**
     * 导入
     * @author zdy
     * @date: 2025/7/31 9:10
     * @param excelFile
     * @return ApiResult
     */
    @PostMapping("/importExcel")
    @LogAction(value = LogActionEnum.IMPORT, desc = "物流-第三方渠道关系表导入")
    public ApiResult<?> importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean b = logisticsThirdChannelRefService.importExcel(excelFile, response);
        return b ? success("导入成功") : failure("导入失败");
    }
    /**
     * 启用/停用
     * @author ZDY
     * @date: 2025/8/1 14:35
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "启用停用:idList={idList},状态值={disabled}(true=禁用,false=启用)")
    @PostMapping("/updateStatus")
    public ApiResult<List<BatchResultDTO>> updateStatus(@RequestBody @Validated LogisticsThirdChannelRefDTO.DisabledParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<LogisticsThirdChannelRefEntity> entityList = logisticsThirdChannelRefService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            LogisticsThirdChannelRefEntity refEntity = entityList.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(refEntity)) {
                submit = BatchResultDTO.fail(id, id, "物流-第三方渠道关系表不存在, 停用/启用失败");
                resultDTOS.add(submit);
                continue;
            }
            try {
                submit = logisticsThirdChannelRefService.updateStatus(id,dto.getDisabled());
            }catch (Exception e){
                log.error("物流-第三方渠道关系表 停用/启用失败",e);
                submit = BatchResultDTO.fail(refEntity.getLogisticsChannelName(), refEntity.getLogisticsSupplierName(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    @PostMapping("/registerLogisticsNumber")
    public void registerLogisticsNumber (){
        Integer registerStatus = 0;
        XxlJobHelper.log("====开始注册物流单号====");
        String jobParam = XxlJobHelper.getJobParam();
        List<String> trackNoList = new ArrayList<>();
        List<String> transportNoList = new ArrayList<>();
        String salesPlatform ="";
        // 获取物流编号 (发货日期须晚于 2026-03-01)
        LocalDateTime deliveryLimitTime = null;
        if (StringUtils.isNotBlank(jobParam)){
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(jobParam);
            trackNoList = jsonObject.getBeanList("trackNoList", String.class);
            transportNoList = jsonObject.getBeanList("transportNoList", String.class);
            registerStatus = jsonObject.getInt("registerStatus", 0);
            salesPlatform = jsonObject.getStr("salesPlatform");
            deliveryLimitTime = jsonObject.getLocalDateTime("deliveryTime", LocalDateTime.of(2026, 3, 1, 0, 0, 0));
        }
        long current = 1;
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .size(100)
                .current(current)
                .registerStatus(registerStatus)
                .trackEnable(true)
                .transportNoList(transportNoList)
                .trackNoList(trackNoList)
                .transportType(LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode())
                .deliveryTime(deliveryLimitTime)//2026-03-01之后
                .salesPlatform(salesPlatform)//shopify
                .build();
        getRegisterData(query);

        XxlJobHelper.log("====结束注册物流单号====");
    }

    @PostMapping("/registerOceanLogisticsNumber")
    public void registerOceanLogisticsNumber(){
        Integer registerStatus = 0;
        XxlJobHelper.log("====开始注册物流单号====");
        String jobParam = XxlJobHelper.getJobParam();
        List<String> trackNoList = new ArrayList<>();
        List<String> transportNoList = new ArrayList<>();
        String salesPlatform ="";
        // 获取物流编号 (发货日期须晚于 2026-03-01)
        LocalDateTime deliveryLimitTime = null;
        if (StringUtils.isNotBlank(jobParam)){
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(jobParam);
            trackNoList = jsonObject.getBeanList("trackNoList", String.class);
            transportNoList = jsonObject.getBeanList("transportNoList", String.class);
            registerStatus = jsonObject.getInt("registerStatus", 0);
            salesPlatform = jsonObject.getStr("salesPlatform");
            deliveryLimitTime = jsonObject.getLocalDateTime("deliveryTime", LocalDateTime.of(2026, 3, 1, 0, 0, 0));
        }
        long current = 1;
        LogisticsBillDetailQueryDTO query = LogisticsBillDetailQueryDTO.builder()
                .trackQueryMode(LogisticsPlatformEnum.TRACK123.getCode())
                .size(100)
                .current(current)
                .registerStatus(registerStatus)
                .trackEnable(true)
                .transportNoList(transportNoList)
                .trackNoList(trackNoList)
                .transportType(LogisticsTransportTypeEnum.OCEAN.getCode())
                .deliveryTime(deliveryLimitTime)//2026-03-01之后
                .salesPlatform(salesPlatform)//shopify
                .build();
        getRegisterData(query);

        XxlJobHelper.log("====结束注册物流单号====");
    }

    private void getRegisterData(LogisticsBillDetailQueryDTO query) {
        XxlJobHelper.log("获取列表请求参数：{}", JSON.toJSONString(query));
        // 从原来的 listTrackDto 切换为基于配置映射表的精确拉取 (弃用旧的 track_query_mode 字段依赖)
        List<LogisticsTrackDTO.UpdateTrackDTO> list = logisticsBillDetailService.listWaitingRegisterByConfig(query, query.getTrackQueryMode()).stream().filter(e->StringUtils.isNotBlank(e.getCfgId())).collect(Collectors.toList());
//        List<LogisticsTrackDTO.UpdateTrackDTO> list = logisticsBillDetailService.listTrackDto(query);
        XxlJobHelper.log("获取列表数：{}", list.size());
        // 获取第三方推送配置 (用于 processRegisterData 内部的具体字段映射匹配)
        List<LogisticsThirdChannelRefDTO.PagingVO> channelRefList = logisticsThirdChannelRefService.listByPlatform(LogisticsPlatformEnum.TRACK123.getCode());
        XxlJobHelper.log("获取第三方推送配置：{}", channelRefList.size());
        if (list.size() > MathUtil.NUMBER_100){
            //列表数据较多情况下，进行分割集合
            List<List<LogisticsTrackDTO.UpdateTrackDTO>> partition = ListUtil.partition(list, MathUtil.NUMBER_100);
            XxlJobHelper.log("拆分列表数：{}", partition.size());
            //物流商数据处理
            partition.forEach(e -> logisticsBaseService.processRegisterData(LogisticsPlatformEnum.TRACK123.getCode(),e, query.getTransportType(),channelRefList));
        }else {
            logisticsBaseService.processRegisterData(LogisticsPlatformEnum.TRACK123.getCode(), list,query.getTransportType(), channelRefList);
        }
        log.info("========同步物流轨迹数据完成==========");
    }
}

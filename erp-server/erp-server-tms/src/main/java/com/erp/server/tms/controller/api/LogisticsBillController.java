package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.enums.TrackQueryTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.server.tms.query.LogisticsBillQueryHandler;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.erp.server.tms.service.LogisticsBillService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsTrackService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public ApiResult exportExcel(@RequestBody @Valid LogisticsBillDTO.PagingParamDTO dto) {
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
        if (StringUtils.isBlank(trackNo) || StringUtils.isBlank(logisticsChannelId)){
            trackNo = transportNo;
        }
        if (StringUtils.isNotBlank(logisticsChannelId)){
            LogisticsChannelEntity channelEntity = logisticsChannelService.getById(logisticsChannelId);
            if (Objects.isNull(channelEntity) || StringUtils.isBlank(channelEntity.getTrackQueryType())
                    || !TrackQueryTypeEnum.TRACK_NO.getCode().equals(channelEntity.getTrackQueryType())){
                trackNo = transportNo;
            }
        }
        LogisticsTrackDTO.ViewDTO list = logisticsTrackService.listByTrackNo(trackNo);
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
     * 初始化历史物流单手机号数据
     *
     * @return
     */
    @PostMapping("/initLogisticsBillPhone")
    public ApiResult<LogisticsTrackDTO.ViewDTO> initLogisticsBillPhone(@RequestBody LogisticsBillDTO.BillPhoneDTO dto) {
        logisticsBillService.initLogisticsBillPhone(dto);
        return success();

    }
}

package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zdy
 * @ClassName LogisticsFeignController
 * @date 2023年11月03日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("物流feign接口")
@RequestMapping("/feign/logistics")
public class LogisticsFeignController {

    @Resource
    private LogisticsBaseService logisticsBaseService;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    private LogisticsAddressService logisticsAddressService;
    @Resource
    private LogisticsTrackService logisticsTrackService;

    @PostMapping("/queryOrderList")
    public List<LogisticsOrderResponseVO> queryOrderList(@RequestBody List<LogisticsQueryBaseVO> logisticsQueryVOList){
        return logisticsBaseService.queryOrderList(logisticsQueryVOList);
    }


    /**
     * 根据供应商id 获取对应渠道的信息
     * @param supplierId
     * @return
     */
    @PostMapping("/listBySupplierId")
    public List<BaseIdDTO.CodeDTO> listBySupplierId(@RequestBody String supplierId){
        return logisticsChannelService.listBySupplierId(supplierId);
    }

    /**
     * 根据物流商id 获取到对应的渠道的启用禁用列表
     * @Author Luo_WG
     * @Date 2024/2/1 20:33
     * @param logisticsSupplierIds
     * @return java.util.List<com.common.business.dto.base.BaseIdDTO.CodeDTO>
     **/
    @PostMapping("/listLogisticsChannel")
    public List<LogisticsChannelDTO.ListSelectDTO> listLogisticsChannel(@RequestBody List<String> logisticsSupplierIds) {
        return logisticsChannelService.listLogisticsChannel(new LogisticsChannelDTO.ParamDTO(logisticsSupplierIds));
    }

    /**
     * 更改物流商状态
     * @param dto
     * @return
     */
    @PostMapping("/updateDisabledBySupplierId")
    public Boolean updateDisabledBySupplierId(@RequestBody LogisticsSupplierDTO.UpdateDisabledDTO dto){
        return logisticsSupplierService.updateDisabledBySupplierId(dto);
    }

    /**
     * 获取渠道 根据渠道id
     * @param channelId
     * @return
     */
    @PostMapping("/getChannelById")
    public LogisticsChannelEntity getChannelById(@RequestBody String channelId){
        return logisticsChannelService.getById(channelId);
    }
    /**
     * 获取渠道 根据渠道名称
     * @param channelName
     * @return
     */
    @PostMapping("/getChannelByName")
    public List<LogisticsChannelEntity> getChannelByName(@RequestBody String channelName){
        return logisticsChannelService.getChannelByName(channelName);
    }
    /**
     * 获取渠道 根据渠道i
     * @param channelId
     * @return
     */
    @PostMapping("/getChannelInfoById")
    public LogisticsChannelDTO.BaseDTO getChannelInfoById(@RequestBody String channelId){
        return logisticsChannelService.getInfoById(channelId);
    }

    /**
     * 根据渠道id查询物流商信息
     * @Author Luo_WG
     * @Date 2023/12/15 15:45
     * @param channelIds
     * @return java.util.List<com.erp.model.tms.dto.LogisticsChannelDTO.BaseDTO>
     **/
    @PostMapping("/listChannelInfoById")
    public List<LogisticsChannelDTO.BaseDTO> listChannelInfoById(@RequestBody List<String> channelIds){
        return logisticsChannelService.listChannelInfoById(channelIds);
    }

    @GetMapping("/getLogisticsChannelConstraint")
    public LogisticsChannelDTO.LogisticsChannelConstraintDTO getLogisticsChannelConstraint(@RequestParam(value = "channelId")String channelId, @RequestParam(value = "country")String country) {
        return logisticsChannelService.getLogisticsChannelConstraint(channelId,country);
    }

    /**
     * @description
     * @param id
     * @return
     * @date 2024-02-18 11:54
     * @author Lambda
     */
    @GetMapping("/getLogisticsAddressById")
    public LogisticsAddressEntity getLogisticsAddressById(@RequestParam("id") String id) {
        return logisticsAddressService.getById(id);
    }

    @GetMapping("/getScaleChannelByChannelById")
    private LogisticsChannelDTO.SignShipDTO getScaleChannelByChannelById(@RequestParam("logisticsChannelId") String logisticsChannelId,
                                                                         @RequestParam("dictPlatform") String dictPlatform
    ) {
        return logisticsChannelService.getScaleChannelByChannelById(logisticsChannelId, dictPlatform);
    }
    /**
     * 根据地址类型获取地址列表
     *
     * @return
     */
    @PostMapping("/listAddressByType")
    public List<LogisticsAddressDTO.ListDTO> listAddressByType(@RequestBody @Validated LogisticsAddressDTO.AddressByTypeDTO dto) {
        return logisticsAddressService.listAddressByType(dto);
    }

    /**
     * 根据渠道汇总时间段内未更新运单号记录
     * @param query
     * @return
     */
    @PostMapping("/getWarnReportByChannel")
    public List<LogisticsChannelDTO.WarnReportDTO> getWarnReportByChannel(@RequestBody LogisticsBillDetailQueryDTO query){
        return logisticsChannelService.getWarnReportByChannel(query);
    }

    /**
     * 接收track123物流轨迹同步数据
     * @return
     */
    @PostMapping("/webhookByTrack123")
    public void webhookByTrack123(@RequestBody LogisticsTrackDTO.TrackWebHookDTO dto){
        logisticsTrackService.webhookByTrack123(dto);
    }
    /**
     * 根据渠道id ， 国家二字码，邮编判断是否属于偏远邮编组
     * @param
     * @return
     */
    @PostMapping("/estimateIsOutOfRangeDelivery")
    public Boolean estimateIsOutOfRangeDelivery(@RequestParam("logisticsChannelId")String logisticsChannelId, @RequestParam("country")String country, @RequestParam("postCode")String postCode){
        return logisticsChannelService.estimateIsOutOfRangeDelivery(logisticsChannelId, country, postCode);
    }
}

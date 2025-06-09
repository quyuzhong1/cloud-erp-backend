package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.entity.TransferDeclareEntity;
import com.erp.server.tms.service.TransferDeclareDetailService;
import com.erp.server.tms.service.TransferDeclareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("中转报关单feign接口")
@RequestMapping("/feign/transferDeclare")
public class TransferDeclareFeignController {

    @Resource
    private TransferDeclareService transferDeclareService;

    @Resource
    private TransferDeclareDetailService transferDeclareDetailService;

    /**
     * 新增中转报关单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/add")
    public BaseResultDTO.AddDTO add(@RequestBody TransferDeclareDTO.AddDTO dto) {
        return transferDeclareService.add(dto);
    }

    /**
     * 批量新增中转报关单
     * @param dtoList
     * @return
     */
    @PostMapping("/batchAdd")
    public List<BaseResultDTO.AddDTO> batchAdd(@RequestBody List<TransferDeclareDTO.AddDTO> dtoList) {
        return transferDeclareService.batchAdd(dtoList);
    }
    /**
     * @description 根据销售订单id 获取中转报关信息
     * @param soId 销售订单id
     * @author Lambda
     * @return
     * @create 2024-01-26 9:25
     */
    @GetMapping("/getBySoId")
    public TransferDeclareDetailEntity getBySoId(@RequestParam("soId") String soId){
        return transferDeclareDetailService.getBySoId(soId);
    }

    /**
     * 修改出库状态
     * @Author Luo_WG
     * @Date 2024/2/1 18:39
     * @param dto
     * @return java.lang.Boolean
     **/
//    @PostMapping("/updateOutstockStatus")
//    public Boolean updateOutstockStatus(@RequestBody TransferDeclareDTO.UpdateOutstockStatusDTO dto) {
//        return transferDeclareDetailService.updateOutstockStatus(dto);
//    }

    /**
     * 根据物流渠道id集合查询
     * @author Will
     * @date: 2024/4/1 12:32
     * @param logisticsChannelIdList
     * @return List<TransferDeclareDetailEntity>
     */
    @PostMapping("/listByLogisticsChannelIdList")
    public List<TransferDeclareDetailEntity> listByLogisticsChannelIdList(@RequestBody List<String> logisticsChannelIdList) {
        List<TransferDeclareDetailEntity> list = transferDeclareDetailService.listByLogisticsChannelIdList(logisticsChannelIdList);
        return list;
    }

    /**
     * b2c订单预报
     * 成功返回第三方code
     * 失败返回b2c订单code
     */
    @PostMapping("/b2cOrderForecast")
    public TransferDeclareDTO.ShippingOrderDTO b2cOrderForecast(@RequestBody @Validated TransferDeclareDTO.B2cOrderForecastDTO b2cOrderForecastDTO) {
        return transferDeclareService.b2cOrderForecast(b2cOrderForecastDTO);
    }

    /**
     * 取消订单预报
     * 失败返回原因
     */
    @PostMapping("/cancelOrderForecast")
    public ApiResult<String> cancelOrderForecast(@RequestBody TransferDeclareDTO.CancelOrderForecastDTO cancelOrderForecastDTO) {
        return transferDeclareService.cancelOrderForecast(cancelOrderForecastDTO);
    }

    /**
     * 通过销售单id查询
     */
    @PostMapping("/listBySoCodeList")
    public List<TransferDeclareDetailEntity> listBySoCodeList(@RequestBody List<String> soCodeList){
        return transferDeclareDetailService.listBySoCodeList(soCodeList);
    }

    /**
     * 批量修改报关单详情上传状态
     * @param list
     * @return
     */
    @PostMapping("/updateTransferStatusByBatch")
    public Boolean updateTransferStatusByBatch(@RequestBody List<TransferDeclareDTO.UpdateForcastStatusDTO> list) {
        return transferDeclareDetailService.updateTransferStatusByBatch(list);
    }
}

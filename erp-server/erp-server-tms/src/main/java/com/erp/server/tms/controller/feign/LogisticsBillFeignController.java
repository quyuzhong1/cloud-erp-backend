package com.erp.server.tms.controller.feign;

import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.erp.server.tms.service.LogisticsBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("物流单feign接口")
@RequestMapping("/feign/logisticsBill")
public class LogisticsBillFeignController {
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;

    /**
     * 新增物流单
     *
     * @param addDTOList
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/11/9 17:59
     **/
    @PostMapping("/logisticsBillBatchSave")
    public Boolean logisticsBillBatchSave(@RequestBody List<LogisticsBillDTO.AddDTO> addDTOList) {
        Boolean flag = logisticsBillService.logisticsBillBatchSave(addDTOList);
        return flag;
    }

    /**
     * 添加物流单
     *
     * @return java.lang.Boolean
     * @parms addDTO
     * @author yl
     * @date 2023-11-17
     */
    @PostMapping("/addLogisticsBill")
    public Boolean addLogisticsBill(@RequestBody LogisticsBillDTO.AddDTO addDTO) {
        Boolean flag = logisticsBillService.add(addDTO);
        return flag;
    }

    @PostMapping("/removeLogisticsBill")
    public Boolean removeLogisticsBill(@RequestBody LogisticsBillDTO.RemoveDTO dto) {
        Boolean flag = logisticsBillService.remove(dto);
        return flag;
    }

    /**
     * 根据来源id查询物流信息及跟踪号
     *
     * @param sourceIdList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsBillDTO.LogisticsBillVo>
     * @Author Luo_WG
     * @Date 2023/11/10 9:06
     **/
    @PostMapping("/listLogisticsBillVoBySourceIds")
    public List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoBySourceIds(@RequestBody List<String> sourceIdList) {
        List<LogisticsBillDTO.LogisticsBillVo> flag = logisticsBillService.listLogisticsBillVoBySourceIds(sourceIdList);
        return flag;
    }

    /**
     * 生成物流单 像物流商下單
     *
     * @return
     * @parms
     * @author yl
     * @date 2023-11-23
     */
    @PostMapping("/generateBill")
    public List<String> generateBill(@RequestBody @Valid LogisticsBillDTO.GenerateBillDTO dto) {
        return logisticsBillService.generateBill(dto);
    }
    
    /**
     * 取消物流单
     * @author yl
     * @date 2023-12-08 11:52
     * @param dto
     * @return 
     */
    @PostMapping("/cancelBill")
    public Boolean cancelBill(@RequestBody @Valid LogisticsBillDTO.CancelBillDTO dto) {
        return logisticsBillService.cancelBill(dto);
    }


    /**
     * 获取物流单数据 用于查询轨迹
     *
     * @param query
     * @return
     */
    @PostMapping("/getLogisticsBillDetails")
    public PagingVO<LogisticsBillDetailEntity> getLogisticsBillDetails(@RequestBody LogisticsBillDetailQueryDTO query) {
        PagingVO<LogisticsBillDetailEntity> page = logisticsBillDetailService.getPage(query);
        return page;
    }


    /**
     * 更改运单号
     *
     * @return
     * @parms
     * @author yl
     * @date 2023-11-24
     */
    @PostMapping("/updateTrackNo")
    public Boolean updateTrackNo(@RequestBody LogisticsBillDTO.UpdateTrackNoDTO billDTO) {
        Boolean result = logisticsBillDetailService.updateTrackNo(billDTO);
        return result;
    }

    /**
     * 根据物流跟踪单号查询物流单详情
     * @Author Luo_WG
     * @Date 2023/12/14 15:45
     * @param trackNo
     * @return com.erp.model.tms.dto.LogisticsBillDTO.BaseDTO
     **/
    @GetMapping("/getLogisticsBillByTrackNo")
    public LogisticsBillDTO.BaseDTO getLogisticsBillByTrackNo(@RequestParam(value = "trackNo") String trackNo) {
        LogisticsBillDTO.BaseDTO entity = logisticsBillService.getBaseByTrackNo(trackNo);
        return entity;
    }
}

package com.erp.server.oms.controller.feign;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.server.oms.service.SoB2cErrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * B2C销售订单异常表
 *
 * @author lambda
 * @since 2023-12-20
 */
@Slf4j
@RestController
@LogSystemModule("B2C销售订单异常表")
@RequestMapping("/feign/soB2cError")
public class SoB2cErrorFeignController extends BaseController {

    @Resource
    private SoB2cErrorService soB2cErrorService;

    /**
     * 添加异常信息
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public Boolean add(@RequestBody SoB2cErrorDTO.AddDTO dto) {
        return soB2cErrorService.add(dto);
    }

    /**
     * @param batchAdd
     * @return
     * @description 批量添加异常订单信息  一个请求中包含多个订单
     * @author zdy
     * @create 2023-12-20 11:06
     */
    @PostMapping("/batchAdd")
    public void batchAddSoB2cError(@RequestBody SoB2cErrorDTO.BatchAdd batchAdd){
        soB2cErrorService.batchAddSoB2cError(batchAdd);
    }

    /**
     * 删除异常信息
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public Boolean delete(@RequestBody SoB2cErrorDTO.DeleteDTO dto) {
        return soB2cErrorService.delete(dto);
    }

    /**
     * 删除明细异常信息
     * @param dto
     * @return
     */
    @PostMapping("/deleteDetail")
    public Boolean deleteDetail(@RequestBody SoB2cErrorDTO.DeleteDetailDTO dto) {
        return soB2cErrorService.deleteDetail(dto);
    }


    /**
     * 批量删除异常信息
     *
     * @param batchDeleteDTO
     * @return
     * @description
     * @author zdy
     * @create 2023-12-20 11:20
     */
    @PostMapping("/deleteErrorByMainIds")
    public void deleteErrorByMainIds(@RequestBody SoB2cErrorDTO.BatchDeleteDTO batchDeleteDTO){
        soB2cErrorService.deleteErrorByMainIds(batchDeleteDTO);
    }
    /**
     * 获取异常信息
     * @param
     * @return
     */
    @PostMapping("/getB2cError")
    public SoB2cErrorEntity getB2cError(@RequestParam("mainId")String mainId,@RequestParam("errorType") String  errorType) {
        return soB2cErrorService.getByMainIdAndType(mainId,errorType);
    }


    /**
     * 删除所有异常信息
     */
    @PostMapping("/deleteAll")
    public Boolean deleteAll(@RequestBody SoB2cErrorDTO.DeleteDetailDTO dto) {
        return soB2cErrorService.deleteAll(dto);
    }

    /**
     * 删除新增订单异常信息
     * @Author Luo_WG
     * @Date 2024/4/19 10:12
     * @param addAndDeleteDTO
     * @return void
     **/
    @PostMapping("/deleteAndAddErrorBatch")
    void deleteAndAddErrorBatch(@RequestBody SoB2cErrorDTO.AddAndDeleteDTO addAndDeleteDTO) {
        soB2cErrorService.deleteAndAddErrorBatch(addAndDeleteDTO);
    }
    /**
     * 获取销售订单全部异常汇总
     * @return
     */
    @PostMapping("/getB2CErrorReport")
    public List<SoB2cErrorDTO.TypeCountDTO> getB2CErrorReport(@RequestBody List<String> typeList){
        return soB2cErrorService.getB2CErrorReport(typeList);
    }

    /**
     * 获取异常信息
     * @param
     * @return
     */
    @PostMapping("/listSoB2cErrorByMainIds")
    public List<SoB2cErrorEntity> listSoB2cErrorByMainIds(@RequestBody List<String> errorSoIds) {
        return soB2cErrorService.listSoB2cErrorByMainIds(errorSoIds);
    }

}

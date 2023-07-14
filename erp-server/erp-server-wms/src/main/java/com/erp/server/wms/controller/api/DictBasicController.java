package com.erp.server.wms.controller.api;


import cn.hutool.core.util.StrUtil;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.server.wms.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 字典管理
 *
 * @author Lambda
 * @since 2023-03-16
 */
@Slf4j
@RestController
@RequestMapping("/dict")
public class DictBasicController extends BaseController {


    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private RedissonClient redissonClient;


    /**
     * 保存或者修改字典信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdateBatch")
    public ApiResult saveOrUpdate(@RequestBody @Validated List<DictBasicDTO.ListDTO> dto) {
        Boolean result = dictBasicService.saveOrUpdateDict(dto);
        return result == true ? success() : failure();
    }


    /**
     * 获取对应字典数据
     * warehouseType 仓库类型
     * qcProblemType 质检单 问题属性
     * handleModeType 质检单 处理措施
     * qcReportResult 质检单 质检报告结果
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<DictBasicDTO.ListDTO>> list(@RequestParam("key") String key) {
        List<DictBasicDTO.ListDTO> list = dictBasicService.getByKey(key);
        return success(list);
    }


    /**
     * 字典通用下拉列表
     * @param type transferType 调拨类型，transferDirection 调拨方向，instockType 入库类型，outstockType 出库类型，workType 事务类型，machineType 加工单类型，inventoryDirection 库存方向
     * @param remark 备注
     * @return
     */
    @GetMapping("/drop/down")
    public ApiResult<List<DictBasicDTO.DropDownDTO>> dictDropDown(@RequestParam(value = "type")String type, @RequestParam(value = "remark", required = false) String remark) {
        List<DictBasicDTO.DropDownDTO> result =  dictBasicService.listByType(type, remark);
        return success(result);
    }
    @Autowired
    private RedissonClient redisson;
    @GetMapping("/write")
    @ResponseBody
    public String writeValue(){
        IntStream.range(1, 100).forEach(i -> {
            new Thread(() -> {
                String s = redissonLock();
                log.info("获取到的值为：{}", s);
            }).start();
        });
        return "success";
    }

    private String redissonLock() {
        RLock lock = redissonClient.getLock("rw-lock");
        try {
            if (!lock.tryLock(10, TimeUnit.SECONDS)) {
                throw new RuntimeException(StrUtil.format("未获取到锁 {} ", Thread.currentThread().getId()));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String s = null;
        try {
            log.info("已获取到锁");
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            log.info("已解锁");
            lock.unlock();//解锁
        }
        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue(){
        RLock lock = redissonClient.getLock("rw-lock");
        if(!lock.tryLock()){
            throw new RuntimeException("未获取到锁");
        }
        String s = null;
//        try {
//            s = redisTemplate.opsForValue().get("writeValue");
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            rLock.unlock();
//        }

        return s;
    }

}

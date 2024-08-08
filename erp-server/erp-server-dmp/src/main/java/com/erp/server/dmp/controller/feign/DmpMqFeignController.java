package com.erp.server.dmp.controller.feign;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.core.anno.LogAction;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 中台发送MQFeign控制类
 *
 * @Author Cloud
 * @Date 2023/9/1 12:03
 **/

@Slf4j
@RestController
@RequestMapping("feign/dmp/")
public class DmpMqFeignController {
    @Resource
    private DmpPushTaskService dmpPushTaskService;

    /**
     * 发送MQ消息并保存任务
     * @param dto
     * @return
     */
    @PostMapping("/save/pushTask")
    public DmpPushTaskEntity saveTask(@RequestBody @Valid DmpPushTaskFeignDTO dto){
        DmpPushTaskEntity entity = dmpPushTaskService.saveTask(dto);
        return entity;
    }

    /**
     * 批量保存旺店通任务！
     * @param dtoList
     * @return
     */
    @PostMapping("/save/pushTaskList")
    public List<DmpPushTaskEntity> saveTaskList(@RequestBody @Valid List<DmpPushTaskFeignDTO> dtoList){
        List<DmpPushTaskEntity> resultList = dmpPushTaskService.saveWdtTaskList(dtoList);
        return resultList;
    }

    /**
     * 推送任务
     * @author Will
     * @date: 2024/5/14 9:38
     * @param list
     * @return Boolean
     */
    @PostMapping("/send/sendTask")
    public Boolean sendTask(@RequestBody  List<DmpPushTaskEntity> list){
        dmpPushTaskService.sendTask(list, 0);
        return Boolean.TRUE;
    }

    /**
     * 根据单个id查询推送任务
     * @param oneDTO
     * @return
     */
    @PostMapping("/getByParam")
    public DmpPushTaskEntity getByParam(@RequestBody @Valid DmpSyncTaskDTO.OneDTO oneDTO){
        DmpPushTaskEntity dmpPushTaskEntity = dmpPushTaskService.getByParam(oneDTO);
        return ObjectUtils.isEmpty(dmpPushTaskEntity) ? new DmpPushTaskEntity() : dmpPushTaskEntity;
    }

    /**
     * 根据多个id查询推送任务
     * @param listDTO
     * @return
     */
    @PostMapping("/listByParam")
    public List<DmpPushTaskEntity> listByParam(@RequestBody @Valid DmpSyncTaskDTO.ListDTO listDTO){
        List<DmpPushTaskEntity> list = dmpPushTaskService.listByParam(listDTO);
        return CollectionUtils.isEmpty(list) ? new ArrayList<>() : list;
    }

    /**
     * 根据多个来源ID查询推送任务
     * @param listDTO
     * @return
     */
    @PostMapping("/listByCodeParam")
    public List<DmpPushTaskEntity> listByCodeParam(@RequestBody @Valid DmpSyncTaskDTO.ListCodeDTO listDTO){
        List<DmpPushTaskEntity> list = dmpPushTaskService.listByCodeParam(listDTO);
        return CollectionUtils.isEmpty(list) ? new ArrayList<>() : list;
    }

    /**
     * 批量修改无需同步
     *
     * @param dto
     * @return ApiResult
     * @author hyj
     * @date 2024/4/11 16:51
     */
    @PostMapping(value = "/batchNoNeedSync")
    public Boolean batchNoNeedSync(@RequestBody BaseIdsDTO.IdsDTO dto) {
        return dmpPushTaskService.batchNoNeedSync(dto.getIds());
    }
    /**
     * 根据sourceId批量修改无需同步
     *
     * @param sourceIds
     * @return ApiResult
     * @author hyj
     */
    @PostMapping(value = "/batchNoNeedSyncBySourceId")
    public Boolean batchNoNeedSyncBySourceId(@RequestBody List<String> sourceIds) {
        return dmpPushTaskService.batchNoNeedSyncBySourceId(sourceIds);
    }

    /**
     * 根据sourceId重新同步
     *
     * @param sourceIds
     * @return
     */
    @PostMapping(value = "/batchSyncBySourceId")
    public Boolean batchSyncBySourceId(@RequestBody List<String> sourceIds) {
        return dmpPushTaskService.batchSyncBySourceId(sourceIds);
    }

    /**
     * 发送MQ延时等级3消息(10秒后消费)
     * @author Jim
     */
    @PostMapping("/send/delayLevel3SendTask")
    public Boolean delayLevel3SendTask(@RequestBody List<DmpPushTaskEntity> list){
        dmpPushTaskService.sendTask(list, 3);
        return Boolean.TRUE;
    }
}

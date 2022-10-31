package com.cloud.erp.chrome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.erp.chrome.constant.TaskState;
import com.cloud.erp.chrome.dto.MabangOrderDTO;
import com.cloud.erp.chrome.entity.MabanIncomeExpensesEntity;
import com.cloud.erp.chrome.entity.OrderGyyDeliverEntity;
import com.cloud.erp.chrome.entity.ScheduleTaskEntity;
import com.cloud.erp.chrome.handler.ConvertHandler;
import com.cloud.erp.chrome.mapper.MabanIncomeExpensesMapper;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.cloud.erp.chrome.service.CsvServer;
import com.cloud.erp.chrome.service.MabanService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-08-24
 */
@Service
public class MabanServiceImpl extends ServiceImpl<MabanIncomeExpensesMapper, MabanIncomeExpensesEntity> implements MabanService {


    @Resource
    private CsvServer csvServer;

    @Resource
    private ChromeTaskInfoService chromeTaskInfoService;


    @Resource
    private ConvertHandler convertHandler;

    /**
     * 导入马帮收支明显csv
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2022-08-24 14:04
     */
    @Override
    @Transactional
    public void importIncomeExpensesCsv(MabangOrderDTO dto) {
        try {
            chromeTaskInfoService.updateTaskState(dto.getTaskId(), TaskState.RECEIVEING);
            cleanExistsData(dto.getTaskId());

            MultipartFile file = dto.getFile();
            List<MabanIncomeExpensesEntity> convertList = csvServer.getObjectListByMultipartFile(file, MabanIncomeExpensesEntity.class);
            if (CollectionUtils.isNotEmpty(convertList)) {
                List<MabanIncomeExpensesEntity> saveList = convertList.stream().filter(m -> StringUtils.isNotBlank(m.getSkuInfo())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(saveList)) {
                    List<List<MabanIncomeExpensesEntity>> lists = convertHandler.splitList(saveList, 1000);
                    for (List<MabanIncomeExpensesEntity> list : lists) {
                        this.saveBatch(list);
                    }
                }
            }

            chromeTaskInfoService.updateTaskState(dto.getTaskId(), TaskState.FINISH);
        } catch (Exception e) {
            log.error("importIncomeExpensesCsv  出错了 e==" + e);
            throw new RuntimeException("importIncomeExpensesCsv  出错了 e==",e);
        }
    }

    // 删除已存在的数据
    private void cleanExistsData(Integer taskId) {
        ScheduleTaskEntity taskEntity= chromeTaskInfoService.getById(taskId);
        if(taskEntity==null){
            return;
        }
        Date startTime=taskEntity.getStartTime();
        Date endTime=taskEntity.getEndTime();

        // 删除已存在的数据
        this.remove(new LambdaQueryWrapper<MabanIncomeExpensesEntity>()
                .gt(MabanIncomeExpensesEntity::getOrderDate,startTime)
                .lt(MabanIncomeExpensesEntity::getOrderDate,endTime)
        );
    }

}

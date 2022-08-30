package com.cloud.erp.chrome.service.impl;

import com.cloud.erp.chrome.constant.TaskState;
import com.cloud.erp.chrome.dto.MabangOrderDTO;
import com.cloud.erp.chrome.entity.MabanIncomeExpensesEntity;
import com.cloud.erp.chrome.entity.YxkOrderEntity;
import com.cloud.erp.chrome.handler.ConvertHandler;
import com.cloud.erp.chrome.mapper.MabanIncomeExpensesMapper;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.cloud.erp.chrome.service.CsvServer;
import com.cloud.erp.chrome.service.MabanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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


    @Autowired
    private CsvServer csvServer;

    @Autowired
    private ChromeTaskInfoService chromeTaskInfoService;


    @Autowired
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
    public void importIncomeExpensesCsv(MabangOrderDTO dto) {
        try {
            MultipartFile file = dto.getFile();
            List<MabanIncomeExpensesEntity> saveList = csvServer.getObjectListByMultipartFile(file, MabanIncomeExpensesEntity.class);
            if (CollectionUtils.isNotEmpty(saveList)) {
                List<List<MabanIncomeExpensesEntity>> lists = convertHandler.splitList(saveList, 1000);
                for(List<MabanIncomeExpensesEntity> list:lists){
                    this.saveBatch(list);
                }
            }
            chromeTaskInfoService.updateTaskState(dto.getTaskId(), TaskState.FINISH);
        } catch (Exception e) {
            log.error("importIncomeExpensesCsv  出错了 e==" + e);
        }


    }
}

package com.erp.server.plm.api.kingdee;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.kingdee.bos.webapi.entity.OperateParam;
import com.kingdee.bos.webapi.entity.OperatorResult;
import com.kingdee.bos.webapi.sdk.K3CloudApi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/**
 * 销售出库单 测试
 */
public class KingdeeoutStockTest {
    public static void main(String[] args) {
        K3CloudApi client = new K3CloudApi();
        String formId="SAL_OUTSTOCK";
        String filePath = "C:\\Users\\Administrator\\Downloads\\马帮4月FBA出库单单号.txt";
        int batchSize = 1000;
        List<String> records = readTxtFile(filePath);
        List<List<String>> batches = splitIntoBatches(records, batchSize);
        for (List<String> batch : batches) {
            try {
//                unAudit(client,formId,batch);
                delete(client,formId,batch);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
//        ExecutorService executor = new ThreadPoolExecutor(
//                10, 20, 60L, TimeUnit.SECONDS,
//                new LinkedBlockingQueue<>(200), Executors.defaultThreadFactory(),
//                new ThreadPoolExecutor.CallerRunsPolicy()
//        );
//        List<CompletableFuture<List<String>>> futures = processBatches(batches, executor, client, formId);
//
//        // 等待所有任务完成，并收集结果
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//        List<String> finalResults = futures.stream()
//                .flatMap(future -> future.join().stream())
//                .collect(Collectors.toList());
//
//        finalResults.forEach(System.out::println);
//
//        executor.shutdown();
    }
    private static void unAudit(K3CloudApi client, String formId, List<String> numberList) throws Exception {
        OperateParam params =new OperateParam();
        params.setNumbers(numberList);
        OperatorResult operatorResult= client.unAudit(formId,params);
        if (!operatorResult.isSuccessfully()){
            if (CollUtil.isNotEmpty(operatorResult.getResult().getResponseStatus().getErrors())) {
                operatorResult.getResult().getResponseStatus().getErrors().stream()
                        .filter(item -> !JSONUtil.toJsonStr(item).contains("不存在"))
                        .forEach(item -> System.out.println("JSONUtil.toJsonStr(item) = " + JSONUtil.toJsonStr(item)));
            }
        }
        System.out.println("unAudit finished "+numberList.size());
    }
    private static void delete(K3CloudApi client, String formId, List<String> numberList) throws Exception {
        OperateParam params =new OperateParam();
        params.setNumbers(numberList);
        OperatorResult operatorResult= client.delete(formId,params);
//        System.out.println(operatorResult.isSuccessfully()+":"+JSONUtil.toJsonStr(operatorResult.getResult()));
        if (!operatorResult.isSuccessfully()){
            if (CollUtil.isNotEmpty(operatorResult.getResult().getResponseStatus().getErrors())) {
                operatorResult.getResult().getResponseStatus().getErrors().stream()
                        .filter(item -> !JSONUtil.toJsonStr(item).contains("不存在"))
                        .forEach(item -> System.out.println("JSONUtil.toJsonStr(item) = " + JSONUtil.toJsonStr(item)));
            }
        }
        System.out.println("delete finished "+numberList.size());
    }


    public static List<String> readTxtFile(String filePath) {
        List<String> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                records.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }
    public static List<List<String>> splitIntoBatches(List<String> records, int batchSize) {
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < records.size(); i += batchSize) {
            batches.add(new ArrayList<>(records.subList(i, Math.min(i + batchSize, records.size()))));
        }
        return batches;
    }
    public static List<CompletableFuture<List<String>>> processBatches(List<List<String>> batches, ExecutorService executor, K3CloudApi client, String formId) {
        List<CompletableFuture<List<String>>> futures = new ArrayList<>();
        for (List<String> batch : batches) {
            CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
                return processBatch(batch,client,formId);
            }, executor);
            futures.add(future);
        }
        return futures;
    }
    public static List<String> processBatch(List<String> batch, K3CloudApi client, String formId) {
                try {
//                    unAudit(client, formId, batch);
                    delete(client,formId,batch);
                    return new ArrayList<>();
                } catch (Exception e) {
//                    System.out.println("e = " + e + "record=" + batch);
                    return batch;
                }

    }
}

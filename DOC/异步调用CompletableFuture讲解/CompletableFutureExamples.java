package org.bsc.langgraph4j.examples;

import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * CompletableFuture 实际应用示例
 * 
 * 这个类展示了 CompletableFuture 的各种用法和最佳实践
 */
public class CompletableFutureExamples {

    // 模拟耗时操作
    private static String fetchData() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "数据获取完成";
    }

    // 模拟可能失败的操作
    private static String riskyOperation() {
        if (Math.random() > 0.5) {
            throw new RuntimeException("操作失败");
        }
        return "操作成功";
    }

    /**
     * 示例 1: 基础异步操作
     */
    public static void example1_BasicAsync() throws Exception {
        System.out.println("\n=== 示例 1: 基础异步操作 ===");
        
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("异步任务开始执行...");
            return fetchData();
        });
        
        // 等待结果
        String result = future.get();
        System.out.println("结果: " + result);
    }

    /**
     * 示例 2: 链式调用 - thenApply
     */
    public static void example2_Chain() throws Exception {
        System.out.println("\n=== 示例 2: 链式调用 ===");
        
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello")
            .thenApply(s -> s + " World")
            .thenApply(String::toUpperCase)
            .thenApply(s -> s + "!");
        
        System.out.println("结果: " + future.get());
    }

    /**
     * 示例 3: 异常处理 - exceptionally
     */
    public static void example3_ExceptionHandling() throws Exception {
        System.out.println("\n=== 示例 3: 异常处理 ===");
        
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            if (Math.random() > 0.5) {
                throw new RuntimeException("随机错误");
            }
            return "成功";
        })
        .exceptionally(ex -> {
            System.out.println("捕获异常: " + ex.getMessage());
            return "默认值";
        });
        
        System.out.println("结果: " + future.get());
    }

    /**
     * 示例 4: 异常处理 - handle
     */
    public static void example4_Handle() throws Exception {
        System.out.println("\n=== 示例 4: handle 方法 ===");
        
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            if (Math.random() > 0.5) {
                throw new RuntimeException("操作失败");
            }
            return "操作成功";
        })
        .handle((result, ex) -> {
            if (ex != null) {
                return "错误处理: " + ex.getMessage();
            }
            return "处理结果: " + result;
        });
        
        System.out.println("结果: " + future.get());
    }

    /**
     * 示例 5: 组合操作 - thenCompose
     */
    public static void example5_ThenCompose() throws Exception {
        System.out.println("\n=== 示例 5: thenCompose (串联) ===");
        
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "第一步");
        
        CompletableFuture<String> future2 = future1.thenCompose(s -> 
            CompletableFuture.supplyAsync(() -> s + " -> 第二步")
        );
        
        CompletableFuture<String> future3 = future2.thenCompose(s -> 
            CompletableFuture.supplyAsync(() -> s + " -> 第三步")
        );
        
        System.out.println("结果: " + future3.get());
    }

    /**
     * 示例 6: 合并操作 - thenCombine
     */
    public static void example6_ThenCombine() throws Exception {
        System.out.println("\n=== 示例 6: thenCombine (合并) ===");
        
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            return "任务1完成";
        });
        
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException e) {}
            return "任务2完成";
        });
        
        CompletableFuture<String> combined = future1.thenCombine(future2, (r1, r2) -> 
            r1 + " 和 " + r2
        );
        
        System.out.println("合并结果: " + combined.get());
    }

    /**
     * 示例 7: 等待所有操作 - allOf
     */
    public static void example7_AllOf() throws Exception {
        System.out.println("\n=== 示例 7: allOf (等待所有) ===");
        
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            return "任务1";
        });
        
        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException e) {}
            return "任务2";
        });
        
        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(800); } catch (InterruptedException e) {}
            return "任务3";
        });
        
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(task1, task2, task3);
        
        allTasks.thenRun(() -> {
            try {
                System.out.println("所有任务完成:");
                System.out.println("  " + task1.get());
                System.out.println("  " + task2.get());
                System.out.println("  " + task3.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        allTasks.get(); // 等待所有完成
    }

    /**
     * 示例 8: 等待任意一个操作 - anyOf
     */
    public static void example8_AnyOf() throws Exception {
        System.out.println("\n=== 示例 8: anyOf (等待任意一个) ===");
        
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            return "任务1完成";
        });
        
        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            return "任务2完成";
        });
        
        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException e) {}
            return "任务3完成";
        });
        
        CompletableFuture<Object> anyTask = CompletableFuture.anyOf(task1, task2, task3);
        
        System.out.println("最快完成的任务: " + anyTask.get());
    }

    /**
     * 示例 9: 消费操作 - thenAccept / thenRun
     */
    public static void example9_Consume() throws Exception {
        System.out.println("\n=== 示例 9: 消费操作 ===");
        
        // thenAccept: 接收结果并处理
        CompletableFuture.supplyAsync(() -> "Hello World")
            .thenAccept(result -> System.out.println("thenAccept: " + result))
            .get();
        
        // thenRun: 不接收结果，只执行操作
        CompletableFuture.supplyAsync(() -> "Hello World")
            .thenRun(() -> System.out.println("thenRun: 任务完成"))
            .get();
    }

    /**
     * 示例 10: 手动完成 Future
     */
    public static void example10_ManualComplete() throws Exception {
        System.out.println("\n=== 示例 10: 手动完成 Future ===");
        
        CompletableFuture<String> future = new CompletableFuture<>();
        
        // 在另一个线程中完成
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                future.complete("手动完成的结果");
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            }
        }).start();
        
        System.out.println("等待结果...");
        System.out.println("结果: " + future.get());
    }

    /**
     * 示例 11: 超时处理
     */
    public static void example11_Timeout() throws Exception {
        System.out.println("\n=== 示例 11: 超时处理 ===");
        
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000); // 模拟耗时操作
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "完成";
        });
        
        try {
            // 设置超时时间为 2 秒
            String result = future.get(2, TimeUnit.SECONDS);
            System.out.println("结果: " + result);
        } catch (TimeoutException e) {
            System.out.println("操作超时");
            future.cancel(true);
        }
    }

    /**
     * 示例 12: 使用自定义线程池
     */
    public static void example12_CustomExecutor() throws Exception {
        System.out.println("\n=== 示例 12: 自定义线程池 ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("线程: " + Thread.currentThread().getName());
            return "使用自定义线程池";
        }, executor);
        
        System.out.println("结果: " + future.get());
        
        executor.shutdown();
    }

    /**
     * 示例 13: 复杂链式调用
     */
    public static void example13_ComplexChain() throws Exception {
        System.out.println("\n=== 示例 13: 复杂链式调用 ===");
        
        CompletableFuture<String> future = CompletableFuture
            .supplyAsync(() -> {
                System.out.println("1. 获取数据");
                return "原始数据";
            })
            .thenApply(data -> {
                System.out.println("2. 转换数据");
                return data.toUpperCase();
            })
            .thenApply(data -> {
                System.out.println("3. 添加前缀");
                return "处理后的: " + data;
            })
            .thenCompose(data -> {
                System.out.println("4. 异步处理");
                return CompletableFuture.supplyAsync(() -> data + " -> 完成");
            })
            .exceptionally(ex -> {
                System.out.println("错误: " + ex.getMessage());
                return "默认值";
            });
        
        System.out.println("最终结果: " + future.get());
    }

    /**
     * 示例 14: 模拟 LangGraph4j 中的异步节点操作
     */
    public static void example14_LangGraphStyle() throws Exception {
        System.out.println("\n=== 示例 14: LangGraph4j 风格 ===");
        
        // 模拟同步操作
        Supplier<String> syncOperation = () -> {
            System.out.println("执行同步操作...");
            return "同步结果";
        };
        
        // 转换为异步操作（类似 AsyncNodeAction.node_async）
        CompletableFuture<String> asyncOperation = CompletableFuture.supplyAsync(() -> {
            CompletableFuture<String> result = new CompletableFuture<>();
            try {
                String value = syncOperation.get();
                result.complete(value);
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
            return result;
        }).thenCompose(f -> f);
        
        System.out.println("异步结果: " + asyncOperation.get());
    }

    /**
     * 示例 15: 并行处理并收集结果
     */
    public static void example15_ParallelProcessing() throws Exception {
        System.out.println("\n=== 示例 15: 并行处理 ===");
        
        // 创建多个并行任务
        CompletableFuture<String>[] tasks = new CompletableFuture[5];
        for (int i = 0; i < 5; i++) {
            final int index = i;
            tasks[i] = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(500 + index * 100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "任务" + (index + 1) + "完成";
            });
        }
        
        // 等待所有任务完成
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(tasks);
        
        allTasks.thenRun(() -> {
            System.out.println("所有任务完成，结果如下:");
            for (int i = 0; i < tasks.length; i++) {
                try {
                    System.out.println("  " + tasks[i].get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        allTasks.get();
    }

    /**
     * 运行所有示例
     */
    public static void main(String[] args) {
        try {
            example1_BasicAsync();
            example2_Chain();
            example3_ExceptionHandling();
            example4_Handle();
            example5_ThenCompose();
            example6_ThenCombine();
            example7_AllOf();
            example8_AnyOf();
            example9_Consume();
            example10_ManualComplete();
            example11_Timeout();
            example12_CustomExecutor();
            example13_ComplexChain();
            example14_LangGraphStyle();
            example15_ParallelProcessing();
            
            System.out.println("\n=== 所有示例执行完成 ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


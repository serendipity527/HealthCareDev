# CompletableFuture 详细讲解

## 目录
1. [什么是 CompletableFuture](#什么是-completablefuture)
2. [为什么需要 CompletableFuture](#为什么需要-completablefuture)
3. [基本用法](#基本用法)
4. [常用方法详解](#常用方法详解)
5. [在 LangGraph4j 中的应用](#在-langgraph4j-中的应用)
6. [实际代码示例](#实际代码示例)
7. [最佳实践](#最佳实践)

---

## 什么是 CompletableFuture

`CompletableFuture` 是 Java 8 引入的一个强大的异步编程工具类，它实现了 `Future` 和 `CompletionStage` 接口。

### 核心概念

- **Future**: 表示一个异步计算的结果
- **CompletionStage**: 表示异步计算的一个阶段，可以与其他阶段组合
- **CompletableFuture**: 既可以作为 Future 使用，也可以作为 CompletionStage 使用

### 特点

1. ✅ **非阻塞**: 不会阻塞调用线程
2. ✅ **链式调用**: 支持函数式编程风格
3. ✅ **异常处理**: 内置异常处理机制
4. ✅ **组合操作**: 可以组合多个异步操作
5. ✅ **回调支持**: 支持完成后的回调处理

---

## 为什么需要 CompletableFuture

### 传统方式的问题

#### 1. 同步阻塞方式
```java
// 问题：会阻塞当前线程
String result = doSomething(); // 等待完成
processResult(result);
```

#### 2. 传统 Future 方式
```java
// 问题：仍然需要阻塞等待结果
Future<String> future = executor.submit(() -> doSomething());
String result = future.get(); // 阻塞等待
processResult(result);
```

### CompletableFuture 的优势

```java
// 非阻塞，链式调用，优雅
CompletableFuture.supplyAsync(() -> doSomething())
    .thenApply(result -> processResult(result))
    .thenAccept(System.out::println);
```

---

## 基本用法

### 1. 创建 CompletableFuture

#### 方式一：使用 `supplyAsync`（有返回值）
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    // 异步执行的任务
    return "Hello World";
});
```

#### 方式二：使用 `runAsync`（无返回值）
```java
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    // 异步执行的任务
    System.out.println("Hello World");
});
```

#### 方式三：手动创建并完成
```java
CompletableFuture<String> future = new CompletableFuture<>();
// 在某个地方完成
future.complete("Hello World");
// 或者完成异常
future.completeExceptionally(new RuntimeException("Error"));
```

### 2. 获取结果

#### 阻塞获取
```java
String result = future.get(); // 可能抛出异常
```

#### 带超时的获取
```java
String result = future.get(5, TimeUnit.SECONDS);
```

#### 立即获取（不阻塞）
```java
String result = future.getNow("默认值"); // 如果未完成，返回默认值
```

---

## 常用方法详解

### 1. 转换操作（thenApply）

将结果转换为另一个值：

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello")
    .thenApply(s -> s + " World")
    .thenApply(String::toUpperCase);

// 结果: "HELLO WORLD"
```

### 2. 消费操作（thenAccept / thenRun）

处理结果但不返回新值：

```java
// thenAccept: 接收结果并处理
CompletableFuture.supplyAsync(() -> "Hello")
    .thenAccept(result -> System.out.println(result));

// thenRun: 不接收结果，只执行操作
CompletableFuture.supplyAsync(() -> "Hello")
    .thenRun(() -> System.out.println("完成"));
```

### 3. 组合操作（thenCompose）

将两个异步操作串联：

```java
CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> future2 = future1.thenCompose(s -> 
    CompletableFuture.supplyAsync(() -> s + " World")
);
// 结果: "Hello World"
```

### 4. 合并操作（thenCombine）

合并两个独立的异步操作：

```java
CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "World");

CompletableFuture<String> combined = future1.thenCombine(future2, (s1, s2) -> s1 + " " + s2);
// 结果: "Hello World"
```

### 5. 异常处理（exceptionally / handle）

```java
// exceptionally: 捕获异常并返回默认值
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    throw new RuntimeException("错误");
})
.exceptionally(ex -> "默认值");

// handle: 同时处理正常结果和异常
CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "Hello")
    .handle((result, ex) -> {
        if (ex != null) {
            return "错误处理";
        }
        return result.toUpperCase();
    });
```

### 6. 等待多个操作（allOf / anyOf）

```java
// allOf: 等待所有操作完成
CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> "Task 1");
CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> "Task 2");
CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() -> "Task 3");

CompletableFuture<Void> all = CompletableFuture.allOf(f1, f2, f3);
all.thenRun(() -> {
    // 所有任务都完成了
});

// anyOf: 等待任意一个操作完成
CompletableFuture<Object> any = CompletableFuture.anyOf(f1, f2, f3);
```

---

## 在 LangGraph4j 中的应用

### 1. AsyncNodeAction 中的使用

查看 `AsyncNodeAction.java`:

```java
@FunctionalInterface
public interface AsyncNodeAction<S extends AgentState> 
    extends Function<S, CompletableFuture<Map<String, Object>>> {
    
    CompletableFuture<Map<String, Object>> apply(S state);
    
    // 将同步操作转换为异步操作
    static <S extends AgentState> AsyncNodeAction<S> node_async(NodeAction<S> syncAction) {
        return t -> {
            CompletableFuture<Map<String, Object>> result = new CompletableFuture<>();
            try {
                result.complete(syncAction.apply(t)); // 完成 Future
            } catch (Exception e) {
                result.completeExceptionally(e); // 完成异常
            }
            return result;
        };
    }
}
```

**关键点：**
- 使用 `complete()` 设置成功结果
- 使用 `completeExceptionally()` 设置异常
- 将同步操作包装为异步操作

### 2. AsyncCommandAction 中的使用

查看 `AsyncCommandAction.java`:

```java
static <S extends AgentState> AsyncCommandAction<S> command_async(CommandAction<S> syncAction) {
    return (state, config) -> {
        var result = new CompletableFuture<Command>();
        try {
            result.complete(syncAction.apply(state, config));
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    };
}
```

### 3. AsyncEdgeAction 中的使用

查看 `AsyncEdgeAction.java`:

```java
static <S extends AgentState> AsyncEdgeAction<S> edge_async(EdgeAction<S> syncAction) {
    return t -> {
        CompletableFuture<String> result = new CompletableFuture<>();
        try {
            result.complete(syncAction.apply(t));
        } catch (Exception e) {
            result.completeExceptionally(e);
        }
        return result;
    };
}
```

### 4. 链式调用示例

在 `AsyncCommandAction` 中：

```java
static <S extends AgentState> AsyncCommandAction<S> of(AsyncEdgeAction<S> action) {
    return (state, config) ->
            action.apply(state).thenApply(Command::new); // 链式调用
}
```

这里使用了 `thenApply` 将 `String` 结果转换为 `Command` 对象。

---

## 实际代码示例

### 示例 1: 基础异步操作

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BasicExample {
    public static void main(String[] args) throws Exception {
        // 创建异步任务
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000); // 模拟耗时操作
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "任务完成";
        });
        
        // 处理结果
        future.thenAccept(result -> {
            System.out.println("结果: " + result);
        });
        
        // 等待完成
        future.get();
    }
}
```

### 示例 2: 异常处理

```java
public class ExceptionExample {
    public static void main(String[] args) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            if (Math.random() > 0.5) {
                throw new RuntimeException("随机错误");
            }
            return "成功";
        })
        .exceptionally(ex -> {
            System.out.println("捕获异常: " + ex.getMessage());
            return "默认值";
        })
        .thenApply(result -> {
            System.out.println("处理结果: " + result);
            return result.toUpperCase();
        });
        
        try {
            System.out.println("最终结果: " + future.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 示例 3: 组合多个异步操作

```java
public class CombineExample {
    public static void main(String[] args) throws Exception {
        // 任务1: 获取用户名
        CompletableFuture<String> getUserName = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            return "张三";
        });
        
        // 任务2: 获取用户年龄
        CompletableFuture<Integer> getUserAge = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            return 25;
        });
        
        // 组合两个任务
        CompletableFuture<String> combined = getUserName.thenCombine(
            getUserAge, 
            (name, age) -> name + " 今年 " + age + " 岁"
        );
        
        System.out.println(combined.get()); // 输出: 张三 今年 25 岁
    }
}
```

### 示例 4: 在 LangGraph4j 中的实际应用

```java
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

// 模拟一个异步节点操作
public class AsyncNodeExample {
    
    // 方式1: 直接返回 CompletableFuture
    AsyncNodeAction<AgentState> asyncAction1 = state -> {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        // 异步执行任务
        CompletableFuture.supplyAsync(() -> {
            // 模拟耗时操作
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return Map.of("result", "处理完成");
        }).thenAccept(future::complete)
          .exceptionally(ex -> {
              future.completeExceptionally(ex);
              return null;
          });
        
        return future;
    };
    
    // 方式2: 使用链式调用
    AsyncNodeAction<AgentState> asyncAction2 = state -> 
        CompletableFuture.supplyAsync(() -> {
            // 执行任务
            return Map.of("result", "处理完成");
        })
        .thenApply(result -> {
            // 进一步处理
            return Map.of("result", result.get("result"), "status", "success");
        });
    
    // 方式3: 从同步操作转换
    NodeAction<AgentState> syncAction = state -> {
        return Map.of("result", "同步处理");
    };
    
    AsyncNodeAction<AgentState> asyncAction3 = AsyncNodeAction.node_async(syncAction);
}
```

### 示例 5: 并行处理多个节点

```java
public class ParallelExample {
    public static void main(String[] args) throws Exception {
        // 创建多个异步任务
        CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            return "任务1完成";
        });
        
        CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException e) {}
            return "任务2完成";
        });
        
        CompletableFuture<String> task3 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(800); } catch (InterruptedException e) {}
            return "任务3完成";
        });
        
        // 等待所有任务完成
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(task1, task2, task3);
        
        allTasks.thenRun(() -> {
            System.out.println("所有任务完成:");
            try {
                System.out.println("  " + task1.get());
                System.out.println("  " + task2.get());
                System.out.println("  " + task3.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        allTasks.get(); // 等待完成
    }
}
```

---

## 最佳实践

### 1. 总是处理异常

```java
// ❌ 不好：没有异常处理
CompletableFuture.supplyAsync(() -> doSomething())
    .thenApply(result -> process(result));

// ✅ 好：有异常处理
CompletableFuture.supplyAsync(() -> doSomething())
    .thenApply(result -> process(result))
    .exceptionally(ex -> {
        log.error("处理失败", ex);
        return defaultValue;
    });
```

### 2. 使用自定义线程池

```java
// ❌ 不好：使用默认的 ForkJoinPool
CompletableFuture.supplyAsync(() -> doSomething());

// ✅ 好：使用自定义线程池
ExecutorService executor = Executors.newFixedThreadPool(10);
CompletableFuture.supplyAsync(() -> doSomething(), executor);
```

### 3. 避免阻塞调用

```java
// ❌ 不好：阻塞调用
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
String result = future.get(); // 阻塞！

// ✅ 好：使用回调
CompletableFuture.supplyAsync(() -> "Hello")
    .thenAccept(result -> processResult(result));
```

### 4. 合理使用链式调用

```java
// ✅ 好的链式调用
CompletableFuture.supplyAsync(() -> fetchData())
    .thenApply(data -> transform(data))
    .thenApply(transformed -> validate(transformed))
    .thenAccept(validated -> save(validated))
    .exceptionally(ex -> {
        handleError(ex);
        return null;
    });
```

### 5. 在 LangGraph4j 中的实践

```java
// ✅ 好的做法：将同步操作转换为异步
NodeAction<State> syncAction = state -> {
    // 同步处理逻辑
    return Map.of("result", "处理完成");
};

// 转换为异步操作
AsyncNodeAction<State> asyncAction = AsyncNodeAction.node_async(syncAction);

// ✅ 好的做法：真正的异步操作
AsyncNodeAction<State> realAsyncAction = state -> 
    CompletableFuture.supplyAsync(() -> {
        // 真正的异步处理（如调用外部API）
        return Map.of("result", "异步处理完成");
    });
```

---

## 总结

### CompletableFuture 的核心优势

1. **非阻塞**: 不会阻塞调用线程，提高并发性能
2. **链式调用**: 支持函数式编程，代码更优雅
3. **组合能力**: 可以轻松组合多个异步操作
4. **异常处理**: 内置完善的异常处理机制
5. **灵活性**: 既可以同步等待，也可以异步回调

### 在 LangGraph4j 中的作用

- **异步节点操作**: `AsyncNodeAction` 使用 `CompletableFuture` 实现异步节点处理
- **异步边操作**: `AsyncEdgeAction` 使用 `CompletableFuture` 实现异步路由决策
- **同步转异步**: 提供了便捷的方法将同步操作转换为异步操作
- **异常处理**: 统一的异常处理机制，确保图的稳定运行

### 学习建议

1. 从简单的 `supplyAsync` 和 `thenApply` 开始
2. 理解 `complete()` 和 `completeExceptionally()` 的作用
3. 掌握链式调用的组合方式
4. 学习异常处理的最佳实践
5. 在实际项目中应用，特别是异步 I/O 操作

---

## 参考资源

- [Java CompletableFuture 官方文档](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html)
- [LangGraph4j AsyncNodeAction 源码](../langgraph4j-core/src/main/java/org/bsc/langgraph4j/action/AsyncNodeAction.java)
- [LangGraph4j AsyncEdgeAction 源码](../langgraph4j-core/src/main/java/org/bsc/langgraph4j/action/AsyncEdgeAction.java)


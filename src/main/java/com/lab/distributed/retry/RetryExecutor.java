package com.lab.distributed.retry;

import java.util.function.Supplier;

public interface RetryExecutor {

    <T> T execute(RetryPolicy policy, Supplier<T> action);
}

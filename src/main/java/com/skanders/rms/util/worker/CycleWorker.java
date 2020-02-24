/*
 * Copyright (c) 2020 Alexander Iskander
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skanders.rms.util.worker;

import com.skanders.rms.base.result.RMSResult;
import com.skanders.rms.base.result.Result;
import com.skanders.rms.def.verify.RMSVerify;
import com.skanders.rms.util.worker.def.WorkerState;
import com.skanders.rms.util.worker.request.CycleWorkerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CycleWorker
{
    private static final Logger LOG = LoggerFactory.getLogger(CycleWorker.class);

    private static final Long START_DELAY_NONE = 0L;
    private static final Integer SINGLE_CORE_POOL = 1;

    private final ScheduledExecutorService worker;
    private final Runnable func;
    private final String name;

    private Duration cyclePeriod;
    private WorkerState state;
    private Future<?> futureFunc;

    private CycleWorker(@Nonnull Duration cyclePeriod, @Nonnull Runnable func, @Nonnull String name)
    {
        RMSVerify.checkNull(cyclePeriod, "cyclePeriod cannot be null");
        RMSVerify.checkNull(func, "func cannot be null");
        RMSVerify.checkNull(name, "name cannot be null");

        this.worker = Executors.newScheduledThreadPool(SINGLE_CORE_POOL);
        this.func = func;
        this.name = name;

        this.cyclePeriod = cyclePeriod;
        this.state = WorkerState.NONE;
        this.futureFunc = null;
    }

    public static CycleWorker create(@Nonnull Duration cyclePeriod, @Nonnull Runnable func, @Nonnull String name)
    {
        return new CycleWorker(cyclePeriod, func, name);
    }

    public Result handler(CycleWorkerRequest request)
    {
        switch (request.getRequestState()) {
            case START:
                return start();
            case STOP:
                return stop();
            case INVOKE:
                return invoke();
            case DURATION:
                return updatePeriod(request.getDuration());
            case STATUS:
                return getStatus();
            default:
                LOG.error("Invalid worker request given");
                return Result.exception("Invalid Worker Request");
        }
    }

    public Result start()
    {
        switch (state) {
            case NONE:
                startWorker();
                return RMSResult.WORKER_STARTED;
            case WORKING:
                return RMSResult.WORKER_ALREADY_STARTED;
            case STOPPED:
                startWorker();
                return RMSResult.WORKER_RESTARTED;
            default:
                LOG.error("Invalid worker request given");
                return Result.exception("Invalid Worker Request");
        }
    }

    public Result stop()
    {
        switch (state) {
            case NONE:
                return RMSResult.WORKER_HAS_NOT_STARTED;
            case WORKING:
                stopWorker();
                return RMSResult.WORKER_STOPPED;
            case STOPPED:
                return RMSResult.WORKER_ALREADY_STOPPED;
            default:
                LOG.error("Invalid worker request given");
                return Result.exception("Invalid Worker Request");
        }
    }

    public Result invoke()
    {
        switch (state) {
            case NONE:
                return RMSResult.WORKER_HAS_NOT_STARTED;
            case WORKING:
                invokeWorker();
                return RMSResult.WORKER_INVOKED;
            case STOPPED:
                return RMSResult.WORKER_CANNOT_INVOKE;
            default:
                LOG.error("Invalid worker request given");
                return Result.exception("Invalid Worker Request");
        }
    }

    public Result updatePeriod(Duration duration)
    {
        switch (state) {
            case NONE:
                this.cyclePeriod = duration;
                startWorker();
                return RMSResult.WORKER_STARTED_DURATION;
            case WORKING:
                stopWorker();
                this.cyclePeriod = duration;
                startWorker();
                return RMSResult.WORKER_RESTARTED_DURATION;
            case STOPPED:
                this.cyclePeriod = duration;
                startWorker();
                return RMSResult.WORKER_RESTARTED_DURATION;
            default:
                LOG.error("Invalid worker request given");
                return Result.exception("Invalid Worker Request");
        }
    }

    public Result getStatus()
    {
        switch (state) {
            case NONE:
                return RMSResult.WORKER_STATUS_NONE;
            case WORKING:
                return RMSResult.WORKER_STATUS_WORKING;
            case STOPPED:
                return RMSResult.WORKER_STATUS_STOPPED;
            default:
                LOG.error("Invalid worker request given");
                return Result.exception("Invalid Worker Request");
        }
    }

    public WorkerState getState()
    {
        return state;
    }

    public String getName()
    {
        return name;
    }

    private void startWorker()
    {
        futureFunc = worker.scheduleAtFixedRate(func, START_DELAY_NONE, cyclePeriod.toMillis(), TimeUnit.MILLISECONDS);
        this.state = WorkerState.WORKING;
    }

    private void stopWorker()
    {
        futureFunc.cancel(true);
        this.state = WorkerState.STOPPED;
    }

    private void invokeWorker()
    {
        worker.execute(func);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CycleWorker that = (CycleWorker) o;
        return worker.equals(that.worker) &&
                func.equals(that.func) &&
                name.equals(that.name) &&
                cyclePeriod.equals(that.cyclePeriod) &&
                state == that.state &&
                Objects.equals(futureFunc, that.futureFunc);
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(worker, func, name, cyclePeriod, state, futureFunc);
    }
}

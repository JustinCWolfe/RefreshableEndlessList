package com.refreshableendlesslist;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;

public abstract class CompleteListenerTask<T> extends AsyncTask<Void, Void, T>
{
    public interface CompleteListener<T>
    {
        void onComplete(TaskResult<T> taskResult);
    }

    public enum TaskStatus {
        PASSED(true), FAILED(false);

        private final boolean value;

        private static final Map<Boolean, TaskStatus> VALUE_TO_ENUM_MAP;

        static {
            VALUE_TO_ENUM_MAP = new HashMap<>();
            for (TaskStatus taskStatusEnum : EnumSet.allOf(TaskStatus.class)) {
                VALUE_TO_ENUM_MAP.put(taskStatusEnum.value, taskStatusEnum);
            }
        }

        TaskStatus(boolean value)
        {
            this.value = value;
        }

        public static TaskStatus getEnumFromValue(boolean value)
        {
            return VALUE_TO_ENUM_MAP.get(value);
        }
    }

    public static class TaskResult<T>
    {
        public final TaskStatus status;

        public final T result;

        public final CompleteListenerTask<T> originalTask;

        public TaskResult(TaskStatus status, T result)
        {
            this(status, result, null);
        }

        public TaskResult(TaskStatus status, CompleteListenerTask<T> originalTask)
        {
            this(status, null, originalTask);
        }

        public TaskResult(TaskStatus status, T result, CompleteListenerTask<T> originalTask)
        {
            this.status = status;
            this.result = result;
            this.originalTask = originalTask;
        }
    }

    private static final String TASK_EXCEPTION_THREAD_NAME = "CompleteListener-UnhandledException";

    private final CompleteListener<T> onCompleteListener;

    private Throwable taskException;

    protected CompleteListenerTask(CompleteListener<T> onCompleteListener)
    {
        this.onCompleteListener = onCompleteListener;
    }

    protected Throwable getTaskException()
    {
        return taskException;
    }

    protected abstract String getTaskName();

    protected abstract T doInBackgroundInternal(Void... params) throws Exception;

    @Override
    protected T doInBackground(Void... params)
    {
        T result = null;
        try {
            Thread.currentThread().setName(getTaskName());
            result = doInBackgroundInternal();
        } catch (Exception e) {
            taskException = e;
        }
        return result;
    }

    protected abstract boolean getTaskWasSuccessful(T result);

    @Override
    protected void onPostExecute(final T result)
    {
        boolean wasSuccessful = getTaskWasSuccessful(result);
        if (onCompleteListener != null) {
            TaskStatus status = taskException != null
                    ? TaskStatus.getEnumFromValue(false)
                    : TaskStatus.getEnumFromValue(wasSuccessful);
            onCompleteListener.onComplete(new TaskResult<>(status, result, this));
        }
        if (taskException != null && taskException instanceof RuntimeException) {
            // Throw this exception on a new thread, since the thread will die as a result of this process.
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
		            throw (RuntimeException) taskException;
                }
            }, TASK_EXCEPTION_THREAD_NAME).start();
        }
    }
}


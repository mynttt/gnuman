package de.hshannover.inform.gnuman.app.model.coordination;

import java.util.HashMap;

/**
 * Deal with timed ghost tasks.
 * @author Marc Herschel
 */

public class TimedTasks {
    private HashMap<String, Long> tasks;

    /**
     * Construct a timed task object.
     */
    public TimedTasks() { tasks = new HashMap<>(); }

    /**
     * Set a task only if no task with the same id exists.
     * @param id of the tasks.
     * @param timedelta time in ms the tasks expires from now.
     */
    public void createTask(String id, long timedelta) {
        tasks.computeIfAbsent(id, task -> System.currentTimeMillis() + timedelta);
    }

    /**
     * Set a task and override a task with the same id if it exists.
     * @param id of the tasks.
     * @param timedelta time in ms the tasks expires from now.
     */
    public void createOrOverrideTask(String id, long timedelta) {
        tasks.compute(id, (task, expires) -> System.currentTimeMillis() + timedelta);
    }

    /**
     * Either create a task or extend it if it exists.
     * @param id id of the task
     * @param timedelta time in ms the tasks expires from now.
     */
    public void createOrExtendTask(String id, long timedelta) {
        if(tasks.containsKey(id)) {
            tasks.compute(id, (task, expires) -> expires + timedelta);
        } else {
            createTask(id, timedelta);
        }
    }

    /**
     * Check if the task has been finished. Once finished the task will be removed.
     * @param id of task to query for.
     * @return true if task finished.
     */
    public boolean isFinished(String id) {
        if(tasks.containsKey(id)) {
            if(System.currentTimeMillis() > tasks.get(id)) { tasks.remove(id); return true; }
        } else {
            return true;
        }
        return false;
    }

    /**
     * Clear all ongoing timed tasks.
     */
    public void clearTasks() { tasks.clear(); }

    /**
     * Adjust time delta if paused.
     * @param timeDelta to adjust for.
     */
    public void adjustDeltaTimeForAllTasks(long timeDelta) {
        tasks.replaceAll((id, expires) -> expires + timeDelta);
    }

    /**
     * Cancel a task.
     * @param id task to cancel
     */
    public void cancelTask(String id) { tasks.remove(id); }

    /**
     * If a tasks exists.
     * @param id to queue for.
     * @return true if task exists.
     */
    public boolean taskExists(String id) {
        return tasks.containsKey(id);
    }

    /**
     * Return the remaining time of a task.
     * @param id of the task.
     * @return the time, 0 if not existing.
     */
    public long getTimeLeftFor(String id) {
        if(tasks.containsKey(id)) { return tasks.get(id) - System.currentTimeMillis(); }
        return 0;
    }

}

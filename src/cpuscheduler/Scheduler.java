package cpuscheduler;

import java.util.List;
import java.util.ArrayList;

public interface Scheduler {
    void schedule(List<Process> processes, List<GanttEntry> ganttEntries, int quantum);
}
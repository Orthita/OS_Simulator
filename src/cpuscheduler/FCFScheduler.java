package cpuscheduler;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class FCFScheduler implements Scheduler {
    @Override
    public void schedule(List<Process> processes, List<GanttEntry> ganttEntries, int quantum) {
        ArrayList<Process> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = 0;
        for (Process p : sorted) {
            if (currentTime < p.arrivalTime) {
                currentTime = p.arrivalTime;
            }
            int sliceStart = currentTime;
            p.startTime = currentTime;
            p.completionTime = currentTime + p.remainingTime;
            ganttEntries.add(new GanttEntry(p.pid, sliceStart, p.completionTime));
            p.turnaroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.startTime - p.arrivalTime;
            p.responseTime = p.startTime - p.arrivalTime;
            currentTime = p.completionTime;
        }
    }
}
package cpuscheduler;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PriorityNonPreemptiveScheduler implements Scheduler {
    @Override
    public void schedule(List<Process> processes, List<GanttEntry> ganttEntries, int quantum) {
        ArrayList<Process> processesCopy = new ArrayList<>(processes);
        processesCopy.sort(Comparator.comparingInt(p -> p.arrivalTime));

        PriorityQueue<Process> readyQueue = new PriorityQueue<>(
                Comparator.comparingInt((Process p) -> p.priority)
                        .thenComparingInt(p -> p.arrivalTime)
        );

        int currentTime = 0;
        int index = 0;

        while (index < processesCopy.size() || !readyQueue.isEmpty()) {
            while (index < processesCopy.size() && processesCopy.get(index).arrivalTime <= currentTime) {
                readyQueue.add(processesCopy.get(index));
                index++;
            }

            if (readyQueue.isEmpty()) {
                if (index < processesCopy.size()) {
                    currentTime = processesCopy.get(index).arrivalTime;
                }
                continue;
            }

            Process current = readyQueue.poll();
            int sliceStart = currentTime;
            current.startTime = currentTime;
            current.completionTime = currentTime + current.remainingTime;
            ganttEntries.add(new GanttEntry(current.pid, sliceStart, current.completionTime));
            current.turnaroundTime = current.completionTime - current.arrivalTime;
            current.waitingTime = current.startTime - current.arrivalTime;
            current.responseTime = current.startTime - current.arrivalTime;
            currentTime = current.completionTime;
        }
    }
}
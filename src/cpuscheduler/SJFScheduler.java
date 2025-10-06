package cpuscheduler;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class SJFScheduler implements Scheduler {
    @Override
    public void schedule(List<Process> processes, List<GanttEntry> ganttEntries, int quantum) {
        ArrayList<Process> processesCopy = new ArrayList<>(processes);
        processesCopy.sort(Comparator.comparingInt(p -> p.arrivalTime));

        PriorityQueue<Process> readyQueue = new PriorityQueue<>((p1, p2) -> {
            if (p1.burstTime != p2.burstTime) {
                return Integer.compare(p1.burstTime, p2.burstTime);
            }
            return Integer.compare(p1.arrivalTime, p2.arrivalTime);
        });

        int currentTime = 0;
        int completed = 0;
        int index = 0;
        int n = processesCopy.size();

        while (completed < n) {
            while (index < n && processesCopy.get(index).arrivalTime <= currentTime) {
                readyQueue.offer(processesCopy.get(index));
                index++;
            }

            if (readyQueue.isEmpty()) {
                if (index < n) {
                    currentTime = processesCopy.get(index).arrivalTime;
                } else {
                    break;
                }
            } else {
                Process current = readyQueue.poll();
                int sliceStart = currentTime;
                if (current.startTime == -1) {
                    current.startTime = currentTime;
                }
                current.completionTime = currentTime + current.remainingTime;
                ganttEntries.add(new GanttEntry(current.pid, sliceStart, current.completionTime));
                current.turnaroundTime = current.completionTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
                current.responseTime = current.startTime - current.arrivalTime;
                currentTime = current.completionTime;
                completed++;
            }
        }
    }
}
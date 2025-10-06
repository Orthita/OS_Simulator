package cpuscheduler;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Queue;
import java.util.LinkedList;

public class RoundRobinScheduler implements Scheduler {
    @Override
    public void schedule(List<Process> processes, List<GanttEntry> ganttEntries, int quantum) {
        ArrayList<Process> processesCopy = new ArrayList<>(processes);
        processesCopy.sort(Comparator.comparingInt(p -> p.arrivalTime));

        Queue<Process> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int index = 0;

        while (!readyQueue.isEmpty() || index < processesCopy.size()) {
            while (index < processesCopy.size() && processesCopy.get(index).arrivalTime <= currentTime) {
                readyQueue.add(processesCopy.get(index));
                index++;
            }

            if (readyQueue.isEmpty()) {
                if (index < processesCopy.size()) {
                    currentTime = processesCopy.get(index).arrivalTime;
                    readyQueue.add(processesCopy.get(index));
                    index++;
                }
                continue;
            }

            Process current = readyQueue.poll();
            if (current.startTime == -1) {
                current.startTime = currentTime;
            }

            int sliceStart = currentTime;
            int executionTime = Math.min(quantum, current.remainingTime);
            current.remainingTime -= executionTime;
            currentTime += executionTime;

            while (index < processesCopy.size() && processesCopy.get(index).arrivalTime <= currentTime) {
                readyQueue.add(processesCopy.get(index));
                index++;
            }

            ganttEntries.add(new GanttEntry(current.pid, sliceStart, currentTime));

            if (current.remainingTime > 0) {
                readyQueue.add(current);
            } else {
                current.completionTime = currentTime;
                current.turnaroundTime = current.completionTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
                current.responseTime = current.startTime - current.arrivalTime;
            }
        }
    }
}
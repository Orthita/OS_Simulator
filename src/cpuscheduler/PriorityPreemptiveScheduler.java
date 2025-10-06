package cpuscheduler;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PriorityPreemptiveScheduler implements Scheduler {
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
        Process currentProcess = null;
        int sliceStart = -1;

        while (index < processesCopy.size() || !readyQueue.isEmpty() || currentProcess != null) {
            while (index < processesCopy.size() && processesCopy.get(index).arrivalTime <= currentTime) {
                readyQueue.add(processesCopy.get(index));
                index++;
            }

            if (currentProcess != null && !readyQueue.isEmpty()) {
                Process highestPriority = readyQueue.peek();
                if (highestPriority.priority < currentProcess.priority) {
                    ganttEntries.add(new GanttEntry(currentProcess.pid, sliceStart, currentTime));
                    readyQueue.add(currentProcess);
                    currentProcess = null;
                    sliceStart = currentTime;
                }
            }

            if (currentProcess == null && !readyQueue.isEmpty()) {
                currentProcess = readyQueue.poll();
                if (currentProcess.startTime == -1) {
                    currentProcess.startTime = currentTime;
                }
                sliceStart = currentTime;
            }

            if (currentProcess == null) {
                currentTime++;
                continue;
            }

            currentProcess.remainingTime--;
            currentTime++;

            if (currentProcess.remainingTime == 0) {
                ganttEntries.add(new GanttEntry(currentProcess.pid, sliceStart, currentTime));
                currentProcess.completionTime = currentTime;
                currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                currentProcess.responseTime = currentProcess.startTime - currentProcess.arrivalTime;
                currentProcess = null;
                sliceStart = currentTime;
            }
        }
    }
}
package cpuscheduler;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class SRTFScheduler implements Scheduler {
    @Override
    public void schedule(List<Process> processes, List<GanttEntry> ganttEntries, int quantum) {
        ArrayList<Process> processesCopy = new ArrayList<>(processes);
        processesCopy.sort(Comparator.comparingInt(p -> p.arrivalTime));

        Comparator<Process> comp = Comparator.comparingInt((Process p) -> p.remainingTime)
                .thenComparingInt(p -> p.arrivalTime);
        java.util.List<Process> readyQueue = new ArrayList<>();

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
                Process shortest = readyQueue.stream().min(comp).orElse(null);
                if (shortest != null && shortest.remainingTime < currentProcess.remainingTime) {
                    ganttEntries.add(new GanttEntry(currentProcess.pid, sliceStart, currentTime));
                    readyQueue.add(currentProcess);
                    currentProcess = null;
                    sliceStart = currentTime;
                }
            }

            if (currentProcess == null && !readyQueue.isEmpty()) {
                currentProcess = readyQueue.stream().min(comp).orElse(null);
                if (currentProcess != null) {
                    readyQueue.remove(currentProcess);
                    if (currentProcess.startTime == -1) {
                        currentProcess.startTime = currentTime;
                    }
                    sliceStart = currentTime;
                }
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
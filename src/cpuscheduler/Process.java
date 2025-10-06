package cpuscheduler;

public class Process {
    public String pid;
    public int arrivalTime, burstTime, priority;
    public int startTime, completionTime, turnaroundTime, waitingTime, responseTime;
    public int remainingTime;

    public Process(String pid, int arrival, int burst, int priority) {
        this.pid = pid;
        this.arrivalTime = arrival;
        this.burstTime = burst;
        this.priority = priority;
        reset();
    }

    public void reset() {
        startTime = -1;
        completionTime = -1;
        turnaroundTime = -1;
        waitingTime = -1;
        responseTime = -1;
        remainingTime = burstTime;
    }
}
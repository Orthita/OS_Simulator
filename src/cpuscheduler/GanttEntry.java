package cpuscheduler;

public class GanttEntry {
    public String pid;
    public int start, end;

    public GanttEntry(String pid, int start, int end) {
        this.pid = pid;
        this.start = start;
        this.end = end;
    }
}
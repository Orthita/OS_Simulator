package cpuscheduler;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public class RMScheduler extends JFrame {

    static class Task {
        int id, period, wcet;
        int remainingTime;
        int nextRelease;

        Task(int id, int period, int wcet) {
            this.id = id;
            this.period = period;
            this.wcet = wcet;
            this.remainingTime = wcet;
            this.nextRelease = 0;
        }
    }

    static class Slot {
        String taskName;
        int start, end;

        Slot(String taskName, int start, int end) {
            this.taskName = taskName;
            this.start = start;
            this.end = end;
        }
    }

    private List<Slot> ganttSlots = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();

    public RMScheduler() {
        setTitle("Rate Monotonic Scheduling - Gantt Chart");
        setSize(1100, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        add(new JScrollPane(new GanttPanel()));
    }

    public void startRMS() {
        // Get input using dialog
        String input = JOptionPane.showInputDialog(this,
                "Enter tasks in format: period1,wcet1;period2,wcet2;...\nExample: 5,2;10,3;15,4",
                "RMS Input",
                JOptionPane.PLAIN_MESSAGE);

        if (input == null || input.trim().isEmpty()) {
            return;
        }

        try {
            parseTasks(input);
            runRMSimulation();
            setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input format! Use: period,wcet;period,wcet;...", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void parseTasks(String input) {
        tasks.clear();
        ganttSlots.clear();

        String[] taskStrings = input.split(";");
        for (int i = 0; i < taskStrings.length; i++) {
            String[] parts = taskStrings[i].split(",");
            int period = Integer.parseInt(parts[0].trim());
            int wcet = Integer.parseInt(parts[1].trim());
            tasks.add(new Task(i, period, wcet));
        }
    }

    private void runRMSimulation() {
        if (!isSchedulable(tasks)) {
            JOptionPane.showMessageDialog(this, "Utility test Failed do realtime based test", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int hp = calculateHyperPeriod(tasks);
        simulateRMS(tasks, hp);
        repaint();

        // Show success message
        JOptionPane.showMessageDialog(this,
                "RMS Simulation Completed!\nHyperperiod: " + hp + "\nTotal Time Slots: " + ganttSlots.size(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------- Utility methods ----------
    private boolean isSchedulable(List<Task> tasks) {  // REMOVED STATIC
        int n = tasks.size();
        double U = 0;
        for (Task t : tasks) {
            U += (double) t.wcet / t.period;
        }
        double bound = n * (Math.pow(2, 1.0 / n) - 1);

        // Show utilization in console
        System.out.printf("RMS Utilization = %.3f, Bound = %.3f\n", U, bound);
        return U <= bound;
    }

    private int calculateHyperPeriod(List<Task> tasks) {  // REMOVED STATIC
        int hp = tasks.get(0).period;
        for (int i = 1; i < tasks.size(); i++) {
            hp = lcm(hp, tasks.get(i).period);
        }
        return hp;
    }

    private int gcd(int a, int b) {  // REMOVED STATIC
        return b == 0 ? a : gcd(b, a % b);
    }

    private int lcm(int a, int b) {  // REMOVED STATIC
        return a * b / gcd(a, b);
    }

    private void simulateRMS(List<Task> taskList, int hp) {  // REMOVED STATIC
        // Reset all tasks
        for (Task t : taskList) {
            t.remainingTime = t.wcet;
            t.nextRelease = 0;
        }

        List<Slot> slots = new ArrayList<>();

        for (int time = 0; time < hp; time++) {
            // Check for new releases
            for (Task t : taskList) {
                if (time == t.nextRelease) {
                    t.remainingTime = t.wcet;
                }
            }

            // Find highest priority task (shortest period)
            Task current = null;
            for (Task t : taskList) {
                if (t.remainingTime > 0) {
                    if (current == null || t.period < current.period)
                        current = t;
                }
            }

            if (current != null) {
                current.remainingTime--;
                slots.add(new Slot("T" + current.id, time, time + 1));
            } else {
                slots.add(new Slot("IDLE", time, time + 1));
            }

            // Update next release times
            for (Task t : taskList) {
                if (time + 1 == t.nextRelease + t.period) {
                    t.nextRelease += t.period;
                }
            }
        }

        // Update the instance ganttSlots
        ganttSlots.clear();
        ganttSlots.addAll(slots);
    }

    // ---------- GUI ----------
    class GanttPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(new Color(240, 245, 255));

            Graphics2D g2 = (Graphics2D) g;
            int y = 60;
            int barHeight = 30;

            // Map tasks to Y positions
            Set<String> names = new LinkedHashSet<>();
            for (Slot s : ganttSlots) names.add(s.taskName);
            Map<String, Integer> yMap = new LinkedHashMap<>();

            int offset = 0;
            for (String name : names) {
                yMap.put(name, y + offset);
                offset += 50;
            }

            // Fixed colors for each task
            Map<String, Color> colorMap = new HashMap<>();
            colorMap.put("IDLE", Color.LIGHT_GRAY);
            colorMap.put("T0", new Color(255, 100, 100));    // Red
            colorMap.put("T1", new Color(100, 100, 255));    // Blue
            colorMap.put("T2", new Color(100, 200, 100));    // Green
            colorMap.put("T3", new Color(255, 200, 50));     // Orange
            colorMap.put("T4", new Color(200, 100, 255));    // Purple
            colorMap.put("T5", new Color(50, 200, 255));     // Light Blue
            colorMap.put("T6", new Color(255, 150, 50));     // Orange-Red
            colorMap.put("T7", new Color(150, 255, 150));    // Light Green
            colorMap.put("T8", new Color(255, 100, 255));    // Pink
            colorMap.put("T9", new Color(100, 255, 255));    // Cyan

            for (Slot s : ganttSlots) {
                // If task color not defined, create a consistent color based on task ID
                if (!colorMap.containsKey(s.taskName) && s.taskName.startsWith("T")) {
                    try {
                        int taskId = Integer.parseInt(s.taskName.substring(1));
                        // Generate consistent color based on task ID
                        Color color = new Color(
                                (taskId * 50) % 200 + 55,
                                (taskId * 80) % 200 + 55,
                                (taskId * 120) % 200 + 55
                        );
                        colorMap.put(s.taskName, color);
                    } catch (NumberFormatException e) {
                        // Fallback color for unknown tasks
                        colorMap.put(s.taskName, Color.GRAY);
                    }
                }

                Color taskColor = colorMap.getOrDefault(s.taskName, Color.GRAY);
                int yPos = yMap.get(s.taskName);
                int x1 = s.start * 30 + 60;
                int width = (s.end - s.start) * 30;

                g2.setColor(taskColor);
                g2.fill(new Rectangle2D.Double(x1, yPos, width, barHeight));
                g2.setColor(Color.BLACK);
                g2.draw(new Rectangle2D.Double(x1, yPos, width, barHeight));

                // Time markers under each slot
                g2.setFont(new Font("Arial", Font.PLAIN, 10));
                g2.drawString(String.valueOf(s.start), x1, yPos + barHeight + 15);
            }

            // Draw time scale at bottom
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            int totalWidth = ganttSlots.size() > 0 ? ganttSlots.get(ganttSlots.size() - 1).end * 30 + 60 : 0;
            for (int t = 0; t <= totalWidth / 30; t++) {
                g2.drawString(String.valueOf(t), 60 + t * 30, y + offset + 20);
            }

            // Task labels
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            for (Map.Entry<String, Integer> e : yMap.entrySet()) {
                g2.drawString(e.getKey(), 10, e.getValue() + 20);
            }

            // Title
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("Rate Monotonic Scheduling - Gantt Chart", 320, 30);
        }

        @Override
        public Dimension getPreferredSize() {
            int width = ganttSlots.size() * 30 + 200;
            return new Dimension(width, 400);
        }
    }
}
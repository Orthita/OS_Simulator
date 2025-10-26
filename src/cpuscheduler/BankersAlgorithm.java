package cpuscheduler;

import javax.swing.*;
import java.awt.*;

public class BankersAlgorithm extends JFrame {

    private int numProcesses, numResources;
    private int[][] max;
    private int[][] allocation;
    private int[][] need;
    private int[] available;
    private JTextArea resultArea;

    public BankersAlgorithm() {
        setTitle("Banker's Algorithm - Deadlock Avoidance");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initializeUI();
    }

    public void startBankers() {
        getInput();
        setVisible(true);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("Banker's Algorithm - Deadlock Avoidance", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        titleLabel.setBackground(new Color(41, 128, 185));
        titleLabel.setOpaque(true);
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        // Result area
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(240, 245, 255));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        add(scrollPane, BorderLayout.CENTER);

        // Control buttons
        JPanel buttonPanel = new JPanel();
        JButton checkSafetyBtn = new JButton("Check Safety");
        JButton resetBtn = new JButton("Reset");

        checkSafetyBtn.setBackground(new Color(39, 174, 96));
        checkSafetyBtn.setForeground(Color.WHITE);
        resetBtn.setBackground(new Color(142, 68, 173));
        resetBtn.setForeground(Color.WHITE);

        checkSafetyBtn.addActionListener(e -> checkSafety());
        resetBtn.addActionListener(e -> startBankers());

        buttonPanel.add(checkSafetyBtn);
        buttonPanel.add(resetBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void getInput() {
        try {
            // Get number of processes
            String processInput = JOptionPane.showInputDialog(this,
                    "Enter number of processes:", "Banker's Algorithm", JOptionPane.PLAIN_MESSAGE);
            if (processInput == null) return;
            numProcesses = Integer.parseInt(processInput.trim());

            // Get number of resources
            String resourceInput = JOptionPane.showInputDialog(this,
                    "Enter number of resource types:", "Banker's Algorithm", JOptionPane.PLAIN_MESSAGE);
            if (resourceInput == null) return;
            numResources = Integer.parseInt(resourceInput.trim());

            // Initialize arrays
            max = new int[numProcesses][numResources];
            allocation = new int[numProcesses][numResources];
            need = new int[numProcesses][numResources];
            available = new int[numResources];

            // Get available resources
            String availableInput = JOptionPane.showInputDialog(this,
                    "Enter available resources (comma separated):\nExample for 3 resources: 3,3,2",
                    "Available Resources", JOptionPane.PLAIN_MESSAGE);
            if (availableInput == null) return;
            String[] availParts = availableInput.split(",");
            for (int i = 0; i < numResources; i++) {
                available[i] = Integer.parseInt(availParts[i].trim());
            }

            // Get allocation matrix
            StringBuilder allocInstructions = new StringBuilder();
            allocInstructions.append("Enter ALLOCATION matrix row by row.\n");
            allocInstructions.append("Example for 3 processes, 2 resources:\n");
            allocInstructions.append("Process 0: 0,1\n");
            allocInstructions.append("Process 1: 2,0\n");
            allocInstructions.append("Process 2: 3,0\n\n");
            allocInstructions.append("Now enter for your ").append(numProcesses).append(" processes:");

            for (int i = 0; i < numProcesses; i++) {
                String processAlloc = JOptionPane.showInputDialog(this,
                        allocInstructions.toString() + "\n\nProcess P" + i + " (comma separated):",
                        "Allocation Matrix", JOptionPane.PLAIN_MESSAGE);
                if (processAlloc == null) return;
                String[] allocParts = processAlloc.split(",");
                for (int j = 0; j < numResources; j++) {
                    allocation[i][j] = Integer.parseInt(allocParts[j].trim());
                }
            }

            // Get max matrix
            StringBuilder maxInstructions = new StringBuilder();
            maxInstructions.append("Enter MAX matrix row by row.\n");
            maxInstructions.append("Example for 3 processes, 2 resources:\n");
            maxInstructions.append("Process 0: 7,5\n");
            maxInstructions.append("Process 1: 3,2\n");
            maxInstructions.append("Process 2: 9,2\n\n");
            maxInstructions.append("Now enter for your ").append(numProcesses).append(" processes:");

            for (int i = 0; i < numProcesses; i++) {
                String processMax = JOptionPane.showInputDialog(this,
                        maxInstructions.toString() + "\n\nProcess P" + i + " (comma separated):",
                        "Max Matrix", JOptionPane.PLAIN_MESSAGE);
                if (processMax == null) return;
                String[] maxParts = processMax.split(",");
                for (int j = 0; j < numResources; j++) {
                    max[i][j] = Integer.parseInt(maxParts[j].trim());
                }
            }

            // Calculate need matrix using your logic
            calculateNeed(need, max, allocation, numProcesses, numResources);
            displayInitialState();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input format! Please check your input.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Your exact logic for calculating need matrix
    private void calculateNeed(int need[][], int maxm[][], int allot[][], int P, int R) {
        for (int i = 0; i < P; i++) {
            for (int j = 0; j < R; j++) {
                need[i][j] = maxm[i][j] - allot[i][j];
            }
        }
    }

    private void displayInitialState() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BANKER'S ALGORITHM INITIAL STATE ===\n\n");

        sb.append("Number of Processes: ").append(numProcesses).append("\n");
        sb.append("Number of Resources: ").append(numResources).append("\n\n");

        sb.append("Available Resources: ");
        for (int i = 0; i < numResources; i++) {
            sb.append(available[i]).append(" ");
        }
        sb.append("\n\n");

        sb.append("ALLOCATION Matrix:\n");
        sb.append("    ");
        for (int i = 0; i < numResources; i++) {
            sb.append("R").append(i).append(" ");
        }
        sb.append("\n");
        for (int i = 0; i < numProcesses; i++) {
            sb.append("P").append(i).append(": ");
            for (int j = 0; j < numResources; j++) {
                sb.append(allocation[i][j]).append("  ");
            }
            sb.append("\n");
        }

        sb.append("\nMAX Matrix:\n");
        sb.append("    ");
        for (int i = 0; i < numResources; i++) {
            sb.append("R").append(i).append(" ");
        }
        sb.append("\n");
        for (int i = 0; i < numProcesses; i++) {
            sb.append("P").append(i).append(": ");
            for (int j = 0; j < numResources; j++) {
                sb.append(max[i][j]).append("  ");
            }
            sb.append("\n");
        }

        sb.append("\nNEED Matrix (Max - Allocation):\n");
        sb.append("    ");
        for (int i = 0; i < numResources; i++) {
            sb.append("R").append(i).append(" ");
        }
        sb.append("\n");
        for (int i = 0; i < numProcesses; i++) {
            sb.append("P").append(i).append(": ");
            for (int j = 0; j < numResources; j++) {
                sb.append(need[i][j]).append("  ");
            }
            sb.append("\n");
        }

        sb.append("\nClick 'Check Safety' to find safe sequence.\n");
        resultArea.setText(sb.toString());
    }

    private void checkSafety() {
        // Create processes array
        int[] processes = new int[numProcesses];
        for (int i = 0; i < numProcesses; i++)
            processes[i] = i;

        // Use your exact safety check logic
        boolean isSafe = isSafe(processes, available, max, allocation, numProcesses, numResources);

        StringBuilder sb = new StringBuilder(resultArea.getText());
        sb.append("\n=== SAFETY ALGORITHM RESULT ===\n\n");

        if (isSafe) {
            sb.append("✓ SYSTEM IS IN A SAFE STATE\n");
        } else {
            sb.append("✗ SYSTEM IS IN AN UNSAFE STATE - DEADLOCK POSSIBLE!\n");
        }

        resultArea.setText(sb.toString());
    }

    // Your exact safety check logic
    private boolean isSafe(int processes[], int avail[], int maxm[][], int allot[][], int P, int R) {
        int[][] need = new int[P][R];
        calculateNeed(need, maxm, allot, P, R);

        boolean[] finish = new boolean[P];
        int[] safeSeq = new int[P];
        int[] work = new int[R];

        for (int i = 0; i < R; i++)
            work[i] = avail[i];

        int count = 0;
        while (count < P) {
            boolean found = false;
            for (int p = 0; p < P; p++) {
                if (!finish[p]) {
                    int j;
                    for (j = 0; j < R; j++) {
                        if (need[p][j] > work[j])
                            break;
                    }

                    if (j == R) {
                        for (int k = 0; k < R; k++)
                            work[k] += allot[p][k];

                        safeSeq[count++] = p;
                        finish[p] = true;
                        found = true;
                    }
                }
            }

            if (!found) {
                return false;
            }
        }

        // Display safe sequence
        StringBuilder sb = new StringBuilder(resultArea.getText());
        sb.append("Safe Sequence: ");
        for (int i = 0; i < P; i++) {
            sb.append("P").append(safeSeq[i]);
            if (i < P - 1) sb.append(" → ");
        }
        sb.append("\n");
        resultArea.setText(sb.toString());

        return true;
    }
}
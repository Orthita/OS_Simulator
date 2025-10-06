package cpuscheduler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class CPUSchedulingSimulator extends JFrame {
    private JTable inputTable, outputTable;
    private DefaultTableModel inputTableModel, outputTableModel;
    private JTextField pidField, arrivalField, burstField, quantumField, priorityField;
    private JComboBox<String> algorithmComboBox;
    private JPanel ganttChartPanel;
    private ArrayList<Process> processes;
    private JPanel dynamicInputPanel;
    private java.util.List<GanttEntry> ganttEntries;

    // Modern Color Scheme
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(39, 174, 96);
    private final Color ACCENT_COLOR = new Color(142, 68, 173);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_COLOR = new Color(33, 37, 41);

    public CPUSchedulingSimulator() {
        processes = new ArrayList<>();
        ganttEntries = new ArrayList<>();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("CPU Scheduling Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BACKGROUND_COLOR);

        createHeader();
        createMainPanel();

        pack();
        setSize(1400, 900);
        setLocationRelativeTo(null);
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel titleLabel = new JLabel("CPU Scheduling Simulator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createMainPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mainPanel.add(createInputPanel());
        mainPanel.add(createOutputPanel());

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(BACKGROUND_COLOR);

        inputPanel.add(createProcessInputCard());
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(createInputTableCard());
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(createAlgorithmCard());

        return inputPanel;
    }

    private JPanel createProcessInputCard() {
        JPanel card = createCard("Add Process", 120);
        card.setLayout(new GridLayout(2, 1, 0, 5));

        JPanel inputPanel = new JPanel(new GridLayout(1, 8, 5, 0));
        inputPanel.setBackground(CARD_COLOR);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        inputPanel.add(createInputLabel("Process ID"));
        pidField = createStyledTextField();
        inputPanel.add(pidField);

        inputPanel.add(createInputLabel("Arrival Time"));
        arrivalField = createStyledTextField();
        inputPanel.add(arrivalField);

        inputPanel.add(createInputLabel("Burst Time"));
        burstField = createStyledTextField();
        inputPanel.add(burstField);

        inputPanel.add(createInputLabel("Priority"));
        priorityField = createStyledTextField();
        priorityField.setText("1");
        inputPanel.add(priorityField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton addBtn = createPrimaryButton("Add Process");
        JButton clearAllBtn = createSecondaryButton("Clear All");

        addBtn.addActionListener(e -> addProcess());
        clearAllBtn.addActionListener(e -> clearAllProcesses());

        buttonPanel.add(clearAllBtn);
        buttonPanel.add(addBtn);

        card.add(inputPanel);
        card.add(buttonPanel);

        return card;
    }

    private JPanel createInputTableCard() {
        JPanel card = createCard("Input Processes", 200);
        card.setLayout(new BorderLayout());

        String[] columns = {"Process ID", "Arrival Time", "Burst Time", "Priority", "Actions"};
        inputTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        inputTable = new JTable(inputTableModel);
        styleTable(inputTable);

        inputTable.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        inputTable.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(inputTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createAlgorithmCard() {
        JPanel card = createCard("Scheduling Algorithms", 150);
        card.setLayout(new BorderLayout(0, 10));

        JPanel algoPanel = new JPanel(new BorderLayout(10, 0));
        algoPanel.setBackground(CARD_COLOR);
        algoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel algoLabel = createSectionLabel("Select Algorithm:");
        String[] algorithms = {"FCFS", "SJF", "SRTF", "Round Robin", "Priority Preemptive", "Priority Non-Preemptive"};
        algorithmComboBox = new JComboBox<>(algorithms);
        styleComboBox(algorithmComboBox);

        algoPanel.add(algoLabel, BorderLayout.WEST);
        algoPanel.add(algorithmComboBox, BorderLayout.CENTER);

        dynamicInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        dynamicInputPanel.setBackground(CARD_COLOR);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setBackground(CARD_COLOR);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JButton simulateBtn = createAccentButton("Run Simulation");
        simulateBtn.addActionListener(e -> runSimulation());

        controlPanel.add(simulateBtn);

        card.add(algoPanel, BorderLayout.NORTH);
        card.add(dynamicInputPanel, BorderLayout.CENTER);
        card.add(controlPanel, BorderLayout.SOUTH);

        algorithmComboBox.addActionListener(e -> updateDynamicInputs());
        updateDynamicInputs();

        return card;
    }

    private JPanel createOutputPanel() {
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
        outputPanel.setBackground(BACKGROUND_COLOR);

        outputPanel.add(createOutputTableCard());
        outputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        outputPanel.add(createGanttChartCard());

        return outputPanel;
    }

    private JPanel createOutputTableCard() {
        JPanel card = createCard("Simulation Results", 300);
        card.setLayout(new BorderLayout());

        String[] columns = {"Process", "Arrival", "Burst", "Start", "Completion", "Waiting", "Turnaround", "Response"};
        outputTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        outputTable = new JTable(outputTableModel);
        styleTable(outputTable);

        JScrollPane scrollPane = new JScrollPane(outputTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createGanttChartCard() {
        JPanel card = createCard("Gantt Chart", 120);
        card.setLayout(new BorderLayout());

        ganttChartPanel = new JPanel();
        ganttChartPanel.setBackground(Color.WHITE);
        ganttChartPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));

        JScrollPane scrollPane = new JScrollPane(ganttChartPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    // UI Component Factory Methods
    private JPanel createCard(String title, int height) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230)),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        card.setPreferredSize(new Dimension(600, height));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(222, 226, 230)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }

    private JLabel createInputLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(TEXT_COLOR);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return field;
    }

    private JButton createPrimaryButton(String text) {
        return createButton(text, PRIMARY_COLOR);
    }

    private JButton createSecondaryButton(String text) {
        return createButton(text, new Color(108, 117, 125));
    }

    private JButton createAccentButton(String text) {
        return createButton(text, SECONDARY_COLOR);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(248, 249, 250));
        table.getTableHeader().setForeground(TEXT_COLOR);
        table.setShowGrid(true);
        table.setGridColor(new Color(222, 226, 230));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    // Business Logic Methods
    private void updateDynamicInputs() {
        String algorithm = (String) algorithmComboBox.getSelectedItem();
        dynamicInputPanel.removeAll();

        if ("Round Robin".equals(algorithm)) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            panel.setBackground(CARD_COLOR);
            panel.add(createSectionLabel("Quantum:"));
            quantumField = createStyledTextField();
            quantumField.setColumns(3);
            quantumField.setText("2");
            panel.add(quantumField);
            dynamicInputPanel.add(panel);
        }

        dynamicInputPanel.revalidate();
        dynamicInputPanel.repaint();
    }

    private void addProcess() {
        try {
            String pid = pidField.getText().trim();
            int arrival = Integer.parseInt(arrivalField.getText().trim());
            int burst = Integer.parseInt(burstField.getText().trim());
            int priority = Integer.parseInt(priorityField.getText().trim());

            if (pid.isEmpty()) {
                showError("Please enter Process ID!");
                return;
            }

            if (arrival < 0 || burst <= 0 || priority < 0) {
                showError("Please enter valid non-negative values!");
                return;
            }

            for (Process p : processes) {
                if (p.pid.equals(pid)) {
                    showError("Process ID must be unique!");
                    return;
                }
            }

            processes.add(new Process(pid, arrival, burst, priority));
            updateInputTable();
            clearInputFields();

        } catch (NumberFormatException ex) {
            showError("Please enter valid numbers!");
        }
    }

    private void clearInputFields() {
        pidField.setText("");
        arrivalField.setText("");
        burstField.setText("");
        priorityField.setText("1");
    }

    private void clearAllProcesses() {
        processes.clear();
        updateInputTable();
        clearOutputTable();
        ganttEntries.clear();
        ganttChartPanel.removeAll();
        ganttChartPanel.repaint();
    }

    private void updateInputTable() {
        inputTableModel.setRowCount(0);
        for (Process p : processes) {
            inputTableModel.addRow(new Object[]{p.pid, p.arrivalTime, p.burstTime, p.priority, "Delete"});
        }
    }

    private void clearOutputTable() {
        outputTableModel.setRowCount(0);
    }

    private void deleteProcess(int row) {
        if (row >= 0 && row < processes.size()) {
            processes.remove(row);
            updateInputTable();
        }
    }

    private void runSimulation() {
        if (processes.isEmpty()) {
            showError("Please add at least one process!");
            return;
        }

        String algorithm = (String) algorithmComboBox.getSelectedItem();

        // Reset process states
        for (Process p : processes) {
            p.reset();
        }
        ganttEntries.clear();

        Scheduler scheduler = null;
        int quantum = 2; // default

        switch (algorithm) {
            case "FCFS":
                scheduler = new FCFScheduler();
                break;
            case "SJF":
                scheduler = new SJFScheduler();
                break;
            case "SRTF":
                scheduler = new SRTFScheduler();
                break;
            case "Round Robin":
                try {
                    quantum = Integer.parseInt(quantumField.getText().trim());
                    if (quantum <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    showError("Please enter a valid quantum value!");
                    return;
                }
                scheduler = new RoundRobinScheduler();
                break;
            case "Priority Preemptive":
                scheduler = new PriorityPreemptiveScheduler();
                break;
            case "Priority Non-Preemptive":
                scheduler = new PriorityNonPreemptiveScheduler();
                break;
        }

        if (scheduler != null) {
            scheduler.schedule(processes, ganttEntries, quantum);
        }

        updateOutputTable();
        showGanttChart();
    }

    private void updateOutputTable() {
        clearOutputTable();

        double totalWaiting = 0;
        double totalTurnaround = 0;
        double totalResponse = 0;

        for (Process p : processes) {
            outputTableModel.addRow(new Object[]{
                    p.pid, p.arrivalTime, p.burstTime, p.startTime,
                    p.completionTime, p.waitingTime, p.turnaroundTime, p.responseTime
            });

            totalWaiting += p.waitingTime;
            totalTurnaround += p.turnaroundTime;
            totalResponse += p.responseTime;
        }

        int count = processes.size();
        outputTableModel.addRow(new Object[]{
                "Average", "", "", "", "",
                String.format("%.2f", totalWaiting / count),
                String.format("%.2f", totalTurnaround / count),
                String.format("%.2f", totalResponse / count)
        });
    }

    private void showGanttChart() {
        ganttChartPanel.removeAll();

        if (ganttEntries.isEmpty()) {
            ganttChartPanel.repaint();
            return;
        }

        ganttEntries.sort(Comparator.comparingInt(e -> e.start));

        int currentTime = 0;
        for (GanttEntry e : ganttEntries) {
            if (e.start > currentTime) {
                addGanttBlock("IDLE", currentTime, e.start, Color.LIGHT_GRAY);
            }
            Color color = getProcessColor(e.pid);
            addGanttBlock(e.pid, e.start, e.end, color);
            currentTime = e.end;
        }

        ganttChartPanel.revalidate();
        ganttChartPanel.repaint();
    }

    private void addGanttBlock(String pid, int start, int end, Color color) {
        JPanel block = new JPanel(new BorderLayout());
        int width = Math.max(40, (end - start) * 20);
        block.setPreferredSize(new Dimension(width, 40));
        block.setBackground(color);
        block.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        JLabel label = new JLabel(pid, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        block.add(label, BorderLayout.CENTER);

        JLabel timeLabel = new JLabel(start + "-" + end, SwingConstants.CENTER);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        block.add(timeLabel, BorderLayout.SOUTH);

        ganttChartPanel.add(block);
    }

    private Color getProcessColor(String pid) {
        int hash = pid.hashCode();
        Random random = new Random(hash);
        return new Color(
                random.nextInt(200) + 55,
                random.nextInt(200) + 55,
                random.nextInt(200) + 55
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CPUSchedulingSimulator().setVisible(true);
        });
    }

    // Table button renderer and editor
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(ACCENT_COLOR);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.PLAIN, 10));
            setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(ACCENT_COLOR);
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.row = row;
            button.setText(value.toString());
            return button;
        }

        public Object getCellEditorValue() {
            deleteProcess(row);
            return "Delete";
        }
    }
}
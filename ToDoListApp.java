import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ToDoListApp extends JFrame {
    private JTextField taskField;
    private DefaultListModel<Task> tasksListModel;
    private JList<Task> tasksList;
    private Connection connection;

    enum Priority {
        HIGH, MEDIUM, LOW
    }

    class Task {
        String name;
        boolean completed;
        Priority priority;

        public Task(String name, boolean completed, Priority priority) {
            this.name = name;
            this.completed = completed;
            this.priority = priority;
        }

        @Override
        public String toString() {
            return getPriorityString(priority) + (completed ? " [X] " : " [ ] ") + name;
        }
    }

    public ToDoListApp() {
        super("To-Do List");

        taskField = new JTextField(20);
        JButton addButton = new JButton("Add");
        JButton viewButton = new JButton("View");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");
        JButton selectAllButton = new JButton("Select All");
        JButton deleteAllButton = new JButton("Delete All");
        JButton updateAllButton = new JButton("Update All");

        tasksListModel = new DefaultListModel<>();
        tasksList = new JList<>(tasksListModel);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(taskField);
        panel.add(addButton);
        panel.add(viewButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(selectAllButton);
        panel.add(deleteAllButton);
        panel.add(updateAllButton);

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@//localhost:1521", "system", "tiger");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String taskName = taskField.getText();
                Priority priority = (Priority) JOptionPane.showInputDialog(
                        ToDoListApp.this,
                        "Select task priority:",
                        "Priority",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        Priority.values(),
                        Priority.MEDIUM
                );
                if (priority != null) {
                    addTask(taskName, priority);
                }
            }
        });

        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewTasks();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = tasksList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String newTaskName = JOptionPane.showInputDialog(ToDoListApp.this, "Enter updated task name:");
                    if (newTaskName != null && !newTaskName.trim().isEmpty()) {
                        updateTask(selectedIndex, newTaskName);
                    }
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = tasksList.getSelectedIndex();
                if (selectedIndex != -1) {
                    deleteTask(selectedIndex);
                }
            }
        });

        selectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectAllTasks();
            }
        });

        deleteAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAllTasks();
            }
        });

        updateAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAllTasks();
            }
        });

        taskField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String taskName = taskField.getText();
                Priority priority = (Priority) JOptionPane.showInputDialog(
                        ToDoListApp.this,
                        "Select task priority:",
                        "Priority",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        Priority.values(),
                        Priority.MEDIUM
                );
                if (priority != null) {
                    addTask(taskName, priority);
                }
            }
        });

        tasksList.setCellRenderer(new TaskCellRenderer());
        tasksList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tasksList.addMouseListener(new TaskMouseListener());

        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(tasksList), BorderLayout.CENTER);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addTask(String taskName, Priority priority) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO tasks (task_name, completed, priority) VALUES (?, ?, ?)");
            preparedStatement.setString(1, taskName);
            preparedStatement.setBoolean(2, false);
            preparedStatement.setString(3, priority.toString());
            preparedStatement.executeUpdate();
            tasksListModel.addElement(new Task(taskName, false, priority));
            taskField.setText("");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String getPriorityString(Priority priority) {
        switch (priority) {
            case HIGH:
                return "[High]";
            case MEDIUM:
                return "[Medium]";
            case LOW:
                return "[Low]";
            default:
                return "[Unknown]";
        }
    }

    private void viewTasks() {
        tasksListModel.clear();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM tasks");
            while (resultSet.next()) {
                String taskName = resultSet.getString("task_name");
                boolean completed = resultSet.getBoolean("completed");
                String priorityStr = resultSet.getString("priority");
                Priority priority = Priority.valueOf(priorityStr.toUpperCase());
                tasksListModel.addElement(new Task(taskName, completed, priority));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateTask(int index, String newTaskName) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE tasks SET task_name = ? WHERE rownum = ?");
            preparedStatement.setString(1, newTaskName);
            preparedStatement.setInt(2, index + 1);
            preparedStatement.executeUpdate();

            Task task = tasksListModel.getElementAt(index);
            task.name = newTaskName;
            tasksListModel.setElementAt(task, index);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void deleteTask(int index) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tasks WHERE rownum = ?");
            preparedStatement.setInt(1, index + 1);
            preparedStatement.executeUpdate();

            tasksListModel.remove(index);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void selectAllTasks() {
        tasksList.setSelectionInterval(0, tasksListModel.size() - 1);
    }

    private void deleteAllTasks() {
        int confirmed = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete all tasks?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );
        if (confirmed == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tasks");
                preparedStatement.executeUpdate();
                tasksListModel.clear();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updateAllTasks() {
        for (int i = 0; i < tasksListModel.size(); i++) {
            Task task = tasksListModel.getElementAt(i);
            String newTaskName = JOptionPane.showInputDialog(this, "Enter updated task name for \"" + task.name + "\":");
            if (newTaskName != null && !newTaskName.trim().isEmpty()) {
                updateTask(i, newTaskName);
            }
        }
    }

    private class TaskCellRenderer extends JCheckBox implements ListCellRenderer<Task> {
        @Override
        public Component getListCellRendererComponent(JList<? extends Task> list, Task task, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText(task.toString());
            setSelected(task.completed);
            setForeground(task.completed ? Color.GRAY : Color.BLACK);
            setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
            return this;
        }
    }

    private class TaskMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            int index = tasksList.locationToIndex(evt.getPoint());
            if (index != -1) {
                Task task = tasksListModel.getElementAt(index);
                task.completed = !task.completed;
                updateTaskCompletionStatus(task);
                tasksList.repaint();
            }
        }
    }

    private void updateTaskCompletionStatus(Task task) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE tasks SET completed = ? WHERE task_name = ?");
            preparedStatement.setBoolean(1, task.completed);
            preparedStatement.setString(2, task.name);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ToDoListApp();
            }
        });
    }
}

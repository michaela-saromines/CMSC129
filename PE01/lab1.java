import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class lab1 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }
    
    private static String[][] dfaTable;
    private static String[] inputArray;
    private static String elem1, elem2;
    private static int pos;
    private static String[] finalState;
    private static boolean dfaLoaded = false;
    private static boolean inLoaded = false;

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Strings and DFA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the default size to 800x400
        frame.setSize(800, 400);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
            }
        });

        //BUTTONS
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton loadBtn = new JButton("Load File");
        JButton processBtn = new JButton("Process");
        buttonPanel.add(loadBtn);
        buttonPanel.add(processBtn);

        //INPUT AND OUTPUT TEXT AREAS
        JTextArea input = new JTextArea();
        JTextArea output = new JTextArea();
        input.setEditable(false);
        output.setEditable(false);
        //JScrollPane to make them scrollable
        JScrollPane inputScrollPane = new JScrollPane(input);
        JScrollPane outputScrollPane = new JScrollPane(output);
        
        //LABELS FOR TEXT AREAS
        JLabel inputLbl = new JLabel("INPUT");
        JLabel outputLbl = new JLabel("OUTPUT");

        //PANEL FOR INPUT TEXT AREA AND LABEL
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputLbl, BorderLayout.NORTH);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);

        //PANEL FOR OUTPUT TEXT AREA AND LABEL
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(outputLbl, BorderLayout.NORTH);
        outputPanel.add(outputScrollPane, BorderLayout.CENTER);

        //TABLE
        DefaultTableModel tableModel = new DefaultTableModel(16, 4){
            @Override
            public boolean isCellEditable(int row, int column) {
                //FALSE TO MAKE TABLE UNEDITABLE
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        JLabel tableLbl = new JLabel("TRANSITION TABLE");
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // HIGHLIGHTS ONLY ONE CELL
        table.setSelectionBackground(table.getBackground()); // MAKES BG SAME AS TABLE BG WHEN HIGHLIGHTED

        //JScrollPane to make them scrollable
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(200, 200));

        //PANEL FOR STATUS AND LABEL
        JPanel statusPanel = new JPanel(new BorderLayout());
        JLabel statusLbl = new JLabel("STATUS:");
        JTextField statusTextField = new JTextField();
        statusTextField.setEditable(false);
        statusTextField.setBorder(javax.swing.BorderFactory.createEmptyBorder()); //REMOVES BORDER
        statusPanel.setBackground(Color.WHITE);
        statusTextField.setPreferredSize(new Dimension(700, 20)); // Set the preferred size

        //SUBPANEL FOR STATUS PANEL
        JPanel subStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        subStatusPanel.add(statusLbl);
        subStatusPanel.add(statusTextField);
        statusPanel.add(subStatusPanel, BorderLayout.WEST);

        //CONTENT PANEL
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        frame.add(buttonPanel, BorderLayout.NORTH); //ADDS BUTTON PANEL
        frame.add(contentPanel, BorderLayout.CENTER); //ADDS CONTENT PANEL
        contentPanel.setLayout(new BorderLayout());

        //ADDS TABLE TO THE LEFT PANEL
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(tableLbl, BorderLayout.NORTH); // Add table label at the top
        leftPanel.add(tableScrollPane, BorderLayout.CENTER); // Add the table in the center

        //ADDS TEXT AREAS TO THE RIGHT PANEL
        JPanel rightPanel = new JPanel(new GridLayout(2, 1)); // Arrange text areas vertically
        rightPanel.add(inputPanel);
        rightPanel.add(outputPanel);

        //ADDS LEFT AND RIGHT PANELS TO THE CONTENT PANEL
        contentPanel.add(leftPanel, BorderLayout.WEST);
        contentPanel.add(rightPanel, BorderLayout.CENTER);

        contentPanel.add(subStatusPanel, BorderLayout.SOUTH);
        
        //IF LOAD BUTTON IS CLICKED
        loadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    try {
                        if (file.getName().endsWith(".dfa")) { // DISPLAYS .DFA FILES TO THE TABLE
                        BufferedReader inputFile = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                        // Read the first line to determine the number of columns
                        String firstLine = inputFile.readLine();
                        int numColumns = countNonCommaCharacters(firstLine) + 2;
                        
                        // Define the column names
                        elem1 = String.valueOf(firstLine.charAt(0));
                        elem2 = String.valueOf(firstLine.charAt(2));
                        String[] columnNames = {"", "STATE", elem1, elem2};
                        
                        // Set the column names for the JTable
                        tableModel.setColumnIdentifiers(columnNames);

                        // Set the column names based on the number of columns
                        tableModel.setColumnCount(numColumns);

                        // Reset the table model
                        tableModel.setRowCount(0);

                        // Read and display the file content in the table
                        String line;
                        ArrayList<String[]> rowDataList = new ArrayList<>();
                        while ((line = inputFile.readLine()) != null) {
                            // Split the line by commas and add each part to a row
                            String[] rowData = line.split(",");
                            tableModel.addRow(rowData);
                            rowDataList.add(rowData);
                        }

                        inputFile.close();
                        statusTextField.setText("File loaded successfully!");                  
                        // Create a 2D array; container of our dfa table data
                        dfaTable = new String[rowDataList.size()][];
                        for (int i = 0; i < rowDataList.size(); i++) {
                            String[] rowData = rowDataList.get(i);
                            dfaTable [i] = new String[rowData.length];

                            for (int j = 0; j < rowData.length; j++) {
                                dfaTable [i][j] = rowData[j];
                            }
                        }

                        // Check the validity of the first column of the DFA table
                        if (isFirstColumnValid(dfaTable)) {
                            statusTextField.setText("Invalid DFA");
                            dfaLoaded = false; // Set the flag to indicate invalid DFA
                            tableModel.setRowCount(0);
                            processBtn.setEnabled(false);
                        } else if (!hasExactlyFourColumns(dfaTable)) {
                            statusTextField.setText("Invalid DFA");
                            dfaLoaded = false; // Set the flag to indicate invalid DFA
                            tableModel.setRowCount(0);
                            processBtn.setEnabled(false);
                        } else if (isFirstColumnValid(dfaTable) && !hasExactlyFourColumns(dfaTable)){
                            statusTextField.setText("Invalid DFA");
                            dfaLoaded = false; // Set the flag to indicate invalid DFA
                            tableModel.setRowCount(0);
                            processBtn.setEnabled(false);
                        } else {
                            dfaLoaded = true; // Set the flag to indicate valid DFA
                            processBtn.setEnabled(true);
                        }

                    }
                        
                        else if (file.getName().endsWith(".in")) { // DISPLAYS .IN FILES TO INPUT TEXT AREA
                            input.setText("");
                            ArrayList<String> inputDataList = new ArrayList<>();
                            BufferedReader inputFile = new BufferedReader(new InputStreamReader(
                            new FileInputStream(file)));
                            // Read each line of the input file and store it in the ArrayList
                            String line;
                            while ((line = inputFile.readLine()) != null) {
                                inputDataList.add(line);
                            }

                            // Convert the ArrayList to a regular array called 'inputData'
                            inputArray = inputDataList.toArray(new String[0]);
                            for (int i = 0; i < inputArray.length; i++) {
                                input.append("[" + i + "] => " + inputArray[i] + "\n"); // Append the line and a newline character
                            }
                            inLoaded = true;
                            inputFile.close();
                        } else {
                            statusTextField.setText("Unsupported file format");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        statusTextField.setText("Error loading file: " + ex.getMessage());
                    }
                } else {
                    System.out.println("Operation is CANCELLED :(");
                }
        }});

        processBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nextPos = "";
                output.setText(""); 
                if (dfaLoaded && inLoaded) {
                    ArrayList<char[]> inputTable = new ArrayList<>();
                
                    // Separate each element of inputArray into characters and store in input2DArrayList
                    for (String str : inputArray) {
                        char[] charArray = str.toCharArray();
                        inputTable.add(charArray);
                    }
                    
                    // find the start state and end state
                    finalState = new String[dfaTable.length];
                    int fCount = 0;
                    for (int i = 0; i < dfaTable.length; i++) {                        
                        if (dfaTable[i][0].equals("-")) {
                            pos = i;
                        }
                        else if (dfaTable[i][0].equals("+")) {
                            finalState[fCount] = dfaTable[i][1];
                            fCount += 1;
                        }
                    }
        
                    String outputFileName = "strings.out"; // Name of the output file
                    BufferedWriter writer = null;
        
                    try {
                        writer = new BufferedWriter(new FileWriter(outputFileName));
                        
                        for (int i = 0; i < inputTable.size(); i++) {
                            char[] charArray = inputTable.get(i);
                            for (int j = 0; j < charArray.length; j++) {
                                char character = charArray[j];
        
                                if (character == elem1.charAt(0)) {
                                    nextPos = dfaTable[pos][2];
                                    for (int l = 0; l < dfaTable.length; l++) {
                                        if (dfaTable[l][1].equals(nextPos)) {
                                            pos = l;
                                        }
                                    }
                                } else {
                                    nextPos = dfaTable[pos][3];
                                    for (int l = 0; l < dfaTable.length; l++) {
                                        if (dfaTable[l][1].equals(nextPos)) {
                                            pos = l;
                                        }
                                    }
                                }
                            }
                            boolean isValid = false; 
        
                            for (int m = 0; m < finalState.length; m++) {
                                if (nextPos.equals(finalState[m])) {
                                    isValid = true;
                                    break;
                                }
                            }
                            
                            if (isValid) {
                                output.append("[" + i + "] VALID \n");
                                writer.write("VALID \n");
                            } else {
                                output.append("[" + i + "] INVALID \n");
                                writer.write("INVALID \n");
                            }
                        }
                    } catch (Exception ioException) {
                        ioException.printStackTrace();
                    } finally {
                        try {
                            if (writer != null) {
                                writer.close();
                            }
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            }
        });        

        // Center the frame on the screen
        frame.setLocationRelativeTo(null);

        // Make the frame visible
        frame.setVisible(true);
    }

    private static int countNonCommaCharacters(String text) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c != ',') {
                count++;
            }
        }
        return count;
    }

    private static boolean isFirstColumnValid(String[][] table) {
        int dashCount = 0;
    
        for (int i = 0; i < table.length; i++) {
            if (table[i][0].equals("-")) {
                dashCount++;
            }
        }
    
        // Check if there are no '-' or 2 or more '-'
        return dashCount == 0 || dashCount >= 2;
    }

    private static boolean hasExactlyFourColumns(String[][] table) {
        if (table == null || table.length == 0) {
            // The table is either null or empty, so it doesn't have 4 columns.
            return false;
        }
    
        int expectedColumns = 4; // Change this to the number of columns you want to check for
    
        for (String[] row : table) {
            if (row.length != expectedColumns) {
                // If any row has a different number of columns, return false.
                return false;
            }
        }
    
        // If all rows have exactly 4 columns, return true.
        return true;
    }
    
}
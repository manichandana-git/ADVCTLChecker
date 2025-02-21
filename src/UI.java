import controller.CTLFormula;
import model.KripkeStructure;
import model.State;
import utils.Util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Objects;

public class UI extends JFrame {

    private final JTextField ctlFormula;
    private final JLabel modelTitle;
    private final JTextArea resultArea;
    private final JTextArea modelText;
    private final JComboBox<String> stateSelector;
    private KripkeStructure kripke;

    public UI() {
        setTitle("Fall_2024_Group_Winter_CTL Model Checker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header Panel
        JLabel headerLabel = new JLabel("Welcome to our CTL Model Checker", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(new Color(0, 102, 204));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        // Upload Panel
        JPanel uploadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton uploadButton = new JButton("CLICK ME - to upload the model file");
        uploadButton.setToolTipText("Click to upload a Kripke model file");
        uploadButton.setBackground(new Color(0, 153, 76));
        uploadButton.setForeground(Color.WHITE);
        uploadButton.addActionListener(new UploadFileListener());
        uploadPanel.add(uploadButton);
        modelTitle = new JLabel("No model loaded");
        modelTitle.setFont(new Font("Arial", Font.ITALIC, 14));
        uploadPanel.add(modelTitle);
        centerPanel.add(uploadPanel);

        // Model Display Panel
        JPanel modelDisplayPanel = new JPanel(new BorderLayout(5, 5));
        JLabel modelLabel = new JLabel("Here are the details of the model you uploaded:");
        modelLabel.setFont(new Font("Arial", Font.BOLD, 16));
        modelDisplayPanel.add(modelLabel, BorderLayout.NORTH);

        modelText = new JTextArea(8, 50);
        modelText.setEditable(false);
        modelText.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane modelScrollPane = new JScrollPane(modelText);
        modelDisplayPanel.add(modelScrollPane, BorderLayout.CENTER);
        centerPanel.add(modelDisplayPanel);

        // Formula Input Panel
        JPanel formulaPanel = new JPanel(new GridLayout(2, 1));

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel formulaLabel = new JLabel("You can enter the CTL Formula here:");
        ctlFormula = new JTextField(20);
        inputPanel.add(formulaLabel);
        inputPanel.add(ctlFormula);

        JLabel stateLabel = new JLabel("Please select the State:");
        stateSelector = new JComboBox<>();
        inputPanel.add(stateLabel);
        inputPanel.add(stateSelector);

        formulaPanel.add(inputPanel);

        JButton checkButton = new JButton("Click here to check the formula");
        checkButton.setBackground(new Color(0, 102, 204));
        checkButton.setForeground(Color.WHITE);
        checkButton.setToolTipText("Click to check if the formula holds");
        checkButton.addActionListener(new CheckActionListener());
        formulaPanel.add(checkButton);

        centerPanel.add(formulaPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = new JPanel(new BorderLayout(5, 5));
        JLabel resultLabel = new JLabel("Result:");
        resultLabel.setFont(new Font("Arial", Font.BOLD, 16));
        footerPanel.add(resultLabel, BorderLayout.NORTH);

        resultArea = new JTextArea(3, 50);
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        footerPanel.add(resultScrollPane, BorderLayout.CENTER);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        setVisible(true);
    }

    class UploadFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
            int returnValue = fileChooser.showOpenDialog(UI.this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    String fileContent = Util.readFile(selectedFile.getAbsolutePath());
                    kripke = new KripkeStructure(Util.cleanText(fileContent));

                    modelTitle.setText("Loaded: " + selectedFile.getName());
                    modelText.setText(kripke.toString());

                    stateSelector.removeAllItems();
                    for (String state : kripke.getStates()) {
                        stateSelector.addItem(state);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UI.this, "Oops! There is an Error loading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    class CheckActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            resultArea.setText("");
            try {
                if (kripke == null) {
                    throw new Exception("Wrong! Please upload a Kripke model first.");
                }

                String formula = ctlFormula.getText().trim();
                if (formula.isEmpty()) {
                    throw new Exception("Oops! Please enter a CTL formula.");
                }

                String selectedState = Objects.requireNonNull(stateSelector.getSelectedItem()).toString();
                State state = new State(selectedState);
                CTLFormula ctl = new CTLFormula(formula, state, kripke);
                boolean result = ctl.IsSatisfy();

                String message = Util.GetMessage(result, formula, selectedState);
                resultArea.setText(message);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(UI.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

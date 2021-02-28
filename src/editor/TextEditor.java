package editor;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem openMenuItem;
    private JMenuItem saveMenuItem;
    private JMenuItem exitMenuItem;
    private JPanel panel;
    private JTextField searchField;
    private JScrollPane scrollPane;
    private JTextArea textArea;
    private JButton saveButton;
    private JButton openButton;
    private JFileChooser jfc;
    private String filePath;
    private JButton startSearchButton;
    private JButton previousMatchButton;
    private JButton nextMatchButton;
    private JCheckBox regexCheckBox;
    private JMenu searchMenu;
    private JMenuItem startSearchMenuItem;
    private JMenuItem previousSearchMenuItem;
    private JMenuItem nextMatchMenuItem;
    private JMenuItem useRegexMenuItem;
    private ArrayList<MatchResult> matches;
    private int currentPosition;

    public TextEditor() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(dimension.width/3,dimension.height/2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        setTitle("Text Editor");
        initBackend();
        initMenu();
        initUI();
        setVisible(true);
    }

    private void initBackend() {
        jfc = new JFileChooser(".");
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setName("FileChooser");

        jfc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.getName().endsWith("txt")) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                return ".txt";
            }
        });

        this.add(jfc);
    }

    private void initMenu() {
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.setName("MenuFile");
        menuBar.add(fileMenu);

        openMenuItem = new JMenuItem("Open");
        openMenuItem.setMnemonic(KeyEvent.VK_L);
        openMenuItem.setName("MenuOpen");
        fileMenu.add(openMenuItem);

        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.setName("MenuSave");
        fileMenu.add(saveMenuItem);

        fileMenu.addSeparator();

        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic(KeyEvent.VK_E);
        exitMenuItem.setName("MenuExit");
        fileMenu.add(exitMenuItem);

        searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");
        menuBar.add(searchMenu);

        startSearchMenuItem = new JMenuItem("Start search");
        startSearchMenuItem.setName("MenuStartSearch");
        searchMenu.add(startSearchMenuItem);

        previousSearchMenuItem = new JMenuItem("Previous search");
        previousSearchMenuItem.setName("MenuPreviousMatch");
        searchMenu.add(previousSearchMenuItem);

        nextMatchMenuItem = new JMenuItem("Next match");
        nextMatchMenuItem.setName("MenuNextMatch");
        searchMenu.add(nextMatchMenuItem);

        useRegexMenuItem = new JMenuItem("Use regular expressions");
        useRegexMenuItem.setName("MenuUseRegExp");
        searchMenu.add(useRegexMenuItem);

        startSearchMenuItem.addActionListener(event -> search());
        previousSearchMenuItem.addActionListener(event -> previous());
        nextMatchMenuItem.addActionListener(event -> next());

        openMenuItem.addActionListener(event -> openFile());
        saveMenuItem.addActionListener(event -> saveFile());
        exitMenuItem.addActionListener(event -> dispose());
    }

    private void initUI() {
        panel = new JPanel();
        this.add(panel, BorderLayout.NORTH);

        Icon openIcon = new ImageIcon("/Users/andrewquach/Projects/Text Editor/src/resources/open_icon.png");
        openButton = new JButton(openIcon);
        openButton.setName("OpenButton");

        Icon saveIcon = new ImageIcon("/Users/andrewquach/Projects/Text Editor/src/resources/save_icon.png");
        saveButton = new JButton(saveIcon);
        saveButton.setName("SaveButton");

        searchField = new JTextField(15);
        searchField.setName("SearchField");

        Icon searchIcon = new ImageIcon("/Users/andrewquach/Projects/Text Editor/src/resources/search_icon.png");
        startSearchButton = new JButton(searchIcon);
        startSearchButton.setName("StartSearchButton");

        Icon previousIcon = new ImageIcon("/Users/andrewquach/Projects/Text Editor/src/resources/previous_icon.png");
        previousMatchButton = new JButton(previousIcon);
        previousMatchButton.setName("PreviousMatchButton");

        Icon nextIcon = new ImageIcon("/Users/andrewquach/Projects/Text Editor/src/resources/next_icon.png");
        nextMatchButton = new JButton(nextIcon);
        nextMatchButton.setName("NextMatchButton");

        regexCheckBox = new JCheckBox("Use regex");
        regexCheckBox.setName("UseRegExCheckbox");

        panel.add(openButton);
        panel.add(saveButton);
        panel.add(searchField);
        panel.add(startSearchButton);
        panel.add(previousMatchButton);
        panel.add(nextMatchButton);
        panel.add(regexCheckBox);

        textArea = new JTextArea(10, 15);
        textArea.setName("TextArea");
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        scrollPane = new JScrollPane(textArea);
        scrollPane.setName("ScrollPane");
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(scrollPane, BorderLayout.CENTER);

        startSearchButton.addActionListener(a -> search());
        previousMatchButton.addActionListener(a -> previous());
        nextMatchButton.addActionListener(a -> next());
        regexCheckBox.addActionListener(a -> regexCheckBox.setSelected(true));
        saveButton.addActionListener(a -> saveFile());
        openButton.addActionListener(a -> openFile());
    }

    private void openFile() {
        int returnValue = jfc.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            filePath = jfc.getSelectedFile().getAbsolutePath();
        }

        try {
            textArea.setText(Files.readString(Paths.get(filePath)));
        } catch (IOException exception) {
            textArea.setText("");
            exception.printStackTrace();
        }

    }

    private void saveFile() {
        int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            filePath = jfc.getSelectedFile().getAbsolutePath();
        }

        try {
            Files.writeString(Paths.get(filePath), textArea.getText());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void search() {
        matches = new ArrayList<>();
        String wordToFind = searchField.getText();
        Pattern pattern;
        Matcher matcher;
        String areaText;

        areaText = textArea.getText();
        pattern = Pattern.compile(wordToFind, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(areaText);

        while (matcher.find()) {
            matches.add(matcher.toMatchResult());
        }

        if (!matches.isEmpty()) {
            currentPosition = 0;
            int index = matches.get(currentPosition).start();
            String foundText = matches.get(currentPosition).group();
            textArea.setCaretPosition(index + foundText.length());
            textArea.select(index, index + foundText.length());
            textArea.grabFocus();
        }

    }

    private void previous() {
        if (currentPosition == 0) {
            currentPosition = matches.size() - 1;
        } else {
            currentPosition -= 1;
        }

        int index = matches.get(currentPosition).start();
        String foundText = matches.get(currentPosition).group();
        textArea.setCaretPosition(index + foundText.length());
        textArea.select(index, index + foundText.length());
        textArea.grabFocus();
    }

    private void next() {
        if (currentPosition == matches.size() - 1) {
            currentPosition = 0;
        } else {
            currentPosition += 1;
        }

        int index = matches.get(currentPosition).start();
        String foundText = matches.get(currentPosition).group();
        textArea.setCaretPosition(index + foundText.length());
        textArea.select(index, index + foundText.length());
        textArea.grabFocus();
    }

}


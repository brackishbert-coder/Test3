package test3;

import javax.swing.*;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

// Main application class
public class SOMHierarchyVisualizer extends JFrame {
    private MultiPanelVisualization multiPanel;
    private ReversibleSOMHierarchy somHierarchy;
    private Timer updateTimer;
    private Random random;
	private WebcamUpdater webcamUpdater;
	private ImageVectorizer imageVectorizer;
	private FeatureVectorizer featureVectorizer;
	private ClientVectorizer clientVectorizer;
    
    public SOMHierarchyVisualizer() {
    	
    	webcamUpdater = new WebcamUpdater();
    	imageVectorizer = new ImageVectorizer(webcamUpdater);
    	featureVectorizer = new FeatureVectorizer(webcamUpdater);
    	clientVectorizer = new ClientVectorizer();
    	Thread thread = new Thread(clientVectorizer);
    	thread.start();
    	
    	
    	
        setTitle("Reversible Multi-Metric SOM Hierarchy - Complete Visualization");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Initialize SOM system
        somHierarchy = new ReversibleSOMHierarchy();
        
        // Create multi-panel visualization
        multiPanel = new MultiPanelVisualization();
        add(multiPanel, BorderLayout.CENTER);
        
        // Create control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);
        
        // Setup update timer
        updateTimer = new Timer(50, e -> updateSystem());
        updateTimer.start();
        
        // Set size and center
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        // Request focus for key events
        multiPanel.getMainPanel().requestFocusInWindow();
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBackground(Color.DARK_GRAY);
        
        // Learning rate slider
        JLabel lrLabel = new JLabel("Learning Rate:");
        lrLabel.setForeground(Color.WHITE);
        panel.add(lrLabel);
        
        JSlider learningRateSlider = new JSlider(1, 50, 10);
        learningRateSlider.setPreferredSize(new Dimension(100, 25));
        learningRateSlider.addChangeListener(e -> {
            somHierarchy.setLearningRate(learningRateSlider.getValue() / 100.0);
        });
        panel.add(learningRateSlider);
        
        JLabel lrValue = new JLabel("0.10");
        lrValue.setForeground(Color.WHITE);
        learningRateSlider.addChangeListener(e -> {
            lrValue.setText(String.format("%.2f", learningRateSlider.getValue() / 100.0));
        });
        panel.add(lrValue);
        
        // Control buttons
        JButton pauseButton = new JButton("Pause/Resume");
        pauseButton.addActionListener(e -> {
            if (updateTimer.isRunning()) {
                updateTimer.stop();
                pauseButton.setText("Resume");
                somHierarchy.setContinuousLearning(false);
            } else {
                updateTimer.start();
                pauseButton.setText("Pause");
                somHierarchy.setContinuousLearning(true);
            }
        });
        panel.add(pauseButton);
        
        JButton resetButton = new JButton("Reset System");
        resetButton.addActionListener(e -> {
            somHierarchy.reset();
            if (!updateTimer.isRunning()) {
                updateTimer.start();
                pauseButton.setText("Pause");
                somHierarchy.setContinuousLearning(true);
            }
        });
        panel.add(resetButton);
        
        JButton addDataButton = new JButton("Add Random Data");
        addDataButton.addActionListener(e -> {
            for (int i = 0; i < 10; i++) {
                double[] input = somHierarchy.generateRandomInput();
                somHierarchy.processInput(input);
            }
        });
        panel.add(addDataButton);
        
        JButton trailsButton = new JButton("Toggle Trails");
        trailsButton.addActionListener(e -> {
            multiPanel.getMainPanel().showWarpTrails = !multiPanel.getMainPanel().showWarpTrails;
            multiPanel.getMainPanel().repaint();
        });
        panel.add(trailsButton);
        
        JButton toggleViewButton = new JButton("Toggle Individual SOMs");
        toggleViewButton.addActionListener(e -> {
            multiPanel.toggleIndividualSOMs();
        });
        panel.add(toggleViewButton);
        
        // Speed control
        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setForeground(Color.WHITE);
        panel.add(speedLabel);
        
        JSlider speedSlider = new JSlider(10, 200, 50);
        speedSlider.setPreferredSize(new Dimension(80, 25));
        speedSlider.addChangeListener(e -> {
            updateTimer.setDelay(speedSlider.getValue());
        });
        panel.add(speedSlider);
        
        // Status label
        JLabel statusLabel = new JLabel("Status: Learning Active");
        statusLabel.setForeground(Color.WHITE);
        panel.add(statusLabel);
        
        // Update status label with timer
        Timer statusTimer = new Timer(1000, e -> {
            if (updateTimer.isRunning()) {
                statusLabel.setText(String.format("Status: Learning Active - Rate: %.2f - Nodes: %d", 
                    somHierarchy.getLearningRate(), multiPanel.getMainPanel().visualNodes.size()));
            } else {
                statusLabel.setText(String.format("Status: Paused - Nodes: %d", 
                    multiPanel.getMainPanel().visualNodes.size()));
            }
        });
        statusTimer.start();
        
        return panel;
    }
    
    private void updateSystem() {
        if (somHierarchy.isContinuousLearning()) {
            // Generate and process random input
            double[] input = clientVectorizer.getFeatureVector();
            somHierarchy.processInput(input);
        }
        
        // Update visualization
        multiPanel.updateVisualization(somHierarchy.getBaseSOMList(), somHierarchy.getMetaSOM());
    }
    
    // Menu creation for additional features
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export State");
        exportItem.addActionListener(e -> exportSystemState());
        fileMenu.add(exportItem);
        
        JMenuItem importItem = new JMenuItem("Import State");
        importItem.addActionListener(e -> importSystemState());
        fileMenu.add(importItem);
        
        fileMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        // View menu
        JMenu viewMenu = new JMenu("View");
        JCheckBoxMenuItem showTrailsItem = new JCheckBoxMenuItem("Show Warp Trails");
        showTrailsItem.addActionListener(e -> {
            multiPanel.getMainPanel().showWarpTrails = showTrailsItem.isSelected();
            multiPanel.getMainPanel().repaint();
        });
        viewMenu.add(showTrailsItem);
        
        JCheckBoxMenuItem showIndividualItem = new JCheckBoxMenuItem("Show Individual SOMs", true);
        showIndividualItem.addActionListener(e -> {
            if (showIndividualItem.isSelected() != multiPanel.rightPanel.isVisible()) {
                multiPanel.toggleIndividualSOMs();
            }
        });
        viewMenu.add(showIndividualItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        JMenuItem controlsItem = new JMenuItem("Controls");
        controlsItem.addActionListener(e -> showControlsDialog());
        helpMenu.add(controlsItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void exportSystemState() {
        // Placeholder for export functionality
        JOptionPane.showMessageDialog(this, 
            "Export functionality would save current SOM states to file.",
            "Export State", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void importSystemState() {
        // Placeholder for import functionality
        JOptionPane.showMessageDialog(this, 
            "Import functionality would load SOM states from file.",
            "Import State", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAboutDialog() {
        String message = "Reversible Multi-Metric SOM Hierarchy Visualizer\n\n" +
                        "A complete implementation of a hierarchical self-organizing system\n" +
                        "featuring multiple distance metrics and reversible computing.\n\n" +
                        "Features:\n" +
                        "• 5 different distance metric SOMs\n" +
                        "• Meta-level organization (SOM of SOMs)\n" +
                        "• 3D visualization with real-time clustering\n" +
                        "• Individual SOM grid visualizations\n" +
                        "• Warp trails for reversible computing\n" +
                        "• Continuous learning without training phases\n\n" +
                        "Distance Metrics: Euclidean, Manhattan, Cosine, Chebyshev, Minkowski";
        
        JOptionPane.showMessageDialog(this, message, "About", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showControlsDialog() {
        String message = "Controls:\n\n" +
                        "3D View:\n" +
                        "• Mouse drag: Rotate 3D view\n" +
                        "• Space: Toggle auto-rotation\n" +
                        "• T: Toggle warp trails\n" +
                        "• R: Reset rotation\n\n" +
                        "Control Panel:\n" +
                        "• Learning Rate slider: Adjust adaptation speed\n" +
                        "• Speed slider: Control update frequency\n" +
                        "• Pause/Resume: Stop/start learning\n" +
                        "• Reset System: Reinitialize all SOMs\n" +
                        "• Add Random Data: Inject data burst\n" +
                        "• Toggle Trails: Show/hide warp trails\n" +
                        "• Toggle Individual SOMs: Show/hide right panels";
        
        JOptionPane.showMessageDialog(this, message, "Controls", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            System.out.println("Starting Complete Reversible Multi-Metric SOM Hierarchy Visualization...");
            System.out.println("=========================================================================");
            System.out.println();
            System.out.println("Features:");
            System.out.println("- 3D Meta-SOM visualization (left panel)");
            System.out.println("- Individual distance metric SOMs (right panels)");
            System.out.println("- Meta-SOM 2D grid view (bottom right)");
            System.out.println("- Real-time warp trails and BMU tracking");
            System.out.println("- Continuous learning without training phases");
            System.out.println();
            System.out.println("Controls:");
            System.out.println("- Mouse drag: Rotate 3D view");
            System.out.println("- Space: Toggle auto-rotation");
            System.out.println("- T: Toggle warp trails");
            System.out.println("- R: Reset rotation");
            System.out.println("- Use control panel for system controls");
            System.out.println("- 'Toggle Individual SOMs' to show/hide right panels");
            System.out.println();
            System.out.println("This demonstrates a complete 'SOM of SOMs' hierarchy where");
            System.out.println("different distance metrics are meta-organized with full reversibility!");
            
            SOMHierarchyVisualizer app = new SOMHierarchyVisualizer();
            app.createMenuBar(); // Add menu after creation
            app.setVisible(true);
        });
    }
}
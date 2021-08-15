package it.multicoredev.opentoall.installer;

import it.multicoredev.opentoall.OpenToALL;
import it.multicoredev.opentoall.util.CrashDialog;
import it.multicoredev.opentoall.util.InstallerUtil;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialOceanicTheme;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class InstallerGui extends JFrame implements InstallerProgress {
    public static boolean MATERIAL = true;
    private static InstallerGui INSTANCE;

    public JLabel statusLabel;
    public JButton buttonInstall;
    private JPanel pane;
    private boolean success;

    public InstallerGui() {
        try {
            if (MATERIAL) UIManager.setLookAndFeel(new MaterialLookAndFeel(new MaterialOceanicTheme()));
            else UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            setSize(404, MATERIAL ? 201 : 236);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setResizable(false);
            setTitle(OpenToALL.MOD_NAME + " Installer");
            generatePane();
            setContentPane(pane);
            setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemClassLoader().getResource("icon.png")));
            if (MATERIAL) setUndecorated(true);
        } catch (Throwable var2) {
            error(var2);
        }
    }

    public static void start() {
        INSTANCE = new InstallerGui();
        InstallerUtil.centerWindow(INSTANCE, null);
        INSTANCE.setVisible(true);
    }

    public static void restart() {
        INSTANCE.setVisible(false);
        INSTANCE.dispose();
        start();
    }

    private void generatePane() {
        pane = new JPanel();
        pane.setName("Pane");
        pane.setLayout(new BorderLayout(5, 5));
        pane.setPreferredSize(new Dimension(404, 203));

        // ROOTBAR
        if (MATERIAL) {
            JPanel rootBar = generateRootBar();
            pane.add(rootBar);
        }
        JLayeredPane mainPane = new JLayeredPane();
        int y = MATERIAL ? 5 : -2;
        mainPane.setLayout(null);

        // IMAGE
        try {
            JLabel imageLabel;
            if (MATERIAL) {
                Image image = new ImageIcon(ClassLoader.getSystemClassLoader().getResource("material_icon.png" )).getImage();
                imageLabel = new JLabel(new ImageIcon(image.getScaledInstance(64, 64, Image.SCALE_FAST)));
            } else {
                imageLabel = new JLabel("<html><p style=\"color:#EBEBEB\">Material<br>Design<br>Version</p></html>");

            }

            imageLabel.setSize(64, 64);
            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        MATERIAL = !MATERIAL;
                        restart();
                    }
                }
            });
            imageLabel.setLocation(20, y);
            mainPane.add(imageLabel);
            mainPane.setLayer(imageLabel, -1);
        } catch (Exception ignored) {
        }

        // MOD NAME
        JLabel modName = new JLabel(OpenToALL.MOD_NAME + " " + OpenToALL.MOD_VERSION);
        modName.setFont(new Font("Dialog", Font.BOLD, 20));
        modName.setHorizontalAlignment(0);
        modName.setBounds(2, 5 + y, 385, 42);

        mainPane.add(modName);

        // MINECRAFT VERSION
        JLabel mineVersion = new JLabel("for Minecraft " + OpenToALL.MINECRAFT_VERSION);
        mineVersion.setFont(new Font("Dialog", Font.BOLD, 14));
        mineVersion.setHorizontalAlignment(0);
        mineVersion.setBounds(2, 38 + y, 385, 25);

        mainPane.add(mineVersion);

        // INFO
        JTextArea info = new JTextArea("This installer will install " + OpenToALL.MOD_NAME + " in the official Minecraft launcher and will create a new profile to run it.");
        info.setFont(new Font("Dialog", Font.PLAIN, 12));
        info.setEditable(false);
        info.setEnabled(true);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setOpaque(false);
        info.setBounds(15, 66 + y, 365, 44);

        mainPane.add(info);

        // LABEL FOLDER
        JLabel labelFolder = new JLabel("Folder");
        labelFolder.setBounds(15, 116 + y, 47, 16);

        mainPane.add(labelFolder);

        // FIELD FOLDER
        JTextField fieldFolder = new JTextField();
        fieldFolder.setBounds(62, 114 + y, 287, 20);
        fieldFolder.setEditable(false);
        fieldFolder.setBackground(new Color(0, 0, 0, 0));
        fieldFolder.setText(InstallerUtil.getWorkingDirectory().toString());

        mainPane.add(fieldFolder);

        // BUTTON FOLDER
        JButton buttonFolder = new JButton("...");
        buttonFolder.setBounds(350, 114 + y, 25, 20);
        buttonFolder.addActionListener(e -> {
            File dirMc = new File(fieldFolder.getText());
            JFileChooser jfc = new JFileChooser(dirMc);
            jfc.setFileSelectionMode(1);
            jfc.setAcceptAllFileFilterUsed(false);
            if (jfc.showOpenDialog(this) == 0) {
                File dir = jfc.getSelectedFile();
                fieldFolder.setText(dir.getPath());
            }
        });

        mainPane.add(buttonFolder);

        // STATUS
        statusLabel = new JLabel("Installer loaded");
        statusLabel.setFont(new Font("Dialog", Font.ITALIC, 12));
        statusLabel.setForeground(UIManager.getColor("Label.foreground"));
        statusLabel.setHorizontalAlignment(0);
        statusLabel.setBounds(2, 133 + y, 385, 20);

        mainPane.add(statusLabel);

        // BUTTON INSTALL
        buttonInstall = new JButton("Install");
        buttonInstall.addActionListener(e -> {
            buttonInstall.setEnabled(false);
            String loaderVersion = Installer.LOADER_META.getLatestVersion(false).getVersion();
            try {
                install(Path.of(fieldFolder.getText()), loaderVersion);
            } catch (Exception ex) {
                ex.printStackTrace();
                error(ex);
            }
        });
        buttonInstall.setBounds(117, 155 + y, 150, 35);

        mainPane.add(buttonInstall);

        pane.add(mainPane);
    }

    public JPanel generateRootBar() {
        JPanel rootBar = new JPanel(null);
        rootBar.setBackground(new Color(0, 0, 0, 0));
        rootBar.setSize(404, 30);
        JButton exit = new JButton("X");
        exit.setLocation(369, 0);
        exit.setSize(35, 30);
        exit.addActionListener((ae) -> System.exit(0));
        rootBar.add(exit, new GridBagConstraints());

        final int[] mp = new int[2];
        rootBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mp[0] = e.getX();
                mp[1] = e.getY();
            }
        });

        rootBar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(
                        getLocation().x + e.getX() - mp[0],
                        getLocation().y + e.getY() - mp[1]);
            }
        });

        return rootBar;
    }

    @Override
    public void updateProgress(String text) {
        System.out.println(text);
        statusLabel.setText(text);
        statusLabel.setForeground(UIManager.getColor("Label.foreground"));
    }

    protected String buildEditorPaneStyle() {
        JLabel label = new JLabel();
        label.setForeground(Color.RED);
        Font font = label.getFont();
        Color background = label.getBackground();
        Color foreground = label.getForeground();
        return String.format(
                "font-family:%s;font-weight:%s;font-size:%dpt;background-color: rgb(%d,%d,%d);color: rgb(%d,%d,%d)",
                font.getFamily(), (font.isBold() ? "bold" : "normal"), font.getSize(), background.getRed(), background.getGreen(), background.getBlue(), foreground.getRed(), foreground.getGreen(), foreground.getBlue()
        );
    }

    protected String buildSuccessfulTest() {
        return "Open to ALL for Minecraft " + OpenToALL.MINECRAFT_VERSION + " has been successfully installed.";
    }

    @Override
    public void error(Throwable throwable) {
        success = false;

        StringWriter sw = new StringWriter(800);

        try (PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
        }

        String st = sw.toString().trim();
        System.err.println(st);

        String html = String.format("<html><body style=\"%s\">%s</body></html>",
                buildEditorPaneStyle(),
                st.replace(System.lineSeparator(), "<br>").replace("\t", "&ensp;"));
        JEditorPane textPane = new JEditorPane("text/html", html);
        textPane.setEditable(false);

        try {
            statusLabel.setText(throwable.getClass().getSimpleName());
            statusLabel.setForeground(Color.RED);
        } catch (Exception e) {
            new CrashDialog(e);
            e.printStackTrace();
        }

        JOptionPane.showMessageDialog(
                this,
                textPane,
                "Exception occurred!",
                JOptionPane.ERROR_MESSAGE
        );
    }


    public void install(Path dirMc, String loaderVersion) throws Exception {
        success = true;
        System.out.println("Dir minecraft: " + dirMc);
        System.out.println(OpenToALL.MOD_NAME + " version: " + OpenToALL.MOD_VERSION);
        System.out.println("Minecraft version: " + OpenToALL.MINECRAFT_VERSION);

        updateProgress("Installing");

        new Thread(() -> {
            try {
                updateProgress("Installing Fabric Loader " + loaderVersion + " on the client");
                String profileName = String.format("fabric-loader-%s-%s", loaderVersion, OpenToALL.MINECRAFT_VERSION);

                Path versionsDir = dirMc.resolve("versions");
                Path profileDir = versionsDir.resolve(profileName);

                if (!Files.exists(profileDir)) {
                    Files.createDirectories(profileDir);
                }

                FabricInstaller.install(dirMc, profileName, loaderVersion, this);
                ProfileInstaller.setupProfile(dirMc, profileName, OpenToALL.MINECRAFT_VERSION, this);
                copyMod(dirMc);
                SwingUtilities.invokeLater(this::showInstalledMessage);
            } catch (Exception e) {
                error(e);
            }

            buttonInstall.setEnabled(true);
        }).start();
    }

    private void copyMod(Path dirMc) {
        updateProgress("Copying " + OpenToALL.MOD_NAME + " JAR");
        File modJar = null;
        try {
            modJar = new File(OpenToALL.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            error(e);
        }
        try {
            Files.copy(modJar.toPath(), dirMc.resolve("mods").resolve(modJar.getName()));
        } catch (IOException e) {
            e.printStackTrace();
            error(e);
        }
    }

    private void showInstalledMessage() {
        if (success) {
            JEditorPane pane = new JEditorPane("text/html", "<html><body style=\"" + buildEditorPaneStyle() + "\">" + buildSuccessfulTest() + "</body></html>");
            pane.setEditable(false);

            pane.addHyperlinkListener(e -> {
                try {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } else {
                            throw new UnsupportedOperationException("Failed to open " + e.getURL().toString());
                        }
                    }
                } catch (Throwable throwable) {
                    error(throwable);
                }
            });

            JOptionPane.showMessageDialog(null, pane, "Successfully Installed", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

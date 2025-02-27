package me.n1ar4.jar.analyzer.gui.tree;

import me.n1ar4.jar.analyzer.starter.Const;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class FileTree extends JTree {
    protected DefaultMutableTreeNode rootNode;
    protected DefaultTreeModel fileTreeModel;
    private final DefaultTreeModel savedModel;
    private static ImageIcon classIcon;

    static {
        try {
            classIcon = new ImageIcon(ImageIO.read(
                    Objects.requireNonNull(FileTree.class
                            .getClassLoader().getResourceAsStream("img/class.png"))));
        } catch (Exception ignored) {
        }
    }

    public FileTree() {
        savedModel = (DefaultTreeModel) this.getModel();
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                          boolean sel, boolean expanded,
                                                          boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (leaf && value instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                    String nodeText = node.getUserObject().toString();
                    String fileExtension = getFileExtension(nodeText);
                    if (fileExtension != null && fileExtension.equalsIgnoreCase("class")) {
                        setText(nodeText.split("\\.")[0]);
                        setIcon(classIcon);
                    }
                }
                return this;
            }

            private String getFileExtension(String fileName) {
                int dotIndex = fileName.lastIndexOf(".");
                if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                    return fileName.substring(dotIndex + 1);
                }
                return null;
            }
        };
        this.setCellRenderer(renderer);

        setModel(null);
    }

    public void refresh() {
        setModel(savedModel);
        fileTreeModel = (DefaultTreeModel) treeModel;
        initComponents();
        initListeners();
        repaint();
    }

    private void initComponents() {
        initRoot();
        setEditable(false);
    }

    private void initListeners() {
        addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent event) {
            }

            public void treeExpanded(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                treeNode.removeAllChildren();
                populateSubTree(treeNode);
                fileTreeModel.nodeStructureChanged(treeNode);
            }
        });
    }

    private void initRoot() {
        File[] roots;
        roots = new File[]{new File(Const.tempDir)};
        rootNode = new DefaultMutableTreeNode(new FileTreeNode(roots[0]));
        populateSubTree(rootNode);
        if (fileTreeModel != null && rootNode != null) {
            fileTreeModel.setRoot(rootNode);
        }
    }

    private void populateSubTree(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof FileTreeNode) {
            FileTreeNode fileTreeNode = (FileTreeNode) userObject;
            File[] files = fileTreeNode.file.listFiles();
            if (files == null) {
                return;
            }

            List<File> fileList = Arrays.asList(files);
            fileList.sort((o1, o2) -> {
                String name1 = o1.getName();
                String name2 = o2.getName();
                boolean isClassFile1 = name1.endsWith(".class");
                boolean isClassFile2 = name2.endsWith(".class");
                if (isClassFile1 && !isClassFile2) {
                    return 1;
                }
                if (!isClassFile1 && isClassFile2) {
                    return -1;
                }
                return name1.compareTo(name2);
            });

            for (File file : fileList) {

                TreeFileFilter filter = new TreeFileFilter(file, true, true);
                if (filter.shouldFilter()) {
                    continue;
                }

                FileTreeNode subFile = new FileTreeNode(file);
                DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(subFile);
                if (file.isDirectory()) {
                    subNode.add(new DefaultMutableTreeNode("fake"));
                }
                node.add(subNode);
            }
        }
    }
}

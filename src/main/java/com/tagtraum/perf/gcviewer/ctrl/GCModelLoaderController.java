package com.tagtraum.perf.gcviewer.ctrl;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.tagtraum.perf.gcviewer.ctrl.FileDropTargetListener.DropFlavor;
import com.tagtraum.perf.gcviewer.model.GCResource;
import com.tagtraum.perf.gcviewer.view.GCDocument;
import com.tagtraum.perf.gcviewer.view.GCViewerGui;
import com.tagtraum.perf.gcviewer.view.GCViewerGuiMenuBar;
import com.tagtraum.perf.gcviewer.view.model.RecentResourceNamesModel;

/**
 * Main controller class of GCViewer. Is responsible for control flow. 
 * 
 * @author <a href="mailto:gcviewer@gmx.ch">Joerg Wuethrich</a>
 * <p>created on: 15.12.2013</p>
 */
public class GCModelLoaderController {
    private GCViewerGui gcViewerGui;
    
    /**
     * Constructor is package protected, because this controller should only be instantiated in 
     * this package.
     */
    GCModelLoaderController(GCViewerGui gcViewerGui) {
        super();
        
        this.gcViewerGui = gcViewerGui;
    }
    
    public void add(String resourceName) {
        add(new File[]{ new File(resourceName) });
    }
    
    public void add(File[] files) {
        for (File file : files) {
            addModel(file.getAbsolutePath());
        }

        getRecentResourceNamesModel().add(toStringList(files));
    }
    
    private void addModel(String resourceName) {
        GCModelLoader loader = new GCModelLoader(new GCResource(resourceName));
        GCDocumentController docController = getDocumentController(gcViewerGui.getSelectedGCDocument());
        docController.addModel(loader);
        
        loader.execute();
    }
    
    private GCDocumentController getDocumentController(GCDocument document) {
        GCDocumentController controller = null;
        for (PropertyChangeListener listener : document.getPropertyChangeListeners()) {
            if (listener instanceof GCDocumentController) {
                controller = (GCDocumentController) listener;
            }
        }
        
        assert controller != null : "instance of GCDocumentController should have been found!";
        
        return controller;
    }
    
    protected GCViewerGui getGCViewerGui() {
        return this.gcViewerGui;
    }
    
    private RecentResourceNamesModel getRecentResourceNamesModel() {
        return ((GCViewerGuiMenuBar) this.gcViewerGui.getJMenuBar()).getRecentResourceNamesModel();
    }
    
    private void openModel(String resourceName) {
        GCModelLoader loader = new GCModelLoader(new GCResource(resourceName));
        GCDocument document = new GCDocument(gcViewerGui.getPreferences(), resourceName);
        document.setDropTarget(
                new DropTarget(document, 
                        DnDConstants.ACTION_COPY, 
                        new FileDropTargetListener(this, DropFlavor.ADD))
                );
        document.addInternalFrameListener(new GCViewerGuiInternalFrameController());
        
        gcViewerGui.addDocument(document);
        
        GCDocumentController docController = new GCDocumentController(document);
        docController.addModel(loader);
        
        loader.execute();
    }
    
    /**
     * Open a new log file.
     * @param file filename
     */
    public void open(File file) {
        open(file.getAbsolutePath());
    }
        
    public void open(File[] files) {
        List<String> fileNames = new LinkedList<String>();
        for (File file : files) {
            fileNames.add(file.getAbsolutePath());
        }
        open(fileNames.toArray(new String[fileNames.size()]));
    }
    
    /**
     * Open a gc log resource from a filename or URL.
     * 
     * @param resourceName filename or URL name.
     */
    public void open(String resourceName) {
        open(new String[]{ resourceName });
    }
    
    public void open(String[] resourceNames) {
        String nameGroup = Arrays.asList(resourceNames).toString();
        nameGroup = nameGroup.substring(1, nameGroup.length() - 1);
        int i = 0; 
        for (String name : resourceNames) {
            if (i == 0) {
                openModel(name);
            }
            else {
                addModel(name);
            }
            ++i;
        }
        
        getRecentResourceNamesModel().add(resourceNames);
    }
    
    public void open(URL[] urls) {
        List<String> resourceNameList = new LinkedList<String>();
        for (URL url : urls) {
            resourceNameList.add("file".equals(url.getProtocol()) ? url.getPath() : url.toString());
        }
        
        open(resourceNameList.toArray(new String[resourceNameList.size()]));
    }
    
    /**
     * Reload all models of <code>gcDocument</code> and provide tracker. The tracker will
     * fire a propertyChangeEvent, as soon as all GCModelLoaders have finished loading.
     * 
     * @param gcDocument document of which models should be reloaded
     * @return tracker to track finish state of all models being loaded
     */
    public GCModelLoaderGroupTracker reload(GCDocument gcDocument) {
        GCModelLoaderGroupTracker tracker = new GCModelLoaderGroupTracker();
        for (GCResource gcResource : gcDocument.getGCResources()) {
            GCModelLoader loader = new GCModelLoader(gcResource);
            GCDocumentController docController = getDocumentController(gcDocument);
            docController.reloadModel(loader);
            
            tracker.addGcModelLoader(loader);
        }
        
        tracker.execute();
        
        return tracker;
    }
    
    /**
     * Set GCViewerGui (for test purposes).
     * @param gui gui to be set
     */
    protected void setGCViewerGui(GCViewerGui gui) {
        this.gcViewerGui = gui;
    }
    
    private String[] toStringList(File[] resourceNames) {
        List<String> resourceNameList = new LinkedList<String>();
        for (File file : resourceNames) {
            resourceNameList.add(file.getAbsolutePath());
        }
        
        return resourceNameList.toArray(new String[resourceNameList.size()]);
    }
    
}
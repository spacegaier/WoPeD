package org.woped.qualanalysis;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.jgraph.JGraph;
import org.woped.core.config.ConfigurationManager;
import org.woped.core.utilities.LoggerManager;
import org.woped.qualanalysis.reachabilitygraph.gui.ReachabilityGraphPanel;
import org.woped.qualanalysis.reachabilitygraph.gui.ReachabilityGraphVC;
import org.woped.qualanalysis.test.ReferenceProvider;
import org.woped.translations.Messages;

public class ImageExport {
	
	private final static int 	MAX_WIDTH			= 2100;
	private final static int	MAX_HEIGHT			= 2100;

    public static RenderedImage getRenderedImage(ReachabilityGraphPanel editor) {
	JGraph graph = editor.getGraph();
        graph.clearSelection();
        Object[] cells = graph.getRoots();
        BufferedImage image = null;
        
        if (cells.length > 0) {
            Rectangle2D rectangle = graph.getCellBounds(cells);

            graph.setGridVisible(false);
            graph.toScreen(rectangle);

            // Create a Buffered Image
            Dimension dimension = rectangle.getBounds().getSize();
            if(dimension.width < MAX_WIDTH && dimension.height < MAX_HEIGHT)
            {
            	image = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB);
            	Graphics2D graphics = image.createGraphics();
                graphics.translate(-rectangle.getX(), -rectangle.getY());
                graph.paint(graphics);

                graph.setGridVisible(ConfigurationManager.getConfiguration().isShowGrid());   
            }
            else
            {
            	ReferenceProvider mediator = new ReferenceProvider();
            	JOptionPane.showMessageDialog(mediator.getMediatorReference().getUi().getComponent(),
    					Messages.getString("QuanlAna.ReachabilityGraph.ExportFailed") + MAX_HEIGHT + "X" + MAX_WIDTH, Messages
    							.getString("QuanlAna.ReachabilityGraph.ExportFailed.Title"),
    					JOptionPane.ERROR_MESSAGE);
            }
                     
        }
        
        return image;
    }
    
    public static boolean saveJPG(RenderedImage image, File file) {
    	if(image != null)
    	{
    		try {
    			ImageIO.write((RenderedImage) image, "jpg", file);
    			LoggerManager.info(Constants.QUALANALYSIS_LOGGER, "File saved to: " + file.getAbsolutePath());
    			return true;
    		} catch (IOException e) {
    			e.printStackTrace();
    			LoggerManager.error(Constants.QUALANALYSIS_LOGGER, "Could not export as JPG");
    			return false;
    		}
    	}
    	else
    	{
    		return false;
    	}
    }
    
    public static boolean savePNG(RenderedImage image, File file) {
	if(image != null){
		try {
		    ImageIO.write((RenderedImage) image, "png", file);
		    LoggerManager.info(Constants.QUALANALYSIS_LOGGER, "File saved to: " + file.getAbsolutePath());
		    return true;
		} catch (IOException e) {
		    e.printStackTrace();
		    LoggerManager.error(Constants.QUALANALYSIS_LOGGER, "Could not export as PNG");
		    return false;
		}
	}
	else{
		return false;
	}
    }
    
    public static boolean saveBMP(RenderedImage image, File file) {
	if(image != null){
		try {
		    ImageIO.write((RenderedImage) image, "bmp", file);
		    LoggerManager.info(Constants.QUALANALYSIS_LOGGER, "File saved to: " + file.getAbsolutePath());
		    return true;
		} catch (IOException e) {
		    e.printStackTrace();
		    LoggerManager.error(Constants.QUALANALYSIS_LOGGER, "Could not export as BMP");
		    return false;
		}
	}
	else{
		return false;
	}
    }
}

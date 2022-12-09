import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Diagnostics.Debugger;
import NeuralNetwork.ActivationFunction;
import NeuralNetwork.Dataset;
import NeuralNetwork.Network;
import NeuralNetwork.SigmoidFunction;

class ButtonsPanel extends JPanel implements ActionListener {
	private Network network;
	//private Siec siec;
	private int [] tabNeuronow = {7, 5, 4};
	private JButton rozpoznaj;
	private JButton ucz;
	private JButton testuj;
	
	private enum Actions {
	    ZAPISZ8x8,
	    WYCZYSC,
	    UCZ,
	    ROZPOZNAJ,
	    TESTUJ
	  }
	public ButtonsPanel() {
		this.setPreferredSize(new Dimension(120, 100));
		this.setBackground(Color.lightGray);
		
		rozpoznaj=new JButton("Rozpoznaj");
		JButton wyczysc=new JButton("Wyczyść");
		JButton zapisz8x8=new JButton("Zapisz 8x8");
		testuj = new JButton("Testuj");
		ucz=new JButton("Ucz");
		
		zapisz8x8.addActionListener(this);
		zapisz8x8.setActionCommand(Actions.ZAPISZ8x8.name());
		wyczysc.addActionListener(this);
		wyczysc.setActionCommand(Actions.WYCZYSC.name());
		testuj.addActionListener(this);
		testuj.setActionCommand(Actions.TESTUJ.name());
		testuj.setEnabled(false);
		ucz.addActionListener(this);
		ucz.setActionCommand(Actions.UCZ.name());
		rozpoznaj.addActionListener(this);
		rozpoznaj.setEnabled(false);
		rozpoznaj.setActionCommand(Actions.ROZPOZNAJ.name());
		
		rozpoznaj.setAlignmentX(CENTER_ALIGNMENT);
		zapisz8x8.setAlignmentX(CENTER_ALIGNMENT);
		wyczysc.setAlignmentX(CENTER_ALIGNMENT);
		testuj.setAlignmentX(CENTER_ALIGNMENT);
		ucz.setAlignmentX(CENTER_ALIGNMENT);
		
		
		
		setLayout (new BoxLayout (this, BoxLayout.Y_AXIS));
		this.add(rozpoznaj);
		this.add(wyczysc);
		this.add(zapisz8x8);
		this.add(testuj);
		this.add(ucz);
		
		
		
	}
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == Actions.ZAPISZ8x8.name()) {
	       
			JFileChooser fileChooser = new JFileChooser();
		      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		      int result = fileChooser.showSaveDialog(this);
		      if (result != JFileChooser.APPROVE_OPTION)
		         return;
		      File saveFile = fileChooser.getSelectedFile();
		      if (!saveFile.getAbsolutePath().toLowerCase().endsWith(".png"))
		         saveFile = new File(saveFile.getAbsolutePath() + ".png");
		      BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
		      for (int x = 0; x < 8; x++) {    	  
		         for (int y = 0; y < 8; y++) {        	 
		        	 int licznik_pikseli = 0;
		        	 for (int i = 0; i < (int)Paint.paintPanel.getSize().width/8; i++) {        		 
		        		 for (int j = 0; j < (int)Paint.paintPanel.getSize().height/8; j++ ) {        			 
		        			 if (Paint.paintPanel.isPixel(new Point(x*(int)Paint.paintPanel.getSize().width/8+i, y*(int)Paint.paintPanel.getSize().height/8+j))) {
		        				 licznik_pikseli ++;        				 
		        			 }
		        		 }
		        	 }
		        	 int kolor_value = 255 - (255*licznik_pikseli)/ (Paint.paintPanel.getSize().width/8*Paint.paintPanel.getSize().height/8);
		        	 Color col = new Color(kolor_value, kolor_value, kolor_value);
		            image.setRGB(x, y, col.getRGB());
		            
		         }
		      }
		      try {
		         ImageIO.write(image, "png", saveFile);
		      } catch (IOException exception) {
		         return;
		      }
			
	       
		}
		if (e.getActionCommand() == Actions.WYCZYSC.name()) {
			Paint.paintPanel.clear();
		}
		if (e.getActionCommand() == Actions.UCZ.name()) {
			ucz.setEnabled(false);
			Ucz uczObject = new Ucz();
			double [][] ciagUczacy = uczObject.WczytajiUcz("uczace");
			double [][] ciagOczekiwany = uczObject.WczytajOczekiwane("uczace");
			ActivationFunction function = new SigmoidFunction();
			network = new Network(function, 64, 3, tabNeuronow);
			Dataset [] daneUczace = new Dataset[ciagUczacy.length];
			for(int i = 0; i < ciagUczacy.length; i++) {
				daneUczace[i] = new Dataset(ciagUczacy[i], ciagOczekiwany[i]);
			}
			network.learn(daneUczace, 150);
			rozpoznaj.setEnabled(true);
			testuj.setEnabled(true);
		}
		if (e.getActionCommand() == Actions.ROZPOZNAJ.name()) {
			String komunikat;
			Rozpoznaj rozpoznaj = new Rozpoznaj();
			double [] doRozpoznania = rozpoznaj.RozpoznajLitere();
			double [] rezultat;
			rezultat = network.getOutput(doRozpoznania);
			Rozpoznaj.Litera litera = rozpoznaj.klasyfikujLiterę(rezultat);
			switch(litera) {
			case LITERA_W:
				komunikat = "W";
				break;
			case LITERA_M:
				komunikat = "M";
				break;
			case LITERA_N:
				komunikat = "N";
				break;
			default:
				komunikat = "niezdefiniowana";
			}

			Debugger.log("W: " + String.format("%.2f", rezultat[0]));
			Debugger.log("M: " + String.format("%.2f", rezultat[1]));
			Debugger.log("N: " + String.format("%.2f", rezultat[2]));
			Debugger.log("");
			JOptionPane.showMessageDialog(null, komunikat, "Rozpoznana litera", JOptionPane.INFORMATION_MESSAGE);
		}
		if(e.getActionCommand() == Actions.TESTUJ.name()) {
			Ucz uczObject = new Ucz();
			Rozpoznaj rozpoznaj = new Rozpoznaj();
			double [] rezultat;
			double [][] ciagTestowy = uczObject.WczytajiUcz("testy");
			double [][] ciagOczekiwany = uczObject.WczytajOczekiwane("testy");

			Rozpoznaj.Litera litera;
			int poprawne = 0;
			for(int i = 0; i < ciagTestowy.length; i++) {
				rezultat = network.getOutput(ciagTestowy[i]);
				litera = rozpoznaj.klasyfikujLiterę(rezultat);
				for(int j = 0; j < rezultat.length; j++) {
					if((ciagOczekiwany[i][j] > 0.5) && (j == litera.ordinal())) {
						poprawne++;
					}
				}
			}
			String komunikat = String.format("%d", poprawne * 100 / ciagTestowy.length);
			Debugger.log("Skuteczność: " + komunikat + "%");
			JOptionPane.showMessageDialog(null, komunikat + "%", "Skuteczność sieci", JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
class ResizeListener extends ComponentAdapter {
    public void componentResized(ComponentEvent e) {
    	
    	Paint.paintPanel.repaint();
    }
}

public class Paint extends JFrame implements ActionListener {
   private final String ACTION_NEW = "New Image";
   private final String ACTION_LOAD = "Load Image";
   private final String ACTION_SAVE = "Save Image";
   
   static JPanel container = new JPanel();
   public static SimplePaintPanel paintPanel = new SimplePaintPanel();
   private final ButtonsPanel buttonsPanel = new ButtonsPanel(); 

   public Paint() {
      super();
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setTitle("Neural Network");
      Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
      
      //setPreferredSize(new Dimension(600,300));
      setPreferredSize(new Dimension(size.height/2+70,size.height/2));
      setLocation(size.width/4, size.height/4);
      setResizable(true);

      initMenu();
      
      container.setLayout(new BorderLayout() );
      container.add(paintPanel, BorderLayout.CENTER);
      container.add(buttonsPanel, BorderLayout.LINE_END);
      
      this.getContentPane().add(container);
      
      
      container.addComponentListener(new ResizeListener());
      pack();
      setVisible(true);
   }

   private void initMenu() {
      JMenuBar menuBar = new JMenuBar();
      JMenu menu = new JMenu("File");
      JMenuItem mnuNew = new JMenuItem(ACTION_NEW);
      JMenuItem mnuLoad = new JMenuItem(ACTION_LOAD);
      JMenuItem mnuSave = new JMenuItem(ACTION_SAVE);
      mnuNew.setActionCommand(ACTION_NEW);
      mnuLoad.setActionCommand(ACTION_LOAD);
      mnuSave.setActionCommand(ACTION_SAVE);
      mnuNew.addActionListener(this);
      mnuLoad.addActionListener(this);
      mnuSave.addActionListener(this);
      menu.add(mnuNew);
      menu.add(mnuLoad);
      menu.add(mnuSave);
      menuBar.add(menu);
      this.setJMenuBar(menuBar);
      
   }
   
   
   @Override
   public void actionPerformed(ActionEvent ev) {
      switch (ev.getActionCommand()) {
      case ACTION_NEW:
         paintPanel.clear();
         break;
      case ACTION_LOAD:
         doLoadImage();
         break;
      case ACTION_SAVE:
         doSaveImage();
         break;
      }
   }

   private void doSaveImage() {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int result = fileChooser.showSaveDialog(this);
      if (result != JFileChooser.APPROVE_OPTION)
         return;
      File saveFile = fileChooser.getSelectedFile();
      if (!saveFile.getAbsolutePath().toLowerCase().endsWith(".png"))
         saveFile = new File(saveFile.getAbsolutePath() + ".png");
      BufferedImage image = new BufferedImage(paintPanel.getSize().width, paintPanel.getSize().height,
            BufferedImage.TYPE_INT_RGB);
      for (int x = 0; x < image.getWidth(); x++) {
         for (int y = 0; y < image.getHeight(); y++) {
            image.setRGB(x, y, Color.white.getRGB());
            if (paintPanel.isPixel(new Point(x, y))) {
               image.setRGB(x, y, Color.black.getRGB());
            }
         }
      }
      try {
         ImageIO.write(image, "png", saveFile);
      } catch (IOException e) {
         return;
      }
   }

   private void doLoadImage() {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      int result = fileChooser.showOpenDialog(this);
      if (result != JFileChooser.APPROVE_OPTION)
         return;
      BufferedImage image;
      File openFile = fileChooser.getSelectedFile();
      try (FileInputStream fis = new FileInputStream(openFile)) {
         image = ImageIO.read(fis);
      } catch (IOException e) {
         return;
      }
      if (image == null)
         return;
      paintPanel.clear();
      Set<Point> blackPixels = new HashSet<Point>();
      for (int x = 0; x < image.getWidth(); x++) {
         for (int y = 0; y < image.getHeight(); y++) {
            Color c = new Color(image.getRGB(x, y));
            if ((c.getBlue() < 128 || c.getRed() < 128 || c.getGreen() < 128) && c.getAlpha() == 255) {
               blackPixels.add(new Point(x, y));
            }
         }
      }
      paintPanel.addPixels(blackPixels);
   }
   
   private void doLoadData() {
	      JFileChooser fileChooser = new JFileChooser();
	      fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	      int result = fileChooser.showOpenDialog(this);
	      if (result != JFileChooser.APPROVE_OPTION)
	         return;
	      BufferedImage image;
	      File openFile = fileChooser.getSelectedFile();
	      try (FileInputStream fis = new FileInputStream(openFile)) {
	         image = ImageIO.read(fis);
	      } catch (IOException e) {
	         return;
	      }
	      if (image == null)
	         return;
	      double wartosci[];
	      wartosci = new double[image.getWidth()*image.getHeight()];
	      for (int x = 0; x < image.getWidth(); x++) {
	         for (int y = 0; y < image.getHeight(); y++) {
	            Color c = new Color(image.getRGB(x, y));          
	            wartosci[x*image.getWidth()+y] = (255.0 - c.getBlue()) / 255;            
	         }
	      } 
	   }

   public static void main(String[] args) {
	   Debugger.setEnabled(false);
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            new Paint();
         }
      });
   }
}

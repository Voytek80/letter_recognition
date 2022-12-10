import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Diagnostics.Debugger;
import NeuralNetwork.ActivationFunction;
import NeuralNetwork.Dataset;
import NeuralNetwork.Network;
import NeuralNetwork.SigmoidFunction;

enum Letter {
	W, M, N, UNDEFINED
}

enum DatasetType {
	LEARNING, TESTING
}

public class App extends JFrame {
	private static final long serialVersionUID = 1L;
	private int width, height;
	private final int MARGIN = 15;
	private PaintPanel paintPanel = new PaintPanel();

	// Ustawienia sieci
	private Network network;
	private int epochCount = 900;
	private int [] neurons = {7, 5, Letter.UNDEFINED.ordinal()};
	private int cols = 8, rows = 8;
	private double treshold = 0.85;

	public App(String title) {
		super(title);
		width = paintPanel.getPreferredSize().width + 3 * MARGIN + 115;
		height = paintPanel.getPreferredSize().height + 4 * MARGIN + 10;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension dimensions = kit.getScreenSize();
		setBounds((dimensions.width - width)/2, (dimensions.height-height)/2, width, height);
		setLayout(new FlowLayout(FlowLayout.LEADING, MARGIN, MARGIN));
		setResizable(false);

		JButton recognizeBtn = new JButton("Rozpoznaj");
		recognizeBtn.setEnabled(false);
		recognizeBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				double [] data = paintPanel.serialize(cols, rows);
				double [] results = network.getOutput(data);
				Letter letter = recognizeLetter(results);
				Debugger.log("W: " + String.format("%.2f", results[0]));
				Debugger.log("M: " + String.format("%.2f", results[1]));
				Debugger.log("N: " + String.format("%.2f", results[2]));
				Debugger.log("");
				JOptionPane.showMessageDialog(null, letter.toString(), "Rozpoznana litera", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		JButton clearBtn = new JButton("Wyczyść");
		clearBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				paintPanel.clear();
			}
		});

		JButton saveBtn = new JButton("Zapisz jako");
		saveBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveImage();
			}
		});

		JButton testBtn = new JButton("Testuj sieć");
		testBtn.setEnabled(false);
		testBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int correct = 0;
				Dataset [] dataset = loadDataset(DatasetType.TESTING);
				for(int i = 0; i < dataset.length; i++) {
					if(network.testData(dataset[i], treshold)) {
						correct++;
					}
				}
				String msg = String.format("%d", correct * 100 / dataset.length);
				Debugger.log("Skuteczność: " + msg + "%");
				JOptionPane.showMessageDialog(null, msg + "%", "Skuteczność sieci", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		JButton learnBtn = new JButton("Ucz sieć");
		learnBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ActivationFunction function = new SigmoidFunction();
				network = new Network(function, cols * rows, neurons.length, neurons);
				Dataset [] dataset = loadDataset(DatasetType.LEARNING);
				network.learn(dataset, epochCount);
				JOptionPane.showMessageDialog(null, "Sieć została nauczona", "", JOptionPane.INFORMATION_MESSAGE);
				recognizeBtn.setEnabled(true);
				testBtn.setEnabled(true);
			}
		});

		int rows = Math.round((float)(paintPanel.getPreferredSize().height) / (float)((recognizeBtn.getPreferredSize().height + 5)));
		GridLayout panelLayout = new GridLayout(rows, 1, 0, 5);
		JPanel menuPanel = new JPanel(panelLayout);
		menuPanel.add(recognizeBtn);
		menuPanel.add(clearBtn);
		menuPanel.add(saveBtn);
		menuPanel.add(testBtn);
		menuPanel.add(learnBtn);
		add(paintPanel);
		add(menuPanel);
		setVisible(true);
	}

	protected Dataset loadImage(File file, Letter letter) {
		Dataset dataset;
		BufferedImage image;
		try (FileInputStream fis = new FileInputStream(file)) {
			image = ImageIO.read(fis);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Nie znaleziono pliku: " + file.getName(), "", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		double [] data = PaintPanel.serializeScaledImage(image);
		double [] desiredOutput = new double[Letter.UNDEFINED.ordinal()];
		if(letter != Letter.UNDEFINED) {
			desiredOutput[letter.ordinal()] = 1.0;
		}
		dataset = new Dataset(data, desiredOutput);
		return dataset;
	}

	protected Dataset [] loadDataset(DatasetType type) {
		int lettersCount = Letter.UNDEFINED.ordinal() + 1;
		String [] paths = new String[lettersCount];
		for(int i = 0; i < lettersCount; i++) {
			String datasetDir = type.toString().toLowerCase();
			String letterDir = Letter.values()[i].toString().toLowerCase();
			paths[i] = new String("dataset/" + datasetDir + '/' + letterDir);
			System.out.println(paths[i]);
		}
		
		File [] dirs = new File[lettersCount];
		File [][] files = new File[lettersCount][];
		for(int i = 0; i < paths.length; i++) {
			dirs[i] = new File(paths[i]);
			files[i] = dirs[i].listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".png");
				}
			});
		}
		
		int datasetSize = 0;
		for(int i = 0; i < lettersCount; i++) {
			datasetSize += files[i].length;
		}
		
		Dataset [] data = new Dataset[datasetSize];
		int index = 0;
		for(int i = 0; i < lettersCount; i++) {
			Letter letter = Letter.values()[i];
			for(int j = 0; j < files[i].length; j++) {
				data[index] = loadImage(files[i][j], letter);
				index++;
			}
		}
		return data;
	}

	protected void saveImage() {
		File defaultDir = new File(System.getProperty("user.dir") + System.getProperty("file.separator")+ "dataset");
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(defaultDir);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int result = fileChooser.showSaveDialog(this);
		if (result != JFileChooser.APPROVE_OPTION)
			return;
		File saveFile = fileChooser.getSelectedFile();
		if (!saveFile.getAbsolutePath().toLowerCase().endsWith(".png"))
			saveFile = new File(saveFile.getAbsolutePath() + ".png");
		BufferedImage image = paintPanel.getScaledBufferedImage(cols, rows);
		try {
			ImageIO.write(image, "png", saveFile);
		} catch (IOException exception) {
			return;
		}
	}

	protected Letter recognizeLetter(double [] inputs) {
		Letter letter = Letter.UNDEFINED;
		for(int i = 0; i < Letter.UNDEFINED.ordinal(); i++) {
			if(inputs[i] > treshold) {
				letter = Letter.values()[i];
			}
		}
		return letter;
	}

	public static void main(String [] args) {
		Debugger.setEnabled(true);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new App("BackPropagation");
			}
		});
	}
}

package uCurrent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class MainClass extends ApplicationFrame implements SerialPortEventListener {

    /**
     * The number of subplots.
     */
    public static final int SUBPLOT_COUNT = 3;

    /**
     * The datasets.
     */
    private TimeSeriesCollection[] datasets;
    
    /**
     * The most recent value added to series 1.
     */
    private double[] lastValue = new double[SUBPLOT_COUNT];
    
    public static ArrayList<CurrentValue> list = new ArrayList();
    public static ArrayList<DomainMarker> domains = new ArrayList();
    
    Map<String, Integer> serialDomains = new HashMap<String, Integer>();
    
    SerialPort serialPort;
    /**
     * The port we're normally going to use.
     */
    private static final String PORT_NAMES[] = {
        "/dev/tty.usbserial-A9007UX1", // Mac OS X
        "/dev/ttyACM0",//rpi
        "/dev/ttyACM1",//rpi
        "/dev/ttyUSB0", // Linux
        "COM3", // Windows
    };
    /**
     * A BufferedReader which will be fed by a InputStreamReader converting the
     * bytes into characters making the displayed results codepage independent
     */
    private BufferedReader input;
    /**
     * The output stream to the port
     */
    private OutputStream output;
    /**
     * Milliseconds to block while waiting for port open
     */
    private static final int TIME_OUT = 2000;
    /**
     * Default bits per second for COM port.
     */
    private static final int DATA_RATE = 115200;
    
    public boolean domain = false;
    
    public Millisecond m;
    
    public XYPlot subplot;
    
    public DomainMarker manual;
    
    public ChartPanel chartPanel;
    
    public JPanel p, panelGeral;
    public JLabel label;    
    
    public int lastIndex,lastMedia,lastPico, count = 0;

    public MainClass(String title) {
        super(title);
        //initialize();

        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new DateAxis("Tempo"));
        this.datasets = new TimeSeriesCollection[SUBPLOT_COUNT];

        this.lastValue[0] = 100.0;
        final TimeSeries series = new TimeSeries("Current ", Millisecond.class);
        this.datasets[0] = new TimeSeriesCollection(series);
        final NumberAxis rangeAxis = new NumberAxis("Corrente (mA)");
        rangeAxis.setAutoRangeIncludesZero(false);
        rangeAxis.setRange(0, 800);
        subplot = new XYPlot(
                this.datasets[0], null, rangeAxis, new StandardXYItemRenderer()
        );
        subplot.setBackgroundPaint(Color.lightGray);
        subplot.setDomainGridlinePaint(Color.white);
        subplot.setRangeGridlinePaint(Color.white);
        plot.add(subplot);

        final JFreeChart chart = new JFreeChart("Gráfico de Consumo de Energia", plot);
        chart.setBorderPaint(Color.black);
        chart.setBorderVisible(true);
        chart.setBackgroundPaint(Color.white);

        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        final ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(120000.0);  // 60 seconds
        

        final JPanel content = new JPanel(new BorderLayout());

        chartPanel = new ChartPanel(chart);
        content.add(chartPanel);

        final JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setLayout(null);
        //buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
                
        buttonPanel.setPreferredSize(new java.awt.Dimension(1100, 200));
        
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        
        JMenu menu = new JMenu("Opções");                         
        
        final JFileChooser fc = new JFileChooser();
        
        JMenuItem flush = new JMenuItem( new AbstractAction("Flush data") {
            public void actionPerformed(ActionEvent ae) {
            	flush();
            }
        });
        
        JMenuItem openfile = new JMenuItem( new AbstractAction("Abrir arquivo...") {
            public void actionPerformed(ActionEvent ae) {
            	int returnVal = fc.showOpenDialog(content);
				
            	if (returnVal == JFileChooser.APPROVE_OPTION) {                    
                    //This is where a real application would open the file.
                    try {
                    	flush();
						abreArquivo(fc.getSelectedFile().getAbsolutePath());
						
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}                    
                } else {
                	System.out.println("Open command cancelled by user.");
                }           
            }
        });
        
        JMenuItem exportfile = new JMenuItem( new AbstractAction("Exportar arquivo (calc, excel)...") {
            public void actionPerformed(ActionEvent ae) {
            	int returnVal = fc.showSaveDialog(content);
            	if (returnVal == JFileChooser.APPROVE_OPTION) {
                    //This is where a real application would open the file.            		
            		try {            			
						exportaArquivo(fc.getSelectedFile().getAbsolutePath());						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}                    
                } else {
                	System.out.println("Save command cancelled by user.");
                }   
            }
        });
        
        JMenuItem savefile = new JMenuItem( new AbstractAction("Salvar como...") {
            public void actionPerformed(ActionEvent ae) {
            	int returnVal = fc.showSaveDialog(content);
            	if (returnVal == JFileChooser.APPROVE_OPTION) {
                    //This is where a real application would open the file.            		
            		try {            			
						salvaArquivo(fc.getSelectedFile().getAbsolutePath());						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}                    
                } else {
                	System.out.println("Save command cancelled by user.");
                }   
            }
        });
        
        menu.add(openfile);
        menu.add(savefile);
        menu.add(exportfile);
        menu.add(flush);        
                
        menubar.add(menu);
        
        
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        chartPanel.setPreferredSize(new java.awt.Dimension(1100, 600));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));        
                       
        JButton start = new JButton("Start");
        start.setBounds(5, 0, 90, 30);        
        buttonPanel.add(start);
        
                
        start.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				initialize();				
			}
		});
        
        JButton stop = new JButton("Stop");
        stop.setBounds(100, 0, 90, 30);     
        buttonPanel.add(stop);
        
        stop.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				close();	
				ajustaLabel();
			}
		});
        
        final JScrollPane sp = new JScrollPane();
        sp.setBounds(5, 40, 1095, 150);             
        
        final JTextField tfDomain = new JTextField();
        tfDomain.setBounds(740, 0, 225, 30);
        buttonPanel.add(tfDomain);
        
        //final JPanel p = new JPanel();
        p = new JPanel();
        BoxLayout bl = new BoxLayout(p, BoxLayout.Y_AXIS);
        p.setLayout(bl);
        label = new JLabel(textoLabel(0,0));
        panelGeral = new JPanel();
    	panelGeral.setLayout(new FlowLayout(FlowLayout.LEFT));
    	JLabel inicio = new JLabel("Dados Gerais da Medição - ");
    	inicio.setFont(new Font(inicio.getFont().getName(), Font.BOLD, inicio.getFont().getSize()));    	
    	panelGeral.add(inicio);
    	panelGeral.add(label);    
    	p.add(panelGeral);
    	sp.setViewportView(p);
        
        JButton db = new JButton("Novo Marcador");
        db.setBounds(975, 0, 125, 30);     
        buttonPanel.add(db);
        
        db.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(domain){
					domain = false;			
					IntervalMarker im = buildMarker(tfDomain.getText(), m.getFirstMillisecond(), new Millisecond().getFirstMillisecond());
					subplot.addDomainMarker(im);      
                    manual.fim = list.size();
                    domains.add(manual);
                    p.add(panelDomain(manual, im));
                    p.revalidate();     
                    
				}else{
					domain = true;
					m = new Millisecond();
					manual = new DomainMarker(tfDomain.getText(), list.size(), list.size());							
					
				}
			}
		});  
        
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        buttonPanel.add(sp);                
        
        setContentPane(content);
    }

    public void initialize() {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
        
        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            for (String portName : PORT_NAMES) {
                if (currPortId.getName().equals(portName)) {
                    portId = currPortId;
                    break;
                }
            }
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));           
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println("Excepción initilize=" + e.toString());
        }
    }

    /**
     * This should be called when you stop using the port. This will prevent
     * port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                String inputLine = input.readLine();                
                if(inputLine.charAt(0) == 'D'){                	
                	inputLine = inputLine.substring(1);                	
                	if(serialDomains.containsKey(inputLine)){                		
                		int inicio = serialDomains.get(inputLine).intValue() - 1;
                		int fim = list.size() - 1;
                		DomainMarker dm = new DomainMarker(inputLine, inicio, fim ); 
                		domains.add(dm);
                		serialDomains.remove(inputLine);
                		
                		IntervalMarker im = buildMarker(inputLine, list.get(inicio).time.getFirstMillisecond(),
                												   list.get(fim).time.getFirstMillisecond());
    					subplot.addDomainMarker(im);                                                      
                        p.add(panelDomain(dm, im));
                        p.revalidate();
                	}else{                		
                		serialDomains.put(inputLine, list.size());
                	}
                }else{
                	inputLine = inputLine.substring(1);
                	CurrentValue v = new CurrentValue(new Millisecond(), new Integer(inputLine).intValue());
                    list.add(v);
                    this.datasets[0].getSeries(0).add(v.time, v.value);
                    if(count > 100){
                    	count = 0;
                    	ajustaLabel();
                    }else{
                    	count++;
                    }
                }
                
                //System.out.println(inputLine);
            } catch (Exception e) {
                System.err.println("Excepción serialEvent=" + e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }
    
    public IntervalMarker buildMarker(String label, long i, long f){
    	IntervalMarker im = new IntervalMarker(i, f);
		im.setPaint(new Color(0xDD, 0xFF, 0xDD, 0x80));
        im.setAlpha(0.5f);
        im.setLabel(label);
        im.setLabelFont(new Font("SansSerif", 0, 11));
		im.setLabelPaint(Color.blue);
		im.setLabelAnchor(RectangleAnchor.TOP_LEFT);
		im.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
		return im;
    }
    
    public void exportaArquivo(String f) throws IOException{
    	File a = new File(f);
    	/*if(a.exists()){
    		if(!(a.delete() && a.createNewFile())){
    			JOptionPane.showMessageDialog(null, "Erro ao salvar o arquivo, talvez esteja em uso");
    			return;
    		}
    	}*/   	
    	PrintWriter w = new PrintWriter(f);
    	long val;
    	float aux;
    	DomainMarker m;
    	for(int i = 0; i<list.size(); i++){
    		val = list.get(i).time.getFirstMillisecond() - list.get(0).time.getFirstMillisecond();
    		aux = (float)val/1000;
    		w.print("" +  aux + ", "+ list.get(i).value);
    		if(i==0 && domains.size() > 0){
    			w.print(",,ids dos marcadores,média,pico,duração(s), inicio, fim");
    		}else if(i>0 && i<= domains.size()){
    			m = domains.get(i-1);
    			w.print(",,"+ m.caption+","+mediaMarker(m)+","+picoMarker(m)+","+(float)tempoMarker(m)/1000+","+(m.inicio+1)+"," + m.fim);
    		}
    		w.println();
    	}
    	w.close();
    }
    
    public void salvaArquivo(String f) throws IOException{
    	FileOutputStream fout = new FileOutputStream(f);
		ObjectOutputStream oos = new ObjectOutputStream(fout);		
		FileSavedObject fso = new FileSavedObject(domains.size(), list.size());
		oos.writeObject(fso);				
		
    	for(int i = 0; i< list.size(); i++){
    		oos.writeObject(list.get(i));
    	}
    	
    	for(int i = 0; i<domains.size(); i++){    		
    		oos.writeObject(domains.get(i));
    	}
    	
    	System.out.println("arquivo salvo - " + fso.toString());
		oos.close();		
		fout.close();		
    }
    
    public void abreArquivo(String f) throws IOException, ClassNotFoundException{    	
    	FileInputStream fin = new FileInputStream(f);		
		ObjectInputStream ois = new ObjectInputStream(fin);
		
		FileSavedObject fso = (FileSavedObject) ois.readObject();
		DomainMarker dm;
		CurrentValue cv;
		IntervalMarker im;		
				
		for(int i=0; i<fso.nv; i++){
			cv = (CurrentValue)ois.readObject();
			list.add(cv);
			this.datasets[0].getSeries(0).add(cv.time, cv.value);
		}
		
		for(int i = 0; i<fso.nd; i++){
			dm = (DomainMarker)ois.readObject();
			domains.add(dm);
			im = buildMarker(dm.caption,
					list.get(dm.inicio).time.getFirstMillisecond(),
					list.get(dm.fim).time.getFirstMillisecond());
			subplot.addDomainMarker(im);
			p.add(panelDomain(dm, im));
			ajustaLabel();
			p.revalidate();
		}
		
		//System.out.println("arquivo lido - " + fso.toString());
		ois.close();
		fin.close();
		
    }
    
    public void ajustaLabel(){
    	mediaTotal();
    	picoTotal();
    	label.setText(textoLabel(lastMedia, lastPico));
    	p.revalidate();
    }
    
    public void mediaTotal(){
    	int val = 0;
    	for(int i= lastIndex; i < list.size(); i++){    		
    		val += list.get(i).value;
    	}
    	lastMedia =  (lastMedia*lastIndex + val)/list.size();    	
    }
        
    public void picoTotal(){    	
    	for(int i = lastIndex; i<list.size(); i++){
    		if(lastPico < list.get(i).value){
    			lastPico = list.get(i).value;
    		}
    	}    	
    }
    
    public String textoLabel(int media, int pico){
    	
    	return "Consumo médio: "+ media+"mA. Pico de consumo: "+ pico+ "mA.";
    }
    
    public int mediaMarker(DomainMarker d){
    	long val = 0;
    	for(int i= d.inicio; i < d.fim; i++){
    		val += list.get(i).value;
    	}    	
    	return Math.round((float)val/(d.fim - d.inicio));
    }
    
    public int picoMarker(DomainMarker d){
    	int val = list.get(d.inicio).value;    
    	for(int i= d.inicio; i < d.fim; i++){
    		if(val < list.get(i).value){
    			val = list.get(i).value;
    		}
    	}
    	return val;
    }     
    
    public long tempoMarker(DomainMarker d){   	
    	
    	return list.get(d.fim-1).time.getFirstMillisecond() - list.get(d.inicio).time.getFirstMillisecond();
    }

    public JPanel panelDomain(final DomainMarker d, final IntervalMarker im){
    	final JPanel panel = new JPanel();    	
    	panel.setLayout(new FlowLayout(FlowLayout.LEFT));    	    
    	JLabel inicio = new JLabel("Marker id: "+d.caption);
    	inicio.setFont(new Font(inicio.getFont().getName(), Font.BOLD, inicio.getFont().getSize()));
    	panel.add(inicio);
    	panel.add(new JLabel(", consumo médio: "+ mediaMarker(d) + "mA, pico de consumo: "+ picoMarker(d) + "mA, duração: "+((float)tempoMarker(d)/1000) + " segundos"));    
    	JButton b = new JButton("Remover");
    	b.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				for(int i = 0; i<domains.size(); i++){
		    		if(domains.get(i).equals(d)){
		    			domains.remove(i);
		    			break;
		    		}
		    	}
				subplot.removeDomainMarker(im);
				panel.getParent().remove(panel);				
				p.revalidate();
			}
		});
    	panel.add(b);      	
    	return panel;
    }
    
    public void flush(){
    	list.clear();
    	lastMedia = 0;
    	lastPico = 0;
    	lastIndex = 0;
    	datasets[0].getSeries(0).clear();
    	subplot.clearDomainMarkers();
    	label.setText(textoLabel(lastMedia, lastPico));
    	for(int i = p.getComponentCount()-1; i>=0; i--){    		
    		if(!p.getComponent(i).equals(panelGeral)){
    			p.remove(i);
    		}
    	}
    	p.revalidate();
    	chartPanel.revalidate();
    }
    
    public static void main(String[] args) throws Exception {
    	
    	list.clear();
    	domains.clear();
    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());    	
    	    	
    	final MainClass demo = new MainClass("Gráfico de Consumo");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        
        demo.setVisible(true);
    }
}
package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import bd.DBDriver;
import controlador.InterfazSRFL;
import servicios.FacturaElectronicaImplServiceStub_old.Documento;
import servicios.FacturaElectronicaImplServiceStub_old.Factura;
import servicios.FacturaElectronicaImplServiceStub_old.Persona;

public class Receptor extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField direccion;
	private JTextField documento;
	private JTextField nombre;
	private JComboBox<String> documentoTipo;
	private JComboBox<String> documentoPais;
	private JComboBox<String> pais;
	private Persona persona = new Persona();
	public static InterfazSRFL interfaz;
	public static Factura factura;
	private JTextField txtDepartamento;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//Receptor frame = new Receptor(null,"",null);
					//frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	//select 'paises.put("'||codigo||'","'||upper(nombre)||'");' from pais
	public static HashMap<String, String> getHashPaises(){
		HashMap<String, String> paises = new HashMap<String, String>();
		paises.put("AF","AFGANISTÃN");
		paises.put("AX","ISLAS GLAND");
		paises.put("AL","ALBANIA");
		paises.put("DE","ALEMANIA");
		paises.put("AD","ANDORRA");
		paises.put("AO","ANGOLA");
		paises.put("AI","ANGUILLA");
		paises.put("AQ","ANTÃ�RTIDA");
		paises.put("AG","ANTIGUA Y BARBUDA");
		paises.put("AN","ANTILLAS HOLANDESAS");
		paises.put("SA","ARABIA SAUDÃ�");
		paises.put("DZ","ARGELIA");
		paises.put("AR","ARGENTINA");
		paises.put("AM","ARMENIA");
		paises.put("AW","ARUBA");
		paises.put("AU","AUSTRALIA");
		paises.put("AT","AUSTRIA");
		paises.put("AZ","AZERBAIYÃ�N");
		paises.put("BS","BAHAMAS");
		paises.put("BH","BAHRÃ‰IN");
		paises.put("BD","BANGLADESH");
		paises.put("BB","BARBADOS");
		paises.put("BY","BIELORRUSIA");
		paises.put("BE","BÃ‰LGICA");
		paises.put("BZ","BELICE");
		paises.put("BJ","BENIN");
		paises.put("BM","BERMUDAS");
		paises.put("BT","BHUTÃ�N");
		paises.put("BO","BOLIVIA");
		paises.put("BA","BOSNIA Y HERZEGOVINA");
		paises.put("BW","BOTSUANA");
		paises.put("BV","ISLA BOUVET");
		paises.put("BR","BRASIL");
		paises.put("BN","BRUNÃ‰I");
		paises.put("BG","BULGARIA");
		paises.put("BF","BURKINA FASO");
		paises.put("BI","BURUNDI");
		paises.put("CV","CABO VERDE");
		paises.put("KY","ISLAS CAIMÃ�N");
		paises.put("KH","CAMBOYA");
		paises.put("CM","CAMERÃšN");
		paises.put("CA","CANADÃ�");
		paises.put("CF","REPÃšBLICA CENTROAFRICANA");
		paises.put("TD","CHAD");
		paises.put("CZ","REPÃšBLICA CHECA");
		paises.put("CL","CHILE");
		paises.put("CN","CHINA");
		paises.put("CY","CHIPRE");
		paises.put("CX","ISLA DE NAVIDAD");
		paises.put("VA","CIUDAD DEL VATICANO");
		paises.put("CC","ISLAS COCOS");
		paises.put("CO","COLOMBIA");
		paises.put("KM","COMORAS");
		paises.put("CD","REPÃšBLICA DEMOCRÃ�TICA DEL CONGO");
		paises.put("CG","CONGO");
		paises.put("CK","ISLAS COOK");
		paises.put("KP","COREA DEL NORTE");
		paises.put("KR","COREA DEL SUR");
		paises.put("CI","COSTA DE MARFIL");
		paises.put("CR","COSTA RICA");
		paises.put("HR","CROACIA");
		paises.put("CU","CUBA");
		paises.put("DK","DINAMARCA");
		paises.put("DM","DOMINICA");
		paises.put("DO","REPÃšBLICA DOMINICANA");
		paises.put("EC","ECUADOR");
		paises.put("EG","EGIPTO");
		paises.put("SV","EL SALVADOR");
		paises.put("AE","EMIRATOS Ã�RABES UNIDOS");
		paises.put("ER","ERITREA");
		paises.put("SK","ESLOVAQUIA");
		paises.put("SI","ESLOVENIA");
		paises.put("ES","ESPAÃ‘A");
		paises.put("UM","ISLAS ULTRAMARINAS DE ESTADOS UNIDOS");
		paises.put("US","ESTADOS UNIDOS");
		paises.put("EE","ESTONIA");
		paises.put("ET","ETIOPÃ�A");
		paises.put("FO","ISLAS FEROE");
		paises.put("PH","FILIPINAS");
		paises.put("FI","FINLANDIA");
		paises.put("FJ","FIYI");
		paises.put("FR","FRANCIA");
		paises.put("GA","GABÃ“N");
		paises.put("GM","GAMBIA");
		paises.put("GE","GEORGIA");
		paises.put("GS","ISLAS GEORGIAS DEL SUR Y SANDWICH DEL SUR");
		paises.put("GH","GHANA");
		paises.put("GI","GIBRALTAR");
		paises.put("GD","GRANADA");
		paises.put("GR","GRECIA");
		paises.put("GL","GROENLANDIA");
		paises.put("GP","GUADALUPE");
		paises.put("GU","GUAM");
		paises.put("GT","GUATEMALA");
		paises.put("GF","GUAYANA FRANCESA");
		paises.put("GN","GUINEA");
		paises.put("GQ","GUINEA ECUATORIAL");
		paises.put("GW","GUINEA-BISSAU");
		paises.put("GY","GUYANA");
		paises.put("HT","HAITÃ�");
		paises.put("HM","ISLAS HEARD Y MCDONALD");
		paises.put("HN","HONDURAS");
		paises.put("HK","HONG KONG");
		paises.put("HU","HUNGRÃ�A");
		paises.put("IN","INDIA");
		paises.put("ID","INDONESIA");
		paises.put("IR","IRÃ�N");
		paises.put("IQ","IRAQ");
		paises.put("IE","IRLANDA");
		paises.put("IS","ISLANDIA");
		paises.put("IL","ISRAEL");
		paises.put("IT","ITALIA");
		paises.put("JM","JAMAICA");
		paises.put("JP","JAPÃ“N");
		paises.put("JO","JORDANIA");
		paises.put("KZ","KAZAJSTÃ�N");
		paises.put("KE","KENIA");
		paises.put("KG","KIRGUISTÃ�N");
		paises.put("KI","KIRIBATI");
		paises.put("KW","KUWAIT");
		paises.put("LA","LAOS");
		paises.put("LS","LESOTHO");
		paises.put("LV","LETONIA");
		paises.put("LB","LÃ�BANO");
		paises.put("LR","LIBERIA");
		paises.put("LY","LIBIA");
		paises.put("LI","LIECHTENSTEIN");
		paises.put("LT","LITUANIA");
		paises.put("LU","LUXEMBURGO");
		paises.put("MO","MACAO");
		paises.put("MK","ARY MACEDONIA");
		paises.put("MG","MADAGASCAR");
		paises.put("MY","MALASIA");
		paises.put("MW","MALAWI");
		paises.put("MV","MALDIVAS");
		paises.put("ML","MALÃ�");
		paises.put("MT","MALTA");
		paises.put("FK","ISLAS MALVINAS");
		paises.put("MP","ISLAS MARIANAS DEL NORTE");
		paises.put("MA","MARRUECOS");
		paises.put("MH","ISLAS MARSHALL");
		paises.put("MQ","MARTINICA");
		paises.put("MU","MAURICIO");
		paises.put("MR","MAURITANIA");
		paises.put("YT","MAYOTTE");
		paises.put("MX","MÃ‰XICO");
		paises.put("FM","MICRONESIA");
		paises.put("MD","MOLDAVIA");
		paises.put("MC","MÃ“NACO");
		paises.put("MN","MONGOLIA");
		paises.put("MS","MONTSERRAT");
		paises.put("MZ","MOZAMBIQUE");
		paises.put("MM","MYANMAR");
		paises.put("NA","NAMIBIA");
		paises.put("NR","NAURU");
		paises.put("NP","NEPAL");
		paises.put("NI","NICARAGUA");
		paises.put("NE","NÃ�GER");
		paises.put("NG","NIGERIA");
		paises.put("NU","NIUE");
		paises.put("NF","ISLA NORFOLK");
		paises.put("NO","NORUEGA");
		paises.put("NC","NUEVA CALEDONIA");
		paises.put("NZ","NUEVA ZELANDA");
		paises.put("OM","OMÃ�N");
		paises.put("NL","PAÃ�SES BAJOS");
		paises.put("PK","PAKISTÃ�N");
		paises.put("PW","PALAU");
		paises.put("PS","PALESTINA");
		paises.put("PA","PANAMÃ�");
		paises.put("PG","PAPÃšA NUEVA GUINEA");
		paises.put("PY","PARAGUAY");
		paises.put("PE","PERÃš");
		paises.put("PN","ISLAS PITCAIRN");
		paises.put("PF","POLINESIA FRANCESA");
		paises.put("PL","POLONIA");
		paises.put("PT","PORTUGAL");
		paises.put("PR","PUERTO RICO");
		paises.put("QA","QATAR");
		paises.put("GB","REINO UNIDO");
		paises.put("RE","REUNIÃ“N");
		paises.put("RW","RUANDA");
		paises.put("RO","RUMANIA");
		paises.put("RU","RUSIA");
		paises.put("EH","SAHARA OCCIDENTAL");
		paises.put("SB","ISLAS SALOMÃ“N");
		paises.put("WS","SAMOA");
		paises.put("AS","SAMOA AMERICANA");
		paises.put("KN","SAN CRISTÃ“BAL Y NEVIS");
		paises.put("SM","SAN MARINO");
		paises.put("PM","SAN PEDRO Y MIQUELÃ“N");
		paises.put("VC","SAN VICENTE Y LAS GRANADINAS");
		paises.put("SH","SANTA HELENA");
		paises.put("LC","SANTA LUCÃ�A");
		paises.put("ST","SANTO TOMÃ‰ Y PRÃ�NCIPE");
		paises.put("SN","SENEGAL");
		paises.put("CS","SERBIA Y MONTENEGRO");
		paises.put("SC","SEYCHELLES");
		paises.put("SL","SIERRA LEONA");
		paises.put("SG","SINGAPUR");
		paises.put("SY","SIRIA");
		paises.put("SO","SOMALIA");
		paises.put("LK","SRI LANKA");
		paises.put("SZ","SUAZILANDIA");
		paises.put("ZA","SUDÃ�FRICA");
		paises.put("SD","SUDÃ�N");
		paises.put("SE","SUECIA");
		paises.put("CH","SUIZA");
		paises.put("SR","SURINAM");
		paises.put("SJ","SVALBARD Y JAN MAYEN");
		paises.put("TH","TAILANDIA");
		paises.put("TW","TAIWÃ�N");
		paises.put("TZ","TANZANIA");
		paises.put("TJ","TAYIKISTÃ�N");
		paises.put("IO","TERRITORIO BRITÃ�NICO DEL OCÃ‰ANO Ã�NDICO");
		paises.put("TF","TERRITORIOS AUSTRALES FRANCESES");
		paises.put("TL","TIMOR ORIENTAL");
		paises.put("TG","TOGO");
		paises.put("TK","TOKELAU");
		paises.put("TO","TONGA");
		paises.put("TT","TRINIDAD Y TOBAGO");
		paises.put("TN","TÃšNEZ");
		paises.put("TC","ISLAS TURCAS Y CAICOS");
		paises.put("TM","TURKMENISTÃ�N");
		paises.put("TR","TURQUÃ�A");
		paises.put("TV","TUVALU");
		paises.put("UA","UCRANIA");
		paises.put("UG","UGANDA");
		paises.put("UY","URUGUAY");
		paises.put("UZ","UZBEKISTÃ�N");
		paises.put("VU","VANUATU");
		paises.put("VE","VENEZUELA");
		paises.put("VN","VIETNAM");
		paises.put("VG","ISLAS VÃ�RGENES BRITÃ�NICAS");
		paises.put("VI","ISLAS VÃ�RGENES DE LOS ESTADOS UNIDOS");
		paises.put("WF","WALLIS Y FUTUNA");
		paises.put("YE","YEMEN");
		paises.put("DJ","YIBUTI");
		paises.put("ZM","ZAMBIA");
		paises.put("ZW","ZIMBABUE");

		return paises;
	}
	public static HashMap<String, String> getHashPaisesInverso(){
		HashMap<String, String> salida = new HashMap<String, String>();
		HashMap<String, String> paises = getHashPaises();
		for(String k:paises.keySet()){
			salida.put(paises.get(k), k);
		}
		return salida;
	}
	//select *,'paises.put("'||codigolargo||'","'||nombre||'");' from pais
	public static ArrayList<String> getPaises(){
		ArrayList<String> paises = new ArrayList<String>();
		paises.add("AfganistÃ¡n");
		paises.add("Islas Gland");
		paises.add("Albania");
		paises.add("Alemania");
		paises.add("Andorra");
		paises.add("Angola");
		paises.add("Anguilla");
		paises.add("AntÃ¡rtida");
		paises.add("Antigua y Barbuda");
		paises.add("Antillas Holandesas");
		paises.add("Arabia SaudÃ­");
		paises.add("Argelia");
		paises.add("Argentina");
		paises.add("Armenia");
		paises.add("Aruba");
		paises.add("Australia");
		paises.add("Austria");
		paises.add("AzerbaiyÃ¡n");
		paises.add("Bahamas");
		paises.add("BahrÃ©in");
		paises.add("Bangladesh");
		paises.add("Barbados");
		paises.add("Bielorrusia");
		paises.add("BÃ©lgica");
		paises.add("Belice");
		paises.add("Benin");
		paises.add("Bermudas");
		paises.add("BhutÃ¡n");
		paises.add("Bolivia");
		paises.add("Bosnia y Herzegovina");
		paises.add("Botsuana");
		paises.add("Isla Bouvet");
		paises.add("Brasil");
		paises.add("BrunÃ©i");
		paises.add("Bulgaria");
		paises.add("Burkina Faso");
		paises.add("Burundi");
		paises.add("Cabo Verde");
		paises.add("Islas CaimÃ¡n");
		paises.add("Camboya");
		paises.add("CamerÃºn");
		paises.add("CanadÃ¡");
		paises.add("RepÃºblica Centroafricana");
		paises.add("Chad");
		paises.add("RepÃºblica Checa");
		paises.add("Chile");
		paises.add("China");
		paises.add("Chipre");
		paises.add("Isla de Navidad");
		paises.add("Ciudad del Vaticano");
		paises.add("Islas Cocos");
		paises.add("Colombia");
		paises.add("Comoras");
		paises.add("RepÃºblica DemocrÃ¡tica del Congo");
		paises.add("Congo");
		paises.add("Islas Cook");
		paises.add("Corea del Norte");
		paises.add("Corea del Sur");
		paises.add("Costa de Marfil");
		paises.add("Costa Rica");
		paises.add("Croacia");
		paises.add("Cuba");
		paises.add("Dinamarca");
		paises.add("Dominica");
		paises.add("RepÃºblica Dominicana");
		paises.add("Ecuador");
		paises.add("Egipto");
		paises.add("El Salvador");
		paises.add("Emiratos Ã�rabes Unidos");
		paises.add("Eritrea");
		paises.add("Eslovaquia");
		paises.add("Eslovenia");
		paises.add("EspaÃ±a");
		paises.add("Islas ultramarinas de Estados Unidos");
		paises.add("Estados Unidos");
		paises.add("Estonia");
		paises.add("EtiopÃ­a");
		paises.add("Islas Feroe");
		paises.add("Filipinas");
		paises.add("Finlandia");
		paises.add("Fiyi");
		paises.add("Francia");
		paises.add("GabÃ³n");
		paises.add("Gambia");
		paises.add("Georgia");
		paises.add("Islas Georgias del Sur y Sandwich del Sur");
		paises.add("Ghana");
		paises.add("Gibraltar");
		paises.add("Granada");
		paises.add("Grecia");
		paises.add("Groenlandia");
		paises.add("Guadalupe");
		paises.add("Guam");
		paises.add("Guatemala");
		paises.add("Guayana Francesa");
		paises.add("Guinea");
		paises.add("Guinea Ecuatorial");
		paises.add("Guinea-Bissau");
		paises.add("Guyana");
		paises.add("HaitÃ­");
		paises.add("Islas Heard y McDonald");
		paises.add("Honduras");
		paises.add("Hong Kong");
		paises.add("HungrÃ­a");
		paises.add("India");
		paises.add("Indonesia");
		paises.add("IrÃ¡n");
		paises.add("Iraq");
		paises.add("Irlanda");
		paises.add("Islandia");
		paises.add("Israel");
		paises.add("Italia");
		paises.add("Jamaica");
		paises.add("JapÃ³n");
		paises.add("Jordania");
		paises.add("KazajstÃ¡n");
		paises.add("Kenia");
		paises.add("KirguistÃ¡n");
		paises.add("Kiribati");
		paises.add("Kuwait");
		paises.add("Laos");
		paises.add("Lesotho");
		paises.add("Letonia");
		paises.add("LÃ­bano");
		paises.add("Liberia");
		paises.add("Libia");
		paises.add("Liechtenstein");
		paises.add("Lituania");
		paises.add("Luxemburgo");
		paises.add("Macao");
		paises.add("ARY Macedonia");
		paises.add("Madagascar");
		paises.add("Malasia");
		paises.add("Malawi");
		paises.add("Maldivas");
		paises.add("MalÃ­");
		paises.add("Malta");
		paises.add("Islas Malvinas");
		paises.add("Islas Marianas del Norte");
		paises.add("Marruecos");
		paises.add("Islas Marshall");
		paises.add("Martinica");
		paises.add("Mauricio");
		paises.add("Mauritania");
		paises.add("Mayotte");
		paises.add("MÃ©xico");
		paises.add("Micronesia");
		paises.add("Moldavia");
		paises.add("MÃ³naco");
		paises.add("Mongolia");
		paises.add("Montserrat");
		paises.add("Mozambique");
		paises.add("Myanmar");
		paises.add("Namibia");
		paises.add("Nauru");
		paises.add("Nepal");
		paises.add("Nicaragua");
		paises.add("NÃ­ger");
		paises.add("Nigeria");
		paises.add("Niue");
		paises.add("Isla Norfolk");
		paises.add("Noruega");
		paises.add("Nueva Caledonia");
		paises.add("Nueva Zelanda");
		paises.add("OmÃ¡n");
		paises.add("PaÃ­ses Bajos");
		paises.add("PakistÃ¡n");
		paises.add("Palau");
		paises.add("Palestina");
		paises.add("PanamÃ¡");
		paises.add("PapÃºa Nueva Guinea");
		paises.add("Paraguay");
		paises.add("PerÃº");
		paises.add("Islas Pitcairn");
		paises.add("Polinesia Francesa");
		paises.add("Polonia");
		paises.add("Portugal");
		paises.add("Puerto Rico");
		paises.add("Qatar");
		paises.add("Reino Unido");
		paises.add("ReuniÃ³n");
		paises.add("Ruanda");
		paises.add("Rumania");
		paises.add("Rusia");
		paises.add("Sahara Occidental");
		paises.add("Islas SalomÃ³n");
		paises.add("Samoa");
		paises.add("Samoa Americana");
		paises.add("San CristÃ³bal y Nevis");
		paises.add("San Marino");
		paises.add("San Pedro y MiquelÃ³n");
		paises.add("San Vicente y las Granadinas");
		paises.add("Santa Helena");
		paises.add("Santa LucÃ­a");
		paises.add("Santo TomÃ© y PrÃ­ncipe");
		paises.add("Senegal");
		paises.add("Serbia y Montenegro");
		paises.add("Seychelles");
		paises.add("Sierra Leona");
		paises.add("Singapur");
		paises.add("Siria");
		paises.add("Somalia");
		paises.add("Sri Lanka");
		paises.add("Suazilandia");
		paises.add("SudÃ¡frica");
		paises.add("SudÃ¡n");
		paises.add("Suecia");
		paises.add("Suiza");
		paises.add("Surinam");
		paises.add("Svalbard y Jan Mayen");
		paises.add("Tailandia");
		paises.add("TaiwÃ¡n");
		paises.add("Tanzania");
		paises.add("TayikistÃ¡n");
		paises.add("Territorio BritÃ¡nico del OcÃ©ano Ã�ndico");
		paises.add("Territorios Australes Franceses");
		paises.add("Timor Oriental");
		paises.add("Togo");
		paises.add("Tokelau");
		paises.add("Tonga");
		paises.add("Trinidad y Tobago");
		paises.add("TÃºnez");
		paises.add("Islas Turcas y Caicos");
		paises.add("TurkmenistÃ¡n");
		paises.add("TurquÃ­a");
		paises.add("Tuvalu");
		paises.add("Ucrania");
		paises.add("Uganda");
		paises.add("Uruguay");
		paises.add("UzbekistÃ¡n");
		paises.add("Vanuatu");
		paises.add("Venezuela");
		paises.add("Vietnam");
		paises.add("Islas VÃ­rgenes BritÃ¡nicas");
		paises.add("Islas VÃ­rgenes de los Estados Unidos");
		paises.add("Wallis y Futuna");
		paises.add("Yemen");
		paises.add("Yibuti");
		paises.add("Zambia");
		paises.add("Zimbabue");

		return paises;
	}
	/**
	 * Create the frame.
	 */
	public Persona Receptor(Factura factura,String error, InterfazSRFL interfaz) {
		//super((Window)null);
		this.setVisible(true);
		Receptor.interfaz = interfaz;
		//Receptor.interfaz.setCancelado(false);
		setModal(true);
		Receptor.factura = factura;
		if(Receptor.factura.getReceptor()==null){
			Receptor.factura.setReceptor(new Persona());
		}
		if(Receptor.factura.getReceptor().getDocumento()==null){
			Receptor.factura.getReceptor().setDocumento(new Documento());
		}
		persona = factura.getReceptor();
		
		
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 776, 243);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblDepartamento = new JLabel("Departamento");
		lblDepartamento.setBounds(10, 11, 224, 14);
		contentPane.add(lblDepartamento);
		
		JLabel lblDireccion = new JLabel("Direccion");
		lblDireccion.setBounds(10, 38, 224, 14);
		contentPane.add(lblDireccion);
		
		direccion = new JTextField();
		direccion.setBounds(273, 38, 448, 20);
		contentPane.add(direccion);
		if(persona.getDireccion()!=null){
			direccion.setText(persona.getDireccion());
		}
		direccion.setColumns(10);
		
		JLabel lblDocumento = new JLabel("Documento");
		lblDocumento.setBounds(10, 63, 224, 14);
		contentPane.add(lblDocumento);
		
		documentoTipo = new JComboBox<String>();
		documentoTipo.setBounds(273, 63, 75, 20);
		documentoTipo.addItem("RUT");
		documentoTipo.addItem("CI");
		if(persona.getDocumento()!=null){
			switch (persona.getDocumento().getTipo()) {
			case 2:
				documentoTipo.setSelectedItem("RUT");
				break;
			case 3:
				documentoTipo.setSelectedItem("CI");
				break;

			default:
				break;
			}
		}
		contentPane.add(documentoTipo);
		
		String txtdocumento = "";
		if(persona.getDocumento()!=null){
			txtdocumento = persona.getDocumento().getDocumento(); 
		}
		documento = new JTextField();
		documento.setBounds(358, 63, 240, 20);
		documento.setText(txtdocumento);
		contentPane.add(documento);
		documento.setColumns(10);
		
		documentoPais = new JComboBox<String>();
		ArrayList<String> paises = getPaises();
		HashMap<String, String> hpaises = getHashPaises();
		for(String spais: paises){
			documentoPais.addItem(spais.toUpperCase());
		}
		String documentopaisdefecto = "UY";
		if(persona.getDocumento()!=null && persona.getDocumento().getPais()!=null && !persona.getDocumento().getPais().equals("")){
			documentopaisdefecto = persona.getPais().toUpperCase();
		}
		if(documentopaisdefecto.length()==2){
			documentopaisdefecto = hpaises.get(documentopaisdefecto.toUpperCase());
		}
		documentoPais.setSelectedItem(documentopaisdefecto);
		documentoPais.setBounds(608, 63, 113, 20);
		contentPane.add(documentoPais);
		
		JLabel lblNombre = new JLabel("Nombre");
		lblNombre.setBounds(10, 88, 224, 14);
		contentPane.add(lblNombre);
		
		nombre = new JTextField();
		nombre.setBounds(273, 88, 448, 20);
		nombre.setText(persona.getNombre());
		contentPane.add(nombre);
		nombre.setColumns(10);
		
		JLabel lblPais = new JLabel("Pais");
		lblPais.setBounds(10, 113, 224, 14);
		contentPane.add(lblPais);
		
		pais = new JComboBox<String>();
		pais.setBounds(273, 113, 448, 20);
		for(String spais: paises){
			pais.addItem(spais.toUpperCase());
		}
		String paisdefecto = "UY";
		if(persona.getPais()!=null && !persona.getPais().equals("")){
			paisdefecto = persona.getPais().toUpperCase();
		}
		if(paisdefecto.length()==2){
			paisdefecto = hpaises.get(paisdefecto.toUpperCase());
		}
		pais.setSelectedItem(paisdefecto);
		
		contentPane.add(pais);
		
		JButton btnAceptar = new JButton("Aceptar");
		btnAceptar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Receptor.factura.getReceptor().setDepartamento(txtDepartamento.getText());
				Receptor.factura.getReceptor().setCiudad(txtDepartamento.getText());
				Receptor.factura.getReceptor().setDireccion(direccion.getText());
				String dt = (String)documentoTipo.getSelectedItem();
				switch (dt) {
				case "RUT":
					Receptor.factura.getReceptor().getDocumento().setTipo(2);
					if(Receptor.factura.getTipo()==101){
						Receptor.factura.setTipo(111);
					}
					if(Receptor.factura.getTipo()==102){
						Receptor.factura.setTipo(112);
					}
					if(Receptor.factura.getTipo()==103){
						Receptor.factura.setTipo(113);
					}
					break;
				case "CI":
					Receptor.factura.getReceptor().getDocumento().setTipo(3);
					if(Receptor.factura.getTipo()==111){
						Receptor.factura.setTipo(101);
					}
					if(Receptor.factura.getTipo()==112){
						Receptor.factura.setTipo(102);
					}
					if(Receptor.factura.getTipo()==113){
						Receptor.factura.setTipo(103);
					}
					break;

				default:
					break;
				}
				Receptor.factura.getReceptor().getDocumento().setDocumento(documento.getText());
				String dp = (String)documentoPais.getSelectedItem();
				HashMap<String, String> paises = Receptor.getHashPaisesInverso();
				dp = paises.get(dp);
				Receptor.factura.getReceptor().getDocumento().setPais(dp);
				Receptor.factura.getReceptor().setNombre(nombre.getText());
				String p = (String)pais.getSelectedItem();
				p = paises.get(p);
				Receptor.factura.getReceptor().setPais(p);
				dispose();
			}
		});
		btnAceptar.setBounds(632, 167, 89, 23);
		contentPane.add(btnAceptar);
		
		txtDepartamento = new JTextField();
		txtDepartamento.setBounds(273, 8, 448, 20);
		txtDepartamento.setText(persona.getDepartamento());
		contentPane.add(txtDepartamento);
		txtDepartamento.setColumns(10);
		
		JLabel lblError = new JLabel(error);
		lblError.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblError.setForeground(Color.RED);
		lblError.setBounds(273, 144, 448, 14);
		contentPane.add(lblError);
		
		JButton btnCancelar = new JButton("Cancelar");
		btnCancelar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Receptor.interfaz.setCancelado(true);
				dispose();
			}
		});
		btnCancelar.setBounds(273, 167, 89, 23);
		contentPane.add(btnCancelar);
		return persona;
	}
	
	public void actualizarDireccion(String direccionNueva){
		Receptor.factura.getReceptor().setDireccion(direccionNueva);
	}
	
	public void actualizarRut(String rutNuevo){
		Receptor.factura.getReceptor().getDocumento().setDocumento(rutNuevo);
		
	}

}

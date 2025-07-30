package controlador;

import gui.Receptor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.activation.DataHandler;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sourceforge.jtds.jdbc.DateTime;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.JDKAlgorithmParameters.IVAlgorithmParameters;

import com.ibm.icu.text.Replaceable;

import servicios.FacturaElectronicaImplServiceStub_old;
import servicios.FacturaElectronicaImplServiceStub_old.CodigoItem;
import servicios.FacturaElectronicaImplServiceStub_old.Descuento;
import servicios.FacturaElectronicaImplServiceStub_old.Documento;
import servicios.FacturaElectronicaImplServiceStub_old.Factura;
import servicios.FacturaElectronicaImplServiceStub_old.GenerarFactura;
import servicios.FacturaElectronicaImplServiceStub_old.GetImprimible;
import servicios.FacturaElectronicaImplServiceStub_old.InfoDescuentoRecargo;
import servicios.FacturaElectronicaImplServiceStub_old.InfoReferencia;
import servicios.FacturaElectronicaImplServiceStub_old.Persona;
import servicios.FacturaElectronicaImplServiceStub_old.Producto;
import servicios.FacturaElectronicaImplServiceStub_old.Respuesta;
import servicios.FacturaElectronicaImplServiceStub_old.Subtotal;
import bd.DBDriver;
import bd.DBDriverPostgreSQL;
import bd.Direccionador;

public class respaldo_OfInterfazSRFL {

	public static boolean TEST = false;
	private static final Logger log = Logger.getLogger(respaldo_OfInterfazSRFL.class
			.getName());
	private boolean cancelado;
	private int codigoRes = 0;
	private String msjRes = null;
	Receptor receptor;
	Factura factura;
	double cotizacion;
	private double montoAdenda = 0;
	public double montoAdendaPosta = 0.0;
	private String ivaexportacion;
	respaldo_OfInterfazSRFL interfaz;
	boolean excedeUi = false;
	int idBusqueda = 0;
	String nombreCliente = "";
	ArrayList<String> ad;
	int banderaAlmuerzoCenaParaFactura = 0;
	int banderaCenaAlmuerzoParaFactura = 0;
	String nombreProductoParaFactura = "";
	boolean hayProductosPdu = false;
	String tipoImpuestoCliente = "";

	public boolean isCancelado() {
		return cancelado;
	}

	public void setCancelado(boolean cancelado) {
		this.cancelado = cancelado;
	}

	public respaldo_OfInterfazSRFL() {
		Properties properties = Direccionador.getInstance()
				.getArchivoConfiguracion();

		try {
			Handler fh = new FileHandler(properties.getProperty("log"), true);
			SimpleFormatter formatterTxt = new SimpleFormatter();
			fh.setFormatter(formatterTxt);
			log.addHandler(fh);
			log.setLevel(Level.INFO);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ArrayList<Object> getId(String serie, int numero) throws Exception {

		DBDriver bd = DBDriver.getInstance();
		bd.conectar();
		String consulta = "select folio from tempcheques "
				+ "where seriefolio=? and numcheque=?";
		PreparedStatement ps = bd.prepareStatement(consulta);
		ps.setString(1, serie);
		ps.setInt(2, numero);
		if (TEST) {
			System.out.println(consulta + "::" + serie + "::" + numero);
		}
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			Integer id = rs.getInt(1);
			idBusqueda = id;
			bd.desconectar();
			ArrayList<Object> s = new ArrayList<>();
			s.add(id);
			s.add("temp");
			return s;
		} else {
			consulta = "select folio from cheques "
					+ "where seriefolio=? and numcheque=?";
			ps = bd.prepareStatement(consulta);
			ps.setString(1, serie);
			ps.setInt(2, numero);
			if (TEST) {
				System.out.println(consulta + "::" + serie + "::" + numero);
			}
			rs = ps.executeQuery();
			if (rs.next()) {
				Integer id = rs.getInt(1);
				bd.desconectar();
				ArrayList<Object> s = new ArrayList<>();
				s.add(id);
				s.add("");
				return s;
			}
		}
		bd.desconectar();
		return null;
	}

	private boolean lineaProductoConIva(Properties propiedades) {

		boolean productoConIva = true;
		String lineaProductoConIva = propiedades
				.getProperty("lineaProductoConIva");
		if (lineaProductoConIva.equalsIgnoreCase("0"))
			productoConIva = false;

		return productoConIva;
	}

	private Factura cargarLineas(double tasabasicaiva,
			double totalmontoivatasabasica, double descuentoGeneral,
			double tasaminimaiva, double totalmontoivatasaminima,
			double totalmontonogravado, double totalmontoensuspenso,
			int nlinea, String prefijo, String consulta, int id,
			PreparedStatement ps, ResultSet rs, DBDriver bd,
			boolean esNotadeCreditoXNegativo, boolean ocultardetalle,
			Properties properties, int indicadorfacturaciondescuento,
			Factura factura, double propina, double cargo,
			double totalmontoivaotratasa, double descuentoGeneralIvaBasico,
			double descuentoGeneralIvaMinimo,
			double descuentoGeneralIvaExcento,double descuentoGeneralIvaExportacion, int nlineaDescuento,
			boolean esFacturaAsimilada, double totalmontoExportacion, String serieorden)
			throws Exception {

		// ACA TENGO QUE PREGUNTAR SI EL FOLIO TIENE CLIENTE Y SI EL CLIENTE NO
		// COBRA IMPUESTO
		// CHECK TILDADO EN SR DE QUE NO COBRA IMPUESTOS ES EXPORTACION
		String checkImpuestos = "0";
		String consultaSiTieneCliente = "select f.idcliente from " + prefijo
				+ "cheques f " + "join " + prefijo
				+ "cheques ff on ff.fecha=f.fecha and f.orden=ff.orden  "
				+ "where f.folio=? ";
		PreparedStatement psCheckCliente = bd
				.prepareStatement(consultaSiTieneCliente);
		psCheckCliente.setInt(1, id);
		if (TEST) {
			System.out.println(consulta + "::" + id);
		}

		ResultSet rsCheckCliente = psCheckCliente.executeQuery();
		if (rsCheckCliente.next()) {
			String consultaCheckImpuestos = "select nocobrarimpuestos from clientes "
					+ "where idcliente =?";
			PreparedStatement psCheckImpuestos = bd
					.prepareStatement(consultaCheckImpuestos);
			psCheckImpuestos.setString(1, rsCheckCliente.getString(1));
			if (TEST) {
				System.out.println(consulta + "::" + id);
			}

			ResultSet rsCheckImpuestos = psCheckImpuestos.executeQuery();
			if (rsCheckImpuestos.next()) {
				if (rsCheckImpuestos.getString(1).equalsIgnoreCase("1")) {
					checkImpuestos = "1";
				}
			}

			// //////////////////////////////////////////////////////////////////

			// ACA VEO SI TIENE IMPUESTO ASIGNADO EN TIPO DE CLIENTE

			// String idcliente = rs.getString("idcliente");
			String consulta2 = "select descripcion from tipoclientes as tp "
					+ "inner join clientes as c "
					+ "on c.idtipocliente = tp.idtipocliente "
					+ "where idcliente = ?";
			PreparedStatement psConsultaIdCliente = bd
					.prepareStatement(consulta2);
			psConsultaIdCliente.setString(1, rsCheckCliente.getString(1));

			ResultSet rsConsultaIdCliente = psConsultaIdCliente.executeQuery();

			while (rsConsultaIdCliente.next()) {
				System.out.println("Tiene un tipo de cliente asignado: "
						+ rsConsultaIdCliente.getString(1));

				tipoImpuestoCliente = "";

				if (rsConsultaIdCliente.getString(1).contains("BASICA")
						|| rsConsultaIdCliente.getString(1).contains("basica")) {
					tipoImpuestoCliente = "3";
				} else if (rsConsultaIdCliente.getString(1).contains("MINIMA")
						|| rsConsultaIdCliente.getString(1).contains("minima")) {
					tipoImpuestoCliente = "2";
				} else if (rsConsultaIdCliente.getString(1).contains("EXCENTO")
						|| rsConsultaIdCliente.getString(1).contains("excento")) {
					tipoImpuestoCliente = "1";
				} else if (rsConsultaIdCliente.getString(1).contains(
						"EXPORTACION")
						|| rsConsultaIdCliente.getString(1).contains(
								"exportacion")) {
					tipoImpuestoCliente = "10";
				} else if (rsConsultaIdCliente.getString(1)
						.contains("SUSPENSO")
						|| rsConsultaIdCliente.getString(1)
								.contains("suspenso")) {
					tipoImpuestoCliente = "12";
				}
			}

			// //////////////////////////////////////////////////////////////////
		}

		ArrayList<Producto> productos = new ArrayList<>();
		ArrayList<InfoDescuentoRecargo> idrs = new ArrayList<>();
		boolean lineaProductoConIva = lineaProductoConIva(properties);
		double montopostaSinDescuentoGeneral = 0;
		try {
			// MR - Mexico Realizo un cambio que cuando reabre una mesa desde el
			// consultar mesa cambia el nombre. Saque el and f.mesa=ff.mesa
			consulta = "select max(ff.folio) from " + prefijo + "cheques f "
					+ "join " + prefijo
					+ "cheques ff on ff.fecha=f.fecha and f.orden=ff.orden  "
					+ "where f.folio=? and ff.cancelado=0 and ff.seriefolio=?";
			ps = bd.prepareStatement(consulta);
			ps.setInt(1, id);
			ps.setString(2, serieorden.trim());
			if (TEST) {
				System.out.println(consulta + "::" + id);
			}

			rs = ps.executeQuery();
			int idfoliodetalle = 0;
			if (rs.next()) {
				idfoliodetalle = rs.getInt(1);
			}
			if (idfoliodetalle == 0) {
				// MR - Mexico Realizo un cambio que cuando reabre una mesa
				// desde el
				// consultar mesa cambia el nombre. Saque el and f.mesa=ff.mesa
				consulta = "select max(ff.folio) from "
						+ prefijo
						+ "cheques f "
						+ "join "
						+ prefijo
						+ "cheques ff on ff.fecha=f.fecha and f.orden=ff.orden "
						+ "where f.folio=?";
				ps = bd.prepareStatement(consulta);
				ps.setInt(1, id);
				if (TEST) {
					System.out.println(consulta + "::" + id);
				}

				rs = ps.executeQuery();
				rs.next();
				idfoliodetalle = rs.getInt(1);
			}

			// Consulta que busca los productos en la factura
			// SI el total es negativo, se mandan todos sus productos en
			// positivo
			// SI el total es positivo, validamos que los casos que tengamos
			// productos negativos se pasa la cantidad a negativo y el
			// totalxLinea
			// como positivo
			
			if(properties.getProperty("facturaproductoshijos")!=null
					&& properties.getProperty("facturaproductoshijos").equalsIgnoreCase("1")){
				consulta = " SELECT d.cantidad AS cantidad,p.idproducto AS clave, "
						+ "p.descripcion AS descripcion, d.precio AS precio ,d.impuesto1 AS impuesto1, "
						+ " d.idproductocompuesto as idproductocompuesto,"
						+ " 0 as nivel,"
						+ "   d.descuento as descuento ,"
						+ "   d.productocompuestoprincipal,"
						+ "   d.modificador,"
						+ "   pd.usarmultiplicadorprodcomp, "
						+ "   d.preciosinimpuestos AS preciosinimpuestos,  "
						+ "   p.plu as plu,  "
						+ "   pd.idunidad as unidadmedida,"
						+ "	p.idgrupo as grupo  "
						+ " FROM "
						+ prefijo
						+ "cheqdet d JOIN productos p ON d.idproducto=p.idproducto"
						+ "    JOIN grupos g ON g.idgrupo=p.idgrupo "
						+ "INNER JOIN productosdetalle pd ON p.idproducto=pd.idproducto "
						+ " WHERE foliodet=? and (p.nofacturable is null or p.nofacturable=0)"
						+ " ORDER BY g.prioridadimpresion,p.descripcion ";
			} else{
			
			consulta = " SELECT d.cantidad AS cantidad,p.idproducto AS clave, "
					+ "p.descripcion AS descripcion, d.precio AS precio ,d.impuesto1 AS impuesto1, "
					+ " d.idproductocompuesto as idproductocompuesto,"
					+ " 0 as nivel,"
					+ "   d.descuento as descuento ,"
					+ "   d.productocompuestoprincipal,"
					+ "   d.modificador,"
					+ "   pd.usarmultiplicadorprodcomp, "
					+ "   d.preciosinimpuestos AS preciosinimpuestos,  "
					+ "   p.plu as plu,  "
					+ "   pd.idunidad as unidadmedida,"
					+ "	p.idgrupo as grupo  "
					+ " FROM "
					+ prefijo
					+ "cheqdet d JOIN productos p ON d.idproducto=p.idproducto AND d.modificador=0"
					+ "    JOIN grupos g ON g.idgrupo=p.idgrupo "
					+ "INNER JOIN productosdetalle pd ON p.idproducto=pd.idproducto "
					+ " WHERE foliodet=? and (p.nofacturable is null or p.nofacturable=0)"
					+ " ORDER BY g.prioridadimpresion,p.descripcion ";
			}

			ps = bd.prepareStatement(consulta);
			ps.setInt(1, idfoliodetalle);

			ArrayList<SRProducto> srproductos = new ArrayList<>();
			rs = ps.executeQuery();
			nlinea = 1;
			nlineaDescuento = 1;

			Properties propiedades = Direccionador.getInstance()
					.getArchivoConfiguracion();

			while (rs.next()) {
				double precio = 0;
				double cantidad = rs.getDouble("cantidad");
				String cod = rs.getString("clave");
				String codigoAlmuerzo = propiedades
						.getProperty("idProductoAlmuerzo");
				String codigoCena = propiedades.getProperty("idProductoCena");
				if (cod.equalsIgnoreCase(codigoAlmuerzo)) {
					banderaAlmuerzoCenaParaFactura = 1;
					nombreProductoParaFactura = rs.getString("descripcion");
				} else if (cod.equalsIgnoreCase(codigoCena)) {
					banderaCenaAlmuerzoParaFactura = 1;
					nombreProductoParaFactura = rs.getString("descripcion");
				}
				String desc = rs.getString("descripcion");
				// Cambio 02/03/23
				if (lineaProductoConIva)
					precio = rs.getDouble("precio");
				else
					precio = rs.getDouble("preciosinimpuestos");
				double impuesto = rs.getDouble("impuesto1") / 100;
				double descuentolinea = rs.getDouble("descuento") / 100;
				String idproductocompuesto = rs
						.getString("idproductocompuesto");
				int productocompuestoprincipal = rs
						.getInt("productocompuestoprincipal");
				int nivel = rs.getInt("nivel");
				int multiplicadorcantidad = rs
						.getInt("usarmultiplicadorprodcomp");
				System.out.println(desc + "--" + precio + "--" + cantidad);
				// SANTI AGREGUE CAMPOS PARA PLU
				String pluProducto = rs.getString("plu");
				pluProducto = pluProducto.trim();

				System.out.println("PLU:" + pluProducto);
				System.out.println("CANTIDAD PLU" + pluProducto.length());
				if (!pluProducto.equalsIgnoreCase("")) {
					hayProductosPdu = true;
					System.out.println(" entre a plu ");
				}
				String unidadMedida = rs.getString("unidadmedida");
				if(unidadMedida!=null){
				if (!unidadMedida.equalsIgnoreCase("")
						&& unidadMedida.length() > 4) {
					unidadMedida = unidadMedida.substring(0, 4);
				}
				}else{
					unidadMedida="KG";
				}
				String idgrupo = rs.getString("grupo");
				if (propiedades.getProperty("idgruposuspenso") != null) {
					String idGruposIvaSuspenso = propiedades
							.getProperty("idgruposuspenso");
					String[] parts = idGruposIvaSuspenso.split(",");
					for (String c : parts) {
						if (idgrupo.equalsIgnoreCase(c)
								&& tipoImpuestoCliente.equalsIgnoreCase("12")) {
							impuesto = 12;
							break;
						}
					}
				}
				SRProducto srp = new SRProducto(desc, idproductocompuesto,
						cantidad, precio, nivel, impuesto, descuentolinea, cod,
						productocompuestoprincipal, multiplicadorcantidad,
						esNotadeCreditoXNegativo, pluProducto, unidadMedida);

				srproductos.add(srp);

			}

			ArrayList<SRProducto> aux = new ArrayList<>();
			for (SRProducto s : srproductos) {
				aux.add(s);
				if (s.getIdproductocompuesto() != null
						&& !s.getIdproductocompuesto().equals("")) {
					consulta = " SELECT d.cantidad AS cantidad ,p.idproducto AS clave, "
							+ "p.descripcion AS descripcion, d.precio AS precio,d.impuesto1 AS impuesto1, "
							+ " d.idproductocompuesto as idproductocompuesto,"
							+ " 1 as nivel,"
							+ "   d.descuento as descuento ,"
							+ "   d.productocompuestoprincipal,"
							+ "   d.modificador, "
							+ "   d.preciosinimpuestos AS preciosinimpuestos, "
							+ "   p.plu as plu  "
							+ " FROM "
							+ prefijo
							+ "cheqdet d JOIN productos p ON d.idproducto=p.idproducto AND d.modificador=1"
							+ "    JOIN grupos g ON g.idgrupo=p.idgrupo"
							+ " WHERE foliodet=? and d.idproductocompuesto=?  and (p.nofacturable is null or p.nofacturable=0)"
							+ " ORDER BY g.prioridadimpresion,p.descripcion ";
					ps = bd.prepareStatement(consulta);
					ps.setInt(1, idfoliodetalle);
					ps.setString(2, s.getIdproductocompuesto());

					rs = ps.executeQuery();
					while (rs.next()) {
						double precio = 0;
						double cantidad = rs.getDouble("cantidad");
						String cod = rs.getString("clave");
						String desc = "-->" + rs.getString("descripcion");
						if (lineaProductoConIva)
							precio = rs.getDouble("precio");
						else
							precio = rs.getDouble("preciosinimpuestos");
						double impuesto = rs.getDouble("impuesto1") / 100;
						double descuentolinea = rs.getDouble("descuento") / 100;
						String idproductocompuesto = rs
								.getString("idproductocompuesto");
						int productocompuestoprincipal = rs
								.getInt("productocompuestoprincipal");
						int nivel = rs.getInt("nivel");
						String pluProducto = rs.getString("plu");
						if (!pluProducto.equalsIgnoreCase("")) {
							hayProductosPdu = true;
						}
						String unidadMedida = "KGM";
						System.out.println("--> MODIFICADOR: " + desc + "--"
								+ precio + "--" + cantidad + "--" + impuesto);
						SRProducto srp = new SRProducto(desc,
								idproductocompuesto, cantidad, precio, nivel,
								impuesto, descuentolinea, cod,
								productocompuestoprincipal, 0,
								esNotadeCreditoXNegativo, pluProducto,
								unidadMedida);
						if (!ocultardetalle) {
							aux.add(srp);
						} else {
							if (precio != 0) {
								// System.out.println("precio mayor a cero");
								System.out
										.println("esta en ocultardetalle, pero el precio es mayor que cero");
								if (s.getMultiplicadorcantidad() != 1
										|| srp.getImpuesto() != s.getImpuesto()) {
									aux.add(srp);
									System.out.println("agrego porque "
											+ s.getMultiplicadorcantidad()
											+ " !=1 o " + srp.getImpuesto()
											+ "!=" + s.getImpuesto());
								} else
									s.setPrecio(s.getPrecio() + srp.getPrecio());
							}
						}
					}
				}
			}
			srproductos = aux;

			// System.out.println("****************Despues de las dos consultas:");
			// this.imprimirproductos(srproductos);

			ArrayList<SRProducto> productosfinales = new ArrayList<>();
			for (SRProducto srpoducto : srproductos) {
				boolean yaestaba = false;

				boolean debug = false;
				if (srpoducto.getNombre().contains("MIXTA")) {
					System.out.println("va a ver si agregupa una mixta: "
							+ srpoducto.getIdproductocompuesto());
					debug = true;
				}
				for (int j = productosfinales.size(); j > 0; j--) {
					SRProducto s = productosfinales.get(j - 1);
					if (!yaestaba && srpoducto.getImpuesto() == s.getImpuesto()
							&& srpoducto.getCodigo().equals(s.getCodigo())
							&& srpoducto.getPrecio() == s.getPrecio()
							&& srpoducto.getNombre().equals(s.getNombre())
							&& srpoducto.getDescuento() == s.getDescuento()) {

						// antes de agregar, si es un producto hijo, lo agrego
						// solo
						// si el padre no esta por separado
						if (srpoducto.getNivel() > 0) {
							boolean encontre = false;
							if (srpoducto.getIdproductocompuesto().equals(
									s.getIdproductocompuesto())) {
								if (debug)
									System.out
											.println("Encontro mismo idproductocompuesto (encontre en falso)");
								encontre = false;
							} else {
								for (SRProducto ss : productosfinales) {
									if (ss.getNivel() == 0
											&&
											// s.getIdproductocompuesto().contains(srpoducto.getIdproductocompuesto())
											// &&
											ss.getIdproductocompuesto()
													.contains(
															srpoducto
																	.getIdproductocompuesto())) {
										System.out
												.println("encontro al padre por ahi (encontre en true)");
										encontre = true;
									}
								}
							}
							if (!encontre) {
								if (debug)
									System.out.println("agrega cantidad");
								s.setCantidad(s.getCantidad()
										+ srpoducto.getCantidad());
								if (!s.getIdproductocompuesto().equals(
										srpoducto.getIdproductocompuesto())) {
									s.setIdproductocompuesto(s
											.getIdproductocompuesto()
											+ " * "
											+ srpoducto
													.getIdproductocompuesto());
									if (debug)
										System.out.println("concatena codigos");

								}
								yaestaba = true;
							}
						} else {
							if (debug)
								System.out
										.println("agrega cantidad - es un padre");

							s.setCantidad(s.getCantidad()
									+ srpoducto.getCantidad());
							yaestaba = true;
						}

					}
				}

				if (!yaestaba) {
					productosfinales.add(srpoducto);
				}
			}
			srproductos = productosfinales;

			if (banderaAlmuerzoCenaParaFactura == 1) {
				double cantidadproductos = 1;
				double montoFactura = 0.0;
				double montoUnitario = 0.0;
				for (int p = 0; p < srproductos.size(); p++) {
					SRProducto s = srproductos.get(p);
					montoFactura = (s.getCantidad() * s.getPrecio())
							+ montoFactura;
					if (s.getCodigo().equalsIgnoreCase(
							propiedades.getProperty("idProductoAlmuerzo"))) {
						cantidadproductos = s.getCantidad();
					}
					// if(s.getNombre().equalsIgnoreCase("almuerzocenaparafactura")){
					// cantidadproductos=s.getCantidad();

					// }
				}
				montoUnitario = montoFactura / cantidadproductos;
				srproductos.clear();
				SRProducto productoUnico = new SRProducto(
						nombreProductoParaFactura, "0", cantidadproductos,
						montoUnitario, 0, 0.22, 0.0, "1", 0, 0,
						esNotadeCreditoXNegativo, "", "");
				srproductos.add(productoUnico);
			}
			if (banderaCenaAlmuerzoParaFactura == 1) {
				double cantidadproductos = 1;
				double montoFactura = 0.0;
				double montoUnitario = 0.0;

				for (int p = 0; p < srproductos.size(); p++) {
					SRProducto s = srproductos.get(p);
					montoFactura = (s.getCantidad() * s.getPrecio())
							+ montoFactura;
					if (s.getCodigo().equalsIgnoreCase(
							propiedades.getProperty("idProductoCena"))) {
						cantidadproductos = s.getCantidad();
					}
					// if(s.getNombre().equalsIgnoreCase("cenaalmuerzoparafactura")){
					// cantidadproductos=s.getCantidad();
					// }
				}
				montoUnitario = montoFactura / cantidadproductos;
				srproductos.clear();
				SRProducto productoUnico = new SRProducto(
						nombreProductoParaFactura, "0", cantidadproductos,
						montoUnitario, 0, 0.22, 0.0, "1", 0, 0,
						esNotadeCreditoXNegativo, "", "");
				srproductos.add(productoUnico);
			}

			// System.out.println("****************Despues de achicar:");
			// this.imprimirproductos(srproductos);
			if (factura.getTipo() != 181) {

				for (SRProducto srproducto : srproductos) {
					Producto p = new Producto();
					p.setAgenteResponsable(false);
					p.setCantidad(srproducto.getCantidad());
					// SANTI ACA PREGUNTO SI TIENE ALGO EN PLU PARA CARGAR EL
					// CODIGO
					ArrayList<CodigoItem> codigos = new ArrayList<>();
					CodigoItem codigo = new CodigoItem();
					if (!srproducto.getPlu().equalsIgnoreCase("")) {
						codigo.setCodigo(srproducto.getPlu());
						if (properties.getProperty("usaCodigoDeBarra")
								.equalsIgnoreCase("1")) {
							codigo.setTipo("GTN13");
						} else if (!properties.getProperty("usaCodigoDeBarra")
								.equalsIgnoreCase("1")) {
							codigo.setTipo("INT1");
						}
						codigos.add(codigo);
					} else {
						codigo.setCodigo(srproducto.getCodigo());
						codigo.setTipo("SR");
						codigos.add(codigo);
					}

					p.setCodigos(codigos.toArray(new CodigoItem[codigos.size()]));
					p.setDescripcion(srproducto.getNombre());
					p.setUnidadDeMedida(srproducto.getUnidadMedida());
					p.setNombre(srproducto.getNombre());

					ArrayList<Descuento> descuentos = new ArrayList<>();

					if (srproducto.getDescuento() > 0) {
						Descuento descuentop = new Descuento();
						descuentop.setTipo("%");
						// antes mandava el %, ahora va el valor
						double desc = srproducto.getDescuento()
								* (srproducto.getPrecio() * srproducto
										.getCantidad());
						desc = desc * 100;
						desc = Math.round(desc);
						desc = desc / 100.0;
						descuentop.setValor(desc);
						descuentos.add(descuentop);
						if (srproducto.getDescuento() == 100)
							p.setNombre(p.getNombre()
									+ " "
									+ properties
											.getProperty("textoredescuentolinea100"));

					}
					p.setDescuentos(descuentos.toArray(new Descuento[descuentos
							.size()]));
					// srproducto.getCantidad() lo agergo como absoluto porque
					// en
					// caso
					// de ser negativo se carga en la cantidad arriba
					// double monto =
					// srproducto.getPrecio()*Math.abs(srproducto.getCantidad());
					double monto = 0.0;
					// if(banderaAlmuerzoCenaParaFactura==0 &&
					// banderaCenaAlmuerzoParaFactura==0){
					monto = srproducto.getPrecio() * srproducto.getCantidad();
					// }
					// else{
					// monto = srproducto.getPrecio();
					// }

					monto = monto * (1.00 - srproducto.getDescuento());
					montopostaSinDescuentoGeneral += monto;
					double montoposta = monto * (100 - descuentoGeneral) / 100;

					// MR - El monto del producto es siempre absoluto
					// p.setMonto(Math.abs(monto));;
					p.setMonto(monto);
					double montosinimp = 0;
					// && !tipoImpuestoCliente.equalsIgnoreCase("12")
					if (lineaProductoConIva && srproducto.getImpuesto() != 12)

						montosinimp = montoposta
								/ (1 + srproducto.getImpuesto());
					else
						montosinimp = montoposta;
					// System.out.println("impuesto: "+srproducto.getImpuesto());
					// if(monto==0){
					// ACA PREGUNTO SI EL CLIENTE TIENE EL CHECK HABILITADO Y LE
					// PONEMOS INDICADOR
					// EN 10
					if (checkImpuestos.contentEquals("1")) {
						esFacturaAsimilada = true;
						if (esFacturaAsimilada) {
							totalmontoExportacion += montosinimp;
							p.setIndicadorFacturacion(10);
							indicadorfacturaciondescuento = 10;
						}
						// Aca agrego el descuento sin iva
						if (descuentoGeneral > 0) {
							descuentoGeneralIvaExportacion += monto - montoposta;
						}
						System.out.println(" ");
					} else if (tipoImpuestoCliente.equalsIgnoreCase("12")
							&& srproducto.getImpuesto() == 12) {
						System.out.println("IVA SUSPENSO");
						// totalmontoensuspenso = totalmontoensuspenso
						// + srproducto.getPrecio();
						totalmontoensuspenso += montosinimp;
						p.setIndicadorFacturacion(12);
						indicadorfacturaciondescuento = 12;
					} else if (srproducto.getPrecio() == 0
							&& factura.getTipo() != 181) {
						System.out.println("PRECIO 0");
						p.setIndicadorFacturacion(5);
						indicadorfacturaciondescuento = 5;
					} else if (srproducto.getImpuesto() * 100 == tasabasicaiva) {
						totalmontoivatasabasica += montosinimp;
						indicadorfacturaciondescuento = 3;
						p.setIndicadorFacturacion(3);
						if (descuentoGeneral > 0) {
							descuentoGeneralIvaBasico += monto - montoposta;
						}
						System.out.println(" ... ");
					} else if (srproducto.getImpuesto() * 100 == tasaminimaiva) {
						totalmontoivatasaminima += montosinimp;
						p.setIndicadorFacturacion(2);
						// if(indicadorfacturaciondescuento==-1){
						indicadorfacturaciondescuento = 2;
						// }
						// Aca agergo el descuento tasaMinima
						if (descuentoGeneral > 0) {
							descuentoGeneralIvaMinimo += monto - montoposta;

						}
						System.out.println(" ");
					} else if (srproducto.getImpuesto() == 0
							&& properties.getProperty("ivaexportacion")
									.contentEquals("1")) {
						esFacturaAsimilada = true;
						if (esFacturaAsimilada) {
							totalmontoExportacion += montosinimp;
							p.setIndicadorFacturacion(10);
							indicadorfacturaciondescuento = 10;
						}
						// Aca agrego el descuento sin iva
						if (descuentoGeneral > 0) {
							descuentoGeneralIvaExportacion += monto - montoposta;
						}
						System.out.println(" ");
					} else if (srproducto.getImpuesto() == 0
							&& properties.getProperty("ivaexportacion")
									.contentEquals("0")) {

						totalmontonogravado += montosinimp;
						p.setIndicadorFacturacion(1);
						indicadorfacturaciondescuento = 1;

						// Aca agrego el descuento sin iva
						if (descuentoGeneral > 0) {
							descuentoGeneralIvaExcento += monto - montoposta;
						}
						System.out.println(" ");
					} else {
						throw new Exception(
								"El impuesto no coincide con 10 ni con 22 ni con 0. ("
										+ srproducto.getNombre() + ": "
										+ srproducto.getImpuesto() + ")");
						// totalmontoivaotratasa+=montosinimp;
						// p.setIndicadorFacturacion(1);
					}
					/*
					 * ivaexportacion =
					 * properties.getProperty("ivaexportacion");
					 * 
					 * if (ivaexportacion.contentEquals("1")) {
					 * esFacturaAsimilada=true; if (esFacturaAsimilada) {
					 * totalmontoExportacion += montosinimp;
					 * p.setIndicadorFacturacion(10);
					 * indicadorfacturaciondescuento = 1; } else {
					 * totalmontonogravado += montosinimp;
					 * p.setIndicadorFacturacion(1);
					 * indicadorfacturaciondescuento = 1; } // Aca agrego el
					 * descuento sin iva if (descuentoGeneral > 0) {
					 * descuentoGeneralIvaExcento += monto - montoposta; }
					 * System.out.println(" "); }
					 */

					p.setNroLinea(nlinea);
					nlinea++;
					p.setPorcentajeDescuento(srproducto.getDescuento() * 100);
					p.setPorcentajeRecargo(0);
					p.setPrecioUnitario(srproducto.getPrecio());
					// p.setUnidadDeMedida("N/A");
					productos.add(p);
				}// cierro for productos

				// MR: Aca se ve el tema de los descuentos generales
				if (descuentoGeneral > 0) {
					InfoDescuentoRecargo idrIvaBasico = new InfoDescuentoRecargo();
					InfoDescuentoRecargo idrIvaMinimo = new InfoDescuentoRecargo();
					InfoDescuentoRecargo idrIvaExcento = new InfoDescuentoRecargo();
					InfoDescuentoRecargo idrIvaExportacion = new InfoDescuentoRecargo();

					if (descuentoGeneral == 100) {
						idrIvaBasico.setGlosa(properties
								.getProperty("textoredescuento100"));
						idrIvaMinimo.setGlosa(properties
								.getProperty("textoredescuento100"));
						idrIvaExcento.setGlosa(properties
								.getProperty("textoredescuento100"));
						idrIvaExportacion.setGlosa(properties
								.getProperty("textoredescuento100"));
					} else {
						idrIvaBasico.setGlosa(properties
								.getProperty("textoredescuento")
								+ " "
								+ descuentoGeneral);
						idrIvaMinimo.setGlosa(properties
								.getProperty("textoredescuento")
								+ " "
								+ descuentoGeneral);
						idrIvaExcento.setGlosa(properties
								.getProperty("textoredescuento")
								+ " "
								+ descuentoGeneral);
						idrIvaExportacion.setGlosa(properties
								.getProperty("textoredescuento")
								+ " "
								+ descuentoGeneral);
					}
					idrIvaBasico.setIndicadorFacturacion(3);
					idrIvaMinimo.setIndicadorFacturacion(2);
					idrIvaExcento.setIndicadorFacturacion(1);
					idrIvaExportacion.setIndicadorFacturacion(10);

					System.out
							.println(" DESCUENTO GENERAL:"
									+ (montopostaSinDescuentoGeneral
											* descuentoGeneral / 100));
					if (descuentoGeneralIvaBasico > 0) {
						idrIvaBasico.setCodigo(1);
						idrIvaBasico.setNroLinea(nlineaDescuento);
						nlineaDescuento++;
						// if(lineaProductoConIva){
						// // MR - Poruqe lo que funciona no se toca
						// idrIvaBasico.setTipoDescuentoRecargo("%");
						// idrIvaBasico.setTipoMovimiento("D");
						// idrIvaBasico.setValor(descuentoGeneral);
						// }else{
						idrIvaBasico.setTipoDescuentoRecargo("$");
						idrIvaBasico.setTipoMovimiento("D");
						System.out.println(" DESCUENTO BASICO:"
								+ descuentoGeneralIvaBasico);
						idrIvaBasico.setValor(descuentoGeneralIvaBasico);
						// }
						idrs.add(idrIvaBasico);
					}
					if (descuentoGeneralIvaMinimo > 0) {
						idrIvaMinimo.setCodigo(2);
						idrIvaMinimo.setNroLinea(nlineaDescuento);
						nlineaDescuento++;
						// if(lineaProductoConIva){
						// // MR - Poruqe lo que funciona no se toca
						// idrIvaMinimo.setTipoDescuentoRecargo("%");
						// idrIvaMinimo.setTipoMovimiento("D");
						// idrIvaMinimo.setValor(descuentoGeneral);
						// }else{
						idrIvaMinimo.setTipoDescuentoRecargo("$");
						idrIvaMinimo.setTipoMovimiento("D");
						System.out.println(" DESCUENTO MINIMO:"
								+ descuentoGeneralIvaMinimo);
						idrIvaMinimo.setValor(descuentoGeneralIvaMinimo);
						// }
						idrs.add(idrIvaMinimo);
					}
					if (descuentoGeneralIvaExcento > 0) {
						idrIvaExcento.setCodigo(3);
						idrIvaExcento.setNroLinea(nlineaDescuento);
						nlineaDescuento++;
						// if(lineaProductoConIva){
						// // MR - Poruqe lo que funciona no se toca
						// idrIvaExcento.setTipoDescuentoRecargo("%");
						// idrIvaExcento.setTipoMovimiento("D");
						// idrIvaExcento.setValor(descuentoGeneral);
						// }else{
						idrIvaExcento.setTipoDescuentoRecargo("$");
						idrIvaExcento.setTipoMovimiento("D");
						System.out.println(" DESCUENTO EXCENTO:"
								+ descuentoGeneralIvaExcento);
						idrIvaExcento.setValor(descuentoGeneralIvaExcento);
						// }
						idrs.add(idrIvaExcento);
					}
					if (descuentoGeneralIvaExportacion > 0) {
						idrIvaExportacion.setCodigo(4);
						idrIvaExportacion.setNroLinea(nlineaDescuento);
						nlineaDescuento++;
						// if(lineaProductoConIva){
						// // MR - Poruqe lo que funciona no se toca
						// idrIvaExcento.setTipoDescuentoRecargo("%");
						// idrIvaExcento.setTipoMovimiento("D");
						// idrIvaExcento.setValor(descuentoGeneral);
						// }else{
						idrIvaExportacion.setTipoDescuentoRecargo("$");
						idrIvaExportacion.setTipoMovimiento("D");
						System.out.println(" DESCUENTO EXPORTACION:"
								+ descuentoGeneralIvaExportacion);
						idrIvaExportacion.setValor(descuentoGeneralIvaExportacion);
						// }
						idrs.add(idrIvaExportacion);
					}
				}

				// System.out.println("descuentos recargos: "+idrs.size());
				factura.setInfoDescuentosRecargos(idrs
						.toArray(new InfoDescuentoRecargo[idrs.size()]));

				double totalmontonofacturable = 0;
				if (propina > 0) {
					Producto p = new Producto();
					p.setAgenteResponsable(false);
					p.setCantidad(1);
					p.setDescripcion(properties
							.getProperty("descripcionpropina"));
					p.setNombre(properties.getProperty("nombrepropina"));
					p.setMonto(propina);
					p.setIndicadorFacturacion(6);
					totalmontonofacturable += propina;
					p.setNroLinea(nlinea);
					nlinea++;
					p.setPorcentajeDescuento(0);
					p.setPorcentajeRecargo(0);
					p.setPrecioUnitario(propina);
					p.setUnidadDeMedida("N/A");
					productos.add(p);
				}
				if (cargo > 0) {
					Producto p = new Producto();
					p.setAgenteResponsable(false);
					p.setCantidad(1);
					p.setDescripcion(properties.getProperty("descripcioncargo"));
					p.setNombre(properties.getProperty("nombrecargo"));
					p.setMonto(cargo);
					p.setIndicadorFacturacion(6);
					totalmontonofacturable += cargo;
					p.setNroLinea(nlinea);
					nlinea++;
					p.setPorcentajeDescuento(0);
					p.setPorcentajeRecargo(0);
					p.setPrecioUnitario(cargo);
					p.setUnidadDeMedida("N/A");
					productos.add(p);
				}

				if (properties.getProperty("ivaexportacion").contentEquals("0")) {

				}
				factura.setProductos(productos.toArray(new Producto[productos
						.size()]));
				// true indica iva incluido en las lineas
				factura.setMontoBruto(lineaProductoConIva);
				factura.setMontoNoFacturable(totalmontonofacturable);

				factura.setTasaBasicaIva(tasabasicaiva);
				factura.setTasaMinimaIva(tasaminimaiva);
				if (!properties.getProperty("tipomoneda").equals("UYU")) {
					cotizacion = getTipoCambio(properties
							.getProperty("idformapago"));

					factura.setTipoCambio(cotizacion);
					factura.setTipoMoneda("USD");
				} else {
					factura.setTipoCambio(1);
					factura.setTipoMoneda(properties.getProperty("tipomoneda"));
				}

				factura.setTotalMontoExportacion(totalmontoExportacion);
				factura.setTotalMontoImpuestoPercibido(0);
				factura.setTotalMontoIvaEnSuspenso(totalmontoensuspenso);
				factura.setTotalMontoIvaOtraTasa(totalmontoivaotratasa);
				factura.setTotalMontoIvaTasaBasica(totalmontoivatasabasica);

				System.out.println("iva tasa basica x: "
						+ totalmontoivatasabasica);
				System.out.println(totalmontoivatasabasica);
				System.out.println("iva tasa basica: "
						+ factura.getTotalMontoIvaTasaBasica());
				factura.setTotalMontoIvaTasaMinima(totalmontoivatasaminima);
				factura.setTotalMontoNoGravado(totalmontonogravado);
				factura.setModalidadVenta(0);
				factura.setViaTransporte(0);

			} else if (factura.getTipo() == 181) {

				for (SRProducto srproducto : srproductos) {
					Producto p = new Producto();
					p.setAgenteResponsable(false);
					p.setCantidad(srproducto.getCantidad());
					// SANTI ACA PREGUNTO SI TIENE ALGO EN PLU PARA CARGAR EL
					// CODIGO
					ArrayList<CodigoItem> codigos = new ArrayList<>();
					CodigoItem codigo = new CodigoItem();
					if (!srproducto.getPlu().equalsIgnoreCase("")) {
						codigo.setCodigo(srproducto.getPlu());
						if (properties.getProperty("usaCodigoDeBarra")
								.equalsIgnoreCase("1")) {
							codigo.setTipo("GTN");
						} else if (!properties.getProperty("usaCodigoDeBarra")
								.equalsIgnoreCase("1")) {
							codigo.setCodigo("INT1");
						}
						codigos.add(codigo);
					} else {
						codigo.setCodigo(srproducto.getCodigo());
						codigo.setTipo("SR");
						codigos.add(codigo);
					}
					p.setCodigos(codigos.toArray(new CodigoItem[codigos.size()]));
					p.setDescripcion(srproducto.getNombre());

					p.setNombre(srproducto.getNombre());

					ArrayList<Descuento> descuentos = new ArrayList<>();

					if (srproducto.getDescuento() > 0) {
						Descuento descuentop = new Descuento();
						descuentop.setTipo("%");
						// antes mandava el %, ahora va el valor
						double desc = srproducto.getDescuento()
								* (srproducto.getPrecio() * srproducto
										.getCantidad());
						desc = desc * 100;
						desc = Math.round(desc);
						desc = desc / 100.0;
						descuentop.setValor(desc);
						descuentos.add(descuentop);
						if (srproducto.getDescuento() == 100)
							p.setNombre(p.getNombre()
									+ " "
									+ properties
											.getProperty("textoredescuentolinea100"));

					}
					p.setDescuentos(descuentos.toArray(new Descuento[descuentos
							.size()]));
					// srproducto.getCantidad() lo agergo como absoluto porque
					// en
					// caso
					// de ser negativo se carga en la cantidad arriba
					// double monto =
					// srproducto.getPrecio()*Math.abs(srproducto.getCantidad());
					double monto = srproducto.getPrecio()
							* srproducto.getCantidad();

					monto = monto * (1.00 - srproducto.getDescuento());
					montopostaSinDescuentoGeneral += monto;
					double montoposta = monto * (100 - descuentoGeneral) / 100;

					montoAdendaPosta = montoAdendaPosta + montoposta;

					// MR - El monto del producto es siempre absoluto
					// p.setMonto(Math.abs(monto));;
					p.setMonto(0);
					double montosinimp = 0;
					if (lineaProductoConIva)
						montosinimp = montoposta
								/ (1 + srproducto.getImpuesto());
					else
						montosinimp = montoposta;
					// System.out.println("impuesto: "+srproducto.getImpuesto());
					// if(monto==0){

					p.setNroLinea(nlinea);
					nlinea++;
					p.setPorcentajeDescuento(0);
					p.setPorcentajeRecargo(0);
					p.setPrecioUnitario(0);
					p.setUnidadDeMedida("N/A");
					// if(infor)
					productos.add(p);
				}// cierro for productos

				ad.add("Monto de remito: " + montoAdendaPosta);
				// MR: Aca se ve el tema de los descuentos generales
				if (descuentoGeneral > 0) {
					InfoDescuentoRecargo idrIvaBasico = new InfoDescuentoRecargo();
					InfoDescuentoRecargo idrIvaMinimo = new InfoDescuentoRecargo();
					InfoDescuentoRecargo idrIvaExcento = new InfoDescuentoRecargo();

					if (descuentoGeneral == 100) {
						idrIvaBasico.setGlosa(properties
								.getProperty("textoredescuento100"));
						idrIvaMinimo.setGlosa(properties
								.getProperty("textoredescuento100"));
						idrIvaExcento.setGlosa(properties
								.getProperty("textoredescuento100"));
					} else {
						idrIvaBasico.setGlosa(properties
								.getProperty("textoredescuento")
								+ " "
								+ descuentoGeneral);
						idrIvaMinimo.setGlosa(properties
								.getProperty("textoredescuento")
								+ " "
								+ descuentoGeneral);
						idrIvaExcento.setGlosa(properties
								.getProperty("textoredescuento")
								+ " "
								+ descuentoGeneral);
					}
					idrIvaBasico.setIndicadorFacturacion(3);
					idrIvaMinimo.setIndicadorFacturacion(2);
					idrIvaExcento.setIndicadorFacturacion(1);

					System.out
							.println(" DESCUENTO GENERAL:"
									+ (montopostaSinDescuentoGeneral
											* descuentoGeneral / 100));
					if (descuentoGeneralIvaBasico > 0) {
						idrIvaBasico.setCodigo(1);
						idrIvaBasico.setNroLinea(nlineaDescuento);
						nlineaDescuento++;
						// if(lineaProductoConIva){
						// // MR - Poruqe lo que funciona no se toca
						// idrIvaBasico.setTipoDescuentoRecargo("%");
						// idrIvaBasico.setTipoMovimiento("D");
						// idrIvaBasico.setValor(descuentoGeneral);
						// }else{
						idrIvaBasico.setTipoDescuentoRecargo("$");
						idrIvaBasico.setTipoMovimiento("D");
						System.out.println(" DESCUENTO BASICO:"
								+ descuentoGeneralIvaBasico);
						idrIvaBasico.setValor(descuentoGeneralIvaBasico);
						// }
						idrs.add(idrIvaBasico);
					}
					if (descuentoGeneralIvaMinimo > 0) {
						idrIvaMinimo.setCodigo(2);
						idrIvaMinimo.setNroLinea(nlineaDescuento);
						nlineaDescuento++;
						// if(lineaProductoConIva){
						// // MR - Poruqe lo que funciona no se toca
						// idrIvaMinimo.setTipoDescuentoRecargo("%");
						// idrIvaMinimo.setTipoMovimiento("D");
						// idrIvaMinimo.setValor(descuentoGeneral);
						// }else{
						idrIvaMinimo.setTipoDescuentoRecargo("$");
						idrIvaMinimo.setTipoMovimiento("D");
						System.out.println(" DESCUENTO MINIMO:"
								+ descuentoGeneralIvaMinimo);
						idrIvaMinimo.setValor(descuentoGeneralIvaMinimo);
						// }
						idrs.add(idrIvaMinimo);
					}
					if (descuentoGeneralIvaExcento > 0) {
						idrIvaExcento.setCodigo(3);
						idrIvaExcento.setNroLinea(nlineaDescuento);
						nlineaDescuento++;
						// if(lineaProductoConIva){
						// // MR - Poruqe lo que funciona no se toca
						// idrIvaExcento.setTipoDescuentoRecargo("%");
						// idrIvaExcento.setTipoMovimiento("D");
						// idrIvaExcento.setValor(descuentoGeneral);
						// }else{
						idrIvaExcento.setTipoDescuentoRecargo("$");
						idrIvaExcento.setTipoMovimiento("D");
						System.out.println(" DESCUENTO EXCENTO:"
								+ descuentoGeneralIvaExcento);
						idrIvaExcento.setValor(descuentoGeneralIvaExcento);
						// }
						idrs.add(idrIvaExcento);
					}
				}

				// System.out.println("descuentos recargos: "+idrs.size());
				factura.setInfoDescuentosRecargos(idrs
						.toArray(new InfoDescuentoRecargo[idrs.size()]));

				double totalmontonofacturable = 0;
				if (propina > 0) {
					Producto p = new Producto();
					p.setAgenteResponsable(false);
					p.setCantidad(1);
					p.setDescripcion(properties
							.getProperty("descripcionpropina"));
					p.setNombre(properties.getProperty("nombrepropina"));
					p.setMonto(propina);
					p.setIndicadorFacturacion(6);
					totalmontonofacturable += propina;
					p.setNroLinea(nlinea);
					nlinea++;
					p.setPorcentajeDescuento(0);
					p.setPorcentajeRecargo(0);
					p.setPrecioUnitario(propina);
					p.setUnidadDeMedida("N/A");
					productos.add(p);
				}
				if (cargo > 0) {
					Producto p = new Producto();
					p.setAgenteResponsable(false);
					p.setCantidad(1);
					p.setDescripcion(properties.getProperty("descripcioncargo"));
					p.setNombre(properties.getProperty("nombrecargo"));
					p.setMonto(cargo);
					p.setIndicadorFacturacion(6);
					totalmontonofacturable += cargo;
					p.setNroLinea(nlinea);
					nlinea++;
					p.setPorcentajeDescuento(0);
					p.setPorcentajeRecargo(0);
					p.setPrecioUnitario(0);
					p.setUnidadDeMedida("N/A");
					productos.add(p);
				}

				factura.setProductos(productos.toArray(new Producto[productos
						.size()]));
				// true indica iva incluido en las lineas
				factura.setMontoBruto(true);
				factura.setMontoNoFacturable(0);

				factura.setTasaBasicaIva(tasabasicaiva);
				factura.setTasaMinimaIva(tasaminimaiva);
				if (!properties.getProperty("tipomoneda").equals("UYU"))
					factura.setTipoCambio(this.getTipoCambio(properties
							.getProperty("idformapago")));
				else
					factura.setTipoCambio(1);
				factura.setTipoMoneda(properties.getProperty("tipomoneda"));
				factura.setTotalMontoExportacion(0);
				factura.setTotalMontoImpuestoPercibido(0);
				factura.setTotalMontoIvaEnSuspenso(0);
				factura.setTotalMontoIvaOtraTasa(0);
				factura.setTotalMontoIvaTasaBasica(0);
				factura.setTipoTraslado(1);

				System.out.println("iva tasa basica x: "
						+ totalmontoivatasabasica);
				System.out.println(totalmontoivatasabasica);
				System.out.println("iva tasa basica: "
						+ factura.getTotalMontoIvaTasaBasica());
				factura.setTotalMontoIvaTasaMinima(0);
				factura.setTotalMontoNoGravado(0);
			}

			return factura;
		} catch (Exception e) {
			//System.out.println(e.);
			System.out.println("Error en cargar lineas " + e.getStackTrace()[0].getLineNumber());
			throw new Exception(e.getMessage());
		}
	}

	private void obtenerUltimaCotizacion() {
		// TODO Auto-generated method stub

	}

	private Factura getFactura(int id, String prefijo, boolean ocultardetalle,
			String nrocompra) throws Throwable {
		Properties properties = Direccionador.getInstance()
				.getArchivoConfiguracion();

		double tasabasicaiva = 22.0;
		double tasaminimaiva = 10.0;
		boolean esNotadeCreditoXNegativo = false;
		boolean esFacturaAsimilada = false;

		try {
			DBDriver bd = DBDriver.getInstance();
			bd.conectar();
			// Consulta que toma los totales de SR
			String consulta = "select c.cargo,c.descuento,c.idcliente,c.subtotal,c.totalimpuesto1,"
					+ "c.mesa,m.nombre as mesero,c.nopersonas,seriefolio,orden,cierre,cambio,propinaincluida, "
					+ "total,observaciones,ISNULL(c.descuentomonedero,0) AS descuentomonedero,c.descuentoimporte  "
					+ "from "
					+ prefijo
					+ "cheques c "
					+ "left join meseros m on m.idmesero=c.idmesero "
					+ "where c.folio=?";
			PreparedStatement ps = bd.prepareStatement(consulta);
			ps.setInt(1, id);

			if (TEST) {
				System.out.println(consulta + "::" + id);
			}
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				throw new Exception("No existe la factura " + id);
			}
			// Pregunto si es una Nota de Credito por total en negativo
			if (rs.getDouble("total") < 0) {
				esNotadeCreditoXNegativo = true;
			}
			String mesero = rs.getString("mesero");
			String mesa = rs.getString("mesa");
			String nropersonas = rs.getString("nopersonas");
			double cargo = Math.abs(rs.getDouble("cargo"));

			// Aca preparo el Descuento
			double descuentoGeneral = Math.abs(rs.getDouble(2));
			double descuentoMonedero = Math.abs(rs
					.getDouble("descuentomonedero"));
			double descuentoGeneralImporte = Math.abs(rs
					.getDouble("descuentoimporte"));
			double subtotal1 = Math.abs(rs.getDouble("subtotal"));
			double subtotal2 = Math.abs(rs.getDouble("totalimpuesto1"));

			String idclientesoft = rs.getString("idcliente");
			System.out.println("id cliente: " + idclientesoft);
			// montoAdenda = subtotal1 + subtotal2-descuentoGeneral;

			System.out.println("descuentoGeneral con Monedero "
					+ descuentoMonedero);
			// Sumo al descuento general el descuento momedero
			if (descuentoMonedero > 0) {
				descuentoGeneral = ((descuentoGeneralImporte + descuentoMonedero) * 100)
						/ subtotal1;
				System.out.println("descuentoGeneral con Monedero "
						+ descuentoGeneral);
			}

			double descuentoGeneralIvaBasico = 0;
			double descuentoGeneralIvaMinimo = 0;
			double descuentoGeneralIvaExcento = 0;
			double descuentoGeneralIvaExportacion = 0;
			String idclientefacturacion = idclientesoft;
			/*
			 * if(idclientefacturacion.contains("       ")){
			 * System.out.println("Sin id de cliente"); idclientefacturacion="";
			 * }
			 */

			String serieorden = rs.getString("seriefolio");
			String orden = rs.getString("orden");
			double propina = Math.abs(rs.getDouble("propinaincluida"));
			Date fecha = rs.getDate("cierre");
			Time fechaHora = rs.getTime("cierre");
			Calendar fechacmp = GregorianCalendar.getInstance();
			if (fecha != null) {
				fechacmp.setTimeInMillis(fecha.getTime());
			}
			Double cambio = Math.abs(rs.getDouble("cambio"));
			Double propinasugerida = Math.abs(Double.parseDouble(properties
					.getProperty("propinasugerida")));
			Double totalmaspropina = Math.abs(Math.round(100.0
					* (rs.getDouble("total")) * (1 + propinasugerida)) / 100.0);
			Double totalpropinacargo = Math.abs(rs.getDouble("total") + cargo
					+ propina);
			String observaciones = rs.getString("observaciones");

			rs.close();
			ps.close();

			// double totalmontoivaotratasa =0;
			double totalmontoivatasabasica = 0;
			double totalmontoivatasaminima = 0;
			double totalmontonogravado = 0;
			double totalmontoensuspenso = 0;
			double totalmontoExportacion = 0;
			Factura factura = new Factura();

			if (factura.getReceptor() != null
					&& factura.getReceptor().getDocumento() != null
					&& (factura.getReceptor().getDocumento().getDocumento() == null || factura
							.getReceptor().getDocumento().getDocumento().trim()
							.isEmpty())) {

				factura.setReceptor(null);
				if (esNotadeCreditoXNegativo) {
					factura.setTipo(102);
					factura = cancelarFacturaXNotaCredito(factura);
				} else {
					factura.setTipo(101);
				}

			} else {
				if (esNotadeCreditoXNegativo) {
					factura.setTipo(102);
					factura = cancelarFacturaXNotaCredito(factura);
				} else {
					factura.setTipo(101);
				}
			}

			ad = new ArrayList<>();
			String ade = properties.getProperty("adenda1");
			if (ade != null && !ade.equals("")) {
				ad.add(ade);
			}
			ade = properties.getProperty("adenda2");
			if (ade != null && !ade.equals("")) {
				ad.add(ade);
			}

			String usuarioApertura = buscarAperturaTurno();
			// MR - Saque la adenda 3 para utilizar ese lugar en imprimir el
			// nombre
			// del equipo
			ad.add("Usuario de cuenta: " + usuarioApertura);
			ad.add("Nombre de equipo: " + buscoNombrePc());
			ad.add("Mesero: " + mesero);
			ad.add("Mesa: " + mesa);
			ad.add("Cantidad personas: " + nropersonas);
			ad.add(nrocompra);

			if (properties.getProperty("adendahora").equalsIgnoreCase("1")) {
				System.out.println(fechaHora.toString());
				int hora = fechaHora.getHours();
				int minutos = fechaHora.getMinutes();
				int segundos = fechaHora.getSeconds();

				ad.add("HORA: " + hora + ":" + minutos + ":" + segundos);
				System.out.println("HORA: " + hora + ":" + minutos + ":"
						+ segundos);

			} else {
				Date dia = Calendar.getInstance().getTime();

				int hora = dia.getHours();
				int minutos = dia.getMinutes();
				int segundos = dia.getSeconds();
				ad.add("HORA: " + hora + ":" + minutos + ":" + segundos);
				System.out.println("HORA: " + hora + ":" + minutos + ":"
						+ segundos);
			}

			ad.add(serieorden + " " + orden);
			System.out.println(serieorden + " " + orden);
			String textocambio = properties.getProperty("textocambio");
			ad.add(textocambio + cambio);

			factura.setFechaVencimiento(fechacmp);
			factura.setFechaComprobante(fechacmp);

			int formapago = 1;
			String fp = null;
			consulta = "select f.descripcion from "
					+ prefijo
					+ "chequespagos c "
					+ "join formasdepago f on c.idFormaDePago=f.idformadepago where c.folio=?";
			ps = bd.prepareStatement(consulta);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (fp == null) {
					fp = rs.getString("descripcion");
				} else {
					fp += ", " + rs.getString("descripcion");
				}
			}
			rs.close();
			ps.close();
			if (fp != null) {
				ad.add(properties.getProperty("textoformapago") + fp);
			} else {
				ad.add("");
			}

			// SB - ESTABA ACA

			// MR - Consulto si el Folio tiene formas de pago asigandas
			consulta = "select count(*) cant from " + prefijo
					+ "chequespagos where folio=?";
			ps = bd.prepareStatement(consulta);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			rs.next();
			if (rs.getInt("cant") >= 1) {
				rs.close();
				ps.close();
				// MR - Consulto si alguna de las formas de pago es CREDITO
				consulta = "select descripcion from "
						+ prefijo
						+ "chequespagos c "
						+ "join formasdepago f on c.idFormaDePago=f.idformadepago where c.folio=?";
				ps = bd.prepareStatement(consulta);
				ps.setInt(1, id);
				rs = ps.executeQuery();
				while (rs.next()) {
					String descformapago = rs.getString("descripcion");
					if (descformapago.toUpperCase().contains("CREDITO")) {
						formapago = 2;
					}
					if (descformapago.toUpperCase().contains("REMITO")) {
						formapago = 1;
						factura.setTipo(181);
						factura.setTipoTraslado(1);

					}
				}
				rs.close();
				ps.close();

			} else {
				rs.close();
				ps.close();
			}

			// Si ninguna de las formas de pago es CREDITO pregunto si el
			// cliente se
			// factura como CREDITO O ASIMILADA
			if (formapago != 2) {
				consulta = "select c.giro from tempcheques t"
						+ " join clientes c on c.idcliente=t.idcliente "
						+ "where folio=?";
				ps = bd.prepareStatement(consulta);
				ps.setInt(1, id);
				rs = ps.executeQuery();
				if (rs.next()) {
					String giro = rs.getString("giro");
					if (giro != null) {
						giro = giro.trim().toUpperCase();
						if (giro.equals("CREDITO") || giro.equals("CR")
								|| giro.equals("C")) {
							formapago = 2;
						} else if (giro.equals("ASIMILADA")
								|| giro.equals("ASIMILADO")
								|| giro.equals("AS")) {
							esFacturaAsimilada = true;
						} else if (giro.equals("REMITO")
								|| giro.equals("REMITO") || giro.equals("RE")) {
							formapago = 1;
							factura.setTipo(181);
							factura.setTipoTraslado(1);
							// ad.add("Monto de remito: "+montoAdenda);
						}
					}
				}
				rs.close();
				ps.close();
			}

			factura.setFormaPago(formapago);

			int nlinea = 1;
			int nlineaDescuento = 1;

			// COMIENZO PRODUCTOS
			int indicadorfacturaciondescuento = -1;
			factura = cargarLineas(tasabasicaiva, totalmontoivatasabasica,
					descuentoGeneral, tasaminimaiva, totalmontoivatasaminima,
					totalmontonogravado, totalmontoensuspenso, nlinea, prefijo,
					consulta, id, ps, rs, bd, esNotadeCreditoXNegativo,
					ocultardetalle, properties, indicadorfacturaciondescuento,
					factura, propina, cargo, totalmontonogravado,
					descuentoGeneralIvaBasico, descuentoGeneralIvaMinimo,
					descuentoGeneralIvaExcento,descuentoGeneralIvaExcento, nlineaDescuento,
					esFacturaAsimilada, totalmontoExportacion, serieorden);

			// FIN PRODUCTOS
			factura.setDestino(null);
			// if (!hayProductosPdu) {
			factura.setNroCompra(nrocompra);
			// }
			Persona persona = null;
			String referencia = "";
			if (idclientefacturacion != null
					&& !idclientefacturacion.trim().equals("")) {
				System.out.println("TIENE CLIENTE");
				persona = new Persona();
				consulta = "SELECT c.estado,c.codigopostal,c.rfc,c.nombre,c.pais, d.calle,d.numeroexterior,d.numerointerior,"
						+ "d.cruzamiento1,d.cruzamiento2, d.referencia,c.telefono1,t.telefonousadodomicilio,c.direccion,c.curp,c.telefono5,c.telefono4,c.idcliente "
						+ "FROM "
						+ prefijo
						+ "cheques t LEFT "
						+ "JOIN clientes c ON t.idcliente LIKE c.idcliente "
						+ "LEFT JOIN direccionesdomicilio d on d.idCliente LIKE t.idCliente  AND t.iddireccion LIKE d.iddireccion "
						+ "WHERE t.folio =?";
				ps = bd.prepareStatement(consulta);
				ps.setInt(1, id);
				rs = ps.executeQuery();
				if (TEST) {
					System.out.println(consulta + "::" + id);
				}
				rs.next();

				System.out.println("ok 1");

				nombreCliente = rs.getString(4);

				if (nombreCliente != null) {
					if (nombreCliente.contains("EFACTURA -")) {
						nombreCliente = nombreCliente.replaceAll("EFACTURA -",
								"");
					} else if (nombreCliente.contains("EREMITO -")) {
						nombreCliente = nombreCliente.replaceAll("EREMITO -",
								"");
					} else if (nombreCliente.contains("EXPORTACION -")) {
						nombreCliente = nombreCliente.replaceAll(
								"EXPORTACION -", "");
					}
				}
				String telefono = rs.getString("telefono1");
				if (telefono != null && !telefono.trim().equals(""))
					ad.add(properties.getProperty("textotelefono") + telefono);
				else {
					telefono = rs.getString("telefonousadodomicilio");
					if (telefono != null && !telefono.trim().equals(""))
						ad.add(properties.getProperty("textotelefono")
								+ telefono);
					else
						ad.add("");
				}
				// System.out.println("TELEFONO: "+telefono);
				String direccion = rs.getString("calle") + " "
						+ rs.getString("numeroexterior");

				String usaproductosplu = properties
						.getProperty("usaproductosplu");

				if (hayProductosPdu == true
						&& usaproductosplu.equalsIgnoreCase("1")) {
					System.out.println("HAY PRODUCTOS CON PDU");
					factura.setDestino(rs.getString("telefono5"));
					factura.setNroCompra(rs.getString("telefono4"));
					System.out.println("NRO COMPRA " + factura.getNroCompra());
				}
				// }
				if (rs.getString("calle") == null
						|| rs.getString(6).equals("calle"))
					direccion = rs.getString("direccion");
				else {
					if (rs.getString("numerointerior") != null
							&& !rs.getString("numerointerior").trim()
									.equals("")) {
						direccion += " apt. " + rs.getString("numerointerior");
					}
					if (rs.getString("cruzamiento1") != null
							&& !rs.getString("cruzamiento1").trim().equals("")) {
						if (rs.getString("cruzamiento2") != null
								&& !rs.getString("cruzamiento2").trim()
										.equals("")) {
							direccion += " entre "
									+ rs.getString("cruzamiento1") + " y "
									+ rs.getString("cruzamiento2");
						} else {
							direccion += " esq. "
									+ rs.getString("cruzamiento1");
						}
					}
					System.out.println(direccion);
					if (direccion.length() >= 70) {
						referencia = direccion
								.substring(70, direccion.length());
						referencia = referencia + " ** Ref:";
						direccion = direccion.substring(0, 70);
					}
					if (rs.getString("referencia") != null
							&& !rs.getString("referencia").trim().equals("")) {

						referencia = referencia + rs.getString("referencia");

					}
				}
				// La direcciï¿½n es un compuesto de campos entre CALLE,NUMERO
				// EXTERIOR, NUMERO INTERIOR, CRUZAMIENTO 1 , CRUZAMIENTO 2 y
				// puede
				// llegar a tener una referencia que se imprime separado en otra
				// linea

				// Ej> 21 de setiembre 2733 apt.104 entre williman y jaime
				// zudanez.
				// REF> no me toques el timbre

				Documento documento = new Documento();
				//ACA CAMBIE SANTI 02/10 IF DOCUMENTO NO ES VACIO LO SETEO
				/*if(!rs.getString("rfc").trim().equalsIgnoreCase("")){
					
				}*/
				documento.setDocumento(rs.getString("rfc"));
				
				String curp2 = "";
				String curp = rs.getString("curp");
				// SANTI MODIFICA
				if (rs.getString("rfc") == null && rs.getString("curp") == null
						&& factura.getTipo() != 181) {
					persona = null;
				} else {

					if (rs.getString("rfc") == null
							&& rs.getString("curp") == null
							&& factura.getTipo() == 181) {
						curp2 = "22222222";
						if (documento.getTipo() != 2 && curp2 != null
								&& !curp2.isEmpty()) {
							if (curp2.length() == 8) {
								documento.setTipo(3);
								documento.setDocumento(curp2);
								persona.setDocumento(documento);
								persona.setNombre(nombreCliente);
								documento.setPais("UY");
							} else {
								documento.setTipo(4);
							}
							documento.setDocumento(curp2);
							if (factura.getTipo() != 181) {
								factura.setTipo(101);
							}
						}
					} else {
						//ACA CAMBIE SANTI 02/10/24 CURP.TRIM
						if (rs.getString("curp") == null
								|| rs.getString("curp").trim().equalsIgnoreCase("")
								&& rs.getString("rfc") != null) {
							System.out.println("RFC: " + rs.getString("rfc"));
							if (rs.getString("rfc").length() > 8) {
								// Preguntar si el total es negativo tiene que
								// ser 112
								if (esNotadeCreditoXNegativo) {
									factura.setTipo(112);
									factura = cancelarFacturaXNotaCredito(factura);
									documento.setTipo(2);
								} else if (factura.getTipo() != 181) {
									factura.setTipo(111);
									documento.setTipo(2);
								}
								documento.setTipo(2);
							} else {
								documento.setTipo(3);
							}
						}

						else if (!rs.getString("curp").trim().isEmpty()) {
							curp = rs.getString("curp");
							if (documento.getTipo() != 2 && curp != null
									&& !curp.isEmpty()) {
								if (curp.length() == 8) {
									documento.setTipo(3);
								} else {
									documento.setTipo(4);
								}
								documento.setDocumento(curp);
								if (factura.getTipo() != 181) {
									factura.setTipo(101);
								}
							}
						}
						String ciudad = rs.getString("estado");
						if (ciudad != null && !ciudad.contentEquals("")
								&& !ciudad.trim().isEmpty()) {
							persona.setCiudad(ciudad);
						} else {
							ciudad = "Ciudad";
							persona.setCiudad(ciudad);
						}

						persona.setDepartamento(ciudad);
						if (direccion != null && !direccion.contentEquals("")
								&& !direccion.trim().isEmpty()) {
							if (direccion.length() > 65) {
								direccion = direccion.substring(0, 65);
							}
							persona.setDireccion(direccion);
						} else {
							direccion = "Direccion";
							persona.setDireccion(direccion);
						}
						// System.out.println("Ver el tema del pais del documento");
						// TODO: ver esto con marcel
						if (rs.getString("curp").trim().isEmpty()) {
							documento.setPais(rs.getString("pais"));
						} else if (!rs.getString("curp").trim().isEmpty()) {
							documento.setPais(rs.getString("pais"));
						}

						if (documento.getDocumento().length() > 0) {
							persona.setDocumento(documento);
						}
						persona.setNombre(nombreCliente);
						persona.setPais(rs.getString("pais"));

						if (!persona.getPais().equalsIgnoreCase("UY")
								&& !persona.getPais().equalsIgnoreCase(
										"Uruguay")) {
							if(factura.getTipo()==111){
								throw new Exception("Verifique el pais del cliente");
							}else{
								documento.setTipo(4);
							}
							
						}

						if (persona.getPais().trim().isEmpty()
								|| persona.getPais()
										.equalsIgnoreCase("Uruguay")) {
							persona.setPais("UY");
							documento.setPais(persona.getPais());

						}
					}
				}
			} else {
				ad.add("");
			}

			System.out.println("ok 2");
			
			//aca cambie santi 02/10 no debe ir persona
			if(persona!=null 
					&& persona.getDocumento()!=null){
				factura.setReceptor(persona);
			}

			ad.add(referencia);

			// total + propina
			ad.add((Math.round(totalmaspropina * 100.0) / 100.00) + "");
			// Agrego las Observaciones de la cuenta al final para no afectar el
			// orden
			ad.add(observaciones);

			// total + propina en cada moneda
			String idformaspago = properties.getProperty("idformaspago");
			if (idformaspago != null && !idformaspago.trim().isEmpty()) {
				idformaspago = idformaspago.trim();
				String monedas[] = properties.getProperty("idformaspago")
						.split(";");
				consulta = "select descripcion,tipodecambio from formasdepago where idformadepago=?";
				ps = bd.prepareStatement(consulta);
				for (String moneda : monedas) {
					ps.setString(1, moneda);
					if (TEST) {
						System.out.println(consulta + "::" + moneda);
					}
					rs = ps.executeQuery();
					/*if (!rs.next()) {
						throw new Exception(
								"No se encontro forma de pago con id " + moneda);
					}*/
					while(rs.next()){
						Double tc = rs.getDouble("tipodecambio");
						ad.add(rs.getString("descripcion")+": "+(Math.round((totalmaspropina/tc)*100.00)/100.00)+"");
						//ad.add(rs.getString("descripcion")+": "+(Math.round((totalpropinacargo / tc) * 100.0) / 100.00)+"");
					}

					rs.close();
				}
				ps.close();
			}

	
			factura.setAdenda(ad.toArray(new String[ad.size()]));

			System.out.println("ok 3");
			ArrayList<Subtotal> subtotales = new ArrayList<>();
			Subtotal st1 = new Subtotal();
			st1.setNumero(1);
			st1.setOrden(1);
			st1.setValor(subtotal1);
			st1.setTitulo(properties.getProperty("textosubtotal1"));
			subtotales.add(st1);
			Subtotal st2 = new Subtotal();
			st2.setNumero(2);
			st2.setOrden(2);
			st2.setValor(subtotal2);
			st2.setTitulo(properties.getProperty("textosubtotal2"));
			subtotales.add(st2);
			factura.setSubtotales(subtotales.toArray(new Subtotal[subtotales
					.size()]));

			// TEMA UI DOLARES Y PESOS
			double sumaTotales = subtotal1 + subtotal2;
			double ui = Double.parseDouble(properties
					.getProperty("unidadindexada"));
			if (factura.getTipoMoneda().equalsIgnoreCase("USD")) {

				sumaTotales = sumaTotales * cotizacion;
			}
			double uiTotales = sumaTotales / ui;
			excedeUi = false;
			if (uiTotales > 5000) {
				excedeUi = true;
				// ACA PEDIMOS RECEPTOR
			}

			if (excedeUi == true && persona == null) {
				// ENTRO A UI TOTAL
				JOptionPane
						.showMessageDialog(
								null,
								"LA FACTURA ES MAYOR A 10MIL UI, DEBE LLEVAR UN CLIENTE. DEBE ANULAR EL FOLIO Y VOLVER A FACTURAR CON DICHO AJUSTE",
								"NO SE GENERO LA FACTURA",
								JOptionPane.WARNING_MESSAGE);
				log.log(Level.SEVERE,
						"POR EL MONTO DE LA FACTURA, DEBE LLEVAR UN CLIENTE");
				System.out
						.println("POR EL MONTO DE LA FACTURA, DEBE LLEVAR UN CLIENTE");
				System.exit(0);
			}

			if (factura.getTipo() == 181) {
				indicadorfacturaciondescuento = 1;
			}

			ps.close();
			bd.desconectar();
			System.out.println("ok 4");
			return factura;

		} catch (Exception e) {
			JOptionPane
			.showMessageDialog(
					null,
					"ERROR: "+e.getMessage(),
					"NO SE GENERO LA FACTURA",
					JOptionPane.WARNING_MESSAGE);
	log.log(Level.SEVERE,
			"ERROR: "+e.getMessage());
	System.out
			.println("ERROR: "+e.getMessage());
	System.exit(0);
			System.out.println("Error " + e.getStackTrace()[0].getLineNumber());
		}
		return null;
	}

	private String buscoNombrePc() {
		String resultado = "";
		try {
			resultado = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
		}
		return resultado;
	}

	private Double getTipoCambio(String moneda) throws Exception {
		DBDriver bd = DBDriver.getInstance();
		bd.conectar();
		String consulta = "select tipodecambio from formasdepago where idformadepago=?";
		PreparedStatement ps = bd.prepareStatement(consulta);
		ps.setString(1, moneda);
		if (TEST) {
			System.out.println(consulta + "::" + moneda);
		}

		ResultSet rs = ps.executeQuery();
		if (!rs.next()) {
			throw new Exception("No se encontro tipo de cambio para la moneda "
					+ moneda);
		}
		Double salida = rs.getDouble(1);
		rs.close();
		ps.close();
		bd.desconectar();
		return salida;
	}

	public String buscarUsuarioApertura(String nrocompra) throws Exception {
		DBDriver bd = DBDriver.getInstance();
		bd.conectar();
		String serie = nrocompra.substring(0, 1);
		int numero = Integer.parseInt(nrocompra.substring(1));
		ArrayList<Object> id = getId(serie, numero);
		// idBusqueda= id.get(1);
		// int nrocomprabusqueda=Integer.parseInt(nrocompra.substring(1));
		String consulta = "select c.usuarioapertura from cheques c where c.folio=?";
		PreparedStatement ps = bd.prepareStatement(consulta);
		ps.setInt(1, idBusqueda);
		ResultSet rs = ps.executeQuery();
		String usuario = null;
		if (rs.next()) {
			usuario = rs.getString(1);
			// ps.execute();
			ps.close();
		}

		bd.desconectar();
		return usuario;
	}

	public String buscarAperturaTurno() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date today = Calendar.getInstance().getTime();
		Calendar cinicio = Calendar.getInstance();
		cinicio.setTime(today);
		cinicio.set(Calendar.HOUR_OF_DAY, 0);
		cinicio.set(Calendar.MINUTE, 0);
		cinicio.set(Calendar.SECOND, 0);
		today = cinicio.getTime();
		String turnoInicio = formatter.format(today);
		Date dt = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.DATE, 1);
		c.set(Calendar.HOUR_OF_DAY, 3);
		c.set(Calendar.MINUTE, 0);
		dt = c.getTime();
		String turnoFin = formatter.format(dt);
		DBDriver bd = DBDriver.getInstance();
		bd.conectar();
		String consulta = "select cajero from turnos t WHERE t.apertura BETWEEN ? AND ?";
		PreparedStatement ps = bd.prepareStatement(consulta);
		ps.setString(1, turnoInicio);
		ps.setString(2, turnoFin);
		ResultSet rs = ps.executeQuery();
		String usuario = null;
		if (rs.next()) {
			usuario = rs.getString(1);
		}
		return usuario;

	}

	public String buscarUsuarioCierre(String nrocompra) throws Exception {
		DBDriver bd = DBDriver.getInstance();
		bd.conectar();
		String serie = nrocompra.substring(0, 1);
		int numero = Integer.parseInt(nrocompra.substring(1));
		ArrayList<Object> id = getId(serie, numero);
		// idBusqueda= id.get(1);
		// int nrocomprabusqueda=Integer.parseInt(nrocompra.substring(1));
		String consulta = "select c.usuarioapertura from cheques c where c.folio=?";
		PreparedStatement ps = bd.prepareStatement(consulta);
		ps.setInt(1, idBusqueda);
		ResultSet rs = ps.executeQuery();
		String usuario = null;
		if (rs.next()) {
			usuario = rs.getString(1);
			// ps.execute();
			ps.close();
		}

		bd.desconectar();
		return usuario;
	}

	public void guardarDatosReceptor(int id, Factura factura, String prefijo)
			throws Exception {
		// Properties properties =
		// Direccionador.getInstance().getArchivoConfiguracion();

		DBDriver bd = DBDriver.getInstance();
		bd.conectar();
		String consulta = "select c.idcliente " + "from " + prefijo
				+ "cheques c " + "where c.folio=?";
		PreparedStatement ps = bd.prepareStatement(consulta);
		ps.setInt(1, id);
		if (TEST) {
			System.out.println(consulta + "::" + id);
		}
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			String idcliente = rs.getString("idcliente");
			rs.close();
			ps.close();
			consulta = "update clientes "
					+ "set estado=?, poblacion=?, rfc=?, nombre=?, pais=? "
					+ "where idcliente=?";
			ps = bd.prepareStatement(consulta);
			String departamento = factura.getReceptor().getDepartamento();
			String documento = factura.getReceptor().getDocumento()
					.getDocumento();
			String nombre = factura.getReceptor().getNombre();
			String pais = factura.getReceptor().getPais();
			ps.setString(1, departamento);
			ps.setString(2, departamento);
			ps.setString(3, documento);
			ps.setString(4, nombre);
			ps.setString(5, pais);
			ps.setString(6, idcliente);

			if (TEST) {
				System.out.println(consulta + "::" + departamento + "-"
						+ departamento + "-" + documento + "-" + nombre + "-"
						+ pais + "-" + idcliente);
			}
			ps.execute();
			ps.close();
		} else {
			System.out.println("[ERROR] No existe la factura " + id);
		}

		bd.desconectar();
	}

	public Respuesta generarFactura(int id, String nrocompra, String prefijo,
			boolean imprimir) throws Throwable {
		interfaz = new respaldo_OfInterfazSRFL();
		Properties propiedades = Direccionador.getInstance()
				.getArchivoConfiguracion();
		FacturaElectronicaImplServiceStub_old fstub = new FacturaElectronicaImplServiceStub_old(
				propiedades.getProperty("endpoint"));
		FacturaElectronicaImplServiceStub_old fstub2 = new FacturaElectronicaImplServiceStub_old(
				propiedades.getProperty("endpoint"));
		int ocultardetalle = Integer.parseInt(propiedades
				.getProperty("ocultardetalle"));
		System.out.println("OCULTAR DETALLE " + ocultardetalle);
		GenerarFactura gf = new GenerarFactura();
		factura = interfaz.getFactura(id, prefijo, ocultardetalle == 1,
				nrocompra);

		// COMENTARIO POP UP RECEPTOR
		/*
		 * String error = Validador.validar(factura); while(error!=null){
		 * Receptor receptor = new Receptor(factura,error,this);
		 * receptor.setVisible(true); if(this.cancelado){ Respuesta r = new
		 * Respuesta(); r.setCodigo(-1);
		 * r.setDescripcion("Cancelado por el usuario."); return r; } //
		 * if(factura.getReceptor()!=null // &&
		 * factura.getReceptor().getDocumento()!=null // &&
		 * (factura.getReceptor().getDocumento().getDocumento()==null // || //
		 * factura
		 * .getReceptor().getDocumento().getDocumento().trim().isEmpty())){ //
		 * factura.setReceptor(null); // factura.setTipo(101); // } error =
		 * Validador.validar(factura); if(error==null &&
		 * factura.getReceptor()!=null &&
		 * factura.getReceptor().getDocumento()!=null){
		 * this.guardarDatosReceptor(id,factura,prefijo); } }
		 */
		System.out.println("Factura nro: " + factura.getNroCompra());
		gf.setArg0(factura);
		gf.setArg1(propiedades.getProperty("impresora"));
		gf.setArg1("");
		boolean seguir = true;
		servicios.FacturaElectronicaImplServiceStub_old.GenerarFacturaResponse res = null;
		while (seguir) {
			seguir = false;

			res = fstub.generarFactura(gf);
			codigoRes = res.get_return().getCodigo();
			if (excedeUi == true) {
				codigoRes = 98;
			}
			if (res.get_return().getCodigo() == 0
					&& res.get_return().getDescripcion()
							.equalsIgnoreCase("Exito")) {
				if (factura.getTipo() == 181) {
					insertarRemito();
				}

				if (propiedades.getProperty("isa") != null) {
					if (propiedades.getProperty("isa").equalsIgnoreCase("1")) {
						String nombrearchivo=propiedades.getProperty("rutaxmlisa");
						String nombrezip=propiedades.getProperty("rutazipisa");
						String serie = res.get_return().getDocumento()
								.getSerie();
						int numero = res.get_return().getDocumento()
								.getNumero();
						int tipodocumento = res.get_return().getTipoCFE();
						imprimir = false;
						guardarXml(serie, numero, tipodocumento, nombrearchivo,nombrezip);
					}
				}

				Respuesta r = new Respuesta();
				if (imprimir) {
					r = interfaz.mandarImprimir(res.get_return().getDocumento()
							.getSerie(), res.get_return().getDocumento()
							.getNumero(), res.get_return().getTipoCFE());

					if (r.getCodigo() > 0) {
						return r;
					}
				} else {
					return r;
				}

			} else {
				log.log(Level.SEVERE, res.get_return().getDescripcion());
				
				//JOptionPane.showMessageDialog(null, res.get_return()
				//		.getDescripcion(), "Factura Electronica", 1);
				//System.exit(0);
				/*
				 * receptor = new Receptor(factura, res.get_return()
				 * .getDescripcion(),interfaz);
				 */
				// receptor.setVisible(false);
				switch (codigoRes) {
				case 22:
					msjRes = "Documento - Tipo de documento debe ser RUT para eFactura";
					mostrarError(msjRes);
					break;
				case 49:
					msjRes = "Validar el codigo del pais enviado debe ser de dos caracteres, ejemplo: UY";
					mostrarError(msjRes);
					break;
				case 69:
					msjRes = "El comprobante no tiene lineas de productos";
					mostrarError(msjRes);
					break;
				case 74:
					msjRes = "El monto excede el permitido sin un receptor,deben ingresar el documento de la persona";
					mostrarError(msjRes);
					break;
				case 76:
					msjRes = "RUT INVALIDO";
					mostrarError(msjRes);
					break;
				case 94:
					msjRes = "RUT INVALIDO";
					mostrarError(msjRes);
					//guardarDatosReceptor(id, factura, prefijo);
					break;
				case 90:
					msjRes = "La dirección no puede superar los 70 caracteres";
					mostrarError(msjRes);
					break;
				case 96:
					msjRes = "RUT o CEDULA INCORRECTO";
					mostrarError(msjRes);
					//guardarDatosReceptor(id, factura, prefijo);
					break;
				case 99:
					msjRes = "Error de conexión - COD: 99"
							+ res.get_return().getDescripcion();
					mostrarError(msjRes);
					break;
				case 98:
					break;
				case 1013:
					msjRes = "ERROR EN EL CODIGO DE PAÍS";
					mostrarError(msjRes);
					break;
				case 1024:
					msjRes = "NO QUEDA RANGO DISPONIBLE";
					mostrarError(msjRes);
					break;

				default:
					msjRes = res.get_return().getDescripcion();
					mostrarError(msjRes);
					break;
				}
				ArrayList<Integer> codigosEsperados = new ArrayList<Integer>();
				codigosEsperados.add(21);
				codigosEsperados.add(22);
				codigosEsperados.add(23);
				codigosEsperados.add(25);
				codigosEsperados.add(26);
				codigosEsperados.add(49);
				codigosEsperados.add(64);
				codigosEsperados.add(65);
				codigosEsperados.add(66);
				codigosEsperados.add(67);
				codigosEsperados.add(74);
				codigosEsperados.add(75);
				codigosEsperados.add(90);
				codigosEsperados.add(91);
				codigosEsperados.add(94);
				codigosEsperados.add(95);
				if (codigosEsperados.contains(res.get_return().getCodigo())) {
					// ACA COMENTE ACTUALIZACION 1.0
					seguir = true;
					/*
					 * if (res.get_return().getCodigo() == 90) {
					 * factura.getReceptor().setDireccion(
					 * factura.getReceptor().getDireccion() .substring(0, 69));
					 * } if (res.get_return().getCodigo() == 91) {
					 * factura.getReceptor().setNombre(
					 * factura.getReceptor().getNombre() .substring(0, 149)); };
					 * Respuesta r = new Respuesta(); r.setCodigo(-1);
					 * r.setDescripcion("Cancelado por el usuario."); return r;
					 */

				}
				System.out.println("codigo error"
						+ res.get_return().getCodigo());
			}
		}
		Respuesta r = new Respuesta();
		r.setCodigo(res.get_return().getCodigo());
		r.setDescripcion(res.get_return().getDescripcion());
		r.setDocumento(res.get_return().getDocumento());
		r.setReferencia(res.get_return().getReferencia());
		return r;

	}

	private void guardarXml(String serie, int numero, int tipodocumento,String nombrearchivo,String nombrezip) {
		// TODO Auto-generated method stub
		try {
			String xml = "";
			DBDriverPostgreSQL bd = new DBDriverPostgreSQL();
			bd.conectar();
			String consulta = "select xmladicional from factura where serie=? and numero=? and tipo=?";
			PreparedStatement ps = bd.prepareStatement(consulta);

			ps.setString(1, serie);
			ps.setInt(2, numero);
			ps.setInt(3, tipodocumento);

			System.out.println(consulta);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				xml = rs.getString(1);
			}

			ps.close();
			bd.desconectar();
			
		      try {
		          FileWriter fileRoute = new FileWriter(nombrearchivo+"cfeEmitido_xml_"+serie+numero+"_"+tipodocumento+".xml");

		          fileRoute.write(xml);
		          
		          fileRoute.close();
		          
		          File[] n = new File[1];
		          n[0]=new File(nombrearchivo+"cfeEmitido_xml_"+serie+numero+"_"+tipodocumento+".xml");
		          
		          
		          addFilesToExistingZip(new File(nombrezip+"CfesEmitidos.zip"),n);
		          
		          //addFileToZip(new File(nombrearchivo+"cfeEmitido_xml_"+serie+numero+"_"+tipodocumento+".xml"), new File(nombrezip+"CfesEmitidos.zip"));
		          
		          /*ZipOutputStream append = new ZipOutputStream(new FileOutputStream(nombrezip+"CfesEmitidos.zip"));

		          // now append some extra content
		          ZipEntry e = new ZipEntry("cfeEmitido_xml_"+serie+numero+"_"+tipodocumento+".xml");
		          BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);

		          System.out.println("append: " + e.getName());
		          append.putNextEntry(e);
		          //append.
		          append.write(xml.getBytes());
		          append.closeEntry();
		          int count;
		          while((count = origin.read(data, 0, BUFFER)) != -1) {
		             out.write(data, 0, count);
		          }
		          origin.close();

		          // close
		          append.close();*/
		          
		          /*Map<String, String> env = new HashMap<>(); 
		          env.put("createalse", "");
		          // locate file system by using the syntax 
		          // defined in java.net.JarURLConnection
		          URI uri = URI.create(nombrezip+"CfesEmitidos.zip");
		          
		         try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
		              Path externalTxtFile = Paths.get(nombrearchivo+"cfeEmitido_xml_"+serie+numero+"_"+tipodocumento+".xml");
		              Path pathInZipfile = zipfs.getPath("/SomeTextFile.txt");          
		              // copy a file into the zip file
		              Files.copy( externalTxtFile,pathInZipfile, 
		                      StandardCopyOption.REPLACE_EXISTING ); 
		          }*/ 
		        } catch (IOException ex) {
		          System.out.println(ex.getMessage());
		        }
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	public static void addFilesToExistingZip(File zipFile, File[] files) throws IOException {
	    // get a temp file
	    File tempFile = File.createTempFile(zipFile.getName(), null);
	    // delete it, otherwise you cannot rename your existing zip to it.
	    tempFile.delete();
	    boolean renameOk = zipFile.renameTo(tempFile);
	    if (!renameOk) {
	        throw new RuntimeException(
	                "could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
	    }
	    byte[] buf = new byte[1024];
	    ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
	    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
	    ZipEntry entry = zin.getNextEntry();
	    while (entry != null) {
	        String name = entry.getName();
	        boolean notInFiles = true;
	        for (File f : files) {
	            if (f.getName().equals(name)) {
	                notInFiles = false;
	                break;
	            }
	        }
	        if (notInFiles) { // Add ZIP entry to output stream.
	            out.putNextEntry(new ZipEntry(name)); // Transfer bytes from the ZIP file to the output file
	            int len;
	            while ((len = zin.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	        }
	        entry = zin.getNextEntry();
	    } // Close the streams
	    zin.close(); // Compress the files
	    for (int i = 0; i < files.length; i++) {
	        InputStream in = new FileInputStream(files[i]); // Add ZIP entry to output stream.
	        out.putNextEntry(new ZipEntry(files[i].getName())); // Transfer bytes from the file to the ZIP file
	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        } // Complete the entry
	        out.closeEntry();
	        in.close();
	    } // Complete the ZIP file
	    out.close();
	    tempFile.delete();
	}

	
	
	private void insertarRemito() throws Exception {
		// TODO Auto-generated method stub

		java.util.Date utilDate = factura.getFechaComprobante().getTime();
		java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
		System.out.println("utilDate:" + utilDate);
		System.out.println("sqlDate:" + sqlDate);

		// Date inActiveDate = null

		DBDriverPostgreSQL bd = new DBDriverPostgreSQL();
		bd.conectar();
		String consulta = "INSERT INTO remitos(fechaemitido, fechatransmision, monto, clienterazonsocial, clienterut, estado) VALUES (?,?,?,?,?,?);";
		PreparedStatement ps = bd.prepareStatement(consulta);

		// ps.setString(1, "nextval('remito_sequence')");
		ps.setDate(1, sqlDate);
		ps.setDate(2, sqlDate);
		ps.setDouble(3, factura.getTotalMontoIvaTasaBasica() * 1.22);
		System.out.println(factura.getTotalMontoNoGravado());
		ps.setString(4, factura.getReceptor().getNombre());
		ps.setString(5, factura.getReceptor().getDocumento().getDocumento());
		ps.setBoolean(6, false);

		System.out.println(consulta);

		ps.executeUpdate();

		ps.close();
		bd.desconectar();
	}

	// public Respuesta reimprimirFactura(String nrocompra) throws Exception{
	// ReimprimirFactura in = new ReimprimirFactura();
	// Properties propiedades =
	// Direccionador.getInstance().getArchivoConfiguracion();
	// FacturaElectronicaImplServiceStub fstub = new
	// FacturaElectronicaImplServiceStub(propiedades.getProperty("endpoint"));
	// in.setArg0(nrocompra);
	// in.setArg1(propiedades.getProperty("impresora"));
	// servicios.FacturaElectronicaImplServiceStub.ReimprimirFacturaResponse res
	// = fstub.reimprimirFactura(in );
	// return res.get_return();
	// }

	public PrintService getImpresora(String impresora) throws Exception {
		PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
		printServiceAttributeSet.add(new PrinterName(impresora, null));

		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(
				null, printServiceAttributeSet);
		if (printServices.length == 0) {
			throw new Exception("No se encontro la impresora " + impresora);
		}
		return printServices[0];
	}

	private void imprimirPDF(String documento, String impresora,
			String urlsumatra) throws Exception {
		PrintService p = this.getImpresora(impresora);
		if (p == null)
			throw new Exception("No se encontro la impresora");
		System.out.println("IMPRESORA: " + impresora);
		PrintService pr = this.getImpresora(impresora);
		if (pr == null)
			throw new Exception("No se encontro la impresora");
		String command = "\"" + urlsumatra
				+ "\" -exit-on-print -print-settings \"fit\" -print-to \""
				+ impresora + "\" \"" + documento + "\"";
		System.out.println(command);
		Process pp = Runtime.getRuntime().exec(command);
		getExitCMD(pp);
	}

	private void getExitCMD(Process p) {
		InputStream stdoutStream;
		try {
			stdoutStream = new BufferedInputStream(p.getInputStream());
			StringBuffer buffer = new StringBuffer();
			for (;;) {
				int c = stdoutStream.read();
				if (c == -1)
					break;
				buffer.append((char) c);

			}

			String outputText = buffer.toString();
			stdoutStream.close();
			System.out.println(outputText);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Respuesta mandarImprimir(String serie, Integer numero,
			Integer tipodocumento) throws Exception {
		Properties propiedades = Direccionador.getInstance()
				.getArchivoConfiguracion();
		String impresora = propiedades.getProperty("impresora");
		String template = propiedades.getProperty("template");
		String patharchivo = propiedades.getProperty("imprimibles");
		String urlsumatra = propiedades.getProperty("urlsumatra");

		FacturaElectronicaImplServiceStub_old fstub = new FacturaElectronicaImplServiceStub_old(
				propiedades.getProperty("endpoint"));
		GetImprimible gi = new GetImprimible();
		gi.setArg0(serie);
		gi.setArg1(numero);
		gi.setArg2(tipodocumento);
		gi.setArg3(template);

		servicios.FacturaElectronicaImplServiceStub_old.GetImprimibleResponse rr = fstub
				.getImprimible(gi);
		if (rr.get_return().getCodigo() == 0) {
			Respuesta r = new Respuesta();
			r.setCodigo(0);
			r.setDescripcion(rr.get_return().getDescripcion());
			DataHandler handler = rr.get_return().getImprimible();
			String stringToWrite = IOUtils.toString(handler.getInputStream(),
					"UTF-8");
			if (propiedades.getProperty("usaGuardadoPdf") != null) {
				if (propiedades.getProperty("usaGuardadoPdf").equalsIgnoreCase(
						"1")) {
					FacturaElectronicaImplServiceStub_old fstub2 = new FacturaElectronicaImplServiceStub_old(
							propiedades.getProperty("endpoint"));
					GetImprimible gi2 = new GetImprimible();
					gi2.setArg0(serie);
					gi2.setArg1(numero);
					gi2.setArg2(tipodocumento);
					gi2.setArg3("pdf");

					servicios.FacturaElectronicaImplServiceStub_old.GetImprimibleResponse rr2 = fstub2
							.getImprimible(gi2);

					DataHandler handler2 = rr2.get_return().getImprimible();
					String stringToWrite2 = IOUtils.toString(
							handler2.getInputStream(), "UTF-8");

					String archivo = patharchivo + numero + ".pdf";
					OutputStream os = new FileOutputStream(new File(archivo));
					handler2.writeTo(os);

					// this.imprimirPDF(archivo, impresora, urlsumatra);
					os.close();
					File fichero = new File(archivo);
					byte[] encoded = Files.readAllBytes(Paths.get(fichero
							.getAbsolutePath()));
					BASE64Encoder base64Encoder = new BASE64Encoder();
					String encode = base64Encoder.encode(encoded);
					guardarPdf(encode, serie, numero);
					if (!fichero.delete()) {
						log.log(Level.SEVERE, "El fichero " + archivo
								+ " no puede ser borrado");
					}
				}
			}

			if (rr.get_return().getTipo().equals("pdf")) {
				String archivo = patharchivo + numero + ".pdf";
				OutputStream os = new FileOutputStream(new File(archivo));
				handler.writeTo(os);

				this.imprimirPDF(archivo, impresora, urlsumatra);
				os.close();
				File fichero = new File(archivo);
				if (!fichero.delete()) {
					log.log(Level.SEVERE, "El fichero " + archivo
							+ " no puede ser borrado");
				}
			} else {
				String archivo = patharchivo + numero + ".txt";
				OutputStream os = new FileOutputStream(new File(archivo));
				handler.writeTo(os);
				Path path = Paths.get(archivo);

				byte[] by = Files.readAllBytes(path);

				DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
				Doc doc = new SimpleDoc(by, flavor, null);

				PrintService p = this.getImpresora(impresora);
				if (p == null)
					throw new Exception("No se encontro la impresora");

				PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
				DocPrintJob job = p.createPrintJob();
				job.print(doc, printRequestAttributeSet);
				os.close();
				File fichero = new File(archivo);

				if (!fichero.delete()) {
					log.log(Level.SEVERE, "El fichero " + archivo
							+ " no puede ser borrado");
				}
			}
			return r;
		} else {
			Respuesta r = new Respuesta();
			r.setCodigo(rr.get_return().getCodigo());
			r.setDescripcion(rr.get_return().getDescripcion());
			return r;
		}
	}

	private void guardarPdf(String encode, String serie, int numero)
			throws Exception {
		// TODO Auto-generated method stub

		DBDriverPostgreSQL bd = new DBDriverPostgreSQL();
		bd.conectar();
		String consulta = "INSERT INTO guardapdf(idguardapdf, numeroFactura, pdf) VALUES (default,?,?);";
		PreparedStatement ps = bd.prepareStatement(consulta);

		// ps.setString(1, "nextval('remito_sequence')");
		ps.setString(1, serie + numero);
		ps.setString(2, encode);

		System.out.println(consulta);

		ps.executeUpdate();

		ps.close();
		bd.desconectar();
	}

	public Respuesta reimprimirFactura(String serie, Integer numero,
			Integer tipodocumento) throws Exception {
		return this.mandarImprimir(serie, numero, tipodocumento);
	}

	public static void main(String[] args) throws Throwable {
		respaldo_OfInterfazSRFL i = new respaldo_OfInterfazSRFL();
		try {
			Respuesta res = null;
			if (args.length == 0) {
				log.log(Level.SEVERE, "No se enviaron parametros");
				JOptionPane.showMessageDialog(null,
						"No se enviaron parametros", "Factura Electronica",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			String nrocompra = args[0];
			if (nrocompra.equals("0")) {
				return;
			}
			Pattern p = Pattern.compile("([A-Z]{0,2})(\\d+)");
			Matcher m = p.matcher(args[0]);
			m.find();
			String serie = m.group(1);
			String snumero = m.group(2);

			int numero = Integer.parseInt(snumero);

			ArrayList<Object> ss = i.getId(serie, numero);
			if (ss == null) {
				log.log(Level.SEVERE, "No se encontro el folio " + args[0]);
				JOptionPane.showMessageDialog(null, "No se encontro el folio "
						+ args[0], "Factura Electronica",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			Integer id = (Integer) ss.get(0);
			String prefijo = (String) ss.get(1);
			String operacion = "";

			// Consulto si Viene segundo parametro una i ... NO IMPRIME
			boolean imprimir = true;
			if (args.length > 1)
				if (args[1].equalsIgnoreCase("i")) {
					imprimir = false;
				}
			InfoReferencia referencia = i.existeFactura(nrocompra);
			if (referencia != null) {
				// Se consulta si la cuenta en SR esta cancelada
				if (i.cancelada(id, prefijo)) {
					// Tendria que consultar antes si el existeFactura devuelve
					// una cuenta ya cancelada
					// para que la reimprima
					if (referencia.getTipoCFEReferencia() == 102
							|| referencia.getTipoCFEReferencia() == 112) {
						res = i.reimprimirFactura(
								referencia.getSerieCFEReferencia(),
								referencia.getNroCFEReferencia(),
								referencia.getTipoCFEReferencia());
						operacion = "Reimprimir Cancelar";
					} else {
						// Se cancela la factura
						res = i.cancelarFactura(id, referencia, nrocompra,
								prefijo);
						operacion = "Cancelar";
					}

				} else {
					res = i.reimprimirFactura(
							referencia.getSerieCFEReferencia(),
							referencia.getNroCFEReferencia(),
							referencia.getTipoCFEReferencia());
					operacion = "Reimprimir";
				}
			} else {
				res = i.generarFactura(id, nrocompra, prefijo, imprimir);
				operacion = "Generar";
			}
			System.out.println(res.getDescripcion() + "[" + operacion + "]"
					+ " " + args[0]);
			log.log(Level.INFO, res.getDescripcion() + "[" + operacion + "]"
					+ " " + args[0]);
			// JOptionPane.showMessageDialog(null, res.getDescripcion());
			if (res.getCodigo() != 0) {
				System.out.println("ok 5");
				log.log(Level.SEVERE, res.getDescripcion() + " " + args[0]);
				JOptionPane.showMessageDialog(null, res.getDescripcion(),
						"Factura Electronica", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
				System.out.println("ok 6");
			}
			System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.out.println("ok ");
			log.log(Level.SEVERE, e.getMessage() + " " + args[0], e);
			JOptionPane
					.showMessageDialog(
							null,
							e.getMessage() + " "
									+ e.getStackTrace()[0].getLineNumber(),
							"Factura Electronica ", JOptionPane.WARNING_MESSAGE);

			System.exit(0);

		}
	}

	public Respuesta cancelarFactura(int id, InfoReferencia referencia,
			String nrocompra, String prefijo) throws Throwable {
		Properties propiedades = Direccionador.getInstance()
				.getArchivoConfiguracion();

		// Espero 2 segundo para emitir la nota de credito para esperar a SR
		System.out.println("Previo al Sleep:"
				+ Calendar.getInstance().getTime());
		Thread.sleep(2 * 1000);
		System.out.println("Posterior al Sleep:"
				+ Calendar.getInstance().getTime());
		int ocultardetalle = Integer.parseInt(propiedades
				.getProperty("ocultardetalle"));
		Factura factura = this.getFactura(id, prefijo, ocultardetalle == 1,
				nrocompra);
		if (factura.getTipo() == 101)
			factura.setTipo(102);
		else if (factura.getTipo() != 181) {
			factura.setTipo(112);
		} else if (factura.getTipo() == 181) {
			Producto[] prds = factura.getProductos();
			for (Producto p : prds) {
				p.setIndicadorFacturacion(8);
			}
			factura.setProductos(prds);
		}
		InfoReferencia[] referencias = new InfoReferencia[1];
		referencias[0] = referencia;
		factura.setInfoReferencias(referencias);

		GenerarFactura gf = new GenerarFactura();
		gf.setArg0(factura);
		// gf.setArg1(propiedades.getProperty("impresora"));
		gf.setArg1(propiedades.getProperty(""));
		FacturaElectronicaImplServiceStub_old fstub = new FacturaElectronicaImplServiceStub_old(
				propiedades.getProperty("endpoint"));
		servicios.FacturaElectronicaImplServiceStub_old.GenerarFacturaResponse res = fstub
				.generarFactura(gf);
		if (res.get_return().getCodigo() == 0) {

			
			if (propiedades.getProperty("isa") != null && propiedades.getProperty("isa").equalsIgnoreCase("1")) {
					String nombrearchivo=propiedades.getProperty("rutaxmlisa");
					String nombrezip=propiedades.getProperty("rutazipisa");
					String serie = res.get_return().getDocumento()
							.getSerie();
					int numero = res.get_return().getDocumento()
							.getNumero();
					int tipodocumento = res.get_return().getTipoCFE();
					guardarXml(serie, numero, tipodocumento, nombrearchivo,nombrezip);
					
				
			}else{
				Respuesta r = this.mandarImprimir(res.get_return().getDocumento()
						.getSerie(), res.get_return().getDocumento().getNumero(),
						res.get_return().getTipoCFE());
				if (r.getCodigo() > 0) {
					return r;
				}
			}
		}
		Respuesta r = new Respuesta();
		r.setCodigo(res.get_return().getCodigo());
		r.setDescripcion(res.get_return().getDescripcion());
		r.setDocumento(res.get_return().getDocumento());
		r.setReferencia(res.get_return().getReferencia());

		return r;
	}

	/**
	 * Metodo creado para aplicar las infoReferencias cuando es una Facutra en
	 * negativo
	 * 
	 * @return
	 * @throws Exception
	 */
	public Factura cancelarFacturaXNotaCredito(Factura factura)
			throws Exception {

		// Properties propiedades =
		// Direccionador.getInstance().getArchivoConfiguracion();
		// Espero 2 segundo para emitir la nota de credito para esperar a SR
		Thread.sleep(2 * 1000);
		InfoReferencia nd = new InfoReferencia();
		Calendar c = Calendar.getInstance();
		nd.setFechaReferencia(c);
		nd.setNroLinea(1);
		nd.setReferenciaGlobal(true);
		nd.setRazonReferencia("NOTA DE CREDITO");

		InfoReferencia[] referencias = new InfoReferencia[1];
		referencias[0] = nd;
		factura.setInfoReferencias(referencias);

		return factura;
	}

	public boolean cancelada(int id, String prefijo) throws Exception {
		DBDriver bd = DBDriver.getInstance();
		bd.conectar();
		boolean salida = false;
		String consulta = "select cancelado from " + prefijo
				+ "cheques where folio=?";
		PreparedStatement ps = bd.prepareStatement(consulta);
		ps.setInt(1, id);
		if (TEST) {
			System.out.println(consulta + "::" + id);
		}

		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			salida = rs.getInt(1) != 0;
			rs.close();
			ps.close();
		} else {
			rs.close();
			ps.close();
			consulta = "select cancelado from cheques where folio=?";
			ps = bd.prepareStatement(consulta);
			ps.setInt(1, id);
			if (TEST) {
				System.out.println(consulta + "::" + id);
			}

			rs = ps.executeQuery();
			if (rs.next()) {
				salida = rs.getInt(1) != 0;
				rs.close();
				ps.close();
			} else {
				rs.close();
				ps.close();
				throw new Exception("No existe la factura en soft restaurant: "
						+ id);
			}
		}

		bd.desconectar();
		return salida;
	}

	public void mostrarError(String text) {
		JOptionPane.showMessageDialog(new JFrame(), text, "ERROR ",
				JOptionPane.CLOSED_OPTION);
		System.exit(0);
	}

	public void corregirRutInput(String rutNuevo) {
		String name = JOptionPane.showInputDialog(null,
				"Verifique y coloque el RUT o CEDULA nuevamente",
				"RUT o CEDULA INCORRECTO", JOptionPane.ERROR_MESSAGE);
		receptor.actualizarRut(name);

		// JOptionPane.showMessageDialog(null, "Hello " + name);
	}

	public void corregirDireccionInput(String rut) {
		// ANTES
		// String name =
		// JOptionPane.showInputDialog(null,"La direccion no puede superar los 70 caracteres, ingrese nuevamente.","DIRECCION INVALIDA",JOptionPane.ERROR_MESSAGE);
		String name = "";
		if (rut.length() > 68) {
			name = rut.substring(0, 68);
		}

		receptor.actualizarDireccion(name);
		// JOptionPane.showMessageDialog(null, "Hello " + name);
	}

	public InfoReferencia existeFactura(String nrocompra) throws Exception {
		// if(TEST){
		// return null;
		// }
		DBDriverPostgreSQL bd = new DBDriverPostgreSQL();
		bd.conectar();
		InfoReferencia salida = bd.existeFactura(nrocompra);
		bd.desconectar();
		return salida;
	}

}
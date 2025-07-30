package bd;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Direccionador  {

	/**
	 * 
	 */
	public static final String PATH  = "\\";
	Path currentRelativePath = Paths.get("");
	String s = currentRelativePath.toAbsolutePath().toString();
	//public static final String PATH  = "C:\\FLClienteSR\\";
	
	private static Direccionador instance = null;

	protected Direccionador() {
	 }
	 public static Direccionador getInstance() {
	    if(instance == null) {
	       instance = new Direccionador();
	    }
	    return instance;
	 }
	 
	 public Properties getArchivoConfiguracion(){
		 try {
			System.out.println("PATH: "+PATH);
			 File entrada = new File("configuracion.txt");
			 String ruta = entrada.getAbsolutePath();
			 System.out.println(ruta);
			FileInputStream f = new FileInputStream(entrada);
			Properties propiedades = new Properties();
			propiedades.load(f);
			f.close();
			return propiedades;
		} catch (FileNotFoundException e) {
			System.out.println("Error en tomando archivo de configuracion");
			e.printStackTrace();
			return null;		
		} catch (IOException e) {
			System.out.println("Error en tomando archivo de configuracion");
			e.printStackTrace();
			return null;
		}
			
	 }
	 
	 
}
	 

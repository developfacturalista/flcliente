package bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;


public class DBDriver {
    private Connection conexion = null;
    private  Statement stmt = null;
    private PreparedStatement ps=null;
    
	   private DBDriver() {
			super();
		}
		public static DBDriver getInstance() {
			return new DBDriver();
		}

		synchronized public void  conectar() throws Exception {
			Class.forName("net.sourceforge.jtds.jdbc.Driver");
			String user,password,url;
			Properties propiedades = Direccionador.getInstance().getArchivoConfiguracion();
			
			user = propiedades.getProperty("usuariosr");
			password =propiedades.getProperty( "clavesr");
			url = propiedades.getProperty( "servidorsr")+";user="+user+";password="+password+";TDS=7.0";
//System.out.println(url);
			conexion = DriverManager.getConnection(url);
            conexion.setAutoCommit(false);
			stmt = conexion.createStatement();
		//	java.sql.DatabaseMetaData dbmd = conexion.getMetaData(); //get MetaData to confirm connection
		}
		public Connection darConexion(){
			return conexion;
		}
		
		public void commit() throws SQLException{
			conexion.commit();
		}

		synchronized  public void desconectar() throws Exception {
				conexion.commit();
				stmt.close();
				conexion.close();
		}

		synchronized  public void abortar() throws Exception {
				conexion.rollback();
				stmt.close();
		}

		synchronized public void execute(String sql) throws Exception {
				stmt.executeUpdate(sql);
	            conexion.commit();
		}

		synchronized  public ResultSet query(String consultaSql) throws Exception {
			ResultSet rs = null;
			rs = stmt.executeQuery(consultaSql);
            conexion.commit();
			return rs;
		}

	    public PreparedStatement PreparedStatement(){
			return ps;
	    }
	    
		
		public java.sql.PreparedStatement prepareStatement(String sql){
			PreparedStatement ps=null;
			try {
				ps=this.conexion.prepareStatement(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return ps;
		}
		

	    
}
